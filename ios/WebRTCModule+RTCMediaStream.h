@import Foundation;
@import AVFoundation;

#import "WebRTCModule.h"
#import "WebRTCValueManager.h"

NS_ASSUME_NONNULL_BEGIN

@interface RTCMediaStream (ReactNativeWebRTCKit) <WebRTCExportable>

@property (nonatomic, nullable) NSString *valueTag;

- (nullable RTCMediaStreamTrack *)trackForTrackId:(NSString *)trackId;

@end

@interface RTCMediaStreamTrack (ReactNativeWebRTCKit) <WebRTCExportable>

@property (nonatomic, nullable) NSString *valueTag;

- (id)json;

@end

@interface RTCVideoTrack (ReactNativeWebRTCKit)

@property (nonatomic) CGFloat aspectRatio;

@end

@interface WebRTCModule (RTCMediaStream)

@end

NS_ASSUME_NONNULL_END
