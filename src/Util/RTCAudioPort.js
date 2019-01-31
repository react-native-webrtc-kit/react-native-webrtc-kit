import WebRTC from '../WebRTC';
import logger from './RTCLogger';

/**
 * Audio Port の種別です。
 * - `'None'` - None (デフォルト)
 * - `'Speaker'` - スピーカー
 * - `'Unknown'` - 不明
 * @typedef {string} RTCAudioPort
 * @version 2.0.0
 */
export type RTCAudioPort =
  | 'None'
  | 'Speaker'
  | 'Unknown'
/*
 * オーディオの出力元を取得します。
 * @return {Promise<RTCAudioPort>}
 */
export function getAudioPort(): Promise<RTCAudioPort> {
  return WebRTC.getAudioPort();
}

/*
 * オーディオの出力先を指定します。
 * @param {isSpeaker} Audio Port を speaker にするか None にするかの指定 
 */
export function setAudioPort(isSpeaker: boolean): Promise<void> {
  logger.log("# audio route change");
  return WebRTC.setAudioPort(isSpeaker);
}

