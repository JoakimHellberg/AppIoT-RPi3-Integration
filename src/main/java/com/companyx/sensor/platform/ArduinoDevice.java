package com.companyx.sensor.platform;

import java.io.UnsupportedEncodingException;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataListener;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.SerialPortException;

public class ArduinoDevice {
	
	private final Logger logger = Logger.getLogger(this.getClass().getName()); 

	private String serialNumber;

	private String device;
	private int baudRate;
	
	public Logger getLogger() {
		return logger;
	}

	public String getDevice() {
		return device;
	}

	public int getBaudRate() {
		return baudRate;
	}

	public DeviceListener getListener() {
		return listener;
	}

	public Serial getSerial() {
		return serial;
	}

	private DeviceListener listener;
	
	
	private final Serial serial; 
	public ArduinoDevice(String serialNumber, String device, int baudrate) {
		this.serialNumber = serialNumber;
		this.device = device;
		this.baudRate = baudrate;

		serial = SerialFactory.createInstance();
	}

	public void connect() throws SerialPortException {
		serial.open(getDevice(), getBaudRate());
		serial.addListener(new SerialListener());		
	}
	
	public void setListener(DeviceListener listener) {
		this.listener = listener;
	}
	
	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	private class SerialListener implements SerialDataListener {
		public void dataReceived(SerialDataEvent event) {
			try {
				String msg = new String(event.getData().getBytes(), "UTF-8");
				StringTokenizer st = new StringTokenizer(msg, "\n");
				while (st.hasMoreTokens()) {
					String row = st.nextToken();
					if (row.indexOf(":") != -1) {
						StringTokenizer st2 = new StringTokenizer(row, ":");
						if (st2.countTokens() == 2) {
							String type = st2.nextToken();
							String valuestr = st2.nextToken();

							try {
								double value = Double.parseDouble(valuestr);

								ArduinoData data = new ArduinoData();
								data.setSerialNumber(serialNumber);
								data.setSensorType(type);
								data.setValue(value);

								fireOnSensorMeasurement(data);
							} catch (NumberFormatException e) {
								logger.log(Level.INFO, "Unable to parse value: " + valuestr);
							}
						} else {
							logger.log(Level.INFO, "Discarding recieved row, too few tokens: " + row);
						}
					}
				}
			} catch (UnsupportedEncodingException e) {
				logger.log(Level.SEVERE, "UTF-8 not supported.", e);
			}
		}

		private void fireOnSensorMeasurement(ArduinoData data) {
			if(listener != null) {
				listener.onData(data);
			}
		}
	}
}
