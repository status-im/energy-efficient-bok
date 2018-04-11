#### Date: 2018-04-11

#### Test goal
To measure and compare battery performance in 1-1 background messaging scenario for Telegram and Status apps.

#### Test overview
Telegram and Status apps were tested separately. Message notifications were silent (without sound or vibration). The test started after creating 1-1 chat and putting the app into background. 100 messages (from number "1" to "100") were received. Both tests took ~3.5 min

#### Builds description
- Telegram v4.8.5
- Status nightly, April 11, 77e6f4

#### Results
Status consumed 3x battery (total battery consumed) and 10x more network (total data transferred) than Telegram for the same scenario.

Notes:
- After the test, when Status was opened the chat did not contain any message. The messages started to appear after some time
- Many of messages in Status were not delivered at all
- Some of the delivered messages in Status had wrong order