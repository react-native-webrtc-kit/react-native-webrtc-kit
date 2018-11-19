// @flow

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

  /**
   * @package
   */
  constructor(info: Object) {
    self.cname = info.cname;
    self.reducedSize = info.reducedSize;
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

  /**
   * @package
   */
  constructor(info: Object) {
    self.uri = info.uri;
    self.id = info.id;
    self.encrypted = info.encrypted;
  }

}

/**
 * RTP エンコーディングに関するパラメーターです。
 * 
 * @since 1.1.0
 */
export class RTCRtpEncodingParameters {

  active: boolean;

  /**
   * 最大ビットレート
   */
  maxBitrate: number | null;

  /**
   * 最小ビットレート
   */
  minBitrate: number | null;

  /**
   * SSRC
   */
  ssrc: number | null;

  /**
   * @package
   */
  constructor(active: boolean) {
    self.active = active;
  }

}

/**
 * RTP コーデックに関するパラメーターです。
 * 
 * @since 1.1.0
 */
export class RTCRtpCodecParameters {

  payloadType: number;
  mimeType: String;
  clockRate: String | null;
  channels: number | null;

  // SDP fmtp parameters "a=fmtp"
  parameters: Map<String, String> = new Map();

  /**
   * @package
   */
  constructor(info: Object) {
    self.payloadType = info.payloadType;
    self.mimeType = info.mimeType;
    self.clockRate = info.clockRate;
    self.channels = info.channels;
    self.parameters = info.parameters;
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

  /**
   * @package
   */
  constructor(info: Object) {
    self.transactionId = info.transactionId;
    self.rtcp = new RTCRtcpParameters(info.rtcp);
    self.headerExtensions = info.headerExtensions.map(info =>
      new RTCRtpHeaderExtensionParameters(info));
    self.encodings = info.encodings.map(info =>
      new RTCRtpEncodingParameters(info));
    self.codecs = info.codecs.map(info =>
      new RTCRtpCodecParameters(info));
  }

}