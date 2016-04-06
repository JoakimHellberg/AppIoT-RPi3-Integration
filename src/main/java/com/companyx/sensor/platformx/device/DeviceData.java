package com.companyx.sensor.platformx.device;

public final class DeviceData {

	private final String serialNumber;
	private final String sensorType;
	private final Double value;

	public DeviceData(String serialNumber, String sensorType) {
		this(serialNumber, sensorType, null);
	}

	public DeviceData(String serialNumber, String sensorType, Double value) {
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

	public Double getValue() {
		return value;
	}
}
