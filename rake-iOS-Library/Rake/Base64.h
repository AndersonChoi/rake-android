//
//  Base64.h
//  rake-iOS-Library
//
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

@interface Base64 : NSObject
+ (NSData *)rk_dataFromBase64String:(NSString *)aString;
+ (NSString *)rk_base64EncodedString:(NSData *)aData;
@end
