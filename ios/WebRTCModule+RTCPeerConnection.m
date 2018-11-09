#import <objc/runtime.h>

#import "WebRTCModule+RTCMediaStream.h"
#import "WebRTCModule+RTCPeerConnection.h"
#import "WebRTCModule+getUserMedia.h"
#import "WebRTCUtils.h"
#import "WebRTCValueManager.h"

NS_ASSUME_NONNULL_BEGIN

@implementation RTCRtpParameters (ReactNativeWebRTCKit)

- (id)json
{
    NSDictionary *rtcp = @{@"cname": self.rtcp.cname,
                           @"reducedSize":
                               [[NSNumber alloc] initWithBool:
                                self.rtcp.isReducedSize]};
    
    NSMutableArray *headerExts = [[NSMutableArray alloc] init];
    for (RTCRtpHeaderExtension *ext in self.headerExtensions) {
        [headerExts addObject:
         @{@"uri": ext.uri,
           @"id": [[NSNumber alloc] initWithInt: ext.id],
           @"encrypted":
               [[NSNumber alloc] initWithBool: ext.encrypted]}];
    }
    
    NSMutableArray *encodings = [[NSMutableArray alloc] init];
    for (RTCRtpEncodingParameters *enc in self.encodings) {
        NSMutableDictionary *json = [[NSMutableDictionary alloc] init];
        json[@"active"] = [[NSNumber alloc] initWithBool: enc.isActive];
        if (enc.maxBitrateBps)
            json[@"maxBitrate"] = enc.maxBitrateBps;
        if (enc.minBitrateBps)
            json[@"minBitrate"] = enc.minBitrateBps;
        if (enc.ssrc)
            json[@"ssrc"] = enc.ssrc;
        [encodings addObject: json];
    }
    
    NSMutableArray *codecs = [[NSMutableArray alloc] init];
    for (RTCRtpCodecParameters *codec in self.codecs) {
        NSMutableDictionary *json = [[NSMutableDictionary alloc] init];
        json[@"payloadType"] = [[NSNumber alloc] initWithInt: codec.payloadType];
        json[@"mimeType"] = [NSString stringWithFormat: @"%@/%@",
                             codec.kind, codec.name];
        json[@"parameters"] = codec.parameters;
        if (codec.clockRate)
            json[@"clockRate"] = codec.clockRate;
        if (codec.numChannels)
            json[@"channels"] = codec.numChannels;
        [codecs addObject: json];
    }
    
    return @{@"transactionId": self.transactionId,
             @"rtcp": rtcp,
             @"headerExtensions": headerExts,
             @"encodings": encodings,
             @"codecs": codecs};
}

@end

@implementation RTCRtpSender (ReactNativeWebRTCKit)

- (nullable NSString *)valueTag {
    return [WebRTCValueManager valueTagForObject: self];
}

- (void)setValueTag:(nullable NSString *)valueTag
{
    [WebRTCValueManager setValueTag: valueTag forObject: self];
}

@end

@implementation RTCRtpReceiver (ReactNativeWebRTCKit)

- (nullable NSString *)valueTag {
    return [WebRTCValueManager valueTagForObject: self];
}

- (void)setValueTag:(nullable NSString *)valueTag
{
    [WebRTCValueManager setValueTag: valueTag forObject: self];
}

- (id)json
{
    // RTCRtpSender/Receiver の track プロパティは
    // RTCMediaStreamTrcak を動的に生成するので、
    // 新しい value tag を割り当てる
    RTCMediaStreamTrack *track = self.track;
    track.valueTag = [[WebRTCModule shared] createNewValueTag];
    [WebRTCModule shared].tracks[track.valueTag] = track;
    
    return @{@"receiverId": self.receiverId,
             @"parameters": [self.parameters json],
             @"track": [track json]};
}

@end

@implementation RTCRtpTransceiver (ReactNativeWebRTCKit)

static void *transceiverValueTagKey = "transceiverValueTagKey";

- (nullable NSString *)valueTag {
    return objc_getAssociatedObject(self, transceiverValueTagKey);
}

- (void)setValueTag:(nullable NSString *)valueTag {
    objc_setAssociatedObject(self,
                             transceiverValueTagKey,
                             valueTag,
                             OBJC_ASSOCIATION_RETAIN_NONATOMIC);
}

- (id)json
{
    return @{@"valueTag": self.valueTag,
             @"mid": self.mid,
             @"sender": [self.sender json],
             @"receiver": [self.receiver json],
             @"stopped": [NSNumber numberWithBool: self.isStopped],
             @"direction": [RTCRtpTransceiver directionDescription: self.direction]};
}

+ (NSString *)directionDescription:(RTCRtpTransceiverDirection)direction
{
    switch (direction) {
        case RTCRtpTransceiverDirectionSendRecv:
            return @"sendrecv";
        case RTCRtpTransceiverDirectionSendOnly:
            return @"sendonly";
        case RTCRtpTransceiverDirectionRecvOnly:
            return @"recvonly";
        case RTCRtpTransceiverDirectionInactive:
            return @"inactive";
    }
}

+ (RTCRtpTransceiverDirection)directionFromString:(NSString *)string
{
    if ([string isEqualToString: @"sendrecv"])
        return RTCRtpTransceiverDirectionSendRecv;
    else if ([string isEqualToString: @"sendonly"])
        return RTCRtpTransceiverDirectionSendOnly;
    else if ([string isEqualToString: @"recvonly"])
        return RTCRtpTransceiverDirectionRecvOnly;
    else if ([string isEqualToString: @"inactive"])
        return RTCRtpTransceiverDirectionInactive;
    else
        NSAssert(NO, @"invalid direction %@", string);
}

@end

@implementation RTCPeerConnection (ReactNativeWebRTCKit)

static void *peerConnectionValueTagKey = "peerConnectionValueTag";

- (nullable NSString *)valueTag {
    return objc_getAssociatedObject(self, peerConnectionValueTagKey);
}

- (void)setValueTag:(nullable NSString *)valueTag {
    objc_setAssociatedObject(self,
                             peerConnectionValueTagKey,
                             valueTag,
                             OBJC_ASSOCIATION_RETAIN_NONATOMIC);
}

static void* remoteStreamsKey = "remoteStreams";

- (NSMutableDictionary<NSString *, RTCMediaStream *> *)remoteStreams {
    return objc_getAssociatedObject(self, remoteStreamsKey);
}

- (void)setRemoteStreams:(NSMutableDictionary<NSNumber *,RTCMediaStream *> *)remoteStreams {
    objc_setAssociatedObject(self, remoteStreamsKey, remoteStreams, OBJC_ASSOCIATION_RETAIN_NONATOMIC);
}

static void *connectionStateKey = "connectionState";

- (RTCPeerConnectionState)connectionState
{
    return (RTCPeerConnectionState)
        [objc_getAssociatedObject(self, connectionStateKey)
         unsignedIntegerValue];
}

- (void)setConnectionState:(RTCPeerConnectionState)newState
{
    objc_setAssociatedObject(self,
                             connectionStateKey,
                             [NSNumber numberWithUnsignedInteger: newState],
                             OBJC_ASSOCIATION_RETAIN_NONATOMIC);
}

- (void)closeAndFinish
{
    if (self.connectionState == RTCPeerConnectionStateDisconnecting ||
        self.connectionState == RTCPeerConnectionStateDisconnected)
        return;
    
    // モジュールの管理から外す
    [SharedModule.peerConnections removeObjectForKey: self.valueTag];
    
    self.connectionState = RTCPeerConnectionStateDisconnecting;
    
    // ストリームを外してから接続を閉じる
    NSMutableArray *localStreams = [[NSMutableArray alloc] initWithArray: self.localStreams];
    for (RTCMediaStream *stream in localStreams) {
        [self removeStream: stream];
    }
    
    NSMutableArray *remoteStreamKeys = [[NSMutableArray alloc] initWithArray: self.remoteStreams.allKeys];
    for (NSString *key in remoteStreamKeys) {
        RTCMediaStream *stream = self.remoteStreams[key];
        if (stream)
            [self removeStream: stream];
    }
    
    [self.remoteStreams removeAllObjects];
    
    [self close];

    self.connectionState = RTCPeerConnectionStateDisconnected;
}

- (void)detectConnectionStateAndFinishWithModule:(WebRTCModule *)module
{
    RTCPeerConnectionState connState = [self connectionState];
    RTCSignalingState sigState = [self signalingState];
    RTCIceGatheringState iceGathState = [self iceGatheringState];
    RTCIceConnectionState iceConnState = [self iceConnectionState];
    
    if (connState == RTCPeerConnectionStateDisconnected)
        return;
    
    else if (sigState == RTCSignalingStateStable &&
        iceGathState == RTCIceGatheringStateComplete &&
        iceConnState == RTCIceConnectionStateCompleted) {
        if (connState == RTCPeerConnectionStateNew ||
            connState == RTCPeerConnectionStateConnecting) {
            [self setConnectionState: RTCPeerConnectionStateConnected];
        }
        
    } else if (sigState == RTCSignalingStateClosed ||
                iceConnState == RTCIceConnectionStateClosed ||
                iceConnState == RTCIceConnectionStateFailed) {
        if (connState == RTCPeerConnectionStateDisconnecting ||
            connState == RTCPeerConnectionStateConnected) {
            [self setConnectionState: RTCPeerConnectionStateDisconnected];
            [self closeAndFinish];
        }
    }
}

@end

@implementation WebRTCModule (RTCPeerConnection)

#pragma mark - React Native Exports

/**
 * NOTE: RCT_EXPORT_METHOD などの React Native のマクロは
 * NS_ASSUME_NONNULL_BEGIN/END セクションを無視するため、
 * JavaScript 側から受け取る引数の nullability を明示的に指定しています。
 * ただし、 NSNumber に変換される number 型の引数は nonnull である必要があります。
 */

// MARK: -peerConnectionInit:constraints:valueTag:

RCT_EXPORT_METHOD(peerConnectionInit:(nonnull RTCConfiguration *)configuration
                  constraints:(nullable NSDictionary *)constraints
                  valueTag:(nonnull NSString *)valueTag) {
    RTCMediaConstraints *mediaConsts = [WebRTCUtils parseMediaConstraints: constraints];
    RTCPeerConnection *peerConnection =
    [self.peerConnectionFactory peerConnectionWithConfiguration:configuration
                                                    constraints: mediaConsts
                                                       delegate: self];
    peerConnection.connectionState = RTCPeerConnectionStateNew;
    peerConnection.valueTag = valueTag;
    peerConnection.remoteStreams = [NSMutableDictionary dictionary];
    self.peerConnections[valueTag] = peerConnection;
}

// MARK: -peerConnectionSetConfiguration:valueTag:

RCT_EXPORT_METHOD(peerConnectionSetConfiguration:(nonnull RTCConfiguration *)configuration
                  valueTag:(nonnull NSString *)valueTag) {
    RTCPeerConnection *peerConnection = self.peerConnections[valueTag];
    if (!peerConnection) {
        return;
    }
    
    [peerConnection setConfiguration:configuration];
}

// MARK: -peerConnectionAddTrack:streamIds:valueTag:

RCT_EXPORT_METHOD(peerConnectionAddTrack:(nonnull NSString *)trackValueTag
                  streamIds:(nonnull NSArray *)streamIds
                  valueTag:(nonnull NSString *)valueTag
                  resolver:(nonnull RCTPromiseResolveBlock)resolve
                  rejecter:(nonnull RCTPromiseRejectBlock)reject) {
    RTCPeerConnection *peerConnection = self.peerConnections[valueTag];
    if (!peerConnection) {
        reject(@"NotFoundError", @"peer connection is not found", nil);
        return;
    }
    RTCMediaStreamTrack *track = self.tracks[trackValueTag];
    if (!track) {
        reject(@"NotFoundError", @"track is not found", nil);
        return;
    }

    RTCRtpSender *sender = [peerConnection addTrack: track
                                          streamIds: streamIds];
    if (!sender) {
        reject(@"PeerConnectionError", @"cannot add the track", nil);
        return;
    }
    sender.valueTag = [self createNewValueTag];
    self.senders[sender.valueTag] = sender;
    
    resolve(@{@"id": sender.senderId,
              @"valueTag": sender.valueTag,
              @"parameters": [sender.parameters json],
              @"track": [track json]});
}

// MARK: -peerConnectionRemoveTrack:valueTag:

RCT_EXPORT_METHOD(peerConnectionRemoveTrack:(nonnull NSString *)senderValueTag
                  valueTag:(nonnull NSString *)valueTag
                  resolver:(nonnull RCTPromiseResolveBlock)resolve
                  rejecter:(nonnull RCTPromiseRejectBlock)reject) {
    RTCPeerConnection *peerConnection = self.peerConnections[valueTag];
    if (!peerConnection) {
        reject(@"NotFoundError", @"peer connection is not found", nil);
        return;
    }
    RTCRtpSender *sender = self.senders[senderValueTag];
    if (!sender) {
        reject(@"NotFoundError", @"sender is not found", nil);
        return;
    }
    
    self.senders[sender.valueTag] = nil;
    if ([peerConnection removeTrack: sender]) {
        resolve(nil);
    } else {
        reject(@"RemoveTrackFailed", @"cannot remove track", nil);
    }
}

// MARK: -peerConnectionCreateOffer:constraints:resolver:rejecter:

RCT_EXPORT_METHOD(peerConnectionCreateOffer:(nonnull NSString *)valueTag
                  constraints:(nullable NSDictionary *)constraints
                  resolver:(nonnull RCTPromiseResolveBlock)resolve
                  rejecter:(nonnull RCTPromiseRejectBlock)reject) {
    RTCPeerConnection *peerConnection = self.peerConnections[valueTag];
    if (!peerConnection) {
        return;
    }
    
    [peerConnection offerForConstraints:[WebRTCUtils parseMediaConstraints:constraints]
                      completionHandler:^(RTCSessionDescription *sdp, NSError *error) {
                          if (error) {
                              reject(@"CreateOfferFailed", error.userInfo[@"error"], error);
                          } else {
                              NSString *type = [RTCSessionDescription stringForType:sdp.type];
                              resolve(@{@"sdp": sdp.sdp, @"type": type});
                          }
                      }];
}

// MARK: -peerConnectionCreateAnswer:constraints:resolver:rejecter:

RCT_EXPORT_METHOD(peerConnectionCreateAnswer:(nonnull NSString *)valueTag
                  constraints:(nullable NSDictionary *)constraints
                  resolver:(nonnull RCTPromiseResolveBlock)resolve
                  rejecter:(nonnull RCTPromiseRejectBlock)reject) {
    RTCPeerConnection *peerConnection = self.peerConnections[valueTag];
    if (!peerConnection) {
        return;
    }
    
    [peerConnection answerForConstraints:[WebRTCUtils parseMediaConstraints:constraints]
                       completionHandler:^(RTCSessionDescription *sdp, NSError *error) {
                           if (error) {
                               reject(@"CreateAnswerFailed", error.userInfo[@"error"], error);
                           } else {
                               NSString *type = [RTCSessionDescription stringForType:sdp.type];
                               resolve(@{@"sdp": sdp.sdp, @"type": type});
                           }
                       }];
}

// MARK: -peerConnectionSetLocalDescription:valueTag:resolver:rejecter:

RCT_EXPORT_METHOD(peerConnectionSetLocalDescription:(nonnull RTCSessionDescription *)sdp
                  valueTag:(nonnull NSString *)valueTag
                  resolver:(nonnull RCTPromiseResolveBlock)resolve
                  rejecter:(nonnull RCTPromiseRejectBlock)reject) {
    RTCPeerConnection *peerConnection = self.peerConnections[valueTag];
    if (!peerConnection) {
        return;
    }
    
    // RTCPeerConnection に明示的な接続開始メソッドはないため、
    // local/remote SDP が指定されたら接続開始と判断する
    if (peerConnection.connectionState == RTCPeerConnectionStateNew)
        peerConnection.connectionState = RTCPeerConnectionStateConnecting;
    
    [peerConnection setLocalDescription:sdp
                      completionHandler: ^(NSError *error) {
                          if (error) {
                              reject(@"SetLocalDescriptionFailed", error.localizedDescription, error);
                          } else {
                              resolve(nil);
                          }
                      }];
}

// MARK: -peerConnectionSetRemoteDescription:valueTag:resolver:rejecter:

RCT_EXPORT_METHOD(peerConnectionSetRemoteDescription:(nonnull RTCSessionDescription *)sdp
                  valueTag:(nonnull NSString *)valueTag
                  resolver:(nonnull RCTPromiseResolveBlock)resolve
                  rejecter:(nonnull RCTPromiseRejectBlock)reject) {
    RTCPeerConnection *peerConnection = self.peerConnections[valueTag];
    if (!peerConnection) {
        return;
    }
    
    // RTCPeerConnection に明示的な接続開始メソッドはないため、
    // local/remote SDP が指定されたら接続開始と判断する
    if (peerConnection.connectionState == RTCPeerConnectionStateNew)
        peerConnection.connectionState = RTCPeerConnectionStateConnecting;
    
    [peerConnection setRemoteDescription:sdp
                       completionHandler: ^(NSError *error) {
                           if (error) {
                               reject(@"SetRemoteDescriptionFailed", error.localizedDescription, error);
                           } else {
                               resolve(nil);
                           }
                       }];
}

// MARK: -peerConnectionAddICECandidate:valueTag:resolver:rejecter:

RCT_EXPORT_METHOD(peerConnectionAddICECandidate:(nonnull RTCIceCandidate*)candidate
                  valueTag:(nonnull NSString *)valueTag
                  resolver:(nonnull RCTPromiseResolveBlock)resolve
                  rejecter:(nonnull RCTPromiseRejectBlock)reject) {
    RTCPeerConnection *peerConnection = self.peerConnections[valueTag];
    if (!peerConnection) {
        return;
    }
    
    [peerConnection addIceCandidate:candidate];
    resolve(nil);
}

// MARK: -peerConnectionClose:

RCT_EXPORT_METHOD(peerConnectionClose:(nonnull NSString *)valueTag)
{
    RTCPeerConnection *peerConnection = self.peerConnections[valueTag];
    if (!peerConnection) {
        return;
    }
    
    if (peerConnection.connectionState == RTCPeerConnectionStateConnecting ||
         peerConnection.connectionState == RTCPeerConnectionStateConnected) {
        [peerConnection closeAndFinish];
    }
}

#pragma mark - RTCPeerConnectionDelegate

- (void)peerConnection:(RTCPeerConnection *)peerConnection didChangeSignalingState:(RTCSignalingState)newState {
    [peerConnection detectConnectionStateAndFinishWithModule: self];
    [self.bridge.eventDispatcher sendDeviceEventWithName:@"peerConnectionSignalingStateChanged"
                                                    body:@{@"valueTag": peerConnection.valueTag,
                                                           @"signalingState": [WebRTCUtils stringForSignalingState:newState]}];
}

- (void)peerConnection:(RTCPeerConnection *)peerConnection didAddStream:(RTCMediaStream *)stream {
    NSMutableArray *tracks = [NSMutableArray array];
    for (RTCVideoTrack *track in stream.videoTracks) {
        track.valueTag = [self createNewValueTag];
        self.tracks[track.valueTag] = track;
        [tracks addObject:@{@"id": track.trackId,
                            @"kind": track.kind,
                            @"label": track.trackId,
                            @"enabled": @(track.isEnabled),
                            @"remote": @(YES),
                            @"readyState": @"live"}];
    }
    for (RTCAudioTrack *track in stream.audioTracks) {
        track.valueTag = [self createNewValueTag];
        self.tracks[track.valueTag] = track;
        [tracks addObject:@{@"id": track.trackId,
                            @"kind": track.kind,
                            @"label": track.trackId,
                            @"enabled": @(track.isEnabled),
                            @"remote": @(YES),
                            @"readyState": @"live"}];
    }
    
    stream.valueTag = [self createNewValueTag];
    peerConnection.remoteStreams[stream.valueTag] = stream;
    [self.bridge.eventDispatcher sendDeviceEventWithName:@"peerConnectionAddedStream"
                                                    body:@{@"valueTag": peerConnection.valueTag,
                                                           @"streamId": stream.streamId,
                                                           @"streamValueTag": stream.valueTag,
                                                           @"tracks": tracks}];
}

- (void)peerConnection:(RTCPeerConnection *)peerConnection didRemoveStream:(RTCMediaStream *)stream {
    // ストリームに対応するキーが複数あればエラー
    NSArray *keysArray = [peerConnection.remoteStreams allKeysForObject:stream];
    if (keysArray.count > 1) {
        NSLog(@"didRemoveStream - more than one stream entry found for stream instance with id: %@", stream.streamId);
    }
    
    NSString *streamValueTag = keysArray.count ? keysArray[0] : nil;
    if (!streamValueTag) {
        NSLog(@"didRemoveStream - stream not found, streamId: %@", stream.streamId);
        return;
    }
    for (RTCVideoTrack *track in stream.videoTracks) {
        [self.tracks removeObjectForKey: track.valueTag];
    }
    for (RTCAudioTrack *track in stream.audioTracks) {
        [self.tracks removeObjectForKey: track.valueTag];
    }
    [peerConnection.remoteStreams removeObjectForKey:streamValueTag];
    [self.bridge.eventDispatcher sendDeviceEventWithName:@"peerConnectionRemovedStream"
                                                    body:@{@"valueTag": peerConnection.valueTag,
                                                           @"streamId": stream.streamId,
                                                           @"streamValueTag": streamValueTag}];
}

- (void)peerConnection:(RTCPeerConnection *)peerConnection
        didAddReceiver:(RTCRtpReceiver *)rtpReceiver
               streams:(NSArray<RTCMediaStream *> *)mediaStreams
{
    rtpReceiver.valueTag = [self createNewValueTag];
    [WebRTCValueManager addNewObject: rtpReceiver];
    self.receivers[rtpReceiver.valueTag] = rtpReceiver;
    
    [self.bridge.eventDispatcher
     sendDeviceEventWithName: @"peerConnectionAddedReceiver"
     body:@{@"valueTag": peerConnection.valueTag,
            @"receiver": [rtpReceiver json]}];
}

- (void)peerConnection:(RTCPeerConnection *)peerConnection
     didRemoveReceiver:(RTCRtpReceiver *)rtpReceiver
{
    
    self.receivers[rtpReceiver.valueTag] = nil;
    [WebRTCValueManager removeValueTagForObject: rtpReceiver];

    [self.bridge.eventDispatcher
     sendDeviceEventWithName: @"peerConnectionRemoveReceiver"
     body:@{@"valueTag": peerConnection.valueTag,
            @"receiver": [rtpReceiver json]}];
}
- (void)peerConnectionShouldNegotiate:(RTCPeerConnection *)peerConnection {
    [self.bridge.eventDispatcher sendDeviceEventWithName:@"peerConnectionShouldNegotiate"
                                                    body:@{@"valueTag": peerConnection.valueTag}];
}

- (void)peerConnection:(RTCPeerConnection *)peerConnection didChangeIceConnectionState:(RTCIceConnectionState)newState {
    [peerConnection detectConnectionStateAndFinishWithModule: self];
    [self.bridge.eventDispatcher sendDeviceEventWithName:@"peerConnectionIceConnectionChanged"
                                                    body:@{@"valueTag": peerConnection.valueTag,
                                                           @"iceConnectionState": [WebRTCUtils stringForICEConnectionState:newState]}];
}

- (void)peerConnection:(RTCPeerConnection *)peerConnection didChangeIceGatheringState:(RTCIceGatheringState)newState {
    [peerConnection detectConnectionStateAndFinishWithModule: self];
    [self.bridge.eventDispatcher sendDeviceEventWithName:@"peerConnectionIceGatheringChanged"
                                                    body:@{@"valueTag": peerConnection.valueTag,
                                                           @"iceGatheringState": [WebRTCUtils stringForICEGatheringState:newState]}];
}

- (void)peerConnection:(RTCPeerConnection *)peerConnection didGenerateIceCandidate:(RTCIceCandidate *)candidate {
    [self.bridge.eventDispatcher sendDeviceEventWithName:@"peerConnectionGotICECandidate"
                                                    body:@{@"valueTag": peerConnection.valueTag,
                                                           @"candidate": @{
                                                                   @"candidate": candidate.sdp,
                                                                   @"sdpMLineIndex": @(candidate.sdpMLineIndex),
                                                                   @"sdpMid": candidate.sdpMid}}];
}

- (void)peerConnection:(RTCPeerConnection *)peerConnection didRemoveIceCandidates:(NSArray<RTCIceCandidate *> *)candidates {
    [peerConnection removeIceCandidates: candidates];
}

- (void)peerConnection:(RTCPeerConnection*)peerConnection didOpenDataChannel:(RTCDataChannel*)dataChannel {
    // DataChannel は現在対応しない
}

@end

NS_ASSUME_NONNULL_END
