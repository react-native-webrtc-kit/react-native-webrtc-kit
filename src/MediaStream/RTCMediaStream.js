// @flow

import { NativeModules } from 'react-native';
import EventTarget from 'event-target-shim';

import RTCMediaStreamEventTarget from './RTCMediaStreamEventTarget';
import RTCMediaStreamTrack from './RTCMediaStreamTrack';
import type { ValueTag } from '../PeerConnection/RTCPeerConnection';

/**
 * @deprecated ストリームの操作は廃止されました。
 * 
 * @version 1.1.0
 */
export default class RTCMediaStream extends RTCMediaStreamEventTarget {

  /**
   * @deprecated
   * @version 1.1.0
   */
  id: string;

  /**
   * @deprecated
   * @version 1.1.0
   */
  active: boolean = true;

  /**
   * @deprecated
   * @version 1.1.0
   */
  getTracks(): Array<RTCMediaStreamTrack> {
    throw new Error("getTracks() is deprecated");
  }

  /**
   * @deprecated
   * @version 1.1.0
   */
  getVideoTracks(): Array<RTCMediaStreamTrack> {
    throw new Error("getVideoTracks() is deprecated");
  }

  /**
   * @deprecated
   * @version 1.1.0
   */
  getAudioTracks(): Array<RTCMediaStreamTrack> {
    throw new Error("getAudioTracks() is deprecated");
  }

}
