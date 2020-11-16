#import "WebRTCModule.h"
#import "WebRTCModule+RTCDataChannel.h"
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

@property (nonatomic, readonly, nonnull) dispatch_queue_t lock;

@property (nonatomic) NSMutableDictionary<NSString *, RTCPeerConnection *> *peerConnectionDict;
@property (nonatomic) NSMutableDictionary<NSString *, RTCMediaStream *> *streamDict;
@property (nonatomic) NSMutableDictionary<NSString *, RTCMediaStreamTrack *> *trackDict;
@property (nonatomic) NSMutableDictionary<NSString*, RTCRtpSender *> *senderDict;
@property (nonatomic) NSMutableDictionary<NSString*, RTCRtpReceiver *> *receiverDict;
@property (nonatomic) NSMutableDictionary<NSString*, RTCRtpTransceiver *> *transceiverDict;
@property (nonatomic) NSMutableDictionary<NSString*, RTCDataChannel *> *dataChannelDict;

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
        self.peerConnectionDict = [NSMutableDictionary dictionary];
        self.streamDict = [NSMutableDictionary dictionary];
        self.trackDict = [NSMutableDictionary dictionary];
        self.senderDict = [NSMutableDictionary dictionary];
        self.receiverDict = [NSMutableDictionary dictionary];
        self.transceiverDict = [NSMutableDictionary dictionary];
        self.dataChannelDict = [NSMutableDictionary dictionary];
        self.portOverride = AVAudioSessionPortOverrideNone;
        self.microphoneEnabled = YES;
        dispatch_queue_attr_t attributes =
        dispatch_queue_attr_make_with_qos_class(DISPATCH_QUEUE_SERIAL,
                                                QOS_CLASS_USER_INITIATED, -1);
        _workerQueue = dispatch_queue_create("WebRTCModule.queue", attributes);
        _lock = dispatch_queue_create("WebRTCModule.lock",
                                      DISPATCH_QUEUE_SERIAL);
    }
    return self;
}

- (void)dealloc {
    [self.peerConnectionDict enumerateKeysAndObjectsUsingBlock:^(NSString * _Nonnull key, RTCPeerConnection * _Nonnull obj, BOOL * _Nonnull stop) {
        obj.delegate = nil;
        [obj close];
    }];
    [self.peerConnectionDict removeAllObjects];
    RTCShutdownInternalTracer();
    RTCCleanupSSL();
}

#pragma mark - Methods

- (NSString *)createNewValueTag {
    return [[NSUUID UUID] UUIDString];
}

- (NSArray <RTCPeerConnection *> *)peerConnections;
{
    return [self.peerConnectionDict allValues];
}

- (nullable RTCPeerConnection *)peerConnectionForKey:(NSString *)key
{
    return self.peerConnectionDict[key];
}

- (void)addPeerConnection:(RTCPeerConnection *)peerConn
                   forKey:(NSString *)key
{
    NSAssert(key != nil, @"key must not be nil");
    
    dispatch_sync(self.lock, ^{
        self.peerConnectionDict[key] = peerConn;
    });
}

- (void)removePeerConnectionForKey:(NSString *)key
{
    NSAssert(key != nil, @"key must not be nil");

    dispatch_sync(self.lock, ^{
        [self.peerConnectionDict removeObjectForKey: key];
    });
}

- (NSArray <RTCMediaStream *> *)streams;
{
    return [self.streamDict allValues];
}

- (nullable RTCMediaStream *)streamForKey:(NSString *)key
{
    return self.streamDict[key];
}

- (void)addStream:(RTCMediaStream *)stream
           forKey:(NSString *)key
{
    NSAssert(key != nil, @"key must not be nil");
    dispatch_sync(self.lock, ^{
        NSLog(@"key = %@", [key description]);
        self.streamDict[key] = stream;
    });
}

- (void)removeStreamForKey:(NSString *)key
{
    NSAssert(key != nil, @"key must not be nil");
    
    dispatch_sync(self.lock, ^{
        NSLog(@"removeStreamForKey = %@", [key description]);
        NSAssert(key != nil, @"key must not be nil");
        [self.streamDict removeObjectForKey: key];
    });
}

- (NSArray <RTCMediaStreamTrack *> *)tracks;
{
    return [self.trackDict allValues];
}

- (nullable RTCMediaStreamTrack *)trackForKey:(NSString *)key
{
    return self.trackDict[key];
}

- (void)addTrack:(RTCMediaStreamTrack *)track
          forKey:(NSString *)key
{
    NSAssert(key != nil, @"key must not be nil");

    dispatch_sync(self.lock, ^{
        self.trackDict[key] = track;
    });
}

- (void)removeTrackForKey:(NSString *)key
{
    NSAssert(key != nil, @"key must not be nil");

    dispatch_sync(self.lock, ^{
        [self.trackDict removeObjectForKey: key];
    });
}

- (NSArray <RTCRtpSender *> *)senders;
{
    return [self.senderDict allValues];
}

- (nullable RTCRtpSender *)senderForKey:(NSString *)key
{
    return self.senderDict[key];
}

- (void)addSender:(RTCRtpSender *)sender
           forKey:(NSString *)key
{
    NSAssert(key != nil, @"key must not be nil");
    
    dispatch_sync(self.lock, ^{
        self.senderDict[key] = sender;
    });
}

- (void)removeSenderForKey:(NSString *)key
{
    NSAssert(key != nil, @"key must not be nil");

    dispatch_sync(self.lock, ^{
        [self.senderDict removeObjectForKey: key];
    });
}

- (NSArray <RTCRtpReceiver *> *)receivers;
{
    return [self.receiverDict allValues];
}

- (nullable RTCRtpReceiver *)receiverForKey:(NSString *)key
{
    return self.receiverDict[key];
}

- (void)addReceiver:(RTCRtpReceiver *)stream
             forKey:(NSString *)key
{
    NSAssert(key != nil, @"key must not be nil");

    dispatch_sync(self.lock, ^{
        self.receiverDict[key] = stream;
    });
}

- (void)removeReceiverForKey:(NSString *)key
{
    NSLog(@"# removeReceiverForKey %@", key);
    NSAssert(key != nil, @"key must not be nil");
    
    dispatch_sync(self.lock, ^{
        [self.receiverDict removeObjectForKey: key];
    });
}

- (NSArray <RTCRtpTransceiver *> *)transceivers;
{
    return [self.transceiverDict allValues];
}

- (nullable RTCRtpTransceiver *)transceiverForKey:(NSString *)key
{
    return self.transceiverDict[key];
}

- (void)addTransceiver:(RTCRtpTransceiver *)transceiver
                forKey:(NSString *)key
{
    NSAssert(key != nil, @"key must not be nil");

    dispatch_sync(self.lock, ^{
        self.transceiverDict[key] = transceiver;
    });
}

- (void)removeTransceiverForKey:(NSString *)key
{
    NSAssert(key != nil, @"key must not be nil");

    dispatch_sync(self.lock, ^{
        [self.transceiverDict removeObjectForKey: key];
    });
}

- (nullable RTCDataChannel *)dataChannelForKey:(NSString *)key
{
    return self.dataChannelDict[key];
}

- (void)addDataChannel:(RTCDataChannel *)channel
           forKey:(NSString *)key
{
    NSAssert(key != nil, @"key must not be nil");
    dispatch_sync(self.lock, ^{
        NSLog(@"key = %@", [key description]);
        self.dataChannelDict[key] = channel;
    });
}

- (void)removeDataChannelForKey:(NSString *)key
{
    NSAssert(key != nil, @"key must not be nil");
    dispatch_sync(self.lock, ^{
        [self.dataChannelDict removeObjectForKey: key];
    });
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
    dispatch_sync(self.lock, ^{
        [WebRTCCamera reloadApplication];
        
        // すべての peer connection を終了する
        for (RTCPeerConnection *conn in self.peerConnections) {
            [conn closeAndFinish];
        }
        
        [self.peerConnectionDict removeAllObjects];
        [self.trackDict removeAllObjects];
        [self.senderDict removeAllObjects];
        [self.receiverDict removeAllObjects];
        [self.transceiverDict removeAllObjects];
        [self.dataChannelDict removeAllObjects];
    });
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
    if (self.portOverride == AVAudioSessionPortOverrideSpeaker) {
        resolve(@"speaker");
    }
    else if (self.portOverride == AVAudioSessionPortOverrideNone) {
        resolve(@"none");
    }
    else {
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
                                     }
                                     else {
                                         RTCLogError(@"Error overriding output port: %@",
                                                     error.localizedDescription);
                                     }
                                     [session unlockForConfiguration];
                                 }];
}

RCT_REMAP_METHOD(setMicrophoneEnabled, setMicrophoneEnabledWithResolver:(BOOL)newValue
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject)
{
    self.microphoneEnabled = newValue;
    resolve([NSNull null]);
}

@end

NS_ASSUME_NONNULL_END
