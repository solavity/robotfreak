package ioio.robotic.roverUI;

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
import ioio.lib.api.DigitalInput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
//import ioio.robotic.rover5.IOIORover5Activity.Looper;
import ioio.robotic.rover5.R;

public class IOIORoverUIActivity extends IOIOActivity {
	public enum eMode { MOD_CHILLING, MOD_CHAMELEON, MOD_PAPARAZZI, MOD_STALKER }
	public float leftSpeed = 0;
	public float rightSpeed = 0;
	public int panValue = 1500;
	public int tiltValue = 1500;
	public int distance = 0;
	public int panIdx = 0;
	public int cliff;
	
	public int[] pVal = new int[] {1500, 1400, 1300, 1400, 1500, 1600, 1700, 1800, 1700, 1600, 1500};
	public int[] dVal = new int[11];
//	public int ceL, ceR, ceU, ceD;
	public eMode Mode;
	public ImageView animation;
	public AnimationDrawable frameAnimation;
	private TextView textViewDistance_;
//	private TextView textViewLt_;
//	private TextView textViewRt_;
//	private TextView textViewUp_;
//	private TextView textViewDn_;
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        textViewDistance_ = (TextView) findViewById(R.id.textView1);
//        textViewLt_ = (TextView) findViewById(R.id.textView2);
//        textViewRt_ = (TextView) findViewById(R.id.textView3);
//        textViewUp_ = (TextView) findViewById(R.id.textView4);
//        textViewDn_ = (TextView) findViewById(R.id.textView5);
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
			Mode = eMode.MOD_CHILLING;
			Toast.makeText(this, "Chilling selected", Toast.LENGTH_SHORT)
					.show();
	        animation.setBackgroundResource(R.drawable.welcome);		// the frame-by-frame animation defined as a xml file within the drawable folder
			frameAnimation = (AnimationDrawable) animation.getBackground();
		    frameAnimation.start();
        break;
		case R.id.subitem2:
			Mode = eMode.MOD_CHAMELEON;
			Toast.makeText(this, "Chameleon selected", Toast.LENGTH_SHORT)
					.show();
	        animation.setBackgroundResource(R.drawable.chameleon);		// the frame-by-frame animation defined as a xml file within the drawable folder
			frameAnimation = (AnimationDrawable) animation.getBackground();
		    frameAnimation.start();
        break;
		case R.id.subitem3:
			Mode = eMode.MOD_PAPARAZZI;
			Toast.makeText(this, "Paparazzi selected", Toast.LENGTH_SHORT)
					.show();
	        animation.setBackgroundResource(R.drawable.paparazzi);		// the frame-by-frame animation defined as a xml file within the drawable folder
			frameAnimation = (AnimationDrawable) animation.getBackground();
		    frameAnimation.start();
        break;
		case R.id.subitem4:
			Mode = eMode.MOD_STALKER;
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
		private IOIOBlinkM blinkm;
		private IOIOCliffSensor cliffsensor;

		// Pin numbers
		// motor controller
		final int pwmA = 5;
		final int dir1A = 3;
		final int dir2A = 4;
		final int pwmB = 14;
		final int dir1B = 12;
		final int dir2B = 13;
		// pan tilt servos
		final int pan = 11;
		final int tilt = 10;
		// ultrasonic sensor
		final int echo = 6;
		final int trigger = 7;
		// cliff sensors
		final int cliffl = 27;
		final int cliffr = 28;
		// compound eye
		final int enable = 30;
		final int lt = 35;
		final int dn = 36;
		final int rt = 37;
		final int up = 38;

		@Override
		public void setup() throws ConnectionLostException {
			try {
				led_ = ioio_.openDigitalOutput(IOIO.LED_PIN, true);
				cliffsensor = new IOIOCliffSensor(ioio_, cliffl, cliffr);
				leftMotor = new IOIOMotor(ioio_, pwmB, dir1B, dir2B);
				rightMotor = new IOIOMotor(ioio_, pwmA, dir1A, dir2A);
				ultrasonic = new IOIOUltrasonic(ioio_, echo, trigger);
				compoundeye = new IOIOCompoundEye(ioio_, enable, up, rt, dn, lt);
				pantilt = new IOIOPanTiltServos(ioio_, pan, tilt);
				blinkm = new IOIOBlinkM(ioio_, 1, 0x09);
				Mode = eMode.MOD_CHILLING;
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
				cliff = cliffsensor.readCliffSensor(ioio_);
				switch (Mode)
				{
					case MOD_CHILLING:
						leftSpeed = 0;
						rightSpeed = 0;
						panValue = 1500;
						tiltValue = 1500;
					break;
					case MOD_CHAMELEON:
					case MOD_PAPARAZZI:
						if (panIdx > 10 || panIdx < 0)
							panIdx = 0;
						dVal[panIdx] = distance;
						panValue = pVal[panIdx++];
						
						Thread.sleep(200);
//						if (distance > 40) 
//						{
//							leftSpeed = (float) 0.3;
//							rightSpeed = (float) 0.3;
//						}
						if (distance > 20) 
						{
							leftSpeed = (float) 0.2;
							rightSpeed = (float) 0.2;
						}
						else if(distance < 10) 
						{
							leftSpeed = (float) -0.2;
							rightSpeed = (float) -0.2;
						}
						else
						{
							if (panValue < 1300)
							{
								leftSpeed = (float) 0.2;
								rightSpeed = (float) 0;
								
							}
							else if (panValue > 1700)
							{
								leftSpeed = (float) 0;
								rightSpeed = (float) 0.2;
								
							}
							else
							{
								leftSpeed = (float) 0;
								rightSpeed = (float) 0;
							}
						}
					break;
					case MOD_STALKER:
						compoundeye.readCompoundEye(ioio_);
						panValue = compoundeye.getPanVal();
						
//						tiltValue = compoundeye.getTiltVal();
//						ceL = compoundeye.getLtVal();
//						ceR = compoundeye.getRtVal();
//						ceU = compoundeye.getUpVal();
//						ceD = compoundeye.getDnVal();
							
//						if (distance > 40) 
//						{
//							leftSpeed = (float) 0.3;
//							rightSpeed = (float) 0.3;
//						}
						if (distance > 20) 
						{
							leftSpeed = (float) 0.2;
							rightSpeed = (float) 0.2;
						}
						else if(distance < 10) 
						{
							leftSpeed = (float) -0.2;
							rightSpeed = (float) -0.2;
						}
						else
						{
							if (panValue < 1300)
							{
								leftSpeed = (float) 0.2;
								rightSpeed = (float) 0;
								
							}
							else if (panValue > 1700)
							{
								leftSpeed = (float) 0;
								rightSpeed = (float) 0.2;
								
							}
							else
							{
								leftSpeed = (float) 0;
								rightSpeed = (float) 0;
							}
						}
					break;
				}
				if (cliff != 0)
				{
					leftSpeed = (float)-0.2;
					rightSpeed = (float)0.2;
				} 
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
//				if (toggleButton_.isChecked()) {
					
				leftMotor.setSpeed(leftSpeed);
				rightMotor.setSpeed(rightSpeed);
				pantilt.setPanTilt(ioio_, panValue, tiltValue);
//				}
				if (distance < 10) 
				  blinkm.fadeRGB(ioio_, 255, 0, 0);
				else if (distance < 20)
					  blinkm.fadeRGB(ioio_, 255, 255, 0);
				else if (distance < 40)
					  blinkm.fadeRGB(ioio_, 0, 255, 0);
				else 
					  blinkm.fadeRGB(ioio_, 0, 0, 255);
				
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
//				textViewLt_.setText(String.valueOf(ceL));
//				textViewRt_.setText(String.valueOf(ceR));
//				textViewUp_.setText(String.valueOf(ceU));
//				textViewDn_.setText(String.valueOf(ceD));
//				textViewR_.setText(Float.toString(rightSpeed));
//				progressBarUS_.setProgress(distance);
			}
		});
	}
}