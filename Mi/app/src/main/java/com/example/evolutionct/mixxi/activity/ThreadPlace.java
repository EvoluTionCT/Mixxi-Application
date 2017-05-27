package com.example.evolutionct.mixxi.activity;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


/**
 * Created by Disantos on 2/16/2017.
 */

public class ThreadPlace extends Thread {

    double latitude = 13.768739;
    double longitude =  100.540684;
    int radius = 5000;
    String type = "food";
    String keyword = "";
    String language = "th";
    String url ;
    String API_KEY ;
    private PlaceActivity CDA ;
    private String status = "";

    Document doc = null ;

    public final static String STATUS_OK = "OK";

    public final static String NAME = "Name";
    public final static String ADDRESS = "Address";
    public final static String LATITUDE = "Latitude";
    public final static String LONGITUDE = "Longitude";
    public final static String ICON = "Icon";
    public final static String OPENNOW = "OpenNow";
    public final static String PHOTO = "Photo";
    public final static String PHONENUMBER = "PhoneNumber";

    public ThreadPlace(PlaceActivity CDA)
    {
        Log.d("checky","In Mytask");
        this.API_KEY = CDA.ApiKey ;

        this.CDA = CDA;

    }

    public void ThreadPlaceInvoke(String query)
    {
        this.type = query ;
       // run();
    }

    @Override
    public void run(){
        this.type = CDA.placeQuery;
        try {

            Log.d("checky","dddddss");
            latitude = CDA.mLastLocation.getLatitude();
            longitude = CDA.mLastLocation.getLongitude();
            Log.d("checky","dddds2"+CDA.mLastLocation.getLatitude());
            url = "https://maps.googleapis.com/maps/api/place/search/xml?";
            url += "location=" + latitude + "," + longitude + "&radius=" + radius;
            url += "&key=" + API_KEY + "&sensor=false";

            if(!type.equals(""))
                url += "&types=" + type.toLowerCase();
            if(!keyword.equals(""))
                url += "&keyword=" + keyword.replace(" ", "+");
            if(!language.equals(""))
                url += "&language=" + language.toLowerCase();

            HttpClient httpClient = new DefaultHttpClient();
            HttpContext localContext = new BasicHttpContext();
            HttpPost httpPost = new HttpPost(String.valueOf(url));

            HttpResponse response = httpClient.execute(httpPost,localContext);
            HttpEntity entity = response.getEntity();

            try {
                InputStream in = entity.getContent();
                DocumentBuilder builder = null;
                builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                try {
                    doc = builder.parse(in);
                } catch (SAXException e) {
                    Log.d("checky","nulllllll22");
                    e.printStackTrace();
                }
            } catch (ParserConfigurationException e) {
                Log.d("checky","nulllllll22");
                e.printStackTrace();
            }


        } catch (IOException e) {
            Log.d("checky","nulllllll22");
            e.printStackTrace();
        }

        Log.d("checky","I will turn to main");

        final ArrayList<ContentValues> arr_cv = new ArrayList<ContentValues>();
        final ArrayList<Double> dist = new ArrayList<Double>();
        final ArrayList<Bitmap> bitMapList = new ArrayList<Bitmap>();
        Log.d("checky","ddddd"+doc.getElementsByTagName("PlaceSearchResponse"));
        NodeList nl1 = doc.getElementsByTagName("PlaceSearchResponse");
        NodeList nl2 = nl1.item(0).getChildNodes();
        Node node = nl2.item(getNodeIndex(nl2, "status"));
        /*if(node == null)
            return null;*/
        status = node.getTextContent();
        Log.d("DataStatus", status);

        if(node.getTextContent().equals(STATUS_OK)) {
            nl1 = doc.getElementsByTagName("result");
            for (int i = 0; i < nl1.getLength(); i++) {
                ContentValues cv = new ContentValues();
                node = nl1.item(i);
                nl2 = node.getChildNodes();
                node = nl2.item(getNodeIndex(nl2, "reference"));
                cv.put("reference", node.getTextContent());
                cv = getReferenceData(cv, getReferenceDocument(node.getTextContent()));
                try {
                    node = nl2.item(getNodeIndex(nl2, "photo"));
                    NodeList nl3 = node.getChildNodes();
                    node = nl3.item(getNodeIndex(nl3, "photo_reference"));;
                    cv.put(PHOTO, node.getTextContent());
                } catch (ArrayIndexOutOfBoundsException e) {
                    cv.put(PHOTO, "");
                }

                arr_cv.add(cv);
                // find Distance
                double tempLat = arr_cv.get(i).getAsDouble(LATITUDE);
                double tempLon = arr_cv.get(i).getAsDouble(LONGITUDE);
                //LatLng tempLatlng = new LatLng(tempLat,tempLon);
                StringBuilder stringBuilder = new StringBuilder();



                try {
                    String urls = "https://maps.googleapis.com/maps/api/directions/json?origin=" + latitude + "," + longitude+ "&destination=" + tempLat  + "," + tempLon+ "&key=AIzaSyAOB0vXFj0IFinCqP-VERrZI-yDRgAf8ck";
                    HttpPost httppost = new HttpPost(urls);

                    HttpClient client = new DefaultHttpClient();
                    HttpResponse response;
                    stringBuilder = new StringBuilder();
                    Log.d("Post",httppost.toString());

                    response = client.execute(httppost);
                    HttpEntity entity = response.getEntity();
                    InputStream stream = entity.getContent();
                    int b;
                    while ((b = stream.read()) != -1) {
                        stringBuilder.append((char) b);
                    }
                } catch (ClientProtocolException e) {
                    Log.i("Distance", "Error1");
                } catch (IOException e) {
                    Log.i("Distance", "Error2");
                }
                JSONObject jsonObject = new JSONObject();
                try {

                    jsonObject = new JSONObject(stringBuilder.toString());

                    JSONArray array = jsonObject.getJSONArray("routes");

                    JSONObject routes = array.getJSONObject(0);

                    JSONArray legs = routes.getJSONArray("legs");

                    JSONObject steps = legs.getJSONObject(0);

                    JSONObject distance = steps.getJSONObject("distance");

                    //Log.i("Distance", distance.toString() + arr_cv.get(i).getAsString(NAME) + nl1.getLength() +" " + i);
                    dist.add(Double.parseDouble(distance.getString("text").replaceAll("[^\\.0123456789]","") ));






                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    dist.add(-1.00);
                    Log.i("Distance", "Error3"  + dist.get(i));
                    e.printStackTrace();

                }
                // End of finding distance
                // Start Bitmap
                try {
                    String PHOTOS = arr_cv.get(i).getAsString(PHOTO);
                    //String PHOTOS = "CnRtAAAATLZNl354RwP_9UKbQ_5Psy40texXePv4oAlgP4qNEkdIrkyse7rPXYGd9D_Uj1rVsQdWT4oRz4QrYAJNpFX7rzqqMlZw2h2E2y5IKMUZ7ouD_SlcHxYq1yL4KbKUv3qtWgTK0A6QbGh87GB3sscrHRIQiG2RrmU_jF4tENr9wGS_YxoUSSDrYjWmrNfeEHSGSc3FyhNLlBU";
                    String url2 = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference="+PHOTOS +"&key="+API_KEY;


                    HttpPost httppost = new HttpPost(url2);
                    HttpClient client = new DefaultHttpClient();
                    HttpResponse response;
                    response = client.execute(httppost);
                    HttpEntity entity = response.getEntity();
                    InputStream in = entity.getContent();
                    //in = httpclient.execute(request).getEntity().getContent();
                    Bitmap bmp = BitmapFactory.decodeStream(in);
                    in.close();
                    bitMapList.add(bmp);

                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }


            Log.d("checky","I will turn to main");
            this.CDA.runOnUiThread(new Runnable() {
                public void run() {
                    CDA.threadPlaceProcess(arr_cv,dist,bitMapList);
                }
            });
//            this.stop();

        }
        else // if zero_result
        {
            CDA.threadFirstCheck=true;
            this.CDA.runOnUiThread(new Runnable() {
                public void run() {

                    Toast.makeText(CDA,"There's no any place you've searched",Toast.LENGTH_SHORT).show();
                }
            });

        }



    }
    private Document getReferenceDocument(String reference) {
        String url = "https://maps.googleapis.com/maps/api/place/details/xml?";
        url += "reference=" + reference + "&key=" + API_KEY + "&sensor=false";

        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpContext localContext = new BasicHttpContext();
            HttpPost httpPost = new HttpPost(url);
            HttpResponse response = httpClient.execute(httpPost, localContext);
            InputStream in = response.getEntity().getContent();
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(in);
            return doc;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    private ContentValues getReferenceData(ContentValues cv, Document doc) {
        NodeList nl1 = doc.getElementsByTagName("result");
        nl1 = nl1.item(0).getChildNodes();

        Node node = nl1.item(getNodeIndex(nl1, "name"));
        cv.put(NAME, node.getTextContent());

        try {
            node = nl1.item(getNodeIndex(nl1, "formatted_phone_number"));
            cv.put(PHONENUMBER, node.getTextContent());
        } catch (ArrayIndexOutOfBoundsException e) {
            cv.put(PHONENUMBER, "Unknown");
        }

        node = nl1.item(getNodeIndex(nl1, "formatted_address"));
        cv.put(ADDRESS, node.getTextContent());

        node = nl1.item(getNodeIndex(nl1, "geometry"));
        NodeList nl2 = node.getChildNodes();
        node = nl2.item(getNodeIndex(nl2, "location"));
        nl2 = node.getChildNodes();
        node = nl2.item(getNodeIndex(nl2, "lat"));
        cv.put(LATITUDE, node.getTextContent());
        node = nl2.item(getNodeIndex(nl2, "lng"));
        cv.put(LONGITUDE, node.getTextContent());

        node = nl1.item(getNodeIndex(nl1, "icon"));
        cv.put(ICON, node.getTextContent());

        return cv;
    }

    private int getNodeIndex(NodeList nl, String nodename) {
        for(int i = 0 ; i < nl.getLength() ; i++) {
            if(nl.item(i).getNodeName().equals(nodename))
                return i;
        }
        return -1;
    }



}
