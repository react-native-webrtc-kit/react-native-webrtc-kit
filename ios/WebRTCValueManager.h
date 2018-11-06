#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@protocol WebRTCExportable <NSObject>

@property (nonatomic, nullable) NSString *valueTag;

@end

@interface WebRTCValueManager : NSObject

+ (NSString *)createNewValueTag;

+ (nullable NSString *)valueTagForObject:(nonnull id<WebRTCExportable>)object;
+ (void)setValueTag:(nullable NSString *)valueTag
          forObject:(nonnull id<WebRTCExportable>)object;
+ (void)addNewObject:(nonnull id<WebRTCExportable>)object;
+ (void)removeValueTagForObject:(nonnull id<WebRTCExportable>)object;

@end

NS_ASSUME_NONNULL_END
