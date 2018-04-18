package status.im.status_go_tester;

import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;

import com.thetransactioncompany.jsonrpc2.JSONRPC2ParseException;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import im.status.ethereum.module.ConnectorHandler;
import im.status.ethereum.module.ServiceConnector;
import im.status.ethereum.module.StatusModule;
import im.status.ethereum.module.StatusService;

public class MainActivity extends AppCompatActivity {

    private StatusModule statusModule;
    private ServiceConnector serviceConnector;

    private TextView labelMessageText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        labelMessageText = findViewById(R.id.label_message_text);
        labelMessageText.setMovementMethod(new ScrollingMovementMethod());


        serviceConnector = new ServiceConnector(this, StatusService.class);
        serviceConnector.registerHandler(new ConnectorHandler() {
            @Override
            public boolean handleMessage(Message message) {
                String event = message.getData().getString("event", "");
                Log.d("STATUS-TESTER/MainActivity", "Got message" + event);
                if (event.contains("node.ready")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.this.setupChat();
                        }
                    });
                }
                return true;
            }

            @Override
            public void onConnectorConnected() {
                Log.d("STATUS-TESTER/MainActivity", "onConnectorConnected");

                serviceConnector.sendMessage();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String config = readNodeConfig();

                        statusModule.startNode(config);

                        String response = statusModule.createAccount("my-cool-password");

                        Log.d("STATUS-TESTER account created?", response);
                    }
                });

            }

            @Override
            public void onConnectorDisconnected() {
                Log.d("STATUS-TESTER/MainActivity", "onConnectorDisconnected");
            }
        });

        statusModule = new StatusModule(this, true, false, false, "debug");

    }

    private void setupChat() {
        String password = "my-cool-password";
        String response = statusModule.createAccount(password);
        if (noError(statusModule.login(accountAddressFromResponse(response), password))) {
            try {
                createChatSpectator();
            } catch (Exception e) {
                Log.d("STATUS-TESTER/setupChat/Error", e.getMessage());
            }
        }
    }

    private void createChatSpectator() throws JSONRPC2ParseException {
        String chatRoom = "humans-need-not-apply";
        List<Object> ls = new ArrayList<Object>();
        ls.add(chatRoom);

        JSONRPC2Request reqOut =
                new JSONRPC2Request(
                        "shh_generateSymKeyFromPassword", ls, 11);


        String jsonString = statusModule.sendWeb3Request(reqOut.toJSONString());

        JSONRPC2Response respIn = JSONRPC2Response.parse(jsonString);
        String key = (String) respIn.getResult();

        List<Object>fpl = new ArrayList<>();
        fpl.add(getTopicFromChannelName(chatRoom));


        Map<String, Object> fp = new HashMap<>();
        fp.put("allowP2P", true);
        fp.put("topics", fpl);
        fp.put("type", "sym");
        fp.put("symKeyID", key);

        List<Object> fpContainer = new ArrayList<>();
        fpContainer.add(fp);

        reqOut =
                new JSONRPC2Request(
                        "shh_newMessageFilter", fpContainer, 12);

        jsonString = statusModule.sendWeb3Request(reqOut.toJSONString());

        respIn = JSONRPC2Response.parse(jsonString);
        String filterID = (String) respIn.getResult();


        List<Object> fs = new ArrayList<>();
        fs.add(filterID);

        final JSONRPC2Request getFilterMessages =
                new JSONRPC2Request(
                        "shh_getFilterMessages", fs, 11);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        String jsonString = statusModule.sendWeb3Request(getFilterMessages.toJSONString());

                        JSONRPC2Response respIn = JSONRPC2Response.parse(jsonString);
                        List messages = (List) respIn.getResult();
                        if (messages.isEmpty()) {
                            Thread.sleep(100);
                            continue;
                        }

                        for (Object message : messages) {
                            String payload = (String) ((Map) message).get("payload");
                            final String decryptedPayload = hexToString(payload);
                            Log.d("STATUS-TESTER/blah", decryptedPayload);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    MainActivity.this.updateLabel(decryptedPayload);
                                }
                            });
                        }
                    } catch (JSONRPC2ParseException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();

                /*
                    // SEND MESSAGE
                    if (a % 10 == 0) {
                        NSString *message = [NSString stringWithFormat:@"Xcode, timestamp: %@", [NSNumber numberWithDouble:[[NSDate date] timeIntervalSince1970]].stringValue];

                        NSString *cmd = [NSString stringWithFormat:@"{\"jsonrpc\":\"2.0\",\"id\":%d,\"method\":\"shh_post\",\"params\":[{\"from\":\"0x42aE6cb59675e43a0069593a6EcA040299955723\",\"topic\":\"0xaabb11ee\",\"payload\":\"%@\",\"symKeyID\":\"%@\",\"sym-key-password\":\"status\",\"ttl\":2400,\"powTarget\":0.001,\"powTime\":1}]}",[NSNumber numberWithDouble:[[NSDate date] timeIntervalSince1970]].intValue, [self encodedMessageContent:message], key];
                        NSLog(@"****** -> %@", cmd);

                        [self callWeb3AndParseResult:cmd];
                    }
                    */
    }

    private Object getTopicFromChannelName(String chatRoom) throws JSONRPC2ParseException {
        byte[] bytes = chatRoom.getBytes(Charset.forName("UTF-8"));
        StringBuilder byteStringBuilder = new StringBuilder();
        for (byte b: bytes) {
            byteStringBuilder.append(String.format("%02x", b));
        }

        String byteString = byteStringBuilder.toString();
        byteString = "0x" + byteString;

        List<Object> params = new ArrayList<>();
        params.add(byteString);

        JSONRPC2Request reqOut =
                new JSONRPC2Request(
                        "web3_sha3", params, 12);


        String jsonString = statusModule.sendWeb3Request(reqOut.toJSONString());

        JSONRPC2Response respIn = JSONRPC2Response.parse(jsonString);
        String topicSha3 = (String) respIn.getResult();

        return topicSha3.substring(0, 10);
    }

    private void updateLabel(String decryptedPayload) {
        String text = decryptedPayload + "\n\n--\n\n" + labelMessageText.getText();
        labelMessageText.setText(text);
    }

    private String hexToString(String payload) {
        byte[] byteArray = hexToByteArray(payload);
        return new String(byteArray, Charset.forName("UTF-8"));
    }

    private byte[] hexToByteArray(String payload) {
        if (payload.startsWith("0x")) {
            payload = payload.substring(2);
        }
        List<Byte> bytes = new ArrayList<>();
        while(payload.length() > 0) {
            int endIndex = Math.min(2, payload.length());
            String currentByteString = payload.substring(0, endIndex);
            payload = payload.substring(2);

            Integer intValue = Integer.valueOf(currentByteString, 16);
            bytes.add(intValue.byteValue());
        }

        byte[] result = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++) {
            result[i] = bytes.get(i);
        }
        return result;
    }

    private boolean noError(String login) {
        try {
            JSONObject object = new JSONObject(login);
            if (object.getString("error").length() == 0) {
                return true;
            } else {
                Log.d("STATUS-TESTER/noError/error", login);
                return false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String accountAddressFromResponse(String response) {
        Log.d("STATUS-TESTER/setupChat", response);
        try {
            JSONObject object = new JSONObject(response);
            return object.getString("address");
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        boolean result = serviceConnector.bindService();
        Log.d("STATUS-TESTER/MainActivity", "Was service bound?" + (result ? "YES" : "NO"));

    }

    protected void onStop() {
        super.onStop();
        serviceConnector.unbindService();
    };

    private String readNodeConfig() {

        InputStream configStream = getResources().openRawResource(R.raw.node_config);
        // https://stackoverflow.com/questions/309424/read-convert-an-inputstream-to-a-string
        Scanner configScanner = new Scanner(configStream).useDelimiter("\\A");

        String configFormat = configScanner.hasNext() ? configScanner.next() : "";

        String cachePath = getCacheDir().getAbsolutePath();

        return String.format(configFormat, cachePath, cachePath, cachePath, cachePath);
    }
}

