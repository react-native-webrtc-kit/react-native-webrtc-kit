// @flow

/**
 * ICE 通信ポリシーを表します。
 * 
 * - `'relay'` - TURN サーバーを経由するメディアリレー候補のみを使用します。
 * - `'all'` - すべての候補を使用します。
 * 
 * @typedef {string} RTCIceTransportPolicy
*/
export type RTCIceTransportPolicy =
  | 'relay'
  | 'all'