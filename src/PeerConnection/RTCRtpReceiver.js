// @flow

import RTCMediaStreamTrack from '../MediaStream/RTCMediaStreamTrack';
import { RTCRtpParameters } from './RTCRtpParameters';
import type { ValueTag } from './RTCPeerConnection';

export default class RTCRtpReceiver {

  receiverId: String;
  parameters: RTCRtpParameters;
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