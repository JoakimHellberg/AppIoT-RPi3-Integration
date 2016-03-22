package com.ericsson.appiot.demo.integration.rpi3.device;

import se.sigma.sensation.gateway.sdk.client.data.SensorMeasurement;

public interface MeasurementListener {

	void onMeasurement(SensorMeasurement measurement);
	
}
