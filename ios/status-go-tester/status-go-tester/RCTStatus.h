#import <Foundation/Foundation.h>
#import "Jail.h"

typedef void(^OnSignalEvent)(NSString *);

@interface Status : NSObject
+ (void)signalEvent:(const char *)signal;
+ (void)jailEvent:(NSString *)chatId
             data:(NSString *)data;
+ (BOOL)JSCEnabled;
@property (nonatomic) Jail * jail;

@property (nonatomic, copy) OnSignalEvent onSignalEvent;

-(void)startNode;
-(NSString *)createAccount:(NSString *)password;
-(NSString *)login:(NSString *)address password:(NSString *)password;
-(NSString *)sendWeb3Request:(NSString *)payload;
-(void)appStateChanged:(NSString *)state;

+ (instancetype)sharedInstance;

@end
