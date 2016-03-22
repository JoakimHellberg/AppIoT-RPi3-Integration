package com.ericsson.appiot.demo.integration.rpi3;

import se.sigma.sensation.gateway.sdk.client.SensorMeasurementAcknowledge;
import se.sigma.sensation.gateway.sdk.client.data.ISensorMeasurement;

public class ArduinoDataMeasurement implements ISensorMeasurement {

	private SensorMeasurementAcknowledge acknowledge;
	
	private double[] value;

	private long timestamp;

	private String serialNumber;
	
	private int hardwareTypeId;
	
	
	public int getSensorHardwareTypeId() {
		return hardwareTypeId;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public double[] getValue() {
		return value;
	}

	public void setAcknowledge(SensorMeasurementAcknowledge acknowledge) {
		this.acknowledge = acknowledge;
	}
	
	public SensorMeasurementAcknowledge getAcknowledge() {
		return acknowledge;
	}

	public long getUnixTimestampUTC() {
		return timestamp;
	}

	public void setUnixTimestampUTC(long timestamp) {
		this.timestamp = timestamp;
	}

	public int getHardwareTypeId() {
		return hardwareTypeId;
	}

	public void setHardwareTypeId(int hardwareTypeId) {
		this.hardwareTypeId = hardwareTypeId;
	}

	public void setValue(double[] value) {
		this.value = value;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}
	
}
