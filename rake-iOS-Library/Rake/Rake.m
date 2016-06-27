#if ! __has_feature(objc_arc)
#error This file must be compiled with ARC. Either turn on ARC for the project or use -fobjc-arc flag on this file.
#endif

#include <arpa/inet.h>
#include <net/if.h>
#include <net/if_dl.h>
#include <sys/socket.h>
#include <sys/sysctl.h>

#import <CommonCrypto/CommonDigest.h>
#import <CoreTelephony/CTCarrier.h>
#import <CoreTelephony/CTTelephonyNetworkInfo.h>
#import <SystemConfiguration/SystemConfiguration.h>

#import <Rake.h>
#import <Base64.h>
#import <RakeExceptionHandler.h>
#import <RakeConfig.h>
#import <RakeClientMetricSentinelShuttle.h>

#ifdef USE_PLCRASHREPORTER
#import <AppCrashLoggerSentinelShuttle.h>
#import <RakeCrashReporter.h>
#endif

#ifdef RAKE_LOG
#define RakeLog(...) NSLog(__VA_ARGS__)
#else
#define RakeLog(...)
#endif

#ifdef RAKE_DEBUG
#define RakeDebug(...) NSLog(__VA_ARGS__)
#else
#define RakeDebug(...)
#endif

#define USE_NO_IFA


#define VERSION RAKE_LIB_VERSION
#define MAX_TRACK_COUNT 500

@interface Rake () <UIAlertViewDelegate> {
    NSUInteger _flushInterval;
}

// re-declare internally as readwrite

@property (atomic, copy) NSString *distinctId;

@property (nonatomic, copy) NSString *apiToken;
@property (atomic, strong) NSDictionary *superProperties;
@property (nonatomic, strong) NSMutableDictionary *automaticProperties; // mutable because we update $wifi when reachability changes
@property (nonatomic, strong) NSTimer *timer;
@property (nonatomic, strong) NSMutableArray *eventsQueue;
@property (nonatomic, strong) NSMutableArray *metricsQueue;
@property (nonatomic, assign) UIBackgroundTaskIdentifier taskId;
@property (nonatomic, strong) dispatch_queue_t serialQueue;
@property (nonatomic, assign) SCNetworkReachabilityRef reachability;
@property (nonatomic, strong) CTTelephonyNetworkInfo *telephonyInfo;
@property (nonatomic, strong) NSDateFormatter *localDateFormatter;
@property (nonatomic, strong) NSDateFormatter *baseDateFormatter;
@property (nonatomic) BOOL isDevServer;
@property (nonatomic, strong) NSDate *appStartDate;


#ifdef USE_PLCRASHREPORTER
@property (nonatomic, assign) RakeCrashReporter *crashReporter;
#endif
@end


//static NSString *RKURLEncode(NSString *s)
//{
//    return (NSString *)CFBridgingRelease(CFURLCreateStringByAddingPercentEscapes(kCFAllocatorDefault, (CFStringRef)s, NULL, CFSTR("!*'();:@&=+$,/?%#[]"), kCFStringEncodingUTF8));
//}


@implementation Rake

static void RakeReachabilityCallback(SCNetworkReachabilityRef target, SCNetworkReachabilityFlags flags, void *info)
{
    if (info != NULL && [(__bridge NSObject*)info isKindOfClass:[Rake class]]) {
        @autoreleasepool {
            Rake *rake = (__bridge Rake *)info;
            [rake reachabilityChanged:flags];
        }
    } else {
        NSLog(@"Rake reachability callback received unexpected info object");
    }
}

static Rake *sharedInstance = nil;
static NSArray* defaultValueBlackList = nil;

//+ (Rake *)sharedInstanceWithToken:(NSString *)apiToken
//{
//    static dispatch_once_t onceToken;
//    dispatch_once(&onceToken, ^{
//        sharedInstance = [[super alloc] initWithToken:apiToken andFlushInterval:60];
//    });
//    return sharedInstance;
//}

+ (Rake *)sharedInstanceWithToken:(NSString *)apiToken andUseDevServer:(BOOL)isDevServer
{
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedInstance = [[super alloc] initWithToken:apiToken andFlushInterval:60];
        sharedInstance.isDevServer = isDevServer;
        if(isDevServer){
            [sharedInstance setServerURL:DEV_SERVER_URL];
            sharedInstance.flushInterval = 10;
        } else {
            [sharedInstance setServerURL:LIVE_SERVER_URL];
        }
        defaultValueBlackList = @[];
    });
    RakeLog(@"shared Instance created");
    return sharedInstance;
}

+ (Rake *)sharedInstance
{
    NSAssert(sharedInstance != nil, @"Warning sharedInstance called before sharedInstanceWithToken:");
    if (sharedInstance == nil) {
        NSLog(@"%@ warning sharedInstance called before sharedInstanceWithToken:", self);
    }
    return sharedInstance;
}

- (instancetype)initWithToken:(NSString *)apiToken andFlushInterval:(NSUInteger)flushInterval
{
    if (apiToken == nil) {
        apiToken = @"";
    }
    if ([apiToken length] == 0) {
        NSLog(@"%@ warning empty api token", self);
    }
    if (self = [self init]) {
        // Install uncaught exception handlers first
        [[RakeExceptionHandler sharedHandler] addRakeInstance:self];
        self.appStartDate = [NSDate date];
        
        self.apiToken = apiToken;
        _flushInterval = flushInterval;
        self.flushOnBackground = YES;
        //        self.showNetworkActivityIndicator = YES;
        self.serverURL = LIVE_SERVER_URL;

        self.distinctId = [self defaultDistinctId];
        self.superProperties = [NSMutableDictionary dictionary];
        self.automaticProperties = [self collectAutomaticProperties];
        self.eventsQueue = [NSMutableArray array];
        self.metricsQueue = [NSMutableArray array];
        
        self.taskId = UIBackgroundTaskInvalid;
        NSString *label = [NSString stringWithFormat:@"com.rake.%@.%p", apiToken, self];
        self.serialQueue = dispatch_queue_create([label UTF8String], DISPATCH_QUEUE_SERIAL);

        self.localDateFormatter = [[NSDateFormatter alloc] init]; //Device Time
        self.baseDateFormatter = [[NSDateFormatter alloc] init]; //server Location time
        [_localDateFormatter setDateFormat:@"yyyyMMddHHmmssSSS"];
        [_baseDateFormatter setLocale:[[NSLocale alloc] initWithLocaleIdentifier:@"en_US_POSIX"]];
        [_baseDateFormatter setDateFormat:@"yyyyMMddHHmmssSSS"];
        [_localDateFormatter setLocale:[[NSLocale alloc] initWithLocaleIdentifier:@"en_US_POSIX"]];
        [_baseDateFormatter setTimeZone:[NSTimeZone timeZoneWithName:@"Asia/Seoul"]];


        // wifi reachability
        BOOL reachabilityOk = NO;
        if ((_reachability = SCNetworkReachabilityCreateWithName(NULL, "api.rake.com")) != NULL) {
            SCNetworkReachabilityContext context = {0, (__bridge void*)self, NULL, NULL, NULL};
            if (SCNetworkReachabilitySetCallback(_reachability, RakeReachabilityCallback, &context)) {
                if (SCNetworkReachabilitySetDispatchQueue(_reachability, self.serialQueue)) {
                    reachabilityOk = YES;
                    RakeDebug(@"%@ successfully set up reachability callback", self);
                } else {
                    // cleanup callback if setting dispatch queue failed
                    SCNetworkReachabilitySetCallback(_reachability, NULL, NULL);
                }
            }
        }
        if (!reachabilityOk) {
            NSLog(@"%@ failed to set up reachability callback: %s", self, SCErrorString(SCError()));
        }

        NSNotificationCenter *notificationCenter = [NSNotificationCenter defaultCenter];

        // cellular info
#if __IPHONE_OS_VERSION_MAX_ALLOWED >= 70000
        //        if (floor(NSFoundationVersionNumber) > NSFoundationVersionNumber_iOS_6_1) {
        //            self.telephonyInfo = [[CTTelephonyNetworkInfo alloc] init];
        //            _automaticProperties[@"$radio"] = [self currentRadio];
        //            [notificationCenter addObserver:self
        //                                   selector:@selector(setCurrentRadio)
        //                                       name:CTRadioAccessTechnologyDidChangeNotification
        //                                     object:nil];
        //        }
#endif

        [notificationCenter addObserver:self
                               selector:@selector(applicationWillTerminate:)
                                   name:UIApplicationWillTerminateNotification
                                 object:nil];
        [notificationCenter addObserver:self
                               selector:@selector(applicationWillResignActive:)
                                   name:UIApplicationWillResignActiveNotification
                                 object:nil];
        [notificationCenter addObserver:self
                               selector:@selector(applicationDidBecomeActive:)
                                   name:UIApplicationDidBecomeActiveNotification
                                 object:nil];
        [notificationCenter addObserver:self
                               selector:@selector(applicationDidEnterBackground:)
                                   name:UIApplicationDidEnterBackgroundNotification
                                 object:nil];
        [notificationCenter addObserver:self
                               selector:@selector(applicationWillEnterForeground:)
                                   name:UIApplicationWillEnterForegroundNotification
                                 object:nil];
        [self unarchiveAndFlush];
    }

    return self;
}
- (instancetype)initWithToken:(NSString *)apiToken andUseDevServer:(BOOL)isDevServer {
    Rake *ret = [self initWithToken:apiToken andFlushInterval:isDevServer];
    if(isDevServer){
        [ret setServerURL:DEV_SERVER_URL];
        ret.flushInterval = 5;
    } else {
        [ret setServerURL:LIVE_SERVER_URL];
    }
    return ret;
}
- (void)dealloc
{
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    if (_reachability != NULL) {
        if (!SCNetworkReachabilitySetCallback(_reachability, NULL, NULL)) {
            NSLog(@"%@ error unsetting reachability callback", self);
        }
        if (!SCNetworkReachabilitySetDispatchQueue(_reachability, NULL)) {
            NSLog(@"%@ error unsetting reachability dispatch queue", self);
        }
        CFRelease(_reachability);
        _reachability = NULL;
        RakeDebug(@"released reachability");
    }
    
}

- (NSString *)description
{
    return [NSString stringWithFormat:@"<Rake: %p %@>", self, self.apiToken];
}

- (NSString *)deviceModel
{
    size_t size;
    sysctlbyname("hw.machine", NULL, &size, NULL, 0);
    char answer[size];
    sysctlbyname("hw.machine", answer, &size, NULL, 0);
    NSString *results = @(answer);
    return results;
}


- (NSString *)IFA
{
    NSString *ifa = nil;

#ifndef USE_NO_IFA
    Class ASIdentifierManagerClass = NSClassFromString(@"ASIdentifierManager");
    if (ASIdentifierManagerClass) {
        SEL sharedManagerSelector = NSSelectorFromString(@"sharedManager");
        id sharedManager = ((id (*)(id, SEL))[ASIdentifierManagerClass methodForSelector:sharedManagerSelector])(ASIdentifierManagerClass, sharedManagerSelector);
        SEL advertisingIdentifierSelector = NSSelectorFromString(@"advertisingIdentifier");
        NSUUID *uuid = ((NSUUID* (*)(id, SEL))[sharedManager methodForSelector:advertisingIdentifierSelector])(sharedManager, advertisingIdentifierSelector);
        ifa = [uuid UUIDString];
    }
#endif

    return ifa;
}


- (NSString*)IDFV
{
    NSString *idfv = [[[UIDevice currentDevice] identifierForVendor] UUIDString];

    return idfv;
}

#if __IPHONE_OS_VERSION_MAX_ALLOWED >= 70000
//- (void)setCurrentRadio
//{
//    dispatch_async(self.serialQueue, ^(){
//        _automaticProperties[@"$radio"] = [self currentRadio];
//    });
//}
//
//- (NSString *)currentRadio
//{
//    NSString *radio = _telephonyInfo.currentRadioAccessTechnology;
//    if (!radio) {
//        radio = @"None";
//    } else if ([radio hasPrefix:@"CTRadioAccessTechnology"]) {
//        radio = [radio substringFromIndex:23];
//    }
//    return radio;
//}
#endif

- (NSString *)libVersion
{
    return [Rake libVersion];
}

+ (NSString *)libVersion
{
    return VERSION;
}


- (NSMutableDictionary *)collectAutomaticProperties
{
    NSMutableDictionary *p = [NSMutableDictionary dictionary];
    UIDevice *device = [UIDevice currentDevice];
    NSString *deviceModel = [self deviceModel];
    [p setValue:@"iphone" forKey:@"rake_lib"];
    [p setValue:RAKE_LIB_VERSION forKey:@"rake_lib_version"];
    [p setValue:[[NSBundle mainBundle] infoDictionary][@"CFBundleVersion"] forKey:@"app_version"];
    [p setValue:[[NSBundle mainBundle] infoDictionary][@"CFBundleShortVersionString"] forKey:@"app_release"];
    [p setValue:@"Apple" forKey:@"manufacturer"];
    [p setValue:[device systemName] forKey:@"os_name"];
    [p setValue:[device systemVersion] forKey:@"os_version"];
    [p setValue:deviceModel forKey:@"device_model"]; // legacy


    CGSize size = [UIScreen mainScreen].bounds.size;
    [p setValue:@((NSInteger)size.height) forKey:@"screen_height"];
    [p setValue:@((NSInteger)size.width) forKey:@"screen_width"];
    [p setValue:[NSString stringWithFormat:@"%d*%d",(int)size.width, (int)size.height] forKey:@"resolution"];

    [p setValue:[[NSLocale preferredLanguages] objectAtIndex:0] forKey:@"language_code"];


    CTTelephonyNetworkInfo *networkInfo = [[CTTelephonyNetworkInfo alloc] init];
    CTCarrier *carrier = [networkInfo subscriberCellularProvider];
    if (carrier.carrierName.length) {
        [p setValue:carrier.carrierName forKey:@"carrier_name"];
    } else {
        [p setValue:@"UNKNOWN" forKey:@"carrier_name"];
    }


//    [p setValue:[Rake wifiAvailable]?@"WIFI" : @"NOT WIFI" forKey:@"network_type"];


    [p setValue:[self IDFV] forKey:@"device_id"];

    return p;
}

+ (BOOL)inBackground
{
    return [UIApplication sharedApplication].applicationState == UIApplicationStateBackground;
}

#pragma mark - Encoding/decoding utilities

- (NSData *)JSONSerializeObject:(id)obj
{
    id coercedObj = [self JSONSerializableObjectForObject:obj];
    NSError *error = nil;
    NSData *data = nil;
    @try {
        data = [NSJSONSerialization dataWithJSONObject:coercedObj options:(NSJSONWritingOptions)0 error:&error];
    }
    @catch (NSException *exception) {
        NSLog(@"%@ exception encoding api data: %@", self, exception);
    }
    if (error) {
        NSLog(@"%@ error encoding api data: %@", self, error);
    }
    return data;
}

- (id)JSONSerializableObjectForObject:(id)obj
{
    // valid json types
    if ([obj isKindOfClass:[NSString class]] ||
        [obj isKindOfClass:[NSNumber class]] ||
        [obj isKindOfClass:[NSNull class]]) {
        return obj;
    }
    // recurse on containers
    if ([obj isKindOfClass:[NSArray class]]) {
        NSMutableArray *a = [NSMutableArray array];
        for (id i in obj) {
            [a addObject:[self JSONSerializableObjectForObject:i]];
        }
        return [NSArray arrayWithArray:a];
    }
    if ([obj isKindOfClass:[NSDictionary class]]) {
        NSMutableDictionary *d = [NSMutableDictionary dictionary];
        for (id key in obj) {
            NSString *stringKey;
            if (![key isKindOfClass:[NSString class]]) {
                stringKey = [key description];
                NSLog(@"%@ warning: property keys should be strings. got: %@. coercing to: %@", self, [key class], stringKey);
            } else {
                stringKey = [NSString stringWithString:key];
            }
            id v = [self JSONSerializableObjectForObject:obj[key]];
            d[stringKey] = v;
        }
        return [NSDictionary dictionaryWithDictionary:d];
    }

    // some common cases
    if ([obj isKindOfClass:[NSDate class]]) {
        return [self.localDateFormatter stringFromDate:obj];
    } else if ([obj isKindOfClass:[NSURL class]]) {
        return [obj absoluteString];
    }
    // default to sending the object's description
    NSString *s = [obj description];
    NSLog(@"%@ warning: property values should be valid json types. got: %@. coercing to: %@", self, [obj class], s);
    return s;
}

- (NSString *)encodeAPIData:(NSArray *)array
{
    NSString *b64String = @"";
    NSData *data = [self JSONSerializeObject:array];
    if (data) {
        b64String = [Base64 rk_base64EncodedString:data];
        b64String = (id)CFBridgingRelease(CFURLCreateStringByAddingPercentEscapes(kCFAllocatorDefault,
                                                                                  (__bridge CFStringRef)b64String,
                                                                                  NULL,
                                                                                  CFSTR("!*'();:@&=+$,/?%#[]"),
                                                                                  kCFStringEncodingUTF8));
    }
    return b64String;
}

#pragma mark - Tracking

+ (void)assertPropertyTypes:(NSDictionary *)properties
{
    for (id __unused k in properties) {
        NSAssert([k isKindOfClass: [NSString class]], @"%@ property keys must be NSString. got: %@ %@", self, [k class], k);
        // would be convenient to do: id v = [properties objectForKey:k]; but
        // when the NSAssert's are stripped out in release, it becomes an
        // unused variable error. also, note that @YES and @NO pass as
        // instances of NSNumber class.
        NSAssert([properties[k] isKindOfClass:[NSString class]] ||
                 [properties[k] isKindOfClass:[NSNumber class]] ||
                 [properties[k] isKindOfClass:[NSNull class]] ||
                 [properties[k] isKindOfClass:[NSArray class]] ||
                 [properties[k] isKindOfClass:[NSDictionary class]] ||
                 [properties[k] isKindOfClass:[NSDate class]] ||
                 [properties[k] isKindOfClass:[NSURL class]],
                 @"%@ property values must be NSString, NSNumber, NSNull, NSArray, NSDictionary, NSDate or NSURL. got: %@ %@", self, [properties[k] class], properties[k]);
    }
}

- (NSString *)defaultDistinctId
{
    NSString *distinctId = [self IFA];

    if (!distinctId && NSClassFromString(@"UIDevice")) {
        distinctId = [[UIDevice currentDevice].identifierForVendor UUIDString];
    }
    if (!distinctId) {
        NSLog(@"%@ error getting device identifier: falling back to uuid", self);
        distinctId = [[NSUUID UUID] UUIDString];
    }
    if (!distinctId) {
        NSLog(@"%@ error getting uuid: no default distinct id could be generated", self);
    }
    return distinctId;
}


- (void)createAlias:(NSString *)alias forDistinctID:(NSString *)distinctID
{
    if (!alias || [alias length] == 0) {
        NSLog(@"%@ create alias called with empty alias: %@", self, alias);
        return;
    }
    if (!distinctID || [distinctID length] == 0) {
        NSLog(@"%@ create alias called with empty distinct id: %@", self, distinctID);
        return;
    }
    [self track:@{@"distinct_id": distinctID, @"alias": alias}];
}
- (void)trackMetric:(RakeClientMetricSentinelShuttle *)trackMetric {

    //user_defined_header
    NSString *transactionID = [NSString stringWithFormat:@"%@_%F",self.distinctId,[[NSDate date] timeIntervalSince1970]];
    [trackMetric transaction_id:transactionID];
    NSString *appName = [[NSBundle mainBundle] bundleIdentifier];
    [trackMetric app_package:appName];
    [trackMetric service_token:self.apiToken];
    //Create Metric
    [trackMetric max_track_count:@(MAX_TRACK_COUNT)];
    [trackMetric auto_flush_interval:@(self.flushInterval)];
    [trackMetric auto_flush_onoff:@"enable"];
    [trackMetric flush_method:@"HttpClient"];
    [trackMetric flush_method:@"TIMER"];
    [trackMetric flush_type:@"AUTO_FLUSH_BY_TIMER"];
    [trackMetric rake_protocol_version:@"V1"];
 
    NSString *apiToken = METRIC_TOKEN_LIVE;
    if(self.isDevServer) {
        apiToken = METRIC_TOKEN_DEV;
        [trackMetric env:@"DEV"];
    } else {
        [trackMetric env:@"LIVE"];
    }
    
    
    [self track:[trackMetric toNSDictionary] ApiToken:apiToken Queue:_metricsQueue];
    
}
#ifdef USE_PLCRASHREPORTER
- (void)trackCrashLog:(AppCrashLoggerSentinelShuttle *)crashLog {
    
    NSString *apiToken = CRASHLOGGER_TOKEN_LIVE;
    if(self.isDevServer) apiToken = CRASHLOGGER_TOKEN_DEV;
    
    [crashLog app_key:self.crashLoggerAppKey];
    NSString *transactionID = [NSString stringWithFormat:@"%@_%F",self.distinctId,[[NSDate date] timeIntervalSince1970]];
    [crashLog transaction_id:transactionID];
    [self track:[crashLog toNSDictionary] ApiToken:apiToken Queue:_eventsQueue];
    
}
#endif
- (void)track:(NSDictionary *)properties  {
    @try {
        [self track:properties ApiToken:self.apiToken Queue:_eventsQueue];
    }
    @catch (NSException *exception) {
        RakeClientMetricSentinelShuttle *trackMetric = [[RakeClientMetricSentinelShuttle alloc] init];
        [trackMetric exception_type:exception.name];
        [trackMetric thread_info:exception.reason];
        [trackMetric status:@"ERROR"];
        [trackMetric action:@"track"];
        NSData *callStacks = [self JSONSerializableObjectForObject:exception.callStackSymbols];
        NSString *strCallStacks = [[NSString alloc] initWithData:callStacks encoding:NSUTF8StringEncoding];
        [trackMetric stacktrace:strCallStacks];
        [self trackMetric:trackMetric];
    }
}
- (void)track:(NSDictionary *)properties ApiToken:(NSString *)apiToken Queue:(NSMutableArray *)queue
{

    properties = [properties copy];
    [Rake assertPropertyTypes:properties];

    NSDate* now = [NSDate date];

    dispatch_async(self.serialQueue, ^{
        NSMutableDictionary *p = [NSMutableDictionary dictionary];

        // 1. super properties
        [p addEntriesFromDictionary:self.superProperties];

        // 3-1. sentinel(schema) meta data
//        NSString* schemaId;
//        NSDictionary* fieldOrder;
//        NSArray* encryptionFields;
        NSDictionary* sentinelMeta;
        BOOL isPropertiesFromSentinel = NO;


        // if properties has schemaId
        if(properties[@"sentinel_meta"] != nil){
            // move schemaId, fieldOrder, encryptionField out of p
//            schemaId = properties[@"sentinel_meta"][@"_$schemaId"];
//            fieldOrder = properties[@"sentinel_meta"][@"_$fieldOrder"];
//            encryptionFields = properties[@"sentinel_meta"][@"_$encryptionFields"];
            // iterate
            sentinelMeta = [properties objectForKey:@"sentinel_meta"];
            isPropertiesFromSentinel = YES;
        }

        NSDictionary *fieldOrder = nil;
        if(properties[@"_$fieldOrder"]!=nil){
            fieldOrder = [properties objectForKey:@"_$fieldOrder"];
        }

        // 2. custom properties
        if (properties) {
            NSString* key;
            NSEnumerator* propertiesEnumerator = [properties keyEnumerator];
            while ( (key = [propertiesEnumerator nextObject]) != nil ) {
                if([key isEqualToString:@"sentinel_meta"]){
                    continue;
                }

                if(fieldOrder != nil){
                    // shuttle
                    if(fieldOrder[key] != nil && [properties valueForKey:key] !=nil){
                        // do not overwrite super properties with empty string
                        [p setObject:properties[key] forKey:key];
                    }
                } else {
                    // no shuttle
                    [p setObject:properties[key] forKey:key];
                }
            }
        }

        // 3-2. auto : device info
        // get only values in fieldOrder
        NSString* key;
        NSEnumerator* enumerator = [self.automaticProperties keyEnumerator];

        while ( (key = [enumerator nextObject]) != nil ) {
            BOOL addToProperties = YES;

            if(fieldOrder != nil){
                if(fieldOrder[key] != nil){
                    addToProperties = YES;
                } else {
                    addToProperties = NO;
                }
            }else if([defaultValueBlackList containsObject:key]){
                addToProperties = NO;
            }

            if(addToProperties){
                [p setObject:self.automaticProperties[key] forKey:key];
            }
        }

        // rake token
        p[@"token"] = apiToken;
        p[@"local_time"] = [_localDateFormatter stringFromDate:now];
        p[@"base_time"] = [_baseDateFormatter stringFromDate:now];
        
        // 4. add properties
        NSMutableDictionary *e = [[NSMutableDictionary alloc]init];
        if(isPropertiesFromSentinel){
//            e = @{@"properties": [NSDictionary dictionaryWithDictionary:p],
//                  @"_$schemaId": schemaId,
//                  @"_$fieldOrder": fieldOrder,
//                  @"_$encryptionFields": encryptionFields
//        };
            [e addEntriesFromDictionary:sentinelMeta];
            [e setObject:p forKey:@"properties"];
        } else {
//            e = @{@"properties": [NSDictionary dictionaryWithDictionary:p]};
            [e setObject:p forKey:@"properties"];
        }

        RakeLog(@"%@ queueing event: %@", self, e);

        [queue addObject:e];
        if ([queue count] > MAX_TRACK_COUNT) {
            [queue removeObjectAtIndex:0];
        }
        if ([Rake inBackground]) {
            [self archiveEvents];
        }


    });
}

- (void)registerSuperProperties:(NSDictionary *)properties
{
    properties = [properties copy];
    [Rake assertPropertyTypes:properties];
    dispatch_async(self.serialQueue, ^{
        NSMutableDictionary *tmp = [NSMutableDictionary dictionaryWithDictionary:self.superProperties];
        [tmp addEntriesFromDictionary:properties];
        self.superProperties = [NSDictionary dictionaryWithDictionary:tmp];
        if ([Rake inBackground]) {
            [self archiveProperties];
        }
    });
}

- (void)registerSuperPropertiesOnce:(NSDictionary *)properties
{
    properties = [properties copy];
    [Rake assertPropertyTypes:properties];
    dispatch_async(self.serialQueue, ^{
        NSMutableDictionary *tmp = [NSMutableDictionary dictionaryWithDictionary:self.superProperties];
        for (NSString *key in properties) {
            if (tmp[key] == nil) {
                tmp[key] = properties[key];
            }
        }
        self.superProperties = [NSDictionary dictionaryWithDictionary:tmp];
        if ([Rake inBackground]) {
            [self archiveProperties];
        }
    });
}

- (void)registerSuperPropertiesOnce:(NSDictionary *)properties defaultValue:(id)defaultValue
{
    properties = [properties copy];
    [Rake assertPropertyTypes:properties];
    dispatch_async(self.serialQueue, ^{
        NSMutableDictionary *tmp = [NSMutableDictionary dictionaryWithDictionary:self.superProperties];
        for (NSString *key in properties) {
            id value = tmp[key];
            if (value == nil || [value isEqual:defaultValue]) {
                tmp[key] = properties[key];
            }
        }
        self.superProperties = [NSDictionary dictionaryWithDictionary:tmp];
        if ([Rake inBackground]) {
            [self archiveProperties];
        }
    });
}

- (void)unregisterSuperProperty:(NSString *)propertyName
{
    dispatch_async(self.serialQueue, ^{
        NSMutableDictionary *tmp = [NSMutableDictionary dictionaryWithDictionary:self.superProperties];
        if (tmp[propertyName] != nil) {
            [tmp removeObjectForKey:propertyName];
        }
        self.superProperties = [NSDictionary dictionaryWithDictionary:tmp];
        if ([Rake inBackground]) {
            [self archiveProperties];
        }
    });
}

- (void)clearSuperProperties
{
    dispatch_async(self.serialQueue, ^{
        self.superProperties = @{};
        if ([Rake inBackground]) {
            [self archiveProperties];
        }
    });
}

- (NSDictionary *)currentSuperProperties
{
    return [self.superProperties copy];
}

- (void)reset
{
    dispatch_async(self.serialQueue, ^{
        self.distinctId = [self defaultDistinctId];
        self.nameTag = nil;
        self.superProperties = [NSMutableDictionary dictionary];
        self.eventsQueue = [NSMutableArray array];
        self.metricsQueue = [NSMutableArray array];
        [self archive];
    });
}

#pragma mark - Network control

- (NSUInteger)flushInterval
{
    @synchronized(self) {
        return _flushInterval;
    }
}

- (void)setFlushInterval:(NSUInteger)interval
{
    @synchronized(self) {
        _flushInterval = interval;
    }
    [self flush];
    [self startFlushTimer];
}

- (void)startFlushTimer
{
    [self stopFlushTimer];
    dispatch_async(dispatch_get_main_queue(), ^{
        if (self.flushInterval > 0) {
            self.timer = [NSTimer scheduledTimerWithTimeInterval:self.flushInterval
                                                          target:self
                                                        selector:@selector(flush)
                                                        userInfo:nil
                                                         repeats:YES];
            RakeDebug(@"%@ started flush timer: %@", self, self.timer);
        }
    });
}

- (void)stopFlushTimer
{
    dispatch_async(dispatch_get_main_queue(), ^{
        if (self.timer) {
            RakeDebug(@"%@ stopped flush timer: %@", self, self.timer);
            [self.timer invalidate];
        }
        self.timer = nil;
    });
}

- (void)flush
{
    dispatch_async(self.serialQueue, ^{
        RakeDebug(@"%@ flush starting", self);

        __strong id<RakeDelegate> strongDelegate = _delegate;
        if (strongDelegate != nil && [strongDelegate respondsToSelector:@selector(RakeWillFlush:)] && ![strongDelegate RakeWillFlush:self]) {
            RakeDebug(@"%@ flush deferred by delegate", self);
            return;
        }
        @try {
            [self flushEvents];
            [self flushMetrics];
        }
        @catch (NSException *exception) {
            RakeClientMetricSentinelShuttle *trackMetric = [[RakeClientMetricSentinelShuttle alloc] init];
            [trackMetric exception_type:exception.name];
            [trackMetric thread_info:exception.reason];
            [trackMetric status:@"ERROR"];
            [trackMetric action:@"flush"];
            NSLog(@"callstack:%@",exception.callStackSymbols);
            NSData *callStacks = [self JSONSerializeObject:exception.callStackSymbols];
            NSString *strCallStacks = [[NSString alloc] initWithData:callStacks encoding:NSUTF8StringEncoding];
            [trackMetric stacktrace:strCallStacks];
            [self trackMetric:trackMetric];
            NSLog(@"%@ exception",exception);
        }
        RakeDebug(@"%@ flush complete", self);
    });
}

- (void)flushEvents
{
    [self flushQueue:_eventsQueue endpoint:@"/track/" maxBatchSize:50];
}
- (void)flushMetrics
{
    [self flushQueue:_metricsQueue endpoint:@"/track/" maxBatchSize:5];
}

- (void)flushQueue:(NSMutableArray *)queue endpoint:(NSString *)endpoint maxBatchSize:(NSUInteger)maxBatchSize
{
    while ([queue count] > 0) {

        
        //Flush 했다고 TrackMetric 전송하기 위한 메트릭
        RakeClientMetricSentinelShuttle *trackMetric = [[RakeClientMetricSentinelShuttle alloc] init];
        [trackMetric endpoint:endpoint];
        [trackMetric action:@"flush"];
        
        NSUInteger batchSize = ([queue count] > maxBatchSize) ? maxBatchSize : [queue count];
        NSArray *batch = [queue subarrayWithRange:NSMakeRange(0, batchSize)];

        NSString *requestData = [self encodeAPIData:batch];
        NSString *postBody = [NSString stringWithFormat:@"compress=plain&data=%@", requestData];
        RakeDebug(@"%@ flushing %lu of %lu to %@: %@", self, (unsigned long)[batch count], (unsigned long)[queue count], endpoint, queue);
        NSURLRequest *request = [self apiRequestWithEndpoint:endpoint andBody:postBody];
        NSError *error = nil;

        [self updateNetworkActivityIndicator:YES];
        NSHTTPURLResponse *response = nil;

        CFTimeInterval reqStartTime = CACurrentMediaTime();
        NSData *responseData = [NSURLConnection sendSynchronousRequest:request returningResponse:&response error:&error];
        CFTimeInterval reqEndTime = CACurrentMediaTime();
        RakeDebug(@"Total Request Runtime: %g s", reqEndTime - reqStartTime);

        [self updateNetworkActivityIndicator:NO];

        //metric
        NSUInteger respTimeUInt = (reqEndTime - reqStartTime) * 1000;
        [trackMetric server_response_time:@(respTimeUInt)]; // sec? ms?
        NSUInteger log_size = [requestData lengthOfBytesUsingEncoding:NSUTF8StringEncoding];
        [trackMetric log_size:@(log_size)];
        [trackMetric log_count:@(batchSize)];

        
        // if network is off, or timeout occurred
        if (error) {
            [trackMetric status:@"RETRY"];
            [self trackMetric:trackMetric];
            NSLog(@"%@ network failure: %@", self, error);
            break;
        }

        // statusCode might be 0 if response == nil
        NSInteger statusCode = [response statusCode];
        [trackMetric server_response_code:@(statusCode)];
        [trackMetric status:@"ERROR"];
        if (statusCode == 500) {
            NSLog(@"%@ internal server error: %ld", self, (long)statusCode);
            [trackMetric status:@"RETRY"];
            [self trackMetric:trackMetric];
            break;
        } else if(statusCode == 400) {
            NSLog(@"%@ server return BAD Request: %ld", self, (long)statusCode);
        }

        NSLog(@"response code: %ld", (long)statusCode);
        NSString *responseBody = [[NSString alloc] initWithData:responseData encoding:NSUTF8StringEncoding];
        NSLog(@"response body %d", [responseBody intValue]);

        if ([responseBody intValue] == -1) {
            NSLog(@"%@ %@ api rejected some items", self, endpoint);
            [trackMetric status:@"DROP"];
        } else if ([responseBody intValue] == 1) {
            NSLog(@"%@ %@ api accepted items", self, endpoint);
            [trackMetric status:@"DONE"];
        } else {
            NSLog(@"%@ %@ api no response Body", self, endpoint);
            [trackMetric status:@"ERROR"];
        }
        
        [queue removeObjectsInArray:batch];

//        NSLog(@"status::",[trackMetric valueForKey:@"_status" ]);
        if(queue == _eventsQueue) {
            if(![[trackMetric valueForKey:@"_status" ] isEqualToString:@"DONE"]) {
                [trackMetric server_response_body:responseBody];
                [self trackMetric:trackMetric];
            }
            RakeDebug(@"_eventsQueue sent");
        } else {
            RakeDebug(@"_metricsQueue sent");
        }
        
    }
}

- (void)updateNetworkActivityIndicator:(BOOL)on
{
    if (_showNetworkActivityIndicator) {
        [UIApplication sharedApplication].networkActivityIndicatorVisible = on;
    }
}

- (void)reachabilityChanged:(SCNetworkReachabilityFlags)flags
{
    // this should be run in the serial queue. the reason we don't dispatch_async here
    // is because it's only ever called by the reachability callback, which is already
    // set to run on the serial queue. see SCNetworkReachabilitySetDispatchQueue in init
    NSMutableDictionary *properties = [self.automaticProperties mutableCopy];
    if (properties) {
        BOOL wifi = (flags & kSCNetworkReachabilityFlagsReachable) && !(flags & kSCNetworkReachabilityFlagsIsWWAN);
        properties[@"network_type"] = wifi ? @"WIFI" : @"NOT WIFI";
        self.automaticProperties = [properties copy];
        RakeDebug(@"%@ reachability changed, wifi=%d", self, wifi);
    }
}

//+ (BOOL)wifiAvailable
//{
//    struct sockaddr_in sockAddr;
//    bzero(&sockAddr, sizeof(sockAddr));
//    sockAddr.sin_len = sizeof(sockAddr);
//    sockAddr.sin_family = AF_INET;
//
//    SCNetworkReachabilityRef nrRef = SCNetworkReachabilityCreateWithAddress(NULL, (struct sockaddr *)&sockAddr);
//    SCNetworkReachabilityFlags flags;
//    BOOL didRetrieveFlags = SCNetworkReachabilityGetFlags(nrRef, &flags);
//    if (!didRetrieveFlags) {
//        RakeDebug(@"%@ unable to fetch the network reachablity flags", self);
//    }
//
//    CFRelease(nrRef);
//
//    if (!didRetrieveFlags || (flags & kSCNetworkReachabilityFlagsReachable) != kSCNetworkReachabilityFlagsReachable) {
//        // unable to connect to a network (no signal or airplane mode activated)
//        return NO;
//    }
//
//    if ((flags & kSCNetworkReachabilityFlagsIsWWAN) == kSCNetworkReachabilityFlagsIsWWAN) {
//        // only a cellular network connection is available.
//        return NO;
//    }
//
//    return YES;
//}


- (NSURLRequest *)apiRequestWithEndpoint:(NSString *)endpoint andBody:(NSString *)body
{
    NSURL *URL = [NSURL URLWithString:[self.serverURL stringByAppendingString:endpoint]];
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:URL];
    [request setValue:@"gzip" forHTTPHeaderField:@"Accept-Encoding"];
//    [request setValue:@"gzip" forHTTPHeaderField:@"Content-Encoding"];

    [request setHTTPMethod:@"POST"];
    [request setHTTPBody:[body dataUsingEncoding:NSUTF8StringEncoding]];
    RakeDebug(@"%@ http request: %@?%@", self, URL, body);
    return request;
}

#pragma mark - Persistence

- (NSString *)filePathForData:(NSString *)data
{
    NSString *filename = [NSString stringWithFormat:@"Rake-%@-%@.plist", self.apiToken, data];
    return [[NSSearchPathForDirectoriesInDomains(NSLibraryDirectory, NSUserDomainMask, YES) lastObject]
            stringByAppendingPathComponent:filename];
}

- (NSString *)eventsFilePath
{
    return [self filePathForData:@"events"];
}

- (NSString *)metricFilePath
{
    return [self filePathForData:@"metrics"];
}

- (NSString *)propertiesFilePath
{
    return [self filePathForData:@"properties"];
}

- (void)archive
{
    [self archiveEvents];
    [self archiveMetrics];
    [self archiveProperties];
    RakeDebug(@"all archived!!!");
}

- (void)archiveEvents
{
    NSString *filePath = [self eventsFilePath];
    NSMutableArray *eventsQueueCopy = [NSMutableArray arrayWithArray:[self.eventsQueue copy]];
    RakeDebug(@"%@ archiving events data to %@: %@", self, filePath, eventsQueueCopy);
    if (![NSKeyedArchiver archiveRootObject:eventsQueueCopy toFile:filePath]) {
        NSLog(@"%@ unable to archive events data", self);
    }
}

- (void)archiveMetrics
{
    NSString *filePath = [self metricFilePath];
    NSMutableArray *metricsQueueCopy = [NSMutableArray arrayWithArray:[self.metricsQueue copy]];
    RakeDebug(@"%@ archiving metrics data to %@: %@", self, filePath, metricsQueueCopy);
    if (![NSKeyedArchiver archiveRootObject:metricsQueueCopy toFile:filePath]) {
        NSLog(@"%@ unable to archive events data", self);
    }
}

- (void)archiveProperties
{
    NSString *filePath = [self propertiesFilePath];
    NSMutableDictionary *p = [NSMutableDictionary dictionary];
    [p setValue:self.distinctId forKey:@"distinctId"];
    [p setValue:self.nameTag forKey:@"nameTag"];
    [p setValue:self.superProperties forKey:@"superProperties"];

    RakeDebug(@"%@ archiving properties data to %@: %@", self, filePath, p);
    if (![NSKeyedArchiver archiveRootObject:p toFile:filePath]) {
        NSLog(@"%@ unable to archive properties data", self);
    }
}

- (void)unarchiveAndFlush {
    // unarchive events from disk
    [self unarchive];
    NSUInteger numOfEvents = [self.eventsQueue count];
    
    // flush iff count > 0
    if (numOfEvents > 0) [self flush];
}

- (void)unarchive
{
    [self unarchiveEvents];
    [self unarchiveMetrics];
    [self unarchiveProperties];
}

- (void)unarchiveMetrics
{
    NSString *filePath = [self metricFilePath];
    @try {
        self.metricsQueue = [NSKeyedUnarchiver unarchiveObjectWithFile:filePath];
        RakeDebug(@"%@ unarchived events data: %@", self, self.eventsQueue);
    }
    @catch (NSException *exception) {
        NSLog(@"%@ unable to unarchive events data, starting fresh", self);
        self.metricsQueue = nil;
    }
    if ([[NSFileManager defaultManager] fileExistsAtPath:filePath]) {
        NSError *error;
        BOOL removed = [[NSFileManager defaultManager] removeItemAtPath:filePath error:&error];
        if (!removed) {
            NSLog(@"%@ unable to remove archived events file at %@ - %@", self, filePath, error);
        }
    }
    if (!self.metricsQueue) {
        self.metricsQueue = [NSMutableArray array];
    }
}


- (void)unarchiveEvents
{
    NSString *filePath = [self eventsFilePath];
    @try {
        self.eventsQueue = [NSKeyedUnarchiver unarchiveObjectWithFile:filePath];
        RakeDebug(@"%@ unarchived events data: %@", self, self.eventsQueue);
    }
    @catch (NSException *exception) {
        NSLog(@"%@ unable to unarchive events data, starting fresh", self);
        self.eventsQueue = nil;
    }
    if ([[NSFileManager defaultManager] fileExistsAtPath:filePath]) {
        NSError *error;
        BOOL removed = [[NSFileManager defaultManager] removeItemAtPath:filePath error:&error];
        if (!removed) {
            NSLog(@"%@ unable to remove archived events file at %@ - %@", self, filePath, error);
        }
    }
    if (!self.eventsQueue) {
        self.eventsQueue = [NSMutableArray array];
    }
}


- (void)unarchiveProperties
{
    NSString *filePath = [self propertiesFilePath];
    NSDictionary *properties = nil;
    @try {
        properties = [NSKeyedUnarchiver unarchiveObjectWithFile:filePath];
        RakeDebug(@"%@ unarchived properties data: %@", self, properties);
    }
    @catch (NSException *exception) {
        NSLog(@"%@ unable to unarchive properties data, starting fresh", self);
    }
    if ([[NSFileManager defaultManager] fileExistsAtPath:filePath]) {
        NSError *error;
        BOOL removed = [[NSFileManager defaultManager] removeItemAtPath:filePath error:&error];
        if (!removed) {
            NSLog(@"%@ unable to remove archived properties file at %@ - %@", self, filePath, error);
        }
    }
    if (properties) {
        self.distinctId = properties[@"distinctId"] ? properties[@"distinctId"] : [self defaultDistinctId];
        self.nameTag = properties[@"nameTag"];
        self.superProperties = properties[@"superProperties"] ? properties[@"superProperties"] : [NSMutableDictionary dictionary];
    }
}
#pragma mark - Property
#ifdef USE_PLCRASHREPORTER
- (void)setCrashLoggerAppKey:(NSString *)crashLoggerAppKey {
    _crashLoggerAppKey = crashLoggerAppKey;
    if(_crashLoggerAppKey) {
        _crashReporter =[RakeCrashReporter sharedInstance];
        [_crashReporter startCrashReport];
        if(_crashReporter.crashLog) {
            [self trackCrashLog:_crashReporter.crashLog];
            _crashReporter.crashLog = nil;
        }
        
    } else {
        _crashReporter = nil;
    }
}
#endif

#pragma mark - UIApplication notifications

- (void)applicationDidBecomeActive:(NSNotification *)notification
{
    RakeDebug(@"%@ application did become active", self);
    [self startFlushTimer];
}

- (void)applicationWillResignActive:(NSNotification *)notification
{
    RakeDebug(@"%@ application will resign active", self);
    [self stopFlushTimer];
}

- (void)applicationDidEnterBackground:(NSNotificationCenter *)notification
{
    RakeDebug(@"%@ did enter background", self);

    __block UIBackgroundTaskIdentifier backgroundTask = [[UIApplication sharedApplication] beginBackgroundTaskWithExpirationHandler:^{
        RakeDebug(@"%@ flush %lu cut short", self, (unsigned long) backgroundTask);
        [[UIApplication sharedApplication] endBackgroundTask:backgroundTask];
        self.taskId = UIBackgroundTaskInvalid;
    }];
    self.taskId = backgroundTask;
    RakeDebug(@"%@ starting background cleanup task %lu", self, (unsigned long)self.taskId);
    
    if (self.flushOnBackground) {
        [self flush];
    }
    
    dispatch_async(_serialQueue, ^{
        [self archive];
        RakeDebug(@"%@ ending background cleanup task %lu", self, (unsigned long)self.taskId);
        if (self.taskId != UIBackgroundTaskInvalid) {
            [[UIApplication sharedApplication] endBackgroundTask:self.taskId];
            self.taskId = UIBackgroundTaskInvalid;
        }
    });
}

- (void)applicationWillEnterForeground:(NSNotificationCenter *)notification
{
    RakeDebug(@"%@ will enter foreground", self);
    dispatch_async(self.serialQueue, ^{
        if (self.taskId != UIBackgroundTaskInvalid) {
            [[UIApplication sharedApplication] endBackgroundTask:self.taskId];
            self.taskId = UIBackgroundTaskInvalid;
            [self updateNetworkActivityIndicator:NO];
        }
    });
//    [self unarchiveAndFlush];
}

- (void)applicationWillTerminate:(NSNotification *)notification
{
    RakeDebug(@"%@ will applicationWillTerminate", self);
    [self archive];
}

@end
