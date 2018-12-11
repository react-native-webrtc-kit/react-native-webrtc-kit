@import Foundation;

#import "WebRTCModule.h"
#import "WebRTCValueManager.h"

typedef NS_ENUM(NSUInteger, RTCPeerConnectionState) {
    RTCPeerConnectionStateNew,
    RTCPeerConnectionStateConnecting,
    RTCPeerConnectionStateConnected,
    RTCPeerConnectionStateDisconnecting,
    RTCPeerConnectionStateDisconnected
};

NS_ASSUME_NONNULL_BEGIN

@interface RTCRtpParameters (ReactNativeWebRTCKit)

- (id)json;

@end

@interface RTCRtpSender (ReactNativeWebRTCKit)

@property (nonatomic, nullable) NSString *valueTag;
@property (nonatomic, nullable) NSArray<NSString *> *streamIds;

- (id)json;

@end

@interface RTCRtpReceiver (ReactNativeWebRTCKit)

@property (nonatomic, nullable) NSString *valueTag;
@property (nonatomic, nullable) NSArray<NSString *> *streamIds;

- (id)json;

+ (NSString *)directionDescription:(RTCRtpTransceiverDirection)direction;
+ (RTCRtpTransceiverDirection)directionFromString:(NSString *)string;

@end

@interface RTCRtpTransceiver (ReactNativeWebRTCKit)

@property (nonatomic, nullable) NSString *valueTag;

- (id)json;

@end

@interface RTCPeerConnection (ReactNativeWebRTCKit) <WebRTCExportable>

@property (nonatomic, nullable) NSString *valueTag;

@property (nonatomic) RTCPeerConnectionState connectionState;

/**
 * 通常の close を含む React Native 向けの終了処理を行う
 */
- (void)closeAndFinish;

@end

@interface WebRTCModule (RTCPeerConnection) <RTCPeerConnectionDelegate>

- (nullable RTCRtpParameters *)rtpParametersForValueTag:(nonnull NSString *)valueTag;
- (nullable RTCRtpEncodingParameters *)rtpEncodingParametersForValueTag:(nonnull NSString *)valueTag ssrc:(nullable NSNumber *)ssrc;

@end

NS_ASSUME_NONNULL_END
