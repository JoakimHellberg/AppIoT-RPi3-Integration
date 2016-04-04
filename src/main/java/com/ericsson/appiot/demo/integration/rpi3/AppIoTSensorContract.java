package com.ericsson.appiot.demo.integration.rpi3;

import java.util.Hashtable;

public class AppIoTSensorContract {

	private Hashtable<String, Integer> sensorHardswareTypeIds = new Hashtable<String, Integer>();
	
	private static AppIoTSensorContract instance;
	
	public static final int RPi3_HARDWARE_TYPE_ID 		= 10000;
	public static final int ARDUINO_HARDWARE_TYPE_ID 	= 10001;
	
	public AppIoTSensorContract() {
		sensorHardswareTypeIds.put("TEMP", 1);
		sensorHardswareTypeIds.put("HUM", 2);
	}
	
	private static AppIoTSensorContract getInstance() {
		if(instance == null) {
			instance = new AppIoTSensorContract();
		}
		return instance;
	}
	
	public static Integer getSensorHardwareTypeId(String platformIdentifier) {
		return getInstance().sensorHardswareTypeIds.get(platformIdentifier);
	}
	
	
	
}
