// @flow

import RTCMediaStreamTrack from '../MediaStream/RTCMediaStreamTrack';
import { RTCRtpParameters } from './RTCRtpParameters';
import type { ValueTag } from './RTCPeerConnection';

/**
 * メディアデータを受信するトラックとその詳細情報です。
 * 
 * @since 1.1.0
 */
export default class RTCRtpReceiver {

  /**
   * レシーバー ID
   */
  receiverId: String;

  /**
   * パラメーター
   */
  parameters: RTCRtpParameters;

  /**
   * トラック
   */
  track: RTCMediaStreamTrack;

  _valueTag: ValueTag;

  /**
   * @package
   */
  constructor(info: Object) {
    this.receiverId = info.receiverId;
    this.parameters = new RTCRtpParameters(info.parameters);
    this.track = new RTCMediaStreamTrack(info.track);
  }

}