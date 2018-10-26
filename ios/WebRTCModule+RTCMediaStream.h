@import Foundation;
@import AVFoundation;

#import "WebRTCModule.h"

NS_ASSUME_NONNULL_BEGIN

@interface RTCMediaStream (ReactNativeWebRTCKit)

@property (nonatomic, nullable) NSString *valueTag;

- (nullable RTCMediaStreamTrack *)trackForTrackId:(NSString *)trackId;

@end

@interface RTCMediaStreamTrack (ReactNativeWebRTCKit)

@property (nonatomic, nullable) NSString *valueTag;

- (id)json;

@end

@interface RTCVideoTrack (ReactNativeWebRTCKit)

@property (nonatomic) CGFloat aspectRatio;

@end

@interface WebRTCModule (RTCMediaStream)

@end

NS_ASSUME_NONNULL_END
