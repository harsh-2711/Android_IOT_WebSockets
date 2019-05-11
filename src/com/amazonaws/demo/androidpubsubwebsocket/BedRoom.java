package com.amazonaws.demo.androidpubsubwebsocket;

import android.content.Intent;
import android.graphics.Color;
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

public class BedRoom extends AppCompatActivity {

    Button light, tv;

    static final String LOG_TAG = PubSubActivity.class.getCanonicalName();

    // --- Constants to modify per your configuration ---

    // Customer specific IoT endpoint
    // AWS Iot CLI describe-endpoint call returns: XXXXXXXXXX.iot.<region>.amazonaws.com,
    private static final String CUSTOMER_SPECIFIC_IOT_ENDPOINT = "ayo7p3wwhcabo-ats.iot.us-west-2.amazonaws.com";

    String clientID;
    AWSIotMqttManager mqttManager;

    int light_check = 0, tv_check = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bed_room);


        Intent intent = getIntent();
        clientID = intent.getStringExtra("clientID");

        mqttManager = new AWSIotMqttManager(clientID, CUSTOMER_SPECIFIC_IOT_ENDPOINT);

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

        connect();

        light = (Button) findViewById(R.id.bed_light_1);
        tv = (Button) findViewById(R.id.bed_tv);

        light.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(light_check == 0) {
                    light_check = 1;
                    light.setBackgroundColor(Color.parseColor("#00FF00"));
                    publish();
                }
                else {
                    light_check = 0;
                    light.setBackgroundColor(Color.parseColor("#FF0000"));
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

        String topic = "$aws/things/esp_last_thing2/shadow/update";
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
        String topic = "$aws/things/esp_last_thing2/shadow/update";
        String msg = "{\"state\": { \"reported\": { \"appl0_status\":\"" + String.valueOf(light_check) + "\",\"appl1_status\":\"" +
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
