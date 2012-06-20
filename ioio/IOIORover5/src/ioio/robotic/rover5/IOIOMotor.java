package ioio.robotic.rover5;

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;

public class IOIOMotor {
	private PwmOutput pwm;
	private DigitalOutput dir;
	private float oldSpeed = 0;
	private boolean oldDir = true;
	private boolean Dir;

	final int pwmFrequency = 100000;

	public IOIOMotor(IOIO ioio, int pwmPin, int dirPin)
			throws ConnectionLostException {
		pwm = ioio.openPwmOutput(pwmPin, pwmFrequency);
		pwm.setDutyCycle(0);
		dir = ioio.openDigitalOutput(dirPin, false);
	}

	public void setSpeed(float speed) throws ConnectionLostException {
		if (speed < 0) {
			Dir = true;
		} else {
			Dir = false;
		}
		if (oldDir != Dir) {
			dir.write(Dir);
			oldDir = Dir;
		}
		if (oldSpeed != speed) {
			pwm.setDutyCycle(Math.abs(speed));
			oldSpeed = speed;
		}
	}
}
