#import "RCTStatus.h"
#import <Statusgo/Statusgo.h>

@interface NSDictionary (BVJSONString)
-(NSString*) bv_jsonStringWithPrettyPrint:(BOOL) prettyPrint;
@end

@implementation NSDictionary (BVJSONString)

-(NSString*) bv_jsonStringWithPrettyPrint:(BOOL) prettyPrint {
    NSError *error;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:self
                                                       options:(NSJSONWritingOptions)    (prettyPrint ? NSJSONWritingPrettyPrinted : 0)
                                                         error:&error];
    
    if (! jsonData) {
        NSLog(@"bv_jsonStringWithPrettyPrint: error: %@", error.localizedDescription);
        return @"{}";
    } else {
        return [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    }
}
@end

@interface NSArray (BVJSONString)
- (NSString *)bv_jsonStringWithPrettyPrint:(BOOL)prettyPrint;
@end

@implementation NSArray (BVJSONString)
-(NSString*) bv_jsonStringWithPrettyPrint:(BOOL) prettyPrint {
    NSError *error;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:self
                                                       options:(NSJSONWritingOptions) (prettyPrint ? NSJSONWritingPrettyPrinted : 0)
                                                         error:&error];
    
    if (! jsonData) {
        NSLog(@"bv_jsonStringWithPrettyPrint: error: %@", error.localizedDescription);
        return @"[]";
    } else {
        return [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    }
}
@end

static bool isStatusInitialized;
@implementation Status {
}

+ (instancetype)sharedInstance
{
    static dispatch_once_t once;
    static id sharedInstance;
    dispatch_once(&once, ^{
        sharedInstance = [[self alloc] init];
    });
    return sharedInstance;
}


////////////////////////////////////////////////////////////////////
#pragma mark - Jails functions
//////////////////////////////////////////////////////////////////// initJail
- (void) initJail:(NSString *) js {
    if([Status JSCEnabled]){
        if(_jail == nil) {
            _jail = [Jail new];
        }
        [_jail initJail:js];
    } else {
        InitJail((char *) [js UTF8String]);
    }
}

//////////////////////////////////////////////////////////////////// parseJail
-(NSString *) parseJail:(NSString *)chatId
                  js:(NSString *)js
{
    NSString *stringResult;
    if([Status JSCEnabled]){
        if(_jail == nil) {
            _jail = [Jail new];
        }
        NSDictionary *result = [_jail parseJail:chatId withCode:js];
        stringResult = [result bv_jsonStringWithPrettyPrint:NO];
    } else {
        char * result = Parse((char *) [chatId UTF8String], (char *) [js UTF8String]);
        stringResult = [NSString stringWithUTF8String: result];
    }
    
    return stringResult;
}

//////////////////////////////////////////////////////////////////// callJail
-(NSString *) callJail:(NSString *)chatId
                  path:(NSString *)path
                  params:(NSString *)params
{
        NSString *stringResult;
        if([Status JSCEnabled]){
            if(_jail == nil) {
                _jail = [Jail new];
            }
            NSDictionary *result = [_jail call:chatId path:path params:params];
            stringResult = [result bv_jsonStringWithPrettyPrint:NO];
        } else {
            char * result = Call((char *) [chatId UTF8String], (char *) [path UTF8String], (char *) [params UTF8String]);
            stringResult = [NSString stringWithUTF8String: result];
        }

        return stringResult;
}

////////////////////////////////////////////////////////////////////
#pragma mark - startNode
//////////////////////////////////////////////////////////////////// startNode
-(void)startNode
{
    signal(SIGPIPE, SIG_IGN);
    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSError *error = nil;
    NSURL *rootUrl =[[fileManager
                      URLsForDirectory:NSDocumentDirectory inDomains:NSUserDomainMask]
                     lastObject];
    NSURL *testnetFolderName = [rootUrl URLByAppendingPathComponent:@"ethereum/testnet"];
    
    if (![fileManager fileExistsAtPath:testnetFolderName.path])
        [fileManager createDirectoryAtPath:testnetFolderName.path withIntermediateDirectories:YES attributes:nil error:&error];
    
    NSURL *flagFolderUrl = [rootUrl URLByAppendingPathComponent:@"ropsten_flag"];
    
    if(![fileManager fileExistsAtPath:flagFolderUrl.path]){
        NSLog(@"remove lightchaindata");
        NSURL *lightChainData = [testnetFolderName URLByAppendingPathComponent:@"StatusIM/lightchaindata"];
        if([fileManager fileExistsAtPath:lightChainData.path]) {
            [fileManager removeItemAtPath:lightChainData.path
                                    error:nil];
        }
        [fileManager createDirectoryAtPath:flagFolderUrl.path
               withIntermediateDirectories:NO
                                attributes:nil
                                     error:&error];
    }
    
    NSLog(@"after remove lightchaindata");
    
    NSURL *oldKeystoreUrl = [testnetFolderName URLByAppendingPathComponent:@"keystore"];
    NSURL *newKeystoreUrl = [rootUrl URLByAppendingPathComponent:@"keystore"];
    if([fileManager fileExistsAtPath:oldKeystoreUrl.path]){
        NSLog(@"copy keystore");
        [fileManager copyItemAtPath:oldKeystoreUrl.path toPath:newKeystoreUrl.path error:nil];
        [fileManager removeItemAtPath:oldKeystoreUrl.path error:nil];
    }
    
    NSLog(@"after lightChainData");

    NSString *configString = @"{\"NetworkId\": 3, \"DataDir\": \"/ethereum/testnet_rpc\",\"UpstreamConfig\": {\"URL\": \"https://ropsten.infura.io/z6GCTmjdP3FETEJmMBI4\"}  }";

    NSLog(@"preconfig: %@", configString);
    NSData *configData = [configString dataUsingEncoding:NSUTF8StringEncoding];
    NSDictionary *configJSON = [NSJSONSerialization JSONObjectWithData:configData options:NSJSONReadingMutableContainers error:nil];
    int networkId = [configJSON[@"NetworkId"] integerValue];
    NSString *dataDir = [configJSON objectForKey:@"DataDir"];
    NSString *upstreamURL = [configJSON valueForKeyPath:@"UpstreamConfig.URL"];
    NSString *networkDir = [rootUrl.path stringByAppendingString:dataDir];
    NSString *devCluster = @"0"; // [ReactNativeConfig envFor:@"ETHEREUM_DEV_CLUSTER"];
    NSString *logLevel = @"DEBUG";// [[ReactNativeConfig envFor:@"LOG_LEVEL_STATUS_GO"] uppercaseString];
    int dev = 0;
    if([devCluster isEqualToString:@"1"]){
        dev = 1;
    }
    char *configChars = GenerateConfig((char *)[networkDir UTF8String], networkId, dev);
    NSString *config = [NSString stringWithUTF8String: configChars];
    configData = [config dataUsingEncoding:NSUTF8StringEncoding];
    NSDictionary *resultingConfigJson = [NSJSONSerialization JSONObjectWithData:configData options:NSJSONReadingMutableContainers error:nil];
    NSURL *networkDirUrl = [NSURL fileURLWithPath:networkDir];
    NSURL *logUrl = [networkDirUrl URLByAppendingPathComponent:@"geth.log"];
    [resultingConfigJson setValue:newKeystoreUrl.path forKey:@"KeyStoreDir"];
    [resultingConfigJson setValue:[NSNumber numberWithBool:[logLevel length] != 0] forKey:@"LogEnabled"];
    [resultingConfigJson setValue:logUrl.path forKey:@"LogFile"];
    [resultingConfigJson setValue:([logLevel length] == 0 ? [NSString stringWithUTF8String: "ERROR"] : logLevel) forKey:@"LogLevel"];
    
    if(upstreamURL != nil) {
        [resultingConfigJson setValue:[NSNumber numberWithBool:YES] forKeyPath:@"UpstreamConfig.Enabled"];
        [resultingConfigJson setValue:upstreamURL forKeyPath:@"UpstreamConfig.URL"];
    }
    NSString *resultingConfig = [resultingConfigJson bv_jsonStringWithPrettyPrint:NO];

    if(![fileManager fileExistsAtPath:networkDirUrl.path]) {
        [fileManager createDirectoryAtPath:networkDirUrl.path withIntermediateDirectories:YES attributes:nil error:nil];
    }
    
    NSLog(@"logUrlPath %@", logUrl.path);
    if(![fileManager fileExistsAtPath:logUrl.path]) {
        NSMutableDictionary *dict = [[NSMutableDictionary alloc] init];
        [dict setObject:[NSNumber numberWithInt:511] forKey:NSFilePosixPermissions];
        [fileManager createFileAtPath:logUrl.path contents:nil attributes:dict];
    }

    char *res = StartNode((char *) [resultingConfig UTF8String]);
    NSLog(@"StartNode result %@", [NSString stringWithUTF8String: res]);
}

////////////////////////////////////////////////////////////////////
#pragma mark - StopNode method
//////////////////////////////////////////////////////////////////// StopNode
-(void) stopNode {
#if DEBUG
    NSLog(@"StopNode() method called");
#endif

    char *res = StopNode();
}

////////////////////////////////////////////////////////////////////
#pragma mark - Accounts method
//////////////////////////////////////////////////////////////////// createAccount
-(NSString *)createAccount:(NSString *)password
{
#if DEBUG
    NSLog(@"CreateAccount() method called");
#endif
    char * result = CreateAccount((char *) [password UTF8String]);
    return [NSString stringWithUTF8String: result];
}

////////////////////////////////////////////////////////////////////
#pragma mark - Notify method
//////////////////////////////////////////////////////////////////// notify
-(NSString *)notify:(NSString *)token
{
#if DEBUG
    NSLog(@"Notify() method called");
#endif
    char * result = Notify((char *) [token UTF8String]);
    return [NSString stringWithUTF8String: result];
}

//////////////////////////////////////////////////////////////////// addPeer
-(NSString *)addPeer:(NSString *)enode
{
#if DEBUG
    NSLog(@"AddPeer() method called");
#endif
  char * result = AddPeer((char *) [enode UTF8String]);
  return [NSString stringWithUTF8String: result];
}

//////////////////////////////////////////////////////////////////// recoverAccount
-(NSString *)recoverAccount:(NSString *)passphrase
                   password:(NSString *)password
{
#if DEBUG
    NSLog(@"RecoverAccount() method called");
#endif
    char * result = RecoverAccount((char *) [password UTF8String], (char *) [passphrase UTF8String]);
    return [NSString stringWithUTF8String: result];
}

//////////////////////////////////////////////////////////////////// login
-(NSString *)login:(NSString *)address
                  password:(NSString *)password
{
#if DEBUG
    NSLog(@"Login() method called");
#endif
    if(_jail != nil) {
        [_jail reset];
    }
    char * result = Login((char *) [address UTF8String], (char *) [password UTF8String]);
    return [NSString stringWithUTF8String: result];
}

////////////////////////////////////////////////////////////////////
#pragma mark - Complete Transactions
//////////////////////////////////////////////////////////////////// completeTransactions
-(NSString *)completeTransactions:(NSString *)hashes
                  password:(NSString *)password
{
#if DEBUG
    NSLog(@"CompleteTransactions() method called");
#endif
    char * result = CompleteTransactions((char *) [hashes UTF8String], (char *) [password UTF8String]);
    return [NSString stringWithUTF8String: result];
}

////////////////////////////////////////////////////////////////////
#pragma mark - Discard Transaction
//////////////////////////////////////////////////////////////////// discardTransaction
-(void)discardTransaction:(NSString *)id
{
#if DEBUG
    NSLog(@"DiscardTransaction() method called");
#endif
    DiscardTransaction((char *) [id UTF8String]);
}

-(void) clearCookies
{
    NSHTTPCookie *cookie;
    NSHTTPCookieStorage *storage = [NSHTTPCookieStorage sharedHTTPCookieStorage];
    for (cookie in [storage cookies]) {
        [storage deleteCookie:cookie];
    }
}

-(void)clearStorageAPIs
{
    [[NSURLCache sharedURLCache] removeAllCachedResponses];
    
    NSString *path = [NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES) lastObject];
    NSArray *array = [[NSFileManager defaultManager] contentsOfDirectoryAtPath:path error:nil];
    for (NSString *string in array) {
        NSLog(@"Removing %@", [path stringByAppendingPathComponent:string]);
        if ([[string pathExtension] isEqualToString:@"localstorage"])
            [[NSFileManager defaultManager] removeItemAtPath:[path stringByAppendingPathComponent:string] error:nil];
    }
}

-(NSString *) sendWeb3Request:(NSString *)payload
{
    char * result = CallRPC((char *) [payload UTF8String]);
    return [NSString stringWithUTF8String: result];
}

+ (void)signalEvent:(const char *) signal
{
    if(!signal){
#if DEBUG
        NSLog(@"SignalEvent nil");
#endif
        return;
    }
    
    NSString *sig = [NSString stringWithUTF8String:signal];
#if DEBUG
    NSLog(@"SignalEvent");
    NSLog(sig);
#endif
    OnSignalEvent ose = [Status sharedInstance].onSignalEvent;
    if (ose != nil) {
        ose(sig);
    }
    // [bridge.eventDispatcher sendAppEventWithName:@"gethEvent"
    //                                        body:@{@"jsonEvent": sig}];
    
    return;
}

+ (void)jailEvent:(NSString *)chatId
             data:(NSString *)data
{
    NSData *signalData = [@"{}" dataUsingEncoding:NSUTF8StringEncoding];
    NSDictionary *dict = [NSJSONSerialization JSONObjectWithData:signalData options:NSJSONReadingMutableContainers error:nil];
    [dict setValue:@"jail.signal" forKey:@"type"];
    NSDictionary *event = [[NSDictionary alloc] initWithObjectsAndKeys:chatId, @"chat_id", data, @"data", nil];
    [dict setValue:event forKey:@"event"];
    NSString *signal = [dict bv_jsonStringWithPrettyPrint:NO];
#if DEBUG
    NSLog(@"SignalEventData");
    NSLog(signal);
#endif
    //[bridge.eventDispatcher sendAppEventWithName:@"gethEvent"
    //                                        body:@{@"jsonEvent": signal}];
    
    return;
}

@end
