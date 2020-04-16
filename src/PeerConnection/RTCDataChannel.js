// @flow

import { DeviceEventEmitter, Platform } from 'react-native';
import { NativeModules } from 'react-native';
import RTCDataChannelEventTarget from "./RTCDataChannelEventTarget";
import { RTCEvent, RTCDataChannelMessageEvent } from '../Event/RTCEvents';
import { nativeBoolean } from '../Util/RTCUtil';
import type { ValueTag } from './RTCPeerConnection';
import logger from '../Util/RTCLogger';
import * as Base64 from 'base64-js';

/** @private */
const { WebRTCModule } = NativeModules;

// DataChannel で送受信するデータのクラスです。
/** @private */
type RTCDataBuffer = {
  data: string;
  // バイナリデータかどうかのフラグ
  binary: boolean;
}

/**
 * RTCDataChannelInit のクラスです。
 * - `id`: number | undefined
 * - `ordered`: boolean | undefined
 * - `maxPacketLifeTime`: number | undefined
 * - `maxRetransmits`: number | undefined
 * - `protocol`: string | undefined
 * - `negotiated`: boolean | undefined
 
 * @typedef {Object} RTCDataChannelInit
 * @property {number|undefined} id データチャネルの固有 ID を表します。
 * @property {boolean|undefined} ordered メッセージの送信順序を保証するかどうかのフラグを表します。
 * @property {number|undefined} maxPacketLifeTime メッセージの送信に成功するまで再送を繰り返す時間（ミリ秒）を表します。
 * @property {number|undefined} maxRetransmits メッセージの最大再送信回数を示す数値を表します。
 * @property {string|undefined} protocol 利用する subprotocol を表します。
 * @property {boolean|undefined} negotiated 利用側のアプリケーションがこのデータチャネルをネゴシエーションしたかどうかのフラグを表します。
 */
export type RTCDataChannelInit = {
  id?: number;
  ordered?: boolean;
  maxPacketLifeTime?: number;
  maxRetransmits?: number;
  protocol?: string;
  negotiated?: boolean;
  // maxRetransmitTime は duplicated。代わりに maxPacketLifeTime を用いる
  // cf: https://chromium.googlesource.com/external/webrtc/+/refs/heads/master/sdk/objc/api/peerconnection/RTCDataChannel.h#78
  // maxRetransmitTime: number;
}

/**
 * RTCDataChannel の接続状態です。
 * - `'connecting'`
 * - `'open'`
 * - `'closing'`
 * - `'closed'`
 * 
 * @typedef {string} RTCDataChannelState
 */
export type RTCDataChannelState =
  | 'connecting'
  | 'open'
  | 'closing'
  | 'closed';

/**
 * DataChannel 接続を表すオブジェクトです。
 * @member {number} foo
 */
export default class RTCDataChannel extends RTCDataChannelEventTarget {

  /** @private */
  static nativeSendDataChannel(valueTag: ValueTag,
    buffer: RTCDataBuffer): Promise<void> {
    return WebRTCModule.dataChannelSend(buffer, valueTag);
  }

  /** @private */
  static nativeCloseDataChannel(valueTag: ValueTag): void {
    return WebRTCModule.dataChannelClose(valueTag);
  }

  _binaryType: string = 'arraybuffer';
  /**
   * 送信できるデータのbinaryType を表します。
   * 現在は `'arraybuffer'` のみ対応しています。
   * @type {string}
   */
  get binaryType() {
    return this._binaryType;
  }
  /**
   * 送信できるデータのbinaryType を表します。
   * 現在は `'arraybuffer'` のみ対応しています。
   * @type {string}
   */
  set binaryType(type: string) {
    // XXX(kdxu): 現在 Chrome / Safari は binaryType = 'blob' をサポートしていない
    // RNKit での対応も保留する
    if (type !== 'arraybuffer') {
      logger.error('現在 binaryType は arraybuffer のみ指定可能です');
      return;
    }
    this._binaryType = this.binaryType;
  }

  _id: number = -1;
  /**
   * DataChannel の id を表します。
   * デフォルトは -1 です。
   * DataChannel の作成時にのみ指定可能です。
   * @type {number}
   */
  get id() {
    return this._id;
  }

  _label: string;
  /**
   * DataChannel のラベルを表します。
   * DataChannel の作成時にのみ指定可能です。
   * @type {string}
   */
  get label() {
    return this._label;
  }

  _readyState: RTCDataChannelState = 'connecting';
  /**
   * DataChannel の現在の接続状態を表します。
   * @type {RTCDataChannelState}
   */
  get readyState() {
    return this._readyState;
  }

  _bufferedAmount: number = 0;
  /**
   * DataChannel の現在の bufferedAmount を表します。
   * @type {number}
   */
  get bufferedAmount() {
    return this._bufferedAmount;
  }

  _maxPacketLifeTime: number | null;
  /**
   * DataChannel の maxPacketLifeTime を表します。
   * デフォルトは null です。
   * DataChannel の作成時にのみ指定可能です。
   * Android ではこのプロパティの取得は未対応です。
   * 
   * @type {number|null}
   */
  get maxPacketLifeTime() {
    if (Platform.OS === 'android') {
      logger.error("android での maxPacketLifeTime プロパティの取得は未対応です")
      return null;
    }
    return this._maxPacketLifeTime;
  }

  _maxRetransmits: number | null;
  /**
   * DataChannel の maxRetransmits を表します。
   * デフォルトは null です。
   * DataChannel の作成時にのみ指定可能です。
   * Android ではこのプロパティの取得は未対応です。
   * 
   * @type {number|null}
   */
  get maxRetransmits() {
    if (Platform.OS === 'android') {
      logger.error("android での maxRetransmits プロパティの取得は未対応です")
      return null;
    }
    return this._maxRetransmits;
  }

  _negotiated: boolean = false;
  /**
   * DataChannel の negotiated フラグです。
   * デフォルトは false です。
   * DataChannel の作成時にのみ指定可能です。
   * Android ではこのプロパティの取得は未対応です。
   * @type {boolean}
   */
  get negotiated() {
    if (Platform.OS === 'android') {
      logger.error("android での negotiated プロパティの取得は未対応です")
      return false;
    }
    return this._negotiated;
  }

  _ordered: boolean = false;
  /**
   * DataChannel の ordered フラグです。
   * デフォルトは false です。
   * DataChannel の作成時にのみ指定可能です。
   * Android ではこのプロパティの取得は未対応です。
   * @type {boolean}
   */
  get ordered() {
    if (Platform.OS === 'android') {
      logger.error("android での ordered プロパティの取得は未対応です")
      return false;
    }
    return this._ordered;
  }

  _protocol: string = '';
  /**
   * DataChannel の user-defined に指定した protocol を表します。
   * デフォルトは `''` です。
   * DataChannel の作成時にのみ指定可能です。
   * Android ではこのプロパティの取得は未対応です。
   * @type {string}
   */
  get protocol() {
    if (Platform.OS === 'android') {
      logger.error("android での protocol プロパティの取得は未対応です")
      return '';
    }
    return this._protocol;
  }

  _valueTag: ValueTag;

  // XXX(kdxu): libwebrtc objc で bufferedAmountLowThreshold に関連するプロパティは存在しない
  // RNKit での実装も保留となる
  // cf: https://chromium.googlesource.com/external/webrtc/+/refs/heads/master/sdk/objc/api/peerconnection/RTCDataChannel.mm#
  // bufferedAmountLowThreshold: number = 0;
  _nativeEventListeners: Array<any> = [];


  /**
   * RTCDataChannel のオブジェクトを生成します。
   * ユーザはここから直接 RTCDataChannel インスタンスを作成することはありません。
   * @listens {open} `RTCEvent`: `readyState` が `open` になると送信されます。
   * @listens {closing} `RTCEvent`: `readyState` が `closing` になると送信されます。
   * @listens {close} `RTCEvent`: `readyState` が `closed` になると送信されます。
   * @listens {message} `RTCDataChannelMessageEvent`: リモートからデータを受信すると送信されます。
   */
  constructor(info: Object) {
    super();
    this._valueTag = info.valueTag;
    this._label = info.label;
    this._id = info.id;
    this._readyState = info.readyState;
    this._bufferedAmount = info.bufferedAmount;

    // これ以下の値は libwebrtc Android の RTCDataChannel クラスのプロパティが存在しない
    // cf: https://chromium.googlesource.com/external/webrtc/+/refs/heads/master/sdk/android/api/org/webrtc/DataChannel.java
    // 特に remote 側 (datachannel の初期化を行わない側) は、以下の値を取るすべがない。
    // よって、指定した値がない場合のデフォルト値を別途指定している
    this._maxPacketLifeTime = info.maxPacketLifeTime || null;
    this._maxRetransmits = info.maxRetransmits || null;
    this._protocol = info.protocol || "";
    // 指定した値がない場合、negotiated と property は必ず `false` になるはず
    this._negotiated = nativeBoolean(info.negotiated);
    this._ordered = nativeBoolean(info.ordered);
    this._registerEventsFromNative();
  }

  /**
   * RTCDataChannel でデータを送信します。
   * @param {string|ArrayBuffer|ArrayBufferView} data 送信するデータ
   * @return {Promise<void>} 結果を表す Promise
   */
  send(data: string | ArrayBuffer | ArrayBufferView): Promise<void> {
    // XXX(kdxu): Chrome, Safari でサポートされていない Blob については実装を行わない
    if (typeof data === 'string') {
      // string の場合は特に変換処理をせずに native にわたす
      return RTCDataChannel.nativeSendDataChannel(this._valueTag, { data: data, binary: false });
    }
    // 以下は ArrayBuffer | ArrayBufferView への対応
    let byteArray;
    if (ArrayBuffer.isView(data)) {
      // ArrayBufferView が渡された場合は buffer, byteoffset, bytelength から Uint8Array を構築
      byteArray = new Uint8Array(data.buffer, data.byteOffset, data.byteLength);
    }
    if (data instanceof ArrayBuffer) {
      // ArrayBuffer が渡された場合、そのまま byteArray に変換する
      byteArray = new Uint8Array(data);
    }
    // バイナリデータは一旦 base64 エンコードしてネイティブレイヤーに渡す
    return RTCDataChannel.nativeSendDataChannel(this._valueTag, { data: Base64.fromByteArray(byteArray), binary: true });
  }

  /**
   * RTCDataChannel の接続を閉じます。
   */
  close(): void {
    // PeerConnection.close() と統一性をもたせるため、こちらは同期メソッドとする
    return RTCDataChannel.nativeCloseDataChannel(this._valueTag);
  }

  /**
   * ネイティブレイヤーからのコールバックイベントを登録します。
   * 発火するイベントは以下の通りです。
    - 'open'
    - 'message'
    - 'bufferedamountlow'
    - 'close'
    - 'closing'
   */
  _registerEventsFromNative(): void {
    logger.log(`# DataChannel[${this._valueTag}]: register events from native`);
    this._nativeEventListeners = [

      DeviceEventEmitter.addListener('dataChannelStateChanged', ev => {
        logger.log("# event: dataChannelStateChanged =>", ev.readyState);
        if (ev.valueTag !== this._valueTag) {
          return;
        }
        this._readyState = ev.readyState;
        switch (ev.readyState) {
          case 'connecting':
            // connecting は initial state なので、onstatechange でここに来ることは無いはず
            // cf: https://www.w3.org/TR/webrtc/#creating-a-data-channel
            break;
          case 'open':
            this.dispatchEvent(new RTCEvent('open'));
            break;
          case 'closing':
            this.dispatchEvent(new RTCEvent('closing'));
            break;
          case 'closed':
            this.dispatchEvent(new RTCEvent('close'));
            // This DataChannel is done, clean up event handlers.
            this._unregisterEventsFromNative();
            break;
          default:
            // 予測していない state type が来た場合警告を出す
            logger.error('# event: dataChannelStateChanged, invalid state=>', ev.readyState);
        }
      }),

      DeviceEventEmitter.addListener('dataChannelOnMessage', ev => {
        logger.log("# event: dataChannelOnMessage =>", ev.data);
        if (ev.valueTag !== this._valueTag) {
          return;
        }
        if (ev.binary === true) {
          // バイナリデータの場合、ネイティブレイヤーで base64 encode されたものが来ているはずなので
          // base64 decode を行って受け取る
          const byteArray = Base64.toByteArray(ev.data);
          return this.dispatchEvent(new RTCDataChannelMessageEvent('message', byteArray.buffer, ev.binary));
        }
        return this.dispatchEvent(new RTCDataChannelMessageEvent('message', ev.data, ev.binary));
      }),

      // bufferedAmount が変更された際に発火する
      // bufferedAmount の数値は更新するが、イベントとしてユーザには通知を行わない
      DeviceEventEmitter.addListener('dataChannelOnChangeBufferedAmount', ev => {
        logger.log("# event: dataChannelOnChangeBufferedAmount =>", ev.data);
        if (ev.valueTag !== this._valueTag) {
          return;
        }
        if (ev.bufferedAmount) {
          this._bufferedAmount = ev.bufferedAmount;
        }
      }),
    ]
  }

  _unregisterEventsFromNative(): void {
    logger.log(`# DataChannel[${this._valueTag}]: unregister events from native`);
    this._nativeEventListeners.forEach(e => e.remove());
    this._nativeEventListeners = [];
  }
}
