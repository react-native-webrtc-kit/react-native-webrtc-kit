# React Native WebRTC Kit

[![GitHub tag](https://img.shields.io/github/tag/shiguredo/react-native-webrtc-kit.svg)](https://github.com/shiguredo/react-native-webrtc-kit)
[![npm version](https://badge.fury.io/js/react-native-webrtc-kit.svg)](https://badge.fury.io/js/react-native-webrtc-kit)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

React Native WebRTC Kit は、 React Native アプリケーションから WebRTC ネイティブライブラリを使うためのライブラリです。
本ライブラリを使うと、マルチプラットフォームに対応する WebRTC ネイティブアプリケーションを React Native で開発できます。

本ライブラリの主な目的は、 [WebRTC SFU Sora](https://sora.shiguredo.jp) (以下 Sora) を React Native アプリケーションで利用することです。
そのため、 Sora が対応しない WebRTC の仕様には対応しません。
WebRTC ネイティブライブラリのすべての機能に対応するライブラリではありませんので注意してください。 Sora 非対応の機能が必要であれば、リポジトリをフォークして実装してください。

## About Support

Support for React Native WebRTC Kit by Shiguredo Inc. are limited
**ONLY in JAPANESE** through GitHub issues and there is no guarantee such
as response time or resolution.

## サポートについて

React Native WebRTC Kit に関する質問・要望・バグなどの報告やプルリクエストは Issues の利用をお願いします。
ただし、 Sora のライセンス契約の有無に関わらず、 Issue への応答時間と問題の解決を保証しませんのでご了承ください。

ソースコードに関する Issue とプルリクエストに関しては、基本的にバグ修正のみ対応します。
機能の追加や変更を要望する Issue とプルリクエストには対応しません。機能の追加や変更を行いたい場合は、本ライブラリをフォークしてご利用ください。

React Native WebRTC Kit に対する有償のサポートについては現在提供しておりません。

## サンプルコード

サンプルコードは https://github.com/shiguredo/react-native-webrtc-kit-samples にあります。

## ドキュメント

ドキュメントは https://sora.shiguredo.jp/react-native-webrtc-kit-doc にあります。

## システム要件

- npm 6.1.0
- yarn 1.9.4
    - 本ライブラリを使うアプリケーションのビルドと実行は yarn に依存しています。 npm を直接使う場合の動作は保証しません。
- watchman 4.9.0

### iOS アプリケーションの開発

- iOS 10.0 以降
- Xcode 10
- CocoaPods 1.5.0

### Android アプリケーションの開発

- Android SDK Version 16 以降
- Android API Level 23 以降
- Android Buld Tools Version 28.0.3

また、以下の機能について、Android は未対応です。

- オーディオポートの切り替え/取得機能 (getAudioPort, setAudioPort)
- トラックの削除イベント検知機能 (onRemoveTrack)

## WebRTC ライブラリについて

本ライブラリは WebRTC M75 に対応しています。

本ライブラリが利用する WebRTC ライブラリは、デフォルトの設定では弊社がビルドしたバイナリを指定しています。
このバイナリは弊社製品用の設定でビルドしてあるので、他のバイナリを使いたい場合は次の方法で入れ替えてください。

- iOS: ビルドした `WebRTC.framework` を `ios/Pods/WebRTC/WebRTC.framework` と入れ替えます。
    - https://github.com/shiguredo/sora-webrtc-ios
- Android: ビルドした `libwebrtc.aar` を `android/libs/` 下に配置し、`android/build.gradle` の dependencies に以下のように追記します。
    - https://github.com/shiguredo/sora-webrtc-android

```
dependencies {
  force api fileTree(dir: 'libs')
}

```

## Issues について

質問やバグ報告の場合は、次の開発環境のバージョンを **「メジャーバージョン、マイナーバージョン、メンテナンスバージョン」** まで含めて書いてください (9.4.1など) 。
これらの開発環境はメンテナンスバージョンの違いでも Sora iOS SDK の挙動が変わる可能性があります。

- React Native WebRTC Kit
- iOS
    - Mac OS X
    - Xcode
    - iOS
- Android
    - Android SDK Version
    - Android Build Tools Version
    - Android OS Version

## ライセンス

```
Copyright 2018-2019, Shiguredo Inc. and Masashi Ono (akisute) and Kyoko Kadowaki (kdxu)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
