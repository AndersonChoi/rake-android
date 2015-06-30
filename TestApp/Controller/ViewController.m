#import "ViewController.h"
#import "AppSampleSentinelShuttle.h"


@interface ViewController ()
@end

@implementation ViewController


- (void)viewDidLoad {
    [super viewDidLoad];

    /*
    initialize rake instance. (singleton)

    (NSString *)apiToken:   rake token

    (BOOL) isDevServer:     If this argument is true
                            1. send log to dev server
                            2. flush "immediately" without saving

    So, if you are going to "release" your app, you must set this parameter to 'false'
    */

    self.rake = [Rake sharedInstanceWithToken: @"your rake token" andUseDevServer: true];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
}

- (IBAction) trackAction:(id)sender {

    // initialize shuttle
    AppSampleSentinelShuttle *shuttle = [[AppSampleSentinelShuttle alloc] init];

    [shuttle setBodyOfaction4_with_field1:@"field1 value"
                                   field3:@"field2 value"
                                   field4:@"field3 value"];

    [self.rake track: [shuttle toNSDictionary]];

    NSLog(@"Track\n%@", [shuttle toJSONString]);
}

- (IBAction) flushAction:(id)sender {
    [self.rake flush];

    NSLog(@"Flush");
}

@end