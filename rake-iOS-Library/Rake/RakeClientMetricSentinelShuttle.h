/*
 * Sentinel-Shuttle for iOS
 *
 * by Data Infrastructure
 * template version 0.1
 */

#import <Foundation/Foundation.h>

@interface RakeClientMetricSentinelShuttle : NSObject

- (id)init;

// all methods for field setting
- (RakeClientMetricSentinelShuttle*) base_time:(NSString *) base_time;

- (RakeClientMetricSentinelShuttle*) local_time:(NSString *) local_time;

- (RakeClientMetricSentinelShuttle*) recv_time:(NSString *) recv_time;

- (RakeClientMetricSentinelShuttle*) device_id:(NSString *) device_id;

- (RakeClientMetricSentinelShuttle*) device_model:(NSString *) device_model;

- (RakeClientMetricSentinelShuttle*) manufacturer:(NSString *) manufacturer;

- (RakeClientMetricSentinelShuttle*) os_name:(NSString *) os_name;

- (RakeClientMetricSentinelShuttle*) os_version:(NSString *) os_version;

- (RakeClientMetricSentinelShuttle*) resolution:(NSString *) resolution;

- (RakeClientMetricSentinelShuttle*) screen_width:(NSNumber *) screen_width;

- (RakeClientMetricSentinelShuttle*) screen_height:(NSNumber *) screen_height;

- (RakeClientMetricSentinelShuttle*) carrier_name:(NSString *) carrier_name;

- (RakeClientMetricSentinelShuttle*) network_type:(NSString *) network_type;

- (RakeClientMetricSentinelShuttle*) language_code:(NSString *) language_code;

- (RakeClientMetricSentinelShuttle*) ip:(NSString *) ip;

- (RakeClientMetricSentinelShuttle*) recv_host:(NSString *) recv_host;

- (RakeClientMetricSentinelShuttle*) app_version:(NSString *) app_version;

- (RakeClientMetricSentinelShuttle*) rake_lib:(NSString *) rake_lib;

- (RakeClientMetricSentinelShuttle*) rake_lib_version:(NSString *) rake_lib_version;

- (RakeClientMetricSentinelShuttle*) token:(NSString *) token;


- (RakeClientMetricSentinelShuttle*) browser_name:(NSString *) browser_name;

- (RakeClientMetricSentinelShuttle*) browser_version:(NSString *) browser_version;

- (RakeClientMetricSentinelShuttle*) referrer:(NSString *) referrer;

- (RakeClientMetricSentinelShuttle*) url:(NSString *) url;

- (RakeClientMetricSentinelShuttle*) document_title:(NSString *) document_title;

- (RakeClientMetricSentinelShuttle*) action:(NSString *) action;

- (RakeClientMetricSentinelShuttle*) status:(NSString *) status;

- (RakeClientMetricSentinelShuttle*) app_package:(NSString *) app_package;

- (RakeClientMetricSentinelShuttle*) transaction_id:(NSString *) transaction_id;

- (RakeClientMetricSentinelShuttle*) service_token:(NSString *) service_token;

- (RakeClientMetricSentinelShuttle*) reserved0:(NSString *) reserved0;

- (RakeClientMetricSentinelShuttle*) reserved1:(NSString *) reserved1;

- (RakeClientMetricSentinelShuttle*) reserved2:(NSString *) reserved2;

- (RakeClientMetricSentinelShuttle*) exception_type:(NSString *) exception_type;

- (RakeClientMetricSentinelShuttle*) stacktrace:(NSString *) stacktrace;

- (RakeClientMetricSentinelShuttle*) thread_info:(NSString *) thread_info;

- (RakeClientMetricSentinelShuttle*) operation_time:(NSNumber *) operation_time;

- (RakeClientMetricSentinelShuttle*) env:(NSString *) env;

- (RakeClientMetricSentinelShuttle*) database_version:(NSNumber *) database_version;

- (RakeClientMetricSentinelShuttle*) persisted_log_count:(NSNumber *) persisted_log_count;

- (RakeClientMetricSentinelShuttle*) expired_log_count:(NSNumber *) expired_log_count;

- (RakeClientMetricSentinelShuttle*) max_track_count:(NSNumber *) max_track_count;

- (RakeClientMetricSentinelShuttle*) logging:(NSString *) logging;

- (RakeClientMetricSentinelShuttle*) endpoint:(NSString *) endpoint;

- (RakeClientMetricSentinelShuttle*) auto_flush_onoff:(NSString *) auto_flush_onoff;

- (RakeClientMetricSentinelShuttle*) auto_flush_interval:(NSNumber *) auto_flush_interval;

- (RakeClientMetricSentinelShuttle*) log_count:(NSNumber *) log_count;

- (RakeClientMetricSentinelShuttle*) log_size:(NSNumber *) log_size;

- (RakeClientMetricSentinelShuttle*) flush_type:(NSString *) flush_type;

- (RakeClientMetricSentinelShuttle*) server_response_time:(NSNumber *) server_response_time;

- (RakeClientMetricSentinelShuttle*) server_response_code:(NSNumber *) server_response_code;

- (RakeClientMetricSentinelShuttle*) server_response_body:(NSString *) server_response_body;

- (RakeClientMetricSentinelShuttle*) flush_method:(NSString *) flush_method;

- (RakeClientMetricSentinelShuttle*) rake_protocol_version:(NSString *) rake_protocol_version;



- (RakeClientMetricSentinelShuttle*) setBodyOf__ERROR_with_exception_type:(NSString *) exception_type
stacktrace:(NSString *) stacktrace
thread_info:(NSString *) thread_info;
- (RakeClientMetricSentinelShuttle*) setBodyOfinstall__ERROR_with_exception_type:(NSString *) exception_type
stacktrace:(NSString *) stacktrace
thread_info:(NSString *) thread_info
endpoint:(NSString *) endpoint
database_version:(NSNumber *) database_version
env:(NSString *) env;
- (RakeClientMetricSentinelShuttle*) setBodyOfinstall__DONE_with_operation_time:(NSNumber *) operation_time
endpoint:(NSString *) endpoint
database_version:(NSNumber *) database_version
persisted_log_count:(NSNumber *) persisted_log_count
expired_log_count:(NSNumber *) expired_log_count
logging:(NSString *) logging
env:(NSString *) env
max_track_count:(NSNumber *) max_track_count
auto_flush_onoff:(NSString *) auto_flush_onoff
auto_flush_interval:(NSNumber *) auto_flush_interval;
- (RakeClientMetricSentinelShuttle*) setBodyOftrack__ERROR_with_exception_type:(NSString *) exception_type
stacktrace:(NSString *) stacktrace
thread_info:(NSString *) thread_info;
- (RakeClientMetricSentinelShuttle*) setBodyOfflush__ERROR_with_exception_type:(NSString *) exception_type
stacktrace:(NSString *) stacktrace
thread_info:(NSString *) thread_info;
- (RakeClientMetricSentinelShuttle*) setBodyOfflush__DONE_with_operation_time:(NSNumber *) operation_time
endpoint:(NSString *) endpoint
log_count:(NSNumber *) log_count
log_size:(NSNumber *) log_size
flush_type:(NSString *) flush_type
server_response_time:(NSNumber *) server_response_time
server_response_code:(NSNumber *) server_response_code
server_response_body:(NSString *) server_response_body
flush_method:(NSString *) flush_method
rake_protocol_version:(NSString *) rake_protocol_version;
- (RakeClientMetricSentinelShuttle*) setBodyOfflush__DROP_with_exception_type:(NSString *) exception_type
stacktrace:(NSString *) stacktrace
thread_info:(NSString *) thread_info
operation_time:(NSNumber *) operation_time
endpoint:(NSString *) endpoint
log_count:(NSNumber *) log_count
log_size:(NSNumber *) log_size
flush_type:(NSString *) flush_type
server_response_time:(NSNumber *) server_response_time
server_response_code:(NSNumber *) server_response_code
server_response_body:(NSString *) server_response_body
flush_method:(NSString *) flush_method
rake_protocol_version:(NSString *) rake_protocol_version;
- (RakeClientMetricSentinelShuttle*) setBodyOfflush__RETRY_with_exception_type:(NSString *) exception_type
stacktrace:(NSString *) stacktrace
thread_info:(NSString *) thread_info
operation_time:(NSNumber *) operation_time
endpoint:(NSString *) endpoint
log_count:(NSNumber *) log_count
log_size:(NSNumber *) log_size
flush_type:(NSString *) flush_type
server_response_time:(NSNumber *) server_response_time
server_response_code:(NSNumber *) server_response_code
server_response_body:(NSString *) server_response_body
flush_method:(NSString *) flush_method
rake_protocol_version:(NSString *) rake_protocol_version;


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
