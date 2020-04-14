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
/** @private */
type RTCDataBuffer = {
  data: string;
  // バイナリデータかどうかのフラグ
  binary: boolean;
}

/**
 * RTCDataChannelInit のクラスです。
 * - `id`: number
 * - `ordered`: boolean
 * - `maxPacketLifeTime`: number
 * - `maxRetransmits`: number
 * - `protocol`: string
 * - `negotiated`: boolean
 * 
 * @typedef {Object} RTCDataChannelInit
 */
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

/**
 * DataChannel 接続を表すオブジェクトです。
 */
export default class RTCDataChannel extends RTCDataChannelEventTarget {

  /** @private */
  static nativeSendDataChannel(valueTag: ValueTag,
    buffer: RTCDataBuffer): Promise<void> {
    return WebRTCModule.dataChannelSend(buffer, valueTag);
  }

  /** @private */
  static nativeCloseDataChannel(valueTag: ValueTag): void {
    return WebRTCModule.dataChannelClose(valueTag)
  }

  // XXX(kdxu): 現在 Chrome / Safari は binaryType = 'blob' をサポートしていない
  // RNKit での対応も保留する

  /**
   * 送信できるデータのbinaryType を表します。
   * 現在は `'arraybuffer'` のみ対応しています。
   */
  binaryType: string = 'arraybuffer';
  /**
   * DataChannel の id を表します。
   * デフォルトは -1 です。
   * DataChannel の作成時にのみ指定可能です。
   */
  id: number = -1;
  /**
   * DataChannel のラベルを表します。
   * DataChannel の作成時にのみ指定可能です。
   */
  label: string;
  /**
   * DataChannel の maxPacketLifeTime を表します。
   * デフォルトは null です。
   * DataChannel の作成時にのみ指定可能です。
   */
  maxPacketLifeTime: number | null;
  /**
   * DataChannel の maxRetransmits を表します。
   * デフォルトは null です。
   * DataChannel の作成時にのみ指定可能です。
   */
  maxRetransmits: number | null;
  /**
   * DataChannel の negotiated フラグです。
   * デフォルトは false です。
   * DataChannel の作成時にのみ指定可能です。
   */
  negotiated: boolean = false;
  /**
   * DataChannel の ordered フラグです。
   * デフォルトは false です。
   * DataChannel の作成時にのみ指定可能です。
   */
  ordered: boolean = false;
  /**
   * DataChannel の現在の接続状態を表します。
   */
  readyState: RTCDataChannelState = 'connecting';
  /**
   * DataChannel の現在の bufferedAmount を表します。
   */
  bufferedAmount: number = 0;
  /**
   * DataChannel の user-defined に指定した protocol を表します。
   * デフォルトは `''` です。
   * DataChannel の作成時にのみ指定可能です。
   */
  protocol: string = '';

  _valueTag: ValueTag;

  // XXX(kdxu): libwebrtc objc で bufferedAmountLowThreshold に関連するプロパティは存在しない
  // RNKit での実装も保留となる
  // cf: https://chromium.googlesource.com/external/webrtc/+/refs/heads/master/sdk/objc/api/peerconnection/RTCDataChannel.mm#
  // bufferedAmountLowThreshold: number = 0;
  _nativeEventListeners: Array<any> = [];


  /**
   * RTCDataChannel のオブジェクトを生成します。
   * ユーザはここから直接 RTCDataChannel インスタンスを作成することはありません。
   * @listens {onopen} `RTCEvent`: `readyState` が `open` になると送信されます。
   * @listens {onclosing} `RTCEvent`: `readyState` が `close` になると送信されます。
   * @listens {onclose} `RTCEvent`: `readyState` が `close` になると送信されます。
   * @listens {onmessage} `RTCDataChannelMessageEvent`: リモートからデータを受信すると送信されます。
   */
  constructor(info: Object) {
    super();
    this._valueTag = info.valueTag;
    this.label = info.label;
    this.id = info.id;
    this.maxPacketLifeTime = info.maxPacketLifeTime;
    this.maxRetransmits = info.maxRetransmits;
    this.negotiated = nativeBoolean(info.negotiated);
    this.ordered = nativeBoolean(info.ordered);
    this.readyState = info.readyState;
    this.protocol = info.protocol;
    this.bufferedAmount = info.bufferedAmount;
    this._registerEventsFromNative();
  }

  /**
   * RTCDataChannel でデータを送信します。
   * @param {string | ArrayBuffer | ArrayBufferView} data 送信するデータ
   * @return {Promise<void>} 結果を表す Promise
   */
  send(data: string | ArrayBuffer | ArrayBufferView): Promise<void> {
    // XXX(kdxu): Chrome, Safari でサポートされていない Blob については実装を保留する
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
   * RTCDataChannel の接続を閉じます。
   */
  close(): void {
    // PeerConnection.close() と統一性をもたせるため、こちらは同期メソッドとする
    return RTCDataChannel.nativeCloseDataChannel(this._valueTag);
  }

  /**
   * ネイティブレイヤーからのコールバックイベントを登録します。
   * 発火するイベントは以下の通りです。
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

      // bufferedAmount が変更された際に発火する
      // bufferedAmount の数値は更新するが、イベントとしてユーザには通知を行わない
      DeviceEventEmitter.addListener('dataChannelOnChangeBufferedAmount', ev => {
        logger.log("# event: dataChannelOnChangeBufferedAmount =>", ev.data);
        if (ev.valueTag !== this._valueTag) {
          return;
        }
        if (ev.bufferedAmount) {
          this.bufferedAmount = ev.bufferedAmount;
        }
      }),
    ]
  }

  _unregisterEventsFromNative(): void {
    logger.log(`# DataChannel[${this._valueTag}]: unregister events from native`);
    this._nativeEventListeners.forEach(e => e.remove());
    this._nativeEventListeners = [];
  }
}
