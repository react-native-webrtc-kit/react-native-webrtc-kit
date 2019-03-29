
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

import org.webrtc.Metrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebRTCModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;

    public WebRTCModule(final ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
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
        final List<Map<String, Object>>results = new ArrayList<>();
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
     * getAudioPort(): Promise<RTCAudioPort>
     */
    @ReactMethod
    public void getAudioPort(@NonNull Promise promise) {
        Log.v(getName(), "getAudioPort()");
        promise.resolve(null);
    }

    /**
     * setAudioPort(port: RTCAudioPort): Promise<void>
     */
    @ReactMethod
    public void setAudioPort(@Nullable String port, @NonNull Promise promise) {
        Log.v(getName(), "setAudioPort()");
        promise.resolve(null);
    }

    /**
     * TODO: convert JSON -> RTCMediaStreamConstraints, there are no RCTConvert mechanics in Android so we need to map ReadableMap into objects by hand
     * getUserMedia(constraints: RTCMediaStreamConstraints): Promise<Object>
     */
    @ReactMethod
    public void getUserMedia(@Nullable ReadableMap constraintsJson, @NonNull Promise promise) {
        Log.v(getName(), "getUserMedia()");
        promise.resolve(null);
    }

    @ReactMethod
    public void stopUserMedia() {
        Log.v(getName(), "stopUserMedia()");
    }

    /**
     * trackSetEnabled(valueTag: ValueTag, enabled: boolean)
     */
    @ReactMethod
    public void trackSetEnabled(boolean isEnabled, @NonNull String valueTag) {
        Log.v(getName(), "getUserMedia()");
    }

    /**
     * trackSetAspectRatio(valueTag: ValueTag, aspectRatio: number)
     */
    @ReactMethod
    public void trackSetAspectRatio(double aspectRatio, @NonNull String valueTag) {
        Log.v(getName(), "trackSetAspectRatio()");
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

}
