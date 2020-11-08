@import Foundation;

#import "WebRTCModule.h"
#import "WebRTCValueManager.h"

NS_ASSUME_NONNULL_BEGIN

@interface RTCRtpParameters (ReactNativeWebRTCKit)

- (id)json;

@end

@interface RTCRtpSender (ReactNativeWebRTCKit)

@property (nonatomic, nullable) NSString *valueTag;
@property (nonatomic) NSArray<NSString *> *streamIds;

- (id)json;

@end

@interface RTCRtpReceiver (ReactNativeWebRTCKit)

@property (nonatomic, nullable) NSString *valueTag;
@property (nonatomic) NSArray<NSString *> *streamIds;

- (id)json;

@end

@interface RTCRtpTransceiver (ReactNativeWebRTCKit)

@property (nonatomic, nullable) NSString *valueTag;

- (id)json;

@end

@interface RTCPeerConnection (ReactNativeWebRTCKit) <WebRTCExportable>

@property (nonatomic, nullable) NSString *valueTag;
@property (nonatomic) BOOL microphoneInitialized;

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
