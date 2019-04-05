package com.reactlibrary;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.webrtc.MediaStreamTrack;
import org.webrtc.VideoTrack;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO: Maybe I must make all the call synchronized and locked. I don't think that's required since RN doesn't run on mutiple threads... but WebRTC does, so it depends.
 */
final class WebRTCMediaStreamRepository {

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

    void clear() {
        trackMap.clear();
        trackValueTagMap.clear();
        trackAspectRatioMap.clear();
    }

}
