//
//  Base64Test.m
//  rake-iOS-Library
//
//  Created by 1002125 on 2016. 1. 21..
//
//

#import <XCTest/XCTest.h>
#import "Base64.h"

@interface Base64Test : XCTestCase

@end

@implementation Base64Test

- (void)setUp {
    [super setUp];
    // Put setup code here. This method is called before the invocation of each test method in the class.
}

- (void)tearDown {
    // Put teardown code here. This method is called after the invocation of each test method in the class.
    [super tearDown];
}

- (void)testEncodeDecodeString {
    NSString *strSample1 = @"Rake-iOS Base64 Encoding-Decoding Test Sample 1";
    NSData *dataSample1 = [strSample1 dataUsingEncoding:NSUTF8StringEncoding];
    
    
    NSString *encodedData = [Base64 rk_base64EncodedString:dataSample1];
    NSData *decodedData = [Base64 rk_dataFromBase64String:encodedData];
    NSString *decodedStrSample1 = [[NSString alloc] initWithData:decodedData encoding:NSUTF8StringEncoding];
    
    XCTAssertEqualObjects(strSample1, decodedStrSample1, @"encoding decoding did not match the expected");
}

- (void)testEncodeDecodeJson {
    
}
//- (void)testExample {
//    // This is an example of a functional test case.
//    // Use XCTAssert and related functions to verify your tests produce the correct results.
//}
//
//- (void)testPerformanceExample {
//    // This is an example of a performance test case.
//    [self measureBlock:^{
//        // Put the code you want to measure the time of here.
//    }];
//}

@end
