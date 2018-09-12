// @flow

import { NativeModules } from 'react-native';

/**
 * ICE candidate を表すオブジェクトです。
 */
export default class RTCIceCandidate {

    /**
     * SDP メッセージ
     */
    candidate: string;

    /**
     * SDP メッセージ内で使われるメディアストリーム ID
     */
    sdpMid: string;

    /**
     * candidate に関連する、 SDP メッセージ内の m=行 の位置
     */
    sdpMLineIndex: number;

    /**
     * @package
     */
    constructor(info: Object) {
        this.candidate = info.candidate;
        this.sdpMLineIndex = info.sdpMLineIndex;
        this.sdpMid = info.sdpMid;
    }

    /**
     * @package
     */
    toJSON(): Object {
        return {
            candidate: this.candidate,
            sdpMLineIndex: this.sdpMLineIndex,
            sdpMid: this.sdpMid,
        };
    }

}
