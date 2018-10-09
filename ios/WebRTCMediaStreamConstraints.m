#import "WebRTCMediaStreamConstraints.h"

const NSString *WebRTCFacingModeUser = @"user";
const NSString *WebRTCFacingModeEnvironment =  @"environment";

@implementation WebRTCMediaTrackVideoConstraints

- (instancetype)init
{
    self = [super init];
    if (self) {
        _facingMode = nil;
        _width = -1;
        _height = -1;
        _frameRate = -1;
        _aspectRatio = -1;
    }
    return self;
}

// MARK: NSCopying

- (id)copyWithZone:(NSZone *)zone
{
    WebRTCMediaTrackVideoConstraints *consts = [[WebRTCMediaTrackVideoConstraints allocWithZone: zone] init];
    consts.facingMode = _facingMode;
    consts.width = _width;
    consts.height = _height;
    consts.frameRate = _frameRate;
    consts.aspectRatio = _aspectRatio;
    return consts;
}

@end

@implementation WebRTCMediaTrackAudioConstraints

// MARK: NSCopying

- (id)copyWithZone:(NSZone *)zone
{
    return [[WebRTCMediaTrackAudioConstraints allocWithZone: zone] init];
}

@end

@implementation WebRTCMediaStreamConstraints

- (instancetype)init
{
    self = [super init];
    if (self) {
        _video = nil;
        _audio = nil;
    }
    return self;
}

// MARK: NSCopying

- (id)copyWithZone:(NSZone *)zone
{
    WebRTCMediaStreamConstraints *consts = [[WebRTCMediaStreamConstraints allocWithZone: zone] init];
    consts.video = _video;
    consts.audio = _audio;
    return consts;
}

@end
