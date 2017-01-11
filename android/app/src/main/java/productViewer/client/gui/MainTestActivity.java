package productViewer.client.gui;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;

import productViewer.client.agent.IMobileAgent;
import productViewer.client.agent.MobileAgent;
import productViewer.client.model.Product;
import productViewer.client.model.ProductResponse;
import jade.android.AndroidHelper;
import jade.android.MicroRuntimeService;
import jade.android.MicroRuntimeServiceBinder;
import jade.android.RuntimeCallback;
import jade.core.MicroRuntime;
import jade.core.Profile;
import jade.util.Logger;
import jade.util.leap.Properties;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;


public class MainTestActivity extends Activity {
    private Logger logger = Logger.getJADELogger(this.getClass().getName());

    private MicroRuntimeServiceBinder microRuntimeServiceBinder;
    private ServiceConnection serviceConnection;

    private MyReceiver myReceiver;
    private MyHandler myHandler;

    private String nickname;
    private ProductAdapter adapter;
    private IMobileAgent mobileAgent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            myReceiver = new MyReceiver();

            IntentFilter productListFilter = new IntentFilter();
            productListFilter.addAction("jade.demo.chat.PRODUCT_LIST");
            registerReceiver(myReceiver, productListFilter);

            myHandler = new MyHandler();

            setContentView(R.layout.activity_main_test);

            Button button = (Button) findViewById(R.id.button_priceList);
            button.setOnClickListener(buttonPriceListListener);

            Button buttonStartAgent = (Button) findViewById(R.id.button_startAgent);
            buttonStartAgent.setOnClickListener(buttonStartAgentListener);

            ArrayList<Product> arrayOfUsers = new ArrayList<Product>();
            adapter = new ProductAdapter(this, arrayOfUsers);
            ListView listView = (ListView) findViewById(R.id.PriceListView);
            listView.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(myReceiver);

        logger.log(Level.INFO, "Destroy activity!");
    }

    private View.OnClickListener buttonPriceListListener = new View.OnClickListener() {
        public void onClick(View v) {

            try {
                mobileAgent = MicroRuntime.getAgent(nickname)
                        .getO2AInterface(IMobileAgent.class);
            } catch (ControllerException e) {
                e.printStackTrace();
            }
            if(mobileAgent == null)
            {
                return;
            }
            mobileAgent.requestFilename();
            }
    };

    private View.OnClickListener buttonStartAgentListener = new View.OnClickListener() {
        public void onClick(View v) {
            startAgent();
            Button buttonStartAgent = (Button) findViewById(R.id.button_startAgent);
            buttonStartAgent.setEnabled(false);

        }
    };

    private void startAgent()
    {
        nickname = "mobileAgent";
        try {
            SharedPreferences settings = getSharedPreferences(
                    "jadeChatPrefsFile", 0);
            String host = settings.getString("defaultHost", "");
            String port = settings.getString("defaultPort", "");
            startAgent(nickname, host, port, agentStartupCallback);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Unexpected exception creating chat agent!");
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    private RuntimeCallback<AgentController> agentStartupCallback = new RuntimeCallback<AgentController>() {
        @Override
        public void onSuccess(AgentController agent) {
        }

        @Override
        public void onFailure(Throwable throwable) {
            myHandler.postError(getString(R.string.msg_agent_in_use));
            microRuntimeServiceBinder.stopAgentContainer(agentStopCallback);
        }
    };

    private RuntimeCallback<Void> agentStopCallback = new RuntimeCallback<Void>() {
        @Override
        public void onSuccess(Void aVoid) {

        }

        @Override
        public void onFailure(Throwable throwable) {

        }
    };

    public void ShowDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainTestActivity.this);
        builder.setMessage(message).setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            ObjectMapper mapper = new ObjectMapper();
            logger.log(Level.INFO, "Received intent " + action);
            if (action.equalsIgnoreCase("jade.demo.chat.PRODUCT_LIST")) {
                try {
                    adapter.clear();
                    ProductResponse response = mapper.readValue(intent.getStringExtra("productResponse"),ProductResponse.class);
                    for(Product product : response.getItems())
                    {
                        adapter.add(product);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            if (bundle.containsKey("error")) {
                String message = bundle.getString("error");
                ShowDialog(message);
            }
        }

        public void postError(String error) {
            Message msg = obtainMessage();
            Bundle b = new Bundle();
            b.putString("error", error);
            msg.setData(b);
            sendMessage(msg);
        }
    }

    public void startAgent(final String nickname, final String host,
                          final String port,
                          final RuntimeCallback<AgentController> agentStartupCallback) {

        final Properties profile = new Properties();
        profile.setProperty(Profile.MAIN_HOST, host);
        profile.setProperty(Profile.MAIN_PORT, port);
        profile.setProperty(Profile.MAIN, Boolean.FALSE.toString());
        profile.setProperty(Profile.JVM, Profile.ANDROID);

        if (AndroidHelper.isEmulator()) {
            // Emulator: this is needed to work with emulated devices
            profile.setProperty(Profile.LOCAL_HOST, AndroidHelper.LOOPBACK);
        } else {
            profile.setProperty(Profile.LOCAL_HOST,
                    AndroidHelper.getLocalIPAddress());
        }
        // Emulator: this is not really needed on a real device
        profile.setProperty(Profile.LOCAL_PORT, "2000");

        if (microRuntimeServiceBinder == null) {
            serviceConnection = new ServiceConnection() {
                public void onServiceConnected(ComponentName className,
                                               IBinder service) {
                    microRuntimeServiceBinder = (MicroRuntimeServiceBinder) service;
                    logger.log(Level.INFO, "Gateway successfully bound to MicroRuntimeService");
                    startContainer(nickname, profile, agentStartupCallback);
                };

                public void onServiceDisconnected(ComponentName className) {
                    microRuntimeServiceBinder = null;
                    logger.log(Level.INFO, "Gateway unbound from MicroRuntimeService");
                }
            };
            logger.log(Level.INFO, "Binding Gateway to MicroRuntimeService...");
            bindService(new Intent(getApplicationContext(),
                            MicroRuntimeService.class), serviceConnection,
                    Context.BIND_AUTO_CREATE);
        } else {
            logger.log(Level.INFO, "MicroRumtimeGateway already binded to service");
            startContainer(nickname, profile, agentStartupCallback);
        }
    }

    private void startContainer(final String nickname, Properties profile,
                                final RuntimeCallback<AgentController> agentStartupCallback) {
        if (!MicroRuntime.isRunning()) {
            microRuntimeServiceBinder.startAgentContainer(profile,
                    new RuntimeCallback<Void>() {
                        @Override
                        public void onSuccess(Void thisIsNull) {
                            logger.log(Level.INFO, "Successfully start of the container...");
                            startAgent(nickname, agentStartupCallback);
                        }

                        @Override
                        public void onFailure(Throwable throwable) {
                            logger.log(Level.SEVERE, "Failed to start the container...");
                        }
                    });
        } else {
            startAgent(nickname, agentStartupCallback);
        }
    }

    private void startAgent(final String nickname,
                            final RuntimeCallback<AgentController> agentStartupCallback) {
        microRuntimeServiceBinder.startAgent(nickname,
                MobileAgent.class.getName(),
                new Object[] { getApplicationContext() },
                new RuntimeCallback<Void>() {
                    @Override
                    public void onSuccess(Void thisIsNull) {
                        logger.log(Level.INFO, "Successfully start of the "
                                + MobileAgent.class.getName() + "...");
                        try {
                            agentStartupCallback.onSuccess(MicroRuntime
                                    .getAgent(nickname));
                        } catch (ControllerException e) {
                            // Should never happen
                            agentStartupCallback.onFailure(e);

                        }
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        logger.log(Level.SEVERE, "Failed to start the "
                                + MobileAgent.class.getName() + "...");
                        agentStartupCallback.onFailure(throwable);
                    }
                });
    }

}
