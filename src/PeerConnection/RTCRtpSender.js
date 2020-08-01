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
  id: string;

  /**
   * パラメーター
   */
  parameters: RTCRtpParameters;

  /**
   * センダーと関連するトラック
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
   * @ignore
   */
  constructor(info: Object) {
    this.id = info.id;
    this._valueTag = info.valueTag;
    this.parameters = new RTCRtpParameters(info.valueTag, info.parameters);
    if (info.track) {
      this.track = new RTCMediaStreamTrack(info.track);
    }
    this.streamIds = info.streamIds ? info.streamIds : [];
  }

}