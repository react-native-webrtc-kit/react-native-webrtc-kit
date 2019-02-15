#import "WebRTCModule+getUserMedia.h"
#import "WebRTCModule+RTCMediaStream.h"
#import "WebRTCMediaStreamConstraints.h"

NS_ASSUME_NONNULL_BEGIN

static WebRTCCameraVideoCapturer *sharedCameraVideoCapturer = nil;

@interface WebRTCCameraVideoCapturer ()

@property (nonatomic, readonly, nonnull) dispatch_queue_t lock;

@end

@implementation WebRTCCameraVideoCapturer

+ (WebRTCCameraVideoCapturer *)shared
{
    if (!sharedCameraVideoCapturer)
        sharedCameraVideoCapturer = [[WebRTCCameraVideoCapturer alloc] init];
    return sharedCameraVideoCapturer;
}

- (instancetype)init
{
    self = [super init];
    if (self) {
        _lock = dispatch_queue_create("WebRTCCameraVideoCapturer",
                                      DISPATCH_QUEUE_SERIAL);
        _nativeCapturer = [[RTCCameraVideoCapturer alloc] initWithDelegate: self];
        _isRunning = NO;
        _trackValueTags = [[NSMutableArray alloc] init];
    }
    return self;
}

+ (NSArray<AVCaptureDevice *> *)captureDevices
{
    return [RTCCameraVideoCapturer captureDevices];
}

+ (nullable AVCaptureDevice *)captureDeviceForPosition:(AVCaptureDevicePosition)position
{
    for (AVCaptureDevice *device in [WebRTCCameraVideoCapturer captureDevices]) {
        if (device.position == position)
            return device;
    }
    return nil;
}

+ (nullable AVCaptureDeviceFormat *)suitableFormatForDevice:(AVCaptureDevice *)device
                                                width:(int)width
                                               height:(int)height
{
    NSArray<AVCaptureDeviceFormat *> *formats = [RTCCameraVideoCapturer supportedFormatsForDevice: device];
    AVCaptureDeviceFormat *currentFormat = nil;
    int currentDiff = INT_MAX;
    for (AVCaptureDeviceFormat *format in formats) {
        CMVideoDimensions dim = CMVideoFormatDescriptionGetDimensions([format formatDescription]);
        int diff = abs(width - dim.width) +  abs(height - dim.height);
        if (diff < currentDiff) {
            currentFormat = format;
            currentDiff = diff;
        }
    }
    return currentFormat;
}

+ (int)suitableFrameRateForFormat:(AVCaptureDeviceFormat *)format
                        frameRate:(int)frameRate
{
    int maxFrameRate = 0;
    for (AVFrameRateRange *range in [format videoSupportedFrameRateRanges]) {
        if (maxFrameRate < range.maxFrameRate)
            maxFrameRate = range.maxFrameRate;
        if (range.minFrameRate <= frameRate && frameRate <= range.maxFrameRate)
            return frameRate;
    }
    return maxFrameRate;
}

- (void)startCaptureWithAllDevices
{
    for (AVCaptureDevice *device in [WebRTCCameraVideoCapturer captureDevices]) {
        for (AVCaptureDeviceFormat *format in [device formats]) {
            // fps は適当
            int frameRate = [WebRTCCameraVideoCapturer
                             suitableFrameRateForFormat: format
                             frameRate: 60];
            [self startCaptureWithDevice: device
                                  format: format
                               frameRate: frameRate];
        }
    }
}

- (void)startCaptureWithDevice:(AVCaptureDevice *)device
                        format:(AVCaptureDeviceFormat *)format

                     frameRate:(int)frameRate
{
    [self startCaptureWithDevice: device
                          format: format
                       frameRate: frameRate
               completionHandler: nil];
}

- (void)startCaptureWithDevice:(AVCaptureDevice *)device
                        format:(AVCaptureDeviceFormat *)format
                     frameRate:(int)frameRate
             completionHandler:(nullable void (^)(NSError *))completionHandler;
{
    if (_isRunning)
        return;
    
    _isRunning = YES;
    frameRate = [WebRTCCameraVideoCapturer suitableFrameRateForFormat: format
                                                            frameRate: frameRate];
    [_nativeCapturer startCaptureWithDevice: device
                                     format: format
                                        fps: frameRate
                          completionHandler: completionHandler];
}

- (void)stopCapture
{
    [self stopCaptureWithCompletionHandler: nil];
}

- (void)stopCaptureWithCompletionHandler:(nullable void (^)(void))completionHandler
{
    if (_isRunning) {
        [_nativeCapturer stopCaptureWithCompletionHandler: ^() {
            if (completionHandler)
                completionHandler();
            _isRunning = NO;
        }];
    }
}

// MARK: RTCVideoCapturerDelegate

- (void)capturer:(RTCVideoCapturer *)capturer didCaptureVideoFrame:(RTCVideoFrame *)frame
{
    if (!_isRunning)
        return;

    // すべてのローカルストリームに対して映像フレームを渡し、
    // タグに対するストリームが存在しない場合はタグを消す。
    NSMutableArray *tagsToRemove = nil;

    for (NSString *valueTag in _trackValueTags) {
        RTCMediaStreamTrack *track = [WebRTCModule shared].tracks[valueTag];
        if ([track isKindOfClass: [RTCVideoTrack class]] &&
            track.readyState == RTCMediaStreamTrackStateLive) {
            RTCVideoTrack *video = (RTCVideoTrack *)track;
            [video.source capturer: capturer didCaptureVideoFrame: frame];
        } else {
            if (!tagsToRemove)
                tagsToRemove = [[NSMutableArray alloc] init];
            [tagsToRemove addObject: valueTag];
        }
    }
    
    if (tagsToRemove) {
        for (NSString *tag in tagsToRemove) {
            [self removeTrackValueTag: tag];
        }
    }
}

- (void)reloadApplication
{
    [self stopCapture];
    
    dispatch_sync(self.lock, ^{
        _trackValueTags = @[];
    });
}

- (void)addTrackValueTag:(NSString *)valueTag
{
    dispatch_sync(self.lock, ^{
        _trackValueTags = [_trackValueTags arrayByAddingObject: valueTag];
    });
}

- (void)removeTrackValueTag:(NSString *)valueTag
{
    dispatch_sync(self.lock, ^{
        NSMutableArray *new = [[NSMutableArray alloc] init];
        for (NSString *old in _trackValueTags) {
            if (![old isEqualToString: valueTag])
                [new addObject: valueTag];
        }
        _trackValueTags = new;
    });
}

@end

@implementation WebRTCModule (getUserMedia)

#pragma mark - React Native Exports

RCT_EXPORT_METHOD(getUserMedia:(WebRTCMediaStreamConstraints *)constraints
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {

    // カメラとマイクを起動する
    // libwebrtc でカメラを起動すると自動的にマイクも起動される
    // そのため、音声のみ必要な場合でもカメラを起動する必要がある
    if (constraints.video) {
        AVCaptureDevicePosition *position;
        if ([constraints.video.facingMode isEqualToString: WebRTCFacingModeUser])
            position = AVCaptureDevicePositionFront;
        else
            position = AVCaptureDevicePositionBack;

        AVCaptureDevice *device = [WebRTCCameraVideoCapturer captureDeviceForPosition: position];
        if (!device) {
            reject(@"NotFoundError", @"video capturer is not found", nil);
            return;
        }
        
        AVCaptureDeviceFormat *format =
        [WebRTCCameraVideoCapturer suitableFormatForDevice: device
                                                     width: constraints.video.width
                                                    height: constraints.video.height];
        if (!format) {
            reject(@"NotFoundError", @"video capturer format is not found", nil);
            return;
        }
        
        int frameRate = [WebRTCCameraVideoCapturer
                         suitableFrameRateForFormat: format
                         frameRate: constraints.video.frameRate];
        [WebRTCCamera startCaptureWithDevice: device
                                      format: format
                                   frameRate: frameRate];
    } else {
        // 映像が不要の場合でも、マイクを起動するためにカメラを起動しておく
        // その場合は後々ストリームから映像トラックを外す
        [WebRTCCamera startCaptureWithAllDevices];
    }
    
    // カメラ用のトラックを持つストリームを生成する
    // このストリームを管理する必要はなく、
    // ストリーム ID のみ getUserMedia に渡せればよい
    RTCMediaStream *mediaStream =
    [self.peerConnectionFactory
     mediaStreamWithStreamId: [self createNewValueTag]];

    // 映像と音声のトラックをストリームに追加する
    RTCVideoSource *videoSource = [self.peerConnectionFactory videoSource];
    RTCVideoTrack *videoTrack =
    [self.peerConnectionFactory
     videoTrackWithSource: videoSource
     trackId: [self createNewValueTag]];
    RTCAudioSource *audioSource = [self.peerConnectionFactory
                                   audioSourceWithConstraints: nil];
    RTCAudioTrack *audioTrack = [self.peerConnectionFactory
                                 audioTrackWithSource: audioSource
                                 trackId: [self createNewValueTag]];
    videoTrack.valueTag = [self createNewValueTag];
    audioTrack.valueTag = [self createNewValueTag];
    self.tracks[videoTrack.valueTag] = videoTrack;
    self.tracks[audioTrack.valueTag] = audioTrack;
    [mediaStream addVideoTrack: videoTrack];
    [mediaStream addAudioTrack: audioTrack];
    [[WebRTCCameraVideoCapturer shared] addTrackValueTag: videoTrack.valueTag];
    
    // constraints の指定に従ってトラックの可否を決める
    videoTrack.isEnabled = constraints.video ? YES : NO;
    audioTrack.isEnabled = constraints.audio ? YES : NO;

    // アスペクト比の設定
    videoTrack.aspectRatio = constraints.video.aspectRatio;
    
    // JS に処理を戻す
    resolve(@{@"streamId": mediaStream.streamId,
              @"tracks": @[[videoTrack json],
                           [audioTrack json]]});
}

RCT_EXPORT_METHOD(stopUserMedia) {
    [WebRTCCamera stopCapture];
}

@end

NS_ASSUME_NONNULL_END
