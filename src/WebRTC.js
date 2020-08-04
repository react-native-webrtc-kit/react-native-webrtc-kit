// @flow

import { NativeModules } from 'react-native';

/** @private */
const { WebRTCModule } = NativeModules;

/**
 * ライブラリ全体の設定をするクラスです。
 */
export default class WebRTC {

  /**
   * マイクの有効/無効を設定します。
   * iOS のみサポートしており、有効の場合、マイクの権限を要求します。
   * デフォルトではマイクは有効です。
   * @param {boolean} newValue false の場合、マイクを無効にします。
   */
  static async setMicrophoneEnabled(newValue) {
    if (Platform.OS === 'ios') {
      await WebRTCModule.setMicrophoneEnabled(newValue);
    } else {
      logger.warn("# setMicrophoneEnabled() is available only on iOS");
    }
  }
}