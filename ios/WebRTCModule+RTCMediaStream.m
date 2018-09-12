#import <objc/runtime.h>
#import "WebRTCModule+RTCMediaStream.h"

NS_ASSUME_NONNULL_BEGIN

@implementation RTCMediaStream (ReactNativeWebRTCKit)

static void *valueTagKey = "valueTag";

- (nullable NSString *)valueTag {
    return objc_getAssociatedObject(self, valueTagKey);
}

- (void)setValueTag:(nullable NSString *)valueTag {
    objc_setAssociatedObject(self, valueTagKey, valueTag, OBJC_ASSOCIATION_RETAIN_NONATOMIC);
}

- (nullable RTCMediaStreamTrack *)trackForTrackId:(NSString *)trackId
{
    for (RTCMediaStreamTrack *track in self.videoTracks) {
        if ([track.trackId isEqualToString: trackId])
            return track;
    }
    for (RTCMediaStreamTrack *track in self.audioTracks) {
        if ([track.trackId isEqualToString: trackId])
            return track;
    }
    return nil;
}

@end

@implementation WebRTCModule (RTCMediaStream)

// MARK: -trackSetEnabled:trackId:valueTag:

RCT_EXPORT_METHOD(trackSetEnabled:(nonnull NSNumber *)isEnabled
                  trackId:(nonnull NSString *)trackId
                  valueTag:(nonnull NSString *)valueTag)
{
    RTCMediaStream *stream = [self streamForValueTag: valueTag];
    if (stream) {
        RTCMediaStreamTrack *track = [stream trackForTrackId: trackId];
        if (track)
            track.isEnabled = [isEnabled boolValue];
    }
}

@end

NS_ASSUME_NONNULL_END
