#import <Foundation/Foundation.h>
#import <WebRTC/WebRTC.h>

@interface WebRTCUtils : NSObject

+ (NSString *)stringForPeerConnectionState:(RTCPeerConnectionState)state;
+ (NSString *)stringForICEConnectionState:(RTCIceConnectionState)state;
+ (NSString *)stringForICEGatheringState:(RTCIceGatheringState)state;
+ (NSString *)stringForSignalingState:(RTCSignalingState)state;

+ (NSDictionary<NSString *, NSString *> *)parseJavaScriptConstraints:(NSDictionary *)src;

/**
 * JavaScript のオブジェクトを RTCMediaConstraints に変換します。
 */
+ (RTCMediaConstraints *)parseMediaConstraints:(nullable NSDictionary *)constraints;

@end
