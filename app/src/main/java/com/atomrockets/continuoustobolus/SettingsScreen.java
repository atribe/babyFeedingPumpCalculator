package com.atomrockets.continuoustobolus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

public class SettingsScreen extends Activity{
	//---Database---
		private DBAdapter db = new DBAdapter(this);
		
	//---UI Items---
		private EditText cal_per_day_input, milk_provides,editText_maxDuration,editText_maxStoppage;
		private Button reset_button, submit_button;
		private RadioGroup radioGroup_units;
		
	protected void onCreate(Bundle savedInstanceState) 
    {
		try
    	{
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.settings);
	        
	        db.open();
	        /*
	         * OptionName			Default OptionValue
	         * neededCalPerKgDay	120
	         * milkProvidesCalPerOz	22
	         * units				SI
	         * maxDuration			180
	         */
	        
	        //---Capturing all the EditTexts---
	        cal_per_day_input = (EditText)findViewById(R.id.cal_per_day_input);
	        	cal_per_day_input.setText(db.getOptionValue("neededCalPerKgDay").toString());
	        milk_provides =(EditText)findViewById(R.id.milk_provides);
	        	milk_provides.setText(db.getOptionValue("milkProvidesCalPerOz").toString());
        	editText_maxDuration =(EditText)findViewById(R.id.editText_maxDuration);
	        	editText_maxDuration.setText(db.getOptionValue("maxDuration").toString());
        	editText_maxStoppage=(EditText)findViewById(R.id.editText_maxStoppage);
        		editText_maxStoppage.setText(db.getOptionValue("maxStoppage").toString());
	        	
	        //---Capturing all the Buttons---
	        reset_button = (Button)findViewById(R.id.button_reset);
	        submit_button = (Button)findViewById(R.id.button_submit);
	        reset_button.setOnClickListener(mAddListener);
	        submit_button.setOnClickListener(mAddListener);
	        
	        //---Capturing all radioButtons---
	        radioGroup_units = (RadioGroup)findViewById(R.id.radioGroup1);

	        if(db.getOptionValue("units").toString().equals("SI"))
	        	radioGroup_units.check(R.id.radio_unit1); //SI units
	        else 
	        	radioGroup_units.check(R.id.radio_unit2); //Imperial
	        	
	        radioGroup_units.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
				
				public void onCheckedChanged(RadioGroup group, int checkedId) {

					switch(checkedId)
					{
					case R.id.radio_unit1:
						try
						{
							db.open();
							db.insertOption("units", "SI");
							db.close();
			        		
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
						break;
					case R.id.radio_unit2:
						try
						{
							db.open();
							db.insertOption("units", "Imperial");
							db.close();
			        		
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
						break;
					}
					
				}
			});
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
	private OnClickListener mAddListener = new OnClickListener()
	{
		public void onClick(View v)
    	{
			Bundle bundle = new Bundle();
        	switch(v.getId())
        	{
        	case R.id.button_reset:
        		db.open();
        		
        		try
        		{
        			db.insertOption("neededCalPerKgDay", 120);
        			db.insertOption("milkProvidesCalPerOz", 22);
        			db.insertOption("units", "SI");
        			db.insertOption("maxDuration", "180");
        			radioGroup_units.check(R.id.radio_unit1);
        		}
        		catch (Exception ex)
        		{
        			Context context = getApplicationContext();
					CharSequence text = ex.toString();
					int duration = Toast.LENGTH_LONG;
					
					Toast toast = Toast.makeText(context, text, duration);
					toast.show();
        		}
        		db.close();
        		
        		break;
        		
        	case R.id.button_submit:
				db.open();
        		
        		try
        		{
        			db.insertOption("neededCalPerKgDay", cal_per_day_input.getText().toString());
        			db.insertOption("milkProvidesCalPerOz", milk_provides.getText().toString());
        			db.insertOption("maxDuration", editText_maxDuration.getText().toString());
        		}
        		catch (Exception ex)
        		{
        			Context context = getApplicationContext();
					CharSequence text = ex.toString();
					int duration = Toast.LENGTH_LONG;
					
					Toast toast = Toast.makeText(context, text, duration);
					toast.show();
        		}
        		db.close();
        		break;
        	}
        	
        	Intent mIntent = new Intent();
            mIntent.putExtras(bundle);
            setResult(RESULT_OK, mIntent);
            finish();
    	}
	};
	
	@Override
	public void onBackPressed()
	{
		Bundle bundle = new Bundle();
		Intent mIntent = new Intent();
        mIntent.putExtras(bundle);
        setResult(RESULT_OK, mIntent);
        finish();
	}
	
}