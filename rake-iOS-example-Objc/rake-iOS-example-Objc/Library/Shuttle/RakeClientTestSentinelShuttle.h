/*
 * Sentinel-Shuttle for iOS
 *
 * by Data Infrastructure
 * template version 0.1
 */

#import <Foundation/Foundation.h>

@interface RakeClientTestSentinelShuttle : NSObject

- (id)init;

// all methods for field setting
- (RakeClientTestSentinelShuttle*) base_time:(NSString *) base_time;

- (RakeClientTestSentinelShuttle*) local_time:(NSString *) local_time;

- (RakeClientTestSentinelShuttle*) recv_time:(NSString *) recv_time;

- (RakeClientTestSentinelShuttle*) device_id:(NSString *) device_id;

- (RakeClientTestSentinelShuttle*) device_model:(NSString *) device_model;

- (RakeClientTestSentinelShuttle*) manufacturer:(NSString *) manufacturer;

- (RakeClientTestSentinelShuttle*) os_name:(NSString *) os_name;

- (RakeClientTestSentinelShuttle*) os_version:(NSString *) os_version;

- (RakeClientTestSentinelShuttle*) resolution:(NSString *) resolution;

- (RakeClientTestSentinelShuttle*) screen_width:(NSNumber *) screen_width;

- (RakeClientTestSentinelShuttle*) screen_height:(NSNumber *) screen_height;

- (RakeClientTestSentinelShuttle*) carrier_name:(NSString *) carrier_name;

- (RakeClientTestSentinelShuttle*) network_type:(NSString *) network_type;

- (RakeClientTestSentinelShuttle*) language_code:(NSString *) language_code;

- (RakeClientTestSentinelShuttle*) ip:(NSString *) ip;

- (RakeClientTestSentinelShuttle*) recv_host:(NSString *) recv_host;

- (RakeClientTestSentinelShuttle*) app_version:(NSString *) app_version;

- (RakeClientTestSentinelShuttle*) rake_lib:(NSString *) rake_lib;

- (RakeClientTestSentinelShuttle*) rake_lib_version:(NSString *) rake_lib_version;

- (RakeClientTestSentinelShuttle*) token:(NSString *) token;


- (RakeClientTestSentinelShuttle*) browser_name:(NSString *) browser_name;

- (RakeClientTestSentinelShuttle*) browser_version:(NSString *) browser_version;

- (RakeClientTestSentinelShuttle*) referrer:(NSString *) referrer;

- (RakeClientTestSentinelShuttle*) url:(NSString *) url;

- (RakeClientTestSentinelShuttle*) document_title:(NSString *) document_title;

- (RakeClientTestSentinelShuttle*) place:(NSString *) place;

- (RakeClientTestSentinelShuttle*) action:(NSString *) action;

- (RakeClientTestSentinelShuttle*) event:(NSString *) event;

- (RakeClientTestSentinelShuttle*) session_id:(NSNumber *) session_id;

- (RakeClientTestSentinelShuttle*) log_source:(NSString *) log_source;

- (RakeClientTestSentinelShuttle*) transaction_id:(NSString *) transaction_id;

- (RakeClientTestSentinelShuttle*) app_package:(NSString *) app_package;

- (RakeClientTestSentinelShuttle*) reserved1:(NSNumber *) reserved1;

- (RakeClientTestSentinelShuttle*) reserved2:(NSNumber *) reserved2;

- (RakeClientTestSentinelShuttle*) reserved3:(NSNumber *) reserved3;

- (RakeClientTestSentinelShuttle*) reserved4:(NSNumber *) reserved4;

- (RakeClientTestSentinelShuttle*) stacktrace:(NSString *) stacktrace;

- (RakeClientTestSentinelShuttle*) exception_type:(NSString *) exception_type;

- (RakeClientTestSentinelShuttle*) push_notification_id:(NSString *) push_notification_id;

- (RakeClientTestSentinelShuttle*) user_id:(NSString *) user_id;

- (RakeClientTestSentinelShuttle*) oauth_provider:(NSString *) oauth_provider;

- (RakeClientTestSentinelShuttle*) oauth_token:(NSString *) oauth_token;

- (RakeClientTestSentinelShuttle*) job_advertisement_id:(NSString *) job_advertisement_id;

- (RakeClientTestSentinelShuttle*) repository:(NSString *) repository;

- (RakeClientTestSentinelShuttle*) branch:(NSString *) branch;

- (RakeClientTestSentinelShuttle*) code_text:(NSString *) code_text;

- (RakeClientTestSentinelShuttle*) issue_id:(NSNumber *) issue_id;

- (RakeClientTestSentinelShuttle*) title:(NSString *) title;

- (RakeClientTestSentinelShuttle*) comment_text:(NSString *) comment_text;

- (RakeClientTestSentinelShuttle*) pull_request_id:(NSString *) pull_request_id;

- (RakeClientTestSentinelShuttle*) target_branch:(NSString *) target_branch;

- (RakeClientTestSentinelShuttle*) ab_test_group:(NSString *) ab_test_group;



- (RakeClientTestSentinelShuttle*) setBodyOf__ERROR_with_stacktrace:(NSString *) stacktrace
exception_type:(NSString *) exception_type;
- (RakeClientTestSentinelShuttle*) setBodyOf__LOGOUT;
- (RakeClientTestSentinelShuttle*) setBodyOf__PUSH_START_with_push_notification_id:(NSString *) push_notification_id;
- (RakeClientTestSentinelShuttle*) setBodyOf_auth__LOGIN_with_user_id:(NSString *) user_id;
- (RakeClientTestSentinelShuttle*) setBodyOf_oauth__LOGIN_with_oauth_provider:(NSString *) oauth_provider
oauth_token:(NSString *) oauth_token;
- (RakeClientTestSentinelShuttle*) setBodyOf_landing___with_ab_test_group:(NSString *) ab_test_group;
- (RakeClientTestSentinelShuttle*) setBodyOf_signup__;
- (RakeClientTestSentinelShuttle*) setBodyOf_signup__SIGNUP_with_user_id:(NSString *) user_id;
- (RakeClientTestSentinelShuttle*) setBodyOf_home___with_job_advertisement_id:(NSString *) job_advertisement_id
ab_test_group:(NSString *) ab_test_group;
- (RakeClientTestSentinelShuttle*) setBodyOf_repository___with_repository:(NSString *) repository;
- (RakeClientTestSentinelShuttle*) setBodyOf_repository__STAR_with_repository:(NSString *) repository;
- (RakeClientTestSentinelShuttle*) setBodyOf_repository__FORK_with_repository:(NSString *) repository;
- (RakeClientTestSentinelShuttle*) setBodyOf_repository__WATCH_with_repository:(NSString *) repository;
- (RakeClientTestSentinelShuttle*) setBodyOf_repository_branch___with_repository:(NSString *) repository
branch:(NSString *) branch;
- (RakeClientTestSentinelShuttle*) setBodyOf_repository_branch__CREATE_with_repository:(NSString *) repository
branch:(NSString *) branch;
- (RakeClientTestSentinelShuttle*) setBodyOf_repository_branch__COMMIT_with_repository:(NSString *) repository
branch:(NSString *) branch
code_text:(NSString *) code_text;
- (RakeClientTestSentinelShuttle*) setBodyOf_repository_branch__DELETE_with_repository:(NSString *) repository
branch:(NSString *) branch;
- (RakeClientTestSentinelShuttle*) setBodyOf_repository_branch_issue___with_repository:(NSString *) repository
branch:(NSString *) branch;
- (RakeClientTestSentinelShuttle*) setBodyOf_repository_branch_issue__CREATE_with_repository:(NSString *) repository
branch:(NSString *) branch
issue_id:(NSNumber *) issue_id;
- (RakeClientTestSentinelShuttle*) setBodyOf_repository_branch_issue__COMMENT_with_repository:(NSString *) repository
branch:(NSString *) branch
issue_id:(NSNumber *) issue_id
title:(NSString *) title;
- (RakeClientTestSentinelShuttle*) setBodyOf_repository_branch_issue__CLOSE_with_repository:(NSString *) repository
branch:(NSString *) branch
issue_id:(NSNumber *) issue_id
comment_text:(NSString *) comment_text;
- (RakeClientTestSentinelShuttle*) setBodyOf_repository_pull_request__CREATE_with_repository:(NSString *) repository
branch:(NSString *) branch;
- (RakeClientTestSentinelShuttle*) setBodyOf_repository_pull_request__MERGE_with_repository:(NSString *) repository
branch:(NSString *) branch
pull_request_id:(NSString *) pull_request_id
target_branch:(NSString *) target_branch;


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
