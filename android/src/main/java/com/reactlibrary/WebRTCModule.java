
package com.reactlibrary;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.MediaStream;
import org.webrtc.MediaStreamTrack;
import org.webrtc.Metrics;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WebRTCModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;
    private final PeerConnectionFactory peerConnectionFactory;
    private final WebRTCCamera cameraCapturer;
    private final WebRTCMediaStreamRepository repository = new WebRTCMediaStreamRepository();

    public WebRTCModule(final ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        this.peerConnectionFactory = PeerConnectionFactory.builder()
                .setVideoEncoderFactory(new DefaultVideoEncoderFactory(eglContext, enableIntelVp8Encoder, enableH264HighProfile))
                .setVideoDecoderFactory(new DefaultVideoDecoderFactory(eglContext))
                .createPeerConnectionFactory();
        this.cameraCapturer = new WebRTCCamera();
    }

    //region ReactContextBaseJavaModule

    @Override
    public String getName() {
        return "WebRTCModule";
    }

    //endregion

    //region ReactMethod

    /**
     * JS レイヤーがロードまたはリロードされたときに呼ばれます。
     * ネイティブ実装の内部状態をクリーンアップするために使用されます。
     */
    @ReactMethod
    public void finishLoading() {
        /* JS レイヤーがロードまたはリロードされたときに呼ばれる。
         * ここでリロード前の古い RTCPeerConnection の終了処理を行わないと、
         * RTCPeerConnection の接続が残ったままになってしまう。
         */
        Log.v(getName(), "finishLoading()");
        /*
        TODO: implement the following code
        [WebRTCCamera reloadApplication];

        // すべての peer connection を終了する
        for (RTCPeerConnection *conn in self.peerConnections) {
            [conn closeAndFinish];
        }

        [self.peerConnectionDict removeAllObjects];
        [self.trackDict removeAllObjects];
        [self.senderDict removeAllObjects];
        [self.receiverDict removeAllObjects];
        [self.transceiverDict removeAllObjects];
         */
    }

    @ReactMethod
    public void enableMetrics() {
        Log.v(getName(), "enableMetrics()");
        Metrics.enable();
    }

    /**
     * getAndResetMetrics(): Promise<Array<RTCMetricsSampleInfo>>
     */
    @ReactMethod
    public void getAndResetMetrics(@NonNull final Promise promise) {
        Log.v(getName(), "getAndResetMetrics()");
        final Metrics metrics = Metrics.getAndReset();
        final List<Map<String, Object>> results = new ArrayList<>();
        for (final String infoName : metrics.map.keySet()) {
            final Metrics.HistogramInfo info = metrics.map.get(infoName);
            final Map<String, Object> infoMap = new HashMap<>();
            infoMap.put("name", infoName);
            infoMap.put("min", info.min);
            infoMap.put("max", info.max);
            infoMap.put("bucketCount", info.bucketCount);
            infoMap.put("samples", info.samples);
            results.add(infoMap);
        }
        promise.resolve(results);
    }

    /**
     * This feature is not supported by the Android SDK.
     * getAudioPort(): Promise<RTCAudioPort>
     */
    @ReactMethod
    public void getAudioPort(@NonNull final Promise promise) {
        Log.v(getName(), "getAudioPort()");
        promise.resolve("none");
    }

    /**
     * This feature is not supported by the Android SDK.
     * setAudioPort(port: RTCAudioPort): Promise<void>
     */
    @ReactMethod
    public void setAudioPort(@Nullable final String port, @NonNull final Promise promise) {
        Log.v(getName(), "setAudioPort()");
        promise.resolve(null);
    }

    /**
     * getUserMedia(constraints: RTCMediaStreamConstraints): Promise<Object>
     */
    @ReactMethod
    public void getUserMedia(@Nullable final ReadableMap constraintsJson, @NonNull final Promise promise) {
        Log.v(getName(), "getUserMedia()");
        final WebRTCMediaStreamConstraints constraints = new WebRTCMediaStreamConstraints(constraintsJson);
        final boolean isVideoEnabled = (constraints.video != null);
        final boolean isAudioEnabled = (constraints.audio != null);

        if (isVideoEnabled) {
            // カメラとマイクを起動する
            // libwebrtc でカメラを起動すると自動的にマイクも起動される
            // そのため、音声のみ必要な場合でもカメラを起動する必要がある
            final String deviceName = cameraCapturer.getSuitableDeviceNameForFacingMode(constraints.video.facingMode);
            if (deviceName == null) {
                promise.reject("NotFoundError", "No suitable camera device is found for the given facing mode.");
                return;
            }
            cameraCapturer.startCapture(deviceName, constraints.video.width, constraints.video.height, constraints.video.frameRate);
            // TODO: Tie the given started device with the video source, using the videoCapturer reference maybe, look for examples for better implementation
        } else {
            // 映像が不要の場合でも、マイクを起動するためにカメラを起動しておく
            // その場合は後々ストリームから映像トラックを外す
            cameraCapturer.startCaptureWithAllDevices();
        }

        // カメラ用のトラックを持つストリームを生成する
        // このストリームを管理する必要はなく、
        // ストリーム ID のみ getUserMedia に渡せればよい
        final MediaStream mediaStream = peerConnectionFactory.createLocalMediaStream(createNewValueTag());

        // 映像と音声のトラックをストリームに追加する
        final VideoSource videoSource = peerConnectionFactory.createVideoSource(videoCapturer);
        final VideoTrack videoTrack = peerConnectionFactory.createVideoTrack(createNewValueTag(), videoSource);
        final AudioSource audioSource = peerConnectionFactory.createAudioSource(null);
        final AudioTrack audioTrack = peerConnectionFactory.createAudioTrack(createNewValueTag(), audioSource);

        repository.addTrack(videoTrack, createNewValueTag());
        repository.addTrack(audioTrack, createNewValueTag());
        mediaStream.addTrack(videoTrack);
        mediaStream.addTrack(audioTrack);
        //[[WebRTCCameraVideoCapturer shared] addTrackValueTag: videoTrack.valueTag]; // TODO: is this needed?

        // constraints の指定に従ってトラックの可否を決める
        videoTrack.setEnabled(isVideoEnabled);
        audioTrack.setEnabled(isAudioEnabled);

        // アスペクト比の設定
        if (isVideoEnabled) {
            repository.setVideoTrackAspectRatio(videoTrack, constraints.video.aspectRatio);
        }

        // JS に処理を戻す
        final Map<String, Object> result = new HashMap<>();
        result.put("streamId", mediaStream.label());
        final List<Object> tracks = new ArrayList<>();
        tracks.add(createJsonMap(videoTrack));
        tracks.add(createJsonMap(audioTrack));
        result.put("tracks", tracks);
        promise.resolve(result);
    }

    @ReactMethod
    public void stopUserMedia() {
        Log.v(getName(), "stopUserMedia()");
        cameraCapturer.stopCapture();
        // TODO: remove the stopped stream/track from the repository, maybe.
    }

    /**
     * trackSetEnabled(valueTag: ValueTag, enabled: boolean)
     */
    @ReactMethod
    public void trackSetEnabled(boolean isEnabled, @NonNull String valueTag) {
        Log.v(getName(), "trackSetEnabled()");
        final MediaStreamTrack track = repository.getTrackByValueTag(valueTag);
        if (track == null) {
            return;
        }
        track.setEnabled(isEnabled);
    }

    /**
     * trackSetAspectRatio(valueTag: ValueTag, aspectRatio: number)
     */
    @ReactMethod
    public void trackSetAspectRatio(double aspectRatio, @NonNull String valueTag) {
        Log.v(getName(), "trackSetAspectRatio()");
        final VideoTrack videoTrack = repository.getVideoTrackByValueTag(valueTag);
        if (videoTrack == null) {
            return;
        }
        repository.setVideoTrackAspectRatio(videoTrack, aspectRatio);
    }

    /**
     * TODO: RTCRtpTransceiverDirection = String
     * transceiverDirection(valueTag: ValueTag): Promise<RTCRtpTransceiverDirection>
     */
    @ReactMethod
    public void transceiverDirection(@NonNull String valueTag, @NonNull Promise promise) {
        Log.v(getName(), "transceiverDirection()");
        promise.resolve(null);

    }

    /**
     * transceiverSetDirection(valueTag: ValueTag, value: RTCRtpTransceiverDirection)
     */
    @ReactMethod
    public void transceiverSetDirection(@NonNull String valueTag, @NonNull String value, @NonNull Promise promise) {
        Log.v(getName(), "transceiverSetDirection()");
        promise.resolve(null);
    }

    /**
     * TODO: RTCRtpTransceiverDirection = String
     * transceiverCurrentDirection(valueTag: ValueTag): Promise<RTCRtpTransceiverDirection>
     */
    @ReactMethod
    public void transceiverCurrentDirection(@NonNull String valueTag, @NonNull Promise promise) {
        Log.v(getName(), "transceiverCurrentDirection()");
        promise.resolve(null);
    }

    /**
     * transceiverStop(valueTag: ValueTag)
     */
    @ReactMethod
    public void transceiverStop(@NonNull String valueTag, @NonNull Promise promise) {
        Log.v(getName(), "transceiverStop()");
        promise.resolve(null);
    }

    /**
     * TODO: JSON conversion RTCConfiguration
     * peerConnectionInit(valueTag: ValueTag, configuration: RTCConfiguration, constraints: RTCMediaConstraints)
     */
    @ReactMethod
    public void peerConnectionInit(@NonNull ReadableMap configuration, @Nullable ReadableMap constraints, @NonNull String valueTag) {
        Log.v(getName(), "peerConnectionInit()");
    }

    /**
     * TODO: JSON conversion RTCConfiguration
     * peerConnectionSetConfiguration(valueTag: ValueTag, configuration: RTCConfiguration)
     */
    @ReactMethod
    public void peerConnectionSetConfiguration(@NonNull ReadableMap configuration, @NonNull String valueTag) {
        Log.v(getName(), "peerConnectionSetConfiguration()");
    }

    /**
     * peerConnectionAddTrack(valueTag: ValueTag, trackValueTag: ValueTag, streamIds: Array<String>): Promise<Object>
     */
    @ReactMethod
    public void peerConnectionAddTrack(@NonNull String trackValueTag,
                                       @NonNull ReadableArray streamIds,
                                       @NonNull String valueTag,
                                       @NonNull Promise promise) {
        Log.v(getName(), "peerConnectionAddTrack()");
        promise.resolve(null);
    }

    /**
     * peerConnectionRemoveTrack(valueTag: ValueTag, senderValueTag: ValueTag)
     */
    @ReactMethod

    public void peerConnectionRemoveTrack(@NonNull String trackValueTag,
                                          @NonNull String valueTag,
                                          @NonNull Promise promise) {
        Log.v(getName(), "peerConnectionRemoveTrack()");
        promise.resolve(null);
    }

    /**
     * peerConnectionCreateOffer(valueTag: ValueTag, constraints: RTCMediaConstraints): Promise<RTCSessionDescription>
     */
    @ReactMethod
    public void peerConnectionCreateOffer(@NonNull String valueTag, @Nullable ReadableMap constraints, @NonNull Promise promise) {
        Log.v(getName(), "peerConnectionCreateOffer()");
        promise.resolve(null);
    }

    /**
     * peerConnectionCreateAnswer(valueTag: ValueTag, constraints: RTCMediaConstraints): Promise<RTCSessionDescription>
     */
    @ReactMethod
    public void peerConnectionCreateAnswer(@NonNull String valueTag, @Nullable ReadableMap constraints, @NonNull Promise promise) {
        Log.v(getName(), "peerConnectionCreateAnswer()");
        promise.resolve(null);
    }

    /**
     * TODO: convert JSON -> RTCSessionDescription
     * peerConnectionSetLocalDescription(valueTag: ValueTag, sdp: RTCSessionDescription): Promise<void>
     */
    @ReactMethod
    public void peerConnectionSetLocalDescription(@NonNull ReadableMap json, @NonNull String valueTag, @NonNull Promise promise) {
        Log.v(getName(), "peerConnectionSetLocalDescription()");
        promise.resolve(null);
    }

    /**
     * TODO: convert JSON -> RTCSessionDescription
     * peerConnectionSetRemoteDescription(valueTag: ValueTag, sdp: RTCSessionDescription): Promise<void>
     */
    @ReactMethod
    public void peerConnectionSetRemoteDescription(@NonNull ReadableMap json, @NonNull String valueTag, @NonNull Promise promise) {
        Log.v(getName(), "peerConnectionSetRemoteDescription()");
        promise.resolve(null);
    }

    /**
     * TODO: convert JSON -> RTCIceCandidate
     * peerConnectionAddICECandidate(valueTag: ValueTag, candidate: RTCIceCandidate): Promise<void>
     */
    @ReactMethod
    public void peerConnectionAddICECandidate(@NonNull ReadableMap json, @NonNull String valueTag, @NonNull Promise promise) {
        Log.v(getName(), "peerConnectionAddICECandidate()");
        promise.resolve(null);
    }

    /**
     * peerConnectionClose(valueTag: ValueTag)
     */
    @ReactMethod
    public void peerConnectionClose(@NonNull String valueTag) {
        Log.v(getName(), "peerConnectionClose()");
    }

    /**
     * rtpEncodingParametersSetActive(owner: ValueTag, ssrc: number | null, flag: boolean)
     */
    @ReactMethod
    public void rtpEncodingParametersSetActive(boolean flag,
                                               long ssrc,
                                               @NonNull String ownerValueTag) {
        Log.v(getName(), "rtpEncodingParametersSetActive()");
    }

    /**
     * rtpEncodingParametersSetMaxBitrate(owner: ValueTag, ssrc: number | null, value: number | null)
     */
    @ReactMethod
    public void rtpEncodingParametersSetMaxBitrate(double bitrate,
                                                   long ssrc,
                                                   @NonNull String ownerValueTag) {
        Log.v(getName(), "rtpEncodingParametersSetMaxBitrate()");
    }

    /**
     * rtpEncodingParametersSetMinBitrate(owner: ValueTag, ssrc: number | null, value: number | null)
     */
    @ReactMethod
    public void rtpEncodingParametersSetMinBitrate(double bitrate,
                                                   long ssrc,
                                                   @NonNull String ownerValueTag) {
        Log.v(getName(), "rtpEncodingParametersSetMinBitrate()");
    }

    //endregion

    @NonNull
    private String createNewValueTag() {
        return UUID.randomUUID().toString();
    }

    @NonNull
    private Map<String, Object> createJsonMap(@NonNull MediaStreamTrack track) {
        final String readyState;
        switch (track.state()) {
            case LIVE:
                readyState = "live";
                break;
            case ENDED:
                readyState = "ended";
                break;
            default:
                throw new IllegalArgumentException();
        }
        final Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("valueTag", ""); // TODO: get a value tag!
        jsonMap.put("enabled", track.enabled());
        jsonMap.put("id", track.id());
        jsonMap.put("kind", track.kind());
        jsonMap.put("readyState", readyState);
        return jsonMap;
    }

}
