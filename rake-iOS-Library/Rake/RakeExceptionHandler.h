//
//  RakeExceptionHandler.h
//  rake-iOS-Library
//
//  Created by 1002125 on 2016. 1. 22..
//
//

#import <Foundation/Foundation.h>

@class Rake;

@interface RakeExceptionHandler : NSObject

+ (instancetype)sharedHandler;
- (void)addRakeInstance:(Rake *)instance;

@end
