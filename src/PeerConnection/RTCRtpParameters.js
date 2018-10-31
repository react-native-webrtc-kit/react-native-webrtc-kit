// @flow

export class RTCRtcpParameters {

  cname: String;
  reducedSize: boolean;

  constructor(cname: String, reducedSize: boolean) {
    self.cname = cname;
    self.reducedSize = reducedSize;
  }

  /**
   * @package
   */
  toJSON(): Object {
    return {
      cname: this.cname,
      reducedSize: this.reducedSize,
    };
  }

}

export class RTCRtpHeaderExtension {

  uri: String;
  id: number;
  encrypted: boolean;

  constructor(uri: String, id: number, encrypted: boolean) {
    self.uri = uri;
    self.id = id;
    self.encrypted = encrypted;
  }

  /**
   * @package
   */
  toJSON(): Object {
    return {
      uri: this.uri,
      id: this.id,
      encrypted: this.encrypted,
    };
  }

}

export class RTCRtpEncodingParameters {

  active: boolean;
  maxBitrate: number | null;
  minBitrate: number | null;
  ssrc: number | null;

  constructor(active: boolean) {
    self.active = active;
  }

  /**
   * @package
   */
  toJSON(): Object {
    return {
      active: this.active,
      maxBitrate: this.maxBitrate,
      minBitrate: this.minBitrate,
      ssrc: this.ssrc
    };
  }

}

export class RTCRtpCodecParameters {

  payloadType: number;
  mimeType: String;
  clockRate: String | null;
  channels: number | null;

  // SDP fmtp parameters "a=fmtp"
  parameters: Map<String, String> = new Map();

  constructor(payloadType: number, mimeType: String) {
    self.payloadType = payloadType;
    self.mimeType = mimeType;
  }

  /**
   * @package
   */
  toJSON(): Object {
    return {
      payloadType: this.payloadType,
      mimeType: this.mimeType,
      clockRate: this.clockRate,
      channels: this.channels,
      parameters: this.parameters
    };
  }

}

export class RTCRtpParameters {

  transactionId: String;
  rtcp: RTCRtcpParameters;
  headerExtensions: Array<RTCRtpHeaderExtension> = [];
  encodings: Array<RTCRtpEncodingParameters> = [];
  codecs: Array<RTCRtpCodecParameters> = [];

  constructor(transactionId: String,
    rtcp: RTCRtcpParameters) {
    self.transactionId = transactionId;
    self.rtcp = rtcp;
  }

  /**
   * @package
   */
  toJSON(): Object {
    return {
      transactionId: this.transactionId,
      rtcp: this.rtcp.toJSON(),
      headerExtensions:
        this.headerExtensions.forEach(ext => ext.toJSON()),
      encodings:
        this.encodings.forEach(enc => enc.toJSON()),
      codecs:
        this.codecs.forEach(codec => codec.toJSON()),
    };
  }

}