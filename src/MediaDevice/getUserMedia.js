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
 * カメラやマイクなどのメディア情報入力デバイスのストリームを生成します。
 * この関数を実行するとデバイスの使用許可がユーザーに要求され、
 * ユーザーが許可すると、 Promise はストリームを引数として解決されます。
 * 
 * このストリームが追加された RTCPeerConnection の接続を解除すると
 * ストリームも閉じられます。
 * 再び入力デバイスのストリームを使う場合は、再度この関数を実行して
 * ストリームを生成する必要があります。
 * 
 * @example
 * getUserMedia(null).then((stream) => {
 *   var pc = new RTCPeerConnection(constraints);
 *   pc.addLocalStream(stream);
 *   ...
 * });
 * 
 * @param {RTCMediaStreamConstraints|null} constraints ストリームの制約
 * @returns {Promise<RTCMediaStream>} ストリームの取得の結果を表す Promise 。
 *  エラー時は {@link RTCMediaStreamError} が渡されます。
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
