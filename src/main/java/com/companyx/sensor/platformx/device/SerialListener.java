package com.companyx.sensor.platformx.device;

import java.io.UnsupportedEncodingException;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataListener;

public class SerialListener implements SerialDataListener {
	private final Logger logger = Logger.getLogger(this.getClass().getName()); 
	
	private ArduinoDevice device;
	public SerialListener(ArduinoDevice device) {
		this.device = device;
	}
	
	public void dataReceived(SerialDataEvent event) {
       	try {
			String msg = new String(event.getData().getBytes(), "UTF-8");
			StringTokenizer stRows = new StringTokenizer(msg, "\n");
			while(stRows.hasMoreTokens()) {
				String row = stRows.nextToken();
				boolean validMeasurement = false;
				if(row.indexOf(";") != -1) {
					String sensorType = null;
					String serialNumber = null;
					Double value = null;
					StringTokenizer stDatas = new StringTokenizer(row, ";");
					while(stDatas.hasMoreTokens()) {
						String data = stDatas.nextToken();
						StringTokenizer measurement = new StringTokenizer(data, ":");
						if(measurement.hasMoreTokens()) {
							sensorType = measurement.nextToken();
							if(measurement.hasMoreTokens()) {
								String valuestr = measurement.nextToken();
								if("ID".equals(sensorType)) {
									serialNumber = valuestr;
									validMeasurement = true;
								} else {
									value = Double.parseDouble(valuestr);
									validMeasurement = true;
								}
							}
						}
					}
					if(validMeasurement) {
						DeviceData deviceData = new DeviceData(sensorType, serialNumber, value);
						this.device.newMeasurement(deviceData);
					}
				} 
			}
       	} catch (UnsupportedEncodingException e) {
			logger.log(Level.SEVERE, "UTF-8 not supported.", e);
		}
	}
}
