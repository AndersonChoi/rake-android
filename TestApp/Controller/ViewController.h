#import <UIKit/UIKit.h>
#import "Rake.h"


@interface ViewController : UIViewController

@property (weak, nonatomic) IBOutlet UIButton *trackButton;
@property (weak, nonatomic) IBOutlet UIButton *flushButton;
@property (strong, nonatomic) Rake *rake;

@end
