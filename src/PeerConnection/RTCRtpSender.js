// @flow

import RTCMediaStreamTrack from '../MediaStream/RTCMediaStreamTrack';
import type { ValueTag } from './RTCPeerConnection';

export default class RTCRtpSender {

  id: String;

  //parameters: RTCRtpParameters;

  track: RTCMediaStreamTrack;

  _valueTag: ValueTag;

  constructor(info: Object) {
    this.id = info.id;
    this._valueTag = info.valueTag;
    this.track = new RTCMediaStreamTrack(info.track);
  }

}