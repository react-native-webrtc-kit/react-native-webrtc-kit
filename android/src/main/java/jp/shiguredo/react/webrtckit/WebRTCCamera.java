package jp.shiguredo.react.webrtckit;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;

import org.webrtc.Camera1Enumerator;
import org.webrtc.CameraEnumerationAndroid;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class WebRTCCamera {

    @NonNull
    private final Camera1Enumerator cameraEnumerator;
    @Nullable
    private Pair<VideoSource, VideoCapturer> runningCapturer = null;

    WebRTCCamera() {
        this.cameraEnumerator = new Camera1Enumerator(true);
    }

    /**
     * 指定された条件を満たす最適なカメラデバイスとキャプチャフォーマットをまとめて、候補として返します。
     */
    @Nullable
    WebRTCCameraDeviceCandidate getSuitableDeviceCandidate(@NonNull final WebRTCMediaStreamConstraints.Video video) {
        return getSuitableDeviceCandidate(video.facingMode, video.width, video.height, video.frameRate);
    }

    @Nullable
    private WebRTCCameraDeviceCandidate getSuitableDeviceCandidate(@Nullable final String facingMode,
                                                                   final int width,
                                                                   final int height,
                                                                   final int framerate) {
        if (facingMode == null) {
            return null;
        }
        final boolean isFrontFacing = facingMode.equals("user");
        // 各要素のデバイス決定における重要度
        // 1. facingMode - 必須。一致しない限りマッチさせない。
        // 2. width*height - 最も近いものを選択。
        // 3. framerate - 最終決定の際に考慮する。基本的には無視するが、万一2.までの条件を満たすデバイスが複数あった場合、framerateを満たすデバイスを優先する。
        final List<WebRTCCameraDeviceCandidate> candidates = new ArrayList<>();
        for (final String name : cameraEnumerator.getDeviceNames()) {
            if (cameraEnumerator.isFrontFacing(name) != isFrontFacing) {
                continue;
            }
            WebRTCCameraDeviceCandidate currentCandidate = null;
            for (final CameraEnumerationAndroid.CaptureFormat format : cameraEnumerator.getSupportedFormats(name)) {
                if (currentCandidate == null) {
                    currentCandidate = new WebRTCCameraDeviceCandidate(name, format, Integer.MAX_VALUE);
                } else {
                    int score = Math.abs(width - format.width) + Math.abs(height - format.height);
                    if (score < currentCandidate.score) {
                        currentCandidate = new WebRTCCameraDeviceCandidate(name, format, score);
                    }
                }
            }
            if (currentCandidate != null) {
                candidates.add(currentCandidate);
            }
        }
        if (candidates.isEmpty()) {
            return null;
        }
        // scoreでソートした先頭が最善候補。基本的にはこの最善候補を返すが、最善候補がframerate要件を満たせず、
        // かつ最善候補と同スコアの別候補が存在してそちらがframerate要件を満たせる場合のみ、そちらの代替候補を利用する。
        Collections.sort(candidates);
        final WebRTCCameraDeviceCandidate bestCandidate = candidates.get(0);
        for (final WebRTCCameraDeviceCandidate candidate : candidates) {
            if (bestCandidate.score < candidate.score) {
                break;
            }
            if (candidate.format.framerate.min <= framerate && framerate <= candidate.format.framerate.max) {
                return candidate;
            }
        }
        return bestCandidate;
    }

    /**
     * 指定された候補を利用してVideoCapturerを新規に生成します。
     * 生成されたVideoCapturerはこの段階では初期化されておらず、またキャプチャも開始していません。
     */
    @NonNull
    VideoCapturer createCapturer(@NonNull final WebRTCCameraDeviceCandidate candidate) {
        return cameraEnumerator.createCapturer(candidate.deviceName, null);
    }

    /**
     * 適当なVideoCapturerを新規に生成します。
     * 生成されたVideoCapturerはこの段階では初期化されておらず、またキャプチャも開始していません。
     */
    @NonNull
    VideoCapturer createCapturerFromAllDevices() {
        // XXX: 万一カメラデバイスが一切ない状態だとIOOBで落ちますが、通常まずありえないと思うので良しとしてます
        return cameraEnumerator.createCapturer(cameraEnumerator.getDeviceNames()[0], null);
    }

    /**
     * 指定された条件で与えられたVideoCapturerによるキャプチャを開始します。
     * すでにキャプチャが開始されている場合は何もしません。
     * 引数のVideoCapturerはすでにVideoSourceと紐付けられた後でなければなりません。
     */
    void startCapture(@NonNull final VideoSource source,
                      @NonNull final VideoCapturer capturer,
                      @NonNull final WebRTCCameraDeviceCandidate candidate,
                      @NonNull final WebRTCMediaStreamConstraints.Video video) {
        if (runningCapturer != null) {
            return;
        }
        final int framerate = Math.max(candidate.format.framerate.min, Math.min(video.frameRate, candidate.format.framerate.max));
        capturer.startCapture(candidate.format.width, candidate.format.height, framerate);
        runningCapturer = new Pair<>(source, capturer);
    }

    /**
     * 現在実行中のキャプチャを停止します。
     * まだキャプチャが開始されていない場合には何もしません。
     */
    void stopCapture() {
        if (runningCapturer == null) {
            return;
        }
        try {
            runningCapturer.second.stopCapture();
            runningCapturer.first.dispose();
            runningCapturer.second.dispose();
        } catch (InterruptedException e) {
            // Squash the exception here
            Log.e("WebRTCCamera", "stopCapture()", e);
        } finally {
            runningCapturer = null;
        }
    }
}
