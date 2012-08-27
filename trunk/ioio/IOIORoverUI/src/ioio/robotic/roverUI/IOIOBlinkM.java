package ioio.robotic.roverUI;

import ioio.lib.api.TwiMaster;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;

public class IOIOBlinkM {
	private TwiMaster twi_;
	private int address;
	
	public IOIOBlinkM(IOIO ioio, int twiNum, int twiAdr)
	throws ConnectionLostException {
		twi_ = ioio.openTwiMaster(twiNum, TwiMaster.Rate.RATE_100KHz, false);
		address = twiAdr;
	}
	
	public void fadeRGB(IOIO ioio, int r, int g, int b)
	 throws ConnectionLostException {
		try {
			byte[] request = new byte[] { 'c', 0x00, 0x00, 0x00 };
			byte[] response = new byte[4];
			request[1] = (byte) r;
			request[2] = (byte) g;
			request[3] = (byte) b;
			twi_.writeRead(address, false, request, request.length, response, 0);		
			} catch (InterruptedException e) {
			ioio.disconnect();
		}
		}

}
