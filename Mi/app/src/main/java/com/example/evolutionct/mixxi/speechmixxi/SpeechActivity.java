package com.example.evolutionct.mixxi.speechmixxi;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.evolutionct.mixxi.GlobalActivity;
import com.example.evolutionct.mixxi.R;
import com.example.evolutionct.mixxi.activity.JsOneActivity;
import com.example.evolutionct.mixxi.activity.MainActivity;
import com.example.evolutionct.mixxi.activity.PlaceActivity;
import com.example.evolutionct.mixxi.activity.SimpleDirectionActivity;
import com.example.evolutionct.mixxi.activity.TrafficMapsActivity;
import com.example.evolutionct.mixxi.activity.Weather2Activity;
import com.example.evolutionct.mixxi.speechrecognize.SpeechRecognizer;
import com.example.evolutionct.mixxi.speed.SpeedActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SpeechActivity extends Thread implements SpeechRecognizer.VoiceRecognizedListener
{
	public  static SpeechRecognizer recognizer;
	//SpeechRecognizer speechRecognizer;

	TextView message;
	public Dialog dialog ;

	boolean ready = false;
	public boolean checkMixxi = false ; // check whether speaker wants to end or not
	boolean checkMatch = false; // Check whether what speaker says
	public boolean checkFirstEntry = false ; // Check whether what speaker hello or not
	public LinearLayout ll;
	public LinearLayout ll2;
	public boolean speechDisable = false;
	public  Activity CDA ;
	public  String nameActivity;
	TextToSpeech Mixxi ;


	public SpeechActivity (Activity CDA)
	{


		Log.d("Activitys", CDA.getLocalClassName() + "1");
		nameActivity = CDA.getLocalClassName();
		if (recognizer == null)
			recognizer = new SpeechRecognizer();
		recognizer.setLanguage("th-TH");
		recognizer.setVoiceRecognizedListener(this);
		try
		{
		//	Log.d("Speech","0" + speechDisable);
			recognizer.start();

		} catch (Exception e)
		{
			e.printStackTrace();
		}
		handlerShowDetectLevel.sendEmptyMessage(0);

		this.CDA = CDA ;
		speechDisable = false ;

		Mixxi =new TextToSpeech(CDA, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    Mixxi.setLanguage(Locale.UK);
                }
            }
        });

	}


	@Override
	public void onVoiceRecognized(String[] results)
	{
		//Log.d("Speech: ", "1"  + speechDisable);
		Message msg = new Message();
		msg.obj = results;
		handlerShowResults.sendMessage(msg);

	}


	Handler handlerShowResults = new Handler()
	{

		@Override
		public void handleMessage(Message msg)
		{//Log.d("Speech: ", "2"  + speechDisable);
			StringBuilder out = new StringBuilder();
			String[] results = (String[]) msg.obj;
			List <String> result_list = new ArrayList<String>();
			Log.d("Activitys", CDA.getLocalClassName() + "2");
			for (String result : results)
			{
				out.append(result);
				out.append("\r\n");
				result_list.add(result);

				result = result.toLowerCase();

				if((result.contains("hello") ||result.contains("สวัสดี")) && checkFirstEntry == false  )
				{

					checkMixxi = true ;
					checkMatch = true;
					checkFirstEntry = true;

					dialog = new Dialog(CDA);
					dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
					//dialog.setTitle("Mixxi is listening .....");
					dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
					dialog.setContentView(R.layout.customlayout_mainactivity);
					dialog.setCancelable(false);

					message = (TextView) dialog.findViewById(R.id.message);
					Mixxi.speak("hello", TextToSpeech.QUEUE_FLUSH, null);



					dialog.show();
					/*ll.setVisibility(LinearLayout.GONE);
					ll2.setVisibility(LinearLayout.GONE);*/

					break;
				}
				// These are individual function
				else if((result.contains("near by") ||result.contains("ใกล้")) && nameActivity.equals("activity.Weather2Activity")  && checkMixxi == true)
				{

					String tempStr = ((Weather2Activity)CDA).nearLo.city + "the temperature is" + ((Weather2Activity)CDA).nearLo.temperature+"celcius"
							+ "humidity is" + ((Weather2Activity)CDA).nearLo.humidity +"percent"+"windspeed is" + ((Weather2Activity)CDA).nearLo.windSpeed + "mps" ;
					Mixxi.speak(tempStr, TextToSpeech.QUEUE_FLUSH, null);
					checkMatch = true ;
					break;
				}
                else if((result.contains("near by") ||result.contains("ใกล้")) && nameActivity.equals("activity.JsOneActivity")  && checkMixxi == true)
                {

					((JsOneActivity)CDA).nearbyAcc();
                    checkMatch = true ;
                    break;
                }
				else if((result.contains("switching") ||result.contains("เปลี่ยนหน้า")) && nameActivity.equals("activity.Weather2Activity")  && checkMixxi == true)
				{

					Mixxi.speak("switch to daily weather", TextToSpeech.QUEUE_FLUSH, null);
					CDA.startActivity(new Intent(CDA, GlobalActivity.class));
					checkMatch = true ;
					break;
				}
				else if((result.contains("switching") ||result.contains("เปลี่ยนหน้า")) && nameActivity.equals("activity.WeatherActivity")  && checkMixxi == true)
				{

					Mixxi.speak("switch to weather around you", TextToSpeech.QUEUE_FLUSH, null);
					CDA.startActivity(new Intent(CDA, Weather2Activity.class));
					checkMatch = true ;
					break;
				}
				else if((result.contains("food") || result.contains("อาหาร")) && nameActivity.equals("activity.PlaceActivity")  && checkMixxi == true)
				{
					Mixxi.speak("nearby restaurants will show", TextToSpeech.QUEUE_FLUSH, null);
					((PlaceActivity)CDA).speakerSearch("food");
					checkMatch = true ;
					break;
				}
				else if((result.contains("gas station") || result.contains("ปั๊มน้ำมัน")) && nameActivity.equals("activity.PlaceActivity")  && checkMixxi == true)
				{
					Mixxi.speak("nearby gas stations will show", TextToSpeech.QUEUE_FLUSH, null);
					((PlaceActivity)CDA).speakerSearch("gas_station");
					checkMatch = true ;
					break;
				}
				else if((result.contains("school") || result.contains("โรงเรียน")) && nameActivity.equals("activity.PlaceActivity")  && checkMixxi == true)
				{
					Mixxi.speak("nearby schools will show", TextToSpeech.QUEUE_FLUSH, null);
					((PlaceActivity)CDA).speakerSearch("school");
					checkMatch = true ;
					break;
				}
				else if((result.contains("police") || result.contains("สถานีตำรวจ")) && nameActivity.equals("activity.PlaceActivity")  && checkMixxi == true)
				{
					Mixxi.speak("nearby police stations will show", TextToSpeech.QUEUE_FLUSH, null);
					((PlaceActivity)CDA).speakerSearch("police");
					checkMatch = true ;
					break;
				}
				else if((result.contains("near by") ||result.contains("ใกล้")) && nameActivity.equals("activity.PlaceActivity")  && checkMixxi == true)
				{
					Mixxi.speak(((PlaceActivity)CDA).closestPlace + String.valueOf(((PlaceActivity)CDA).closest) +"kilometers from here", TextToSpeech.QUEUE_FLUSH, null);
					((PlaceActivity)CDA).speakerClosest();
					checkMatch = true ;
					break;
				}
				else if((result.contains("to") ||result.contains("ถึง")) && nameActivity.equals("activity.SimpleDirectionActivity")  && checkMixxi == true)
				{

					((SimpleDirectionActivity)CDA).speakerDestination(result);
					Mixxi.speak("ok", TextToSpeech.QUEUE_FLUSH, null);
					checkMatch = true ;
					break;
				}
				else if((result.contains("how much") ||result.contains("cost") ||result.contains("ราคา") ||result.contains("เท่าไร")  ) && nameActivity.equals("activity.SimpleDirectionActivity")  && checkMixxi == true)
				{

					((SimpleDirectionActivity)CDA).speakerFuel(result);

					checkMatch = true ;
					break;
				}

				// These are the main speech function
				else if ((result.contains("hello") ||result.contains("สวัสดี"))  && checkFirstEntry == true && checkMixxi == true)
				{
					Mixxi.speak("hello", TextToSpeech.QUEUE_FLUSH, null);
					break ;
				}

				else if ((result.contains("bye") || (result.contains("บ๊ายบาย") || result.contains("goodbye")) && checkMixxi == true))
				{
					checkMatch = true ;
					checkMixxi = false;
					checkFirstEntry = false ;
					Mixxi.speak("bye bye", TextToSpeech.QUEUE_FLUSH, null);
					dialog.cancel();

					break ;

				}
				else if((result.contains("traffic") || result.contains("จราจร")) && checkMixxi == true)
				{

					Mixxi.speak("traffic will show", TextToSpeech.QUEUE_FLUSH, null);

					CDA.startActivity(new Intent(CDA, TrafficMapsActivity.class));
					checkMatch = true ;
					break;
				}
				else if((result.contains("weather")|| result.contains("สภาพอากาศ")) && checkMixxi == true)
				{

					Mixxi.speak("weather will show", TextToSpeech.QUEUE_FLUSH, null);
					CDA.startActivity(new Intent(CDA, Weather2Activity.class));
					checkMatch = true ;
					break;
				}
				else if((result.contains("places") || result.contains("สถานที่")) && checkMixxi == true)
				{

					Mixxi.speak("nearby places will show", TextToSpeech.QUEUE_FLUSH, null);
					CDA.startActivity(new Intent(CDA, PlaceActivity.class));
					checkMatch = true ;
					break;
				}
				else if((result.contains("speedometer") || result.contains("วัดความเร็ว")) && checkMixxi == true)
				{

					Mixxi.speak("speedometer will show", TextToSpeech.QUEUE_FLUSH, null);
					CDA.startActivity(new Intent(CDA, SpeedActivity.class));
					checkMatch = true ;
					break;
				}
				else if((result.contains("accident")|| result.contains("อุบัติเหตุ")) && checkMixxi == true)
				{

					Mixxi.speak("real-time accident will show", TextToSpeech.QUEUE_FLUSH, null);
					CDA.startActivity(new Intent(CDA, JsOneActivity.class));
					checkMatch = true ;
					break;
				}
				else if((result.contains("calculation")|| result.contains("คำนวณน้ำมัน")) && checkMixxi == true)
				{

					Mixxi.speak("fuel calculation will show", TextToSpeech.QUEUE_FLUSH, null);
					CDA.startActivity(new Intent(CDA, SimpleDirectionActivity.class));
					checkMatch = true ;
					break;
				}
				else if((result.contains("main menu")|| result.contains("หน้าหลัก")) && checkMixxi == true)
				{

					Mixxi.speak("back to main page", TextToSpeech.QUEUE_FLUSH, null);
					CDA.startActivity(new Intent(CDA, MainActivity.class));
					checkMatch = true ;
					break;
				}
				else if((result.contains("ออกโปรแกรม")||result.contains("exit")) && checkMixxi == true)
				{

					Mixxi.speak("see you", TextToSpeech.QUEUE_FLUSH, null);

					checkMatch = true ;
					System.exit(0);
					break;
				}
				else if((result.contains("who are you") || result.contains("what's your name")|| result.contains("คุณคือใคร") || result.contains("คุณชื่ออะไร") || result.contains("คุณชื่ออะไร")) && checkMixxi == true)
				{

					Mixxi.speak("I am Mixxi the intelligent driving assistant", TextToSpeech.QUEUE_FLUSH, null);
					checkMatch = true ;
					break;
				}
				else if((result.contains("how are you") || result.contains("สบายดีไหม") || result.contains("เป็นไง"))  && checkMixxi == true)
				{

					Mixxi.speak("I'm ok and you?", TextToSpeech.QUEUE_FLUSH, null);
					checkMatch = true ;
					break;
				}
				else if((result.contains("i'm fine") || result.contains("สบายดี"))  && checkMixxi == true)
				{

					Mixxi.speak("alright", TextToSpeech.QUEUE_FLUSH, null);
					checkMatch = true ;
					break;
				}
				else if((result.contains("what can you do") || result.contains("ทำอะไรได้"))  && checkMixxi == true)
				{

					Mixxi.speak("I can do everything", TextToSpeech.QUEUE_FLUSH, null);
					checkMatch = true ;
					break;
				}
				else if((result.contains("really") || result.contains("จริงหรอ")|| result.contains("จริงหรือ"))  && checkMixxi == true)
				{

					Mixxi.speak("mai jing", TextToSpeech.QUEUE_FLUSH, null);
					checkMatch = true ;
					break;
				}
				else if((result.contains("who is the most handsome") || result.contains("ใครหน้าตาดีที่สุด"))  && checkMixxi == true)
				{

					Mixxi.speak("kon tum project", TextToSpeech.QUEUE_FLUSH, null);
					checkMatch = true ;
					break;
				}
				else if((result.contains("f*** you") || result.contains("ค**"))  && checkMixxi == true)
				{

					Mixxi.speak("you son of the bitch", TextToSpeech.QUEUE_FLUSH, null);
					checkMatch = true ;
					break;
				}
				else if((result.contains("avada kedavra")|| (result.contains("อบตคือดร") ))  && checkMixxi == true)
				{

					Mixxi.speak("your dad is joke", TextToSpeech.QUEUE_FLUSH, null);
					checkMatch = true ;
					break;
				}
				else if((result.contains("ใครสรา้ง")|| (result.contains("where are you from") ))  && checkMixxi == true)
				{

					Mixxi.speak("Mix and Captain", TextToSpeech.QUEUE_FLUSH, null);
					checkMatch = true ;
					break;
				}


				else if((result.contains("เก่งจริง") || result.contains("you are good")) && checkMixxi == true)
				{

					Mixxi.speak("nae norn ka", TextToSpeech.QUEUE_FLUSH, null);
					checkMatch = true ;
					break;
				}
				else if((result.contains("sing ช้าง") || result.contains("ร้องเพลงช้าง"))  && checkMixxi == true)
				{

					Mixxi.speak("chang chang chang chang chang jo jo jo nong ko hen chang rue pao jo jo jo chang man tua to mai bao" +
							"ja mook kor yao leg wa wuang jo jo jo", TextToSpeech.QUEUE_FLUSH, null);
					checkMatch = true ;
					break;
				}
				else if((result.contains("sing a song") || result.contains("ร้องเพลง")) && checkMixxi == true)
				{

					Mixxi.speak("tan kamlang kao su borikan rub fak hua jai jo jo jo long thabian fak wai tua ao kub pai jai hai kep rak sa jo jo jo", TextToSpeech.QUEUE_FLUSH, null);
					checkMatch = true ;
					break;
				}

				else if((result.contains("thank you") || result.contains("ขอบคุณ")) && checkMixxi == true)
				{

					Mixxi.speak("You're welcome", TextToSpeech.QUEUE_FLUSH, null);
					checkMatch = true ;
					break;
				}



				else
				{
					checkMatch = false;
				}




			}
			if(checkMixxi == true)
			{
				message.setText(result_list.get(0));
			}



			if(result_list.size()==0 || (checkMatch == false && checkMixxi == true))
			{
				Mixxi.speak("I don't know what you said", TextToSpeech.QUEUE_FLUSH, null);
			}


			//Log.d("result", String.valueOf(result_list.size()));


		}
	};

	Handler handlerShowDetectLevel = new Handler()
	{

		@Override
		public void handleMessage(Message msg)
		{
			/*if(speechDisable == true) {
				recognizer.stop();
			}
			else
			{*/
				//Log.d("Speech: ", "3" + speechDisable);
				if (recognizer != null) {
					int detectLevel = recognizer.getDetectLevel();
					//Log.d("TestVol", String.valueOf(detectLevel) + ready);
					//((TextView) findViewById(R.id.detect_level)).setText("Current detect volume level: " + detectLevel);
					if (!ready && detectLevel < 50000) {
						ready = true;
						//((TextView) findViewById(R.id.text)).setText(R.string.speak);
					}
				}
				handlerShowDetectLevel.sendEmptyMessageDelayed(0, 500);
			//}



		}
	};

}
