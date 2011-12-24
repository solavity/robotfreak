package ioio.examples.robot_controller;

import ioio.lib.api.DigitalInput;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.AbstractIOIOActivity;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.Button;



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

	private SeekBar seekBarL_;
	private SeekBar seekBarM_;
	private SeekBar seekBarR_;
	private TextView textViewSl_;
	private TextView textViewSm_;
	private TextView textViewSr_;
	private ToggleButton autoButton_;
	private ToggleButton stopButton_;
	private Button buttonL_;
	private Button buttonR_;
	private int valL, valM, valR;
	private int speedL, actSpeedL;
	private int speedR, actSpeedR;
	private int timerVal;
	private boolean bumpL = false;
	private boolean bumpR = false;
	
	/**
	 *  Called upon creation for initialization
	 
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        seekBarL_ = (SeekBar)findViewById(R.id.seekBarL);
        seekBarM_ = (SeekBar)findViewById(R.id.seekBarM);
        seekBarR_ = (SeekBar)findViewById(R.id.seekBarR);
        textViewSl_ = (TextView)findViewById(R.id.textViewSl);
        textViewSm_ = (TextView)findViewById(R.id.textViewSm);
        textViewSr_ = (TextView)findViewById(R.id.textViewSr);
        autoButton_ = (ToggleButton)findViewById(R.id.toggleButtonAuto);
        stopButton_ = (ToggleButton)findViewById(R.id.toggleButtonStop);
        buttonL_ = (Button)findViewById(R.id.buttonL);
        buttonR_ = (Button)findViewById(R.id.buttonR);
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
		private PwmOutput servoM_;
		private PwmOutput servoR_;
		private DigitalInput bumperL;
		private DigitalInput bumperR;
		
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
				servoM_ = ioio_.openPwmOutput(11, 50);
				servoR_ = ioio_.openPwmOutput(12, 50);
				bumperL = ioio_.openDigitalInput(5, DigitalInput.Spec.Mode.PULL_UP);
				bumperR = ioio_.openDigitalInput(6, DigitalInput.Spec.Mode.PULL_UP);
				speedL = 54;
				speedR = 46;
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
				if (stopButton_.isChecked())
				{
					speedL = 54;
					speedR = 46;
				}
				else if (!autoButton_.isChecked())
				{
					speedL = seekBarL_.getProgress();
					speedR = seekBarR_.getProgress();
					bumpL = buttonL_.isPressed();
					bumpR = buttonR_.isPressed();
				}
				else
				{
					if (timerVal > 0)
						timerVal--;
					else
					{
						bumpL = bumperL.read();
						bumpR = bumperR.read();
						if (!bumpL && !bumpR)
						{
							speedL = 0;
							speedR = 30;
							timerVal = 50;
						
						}
						else if (!bumpL && bumpR)
						{
							speedL = 0;
							speedR = 46;
							timerVal = 50;
						
						}
						else if (!bumpR && bumpL)
						{	
							speedL = 54;
							speedR = 0;
							timerVal = 50;
						
						}
						else
						{	
							speedL = 80;
							speedR = 80;	
						}
					}
				
				}
                if (Math.abs(actSpeedL) - Math.abs(speedL) > 10)
                {
                	if (actSpeedL > speedL)
                		actSpeedL -=10;
                	else
                		actSpeedL += 10;
                }
                else
                	actSpeedL = speedL;
                if (Math.abs(actSpeedR) - Math.abs(speedR) > 10)
                {
                	if (actSpeedR > speedR)
                		actSpeedR -=10;
                	else
                		actSpeedR += 10;
                }
                else
                	actSpeedR = speedR;
				/* calculate servo pulse length between 1000..2000ms */
				valL = 1000 + actSpeedL * 10;
				/* right servo is running in opposite direction */
				valR = 2000 - actSpeedR * 10;
				valM = 1000 + seekBarR_.getProgress() * 10;
				/* set servo pulse length */
				servoL_.setPulseWidth(valL);
				servoR_.setPulseWidth(valR);
				servoM_.setPulseWidth(valM);
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
				seekBarM_.setEnabled(enable);
				seekBarR_.setEnabled(enable);
		
			}
		});
	}
	
	private void updateViews() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				textViewSl_.setText(String.valueOf(valL));
				textViewSm_.setText(String.valueOf(valM));
				textViewSr_.setText(String.valueOf(valR));
				if (autoButton_.isChecked())
				{
					buttonL_.setPressed(bumpL);
					buttonR_.setPressed(bumpR);
					seekBarL_.setProgress(actSpeedL);
					seekBarR_.setProgress(actSpeedR);
				}
			}
		});
	} 	

}

