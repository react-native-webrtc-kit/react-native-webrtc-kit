// @flow

import type { RTCMediaStreamTrackKind } from '../MediaStream/RTCMediaStreamTrack';

export type RTCRtpCodec =
  | 'rtx'
  | 'red'
  | 'ulpfec'
  | 'flexfec'
  | 'opus'
  | 'isac'
  | 'l16'
  | 'g722'
  | 'ilbc'
  | 'pcmu'
  | 'pcma'
  | 'dtmf'
  | 'comfortnoise'
  | 'vp8'
  | 'vp9'
  | 'h264';

export class RTCRtcpParameters {

  cname: String;
  isReducedSize: boolean;

  constructor(cname: String, isReducedSize: boolean) {
    self.cname = cname;
    self.isReducedSize = isReducedSize;
  }

  /**
   * @package
   */
  toJSON(): Object {
    return {
      cname: this.cname,
      isReducedSize: this.isReducedSize,
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
  maxBitrateBps: number | null;
  minBitrateBps: number | null;
  ssrc: String | null;

  constructor(active: boolean) {
    self.active = active;
  }

  /**
   * @package
   */
  toJSON(): Object {
    return {
      active: this.active,
      maxBitrateBps: this.maxBitrateBps,
      minBitrateBps: this.minBitrateBps,
      ssrc: this.ssrc
    };
  }

}

export class RTCRtpCodecParameters {

  payloadType: number;
  codec: RTCRtpCodec;
  kind: RTCMediaStreamTrackKind;
  clockRate: String | null;
  numChannels: number | null;
  parameters: Map<String, String> = new Map();

  constructor(payloadType: number, codec: RTCRtpCodec) {
    self.payloadType = payloadType;
    self.codec = codec;
  }

  /**
   * @package
   */
  toJSON(): Object {
    return {
      payloadType: this.payloadType,
      codec: this.codec,
      kind: this.kind,
      clockRate: this.clockRate,
      numChannels: this.numChannels,
      parameters: this.parameters
    };
  }

}

export class RTCRtpParameters {

  transactionId: String;
  rtcp: RTCRtcpParameters;
  headerExtensions: RTCRtpHeaderExtension;
  encodings: RTCRtpEncodingParameters;
  codecs: RTCRtpCodecParameters;

  constructor(transactionId: String,
    rtcp: RTCRtcpParameters,
    headerExtensions: RTCRtpHeaderExtension,
    encodings: RTCRtpEncodingParameters,
    codecs: RTCRtpCodecParameters) {
    self.transactionId = transactionId;
    self.rtcp = rtcp;
    self.headerExtensions = headerExtensions;
    self.encodings = encodings;
    self.codecs = codecs;
  }

  /**
   * @package
   */
  toJSON(): Object {
    return {
      transactionId: this.transactionId,
      rtcp: this.rtcp.toJSON(),
      headerExtensions: this.headerExtensions.toJSON(),
      encodings: this.encodings.toJSON(),
      codecs: this.codecs.toJSON()
    };
  }

}