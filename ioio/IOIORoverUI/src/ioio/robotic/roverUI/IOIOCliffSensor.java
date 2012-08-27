package ioio.robotic.roverUI;

import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.api.DigitalInput;

public class IOIOCliffSensor {
	private DigitalInput leftCliff_;
	private DigitalInput rightCliff_;
	private int cliffret;
	
	public IOIOCliffSensor(IOIO ioio, int leftPin, int rightPin)
			throws ConnectionLostException {
		/* cliff sensor */
		leftCliff_ = ioio.openDigitalInput(leftPin);
		rightCliff_ = ioio.openDigitalInput(rightPin);
	}
	public int readCliffSensor(IOIO ioio) throws ConnectionLostException {
		try {
			if (leftCliff_.read() || rightCliff_.read())
				cliffret = 1;
			else
				cliffret = 0;
		} catch (InterruptedException e) {
			ioio.disconnect();
			e.printStackTrace();
		}
		return cliffret;

	}

}
