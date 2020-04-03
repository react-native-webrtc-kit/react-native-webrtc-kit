import RTCDataChannelEventTarget from "./RTCDataChannelEventTarget";
import { RTCEvent, RTCDataChannelMessageEvent } from '../Event/RTCEvents';
import { nativeBoolean } from '../Util/RTCUtil';
import type { ValueTag } from './RTCPeerConnection';

// RTCDataChannelInit のクラスです。
type RTCDataChannelInit {
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
    maxPacketLifeTime: number | null = null;
    maxRetransmits: number | null = null;
    negotiated: boolean = false;
    ordered: boolean = false;
    readyState: RTCDataChannelState = 'connecting';
    bufferedAmount: number = 0;
    bufferedAmountLowThreshold: number = 0;
    protocol: string = '';

    constructor(info: Object) {
        super();
        this._valueTag = info.valueTag;
        this.label = info.label;
        this.maxPacketLifeTime = info.maxPacketLifeTime;
        this.maxRetransmits = info.maxRetransmits;
        this.negotiated = nativeBoolean(info.negotiated);
        this.ordered = nativeBoolean(info.ordered);
        this.readyState = info.readyState;
        this.bufferedAmount = info.bufferedAmount;
        this.bufferedAmountLowThreshold = info.bufferedAmountLowThreshold;
        this._registerEventsFromNative();
    }

    // TODO(kdxu) 必要なメソッドを実装する
    send() {
     // WebRTCModule.nativeSendDataChannel()
    }

    close() {
     // WebRTCModule.nativeCloseDataChannel()
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
        this.dispatchEvent(new RTCDataChannelMessageEvent(ev));
      }),
      DeviceEventEmitter.addListener('dataChannelOnChangeBufferedAmount', ev => {
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