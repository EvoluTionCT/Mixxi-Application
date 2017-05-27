package com.example.evolutionct.mixxi.activity;

import android.location.Address;
import android.util.Log;

import com.firebase.client.Firebase;
import com.google.android.gms.maps.GoogleMap;

import java.util.ArrayList;
import java.util.List;

import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Created by Disantos on 1/26/2017.
 */


public class ThreadActivity extends Thread {
    twitter4j.Twitter twitter;
    List<Address> addressList = null;
    List<String> roadList = new ArrayList<String>();
    // Test twitter 4J
    private int count;
    private Boolean stop = false;
    private JsOneActivity CDA;
    private GoogleMap mMap;
    private Firebase mRef;
    List<String> tweetList = new ArrayList<String>();

    //รับค่า count จาก CountDownActivity เพื่อมานับถอยหลัง
    //รับ Object CountDownActivity เพื่อเรียกใช้งาน runOnUiThread ในการ Update View
    public ThreadActivity(JsOneActivity CDA) {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey("amD496go0VvK4BlLJInFmFLXD")
                .setOAuthConsumerSecret("nEaZRXj2toYYl2jEPGgQ9hFNrX7Z8Ydcp8IjCBg8qcWJgbrFCb")
                .setOAuthAccessToken("814822071918133248-glVFym3FHdCpwOym7Zsrc0duXX6Uwqr")
                .setOAuthAccessTokenSecret("CE2eIu2SS8SBL8OLwTdUHXcWyuJcdkhcN9jOKqko78ZOL");
        TwitterFactory tf = new TwitterFactory(cb.build());
        twitter = tf.getInstance();



        this.CDA = CDA;

    }

    //function ไว้สำหรับร้องขอการหยุดการทำงานของ Thread
    public void requestStop() {
        this.stop = true;
    }

    //function ส่วนการทำงานของ Thread
    @Override
    public void run() {
        // TODO Auto-generated method stub

        while (true) {


            Log.d("log_success", "dd" + count );
            ResponseList<Status> status;
            try {
                status = twitter.getUserTimeline("js100radio");
                for (twitter4j.Status st : status) {
                    if((st.getText().contains("อุบัติเหตุ")) || (st.getText().contains("ชนกับ")) || (st.getText().contains("ชนท้าย")) || (st.getText().contains("รถชน")) || (st.getText().contains("เสียหลัก")))
                    {
                        //Log.d("Tweet", st.getUser().getName() + "---------" + st.getText());
                        tweetList.add(st.getText());
                    }



                }
            } catch (TwitterException e) {
                e.printStackTrace();
            }


            if (this.stop) //ถ้ามีการร้องขอการหยุดแล้ว ให้จบการทำงานของ Thread
                return;
            /*for(String str : tweetList)
            {
                Log.d("Tweet", str);
            }*/
           // Log.d("checktweetList", "Thread: " + tweetList.size());
            //count--;

            //เรียกใช้ function threadProcess ของ threadProcess
            this.CDA.runOnUiThread(new Runnable() {
                public void run() {
                    CDA.threadProcess(tweetList);
                }
            });
            try {
                sleep((long)900000); //หยุดการทำงาน 50 วินาที
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
