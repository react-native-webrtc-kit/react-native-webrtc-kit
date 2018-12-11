// @flow

import { DeviceEventEmitter } from 'react-native';

import * as RTCUtil from '../Util/RTCUtil';
import RTCMediaStream from '../MediaStream/RTCMediaStream';
import RTCMediaStreamTrack from '../MediaStream/RTCMediaStreamTrack';
import { RTCEvent, RTCMediaStreamTrackEvent, RTCIceCandidateEvent } from '../Event/RTCEvents';
import RTCIceCandidate from './RTCIceCandidate';
import RTCPeerConnectionEventTarget from './RTCPeerConnectionEventTarget';
import RTCSessionDescription from './RTCSessionDescription';
import RTCConfiguration from './RTCConfiguration';
import RTCRtpSender from './RTCRtpSender';
import RTCRtpReceiver from './RTCRtpReceiver';
import RTCRtpTransceiver from './RTCRtpTransceiver';
import logger from '../Util/RTCLogger';
import RTCMediaConstraints from './RTCMediaConstraints';
import WebRTC from '../WebRTC';

/**
 * @package
 * 
 * ネイティブレイヤーのオブジェクトに関連付けられたユニークな文字列です。
 * JavaScript からネイティブのオブジェクトを操作するために使われます。
 * 例えばタグが `foo` であれば、
 * ネイティブレイヤーのオブジェクトのうち、タグが `foo` である
 * オブジェクトを指定して操作できます。
 * 
 * @typedef {string} ValueTag
 */
export type ValueTag = string;

/**
 * RTCPeerConnection の接続状態です。
 * 
 * - `'new'`
 * - `'connecting'`
 * - `'connected'`
 * - `'disconnected'`
 * - `'failed'`
 * - `'closed'`
 * 
 * @typedef {string} RTCPeerConnectionState
 */
export type RTCPeerConnectionState =
  | 'new'
  | 'connecting'
  | 'connected'
  | 'disconnected'
  | 'failed'
  | 'closed'

/**
 * RTCPeerConnection のシグナリング接続の状態です。
 * 
 * - `'stable'`
 * - `'have-local-offer'`
 * - `'have-remote-offer'`
 * - `'have-local-pranswer'`
 * - `'have-remote-pranswer'`
 * 
 * @typedef {string} RTCSignalingState
 */
export type RTCSignalingState =
  | 'stable'
  | 'have-local-offer'
  | 'have-remote-offer'
  | 'have-local-pranswer'
  | 'have-remote-pranswer'

/**
 * RTCPeerConnection の ICE ギャザリングの状態です。
 * 
 * - `'new'`
 * - `'gathering'`
 * - `'complete'`
 * 
 * @typedef {string} RTCIceGatheringState
 */
export type RTCIceGatheringState =
  | 'new'
  | 'gathering'
  | 'complete'

/**
 * RTCPeerConnection の ICE 接続の状態です。
 * 
 * - `'new'`
 * - `'checking'`
 * - `'connected'`
 * - `'completed'`
 * - `'failed'`
 * - `'disconnected'`
 * - `'closed'`
 * 
 * @typedef {string} RTCIceConnectionState
 */
export type RTCIceConnectionState =
  | 'new'
  | 'checking'
  | 'connected'
  | 'completed'
  | 'failed'
  | 'disconnected'
  | 'closed'

/**
 * RTCPeerConnection のタグを生成するために使われます。
 */
let nextPeerConnectionValueTag = 0;

/**
 * クライアントとの接続を表すオブジェクトです。
 */
export default class RTCPeerConnection extends RTCPeerConnectionEventTarget {

  /**
   * クライアントとの総合的な接続状態を表します。
   */
  connectionState: RTCPeerConnectionState = 'new';

  /**
   * ICE 接続の状態を表します。
   */
  iceConnectionState: RTCIceConnectionState = 'new';

  /**
   * ICE ギャザリングの状態を表します。
   */
  iceGatheringState: RTCIceGatheringState = 'new';

  /**
   * シグナリング接続の状態を表します。
   */
  signalingState: RTCSignalingState = 'stable';

  /**
   * ローカルマシンで生成した SDP です。
   */
  localDescription: RTCSessionDescription | null;

  /**
   * リモートから受信した SDP です。
   */
  remoteDescription: RTCSessionDescription | null;

  /**
   * センダーのリスト。
   * リストの並びは順不同です。
   * 
   * @since 1.1.0
   */
  senders: Array<RTCRtpSender> = [];

  /**
   * レシーバーのリスト。
   * リストの並びは順不同です。
   * 
   * @since 1.1.0
   */
  receivers: Array<RTCRtpReceiver> = [];

  /**
   * トランシーバーのリスト。
   * リストの並びは順不同です。
   * 
   * @since 1.1.0
   */
  transceivers: Array<RTCRtpTransceiver> = [];

  _valueTag: ValueTag;
  _nativeEventListeners: Array<any> = [];
  _constraints: RTCMediaConstraints;

  /**
   * オブジェクトを生成し、リモートのピアまたはサーバーに接続します。
   * 
   * @param {RTCConfiguration|null} [configuration=null] 設定
   * @param {RTCMediaConstraints|null} [constraints=null] メディアに関する制約
  * 
   * @listens {connectionstatechange} `RTCEvent`: `connectionState` が変更されると送信されます。
   * @listens {icecandidate} `RTCIceCandidateEvent`: ICE Candidate が生成されると送信されます。
   * @listens {iceconnectionstatechange} `RTCEvent`: `iceConnectionState` が変更されると送信されます。
   * @listens {icegatheringstatechange} `RTCIceCandidateEvent` | `RTCIceCandidateEvent`: `iceGatheringState` が変更されると送信されるイベント
   * @listens {negotiationneeded} `RTCEvent`: ネゴシエーションが必要になったときに送信されます。
   * @listens {signalingstatechange} `RTCEvent`: `signalingState` が変更されると送信されます。
   * @listens {track} `RTCEvent`: RTCPeerConnection にトラックが追加・削除されると送信されます。
   * @listens {addstream} このイベントは廃止されました。
   * @listens {removestream} このイベントは廃止されました。
   */
  constructor(configuration: RTCConfiguration | null = null,
    constraints: RTCMediaConstraints | null = null) {
    super();
    if (configuration == null) {
      configuration = new RTCConfiguration();
    }
    if (constraints == null) {
      constraints = new RTCMediaConstraints();
    }
    this._valueTag = (nextPeerConnectionValueTag++).toString();
    this._constraints = constraints;
    WebRTC.peerConnectionInit(this._valueTag,
      configuration,
      constraints);
    this._registerEventsFromNative();
  }

  // connectionState の状態を変更します。
  _beginConnect(): void {
    this._setConnectionState('connecting');
  }

  // peer connection の状態を判断します。
  _updateConnectionState(): void {
    logger.group("# update connection state");
    logger.log("# signaling state => ", this.signalingState);
    logger.log("# ice connection state => ", this.iceConnectionState);
    logger.log("# ice gathering state => ", this.iceGatheringState);
    if (this.signalingState == 'stable' &&
      this.iceConnectionState == 'connected' &&
      this.iceGatheringState == 'complete') {
      logger.log("# connection connected");
      this._setConnectionState('connected');
    }
    logger.groupEnd();
  }

  _setConnectionState(state: RTCPeerConnectionState): void {
    logger.log("# set connection state => ", state);
    this.connectionState = state;
    this.dispatchEvent(new RTCEvent('connectionstatechange'));
  }

  /**
   * 接続を解除します。
   * すべてのストリームの接続も閉じられます。
   */
  close(): void {
    logger.log("# connection close");
    this._setConnectionState('closed');
    this._finish();
  }

  _fail(): void {
    logger.log("# connection fail");
    this._setConnectionState('failed');
    this._finish();
  }

  _finish(): void {
    logger.log("# connection finish");
    this.senders.forEach(sender => this.removeTrack(sender));
    WebRTC.peerConnectionClose(this._valueTag);
  }

  /**
   * Answer SDP を生成します。
   * 
   * @param {RTCMediaConstraints} constraints 制約
   * @return {Promise<RTCSessionDescription>} 生成結果を示す Promise
   */
  createAnswer(constraints: Object): Promise<RTCSessionDescription> {
    logger.log("# create answer");
    return WebRTC.peerConnectionCreateAnswer(this._valueTag, constraints)
      .then(data => new RTCSessionDescription(data.type, data.sdp));
  }

  /**
   * Offer SDP を生成します。
   * 
   * @param {RTCMediaConstraints} constraints 制約
   * @return {Promise<RTCSessionDescription>} 生成結果を示す Promise
   */
  createOffer(constraints: RTCMediaConstraints): Promise<RTCSessionDescription> {
    logger.log("# create offer");
    return WebRTC.peerConnectionCreateOffer(this._valueTag, constraints)
      .then(data => new RTCSessionDescription(data.type, data.sdp));
  }

  /**
   * ICE candidate を追加します。
   * 
   * @param {RTCIceCandidate} candidate ICE candidate
   * @return {Promise<Void>} 結果を示す Promise
   */
  addIceCandidate(candidate: RTCIceCandidate): Promise<void> {
    return WebRTC.peerConnectionAddICECandidate(this._valueTag, candidate);
  }

  /**
   * @deprecated ストリームの操作は廃止されました。 senders を使用してください。
   * 
   * @version 1.1.0
   */
  getLocalStreams(): Array<RTCMediaStream> {
    throw new Error("getLocalStream() is deprecated")
  }

  /**
   * @deprecated ストリームの操作は廃止されました。 receivers を使用してください。
   * 
   * @version 1.1.0
   */
  getRemoteStreams(): Array<RTCMediaStream> {
    return this._remoteStreams.slice();
  }

  /**
   * @deprecated ストリームの操作は廃止されました。 addTrack() を使用してください。
   * 
   * @version 1.1.0
   */
  addStream(stream: RTCMediaStream): void {
    throw new Error("addStream() is deprecated");
  }

  /**
   * @deprecated ストリームの操作は廃止されました。 addTrack() を使用してください。
   * 
   * @version 1.1.0
   */
  addLocalStream(stream: RTCMediaStream): void {
    throw new Error("addLocalStream() is deprecated");
  }

  /**
   * @deprecated ストリームの操作は廃止されました。 removeTrack() を使用してください。
   * 
   * @version 1.1.0
   */
  removeLocalStream(stream: RTCMediaStream): void {
    throw new Error("removeLocalStream() is deprecated")
  }

  /**
   * 指定したストリームにトラックを追加します。
   * 
   * @param {RTCMediaStreamTrack} track 追加するトラック
   * @param {Array<String>} streamIds トラックを追加するストリーム ID
   * @return {Promise<RTCRtpSender>} 結果を表す Promise 。追加されたトラックを返す
   * 
   * @since 1.1.0
   */
  addTrack(track: RTCMediaStreamTrack, streamIds: Array<String>): Promise<RTCRtpSender> {
    var streamValueTags = [];
    return WebRTC.peerConnectionAddTrack(this._valueTag, track._valueTag, streamIds)
      .then((info) => {
        console.log("addTrack: sender => ", info);
        let sender = new RTCRtpSender(info);
        this.senders.push(sender);
        return sender;
      });
  }

  /**
   * 送信用のトラックを取り除きます。
   * 
   * @param {RTCRtpSender} sender 取り除くトラック
   * 
   * @since 1.1.0
   */
  removeTrack(sender: RTCRtpSender) {
    this.senders = this.senders.filter(
      e => e.id != sender.id);
    WebRTC.peerConnectionRemoveTrack(this._valueTag, sender._valueTag);
  }

  /**
   * 設定を反映します。
   * 
   * @param {RTCConfiguration} configuration 設定
   */
  setConfiguration(configuration: RTCConfiguration): void {
    logger.log("# set configuration");
    WebRTC.peerConnectionSetConfiguration(this._valueTag, configuration);
  }

  /**
   * ローカルの SDP を設定します。
   * 
   * @param {RTCSessionDescription} sessionDescription 設定する SDP
   * @return {Promise<void>} セットした結果を示す Promise
   */
  setLocalDescription(sessionDescription: RTCSessionDescription): Promise<void> {
    logger.log("# set local description");
    return WebRTC.peerConnectionSetLocalDescription(
      this._valueTag, sessionDescription).then(() => {
        this.localDescription = sessionDescription;
        return;
      });
  }

  /**
   * リモートの SDP を設定します。
   * 
   * @param {RTCSessionDescription} sessionDescription 設定する SDP
   * @return {Promise<void>} セットした結果を示す Promise
   */
  setRemoteDescription(sessionDescription: RTCSessionDescription): Promise<void> {
    logger.log("# set remote description");
    return WebRTC.peerConnectionSetRemoteDescription(
      this._valueTag, sessionDescription)
      .then(() => {
        this.remoteDescription = sessionDescription;
        return;
      });
  }

  _registerEventsFromNative(): void {
    logger.log("# register events from native");
    this._nativeEventListeners = [
      DeviceEventEmitter.addListener('peerConnectionShouldNegotiate', ev => {
        logger.log("# event: peerConnectionShouldNegotiate");
        if (ev.valueTag !== this._valueTag) {
          return;
        }
        this.dispatchEvent(new RTCEvent('negotiationneeded'));
      }),

      DeviceEventEmitter.addListener('peerConnectionIceConnectionChanged', ev => {
        logger.log("# event: peerConnectionIceConnectionChanged");
        if (ev.valueTag !== this._valueTag) {
          return;
        }
        this.iceConnectionState = ev.iceConnectionState;
        this._updateConnectionState();
        this.dispatchEvent(new RTCEvent('iceconnectionstatechange'));
        if (ev.iceConnectionState === 'closed') {
          // This PeerConnection is done, clean up event handlers.
          this._unregisterEventsFromNative();
        }
      }),

      DeviceEventEmitter.addListener('peerConnectionSignalingStateChanged', ev => {
        logger.log("# event: peerConnectionSignalingStateChanged");
        if (ev.valueTag !== this._valueTag) {
          return;
        }
        this.signalingState = ev.signalingState;
        this._updateConnectionState();
        this.dispatchEvent(new RTCEvent('signalingstatechange'));
      }),

      DeviceEventEmitter.addListener('peerConnectionAddedReceiver', ev => {
        logger.log("# event: peerConnectionAddedReceiver =>", ev.valueTag);
        if (ev.valueTag !== this._valueTag) {
          return;
        }

        let receiver = new RTCRtpReceiver(ev.receiver);
        this.receivers.push(receiver);
        this.dispatchEvent(new RTCMediaStreamTrackEvent('track',
          { track: receiver.track, receiver: receiver }));
      }),

      DeviceEventEmitter.addListener('peerConnectionRemovedReceiver', ev => {
        logger.log("# event: peerConnectionRemovedReceiver =>", ev.valueTag);
        if (ev.valueTag !== this._valueTag) {
          return;
        }

        let receiver = new RTCRtpReceiver(ev.receiver);
        this.receivers.push(receiver);
        this.dispatchEvent(new RTCMediaStreamTrackEvent('track',
          { track: receiver.track, receiver: receiver }));
      }),

      DeviceEventEmitter.addListener('peerConnectionStartTransceiver', ev => {
        logger.log("# event: peerConnectionStartTransceiver =>", ev.valueTag);
        if (ev.valueTag !== this._valueTag) {
          return;
        }

        let trans = new RTCRtpTransceiver(ev.transceiver);
        this.transceivers.push(trans);
        this.dispatchEvent(new RTCMediaStreamTrackEvent('track',
          { track: trans.sender.track, transceiver: trans }));
      }),

      DeviceEventEmitter.addListener('peerConnectionGotICECandidate', ev => {
        logger.log("# event: peerConnectionGotICECandidate =>", ev.valueTag);
        if (ev.valueTag !== this._valueTag) {
          return;
        }
        const candidate = new RTCIceCandidate(ev.candidate);
        this.dispatchEvent(new RTCIceCandidateEvent('icecandidate', { candidate }));
      }),

      DeviceEventEmitter.addListener('peerConnectionIceGatheringChanged', ev => {
        logger.log("# event: peerConnectionIceGatheringChanged=>", ev.valueTag);
        if (ev.valueTag !== this._valueTag) {
          return;
        }
        this.iceGatheringState = ev.iceGatheringState;
        this._updateConnectionState();
        if (this.iceGatheringState === 'complete') {
          this.dispatchEvent(new RTCIceCandidateEvent('icecandidate'));
        }
        this.dispatchEvent(new RTCEvent('icegatheringstatechange'));
      })
    ]
  }

  _unregisterEventsFromNative(): void {
    logger.log("# unregister events from native");
    this._nativeEventListeners.forEach(e => e.remove());
    this._nativeEventListeners = [];
  }

}
