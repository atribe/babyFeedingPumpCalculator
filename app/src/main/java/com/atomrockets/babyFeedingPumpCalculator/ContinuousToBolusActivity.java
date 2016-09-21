package com.atomrockets.babyFeedingPumpCalculator;

import java.text.DecimalFormat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

public class ContinuousToBolusActivity extends Activity {
	//---Activity Stuff---
	private static final int ACTIVITY_SETTINGS=0;
	public static final String TAG = "MainActivity";

	//---Database---
	DBAdapter db = new DBAdapter(this);

	//---UI Items---
	EditText editText_weight;
	SeekBar seekBar_duration;
	SeekBar seekBar_stoppageDuration;
	Button button_calculate;
	TextView textView_durationText;
	TextView textView_stoppageDurationText;
	TextView weight_input_label;
	TextView textview_result_feedRate_label;
	TextView textview_result_feedRate;
	TextView textview_result_totalVolume_label;
	TextView textview_result_totalVolume;

	//---Options from the database---
	int neededCaloriesPerKgPerDay;
	int milkProvidesCaloriesPerOz;
	int feedDuration;
	int maxDuration; //in minutes
	int stoppageDuration;
	int maxStoppage; //in minutes

	//---Constants---
	double milPerOz = 29.5735296;

	//---Class Variables---
	String weight;

	//---Menu Stuff---


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	try {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.main);

	        Log.v(TAG, "ContToBolus Activity Started");

	        Log.v(TAG, "Entering Database Open");
	        db.open();
	        Log.v(TAG, "Exiting Database Open");
	        /*
	         * OptionName	Default OptionValue
	         * duration		150
	         * weight_kg	no default
	         */

			initGui();

	        //Setting the text of labels and text inputs based on the units
	        setScreenText();

	        //Calculating a rate when program is run
	        try {
	        	calcRate();
	    	} catch (Exception ex) {
	        	  Context context = getApplicationContext();
	        	  CharSequence text = ex.toString();
	        	  int duration = Toast.LENGTH_LONG;

	        	  Toast toast = Toast.makeText(context, text, duration);
	        	  toast.show();
	        	  System.out.println(ex.getMessage());
	          }
    	} catch (Exception ex) {
      	  Context context = getApplicationContext();
      	  CharSequence text = ex.toString();
      	  int duration = Toast.LENGTH_LONG;

      	  Toast toast = Toast.makeText(context, text, duration);
      	  toast.show();
      	  System.out.println(ex.getMessage());
        }
    }
    private OnClickListener mAddListener = new OnClickListener() {
		public void onClick(View v) {
			//On the Calculate button click hide the keyboard
			InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editText_weight.getWindowToken(), 0);

			//calculate the volume and rate
			calcRate();
		}
	};

	private void initGui() throws Exception {
		//---Getting options from the database---
		neededCaloriesPerKgPerDay = Integer.parseInt(db.getOptionValue("neededCalPerKgDay"));
		milkProvidesCaloriesPerOz = Integer.parseInt(db.getOptionValue("milkProvidesCalPerOz"));
		feedDuration = Integer.parseInt(db.getOptionValue("duration"));
		maxDuration = Integer.parseInt(db.getOptionValue("maxDuration"));
		stoppageDuration = Integer.parseInt(db.getOptionValue("stoppageDuration"));
		maxStoppage = Integer.parseInt(db.getOptionValue("maxStoppage"));

		//---Capturing all the textViews---
		weight_input_label = (TextView)findViewById(R.id.weight_input_label);
		textview_result_feedRate_label = (TextView)findViewById(R.id.textview_result_feedRate_label);
		textview_result_feedRate = (TextView)findViewById(R.id.textview_result_feedRate);
		textview_result_totalVolume_label = (TextView)findViewById(R.id.textview_result_totalVolume_label);
		textview_result_totalVolume = (TextView)findViewById(R.id.textview_result_totalVolume);
		textView_durationText = (TextView)findViewById(R.id.textView_durationText);
		textView_stoppageDurationText = (TextView)findViewById(R.id.textView_stoppageDurationText);

		//---Capturing all the text edits---
		editText_weight = (EditText)findViewById(R.id.editText_weight);
		editText_weight.setOnEditorActionListener(new TextView.OnEditorActionListener()
		{
			public boolean onEditorAction(TextView v, int actionId, KeyEvent even)
			{
				if(actionId == EditorInfo.IME_ACTION_DONE) {
					//On a "Done" action (like hitting enter) close the virtual keyboard
					InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(editText_weight.getWindowToken(), 0);

					calcRate();
					return true;
				}
				return false;
			}
		});


		//---Capturing all Seekbars---
		seekBar_duration = (SeekBar)findViewById(R.id.seekBar_duration);
		seekBar_duration.setMax(maxDuration);//3 hours = 180 minutes
		seekBar_duration.setProgress(Integer.parseInt(db.getOptionValue("duration")));//default is 150
		seekBar_duration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				Log.v(TAG, "Duration Seekbar Changed");
				setDurationText(progress, 1);

				//These two lines make the keyboard disappear if you move the seekbar
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(editText_weight.getWindowToken(), 0);

				//Calculate the rate and total volume anytime the seekbar is moved
				calcRate();
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				Log.v(TAG, "Duration Seekbar Start Tracking");
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
				Log.v(TAG, "Duration Seekbar Stop Tracking");
				db.insertOption("duration", seekBar.getProgress());
				calcRate();
			}
		});

		seekBar_stoppageDuration = (SeekBar)findViewById(R.id.seekBar_stoppageDuration);
		seekBar_stoppageDuration.setMax(maxStoppage);
		seekBar_stoppageDuration.setProgress(Integer.parseInt(db.getOptionValue("stoppageDuration")));//default is 150
		seekBar_stoppageDuration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
		{

			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				setDurationText(progress, 2);
				Log.v(TAG, "Duration Seekbar Changed");

				//These two lines make the keyboard disappear if you move the seekbar
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(editText_weight.getWindowToken(), 0);

				//Calculate the rate and total volume anytime the seekbar is moved
				//calcRate();
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				Log.v(TAG, "Duration Seekbar Changed");
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
				Log.v(TAG, "Duration Seekbar Changed");
				db.insertOption("stoppageDuration", seekBar.getProgress());
				calcRate();
			}
		});

		//---Capturing all the buttons---
		button_calculate = (Button)findViewById(R.id.button_calculate);
		button_calculate.setOnClickListener(mAddListener);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu, menu);
	    return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.settings:
	    	settingsScreen();
	        return true;
	    case R.id.about:
	        aboutScreen();
	        return true;
	    case R.id.quit:
	    	finish();
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	private void settingsScreen() {
		try {
			Intent i = new Intent(this, SettingsScreen.class);
			startActivityForResult(i,ACTIVITY_SETTINGS);
    	} catch (Exception ex) {
	  	  Context context = getApplicationContext();
	  	  CharSequence text = ex.toString();
	  	  int duration = Toast.LENGTH_LONG;

	  	  Toast toast = Toast.makeText(context, text, duration);
	  	  toast.show();
	  	  System.out.println(ex.getMessage());
	    }
	}
	private void aboutScreen() {
		try {
			Intent i = new Intent(this, AboutScreen.class);
			startActivity(i);
    	} catch (Exception ex) {
	  	  Context context = getApplicationContext();
	  	  CharSequence text = ex.toString();
	  	  int duration = Toast.LENGTH_LONG;

	  	  Toast toast = Toast.makeText(context, text, duration);
	  	  toast.show();
	  	  System.out.println(ex.getMessage());
	    }
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		try {
			super.onActivityResult(requestCode, resultCode, intent);
			switch(requestCode) {
			case ACTIVITY_SETTINGS:

				//On completion of either about or settings activity
				//Change the text based on the units
				setScreenText();

				//Updating the feed duration seekbar
					//Get the maxDuration set in the options
					maxDuration = Integer.parseInt(db.getOptionValue("maxDuration"));
					//Set the seekbars new max to the maxDuration
					seekBar_duration.setMax(maxDuration);
					//Get the current duration from the options db
					int duration = Integer.parseInt(db.getOptionValue("duration"));
					//if the duration is greater than the new max then set the current duration equal to the max
					if (duration>maxDuration)
						duration=maxDuration;
					//set the duration text appropriately
					setDurationText(duration, 1);

				//updating the stoppage duration seekbar
				//Get the maxDuration set in the options
				maxStoppage = Integer.parseInt(db.getOptionValue("maxStoppage"));
				//Set the seekbars new max to the maxDuration
				seekBar_stoppageDuration.setMax(maxStoppage);
				//Get the current duration from the options db
				stoppageDuration = Integer.parseInt(db.getOptionValue("stoppageDuration"));
				//if the duration is greater than the new max then set the current duration equal to the max
				if (stoppageDuration>maxStoppage)
					stoppageDuration=maxStoppage;
				//set the duration text appropriately
				setDurationText(stoppageDuration, 2);
				//Display the result of the calculations
				calcRate();
				break;
			}
    	} catch (Exception ex) {
	  	  Context context = getApplicationContext();
	  	  CharSequence text = ex.toString();
	  	  int duration = Toast.LENGTH_LONG;

	  	  Toast toast = Toast.makeText(context, text, duration);
	  	  toast.show();
	  	  System.out.println(ex.getMessage());
	    }
	}
	private void calcRate() {
		weight = editText_weight.getText().toString();
		if(weight.length()>0) {
			if(db.getOptionValue("units").toString().equals("SI")) {
				db.insertOption("weight_kg", weight);
			} else {
				Double Dweight = Double.parseDouble(weight);
				Dweight = Dweight*0.45359237;
				weight = Double.toString(Dweight);
				db.insertOption("weight_kg", weight);
			} try {
				//getting values from the db and the seekbars
				neededCaloriesPerKgPerDay = Integer.parseInt(db.getOptionValue("neededCalPerKgDay"));
	    		milkProvidesCaloriesPerOz = Integer.parseInt(db.getOptionValue("milkProvidesCalPerOz"));
	    		maxDuration = Integer.parseInt(db.getOptionValue("maxDuration"));
	    		maxStoppage = Integer.parseInt(db.getOptionValue("maxStoppage"));
	    		stoppageDuration = seekBar_stoppageDuration.getProgress();
	    		feedDuration = seekBar_duration.getProgress();
	    		double childWeight = Double.parseDouble(weight);

	    		//calculations
				double milkProvidesCaloriesPerMil = milkProvidesCaloriesPerOz/milPerOz;
				double babyNeedsCaloriesPer24Hours = neededCaloriesPerKgPerDay*childWeight;

				//needs per hour is adjusted based on how long they are not feeding at night
				double babyNeedsMilPerHour = (babyNeedsCaloriesPer24Hours*(1/milkProvidesCaloriesPerMil))*(1.0/(1440-stoppageDuration)*60.0);
				int totalVolume = (int)(babyNeedsMilPerHour/60*maxDuration);

				int feedRate = (int) (babyNeedsMilPerHour*maxDuration/feedDuration);
				displayResults(feedRate, totalVolume);
			} catch (Exception ex) {
				Context context = getApplicationContext();
				CharSequence text = ex.toString();
				int duration = Toast.LENGTH_LONG;

				Toast toast = Toast.makeText(context, text, duration);
				toast.show();
			}
		} else {
			try {
	    		textview_result_totalVolume_label.setText(getString(R.string.No_Weight_SI));
	    		textview_result_totalVolume.setText("");
	    		textview_result_feedRate_label.setText("");
				textview_result_feedRate.setText("");
			} catch (Exception ex) {
				Context context = getApplicationContext();
				CharSequence text = ex.toString();
				int duration = Toast.LENGTH_LONG;

				Toast toast = Toast.makeText(context, text, duration);
				toast.show();
			}
		}
	}
	private void displayResults(int feedRate, int totalVolume) {
		try {
			textview_result_totalVolume_label.setText(getString(R.string.TotalVolumeLabel));
			textview_result_feedRate_label.setText(getString(R.string.FeedRateLabel));

			if(db.getOptionValue("units").toString().equals("SI")) {
				textview_result_totalVolume.setText(Integer.toString(totalVolume) + " " + getString(R.string.totalVolumeunits_SI));
				textview_result_feedRate.setText(Integer.toString(feedRate) + " " + getString(R.string.feedRateunits_SI));
			} else {
				textview_result_totalVolume.setText(Double.toString(roundTwoDecimals((double) (totalVolume/29.5735296))) + " " + getString(R.string.totalVolumeunits_US));
				textview_result_feedRate.setText(Double.toString(roundTwoDecimals((double) (feedRate/29.5735296))) + " " + getString(R.string.feedRateunits_US));
			}
		} catch (Exception ex) {
			Context context = getApplicationContext();
			CharSequence text = ex.toString();
			int duration = Toast.LENGTH_LONG;

			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		}
	}
	private void setScreenText() {
		weight = db.getOptionValue("weight_kg");

		//--Setting the text of the labels over the seekbars
		setDurationText(feedDuration, 1);
		setDurationText(stoppageDuration, 2);

		String units=db.getOptionValue("units").toString();
		if(units.equals("SI")) {
			weight_input_label.setText(getString(R.string.text_inputweight_SI));
			if(weight.length()>0) {
				Double Dweight = Double.parseDouble(weight);
				Dweight = roundTwoDecimals(Dweight);
				weight = Double.toString(Dweight);
				editText_weight.setText(weight);
			}
		} else {
			weight_input_label.setText(getString(R.string.text_inputweight_US));
			if(weight.length()>0) {
				Double Dweight = Double.parseDouble(weight);
				Dweight = Dweight*2.20462262;
				Dweight = roundTwoDecimals(Dweight);
				weight = Double.toString(Dweight);
				editText_weight.setText(weight);
			}
		}
	}
	//This function sets the text of the duration text view
	private void setDurationText(int seekbarProgress, int textViewId) {
		try {
			/*textViewId	TextView
			*	1			textView_durationText
			*	2			textView_stoppageDurationText
			*/
			switch (textViewId) {
			case 1:
				//Gets the maxDuration from the option database to ensure it is current
				maxDuration=Integer.parseInt(db.getOptionValue("maxDuration"));

				if(seekbarProgress==maxDuration) {
                    textView_durationText.setText(getString(R.string.continuous));//If the duration is set to 180 out of 180 change output text to "Continuously Feeding"
                } else {
					//Set the progress to however many hours and minutes
		        	int durationInHours = seekbarProgress/60;
		        	textView_durationText.setText(durationInHours + " hours and " + (seekbarProgress-durationInHours*60) + " minutes/" + maxDuration/60 + " hours and " + (maxDuration - (maxDuration/60)*60) + " minutes");
				}
				break;
			case 2:
				maxStoppage=Integer.parseInt(db.getOptionValue("maxStoppage"));

				if(seekbarProgress==maxStoppage) {
                    textView_stoppageDurationText.setText(getString(R.string.FullNightSleep));//If the duration is set to 180 out of 180 change output text to "Continuously Feeding"
                } else if(seekbarProgress==0) {
                    textView_stoppageDurationText.setText(getString(R.string.continuous));//If the duration is set to 0 out of max change output text to "Continuously Feeding"
                } else {
					//Set the progress to however many hours and minutes
		        	int durationInHours = seekbarProgress/60;
		        	textView_stoppageDurationText.setText(durationInHours + " hours and " + (seekbarProgress-durationInHours*60) + " minutes/" + maxStoppage/60 + " hours and " + (maxStoppage - (maxStoppage/60)*60) + " minutes");
				}
				break;
			}
		}
		catch (Exception ex) {
			Context context = getApplicationContext();
			CharSequence text = ex.toString();
			int duration = Toast.LENGTH_LONG;

			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		}
	}
	double roundTwoDecimals(double d) {
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        return Double.valueOf(twoDForm.format(d));
	}
}