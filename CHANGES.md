# 変更履歴

- UPDATE
    - 下位互換がある変更
- ADD
    - 下位互換がある追加
- CHANGE
    - 下位互換のない変更
- FIX
    - バグ修正

## 2.1.1

- [FIX] package.json 内のバージョンを更新した

## 2.1.0

- [CHANGE] 音声の出力先を変更できるようにした。次の API を追加した。

    - ``getAudioPort()``

    - ``setAudioPort()``

    - ``enum RTCAudioPort``

## 2.0.0

- [CHANGE] iOS: システム条件を変更した

    - iOS 10

- [CHANGE] Unified Plan に対応した

- [CHANGE] メトリクスに関する次の API を追加した

    - ``enableMetrics``

    - ``getAndResetMetrics``

    - ``RTCMetricsSampleInfo``

- [CHANGE] API: ``RTCPeerConnection``: ``onaddtrack``, ``onremovetrack`` イベントハンドラを ``ontrack`` にまとめた

- [CHANGE] API: ``RTCRtpSender``: ``streamIds``: 追加した

- [CHANGE] API: ``RTCRtpReceiver``: ``streamIds``: 追加した

## 1.1.0

- [CHANGE] iOS: システム条件を変更した

    - iOS 12
    - Xcode 10

- [CHANGE] 映像トラックのアスペクト比を指定可能にした

    - API: `RTCAspectRatio`: 追加した

    - API: `RTCMediaStreamVideoConstraints`: `aspectRatio`: 追加した

    - API: `RTCMediaStreamTrack`: `aspectRatio`: 追加した

- [CHANGE] トラックを操作する API を変更した

    - API: `getUserMedia`: 戻り値の型を `Promise<RTCUserMedia>` に変更した

    - API: `RTCMediaStreamTrackKind`: 追加した

    - API: `RTCPeerConnection()`: イベント `addtrack` と `removetrack` を追加した

    - API: `RTCPeerConnection`: `senders`: 追加した

    - API: `RTCPeerConnection`: `receivers`: 追加した

    - API: `RTCPeerConnection`: `transceivers`: 追加した

    - API: `RTCPeerConnection`: `addTrack()`: 追加した

    - API: `RTCPeerConnection`: `removeTrack()`: 追加した

    - API: `RTCRtpCodecParameters`: 追加した

    - API: `RTCRtpEncodingParameters`: 追加した

    - API: `RTCRtpHeaderExtensionParameters`: 追加した

    - API: `RTCRtpParameters`: 追加した

    - API: `RTCRtpReceiver`: 追加した

    - API: `RTCRtpSender`: 追加した

    - API: `RTCRtpTransceiver`: 追加した

    - API: `RTCRtpTransceiverDirection`: 追加した

    - API: `RTCRtpcParameters`: 追加した

    - API: `RTCUserMedia`: 追加した

    - API: `VideoView`: `track`: を追加した

- [CHANGE] ストリームに関する次の API を変更した

    - API: `RTCMediaStream`: 廃止した

    - API: `RTCPeerConnection()`: イベント `addstream` と `removestream` を削除した

    - API: `RTCPeerConnection`: `getLocalStreams()`: 廃止した

    - API: `RTCPeerConnection`: `getRemoteStreams()`: 廃止した

    - API: `RTCPeerConnection`: `addLocalStream()`: 廃止した

    - API: `RTCPeerConnection`: `removeLocalStream()`: 廃止した

    - API: `VideoView`: `streamValueTag` プロパティを削除した

- [CHANGE] API: `RTCSdpSemancis`: `default` を削除し、デフォルト値を `planb` にした

- [CHANGE] API: `ValueTag` 型を廃止した

## 1.0.2

- [CHANGE] `RTCVideoView`: `objectFit` プロパティに `fill` オプションを追加した

- [FIX] `getUserMedia()`: `facingMode` に `user` を指定しても前面カメラに切り換わらない現象を修正する @kdxu

## 1.0.1

- [FIX] audio トラックが常に NO になってしまう問題を修正 @kdxu
- [FIX] ドキュメントの typo 修正 @kdxu

## 1.0.0

リリース
