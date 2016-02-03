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
#import "AppCrashLoggerSentinelShuttle.h"



@interface AppCrashLoggerSentinelShuttle()

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
@property(nonatomic) NSString *  log_id;
@property(nonatomic) NSString *  transaction_id;
@property(nonatomic) NSString *  crash_logger_version;
@property(nonatomic) NSString *  app_key;
@property(nonatomic) NSString *  report_type;
@property(nonatomic) NSString *  package_name;
@property(nonatomic) NSNumber *  total_disk_size;
@property(nonatomic) NSNumber *  available_disk_size;
@property(nonatomic) NSNumber *  total_memory_size;
@property(nonatomic) NSNumber *  available_memory_size;
@property(nonatomic) NSString *  logcat;
@property(nonatomic) NSString *  user_comment;
@property(nonatomic) NSString *  app_started_at;
@property(nonatomic) NSString *  exception_created_at;
@property(nonatomic) NSString *  exception_type;
@property(nonatomic) NSString *  exception_info;
@property(nonatomic) NSNumber *  exception_code;
@property(nonatomic) NSNumber *  thread_count;
@property(nonatomic) NSString *  thread_info;
@property(nonatomic) NSString *  stacktrace;
@property(nonatomic) NSNumber *  footprint_count;
@property(nonatomic) NSMutableArray *  footprint;
@property(nonatomic) NSString *  crash_logger_config;
@property(nonatomic) NSString *  cpu_info;
@property(nonatomic) NSString *  memory_info;


@end

@implementation AppCrashLoggerSentinelShuttle



static NSString* _$ssTemplateVersion = @"0.1.3";
static NSString* _$ssVersion = @"15.11.18:1.5.54:32";
static NSString* _$ssSchemaId = @"5653282e1b00003601047d9a";
static NSString*  _$ssDelim = @"\t";
static NSString* _$logVersionKey = @"log_version";

static NSArray* headerFieldNameList;
static NSArray* bodyFieldNameList;
static NSArray* actionKeyNameList;
static NSDictionary* bodyRule;
static NSArray* encryptedFieldsList;

+(void)initialize
{
    headerFieldNameList = @[@"base_time",@"local_time",@"recv_time",@"device_id",@"device_model",@"manufacturer",@"os_name",@"os_version",@"resolution",@"screen_width",@"screen_height",@"carrier_name",@"network_type",@"language_code",@"recv_host",@"app_version",@"rake_lib",@"rake_lib_version",@"token",@"log_version",@"browser_name",@"browser_version",@"referrer",@"url",@"document_title",@"log_id",@"transaction_id",@"crash_logger_version"];
    bodyFieldNameList = @[@"app_key",@"report_type",@"package_name",@"total_disk_size",@"available_disk_size",@"total_memory_size",@"available_memory_size",@"logcat",@"user_comment",@"app_started_at",@"exception_created_at",@"exception_type",@"exception_info",@"exception_code",@"thread_count",@"thread_info",@"stacktrace",@"footprint_count",@"footprint",@"crash_logger_config",@"cpu_info",@"memory_info"];
    actionKeyNameList = @[@"log_id"];
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
- (AppCrashLoggerSentinelShuttle*) base_time:(NSString *) base_time
{
    _base_time = base_time;
    return self;
}


- (AppCrashLoggerSentinelShuttle*) local_time:(NSString *) local_time
{
    _local_time = local_time;
    return self;
}


- (AppCrashLoggerSentinelShuttle*) recv_time:(NSString *) recv_time
{
    _recv_time = recv_time;
    return self;
}


- (AppCrashLoggerSentinelShuttle*) device_id:(NSString *) device_id
{
    _device_id = device_id;
    return self;
}


- (AppCrashLoggerSentinelShuttle*) device_model:(NSString *) device_model
{
    _device_model = device_model;
    return self;
}


- (AppCrashLoggerSentinelShuttle*) manufacturer:(NSString *) manufacturer
{
    _manufacturer = manufacturer;
    return self;
}


- (AppCrashLoggerSentinelShuttle*) os_name:(NSString *) os_name
{
    _os_name = os_name;
    return self;
}


- (AppCrashLoggerSentinelShuttle*) os_version:(NSString *) os_version
{
    _os_version = os_version;
    return self;
}


- (AppCrashLoggerSentinelShuttle*) resolution:(NSString *) resolution
{
    _resolution = resolution;
    return self;
}


- (AppCrashLoggerSentinelShuttle*) screen_width:(NSNumber *) screen_width
{
    _screen_width = screen_width;
    return self;
}


- (AppCrashLoggerSentinelShuttle*) screen_height:(NSNumber *) screen_height
{
    _screen_height = screen_height;
    return self;
}


- (AppCrashLoggerSentinelShuttle*) carrier_name:(NSString *) carrier_name
{
    _carrier_name = carrier_name;
    return self;
}


- (AppCrashLoggerSentinelShuttle*) network_type:(NSString *) network_type
{
    _network_type = network_type;
    return self;
}


- (AppCrashLoggerSentinelShuttle*) language_code:(NSString *) language_code
{
    _language_code = language_code;
    return self;
}


- (AppCrashLoggerSentinelShuttle*) recv_host:(NSString *) recv_host
{
    _recv_host = recv_host;
    return self;
}


- (AppCrashLoggerSentinelShuttle*) app_version:(NSString *) app_version
{
    _app_version = app_version;
    return self;
}


- (AppCrashLoggerSentinelShuttle*) rake_lib:(NSString *) rake_lib
{
    _rake_lib = rake_lib;
    return self;
}


- (AppCrashLoggerSentinelShuttle*) rake_lib_version:(NSString *) rake_lib_version
{
    _rake_lib_version = rake_lib_version;
    return self;
}


- (AppCrashLoggerSentinelShuttle*) token:(NSString *) token
{
    _token = token;
    return self;
}



- (AppCrashLoggerSentinelShuttle*) browser_name:(NSString *) browser_name
{
    _browser_name = browser_name;
    return self;
}


- (AppCrashLoggerSentinelShuttle*) browser_version:(NSString *) browser_version
{
    _browser_version = browser_version;
    return self;
}


- (AppCrashLoggerSentinelShuttle*) referrer:(NSString *) referrer
{
    _referrer = referrer;
    return self;
}


- (AppCrashLoggerSentinelShuttle*) url:(NSString *) url
{
    _url = url;
    return self;
}


- (AppCrashLoggerSentinelShuttle*) document_title:(NSString *) document_title
{
    _document_title = document_title;
    return self;
}


- (AppCrashLoggerSentinelShuttle*) log_id:(NSString *) log_id
{
    _log_id = log_id;
    return self;
}


- (AppCrashLoggerSentinelShuttle*) transaction_id:(NSString *) transaction_id
{
    _transaction_id = transaction_id;
    return self;
}


- (AppCrashLoggerSentinelShuttle*) crash_logger_version:(NSString *) crash_logger_version
{
    _crash_logger_version = crash_logger_version;
    return self;
}


- (AppCrashLoggerSentinelShuttle*) app_key:(NSString *) app_key
{
    _app_key = app_key;
    return self;
}


- (AppCrashLoggerSentinelShuttle*) report_type:(NSString *) report_type
{
    _report_type = report_type;
    return self;
}


- (AppCrashLoggerSentinelShuttle*) package_name:(NSString *) package_name
{
    _package_name = package_name;
    return self;
}


- (AppCrashLoggerSentinelShuttle*) total_disk_size:(NSNumber *) total_disk_size
{
    _total_disk_size = total_disk_size;
    return self;
}


- (AppCrashLoggerSentinelShuttle*) available_disk_size:(NSNumber *) available_disk_size
{
    _available_disk_size = available_disk_size;
    return self;
}


- (AppCrashLoggerSentinelShuttle*) total_memory_size:(NSNumber *) total_memory_size
{
    _total_memory_size = total_memory_size;
    return self;
}


- (AppCrashLoggerSentinelShuttle*) available_memory_size:(NSNumber *) available_memory_size
{
    _available_memory_size = available_memory_size;
    return self;
}


- (AppCrashLoggerSentinelShuttle*) logcat:(NSString *) logcat
{
    _logcat = logcat;
    return self;
}


- (AppCrashLoggerSentinelShuttle*) user_comment:(NSString *) user_comment
{
    _user_comment = user_comment;
    return self;
}


- (AppCrashLoggerSentinelShuttle*) app_started_at:(NSString *) app_started_at
{
    _app_started_at = app_started_at;
    return self;
}


- (AppCrashLoggerSentinelShuttle*) exception_created_at:(NSString *) exception_created_at
{
    _exception_created_at = exception_created_at;
    return self;
}


- (AppCrashLoggerSentinelShuttle*) exception_type:(NSString *) exception_type
{
    _exception_type = exception_type;
    return self;
}


- (AppCrashLoggerSentinelShuttle*) exception_info:(NSString *) exception_info
{
    _exception_info = exception_info;
    return self;
}


- (AppCrashLoggerSentinelShuttle*) exception_code:(NSNumber *) exception_code
{
    _exception_code = exception_code;
    return self;
}


- (AppCrashLoggerSentinelShuttle*) thread_count:(NSNumber *) thread_count
{
    _thread_count = thread_count;
    return self;
}


- (AppCrashLoggerSentinelShuttle*) thread_info:(NSString *) thread_info
{
    _thread_info = thread_info;
    return self;
}


- (AppCrashLoggerSentinelShuttle*) stacktrace:(NSString *) stacktrace
{
    _stacktrace = stacktrace;
    return self;
}


- (AppCrashLoggerSentinelShuttle*) footprint_count:(NSNumber *) footprint_count
{
    _footprint_count = footprint_count;
    return self;
}


- (AppCrashLoggerSentinelShuttle*) footprint:(NSMutableArray *) footprint
{
    _footprint = footprint;
    return self;
}


- (AppCrashLoggerSentinelShuttle*) crash_logger_config:(NSString *) crash_logger_config
{
    _crash_logger_config = crash_logger_config;
    return self;
}


- (AppCrashLoggerSentinelShuttle*) cpu_info:(NSString *) cpu_info
{
    _cpu_info = cpu_info;
    return self;
}


- (AppCrashLoggerSentinelShuttle*) memory_info:(NSString *) memory_info
{
    _memory_info = memory_info;
    return self;
}




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
footprint_count:(NSNumber *) footprint_count{
		[self clearBody];
		_log_id = @"crash";
		_report_type = report_type;
		_app_key = app_key;
		_package_name = package_name;
		_total_disk_size = total_disk_size;
		_available_disk_size = available_disk_size;
		_total_memory_size = total_memory_size;
		_available_memory_size = available_memory_size;
		_logcat = logcat;
		_user_comment = user_comment;
		_app_started_at = app_started_at;
		_exception_created_at = exception_created_at;
		_exception_type = exception_type;
		_exception_info = exception_info;
		_exception_code = exception_code;
		_thread_count = thread_count;
		_thread_info = thread_info;
		_stacktrace = stacktrace;
		_crash_logger_config = crash_logger_config;
		_footprint_count = footprint_count;
		return self;
	}

- (AppCrashLoggerSentinelShuttle*) setBodyOfthread_with_thread_count:(NSNumber *) thread_count
thread_info:(NSString *) thread_info
stacktrace:(NSString *) stacktrace{
		[self clearBody];
		_log_id = @"thread";
		_thread_count = thread_count;
		_thread_info = thread_info;
		_stacktrace = stacktrace;
		return self;
	}

- (AppCrashLoggerSentinelShuttle*) setBodyOfsystem_with_cpu_info:(NSString *) cpu_info
memory_info:(NSString *) memory_info{
		[self clearBody];
		_log_id = @"system";
		_cpu_info = cpu_info;
		_memory_info = memory_info;
		return self;
	}

- (AppCrashLoggerSentinelShuttle*) setBodyOffootprint_with_footprint_count:(NSNumber *) footprint_count
footprint:(NSMutableArray *) footprint{
		[self clearBody];
		_log_id = @"footprint";
		_footprint_count = footprint_count;
		_footprint = footprint;
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
    [sentinelDictionary setValue:[[AppCrashLoggerSentinelShuttle getSentinelMeta] valueForKey:@"sentinel_meta"] forKey:@"sentinel_meta"];

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
    [properties setValue:[[AppCrashLoggerSentinelShuttle getSentinelMeta] valueForKey:@"sentinel_meta"] forKey:@"sentinel_meta"];

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
