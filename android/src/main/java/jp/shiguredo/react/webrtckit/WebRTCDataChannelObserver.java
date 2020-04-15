package jp.shiguredo.react.webrtckit;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import android.util.Log;
import android.util.Pair;
import android.util.Base64;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.queue.ReactQueueConfiguration;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import org.webrtc.DataChannel;

import static jp.shiguredo.react.webrtckit.WebRTCConverter.dataChannelStateStringValue;

final class WebRTCDataChannelObserver implements DataChannel.Observer {

    @NonNull
    private final ReactContext reactContext;
    /**
     * First is valueTag, Seconds is DataChannel.
     */
    @Nullable
    Pair<String, DataChannel> dataChannelPair = null;

    WebRTCDataChannelObserver(@NonNull final ReactContext reactContext) {
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


    private void closeAndFinish() {
        if (dataChannelPair != null) {
            Log.d("WebRTCModule", "DataChannel closeAndFinish()[" + dataChannelPair.first + "]");
            final WebRTCModule module = getModule();
            final ReactQueueConfiguration queueConfiguration = module.getReactContext().getCatalystInstance().getReactQueueConfiguration();
            final String valueTag = dataChannelPair.first;
            dataChannelPair = null;
            queueConfiguration.getNativeModulesQueueThread().runOnQueue(() -> {
                module.dataChannelClose(valueTag);
            });
        }
    }

    //region DataChannel.Observer

    @Override
    public void onStateChange() {
        if (dataChannelPair == null) return;
        final WritableMap params = Arguments.createMap();
        params.putString("valueTag", dataChannelPair.first);
        final DataChannel dataChannel = dataChannelPair.second;
        final DataChannel.State state = dataChannel.state();
        Log.d("DataChannelObserver", "onStateChange()[" + dataChannelPair.first + "] - newReadyState=" + state);
        // state が closed な場合 finish する
        if (state == DataChannel.State.CLOSED) {
            closeAndFinish();
        }
        params.putString("readyState", dataChannelStateStringValue(dataChannel.state()));
        sendDeviceEvent("dataChannelStateChanged", params);
    }

    @Override
     public void onBufferedAmountChange(long previousAmount) {
        if (dataChannelPair == null) return;
        final WritableMap params = Arguments.createMap();
        params.putString("valueTag", dataChannelPair.first);
        final DataChannel dataChannel = dataChannelPair.second;
        Log.d("DataChannelObserver", "onBufferedAmountChange()[" + dataChannelPair.first + "] - newBufferedAmount=" + dataChannel.bufferedAmount());
        params.putDouble("bufferedAmount", dataChannel.bufferedAmount());
        sendDeviceEvent("dataChannelOnBufferedAmount", params);
    }

    @Override
    public void onMessage(DataChannel.Buffer buffer) {
        if (dataChannelPair == null) return;
        WritableMap params = Arguments.createMap();
        params.putString("valueTag", dataChannelPair.first);

        // ByteBuffer を string に変換する前処理
        // cf: https://stackoverflow.com/a/308562520
        final ByteBuffer bufferData = buffer.data;
        byte[] bytes;
        if (bufferData.hasArray()) {
            bytes = bufferData.array();
        } else {
            bytes = new byte[bufferData.remaining()];
            bufferData.get(bytes);
        }

        String data;
        if (buffer.binary) {
            // binary data の場合 base64 encoding して JS レイヤーに通知する
            data = Base64.encodeToString(bytes, Base64.NO_WRAP);
        } else {
            // binary data でない場合はそのまま UTF-8 String にする
            // 以下のように書きたいところだが、 StandardCharsets は API Level 19 以上のみサポートしている (RNKit の API Level は 16)
            // data = new String(bytes, StandardCharsets.UTF_8));
            // 仕方がないので `Charset.forName` で代用する
            data = new String(bytes, Charset.forName("UTF-8"));
        }
        params.putString("data", data);
        params.putBoolean("binary", buffer.binary);
        sendDeviceEvent("dataChannelOnMessage", params);
    }
    //endregion
}
