package com.muwuprojects.portablepocketperson;

import java.util.Locale;
import java.util.Random;


import android.content.Context;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.os.CountDownTimer;
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
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
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
	TextView timerText;
	ScrollView myScroll;
	Conversation defaultConversation = new HappyConversation(MODE_TEXT);
	String prePhrase = "";

	CountDownTimer myCountDown = null;
	int speechKickerTick=0; //when to say a phrase
	int speechKickerCount=0; //the counter for when to say a phrase
	int nextTimeAlert=0;

	// playback modes
	public static final int MODE_TEXT=0;
	public static final int MODE_SPEECH=1;

	private int lastRandom=-1;
	// the number of random conversations to choose from
	public static final int MAX_RANDOM=4;

	// different modes for the main activity
	public static final int STATE_MIND_START = 0;
	public static final int STATE_MIND_WHAT = 1;
	public static final int STATE_MIND_RESTART = 2;
	public static final int STATE_MIND_OFFLOAD = 3;
	public static final int STATE_PLAY_SEQUENCE = 4;
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

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		InitializeSpeechEngine();

		myText = (AutoCompleteTextView) findViewById(R.id.userText);
		theConversation = (TextView) findViewById(R.id.textView1);
		timerText = (TextView) findViewById(R.id.timerText);
		timerText.setVisibility(View.INVISIBLE);
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
		
		myText.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View arg0, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
						(keyCode == KeyEvent.KEYCODE_ENTER)) {
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
		//if (tts!=null)
		//{
	//		tts.stop();
//			tts.shutdown();
			//textToSpeechEnabled=false;
			//tts=null;
		//}

		//only the user can stop the countdown
		//if(myCountDown != null)
		//{
		//	myCountDown.cancel();
		//	timerText.setVisibility(View.INVISIBLE);
		//}

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
				publishPhrase("Ah, continue.");
			else if(r==2)
				publishPhrase("Yes, I see.");
			else if(r==3)
				publishPhrase("OK, I understand.");
			else
				publishPhrase("(Gentle nod).");
		}
		else if(state==STATE_PLAY_SEQUENCE)
		{
			state=STATE_MIND_WHAT;
			publishPhrase("Stopping play back mode ...");
			timerText.setVisibility(View.INVISIBLE);
			myCountDown.cancel();
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			toggleSpeech();
			Phrase thePhrase = defaultConversation.getWholePhrase();
			publishPhrase(thePhrase.getPhrase());

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
		myScroll.post(new Runnable() {
			public void run() {
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
		else if(cmd.contentEquals("about"))
		{
			publishPhrase("Thanks for your interest in this app");
			Intent i = new Intent(getApplicationContext(), AboutActivity.class);
			startActivity(i);

			return true;
		}
		else if(cmd.startsWith("play"))
		{
			// check speech engine
			InitializeSpeechEngine();
			if(textToSpeechSupported==false)
			{
				publishPhrase("Speech not working for some reason");
				return true;
			}

			if(checkPlaySequence(cmd)==false) {
				publishPhrase("Hmm, I'm not sure which sequence to play. Currently you can choose " +
						"zen, simple or anapanasati, e.g. play zen or play ana");
			}
			return true;

		}
		else if(cmd.contentEquals("speak"))
		{
			toggleSpeech();
		}
		else if(cmd.contentEquals("") && emptyOK == false)
		{
			publishPhrase("You need to say something here.");
			return true;
		}
		return false;
		
	}

	private boolean checkPlaySequence(String cmdIn)
	{
		String[] separated = cmdIn.split(" ");
		int timerTime=5; //minutes

		if(separated.length<2)
		{
			return false;
		}
		String cmd = separated[0];
		String choice = separated[1].toLowerCase();

		if(choice.contentEquals("zen"))
		{
			//load the zen play back conversation
			loadZen(MODE_SPEECH);
			timerTime=10; // that's 10 minutes
		}
		else if(choice.contentEquals("ana"))
		{
			loadAnaPanaSati(MODE_SPEECH);
			timerTime=15; // that's 15 minutes
		}
		else if(choice.contentEquals("simple"))
		{
			loadSimple(MODE_SPEECH);
			timerTime=5;
		}
		else
		{
			return false;
		}

		int totalPhrases = defaultConversation.getNumPhrases();
		speechKickerTick=60*timerTime/totalPhrases; // divide them evenly for now

		publishPhrase("Starting timer ...");
		//publishPhrase("Starting timer ... " + Integer.toString(totalPhrases) + " split into " + Integer.toString(speechKickerTick));
		textToSpeechEnabled=true;
		// say the first phrase
		Phrase thePhrase = defaultConversation.getWholePhrase();
		publishPhrase(thePhrase.getPhrase());

		startTimer(timerTime);
		// work out how many phrase we've got
		// then tick the conversation along automatically
		state = STATE_PLAY_SEQUENCE;

		return true;

	}

	private void startTimer(int timerTime)
	{
		if(myCountDown!=null)
		{
			myCountDown.cancel();
			myCountDown = null;
		}

		speechKickerCount=speechKickerTick; // wait before saying something
		nextTimeAlert = timerTime*60 - speechKickerTick; // when we expect the next message

		myCountDown = new CountDownTimer(timerTime*1000*60, 1000) {

			public void onTick(long millisUntilFinished) {
				//timerText.setText("Seconds remaining: " + millisUntilFinished / 1000);
				int secondsLeft = (int)(millisUntilFinished/1000)%60;
				int minutesLeft = (int)(millisUntilFinished/60000);
				//timerText.setText(" " + Long.toString(millisUntilFinished / 1000) + " ");
				if(secondsLeft<10)
			    	timerText.setText(" " + Integer.toString(minutesLeft) + ":0" + Integer.toString(secondsLeft));
				else
					timerText.setText(" " + Integer.toString(minutesLeft) + ":" + Integer.toString(secondsLeft));

				//if(secondsLeft<10)
				//	timerText.setText(" " + Integer.toString((int)millisUntilFinished/1000) + " " + Integer.toString(speechKickerCount) + " " + Integer.toString(nextTimeAlert) + " ");
				//else
				//	timerText.setText(" " + Integer.toString((int)millisUntilFinished/1000) + " " + Integer.toString(speechKickerCount) + " " + Integer.toString(nextTimeAlert) + " ");

				if(nextTimeAlert>(int)millisUntilFinished/1000)
				{
					// somehow we missed some time, perhaps the phone went to sleep
					speechKickerCount=0;
				}

				if(speechKickerCount==0) {
					// time to say the current phrase out loud

					speechKickerCount = speechKickerTick;
					nextTimeAlert = (int)millisUntilFinished/1000 - speechKickerTick;
					defaultConversation.moveOn();
					if (defaultConversation.isFinished() == true) {
						if (defaultConversation.getNextConversation() != null) {
							defaultConversation = defaultConversation.getNextConversation();
						} else {
							defaultConversation = getRandomConversation();
						}
					}
					else {
						Phrase thePhrase = defaultConversation.getWholePhrase();
						publishPhrase(thePhrase.getPhrase());
					}
				}
				speechKickerCount--;
			}

			public void onFinish() {
				publishPhrase("Your meditation is complete. Take a few moments to relax before resuming your day");
				timerText.setVisibility(View.INVISIBLE);
				state=STATE_MIND_WHAT;
				toggleSpeech();
				getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			}
		};

		timerText.setVisibility(View.VISIBLE);
		myCountDown.start();
	}

	private void toggleSpeech()
	{
		if(textToSpeechEnabled==false)
		{
			InitializeSpeechEngine();
			if(textToSpeechSupported==true)
			{
				publishPhrase("Speech enabled");
				textToSpeechEnabled=true;
			}
			else
				publishPhrase("Speech not supported");
		}
		else
		{
			textToSpeechEnabled=false;
			publishPhrase("Speech disabled");
		}
	}

	private Conversation getRandomConversation() {
		Conversation randomConversation = new ZenConversation(MODE_TEXT);
		Random rand = new Random();
		int r = rand.nextInt(MAX_RANDOM);

		if(r==lastRandom)
			r++;

		if(r>MAX_RANDOM)
			r=0;

		if(r==0)
			randomConversation = new ListenConversation(MODE_TEXT);
		else if(r==1)
			randomConversation = new ZenConversation(MODE_TEXT);
		else if(r==2)
			randomConversation = new AnaPanaConversation(MODE_TEXT);
		else if(r==3)
			randomConversation = new ThreeMinuteConversation(MODE_TEXT);

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
			publishPhrase("Mindfulness is the simple, effortless knowing of what is going on. Hit next " +
					"to begin an exercise ...");
			state=STATE_MIND_RESTART;
			defaultConversation = new ListenConversation(MODE_TEXT);
			lastRandom=0;
			return true;
		case R.id.zen:
			publishPhrase("Let's try some zazen. Hit next to begin ...");
			loadZen(MODE_TEXT);
			return true;
		case R.id.threemin:
			publishPhrase("Time to move on. Hit next to begin ...");
			state=STATE_MIND_RESTART;
			defaultConversation = new ThreeMinuteConversation(MODE_TEXT);
			lastRandom=3;
			return true;
		case R.id.check_in:
			publishPhrase("Time to move on. Hit next to begin ...");
			state=STATE_MIND_RESTART;
			defaultConversation = new CheckInConversation(MODE_TEXT);
			return true;
		case R.id.anapana:
			publishPhrase("Time to move on. Hit next to begin ...");
			loadAnaPanaSati(MODE_TEXT);
			return true;
		//case R.id.choose:
			// when we have extra activities to do 
        	//Intent i = new Intent(getApplicationContext(), ActivitiesActivity.class);
        	//startActivity(i);
			
			//this.finish();
			//return true;
		case R.id.about:
			publishPhrase("Thanks for your interest in this app");
			Intent i = new Intent(getApplicationContext(), AboutActivity.class);
			startActivity(i);

			return true;

		}
		return super.onOptionsItemSelected(item);
	}

	void loadZen(int playBackMode)
	{
		state=STATE_MIND_RESTART;
		defaultConversation = new ZenConversation(playBackMode);
		lastRandom=1;
	}

	void loadAnaPanaSati(int playBackMode)
	{
		state=STATE_MIND_RESTART;
		defaultConversation = new AnaPanaConversation(playBackMode);
		lastRandom=2;
	}

	void loadSimple(int playBackMode)
	{
		state=STATE_MIND_RESTART;
		defaultConversation = new SimpleConversation(playBackMode);
		lastRandom=-1;

	}

}
