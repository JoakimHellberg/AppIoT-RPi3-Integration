package com.companyx.sensor.platform;

public final class ArduinoData {

	private final String serialNumber;
	private final String sensorType;
	private final double value;

	public ArduinoData(String serialNumber, String sensorType, double value) {
		this.serialNumber = serialNumber;
		this.sensorType = sensorType;
		this.value = value;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public String getSensorType() {
		return sensorType;
	}

	public double getValue() {
		return value;
	}

}
