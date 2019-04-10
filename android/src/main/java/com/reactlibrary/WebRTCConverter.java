package com.reactlibrary;

import android.support.annotation.NonNull;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;

import org.webrtc.PeerConnection;
import org.webrtc.RtpTransceiver;

import java.util.ArrayList;
import java.util.List;

final class WebRTCConverter {

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
        final List<String> urls = new ArrayList<>();
        for (int i = 0; i < urlsJson.size(); i++) {
            final String url = urlsJson.getString(i);
            if (url == null) {
                throw new NullPointerException("each RTCIceServer.urls");
            }
            urls.add(url);
        }

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

}