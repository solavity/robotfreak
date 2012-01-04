package ioio.examples.object_follower;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.AbstractIOIOActivity;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

public class IOIOObjectFollowerActivity extends AbstractIOIOActivity {
	private SeekBar seekBarL_;
	private SeekBar seekBarR_;
	private SeekBar seekBarPan_;
	private SeekBar seekBarTilt_;
	private ProgressBar progressBarUp_;
	private ProgressBar progressBarLt_;
	private ProgressBar progressBarRt_;
	private ProgressBar progressBarDn_;
	private TextView textViewSLt_;
	private TextView textViewSRt_;
	private TextView textViewSPan_;
	private TextView textViewSTilt_;
	private TextView textViewUp_;
	private TextView textViewLt_;
	private TextView textViewRt_;
	private TextView textViewDn_;
	private TextView textViewDist_;
	private TextView textViewPanScale_;
	private TextView textViewTiltScale_;
	private TextView textViewUpDn_;
	private TextView textViewLtRt_;
	private ToggleButton autoButton_;
	private ToggleButton enableButton_;
	private ToggleButton stopButton_;
//	private int ls1,ls2,ls3,ls4,ls5,ls6;
	private int ceUp, ceLt, ceRt, ceDn;
	private int onUp, onLt, onRt, onDn;
	private int ofUp, ofLt, ofRt, ofDn;
	private int valL, valR, valPan, valTilt;
	private int panScale, tiltScale, distance;
	private int leftright, updown;
	private int speedL, actSpeedL;
	private int speedR, actSpeedR;
	private int valSpeed;
	private int timerVal;
	
	/**
	 *  Called upon creation for initialization
	 
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        seekBarL_ = (SeekBar)findViewById(R.id.seekBarL);
        seekBarR_ = (SeekBar)findViewById(R.id.seekBarR);
        seekBarPan_ = (SeekBar)findViewById(R.id.seekBarPan);
        seekBarTilt_ = (SeekBar)findViewById(R.id.seekBarTilt);
        progressBarUp_ = (ProgressBar)findViewById(R.id.progressBarUp);
        progressBarLt_ = (ProgressBar)findViewById(R.id.progressBarLt);
        progressBarRt_ = (ProgressBar)findViewById(R.id.progressBarRt);
        progressBarDn_ = (ProgressBar)findViewById(R.id.progressBarDn);
        textViewSLt_ = (TextView)findViewById(R.id.textViewSLt);
        textViewSRt_ = (TextView)findViewById(R.id.textViewSRt);
        textViewSPan_ = (TextView)findViewById(R.id.textViewSPan);
        textViewSTilt_ = (TextView)findViewById(R.id.textViewSTilt);
        textViewUp_ = (TextView)findViewById(R.id.textViewUp);
        textViewLt_ = (TextView)findViewById(R.id.textViewLt);
        textViewRt_ = (TextView)findViewById(R.id.textViewRt);
        textViewDn_ = (TextView)findViewById(R.id.textViewDn);
        textViewDist_ = (TextView)findViewById(R.id.textViewDist);
        textViewPanScale_ = (TextView)findViewById(R.id.textViewPanScale);
        textViewTiltScale_ = (TextView)findViewById(R.id.textViewTiltScale);
        textViewUpDn_ = (TextView)findViewById(R.id.textViewUpDn);
        textViewLtRt_ = (TextView)findViewById(R.id.textViewLtRt);
        autoButton_ = (ToggleButton)findViewById(R.id.toggleButtonAuto);
        enableButton_ = (ToggleButton)findViewById(R.id.toggleButtonEnable);
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
		private PwmOutput servoL_;
		private PwmOutput servoR_;
		private PwmOutput servoPan_;
		private PwmOutput servoTilt_;
//		private DigitalOutput enableLS_;
//		private AnalogInput line1_;
//		private AnalogInput line2_;
//		private AnalogInput line3_;
//		private AnalogInput line4_;
//		private AnalogInput line5_;
//		private AnalogInput line6_;
		private DigitalOutput enableCE_;
		private AnalogInput ceUp_;
		private AnalogInput ceLt_;
		private AnalogInput ceRt_;
		private AnalogInput ceDn_;
		
        /**
         * Called every time a connection with IOIO has been established.
         * (opens pins)
         * 
         * @throws ConnectionLostException
         * (if IOIO connection is lost)
         */

		public void setup() throws ConnectionLostException {
			try {
			    /* set PWM to 20ms / 50Hz for servos */ 
				servoL_ = ioio_.openPwmOutput(10, 50);
				servoR_ = ioio_.openPwmOutput(11, 50);
				servoPan_ = ioio_.openPwmOutput(12, 50);
				servoTilt_ = ioio_.openPwmOutput(13, 50);
//				enableLS_ = ioio_.openDigitalOutput(14);
//				line1_ = ioio_.openAnalogInput(33);
//				line2_ = ioio_.openAnalogInput(34);
//				line3_ = ioio_.openAnalogInput(35);
//				line4_ = ioio_.openAnalogInput(36);
//				line5_ = ioio_.openAnalogInput(37);
//				line6_ = ioio_.openAnalogInput(38);
				enableCE_ = ioio_.openDigitalOutput(18);
				ceUp_ = ioio_.openAnalogInput(39);
				ceLt_ = ioio_.openAnalogInput(40);
				ceRt_ = ioio_.openAnalogInput(42);
				ceDn_ = ioio_.openAnalogInput(41);
				speedL = 1540;
				speedR = 1540;
				actSpeedL = speedL;
				actSpeedR = speedR;

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
		
				
				if (stopButton_.isChecked())
				{
					speedL = 1540;
					speedR = 1540;
				}
				else if (!autoButton_.isChecked())
				{
					speedL = 1000 + seekBarL_.getProgress();
					speedR = 1000 + seekBarR_.getProgress();
					valPan = 1000 + seekBarPan_.getProgress();
					valTilt = 1000 + seekBarTilt_.getProgress();
				}
				else
				{
					if (timerVal > 0)
						timerVal--;

					if (distance<300)
					{
					  if (valPan>1500)valPan=valPan-10;
					  if (valPan<1500)valPan=valPan+10;
					  if (valTilt>1500)valTilt=valTilt-10;
					  if (valTilt<1500)valTilt=valTilt+10;
       		    	  speedL=1540;
				      speedR=1540;
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
					    	speedR=1540-(valPan-1500);
					    	speedL=1540-(valPan-1500);
					    }
					    else if (valPan<1400) // turn left
					    {
					    	speedL=1540+(1500-valPan);
					        speedR=1540+(1500-valPan);
					    } 
					    else
					    {
					    	speedL=1540;
					    	speedR=1540;
					    }
					}
				}
              	actSpeedL = speedL;
               	actSpeedR = speedR;
				/* calculate servo pulse length between 1000..2000ms */
				valL = actSpeedL;
				/* right servo is running in opposite direction */
				valR = actSpeedR;
				/* set servo pulse length */
				servoL_.setPulseWidth(valL);
				servoR_.setPulseWidth(valR);
				servoPan_.setPulseWidth(valPan);
				servoTilt_.setPulseWidth(valTilt);
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
				textViewSLt_.setText(String.valueOf(valL));
				textViewSRt_.setText(String.valueOf(valR));
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
				progressBarUp_.setProgress(ceUp);
				progressBarLt_.setProgress(ceLt);
				progressBarRt_.setProgress(ceRt);
				progressBarDn_.setProgress(ceDn);
				if (autoButton_.isChecked())
				{
					seekBarL_.setProgress(actSpeedL-1000);
					seekBarR_.setProgress(actSpeedR-1000);
					seekBarPan_.setProgress(valPan-1000);
					seekBarTilt_.setProgress(valTilt-1000);
				}
			}
		});
	} 	

}

