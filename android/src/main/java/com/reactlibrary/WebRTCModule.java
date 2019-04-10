
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
import org.webrtc.DataChannel;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.MediaStreamTrack;
import org.webrtc.Metrics;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RtpReceiver;
import org.webrtc.RtpTransceiver;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.reactlibrary.WebRTCConverter.rtcConfiguration;
import static com.reactlibrary.WebRTCConverter.rtpTransceiverDirection;
import static com.reactlibrary.WebRTCConverter.rtpTransceiverDirectionStringValue;

public class WebRTCModule extends ReactContextBaseJavaModule {

    @NonNull
    private final ReactApplicationContext reactContext;
    @NonNull
    private final EglBase eglBase;
    @NonNull
    private final PeerConnectionFactory peerConnectionFactory;
    @NonNull
    private final WebRTCCamera cameraCapturer;
    /**
     * XXX: Maybe we should move the surfaceTextureHelper to the WebRTCCamera so that the camera module handles all capturing tasks instead of this module
     */
    @NonNull
    private final SurfaceTextureHelper surfaceTextureHelper;
    @NonNull
    final WebRTCRepository repository = new WebRTCRepository();


    public WebRTCModule(@NonNull final ReactApplicationContext reactContext) {
        super(reactContext);

        // PeerConnectionFactory自体を最初に初期化する必要がある
        final PeerConnectionFactory.InitializationOptions pcfInitializationOptions =
                PeerConnectionFactory.InitializationOptions.builder(reactContext)
                        .setEnableInternalTracer(false)
                        .setFieldTrials("")
                        .createInitializationOptions();
        PeerConnectionFactory.initialize(pcfInitializationOptions);

        // 各フィールドを初期化
        // XXX: DefaultVideoEncoderFactory - VP8 encoder / H264 の利用可否を適切に判断する
        //      現在のところは決め打ちで両方有効にしているが、ハードウェアによっては利用できない場合がある
        //      RN経由でユーザーから調整可能にしてもよいが、可能であればここで利用可否を判断できるのが望ましい
        this.reactContext = reactContext;
        this.eglBase = EglBase.create();
        this.peerConnectionFactory = PeerConnectionFactory.builder()
                .setVideoEncoderFactory(new DefaultVideoEncoderFactory(getEglContext(), true, true))
                .setVideoDecoderFactory(new DefaultVideoDecoderFactory(getEglContext()))
                .createPeerConnectionFactory();
        this.cameraCapturer = new WebRTCCamera();
        this.surfaceTextureHelper = SurfaceTextureHelper.create("WebRTCCameraCaptureThread", getEglContext());
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
        cameraCapturer.stopCapture();
        repository.clear();
        // EGLのrelease()やlocalRenderer/remoteRendererのrelease()は行わない
        // finishLoading後もアプリの動作は継続するため、一度捨てたら再利用できないフィールドは破棄しない
        // これでリロードがうまく動作しない場合は、コンストラクタではなくfinishLoading()内でフィールドを初期化するように変更する必要がある
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

        final WebRTCCameraDeviceCandidate deviceCandidate;
        final VideoCapturer videoCapturer;
        if (isVideoEnabled) {
            // カメラとマイクを起動する
            // libwebrtc でカメラを起動すると自動的にマイクも起動される
            // そのため、音声のみ必要な場合でもカメラを起動する必要がある
            deviceCandidate = cameraCapturer.getSuitableDeviceCandidate(constraints.video);
            if (deviceCandidate == null) {
                promise.reject("NotFoundError", "No suitable camera device is found for the given facing mode.");
                return;
            }
            videoCapturer = cameraCapturer.createCapturer(deviceCandidate);
        } else {
            // 映像が不要の場合でも、マイクを起動するためにカメラを起動しておく
            // その場合は後々ストリームから映像トラックを外す
            deviceCandidate = null;
            videoCapturer = cameraCapturer.createCapturerFromAllDevices();
        }

        // カメラ用のトラックを持つストリームを生成する
        // このストリームを管理する必要はなく、
        // ストリーム ID のみ getUserMedia に渡せればよい
        final MediaStream mediaStream = peerConnectionFactory.createLocalMediaStream(createNewValueTag());

        // 映像と音声のトラックをストリームに追加する
        // XXX: PeerConnectionFactory::createVideoSource(VideoCapturer videoCapturer) の実装により、videoCapturerが適切にinitializeされる
        //      したがってcreateVideoSource()の後videoCapturerをstartすればOK
        final VideoSource videoSource = peerConnectionFactory.createVideoSource(videoCapturer.isScreencast());
        videoCapturer.initialize(surfaceTextureHelper, reactContext, videoSource.getCapturerObserver());
        final VideoTrack videoTrack = peerConnectionFactory.createVideoTrack(createNewValueTag(), videoSource);
        final AudioSource audioSource = peerConnectionFactory.createAudioSource(null);
        final AudioTrack audioTrack = peerConnectionFactory.createAudioTrack(createNewValueTag(), audioSource);

        repository.addTrack(videoTrack, createNewValueTag());
        repository.addTrack(audioTrack, createNewValueTag());
        mediaStream.addTrack(videoTrack);
        mediaStream.addTrack(audioTrack);

        // constraints の指定に従ってトラックの可否を決める
        videoTrack.setEnabled(isVideoEnabled);
        audioTrack.setEnabled(isAudioEnabled);

        // アスペクト比の設定と、カメラデバイスのキャプチャ開始
        // XXX: キャプチャ開始はlocal stream追加まで待ったほうがいいかもしれないけど、ここではiOS版に揃えて即開始します。ダメそうなら待つように実装を修正する。
        if (isVideoEnabled) {
            repository.setVideoTrackAspectRatio(videoTrack, constraints.video.aspectRatio);
            cameraCapturer.startCapture(videoCapturer, deviceCandidate, constraints.video);
        }

        // JS に処理を戻す
        final Map<String, Object> result = new HashMap<>();
        result.put("streamId", mediaStream.getId());
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
     * transceiverDirection(valueTag: ValueTag): Promise<RTCRtpTransceiverDirection>
     */
    @ReactMethod
    public void transceiverDirection(@NonNull String valueTag, @NonNull Promise promise) {
        Log.v(getName(), "transceiverDirection()");
        final RtpTransceiver transceiver = repository.getTransceiverByValueTag(valueTag);
        if (transceiver == null) {
            promise.reject("NotFoundError", "transceiver is not found", null);
            return;
        }
        promise.resolve(rtpTransceiverDirectionStringValue(transceiver.getDirection()));
    }

    /**
     * transceiverSetDirection(valueTag: ValueTag, value: RTCRtpTransceiverDirection)
     */
    @ReactMethod
    public void transceiverSetDirection(@NonNull String valueTag, @NonNull String value, @NonNull Promise promise) {
        Log.v(getName(), "transceiverSetDirection()");
        final RtpTransceiver transceiver = repository.getTransceiverByValueTag(valueTag);
        if (transceiver == null) {
            promise.reject("NotFoundError", "transceiver is not found", null);
            return;
        }
        transceiver.setDirection(rtpTransceiverDirection(value));
        promise.resolve(null);
    }

    /**
     * transceiverCurrentDirection(valueTag: ValueTag): Promise<RTCRtpTransceiverDirection>
     */
    @ReactMethod
    public void transceiverCurrentDirection(@NonNull String valueTag, @NonNull Promise promise) {
        Log.v(getName(), "transceiverCurrentDirection()");
        final RtpTransceiver transceiver = repository.getTransceiverByValueTag(valueTag);
        if (transceiver == null) {
            promise.reject("NotFoundError", "transceiver is not found", null);
            return;
        }
        final RtpTransceiver.RtpTransceiverDirection currentDirection = transceiver.getCurrentDirection();
        if (currentDirection == null) {
            promise.resolve(null);
            return;
        }
        promise.resolve(rtpTransceiverDirectionStringValue(currentDirection));
    }

    /**
     * transceiverStop(valueTag: ValueTag)
     */
    @ReactMethod
    public void transceiverStop(@NonNull String valueTag, @NonNull Promise promise) {
        Log.v(getName(), "transceiverStop()");
        final RtpTransceiver transceiver = repository.getTransceiverByValueTag(valueTag);
        if (transceiver == null) {
            promise.reject("NotFoundError", "transceiver is not found", null);
            return;
        }
        transceiver.stop();
        promise.resolve(null);
    }

    /**
     * peerConnectionInit(valueTag: ValueTag, configuration: RTCConfiguration, constraints: RTCMediaConstraints)
     * TODO: MediaConstraintsがdeprecated扱いになっているがどうするべきか？とりあえず現状はconstraints引数を無視するようにしているが・・・
     */
    @ReactMethod
    public void peerConnectionInit(@NonNull ReadableMap configurationJson, @Nullable ReadableMap constraintsJson, @NonNull String valueTag) {
        Log.v(getName(), "peerConnectionInit()");
        final PeerConnection.RTCConfiguration configuration = rtcConfiguration(configurationJson);
        final PeerConnection peerConnection = peerConnectionFactory.createPeerConnection(configuration, peerConnectionObserver);
        if (peerConnection == null) {
            throw new IllegalStateException("createPeerConnection failed");
        }
        repository.addPeerConnection(peerConnection, createNewValueTag());
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

    //region PeerConnection.Observer

    private final PeerConnection.Observer peerConnectionObserver = new PeerConnection.Observer() {
        @Override
        public void onSignalingChange(PeerConnection.SignalingState signalingState) {
                /*
                [peerConnection detectConnectionStateAndFinishWithModule: self];
                [self.bridge.eventDispatcher sendDeviceEventWithName:@"peerConnectionSignalingStateChanged"
                                                                body:@{@"valueTag": peerConnection.valueTag,
                                                                       @"signalingState": [WebRTCUtils stringForSignalingState:newState]}];
                 */
        }

        @Override
        public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
                /*
                [peerConnection detectConnectionStateAndFinishWithModule: self];
[self.bridge.eventDispatcher sendDeviceEventWithName:@"peerConnectionIceConnectionChanged"
                                                body:@{@"valueTag": peerConnection.valueTag,
                                                       @"iceConnectionState": [WebRTCUtils stringForICEConnectionState:newState]}];
                 */
        }

        @Override
        public void onIceConnectionReceivingChange(boolean b) {
            // Do nothing
        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
                /*
                [peerConnection detectConnectionStateAndFinishWithModule: self];
[self.bridge.eventDispatcher sendDeviceEventWithName:@"peerConnectionIceGatheringChanged"
                                                body:@{@"valueTag": peerConnection.valueTag,
                                                       @"iceGatheringState": [WebRTCUtils stringForICEGatheringState:newState]}];
                 */
        }

        @Override
        public void onIceCandidate(IceCandidate iceCandidate) {
                /*
                [self.bridge.eventDispatcher sendDeviceEventWithName:@"peerConnectionGotICECandidate"
                                                body:@{@"valueTag": peerConnection.valueTag,
                                                       @"candidate": @{
                                                               @"candidate": candidate.sdp,
                                                               @"sdpMLineIndex": @(candidate.sdpMLineIndex),
                                                               @"sdpMid": candidate.sdpMid}}];
                 */
        }

        @Override
        public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
                /*
                [peerConnection removeIceCandidates: candidates];
                */
        }

        @Override
        public void onAddStream(MediaStream mediaStream) {
                /*
                stream.valueTag = [self createNewValueTag];
                [self addStream: stream forKey: stream.valueTag];

                for (RTCMediaStreamTrack *track in stream.allTracks) {
                    track.valueTag = [self createNewValueTag];
                    [self addTrack: track forKey: track.valueTag];
                }
                 */
        }

        @Override
        public void onRemoveStream(MediaStream mediaStream) {
                /*
                if (stream.valueTag) {
                    [WebRTCValueManager removeValueTagForObject: stream];
                    [self removeStreamForKey: stream.valueTag];
                }
                 */
        }

        @Override
        public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {
                /*
                rtpReceiver.valueTag = [self createNewValueTag];
                [self addReceiver: rtpReceiver forKey: rtpReceiver.valueTag];
                NSMutableArray *streamIds = [[NSMutableArray alloc] init];
                for (RTCMediaStream *stream in mediaStreams) {
                    [streamIds addObject: stream.streamId];
                }
                rtpReceiver.streamIds = streamIds;

                [self.bridge.eventDispatcher
                 sendDeviceEventWithName: @"peerConnectionAddedReceiver"
                 body:@{@"valueTag": peerConnection.valueTag,
                        @"receiver": [rtpReceiver json]}];
                 */
        }

        // TODO: rtpReceiverがremoveされたタイミングで呼び出されるobserverが現状存在しない。
        //       別口で調べてみたところ以下のコメントを見つけた
        //       > (shino): Unified plan に onRemoveTrack が来たらこっち = void onTrack(RtpTransceiver transceiver) で対応する。
        //       > 今は SDP semantics に関わらず onAddStream/onRemoveStream でシグナリングに通知している
        //       とのこと、やはり何か実装が足りていない様子。一旦フローと各callbackの呼び出しタイミングを整理する。
        //       ちなみにiOS側のonRemoveTrackに相当する実装は以下の通り
            /*
            if (rtpReceiver.valueTag != nil) {
                [self removeReceiverForKey: rtpReceiver.valueTag];
                [WebRTCValueManager removeValueTagForString: rtpReceiver.receiverId];
            }

            [self.bridge.eventDispatcher
             sendDeviceEventWithName: @"peerConnectionRemoveReceiver"
             body:@{@"valueTag": peerConnection.valueTag,
                    @"receiver": [rtpReceiver json]}];
             */

        @Override
        public void onTrack(RtpTransceiver transceiver) {
                /*
                transceiver.valueTag = [self createNewValueTag];
                [self removeTransceiverForKey: transceiver.valueTag];
                [WebRTCValueManager removeValueTagForObject: transceiver];

                [self.bridge.eventDispatcher
                 sendDeviceEventWithName: @"peerConnectionStartTransceiver"
                 body:@{@"valueTag": peerConnection.valueTag,
                        @"transceiver": [transceiver json]}];
                 */
        }

        @Override
        public void onDataChannel(DataChannel dataChannel) {
            // DataChannel は現在対応しない
        }

        @Override
        public void onRenegotiationNeeded() {
                /*
                [self.bridge.eventDispatcher sendDeviceEventWithName:@"peerConnectionShouldNegotiate"
                                                body:@{@"valueTag": peerConnection.valueTag}];
                 */
        }
    };

    //endregion

    @NonNull
    EglBase.Context getEglContext() {
        return eglBase.getEglBaseContext();
    }

    @NonNull
    private String createNewValueTag() {
        return UUID.randomUUID().toString();
    }

    // TODO: Move this to WebRTCConverter, maybe that should be better
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
