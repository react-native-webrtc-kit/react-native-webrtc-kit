#import "WebRTCModule+getUserMedia.h"
#import "WebRTCModule+RTCMediaStream.h"
#import "WebRTCMediaStreamConstraints.h"

NS_ASSUME_NONNULL_BEGIN

static WebRTCCameraVideoCapturer *sharedCameraVideoCapturer = nil;

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
        _nativeCapturer = [[RTCCameraVideoCapturer alloc] initWithDelegate: self];
        _isRunning = NO;
        _streamValueTags = [[NSMutableArray alloc] init];
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
    // ただし、すべてのタグを一度に消すために
    // 毎回チェック用の配列を用意すると重いので、一度に一つずつ消す
    NSString *tagToRemove = nil;
    for (NSString *valueTag in _streamValueTags) {
        RTCMediaStream *stream = [WebRTCModule shared].localStreams[valueTag];
        if (!stream) {
            tagToRemove = valueTag;
        } else if (stream.videoTracks.count > 0) {
            RTCVideoTrack *track = stream.videoTracks[0];
            if (track) {
                [track.source capturer: capturer didCaptureVideoFrame: frame];
            }
        }
    }
    
    if (tagToRemove) {
        dispatch_sync(dispatch_get_main_queue(), ^() {
            NSMutableArray *newTags = [[NSMutableArray alloc] initWithArray: _streamValueTags];
            [newTags removeObject: tagToRemove];
            _streamValueTags = newTags;
        });
    }
}

- (void)removeStreamForValueTag:(NSString *)valueTag
{
    [_streamValueTags removeObjectIdenticalTo: valueTag];
}

- (void)reloadApplication
{
    [self stopCapture];
    [_streamValueTags removeAllObjects];
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
    
    // ストリームを生成して、カメラの映像の出力先に指定する
    NSString *streamValueTag = [self createNewValueTag];
    RTCMediaStream *mediaStream = [self.peerConnectionFactory mediaStreamWithStreamId: streamValueTag];
    mediaStream.valueTag = streamValueTag;
    [WebRTCCamera.streamValueTags addObject: streamValueTag];
    
    // JS からアクセスできるようにするため、
    // ストリームに対するモジュール ID をグローバルに保持する
    self.localStreams[streamValueTag] = mediaStream;

    // 映像と音声のトラックをストリームに追加する
    RTCVideoSource *videoSource = [self.peerConnectionFactory videoSource];
    RTCVideoTrack *videoTrack =
    [self.peerConnectionFactory videoTrackWithSource: videoSource
                                             trackId: [self createNewValueTag]];
    RTCAudioSource *audioSource = [self.peerConnectionFactory audioSourceWithConstraints: nil];
    RTCAudioTrack *audioTrack = [self.peerConnectionFactory audioTrackWithSource: audioSource trackId: [self createNewValueTag]];
    videoTrack.valueTag = [self createNewValueTag];
    audioTrack.valueTag = [self createNewValueTag];
    [mediaStream addVideoTrack: videoTrack];
    [mediaStream addAudioTrack: audioTrack];
    
    // constraints の指定に従ってトラックの可否を決める
    videoTrack.isEnabled = constraints.video ? YES : NO;
    audioTrack.isEnabled = constraints.audio ? YES : NO;

    // アスペクト比の設定
    videoTrack.aspectRatio = constraints.video.aspectRatio;
    
    // トラックの情報を集める
    NSMutableArray *tracks = [NSMutableArray array];
    for (NSString *propertyName in @[ @"audioTracks", @"videoTracks" ]) {
        SEL sel = NSSelectorFromString(propertyName);
        for (RTCMediaStreamTrack *track in [mediaStream performSelector:sel]) {
            self.localTracks[track.valueTag] = track;
            [tracks addObject: [track json]];
        }
    }
    
    // JS に処理を戻す
    resolve(@{@"streamId": mediaStream.streamId,
              @"valueTag": streamValueTag,
              @"tracks": tracks});
}

RCT_EXPORT_METHOD(stopUserMedia) {
    [WebRTCCamera stopCapture];
}

@end

NS_ASSUME_NONNULL_END
