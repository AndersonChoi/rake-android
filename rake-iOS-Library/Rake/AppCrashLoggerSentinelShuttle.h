/*
 * Sentinel-Shuttle for iOS
 *
 * by Data Infrastructure
 * template version 0.1
 */

#import <Foundation/Foundation.h>

@interface AppCrashLoggerSentinelShuttle : NSObject

- (id)init;

// all methods for field setting
- (AppCrashLoggerSentinelShuttle*) base_time:(NSString *) base_time;

- (AppCrashLoggerSentinelShuttle*) local_time:(NSString *) local_time;

- (AppCrashLoggerSentinelShuttle*) recv_time:(NSString *) recv_time;

- (AppCrashLoggerSentinelShuttle*) device_id:(NSString *) device_id;

- (AppCrashLoggerSentinelShuttle*) device_model:(NSString *) device_model;

- (AppCrashLoggerSentinelShuttle*) manufacturer:(NSString *) manufacturer;

- (AppCrashLoggerSentinelShuttle*) os_name:(NSString *) os_name;

- (AppCrashLoggerSentinelShuttle*) os_version:(NSString *) os_version;

- (AppCrashLoggerSentinelShuttle*) resolution:(NSString *) resolution;

- (AppCrashLoggerSentinelShuttle*) screen_width:(NSNumber *) screen_width;

- (AppCrashLoggerSentinelShuttle*) screen_height:(NSNumber *) screen_height;

- (AppCrashLoggerSentinelShuttle*) carrier_name:(NSString *) carrier_name;

- (AppCrashLoggerSentinelShuttle*) network_type:(NSString *) network_type;

- (AppCrashLoggerSentinelShuttle*) language_code:(NSString *) language_code;

- (AppCrashLoggerSentinelShuttle*) recv_host:(NSString *) recv_host;

- (AppCrashLoggerSentinelShuttle*) app_version:(NSString *) app_version;

- (AppCrashLoggerSentinelShuttle*) rake_lib:(NSString *) rake_lib;

- (AppCrashLoggerSentinelShuttle*) rake_lib_version:(NSString *) rake_lib_version;

- (AppCrashLoggerSentinelShuttle*) token:(NSString *) token;


- (AppCrashLoggerSentinelShuttle*) browser_name:(NSString *) browser_name;

- (AppCrashLoggerSentinelShuttle*) browser_version:(NSString *) browser_version;

- (AppCrashLoggerSentinelShuttle*) referrer:(NSString *) referrer;

- (AppCrashLoggerSentinelShuttle*) url:(NSString *) url;

- (AppCrashLoggerSentinelShuttle*) document_title:(NSString *) document_title;

- (AppCrashLoggerSentinelShuttle*) log_id:(NSString *) log_id;

- (AppCrashLoggerSentinelShuttle*) transaction_id:(NSString *) transaction_id;

- (AppCrashLoggerSentinelShuttle*) crash_logger_version:(NSString *) crash_logger_version;

- (AppCrashLoggerSentinelShuttle*) app_key:(NSString *) app_key;

- (AppCrashLoggerSentinelShuttle*) report_type:(NSString *) report_type;

- (AppCrashLoggerSentinelShuttle*) package_name:(NSString *) package_name;

- (AppCrashLoggerSentinelShuttle*) total_disk_size:(NSNumber *) total_disk_size;

- (AppCrashLoggerSentinelShuttle*) available_disk_size:(NSNumber *) available_disk_size;

- (AppCrashLoggerSentinelShuttle*) total_memory_size:(NSNumber *) total_memory_size;

- (AppCrashLoggerSentinelShuttle*) available_memory_size:(NSNumber *) available_memory_size;

- (AppCrashLoggerSentinelShuttle*) logcat:(NSString *) logcat;

- (AppCrashLoggerSentinelShuttle*) user_comment:(NSString *) user_comment;

- (AppCrashLoggerSentinelShuttle*) app_started_at:(NSString *) app_started_at;

- (AppCrashLoggerSentinelShuttle*) exception_created_at:(NSString *) exception_created_at;

- (AppCrashLoggerSentinelShuttle*) exception_type:(NSString *) exception_type;

- (AppCrashLoggerSentinelShuttle*) exception_info:(NSString *) exception_info;

- (AppCrashLoggerSentinelShuttle*) exception_code:(NSNumber *) exception_code;

- (AppCrashLoggerSentinelShuttle*) thread_count:(NSNumber *) thread_count;

- (AppCrashLoggerSentinelShuttle*) thread_info:(NSString *) thread_info;

- (AppCrashLoggerSentinelShuttle*) stacktrace:(NSString *) stacktrace;

- (AppCrashLoggerSentinelShuttle*) footprint_count:(NSNumber *) footprint_count;

- (AppCrashLoggerSentinelShuttle*) footprint:(NSMutableArray *) footprint;

- (AppCrashLoggerSentinelShuttle*) crash_logger_config:(NSString *) crash_logger_config;

- (AppCrashLoggerSentinelShuttle*) cpu_info:(NSString *) cpu_info;

- (AppCrashLoggerSentinelShuttle*) memory_info:(NSString *) memory_info;



- (AppCrashLoggerSentinelShuttle*) setBodyOfcrash_with_report_type:(NSString *) report_type
app_key:(NSString *) app_key
package_name:(NSString *) package_name
total_disk_size:(NSNumber *) total_disk_size
available_disk_size:(NSNumber *) available_disk_size
total_memory_size:(NSNumber *) total_memory_size
available_memory_size:(NSNumber *) available_memory_size
logcat:(NSString *) logcat
user_comment:(NSString *) user_comment
app_started_at:(NSString *) app_started_at
exception_created_at:(NSString *) exception_created_at
exception_type:(NSString *) exception_type
exception_info:(NSString *) exception_info
exception_code:(NSNumber *) exception_code
thread_count:(NSNumber *) thread_count
thread_info:(NSString *) thread_info
stacktrace:(NSString *) stacktrace
crash_logger_config:(NSString *) crash_logger_config
footprint_count:(NSNumber *) footprint_count;
- (AppCrashLoggerSentinelShuttle*) setBodyOfthread_with_thread_count:(NSNumber *) thread_count
thread_info:(NSString *) thread_info
stacktrace:(NSString *) stacktrace;
- (AppCrashLoggerSentinelShuttle*) setBodyOfsystem_with_cpu_info:(NSString *) cpu_info
memory_info:(NSString *) memory_info;
- (AppCrashLoggerSentinelShuttle*) setBodyOffootprint_with_footprint_count:(NSNumber *) footprint_count
footprint:(NSMutableArray *) footprint;


// 10 public util functions
- (NSString*) toString;
- (NSString*) toHBString;
- (NSString*) toHBString:(NSString *)delim;
- (NSString*) headerToString;
- (void) clearBody;
- (NSDictionary*) getBody;
- (NSString*) bodyToString;
+ (NSDictionary*) toNSDictionary:(NSDictionary *)dict;
- (NSDictionary*) toNSDictionary;
- (NSString*) toJSONString;
- (NSString*) getEscapedValue:(NSString *)value;
+ (NSDictionary*) getSentinelMeta;


@end
