package ioio.examples.line_follower;

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

public class IOIOLineFollowerActivity extends AbstractIOIOActivity {
	private SeekBar seekBarL_;
	private SeekBar seekBarM_;
	private SeekBar seekBarR_;
	private ProgressBar progressBar1_;
	private ProgressBar progressBar2_;
	private ProgressBar progressBar3_;
	private ProgressBar progressBar4_;
	private ProgressBar progressBar5_;
	private ProgressBar progressBar6_;
	private TextView textViewSl_;
	private TextView textViewSm_;
	private TextView textViewSr_;
	private TextView textViewL1_;
	private TextView textViewL2_;
	private TextView textViewL3_;
	private TextView textViewL4_;
	private TextView textViewL5_;
	private TextView textViewL6_;
	private ToggleButton autoButton_;
	private ToggleButton enableButton_;
	private ToggleButton stopButton_;
	private int ls1,ls2,ls3,ls4,ls5,ls6;
	private int valL, valM, valR;
	private int speedL, actSpeedL;
	private int speedR, actSpeedR;
	private int timerVal;
	
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
        progressBar1_ = (ProgressBar)findViewById(R.id.progressBar1);
        progressBar2_ = (ProgressBar)findViewById(R.id.progressBar2);
        progressBar3_ = (ProgressBar)findViewById(R.id.progressBar3);
        progressBar4_ = (ProgressBar)findViewById(R.id.progressBar4);
        progressBar5_ = (ProgressBar)findViewById(R.id.progressBar5);
        progressBar6_ = (ProgressBar)findViewById(R.id.progressBar6);
        textViewSl_ = (TextView)findViewById(R.id.textViewSl);
        textViewSm_ = (TextView)findViewById(R.id.textViewSm);
        textViewSr_ = (TextView)findViewById(R.id.textViewSr);
        textViewL1_ = (TextView)findViewById(R.id.textViewL1);
        textViewL2_ = (TextView)findViewById(R.id.textViewL2);
        textViewL3_ = (TextView)findViewById(R.id.textViewL3);
        textViewL4_ = (TextView)findViewById(R.id.textViewL4);
        textViewL5_ = (TextView)findViewById(R.id.textViewL5);
        textViewL6_ = (TextView)findViewById(R.id.textViewL6);
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
		private PwmOutput servoM_;
		private PwmOutput servoR_;
		private DigitalOutput enableLS_;
		private AnalogInput line1_;
		private AnalogInput line2_;
		private AnalogInput line3_;
		private AnalogInput line4_;
		private AnalogInput line5_;
		private AnalogInput line6_;
		
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
				enableLS_ = ioio_.openDigitalOutput(13);
				line1_ = ioio_.openAnalogInput(33);
				line2_ = ioio_.openAnalogInput(34);
				line3_ = ioio_.openAnalogInput(35);
				line4_ = ioio_.openAnalogInput(36);
				line5_ = ioio_.openAnalogInput(37);
				line6_ = ioio_.openAnalogInput(38);
				speedL = 54;
				speedR = 54;
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
				ls1 = (int) (line1_.getVoltage() * 1000); 
				ls2 = (int) (line2_.getVoltage() * 1000); 
				ls3 = (int) (line3_.getVoltage() * 1000); 
				ls4 = (int) (line4_.getVoltage() * 1000); 
				ls5 = (int) (line5_.getVoltage() * 1000); 
				ls6 = (int) (line6_.getVoltage() * 1000); 
				enableLS_.write(enableButton_.isChecked());
				
				if (stopButton_.isChecked())
				{
					speedL = 54;
					speedR = 46;
				}
				else if (!autoButton_.isChecked())
				{
					speedL = seekBarL_.getProgress();
					speedR = seekBarR_.getProgress();
				}
				else
				{
					if (timerVal > 0)
						timerVal--;
					if (ls3 > 1800 || ls4 > 1800)
					{
						speedL = 84;
						speedR = 76;
					}
					else if (ls2 > 1800)
					{
						speedL = 84;
						speedR = 66;
					}
					else if (ls1 > 1800)
					{
						speedL = 84;
						speedR = 46;
					}
					else if (ls5 > 1800)
					{
						speedL = 64;
						speedR = 76;
					}
					else if (ls6 > 1800)
					{
						speedL = 54;
						speedR = 76;
					}
				
				}
                if (Math.abs(actSpeedL) - Math.abs(speedL) > 4)
                {
                	if (actSpeedL > speedL)
                		actSpeedL -= 4;
                	else
                		actSpeedL += 4;
                }
                else
                	actSpeedL = speedL;
                if (Math.abs(actSpeedR) - Math.abs(speedR) > 4)
                {
                	if (actSpeedR > speedR)
                		actSpeedR -= 4;
                	else
                		actSpeedR += 4;
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
				textViewL1_.setText(String.valueOf(ls1));
				textViewL2_.setText(String.valueOf(ls2));
				textViewL3_.setText(String.valueOf(ls3));
				textViewL4_.setText(String.valueOf(ls4));
				textViewL5_.setText(String.valueOf(ls5));
				textViewL6_.setText(String.valueOf(ls6));
				progressBar1_.setProgress(ls1);
				progressBar2_.setProgress(ls2);
				progressBar3_.setProgress(ls3);
				progressBar4_.setProgress(ls4);
				progressBar5_.setProgress(ls5);
				progressBar6_.setProgress(ls6);
				if (autoButton_.isChecked())
				{
					seekBarL_.setProgress(actSpeedL);
					seekBarR_.setProgress(actSpeedR);
				}
			}
		});
	} 	

}

