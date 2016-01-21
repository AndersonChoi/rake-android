//
//  Base64.h
//  rake-iOS-Library
//
//  Created by 1002125 on 2016. 1. 21..
//
//

#import <Foundation/Foundation.h>

@interface Base64 : NSObject
+ (NSData *)rk_dataFromBase64String:(NSString *)aString;
+ (NSString *)rk_base64EncodedString:(NSData *)aData;
@end
