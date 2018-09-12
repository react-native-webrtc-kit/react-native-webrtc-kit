@import Foundation;
@import UIKit;

#import <WebRTC/WebRTC.h>

NS_ASSUME_NONNULL_BEGIN

@interface WebRTCVideoView : UIView <RTCVideoRenderer>

@property (nonatomic, nullable) RTCVideoTrack *videoTrack;

@end

NS_ASSUME_NONNULL_END
