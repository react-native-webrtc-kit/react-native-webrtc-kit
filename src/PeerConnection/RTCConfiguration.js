import RTCIceServer from './RTCIceServer';
import RTCIceTransportPolicy from './RTCIceTransportPolicy';

/**
 * SDP でのマルチストリームの記述方式です。
 * 
 * - `'default'` - デフォルト
 * - `'planb'` - Plan B
 * - `'unified'` - Unified Plan

 * @typedef {string} RTCSdpSemantics
 */
export type RTCSdpSemantics =
  | 'default'
  | 'planb'
  | 'unified'

/**
 * RTCPeerConnection に関する設定です。
 */
export default class RTCConfiguration {

  /**
   * ICE サーバー情報のリスト
   */
  iceServers: Array<RTCIceServer> | null;

  /**
   * ICE 通信ポリシー
   */
  iceTransportPolicy: RTCIceTransportPolicy | null;

  /**
   * SDP のマルチストリームの記述方式
   */
  sdpSemantics: RTCSdpSemantics | null;

  /**
   * @package
   */
  toJSON(): Object {
    var json = {};
    if (this.iceServers) {
      json.iceServers = [];
      this.iceServers.forEach(e => {
        json.iceServers.push(e.toJSON());
      });
    }
    if (this.iceTransportPolicy) {
      json.iceTransportPolicy = this.iceTransportPolicy;
    }
    if (this.sdpSemantics) {
      json.sdpSemantics = this.sdpSemantics;
    }
    return json;
  }

}