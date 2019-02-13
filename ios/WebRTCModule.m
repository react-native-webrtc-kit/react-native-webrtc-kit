#import "WebRTCModule.h"
#import "WebRTCModule+RTCPeerConnection.h"
#import "WebRTCModule+getUserMedia.h"

const NSString *WebRTCErrorDomain = @"WebRTCError";

NS_ASSUME_NONNULL_BEGIN

static WebRTCModule *sharedModule;

@interface WebRTCModule ()

// React Native の処理専用のスレッド。
// 専用のスレッドを用意することで、メインスレッドで発生する例外に影響されなくなる。
// 実装は以下のコードを参考にした。
// https://github.com/jitsi/react-native-webrtc/pull/29
@property(nonatomic, strong) dispatch_queue_t workerQueue;

@end

@implementation WebRTCModule

// for RCTBridgeModule
@synthesize bridge = _bridge;

#pragma mark - Init/dealloc

+ (WebRTCModule *)shared
{
    return sharedModule;
}

- (instancetype)init {
    self = [super init];
    if (self) {
        RTCInitializeSSL();
        RTCEnableMetrics();
        sharedModule = self;
        
        RTCDefaultVideoEncoderFactory *encoderFactory =
        [[RTCDefaultVideoEncoderFactory alloc] init];
        RTCDefaultVideoDecoderFactory *decoderFactory =
        [[RTCDefaultVideoDecoderFactory alloc] init];
        self.peerConnectionFactory =
        [[RTCPeerConnectionFactory alloc]
         initWithEncoderFactory: encoderFactory
         decoderFactory: decoderFactory];
        self.peerConnections = [NSMutableDictionary dictionary];
        self.streams = [NSMutableDictionary dictionary];
        self.tracks = [NSMutableDictionary dictionary];
        self.senders = [NSMutableDictionary dictionary];
        self.receivers = [NSMutableDictionary dictionary];
        self.transceivers = [NSMutableDictionary dictionary];
        self.portOverride = AVAudioSessionPortOverrideNone;
        dispatch_queue_attr_t attributes =
        dispatch_queue_attr_make_with_qos_class(DISPATCH_QUEUE_SERIAL,
                                                QOS_CLASS_USER_INITIATED, -1);
        _workerQueue = dispatch_queue_create("WebRTCModule.queue", attributes);
    }
    return self;
}

- (void)dealloc {
    [self.peerConnections enumerateKeysAndObjectsUsingBlock:^(NSString * _Nonnull key, RTCPeerConnection * _Nonnull obj, BOOL * _Nonnull stop) {
        obj.delegate = nil;
        [obj close];
    }];
    [self.peerConnections removeAllObjects];
    RTCShutdownInternalTracer();
    RTCCleanupSSL();
}

#pragma mark - Methods

- (NSString *)createNewValueTag {
    return [[NSUUID UUID] UUIDString];
}

#pragma mark - React Native Exports

RCT_EXPORT_MODULE();

+ (BOOL)requiresMainQueueSetup
{
    return NO;
}

- (dispatch_queue_t)methodQueue
{
    return _workerQueue;
}

/* JS レイヤーがロードまたはリロードされたときに呼ばれる。
 * ここでリロード前の古い RTCPeerConnection の終了処理を行わないと、
 * RTCPeerConnection の接続が残ったままになってしまう。
 */
RCT_EXPORT_METHOD(finishLoading) {
    [WebRTCCamera reloadApplication];
    
    // すべての peer connection を終了する
    for (RTCPeerConnection *conn in _peerConnections) {
        [conn closeAndFinish];
    }
    
    [_peerConnections removeAllObjects];
    [_tracks removeAllObjects];
    [_senders removeAllObjects];
    [_receivers removeAllObjects];
    [_transceivers removeAllObjects];
}

RCT_EXPORT_METHOD(enableMetrics) {
    RTCEnableMetrics();
}

RCT_EXPORT_METHOD(getAndResetMetrics:(nonnull RCTPromiseResolveBlock)resolve
                  rejecter:(nonnull RCTPromiseRejectBlock)reject) {
    NSMutableArray *infos = [[NSMutableArray alloc] init];
    for (RTCMetricsSampleInfo *info in RTCGetAndResetMetrics()) {
        NSMutableDictionary *samples = [[NSMutableDictionary alloc] init];
        for (NSNumber *key in [info.samples keyEnumerator]) {
            samples[[key stringValue]] = info.samples[key];
        }
        [infos addObject:
         @{@"name": info.name,
           @"min": [NSNumber numberWithInt: info.min],
           @"max": [NSNumber numberWithInt: info.max],
           @"bucketCount": [NSNumber numberWithInt: info.bucketCount],
           @"samples": samples}];
    }
    resolve(infos);
}

// MARK: -getAudioPort:resolver:rejecter:
RCT_REMAP_METHOD(getAudioPort, resolver: (RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject){
    if(self.portOverride == AVAudioSessionPortOverrideSpeaker){
        resolve(@"speaker");
    }else if(self.portOverride == AVAudioSessionPortOverrideNone){
        resolve(@"none");
    }else{
        resolve(@"unknown");
    }
}

// MARK: -setAudioPort:resolver:rejecter:
RCT_EXPORT_METHOD(setAudioPort:(NSString *)port
                  resolver: (nonnull RCTPromiseResolveBlock)resolve
                  rejecter:(nonnull RCTPromiseRejectBlock)reject) {
    AVAudioSessionPortOverride override = AVAudioSessionPortOverrideNone;
    if ([port isEqualToString: @"speaker"]) {
        override = AVAudioSessionPortOverrideSpeaker;
    }
    [RTCDispatcher dispatchAsyncOnType:RTCDispatcherTypeAudioSession
                                 block:^{
                                     RTCAudioSession *session = [RTCAudioSession sharedInstance];
                                     [session lockForConfiguration];
                                     NSError *error = nil;
                                     if ([session overrideOutputAudioPort:override error:&error]) {
                                         self.portOverride = override;
                                         resolve(nil);
                                     } else {
                                         RTCLogError(@"Error overriding output port: %@",
                                                     error.localizedDescription);
                                     }
                                     [session unlockForConfiguration];
                                 }];
}


@end

NS_ASSUME_NONNULL_END
