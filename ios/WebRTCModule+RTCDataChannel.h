@import Foundation;
@import AVFoundation;

#import "WebRTCModule.h"
#import "WebRTCValueManager.h"

NS_ASSUME_NONNULL_BEGIN

@interface RTCDataChannel (ReactNativeWebRTCKit) <WebRTCExportable>

@property (nonatomic, nullable) NSString *valueTag;

- (id)json;

@end

@interface WebRTCModule (RTCDataChannel) <RTCDataChannelDelegate>

@end

NS_ASSUME_NONNULL_END
