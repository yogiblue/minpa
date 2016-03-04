package com.muwuprojects.portablepocketperson;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

class Phrase {
	String phrase;
	String help;
	ArrayList<String> expectedResponse = new ArrayList<String>();
	ArrayList<String> expectedResponsePhrase = new ArrayList<String>();
	
	public Phrase(String phraseIn)
	{
		help = "";
		phrase = phraseIn;
	}

	public void setHelp(String string) {
		// TODO Auto-generated method stub
		help = string;
	}

	public String getPhrase() {
		// TODO Auto-generated method stub
		return phrase;
	}

	public String getHelp() {
		// TODO Auto-generated method stub
		return help;
	}

	public void addResponse(String string) {
		// TODO Auto-generated method stub
		expectedResponse.add(string);
	}

	public boolean checkResponse(String cmd) {
		// TODO Auto-generated method stub
		if(expectedResponse.size()==0)
			return true;
		
		for(int i=0;i<expectedResponse.size();i++)
		{
			if(cmd.contentEquals(expectedResponse.get(i).toLowerCase(Locale.ENGLISH)))
					return true;
		}
		
		
		return false;
	}

	public String getResponses() {
		// TODO Auto-generated method stub
		String myResponse = "I was expecting: ";
		for(int i=0;i<expectedResponse.size();i++)
		{
			if(i<expectedResponse.size()-1)
				myResponse = myResponse + expectedResponse.get(i) + ", ";
			else
				myResponse = myResponse + expectedResponse.get(i);
		}
		return myResponse;
	}

	public void addResponse(String string, Phrase extensionPhrase1) {
		// TODO Auto-generated method stub
		expectedResponse.add(string);
		//expectedResponsePhrase.add(extensionPhrase1);
		
	}

	public void addResponse(String string, String string2) {
		// TODO Auto-generated method stub
		expectedResponse.add(string);
		expectedResponsePhrase.add(string2);
		
	}

	public String getResponsePhrase(String cmd) {
		// TODO Auto-generated method stub
		if(expectedResponsePhrase.size()==0)
			return null;
		
		for(int i=0;i<expectedResponse.size();i++)
		{
			if(cmd.contentEquals(expectedResponse.get(i)))
					return expectedResponsePhrase.get(i);
		}
		return null;
	}
}

public class Conversation {
	
	int state=0;
	int max_questions=1;
	protected ArrayList<String> phrases = new ArrayList<String>();
	protected ArrayList<String> help = new ArrayList<String>();
	protected ArrayList<Phrase> allPhrases = new ArrayList<Phrase>();
	protected boolean finished;
	protected boolean emptyAllowed=true;
	Conversation nextConversation=null;
	
	public Conversation(int mode){
		finished = false;
	}
	
	public String getPhrase()
	{
		if(state>=max_questions)
		{
			return "We have finished that conversation";
		}
		state++;
		if(state>=max_questions)
		{
			finished = true;
		}
		return allPhrases.get(state-1).getPhrase();
	}

	public int getNumPhrases()
	{
		return max_questions;
	}

	public boolean isFinished() {
		// TODO Auto-generated method stub
		return finished;
	}

	public String getHelp() {
		// TODO Auto-generated method stub
		if(state>=0)
			return allPhrases.get(state).getHelp();
		else
			return "No help to give here. Do your best.";
	}
	
	public boolean emptyIsOK()
	{
		return emptyAllowed;
	}

	public Phrase getWholePhrase() {
		// TODO Auto-generated method stub
		if(state>=max_questions)
		{
			return null;
		}
		return allPhrases.get(state);
	}

	public void moveOn() {
		// TODO Auto-generated method stub
		state++;
		if(state>=max_questions)
		{
			finished = true;
		}
		if(state>=max_questions-1)
			emptyAllowed=true;
		
	}
	
	public String getStatus()
	{
		String myString = Integer.toString(state) + ":" + Integer.toString(max_questions); 
		return myString;
	}

	public void responseDelivered(String cmd) {
	}

	public Conversation getNextConversation() {
		// TODO Auto-generated method stub
		return nextConversation;
	}

}

class HappyConversation extends Conversation{

	public HappyConversation(int mode){
		super(mode);
		state=0;
		emptyAllowed=false;
		Phrase myPhrase;
		Phrase extensionPhrase1;
		Phrase extensionPhrase2;
		extensionPhrase1 = new Phrase("Why are you happy?");
		extensionPhrase2 = new Phrase("Why are you unhappy?");

		myPhrase = new Phrase("Are you happy (yes/no/maybe)?");
		myPhrase.setHelp("Don't think too long. Are you happy at this moment?");
		myPhrase.addResponse("yes");
		myPhrase.addResponse("no");
		myPhrase.addResponse("maybe");
		allPhrases.add(myPhrase);
		max_questions = allPhrases.size();
	}

	public void responseDelivered(String cmd) {
		if(state==0)
		{
			finished = false;
			if(cmd.contentEquals("yes"))
			{
				Phrase myPhrase;
				myPhrase = new Phrase("That's great, take a moment to enjoy it and then continue ...");
				myPhrase.setHelp("You have recognised happiness in yourself.");
				allPhrases.add(myPhrase);
				emptyAllowed = true;
				nextConversation = new CheckInConversation(MainActivity.MODE_TEXT);
				max_questions = allPhrases.size();
				
			}
			else if (cmd.contentEquals("no"))
			{
				Phrase myPhrase;
				myPhrase = new Phrase("OK. Do you want to talk?");
				myPhrase.setHelp("Yes or no please");
				allPhrases.add(myPhrase);
				myPhrase.addResponse("yes");
				myPhrase.addResponse("no");
				nextConversation = new CheckInConversation(MainActivity.MODE_TEXT);
				max_questions = allPhrases.size();				
			}
			else if (cmd.contentEquals("maybe"))
			{
				Phrase myPhrase;
				myPhrase = new Phrase("It's good to be somewhere in the middle. Balance is healthy." +
						" Hit next when you are ready...");
				myPhrase.setHelp("Keep resting in the middle. Hit next to carry on...");
				allPhrases.add(myPhrase);
				emptyAllowed = true;
				nextConversation = new CheckInConversation(MainActivity.MODE_TEXT);
				max_questions = allPhrases.size();
			}
		}			
		else if(state==1)
		{
			finished = false;
			if(cmd.contentEquals("yes"))
			{
				Phrase myPhrase;
				myPhrase = new Phrase("You can vent your frustrations at any time by typing the 'vent' command. " +
						"Keep writing sentences until you've got it all out and then enter an empty " +
						"sentence to finish.");
				myPhrase.setHelp("Just let it all out");
				myPhrase.addResponse("vent");
				allPhrases.add(myPhrase);
				nextConversation = null;
				max_questions = allPhrases.size();
				
			}
			else if(cmd.contentEquals("no"))
			{
				emptyAllowed = true;
				Phrase myPhrase;
				myPhrase = new Phrase("That's OK. Let's move on. You can select an exercise from the menu or just hit carry on ...");
				myPhrase.setHelp("All things change.");
				allPhrases.add(myPhrase);
				nextConversation = null;
				max_questions = allPhrases.size();
				
			}
			
		}
	}
}

class ZenConversation extends Conversation{

	public ZenConversation(int mode){
		super(mode);
		state=0;
		emptyAllowed=true;
		Phrase myPhrase;

		if(mode==MainActivity.MODE_TEXT) {
			myPhrase = new Phrase("In Zen, we just sit. <br /><br />Remember, type help (h) at any time for extra instructions.");
			myPhrase.setHelp("Find a comfortable upright posture and sit still.");
			allPhrases.add(myPhrase);
		}
		else if(mode==MainActivity.MODE_SPEECH)
		{
			myPhrase = new Phrase("In Zen, we just sit. This meditation will last 10 minutes. Relax and take a deep breath.");
			myPhrase.setHelp("Find a comfortable upright posture and sit still.");
			allPhrases.add(myPhrase);
		}


		myPhrase = new Phrase("Place your attention on the rising and falling of the belly.");
		myPhrase.setHelp("Notice it rising and falling.");
		allPhrases.add(myPhrase);

		myPhrase = new Phrase("Keep sitting.");
		myPhrase.setHelp("Be still, empty the mind.");
		allPhrases.add(myPhrase);

		myPhrase = new Phrase("Notice the mind drifting off. Maintain awarenesss of the breath.");
		myPhrase.setHelp("Bring the mind back to the breath.");
		allPhrases.add(myPhrase);

		myPhrase = new Phrase("The posture is the expression of the practice. The breath unifies body and mind.");
		myPhrase.setHelp("Keep sitting. Be still.");
		allPhrases.add(myPhrase);

		myPhrase = new Phrase("Be still, empty the mind. Rest on the breath.");
		myPhrase.setHelp("Keep sitting.");
		allPhrases.add(myPhrase);

		if(mode==MainActivity.MODE_TEXT) {
			myPhrase = new Phrase("Sit as long as you can.");
			myPhrase.setHelp("Keep sitting. Be still.");
			allPhrases.add(myPhrase);

			myPhrase = new Phrase("When you are ready, hit next ...");
			myPhrase.setHelp("Take any stillness into the next moments of your day.");
			allPhrases.add(myPhrase);
		}
		else if(mode==MainActivity.MODE_SPEECH)
		{
			myPhrase = new Phrase("Maintain awareness of the breath.");
			myPhrase.setHelp("Keep sitting. Be still.");
			allPhrases.add(myPhrase);

			myPhrase = new Phrase("Find stillness.");
			myPhrase.setHelp("Take any stillness into the next moments of your day.");
			allPhrases.add(myPhrase);
		}


		nextConversation = new CoolDownConversation(MainActivity.MODE_TEXT);

		max_questions = allPhrases.size();
	}

}

class CoolDownConversation extends Conversation{

	public CoolDownConversation(int mode){
		super(mode);
		state=0;
		emptyAllowed=true;
		Phrase myPhrase;

		myPhrase = new Phrase("You can use the menu to choose another exercise or carry on for some pointers...");
		myPhrase.setHelp("Time to decide.");
		allPhrases.add(myPhrase);

		myPhrase = new Phrase("After meditation, avoid jumping back into everyday life.");
		myPhrase.setHelp("Spend time watching phenomena.");
		allPhrases.add(myPhrase);

		myPhrase = new Phrase("Notice the body.");
		myPhrase.setHelp("Keep relaxing");
		allPhrases.add(myPhrase);

		myPhrase = new Phrase("Notice how you feel.");
		myPhrase.setHelp("Pleasant, unpleasant, neutral");
		allPhrases.add(myPhrase);

		myPhrase = new Phrase("Notice the atmosphere of the mind.");
		myPhrase.setHelp("Is it agitated? Still? Swirling? Tight? Relaxed?");
		allPhrases.add(myPhrase);

		myPhrase = new Phrase("Notice thoughts arising.");
		myPhrase.setHelp("They are a natural part of the mind");
		allPhrases.add(myPhrase);
		
		myPhrase = new Phrase("When standing, just stand.");
		myPhrase.setHelp("Be present");
		allPhrases.add(myPhrase);

		myPhrase = new Phrase("When walking, just walk.");
		myPhrase.setHelp("Be present");
		allPhrases.add(myPhrase);

		myPhrase = new Phrase("Avoid the tangle.");
		myPhrase.setHelp("Recognise openness");
		allPhrases.add(myPhrase);

		myPhrase = new Phrase("The more you meditate, the more you will understand the mind.");
		myPhrase.setHelp("See through the tangle");
		allPhrases.add(myPhrase);

		myPhrase = new Phrase("Understand the mind and experience a sense of freedom.");
		myPhrase.setHelp("No self, complete freedom");
		allPhrases.add(myPhrase);

		nextConversation = new HowWasItConversation(MainActivity.MODE_TEXT);

		max_questions = allPhrases.size();
	}

}

class HowWasItConversation extends Conversation{

	public HowWasItConversation(int mode){
		super(mode);
		state=0;
		emptyAllowed=false;
		Phrase myPhrase;

		myPhrase = new Phrase("Take a moment to tell me about your last meditation.");
		myPhrase.setHelp("What did you notice? What happened?.");
		allPhrases.add(myPhrase);

		myPhrase = new Phrase("Thank you. Reviewing your meditation experience is very helpful.");
		myPhrase.setHelp("There is always more to explore in the mind");
		allPhrases.add(myPhrase);
		
		nextConversation = new FillerConversation(MainActivity.MODE_TEXT);

		max_questions = allPhrases.size();
	}

}

class ListenConversation extends Conversation{

	public ListenConversation(int mode){
		super(mode);
		state=0;
		emptyAllowed=true;
		Phrase myPhrase;

		myPhrase = new Phrase("Take a comfortable seat, and take a relaxing breath.");
		myPhrase.setHelp("Breathe in, and then breathe out. Relax.");
		allPhrases.add(myPhrase);

		myPhrase = new Phrase("Pay attention to sounds. What can you hear?");
		myPhrase.setHelp("There's always some noise going on somwhere, just tune into it");
		allPhrases.add(myPhrase);

		myPhrase = new Phrase("Don't go searching for sounds to listen to. Just let them come to you and relax.");
		myPhrase.setHelp("Just let sounds come into your awareness");
		allPhrases.add(myPhrase);

		myPhrase = new Phrase("Now, notice the difference between the sound arising and any thoughts about that sound.");
		myPhrase.setHelp("Focus on the difference between the quality of the sound, and the labelling of the sound.");
		allPhrases.add(myPhrase);

		myPhrase = new Phrase("Simply allow sounds to come and go. You don't have to think about a sound to hear it.");
		myPhrase.setHelp("Relax any effort around listening.");
		allPhrases.add(myPhrase);

		myPhrase = new Phrase("This relaxed attention to an object arising and passing away in the mind is mindfulness");
		myPhrase.setHelp("Mindfulness should be effortless and relaxed.");
		allPhrases.add(myPhrase);

		myPhrase = new Phrase("When you are ready, hit next to move on ...");
		myPhrase.setHelp("Just tap the next button.");
		allPhrases.add(myPhrase);

		nextConversation = new FillerConversation(MainActivity.MODE_TEXT);
		
		max_questions = allPhrases.size();
	}

}

class CheckInConversation extends Conversation{

	public CheckInConversation(int mode){
		super(mode);
		state=0;
		emptyAllowed=false;
		Phrase myPhrase;

		myPhrase = new Phrase("Let's do a check in.\n\nWhat is going on right now? Where are " +
				"you? What are you doing?");
		myPhrase.setHelp("Just give a simple assessment of current situation. " +
				"There's no right answer, just keep it simple.");
		allPhrases.add(myPhrase);
		
		myPhrase = new Phrase("How are you feeling (good/bad/OK)?");
		myPhrase.setHelp("How do you feel in general? Do you feel good? Do you feel bad? Or just OK?");
		myPhrase.addResponse("good");
		myPhrase.addResponse("bad");
		myPhrase.addResponse("OK");
		allPhrases.add(myPhrase);

		myPhrase = new Phrase("What kind of thoughts are present? (Or were present 5 minutes ago)");
		myPhrase.setHelp("Here's a few ideas: planning, problem solving, regret, hope, " +
				"obsessive, wanting, spaced out, doing, trying to fix it, unwholesome, wholesome, vacant");
		allPhrases.add(myPhrase);

		
		myPhrase = new Phrase("Are the thoughts tight or relaxed?");
		myPhrase.setHelp("Are the thoughts running in contracted loops (tight)? " +
				"Or are they drifting in (relaxed)?");
		myPhrase.addResponse("tight");
		myPhrase.addResponse("relaxed");
		allPhrases.add(myPhrase);

		myPhrase = new Phrase("What mood is present?");
		myPhrase.setHelp("There are many moods: angry, happy, sad, annoyed, depressed, low, up, down, greedy, content, " +
				"unhappy, nervous, anxious, unpleasant, hateful, mean, joyful, excited, empty, calm, discontented, agitated," +
				" tense, blissful.");
		allPhrases.add(myPhrase);

		myPhrase = new Phrase("Check into your body. Is there any tension in the body? Where is it?");
		myPhrase.setHelp("Pay attention to your physical experience - some part of the body is usually tense. Check the" +
				"shoulders, neck, face, mouth, forehead, belly, legs, feet, hands.");
		allPhrases.add(myPhrase);


		myPhrase = new Phrase("Now, just take a moment to relax. Deep breathing is an excellent place to start. \n\nChecking in like this is a useful way to strengthen " +
				"everyday mindfulness.");
		myPhrase.setHelp("Relax, relax, relax. Just be aware of what is going on in a relaxed fashion.");
		allPhrases.add(myPhrase);

		max_questions = allPhrases.size();
		
	}
	
}

class FillerConversation extends Conversation{
	
	public FillerConversation(int mode){
		super(mode);
		state=0;
		Phrase myPhrase;

		Random rand = new Random();
		int r = rand.nextInt(4);
		
		if(r==0)
			myPhrase = new Phrase("Pay attention to nice things. Relax.");
		else if(r==1)
			myPhrase = new Phrase("Allow yourself a moment to breathe.");
		else if(r==2)
			myPhrase = new Phrase("Be spacious. Be open. Be happy.");
		else
			myPhrase = new Phrase("Breathe in, breathe out. Relax for a moment");

		myPhrase.setHelp("Take a moment to relax.");
		allPhrases.add(myPhrase);	
		max_questions = allPhrases.size();
	}
}

class SimpleConversation extends Conversation{

	public SimpleConversation(int mode){
		super(mode);
		state=0;
		Phrase myPhrase;

		myPhrase = new Phrase("Let's sit quietly for 5 minutes");
		allPhrases.add(myPhrase);

		myPhrase = new Phrase("Relax into the breathing");
		allPhrases.add(myPhrase);

		nextConversation = new FillerConversation(MainActivity.MODE_TEXT);

		max_questions = allPhrases.size();
	}
}

class ThreeMinuteConversation extends Conversation{
	
	public ThreeMinuteConversation(int mode){
		super(mode);
		state=0;
		Phrase myPhrase;

		if(mode==MainActivity.MODE_TEXT) {

			myPhrase = new Phrase("Let's do a three minute mindfulness exercise.<br /><br />Find somewhere quiet and then begin ...");
			myPhrase.setHelp("Do this regularly to build up your mindfulness skills");
			allPhrases.add(myPhrase);
		}

		myPhrase = new Phrase("Take a minute and tune in to the present moment. Breathe in and out slowly. " +
				"Acknowledge your surroundings. Pay attention to thoughts and feelings.");
		myPhrase.setHelp("What are you thinking? How do you feel?");
		allPhrases.add(myPhrase);

		myPhrase = new Phrase("Now, take a minute and scan your body. Breathe in and out slowly. Notice any tension and release it.");
		myPhrase.setHelp("Look for common areas of tension: shoulder, neck, face, head, legs, stomach.");
		allPhrases.add(myPhrase);

		myPhrase = new Phrase("Finally, spend a minute breathing into the whole body and head. Focus on wholeness and relaxation");
		myPhrase.setHelp("It's common to spend a lot of time in our thoughts. Doing integrated breathing like this expands things out " +
				"and our experience becomes more unified.");
		allPhrases.add(myPhrase);

		if(mode==MainActivity.MODE_TEXT) {

			myPhrase = new Phrase("Good, time is up. Notice any changes that may have happened during the last three minutes.");
			myPhrase.setHelp("It's good to notice how things affect your body and mind.");
			allPhrases.add(myPhrase);
		}

		nextConversation = new FillerConversation(MainActivity.MODE_TEXT);
		
		max_questions = allPhrases.size();
	}
}

class AnaPanaConversation extends Conversation{
	
	public AnaPanaConversation(int mode){
		super(mode);
		state=0;
		Phrase myPhrase;

		if(mode==MainActivity.MODE_TEXT) {

			myPhrase = new Phrase("Let's work through mindfulness of breathing (anapanasati). Remember, type help (h) at any time.<br /><br />Find somewhere quiet and then begin ...");
			myPhrase.setHelp("Anapanasati means mindfulness of the breath. It is the classic, ancient instructions that " +
					"cover the complete experience of mindfulness of breathing meditation.");
			allPhrases.add(myPhrase);
		}
		else if(mode==MainActivity.MODE_SPEECH) {
			myPhrase = new Phrase("Let's work through mindfulness of breathing (anapanasati). This meditation will last 15 minutes. Take a deep " +
					" breath and relax.");
			myPhrase.setHelp("Anapanasati means mindfulness of the breath. Take a moment to relax.");
			allPhrases.add(myPhrase);

		}

		myPhrase = new Phrase("While inhaling and exhaling, know if you are breathing long.");
		myPhrase.setHelp("Do not try to control the breathing, just observe what is going on - long or short.");
		allPhrases.add(myPhrase);

		myPhrase = new Phrase("Know if you are breathing short.");
		myPhrase.setHelp("Some people like to count the breaths to help stabilise the mind.");
		allPhrases.add(myPhrase);

		myPhrase = new Phrase("Experience the whole body while breathing.");
		myPhrase.setHelp("Imagine the breath going into the whole body. You can just do it, or you " +
				"can progressively imagine the breath going into different parts of the body until you " +
				"have covered it all.");
		allPhrases.add(myPhrase);

		myPhrase = new Phrase("Relax the whole body while breathing.");
		myPhrase.setHelp("Once you have the breath going into the whole body, relax into it. Imagining the " +
				"breath massaging the body can be useful.");
		allPhrases.add(myPhrase);

		myPhrase = new Phrase("While inhaling and exhaling, experience pleasure.");
		myPhrase.setHelp("Breathing and relaxing will lead to sensations of pleasure. Pay attention to pleasure and " +
				"soak in it.");
		allPhrases.add(myPhrase);

		myPhrase = new Phrase("Experience bliss.");
		myPhrase.setHelp("Focusing on pleasure will induce a state of bliss. Bliss is a sweet, delicious experience.");
		allPhrases.add(myPhrase);

		myPhrase = new Phrase("Experience mental activities.");
		myPhrase.setHelp("Now, you might be experiencing bliss and you might become aware of the mind " +
				"trying to get in on the act - analysing, commentating. See this happening.");
		allPhrases.add(myPhrase);

		myPhrase = new Phrase("Relax mental activities.");
		myPhrase.setHelp("Now you are aware of mental activities, you can relax them as you breathe and let go into a deeper, quieter place.");
		allPhrases.add(myPhrase);

		myPhrase = new Phrase("While inhaling and exhaling, experience the mind.");
		myPhrase.setHelp("The mind is more than just your thoughts. Once you stop thinking, you will " +
				"become aware of the wider expanse of the mind - you might experience the mind in the heart " +
				"or body.");
		allPhrases.add(myPhrase);

		myPhrase = new Phrase("Gladden the mind.");
		myPhrase.setHelp("Raise a faint smile on the lips and then smile with your heart. You will experience a " +
				"welling up of gladness.");
		allPhrases.add(myPhrase);

		myPhrase = new Phrase("Steady the mind in samadhi.");
		myPhrase.setHelp("Keep a glad mind and it will still itself completely. This is samadhi. It is a state of " +
				"calm tranquility that does not move to grasp experience.");
		allPhrases.add(myPhrase);

		myPhrase = new Phrase("Release the mind.");
		myPhrase.setHelp("Drop any pretense of being there. Let go, let go, let go.");
		allPhrases.add(myPhrase);

		myPhrase = new Phrase("While inhaling and exhaling, recognise impermanence.");
		myPhrase.setHelp("The present moment is a flux of millions of things changing all at once. When the mind " +
				"is released you will experience this flux first hand. Notice the arising and falling of all things in the mind.");
		allPhrases.add(myPhrase);

		myPhrase = new Phrase("Recognise fading of desire.");
		myPhrase.setHelp("There is no desire to leave this place. The mind wants for nothing.");
		allPhrases.add(myPhrase);

		myPhrase = new Phrase("Recognise cessation.");
		myPhrase.setHelp("We see the ending of all arisen phenomena and we are not drawn to it. We are still. There is no attempt by the mind to leave this experience.");
		allPhrases.add(myPhrase);

		myPhrase = new Phrase("Recognise complete freedom.");
		myPhrase.setHelp("There is a great relief at being in this place, however long it lasts. The self drops away and we feel great freedom.");
		allPhrases.add(myPhrase);

		if(mode==MainActivity.MODE_TEXT) {
			myPhrase = new Phrase("Take some time to bring yourself back into the room");
			myPhrase.setHelp("");
			allPhrases.add(myPhrase);
		}
		nextConversation = new CoolDownConversation(MainActivity.MODE_TEXT);

		max_questions = allPhrases.size();
	}

}


class SevenFactorsConversation extends Conversation{

	public SevenFactorsConversation(int mode){
		super(mode);
		state=0;
		Phrase myPhrase;

		myPhrase = new Phrase("Let's work through the seven factors of awakening. Take a deep breath and relax.");
		myPhrase.setHelp("The seven factors of awakening are a good way to channel your practice");
		allPhrases.add(myPhrase);

		myPhrase = new Phrase("Establish mindfulness");
		myPhrase.setHelp("The first factor. An anchor for the others.");
		allPhrases.add(myPhrase);

		myPhrase = new Phrase("Investigate your experience. What is a thought? What is the mind? Where is the mind?");
		myPhrase.setHelp("The second factor. Investigation.");
		allPhrases.add(myPhrase);

		myPhrase = new Phrase("Become aware of energy");
		myPhrase.setHelp("Invesitation leads to energy, a brightening of the mind.");
		allPhrases.add(myPhrase);

		myPhrase = new Phrase("Feel joy arising");
		myPhrase.setHelp("Energy and investigation lead to joy.");
		allPhrases.add(myPhrase);

		myPhrase = new Phrase("Become aware of tranquility");
		myPhrase.setHelp("As joy subsides, the mind becomes tranquil");
		allPhrases.add(myPhrase);

		myPhrase = new Phrase("Relax into samadhi");
		myPhrase.setHelp("The sixth factor. The mind becomes still and calm.");
		allPhrases.add(myPhrase);

		myPhrase = new Phrase("Know equinimity ");
		myPhrase.setHelp("The seventh factor. As samadhi deepens, equinimity (fading of desire and insight into cessation) develops.");
		allPhrases.add(myPhrase);

		if(mode==MainActivity.MODE_TEXT) {
			myPhrase = new Phrase("Take some time to bring yourself back into the room");
			myPhrase.setHelp("Use the seven factors to develop practice.");
			allPhrases.add(myPhrase);
		}

		nextConversation = new FillerConversation(MainActivity.MODE_TEXT);

		max_questions = allPhrases.size();
	}
}


class BodyScanConversation extends Conversation{

	public BodyScanConversation(int mode){
		super(mode);
		state=0;
		Phrase myPhrase;

		myPhrase = new Phrase("Let's do a body scan meditation. Begin by taking a deep breath in and allowing yourself a moment to relax.");
		myPhrase.setHelp("Mindfulness of the body is an important way of checking in with yourself. It is also very relaxing and a good " +
				"way to find peace and ease");
		allPhrases.add(myPhrase);

		myPhrase = new Phrase("Breathe into the feet and let them release.");
		myPhrase.setHelp("Imagine the breath going down through the body and into the feet. As you breathe out, let the feet relax");
		allPhrases.add(myPhrase);

		myPhrase = new Phrase("Imagine the breath going down into the legs and then let them relax.");
		myPhrase.setHelp("Sometimes it can help to tense up your muscles and then let them release.");
		allPhrases.add(myPhrase);

		myPhrase = new Phrase("Now, imagine the breath going into the hips and groin. Allow this area to open as you breathe out.");
		myPhrase.setHelp("Imagine the base of your spine relaxing and opening.");
		allPhrases.add(myPhrase);

		myPhrase = new Phrase("Feel the breath going into the belly. Relax the belly as you breathe out.");
		myPhrase.setHelp("The area behind the belly is also known as the hara. It is important to relax this area to release hidden tension.");
		allPhrases.add(myPhrase);

		myPhrase = new Phrase("Breathe into the solar plexus and let it go as you breathe out.");
		myPhrase.setHelp("Allow the body to breathe itself");
		allPhrases.add(myPhrase);

		myPhrase = new Phrase("Imagine the breath going into the heart and, as you breathe out, let the muscles in the chest area relax.");
		myPhrase.setHelp("Imagine the breath gladdening the heart.");
		allPhrases.add(myPhrase);

		myPhrase = new Phrase("Feel the breath going into the shoulders and neck. Allow the muscles to let go.");
		myPhrase.setHelp("We hold a lot of tension here. Relax the muscles and allow yourself to relax");
		allPhrases.add(myPhrase);

		myPhrase = new Phrase("Imagine the breath going down the arms and into the hands. Let the arms and hands release.");
		myPhrase.setHelp("Feel the fingers loosen and relax.");
		allPhrases.add(myPhrase);

		myPhrase = new Phrase("Now, notice any tension in your face and let it go. You may find tension in the jaw, lips, " +
				"eyes or forehead.");
		myPhrase.setHelp("We have a lot of muscles in our face. Let them all relax.");
		allPhrases.add(myPhrase);

		myPhrase = new Phrase("Next, imagine the breath going into the head: the whole head. Breathe in and as you breathe out " +
				"let the whole head relax.");
		myPhrase.setHelp("Feel the breath filling the head with space and relaxation.");
		allPhrases.add(myPhrase);

		myPhrase = new Phrase("Spend a few minutes enjoying head to toe relaxation. Feel the body, mind and breath unite as one.");
		myPhrase.setHelp("This is a pleasant way to spend your time relaxing.");
		allPhrases.add(myPhrase);


		if(mode==MainActivity.MODE_TEXT) {
			myPhrase = new Phrase("The meditation is complete. Bring yourself back into the room slowly and with care.");
			myPhrase.setHelp("Use the body scan as often as you can.");
			allPhrases.add(myPhrase);
		}

		nextConversation = new FillerConversation(MainActivity.MODE_TEXT);

		max_questions = allPhrases.size();
	}
}
