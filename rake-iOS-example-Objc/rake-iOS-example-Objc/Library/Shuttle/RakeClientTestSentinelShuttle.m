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
#import "RakeClientTestSentinelShuttle.h"



@interface RakeClientTestSentinelShuttle()

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
@property(nonatomic) NSString *  place;
@property(nonatomic) NSString *  action;
@property(nonatomic) NSString *  event;
@property(nonatomic) NSNumber *  session_id;
@property(nonatomic) NSString *  log_source;
@property(nonatomic) NSString *  transaction_id;
@property(nonatomic) NSString *  app_package;
@property(nonatomic) NSNumber *  reserved1;
@property(nonatomic) NSNumber *  reserved2;
@property(nonatomic) NSNumber *  reserved3;
@property(nonatomic) NSNumber *  reserved4;
@property(nonatomic) NSString *  stacktrace;
@property(nonatomic) NSString *  exception_type;
@property(nonatomic) NSString *  push_notification_id;
@property(nonatomic) NSString *  user_id;
@property(nonatomic) NSString *  oauth_provider;
@property(nonatomic) NSString *  oauth_token;
@property(nonatomic) NSString *  job_advertisement_id;
@property(nonatomic) NSString *  repository;
@property(nonatomic) NSString *  branch;
@property(nonatomic) NSString *  code_text;
@property(nonatomic) NSNumber *  issue_id;
@property(nonatomic) NSString *  title;
@property(nonatomic) NSString *  comment_text;
@property(nonatomic) NSString *  pull_request_id;
@property(nonatomic) NSString *  target_branch;
@property(nonatomic) NSString *  ab_test_group;


@end

@implementation RakeClientTestSentinelShuttle



static NSString* _$ssTemplateVersion = @"0.1.3";
static NSString* _$ssVersion = @"15.11.25:1.6.1:27";
static NSString* _$ssSchemaId = @"570b606f2a00002631419fc5";
static NSString*  _$ssDelim = @"\t";
static NSString* _$logVersionKey = @"log_version";

static NSArray* headerFieldNameList;
static NSArray* bodyFieldNameList;
static NSArray* actionKeyNameList;
static NSDictionary* bodyRule;
static NSArray* encryptedFieldsList;

+(void)initialize
{
    headerFieldNameList = @[@"base_time",@"local_time",@"recv_time",@"device_id",@"device_model",@"manufacturer",@"os_name",@"os_version",@"resolution",@"screen_width",@"screen_height",@"carrier_name",@"network_type",@"language_code",@"ip",@"recv_host",@"app_version",@"rake_lib",@"rake_lib_version",@"token",@"log_version",@"browser_name",@"browser_version",@"referrer",@"url",@"document_title",@"place",@"action",@"event",@"session_id",@"log_source",@"transaction_id",@"app_package",@"reserved1",@"reserved2",@"reserved3",@"reserved4"];
    bodyFieldNameList = @[@"stacktrace",@"exception_type",@"push_notification_id",@"user_id",@"oauth_provider",@"oauth_token",@"job_advertisement_id",@"repository",@"branch",@"code_text",@"issue_id",@"title",@"comment_text",@"pull_request_id",@"target_branch",@"ab_test_group"];
    actionKeyNameList = @[@"place",@"action"];
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
- (RakeClientTestSentinelShuttle*) base_time:(NSString *) base_time
{
    _base_time = base_time;
    return self;
}


- (RakeClientTestSentinelShuttle*) local_time:(NSString *) local_time
{
    _local_time = local_time;
    return self;
}


- (RakeClientTestSentinelShuttle*) recv_time:(NSString *) recv_time
{
    _recv_time = recv_time;
    return self;
}


- (RakeClientTestSentinelShuttle*) device_id:(NSString *) device_id
{
    _device_id = device_id;
    return self;
}


- (RakeClientTestSentinelShuttle*) device_model:(NSString *) device_model
{
    _device_model = device_model;
    return self;
}


- (RakeClientTestSentinelShuttle*) manufacturer:(NSString *) manufacturer
{
    _manufacturer = manufacturer;
    return self;
}


- (RakeClientTestSentinelShuttle*) os_name:(NSString *) os_name
{
    _os_name = os_name;
    return self;
}


- (RakeClientTestSentinelShuttle*) os_version:(NSString *) os_version
{
    _os_version = os_version;
    return self;
}


- (RakeClientTestSentinelShuttle*) resolution:(NSString *) resolution
{
    _resolution = resolution;
    return self;
}


- (RakeClientTestSentinelShuttle*) screen_width:(NSNumber *) screen_width
{
    _screen_width = screen_width;
    return self;
}


- (RakeClientTestSentinelShuttle*) screen_height:(NSNumber *) screen_height
{
    _screen_height = screen_height;
    return self;
}


- (RakeClientTestSentinelShuttle*) carrier_name:(NSString *) carrier_name
{
    _carrier_name = carrier_name;
    return self;
}


- (RakeClientTestSentinelShuttle*) network_type:(NSString *) network_type
{
    _network_type = network_type;
    return self;
}


- (RakeClientTestSentinelShuttle*) language_code:(NSString *) language_code
{
    _language_code = language_code;
    return self;
}


- (RakeClientTestSentinelShuttle*) ip:(NSString *) ip
{
    _ip = ip;
    return self;
}


- (RakeClientTestSentinelShuttle*) recv_host:(NSString *) recv_host
{
    _recv_host = recv_host;
    return self;
}


- (RakeClientTestSentinelShuttle*) app_version:(NSString *) app_version
{
    _app_version = app_version;
    return self;
}


- (RakeClientTestSentinelShuttle*) rake_lib:(NSString *) rake_lib
{
    _rake_lib = rake_lib;
    return self;
}


- (RakeClientTestSentinelShuttle*) rake_lib_version:(NSString *) rake_lib_version
{
    _rake_lib_version = rake_lib_version;
    return self;
}


- (RakeClientTestSentinelShuttle*) token:(NSString *) token
{
    _token = token;
    return self;
}



- (RakeClientTestSentinelShuttle*) browser_name:(NSString *) browser_name
{
    _browser_name = browser_name;
    return self;
}


- (RakeClientTestSentinelShuttle*) browser_version:(NSString *) browser_version
{
    _browser_version = browser_version;
    return self;
}


- (RakeClientTestSentinelShuttle*) referrer:(NSString *) referrer
{
    _referrer = referrer;
    return self;
}


- (RakeClientTestSentinelShuttle*) url:(NSString *) url
{
    _url = url;
    return self;
}


- (RakeClientTestSentinelShuttle*) document_title:(NSString *) document_title
{
    _document_title = document_title;
    return self;
}


- (RakeClientTestSentinelShuttle*) place:(NSString *) place
{
    _place = place;
    return self;
}


- (RakeClientTestSentinelShuttle*) action:(NSString *) action
{
    _action = action;
    return self;
}


- (RakeClientTestSentinelShuttle*) event:(NSString *) event
{
    _event = event;
    return self;
}


- (RakeClientTestSentinelShuttle*) session_id:(NSNumber *) session_id
{
    _session_id = session_id;
    return self;
}


- (RakeClientTestSentinelShuttle*) log_source:(NSString *) log_source
{
    _log_source = log_source;
    return self;
}


- (RakeClientTestSentinelShuttle*) transaction_id:(NSString *) transaction_id
{
    _transaction_id = transaction_id;
    return self;
}


- (RakeClientTestSentinelShuttle*) app_package:(NSString *) app_package
{
    _app_package = app_package;
    return self;
}


- (RakeClientTestSentinelShuttle*) reserved1:(NSNumber *) reserved1
{
    _reserved1 = reserved1;
    return self;
}


- (RakeClientTestSentinelShuttle*) reserved2:(NSNumber *) reserved2
{
    _reserved2 = reserved2;
    return self;
}


- (RakeClientTestSentinelShuttle*) reserved3:(NSNumber *) reserved3
{
    _reserved3 = reserved3;
    return self;
}


- (RakeClientTestSentinelShuttle*) reserved4:(NSNumber *) reserved4
{
    _reserved4 = reserved4;
    return self;
}


- (RakeClientTestSentinelShuttle*) stacktrace:(NSString *) stacktrace
{
    _stacktrace = stacktrace;
    return self;
}


- (RakeClientTestSentinelShuttle*) exception_type:(NSString *) exception_type
{
    _exception_type = exception_type;
    return self;
}


- (RakeClientTestSentinelShuttle*) push_notification_id:(NSString *) push_notification_id
{
    _push_notification_id = push_notification_id;
    return self;
}


- (RakeClientTestSentinelShuttle*) user_id:(NSString *) user_id
{
    _user_id = user_id;
    return self;
}


- (RakeClientTestSentinelShuttle*) oauth_provider:(NSString *) oauth_provider
{
    _oauth_provider = oauth_provider;
    return self;
}


- (RakeClientTestSentinelShuttle*) oauth_token:(NSString *) oauth_token
{
    _oauth_token = oauth_token;
    return self;
}


- (RakeClientTestSentinelShuttle*) job_advertisement_id:(NSString *) job_advertisement_id
{
    _job_advertisement_id = job_advertisement_id;
    return self;
}


- (RakeClientTestSentinelShuttle*) repository:(NSString *) repository
{
    _repository = repository;
    return self;
}


- (RakeClientTestSentinelShuttle*) branch:(NSString *) branch
{
    _branch = branch;
    return self;
}


- (RakeClientTestSentinelShuttle*) code_text:(NSString *) code_text
{
    _code_text = code_text;
    return self;
}


- (RakeClientTestSentinelShuttle*) issue_id:(NSNumber *) issue_id
{
    _issue_id = issue_id;
    return self;
}


- (RakeClientTestSentinelShuttle*) title:(NSString *) title
{
    _title = title;
    return self;
}


- (RakeClientTestSentinelShuttle*) comment_text:(NSString *) comment_text
{
    _comment_text = comment_text;
    return self;
}


- (RakeClientTestSentinelShuttle*) pull_request_id:(NSString *) pull_request_id
{
    _pull_request_id = pull_request_id;
    return self;
}


- (RakeClientTestSentinelShuttle*) target_branch:(NSString *) target_branch
{
    _target_branch = target_branch;
    return self;
}


- (RakeClientTestSentinelShuttle*) ab_test_group:(NSString *) ab_test_group
{
    _ab_test_group = ab_test_group;
    return self;
}




- (RakeClientTestSentinelShuttle*) setBodyOf__ERROR_with_stacktrace:(NSString *) stacktrace
exception_type:(NSString *) exception_type{
		[self clearBody];
		_place = @"";
		_action = @"ERROR";
		_stacktrace = stacktrace;
		_exception_type = exception_type;
		return self;
	}

- (RakeClientTestSentinelShuttle*) setBodyOf__LOGOUT{
		[self clearBody];
		_place = @"";
		_action = @"LOGOUT";
		return self;
	}

- (RakeClientTestSentinelShuttle*) setBodyOf__PUSH_START_with_push_notification_id:(NSString *) push_notification_id{
		[self clearBody];
		_place = @"";
		_action = @"PUSH_START";
		_push_notification_id = push_notification_id;
		return self;
	}

- (RakeClientTestSentinelShuttle*) setBodyOf_auth__LOGIN_with_user_id:(NSString *) user_id{
		[self clearBody];
		_place = @"/auth";
		_action = @"LOGIN";
		_user_id = user_id;
		return self;
	}

- (RakeClientTestSentinelShuttle*) setBodyOf_oauth__LOGIN_with_oauth_provider:(NSString *) oauth_provider
oauth_token:(NSString *) oauth_token{
		[self clearBody];
		_place = @"/oauth";
		_action = @"LOGIN";
		_oauth_provider = oauth_provider;
		_oauth_token = oauth_token;
		return self;
	}

- (RakeClientTestSentinelShuttle*) setBodyOf_landing___with_ab_test_group:(NSString *) ab_test_group{
		[self clearBody];
		_place = @"/landing";
		_action = @"";
		_ab_test_group = ab_test_group;
		return self;
	}

- (RakeClientTestSentinelShuttle*) setBodyOf_signup__{
		[self clearBody];
		_place = @"/signup";
		_action = @"";
		return self;
	}

- (RakeClientTestSentinelShuttle*) setBodyOf_signup__SIGNUP_with_user_id:(NSString *) user_id{
		[self clearBody];
		_place = @"/signup";
		_action = @"SIGNUP";
		_user_id = user_id;
		return self;
	}

- (RakeClientTestSentinelShuttle*) setBodyOf_home___with_job_advertisement_id:(NSString *) job_advertisement_id
ab_test_group:(NSString *) ab_test_group{
		[self clearBody];
		_place = @"/home";
		_action = @"";
		_job_advertisement_id = job_advertisement_id;
		_ab_test_group = ab_test_group;
		return self;
	}

- (RakeClientTestSentinelShuttle*) setBodyOf_repository___with_repository:(NSString *) repository{
		[self clearBody];
		_place = @"/repository";
		_action = @"";
		_repository = repository;
		return self;
	}

- (RakeClientTestSentinelShuttle*) setBodyOf_repository__STAR_with_repository:(NSString *) repository{
		[self clearBody];
		_place = @"/repository";
		_action = @"STAR";
		_repository = repository;
		return self;
	}

- (RakeClientTestSentinelShuttle*) setBodyOf_repository__FORK_with_repository:(NSString *) repository{
		[self clearBody];
		_place = @"/repository";
		_action = @"FORK";
		_repository = repository;
		return self;
	}

- (RakeClientTestSentinelShuttle*) setBodyOf_repository__WATCH_with_repository:(NSString *) repository{
		[self clearBody];
		_place = @"/repository";
		_action = @"WATCH";
		_repository = repository;
		return self;
	}

- (RakeClientTestSentinelShuttle*) setBodyOf_repository_branch___with_repository:(NSString *) repository
branch:(NSString *) branch{
		[self clearBody];
		_place = @"/repository/branch";
		_action = @"";
		_repository = repository;
		_branch = branch;
		return self;
	}

- (RakeClientTestSentinelShuttle*) setBodyOf_repository_branch__CREATE_with_repository:(NSString *) repository
branch:(NSString *) branch{
		[self clearBody];
		_place = @"/repository/branch";
		_action = @"CREATE";
		_repository = repository;
		_branch = branch;
		return self;
	}

- (RakeClientTestSentinelShuttle*) setBodyOf_repository_branch__COMMIT_with_repository:(NSString *) repository
branch:(NSString *) branch
code_text:(NSString *) code_text{
		[self clearBody];
		_place = @"/repository/branch";
		_action = @"COMMIT";
		_repository = repository;
		_branch = branch;
		_code_text = code_text;
		return self;
	}

- (RakeClientTestSentinelShuttle*) setBodyOf_repository_branch__DELETE_with_repository:(NSString *) repository
branch:(NSString *) branch{
		[self clearBody];
		_place = @"/repository/branch";
		_action = @"DELETE";
		_repository = repository;
		_branch = branch;
		return self;
	}

- (RakeClientTestSentinelShuttle*) setBodyOf_repository_branch_issue___with_repository:(NSString *) repository
branch:(NSString *) branch{
		[self clearBody];
		_place = @"/repository/branch/issue";
		_action = @"";
		_repository = repository;
		_branch = branch;
		return self;
	}

- (RakeClientTestSentinelShuttle*) setBodyOf_repository_branch_issue__CREATE_with_repository:(NSString *) repository
branch:(NSString *) branch
issue_id:(NSNumber *) issue_id{
		[self clearBody];
		_place = @"/repository/branch/issue";
		_action = @"CREATE";
		_repository = repository;
		_branch = branch;
		_issue_id = issue_id;
		return self;
	}

- (RakeClientTestSentinelShuttle*) setBodyOf_repository_branch_issue__COMMENT_with_repository:(NSString *) repository
branch:(NSString *) branch
issue_id:(NSNumber *) issue_id
title:(NSString *) title{
		[self clearBody];
		_place = @"/repository/branch/issue";
		_action = @"COMMENT";
		_repository = repository;
		_branch = branch;
		_issue_id = issue_id;
		_title = title;
		return self;
	}

- (RakeClientTestSentinelShuttle*) setBodyOf_repository_branch_issue__CLOSE_with_repository:(NSString *) repository
branch:(NSString *) branch
issue_id:(NSNumber *) issue_id
comment_text:(NSString *) comment_text{
		[self clearBody];
		_place = @"/repository/branch/issue";
		_action = @"CLOSE";
		_repository = repository;
		_branch = branch;
		_issue_id = issue_id;
		_comment_text = comment_text;
		return self;
	}

- (RakeClientTestSentinelShuttle*) setBodyOf_repository_pull_request__CREATE_with_repository:(NSString *) repository
branch:(NSString *) branch{
		[self clearBody];
		_place = @"/repository/pull_request";
		_action = @"CREATE";
		_repository = repository;
		_branch = branch;
		return self;
	}

- (RakeClientTestSentinelShuttle*) setBodyOf_repository_pull_request__MERGE_with_repository:(NSString *) repository
branch:(NSString *) branch
pull_request_id:(NSString *) pull_request_id
target_branch:(NSString *) target_branch{
		[self clearBody];
		_place = @"/repository/pull_request";
		_action = @"MERGE";
		_repository = repository;
		_branch = branch;
		_pull_request_id = pull_request_id;
		_target_branch = target_branch;
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
    [sentinelDictionary setValue:[[RakeClientTestSentinelShuttle getSentinelMeta] valueForKey:@"sentinel_meta"] forKey:@"sentinel_meta"];

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
    [properties setValue:[[RakeClientTestSentinelShuttle getSentinelMeta] valueForKey:@"sentinel_meta"] forKey:@"sentinel_meta"];

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
