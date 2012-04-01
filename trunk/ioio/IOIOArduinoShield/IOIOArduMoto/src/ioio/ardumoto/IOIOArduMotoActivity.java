package ioio.ardumoto;

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

public class IOIOArduMotoActivity extends IOIOActivity {
	public float leftSpeed = 0;
	public float rightSpeed = 0; 
	
	private TextView textViewL_;
	private TextView textViewR_;
	private SeekBar seekBarL_;
	private SeekBar seekBarR_;
	private ToggleButton toggleButton_;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        seekBarL_ = (SeekBar)findViewById(R.id.SeekBarL);
        seekBarR_ = (SeekBar)findViewById(R.id.SeekBarR);
        textViewL_ = (TextView)findViewById(R.id.TextViewL);
        textViewR_ = (TextView)findViewById(R.id.TextViewR);
        toggleButton_ = (ToggleButton)findViewById(R.id.ToggleButton);

        enableUi(false);
    }
	
	class Looper extends BaseIOIOLooper {
		private DigitalOutput led_;
		private IOIOMotor leftMotor;
		private IOIOMotor rightMotor;

		//Pin numbers
		final int pwmA = 13;
		final int dirA = 47;
		final int pwmB = 14; 		
		final int dirB = 48;

		@Override
		public void setup() throws ConnectionLostException {
			try {
				led_ = ioio_.openDigitalOutput(IOIO.LED_PIN, true);
				leftMotor = new IOIOMotor(ioio_, pwmB, dirB);
				rightMotor = new IOIOMotor(ioio_, pwmA, dirA); 
				enableUi(true);
			} catch (ConnectionLostException e) {
				enableUi(false);
				throw e;
			}
		}
		
		@Override
		public void loop() throws ConnectionLostException {
			try {
				led_.write(!toggleButton_.isChecked());
				leftSpeed =  (float) ((float) (((float) (seekBarL_.getProgress())/1000.0) - 0.5) * 2.0);
				rightSpeed = (float) ((float) (((float) (seekBarR_.getProgress())/1000.0) - 0.5) * 2.0);
				
				leftMotor.setSpeed(leftSpeed);
				rightMotor.setSpeed(rightSpeed); 
				setText(Float.toString(leftSpeed), Float.toString(rightSpeed));
				Thread.sleep(100);
			} catch (InterruptedException e) {
				ioio_.disconnect();
			} catch (ConnectionLostException e) {
				enableUi(false);
				throw e;
			}
		}
	}

	@Override
	protected IOIOLooper createIOIOLooper() {
		return new Looper();
	}

	private void enableUi(final boolean enable) { 
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				seekBarL_.setEnabled(enable);
				seekBarR_.setEnabled(enable);
				toggleButton_.setEnabled(enable);
			}
		});
	}
	private void setText(final String strL, final String strR) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				textViewL_.setText(strL);
				textViewR_.setText(strR);
			}
		});
	}
}
