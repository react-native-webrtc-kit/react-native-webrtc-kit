// @flow

import { NativeModules } from 'react-native';
import RTCConfiguration from './PeerConnection/RTCConfiguration';
import RTCIceCandidate from './PeerConnection/RTCIceCandidate';
import RTCMediaStreamTrack from './MediaStream/RTCMediaStreamTrack';
import RTCMediaStreamTrackEventTarget from './MediaStream/RTCMediaStreamTrackEventTarget';
import RTCMediaConstraints from './PeerConnection/RTCMediaConstraints';
import RTCMediaStreamConstraints from './MediaStream/RTCMediaStreamConstraints';
import RTCRtpSender from './PeerConnection/RTCRtpSender';
import RTCSessionDescription from './PeerConnection/RTCSessionDescription';
import type { ValueTag } from './PeerConnection/RTCPeerConnection';
import type { RTCRtpTransceiverDirection } from './PeerConnection/RTCRtpTransceiver';
import type { RTCAudioPort } from './MediaDevice/RTCAudioPort';

/** @private */
const { WebRTCModule } = NativeModules;

/**
 * @package
 * 
 * ネイティブモジュールのメソッドを呼び出すラッパークラスです。
 * ネイティブメソッドに型注釈をつけて、 Flow で型検査をできるようにします。
 */
export default class WebRTC {

  static finishLoading() {
    WebRTCModule.finishLoading();
  }

  static getUserMedia(constraints: RTCMediaStreamConstraints): Promise<Object> {
    return WebRTCModule.getUserMedia(constraints.toJSON());
  }

  static stopUserMedia() {
    WebRTCModule.stopUserMedia();
  }

  static peerConnectionInit(valueTag: ValueTag,
    configuration: RTCConfiguration,
    constraints: RTCMediaConstraints) {
    WebRTCModule.peerConnectionInit(
      configuration.toJSON(), constraints.toJSON(), valueTag);
  }

  static peerConnectionAddICECandidate(valueTag: ValueTag,
    candidate: RTCIceCandidate): Promise<void> {
    return WebRTCModule.peerConnectionAddICECandidate(candidate.toJSON(), valueTag);
  }

  static peerConnectionAddTrack(valueTag: ValueTag,
    trackValueTag: ValueTag,
    streamIds: Array<String>,
  ): Promise<Object> {
    return WebRTCModule.peerConnectionAddTrack(trackValueTag, streamIds, valueTag);
  }

  static peerConnectionRemoveTrack(valueTag: ValueTag, senderValueTag: ValueTag): Promise<void> {
    WebRTCModule.peerConnectionRemoveTrack(senderValueTag, valueTag);
  }

  static peerConnectionClose(valueTag: ValueTag) {
    WebRTCModule.peerConnectionClose(valueTag);
  }

  static peerConnectionCreateAnswer(valueTag: ValueTag,
    constraints: RTCMediaConstraints): Promise<RTCSessionDescription> {
    return WebRTCModule.peerConnectionCreateAnswer(valueTag, constraints.toJSON());
  }

  static peerConnectionCreateOffer(valueTag: ValueTag,
    constraints: RTCMediaConstraints): Promise<RTCSessionDescription> {
    return WebRTCModule.peerConnectionCreateOffer(valueTag, constraints.toJSON());
  }

  static peerConnectionRemoveStream(valueTag: ValueTag, streamValueTag: ValueTag) {
    WebRTCModule.peerConnectionRemoveStream(streamValueTag, valueTag);
  }

  static peerConnectionSetConfiguration(valueTag: ValueTag,
    configuration: RTCConfiguration) {
    WebRTCModule.peerConnectionSetConfiguration(configuration.toJSON(), valueTag);
  }

  static peerConnectionSetLocalDescription(valueTag: ValueTag,
    sdp: RTCSessionDescription): Promise<void> {
    return WebRTCModule.peerConnectionSetLocalDescription(sdp.toJSON(), valueTag);
  }

  static peerConnectionSetRemoteDescription(valueTag: ValueTag, sdp: RTCSessionDescription): Promise<void> {
    return WebRTCModule.peerConnectionSetRemoteDescription(sdp.toJSON(), valueTag);
  }

  static trackSetEnabled(valueTag: ValueTag, enabled: boolean) {
    WebRTCModule.trackSetEnabled(enabled, valueTag);
  }

  static trackSetAspectRatio(valueTag: ValueTag,
    aspectRatio: number) {
    WebRTCModule.trackSetAspectRatio(aspectRatio, valueTag);
  }

  static transceiverDirection(valueTag: ValueTag): Promise<RTCRtpTransceiverDirection> {
    return WebRTCModule.transceiverDirection(valueTag)
  }

  static transceiverSetDirection(valueTag: ValueTag, value: RTCRtpTransceiverDirection) {
    WebRTCModule.transceiverSetDirection(valueTag, value)
  }

  static transceiverCurrentDirection(valueTag: ValueTag): Promise<RTCRtpTransceiverDirection> {
    return WebRTCModule.transceiverCurrentDirection(valueTag)
  }

  static transceiverStop(valueTag: ValueTag) {
    WebRTCModule.transceiverStop(valueTag);
  }

  static rtpEncodingParametersSetActive(owner: ValueTag, ssrc: number | null, flag: boolean) {
    WebRTCModule.rtpEncodingParametersSetActive(flag, ssrc, owner);
  }

  static rtpEncodingParametersSetMaxBitrate(owner: ValueTag, ssrc: number | null, value: number | null) {
    if (value == null) {
      value = -1;
    }
    WebRTCModule.rtpEncodingParametersSetMaxBitrate(value, ssrc, owner);
  }

  static rtpEncodingParametersSetMinBitrate(owner: ValueTag, ssrc: number | null, value: number | null) {
    if (value == null) {
      value = -1;
    }
    WebRTCModule.rtpEncodingParametersSetMinBitrate(value, ssrc, owner);
  }

  static enableMetrics() {
    WebRTCModule.enableMetrics();
  }

  static getAndResetMetrics(): Promise<Array<RTCMetricsSampleInfo>> {
    return WebRTCModule.getAndResetMetrics();
  }

  static getAudioPort(): Promise<RTCAudioPort> {
    return WebRTCModule.getAudioPort();
  }

  static setAudioPort(port: RTCAudioPort): Promise<void> {
    return WebRTCModule.setAudioPort(port);
  }
}
