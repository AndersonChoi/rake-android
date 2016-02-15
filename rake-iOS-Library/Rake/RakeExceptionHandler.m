//
//  RakeExceptionHandler.m
//  rake-iOS-Library
//  
//  Created by 1002125 on 2016. 1. 22..
//
//

#import "Rake.h"
#import "RakeExceptionHandler.h"

@interface RakeExceptionHandler()

@property (nonatomic) NSUncaughtExceptionHandler *defaultExceptionHandler;
@property (nonatomic, strong) NSHashTable *rakeInstances;

@end

@implementation RakeExceptionHandler

+ (instancetype)sharedHandler {

    static RakeExceptionHandler *gSharedHandler = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        gSharedHandler = [[RakeExceptionHandler alloc] init];
    });
    return gSharedHandler;
}

- (instancetype)init {
    self = [super init];
    if (self) {
        // Create a hash table of weak pointers to Rake instances
        _rakeInstances = [NSHashTable weakObjectsHashTable];
        
        // Save the existing exception handler
        _defaultExceptionHandler = NSGetUncaughtExceptionHandler();
        // Install our handler
        NSSetUncaughtExceptionHandler(&rk_handleUncaughtException);
    }
    return self;
}

- (void)addRakeInstance:(Rake *)instance {
    NSParameterAssert(instance != nil);
    
    [self.rakeInstances addObject:instance];
}

static void rk_handleUncaughtException(NSException *exception) {
    RakeExceptionHandler *handler = [RakeExceptionHandler sharedHandler];
    
    // Archive the values for each Rake instance
    for (Rake *instance in handler.rakeInstances) {
        // Since we're storing the instances in a weak table, we need to ensure the pointer hasn't become nil
        if (instance) {
            [instance archive];
        }
    }
    
    NSLog(@"Encountered an uncaught exception. All Rake instances were archived.");
    
    if (handler.defaultExceptionHandler) {
        // Ensure the existing handler gets called once we're finished
        handler.defaultExceptionHandler(exception);
    }
}
@end
