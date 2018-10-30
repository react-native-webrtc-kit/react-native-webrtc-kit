// @flow

import {
  NativeModules,
  requireNativeComponent,
  ViewPropTypes
} from 'react-native';
import PropTypes from 'prop-types';
import React from 'react';
import RTCMediaStreamTrack from '../MediaStream/RTCMediaStreamTrack';

import type { ValueTag } from '../PeerConnection/RTCPeerConnection';

/** 
 * 映像のサイズ調整方法です。
 * CSS の `object-fit` に近い動作の設定です。
 * 
 * - `'fill'` -
 *   アスペクト比を無視してビュー全体を埋めます。
 *   
 * - `'contain'` -
 *   アスペクト比を維持しつつ、ビュー全体の中に映像が収まるようにします。
 *   映像とビューのサイズが異なる場合は、映像の両側に空白が入ります。
 * 
 * - `'cover'` -
 *   アスペクト比を維持しつつ、ビュー全体を埋めます。
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
  track: RTCMediaStreamTrack

};

const NativeProps = {
  name: 'WebRTCVideoView',
  propTypes: {
    objectFit: PropTypes.oneOf(['fill', 'contain', 'cover']),
    track: PropTypes.instanceOf(RTCMediaStreamTrack),
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

  /**
   * @deprecated track を使用してください。
   */
  streamValueTag: ValueTag;

  /**
   * 描画する映像トラック
   */
  track: RTCMediaStreamTrack | null;

}