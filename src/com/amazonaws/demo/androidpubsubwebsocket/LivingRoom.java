package com.amazonaws.demo.androidpubsubwebsocket;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.CountDownLatch;

public class LivingRoom extends AppCompatActivity {

    Button light1, fan1, fan2, tv;

    static final String LOG_TAG = PubSubActivity.class.getCanonicalName();

    // --- Constants to modify per your configuration ---

    // Customer specific IoT endpoint
    // AWS Iot CLI describe-endpoint call returns: XXXXXXXXXX.iot.<region>.amazonaws.com,
    private static final String CUSTOMER_SPECIFIC_IOT_ENDPOINT = "ayo7p3wwhcabo-ats.iot.us-west-2.amazonaws.com";

    String clientID;
    AWSIotMqttManager mqttManager;

    int light1_check = 0, fan1_check = 0, fan2_check = 0, tv_check = 0;

    boolean isConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_living_room);

        Intent intent = getIntent();
        clientID = intent.getStringExtra("clientID");

        // Initialize the credentials provider
        final CountDownLatch latch = new CountDownLatch(1);
        AWSMobileClient.getInstance().initialize(
                getApplicationContext(),
                new Callback<UserStateDetails>() {
                    @Override
                    public void onResult(UserStateDetails result) {
                        latch.countDown();
                    }

                    @Override
                    public void onError(Exception e) {
                        latch.countDown();
                        Log.e(LOG_TAG, "onError: ", e);
                    }
                }
        );

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mqttManager = new AWSIotMqttManager(clientID, CUSTOMER_SPECIFIC_IOT_ENDPOINT);

        connect();

        light1 = (Button) findViewById(R.id.living_light_1);
        fan1 = (Button) findViewById(R.id.living_fan_1);
        fan2 = (Button) findViewById(R.id.living_fan_2);
        tv = (Button) findViewById(R.id.living_tv);

        light1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(light1_check == 0) {
                    light1_check = 1;
                    light1.setBackgroundColor(Color.parseColor("#00FF00"));
                    subscribe();
                }
                else {
                    light1_check = 0;
                    light1.setBackgroundColor(Color.parseColor("#FF0000"));
                    publish();
                }
            }
        });

        fan1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fan1_check == 0) {
                    fan1_check = 1;
                    fan1.setBackgroundColor(Color.parseColor("#00FF00"));
                    publish();
                }
                else {
                    fan1_check = 0;
                    fan1.setBackgroundColor(Color.parseColor("#FF0000"));
                    publish();
                }
            }
        });

        fan2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fan2_check == 0) {
                    fan2_check = 1;
                    fan2.setBackgroundColor(Color.parseColor("#00FF00"));
                    publish();
                }
                else {
                    fan2_check = 0;
                    fan2.setBackgroundColor(Color.parseColor("#FF0000"));
                    publish();
                }
            }
        });

        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tv_check == 0) {
                    tv_check = 1;
                    tv.setBackgroundColor(Color.parseColor("#00FF00"));
                    publish();
                }
                else {
                    tv_check = 0;
                    tv.setBackgroundColor(Color.parseColor("#FF0000"));
                    publish();
                }
            }
        });

    }

    public void subscribe() {

        //String topic = "$aws/things/esp_last_thing4/shadow/update";
        String topic = "$aws/things/esp_01_4002/shadow/update";
        Log.d(LOG_TAG, "topic = " + topic);

        try {
            mqttManager.subscribeToTopic(topic, AWSIotMqttQos.QOS0,
                    new AWSIotMqttNewMessageCallback() {
                        @Override
                        public void onMessageArrived(final String topic, final byte[] data) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        String message = new String(data, "UTF-8");
                                        Log.d(LOG_TAG, "Message arrived:");
                                        Log.d(LOG_TAG, "   Topic: " + topic);
                                        Log.d(LOG_TAG, " Message: " + message);

                                    } catch (UnsupportedEncodingException e) {
                                        Log.e(LOG_TAG, "Message encoding error.", e);
                                    }
                                }
                            });
                        }
                    });
        } catch (Exception e) {
            Log.e(LOG_TAG, "Subscription error.", e);
        }
    }

    public void publish() {
        //String topic = "$aws/things/esp_last_thing4/shadow/update";
        String topic = "$aws/things/esp_01_4002/shadow/update";
        String msg = "{\"state\": { \"desired\": { \"appl0_status\":\"" + String.valueOf(light1_check) + "\",\"appl1_status\":\"" +
                String.valueOf(fan1_check) + "\",\"appl2_status\":\"" + String.valueOf(fan2_check) + "\",\"appl3_status\":\"" +
                String.valueOf(tv_check) + "\" } } }";

        try {
            mqttManager.publishString(msg, topic, AWSIotMqttQos.QOS0);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Publish error.", e);
        }
    }


    public void connect() {

        try {
            mqttManager.connect(AWSMobileClient.getInstance(), new AWSIotMqttClientStatusCallback() {
                @Override
                public void onStatusChanged(final AWSIotMqttClientStatus status,
                                            final Throwable throwable) {
                    Log.d(LOG_TAG, "Status = " + String.valueOf(status));
                    if(String.valueOf(status).equals("Connected"))
                        subscribe();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (throwable != null) {
                                Log.e(LOG_TAG, "Connection error.", throwable);
                            }
                        }
                    });
                }
            });
        } catch (final Exception e) {
            Log.e(LOG_TAG, "Connection error.", e);
        }
    }
}
