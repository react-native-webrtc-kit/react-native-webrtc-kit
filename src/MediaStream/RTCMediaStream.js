// @flow

import { NativeModules } from 'react-native';
import EventTarget from 'event-target-shim';

import RTCMediaStreamEventTarget from './RTCMediaStreamEventTarget';
import RTCMediaStreamTrack from './RTCMediaStreamTrack';
import type { ValueTag } from '../PeerConnection/RTCPeerConnection';

/**
 * メディアストリームを表すオブジェクトです。
 */
export default class RTCMediaStream extends RTCMediaStreamEventTarget {

  /**
   * ストリーム ID
   */
  id: string;

  /**
   * ストリームが有効であれば `true`
   */
  active: boolean = true;

  /**
   * ネイティブのオブジェクトと関連付けられたタグ
   */
  _valueTag: ValueTag;

  _tracks: Array<RTCMediaStreamTrack> = [];

  /**
   * @private
   */
  constructor(id: string, valueTag: string) {
    super();
    this.id = id;
    this._valueTag = valueTag;
  }

  /**
   * @ignore
   */
  _addTrack(track: RTCMediaStreamTrack): void {
    this._tracks.push(track);
  }

  /**
   * @ignore
   */
  _removeTrack(track: RTCMediaStreamTrack): void {
  }

  /**
   * トラックのリストを返します。
   * 
   * @return {Array<RTCMediaStreamTrack>} トラックのリスト
   */
  getTracks(): Array<RTCMediaStreamTrack> {
    return this._tracks;
  }

  /**
   * 映像トラックのリストを返します。
   * 
   * @return {Array<RTCMediaStreamTrack>} トラックのリスト
   */
  getVideoTracks(): Array<RTCMediaStreamTrack> {
    var tracks = [];
    this._tracks.forEach(track => {
      if (track.kind == 'video') {
        tracks.push(track);
      }
    });
    return tracks;
  }

  /**
   * 音声トラックのリストを返します。
   * 
   * @return {Array<RTCMediaStreamTrack>} トラックのリスト
   */
  getAudioTracks(): Array<RTCMediaStreamTrack> {
    var tracks = [];
    this._tracks.forEach(track => {
      if (track.kind == 'audio') {
        tracks.push(track);
      }
    });
    return tracks;
  }

  _close() {
    this.active = false;
    this._tracks.forEach(track => {
      track._close();
    });
  }

}
