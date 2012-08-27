package ioio.robotic.roverUI;

import ioio.lib.api.PwmOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;

public class IOIOPanTiltServos {
	private PwmOutput servoPan_;
	private PwmOutput servoTilt_;
	private int oldPan = 1500;
	private int oldTilt = 1500;

	public IOIOPanTiltServos(IOIO ioio, int panPin, int tiltPin)
			throws ConnectionLostException {
		servoPan_ = ioio.openPwmOutput(panPin, 50);
		servoTilt_ = ioio.openPwmOutput(tiltPin, 50);
	}

	public void setPanTilt(IOIO ioio, int pan, int tilt)
			throws ConnectionLostException {
		if (pan < 1000)
			pan = 1000;
		if (pan > 2000)
			pan = 2000;
		if (tilt < 1000)
			tilt = 1000;
		if (tilt > 1900)
			tilt = 1900;
		if (pan != oldPan) {
			servoPan_.setPulseWidth(pan);
			oldPan = pan;
		}
		if (tilt != oldTilt) {
			servoTilt_.setPulseWidth(tilt);
			oldTilt = tilt;
		}
	}
}
