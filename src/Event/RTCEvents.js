// @flow

import RTCMediaStream from '../MediaStream/RTCMediaStream';
import RTCMediaStreamTrack from '../MediaStream/RTCMediaStreamTrack';
import RTCIceCandidate from '../PeerConnection/RTCIceCandidate';
import RTCRtpReceiver from '../PeerConnection/RTCRtpReceiver';
import RTCRtpTransceiver from '../PeerConnection/RTCRtpTransceiver';

/**
 * 特にプロパティを持たない一般的なイベントを表します。
 */
export class RTCEvent {

    /**
     * イベントの種別
     */
    type: string;

    /**
     * @package
     */
    constructor(type: string, eventInitDict?: Object) {
        this.type = type.toString();
        Object.assign(this, eventInitDict);
    }

}

/**
 * トラックに関するイベントを表します。
 */
export class RTCMediaStreamTrackEvent {

    /**
     * イベントの種別
     */
    type: string;

    /**
     * トラック
     */
    track: RTCMediaStreamTrack;

    /**
     * レシーバー
     */
    receiver: RTCRtpReceiver;

    /**
     * トランシーバー
     */
    transceiver: RTCRtpTransceiver;

    /**
     * @package
     */
    constructor(type: string, eventInitDict?: Object) {
        this.type = type.toString();
        Object.assign(this, eventInitDict);
    }

}

/**
 * ICE candidate に関するイベントを表します。
 */
export class RTCIceCandidateEvent {

    /**
     * イベントの種別
     */
    type: string;

    /**
     * ICE candidate
     */
    candidate: RTCIceCandidate | null;

    /**
     * @package
     */
    constructor(type: string, eventInitDict?: Object) {
        this.type = type.toString();
        this.candidate = null;
        if (eventInitDict && eventInitDict.candidate) {
            this.candidate = eventInitDict.candidate;
        }
    }
}
