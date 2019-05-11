package com.amazonaws.demo.androidpubsubwebsocket;

import android.content.Context;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.document.DeleteItemOperationConfig;
import com.amazonaws.mobileconnectors.dynamodbv2.document.PutItemOperationConfig;
import com.amazonaws.mobileconnectors.dynamodbv2.document.ScanOperationConfig;
import com.amazonaws.mobileconnectors.dynamodbv2.document.Search;
import com.amazonaws.mobileconnectors.dynamodbv2.document.Table;
import com.amazonaws.mobileconnectors.dynamodbv2.document.UpdateItemOperationConfig;
import com.amazonaws.mobileconnectors.dynamodbv2.document.datatype.Document;
import com.amazonaws.mobileconnectors.dynamodbv2.document.datatype.DynamoDBEntry;
import com.amazonaws.mobileconnectors.dynamodbv2.document.datatype.DynamoDBList;
import com.amazonaws.mobileconnectors.dynamodbv2.document.datatype.Primitive;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class DatabaseAccess {

    private String TAG = "DynamoDb_Demo";

    /*taken from official Amazon sample app -
    see: https://github.com/awslabs/aws-sdk-android-samples*/

    /*see blogs for more examples of using Document API:
    https://aws.amazon.com/blogs/mobile/using-amazon-dynamodb-document-api-with-aws-mobile-sdk-for-android-part-1/
    and:
    https://aws.amazon.com/blogs/mobile/using-amazon-dynamodb-document-api-with-the-aws-mobile-sdk-for-android-part-2/*/

    private final String COGNITO_IDENTITY_POOL_ID = "us-west-2:46abaf05-29f7-4494-96aa-199c2843078e";
    private final Regions COGNITO_IDENTITY_POOL_REGION = Regions.US_WEST_2;
    private final String DYNAMODB_TABLE = "TestTable";
    private Context context;
    private CognitoCachingCredentialsProvider credentialsProvider;
    private AmazonDynamoDBClient dbClient;
    private Table dbTable;

    /**
     * This class is a singleton - storage for the current instance.
     */
    private static volatile DatabaseAccess instance;


    private DatabaseAccess(Context context) {
        this.context = context;

        // Create a new credentials provider
        credentialsProvider = new CognitoCachingCredentialsProvider(context, COGNITO_IDENTITY_POOL_ID, COGNITO_IDENTITY_POOL_REGION);

        // Create a connection to the DynamoDB service
        dbClient = new AmazonDynamoDBClient(credentialsProvider);

        /*MUST SET db client REGION HERE ELSE DEFAULTS TO US_EAST_1*/
        dbClient.setRegion(Region.getRegion(Regions.EU_WEST_1));

        // Create a table reference
        dbTable = Table.loadTable(dbClient, DYNAMODB_TABLE);

    }

    /**
     * Singleton pattern - retrieve an instance of the DatabaseAccess
     */
    public static synchronized DatabaseAccess getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseAccess(context);
        }
        return instance;
    }

    /*gets a contact for given primary key*/
    public Document getItem(String telephone) {
        Document result = dbTable.getItem(new Primitive(telephone));
        return result;
    }

    /*gets all the contacts*/
    public List<Document> getAllContacts() {
        /*using scan to get all contacts*/
        ScanOperationConfig scanConfig = new ScanOperationConfig();
        List<String> attributeList = new ArrayList<>();
        attributeList.add("Telephone");
        attributeList.add("name");
        scanConfig.withAttributesToGet(attributeList);
        Search searchResult = dbTable.scan(scanConfig);
        return searchResult.getAllResults();
    }

    /*deletes a contact*/
    public Document deleteContact(String string) {
        /*DeleteItem only supports ReturnValues.All_OLD and ReturnValues.NONE*/
        Document result = dbTable.deleteItem(new Primitive(string), new DeleteItemOperationConfig().withReturnValues(ReturnValue.ALL_OLD));
        return result;
    }

    /*updates a contact*/
    public boolean updateContact(String telephone) {
        Log.i(TAG, "in updateContact()....");
        /*get the item*/
        Document retrievedDoc = dbTable.getItem(new Primitive(telephone));

        if (retrievedDoc != null) {

            String newName = "Billy Bob";

            if (retrievedDoc.get("name").asString().equals("Billy Bob")) {
                newName = "Jimmy Gee";
            }

            if (retrievedDoc.get("name").asString().equals("Jimmy Gee")) {
                newName = "Billy Bob";
            }

            /*update name*/
            retrievedDoc.put("name", newName);

            /*update Set*/
            Set<String> mySet = new HashSet<String>();
            /*get the Set*/
            DynamoDBEntry theSet = retrievedDoc.get("Set");
            /*convert Set to List*/
            List<String> stringSetList = theSet.convertToAttributeValue().getSS();
            /*add the list to the Set*/
            mySet.addAll(stringSetList);

            /*now add new items*/
            mySet.add("set item 1011");
            mySet.add("set item 2022");
            retrievedDoc.put("Set", mySet);

            /*updating Car attribute - get existing Car map*/
            Document retrievedCarDoc = retrievedDoc.get("Car").asDocument();
            /*create new Car doc*/
            Document car = new Document();
            /*add existing Car attributes*/
            car.put("Car_color", retrievedCarDoc.get("Car_color").asString());
            car.put("Car_make", retrievedCarDoc.get("Car_make").asString());
            /*add new Car attribute*/
            car.put("Car_wheels", "square");
            /*add map to doc*/
            retrievedDoc.put("Car", car);

            Document updateResult = dbTable.updateItem(retrievedDoc, new Primitive(telephone)
                    , new UpdateItemOperationConfig().withReturnValues(ReturnValue.UPDATED_NEW));

            try {
                Log.i(TAG, "updateResult: " + Document.toJson(updateResult));
            } catch (IOException e) {
                e.printStackTrace();
                Log.i(TAG, "updateResult json error: " + e.getLocalizedMessage());
            }

            /*******  shows how to get unknown type data out document  **********/
//            getUnknowDataTypes(retrievedDoc);
//            /* <<<<<  *****************************************************************<<<<   */

            return true;
        }
        return false;
    }

    /*add a single item*/
    public Document addContact(Document dummyContact) {
        Log.i(TAG, "adding contact...");

        String newName = "Billy Bob";

        if (dummyContact.get("name").asString().equals("Billy Bob")) {
            newName = "Jimmy Gee";
        }

        if (dummyContact.get("name").asString().equals("Jimmy Gee")) {
            newName = "Billy Bob";
        }

        /*new attributes*/
        dummyContact.put("name", newName);

        /*boolean*/
        dummyContact.put("Male", false);
        dummyContact.put("School", "no school");

        /*integer*/
        dummyContact.put("Age", 101);

        dummyContact.put("country", "USA");
        dummyContact.put("song", "Ding Dong");

        /*unordered list as Set*/
        Set<String> mySet = new HashSet<String>();
        mySet.add("set item 1");
        mySet.add("set item 2");
        dummyContact.put("Set", mySet);

        // An Ordered List
        DynamoDBEntry item1 = new Primitive("orderedlist item 75");
        DynamoDBEntry item2 = new Primitive("orderedlist item 57");

        DynamoDBList dynamoList = new DynamoDBList();
        dynamoList.add(item1);
        dynamoList.add(item2);
        dummyContact.put("Ordered_list", dynamoList);

        /*key-value map*/
        Document car = new Document();
        car.put("Car_make", "Taz");
        car.put("Car_color", "Indigo");
        dummyContact.put("Car", car);

        /*PutItem only supports ReturnValues All old and None*/
        PutItemOperationConfig putItemOperationConfig = new PutItemOperationConfig();
        putItemOperationConfig.withReturnValues(ReturnValue.ALL_OLD);
//        putItemOperationConfig.withReturnValues(ReturnValue.NONE);

        Document result = dbTable.putItem(dummyContact, putItemOperationConfig);

        return result;
    }

    /*for getting unknown data types*/
    private void getUnknowDataTypes(Document doc) {

        Document retrievedDoc = doc.get("Car").asDocument();

        Log.i(TAG, "in getUnknowDataTypes()...");
        try {
            String jsonString = Document.toJson(retrievedDoc);
            /*convert to json object*/
            JsonParser parser = new JsonParser();
            JsonObject jsonObject = parser.parse(jsonString).getAsJsonObject();

            /*use json to get attribute value*/
            String carColor = jsonObject.get("Car_color").getAsString();
            Log.i(TAG, "Car color: " + carColor);

            /*convert to pretty print format*/
            Gson gsonBuild = new GsonBuilder().setPrettyPrinting().create();
            String jsonBuild = gsonBuild.toJson(jsonObject);
            Log.i(TAG, "Car map: " + jsonBuild);

        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, "error converting map to json: " + e.getLocalizedMessage());
        }
    }

   /* private void updateByPutItem(Document retrievedDoc) {
        Log.i(TAG, "in updateByPutItem()...");
        try {
            Log.i(TAG, "in updateByPutItem()..retrievedDoc: " + Document.toJson(retrievedDoc));
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, "Error: in updateByPutItem()...converting retrievedDoc to json: " + e.getLocalizedMessage());
        }

        HashMap<String, AttributeValue> item_values =
                new HashMap<String, AttributeValue>();

        item_values.put("Telephone", new AttributeValue(retrievedDoc.get("Telephone").asString()));

        String newName = "Billy Bob";

        if (retrievedDoc.get("name").asString().equals("Billy Bob")) {
            newName = "Jimmy Gee";
        }

        if (retrievedDoc.get("name").asString().equals("Jimmy Gee")) {
            newName = "Billy Bob";
        }

        *//*new attributes*//*
        item_values.put("name", new AttributeValue(newName));
        item_values.put("country", new AttributeValue("USA"));
        item_values.put("song", new AttributeValue("Ding Dong"));

        AttributeValue sex = new AttributeValue();
        sex.setBOOL(true);
        item_values.put("Male", sex);

        item_values.put("School", new AttributeValue("this school"));

        AttributeValue age = new AttributeValue();
        age.setN("37");
        item_values.put("Male", sex);
        item_values.put("Age", age);

        *//*unordered list as Set*//*
        Set<String> mySet = new HashSet<String>();
        mySet.add("set item 1");
        mySet.add("set item 2");

        AttributeValue stringSet = new AttributeValue();
        stringSet.withSS(mySet);
        item_values.put("Set", stringSet);

        // An Ordered List
        List<AttributeValue> list = new ArrayList<>();
        list.add(new AttributeValue("orderedlist item 22"));
        list.add(new AttributeValue("orderedlist item 33"));

        AttributeValue listAttribute = new AttributeValue();
        listAttribute.setL(list);
        item_values.put("Ordered_list", listAttribute);

        *//*key-value map*//*
        Map<String, AttributeValue> car = new HashMap<>();
        car.put("Car_make", new AttributeValue("VolksVagen"));
        car.put("Car_color", new AttributeValue("Suede"));

        AttributeValue map = new AttributeValue();
        map.withM(car);
        item_values.put("Car", map);

        String returnValues = null;

        Log.i(TAG, "in updateByPutItem() starting putItem....");
        PutItemRequest putItemRequest = new PutItemRequest("TestMyDynoApp", item_values
                , returnValues).withReturnValues(ReturnValue.ALL_OLD);
        PutItemResult putItemRequestResult = dbClient.putItem(putItemRequest);
        Log.i(TAG, "put item request result: " + putItemRequestResult);
    }*/



}
