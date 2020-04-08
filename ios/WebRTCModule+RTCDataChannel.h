@import Foundation;
@import AVFoundation;

#import "WebRTCModule.h"
#import "WebRTCValueManager.h"
#import <WebRTC/RTCDataChannel.h>

NS_ASSUME_NONNULL_BEGIN

@interface RTCDataChannel (ReactNativeWebRTCKit) <WebRTCExportable>

@property (nonatomic, nullable) NSString *valueTag;

- (id)json;

/**
 * 通常の close を含む React Native 向けの終了処理を行う
 */
- (void)closeAndFinish;

@end

@interface WebRTCModule (RTCDataChannel) <RTCDataChannelDelegate>

@end

NS_ASSUME_NONNULL_END
