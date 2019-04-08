package com.reactlibrary;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.ViewGroup;

import org.webrtc.SurfaceViewRenderer;

public class WebRTCVideoView extends ViewGroup {

    final SurfaceViewRenderer surfaceViewRenderer;

    public WebRTCVideoView(@Nullable final Context context) {
        super(context);
        surfaceViewRenderer = new SurfaceViewRenderer(context);
        final LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(surfaceViewRenderer, lp);
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

    // TODO: setVideoTrack() を実装する

/*
- (void)setVideoTrack:(nullable RTCVideoTrack *)videoTrack {
    if (videoTrack) {
        NSAssert([videoTrack.kind isEqualToString: kRTCMediaStreamTrackKindVideo],
                 @"track must be video track %@",
                 [videoTrack description]);
    }

    RTCVideoTrack *oldValue = self.videoTrack;
    if (oldValue == videoTrack) {
        return;
    }
    
    if (oldValue) {
        [oldValue removeRenderer:self];
        self.isRendererAdded = NO;
    }
    _videoTrack = videoTrack;

    if (videoTrack && self.window) {
        [videoTrack addRenderer:self];
        self.isRendererAdded = YES;
    }
}
*/

    // TODO: iOS側ではdidMoveToWindowタイミングでtrackとviewの関係の調整を行っている、詳細は以下のコードコメ参照、Android側でも同様の対応が必要か調査

    /*
- (void)didMoveToWindow {
    [super didMoveToWindow];

    if (self.videoTrack) {
        if (self.window) {
            // ビューがウィンドウに配置されたタイミングで
            // トラックにレンダラーが追加されていない場合は追加する。
            // 二重にレンダラーを追加すると NSAssert で落ちてしまうので注意。
            if (!self.isRendererAdded) {
                [self.videoTrack addRenderer:self];
                self.isRendererAdded = YES;
            }
        } else {
            // ウィンドウから外れたらレンダラーも外す。
            // トラックにレンダラーが追加されたままだと、
            // バックグラウンドで描画しようとして UIView の警告が出る。
            [self.videoTrack removeRenderer:self];
            self.isRendererAdded = NO;
        }
    }
}
     */

}
