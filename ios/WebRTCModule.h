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

@property(nonatomic) RTCPeerConnectionFactory *peerConnectionFactory;
@property(nonatomic, readonly) NSArray<RTCPeerConnection *> *peerConnections;
@property(nonatomic, readonly) NSArray<RTCMediaStream *> *streams;
@property(nonatomic, readonly) NSArray<RTCMediaStreamTrack *> *tracks;
@property(nonatomic, readonly) NSArray<RTCRtpSender *> *senders;
@property(nonatomic, readonly) NSArray<RTCRtpReceiver *> *receivers;
@property(nonatomic, readonly) NSArray<RTCRtpTransceiver *> *transceivers;
// DataChannel のリストは JS の RTCPeerConnection のプロパティとして保持することはないので、以下の実装は行わない
// @property (nonatomic, readonly) NSArray <RTCDataChannel *> *dataChannels;

@property(nonatomic, assign) AVAudioSessionPortOverride portOverride;
@property(nonatomic) BOOL microphoneEnabled;

+ (WebRTCModule *)shared;

- (NSString *)createNewValueTag;

- (nullable RTCPeerConnection *)peerConnectionForKey:(NSString *)key;
- (void)addPeerConnection:(RTCPeerConnection *)peerConn
                   forKey:(NSString *)key;
- (void)removePeerConnectionForKey:(NSString *)key;

- (nullable RTCMediaStream *)streamForKey:(NSString *)key;
- (void)addStream:(RTCMediaStream *)stream
           forKey:(NSString *)key;
- (void)removeStreamForKey:(NSString *)key;

- (nullable RTCMediaStreamTrack *)trackForKey:(NSString *)key;
- (void)addTrack:(RTCMediaStreamTrack *)track
          forKey:(NSString *)key;
- (void)removeTrackForKey:(NSString *)key;

- (nullable RTCRtpSender *)senderForKey:(NSString *)key;
- (void)addSender:(RTCRtpSender *)sender
           forKey:(NSString *)key;
- (void)removeSenderForKey:(NSString *)key;

- (nullable RTCRtpReceiver *)receiverForKey:(NSString *)key;
- (void)addReceiver:(RTCRtpReceiver *)receiver
             forKey:(NSString *)key;
- (void)removeReceiverForKey:(NSString *)key;

- (nullable RTCRtpTransceiver *)transceiverForKey:(NSString *)key;
- (void)addTransceiver:(RTCRtpTransceiver *)transceiver
                forKey:(NSString *)key;
- (void)removeTransceiverForKey:(NSString *)key;

- (nullable RTCDataChannel *)dataChannelForKey:(NSString *)key;
- (void)addDataChannel:(RTCDataChannel *)channel
                forKey:(NSString *)key;
- (void)removeDataChannelForKey:(NSString *)key;

@end

NS_ASSUME_NONNULL_END
