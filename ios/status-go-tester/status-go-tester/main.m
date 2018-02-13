//
//  main.m
//  status-go-tester
//
//  Created by Igor Mandrigin on 2018-02-06.
//  Copyright © 2018 Igor Mandrigin. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "AppDelegate.h"

#include <signal.h>

int main(int argc, char * argv[]) {
    @autoreleasepool {
        signal(SIGPIPE, SIG_IGN);
        return UIApplicationMain(argc, argv, nil, NSStringFromClass([AppDelegate class]));
    }
}
