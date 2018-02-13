#import "ViewController.h"
#import <Statusgo/Statusgo.h>
#import "RCTStatus.h"

static NSString *const kAccountIdKey = @"ACCOUNT_ID";
static NSString *const kChatroomName = @"humans-need-not-apply";
static NSString *const kUsername = @"Xcodified Tiny Rabbit";


@implementation ViewController {
    Status *_status;
    dispatch_source_t _timer;
    UILabel *_currentMessageLabel;
}

- (NSString *)storedAccountID
{
    return [[NSUserDefaults standardUserDefaults] stringForKey:kAccountIdKey];
}

- (void)storeAccountID:(NSString *)accountID
{
    [[NSUserDefaults standardUserDefaults] setObject:accountID forKey:kAccountIdKey];
}

- (NSDictionary *)callWeb3AndParseResult:(NSString *)cmd {
    NSString *resultString = [[Status sharedInstance] sendWeb3Request:cmd];
    NSDictionary *resultJSON = [NSJSONSerialization JSONObjectWithData:[resultString dataUsingEncoding:NSUTF8StringEncoding]
                                                               options:NSJSONReadingMutableContainers
                                                                 error:nil];
    return resultJSON;
}

- (NSData *)dataFromHexString:(NSString *)originalHexString
{
    NSString *hexString = [originalHexString stringByReplacingOccurrencesOfString:@"[ <>]" withString:@"" options:NSRegularExpressionSearch range:NSMakeRange(0, [originalHexString length])]; // strip out spaces (between every four bytes), "<" (at the start) and ">" (at the end)
    if ([hexString hasPrefix:@"0x"]) {
        hexString = [hexString substringFromIndex:2];
    }
    NSMutableData *data = [NSMutableData dataWithCapacity:[hexString length] / 2];
    for (NSInteger i = 0; i < [hexString length]; i += 2)
    {
        NSString *hexChar = [hexString substringWithRange: NSMakeRange(i, 2)];
        int value;
        sscanf([hexChar cStringUsingEncoding:NSASCIIStringEncoding], "%x", &value);
        uint8_t byte = value;
        [data appendBytes:&byte length:1];
    }

    return data;
}

- (NSString *)decodedMessageContent:(NSString *)encodedContent
{
    return [[NSString alloc] initWithData:[self dataFromHexString:encodedContent] encoding:NSUTF8StringEncoding];
}

- (NSString *)encodedMessageContent:(NSString *)message
{
    NSString *messageID = [[NSUUID UUID] UUIDString];
    NSString *timestamp = [NSString stringWithFormat:@"%ld", (long)[NSNumber numberWithDouble:[[NSDate date] timeIntervalSince1970]].integerValue];
    NSString *fullMessage = [NSString stringWithFormat:@"{:message-id \"%@\", :group-id \"%@\", :content \"%@\", :username \"%@\", :type :public-group-message, :show? true, :clock-value 1, :requires-ack? false, :content-type \"text/plain\", :timestamp %@}",
                             messageID, kChatroomName, message, kUsername, timestamp];

    NSString *hexString = [[fullMessage dataUsingEncoding:NSUTF8StringEncoding] description];
    hexString = [hexString stringByReplacingOccurrencesOfString:@"[ <>]"
                                                     withString:@""
                                                        options:NSRegularExpressionSearch
                                                          range:NSMakeRange(0, [hexString length])]; // strip out spaces (between every four bytes), "<" (at the start) and ">" (at the end)


    return [NSString stringWithFormat:@"0x%@", hexString];
}

- (void)viewDidLoad {
    [super viewDidLoad];

    _currentMessageLabel = [UILabel new];
    _currentMessageLabel.numberOfLines = 0;
    _currentMessageLabel.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
    _currentMessageLabel.frame = self.view.bounds;
    [self.view addSubview:_currentMessageLabel];

    [Status sharedInstance].onSignalEvent = ^(NSString *sig) {
        if ([sig hasPrefix:@"{\"type\":\"node.ready\""]) {
            NSLog(@"NODE READY!");
            NSString *accountId = [self storedAccountID];
            if ([accountId length] == 0) {
                NSString *accountString = [[Status sharedInstance] createAccount:@"my-awesome-test-password"];
                NSDictionary *accountJSON = [NSJSONSerialization JSONObjectWithData:[accountString dataUsingEncoding:NSUTF8StringEncoding]
                                                                           options:NSJSONReadingMutableContainers
                                                                             error:nil];
                [self storeAccountID:accountJSON[@"address"]];
            }
            NSLog(@"Logging in: %@",
                  [[Status sharedInstance] login:[self storedAccountID]
                                        password:@"my-awesome-test-password"]);

            NSLog(@"Joining #%@...", kChatroomName);

            NSDictionary *result = [self callWeb3AndParseResult:
                                    [NSString stringWithFormat:@"{\"jsonrpc\":\"2.0\",\"id\":2950,\"method\":\"shh_generateSymKeyFromPassword\",\"params\":[\"%@\"]}", kChatroomName]];


            NSString *key = result[@"result"];
            NSString *cmd = [NSString stringWithFormat:@"{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":\"shh_newMessageFilter\",\"params\":[{\"allowP2P\":true,\"topics\":[\"0xaabb11ee\"],\"type\":\"sym\",\"symKeyID\":\"%@\"}]}", key];

            NSLog(@"Listening to #%@...", kChatroomName);
            result = [self callWeb3AndParseResult:cmd];
            NSString *filterID = result[@"result"];

            // NORMAL
            cmd = [NSString stringWithFormat:@"{\"jsonrpc\":\"2.0\",\"id\":2968,\"method\":\"shh_getFilterMessages\",\"params\":[\"%@\"]}", filterID];

            _timer = dispatch_source_create(DISPATCH_SOURCE_TYPE_TIMER, 0, 0, dispatch_get_main_queue());

            __block NSInteger a = 0;
            if (_timer)
            {
                dispatch_source_set_timer(_timer, DISPATCH_TIME_NOW, 1 * NSEC_PER_SEC, (1ull * NSEC_PER_SEC) / 10);
                dispatch_source_set_event_handler(_timer, ^{
                    NSDictionary *result = [self callWeb3AndParseResult:cmd];
                    if ([result[@"result"] count] > 0) {
                        for (NSDictionary *msg in result[@"result"]) {
                            NSString *decodedMsg = [self decodedMessageContent:msg[@"payload"]];
                            NSLog(@"Reading #%@: %@", kChatroomName, decodedMsg);
                            _currentMessageLabel.text = [NSString stringWithFormat:@"%@ -> %@", [NSDate date], decodedMsg];
                        }
                    }

                    /*
                    // SEND MESSAGE
                    if (a % 10 == 0) {
                        NSString *message = [NSString stringWithFormat:@"Xcode, timestamp: %@", [NSNumber numberWithDouble:[[NSDate date] timeIntervalSince1970]].stringValue];

                        NSString *cmd = [NSString stringWithFormat:@"{\"jsonrpc\":\"2.0\",\"id\":%d,\"method\":\"shh_post\",\"params\":[{\"from\":\"0x42aE6cb59675e43a0069593a6EcA040299955723\",\"topic\":\"0xaabb11ee\",\"payload\":\"%@\",\"symKeyID\":\"%@\",\"sym-key-password\":\"status\",\"ttl\":2400,\"powTarget\":0.001,\"powTime\":1}]}",[NSNumber numberWithDouble:[[NSDate date] timeIntervalSince1970]].intValue, [self encodedMessageContent:message], key];
                        NSLog(@"****** -> %@", cmd);

                        [self callWeb3AndParseResult:cmd];
                    }
                     */
                    a++;
                });
                dispatch_resume(_timer);
            }


        }
    };

    [[Status sharedInstance] startNode];
}

@end
