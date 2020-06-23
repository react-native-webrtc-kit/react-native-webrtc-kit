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
  receiverId: string;

  /**
   * パラメーター
   */
  parameters: RTCRtpParameters;

  /**
   * トラック
   */
  track: RTCMediaStreamTrack;

  /**
   * ストリーム ID のリスト
   * 
   * @since 2.0.0
   */
  streamIds: Array<string>;

  _valueTag: ValueTag;

  /**
   * @package
   */
  constructor(info: Object) {
    this._valueTag = info.valueTag;
    this.receiverId = info.receiverId;
    this.parameters = new RTCRtpParameters(this._valueTag, info.parameters);
    if (info.track) {
      this.track = new RTCMediaStreamTrack(info.track);
    }
    this.streamIds = info.streamIds ? info.streamIds : [];
  }

}