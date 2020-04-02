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

- (id)json
{
    NSString *state;
    switch (self.readyState) {
        case RTCDataChannelStateConnecting:
            state = @"connecting";
            break;
        case RTCDataChannelStateOpen:
            state = @"open";
            break;
        case RTCDataChannelStateClosing:
            state = @"closing";
            break;
        case RTCDataChannelStateClosed :
            state = @"closed";
            break;
        default:
            NSAssert(NO, @"invalid ready state");
    }
    return @{@"valueTag": self.valueTag,
             @"bufferedAmount": [[NSNumber alloc] initWithUnsignedLong: self.bufferedAmount],
             @"maxRetransmits": [[NSNumber alloc] initWithInt: self.maxRetransmits],
             @"maxPacketLifeTime": [[NSNumber alloc] initWithInt: self.maxPacketLifeTime],
             @"protocol": self.protocol,
             @"negotiated": @(self.isNegotiated),
             @"id":  [[NSNumber alloc] initWithInt: self.channelId],
             @"readyState": state};
}

@end


@implementation WebRTCModule (RTCDataChannel)

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
