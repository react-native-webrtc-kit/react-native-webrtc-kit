// @flow

import { NativeModules } from 'react-native';
import RTCMediaStreamTrackEventTarget from './RTCMediaStreamTrackEventTarget';
import WebRTC from '../WebRTC';
import aspectRatioValue from './RTCMediaStreamConstraints';
import type { RTCAspectRatio } from './RTCMediaStreamConstraints';
import type { ValueTag } from '../PeerConnection/RTCPeerConnection';

/**
 * トラックの種別を表します。
 * 
 * - `'video'` - 映像
 * 
 * - `'audio'` - 音声
 * 
 * @typedef {string} RTCMediaStreamTrackKind
 * @since 1.1.0
 */
export type RTCMediaStreamTrackKind =
    | 'video'
    | 'audio';

/**
 * トラックの状態を表します。
 * 
 * - `'live'` - トラックへの入力が継続している状態を示します。
 * 出力の可否は `enabled` で変更できますが、出力を無効にしても状態は `'live'` のままです。
 * 
 * - `'ended'` - トラックへの入力が停止し、再開する可能性がない状態を示します。
 * 
 * @typedef {string} RTCMediaStreamTrackState
 */
export type RTCMediaStreamTrackState =
    | 'live'
    | 'ended';

export default class RTCMediaStreamTrack extends RTCMediaStreamTrackEventTarget {

    /**
     * トラック ID
     */
    id: string;

    /**
     * トラックの種別
     * 
     * - `'video'` - 映像トラック
     * - `'audio'` - 音声トラック
     * 
     * @since 1.1.0
     */
    kind: RTCMediaStreamTrackKind;

    /**
     * トラックの状態
     */
    readyState: RTCMediaStreamTrackState;

    /**
     * リモートのトラックであれば `true`
     */
    remote: boolean;

    _valueTag: ValueTag;
    _enabled: boolean;
    _aspectRatio: number | null;

    /**
     * トラックの出力の可否
     * 
     * @type {boolean}
     */
    get enabled(): boolean {
        return this._enabled;
    }

    /**
     * トラックの出力の可否
     * 
     * @type {boolean}
     */
    set enabled(enabled: boolean): void {
        if (this._enabled === enabled) {
            return;
        }
        this._enabled = enabled;
        WebRTC.trackSetEnabled(this._valueTag, enabled);
    }

    /**
     * アスペクト比
     * 
     * @type {number|null}
     * @since 1.1.0
     */
    get aspectRatio(): number | null {
        return this._aspectRatio;
    }

    /**
     * アスペクト比
     * 
     * @type {number|null}
     * @since 1.1.0
     */
    set aspectRatio(ratio: RTCAspectRatio | number | null): void {
        let value = aspectRatioValue(ratio);
        if (this._aspectRatio == value) {
            return;
        }
        this._aspectRatio = value;
        WebRTC.trackSetAspectRatio(this._valueTag, value);
    }

    /**
     * @private
     */
    constructor(info: Object) {
        super();
        this.id = info.id;
        this.kind = info.kind;
        this.readyState = info.readyState;
        this.remote = info.remote;
        this._valueTag = info.valueTag;
        this._enabled = info.enabled;
    }

    _close() {
        this._enabled = false;
        this.readyState = 'ended';
    }

}