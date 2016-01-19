//
//  ViewController.m
//  rake-iOS-example-Objc
//
//  Created by 1002125 on 2016. 1. 18..
//  Copyright © 2016년 skpdi. All rights reserved.
//
#import <Rake/Rake.h>
#import "ViewController.h"
#import "RakeClientTestSentinelShuttle.h"

@interface ViewController ()
@property (weak, nonatomic) IBOutlet UIButton *btnTrack;
@property (weak, nonatomic) IBOutlet UIButton *btnFlush;
@property (strong, nonatomic) Rake *rake;

@end

@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view, typically from a nib.
    self.rake = [Rake sharedInstanceWithToken: @"your-rake-token" andUseDevServer: true];

}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}
- (IBAction)actionBtnFlush:(id)sender {
    [self.rake flush];

}
- (IBAction)actionBtnTrack:(id)sender {
    RakeClientTestSentinelShuttle* shuttle = [[RakeClientTestSentinelShuttle alloc] init];
    
    [shuttle ab_test_group: @"1"];
    
    [self.rake track: [shuttle toNSDictionary]];

    NSLog(@"Flush");
}

@end
