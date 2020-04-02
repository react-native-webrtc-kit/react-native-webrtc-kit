#import <objc/runtime.h>
#import "WebRTCModule+RTCDataChannel.h"
#import "WebRTCValueManager.h"
#import <WebRTC/RTCDataChannelConfiguration.h>

NS_ASSUME_NONNULL_BEGIN

@implementation RTCDataChannel (ReactNativeWebRTCKit)

- (nullable NSString *)valueTag {
    return [WebRTCValueManager valueTagForObject: self];
}

- (void)setValueTag:(nullable NSString *)valueTag
{
    [WebRTCValueManager setValueTag: valueTag forObject: self];
}

@end


@implementation WebRTCModule (RTCDataChannel)

// MARK: -peerConnectionCreateDataChannel:label:config:valueTag:resolver:rejecter

RCT_EXPORT_METHOD(peerConnectionCreateDataChannel: (NSString *)label
                  config:(RTCDataChannelConfiguration *)config
                  valueTag: (NSString *) valueTag
                  resolver:(nonnull RCTPromiseResolveBlock)resolve
                  rejecter:(nonnull RCTPromiseRejectBlock)reject)
{
  // valueTag に相当する peer Connection を見つける
  RTCPeerConnection *peerConnection = [self peerConnectionForKey: valueTag];
  if (!peerConnection) {
    // peer connection がなければ reject する
    reject(@"NotFoundError", @"peer connection is not found", nil);
    return;
  }
  // DataChannel を Peer Connection に追加する
  RTCDataChannel *dataChannel = [peerConnection dataChannelForLabel:label configuration:config];
  // 新たに valueTag を紐付ける
  dataChannel.valueTag = [self createValueTag];
  resolve([dataChannel json]);
}

// MARK: -dataChannelSend:message:valueTag:
// TODO(kdxu): RTCDataChannelSend 関数を実装する
// RCT_EXPORT_METHOD(send:(nonnull NSString*) message
//                     valueTag:(nonnull NSString *) valueTag)
//{
//}

// MARK: -close:message:valueTag:
// TODO(kdxu): RTCDataChannelClose 関数を実装する
// RCT_EXPORT_METHOD(dataChannelClose:(nonnull NSString *) valueTag)
//{
//}

@end

NS_ASSUME_NONNULL_END
