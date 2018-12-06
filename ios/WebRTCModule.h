@import Foundation;

#import <WebRTC/WebRTC.h>

#if __has_include("RCTBridgeModule.h")
#import "RCTBridgeModule.h"
#import "RCTBridge.h"
#import "RCTEventDispatcher.h"
#import "RCTUtils.h"
#else
#import <React/RCTBridgeModule.h>
#import <React/RCTBridge.h>
#import <React/RCTEventDispatcher.h>
#import <React/RCTUtils.h>
#endif

#define SharedModule ([WebRTCModule shared])

extern const NSString *WebRTCErrorDomain;

NS_ASSUME_NONNULL_BEGIN

/* JavaScript 側から操作されるオブジェクト (RTCPeerConnection など) には
 * ユニークなタグ (value tag) を付与します。
 * JavaScript 側では value tag を指定することで
 * ネイティブ側のオブジェクトを操作できます。
 */
@interface WebRTCModule : NSObject <RCTBridgeModule>

@property (nonatomic) RTCPeerConnectionFactory *peerConnectionFactory;

@property (nonatomic) NSMutableDictionary<NSString *, RTCPeerConnection *> *peerConnections;
@property (nonatomic) NSMutableDictionary<NSString *, RTCMediaStreamTrack *> *tracks;
@property (nonatomic) NSMutableDictionary<NSString*, RTCRtpSender *> *senders;
@property (nonatomic) NSMutableDictionary<NSString*, RTCRtpReceiver *> *receivers;
@property (nonatomic) NSMutableDictionary<NSString*, RTCRtpTransceiver *> *transceivers;

+ (WebRTCModule *)shared;

- (NSString *)createNewValueTag;

@end

NS_ASSUME_NONNULL_END
