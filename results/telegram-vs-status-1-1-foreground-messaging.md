#### Date: 2018-04-11

#### Test goal
To measure and compare battery performance in 1-1 foreground messaging scenario for Telegram and Status apps.

#### Test overview
Telegram and Status apps were tested separately. 100 messages (from number "1" to "100") were received and then another 100 messages were sent. The app under test was in the foreground for all the time. Both tests took ~4.5 min

#### Builds description
- Telegram v4.8.5
- Status nightly, April 11, 77e6f4

#### Results
Status consumed 4x battery (total battery consumed) and 6.7x more network (total data transferred) than Telegram for the same scenario.

Notes:
- Many of messages in Status were not delivered at all
- Some of the delivered messages in Status had wrong order
- The latency was noticeable whereas Telegram messages were received instantly