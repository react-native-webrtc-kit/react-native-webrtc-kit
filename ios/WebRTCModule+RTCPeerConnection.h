@import Foundation;

#import "WebRTCModule.h"

typedef NS_ENUM(NSUInteger, RTCPeerConnectionState) {
    RTCPeerConnectionStateNew,
    RTCPeerConnectionStateConnecting,
    RTCPeerConnectionStateConnected,
    RTCPeerConnectionStateDisconnecting,
    RTCPeerConnectionStateDisconnected
};

NS_ASSUME_NONNULL_BEGIN

@interface RTCPeerConnection (ReactNativeWebRTCKit)

@property (nonatomic, nullable) NSString *valueTag;

 // key = valueTag, not MediaStream.id
@property (nonatomic) NSMutableDictionary<NSString *, RTCMediaStream *> *remoteStreams;

 // key = MediaStreamTrack.id
@property (nonatomic) NSMutableDictionary<NSString*, RTCMediaStreamTrack *> *remoteTracks;

@property (nonatomic) RTCPeerConnectionState connectionState;

/**
 * 通常の close を含む React Native 向けの終了処理を行う
 */
- (void)closeAndFinish;

@end


@interface WebRTCModule (RTCPeerConnection) <RTCPeerConnectionDelegate>

@end

NS_ASSUME_NONNULL_END
