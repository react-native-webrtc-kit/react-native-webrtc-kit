# React Native WebRTC Kit

**このライブラリの開発とメンテナンスは終了しました**

今後は https://github.com/react-native-webrtc/react-native-webrtc をご利用ください。

### 開発とメンテナンスの終了の経緯

もともとは react-native-webrtc のメンテナンスが活発では無く、
さらには自分たちが求める設計では無かった事が react-native-webrtc-kit を開発していました。

今は react-native-webrtc は十分メンテナンスされている状態にあります。 React Native WebRTC Kit の役割はおわったと考えたためです。

---

[![libwebrtc](https://img.shields.io/badge/libwebrtc-m88.4324.2-blue.svg)](https://chromium.googlesource.com/external/webrtc/+/branch-heads/4240)
[![GitHub tag](https://img.shields.io/github/tag/react-native-webrtc-kit/react-native-webrtc-kit.svg)](https://github.com/react-native-webrtc-kit/react-native-webrtc-kit)
[![npm version](https://badge.fury.io/js/react-native-webrtc-kit.svg)](https://badge.fury.io/js/react-native-webrtc-kit)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

React Native WebRTC Kit は、 React Native アプリケーションから WebRTC ネイティブライブラリを使うためのライブラリです。  
本ライブラリを使うと、マルチプラットフォームに対応する WebRTC ネイティブアプリケーションを React Native で開発できます。  

## About Shiguredo's open source software

We will not respond to PRs or issues that have not been discussed on Discord. Also, Discord is only available in Japanese.

Please read https://github.com/shiguredo/oss/blob/master/README.en.md before use.

## 時雨堂のオープンソースソフトウェアについて

利用前に https://github.com/shiguredo/oss をお読みください。

## 利用 libwebrtc バージョン

本ライブラリは WebRTC M88 を利用しています。

## Web API (ブラウザ) との互換性について

本ライブラリの API は できるだけ [WebRTC の Web API](https://developer.mozilla.org/ja/docs/Web/API/WebRTC_API) に近づけていますが、
ネイティブ API では実装が難しい API や本ライブラリの都合で同一にできない API があります。

相違点については [ドキュメント](https://react-native-webrtc-kit.shiguredo.jp/apidiff.html) を参照してください。

## サンプルコード

サンプルコードは https://github.com/react-native-webrtc-kit/react-native-webrtc-kit-samples にあります。

## ドキュメント

ドキュメントは https://react-native-webrtc-kit.shiguredo.jp/ にあります。

## システム要件

- npm 6.11.3
- yarn v1.22.4
    - 本ライブラリを使うアプリケーションのビルドと実行は yarn に依存しています。 npm を直接使う場合の動作は保証しません。
- watchman 4.9.0

### iOS アプリケーションの開発

- iOS 10.0 以降
- Xcode 10
- CocoaPods 1.5.0

### Android アプリケーションの開発

- Android 5 以降
- Android Studio 3.5.1 以降

また、以下の機能について、Android は未対応です。

- オーディオポートの切り替え/取得機能 (getAudioPort, setAudioPort)

## WebRTC ライブラリについて

本ライブラリが利用する WebRTC ライブラリは、デフォルトの設定では弊社がビルドしたバイナリを指定しています。  
このバイナリは弊社製品用の設定でビルドしてあるので、他のバイナリを使いたい場合は次の方法で入れ替えてください。

- iOS: ビルドした `WebRTC.framework` を `ios/Pods/WebRTC/WebRTC.framework` と入れ替えます。
    - https://github.com/react-native-webrtc-kit/webrtc-ios
- Android: ビルドした `libwebrtc.aar` を `android/libs/` 下に配置し、`android/build.gradle` の dependencies を以下のように編集します。
    - https://github.com/react-native-webrtc-kit/webrtc-android

```
 dependencies {
     implementation 'com.facebook.react:react-native:+'
     // api "com.github.react-native-webrtc-kit:webrtc-android:88.4324.2.0"
     implementation "androidx.annotation:annotation:1.1.0"
     api fileTree(dir: 'libs')
 }
```

## Issues について

Discord で Issue を作成して欲しいと言われた際、
次の開発環境のバージョンを **「メジャーバージョン、マイナーバージョン、メンテナンスバージョン」** まで含めて書いてください (9.4.1など) 。  
これらの開発環境はメンテナンスバージョンの違いでも Sora iOS SDK の挙動が変わる可能性があります。

- Discord ID
- React Native WebRTC Kit
- iOS
    - Mac OS X
    - Xcode
    - iOS
- Android
    - Android SDK Version
    - Android Build Tools Version
    - Android OS Version

## 継続的な更新

- 最新の iOS や Android への対応
- 最新の libwebrtc への対応

## 今後の予定

モバイルだけでなくデスクトップでも利用可能な仕組みを提供していきます。

- [ ] [Simulcast functionality](https://www.w3.org/TR/webrtc/#simulcast-functionality) への対応
- [ ] [Identifiers for WebRTC's Statistics API](https://www.w3.org/TR/webrtc-stats/) への対応
- [ ] [React Native for Windows](https://github.com/microsoft/react-native-windows) への対応
    - ペンディング
- [ ] [React Naitve for macOS](https://github.com/microsoft/react-native-macos) への対応
    - ペンディング

## ライセンス

```
Copyright 2018-2019, Masashi Ono aka akisute (Original Author)
Copyright 2018-2021, Shiguredo Inc.

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

