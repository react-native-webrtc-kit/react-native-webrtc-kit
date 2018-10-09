@import Foundation;
@import AVFoundation;

extern const NSString *WebRTCFacingModeUser;
extern const NSString *WebRTCFacingModeEnvironment;

@interface WebRTCMediaTrackVideoConstraints : NSObject <NSCopying>

@property (copy, nullable) NSString *facingMode;
@property (assign) int width;
@property (assign) int height;
@property (assign) int frameRate;
@property (assign) CGFloat aspectRatio;

// 映像ソースにするデバイス (AVCaptureDevice) の ID
@property (copy, nullable) NSString *sourceId;

@end

@interface WebRTCMediaTrackAudioConstraints : NSObject <NSCopying>

@end

@interface WebRTCMediaStreamConstraints : NSObject <NSCopying>

@property (copy, nullable) WebRTCMediaTrackVideoConstraints *video;
@property (copy, nullable) WebRTCMediaTrackAudioConstraints *audio;

- (instancetype)init;

@end
