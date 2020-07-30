// @flow

import { NativeModules } from 'react-native';

/** @private */
const { WebRTCModule } = NativeModules;

/**
 * @package
 * 
 * ネイティブモジュールのメソッドを呼び出すラッパークラスです。
 * ネイティブメソッドに型注釈をつけて、 Flow で型検査をできるようにします。
 */
export default class WebRTC {

  static setMicrophoneEnabled(newValue) {
    if (Platform.OS === 'ios') {
      (async () => {
        WebRTCModule.setMicrophoneEnabled(newValue)
      })()
    } else {
      logger.warn("# setMicrophoneEnabled() is available only on iOS");
    }
  }
}