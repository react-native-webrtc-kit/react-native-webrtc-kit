#import <objc/runtime.h>
#import "WebRTCModule.h"
#import "WebRTCValueManager.h"

@implementation WebRTCValueManager

static const char *valueTagKey = "valueTag";

+ (NSString *)createNewValueTag
{
    return [[NSUUID UUID] UUIDString];
}

+ (nullable NSString *)valueTagForObject:(nonnull id<WebRTCExportable>)object;
{
    return objc_getAssociatedObject(object, valueTagKey);
}

+ (void)setValueTag:(nullable NSString *)valueTag
          forObject:(nonnull id<WebRTCExportable>)object
{
    objc_setAssociatedObject(object, valueTagKey, valueTag,
                             OBJC_ASSOCIATION_ASSIGN);
}

+ (void)addNewObject:(nonnull id<WebRTCExportable>)object
{
    object.valueTag = [self createNewValueTag];
}

+ (void)removeValueTagForObject:(nonnull id<WebRTCExportable>)object
{
    [self setValueTag: nil forObject: object];
    object.valueTag = nil;
}

@end
