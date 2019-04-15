package com.reactlibrary;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import org.webrtc.MediaStream;
import org.webrtc.MediaStreamTrack;
import org.webrtc.PeerConnection;
import org.webrtc.RtpReceiver;
import org.webrtc.RtpTransceiver;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * WebRTCモジュールが使用するすべてのWebRTC関連のオブジェクト (PeerConnection, MediaStream, MediaStreamTrack等) を管理するリポジトリです。
 * TODO: Maybe I must make all the call synchronized and locked. I don't think that's required since RN doesn't run on mutiple threads... but WebRTC does, so it depends.
 */
final class WebRTCRepository {


    //region PeerConnection

    /**
     * Key is valueTag, Value is PeerConnection.
     */
    private final Map<String, PeerConnection> peerConnectionMap = new HashMap<>();

    void addPeerConnection(@NonNull final Pair<String, PeerConnection> peerConnectionPair) {
        peerConnectionMap.put(peerConnectionPair.first, peerConnectionPair.second);
    }

    void removePeerConnectionByValueTag(@Nullable final String valueTag) {
        if (valueTag == null) {
            return;
        }
        peerConnectionMap.remove(valueTag);
    }

    @Nullable
    PeerConnection getPeerConnectionByValueTag(@Nullable final String valueTag) {
        if (valueTag == null) {
            return null;
        }
        return peerConnectionMap.get(valueTag);
    }

    //endregion


    //region Stream

    /**
     * Key is id, Value is stream.
     */
    private final Map<String, MediaStream> streamMap = new HashMap<>();
    /**
     * Key is id, Value is valueTag. Should better be a BiMap, but not available without Google Guava.
     */
    private final Map<String, String> streamValueTagMap = new HashMap<>();

    void addStream(@NonNull final Pair<String, MediaStream> streamPair) {
        streamMap.put(streamPair.second.getId(), streamPair.second);
        streamValueTagMap.put(streamPair.second.getId(), streamPair.first);
    }

    void removeStreamById(@Nullable final String id) {
        if (id == null) {
            return;
        }
        streamMap.remove(id);
        streamValueTagMap.remove(id);
    }

    void removeStreamByValueTag(@Nullable final String valueTag) {
        final MediaStream stream = getStreamByValueTag(valueTag);
        if (stream != null) {
            removeStreamById(stream.getId());
        }
    }

    @Nullable
    MediaStream getStreamByValueTag(@Nullable final String valueTag) {
        if (valueTag == null) {
            return null;
        }
        for (Map.Entry<String, String> kv : streamValueTagMap.entrySet()) {
            if (kv.getValue().equals(valueTag)) {
                return streamMap.get(kv.getKey());
            }
        }
        return null;
    }

    //endregion


    //region Track

    /**
     * Key is id, Value is track.
     */
    private final Map<String, MediaStreamTrack> trackMap = new HashMap<>();
    /**
     * Key is id, Value is valueTag. Should better be a BiMap, but not available without Google Guava.
     */
    private final Map<String, String> trackValueTagMap = new HashMap<>();
    /**
     * Key is id, Value is aspectRatio.
     */
    private final Map<String, Double> trackAspectRatioMap = new HashMap<>();

    void addTrack(@NonNull final Pair<String, MediaStreamTrack> trackPair) {
        trackMap.put(trackPair.second.id(), trackPair.second);
        trackValueTagMap.put(trackPair.second.id(), trackPair.first);
    }

    private void removeTrackById(@Nullable final String id) {
        if (id == null) {
            return;
        }
        trackMap.remove(id);
        trackValueTagMap.remove(id);
        trackAspectRatioMap.remove(id);
    }

    void removeTrackByValueTag(@Nullable final String valueTag) {
        final MediaStreamTrack track = getTrackByValueTag(valueTag);
        if (track != null) {
            removeTrackById(track.id());
        }
    }

    @Nullable
    MediaStreamTrack getTrackByValueTag(@Nullable final String valueTag) {
        if (valueTag == null) {
            return null;
        }
        for (Map.Entry<String, String> kv : trackValueTagMap.entrySet()) {
            if (kv.getValue().equals(valueTag)) {
                return trackMap.get(kv.getKey());
            }
        }
        return null;
    }

    @Nullable
    VideoTrack getVideoTrackByValueTag(@Nullable final String valueTag) {
        final MediaStreamTrack track = getTrackByValueTag(valueTag);
        if (track instanceof VideoTrack) {
            return (VideoTrack) track;
        } else {
            return null;
        }
    }

    void setVideoTrackAspectRatio(@NonNull final VideoTrack videoTrack, double aspectRatio) {
        if (!trackMap.containsKey(videoTrack.id()) || !trackValueTagMap.containsKey(videoTrack.id())) {
            return;
        }
        trackAspectRatioMap.put(videoTrack.id(), aspectRatio);
    }

    //endregion


    //region RTP Sender
    //endregion


    //region RTP Receiver

    /**
     * Key is id, Value is receiver.
     */
    private final Map<String, RtpReceiver> receiverMap = new HashMap<>();
    /**
     * Key is id, Value is valueTag. Should better be a BiMap, but not available without Google Guava.
     */
    private final Map<String, String> receiverValueTagMap = new HashMap<>();
    /**
     * Key is id, Value is associated stream ids.
     */
    private final Map<String, List<String>> receiverStreamIdsMap = new HashMap<>();

    void addReceiver(@NonNull final Pair<String, RtpReceiver> receiverPair) {
        receiverMap.put(receiverPair.second.id(), receiverPair.second);
        receiverValueTagMap.put(receiverPair.second.id(), receiverPair.first);
    }

//    private void removeTrackById(@Nullable final String id) {
//        if (id == null) {
//            return;
//        }
//        trackMap.remove(id);
//        trackValueTagMap.remove(id);
//        trackAspectRatioMap.remove(id);
//    }
//
//    void removeTrackByValueTag(@Nullable final String valueTag) {
//        final MediaStreamTrack track = getTrackByValueTag(valueTag);
//        if (track != null) {
//            removeTrackById(track.id());
//        }
//    }
//
//    @Nullable
//    MediaStreamTrack getTrackByValueTag(@Nullable final String valueTag) {
//        if (valueTag == null) {
//            return null;
//        }
//        for (Map.Entry<String, String> kv : trackValueTagMap.entrySet()) {
//            if (kv.getValue().equals(valueTag)) {
//                return trackMap.get(kv.getKey());
//            }
//        }
//        return null;
//    }

    void setStreamIdsForReceiver(@NonNull final String receiverId, @Nullable final MediaStream[] mediaStreams) {
        if (mediaStreams == null) {
            receiverStreamIdsMap.remove(receiverId);
            return;
        }
        final List<String> streamIds = new ArrayList<>();
        for (final MediaStream stream : mediaStreams) {
            streamIds.add(stream.getId());
        }
        if (streamIds.size() == 0) {
            receiverStreamIdsMap.remove(receiverId);
            return;
        }
        receiverStreamIdsMap.put(receiverId, streamIds);
    }

    //endregion


    //region RTP Transceiver

    /**
     * Key is valueTag, Value is transceiver.
     */
    private final Map<String, RtpTransceiver> transceiverMap = new HashMap<>();

    void addTransceiver(@NonNull final RtpTransceiver transceiver, @NonNull final String valueTag) {
        transceiverMap.put(valueTag, transceiver);
    }

    void removeTransceiverByValueTag(@Nullable final String valueTag) {
        if (valueTag == null) {
            return;
        }
        transceiverMap.remove(valueTag);
    }

    @Nullable
    RtpTransceiver getTransceiverByValueTag(@Nullable final String valueTag) {
        if (valueTag == null) {
            return null;
        }
        return transceiverMap.get(valueTag);
    }

    //endregion


    /**
     * このリポジトリの中身を完全に空にします。
     * その際、格納されていたWebRTC関連のオブジェクトは、現在のところ、明示的に初期化されません。
     * ※将来的にこの挙動は変更される可能性があります。
     */
    void clear() {
        peerConnectionMap.clear();

        streamMap.clear();
        streamValueTagMap.clear();

        trackMap.clear();
        trackValueTagMap.clear();
        trackAspectRatioMap.clear();

        receiverMap.clear();
        receiverValueTagMap.clear();
        receiverStreamIdsMap.clear();

        transceiverMap.clear();
    }

}
