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
             @"bufferedAmount": [[NSNumber alloc] initWithUnsignedLongLong: self.bufferedAmount],
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

RCT_EXPORT_METHOD(dataChannelSend:(nonnull RTCDataBuffer*) buffer
                     valueTag:(nonnull NSString *) valueTag)
{
    RTCDataChannel *channel = [self dataChannelForKey:valueTag];
    if (channel) {
        [channel sendData:buffer];
    }
}

// MARK: -close:message:valueTag:
RCT_EXPORT_METHOD(dataChannelClose:(nonnull NSString *) valueTag)
{
    RTCDataChannel *channel = [self dataChannelForKey:valueTag];
    if (channel) {
        [channel close];
        [self removeDataChannelForKey:valueTag];
    }
}

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
    id event = @{@"id": @(channel.channelId),
                 @"valueTag": channel.valueTag,
                 @"buffer": buffer};
  [self.bridge.eventDispatcher sendDeviceEventWithName:@"dataChannelOnMessage" body:event];
}

- (void) dataChannel:(RTCDataChannel *)channel didChangeBufferedAmount:(uint64_t)amount {
    [self.bridge.eventDispatcher sendDeviceEventWithName:@"dataChannelOnChangeBufferedAmount"
                                                    body: @{@"id": @(channel.channelId),
                                                            @"valueTag": channel.valueTag,
                                                            @"bufferedAmount": [[NSNumber alloc] initWithUnsignedLongLong: amount]}];
}


@end

NS_ASSUME_NONNULL_END
