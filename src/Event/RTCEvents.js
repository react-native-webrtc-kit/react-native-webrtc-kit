// @flow

import RTCMediaStreamTrack from '../MediaStream/RTCMediaStreamTrack';
import RTCIceCandidate from '../PeerConnection/RTCIceCandidate';
import RTCRtpReceiver from '../PeerConnection/RTCRtpReceiver';
import RTCRtpTransceiver from '../PeerConnection/RTCRtpTransceiver';
// TODO(kdxu): ここでビルド時に `cycle requirements` の警告が出ている
// RTCDataChannel で RTCEvent モジュールをインポートし、このファイルで RTCDataChannel をインポートしているため。
// RTCDataChannel をインポートしている RTCDataChannelEvent を別ファイルに定義すれば解消されるが、設計上良くなさそう
// ただし重大な問題ではないので、別途対応手段を考える
import RTCDataChannel from '../PeerConnection/RTCDataChannel';

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
    /**
     * 受信するデータ
     */
    data: string | ArrayBuffer | null;
    /**
     * データがバイナリかどうかのフラグ
     */
    binary: boolean;

    /**
     * @package
     */
    constructor(type: string, data: string | ArrayBuffer | null, binary: boolean) {
        this.type = type.toString();
        this.data = data;
        this.binary = binary;
    }
}
