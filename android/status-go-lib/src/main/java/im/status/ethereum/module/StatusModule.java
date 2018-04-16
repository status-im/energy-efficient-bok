package im.status.ethereum.module;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.*;
import android.view.WindowManager;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebStorage;

import com.github.status_im.status_go.Statusgo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONObject;
import org.json.JSONException;

public class StatusModule implements ConnectorHandler {

    private static final String TAG = "STATUS-TESTER/StatusModule";

    private boolean debug;
    private boolean devCluster;
    private String logLevel;
    private Jail jail;
    private Activity activity;

    public StatusModule(Activity activity, boolean debug, boolean devCluster, boolean jscEnabled, String logLevel) {
        this.activity = activity;
        this.debug = debug;
        this.devCluster = devCluster;
        this.logLevel = logLevel;
        jail = new OttoJail();
    }

    private Activity getCurrentActivity() {
        return this.activity;
    }

    private Boolean checkAvailability() {
        return this.activity != null;
    }

    private void signalEvent(String jsonEvent) {
        Log.d("signaling", jsonEvent);
    }


    private String prepareLogsFile() {
        String gethLogFileName = "geth.log";
        File pubDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File logFile = new File(pubDirectory, gethLogFileName);

        try {
            logFile.setReadable(true);
            File parent = logFile.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }
            logFile.createNewFile();
            logFile.setReadable(true);
            Uri gethLogUri = Uri.fromFile(logFile);
            try {
                Log.d(TAG, "Attach to geth.log to instabug " + gethLogUri.getPath());
            } catch (NullPointerException e) {
                Log.d(TAG, "Instabug is not initialized!");
            }

            String gethLogFilePath = logFile.getAbsolutePath();
            Log.d("ExtDirLog", gethLogFilePath);

            return gethLogFilePath;
        } catch (Exception e) {
            Log.d(TAG, "Can't create geth.log file! " + e.getMessage());
        }

        return null;
    }

    private void doStartNode(final String defaultConfig) {

        Activity currentActivity = getCurrentActivity();

        String root = currentActivity.getApplicationInfo().dataDir;
        String dataFolder = root + "/ethereum/testnet";
        Log.d(TAG, "Starting Geth node in folder: " + dataFolder);

        try {
            final File newFile = new File(dataFolder);
            // todo handle error?
            newFile.mkdir();
        } catch (Exception e) {
            Log.e(TAG, "error making folder: " + dataFolder, e);
        }

        final String ropstenFlagPath = root + "/ropsten_flag";
        final File ropstenFlag = new File(ropstenFlagPath);
        if (!ropstenFlag.exists()) {
            try {
                final String chaindDataFolderPath = dataFolder + "/StatusIM/lightchaindata";
                final File lightChainFolder = new File(chaindDataFolderPath);
                if (lightChainFolder.isDirectory()) {
                    String[] children = lightChainFolder.list();
                    for (int i = 0; i < children.length; i++) {
                        new File(lightChainFolder, children[i]).delete();
                    }
                }
                lightChainFolder.delete();
                ropstenFlag.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String testnetDataDir = root + "/ethereum/testnet";
        String oldKeystoreDir = testnetDataDir + "/keystore";
        String newKeystoreDir = root + "/keystore";
        final File oldKeystore = new File(oldKeystoreDir);
        if (oldKeystore.exists()) {
            try {
                final File newKeystore = new File(newKeystoreDir);
                copyDirectory(oldKeystore, newKeystore);

                if (oldKeystore.isDirectory()) {
                    String[] children = oldKeystore.list();
                    for (int i = 0; i < children.length; i++) {
                        new File(oldKeystoreDir, children[i]).delete();
                    }
                }
                oldKeystore.delete();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        int dev = 0;
        if (this.devCluster) {
            dev = 1;
        }

        String config = Statusgo.GenerateConfig(testnetDataDir, 3, dev);
        try {
            JSONObject customConfig = new JSONObject(defaultConfig);
            JSONObject jsonConfig = new JSONObject(config);

            String gethLogFilePath = prepareLogsFile();
            boolean logsEnabled = (gethLogFilePath != null) && !TextUtils.isEmpty(this.logLevel);
            String dataDir = root + customConfig.get("DataDir");
            jsonConfig.put("LogEnabled", logsEnabled);
            jsonConfig.put("LogFile", gethLogFilePath);
            jsonConfig.put("LogLevel", TextUtils.isEmpty(this.logLevel) ? "ERROR" : this.logLevel.toUpperCase());
            jsonConfig.put("DataDir", dataDir);
            jsonConfig.put("NetworkId", customConfig.get("NetworkId"));
            try {
                Object upstreamConfig = customConfig.get("UpstreamConfig");
                if (upstreamConfig != null) {
                    Log.d(TAG, "UpstreamConfig is not null");
                    jsonConfig.put("UpstreamConfig", upstreamConfig);
                }
            } catch (Exception e) {

            }
            try {
                JSONObject whisperConfig = (JSONObject) jsonConfig.get("WhisperConfig");
                if (whisperConfig == null) {
                    whisperConfig = new JSONObject();
                }
                whisperConfig.put("LightClient", true);
                jsonConfig.put("WhisperConfig", whisperConfig);
            } catch (Exception e) {

            }
            jsonConfig.put("KeyStoreDir", newKeystoreDir);

            config = jsonConfig.toString();
        } catch (JSONException e) {
            Log.d(TAG, "Something went wrong " + e.getMessage());
            Log.d(TAG, "Default configuration will be used");
        }

        String configOutput = config;
        final int maxOutputLen = 4000;
        while (!configOutput.isEmpty()) {
            Log.d(TAG, "Node config:" + configOutput.substring(0, Math.min(maxOutputLen, configOutput.length())));
            if (configOutput.length() > maxOutputLen) {
                configOutput = configOutput.substring(maxOutputLen);
            } else {
                break;
            }
        }

        String res = Statusgo.StartNode(config);
        if (res.startsWith("{\"error\":\"\"")) {
            Log.d(TAG, "StartNode result: " + res);
        }
        else {
            Log.e(TAG, "StartNode failed: " + res);
        }
        Log.d(TAG, "Geth node started");
    }

    private String getOldExternalDir() {
        File extStore = Environment.getExternalStorageDirectory();
        return extStore.exists() ? extStore.getAbsolutePath() + "/ethereum/testnet" : getNewInternalDir();
    }

    private String getNewInternalDir() {
        Activity currentActivity = getCurrentActivity();
        return currentActivity.getApplicationInfo().dataDir + "/ethereum/testnet";
    }

    private void deleteDirectory(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteDirectory(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }

    private void copyDirectory(File sourceLocation, File targetLocation) throws IOException {
        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists() && !targetLocation.mkdirs()) {
                throw new IOException("Cannot create dir " + targetLocation.getAbsolutePath());
            }

            String[] children = sourceLocation.list();
            for (int i = 0; i < children.length; i++) {
                copyDirectory(new File(sourceLocation, children[i]), new File(targetLocation, children[i]));
            }
        } else {
            File directory = targetLocation.getParentFile();
            if (directory != null && !directory.exists() && !directory.mkdirs()) {
                throw new IOException("Cannot create dir " + directory.getAbsolutePath());
            }

            InputStream in = new FileInputStream(sourceLocation);
            OutputStream out = new FileOutputStream(targetLocation);

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }
    }

    public void startNode(final String config) {
        Log.d(TAG, "startNode");
        if (!checkAvailability()) {
            return;
        }

        Thread thread = new Thread() {
            @Override
            public void run() {
                doStartNode(config);
            }
        };

        thread.start();
    }

    public void stopNode() {

        Thread thread = new Thread() {
            @Override
            public void run() {
                Log.d(TAG, "stopNode");
                String res = Statusgo.StopNode();
            }
        };

        thread.start();
    }

    public String login(final String address, final String password) {
        Log.d(TAG, "login");
        if (!checkAvailability()) {
            return "";
        }

        jail.reset();

        return Statusgo.Login(address, password);
    }

    public String createAccount(final String password) {
        Log.d(TAG, "createAccount");
        if (!checkAvailability()) {
            return "";
        }

        return Statusgo.CreateAccount(password);
    }

    public String addPeer(final String enode) {
        Log.d(TAG, "addPeer");
        if (!checkAvailability()) {
            return "";
        }

        return Statusgo.AddPeer(enode);
    }


    public String recoverAccount(final String passphrase, final String password)  {
        Log.d(TAG, "recoverAccount");
        if (!checkAvailability()) {
            return "";
        }
        return Statusgo.RecoverAccount(password, passphrase);
    }

    private String createIdentifier() {
        return UUID.randomUUID().toString();
    }

    public String completeTransactions(final String hashes, final String password) {
        Log.d(TAG, "completeTransactions");
        if (!checkAvailability()) {
            return "";
        }

        return Statusgo.ApproveSignRequests(hashes, password);
    }


    public String discardTransaction(final String id) {
        Log.d(TAG, "discardTransaction");
        if (!checkAvailability()) {
            return "";
        }
        return Statusgo.DiscardSignRequest(id);
    }

    // Jail

    public String initJail(final String js) {
        Log.d(TAG, "initJail");
        if (!checkAvailability()) {
            return "";
        }

        jail.initJail(js);
        return "";
    }

    public String parseJail(final String chatId, final String js) {
        Log.d(TAG, "parseJail chatId:" + chatId);
        //Log.d(TAG, js);
        if (!checkAvailability()) {
            return "";
        }

        return jail.parseJail(chatId, js);
    }

    public String callJail(final String chatId, final String path, final String params) {
        Log.d(TAG, "callJail");
        Log.d(TAG, path);
        if (!checkAvailability()) {
            return "";
        }
        
        Log.d(TAG, "startCallJail");
        return jail.callJail(chatId, path, params);
    }

    public void setAdjustResize() {
        Log.d(TAG, "setAdjustResize");
        final Activity activity = getCurrentActivity();
        if (activity == null) {
            return;
        }

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            }
        });
    }

    public void setAdjustPan() {
        Log.d(TAG, "setAdjustPan");
        final Activity activity = getCurrentActivity();
        if (activity == null) {
            return;
        }

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            }
        });
    }

    public void setSoftInputMode(final int mode) {
        Log.d(TAG, "setSoftInputMode");
        final Activity activity = getCurrentActivity();
        if (activity == null) {
            return;
        }

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.getWindow().setSoftInputMode(mode);
            }
        });
    }

    @SuppressWarnings("deprecation")
    public void clearCookies() {
        Log.d(TAG, "clearCookies");
        final Activity activity = getCurrentActivity();
        if (activity == null) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else {
            CookieSyncManager cookieSyncManager = CookieSyncManager.createInstance(activity);
            cookieSyncManager.startSync();
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncManager.stopSync();
            cookieSyncManager.sync();
        }
    }

    public void clearStorageAPIs() {
        Log.d(TAG, "clearStorageAPIs");
        final Activity activity = getCurrentActivity();
        if (activity == null) {
            return;
        }

        WebStorage storage = WebStorage.getInstance();
        if (storage != null) {
            storage.deleteAllData();
        }
    }

    @Override
    public boolean handleMessage(Message message) {

        Log.d(TAG, "Received message: " + message.toString());
        Bundle bundle = message.getData();

        String event = bundle.getString("event");
        signalEvent(event);

        return true;
    }

    @Override
    public void onConnectorConnected() {

    }

    @Override
    public void onConnectorDisconnected() {

    }

    public String sendWeb3Request(final String payload) {
        return Statusgo.CallRPC(payload);
        // TODO: check the same with threads.
        /*
        Thread thread = new Thread() {
            @Override
            public void run() {
                callback.invoke(res);
            }
        };

        thread.start();
        */
    }

    public void connectionChange(final String type, final boolean isExpensive) {
        Log.d(TAG, "ConnectionChange: " + type + ", is expensive " + isExpensive);
        Statusgo.ConnectionChange(type, isExpensive ? 1 : 0);
    }

    public void appStateChange(final String type) {
        Log.d(TAG, "AppStateChange: " + type);
        Statusgo.AppStateChange(type);
    }
}
