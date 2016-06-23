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
    
    [shuttle setBodyOf_home___with_stacktrace:@" " exception_type:@" " push_notification_id:@" " user_id:@" " oauth_provider:@" " oauth_token:@" " job_advertisement_id:@" " repository:@" " branch:@" " code_text:@" " issue_id:@" " title:@" " comment_text:@" " pull_request_id:@" " target_branch:@" " ab_test_group:@"1"];
    
    [self.rake track: [shuttle toNSDictionary]];

    NSLog(@"Track");
//    NSException *e =[NSException exceptionWithName:@"hihihi" reason:@"idknow" userInfo:nil];
//    [e raise];
//    CFRelease(NULL);
//    [self performSelector:@selector(string) withObject:nil afterDelay:2.0];
//    [self performSelector:@selector(badAccess) withObject:nil afterDelay:2.0];

}


- (void)badAccess
{
    void (*nullFunction)() = NULL;
    
    nullFunction();
}


@end
