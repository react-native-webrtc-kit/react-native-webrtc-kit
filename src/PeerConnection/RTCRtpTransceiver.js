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
   * センダーとレシーバーによるデータの送受信が停止されていれば `true`
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

  /**
   * センダーとレシーバーによるデータの送受信を停止します。
   * 一旦停止すると、送受信を再開することはできません。
   */
  stop() {
    WebRTC.transceiverStop(this._valueTag);
    this.stopped = true;
  }

  /**
   * トランシーバーのデータ送受信の方向を表します。
   * この値は `RTCPeerConnection.createOffer` 及び
   * `RTCPeerConnection.createAnswer` の次回実行時に参照されます。
   */
  direction(): Promise<RTCRtpTransceiverDirection> {
    return WebRTC.transceiverDirection(this._valueTag)
  }

  /**
   * トランシーバーのデータ送受信の方向を指定します。
   */
  setDirection(value: RTCRtpTransceiverDirection) {
    WebRTC.transceiverSetDirection(this._valueTag, value)
  }

  /**
   * トランシーバーのデータ送受信の方向を指定します。
   * このメソッドの実行時点で使用されている値を返します。
   */
  currentDirection(): Promise<RTCRtpTransceiverDirection> {
    return WebRTC.transceiverCurrentDirection(this._valueTag);
  }

}