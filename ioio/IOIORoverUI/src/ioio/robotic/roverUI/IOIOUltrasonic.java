package ioio.robotic.roverUI;

import ioio.lib.api.PulseInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.PulseInput.PulseMode;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;

public class IOIOUltrasonic {
	private DigitalOutput trigger_;
	private PulseInput echo_;
	private int echoSeconds;
	private int echoDistanceCm;
	private int echoDistanceInch;

	public IOIOUltrasonic(IOIO ioio, int echoPin, int triggerPin)
			throws ConnectionLostException {
		/* ultrasonic sensor */
		echo_ = ioio.openPulseInput(echoPin, PulseMode.POSITIVE);
		trigger_ = ioio.openDigitalOutput(triggerPin);
	}

	public int readUltrasonic(IOIO ioio) throws ConnectionLostException {
		// read HC-SR04 ultrasonic sensor
		try {
			trigger_.write(false);
			Thread.sleep(5);
			trigger_.write(true);
			Thread.sleep(1);
			trigger_.write(false);
			echoSeconds = (int) (echo_.getDuration() * 1000 * 1000);
			setEchoDistanceCm(echoSeconds / 29 / 2);
			setEchoDistanceInch(echoSeconds / 47 / 2);
		} catch (InterruptedException e) {
			ioio.disconnect();
		}
		return echoDistanceCm;
	}

	public void setEchoDistanceCm(int echoDistanceCm) {
		this.echoDistanceCm = echoDistanceCm;
	}

	public int getEchoDistanceCm() {
		return echoDistanceCm;
	}

	public void setEchoDistanceInch(int echoDistanceInch) {
		this.echoDistanceInch = echoDistanceInch;
	}

	public int getEchoDistanceInch() {
		return echoDistanceInch;
	}
}
