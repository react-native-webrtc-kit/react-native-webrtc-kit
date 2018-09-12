@import AVFoundation;

#import "WebRTCVideoView.h"

NS_ASSUME_NONNULL_BEGIN

@interface WebRTCVideoView () <RTCEAGLVideoViewDelegate>

/**
 * -[RTCVideoTrack addRenderer:] がレンダラーを強参照するので、
 * 循環参照を防ぐために弱参照にしています。
 */
@property (nonatomic, weak) RTCEAGLVideoView *videoView;

@property (nonatomic) CGSize videoSize;
@property (nonatomic) BOOL isRendererAdded;

@end

/**
 * レンダリングモードの調整は contentMode プロパティを操作すること。
 * 現在 scaleAspectFit と scaleAspectFill にのみ対応しており、
 * デフォルトは scaleAspectFit です。
 */
@implementation WebRTCVideoView

#pragma mark - Lifecycle

- (instancetype)initWithFrame:(CGRect)frame {
    self = [super initWithFrame:frame];
    if (self) {
        RTCEAGLVideoView *videoView = [[RTCEAGLVideoView alloc] init];
        videoView.delegate = self;
        [self addSubview:videoView];
        self.videoView = videoView;
        
        self.videoSize = CGSizeZero;
        self.isRendererAdded = NO;
        
        self.contentMode = UIViewContentModeScaleAspectFit;
        self.opaque = NO;
        self.clipsToBounds = YES;
    }
    return self;
}

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

- (void)layoutSubviews {
    [super layoutSubviews];
    
    CGFloat width = self.videoSize.width, height = self.videoSize.height;
    CGRect newFrame;
    if (width <= 0 || height <= 0) {
        newFrame = CGRectZero;
    } else if (self.contentMode == UIViewContentModeScaleAspectFill) {
        // cover
        newFrame = self.bounds;
        if (newFrame.size.width != width || newFrame.size.height != height) {
            CGFloat scaleFactor = MAX(newFrame.size.width / width, newFrame.size.height / height);
            width *= scaleFactor;
            height *= scaleFactor;
            newFrame.origin.x += (newFrame.size.width - width) / 2.0;
            newFrame.origin.y += (newFrame.size.height - height) / 2.0;
            newFrame.size.width = width;
            newFrame.size.height = height;
        }
    } else {
        newFrame = AVMakeRectWithAspectRatioInsideRect(CGSizeMake(width, height), self.bounds);
    }
    
    CGRect oldFrame = self.videoView.frame;
    if (!CGRectEqualToRect(oldFrame, newFrame)) {
        self.videoView.frame = newFrame;
    }
}

#pragma mark - RTCVideoRenderer

- (void)setSize:(CGSize)size {
    [self.videoView setSize:size];
}

- (void)renderFrame:(nullable RTCVideoFrame *)frame {
    [self.videoView renderFrame:frame];
}

#pragma mark - RTCEAGLVideoViewDelegate

- (void)videoView:(RTCEAGLVideoView *)videoView didChangeVideoSize:(CGSize)size {
    self.videoSize = size;
    [self setNeedsLayoutAsync];
}

#pragma mark - Public

- (void)setVideoTrack:(nullable RTCVideoTrack *)videoTrack {
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

#pragma mark - Private

- (void)setNeedsLayoutAsync {
    __weak typeof(self) weakSelf = self;
    dispatch_async(dispatch_get_main_queue(), ^{
        typeof(self) strongSelf = weakSelf;
        [strongSelf setNeedsLayout];
    });
}

@end

NS_ASSUME_NONNULL_END
