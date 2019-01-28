#import <UIKit/UIKit.h>

@class WebRTCVideoViewController;
@protocol WebRTCVideoViewControllerDelegate <NSObject>

- (void)viewControllerDidFinish:(WebRTCVideoViewController *)viewController;

@end

@interface WebRTCVideoViewController : UIViewController

@property(nonatomic, weak) id<WebRTCVideoViewControllerDelegate> delegate;

@end
