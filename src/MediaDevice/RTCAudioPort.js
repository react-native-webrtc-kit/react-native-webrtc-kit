import WebRTC from '../WebRTC';
import logger from '../Util/RTCLogger';

/**
 * Audio Port の種別です。
 * - `'none'` - None (デフォルト)
 * - `'speaker'` - スピーカー
 * - `'unknown'` - 不明
 * @typedef {string} RTCAudioPort
 * 
 * @since 2.1.0
 */
export type RTCAudioPort =
  | 'none'
  | 'speaker'
  | 'unknown'

/**
 * 音声の出力元を取得します。
 * 
 * @return {Promise<RTCAudioPort>}
 * 
 * @since 2.1.0
 */
export function getAudioPort(): Promise<RTCAudioPort> {
  return WebRTC.getAudioPort();
}

/**
 * 音声の出力先を指定します。
 * 
 * @param {RTCAudioPort} port 音声の出力先
 * @returns {void}
 * 
 * @since 2.1.0
 */
export function setAudioPort(port: RTCAudioPort): Promise<void> {
  logger.log("# audio route change => ", port);
  return WebRTC.setAudioPort(port);
}

