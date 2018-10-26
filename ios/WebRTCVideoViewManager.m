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
    if ([s isEqualToString:@"fill"]) {
        contentMode = UIViewContentModeScaleToFill;
    } else if ([s isEqualToString:@"cover"]) {
        contentMode = UIViewContentModeScaleAspectFit;
    } else if ([s isEqualToString:@"contain"]) {
        contentMode = UIViewContentModeScaleAspectFill;
    } else {
        contentMode = UIViewContentModeScaleAspectFill;
    }
    view.contentMode = contentMode;
    [view setNeedsLayout];
}

RCT_CUSTOM_VIEW_PROPERTY(trackValueTag, NSString, WebRTCVideoView) {
    RTCVideoTrack *videoTrack = nil;
    
    if (json) {
        NSString *valueTag = (NSString *)json;
        
        WebRTCModule *module = [self.bridge moduleForName:@"WebRTCModule"];
        videoTrack = module.localTracks[valueTag];
        if (!videoTrack) {
            NSLog(@"track for value tag %@ is not found", valueTag);
            return;
        }
    }
    
    view.videoTrack = videoTrack;
}

@end
