import WebRTC from '../WebRTC';
import logger from './RTCLogger';

/**
 * Audio Port の種別です。
 * - `'None'` - None (デフォルト)
 * - `'Speaker'` - スピーカー
 * - `'Unknown'` - 不明
 * @typedef {string} RTCAudioPorts
 * @version 2.0.0
 */
export type RTCAudioPorts =
  | 'None'
  | 'Speaker'
  | 'Unknown'
/*
 * オーディオの出力元を取得します。
 * @return {Promise<RTCAudioPorts>}
 */
export function getAudioPort(): Promise<RTCAudioPorts> {
  return WebRTC.getAudioPort();
}

/*
 * オーディオの出力先を指定します。
 * @param {isSpeaker} Audio Port を speaker にするか None にするかの指定 
 */
export function setAudioRoute(isSpeaker: boolean): Promise<void> {
  logger.log("# audio route change");
  WebRTC.onAudioRouteChange(isSpeaker);
}

