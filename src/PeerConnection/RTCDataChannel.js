// @flow

import { DeviceEventEmitter } from 'react-native';
import { NativeModules } from 'react-native';
import RTCDataChannelEventTarget from "./RTCDataChannelEventTarget";
import { RTCEvent, RTCDataChannelMessageEvent } from '../Event/RTCEvents';
import { nativeBoolean } from '../Util/RTCUtil';
import type { ValueTag } from './RTCPeerConnection';
import logger from '../Util/RTCLogger';
import * as Base64 from 'base64-js';

/** @private */
const { WebRTCModule } = NativeModules;

// DataChannel で送受信するデータのクラスです。
type RTCDataBuffer = {
  data: string;
  // バイナリデータかどうかのフラグ
  binary: boolean;
}

// RTCDataChannelInit のクラスです。
type RTCDataChannelInit = {
  id?: number;
  ordered?: boolean;
  maxPacketLifeTime?: number;
  maxRetransmits?: number;
  protocol?: string;
  negotiated?: boolean;
  // maxRetransmitTime は duplicated。代わりに maxPacketLifeTime を用いる
  // cf: https://chromium.googlesource.com/external/webrtc/+/refs/heads/master/sdk/objc/api/peerconnection/RTCDataChannel.h#78
  // maxRetransmitTime: number;
}

/**
 * RTCDataChannel の接続状態です。
 * - `'connecting'`
 * - `'open'`
 * - `'closing'`
 * - `'closed'`
 * 
 * @typedef {string} RTCDataChannelState
 */
export type RTCDataChannelState =
  | 'connecting'
  | 'open'
  | 'closing'
  | 'closed';

// RTCDataChannel のクラスです。
export default class RTCDataChannel extends RTCDataChannelEventTarget {
  _valueTag: ValueTag;
  // XXX(kdxu): 現在 Chrome / Safari は binaryType = 'blob' をサポートしていない
  // RNKit での対応も保留する
  binaryType: string = 'arraybuffer';
  id: number = -1;
  label: string;
  maxPacketLifeTime: number;
  maxRetransmits: number;
  negotiated: boolean = false;
  ordered: boolean = false;
  readyState: RTCDataChannelState = 'connecting';
  bufferedAmount: number = 0;
  protocol: string = '';
  _nativeEventListeners: Array<any> = [];

  /** @private */
  static nativeSendDataChannel(valueTag: ValueTag,
    buffer: RTCDataBuffer): Promise<void> {
    return WebRTCModule.dataChannelSend(buffer, valueTag);
  }

  /** @private */
  static nativeCloseDataChannel(valueTag: ValueTag): Promise<void> {
    return WebRTCModule.dataChannelClose(valueTag)
  }

  constructor(info: Object) {
    super();
    this._valueTag = info.valueTag;
    this.label = info.label;
    this.maxPacketLifeTime = info.maxPacketLifeTime;
    this.maxRetransmits = info.maxRetransmits;
    this.negotiated = nativeBoolean(info.negotiated);
    this.ordered = nativeBoolean(info.ordered);
    this.readyState = info.readyState;
    this.id = info.id;
    this.bufferedAmount = info.bufferedAmount;
    this._registerEventsFromNative();
  }

  /**
   * RTCDataChannel でデータを送信します。
   * @param {string | ArrayBuffer | ArrayBufferView} data 送信するデータ
   */
  send(data: string | ArrayBuffer | ArrayBufferView): Promise<void> {
    // XXX(kdxu): Chrome, Safari でサポートされていない Blob については一旦実装を保留する
    if (typeof data === 'Blob') {
      logger.warn('Blob support not implemented');
      return;
    }
    if (typeof data === 'string') {
      // string の場合は特に変換処理をせずに native にわたす
      return RTCDataChannel.nativeSendDataChannel(this._valueTag, { data: data, binary: false });
    }
    // 以下は ArrayBuffer | ArrayBufferView への対応
    let byteArray;
    if (ArrayBuffer.isView(data)) {
      // ArrayBufferView が渡された場合は buffer, byteoffset, bytelength から Uint8Array を構築
      byteArray = new Uint8Array(data.buffer, data.byteOffset, data.byteLength);
    }
    if (data instanceof ArrayBuffer) {
      // ArrayBuffer が渡された場合、そのまま byteArray に変換する
      byteArray = new Uint8Array(data);
    }
    // バイナリデータは一旦 base64 エンコードしてネイティブレイヤーに渡す
    return RTCDataChannel.nativeSendDataChannel(this._valueTag, { data: Base64.fromByteArray(byteArray), binary: true });
  }

  /**
   * RTCDataChannel を閉じます。
   */
  close(): Promise<void> {
    return RTCDataChannel.nativeCloseDataChannel(this._valueTag);
  }

  /**
   * ネイティブレイヤーからのコールバックイベントを登録します。
   * 受け取るイベントは以下の通りです。
    - 'open'
    - 'message'
    - 'bufferedamountlow'
    - 'close'
    - 'closing'
   */
  _registerEventsFromNative(): void {
    logger.log(`# DataChannel[${this._valueTag}]: register events from native`);
    this._nativeEventListeners = [
      DeviceEventEmitter.addListener('dataChannelStateChanged', ev => {
        logger.log("# event: dataChannelStateChanged =>", ev.readyState);
        if (ev.valueTag !== this._valueTag) {
          return;
        }
        this.readyState = ev.readyState;
        switch (ev.readyState) {
          case 'connecting':
            // connecting は initial state なので、onstatechange でここに来ることは無いはず
            // cf: https://www.w3.org/TR/webrtc/#creating-a-data-channel
            break;
          case 'open':
            this.dispatchEvent(new RTCEvent('open'));
            break;
          case 'closing':
            this.dispatchEvent(new RTCEvent('closing'));
            break;
          case 'closed':
            this.dispatchEvent(new RTCEvent('close'));
            // This DataChannel is done, clean up event handlers.
            this._unregisterEventsFromNative();
            break;
          default:
            // 予測していない state type が来た場合警告を出す
            logger.warn('# event: dataChannelStateChanged, invalid state=>', ev.readyState);
        }
      }),
      DeviceEventEmitter.addListener('dataChannelOnMessage', ev => {
        logger.log("# event: dataChannelOnMessage =>", ev.data);
        if (ev.valueTag !== this._valueTag) {
          return;
        }
        if (ev.binary === true) {
          // バイナリデータの場合、ネイティブレイヤーで base64 encode されたものが来ているはずなので
          // base64 decode を行って受け取る
          const byteArray = Base64.toByteArray(ev.data);
          return this.dispatchEvent(new RTCDataChannelMessageEvent('message', byteArray.buffer, ev.binary));
        }
        return this.dispatchEvent(new RTCDataChannelMessageEvent('message', ev.data, ev.binary));
      }),
      DeviceEventEmitter.addListener('dataChannelOnChangeBufferedAmount', ev => {
        logger.log("# event: dataChannelOnChangeBufferedAmount =>", ev.data);
        if (ev.valueTag !== this._valueTag) {
          return;
        }
        this.dispatchEvent(new RTCEvent('bufferedamountlow', ev));
      }),
    ]
  }

  _unregisterEventsFromNative(): void {
    logger.log(`# DataChannel[${this._valueTag}]: unregister events from native`);
    this._nativeEventListeners.forEach(e => e.remove());
    this._nativeEventListeners = [];
  }
}
