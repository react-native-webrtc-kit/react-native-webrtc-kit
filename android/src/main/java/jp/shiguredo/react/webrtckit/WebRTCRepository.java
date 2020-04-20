package jp.shiguredo.react.webrtckit;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import android.util.Pair;

import org.webrtc.DataChannel;
import org.webrtc.MediaStream;
import org.webrtc.MediaStreamTrack;
import org.webrtc.PeerConnection;
import org.webrtc.RtpParameters;
import org.webrtc.RtpReceiver;
import org.webrtc.RtpSender;
import org.webrtc.RtpTransceiver;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static jp.shiguredo.react.webrtckit.WebRTCConverter.mediaStreamTrackDump;
import static jp.shiguredo.react.webrtckit.WebRTCConverter.rtpTransceiverDump;

/**
 * WebRTCモジュールが使用するすべてのWebRTC関連のオブジェクト (PeerConnection, MediaStream, MediaStreamTrack等) を管理するリポジトリです。
 *
 * XXX: 現状一切同期処理を行っていないので、複数スレッドから同時に触られると壊れる可能性が高いです。
 *      同期ロックが必要になった場合追加してください。
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

    @NonNull
    Iterable<PeerConnection> allPeerConnections() {
        return new Iterable<PeerConnection>() {
            @NonNull
            @Override
            public Iterator<PeerConnection> iterator() {
                return peerConnectionMap.values().iterator();
            }
        };
    }

    //endregion


    //region Stream

    final DualKeyMap<MediaStream> streams = new DualKeyMap<>();

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

    void setStreamIdsForSender(@NonNull final RtpSender sender, @Nullable final List<String> streamIds) {
        if (streamIds == null || streamIds.size() == 0) {
            receiverStreamIdsMap.remove(sender.id());
            return;
        }
        receiverStreamIdsMap.put(sender.id(), streamIds);
    }

    void setStreamIdsForSender(@NonNull final RtpSender sender, @Nullable final MediaStream[] mediaStreams) {
        if (mediaStreams == null) {
            setStreamIdsForSender(sender, Collections.emptyList());
            return;
        }
        final List<String> streamIds = new ArrayList<>();
        for (final MediaStream stream : mediaStreams) {
            streamIds.add(stream.getId());
        }
        setStreamIdsForSender(sender, streamIds);
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


    //region RTP Parameters

    @Nullable
    RtpParameters getRtpParametersByValueTag(@NonNull final String valueTag) {
        for (final RtpSender sender : senders.all()) {
            if (valueTag.equals(senders.getValueTag(sender.id()))) {
                return sender.getParameters();
            }
        }
        for (final RtpReceiver receiver : receivers.all()) {
            if (valueTag.equals(receivers.getValueTag(receiver.id()))) {
                return receiver.getParameters();
            }
        }
        return null;
    }

    @Nullable
    RtpParameters.Encoding getRtpEncodingParametersByValueTag(@NonNull final String valueTag, long ssrc) {
        final RtpParameters rtpParameters = getRtpParametersByValueTag(valueTag);
        if (rtpParameters == null) return null;
        for (final RtpParameters.Encoding encoding : rtpParameters.encodings) {
            if (encoding.ssrc != null && encoding.ssrc == ssrc) {
                return encoding;
            }
        }
        return null;
    }

    //endregion


    //region Data Channel

    private final Map<String, DataChannel> dataChannelMap = new HashMap<>();

    void addDataChannel(@NonNull final Pair<String, DataChannel> dataChannelPair) {
        dataChannelMap.put(dataChannelPair.first, dataChannelPair.second);
    }

    void removeDataChannelByValueTag(@Nullable final String valueTag) {
        if (valueTag == null) {
            return;
        }
        dataChannelMap.remove(valueTag);
    }

    @Nullable
    DataChannel getDataChannelByValueTag(@Nullable final String valueTag) {
        if (valueTag == null) {
            return null;
        }
        return dataChannelMap.get(valueTag);
    }

    //endregion

    /**
     * このリポジトリの中身を完全に空にします。
     * その際、格納されていたWebRTC関連のオブジェクトは、現在のところ、明示的に初期化されません。
     * ※将来的にこの挙動は変更される可能性があります。
     */
    void clear() {
        peerConnectionMap.clear();

        streams.clear();

        tracks.clear();
        trackAspectRatioMap.clear();

        senders.clear();

        receivers.clear();
        receiverStreamIdsMap.clear();

        transceivers.clear();

        dataChannelMap.clear();
    }

    static final class DualKeyMap<V> {
        @NonNull
        private final Map<String, V> idMap = new HashMap<>();
        @NonNull
        private final Map<String, String> idToValueTag = new HashMap<>();
        @NonNull
        private final Map<String, String> valueTagToId = new HashMap<>();

        void add(@NonNull final String id, @NonNull final String valueTag, @NonNull final V value) {
            // すでに同一のIDで同一のインスタンスが登録されている場合は上書きしないで無視します
            // 同一のIDがすでに登録されていても、インスタンスが別であれば上書きします
            // XXX: ひょっとしたらIDだけ同一なら上書きしないほうがいいかも、というのはAndroidの場合実態はnative側にあって、
            //      Java側のインスタンスはただのラッパーなので、毎回毎回同一のnativeオブジェクトに対して必要に応じてJava側のラッパーが生成される、
            //      すなわちインスタンスは同じIDでも毎回別になる内部実装になっている恐れがあるため。
            //      実際に試してみてダメそうなら調整する。
            final V oldValue = idMap.get(id);
            if (oldValue == value) {
                return;
            }
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

        @NonNull
        Iterable<V> all() {
            return new Iterable<V>() {
                @NonNull
                @Override
                public Iterator<V> iterator() {
                    return idMap.values().iterator();
                }
            };
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

        /**
         * このRepositoryが抱えているすべてのID - ValueTag - Valueペアをダンプして文字列にします。
         */
        String dump() {
            final StringBuilder sb = new StringBuilder(" * ID - ValueTag - Value\n");
            for (final String id : idMap.keySet()) {
                final String valueTag = idToValueTag.get(id);
                final V value = idMap.get(id);
                final String valueString;
                if (value instanceof MediaStreamTrack) {
                    final MediaStreamTrack track = (MediaStreamTrack) value;
                    valueString = mediaStreamTrackDump(track);
                } else if (value instanceof RtpTransceiver) {
                    final RtpTransceiver transceiver = (RtpTransceiver) value;
                    valueString = rtpTransceiverDump(transceiver);
                } else {
                    valueString = value.toString();
                }
                sb.append(String.format(" * %s - %s - %s", id, valueTag, valueString));
                sb.append('\n');
            }
            return sb.toString();
        }

    }

}
