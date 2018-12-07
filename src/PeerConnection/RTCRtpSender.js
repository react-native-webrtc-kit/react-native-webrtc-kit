// @flow

import RTCMediaStreamTrack from '../MediaStream/RTCMediaStreamTrack';
import { RTCRtpParameters } from './RTCRtpParameters';
import type { ValueTag } from './RTCPeerConnection';

/**
 * メディアデータを送信するトラックとその詳細情報です。
 * 
 * @since 1.1.0
 */
export default class RTCRtpSender {

  /**
   * センダー ID
   */
  id: String;

  /**
   * パラメーター
   */
  parameters: RTCRtpParameters;

  /**
   * センダーと関連するトラック
   */
  track: RTCMediaStreamTrack;

  _valueTag: ValueTag;

  /**
   * @ignore
   */
  constructor(info: Object) {
    this.id = info.id;
    this._valueTag = info.valueTag;
    this.parameters = new RTCRtpParameters(info.parameters);
    this.track = new RTCMediaStreamTrack(info.track);
  }

}