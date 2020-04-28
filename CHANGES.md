# 変更履歴

- UPDATE
    - 下位互換がある変更
- ADD
    - 下位互換がある追加
- CHANGE
    - 下位互換のない変更
- FIX
    - バグ修正

## develop

- [FIX] iOS でトラック削除時のイベント名を typo していたので修正する
    - @enm10k

## 2020.3.0

- [ADD] DataChannel に対応する
    - @kdxu
- [ADD] Android の onRemoveTrack に対応
    - @enm10k
- [CHANGE] CircleCI の設定を削除する
    - @kdxu
- [CHANGE]  サポートする React Native の最低バージョンを 0.61.5 に更新
    - @enm10k

## 2020.2.0

- [CHANGE] システム要件を変更する
    - Android 5 以降 (シミュレーターは不可)
    - Android Studio 3.5.1 以降
- [CHANGE] WebRTC M79 に対応する
    - @szktty @enm10k
- [FIX] Android アプリのクラッシュを修正する
    - @enm10k
- [CHANGE] 依存パッケージ handlebars を更新する
    - @enm10k

## 2020.1.0

- リリースミスによりスキップ

## 3.0.0

- [CHANGE] システム要件を変更する
    - npm 6.11.3
    - yarn 1.17.3
- [CHANGE] Android に対応する
    - @akisute @kdxu @szktty
- [CHANGE] iOS: WebRTC M75 に対応する
    - @szktty
- [FIX] iOS: sender の valueTag が登録できないバグを修正する
    - @szktty

## 2.1.2

- [FIX] VideoView の使用時にトラックを追加または削除するとアプリケーションがクラッシュする現象を修正する
    - @szktty

## 2.1.1

- [FIX] package.json 内のバージョンを更新する
    - @szktty

## 2.1.0

- [CHANGE] 音声の出力先を変更できるようにする。次の API を追加する
    - ``getAudioPort()``
    - ``setAudioPort()``
    - ``enum RTCAudioPort``
    - @szktty

## 2.0.0

- [CHANGE] iOS: システム条件を変更する
    - iOS 10
    - @szktty
- [CHANGE] Unified Plan に対応する
    - @szktty
- [CHANGE] メトリクスに関する次の API を追加する
    - ``enableMetrics``
    - ``getAndResetMetrics``
    - ``RTCMetricsSampleInfo``
    - @szktty
- [CHANGE] API: ``RTCPeerConnection``: ``onaddtrack``, ``onremovetrack`` イベントハンドラを ``ontrack`` にまとめる
    - @szktty
- [CHANGE] API: ``RTCRtpSender``: ``streamIds``: 追加する
    - @szktty
- [CHANGE] API: ``RTCRtpReceiver``: ``streamIds``: 追加する
    - @szktty

## 1.1.0

- [CHANGE] iOS: システム条件を変更する
    - iOS 12
    - Xcode 10
    - @szktty
- [CHANGE] 映像トラックのアスペクト比を指定可能にする
    - API: `RTCAspectRatio`: 追加する
    - API: `RTCMediaStreamVideoConstraints`: `aspectRatio`: 追加する
    - API: `RTCMediaStreamTrack`: `aspectRatio`: 追加する
    - @szktty
- [CHANGE] トラックを操作する API を変更する
    - API: `getUserMedia`: 戻り値の型を `Promise<RTCUserMedia>` に変更する
    - API: `RTCMediaStreamTrackKind`: 追加する
    - API: `RTCPeerConnection()`: イベント `addtrack` と `removetrack` を追加する
    - API: `RTCPeerConnection`: `senders`: 追加する
    - API: `RTCPeerConnection`: `receivers`: 追加する
    - API: `RTCPeerConnection`: `transceivers`: 追加する
    - API: `RTCPeerConnection`: `addTrack()`: 追加する
    - API: `RTCPeerConnection`: `removeTrack()`: 追加する
    - API: `RTCRtpCodecParameters`: 追加する
    - API: `RTCRtpEncodingParameters`: 追加する
    - API: `RTCRtpHeaderExtensionParameters`: 追加する
    - API: `RTCRtpParameters`: 追加する
    - API: `RTCRtpReceiver`: 追加する
    - API: `RTCRtpSender`: 追加する
    - API: `RTCRtpTransceiver`: 追加する
    - API: `RTCRtpTransceiverDirection`: 追加する
    - API: `RTCRtpcParameters`: 追加する
    - API: `RTCUserMedia`: 追加する
    - API: `VideoView`: `track`: を追加する
    - @szktty
- [CHANGE] ストリームに関する次の API を変更する
    - API: `RTCMediaStream`: 廃止する
    - API: `RTCPeerConnection()`: イベント `addstream` と `removestream` を削除する
    - API: `RTCPeerConnection`: `getLocalStreams()`: 廃止する
    - API: `RTCPeerConnection`: `getRemoteStreams()`: 廃止する
    - API: `RTCPeerConnection`: `addLocalStream()`: 廃止する
    - API: `RTCPeerConnection`: `removeLocalStream()`: 廃止する
    - API: `VideoView`: `streamValueTag` プロパティを削除する
    - @szktty
- [CHANGE] API: `RTCSdpSemancis`: `default` を削除し、デフォルト値を `planb` にする
    - @szktty
- [CHANGE] API: `ValueTag` 型を廃止する
    - @szktty

## 1.0.2

- [CHANGE] `RTCVideoView`: `objectFit` プロパティに `fill` オプションを追加する
    - @szktty
- [FIX] `getUserMedia()`: `facingMode` に `user` を指定しても前面カメラに切り換わらない現象を修正する
    - @kdxu

## 1.0.1

- [FIX] audio トラックが常に NO になってしまう問題を修正
    - @kdxu
- [FIX] ドキュメントの typo 修正
    - @kdxu

## 1.0.0

**公開**

