// @flow

import WebRTC from './src/WebRTC';

export { RTCEvent } from './src/Event/RTCEvents';
export { RTCMediaStreamEvent } from './src/Event/RTCEvents';
export { RTCMediaStreamTrackEvent } from './src/Event/RTCEvents';
export { RTCIceCandidateEvent } from './src/Event/RTCEvents';
export { default as RTCConfiguration } from './src/PeerConnection/RTCConfiguration';
export { default as RTCPeerConnection } from './src/PeerConnection/RTCPeerConnection';
export { default as RTCIceCandidate } from './src/PeerConnection/RTCIceCandidate';
export { default as RTCIceTransportPolicy } from './src/PeerConnection/RTCIceTransportPolicy';
export { default as RTCIceServer } from './src/PeerConnection/RTCIceServer';
export { default as RTCRtpSender } from './src/PeerConnection/RTCRtpSender';
export { default as RTCRtpReceiver } from './src/PeerConnection/RTCRtpReceiver';
export { default as RTCRtpTransceiver } from './src/PeerConnection/RTCRtpTransceiver';
export { RTCRtcpParameters } from './src/PeerConnection/RTCRtpParameters';
export { RTCRtpParameters } from './src/PeerConnection/RTCRtpParameters';
export { RTCRtpHeaderExtension } from './src/PeerConnection/RTCRtpParameters';
export { RTCRtpEncodingParameters } from './src/PeerConnection/RTCRtpParameters';
export { RTCRtpCodecParameters } from './src/PeerConnection/RTCRtpParameters';
export { default as RTCSessionDescription } from './src/PeerConnection/RTCSessionDescription';
export { default as RTCVideoView } from './src/VideoView/RTCVideoView';
export { default as RTCMediaStream } from './src/MediaStream/RTCMediaStream';
export { default as RTCMediaStreamTrack } from './src/MediaStream/RTCMediaStreamTrack';
export { default as RTCMediaStreamConstraints } from './src/MediaStream/RTCMediaStreamConstraints';
export { default as RTCLogger } from './src/Util/RTCLogger';
export { getUserMedia } from './src/MediaDevice/getUserMedia';
export { stopUserMedia } from './src/MediaDevice/getUserMedia';

export type { RTCPeerConnectionState } from './src/PeerConnection/RTCPeerConnection';
export type { RTCSignalingState } from './src/PeerConnection/RTCPeerConnection';
export type { RTCIceGatheringState } from './src/PeerConnection/RTCPeerConnection';
export type { RTCIceConnectionState } from './src/PeerConnection/RTCPeerConnection';
export type { RTCFacingMode } from './src/MediaStream/RTCMediaStreamConstraints';
export type { RTCSdpType } from './src/PeerConnection/RTCSessionDescription';
export type { RTCUserMedia } from './src/MediaDevice/getUserMedia';

// ネイティブモジュールに JS レイヤーのロードを知らせる。
// デバッグモードでのリロード時に古い接続の終了処理を行う。
// ネイティブ側で終了処理を行わないと、リロード前の接続が残ってしまう。
WebRTC.finishLoading();