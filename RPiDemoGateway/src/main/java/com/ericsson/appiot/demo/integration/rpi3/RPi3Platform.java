package com.ericsson.appiot.demo.integration.rpi3;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ericsson.appiot.demo.integration.rpi3.device.Arduino;

import se.sigma.sensation.gateway.sdk.client.Platform;
import se.sigma.sensation.gateway.sdk.client.PlatformInitialisationException;
import se.sigma.sensation.gateway.sdk.client.SensationClient;
import se.sigma.sensation.gateway.sdk.client.data.DataCollectorDeleteResponseCode;
import se.sigma.sensation.gateway.sdk.client.data.DataCollectorStatus;
import se.sigma.sensation.gateway.sdk.client.data.DiscoveredSensorCollection;
import se.sigma.sensation.gateway.sdk.client.data.ISensorMeasurement;
import se.sigma.sensation.gateway.sdk.client.data.NetworkSetting;
import se.sigma.sensation.gateway.sdk.client.data.NetworkSettingResponseCode;
import se.sigma.sensation.gateway.sdk.client.data.RebootResponseCode;
import se.sigma.sensation.gateway.sdk.client.data.RestartApplicationResponseCode;
import se.sigma.sensation.gateway.sdk.client.data.SensorCollectionRegistrationResponseCode;
import se.sigma.sensation.gateway.sdk.client.data.UpdatePackage;
import se.sigma.sensation.gateway.sdk.client.data.UpdatePackageResponseCode;
import se.sigma.sensation.gateway.sdk.client.registry.SensorCollectionRegistration;
import se.sigma.sensation.gateway.sdk.client.registry.SensorRegistration;
import se.sigma.sensation.gateway.sdk.deployment.DeploymentApplicationConnector;
import se.sigma.sensation.gateway.sdk.deployment.bluetooth.JSR82Connector;

public class RPi3Platform implements Platform {
	
	private final Logger logger = Logger.getLogger(this.getClass().getName()); 

	private static final String FIRMWARE_VERSION = "1.0";
	private static final String HARDWARE_VERSION = "1.0";
	private static final String SOFTWARE_VERSION = "1.0";
	private static final int HARDWARE_TYPE_ID = 10000;

	private RPiPlatformManager manager;
	private SensationClient client;
	private DeploymentApplicationConnector bluetoothConnector;
	
	public void init(SensationClient client) throws PlatformInitialisationException {
		this.client = client;
		this.manager = new RPiPlatformManager(client);
		bluetoothConnector = new JSR82Connector();
	}

	public void reportDiscoveredSensorCollections(String correlationId) {
		List<DiscoveredSensorCollection> availableSensorCollections = manager.getAvailableDevices();
		for(DiscoveredSensorCollection discoveredSensorCollection : availableSensorCollections) {
			client.reportDiscoveredSensorCollection(correlationId, discoveredSensorCollection);
		}
	}
	
	private void printRegistration(SensorCollectionRegistration registration) {
		logger.log(Level.INFO, "S/N: " + registration.getSerialNumber());
		logger.log(Level.INFO, "Hardware Type Id: " + registration.getHardwareTypeId());
		
		Hashtable<String, String> settings = registration.getSettings();
		Enumeration<String> keys = settings.keys();
		while(keys.hasMoreElements()) {
			String key = keys.nextElement();
			String value = settings.get(key);
			logger.info(key + "=" + value);
		}
		logger.log(Level.INFO, "SENSORS");
		Iterator<SensorRegistration> sensorRegistrations = registration.sensorRegistrations.iterator();
		while(sensorRegistrations.hasNext()) {
			SensorRegistration sr = sensorRegistrations.next();
			logger.log(Level.INFO, "Type ID: " + sr.getHardwareTypeId() + " SensorID: " + sr.getSensorId());
			Hashtable<String, String> srSettings = sr.getSettings();
			Enumeration<String> srKeys = srSettings.keys();
			while(srKeys.hasMoreElements()) {
				String key = srKeys.nextElement();
				String value = srSettings.get(key);
				logger.info(key + "=" + value);
			}
		}		
	}

	public SensorCollectionRegistrationResponseCode sensorCollectionRegistrationCreated(SensorCollectionRegistration registration) {
		logger.log(Level.INFO, "called");
		printRegistration(registration);

		Arduino registeredDevice = null;
		for(Arduino device : manager.getDevices()) {
			if(registration.getSerialNumber().equalsIgnoreCase(device.getSerialNumber())) {
				registeredDevice = device;
				break;
			}
		}
		
		if(registeredDevice != null) {			
			registeredDevice.Connect();			
			logger.log(Level.INFO, "Successfully registered sensor collection " + registration.getSerialNumber());
			return SensorCollectionRegistrationResponseCode.ADD_OK;
		}
		return SensorCollectionRegistrationResponseCode.UNABLE_TO_HANDLE_REGISTRATION_REQUEST;
	}

	public SensorCollectionRegistrationResponseCode sensorCollectionRegistrationUpdated(SensorCollectionRegistration registration) {
		logger.log(Level.INFO, "called");
		registration.getSettings();
		printRegistration(registration);
		return SensorCollectionRegistrationResponseCode.ADD_OK;
	}

	public SensorCollectionRegistrationResponseCode sensorCollectionRegistrationDeleted(SensorCollectionRegistration registration) {
		logger.log(Level.INFO, "called");
		return SensorCollectionRegistrationResponseCode.DELETE_OK;
	}	
	
	public SensorCollectionRegistration updateSensorCollectionStatus(SensorCollectionRegistration registration) {
		logger.log(Level.INFO, "updateSensorCollectionStatus called.");
		registration.setStatus(200);
		return registration;		
	}

	public DataCollectorStatus updateDataCollectorStatus() {
		DataCollectorStatus result = new DataCollectorStatus();
		result.setBluetoothMacAddress(bluetoothConnector.getBluetoothAddress());
		result.setHardwareTypeId(HARDWARE_TYPE_ID);
		result.setFirmwareVersion(FIRMWARE_VERSION);
		result.setHardwareVersion(HARDWARE_VERSION);
		result.setSoftwareVersion(SOFTWARE_VERSION);
		result.setStatus(200);
		result.setNetworkCards(manager.getNetworkCards());
		return result;
	}
	
	public RebootResponseCode reboot() {		
		logger.log(Level.INFO, "reboot called.");
		manager.reboot();
		return RebootResponseCode.OK;
	}

	public RestartApplicationResponseCode restartApplication() {
		// Handle application restart.	
		logger.log(Level.INFO, "restartApplication called.");
		return RestartApplicationResponseCode.OK;
	}

	
	public NetworkSettingResponseCode addNetworkSetting(NetworkSetting networkSetting) {
		// Handle new network configuration settings.
		logger.log(Level.INFO, "addNetworkSettings called.");
		return NetworkSettingResponseCode.OK;
	}

	public UpdatePackageResponseCode updateSystem(UpdatePackage updatePackage) {
		// Handle FOTA update of system / OS.
		logger.log(Level.INFO, "updateSystem called.");
		return UpdatePackageResponseCode.OK;
	}

	public UpdatePackageResponseCode updateApplication(UpdatePackage updatePackage) {
		// Handle FOTA update of application.
		logger.log(Level.INFO, "updateApplication called.");
		return UpdatePackageResponseCode.OK;
	}
	
	public UpdatePackageResponseCode updateSensorCollection(SensorCollectionRegistration registration, UpdatePackage updatePackage) {
		logger.log(Level.INFO, "Update Sensor Collection called.");
		
		String serialNumber = registration.getSerialNumber();
		UpdatePackageResponseCode responseCode = UpdatePackageResponseCode.OK;
		Arduino device = manager.getDeviceBySerialNumber(serialNumber);
		if(device != null) {
			try {				
				int result = manager.flashDevice(device, updatePackage.getFile());
				if(result != 0) {
					logger.severe("Failed to flash device");
					responseCode = UpdatePackageResponseCode.FAILED_TO_APPLY;
				} else {
					logger.severe("Device successfully flashed!");
					device.Connect();
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Failed flash device", e);
				responseCode = UpdatePackageResponseCode.UNKNOWN_ERROR;
			}
		}
		return responseCode;
	}
	
	public DataCollectorDeleteResponseCode deleteDataCollector(boolean forceDelete) {
		logger.log(Level.INFO, "Delete Data Collector called.");
		return DataCollectorDeleteResponseCode.OK;
	}

	public void handleCustomCommand(String correlationId, String actorId, String payloadType, String payload) {
		logger.log(Level.INFO, "handle Custom Command called.");
		logger.log(Level.INFO, "Payload type: " + payloadType);
		logger.log(Level.INFO, "Payload: " + payload);		
		client.sendCustomCommandResponse(correlationId, actorId, payloadType, payload);
	}

	@Override
	public void acknowledgeMeasurementsSent(List<ISensorMeasurement> measurementsSent) {

		
	}

}
