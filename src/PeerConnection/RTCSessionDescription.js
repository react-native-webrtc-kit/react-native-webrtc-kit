// @flow

import { NativeModules } from 'react-native';

/**
 * SDP メッセージの種別です。
 * 
 * - `'answer'`
 * - `'offer'`
 * - `'pranswer'`
 * - `'rollback'`
 * 
 * @typedef {string} RTCSdpType
 */
export type RTCSdpType =
    | 'answer'
    | 'offer'
    | 'pranswer'
    | 'rollback'

/**
 * SDP メッセージを表します。
 */
export default class RTCSessionDescription {

    /**
     * メッセージ
     */
    sdp: string;

    /**
     * SDP の種別
     */
    type: RTCSdpType;

    /**
     * SDP メッセージを生成します。
     * 
     * @param {RTCSdpType} type SDP 種別
     * @param {string} sdp メッセージ
     */
    constructor(type: RTCSdpType, sdp: string) {
        this.type = type;
        this.sdp = sdp;
    }

    /**
     * @package
     */
    toJSON(): Object {
        return {
            sdp: this.sdp,
            type: this.type
        };
    }

}
