//
//  ArchiveTest.m
//  rake-iOS-Library
//
//  Created by 1002125 on 2016. 9. 13..
//
//

#import <XCTest/XCTest.h>
#import "Rake.h"
#import "RakeClientTestSentinelShuttle.h"

@interface ArchiveTest : XCTestCase
@property (nonatomic,weak) Rake *rake;

@end
@implementation ArchiveTest

- (void)setUp {
    [super setUp];
    // Put setup code here. This method is called before the invocation of each test method in the class.
    self.rake =[Rake sharedInstanceWithToken:@"59c1dacfdce83f9c90214748f1db107185749a43" andUseDevServer:YES];

}

- (void)tearDown {
    // Put teardown code here. This method is called after the invocation of each test method in the class.
    [super tearDown];
}

- (void)testArchiving {
    // This is an example of a functional test case.
    // Use XCTAssert and related functions to verify your tests produce the correct results.
    RakeClientTestSentinelShuttle *shuttle = [[RakeClientTestSentinelShuttle alloc] init];
    [shuttle user_id:@"hi_test_uid"];
    [shuttle ab_test_group: @"1"];
    
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wundeclared-selector"
    
    NSString* testPath = [self.rake performSelector:@selector(filePathForData:) withObject:@"rake-ArchiveTestCase"];
    NSMutableArray * queue = [NSMutableArray array];
    NSDictionary *orig = [shuttle toNSDictionary];

    [queue addObject:orig];
    
    
    BOOL isSuccess = (BOOL)[self.rake performSelector:@selector(archiveObject:withFilePath:) withObject:queue withObject:testPath];
    XCTAssertTrue(isSuccess,@"Archive success");
    
    NSArray *arr = [NSKeyedUnarchiver unarchiveObjectWithFile:testPath];
    NSDictionary *arc = [arr objectAtIndex:0];
    
    NSString *valL = [[orig objectForKey:@"_$body"] objectForKey:@"user_id"];
    NSString *valR = [[orig objectForKey:@"_$body"] objectForKey:@"user_id"];
    XCTAssertTrue([valL isEqualToString:valR],@"archive data is not as same as original data");

    NSString *valL2 = [[arc objectForKey:@"_$body"] objectForKey:@"ab_test_group"];
    NSString *valR2 = [[arc objectForKey:@"_$body"] objectForKey:@"ab_test_group"];
    XCTAssertTrue([valL2 isEqualToString:valR2],@"archive data is not as same as original data");
    
    if ([[NSFileManager defaultManager] fileExistsAtPath:testPath]) {
        NSError *error;
        BOOL removed = [[NSFileManager defaultManager] removeItemAtPath:testPath error:&error];
        if (!removed) {
            NSLog(@"%@ unable to remove archived events file at %@ - %@", self, testPath, error);
        }
    }
    
#pragma clang diagnostic pop
    
}

@end
