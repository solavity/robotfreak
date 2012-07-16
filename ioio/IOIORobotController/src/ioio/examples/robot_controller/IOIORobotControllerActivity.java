package ioio.examples.robot_controller;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.PulseInput;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.PulseInput.PulseMode;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.AbstractIOIOActivity;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;



/**
* import ioio.lib.bluetooth.*;
*/
/**
 * http://swantron.com | Joseph Swanson | 2011
 * IOIO Servo main activity
 * Driving servo via Android
 * utilizing PWM output from IOIO
 */

public class IOIORobotControllerActivity extends AbstractIOIOActivity {

	private ToggleButton autoButton_;
	private ToggleButton stopButton_;

	private SeekBar seekBarL_;
	private SeekBar seekBarR_;
	private TextView textViewSl_;
	private TextView textViewSr_;
	private float speedL, speedR;
	/* compound eye */
	private ProgressBar progressBarUp_;
	private ProgressBar progressBarLt_;
	private ProgressBar progressBarRt_;
	private ProgressBar progressBarDn_;
	private TextView textViewUp_;
	private TextView textViewLt_;
	private TextView textViewRt_;
	private TextView textViewDn_;
	private TextView textViewDist_;
	private TextView textViewUpDn_;
	private TextView textViewLtRt_;
	private int ceUp, ceLt, ceRt, ceDn;
	private int onUp, onLt, onRt, onDn;
	private int ofUp, ofLt, ofRt, ofDn;
	private int leftright, updown;
	/* pan/tilt head */
	private SeekBar seekBarPan_;
	private SeekBar seekBarTilt_;
	private TextView textViewSPan_;
	private TextView textViewSTilt_;
	private TextView textViewPanScale_;
	private TextView textViewTiltScale_;
	private int valPan, valTilt;
	private int panScale, tiltScale, distance;
	/* ultrasonic sensor */
	private TextView textViewUSDist_;
	private int echoSeconds;
	private int echoDistanceCm;
	
	/**
	 *  Called upon creation for initialization
	 
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		/* motor driver */
        seekBarL_ = (SeekBar)findViewById(R.id.seekBarL);
        seekBarR_ = (SeekBar)findViewById(R.id.seekBarR);
        textViewSl_ = (TextView)findViewById(R.id.textViewSl);
        textViewSr_ = (TextView)findViewById(R.id.textViewSr);
		/* pan/tilt head */
        seekBarPan_ = (SeekBar)findViewById(R.id.seekBarPan);
        seekBarTilt_ = (SeekBar)findViewById(R.id.seekBarTilt);
        textViewSPan_ = (TextView)findViewById(R.id.textViewSPan);
        textViewSTilt_ = (TextView)findViewById(R.id.textViewSTilt);
        textViewPanScale_ = (TextView)findViewById(R.id.textViewPanScale);
        textViewTiltScale_ = (TextView)findViewById(R.id.textViewTiltScale);
		/* compound eye */
        progressBarUp_ = (ProgressBar)findViewById(R.id.progressBarUp);
        progressBarLt_ = (ProgressBar)findViewById(R.id.progressBarLt);
        progressBarRt_ = (ProgressBar)findViewById(R.id.progressBarRt);
        progressBarDn_ = (ProgressBar)findViewById(R.id.progressBarDn);
        textViewUp_ = (TextView)findViewById(R.id.textViewUp);
        textViewLt_ = (TextView)findViewById(R.id.textViewLt);
        textViewRt_ = (TextView)findViewById(R.id.textViewRt);
        textViewDn_ = (TextView)findViewById(R.id.textViewDn);
        textViewDist_ = (TextView)findViewById(R.id.textViewDist);
        textViewUpDn_ = (TextView)findViewById(R.id.textViewUpDn);
        textViewLtRt_ = (TextView)findViewById(R.id.textViewLtRt);
		/* ultrasonic sensor */
        textViewUSDist_ = (TextView)findViewById(R.id.textViewUSDist);

        autoButton_ = (ToggleButton)findViewById(R.id.toggleButtonAuto);
        stopButton_ = (ToggleButton)findViewById(R.id.toggleButtonStop);
        enableUi(false);
    }
    /**
     * Primary thread...runs until interrupted 
     */
	class IOIOThread extends AbstractIOIOActivity.IOIOThread {
		/**
		 * define pin
		 */
		/* motor driver */
		private PwmOutput pwmL_;
		private PwmOutput pwmR_;
		private DigitalOutput dirL_;
		private DigitalOutput dirR_;
		/* pan/tilt head */
		private PwmOutput servoPan_;
		private PwmOutput servoTilt_;
		/* compound eye */
		private DigitalOutput enableCE_;
		private AnalogInput ceUp_;
		private AnalogInput ceLt_;
		private AnalogInput ceRt_;
		private AnalogInput ceDn_;
		/* ultrasonic sensor */
		private DigitalOutput triggerPin_;
		private PulseInput echoPin_;
		
        /**
         * Called every time a connection with IOIO has been established.
         * (opens pins)
         * 
         * @throws ConnectionLostException
         * (if IOIO connection is lost)
         */

		public void setup() throws ConnectionLostException {
			try {
				/* motor driver */
			    /* set PWM to 1000Hz for motors */ 
				pwmL_ = ioio_.openPwmOutput(3, 1000);
				pwmR_ = ioio_.openPwmOutput(13, 1000);
				dirL_ = ioio_.openDigitalOutput(4);
				dirR_ = ioio_.openDigitalOutput(14);
				speedL = 0;
				speedR = 0;
				/* pan/tilt head */
				servoPan_ = ioio_.openPwmOutput(11, 50);
				servoTilt_ = ioio_.openPwmOutput(10, 50);
				/* compound eye */
				enableCE_ = ioio_.openDigitalOutput(30);
				ceUp_ = ioio_.openAnalogInput(38);
				ceLt_ = ioio_.openAnalogInput(37);
				ceRt_ = ioio_.openAnalogInput(36);
				ceDn_ = ioio_.openAnalogInput(35);
				/* ultrasonic sensor */
				echoPin_ = ioio_.openPulseInput(6, PulseMode.POSITIVE);
				triggerPin_ = ioio_.openDigitalOutput(7);

				enableUi(true);
			} catch (ConnectionLostException e) {
				enableUi(false);
				throw e;
			}
		}
        /**
         * Loop section
         */

		public void loop() throws ConnectionLostException {
			try {
				// read compounde eye IR sensor 
				enableCE_.write(false);
				sleep(5);
				onUp = (int) (ceUp_.getVoltage() * 1000); 
				onLt = (int) (ceLt_.getVoltage() * 1000); 
				onRt = (int) (ceRt_.getVoltage() * 1000); 
				onDn = (int) (ceDn_.getVoltage() * 1000); 
				enableCE_.write(true);
				sleep(5);
				ofUp = (int) (ceUp_.getVoltage() * 1000); 
				ofLt = (int) (ceLt_.getVoltage() * 1000); 
				ofRt = (int) (ceRt_.getVoltage() * 1000); 
				ofDn = (int) (ceDn_.getVoltage() * 1000);
				ceUp=onUp-ofUp;
				ceLt=onLt-ofLt;
				ceRt=onRt-ofRt;
				ceDn=onDn-ofDn;
				distance = (ceUp+ceLt+ceRt+ceDn)/4;
		        // read HC-SR04 ultrasonic sensor  
				triggerPin_.write(false);
				sleep(5);
				triggerPin_.write(true);
				sleep(1);
				triggerPin_.write(false);
				echoSeconds = (int) (echoPin_.getDuration() * 1000 * 1000);
				echoDistanceCm = echoSeconds / 29 / 2;
				if (stopButton_.isChecked())
				{
					speedL = 0;
					speedR = 0;
				}
				else if (autoButton_.isChecked())   // automatic mode
				{
					FollowObject();
				}
				else                                // manual mode
				{
					speedL = (float) ((float)(seekBarL_.getProgress())/100.0);
					speedR = (float) ((float)(seekBarR_.getProgress())/100.0);
					valPan = 1000 + seekBarPan_.getProgress();
					valTilt = 1000 + seekBarTilt_.getProgress();
				}
				/* set servo pulse length */
				pwmL_.setDutyCycle(speedL);
				pwmR_.setDutyCycle(speedR);
				servoPan_.setPulseWidth(valPan);
				servoTilt_.setPulseWidth(valTilt);
				dirL_.write(false);
				dirR_.write(false);
				/* update UI */
				updateViews();

				sleep(20);
			} catch (InterruptedException e) {
				ioio_.disconnect();
				
			} catch (ConnectionLostException e) {
				enableUi(false);
				throw e;
				
			}
		}
		
		public void FollowObject() {
		
		if (distance<300)
		{
		  if (valPan>1500)valPan=valPan-10;
		  if (valPan<1500)valPan=valPan+10;
		  if (valTilt>1500)valTilt=valTilt-10;
		  if (valTilt<1500)valTilt=valTilt+10;
		}
		else
		{
		    panScale=(ceLt+ceRt)/3;
		    tiltScale=(ceUp+ceDn)/3; 
		    
		    leftright=(ceLt-ceRt)*5/panScale;
		    valPan=valPan+leftright*10;
		    updown=(ceDn-ceUp)*5/tiltScale;
		    valTilt=valTilt+updown*10;
		    
		    if(valPan<1000)valPan=1000;
		    if(valPan>2000)valPan=2000;
		    if(valTilt<1000)valTilt=1000;
		    if(valTilt>1900)valTilt=1700;
		    
		    if (valPan>1600) // turn right
		    {
		    }
		    else if (valPan<1400) // turn left
		    {
		    } 
		    else   // stop
		    {
		    }
		}
		}

	}
    /**
     * A method to create our IOIO thread.
     */

	@Override
	protected AbstractIOIOActivity.IOIOThread createIOIOThread() {
		return new IOIOThread();
	}

	private void enableUi(final boolean enable) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				seekBarL_.setEnabled(enable);
				seekBarR_.setEnabled(enable);
				seekBarPan_.setEnabled(enable);
				seekBarTilt_.setEnabled(enable);
		
			}
		});
	}
	
	private void updateViews() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				textViewSl_.setText(String.valueOf(speedL));
				textViewSr_.setText(String.valueOf(speedR));
				textViewSPan_.setText(String.valueOf(valPan));
				textViewSTilt_.setText(String.valueOf(valTilt));
				textViewUp_.setText(String.valueOf(ceUp));
				textViewLt_.setText(String.valueOf(ceLt));
				textViewRt_.setText(String.valueOf(ceRt));
				textViewDn_.setText(String.valueOf(ceDn));
				textViewDist_.setText(String.valueOf(distance));
				textViewPanScale_.setText(String.valueOf(panScale));
				textViewTiltScale_.setText(String.valueOf(tiltScale));
				textViewUpDn_.setText(String.valueOf(updown));
				textViewLtRt_.setText(String.valueOf(leftright));
				textViewUSDist_.setText(String.valueOf(echoDistanceCm));
				progressBarUp_.setProgress(ceUp);
				progressBarLt_.setProgress(ceLt);
				progressBarRt_.setProgress(ceRt);
				progressBarDn_.setProgress(ceDn);
				if (autoButton_.isChecked())
				{
					seekBarL_.setProgress((int)(speedL*100));
					seekBarR_.setProgress((int)(speedR*100));
					seekBarPan_.setProgress(valPan-1000);
					seekBarTilt_.setProgress(valTilt-1000);
				}
			}
		});
	} 	

}

