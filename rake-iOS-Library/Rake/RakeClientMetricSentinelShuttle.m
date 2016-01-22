/*
 * Sentinel-Shuttle for iOS
 *
 * by Data Infrastructure
 * Template version
 * - 0.1.1 : first release
 * - 0.1.2 : log_id methods
 * - 0.1.3 : remove _$ssToken
 * - 0.1.4 : toNSDictionary(NSDictionary*)
 *
 * Author
 *  - Sentinel Shuttle Generator v1.5
 *  - junghyun@sk.com (Data Infrastructure)
 */

#import <objc/message.h>
#import <foundation/NSJSONSerialization.h>
#import "RakeClientMetricSentinelShuttle.h"



@interface RakeClientMetricSentinelShuttle()

// private members (meta data)

// private fields
@property(nonatomic) NSString *  base_time;
@property(nonatomic) NSString *  local_time;
@property(nonatomic) NSString *  recv_time;
@property(nonatomic) NSString *  device_id;
@property(nonatomic) NSString *  device_model;
@property(nonatomic) NSString *  manufacturer;
@property(nonatomic) NSString *  os_name;
@property(nonatomic) NSString *  os_version;
@property(nonatomic) NSString *  resolution;
@property(nonatomic) NSNumber *  screen_width;
@property(nonatomic) NSNumber *  screen_height;
@property(nonatomic) NSString *  carrier_name;
@property(nonatomic) NSString *  network_type;
@property(nonatomic) NSString *  language_code;
@property(nonatomic) NSString *  ip;
@property(nonatomic) NSString *  recv_host;
@property(nonatomic) NSString *  app_version;
@property(nonatomic) NSString *  rake_lib;
@property(nonatomic) NSString *  rake_lib_version;
@property(nonatomic) NSString *  token;
@property(nonatomic) NSString *  log_version;
@property(nonatomic) NSString *  browser_name;
@property(nonatomic) NSString *  browser_version;
@property(nonatomic) NSString *  referrer;
@property(nonatomic) NSString *  url;
@property(nonatomic) NSString *  document_title;
@property(nonatomic) NSString *  action;
@property(nonatomic) NSString *  status;
@property(nonatomic) NSString *  app_package;
@property(nonatomic) NSString *  transaction_id;
@property(nonatomic) NSString *  service_token;
@property(nonatomic) NSString *  reserved0;
@property(nonatomic) NSString *  reserved1;
@property(nonatomic) NSString *  reserved2;
@property(nonatomic) NSString *  exception_type;
@property(nonatomic) NSString *  stacktrace;
@property(nonatomic) NSString *  thread_info;
@property(nonatomic) NSNumber *  operation_time;
@property(nonatomic) NSString *  env;
@property(nonatomic) NSNumber *  database_version;
@property(nonatomic) NSNumber *  persisted_log_count;
@property(nonatomic) NSNumber *  expired_log_count;
@property(nonatomic) NSNumber *  max_track_count;
@property(nonatomic) NSString *  logging;
@property(nonatomic) NSString *  endpoint;
@property(nonatomic) NSString *  auto_flush_onoff;
@property(nonatomic) NSNumber *  auto_flush_interval;
@property(nonatomic) NSNumber *  log_count;
@property(nonatomic) NSNumber *  log_size;
@property(nonatomic) NSString *  flush_type;
@property(nonatomic) NSNumber *  server_response_time;
@property(nonatomic) NSNumber *  server_response_code;
@property(nonatomic) NSString *  server_response_body;
@property(nonatomic) NSString *  flush_method;
@property(nonatomic) NSString *  rake_protocol_version;


@end

@implementation RakeClientMetricSentinelShuttle



static NSString* _$ssTemplateVersion = @"0.1.3";
static NSString* _$ssVersion = @"16.01.04:1.5.54:38";
static NSString* _$ssSchemaId = @"568a0a43420000a801178b32";
static NSString*  _$ssDelim = @"\t";
static NSString* _$logVersionKey = @"log_version";

static NSArray* headerFieldNameList;
static NSArray* bodyFieldNameList;
static NSArray* actionKeyNameList;
static NSDictionary* bodyRule;
static NSArray* encryptedFieldsList;

+(void)initialize
{
    headerFieldNameList = @[@"base_time",@"local_time",@"recv_time",@"device_id",@"device_model",@"manufacturer",@"os_name",@"os_version",@"resolution",@"screen_width",@"screen_height",@"carrier_name",@"network_type",@"language_code",@"ip",@"recv_host",@"app_version",@"rake_lib",@"rake_lib_version",@"token",@"log_version",@"browser_name",@"browser_version",@"referrer",@"url",@"document_title",@"action",@"status",@"app_package",@"transaction_id",@"service_token",@"reserved0",@"reserved1",@"reserved2"];
    bodyFieldNameList = @[@"exception_type",@"stacktrace",@"thread_info",@"operation_time",@"env",@"database_version",@"persisted_log_count",@"expired_log_count",@"max_track_count",@"logging",@"endpoint",@"auto_flush_onoff",@"auto_flush_interval",@"log_count",@"log_size",@"flush_type",@"server_response_time",@"server_response_code",@"server_response_body",@"flush_method",@"rake_protocol_version"];
    actionKeyNameList = @[@"action",@"status"];
    encryptedFieldsList = @[@"device_id"];

    bodyRule = [[NSMutableDictionary alloc]init];



}

- (id)init
{
    self = [super init];
    if(self){
        _log_version = _$ssVersion;
    }

    return self;
}


/**
 * Methods
 */
- (RakeClientMetricSentinelShuttle*) base_time:(NSString *) base_time
{
    _base_time = base_time;
    return self;
}


- (RakeClientMetricSentinelShuttle*) local_time:(NSString *) local_time
{
    _local_time = local_time;
    return self;
}


- (RakeClientMetricSentinelShuttle*) recv_time:(NSString *) recv_time
{
    _recv_time = recv_time;
    return self;
}


- (RakeClientMetricSentinelShuttle*) device_id:(NSString *) device_id
{
    _device_id = device_id;
    return self;
}


- (RakeClientMetricSentinelShuttle*) device_model:(NSString *) device_model
{
    _device_model = device_model;
    return self;
}


- (RakeClientMetricSentinelShuttle*) manufacturer:(NSString *) manufacturer
{
    _manufacturer = manufacturer;
    return self;
}


- (RakeClientMetricSentinelShuttle*) os_name:(NSString *) os_name
{
    _os_name = os_name;
    return self;
}


- (RakeClientMetricSentinelShuttle*) os_version:(NSString *) os_version
{
    _os_version = os_version;
    return self;
}


- (RakeClientMetricSentinelShuttle*) resolution:(NSString *) resolution
{
    _resolution = resolution;
    return self;
}


- (RakeClientMetricSentinelShuttle*) screen_width:(NSNumber *) screen_width
{
    _screen_width = screen_width;
    return self;
}


- (RakeClientMetricSentinelShuttle*) screen_height:(NSNumber *) screen_height
{
    _screen_height = screen_height;
    return self;
}


- (RakeClientMetricSentinelShuttle*) carrier_name:(NSString *) carrier_name
{
    _carrier_name = carrier_name;
    return self;
}


- (RakeClientMetricSentinelShuttle*) network_type:(NSString *) network_type
{
    _network_type = network_type;
    return self;
}


- (RakeClientMetricSentinelShuttle*) language_code:(NSString *) language_code
{
    _language_code = language_code;
    return self;
}


- (RakeClientMetricSentinelShuttle*) ip:(NSString *) ip
{
    _ip = ip;
    return self;
}


- (RakeClientMetricSentinelShuttle*) recv_host:(NSString *) recv_host
{
    _recv_host = recv_host;
    return self;
}


- (RakeClientMetricSentinelShuttle*) app_version:(NSString *) app_version
{
    _app_version = app_version;
    return self;
}


- (RakeClientMetricSentinelShuttle*) rake_lib:(NSString *) rake_lib
{
    _rake_lib = rake_lib;
    return self;
}


- (RakeClientMetricSentinelShuttle*) rake_lib_version:(NSString *) rake_lib_version
{
    _rake_lib_version = rake_lib_version;
    return self;
}


- (RakeClientMetricSentinelShuttle*) token:(NSString *) token
{
    _token = token;
    return self;
}



- (RakeClientMetricSentinelShuttle*) browser_name:(NSString *) browser_name
{
    _browser_name = browser_name;
    return self;
}


- (RakeClientMetricSentinelShuttle*) browser_version:(NSString *) browser_version
{
    _browser_version = browser_version;
    return self;
}


- (RakeClientMetricSentinelShuttle*) referrer:(NSString *) referrer
{
    _referrer = referrer;
    return self;
}


- (RakeClientMetricSentinelShuttle*) url:(NSString *) url
{
    _url = url;
    return self;
}


- (RakeClientMetricSentinelShuttle*) document_title:(NSString *) document_title
{
    _document_title = document_title;
    return self;
}


- (RakeClientMetricSentinelShuttle*) action:(NSString *) action
{
    _action = action;
    return self;
}


- (RakeClientMetricSentinelShuttle*) status:(NSString *) status
{
    _status = status;
    return self;
}


- (RakeClientMetricSentinelShuttle*) app_package:(NSString *) app_package
{
    _app_package = app_package;
    return self;
}


- (RakeClientMetricSentinelShuttle*) transaction_id:(NSString *) transaction_id
{
    _transaction_id = transaction_id;
    return self;
}


- (RakeClientMetricSentinelShuttle*) service_token:(NSString *) service_token
{
    _service_token = service_token;
    return self;
}


- (RakeClientMetricSentinelShuttle*) reserved0:(NSString *) reserved0
{
    _reserved0 = reserved0;
    return self;
}


- (RakeClientMetricSentinelShuttle*) reserved1:(NSString *) reserved1
{
    _reserved1 = reserved1;
    return self;
}


- (RakeClientMetricSentinelShuttle*) reserved2:(NSString *) reserved2
{
    _reserved2 = reserved2;
    return self;
}


- (RakeClientMetricSentinelShuttle*) exception_type:(NSString *) exception_type
{
    _exception_type = exception_type;
    return self;
}


- (RakeClientMetricSentinelShuttle*) stacktrace:(NSString *) stacktrace
{
    _stacktrace = stacktrace;
    return self;
}


- (RakeClientMetricSentinelShuttle*) thread_info:(NSString *) thread_info
{
    _thread_info = thread_info;
    return self;
}


- (RakeClientMetricSentinelShuttle*) operation_time:(NSNumber *) operation_time
{
    _operation_time = operation_time;
    return self;
}


- (RakeClientMetricSentinelShuttle*) env:(NSString *) env
{
    _env = env;
    return self;
}


- (RakeClientMetricSentinelShuttle*) database_version:(NSNumber *) database_version
{
    _database_version = database_version;
    return self;
}


- (RakeClientMetricSentinelShuttle*) persisted_log_count:(NSNumber *) persisted_log_count
{
    _persisted_log_count = persisted_log_count;
    return self;
}


- (RakeClientMetricSentinelShuttle*) expired_log_count:(NSNumber *) expired_log_count
{
    _expired_log_count = expired_log_count;
    return self;
}


- (RakeClientMetricSentinelShuttle*) max_track_count:(NSNumber *) max_track_count
{
    _max_track_count = max_track_count;
    return self;
}


- (RakeClientMetricSentinelShuttle*) logging:(NSString *) logging
{
    _logging = logging;
    return self;
}


- (RakeClientMetricSentinelShuttle*) endpoint:(NSString *) endpoint
{
    _endpoint = endpoint;
    return self;
}


- (RakeClientMetricSentinelShuttle*) auto_flush_onoff:(NSString *) auto_flush_onoff
{
    _auto_flush_onoff = auto_flush_onoff;
    return self;
}


- (RakeClientMetricSentinelShuttle*) auto_flush_interval:(NSNumber *) auto_flush_interval
{
    _auto_flush_interval = auto_flush_interval;
    return self;
}


- (RakeClientMetricSentinelShuttle*) log_count:(NSNumber *) log_count
{
    _log_count = log_count;
    return self;
}


- (RakeClientMetricSentinelShuttle*) log_size:(NSNumber *) log_size
{
    _log_size = log_size;
    return self;
}


- (RakeClientMetricSentinelShuttle*) flush_type:(NSString *) flush_type
{
    _flush_type = flush_type;
    return self;
}


- (RakeClientMetricSentinelShuttle*) server_response_time:(NSNumber *) server_response_time
{
    _server_response_time = server_response_time;
    return self;
}


- (RakeClientMetricSentinelShuttle*) server_response_code:(NSNumber *) server_response_code
{
    _server_response_code = server_response_code;
    return self;
}


- (RakeClientMetricSentinelShuttle*) server_response_body:(NSString *) server_response_body
{
    _server_response_body = server_response_body;
    return self;
}


- (RakeClientMetricSentinelShuttle*) flush_method:(NSString *) flush_method
{
    _flush_method = flush_method;
    return self;
}


- (RakeClientMetricSentinelShuttle*) rake_protocol_version:(NSString *) rake_protocol_version
{
    _rake_protocol_version = rake_protocol_version;
    return self;
}




- (RakeClientMetricSentinelShuttle*) setBodyOf__ERROR_with_exception_type:(NSString *) exception_type
stacktrace:(NSString *) stacktrace
thread_info:(NSString *) thread_info{
		[self clearBody];
		_action = @"";
		_status = @"ERROR";
		_exception_type = exception_type;
		_stacktrace = stacktrace;
		_thread_info = thread_info;
		return self;
	}

- (RakeClientMetricSentinelShuttle*) setBodyOfinstall__ERROR_with_exception_type:(NSString *) exception_type
stacktrace:(NSString *) stacktrace
thread_info:(NSString *) thread_info
endpoint:(NSString *) endpoint
database_version:(NSNumber *) database_version
env:(NSString *) env{
		[self clearBody];
		_action = @"install";
		_status = @"ERROR";
		_exception_type = exception_type;
		_stacktrace = stacktrace;
		_thread_info = thread_info;
		_endpoint = endpoint;
		_database_version = database_version;
		_env = env;
		return self;
	}

- (RakeClientMetricSentinelShuttle*) setBodyOfinstall__DONE_with_operation_time:(NSNumber *) operation_time
endpoint:(NSString *) endpoint
database_version:(NSNumber *) database_version
persisted_log_count:(NSNumber *) persisted_log_count
expired_log_count:(NSNumber *) expired_log_count
logging:(NSString *) logging
env:(NSString *) env
max_track_count:(NSNumber *) max_track_count
auto_flush_onoff:(NSString *) auto_flush_onoff
auto_flush_interval:(NSNumber *) auto_flush_interval{
		[self clearBody];
		_action = @"install";
		_status = @"DONE";
		_operation_time = operation_time;
		_endpoint = endpoint;
		_database_version = database_version;
		_persisted_log_count = persisted_log_count;
		_expired_log_count = expired_log_count;
		_logging = logging;
		_env = env;
		_max_track_count = max_track_count;
		_auto_flush_onoff = auto_flush_onoff;
		_auto_flush_interval = auto_flush_interval;
		return self;
	}

- (RakeClientMetricSentinelShuttle*) setBodyOftrack__ERROR_with_exception_type:(NSString *) exception_type
stacktrace:(NSString *) stacktrace
thread_info:(NSString *) thread_info{
		[self clearBody];
		_action = @"track";
		_status = @"ERROR";
		_exception_type = exception_type;
		_stacktrace = stacktrace;
		_thread_info = thread_info;
		return self;
	}

- (RakeClientMetricSentinelShuttle*) setBodyOfflush__ERROR_with_exception_type:(NSString *) exception_type
stacktrace:(NSString *) stacktrace
thread_info:(NSString *) thread_info{
		[self clearBody];
		_action = @"flush";
		_status = @"ERROR";
		_exception_type = exception_type;
		_stacktrace = stacktrace;
		_thread_info = thread_info;
		return self;
	}

- (RakeClientMetricSentinelShuttle*) setBodyOfflush__DONE_with_operation_time:(NSNumber *) operation_time
endpoint:(NSString *) endpoint
log_count:(NSNumber *) log_count
log_size:(NSNumber *) log_size
flush_type:(NSString *) flush_type
server_response_time:(NSNumber *) server_response_time
server_response_code:(NSNumber *) server_response_code
server_response_body:(NSString *) server_response_body
flush_method:(NSString *) flush_method
rake_protocol_version:(NSString *) rake_protocol_version{
		[self clearBody];
		_action = @"flush";
		_status = @"DONE";
		_operation_time = operation_time;
		_endpoint = endpoint;
		_log_count = log_count;
		_log_size = log_size;
		_flush_type = flush_type;
		_server_response_time = server_response_time;
		_server_response_code = server_response_code;
		_server_response_body = server_response_body;
		_flush_method = flush_method;
		_rake_protocol_version = rake_protocol_version;
		return self;
	}

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
rake_protocol_version:(NSString *) rake_protocol_version{
		[self clearBody];
		_action = @"flush";
		_status = @"DROP";
		_exception_type = exception_type;
		_stacktrace = stacktrace;
		_thread_info = thread_info;
		_operation_time = operation_time;
		_endpoint = endpoint;
		_log_count = log_count;
		_log_size = log_size;
		_flush_type = flush_type;
		_server_response_time = server_response_time;
		_server_response_code = server_response_code;
		_server_response_body = server_response_body;
		_flush_method = flush_method;
		_rake_protocol_version = rake_protocol_version;
		return self;
	}

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
rake_protocol_version:(NSString *) rake_protocol_version{
		[self clearBody];
		_action = @"flush";
		_status = @"RETRY";
		_exception_type = exception_type;
		_stacktrace = stacktrace;
		_thread_info = thread_info;
		_operation_time = operation_time;
		_endpoint = endpoint;
		_log_count = log_count;
		_log_size = log_size;
		_flush_type = flush_type;
		_server_response_time = server_response_time;
		_server_response_code = server_response_code;
		_server_response_body = server_response_body;
		_flush_method = flush_method;
		_rake_protocol_version = rake_protocol_version;
		return self;
	}



// 12 public util functions
- (NSString*) toString
{
    return [self toHBString];
}

- (NSString*) toHBString
{
    return [self toHBString: _$ssDelim];
}

- (NSString*) toHBString:(NSString *)delim
{
    return [NSString stringWithFormat:@"%@%@",[self headerToString],[self bodyToString]];
}

- (NSString*) headerToString
{
    NSString* headerString = @"";
    for(NSString* fieldName in headerFieldNameList){

        NSString* valueString = @"";
        NSObject* value =  [self valueForKey:fieldName];

        if(value != nil){
            if ([value isKindOfClass:[NSNumber class]]) {
                valueString = [NSString stringWithFormat:@"%@",value];
            }else if([value isKindOfClass:[NSString class]]){
                valueString = [self getEscapedValue:(NSString*)value];
            }
        }

        headerString = [headerString stringByAppendingString:[valueString stringByAppendingString:@"\t"]];
    }

    return headerString;
}

- (void) clearBody
{
    for(NSString* bodyFieldName in bodyFieldNameList){
        [self setValue:nil forKey:bodyFieldName];
    }
}


- (NSDictionary*) getBody
{
    NSMutableDictionary *body = [[NSMutableDictionary alloc] init];

    NSString* _$actionKey = @"";
    for(NSString* actionKeyName in actionKeyNameList){
        _$actionKey = [_$actionKey stringByAppendingString:[self valueForKey:actionKeyName]?[self valueForKey:actionKeyName]:@""];
        if ([actionKeyName compare:[actionKeyNameList lastObject]] != NSOrderedSame) {
            _$actionKey = [_$actionKey stringByAppendingString:@":"];
        }
    }

    for(NSString* bodyFieldName in bodyFieldNameList){
        if([self valueForKey:bodyFieldName]){
            [body setValue:[self valueForKey:bodyFieldName] forKey:bodyFieldName];
        }


    }
    return body;
}

- (NSString*) bodyToString
{
    NSError * err;
    NSData * jsonData = [NSJSONSerialization dataWithJSONObject:[self getBody]
                                                        options:0 error:&err];

    NSString * jsonString = [[NSString alloc] initWithData:jsonData
                                                  encoding:NSUTF8StringEncoding];

    return [self getEscapedValue:jsonString];
}

+ (NSDictionary*) getSentinelMeta
{

    NSMutableDictionary *fieldOrder = [[NSMutableDictionary alloc] init];

    int i = 0;
    for(i = 0; i < headerFieldNameList.count ; i++){
        NSString *headerFieldName = [headerFieldNameList objectAtIndex:i];
        [fieldOrder setValue:[NSNumber numberWithInt:i] forKey:headerFieldName];
    }
    [fieldOrder setValue:[NSNumber numberWithInt:i] forKey:@"_$body"];

    NSDictionary* sentinel_meta = @{@"sentinel_meta":@{
                                            @"_$schemaId": _$ssSchemaId,
                                            @"_$fieldOrder":fieldOrder,
                                            @"_$encryptionFields":encryptedFieldsList,
                                            @"_$projectId":@""
                                            }};

    return sentinel_meta;
}

+ (NSDictionary*) toNSDictionary:(NSDictionary *)dict
{
    NSMutableDictionary* sentinelDictionary = [[NSMutableDictionary alloc] init];
    NSMutableDictionary* bodyDictionary = [[NSMutableDictionary alloc] init];

    for(NSString* fieldName in headerFieldNameList){
        if([dict valueForKey:fieldName] != nil) {
            sentinelDictionary[fieldName] = dict[fieldName];
        }
        else {
            sentinelDictionary[fieldName] = @"";
        }
    }

    sentinelDictionary[_$logVersionKey] = _$ssVersion;

    for(NSString* fieldName in bodyFieldNameList){
        if([dict valueForKey:fieldName] != nil) {
            bodyDictionary[fieldName] = dict[fieldName];
        }
    }

    [sentinelDictionary setValue:bodyDictionary forKey:@"_$body"];
    [sentinelDictionary setValue:[[RakeClientMetricSentinelShuttle getSentinelMeta] valueForKey:@"sentinel_meta"] forKey:@"sentinel_meta"];

    return sentinelDictionary;
}

- (NSDictionary*) toNSDictionary
{
    NSMutableDictionary* properties = [[NSMutableDictionary alloc] init];

    // header
    for (NSString* headerFieldName in headerFieldNameList) {
        NSString* valueString = @"";
        if([self valueForKey:headerFieldName] != nil){
            valueString = [self valueForKey:headerFieldName];
            if([valueString isKindOfClass:[NSString class]]){
                valueString = [self getEscapedValue:valueString];
            }
        }
        [properties setValue:valueString forKey:headerFieldName];
    }

    // body
    [properties setValue:[self getBody] forKey:@"_$body"];

    // sentinel_meta
    [properties setValue:[[RakeClientMetricSentinelShuttle getSentinelMeta] valueForKey:@"sentinel_meta"] forKey:@"sentinel_meta"];

    return properties;
}


- (NSString*) toJSONString
{
    NSError *error;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:[self toNSDictionary]
                                                       options:(NSJSONWritingOptions)NSJSONWritingPrettyPrinted
                                                         error:&error];
    if (! jsonData) {
        NSLog(@"bv_jsonStringWithPrettyPrint: error: %@", error.localizedDescription);
        return @"{}";
    } else {
        return [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    }
}

- (NSString*) getEscapedValue:(NSString *)value
{
    return [[[value stringByReplacingOccurrencesOfString:@"\n" withString:@"\\n"] stringByReplacingOccurrencesOfString:@"\t" withString:@"\\t"] stringByReplacingOccurrencesOfString:@"\r" withString:@"\\r"];
}
@end
