package com.amazonaws.demo.androidpubsubwebsocket;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.AWSStartupHandler;
import com.amazonaws.mobile.client.AWSStartupResult;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobileconnectors.dynamodbv2.document.Table;
import com.amazonaws.mobileconnectors.dynamodbv2.document.datatype.Document;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

public class SampleActivity extends AppCompatActivity {

    static final String LOG_TAG = PubSubActivity.class.getCanonicalName();

    static final String TAG = "DynamoDB_Demo";

    // --- Constants to modify per your configuration ---

    // Customer specific IoT endpoint
    // AWS Iot CLI describe-endpoint call returns: XXXXXXXXXX.iot.<region>.amazonaws.com,
    private static final String CUSTOMER_SPECIFIC_IOT_ENDPOINT = "ayo7p3wwhcabo-ats.iot.us-west-2.amazonaws.com";

    AWSIotMqttManager mqttManager;

    String clientId;

    Button living, bed, kitchen;


    /////////

    CognitoCachingCredentialsProvider credentialsProvider;
    AmazonDynamoDBClient dbClient;
    Table dbTable;

    DynamoDBMapper dynamoDBMapper;

    //////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);

        living = (Button) findViewById(R.id.living_room);
        bed = (Button) findViewById(R.id.bed_room);
        kitchen = (Button) findViewById(R.id.kitchen);

        clientId = UUID.randomUUID().toString();

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

        // MQTT Client
        mqttManager = new AWSIotMqttManager(clientId, CUSTOMER_SPECIFIC_IOT_ENDPOINT);

        //connect();


        living.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SampleActivity.this, LivingRoom.class);
                intent.putExtra("clientID", clientId);
                startActivity(intent);
            }
        });

        bed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SampleActivity.this, BedRoom.class);
                intent.putExtra("clientID", clientId);
                startActivity(intent);
            }
        });

        kitchen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SampleActivity.this, Kitchen.class);
                intent.putExtra("clientID", clientId);
                startActivity(intent);
            }
        });


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

    public class LoadTable extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {

            ////////////////

            // Create a connection to DynamoDB
            dbClient = new AmazonDynamoDBClient(credentialsProvider);

            // Create a table reference
            dbTable = Table.loadTable(dbClient, "TestTable");


            List<String> sd = dbTable.getHashKeys();
            //List<String> attributeList = new ArrayList<>();
            Document doc = new Document();

            dbTable.putItem(doc);

            /////////////////
            return null;
        }
    }
}
