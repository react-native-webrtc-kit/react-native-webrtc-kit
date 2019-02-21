#import <objc/runtime.h>
#import "WebRTCModule.h"
#import "WebRTCValueManager.h"

@implementation WebRTCValueManager

static const char *valueTagKey = "valueTag";
static NSMutableDictionary<NSString *, NSString *> *stringDict;
static dispatch_queue_t lockQueue;

+ (void)initialize
{
    stringDict = [[NSMutableDictionary alloc] init];
    lockQueue = dispatch_queue_create("WebRTCValueManager.lock",
                                      DISPATCH_QUEUE_SERIAL);
}

+ (NSString *)createNewValueTag
{
    return [[NSUUID UUID] UUIDString];
}

+ (nullable NSString *)valueTagForObject:(nonnull id<WebRTCExportable>)object;
{
    return objc_getAssociatedObject(object, valueTagKey);
}

+ (nullable NSString *)valueTagForString:(nonnull NSString *)key
{
    return stringDict[key];
}

+ (void)setValueTag:(nullable NSString *)valueTag
          forObject:(nonnull id<WebRTCExportable>)object
{
    dispatch_sync(lockQueue, ^{
        objc_setAssociatedObject(object, valueTagKey, valueTag,
                                 OBJC_ASSOCIATION_ASSIGN);
    });
}

+ (void)setValueTag:(nullable NSString *)valueTag
          forString:(nonnull NSString *)key
{
    dispatch_sync(lockQueue, ^{
        stringDict[key] = valueTag;
    });
}

+ (void)removeValueTagForObject:(nonnull id<WebRTCExportable>)object
{
    [self setValueTag: nil forObject: object];
    object.valueTag = nil;
}

+ (void)removeValueTagForString:(nonnull NSString *)key
{
    dispatch_sync(lockQueue, ^{
        [stringDict removeObjectForKey: key];
    });
}

@end
