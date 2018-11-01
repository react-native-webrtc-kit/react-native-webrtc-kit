// @flow

export class RTCRtcpParameters {

  cname: String;
  reducedSize: boolean;

  constructor(info: Object) {
    self.cname = info.cname;
    self.reducedSize = info.reducedSize;
  }

}

export class RTCRtpHeaderExtension {

  uri: String;
  id: number;
  encrypted: boolean;

  constructor(info: Object) {
    self.uri = info.uri;
    self.id = info.id;
    self.encrypted = info.encrypted;
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

}

export class RTCRtpCodecParameters {

  payloadType: number;
  mimeType: String;
  clockRate: String | null;
  channels: number | null;

  // SDP fmtp parameters "a=fmtp"
  parameters: Map<String, String> = new Map();

  constructor(info: Object) {
    self.payloadType = info.payloadType;
    self.mimeType = info.mimeType;
    self.clockRate = info.clockRate;
    self.channels = info.channels;
    self.parameters = info.parameters;
  }

}

export class RTCRtpParameters {

  transactionId: String;
  rtcp: RTCRtcpParameters;
  headerExtensions: Array<RTCRtpHeaderExtension>;
  encodings: Array<RTCRtpEncodingParameters>;
  codecs: Array<RTCRtpCodecParameters>;

  constructor(info: Object) {
    self.transactionId = info.transactionId;
    self.rtcp = new RTCRtcpParameters(info.rtcp);
    self.headerExtensions = info.headerExtensions.map(info =>
      new RTCRtpHeaderExtension(info));
    self.encodings = info.encodings.map(info =>
      new RTCRtpEncodingParameters(info));
    self.codecs = info.codecs.map(info =>
      new RTCRtpCodecParameters(info));
  }

}