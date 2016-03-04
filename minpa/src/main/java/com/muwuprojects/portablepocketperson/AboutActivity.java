package com.muwuprojects.portablepocketperson;

import android.os.Bundle;
import android.app.Activity;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AboutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setTitle("About minpa");

        Button myButton = (Button) findViewById(R.id.help_ok);
        myButton.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });

        TextView helpText = (TextView)findViewById(R.id.help_text);

        Spanned bigtext = Html.fromHtml("<b>Version 1.04.</b><br /><br /> " +
                "All rights reserved, 2016. <br /><br />" +
                "I made this app to help with " +
                "regular mindfulness practice. I hope you find it useful. Here are some useful commands:<br /><br />" +
                "<b>Context sensitive help</b><br /><br />" +
                "Type <i>help</i> (or just 'h') at any point to get context sensitive help. It will also give " +
                "additional meditation instructions to the ones provided.<br /><br />" +
                "<b>Meditation playback</b><br /><br />" +
                "This app uses the text to speech engine on your phone to read out meditation sequences. The " +
                "following meditations are currently available: <br /><br />" +
                "<i>play simple</i> - a 5 minute break<br />" +
                "<i>play body</i> - a body scan meditation<br />" +
                "<i>play three</i> - a three minute check in<br />" +
                "<i>play zen</i> - a zen sequence<br />" +
                "<i>play ana</i> - the anapanasati sequence<br /><br />" +
                "You can stop a meditation sequence at any point by typing anything in to the app (except help). <br /><br />" +
                "You can test text to speech playback by typing the <i>speak</i> command. This will cause the" +
                " app to start speaking to you. (Type <i>speak</i> again to turn it off).<br /><br />" +
                "<b>Other useful features</b><br /><br />" +
                "You can type 'y' for yes and 'n' for no at any point to save your fingers...<br /><br />" +
                "<b>vent command</b><br /><br />" +
                "You can type '<i>vent</i>' at any time to enter a one way conversation where you can feel free to vent " +
                "your frustrations. Enter an empty sentence to finish. <br /><br />" +
                "<b>Voice operation</b><br /><br />" +
                "You can use voice commands to operate the software using the built in speech recognition software on your device. " +
                "Tap the microphone icon on the soft keyboard to start speaking to the software." +
                "When you want to submit a command, simply say '<i>next</i>' to push the command to the device ('next' is a reserved " +
                "word and will always submit your text to the device). I cannot vouch for the accuracy of the speech recognition but it's " +
                "there if you want it.<br /><br />");
        helpText.setText(bigtext);

    }

}
