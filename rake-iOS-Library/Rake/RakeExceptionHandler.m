//
//  RakeExceptionHandler.m
//  rake-iOS-Library
//  
//  Created by 1002125 on 2016. 1. 22..
//
//

#import "Rake.h"
#import "RakeExceptionHandler.h"
#import <CrashReporter/CrashReporter.h>
#import "RakeConfig.h"


#if !defined(USE_PLCRASHREPORTER)
#include <libkern/OSAtomic.h>
#include <execinfo.h>
#endif

@interface RakeExceptionHandler()

#if !defined(USE_PLCRASHREPORTER)
@property (nonatomic) NSUncaughtExceptionHandler *defaultExceptionHandler;
#endif
@property (nonatomic, strong) NSHashTable *rakeInstances;

@end


@implementation RakeExceptionHandler
#if !defined(USE_PLCRASHREPORTER)
static uint32_t volatile isAlreadyExceptionOccured =0;
static int fatal_signals[] =
{
    SIGILL  ,   /* illegal instruction (not reset when caught) */
    SIGTRAP ,   /* trace trap (not reset when caught) */
    SIGABRT ,   /* abort() */
    SIGFPE  ,   /* floating point exception */
    SIGBUS  ,   /* bus error */
    SIGSEGV ,   /* segmentation violation */
    SIGSYS  ,   /* bad argument to system call */
};
static int n_fatal_signals = (sizeof(fatal_signals) / sizeof(fatal_signals[0]));
#endif

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
#if !defined(USE_PLCRASHREPORTER)
        [self setupCrashHandler];
#else
        [self setupPLCrasReporterHandler];
#endif
    }
    return self;
}

- (void)addRakeInstance:(Rake *)instance {
    NSParameterAssert(instance != nil);
    [self.rakeInstances addObject:instance];
}
#if !defined(USE_PLCRASHREPORTER)
- (void)setupCrashHandler
{
    // Save the existing exception handler
    _defaultExceptionHandler = NSGetUncaughtExceptionHandler();
    NSSetUncaughtExceptionHandler(&rk_handleUncaughtException);
    
    registerFatalSignals();
}
#endif
- (void)setupPLCrasReporterHandler {
    PLCrashReporterCallbacks cb = {
        .version = 0,
        .context = nil,
        .handleSignal = rk_PLCrashReporterReporterCallback
    };
    
    [[PLCrashReporter sharedReporter] setCrashCallbacks:&cb];
}

static void rk_PLCrashReporterReporterCallback (siginfo_t *info, ucontext_t *uap, void *context) {
    // this is not async-safe, but this is a test implementation
    RakeExceptionHandler *handler = [RakeExceptionHandler sharedHandler];
    for (Rake *instance in handler.rakeInstances) {
        if (instance) {
            [instance archive];
        }
    }
    NSLog(@"Encountered crash All Rake instances were archived - signo=%d, uap=%p, context=%p", info->si_signo, uap, context);

}
#if !defined(USE_PLCRASHREPORTER)
void rk_handleSignal(int sig, siginfo_t *info, void *context) {
    unregisterFatalSignals();
    NSLog(@"We received a signal: %d", sig);
    
    //Save somewhere that your app has crashed.
    NSException* exception = [NSException
                              exceptionWithName:@"UncaughtException"
                              reason:
                              [NSString stringWithFormat:
                               NSLocalizedString(@"Signal %d was raised.", nil),
                               sig]
                              userInfo:
                              [NSDictionary
                               dictionaryWithObject:[NSNumber numberWithInt:sig]
                               forKey:@"UncaughtExceptionSignalKey"]];
    
    rk_handleUncaughtException(exception);
}


static void rk_handleUncaughtException(NSException *exception) {
//    NSLog(@"??????");
    RakeExceptionHandler *handler = [RakeExceptionHandler sharedHandler];


    if(isAlreadyExceptionOccured==0) {
        OSAtomicOr32Barrier(1, &isAlreadyExceptionOccured);
        // Archive the values for each Rake instance
        for (Rake *instance in handler.rakeInstances) {
            // Since we're storing the instances in a weak table, we need to ensure the pointer hasn't become nil
            if (instance) {
                [instance archive];
            }
        }
        NSLog(@"Encountered an uncaught exception. All Rake instances were archived.");
    }
    
    if (handler.defaultExceptionHandler) {
        // Ensure the existing handler gets called once we're finished
        handler.defaultExceptionHandler(exception);
    }
}


static void registerFatalSignals() {
    struct sigaction sa;
    /* Configure action */
    memset(&sa, 0, sizeof(sa));
    sa.sa_flags =  SA_SIGINFO | SA_ONSTACK;
    sa.sa_sigaction = &rk_handleSignal;
    sigemptyset(&sa.sa_mask);
    /* Set new sigaction */
    for (int i =0 ;i<n_fatal_signals; i++) {
        if (sigaction(fatal_signals[i], &sa, NULL) != 0) {
//            int err = errno;
//            NSAssert(0,"Signal registration for %s failed: %s", strsignal(fatal_signals[i]), strerror(err));
        }
    }
    
}
static void unregisterFatalSignals() {
    struct sigaction sa;
    memset(&sa, 0, sizeof(sa));
    sa.sa_handler = SIG_DFL;
    sigemptyset(&sa.sa_mask);
    for (int i = 0; i < n_fatal_signals; i++) {
        sigaction(fatal_signals[i], &sa, NULL);
    }
}
#endif
@end
