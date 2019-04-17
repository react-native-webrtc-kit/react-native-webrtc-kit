package com.reactlibrary;

import android.support.annotation.NonNull;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import org.webrtc.MediaStreamTrack;
import org.webrtc.PeerConnection;
import org.webrtc.RtpParameters;
import org.webrtc.RtpReceiver;
import org.webrtc.RtpSender;
import org.webrtc.RtpTransceiver;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class WebRTCConverter {

    //region RtpParameters

    @NonNull
    static WritableMap rtpParametersJsonValue(@NonNull final RtpParameters parameters) {
        final WritableMap rtcp = Arguments.createMap();
        rtcp.putString("cname", parameters.getRtcp().getCname());
        rtcp.putBoolean("reducedSize", parameters.getRtcp().getReducedSize());

        final WritableArray headerExtensions = Arguments.createArray();
        for (final RtpParameters.HeaderExtension extension : parameters.getHeaderExtensions()) {
            final WritableMap obj = Arguments.createMap();
            obj.putString("uri", extension.getUri());
            obj.putInt("id", extension.getId());
            obj.putBoolean("encrypted", extension.getEncrypted());
            headerExtensions.pushMap(obj);
        }

        final WritableArray encodings = Arguments.createArray();
        for (final RtpParameters.Encoding encoding : parameters.encodings) {
            final WritableMap obj = Arguments.createMap();
            obj.putBoolean("active", encoding.active);
            if (encoding.maxBitrateBps != null) {
                obj.putInt("maxBitrate", encoding.maxBitrateBps);
            }
            if (encoding.minBitrateBps != null) {
                obj.putInt("minBitrate", encoding.minBitrateBps);
            }
            if (encoding.ssrc != null) {
                // XXX: putLong()が存在しない、putIntでは桁落ちする危険性がある、putDoubleでは元の値と異なる値になって壊れる危険性がある、参った
                //      一応putIntで対応するが、桁落ちした場合はputDoubleを試すしかないかもしれない
                obj.putInt("ssrc", encoding.ssrc.intValue());
            }
            encodings.pushMap(obj);
        }

        final WritableArray codecs = Arguments.createArray();
        for (final RtpParameters.Codec codec : parameters.codecs) {
            final WritableMap obj = Arguments.createMap();
            obj.putInt("payloadType", codec.payloadType);
            // XXX: 一体全体なぜかわからないが肝心のkindだけ何故かinternalになっていて普通に取り出せないのでreflectionします、最低最悪、絶対libwebrtcのバグだと思うが他に手がないです
            try {
                final Field kindField = codec.getClass().getField("kind");
                final MediaStreamTrack.MediaType kind = (MediaStreamTrack.MediaType) kindField.get(codec);
                switch (kind) {
                    case MEDIA_TYPE_AUDIO:
                        obj.putString("mimeType", String.format("audio/%s", codec.name));
                    case MEDIA_TYPE_VIDEO:
                        obj.putString("mimeType", String.format("video/%s", codec.name));
                }
            } catch (final Throwable e) {
                // Do nothing
            }
            final WritableMap parametersObj = Arguments.createMap();
            for (final Map.Entry<String, String> entry : codec.parameters.entrySet()) {
                parametersObj.putString(entry.getKey(), entry.getValue());
            }
            obj.putMap("parameters", parametersObj);
            if (codec.clockRate != null) {
                obj.putInt("clockRate", codec.clockRate);
            }
            if (codec.numChannels != null) {
                obj.putInt("channels", codec.numChannels);
            }
            codecs.pushMap(obj);
        }

        final WritableMap json = Arguments.createMap();
        json.putString("transactionId", parameters.transactionId);
        json.putMap("rtcp", rtcp);
        json.putArray("headerExtensions", headerExtensions);
        json.putArray("encodings", encodings);
        json.putArray("codecs", codecs);
        return json;
    }

    //endregion

    //region RtpSender

    @NonNull
    static WritableMap rtpSenderJsonValue(@NonNull final RtpSender sender,
                                          @NonNull final WebRTCRepository repository) {
        final String valueTag = repository.senders.getValueTag(sender.id());
        final List<String> streamIds = repository.getStreamIdsForSender(sender);
        if (streamIds == null) throw new IllegalStateException();

        final WritableArray streamIdsArray = Arguments.createArray();
        for (final String streamId : streamIds) {
            streamIdsArray.pushString(streamId);
        }

        final WritableMap json = Arguments.createMap();
        json.putString("id", sender.id());
        json.putMap("parameters", rtpParametersJsonValue(sender.getParameters()));
        json.putArray("streamIds", streamIdsArray);
        if (valueTag != null) {
            json.putString("valueTag", valueTag);
        }
        // iOSの実装と異なり、Androidでは RtpReceiver.track() はコンストラクタの地点で生成・準備されているため、呼び出し時に突然trackの新規インスタンスが生成されることはない。
        // したがってここでは単純にtrackをJSONとして追加するだけで良い。
        final MediaStreamTrack track = sender.track();
        if (track != null) {
            json.putMap("track", mediaStreamTrackJsonValue(track, repository));
        }
        return json;
    }

    //endregion

    //region RtpReceiver

    @NonNull
    static WritableMap rtpReceiverJsonValue(@NonNull final RtpReceiver receiver,
                                            @NonNull final WebRTCRepository repository) {
        final String valueTag = repository.receivers.getValueTag(receiver.id());
        final List<String> streamIds = repository.getStreamIdsForReceiver(receiver);
        if (streamIds == null) throw new IllegalStateException();

        final WritableArray streamIdsArray = Arguments.createArray();
        for (final String streamId : streamIds) {
            streamIdsArray.pushString(streamId);
        }

        final WritableMap json = Arguments.createMap();
        json.putString("id", receiver.id());
        json.putMap("parameters", rtpParametersJsonValue(receiver.getParameters()));
        json.putArray("streamIds", streamIdsArray);
        if (valueTag != null) {
            json.putString("valueTag", valueTag);
        }
        // iOSの実装と異なり、Androidでは RtpReceiver.track() はコンストラクタの地点で生成・準備されているため、呼び出し時に突然trackの新規インスタンスが生成されることはない。
        // したがってここでは単純にtrackをJSONとして追加するだけで良い。
        final MediaStreamTrack track = receiver.track();
        if (track != null) {
            json.putMap("track", mediaStreamTrackJsonValue(track, repository));
        }
        return json;
    }

    //endregion

    //region RtpTransceiver

    @NonNull
    static WritableMap rtpTransceiverJsonValue(@NonNull final RtpTransceiver transceiver,
                                               @NonNull final WebRTCRepository repository) {
        final String valueTag = repository.transceivers.getValueTag(transceiver.getMid());
        final WritableMap json = Arguments.createMap();
        json.putString("mid", transceiver.getMid());
        json.putMap("sender", rtpSenderJsonValue(transceiver.getSender(), repository));
        json.putMap("receiver", rtpReceiverJsonValue(transceiver.getReceiver(), repository));
        json.putBoolean("stopped", transceiver.isStopped());
        if (valueTag != null) {
            json.putString("valueTag", valueTag);
        }
        return json;
    }

    //endregion

    //region RtpTransceiver.RtpTransceiverDirection

    @NonNull
    static RtpTransceiver.RtpTransceiverDirection rtpTransceiverDirection(@NonNull final String string) {
        switch (string) {
            case "sendrecv":
                return RtpTransceiver.RtpTransceiverDirection.SEND_RECV;
            case "sendonly":
                return RtpTransceiver.RtpTransceiverDirection.SEND_ONLY;
            case "recvonly":
                return RtpTransceiver.RtpTransceiverDirection.RECV_ONLY;
            case "inactive":
                return RtpTransceiver.RtpTransceiverDirection.INACTIVE;
            default:
                throw new IllegalArgumentException("invalid direction string " + string);
        }
    }

    @NonNull
    static String rtpTransceiverDirectionStringValue(@NonNull final RtpTransceiver.RtpTransceiverDirection direction) {
        switch (direction) {
            case SEND_RECV:
                return "sendrecv";
            case SEND_ONLY:
                return "sendonly";
            case RECV_ONLY:
                return "recvonly";
            case INACTIVE:
                return "inactive";
            default:
                throw new IllegalArgumentException("invalid direction " + direction);
        }
    }

    //endregion


    //region PeerConnection.IceServer

    @NonNull
    static PeerConnection.IceServer iceServer(@NonNull final ReadableMap json) {
        final ReadableArray urlsJson = json.getArray("urls");
        if (urlsJson == null) {
            throw new NullPointerException("RTCIceServer.urls");
        } else if (urlsJson.size() == 0) {
            throw new IllegalArgumentException("RTCIceServer.urls is empty");
        }
        final List<String> urls = toStringList(urlsJson);
        final PeerConnection.IceServer.Builder builder = PeerConnection.IceServer.builder(urls);
        final String usernameString = json.getString("username");
        if (usernameString != null) {
            builder.setUsername(usernameString);
        }
        final String credentialString = json.getString("credential");
        if (credentialString != null) {
            builder.setPassword(credentialString);
        }

        return builder.createIceServer();
    }

    //endregion


    //region PeerConnection.RTCConfiguration

    @NonNull
    static PeerConnection.RTCConfiguration rtcConfiguration(@NonNull final ReadableMap json) {
        final ReadableArray iceServersJson = json.getArray("iceServers");
        final List<PeerConnection.IceServer> iceServers = new ArrayList<>();
        if (iceServersJson != null) {
            for (int i = 0; i < iceServersJson.size(); i++) {
                final ReadableMap iceServerJson = iceServersJson.getMap(i);
                if (iceServerJson == null) {
                    throw new NullPointerException("each RTCConfiguration.iceServers");
                }
                iceServers.add(iceServer(iceServerJson));
            }
        }

        final PeerConnection.RTCConfiguration configuration = new PeerConnection.RTCConfiguration(iceServers);

        final String policyString = json.getString("iceTransportPolicy");
        if (policyString == null) {
            throw new NullPointerException("RTCConfiguration.iceTransportPolicy");
        }
        configuration.iceTransportsType = iceTransportsType(policyString);


        final String semanticsString = json.getString("sdpSemantics");
        if (semanticsString == null) {
            throw new NullPointerException("RTCConfiguration.sdpSemantics");
        }
        configuration.sdpSemantics = sdpSemantics(semanticsString);

        return configuration;
    }

    //endregion


    //region PeerConnection.IceTransportsType

    @NonNull
    static PeerConnection.IceTransportsType iceTransportsType(@NonNull final String string) {
        switch (string) {
            case "none":
                return PeerConnection.IceTransportsType.NONE;
            case "relay":
                return PeerConnection.IceTransportsType.RELAY;
            case "nohost":
                return PeerConnection.IceTransportsType.NOHOST;
            case "all":
                return PeerConnection.IceTransportsType.ALL;
            default:
                throw new IllegalArgumentException("invalid type string " + string);
        }
    }

    //endregion


    //region PeerConnection.SdpSemantics

    @NonNull
    static PeerConnection.SdpSemantics sdpSemantics(@NonNull final String string) {
        switch (string) {
            case "planb":
                return PeerConnection.SdpSemantics.PLAN_B;
            case "unified":
                return PeerConnection.SdpSemantics.UNIFIED_PLAN;
            default:
                throw new IllegalArgumentException("invalid semantics string " + string);
        }
    }

    //endregion


    //region PeerConnection.SignalingState

    @NonNull
    static String signalingStateStringValue(@NonNull final PeerConnection.SignalingState signalingState) {
        switch (signalingState) {
            case STABLE:
                return "stable";
            case HAVE_LOCAL_OFFER:
                return "have-local-offer";
            case HAVE_LOCAL_PRANSWER:
                return "have-local-pranswer";
            case HAVE_REMOTE_OFFER:
                return "have-remote-offer";
            case HAVE_REMOTE_PRANSWER:
                return "have-remote-pranswer";
            case CLOSED:
                return "closed";
            default:
                throw new IllegalArgumentException("invalid signalingState");
        }
    }

    //endregion


    //region PeerConnection.PeerConnectionState
    //endregion


    //region PeerConnection.IceConnectionState

    @NonNull
    static String iceConnectionStateStringValue(@NonNull final PeerConnection.IceConnectionState iceConnectionState) {
        switch (iceConnectionState) {
            case NEW:
                return "new";
            case CHECKING:
                return "checking";
            case CONNECTED:
                return "connected";
            case COMPLETED:
                return "completed";
            case FAILED:
                return "failed";
            case DISCONNECTED:
                return "disconnected";
            case CLOSED:
                return "closed";
            // XXX: IceConnectionState.COUNT がAndroid SDKに存在しないので、COUNTが飛んできたら多分死ぬ。SDK側が対応しない限りどうしようもない。
            default:
                throw new IllegalArgumentException("invalid iceConnectionState");
        }
    }

    //endregion


    //region PeerConnection.IceGatheringState

    @NonNull
    static String iceGatheringStateStringValue(@NonNull final PeerConnection.IceGatheringState iceGatheringState) {
        switch (iceGatheringState) {
            case NEW:
                return "new";
            case GATHERING:
                return "gathering";
            case COMPLETE:
                return "complete";
            default:
                throw new IllegalArgumentException("invalid iceGatheringState");
        }
    }

    //endregion


    //region MediaStreamTrack

    @NonNull
    static WritableMap mediaStreamTrackJsonValue(@NonNull final MediaStreamTrack track,
                                                 @NonNull final WebRTCRepository repository) {
        final String valueTag = repository.tracks.getValueTag(track.id());
        final WritableMap json = Arguments.createMap();
        json.putString("id", track.id());
        json.putBoolean("enabled", track.enabled());
        json.putString("kind", track.kind());
        json.putString("readyState", mediaStreamTrackStateStringValue(track.state()));
        if (valueTag != null) {
            json.putString("valueTag", valueTag);
        }
        return json;
    }

    //endregion


    //region MediaStreamTrack.State

    @NonNull
    static String mediaStreamTrackStateStringValue(@NonNull final MediaStreamTrack.State state) {
        switch (state) {
            case LIVE:
                return "live";
            case ENDED:
                return "ended";
            default:
                throw new IllegalArgumentException("invalid mediaStreamTrackState");
        }
    }

    //endregion


    @NonNull
    static List<String> toStringList(@NonNull final ReadableArray arrayJson) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < arrayJson.size(); i++) {
            final String value = arrayJson.getString(i);
            if (value == null) {
                throw new IllegalStateException(String.format("Index %d is not a string: %s", i, arrayJson));
            }
            result.add(value);
        }
        return result;
    }

}
