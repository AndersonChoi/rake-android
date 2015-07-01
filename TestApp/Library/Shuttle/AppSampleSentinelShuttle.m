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
#import "AppSampleSentinelShuttle.h"



@interface AppSampleSentinelShuttle()

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
@property(nonatomic) NSString *  action;
@property(nonatomic) NSString *  header1;
@property(nonatomic) NSString *  field1;
@property(nonatomic) NSString *  field2;
@property(nonatomic) NSString *  field3;
@property(nonatomic) NSString *  field4;


@end

@implementation AppSampleSentinelShuttle



static NSString* _$ssTemplateVersion = @"0.1.3";
static NSString* _$ssVersion = @"15.06.10:1.5.29:72";
static NSString* _$ssSchemaId = @"557a6c54e4b05bb5fc9d5c9b";
static NSString*  _$ssDelim = @"\t";
static NSString* _$logVersionKey = @"log_version";

static NSArray* headerFieldNameList;
static NSArray* bodyFieldNameList;
static NSArray* actionKeyNameList;
static NSDictionary* bodyRule;
static NSArray* encryptedFieldsList;

+(void)initialize
{
    headerFieldNameList = @[@"base_time",@"local_time",@"recv_time",@"device_id",@"device_model",@"manufacturer",@"os_name",@"os_version",@"resolution",@"screen_width",@"screen_height",@"carrier_name",@"network_type",@"language_code",@"ip",@"recv_host",@"app_version",@"rake_lib",@"rake_lib_version",@"token",@"log_version",@"action",@"header1"];
    bodyFieldNameList = @[@"field1",@"field2",@"field3",@"field4"];
    actionKeyNameList = @[@"action"];
    encryptedFieldsList = @[@"field1",@"field3"];

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
- (AppSampleSentinelShuttle*) base_time:(NSString *) base_time
{
    _base_time = base_time;
    return self;
}


- (AppSampleSentinelShuttle*) local_time:(NSString *) local_time
{
    _local_time = local_time;
    return self;
}


- (AppSampleSentinelShuttle*) recv_time:(NSString *) recv_time
{
    _recv_time = recv_time;
    return self;
}


- (AppSampleSentinelShuttle*) device_id:(NSString *) device_id
{
    _device_id = device_id;
    return self;
}


- (AppSampleSentinelShuttle*) device_model:(NSString *) device_model
{
    _device_model = device_model;
    return self;
}


- (AppSampleSentinelShuttle*) manufacturer:(NSString *) manufacturer
{
    _manufacturer = manufacturer;
    return self;
}


- (AppSampleSentinelShuttle*) os_name:(NSString *) os_name
{
    _os_name = os_name;
    return self;
}


- (AppSampleSentinelShuttle*) os_version:(NSString *) os_version
{
    _os_version = os_version;
    return self;
}


- (AppSampleSentinelShuttle*) resolution:(NSString *) resolution
{
    _resolution = resolution;
    return self;
}


- (AppSampleSentinelShuttle*) screen_width:(NSNumber *) screen_width
{
    _screen_width = screen_width;
    return self;
}


- (AppSampleSentinelShuttle*) screen_height:(NSNumber *) screen_height
{
    _screen_height = screen_height;
    return self;
}


- (AppSampleSentinelShuttle*) carrier_name:(NSString *) carrier_name
{
    _carrier_name = carrier_name;
    return self;
}


- (AppSampleSentinelShuttle*) network_type:(NSString *) network_type
{
    _network_type = network_type;
    return self;
}


- (AppSampleSentinelShuttle*) language_code:(NSString *) language_code
{
    _language_code = language_code;
    return self;
}


- (AppSampleSentinelShuttle*) ip:(NSString *) ip
{
    _ip = ip;
    return self;
}


- (AppSampleSentinelShuttle*) recv_host:(NSString *) recv_host
{
    _recv_host = recv_host;
    return self;
}


- (AppSampleSentinelShuttle*) app_version:(NSString *) app_version
{
    _app_version = app_version;
    return self;
}


- (AppSampleSentinelShuttle*) rake_lib:(NSString *) rake_lib
{
    _rake_lib = rake_lib;
    return self;
}


- (AppSampleSentinelShuttle*) rake_lib_version:(NSString *) rake_lib_version
{
    _rake_lib_version = rake_lib_version;
    return self;
}


- (AppSampleSentinelShuttle*) token:(NSString *) token
{
    _token = token;
    return self;
}



- (AppSampleSentinelShuttle*) action:(NSString *) action
{
    _action = action;
    return self;
}


- (AppSampleSentinelShuttle*) header1:(NSString *) header1
{
    _header1 = header1;
    return self;
}


- (AppSampleSentinelShuttle*) field1:(NSString *) field1
{
    _field1 = field1;
    return self;
}


- (AppSampleSentinelShuttle*) field2:(NSString *) field2
{
    _field2 = field2;
    return self;
}


- (AppSampleSentinelShuttle*) field3:(NSString *) field3
{
    _field3 = field3;
    return self;
}


- (AppSampleSentinelShuttle*) field4:(NSString *) field4
{
    _field4 = field4;
    return self;
}




- (AppSampleSentinelShuttle*) setBodyOfaction1_with_field1:(NSString *) field1{
		[self clearBody];
		_action = @"action1";
		_field1 = field1;
		return self;
	}

- (AppSampleSentinelShuttle*) setBodyOfaction2_with_field2:(NSString *) field2
field4:(NSString *) field4{
		[self clearBody];
		_action = @"action2";
		_field2 = field2;
		_field4 = field4;
		return self;
	}

- (AppSampleSentinelShuttle*) setBodyOfaction3_with_field3:(NSString *) field3{
		[self clearBody];
		_action = @"action3";
		_field3 = field3;
		return self;
	}

- (AppSampleSentinelShuttle*) setBodyOfaction4_with_field1:(NSString *) field1
field3:(NSString *) field3
field4:(NSString *) field4{
		[self clearBody];
		_action = @"action4";
		_field1 = field1;
		_field3 = field3;
		_field4 = field4;
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
    [sentinelDictionary setValue:[[AppSampleSentinelShuttle getSentinelMeta] valueForKey:@"sentinel_meta"] forKey:@"sentinel_meta"];

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
    [properties setValue:[[AppSampleSentinelShuttle getSentinelMeta] valueForKey:@"sentinel_meta"] forKey:@"sentinel_meta"];

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
