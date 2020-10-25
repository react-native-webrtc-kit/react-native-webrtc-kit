@import Foundation;

#if !TARGET_OS_OSX
#import <WebRTC/WebRTC.h>
#endif

#if __has_include("RCTConvert.h")
#import "RCTConvert.h"
#else
#import <React/RCTConvert.h>
#endif

#import "WebRTCModule.h"
#import "WebRTCMediaStreamConstraints.h"

NS_ASSUME_NONNULL_BEGIN

@interface RCTConvert (WebRTC)

+ (nullable RTCIceCandidate *)RTCIceCandidate:(nullable id)json;
+ (nullable RTCSessionDescription *)RTCSessionDescription:(nullable id)json;
+ (nullable RTCIceServer *)RTCIceServer:(nullable id)json;
+ (nullable RTCConfiguration *)RTCConfiguration:(nullable id)json;
+ (nullable WebRTCMediaStreamConstraints *)WebRTCMediaStreamConstraints:(nullable id)json;
+ (nullable RTCDataChannelConfiguration *)RTCDataChannelConfiguration:(nullable id)json;
+ (nullable RTCDataBuffer *)RTCDataBuffer:(nullable id)json;
@end

NS_ASSUME_NONNULL_END
