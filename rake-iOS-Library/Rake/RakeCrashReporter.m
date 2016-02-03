//
//  RakeCrashReporter.m
//  rake-iOS-Library
//
//  Created by 1002125 on 2016. 2. 2..
//
//
//https://plcrashreporter.org/
//https://github.com/bitstadium/QuincyKit
#import <CrashReporter/CrashReporter.h>
#import <CrashReporter/PLCrashReportTextFormatter.h>
#import <AppCrashLoggerSentinelShuttle.h>
#import <RakeCrashReporter.h>
#import <RakeCrashLoggerTextFormatter.h>
#import <RakeConfig.h>
@interface RakeCrashReporter()
- (void) _handleCrashReport;
- (void) _makeCrashLog:(PLCrashReport *)report;
@end
@implementation RakeCrashReporter
static RakeCrashReporter *sharedInstance = nil;


+ (RakeCrashReporter *)sharedInstance {
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedInstance = [[super alloc] init];
    });
    return sharedInstance;
}

- (instancetype) init {
    if (self = [super init]) {
        _crashLog = nil;
    }
    return self;
}

- (void) _handleCrashReport {
    PLCrashReporter *crashReporter = [PLCrashReporter sharedReporter];
    NSData *crashData;
    NSError *error;
    
    // Try loading the crash report
    crashData = [crashReporter loadPendingCrashReportDataAndReturnError: &error];
    if (crashData == nil) {
        NSLog(@"Could not load crash report: %@", error);
        [crashReporter purgePendingCrashReport];
        self.crashLog = nil;
        return;
    }
    PLCrashReport *report = [[PLCrashReport alloc] initWithData: crashData error: &error] ;
    if (report == nil) {
        NSLog(@"Could not parse crash report");
        [crashReporter purgePendingCrashReport];
        self.crashLog = nil;
        return;
    }
    [self _makeCrashLog:report];
    [crashReporter purgePendingCrashReport];
//    NSLog(@"Crashed on %@", report.systemInfo.timestamp);
//    NSLog(@"Crashed with signal %@ (code %@, address=0x%" PRIx64 ")", report.signalInfo.name,
//          report.signalInfo.code, report.signalInfo.address);
    
}
- (void) _makeCrashLog:(PLCrashReport *)report {
    self.crashLog = [[AppCrashLoggerSentinelShuttle alloc] init];
    
    NSString* stackTrace = [RakeCrashLoggerTextFormatter stringValueForStackTrace:report];
    NSString* package_name = report.applicationInfo.applicationIdentifier;
    NSString *appVersion = [[[NSBundle mainBundle] infoDictionary] objectForKey:@"CFBundleVersion"];
    NSString *osVersion = report.systemInfo.operatingSystemVersion;
    NSString *deviceModel = report.machineInfo.modelName;
    NSString *exceptionType;
//    NSString *libVersion = RAKE_LIB_VERSION;
    
    if(report.hasExceptionInfo) {
        exceptionType = report.exceptionInfo.exceptionName;
    }else {
        exceptionType = report.signalInfo.name;
    }
    NSString *crashLogString = [RakeCrashLoggerTextFormatter stringValueForCrashReport:report];
    
    [self.crashLog stacktrace:stackTrace];
    [self.crashLog package_name:package_name];
    [self.crashLog app_version:appVersion];
    [self.crashLog os_version:osVersion];
    [self.crashLog device_model:deviceModel];
    [self.crashLog logcat:crashLogString];
    [self.crashLog report_type:@"crashed"];
    [self.crashLog log_id:@"crash"];
    [self.crashLog crash_logger_version:RAKE_LIB_VERSION];
    /*memory,disk*/
    if(exceptionType)
        [self.crashLog exception_type:exceptionType];
    
}
- (void)startCrashReport {
    PLCrashReporter *crashReporter = [PLCrashReporter sharedReporter];
    NSError *error;
    // Check if we previously crashed
    if ([crashReporter hasPendingCrashReport])
        [self _handleCrashReport];
    // Enable the Crash Reporter
    if (![crashReporter enableCrashReporterAndReturnError: &error])
        NSLog(@"Warning: Could not enable crash reporter: %@", error);
    
}
- (AppCrashLoggerSentinelShuttle *)getCrashLog {
    AppCrashLoggerSentinelShuttle *retCrashLog = [_crashLog copy] ;
    _crashLog = nil;
    return retCrashLog;
}
@end
