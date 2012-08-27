package ioio.robotic.rover5;

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

public class IOIORover5Activity extends IOIOActivity {
	public float leftSpeed = 0;
	public float rightSpeed = 0;
	public int panValue = 1500;
	public int tiltValue = 1500;
	public int distance = 0;

	private TextView textViewL_;
	private TextView textViewR_;
	private SeekBar seekBarL_;
	private SeekBar seekBarR_;
	private SeekBar seekBarPan_;
	private SeekBar seekBarTilt_;
	private ProgressBar progressBarUS_;

	private ToggleButton toggleButton_;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		seekBarL_ = (SeekBar) findViewById(R.id.SeekBarL);
		seekBarR_ = (SeekBar) findViewById(R.id.SeekBarR);
		seekBarPan_ = (SeekBar) findViewById(R.id.SeekBarPan);
		seekBarTilt_ = (SeekBar) findViewById(R.id.SeekBarTilt);
		textViewL_ = (TextView) findViewById(R.id.TextViewL);
		textViewR_ = (TextView) findViewById(R.id.TextViewR);
		progressBarUS_ = (ProgressBar) findViewById(R.id.progressBarUS);
		toggleButton_ = (ToggleButton) findViewById(R.id.ToggleButton);

		enableUi(false);
	}

	class Looper extends BaseIOIOLooper {
		private DigitalOutput led_;
		private IOIOMotor leftMotor;
		private IOIOMotor rightMotor;
		private IOIOUltrasonic ultrasonic;
		// private IOIOCompoundEye compoundeye;
		private IOIOPanTiltServos pantilt;

		// Pin numbers
		// motor controller
		final int pwmA = 5;
		final int dirA1 = 3;
		final int dirA2 = 4;
		final int pwmB = 14;
		final int dirB1 = 12;
		final int dirB2 = 13;
		// pan tilt servos
		final int pan = 11;
		final int tilt = 10;
		// ultrasonic sensor
		final int echo = 6;
		final int trigger = 7;
		// compound eye
		final int enable = 30;
		final int up = 35;
		final int lt = 35;
		final int rt = 35;
		final int dn = 35;

		@Override
		public void setup() throws ConnectionLostException {
			try {
				led_ = ioio_.openDigitalOutput(IOIO.LED_PIN, true);
				leftMotor = new IOIOMotor(ioio_, pwmB, dirB1, dirB2);
				rightMotor = new IOIOMotor(ioio_, pwmA, dirA1, dirA2);
				ultrasonic = new IOIOUltrasonic(ioio_, echo, trigger);
				// compoundeye = new IOIOCompoundEye(ioio_, enable, up, lt, rt,
				// dn);
				pantilt = new IOIOPanTiltServos(ioio_, pan, tilt);
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
				distance = ultrasonic.readUltrasonic(ioio_);
				if (toggleButton_.isChecked()) {
					leftSpeed = (float) ((float) (((float) (seekBarL_
							.getProgress()) / 1000.0) - 0.5) * 2.0);
					rightSpeed = (float) ((float) (((float) (seekBarR_
							.getProgress()) / 1000.0) - 0.5) * 2.0);
					panValue = 1000 + seekBarPan_.getProgress();
					tiltValue = 1000 + seekBarTilt_.getProgress();
				}
				leftMotor.setSpeed(leftSpeed);
				rightMotor.setSpeed(rightSpeed);
				pantilt.setPanTilt(ioio_, panValue, tiltValue);
				setUi();
				Thread.sleep(50);
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
				seekBarPan_.setEnabled(enable);
				seekBarTilt_.setEnabled(enable);
				
				toggleButton_.setEnabled(enable);
			}
		});
	}

	private void setUi() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				textViewL_.setText(Float.toString(leftSpeed));
				textViewR_.setText(Float.toString(rightSpeed));
				progressBarUS_.setProgress(distance);
			}
		});
	}
}
