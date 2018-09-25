// @flow

import {
  NativeModules,
  requireNativeComponent,
  ViewPropTypes
} from 'react-native';
import PropTypes from 'prop-types';
import React from 'react';

import type { ValueTag } from '../PeerConnection/RTCPeerConnection';

/** 
 * 映像のサイズ調整方法です。
 * CSS の `object-fit` に近い動作の設定です。
 * 
 * - `'fill'` -
 *   アスペクト比を無視してビュー全体を埋めます。
 *   
 * - `'contain'` -
 *   アスペクト比を維持しつつ、ビュー全体の中にビデオが収まるようにします
 *   (空白になる領域が存在する可能性があります) 。
 *   映像は途切れません。
 * 
 * - `'cover'` -
 *   アスペクト比を維持しつつ、ビュー全体を埋めます。
 *   映像が途切れる可能性があります。
 *
 * @typedef {string} RTCObjectFit
 * @see https://www.w3.org/TR/html5/embedded-content-0.html#dom-video-videowidth
 * @see https://www.w3.org/TR/html5/rendering.html#video-object-fit
  */
export type RTCObjectFit =
  | 'fill'
  | 'contain'
  | 'cover'

type Props = {

  objectFit: RTCObjectFit,
  streamValueTag: string

};

const NativeProps = {
  name: 'WebRTCVideoView',
  propTypes: {
    objectFit: PropTypes.oneOf(['fill', 'contain', 'cover']),
    streamValueTag: PropTypes.string,
    ...ViewPropTypes
  }
};

/**
 * @package
 */
const WebRTCVideoView = requireNativeComponent('WebRTCVideoView', NativeProps);
export default WebRTCVideoView;

/**
 * ストリームから出力される映像を描画します (音声も同時に再生されます) 。
 * 
 * NOTE: このクラス定義は ESDoc で RTCVideoView の説明を記述するために用意してあります。
 * RTCVideoView は React ネイティブコンポーネントとして実装されており、
 * このクラスのインスタンスは本ライブラリ中では使われません。
 */
export class RTCVideoView extends React.Component<Props> {

  /**
   * 映像のサイズ調整方法
   */
  objectFit: RTCObjectFit = 'contain';

  /** ストリームのタグ
   */
  streamValueTag: ValueTag;

}