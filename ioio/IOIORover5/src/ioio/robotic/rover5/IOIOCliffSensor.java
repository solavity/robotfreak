package ioio.robotic.rover5;

import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.api.DigitalInput;

public class IOIOCliffSensor {
	private DigitalInput leftCliff_;
	private DigitalInput rightCliff_;
	
	public IOIOCliffSensor(IOIO ioio, int leftPin, int rightPin)
			throws ConnectionLostException {
		/* ultrasonic sensor */
		leftCliff_ = ioio.openDigitalInput(leftPin);
		rightCliff_ = ioio.openDigitalInput(rightPin);
	}

}
