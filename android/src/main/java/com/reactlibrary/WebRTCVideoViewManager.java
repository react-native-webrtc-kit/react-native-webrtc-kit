package com.reactlibrary;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;

import org.webrtc.VideoTrack;

import java.util.NoSuchElementException;

import static org.webrtc.RendererCommon.ScalingType.SCALE_ASPECT_FILL;
import static org.webrtc.RendererCommon.ScalingType.SCALE_ASPECT_FIT;

public class WebRTCVideoViewManager extends SimpleViewManager<WebRTCVideoView> {

    @Override
    @NonNull
    public String getName() {
        return "WebRTCVideoView";
    }

    @Override
    @NonNull
    public WebRTCVideoView createViewInstance(@NonNull final ThemedReactContext context) {
        return new WebRTCVideoView(context);
    }

    //region ReactProp

    @ReactProp(name = "objectFit")
    public void setObjectFit(@NonNull final WebRTCVideoView view, @Nullable final String objectFit) {
        if (objectFit == null) {
            // Default = "contain" なので "contain" の実装に合わせる
            view.surfaceViewRenderer.setScalingType(SCALE_ASPECT_FILL, SCALE_ASPECT_FILL);
            return;
        }
        // XXX: "cover" と "contain" が怪しい、iOS側の実装が逆なのでは？
        // XXX: 現在surfaceViewRendererのscalingTypeを使う実装になっているが、いくつか問題があって、
        //      fitモードの動作が完全にWebRTCのsurfaceViewRendererの実装依存になってしまうのが一つと、
        //      onMeasureを使って動作させるようになっているのでこの親のWebRTCVideoView extends ViewGroupに対してMATCH_PARENT/MATCH_PARENTだと上手く動作しない気がする
        //      あと外からaspectRatioを指定できなくなってしまう (WebRTCMediaStreamConstraints.Video.aspectRatioを反映させられなくなる)
        //      これでは動作要件を満たせないので後からやっぱり修正しないとダメなきがする
        //      基本的にはfill時にはMATCH_PARENT/MATCH_PARENTのFILL_FILLで良いと思うがeglRendererのレンダリングサイズがsurfaceViewRenderer自身のsizeを見て決定されている
        //      (元の動画フレームじゃなくて描画される側サイズに合わせて描画・・・？)
        //      ので更に調査が必要かも・・・
        switch (objectFit) {
            case "fill":
                // "fill" = UIViewContentModeScaleToFill in iOS
                // アスペクト比を破ってでも表示領域内を埋め尽くします。
                // XXX: `setScalingType` can't distinguish between ScaleToFill and ScaleAspectFill, just like iOS.
                //      `setScalingType` can specify 2 scale types but the `scalingTypeMismatchOrientation` argument is only consider its orientation (horizontal or vertical)
                //      so it just doesn't matter the aspect ratio when it matches the orientation. Bummer!
                //      Thus both "fill" and "contain" has to be identical for now. It can be manually implemented later, maybe.
                view.surfaceViewRenderer.setScalingType(SCALE_ASPECT_FILL, SCALE_ASPECT_FILL);
                break;
            case "cover":
                // "cover" = UIViewContentModeScaleAspectFit in iOS
                // アスペクト比を保ちながら表示領域の中に収まるように映像を小さくして収めます。
                view.surfaceViewRenderer.setScalingType(SCALE_ASPECT_FIT, SCALE_ASPECT_FIT);

                break;
            case "contain":
            default:
                // "contain" = UIViewContentModeScaleAspectFill in iOS
                // アスペクト比を保ちながら表示領域の中に収まるように映像を拡大して全体を埋め尽くします。
                // XXX: `setScalingType` can't distinguish between ScaleToFill and ScaleAspectFill, just like iOS.
                //      `setScalingType` can specify 2 scale types but the `scalingTypeMismatchOrientation` argument is only consider its orientation (horizontal or vertical)
                //      so it just doesn't matter the aspect ratio when it matches the orientation. Bummer!
                //      Thus both "fill" and "contain" has to be identical for now. It can be manually implemented later, maybe.
                view.surfaceViewRenderer.setScalingType(SCALE_ASPECT_FILL, SCALE_ASPECT_FILL);
                break;
        }
    }

    @ReactProp(name = "reactStreamId")
    public void setReactStreamId(@NonNull final WebRTCVideoView view, @Nullable final String reactStreamId) {
        throw new NoSuchElementException("'streamValueTag' property is deprecated. use 'track' property");
    }

    @ReactProp(name = "track")
    public void setTrack(@NonNull final WebRTCVideoView view, @Nullable final ReadableMap json) {
        if (json == null) {
            view.setVideoTrack(null);
            return;
        }
        final String valueTag = json.getString("_valueTag");
        if (valueTag == null) {
            // XXX: このケースはUnexpected (videoTrackの指定がない), Exception吐いたほうがいいかも
            return;
        }

        final ThemedReactContext reactContext = view.getReactContext();
        final WebRTCModule module = reactContext.getNativeModule(WebRTCModule.class);
        final VideoTrack videoTrack = module.repository.getVideoTrackByValueTag(valueTag);
        if (videoTrack == null) {
            // XXX: このケースはUnexpected (指定されたvideoTrackが見つからない), Exception吐いたほうがいいかも
            return;
        }
        view.setVideoTrack(videoTrack);
    }

    //endregion

}
