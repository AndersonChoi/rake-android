//
//  MemoryLeak.m
//  rake-iOS-Library
//
//  Created by 1002125 on 2016. 1. 22..
//
//

#import <XCTest/XCTest.h>
#import <Rake/Rake.h>
@interface MemoryLeakTest : XCTestCase<RakeDelegate>
@property (nonatomic, strong) Rake *rake;
@end

@implementation MemoryLeakTest

- (void)setUp {
    [super setUp];
    // Put setup code here. This method is called before the invocation of each test method in the class.
    
    self.rake = [[Rake alloc] initWithToken:@"testToken" andFlushInterval:60];
    self.rake.delegate = self;
 
    
}

- (void)tearDown {
    // Put teardown code here. This method is called after the invocation of each test method in the class.
    [super tearDown];
}

- (void)testExample {
    // This is an example of a functional test case.
    // Use XCTAssert and related functions to verify your tests produce the correct results.    
    [self.rake track:@{@"test":@"gi"}];
    [self.rake flush];
    self.rake = nil;

    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        self.rake = nil;
        XCTAssertNil(self.rake);
    });

//    __weak Rake *weakRake;
//    @autoreleasepool {
//        Rake *rake = [[Rake alloc] initWithToken:@"testToken" andFlushInterval:60];
//        rake.delegate = self;
//        weakRake =rake;
//        [weakRake flush];
//    }
    
    
}

- (void)testPerformanceExample {
    // This is an example of a performance test case.
    [self measureBlock:^{
        // Put the code you want to measure the time of here.
    }];
}

#pragma mark - Rake Delegate
- (BOOL)RakeWillFlush:(Rake *)Rake {
    NSLog(@"Rake will Flush");
    return true;
}
@end
