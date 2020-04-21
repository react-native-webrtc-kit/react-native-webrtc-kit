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

// MARK: -dataChannelSend:message:valueTag:resolver:rejecter:
RCT_EXPORT_METHOD(dataChannelSend:(nonnull RTCDataBuffer*) buffer
                  valueTag:(nonnull NSString *) valueTag
                  resolver:(nonnull RCTPromiseResolveBlock)resolve
                  rejecter:(nonnull RCTPromiseRejectBlock)reject)
{
    RTCDataChannel *channel = [self dataChannelForKey:valueTag];
    if (!channel) {
        reject(@"NotFoundError", @"datachannel is not found", nil);
        return;
    }
    NSLog(@"# dataChannelSend: buffer %@", [[buffer data] description]);
    [channel sendData:buffer];
    resolve(nil);
}

// XXX(kdxu): PeerConnection.close() と統一性をもたせるためclose メソッドは同期メソッドにする
// MARK: -close:message:valueTag
RCT_EXPORT_METHOD(dataChannelClose:(nonnull NSString *) valueTag)
{
    RTCDataChannel *channel = [self dataChannelForKey:valueTag];
    if (!channel) {
        return;
    }
    // open でない場合閉じさせない
    if (channel.readyState != RTCDataChannelStateOpen) {
      return;
    }
    [channel close];
    return;
}

#pragma mark - RTCDataChannelDelegate

- (void)dataChannelDidChangeState:(RTCDataChannel*)channel
{
    if (!channel.valueTag) {
        return;
    }
    [self.bridge.eventDispatcher sendDeviceEventWithName:@"dataChannelStateChanged"
                                                    body: @{@"id": [[NSNumber alloc] initWithInt: channel.channelId],
                                                            @"valueTag": channel.valueTag,
                                                            @"readyState": [WebRTCUtils stringForDataChannelState:channel.readyState]}];
    // data channel が閉じられた場合はモジュールの管理から該当の dataChannel を外す
    if(channel.readyState == RTCDataChannelStateClosed) {
        [self removeDataChannelForKey:channel.valueTag];
    }
}

- (void)dataChannel:(RTCDataChannel *)channel didReceiveMessageWithBuffer:(RTCDataBuffer *)buffer
{

    NSString *data;
    if (buffer.isBinary) {
      // バイナリデータの場合は base64 で string に戻す
      data = [buffer.data base64EncodedStringWithOptions:0];
    } else {
       // バイナリデータでない場合、UTF-8 エンコーディングで string に戻す
      data = [[NSString alloc] initWithData:buffer.data
                                   encoding:NSUTF8StringEncoding];
    }
    [self.bridge.eventDispatcher sendDeviceEventWithName:@"dataChannelOnMessage"
                                                    body:@{@"id": [[NSNumber alloc] initWithInt: channel.channelId],
                                                           @"valueTag": channel.valueTag,
                                                           @"binary": @(buffer.isBinary),
                                                           // data は base64 encoding に失敗した場合 nil が返ってくる可能性がある
                                                           // 辞書リテラルで nil を代入すると落ちるので nil の場合をチェックしてその場合は NSNull を代入するようにする
                                                           @"data": (data ? data : [NSNull null])
                                                    }];

}

- (void) dataChannel:(RTCDataChannel *)channel didChangeBufferedAmount:(uint64_t)previousAmount {
    [self.bridge.eventDispatcher sendDeviceEventWithName:@"dataChannelOnChangeBufferedAmount"
                                                    body: @{@"id": [[NSNumber alloc] initWithInt: channel.channelId],
                                                            @"valueTag": channel.valueTag,
                                                            @"bufferedAmount": [[NSNumber alloc] initWithUnsignedLongLong: channel.bufferedAmount]
                                                    }];
}


@end

NS_ASSUME_NONNULL_END
