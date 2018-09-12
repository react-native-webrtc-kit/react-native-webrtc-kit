@import Foundation;

#import "WebRTCModule.h"

NS_ASSUME_NONNULL_BEGIN

@interface RTCMediaStream (ReactNativeWebRTCKit)

@property (nonatomic, nullable) NSString *valueTag;

- (nullable RTCMediaStreamTrack *)trackForTrackId:(NSString *)trackId;

@end

@interface WebRTCModule (RTCMediaStream)

@end

NS_ASSUME_NONNULL_END
