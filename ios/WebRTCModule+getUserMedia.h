@import Foundation;

#import "WebRTCModule.h"
#import <WebRTC/WebRTC.h>

#define WebRTCCamera ([WebRTCCameraVideoCapturer shared])

NS_ASSUME_NONNULL_BEGIN

@interface WebRTCCameraVideoCapturer : NSObject <RTCVideoCapturerDelegate>

@property (nonatomic, readonly, nonnull) RTCCameraVideoCapturer *nativeCapturer;
@property (nonatomic, readonly) BOOL isRunning;
@property (nonatomic, readonly, nonnull) NSArray<NSString *> *trackValueTags;

+ (WebRTCCameraVideoCapturer *)shared;
+ (NSArray<AVCaptureDevice *> *)captureDevices;
+ (nullable AVCaptureDevice *)captureDeviceForPosition:(AVCaptureDevicePosition)position;
+ (nullable AVCaptureDeviceFormat *)suitableFormatForDevice:(AVCaptureDevice *)device
                                                      width:(int)width
                                                     height:(int)height;
+ (int)suitableFrameRateForFormat:(AVCaptureDeviceFormat *)format
                        frameRate:(int)frameRate;

- (void)startCaptureWithAllDevices;
- (void)startCaptureWithDevice:(AVCaptureDevice *)device
                        format:(AVCaptureDeviceFormat *)format
                     frameRate:(int)frameRate;
- (void)startCaptureWithDevice:(AVCaptureDevice *)device
                        format:(AVCaptureDeviceFormat *)format
                     frameRate:(int)frameRate
             completionHandler:(nullable void (^)(NSError *))completionHandler;
- (void)stopCapture;
- (void)stopCaptureWithCompletionHandler:(nullable void (^)(void))completionHandler;

- (void)reloadApplication;

- (void)addTrackValueTag:(NSString *)valueTag;
- (void)removeTrackValueTag:(NSString *)valueTag;

@end

@interface WebRTCModule (getUserMedia)

- (void)reloadGetUserMedia;

@end

NS_ASSUME_NONNULL_END
