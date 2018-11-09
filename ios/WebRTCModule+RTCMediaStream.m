#import <objc/runtime.h>
#import "WebRTCModule+RTCMediaStream.h"
#import "WebRTCValueManager.h"

NS_ASSUME_NONNULL_BEGIN

@implementation RTCMediaStream (ReactNativeWebRTCKit)

- (nullable NSString *)valueTag {
    return [WebRTCValueManager valueTagForObject: self];
}

- (void)setValueTag:(nullable NSString *)valueTag
{
    [WebRTCValueManager setValueTag: valueTag forObject: self];
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

@implementation RTCMediaStreamTrack (ReactNativeWebRTCKit)

- (nullable NSString *)valueTag {
    return [WebRTCValueManager valueTagForObject: self];
}

- (void)setValueTag:(nullable NSString *)valueTag
{
    [WebRTCValueManager setValueTag: valueTag forObject: self];
}

- (id)json
{
    NSString *state;
    switch (self.readyState) {
        case RTCMediaStreamTrackStateLive:
            state = @"live";
            break;
        case RTCMediaStreamTrackStateEnded:
            state = @"ended";
            break;
        default:
            NSAssert(NO, @"invalid ready state");
    }
    return @{@"valueTag": self.valueTag,
             @"enabled": @(self.isEnabled),
             @"id": self.trackId,
             @"kind": self.kind,
             @"readyState": state};
}

@end

@implementation RTCVideoTrack (ReactNativeWebRTCKit)

static void *aspectRatioKey = "aspectRatio";

- (CGFloat)aspectRatio
{
    NSNumber *ratio = objc_getAssociatedObject(self, aspectRatioKey);
    if (ratio)
        return (CGFloat)[ratio doubleValue];
    else
        return -1;
}

- (void)setAspectRatio:(CGFloat)aspectRatio
{
    objc_setAssociatedObject(self, aspectRatioKey,
                             [[NSNumber alloc] initWithDouble: aspectRatio],
                             OBJC_ASSOCIATION_COPY_NONATOMIC);
}

@end

@implementation WebRTCModule (RTCMediaStream)

// MARK: -trackSetEnabled:trackId:valueTag:

RCT_EXPORT_METHOD(trackSetEnabled:(nonnull NSNumber *)isEnabled
                  valueTag:(nonnull NSString *)valueTag)
{
    RTCMediaStreamTrack *track = self.tracks[valueTag];
    if (track)
        track.isEnabled = [isEnabled boolValue];
}

// MARK: -trackSetAspectRatio:trackId:valueTag:

RCT_EXPORT_METHOD(trackSetAspectRatio:(nonnull NSNumber *)aspectRatio
                  valueTag:(nonnull NSString *)valueTag)
{
    RTCMediaStreamTrack *track = self.tracks[valueTag];
    if ([track isKindOfClass: [RTCVideoTrack class]]) {
        ((RTCVideoTrack *)track).aspectRatio = [aspectRatio doubleValue];
    }
}

@end

NS_ASSUME_NONNULL_END
