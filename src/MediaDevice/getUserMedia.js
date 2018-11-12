// @flow

import WebRTC from '../WebRTC';
import RTCMediaStream from '../MediaStream/RTCMediaStream';
import RTCMediaStreamTrack from '../MediaStream/RTCMediaStreamTrack';
import RTCMediaStreamConstraints from '../MediaStream/RTCMediaStreamConstraints';
import RTCMediaStreamError from '../MediaStream/RTCMediaStreamError';
import logger from '../Util/RTCLogger';

export type RTCUserMedia = {
  tracks: Array<RTCMediaStreamTrack>,
  streamId: String
}

/** 
 * カメラやマイクなどのメディア情報入力デバイスのトラックを生成します。
 * この関数を実行するとデバイスの使用許可がユーザーに要求され、
 * ユーザーが許可すると、 Promise は {@link RTCUserMedia} を引数として解決されます。
 * {@link RTCPeerConnection} でトラックを利用するには `addTrack()` で追加します。
 * 
 * この関数で生成されるトラックの使用は一度きりです。
 * 再び入力デバイスを使う場合は、再度この関数を実行して
 * 新しいトラックを生成する必要があります。
 * 
 * @example
 * getUserMedia(null).then((info) => {
 *   var pc = new RTCPeerConnection(constraints);
 *   info.tracks.forEach(track =>
 *     this._pc.addTrack(track, [info.streamId])
 *   );
 *   ...
 * });
 * 
 * @param {RTCMediaStreamConstraints|null} constraints トラックの制約
 * @returns {Promise<RTCUserMedia>} トラックの取得の結果を表す Promise 。
 *  エラー時は {@link RTCMediaStreamError} が渡されます。
 * @version 1.1.0
 */
export function getUserMedia(constraints: RTCMediaStreamConstraints | null):
  Promise<RTCUserMedia> {
  logger.log("# get user media");
  if (constraints == null) {
    constraints = new RTCMediaStreamConstraints();
  }
  return WebRTC.getUserMedia(constraints)
    .then(ev => {
      var tracks = [];
      for (const track of ev.tracks) {
        tracks.push(new RTCMediaStreamTrack(track));
      }
      return { tracks, streamId: ev.streamId };
    })
    .catch(({ message, code }) => {
      let error;
      switch (code) {
        case 'TypeError':
          error = new TypeError(message);
          break;
      }
      if (!error) {
        error = new RTCMediaStreamError({ message, name: code });
      }
      throw error;
    });

}

/**
 * 稼働中のすべてのメディア入力デバイスを停止します。
 * デバイスの停止中はストリームにメディアデータが送信されません。
 * 再開するには {@link getUserMedia} を実行します。
 * 
 * @returns {void}
 */
export function stopUserMedia(): void {
  logger.log("# stop user media");
  WebRTC.stopUserMedia();
}
