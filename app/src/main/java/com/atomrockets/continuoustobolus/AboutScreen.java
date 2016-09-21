package com.atomrockets.continuoustobolus;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;


public class AboutScreen extends Activity{
	
	TextView textView_about;
	
	protected void onCreate(Bundle savedInstanceState) 
    {
		try
    	{
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.about);

    	}
		catch (Exception ex)
        {
      	  Context context = getApplicationContext();
      	  CharSequence text = ex.toString();
      	  int duration = Toast.LENGTH_LONG;

      	  Toast toast = Toast.makeText(context, text, duration);
      	  toast.show();
      	  System.out.println(ex.getMessage());
        }
    }
}
