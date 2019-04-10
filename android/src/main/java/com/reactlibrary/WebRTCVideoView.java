package com.reactlibrary;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.ViewGroup;

import com.facebook.react.uimanager.ThemedReactContext;

import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoTrack;

public class WebRTCVideoView extends ViewGroup {


    @NonNull
    final SurfaceViewRenderer surfaceViewRenderer;
    private boolean isSurfaceViewRendererInitialized = false;
    @Nullable
    private VideoTrack videoTrack = null;
    private boolean isVideoTrackRendererAdded = false;


    public WebRTCVideoView(@Nullable final Context context) {
        super(context);
        if (!(context instanceof ThemedReactContext)) {
            throw new IllegalArgumentException("The context to initialize WebRTCVideoView is expected to be an instance of ThemedReactContext.");
        }
        surfaceViewRenderer = new SurfaceViewRenderer(context);
        final LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(surfaceViewRenderer, lp);
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isSurfaceViewRendererInitialized) {
            final ThemedReactContext reactContext = getReactContext();
            final WebRTCModule module = reactContext.getNativeModule(WebRTCModule.class);
            surfaceViewRenderer.init(module.getEglContext(), null);
            isSurfaceViewRendererInitialized = true;
        }
        attachVideoTrackWithRenderer();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        detachVideoTrackWithRenderer();
    }

    @Override
    protected void onLayout(final boolean changed,
                            final int l,
                            final int t,
                            final int r,
                            final int b) {
        // Do nothing, currently the video content mode (fit/fill) is managed by `surfaceViewRenderer.setScalingType()`.
        // We don't have to manually calculate the size of the surfaceViewRenderer here. Just let it MATCH_PARENT/MATCH_PARENT.
        // XXX: そのうち修正しないとダメな可能性が高い、詳細はWebRTCVideoViewManager.objectFitのコメントを参照
    }


    @NonNull
    ThemedReactContext getReactContext() {
        return (ThemedReactContext) getContext();
    }

    void setVideoTrack(@Nullable final VideoTrack videoTrack) {
        // XXX: iOSの実装ではwindowがある場合のみ即座にrendererを紐付け、windowがまだ間に合ってない場合はwindow管理下になってから紐付けるようになっている。
        //      Android側の実装でも同様にしないと問題が発生するかどうかをみてから判断する。
        if (this.videoTrack == videoTrack) {
            return;
        }
        detachVideoTrackWithRenderer();
        this.videoTrack = videoTrack;
        attachVideoTrackWithRenderer();
    }

    private void attachVideoTrackWithRenderer() {
        if (videoTrack != null
                && !isVideoTrackRendererAdded) {
            videoTrack.addSink(surfaceViewRenderer);
            isVideoTrackRendererAdded = true;
        }
    }

    private void detachVideoTrackWithRenderer() {
        if (videoTrack != null
                && isVideoTrackRendererAdded) {
            videoTrack.removeSink(surfaceViewRenderer);
            isVideoTrackRendererAdded = false;
        }
    }

}
