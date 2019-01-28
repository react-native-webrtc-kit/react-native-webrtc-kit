#import "WebRTCVideoViewController.h"

#import <WebRTC/RTCAudioSession.h>
#import <WebRTC/RTCCameraVideoCapturer.h>
#import <WebRTC/RTCDispatcher.h>
#import <WebRTC/RTCLogging.h>
#import <WebRTC/RTCMediaConstraints.h>
#import "WebRTCVideoView.h"

@interface WebRTCVideoViewController () <WebRTCVideoViewDelegate,
RTCAudioSessionDelegate>
@property(nonatomic, strong) RTCVideoTrack *remoteVideoTrack;
@property(nonatomic, readonly) WebRTCVideoView *videoView;
@property(nonatomic, assign) AVAudioSessionPortOverride portOverride;
@end

@implementation WebRTCVideoViewController

@synthesize delegate = _delegate;
@synthesize portOverride = _portOverride;

- (void)loadView {
    _videoView = [[WebRTCVideoView alloc] initWithFrame:CGRectZero];
    _videoView.delegate = self;
    RTCAudioSession *session = [RTCAudioSession sharedInstance];
    [session addDelegate:self];
}

#pragma mark - WebRTCVideoViewDelegate

- (void)videoViewDidChangeRoute:(WebRTCVideoView *)view {
    AVAudioSessionPortOverride override = AVAudioSessionPortOverrideNone;
    if (_portOverride == AVAudioSessionPortOverrideNone) {
        override = AVAudioSessionPortOverrideSpeaker;
    }
    [RTCDispatcher dispatchAsyncOnType:RTCDispatcherTypeAudioSession
                                 block:^{
                                     RTCAudioSession *session = [RTCAudioSession sharedInstance];
                                     [session lockForConfiguration];
                                     NSError *error = nil;
                                     if ([session overrideOutputAudioPort:override error:&error]) {
                                         self.portOverride = override;
                                     } else {
                                         RTCLogError(@"Error overriding output port: %@",
                                                     error.localizedDescription);
                                     }
                                     [session unlockForConfiguration];
                                 }];
}

#pragma mark - RTCAudioSessionDelegate

- (void)audioSession:(RTCAudioSession *)audioSession
didDetectPlayoutGlitch:(int64_t)totalNumberOfGlitches {
    RTCLog(@"Audio session detected glitch, total: %lld", totalNumberOfGlitches);
}
@end


