// @flow

import RTCMediaStreamTrack from '../MediaStream/RTCMediaStreamTrack';
import { RTCRtpParameters } from './RTCRtpParameters';
import type { ValueTag } from './RTCPeerConnection';

export default class RTCRtpSender {

  id: String;

  parameters: RTCRtpParameters;

  track: RTCMediaStreamTrack;

  _valueTag: ValueTag;

  constructor(info: Object) {
    this.id = info.id;
    this._valueTag = info.valueTag;
    this.parameters = new RTCRtpParameters(info.parameters);
    this.track = new RTCMediaStreamTrack(info.track);
  }

}