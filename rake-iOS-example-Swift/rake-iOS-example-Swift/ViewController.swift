//
//  ViewController.swift
//  rake-iOS-example-Swift
//
//  Created by 1002125 on 2016. 1. 21..
//  Copyright © 2016년 skpdi. All rights reserved.
//

import UIKit
class ViewController: UIViewController {

    @IBOutlet weak var btnTrack: UIButton!
    @IBOutlet weak var btnFlush: UIButton!
    let rake = Rake.sharedInstanceWithToken("your-rake-token", andUseDevServer: true)
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view, typically from a nib.
        print("rake libversion :\(rake.libVersion())")

    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }


    @IBAction func actionBtnTrack(sender: AnyObject) {
        let shuttle = RakeClientTestSentinelShuttle()
        shuttle.ab_test_group("1")
        rake.track(shuttle.toNSDictionary())
        print("track")
        
    }
    @IBAction func actionBtnFlush(sender: AnyObject) {
        rake.flush()
        print("flush")
    }
}

