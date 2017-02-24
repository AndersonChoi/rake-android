//
//  RakeTest.m
//  rake-iOS-Library
//
//  Created by 1002125 on 2016. 1. 18..
//
//

#import <XCTest/XCTest.h>
#import "RakeConfig.h"
#import "Rake.h"
#import "RakeClientTestSentinelShuttle.h"
@interface RakeTest : XCTestCase
@property (nonatomic,weak) Rake *rake;
@end

@implementation RakeTest

- (void)setUp {
    [super setUp];
    // Put setup code here. This method is called before the invocation of each test method in the class.
    self.rake =[Rake sharedInstanceWithToken:@"59c1dacfdce83f9c90214748f1db107185749a43" andUseDevServer:YES];
//    [self.rake performSelector:@selector(kill:) withObject:nil withObject:nil];
}

- (void)tearDown {
    // Put teardown code here. This method is called after the invocation of each test method in the class.
    [super tearDown];
}

- (void)testRakeWithShuttle {
    
    RakeClientTestSentinelShuttle *shuttle = [[RakeClientTestSentinelShuttle alloc] init];
    [shuttle user_id:@"hi_test_uid"];
    [shuttle ab_test_group: @"1"];
    
    [self.rake track: [shuttle toNSDictionary]];
    [self.rake flush];
   
    
    [NSThread sleepForTimeInterval:0.2f];
    NSLog(@"Flush");
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

- (void)testPortChanged{
    NSInteger port = 8663;
    [self.rake setServerPort:port];
    
    XCTAssertNotEqual([self.rake serverURL], DEV_SERVER_URL);
}

@end
