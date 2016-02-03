//
//  RakeCrashReporter.h
//  rake-iOS-Library
//
//  Created by 1002125 on 2016. 2. 2..
//
//

#import <Foundation/Foundation.h>
@class CrashReporter;
@class PLCrashReporter;
@class AppCrashLoggerSentinelShuttle;
@interface RakeCrashReporter : NSObject

@property (nonatomic,strong) AppCrashLoggerSentinelShuttle *crashLog;

- (void) startCrashReport;
- (AppCrashLoggerSentinelShuttle *)getCrashLog;

+ (RakeCrashReporter *)sharedInstance;

@end
