//
//  NSData+Base64.h
//  base64
//
//  Created by Matt Gallagher on 2009/06/03.
//  Modified by JungHyun Kim on 2014/04/10
//  Copyright 2009 Matt Gallagher. All rights reserved.
//
//  Permission is given to use this source code file, free of charge, in any
//  project, commercial or otherwise, entirely at your risk, with the condition
//  that any redistribution (in part or whole) of source code must retain
//  this copyright and permission notice. Attribution in compiled projects is
//  appreciated but not required.
//

#import <Foundation/Foundation.h>

void *RK_NewBase64Decode(
                         const char *inputBuffer,
                         size_t length,
                         size_t *outputLength);

char *RK_NewBase64Encode(
                         const void *inputBuffer,
                         size_t length,
                         bool separateLines,
                         size_t *outputLength);

@interface NSData (Rake_Base64)

+ (NSData *)rk_dataFromBase64String:(NSString *)aString;
- (NSString *)rk_base64EncodedString;

@end
