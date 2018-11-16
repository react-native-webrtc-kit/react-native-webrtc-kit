// @flow

import RTCRtpSender from './RTCRtpSender';
import RTCRtpReceiver from './RTCRtpReceiver';
import { nativeBoolean } from '../Util/RTCUtil';
import WebRTC from '../WebRTC';
import type { ValueTag } from './RTCPeerConnection';

/**
 * {@link RTCRtpTransceiver} のトラックの送受信の方向を表します。
 * 
 * - `'sendrecv'`
 * - `'sendonly'`
 * - `'recvonly'`
 * - `'inactive'`
 * 
 * @typedef {string} RTCRtpTransceiverDirection
 * 
 * @since 1.1.0
 */
export type RTCRtpTransceiverDirection =
  | 'sendrecv'
  | 'sendonly'
  | 'recvonly'
  | 'inactive'

/**
 * {@link RTCRtpSender} と {@link RTCRtpReceiver} のペアです。
 * 両方がデータを共有する場合に使われます。
 * 
 * @since 1.1.0
 */
export default class RTCRtpTransceiver {

  /**
   * メディア ID
   */
  mid: String;

  /**
   * センダー
   */
  sender: RTCRtpSender;

  /**
   * レシーバー
   */
  receiver: RTCRtpReceiver;

  /**
   * センダーとレシーバーが動作しており、データの送受信が可能であれば `true`
   */
  stopped: boolean;

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
  }

  stop() {
    WebRTC.transceiverStop(this._valueTag);
    this.stopped = true;
  }

  /**
   * トランシーバーのデータ送受信の方向を表します。
   * この値は `RTCPeerConnection.createOffer` 及び
   * `RTCPeerConnection.createAnswer` で参照されます。
   */
  direction(): Promise<RTCRtpTransceiverDirection> {
    return WebRTC.transceiverDirection(this._valueTag)
  }

  setDirection(value: RTCRtpTransceiverDirection) {
    WebRTC.transceiverSetDirection(this._valueTag, value)
  }

  currentDirection(): Promise<RTCRtpTransceiverDirection> {
    return WebRTC.transceiverCurrentDirection(this._valueTag);
  }

}