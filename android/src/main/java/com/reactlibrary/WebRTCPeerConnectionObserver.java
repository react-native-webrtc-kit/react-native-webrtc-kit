package com.reactlibrary;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.RtpReceiver;
import org.webrtc.RtpTransceiver;

final class WebRTCPeerConnectionObserver implements PeerConnection.Observer {

    @NonNull
    private final ReactContext reactContext;
    @Nullable
    PeerConnection peerConnection = null;

    WebRTCPeerConnectionObserver(@NonNull final ReactContext reactContext) {
        this.reactContext = reactContext;
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
        if (peerConnection != null) {
            // TODO: remove the peer connection from the module repository before disposing it
            peerConnection.dispose();
            peerConnection = null;
        }
    }

    //region PeerConnection.Observer

    @Override
    public void onSignalingChange(PeerConnection.SignalingState newSignalingState) {
        final WritableMap params = Arguments.createMap();
        params.putString("valueTag", null); // TODO: peerConnection.valueTag somehow
        params.putString("signalingState", null); // TODO:  [WebRTCUtils stringForSignalingState:newSignalingState]
        sendDeviceEvent("peerConnectionSignalingStateChanged", params);
    }

    @Override
    public void onIceConnectionChange(PeerConnection.IceConnectionState newIceConnectionState) {
        final WritableMap params = Arguments.createMap();
        params.putString("valueTag", null); // TODO: peerConnection.valueTag somehow
        params.putString("iceConnectionState", null); // TODO:  [WebRTCUtils stringForICEConnectionState:newIceConnectionState]
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

    //endregion

}
