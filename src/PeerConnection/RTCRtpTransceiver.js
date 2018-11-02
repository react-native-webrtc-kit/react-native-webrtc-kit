// @flow

import RTCRtpSender from './RTCRtpSender';
import RTCRtpReceiver from './RTCRtpReceiver';
import { nativeBoolean } from '../Util/RTCUtil';
import WebRTC from '../WebRTC';
import type { ValueTag } from './RTCPeerConnection';

export type RTCRtpTransceiverDirection =
  | 'sendrecv'
  | 'sendonly'
  | 'recvonly'
  | 'inactive'

export default class RTCRtpTransceiver {

  mid: String;
  sender: RTCRtpSender;
  receiver: RTCRtpReceiver;
  stopped: boolean;
  direction: RTCRtpTransceiverDirection;

  // setter で動的にアクセスする
  get currentDirection(): RTCRtpTransceiverDirection | null {
    WebRTC.transceiverCurrentDirection(this._valueTag)
      .then(dir => { return dir; });
    return null;
  }

  _valueTag: ValueTag;

  /**
   * @package
   */
  constructor(info: Object) {
    this._valueTag = info.valueTag;
    this.mid = info.mid;
    this.sender = new RTCRtpSender(info.sender);
    this.receiver = new RTCRtpReceiver(info.receiver);
    this.stopped = nativeBoolean(info.stopped);
    this.direction = info.direction;
  }

  stop() {
    WebRTC.transceiverStop(this._valueTag);
    this.stopped = true;
  }

}