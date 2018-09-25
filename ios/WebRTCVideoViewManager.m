#import "WebRTCVideoViewManager.h"

#import "WebRTCVideoView.h"
#import "WebRTCModule.h"

@implementation WebRTCVideoViewManager

RCT_EXPORT_MODULE()

- (UIView *)view {
    WebRTCVideoView *view = [[WebRTCVideoView alloc] init];
    return view;
}

- (dispatch_queue_t)methodQueue {
    // UIView を扱う都合上、常に main queue から操作される必要がある
    return dispatch_get_main_queue();
}

RCT_CUSTOM_VIEW_PROPERTY(objectFit, NSString *, WebRTCVideoView) {
    NSString *s = [RCTConvert NSString:json];
    UIViewContentMode contentMode;
    if ([s isEqualToString:@"cover"]) {
        contentMode = UIViewContentModeScaleAspectFit;
    } else if ([s isEqualToString:@"contain"]) {
        contentMode = UIViewContentModeScaleAspectFill;
    } else {
        contentMode = UIViewContentModeScaleAspectFill;
    }
    view.contentMode = contentMode;
    [view setNeedsLayout];
}

RCT_CUSTOM_VIEW_PROPERTY(streamValueTag, NSString, WebRTCVideoView) {
    RTCVideoTrack *videoTrack = nil;
    
    if (json) {
        NSString *valueTag = (NSString *)json;
        
        WebRTCModule *module = [self.bridge moduleForName:@"WebRTCModule"];
        RTCMediaStream *stream = [module streamForValueTag: valueTag];
        NSArray *videoTracks = stream ? stream.videoTracks : nil;
        
        videoTrack = videoTracks && videoTracks.count ? videoTracks[0] : nil;
        if (!videoTrack) {
            NSLog(@"No video track found for stream, streamId: %@ valueTag: %@", stream.streamId, valueTag);
        }
    }
    
    view.videoTrack = videoTrack;
}

@end
