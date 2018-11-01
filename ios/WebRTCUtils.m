#import "WebRTCUtils.h"

@implementation WebRTCUtils

+ (NSString *)stringForICEConnectionState:(RTCIceConnectionState)state
{
    switch (state) {
        case RTCIceConnectionStateNew: return @"new";
        case RTCIceConnectionStateChecking: return @"checking";
        case RTCIceConnectionStateConnected: return @"connected";
        case RTCIceConnectionStateCompleted: return @"completed";
        case RTCIceConnectionStateFailed: return @"failed";
        case RTCIceConnectionStateDisconnected: return @"disconnected";
        case RTCIceConnectionStateClosed: return @"closed";
        case RTCIceConnectionStateCount: return @"count";
    }
    return nil;
}

+ (NSString *)stringForICEGatheringState:(RTCIceGatheringState)state
{
    switch (state) {
        case RTCIceGatheringStateNew: return @"new";
        case RTCIceGatheringStateGathering: return @"gathering";
        case RTCIceGatheringStateComplete: return @"complete";
    }
    return nil;
}

+ (NSString *)stringForSignalingState:(RTCSignalingState)state
{
    switch (state) {
        case RTCSignalingStateStable: return @"stable";
        case RTCSignalingStateHaveLocalOffer: return @"have-local-offer";
        case RTCSignalingStateHaveLocalPrAnswer: return @"have-local-pranswer";
        case RTCSignalingStateHaveRemoteOffer: return @"have-remote-offer";
        case RTCSignalingStateHaveRemotePrAnswer: return @"have-remote-pranswer";
        case RTCSignalingStateClosed: return @"closed";
    }
    return nil;
}

+ (NSDictionary<NSString *, NSString *> *)parseJavaScriptConstraints:(NSDictionary *)src
{
    NSMutableDictionary<NSString *, NSString *> *result = [NSMutableDictionary dictionary];
    
    [src enumerateKeysAndObjectsUsingBlock:^(id  _Nonnull key, id  _Nonnull obj, BOOL * _Nonnull stop) {
        NSString *value;
        if ([obj isKindOfClass:[NSNumber class]]) {
            value = [obj boolValue] ? @"true" : @"false";
        } else {
            value = [obj description];
        }
        result[[key description]] = value;
    }];
    
    return result;
}

+ (RTCMediaConstraints *)parseMediaConstraints:(nullable NSDictionary *)constraints {
    id mandatory = constraints[@"mandatory"];
    NSDictionary<NSString *, NSString *> *mandatory_;
    
    if ([mandatory isKindOfClass:[NSDictionary class]]) {
        mandatory_ = [self parseJavaScriptConstraints:(NSDictionary *)mandatory];
    }
    
    id optional = constraints[@"optional"];
    NSMutableDictionary <NSString *, NSString *> *optional_ = [NSMutableDictionary dictionary];
    
    if ([optional isKindOfClass:[NSArray class]]) {
        for (id o in (NSArray *)optional) {
            if ([o isKindOfClass:[NSDictionary class]]) {
                NSDictionary<NSString *, NSString *> *o_ = [self parseJavaScriptConstraints:(NSDictionary *)o];
                [optional_ addEntriesFromDictionary:o_];
            }
        }
    }
    
    return [[RTCMediaConstraints alloc] initWithMandatoryConstraints:mandatory_
                                                 optionalConstraints:optional_];
}

@end
