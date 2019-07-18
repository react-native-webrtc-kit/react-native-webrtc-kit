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

  static finishLoading() {
    WebRTCModule.finishLoading();
  }
}
