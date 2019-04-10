package com.reactlibrary;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.webrtc.MediaStreamTrack;
import org.webrtc.RtpTransceiver;
import org.webrtc.VideoTrack;

import java.util.HashMap;
import java.util.Map;

/**
 * WebRTCモジュールが使用するすべてのWebRTC関連のオブジェクト (PeerConnection, MediaStream, MediaStreamTrack等) を管理するリポジトリです。
 * TODO: Maybe I must make all the call synchronized and locked. I don't think that's required since RN doesn't run on mutiple threads... but WebRTC does, so it depends.
 */
final class WebRTCRepository {


    //region PeerConnection
    //endregion


    //region Stream
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

    void addTrack(@NonNull final MediaStreamTrack track, @NonNull final String valueTag) {
        trackMap.put(track.id(), track);
        trackValueTagMap.put(track.id(), valueTag);
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
        trackMap.clear();
        trackValueTagMap.clear();
        trackAspectRatioMap.clear();
        transceiverMap.clear();
    }

}
