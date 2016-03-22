package com.ericsson.appiot.demo.integration.rpi3.device;

import java.io.UnsupportedEncodingException;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataListener;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.SerialPortException;

import se.sigma.sensation.gateway.sdk.client.data.SensorMeasurement;

public class Arduino {
	private final Logger logger = Logger.getLogger(this.getClass().getName()); 

	private String serialNumber;
	private int hardwareTypeId = 10001;

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

	public MeasurementListener getListener() {
		return listener;
	}

	public Serial getSerial() {
		return serial;
	}

	private MeasurementListener listener;
	
	
	private final Serial serial; 
	public Arduino(String serialNumber, String device, int baudrate) {
		this.serialNumber = serialNumber;
		this.device = device;
		this.baudRate = baudrate;

		serial = SerialFactory.createInstance();
	}	
	
	public void Connect() throws SerialPortException {
		serial.open(getDevice(), getBaudRate());
		serial.addListener(new ArduinoListener());		
	}
	
	public void setListener(MeasurementListener listener) {
		this.listener = listener;
	}
	
	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	public int getHardwareTypeId() {
		return hardwareTypeId;
	}

	public void setHardwareTypeId(int hardwareTypeId) {
		this.hardwareTypeId = hardwareTypeId;
	}

	private class ArduinoListener implements SerialDataListener {
		public void dataReceived(SerialDataEvent event) {
	       	try {
				String msg = new String(event.getData().getBytes(), "UTF-8");
				StringTokenizer st = new StringTokenizer(msg, "\n");
				while(st.hasMoreTokens()) {
					String row = st.nextToken();
					if(row.indexOf(":") != -1) {
						StringTokenizer st2 = new StringTokenizer(row, ":");
						String idstr = st2.nextToken();
						String valuestr = st2.nextToken();
						
						int id = Integer.parseInt(idstr);
						double value = Double.parseDouble(valuestr);
						
						SensorMeasurement measurement = new SensorMeasurement();
						measurement.setSensorHardwareTypeId(id);
						measurement.setSerialNumber(getSerialNumber());
						measurement.setValue(new double[]{value});
						measurement.setUnixTimestampUTC(System.currentTimeMillis());
						
						fireOnSensorMeasurement(measurement);
					} 
				}
	       	} catch (UnsupportedEncodingException e) {
				logger.log(Level.SEVERE, "UTF-8 not supported.", e);
			}
		}

		private void fireOnSensorMeasurement(SensorMeasurement measurement) {
			if(listener != null) {
				listener.onMeasurement(measurement);
			}
		}
	}
}
