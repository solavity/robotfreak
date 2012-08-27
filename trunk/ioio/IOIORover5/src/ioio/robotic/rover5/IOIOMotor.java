package ioio.robotic.rover5;

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;

public class IOIOMotor {
	private PwmOutput pwm;
	private DigitalOutput dir1;
	private DigitalOutput dir2;
	private float oldSpeed = 0;
	private boolean oldDir = true;
	private boolean Dir;

	final int pwmFrequency = 100000;

	public IOIOMotor(IOIO ioio, int pwmPin, int dirPin1, int dirPin2)
			throws ConnectionLostException {
		pwm = ioio.openPwmOutput(pwmPin, pwmFrequency);
		pwm.setDutyCycle(0);
		dir1 = ioio.openDigitalOutput(dirPin1, false);
		dir2 = ioio.openDigitalOutput(dirPin2, false);
	}

	public void setSpeed(float speed) throws ConnectionLostException {
		if (speed < 0) {
			Dir = true;
		} else {
			Dir = false;
		}
		if (oldDir != Dir) {
			dir1.write(Dir);
			dir2.write(!Dir);
			oldDir = Dir;
		}
		if (oldSpeed != speed) {
			pwm.setDutyCycle(Math.abs(speed));
			oldSpeed = speed;
		}
	}
}
