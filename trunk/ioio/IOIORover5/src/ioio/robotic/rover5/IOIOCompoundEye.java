package ioio.robotic.rover5;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;

public class IOIOCompoundEye {
	private DigitalOutput enableCE_;
	private AnalogInput ceUp_;
	private AnalogInput ceLt_;
	private AnalogInput ceRt_;
	private AnalogInput ceDn_;
	private int ceUp, ceLt, ceRt, ceDn;
	private int onUp, onLt, onRt, onDn;
	private int ofUp, ofLt, ofRt, ofDn;
	private int distance;
	private int valPan, valTilt;
	private int panScale, tiltScale;
	private int leftright, updown;

	public IOIOCompoundEye(IOIO ioio, int enablePin, int upPin, int rtPin,
			int dnPin, int ltPin) throws ConnectionLostException {
		enableCE_ = ioio.openDigitalOutput(enablePin);
		ceUp_ = ioio.openAnalogInput(upPin);
		ceRt_ = ioio.openAnalogInput(rtPin);
		ceDn_ = ioio.openAnalogInput(dnPin);
		ceLt_ = ioio.openAnalogInput(ltPin);
	}

	public void readCompoundEye(IOIO ioio) throws ConnectionLostException {
		try {
			// read compound eye IR sensor
			enableCE_.write(false);
			Thread.sleep(5);
			onUp = (int) (ceUp_.getVoltage() * 1000);
			onLt = (int) (ceLt_.getVoltage() * 1000);
			onRt = (int) (ceRt_.getVoltage() * 1000);
			onDn = (int) (ceDn_.getVoltage() * 1000);
			enableCE_.write(true);
			Thread.sleep(5);
			ofUp = (int) (ceUp_.getVoltage() * 1000);
			ofLt = (int) (ceLt_.getVoltage() * 1000);
			ofRt = (int) (ceRt_.getVoltage() * 1000);
			ofDn = (int) (ceDn_.getVoltage() * 1000);
		} catch (InterruptedException e) {
			ioio.disconnect();
		}
		ceUp = onUp - ofUp;
		ceLt = onLt - ofLt;
		ceRt = onRt - ofRt;
		ceDn = onDn - ofDn;
		distance = (ceUp + ceLt + ceRt + ceDn) / 4;
		if (distance < 300) {
			if (valPan > 1500)
				valPan = valPan - 10;
			if (valPan < 1500)
				valPan = valPan + 10;
			if (valTilt > 1500)
				valTilt = valTilt - 10;
			if (valTilt < 1500)
				valTilt = valTilt + 10;
		} else {
			panScale = (ceLt + ceRt) / 3;
			tiltScale = (ceUp + ceDn) / 3;

			leftright = (ceLt - ceRt) * 5 / panScale;
			valPan = valPan + leftright * 10;
			updown = (ceDn - ceUp) * 5 / tiltScale;
			valTilt = valTilt + updown * 10;

		}
		if (valPan < 1000)
			valPan = 1000;
		if (valPan > 2000)
			valPan = 2000;
		if (valTilt < 1000)
			valTilt = 1000;
		if (valTilt > 1900)
			valTilt = 1700;
	}
	
	public int getPanVal()
	{
		return this.valPan;
	}

	public int getTiltVal()
	{
		return this.valTilt;
	}
}
