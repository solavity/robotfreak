package ioio.ardumoto;

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException; 

public class IOIOMotor {
	private PwmOutput pwm;
	private DigitalOutput dir;
	
	final int pwmFrequency = 100000;
	
	public IOIOMotor(IOIO ioio, int pwmPin, int dirPin) throws ConnectionLostException {
		pwm = ioio.openPwmOutput(pwmPin, pwmFrequency);
		pwm.setDutyCycle(0);
		dir = ioio.openDigitalOutput(dirPin, false);
	}
	
	public void setSpeed(float speed) throws ConnectionLostException {
		if (speed < 0) {
			dir.write(true);
		} else if (speed > 0) {
			dir.write(false);
		} else {
			dir.write(false);
		}
		pwm.setDutyCycle(Math.abs(speed));
	} 
}
