package jp.shiguredo.react.webrtckit;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.facebook.react.bridge.ReadableMap;

import static jp.shiguredo.react.webrtckit.Readables.isTruthy;
import static jp.shiguredo.react.webrtckit.Readables.jdouble;
import static jp.shiguredo.react.webrtckit.Readables.jint;
import static jp.shiguredo.react.webrtckit.Readables.map;
import static jp.shiguredo.react.webrtckit.Readables.string;

final class WebRTCMediaStreamConstraints {

    @Nullable
    final Video video;
    @Nullable
    final Audio audio;

    WebRTCMediaStreamConstraints(@Nullable final ReadableMap json) {
        if (json == null) {
            this.video = null;
            this.audio = null;
        } else {
            this.video = Video.fromJson(json);
            this.audio = Audio.fromJson(json);
        }
    }


    static class Video {

        @Nullable
        final String facingMode;
        final int width;
        final int height;
        final int frameRate;
        final double aspectRatio;

        // TODO: sourceId のサポートをどうするか考える。
        //       iOS側はAVCaptureDevice.uniqueID を sourceId として指定させているように見えるが、実装上で使っているようには見えない。
        //       そもそもAndroid側は deviceName でデバイスを管理するので sourceId という名前が良いとも思えない (sourceNameだな)
        //       とはいえiOS側と名前を合わせないと機能しない・・・とりま未実装で放置しておく

        @Nullable
        static Video fromJson(@NonNull final ReadableMap json) {
            final ReadableMap videoJson = map(json, "video");
            if (videoJson == null) {
                return null;
            } else {
                return new Video(videoJson);
            }
        }

        private Video(@NonNull final ReadableMap videoJson) {
            final String facingModeValue = string(videoJson, "facingMode");
            if (facingModeValue != null) {
                switch (facingModeValue) {
                    case "user":
                        this.facingMode = facingModeValue;
                        break;
                    case "environment":
                        this.facingMode = facingModeValue;
                        break;
                    default:
                        this.facingMode = null;
                        break;
                }
            } else {
                this.facingMode = null;
            }
            width = jint(videoJson, "width", -1);
            height = jint(videoJson, "width", -1);
            frameRate = jint(videoJson, "frameRate", -1);
            aspectRatio = jdouble(videoJson, "aspectRatio", -1);
        }
    }

    static class Audio {
        @Nullable
        static Audio fromJson(@NonNull final ReadableMap json) {
            if (isTruthy(json, "audio")) {
                return new Audio();
            } else {
                return null;
            }
        }

        private Audio() {
            // 現在は bool 以外にパラメーターがないため、特に処理はなし
        }
    }


}
