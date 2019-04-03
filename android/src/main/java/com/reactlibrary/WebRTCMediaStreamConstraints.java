package com.reactlibrary;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableType;

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
            final ReadableMap videoJson = json.getMap("video");
            if (videoJson == null) {
                return null;
            } else {
                return new Video(videoJson);
            }
        }

        private Video(@NonNull final ReadableMap videoJson) {
            final String facingModeValue = videoJson.getString("facingMode");
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
            width = (videoJson.hasKey("width") && !videoJson.isNull("width")) ? videoJson.getInt("width") : -1;
            height = (videoJson.hasKey("height") && !videoJson.isNull("height")) ? videoJson.getInt("height") : -1;
            frameRate = (videoJson.hasKey("frameRate") && !videoJson.isNull("frameRate")) ? videoJson.getInt("frameRate") : -1;
            aspectRatio = (videoJson.hasKey("aspectRatio") && !videoJson.isNull("aspectRatio")) ? videoJson.getDouble("aspectRatio") : -1;
        }
    }

    static class Audio {
        @Nullable
        static Audio fromJson(@NonNull final ReadableMap json) {
            if (json.getType("audio") != ReadableType.Null) {
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
