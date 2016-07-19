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
    self.rake = [Rake sharedInstance];
    NSLog(@"rake Version %@",[self.rake libVersion]);

}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}
- (IBAction)actionBtnFlush:(id)sender {
    [self.rake flush];
    NSLog(@"Flush");

}
- (IBAction)actionBtnTrack:(id)sender {
    RakeClientTestSentinelShuttle* shuttle = [[RakeClientTestSentinelShuttle alloc] init];
    [shuttle setBodyOf__PUSH_START_with_push_notification_id:@"NotiID"];
    [self.rake track: [shuttle toNSDictionary]];
    NSLog(@"Track");
}


- (void)badAccess
{
    void (*nullFunction)() = NULL;
    
    nullFunction();
}


@end
