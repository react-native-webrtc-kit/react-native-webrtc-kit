#if TARGET_OS_OSX
#import "api/peer_connection_interface.h"
#else
#import <Foundation/Foundation.h>
#import <WebRTC/WebRTC.h>
#endif

@interface WebRTCUtils : NSObject

+ (NSString *)stringForPeerConnectionState:(RTCPeerConnectionState)state;
+ (NSString *)stringForICEConnectionState:(RTCIceConnectionState)state;
+ (NSString *)stringForICEGatheringState:(RTCIceGatheringState)state;
+ (NSString *)stringForSignalingState:(RTCSignalingState)state;
+ (NSString *)stringForDataChannelState:(RTCDataChannelState)state;
+ (NSDictionary<NSString *, NSString *> *)parseJavaScriptConstraints:(NSDictionary *)src;

/**
 * JavaScript のオブジェクトを RTCMediaConstraints に変換します。
 */
+ (RTCMediaConstraints *)parseMediaConstraints:(nullable NSDictionary *)constraints;

@end
