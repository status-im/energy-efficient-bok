# Energy consumption test results

## Add new
- [Click to add](https://github.com/mandrigin/status-go-n-wrapper/compare/master...report/example?quick_pull=1&template=test_result_pr_template.md&title=Energy%20consumption%20test%20result%20for%20build:%20{build%20name},%20os:%20{Android,%20iOS}) new result of a [manual energy consumption test](./QA.md)

## Manual test results

|Date|Build|Platform|Test session|Device|Conditions|Battery (total)*|Battery (CPU)*|Total data transferred*|Logs|
|---|---|---|---|---|---|--|--|--|--|
|2018-04-11|77e6f4 (2018-04-11 nightly)|Android|[1-1 messaging in background](results/telegram-vs-status-1-1-background-messaging.md)|Samsung Galaxy S8|wifi only|0.03%|0.04%|0.83MB|bugreport-2018-04-11-15-18-12-status.zip|
|2018-04-11|Telegram v4.8.5|Android|[1-1 messaging in background](results/telegram-vs-status-1-1-background-messaging.md)|Samsung Galaxy S8|wifi only|0.01%|0.00%|83KB|bugreport-2018-04-11-15-06-03-telegram.zip|
|2018-04-11|77e6f4 (2018-04-11 nightly)|Android|[1-1 messaging in foreground](results/telegram-vs-status-1-1-foreground-messaging.md)|Samsung Galaxy S8|wifi only, 50% brightness|0.4%|0.67%|2.43MB|bugreport-2018-04-11-15-49-45-status.zip|
|2018-04-11|Telegram v4.8.5|Android|[1-1 messaging in foreground](results/telegram-vs-status-1-1-foreground-messaging.md)|Samsung Galaxy S8|wifi only, 50% brightness|0.09%|0.04%|361.79KB|bugreport-2018-04-11-15-59-03-telegram.zip|
|2018-03-17|9533d1 (2018-03-17 nightly)|Android|Release testing (30 min)|Samsung Galaxy S8|wifi only, 50% screen brightness|0.97%|1.53%|43.42MB|bugreport-2018-03-17-11-32-14.zip|

----
**Battery (total)** - Device estimated power use

**Battery (CPU)** - Device estimated power use due to CPU usage

**Total data transferred** - Wifi data + Mobile data that was transferred

**PR** - Pull request ID with test report
