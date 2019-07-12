import { Platform } from 'react-native';
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
 * 音声の出力元を取得します。(iOS のみ)
 * 
 * @return {Promise<RTCAudioPort>}
 * 
 * @since 2.1.0
 */
export function getAudioPort(): Promise<RTCAudioPort> {
  // Android は未実装
  // TODO(kdxu): Android のオーディオポート取得機能を入れる
  if (Platform.OS === 'ios') {
    return WebRTC.getAudioPort();
  } else {
    logger.warn("# getAudioPort() does not support Android ");
  }
}

/**
 * 音声の出力先を指定します。(iOS のみ)
 * 
 * @param {RTCAudioPort} port 音声の出力先
 * @returns {void}
 * 
 * @since 2.1.0
 */
export function setAudioPort(port: RTCAudioPort): Promise<void> {
  // Android は未実装
  // TODO(kdxu): Android のオーディオポート取得機能を入れる
  if (Platform.OS === 'ios') {
    logger.log("# audio route change => ", port);
    return WebRTC.setAudioPort(port);
  } else {
    logger.warn("# setAudioPort() does not support Android ");
  }
}

