import RTCDataChannelEventTarget from "./RTCDataChannelEventTarget";

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
    _valueTag: string;
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

    constructor(label: string, options: RTCDataChannelInit | null = null) {
        super();
        this.label = label;
        if (options) {
            this.id = options.id;
            this.maxPacketLifeTime = options.maxPacketLifeTime;
            this.maxRetransmits = options.maxRetransmits;
            this.ordered = options.ordered;
            this.protocol = options.protocol || '';
        }
    }

    // TODO(kdxu) 必要なメソッドを実装する
    send() {
     // WebRTCModule.nativeSendDataChannel()
    }

    close() {
     // WebRTCModule.nativeCloseDataChannel()
    }

  _registerEventsFromNative(): void {
    logger.log(`# DataChannel[${this._valueTag}]: register events from native`);
    this._nativeEventListeners = [
      DeviceEventEmitter.addListener('dataChannelOpen', ev => {
        if (ev.valueTag !== this._valueTag) {
          return;
        }
        this.dispatchEvent(new RTCEvent('open'));
      }),

      DeviceEventEmitter.addListener('dataChannelStateChanged', ev => {
        logger.log("# event: dataChannelStateChanged =>", ev.readyState);
        if (ev.valueTag !== this._valueTag) {
          return;
        }
        this.readyState = ev.readyState;
        this.dispatchEvent(new RTCEvent('statechange'));
        if (ev.connectionState === 'closed') {
          // This DataChannel is done, clean up event handlers.
          this._unregisterEventsFromNative();
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