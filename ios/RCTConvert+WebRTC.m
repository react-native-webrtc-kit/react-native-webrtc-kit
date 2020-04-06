#import "RCTConvert+WebRTC.h"

#define IsNull(value) ((value) == nil || (value) == [NSNull null])

#define IsNonNull(value) (!IsNull(value))

#define Nullable(json) (IsNull(json) ? nil : (json))

#define InvalidValueError(propName, json) \
do { \
    RCTLogError(@"%@: invalid value '%@'", propName, json); \
    return nil; \
} while (0)

#define AssertNullable(propName, className, json) \
do { \
    if (IsNonNull(json) && ![json isKindOfClass: NSClassFromString(@"" #className)]) { \
        RCTLogError(@"%@: JSON value '%@' of type %@ cannot be converted to %@", \
            propName, json, [json classForCoder], @"" #className); \
        return nil; \
    } \
} while (0)

#define AssertNonNull(propName, className, json) \
do { \
    if (IsNull(json)) { \
        RCTLogError(@"%@: JSON value must not be null", propName); \
        return nil; \
    } else if (![json isKindOfClass: NSClassFromString(@"" #className)]) { \
        RCTLogError(@"%@: JSON value '%@' of type %@ cannot be converted to %@", \
            propName, json, [json classForCoder], @"" #className); \
        return nil; \
    } \
} while (0)

#define NonNullError(propName) \
do { \
    RTCLogError(@"%@: value must not be null", propName); \
    return nil;\
} while (0)

NS_ASSUME_NONNULL_BEGIN

@implementation RCTConvert (WebRTC)

+ (nullable RTCSessionDescription *)RTCSessionDescription:(nullable id)json
{
    AssertNonNull(@"RTCSessionDescription", NSDictionary, json);
    AssertNonNull(@"RTCSessionDescription.sdp", NSString, json[@"sdp"]);

    NSString *sdp = json[@"sdp"];
    RTCSdpType sdpType = [RTCSessionDescription typeForString:json[@"type"]];
    
    return [[RTCSessionDescription alloc] initWithType:sdpType sdp:sdp];
}

+ (nullable RTCIceCandidate *)RTCIceCandidate:(nullable id)json
{
    AssertNonNull(@"RTCIceCandidate", NSDictionary, json);
    AssertNonNull(@"RTCIceCandidate.candidate", NSString, json[@"candidate"]);
    
    NSString *sdp = json[@"candidate"];
    int sdpMLineIndex = [RCTConvert int:json[@"sdpMLineIndex"]];
    NSString *sdpMid = json[@"sdpMid"];
    
    return [[RTCIceCandidate alloc] initWithSdp: sdp
                                  sdpMLineIndex: sdpMLineIndex
                                         sdpMid: sdpMid];
}

+ (nullable RTCIceServer *)RTCIceServer:(nullable id)json
{
    AssertNonNull(@"RTCIceServer", NSDictionary, json);
    AssertNonNull(@"RTCIceServer.urls", NSArray, json[@"urls"]);
    AssertNullable(@"RTCIceServer.username", NSString, json[@"username"]);
    AssertNullable(@"RTCIceServer.credential", NSString, json[@"credential"]);

    NSArray<NSString *> *urls = [RCTConvert NSArray: json[@"urls"]];
    for (NSString *url in urls) {
        AssertNonNull(@"each RTCIceServer.urls", NSString, url);
    }
    
    return [[RTCIceServer alloc] initWithURLStrings: urls
                                           username: Nullable(json[@"username"])
                                         credential: Nullable(json[@"credential"])];
}

+ (nullable RTCConfiguration *)RTCConfiguration:(nullable id)json
{
    AssertNonNull(@"RTCConfiguration", NSDictionary, json);
    AssertNullable(@"RTCConfiguration.iceServers", NSArray, json[@"iceServers"]);

    RTCConfiguration *config = [[RTCConfiguration alloc] init];
    
    id iceServersJson = Nullable(json[@"iceServers"]);
    if (iceServersJson) {
        NSMutableArray<RTCIceServer *> *iceServers = [NSMutableArray new];
        for (id server in iceServersJson) {
            RTCIceServer *convert = [RCTConvert RTCIceServer: server];
            if (convert == nil)
                NonNullError(@"each RTCConfiguration.iceServers");
            else
                [iceServers addObject: convert];
        }
        config.iceServers = iceServers;
    }
    
    NSString *policy = Nullable(json[@"iceTransportPolicy"]);
    if (policy) {
        if ([policy isEqualToString: @"relay"])
            config.iceTransportPolicy = RTCIceTransportPolicyRelay;
        else if ([policy isEqualToString: @"all"])
            config.iceTransportPolicy = RTCIceTransportPolicyAll;
        else {
            InvalidValueError(@"RTCConfiguration.iceTransportPolicy", policy);
            return nil;
        }
    }
    
    NSString *semantics = Nullable(json[@"sdpSemantics"]);
    if (semantics) {
        if ([semantics isEqualToString: @"planb"])
            config.sdpSemantics = RTCSdpSemanticsPlanB;
        else if ([semantics isEqualToString: @"unified"])
            config.sdpSemantics = RTCSdpSemanticsUnifiedPlan;
        else {
            InvalidValueError(@"RTCConfiguration.sdpSemantics", semantics);
            return nil;
        }
    }
    
    return config;
}

+ (nullable WebRTCMediaStreamConstraints *)WebRTCMediaStreamConstraints:(nullable id)json
{
    AssertNonNull(@"RTCMediaStreamConstraints", NSDictionary, json);
    AssertNullable(@"RTCMediaStreamConstraints.video", NSDictionary, json[@"video"]);

    WebRTCMediaStreamConstraints *consts = [[WebRTCMediaStreamConstraints alloc] init];
    
    NSDictionary *videoConsts = Nullable(json[@"video"]);
    if (videoConsts) {
        AssertNullable(@"RTCMediaStreamConstraints.video.facingMode",
                       NSString, videoConsts[@"facingMode"]);
        AssertNullable(@"RTCMediaStreamConstraints.video.width",
                       NSNumber, videoConsts[@"width"]);
        AssertNullable(@"RTCMediaStreamConstraints.video.height",
                       NSNumber, videoConsts[@"height"]);
        AssertNullable(@"RTCMediaStreamConstraints.video.frameRate",
                       NSNumber, videoConsts[@"frameRate"]);
        AssertNullable(@"RTCMediaStreamConstraints.video.aspectRatio",
                       NSNumber, videoConsts[@"aspectRatio"]);
        AssertNullable(@"RTCMediaStreamConstraints.video.sourceId",
                       NSString, videoConsts[@"sourceId"]);
        
        consts.video = [[WebRTCMediaTrackVideoConstraints alloc] init];
        NSString *facingMode = videoConsts[@"facingMode"];
        if (facingMode) {
            if (!([facingMode isEqualToString: WebRTCFacingModeUser] ||
                  [facingMode isEqualToString: WebRTCFacingModeEnvironment])) {
                InvalidValueError(@"RTCMediaStreamConstraints.video.facingMode", facingMode);
            }
            consts.video.facingMode = facingMode;
        }
        
        NSNumber *width = videoConsts[@"width"];
        if (width)
            consts.video.width = [width intValue];
        
        NSNumber *height = videoConsts[@"height"];
        if (height) {
            consts.video.height = [height intValue];
        }
        
        NSNumber *frameRate = videoConsts[@"frameRate"];
        if (frameRate)
            consts.video.frameRate = [frameRate intValue];
        
        NSNumber *aspectRatio = videoConsts[@"aspectRatio"];
        if (aspectRatio)
            consts.video.aspectRatio = (CGFloat)[aspectRatio doubleValue];
        
        NSString *sourceId = videoConsts[@"sourceId"];
        if (sourceId)
            consts.video.sourceId = sourceId;
    }
    
    id audioConsts = Nullable(json[@"audio"]);
    if (audioConsts) {
        consts.audio = [[WebRTCMediaTrackAudioConstraints alloc] init];
        // 現在は bool 以外にパラメーターがないため、特に処理はなし
    }
    
    return consts;
}

+ (nullable RTCDataChannelConfiguration *)RTCDataChannelConfiguration:(nullable id)json {
    RTCDataChannelConfiguration *config = [[RTCDataChannelConfiguration alloc] init];
    if (json[@"ordered"]) {
        config.isOrdered = [RCTConvert BOOL:json[@"ordered"]];
    }
    if (json[@"negotiated"]) {
        config.isOrdered = [RCTConvert BOOL:json[@"negotiated"]];
    }
    NSNumber *channelId = Nullable(json[@"id"]);
    if (channelId) {
        config.channelId = [channelId intValue];
    }
    NSNumber *maxRetransmits = Nullable(json[@"maxRetransmits"]);
    if (channelId) {
        config.maxRetransmits = [maxRetransmits intValue];
    }
    NSNumber *maxPacketLifeTime = Nullable(json[@"maxPacketLifeTime"]);
    if (maxPacketLifeTime) {
        config.maxPacketLifeTime = [maxPacketLifeTime intValue];
    }
    NSString *protocol = Nullable(json[@"protocol"]);
    if (protocol) {
        config.protocol = protocol;
    }
    return config;
}

+ (nullable RTCDataBuffer *)RTCDataBuffer:(nullable id)json {
    AssertNonNull(@"data", NSString, json);
    AssertNonNull(@"binary", BOOL, json);
    BOOL isBinary = [RCTConvert BOOL:json[@"binary"]];
    NSData *data;
    if (isBinary) {
        // バイナリデータの場合 Base64 Encoded String に変換
        data = [[NSData alloc] initWithBase64EncodedString:json[@"data"] options:0];
    } else {
        // それ以外の場合は UTF8 String に変換する
        data = [json[@"data"] dataUsingEncoding:NSUTF8StringEncoding];
    }
    RTCDataBuffer *buffer = [[RTCDataBuffer alloc] initWithData:data isBinary:isBinary];
    return buffer;
}

@end

NS_ASSUME_NONNULL_END
