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
        self.localStreams = [NSMutableDictionary dictionary];
        self.tracks = [NSMutableDictionary dictionary];
        self.senders = [NSMutableDictionary dictionary];
        self.receivers = [NSMutableDictionary dictionary];
        self.transceivers = [NSMutableDictionary dictionary];

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
    NSString *valueTag;
    // Make sure ID does not exist across local and remote streams (for any peerConnection)
    do {
        valueTag = [[NSUUID UUID] UUIDString];
    } while ([self streamForValueTag: valueTag]);
    return valueTag;
}

- (nullable RTCMediaStream *)streamForValueTag:(NSString *)valueTag {
    __block RTCMediaStream *stream = self.localStreams[valueTag];
    if (!stream) {
        [self.peerConnections enumerateKeysAndObjectsUsingBlock:^(NSString * _Nonnull key, RTCPeerConnection * _Nonnull obj, BOOL * _Nonnull stop) {
            stream = obj.remoteStreams[valueTag];
            if (stream) {
                *stop = YES;
            }
        }];
    }
    return stream;
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
    [_localStreams removeAllObjects];
    [_senders removeAllObjects];
    [_receivers removeAllObjects];
    [_transceivers removeAllObjects];
}

@end

NS_ASSUME_NONNULL_END
