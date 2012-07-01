package ioio.robotic.rover5;

//import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
//import ioio.robotic.rover5.IOIORover5Activity.Looper;

public class IOIORoverUIActivity extends IOIOActivity {
	public float leftSpeed = 0;
	public float rightSpeed = 0;
	public int panValue = 1500;
	public int tiltValue = 1500;
	public int distance = 0;
	public ImageView animation;
	public AnimationDrawable frameAnimation;
	private TextView textViewDistance_;
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        textViewDistance_ = (TextView) findViewById(R.id.textView1);
        animation = (ImageView)findViewById(R.id.imageAnimation);
     
        animation.setBackgroundResource(R.drawable.welcome);		// the frame-by-frame animation defined as a xml file within the drawable folder
        
        /*
         * NOTE: It's not possible to start the animation during the onCreate.
         */ 
		enableUi(false);
   }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mainmenu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.subitem1:
			Toast.makeText(this, "Idle selected", Toast.LENGTH_SHORT)
					.show();
	        animation.setBackgroundResource(R.drawable.welcome);		// the frame-by-frame animation defined as a xml file within the drawable folder
			frameAnimation = (AnimationDrawable) animation.getBackground();
		    frameAnimation.start();
        break;
		case R.id.subitem2:
			Toast.makeText(this, "Chameleon selected", Toast.LENGTH_SHORT)
					.show();
	        animation.setBackgroundResource(R.drawable.chameleon);		// the frame-by-frame animation defined as a xml file within the drawable folder
			frameAnimation = (AnimationDrawable) animation.getBackground();
		    frameAnimation.start();
        break;
		case R.id.subitem3:
			Toast.makeText(this, "Paparazzi selected", Toast.LENGTH_SHORT)
					.show();
	        animation.setBackgroundResource(R.drawable.paparazzi);		// the frame-by-frame animation defined as a xml file within the drawable folder
			frameAnimation = (AnimationDrawable) animation.getBackground();
		    frameAnimation.start();
        break;
		case R.id.subitem4:
			Toast.makeText(this, "Stalker selected", Toast.LENGTH_SHORT)
					.show();
	        animation.setBackgroundResource(R.drawable.stalker);		// the frame-by-frame animation defined as a xml file within the drawable folder
			frameAnimation = (AnimationDrawable) animation.getBackground();
		    frameAnimation.start();
        break;

		default:
			break;
		}

		return true;
	}
    
	class Looper extends BaseIOIOLooper {
		private DigitalOutput led_;
		private IOIOMotor leftMotor;
		private IOIOMotor rightMotor;
		private IOIOUltrasonic ultrasonic;
		private IOIOCompoundEye compoundeye;
		private IOIOPanTiltServos pantilt;

		// Pin numbers
		// motor controller
		final int pwmA = 3;
		final int dirA = 4;
		final int pwmB = 13;
		final int dirB = 14;
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
				leftMotor = new IOIOMotor(ioio_, pwmB, dirB);
				rightMotor = new IOIOMotor(ioio_, pwmA, dirA);
				ultrasonic = new IOIOUltrasonic(ioio_, echo, trigger);
				compoundeye = new IOIOCompoundEye(ioio_, enable, up, lt, rt, dn);
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
//				led_.write(!toggleButton_.isChecked());
				distance = ultrasonic.readUltrasonic(ioio_);
//				if (toggleButton_.isChecked()) {
//					leftSpeed = (float) ((float) (((float) (seekBarL_
//							.getProgress()) / 1000.0) - 0.5) * 2.0);
//					rightSpeed = (float) ((float) (((float) (seekBarR_
//							.getProgress()) / 1000.0) - 0.5) * 2.0);
//					panValue = 1000 + seekBarPan_.getProgress();
//					tiltValue = 1000 + seekBarTilt_.getProgress();
//				}
//				else {
//					leftSpeed = 0;
//					rightSpeed = 0;
//				}
					
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
			public void run() {
				frameAnimation = (AnimationDrawable) animation.getBackground();
  			    frameAnimation.start();
//				seekBarL_.setEnabled(enable);
//				seekBarR_.setEnabled(enable);
//				seekBarPan_.setEnabled(enable);
//				seekBarTilt_.setEnabled(enable);
				
//				toggleButton_.setEnabled(enable);
			}
		});
	}

	private void setUi() {
		runOnUiThread(new Runnable() {
			public void run() {
				textViewDistance_.setText(String.valueOf(distance) + " cm");
//				textViewR_.setText(Float.toString(rightSpeed));
//				progressBarUS_.setProgress(distance);
			}
		});
	}
}