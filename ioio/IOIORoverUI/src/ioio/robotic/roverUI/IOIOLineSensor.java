package ioio.robotic.roverUI;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;

public class IOIOLineSensor {
	private int[] ls;
	private DigitalOutput enableLS_;
	private AnalogInput line1_;
	private AnalogInput line2_;
	private AnalogInput line3_;
	private AnalogInput line4_;
	private AnalogInput line5_;
	private AnalogInput line6_;

	public IOIOLineSensor(IOIO ioio, int enablePin, int[] lsPins)
			throws ConnectionLostException {
		enableLS_ = ioio.openDigitalOutput(enablePin);
		line1_ = ioio.openAnalogInput(lsPins[0]);
		line2_ = ioio.openAnalogInput(lsPins[0]);
		line3_ = ioio.openAnalogInput(lsPins[0]);
		line4_ = ioio.openAnalogInput(lsPins[0]);
		line5_ = ioio.openAnalogInput(lsPins[0]);
		line6_ = ioio.openAnalogInput(lsPins[0]);
	}

	public void readLineSensor(IOIO ioio) throws ConnectionLostException {
		enableLS_.write(false);
		try {
			Thread.sleep(5);
			ls[0] = (int) (line1_.getVoltage() * 1000);
			ls[1] = (int) (line2_.getVoltage() * 1000);
			ls[2] = (int) (line3_.getVoltage() * 1000);
			ls[2] = (int) (line4_.getVoltage() * 1000);
			ls[4] = (int) (line5_.getVoltage() * 1000);
			ls[5] = (int) (line6_.getVoltage() * 1000);
		} catch (InterruptedException e) {
			ioio.disconnect();
			e.printStackTrace();
		}
		enableLS_.write(true);

	}
}
