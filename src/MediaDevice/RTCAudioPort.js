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
 * オーディオの出力元を取得します。
 * @return {Promise<RTCAudioPort>}
 * 
 * @since 2.1.0
 */
export function getAudioPort(): Promise<RTCAudioPort> {
  return WebRTC.getAudioPort();
}

/**
 * オーディオの出力先を指定します。
 * @param {boolean} isSpeaker Audio Port を Speaker にするか None にするかの指定 
 * 
 * @since 2.1.0
 */
export function setAudioPort(isSpeaker: boolean): Promise<void> {
  logger.log("# audio route change");
  return WebRTC.setAudioPort(isSpeaker);
}

