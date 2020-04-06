@import Foundation;
@import AVFoundation;

#import "WebRTCModule.h"
#import "WebRTCValueManager.h"
#import <WebRTC/RTCDataChannel.h>
#if __has_include("RCTConvert.h")
#import "RCTConvert.h"
#else
#import <React/RCTConvert.h>
#endif

NS_ASSUME_NONNULL_BEGIN

@interface RTCDataChannel (ReactNativeWebRTCKit) <WebRTCExportable>

@property (nonatomic, nullable) NSString *valueTag;

- (id)json;

@end

@interface WebRTCModule (RTCDataChannel) <RTCDataChannelDelegate>

@end

NS_ASSUME_NONNULL_END
