//
//  RakeCrashLoggerTextFormatter.h
//  rake-iOS-Library
//
//  Created by 1002125 on 2016. 2. 3..
//
//
//#import <CrashReporter/CrashReporter.h>

@class PLCrashReportTextFormatter;
@class PLCrashReport;
@class PLCrashReportStackFrameInfo;
@interface RakeCrashLoggerTextFormatter : NSObject
+ (NSString *) stringValueForCrashReport: (PLCrashReport *) report;
+ (NSString *)stringValueForStackTrace:(PLCrashReport *)report;
+ (NSString *)formatStackFrameOffset: (PLCrashReportStackFrameInfo *) frameInfo
                          frameIndex: (NSUInteger) frameIndex
                              report: (PLCrashReport *) report;
+ (NSString *)formatStackFrame: (PLCrashReportStackFrameInfo *) frameInfo
                    frameIndex: (NSUInteger) frameIndex
                        report: (PLCrashReport *) report;
@end
