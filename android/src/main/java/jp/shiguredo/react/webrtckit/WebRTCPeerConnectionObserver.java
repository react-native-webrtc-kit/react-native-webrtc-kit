package jp.shiguredo.react.webrtckit;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import android.util.Log;
import android.util.Pair;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.queue.ReactQueueConfiguration;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.MediaStreamTrack;
import org.webrtc.PeerConnection;
import org.webrtc.RtpReceiver;
import org.webrtc.RtpSender;
import org.webrtc.RtpTransceiver;

import static jp.shiguredo.react.webrtckit.WebRTCConverter.peerConnectionStateStringValue;
import static jp.shiguredo.react.webrtckit.WebRTCConverter.iceConnectionStateStringValue;
import static jp.shiguredo.react.webrtckit.WebRTCConverter.iceGatheringStateStringValue;
import static jp.shiguredo.react.webrtckit.WebRTCConverter.rtpReceiverJsonValue;
import static jp.shiguredo.react.webrtckit.WebRTCConverter.rtpTransceiverJsonValue;
import static jp.shiguredo.react.webrtckit.WebRTCConverter.signalingStateStringValue;

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
     * Closes the PeerConnection managed by this observer in response to the given callbacks.
     * <p>
     * Important thing is PeerConenction.close() / PeerConenction.dispose() MUST NOT BE called
     * in the same runloop of the PeerConnection.Observer callbacks, meaning we have to dispatch the close call asynchronously.
     * https://bugs.chromium.org/p/webrtc/issues/detail?id=3721
     * This behavior is even noted in the PeerConnection.close() documentation.
     */
    private void closeAndFinish() {
        if (peerConnectionPair != null) {
            Log.d("WebRTCModule", "closeAndFinish()[" + peerConnectionPair.first + "]");
            final WebRTCModule module = getModule();
            final ReactQueueConfiguration queueConfiguration = module.getReactContext().getCatalystInstance().getReactQueueConfiguration();
            final String valueTag = peerConnectionPair.first;
            peerConnectionPair = null;
            queueConfiguration.getNativeModulesQueueThread().runOnQueue(() -> {
                module.peerConnectionClose(valueTag);
            });
        }
    }

    //region PeerConnection.Observer

    @Override
    public void onSignalingChange(@NonNull final PeerConnection.SignalingState newSignalingState) {
        if (peerConnectionPair == null) return;
        Log.d("WebRTCModule", "onSignalingChange()[" + peerConnectionPair.first + "] - newSignalingState=" + newSignalingState);
        final WritableMap params = Arguments.createMap();
        params.putString("valueTag", peerConnectionPair.first);
        params.putString("signalingState", signalingStateStringValue(newSignalingState));
        sendDeviceEvent("peerConnectionSignalingStateChanged", params);
    }

    @Override
    public  void onConnectionChange(@NonNull final PeerConnection.PeerConnectionState newState) {
        if (peerConnectionPair == null) return;
        Log.d("WebRTCModule", "onConnectionChange()[" + peerConnectionPair.first + "] - newState=" + newState);
        final WritableMap params = Arguments.createMap();
        params.putString("valueTag", peerConnectionPair.first);
        params.putString("connectionState", peerConnectionStateStringValue(newState));
        sendDeviceEvent("peerConnectionConnectionStateChanged", params);
    }

    @Override
    public void onIceConnectionChange(@NonNull final PeerConnection.IceConnectionState newIceConnectionState) {
        if (peerConnectionPair == null) return;
        Log.d("WebRTCModule", "onIceConnectionChange()[" + peerConnectionPair.first + "] - newIceConnectionState=" + newIceConnectionState);
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
        Log.d("WebRTCModule", "onIceGatheringChange()[" + peerConnectionPair.first + "] - iceGatheringState=" + iceGatheringState);
        final WritableMap params = Arguments.createMap();
        params.putString("valueTag", peerConnectionPair.first);
        params.putString("iceGatheringState", iceGatheringStateStringValue(iceGatheringState));
        sendDeviceEvent("peerConnectionIceGatheringChanged", params);
    }

    @Override
    public void onIceCandidate(IceCandidate iceCandidate) {
        if (peerConnectionPair == null) return;
        Log.d("WebRTCModule", "onIceCandidate()[" + peerConnectionPair.first + "] - iceCandidate=" + iceCandidate);
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
        Log.d("WebRTCModule", "onIceCandidatesRemoved()[" + peerConnectionPair.first + "]");
        peerConnectionPair.second.removeIceCandidates(iceCandidates);
        // JS側へのイベント通知は無し
    }

    @Override
    public void onAddStream(MediaStream mediaStream) {
        if (peerConnectionPair == null) return;
        Log.d("WebRTCModule", "onAddStream()[" + peerConnectionPair.first + "] - mediaStream=" + mediaStream);
        final WebRTCModule module = getModule();
        module.repository.streams.add(mediaStream.getId(), module.createNewValueTag(), mediaStream);

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
        Log.d("WebRTCModule", "onRemoveStream()[" + peerConnectionPair.first + "] - mediaStream=" + mediaStream);
        final WebRTCModule module = getModule();
        module.repository.streams.removeById(mediaStream.getId());
        // このstream管理下のtrackはrepositoryから削除しなくてもよい
        // trackも削除したい場合は利用者側で明示的にremoveTrack()する仕様となっている
    }

    @Override
    public void onAddTrack(RtpReceiver receiver, MediaStream[] mediaStreams) {
        if (peerConnectionPair == null) return;
        Log.d("WebRTCModule", "onAddTrack()[" + peerConnectionPair.first + "] - receiver=" + receiver);
        final WebRTCModule module = getModule();
        final Pair<String, RtpReceiver> receiverPair = new Pair<>(module.createNewValueTag(), receiver);
        module.repository.receivers.add(receiver.id(), module.createNewValueTag(), receiver);
        final MediaStreamTrack track = receiver.track();
        if (track != null) {
            module.repository.tracks.add(track.id(), module.createNewValueTag(), track);
        }
        module.repository.setStreamIdsForReceiver(receiver, mediaStreams);

        final WritableMap params = Arguments.createMap();
        params.putString("valueTag", peerConnectionPair.first);
        params.putMap("receiver", rtpReceiverJsonValue(receiver, module.repository));
        sendDeviceEvent("peerConnectionAddedReceiver", params);
    }

    // TODO: rtpReceiverがremoveされたタイミングで呼び出されるobserverが現状存在しないため未実装になっている
    //       別口で調べてみたところ以下のコメントを見つけた
    //       > (shino): Unified plan に onRemoveTrack が来たらこっち = void onTrack(RtpTransceiver transceiver) で対応する。
    //       > 今は SDP semantics に関わらず onAddStream/onRemoveStream でシグナリングに通知している
    //       現在libwebrtcのAndroid側実装にonRemoveTrackが存在しないため上記の様なワークアラウンドをするしかないようだが、
    //       Streamに複数Trackが追加されている場合、onRemoveStreamを経由しないため、やはりすべてのケースには対応できない

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
        Log.d("WebRTCModule", "onTrack()[" + peerConnectionPair.first + "] - transceiver=" + transceiver);
        final WebRTCModule module = getModule();
        module.repository.transceivers.add(transceiver.getMid(), module.createNewValueTag(), transceiver);
        final RtpSender sender = transceiver.getSender();
        final MediaStreamTrack senderTrack = sender.track();
        final RtpReceiver receiver = transceiver.getReceiver();
        final MediaStreamTrack receiverTrack = receiver.track();
        module.repository.senders.add(sender.id(), module.createNewValueTag(), sender);
        module.repository.receivers.add(receiver.id(), module.createNewValueTag(), receiver);
        if (senderTrack != null) {
            module.repository.tracks.add(senderTrack.id(), module.createNewValueTag(), senderTrack);
        }
        if (receiverTrack != null) {
            module.repository.tracks.add(receiverTrack.id(), module.createNewValueTag(), receiverTrack);
        }
        // XXX: 本来であればここで sender.streams() や receiver.streams() を使ってstreamIdsを取得し、repository.setStreamIds(...)とする必要がある
        //      しかしながら現在libwebrtcに sender.streams() や receiver.streams() の実装がないため実現不能

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
        Log.d("WebRTCModule", "onRenegotiationNeeded()[" + peerConnectionPair.first + "]");
        final WritableMap params = Arguments.createMap();
        params.putString("valueTag", peerConnectionPair.first);
        sendDeviceEvent("peerConnectionShouldNegotiate", params);
    }

    //endregion

}
