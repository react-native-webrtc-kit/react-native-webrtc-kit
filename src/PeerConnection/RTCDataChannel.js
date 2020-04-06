// @flow

import { DeviceEventEmitter } from 'react-native';
import { NativeModules } from 'react-native';
import RTCDataChannelEventTarget from "./RTCDataChannelEventTarget";
import { RTCEvent, RTCDataChannelMessageEvent } from '../Event/RTCEvents';
import { nativeBoolean } from '../Util/RTCUtil';
import type { ValueTag } from './RTCPeerConnection';
import logger from '../Util/RTCLogger';

/** @private */
const { WebRTCModule } = NativeModules;

export type RTCDataBuffer = {
  data: string;
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
   * @param {RTCDataBuffer} data 送信するデータ
   */
  send(data: RTCDataBuffer): Promise<void> {
    return RTCDataChannel.nativeSendDataChannel(this._valueTag, data);
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
        // TODO(kdxu): バイナリデータの場合ここで Array Buffer にして event に渡す？
        this.dispatchEvent(new RTCDataChannelMessageEvent('message', ev.data, ev.binary));
      }),
      DeviceEventEmitter.addListener('dataChannelOnChangeBufferedAmount', ev => {
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
