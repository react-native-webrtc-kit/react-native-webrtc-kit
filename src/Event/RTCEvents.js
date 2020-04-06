// @flow

import RTCMediaStream from '../MediaStream/RTCMediaStream';
import RTCMediaStreamTrack from '../MediaStream/RTCMediaStreamTrack';
import RTCIceCandidate from '../PeerConnection/RTCIceCandidate';
import RTCRtpReceiver from '../PeerConnection/RTCRtpReceiver';
import RTCRtpTransceiver from '../PeerConnection/RTCRtpTransceiver';
import RTCDataChannel, { RTCDataBuffer } from '../PeerConnection/RTCDataChannel';

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
     * 
     * @since 1.1.0
     */
    receiver: RTCRtpReceiver | null;

    /**
     * トランシーバー
     * 
     * @since 1.1.0
     */
    transceiver: RTCRtpTransceiver | null;

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

/**
 * RTCDataChannel に関するイベントを表します。
 * RTCPeerConnection.ondatachannel にて利用します。
 */
export class RTCDataChannelEvent {

    /**
     * イベントの種別
     */
    type: string;
    /**
     * datachannel
     */
    channel: RTCDataChannel;

    /**
     * @package
     */
    constructor(type: string, channel: RTCDataChannel) {
        this.type = type.toString();
        this.channel = channel;
    }
}

/**
 * RTCDataChannel.onmessage に関するイベントを表します。
 * cf: https://developer.mozilla.org/ja/docs/Web/API/MessageEvent
 */
export class RTCDataChannelMessageEvent {
    /**
     * イベントの種別
     */
    type: string;
    data: string | null;
    binary: boolean;

    /**
     * @package
     */
    constructor(type: string, data: string, binary: boolean) {
        this.type = type.toString();
        this.data = data.toString();
        this.binary = binary;
    }
}