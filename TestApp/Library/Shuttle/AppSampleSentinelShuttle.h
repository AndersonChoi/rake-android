/*
 * Sentinel-Shuttle for iOS
 *
 * by Data Infrastructure
 * template version 0.1
 */

#import <Foundation/Foundation.h>

@interface AppSampleSentinelShuttle : NSObject

- (id)init;

// all methods for field setting
- (AppSampleSentinelShuttle*) base_time:(NSString *) base_time;

- (AppSampleSentinelShuttle*) local_time:(NSString *) local_time;

- (AppSampleSentinelShuttle*) recv_time:(NSString *) recv_time;

- (AppSampleSentinelShuttle*) device_id:(NSString *) device_id;

- (AppSampleSentinelShuttle*) device_model:(NSString *) device_model;

- (AppSampleSentinelShuttle*) manufacturer:(NSString *) manufacturer;

- (AppSampleSentinelShuttle*) os_name:(NSString *) os_name;

- (AppSampleSentinelShuttle*) os_version:(NSString *) os_version;

- (AppSampleSentinelShuttle*) resolution:(NSString *) resolution;

- (AppSampleSentinelShuttle*) screen_width:(NSNumber *) screen_width;

- (AppSampleSentinelShuttle*) screen_height:(NSNumber *) screen_height;

- (AppSampleSentinelShuttle*) carrier_name:(NSString *) carrier_name;

- (AppSampleSentinelShuttle*) network_type:(NSString *) network_type;

- (AppSampleSentinelShuttle*) language_code:(NSString *) language_code;

- (AppSampleSentinelShuttle*) ip:(NSString *) ip;

- (AppSampleSentinelShuttle*) recv_host:(NSString *) recv_host;

- (AppSampleSentinelShuttle*) app_version:(NSString *) app_version;

- (AppSampleSentinelShuttle*) rake_lib:(NSString *) rake_lib;

- (AppSampleSentinelShuttle*) rake_lib_version:(NSString *) rake_lib_version;

- (AppSampleSentinelShuttle*) token:(NSString *) token;


- (AppSampleSentinelShuttle*) action:(NSString *) action;

- (AppSampleSentinelShuttle*) header1:(NSString *) header1;

- (AppSampleSentinelShuttle*) field1:(NSString *) field1;

- (AppSampleSentinelShuttle*) field2:(NSString *) field2;

- (AppSampleSentinelShuttle*) field3:(NSString *) field3;

- (AppSampleSentinelShuttle*) field4:(NSString *) field4;



- (AppSampleSentinelShuttle*) setBodyOfaction1_with_field1:(NSString *) field1;
- (AppSampleSentinelShuttle*) setBodyOfaction2_with_field2:(NSString *) field2
field4:(NSString *) field4;
- (AppSampleSentinelShuttle*) setBodyOfaction3_with_field3:(NSString *) field3;
- (AppSampleSentinelShuttle*) setBodyOfaction4_with_field1:(NSString *) field1
field3:(NSString *) field3
field4:(NSString *) field4;


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
