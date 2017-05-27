package com.example.evolutionct.mixxi.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;

import com.example.evolutionct.mixxi.GlobalActivity;
import com.example.evolutionct.mixxi.R;
import com.example.evolutionct.mixxi.activity.JsOneActivity;
import com.example.evolutionct.mixxi.activity.PlaceActivity;
import com.example.evolutionct.mixxi.activity.SimpleDirectionActivity;
import com.example.evolutionct.mixxi.speed.SpeedActivity;
import com.example.evolutionct.mixxi.activity.TrafficMapsActivity;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class FacebookFragment extends Fragment {
    public ListView mList;
    public ImageButton speakButton;
    TextToSpeech t1;
    ArrayList<String> matches = new ArrayList<String>();
    public static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
    View rootView;


    public FacebookFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_calendar, container, false);
        /*speakButton = (ImageButton) rootView.findViewById(R.id.btn_speak);
        speakButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startVoiceRecognitionActivity();
            }
        });
        t1=new TextToSpeech(getActivity().getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.UK);
                }
            }
        });*/


        //speakButton = (ImageButton) rootView.findViewById(R.id.btn_speak);
        mList = (ListView) rootView.findViewById(R.id.list);
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }



    public void startVoiceRecognitionActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Recognizing Speech...");
        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
    }
   /* public void informationMenu() {
        startActivity(new Intent("android.intent.action.INFOSCREEN"));
    }*/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            // Fill the list view with the strings the recognizer thought it
            // could have heard
            matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            boolean checkMatch = false ;
            for(String toSpeak : matches)
            {
                if(toSpeak.contains("traffic"))
                {
                    Log.d("Speak",matches.get(0).toString());
                    t1.speak("traffic will show", TextToSpeech.QUEUE_FLUSH, null);
                    startActivity(new Intent(this.getActivity(), TrafficMapsActivity.class));
                    checkMatch = true;
                    break;
                }
                else if(toSpeak.contains("weather"))
                {
                    Log.d("Speak",matches.get(0).toString());
                    t1.speak("weather will show", TextToSpeech.QUEUE_FLUSH, null);
                    startActivity(new Intent(this.getActivity(), GlobalActivity.class));
                    checkMatch = true;
                    break;
                }
                else if(toSpeak.contains("places"))
                {
                    Log.d("Speak",matches.get(0).toString());
                    t1.speak("nearby places will show", TextToSpeech.QUEUE_FLUSH, null);
                    startActivity(new Intent(this.getActivity(), PlaceActivity.class));
                    checkMatch = true;
                    break;
                }
                else if(toSpeak.contains("speedometer"))
                {
                    Log.d("Speak",matches.get(0).toString());
                    t1.speak("speedometer will show", TextToSpeech.QUEUE_FLUSH, null);
                    startActivity(new Intent(this.getActivity(), SpeedActivity.class));
                    checkMatch = true;
                    break;
                }
                else if(toSpeak.contains("accident"))
                {
                    Log.d("Speak",matches.get(0).toString());
                    t1.speak("real-time accident will show", TextToSpeech.QUEUE_FLUSH, null);
                    startActivity(new Intent(this.getActivity(), JsOneActivity.class));
                    checkMatch = true;
                    break;
                }
                else if(toSpeak.contains("fuel calculation"))
                {
                    Log.d("Speak",matches.get(0).toString());
                    t1.speak("fuel calculation will show", TextToSpeech.QUEUE_FLUSH, null);
                    startActivity(new Intent(this.getActivity(), SimpleDirectionActivity.class));
                    checkMatch = true;
                    break;
                }
                else if(toSpeak.contains("who are you") || toSpeak.contains("what's your name"))
                {
                    Log.d("Speak",matches.get(0).toString());
                    t1.speak("I am Mixxi the intelligent driving assistant", TextToSpeech.QUEUE_FLUSH, null);
                    checkMatch = true;
                    break;
                }
                else if(toSpeak.contains("hello"))
                {
                    Log.d("Speak",matches.get(0).toString());
                    t1.speak("Sa wad dee Ka", TextToSpeech.QUEUE_FLUSH, null);
                    checkMatch = true;
                    break;
                }




            }
            if(checkMatch == false)
            {
                Log.d("Speak",matches.get(0).toString());
                t1.speak("I don't know what you said", TextToSpeech.QUEUE_FLUSH, null);
            }

            // matches is the result of voice input. It is a list of what the
            // user possibly said.
            // Using an if statement for the keyword you want to use allows the
            // use of any activity if keywords match
            // it is possible to set up multiple keywords to use the same
            // activity so more than one word will allow the user
            // to use the activity (makes it so the user doesn't have to
            // memorize words from a list)
            // to use an activity from the voice input information simply use
            // the following format;
            // if (matches.contains("keyword here") { startActivity(new
            // Intent("name.of.manifest.ACTIVITY")

           /* if (matches.contains("information")) {
                informationMenu();
            }*/
        }
    }
}
