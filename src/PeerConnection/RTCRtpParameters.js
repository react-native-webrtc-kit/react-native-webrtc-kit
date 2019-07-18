// @flow

import { NativeModules } from 'react-native';
import type { ValueTag } from './RTCPeerConnection';

/** @private */
const { WebRTCModule } = NativeModules;

/**
 * RTCP に関するパラメーターです。
 * 
 * @since 1.1.0
 */
export class RTCRtcpParameters {

  /**
   * CNAME (Canonical Name)
   */
  cname: String;

  /**
   * サイズが減らされていれば `true`
   */
  reducedSize: boolean;

  _owner: ValueTag;

  /**
   * @package
   */
  constructor(owner: ValueTag, info: Object) {
    this._owner = owner;
    this.cname = info.cname;
    this.reducedSize = info.reducedSize;
  }

}

/**
 * RTP 拡張ヘッダーに関するパラメーターです。
 * 
 * @since 1.1.0
 */
export class RTCRtpHeaderExtensionParameters {

  /**
   * URI
   */
  uri: String;

  /**
   * 拡張ヘッダーの ID
   */
  id: number;

  /**
   * 拡張ヘッダーが暗号化されていれば `true`
   */
  encrypted: boolean;

  _owner: ValueTag;

  /**
   * @package
   */
  constructor(owner: ValueTag, info: Object) {
    this._owner = owner;
    this.uri = info.uri;
    this.id = info.id;
    this.encrypted = info.encrypted;
  }

}

/**
 * RTP エンコーディングに関するパラメーターです。
 * このエンコーディングの設定はメディアデータの送信時に使われます。
 * 
 * @since 1.1.0
 */
export class RTCRtpEncodingParameters {

  /** @private */
  static nativeSetActive(owner: ValueTag, ssrc: number | null, flag: boolean) {
    WebRTCModule.rtpEncodingParametersSetActive(flag, ssrc, owner);
  }

  /** @private */
  static nativeSetMaxBitrate(owner: ValueTag, ssrc: number | null, value: number | null) {
    if (value == null) {
      value = -1;
    }
    WebRTCModule.rtpEncodingParametersSetMaxBitrate(value, ssrc, owner);
  }

  /** @private */
  static nativeSetMinBitrate(owner: ValueTag, ssrc: number | null, value: number | null) {
    if (value == null) {
      value = -1;
    }
    WebRTCModule.rtpEncodingParametersSetMinBitrate(value, ssrc, owner);
  }

  _active: boolean;
  _maxBitrate: number | null;
  _minBitrate: number | null;

  /**
   * エンコーディングがメディアデータの送信時に使われるのであれば `true`
   * 
   * @type {boolean}
   */
  get active(): boolean {
    return this._active;
  }

  /**
   * エンコーディングがメディアデータの送信時に使われるのであれば `true`
   * 
   * @type {boolean}
   */
  set active(flag: boolean) {
    this._active = flag;
    RTCRtpEncodingParameters.nativeSetActive(this._owner, this.ssrc, flag);
  }

  /**
   * 最大ビットレート
   *
   * @type {number|null}
   */
  get maxBitrate(): number | null {
    return this._maxBitrate;
  }

  /**
   * 最大ビットレート
   *
   * @type {number|null}
   */
  set maxBitrate(value: number | null) {
    this._maxBitrate = value;
    RTCRtpEncodingParameters.nativeSetMaxBitrate(this._owner, this.ssrc, value);
  }

  /**
   * 最小ビットレート
   * 
   * @type {number|null}
   */
  get minBitrate(): number | null {
    return this._minBitrate;
  }

  /**
   * 最小ビットレート
   * 
   * @type {number|null}
   */
  set minBitrate(value: number | null) {
    this._minBitrate = value;
    RTCRtpEncodingParameters.nativeSetMinBitrate(this._owner, this.ssrc, value);
  }

  /**
   * SSRC
   */
  ssrc: number | null;

  _owner: ValueTag;
  _active: boolean;
  _maxBitrate: number | null;
  _minBitrate: number | null;

  /**
   * @package
   */
  constructor(owner: ValueTag, info: Object) {
    this._owner = owner;
    this._active = info.active;
    this._maxBitrate = info.maxBitrate;
    this._minBitrate = info.minBitrate;
    this.ssrc = info.ssrc;
  }

}

/**
 * RTP コーデックに関するパラメーターです。
 * 
 * @since 1.1.0
 */
export class RTCRtpCodecParameters {

  /**
   * RTP ペイロードの種別
   */
  payloadType: number;

  /**
   * MIME タイプ
   */
  mimeType: String;

  /**
   * クロックレート
   */
  clockRate: String | null;

  /**
   * チャンネル数 (モノラルなら 1 、ステレオなら 2)
   */
  channels: number | null;

  /**
   * SDP の "a=fmtp" 行に含まれる "format specific parameters"
   */
  parameters: Map<String, String> = new Map();

  _owner: ValueTag;

  /**
   * @package
   */
  constructor(owner: ValueTag, info: Object) {
    this._owner = owner;
    this.payloadType = info.payloadType;
    this.mimeType = info.mimeType;
    this.clockRate = info.clockRate;
    this.channels = info.channels;
    this.parameters = info.parameters;
  }

}

/**
 * RTP に関するパラメーターです。
 * 
 * @since 1.1.0
 */
export class RTCRtpParameters {

  /**
   * トランザクション ID
   */
  transactionId: String;

  /**
   * RTCP 用のパラメーター
   */
  rtcp: RTCRtcpParameters;

  /**
   * RTP 拡張ヘッダーに関するパラメーターのリスト
   */
  headerExtensions: Array<RTCRtpHeaderExtensionParameters>;

  /**
   * RTP エンコーディングに関するパラメーターのリスト
   */
  encodings: Array<RTCRtpEncodingParameters>;

  /**
   * RTP コーデックに関するパラメーターのリスト
   */
  codecs: Array<RTCRtpCodecParameters>;

  _owner: ValueTag;

  /**
   * @package
   */
  constructor(owner: ValueTag, info: Object) {
    this._owner = owner;
    this.transactionId = info.transactionId;
    this.rtcp = new RTCRtcpParameters(owner, info.rtcp);
    this.headerExtensions = info.headerExtensions.map(info =>
      new RTCRtpHeaderExtensionParameters(owner, info));
    this.encodings = info.encodings.map(info =>
      new RTCRtpEncodingParameters(owner, info));
    this.codecs = info.codecs.map(info =>
      new RTCRtpCodecParameters(owner, info));
  }

}