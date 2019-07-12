import { NativeModules, Platform } from 'react-native';
import logger from '../Util/RTCLogger';

/** @private */
const { WebRTCModule } = NativeModules;

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

/** @private */
function nativeGetAudioPort(): Promise<RTCAudioPort> {
  return WebRTCModule.getAudioPort();
}

/** @private */
function nativeSetAudioPort(port: RTCAudioPort): Promise<void> {
  return WebRTCModule.setAudioPort(port);
}

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
    return nativeGetAudioPort();
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
    return nativeSetAudioPort(port);
  } else {
    logger.warn("# setAudioPort() does not support Android ");
  }
}

