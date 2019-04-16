package com.reactlibrary;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import org.webrtc.MediaStream;
import org.webrtc.MediaStreamTrack;
import org.webrtc.PeerConnection;
import org.webrtc.RtpReceiver;
import org.webrtc.RtpSender;
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

    final DualKeyMap<MediaStreamTrack> tracks = new DualKeyMap<>();
    /**
     * Key is id, Value is aspectRatio.
     */
    private final Map<String, Double> trackAspectRatioMap = new HashMap<>();

    void setVideoTrackAspectRatio(@NonNull final VideoTrack videoTrack, double aspectRatio) {
        if (!tracks.containsId(videoTrack.id())) {
            return;
        }
        trackAspectRatioMap.put(videoTrack.id(), aspectRatio);
    }

    //endregion


    //region RTP Sender

    final DualKeyMap<RtpSender> senders = new DualKeyMap<>();
    /**
     * Key is id, Value is associated stream ids.
     */
    private final Map<String, List<String>> senderStreamIdsMap = new HashMap<>();

    @Nullable
    List<String> getStreamIdsForSender(@NonNull final RtpSender sender) {
        return receiverStreamIdsMap.get(sender.id());
    }

    void setStreamIdsForSender(@NonNull final RtpSender sender, @Nullable final MediaStream[] mediaStreams) {
        if (mediaStreams == null) {
            receiverStreamIdsMap.remove(sender.id());
            return;
        }
        final List<String> streamIds = new ArrayList<>();
        for (final MediaStream stream : mediaStreams) {
            streamIds.add(stream.getId());
        }
        if (streamIds.size() == 0) {
            receiverStreamIdsMap.remove(sender.id());
            return;
        }
        receiverStreamIdsMap.put(sender.id(), streamIds);
    }

    //endregion


    //region RTP Receiver

    final DualKeyMap<RtpReceiver> receivers = new DualKeyMap<>();
    /**
     * Key is id, Value is associated stream ids.
     */
    private final Map<String, List<String>> receiverStreamIdsMap = new HashMap<>();

    @Nullable
    List<String> getStreamIdsForReceiver(@NonNull final RtpReceiver receiver) {
        return receiverStreamIdsMap.get(receiver.id());
    }

    void setStreamIdsForReceiver(@NonNull final RtpReceiver receiver, @Nullable final MediaStream[] mediaStreams) {
        if (mediaStreams == null) {
            receiverStreamIdsMap.remove(receiver.id());
            return;
        }
        final List<String> streamIds = new ArrayList<>();
        for (final MediaStream stream : mediaStreams) {
            streamIds.add(stream.getId());
        }
        if (streamIds.size() == 0) {
            receiverStreamIdsMap.remove(receiver.id());
            return;
        }
        receiverStreamIdsMap.put(receiver.id(), streamIds);
    }

    //endregion


    //region RTP Transceiver

    final DualKeyMap<RtpTransceiver> transceivers = new DualKeyMap<>();

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

        tracks.clear();
        trackAspectRatioMap.clear();

        senders.clear();

        receivers.clear();
        receiverStreamIdsMap.clear();

        transceivers.clear();
    }

    static final class DualKeyMap<V> {
        @NonNull
        private final Map<String, V> idMap = new HashMap<>();
        @NonNull
        private final Map<String, String> idToValueTag = new HashMap<>();
        @NonNull
        private final Map<String, String> valueTagToId = new HashMap<>();

        void add(@NonNull final String id, @NonNull final String valueTag, @NonNull final V value) {
            idMap.put(id, value);
            idToValueTag.put(id, valueTag);
            valueTagToId.put(valueTag, id);
        }

        @Nullable
        String getId(@Nullable final String valueTag) {
            if (valueTag == null) return null;
            return valueTagToId.get(valueTag);
        }

        @Nullable
        String getValueTag(@Nullable final String id) {
            if (id == null) return null;
            return idToValueTag.get(id);
        }

        boolean containsId(@Nullable final String id) {
            if (id == null) return false;
            return idToValueTag.containsKey(id);
        }

        boolean containsValueTag(@Nullable final String valueTag) {
            if (valueTag == null) return false;
            return valueTagToId.containsKey(valueTag);
        }

        @Nullable
        V getById(@Nullable final String id) {
            if (id == null) return null;
            return idMap.get(id);
        }

        @Nullable
        V getByValueTag(@Nullable final String valueTag) {
            if (valueTag == null) return null;
            final String id = valueTagToId.get(valueTag);
            if (id == null) return null;
            return idMap.get(id);
        }

        void removeById(@Nullable final String id) {
            if (id == null) return;
            final String valueTag = idToValueTag.get(id);
            if (valueTag == null) return;
            idMap.remove(id);
            idToValueTag.remove(id);
            valueTagToId.remove(valueTag);
        }

        void removeByValueTag(@Nullable final String valueTag) {
            if (valueTag == null) return;
            final String id = valueTagToId.get(valueTag);
            if (id == null) return;
            idMap.remove(id);
            idToValueTag.remove(id);
            valueTagToId.remove(valueTag);
        }

        void clear() {
            idMap.clear();
            idToValueTag.clear();
            valueTagToId.clear();
        }

    }

}
