// @flow

/**
 * 使用するカメラの位置を表します。
 * 
 * - `'user'` - 前面のカメラ
 * - `'environment'` - 背面のカメラ
 *
 * @typedef {string} RTCFacingMode
 */
export type RTCFacingMode =
    | 'user'
    | 'environment'

/**
 * 映像に関する制約です。
 */
export class RTCMediaStreamVideoConstraints {

    /**
     * 使用するカメラの位置
     */
    facingMode: RTCFacingMode | null;

    /**
     * 映像の幅
     */
    width: number | null;

    /**
     * 映像の高さ
     */
    height: number | null;

    /**
     * 映像のフレームレート
     */
    frameRate: number | null;

}

/**
 * 音声に関する制約です。現在、特にプロパティはありません。
 */
export class RTCMediaStreamAudioConstraints {

}

const DEFAULT_VIDEO_CONSTRAINTS = new RTCMediaStreamVideoConstraints();
DEFAULT_VIDEO_CONSTRAINTS.facingMode = "user";
DEFAULT_VIDEO_CONSTRAINTS.width = 1280;
DEFAULT_VIDEO_CONSTRAINTS.height = 720;
DEFAULT_VIDEO_CONSTRAINTS.frameRate = 30;

const DEFAULT_AUDIO_CONSTRAINTS = true;

/**
 * メディアストリームに関する制約です。
 */
export default class RTCMediaStreamConstraints {

    /**
     * 映像に関する制約
     */
    video: boolean | RTCMediaStreamVideoConstraints | null;

    /**
     * 音声に関する制約
     */
    audio: boolean | RTCMediaStreamAudioConstraints | null;

    /**
     * オブジェクトを生成します。
     * 
     * デフォルトの制約は以下の通りです。
     * 
     * - `video.facingMode = 'user'`
     * - `video.width = 1280`
     * - `video.height = 720`
     * - `video.frameRate = 30`
     * - `audio = true`
     * 
     */
    constructor() {
        this.video = DEFAULT_VIDEO_CONSTRAINTS;
        this.audio = DEFAULT_AUDIO_CONSTRAINTS;
    }

    /**
     * @package
     */
    toJSON(): Object {
        var json = {};

        var video = this.video;
        if (video === true) {
            video = DEFAULT_VIDEO_CONSTRAINTS;
        }
        if (video) {
            json.video = {
                facingMode: video.facingMode,
                width: video.width,
                height: video.height,
                frameRate: video.frameRate
            };
        }
        var audio = this.audio;
        if(audio === true || audio === null){
          json.audio = DEFAULT_AUDIO_CONSTRAINTS;
        } else {
          json.audio = audio;
        }

        return json;
    }

}
