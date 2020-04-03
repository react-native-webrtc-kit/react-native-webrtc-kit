#import <objc/runtime.h>
#import "WebRTCModule+RTCDataChannel.h"
#import "WebRTCValueManager.h"
#import <WebRTC/RTCDataChannelConfiguration.h>
#import "WebRTCUtils.h"

NS_ASSUME_NONNULL_BEGIN

@implementation RTCDataChannel (ReactNativeWebRTCKit)

- (nullable NSString *)valueTag {
    return [WebRTCValueManager valueTagForObject: self];
}

- (void)setValueTag:(nullable NSString *)valueTag
{
    [WebRTCValueManager setValueTag: valueTag forObject: self];
}

- (id)json
{
    return @{@"valueTag": self.valueTag,
             @"bufferedAmount": [[NSNumber alloc] initWithUnsignedLong: self.bufferedAmount],
             @"maxRetransmits": [[NSNumber alloc] initWithInt: self.maxRetransmits],
             @"maxPacketLifeTime": [[NSNumber alloc] initWithInt: self.maxPacketLifeTime],
             @"protocol": self.protocol,
             @"negotiated": @(self.isNegotiated),
             @"ordered": @(self.isOrdered),
             @"id":  [[NSNumber alloc] initWithInt: self.channelId],
             @"label": self.label,
             @"readyState": [WebRTCUtils stringForDataChannelState:self.readyState]};
}


@end

@implementation WebRTCModule (RTCDataChannel)

// MARK: -dataChannelSend:message:valueTag:
// TODO(kdxu): RTCDataChannelSend 関数を実装する
// RCT_EXPORT_METHOD(dataChannelSend:(nonnull NSString*) message
//                     valueTag:(nonnull NSString *) valueTag)
//{
//}

// MARK: -close:message:valueTag:
// TODO(kdxu): RTCDataChannelClose 関数を実装する
// RCT_EXPORT_METHOD(dataChannelClose:(nonnull NSString *) valueTag)
//{
//}

#pragma mark - RTCDataChannelDelegate

- (void)dataChannelDidChangeState:(RTCDataChannel*)channel
{
  [self.bridge.eventDispatcher sendDeviceEventWithName:@"dataChannelStateChanged"
                                                  body: @{@"id": @(channel.channelId),
                                                  @"valueTag": channel.valueTag,
                                                  @"readyState": [WebRTCUtils stringForDataChannelState:channel.readyState]}];
}

- (void)dataChannel:(RTCDataChannel *)channel didReceiveMessageWithBuffer:(RTCDataBuffer *)buffer
{

//  [self.bridge.eventDispatcher sendDeviceEventWithName:@"dataChannelReceiveMessage"
 //                                                 body:event];
}


@end

NS_ASSUME_NONNULL_END
