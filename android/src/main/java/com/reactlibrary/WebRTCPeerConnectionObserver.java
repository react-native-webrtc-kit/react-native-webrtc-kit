package com.reactlibrary;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.MediaStreamTrack;
import org.webrtc.PeerConnection;
import org.webrtc.RtpReceiver;
import org.webrtc.RtpTransceiver;

import static com.reactlibrary.WebRTCConverter.iceConnectionStateStringValue;
import static com.reactlibrary.WebRTCConverter.iceGatheringStateStringValue;
import static com.reactlibrary.WebRTCConverter.rtpReceiverJsonValue;
import static com.reactlibrary.WebRTCConverter.rtpTransceiverJsonValue;
import static com.reactlibrary.WebRTCConverter.signalingStateStringValue;

final class WebRTCPeerConnectionObserver implements PeerConnection.Observer {

    @NonNull
    private final ReactContext reactContext;
    /**
     * First is valueTag, Seconds is PeerConnection.
     */
    @Nullable
    Pair<String, PeerConnection> peerConnectionPair = null;

    WebRTCPeerConnectionObserver(@NonNull final ReactContext reactContext) {
        this.reactContext = reactContext;
    }

    @NonNull
    private WebRTCModule getModule() {
        return reactContext.getNativeModule(WebRTCModule.class);
    }

    /**
     * Sends out an event to JavaScript.
     * https://facebook.github.io/react-native/docs/native-modules-android#sending-events-to-javascript
     */
    private void sendDeviceEvent(@NonNull final String eventName,
                                 @Nullable final WritableMap params) {
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, params);
    }

    /**
     * Closes the PeerConnection managed by this observer, completely disposing it from everywhere.
     */
    private void closeAndFinish() {
        if (peerConnectionPair != null) {
            getModule().repository.removePeerConnectionByValueTag(peerConnectionPair.first);
            peerConnectionPair.second.dispose();
            peerConnectionPair = null;
        }
    }

    //region PeerConnection.Observer

    @Override
    public void onSignalingChange(@NonNull final PeerConnection.SignalingState newSignalingState) {
        if (peerConnectionPair == null) return;
        final WritableMap params = Arguments.createMap();
        params.putString("valueTag", peerConnectionPair.first);
        params.putString("signalingState", signalingStateStringValue(newSignalingState));
        sendDeviceEvent("peerConnectionSignalingStateChanged", params);
    }

    @Override
    public void onIceConnectionChange(@NonNull final PeerConnection.IceConnectionState newIceConnectionState) {
        if (peerConnectionPair == null) return;
        final WritableMap params = Arguments.createMap();
        params.putString("valueTag", peerConnectionPair.first);
        params.putString("iceConnectionState", iceConnectionStateStringValue(newIceConnectionState));
        sendDeviceEvent("peerConnectionIceConnectionChanged", params);
        switch (newIceConnectionState) {
            case NEW:
                break;
            case CHECKING:
                break;
            case CONNECTED:
                break;
            case COMPLETED:
                break;
            case FAILED:
                closeAndFinish();
                break;
            case DISCONNECTED:
            case CLOSED:
                closeAndFinish();
                break;
        }
    }

    @Override
    public void onIceConnectionReceivingChange(boolean b) {
        // Do nothing
        // JS側へのイベント通知は無し
    }

    @Override
    public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
        if (peerConnectionPair == null) return;
        final WritableMap params = Arguments.createMap();
        params.putString("valueTag", peerConnectionPair.first);
        params.putString("iceGatheringState", iceGatheringStateStringValue(iceGatheringState));
        sendDeviceEvent("peerConnectionIceGatheringChanged", params);
    }

    @Override
    public void onIceCandidate(IceCandidate iceCandidate) {
        if (peerConnectionPair == null) return;
        final WritableMap candidate = Arguments.createMap();
        candidate.putString("candidate", iceCandidate.sdp);
        candidate.putInt("sdpMLineIndex", iceCandidate.sdpMLineIndex);
        candidate.putString("sdpMid", iceCandidate.sdpMid);
        final WritableMap params = Arguments.createMap();
        params.putString("valueTag", peerConnectionPair.first);
        params.putMap("candidate", candidate);
        sendDeviceEvent("peerConnectionGotICECandidate", params);
    }

    @Override
    public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
        if (peerConnectionPair == null) return;
        peerConnectionPair.second.removeIceCandidates(iceCandidates);
        // JS側へのイベント通知は無し
    }

    @Override
    public void onAddStream(MediaStream mediaStream) {
        if (peerConnectionPair == null) return;
        final WebRTCModule module = getModule();
        final Pair<String, MediaStream> streamPair = new Pair<>(module.createNewValueTag(), mediaStream);
        module.repository.addStream(streamPair);

        // XXX: Preserved Video Trackについては現在無視しているがこれも管理したほうが良いか？
        for (final MediaStreamTrack track : mediaStream.videoTracks) {
            module.repository.tracks.add(track.id(), module.createNewValueTag(), track);
        }
        for (final MediaStreamTrack track : mediaStream.audioTracks) {
            module.repository.tracks.add(track.id(), module.createNewValueTag(), track);
        }
        // JS側へのイベント通知は無し (Unified Plan移行につき、旧Plan BのStreamベースのdeprecatedイベント通知は使用しない)
        // XXX: libwebrtc AndroidにonRemoveTrack()が存在しないため、現状JS側がstream/trackをremoveするのに適したイベントが一切存在しない状態になってしまっている
        //      iOS側はonRemoveTrack()の実装があるので `peerConnectionRemoveReceiver` イベントをJS側に供給できているが、Android側はその実装が不可能。
        //      一旦はその状態でおいておくが後々必ず修正が必要。
    }

    @Override
    public void onRemoveStream(MediaStream mediaStream) {
        if (peerConnectionPair == null) return;
        getModule().repository.removeStreamById(mediaStream.getId());
        // このstream管理下のtrackはrepositoryから削除しなくてもよい
        // trackも削除したい場合は利用者側で明示的にremoveTrack()する仕様となっている

        // TODO: onRemoveTrack()に相当するコールバックがAndroid実装にないので、代わりと言っては何だがここでonRemoveTrackに相当するイベントをJS側に投げてみたらどうか？
        //       > なので、1track が 多くとも1stream という制限であれば native/Java の onRemoveStream から JS の onRemoveTrack よべばいいのかなぁと。
        //       確かに合理的な気がする
    }

    @Override
    public void onAddTrack(RtpReceiver receiver, MediaStream[] mediaStreams) {
        if (peerConnectionPair == null) return;
        final WebRTCModule module = getModule();
        final Pair<String, RtpReceiver> receiverPair = new Pair<>(module.createNewValueTag(), receiver);
        module.repository.receivers.add(receiver.id(), module.createNewValueTag(), receiver);
        module.repository.setStreamIdsForReceiver(receiver, mediaStreams);

        final WritableMap params = Arguments.createMap();
        params.putString("valueTag", peerConnectionPair.first);
        params.putMap("receiver", rtpReceiverJsonValue(receiver, module.repository));
        sendDeviceEvent("peerConnectionAddedReceiver", params);
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
        // XXX: iOS側のSDKだと何故かここで以下のようにtransceiverを削除しているが、絶対におかしい、ここは追加するタイミングのはず
        //      恐らくiOS側が間違っていると判断する
        /*
        transceiver.valueTag = [self createNewValueTag];
        [self removeTransceiverForKey: transceiver.valueTag];
        [WebRTCValueManager removeValueTagForObject: transceiver];
         */
        if (peerConnectionPair == null) return;
        final WebRTCModule module = getModule();
        module.repository.transceivers.add(transceiver.getMid(), module.createNewValueTag(), transceiver);

        final WritableMap params = Arguments.createMap();
        params.putString("valueTag", peerConnectionPair.first);
        params.putMap("transceiver", rtpTransceiverJsonValue(transceiver, module.repository));
        sendDeviceEvent("peerConnectionStartTransceiver", params);
    }

    @Override
    public void onDataChannel(DataChannel dataChannel) {
        // DataChannel は現在対応しない
    }

    @Override
    public void onRenegotiationNeeded() {
        if (peerConnectionPair == null) return;
        final WritableMap params = Arguments.createMap();
        params.putString("valueTag", peerConnectionPair.first);
        sendDeviceEvent("peerConnectionShouldNegotiate", params);
    }

    //endregion

}
