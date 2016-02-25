package com.muwuprojects.portablepocketperson;

import java.util.Locale;
import java.util.Random;


import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	AutoCompleteTextView myText;
	Button myButton;
	TextView theConversation;
	ScrollView myScroll;
	Conversation defaultConversation = new HappyConversation();
	String prePhrase = "";

	private int lastRandom=-1;
	public static final int MAX_RANDOM=4;
	public static final int STATE_MIND_START = 0;
	public static final int STATE_MIND_WHAT = 1;
	public static final int STATE_MIND_RESTART = 2;
	public static final int STATE_MIND_OFFLOAD = 3;
	int state=STATE_MIND_START;

	private boolean textToSpeechEnabled=false;
	private boolean textToSpeechSupported=false;
	private TextToSpeech tts=null;

	private DatabaseManager db = new DatabaseManager(this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setTitle("minpa");

		InitializeSpeechEngine();

		myText = (AutoCompleteTextView) findViewById(R.id.userText);
		theConversation = (TextView) findViewById(R.id.textView1);
		long lastVisitLong = db.getLastDate();
		if(lastVisitLong==0)
		{
			theConversation.setText(Html.fromHtml("<b>Welcome to minpa, a simple way to explore mindfulness. Type" +
					" help (h) for assistance at any time. The idea is to chat with the app, but there's also " +
					"a menu if you want to jump to a practice.</b>"));
			//give the user some simple instructions
			db.createDate();
			theConversation.append(Html.fromHtml("<br /><br />"));
			theConversation.append(Html.fromHtml("<b>How are you today?</b>"));
			theConversation.append(Html.fromHtml("<br /><br />"));
		}
		else
		{
			//long numDays = db.getNumDaysSinceLastVisit();
			long numHours = db.getNumHoursSinceLastVisit();
    		//String dateString = (String) DateFormat.format("dd/MM/yyyy",lastVisitLong);
			if(numHours<12)
			{
				theConversation.setText(Html.fromHtml("<b>Welcome back, let's do some mindfulness</b>"));
				//let the user do some exercises straight away
				theConversation.append(Html.fromHtml("<br /><br />"));
				theConversation.append(Html.fromHtml("<b>Admire your surroundings and then begin...</b>"));
				theConversation.append(Html.fromHtml("<br /><br />"));
				state=STATE_MIND_RESTART;
			}
			else
			{
				theConversation.setText(Html.fromHtml("<b>Welcome back again</b>"));
				//ask for feelings and record them here
				theConversation.append(Html.fromHtml("<br /><br />"));
				theConversation.append(Html.fromHtml("<b>How are you today?</b>"));
				theConversation.append(Html.fromHtml("<br /><br />"));
			}
			db.updateDate();
		}
		myScroll = (ScrollView) findViewById(R.id.scrollView1);
	 
		String[] vedana = getResources().getStringArray(R.array.vedana);
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, vedana);
		
		myText.setAdapter(adapter);
        //myText.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
	
		//publishPhrase(db.getLastDate());
		//theConversation.setText(db.getLastDate());
		
		myText.setOnEditorActionListener(new OnEditorActionListener (){

			@Override
			public boolean onEditorAction(TextView arg0, int actionId, KeyEvent event) {
				// TODO Auto-generated method stub
				//myText.showDropDown();
				if(actionId == EditorInfo.IME_ACTION_NEXT){
					submitText();					
					return true;
				}
				return false;
			}
			
		});
		
		myText.setOnKeyListener(new OnKeyListener (){

			@Override
			public boolean onKey(View arg0, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				if((event.getAction() == KeyEvent.ACTION_DOWN) && 
						(keyCode == KeyEvent.KEYCODE_ENTER))
				{
					//submitText();
					//Toast.makeText(getApplicationContext(), "hih", Toast.LENGTH_SHORT);
					
					return true;
				}
				return false; 
			}
			
		});
		
		
		
	}

	@Override
	protected void onPause(){
		if (tts!=null)
		{
			tts.stop();
			tts.shutdown();
			textToSpeechEnabled=false;
			tts=null;
		}
		super.onPause();
	}

	void InitializeSpeechEngine(){
		if(tts!=null)
			return;

		tts = new TextToSpeech(MainActivity.this, new TextToSpeech.OnInitListener() {
			@Override
			public void onInit(int status) {
				if(status==TextToSpeech.SUCCESS){
					int result = tts.setLanguage(Locale.getDefault());
					if(result == TextToSpeech.LANG_MISSING_DATA || result==TextToSpeech.LANG_NOT_SUPPORTED)
					{
						// not supported
						textToSpeechSupported=false;
					}
					else
					{
						textToSpeechSupported=true;
					}
				}
			}
		});

	}

	protected void submitText() {
		// TODO Auto-generated method stub
		String newText="";
		
		String cmd = myText.getText().toString();
		
		if(cmd.contentEquals("y"))
			cmd = "yes";
		
		if(cmd.contentEquals("n"))
			cmd="no";
		
		if(specialCommand(cmd)==true)
		{
			return;
		}
		
		if(state==STATE_MIND_WHAT)
		{
			Phrase thePhrase = defaultConversation.getWholePhrase();
			
			
			if(thePhrase!=null)
			{
				if(checkExpectedResponse(cmd, thePhrase)==true)
				{
					// move on and get the next phrase
					defaultConversation.moveOn();
					if(defaultConversation.isFinished()==true)
					{
						if(defaultConversation.getNextConversation()!=null)
						{
							defaultConversation = defaultConversation.getNextConversation();
						}
						else
						{
							defaultConversation = getRandomConversation();
						}
					}
					thePhrase = defaultConversation.getWholePhrase();
					publishPhrase(thePhrase.getPhrase());
					//publishPhrase(defaultConversation.getStatus());
				}
				
			}
			else
			{
				publishPhrase("Hmm, something unexpected has happened");				
			}
		}
		else if(state==STATE_MIND_START || state==STATE_MIND_RESTART)
		{
			state=STATE_MIND_WHAT;			
			Phrase thePhrase = defaultConversation.getWholePhrase();
			publishPhrase(thePhrase.getPhrase());
			
		}
		else if(state==STATE_MIND_OFFLOAD)
		{
			Random rand = new Random();
			int r = rand.nextInt(6);
			if(r==0)
				publishPhrase("I see, go on.");
			else if(r==1)
				publishPhrase("Uh huh, continue.");
			else if(r==2)
				publishPhrase("Yes, I see.");
			else if(r==3)
				publishPhrase("OK, I understand.");
			else
				publishPhrase("(Gentle nod).");
		}
		else
		{
			publishPhrase("Hmm, something strange has happened");
			state=STATE_MIND_WHAT;
		}
				
	}

	private boolean checkExpectedResponse(String cmd, Phrase thePhrase) {
		// TODO Auto-generated method stub
		cmd = cmd.toLowerCase(Locale.ENGLISH);
		
		if(thePhrase.checkResponse(cmd)==true)
		{
			String miniPhrase = thePhrase.getResponsePhrase(cmd);
			if(miniPhrase!=null)
				prePhrase = miniPhrase;

			defaultConversation.responseDelivered(cmd);
			
			return true;
		}
		publishPhrase(thePhrase.getResponses());
		return false;
	}

	private void publishPhrase(String phrase) {
		String newText;

		if(tts!=null && textToSpeechEnabled==true)
		{
			// replace any <br/> that might have crept in
			String speakText = phrase.replaceAll("\\<.*?\\> ?","  ");
			tts.speak(speakText, TextToSpeech.QUEUE_FLUSH,null);
		}
		
		if(prePhrase=="")
			newText = "<b>" + phrase + "</b>";
		else
			newText = "<br /><br /><b>" + prePhrase +"</b><br /><br />" + "<b>" + phrase + "</b>";

		prePhrase = "";
		
		Spanned bigtext = Html.fromHtml("> "
				+ myText.getText() + "<br /><br />" + 
				newText + "<br /><br />");
		theConversation.append(bigtext);
		myText.setText("");
		myScroll.post(new Runnable(){
			public void run(){
				myScroll.fullScroll(View.FOCUS_DOWN);
			}
		});
		//myScroll.fullScroll(View.FOCUS_DOWN);
		//myScroll.scrollTo(0, myScroll.getBottom()+1200);
		
	}


	private boolean specialCommand(String cmd) {
		// TODO Auto-generated method stub
		
		cmd = cmd.toLowerCase(Locale.ENGLISH);
		String helpPhrase="";
		boolean emptyOK = false;
		
		if(state==STATE_MIND_START)
		{
			helpPhrase= "No pressure, just say what you feel";
		}
		else if(state==STATE_MIND_RESTART)
		{
			helpPhrase= "Just hit the next key if you ever wonder what to do.";
			emptyOK = true;
		}
		else if(state==STATE_MIND_OFFLOAD)
		{
			if(cmd.contentEquals(""))
			{
				state = STATE_MIND_RESTART;
				defaultConversation = getRandomConversation();
				helpPhrase = "Thanks, let's move on. Hit next when you are ready ...";
				publishPhrase(helpPhrase);
				return true;
			}
			helpPhrase = "Just keep letting it all out ... I am here to listen. When you are ready, give me some empty text to finish ...";
		}
		else
		{
			emptyOK = defaultConversation.emptyIsOK();
			helpPhrase = defaultConversation.getHelp();
		}
		
		if(cmd.contains("help") || cmd.contentEquals("h") || cmd.contentEquals("?"))
		{
			publishPhrase(helpPhrase);
			return true;
		}
		else if(cmd.startsWith("what"))
		{
			publishPhrase(helpPhrase);
			return true;			
		}
		else if(cmd.contentEquals("offload"))
		{
			publishPhrase("I am ready to listen, tell me all about it.");
			state = STATE_MIND_OFFLOAD;
			return true;			
		}
		else if(cmd.contentEquals("speak"))
		{
			if(textToSpeechEnabled==false)
			{
				InitializeSpeechEngine();
				textToSpeechEnabled=true;
				if(textToSpeechSupported==true)
					publishPhrase("Speech enabled");
				else
					publishPhrase("Speech not supported");
			}
			else
			{
				textToSpeechEnabled=false;
				publishPhrase("Speech disabled");
			}
		}
		else if(cmd.contentEquals("") && emptyOK == false)
		{
			publishPhrase("You need to say something here.");
			return true;
		}
		return false;
		
	}

	private Conversation getRandomConversation() {
		Conversation randomConversation = new ZenConversation();
		Random rand = new Random();
		int r = rand.nextInt(MAX_RANDOM);

		if(r==lastRandom)
			r++;

		if(r>MAX_RANDOM)
			r=0;

		if(r==0)
			randomConversation = new ListenConversation();
		else if(r==1)
			randomConversation = new ZenConversation();
		else if(r==2)
			randomConversation = new AnaPanaConversation();
		else if(r==3)
			randomConversation = new ThreeMinuteConversation();

		lastRandom = r;

		return randomConversation;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.simple:
			publishPhrase("Mindfulness is the simple, effortless knowing of what is going. Hit next " +
					"to begin an exercise ...");
			state=STATE_MIND_RESTART;
			defaultConversation = new ListenConversation();
			lastRandom=0;
			return true;
		case R.id.zen:
			publishPhrase("Let's try some zazen. Hit next to begin ...");
			state=STATE_MIND_RESTART;
			defaultConversation = new ZenConversation();
			lastRandom=1;
			return true;
		case R.id.threemin:
			publishPhrase("Time to move on. Hit next to begin ...");
			state=STATE_MIND_RESTART;
			defaultConversation = new ThreeMinuteConversation();
			lastRandom=3;
			return true;
		case R.id.check_in:
			publishPhrase("Time to move on. Hit next to begin ...");
			state=STATE_MIND_RESTART;
			defaultConversation = new CheckInConversation();
			return true;
		case R.id.anapana:
			publishPhrase("Time to move on. Hit next to begin ...");
			state=STATE_MIND_RESTART;
			defaultConversation = new AnaPanaConversation();
			lastRandom=2;
			return true;
		//case R.id.choose:
			// when we have extra activities to do 
        	//Intent i = new Intent(getApplicationContext(), ActivitiesActivity.class);
        	//startActivity(i);
			
			//this.finish();
			//return true;
		case R.id.about:
			publishPhrase("Version 1.03. All rights reserved, 2016. I made this app to help with " +
					"regular mindfulness practice. I hope you find it useful. " +
					"Hit next " +
					"to carry on or type help (h) for help...");
			state=STATE_MIND_RESTART;
			if(defaultConversation.getNextConversation()!=null)
			{
				defaultConversation = defaultConversation.getNextConversation();
			}
			else
			{
				defaultConversation = new AnaPanaConversation();
			}
			return true;

		}
		return super.onOptionsItemSelected(item);
	}


}
