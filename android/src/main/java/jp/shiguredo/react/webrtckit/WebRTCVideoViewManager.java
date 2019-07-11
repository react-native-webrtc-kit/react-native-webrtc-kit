package jp.shiguredo.react.webrtckit;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;

import org.webrtc.MediaStreamTrack;
import org.webrtc.VideoTrack;

import java.util.NoSuchElementException;

import static jp.shiguredo.react.webrtckit.Readables.string;
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

    @Override
    public void onDropViewInstance(WebRTCVideoView view) {
        // ドキュメントによるとviewが捨てられるときに呼び出されるらしいですが、リロード時には呼び出されません
        // 一応呼び出しだけは残しておきますが現状クリーンアップはWebRTCVideoView.onDetachedFromWindow()で行っています
        view.release();
    }

    /*
     XXX:とりあえずコメントアウト
    @Override
    public void onCatalystInstanceDestroy() {
        // 本メソッドは何をやってもどのようなタイミングでも一切呼び出されません
        // 本メソッドは使えないということを明示的に知らしめるためだけに実装してあります
        super.onCatalystInstanceDestroy();
    }
    */

    //region ReactProp

    @ReactProp(name = "objectFit")
    public void setObjectFit(@NonNull final WebRTCVideoView view, @Nullable final String objectFit) {
        Log.d(getName(), "setObjectFit() - objectFit=" + objectFit);
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
        Log.d(getName(), "setTrack() - track=" + json);
        if (json == null) {
            view.setVideoTrack(null);
            return;
        }
        final String valueTag = string(json, "_valueTag");
        if (valueTag == null) {
            throw new IllegalStateException("track._valueTag is not defined");
        }

        final ThemedReactContext reactContext = view.getReactContext();
        final WebRTCModule module = reactContext.getNativeModule(WebRTCModule.class);
        Log.d(getName(), "repository.tracks.dump()=\n" + module.repository.tracks.dump());
        Log.d(getName(), "repository.transceiver.dump()=\n" + module.repository.transceivers.dump());
        final MediaStreamTrack track = module.repository.tracks.getByValueTag(valueTag);
        if (!(track instanceof VideoTrack)) {
            throw new IllegalStateException("VideoTrack with valueTag " + valueTag + " is not found");
        }
        final VideoTrack videoTrack = (VideoTrack) track;
        view.setVideoTrack(videoTrack);
    }

    //endregion

}
