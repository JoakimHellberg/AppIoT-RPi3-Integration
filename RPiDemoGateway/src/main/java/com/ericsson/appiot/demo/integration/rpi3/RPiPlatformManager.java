package com.ericsson.appiot.demo.integration.rpi3;

import java.io.File;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ericsson.appiot.demo.integration.rpi3.device.Arduino;
import com.ericsson.appiot.demo.integration.rpi3.device.MeasurementListener;

import se.sigma.sensation.gateway.sdk.client.SensationClient;
import se.sigma.sensation.gateway.sdk.client.data.DiscoveredSensorCollection;
import se.sigma.sensation.gateway.sdk.client.data.NetworkCard;
import se.sigma.sensation.gateway.sdk.client.data.SensorMeasurement;
import se.sigma.sensation.gateway.sdk.client.platform.ConnectivitySettings;
import se.sigma.sensation.gateway.sdk.client.platform.linux.LinuxManager;

public class RPiPlatformManager {

	private final Logger logger = Logger.getLogger(this.getClass().getName()); 
	private List<DiscoveredSensorCollection> availableDevices;
	
	private List<Arduino> devices = new Vector<Arduino>();
	
	private final SensationClient client;
	private LinuxManager linuxManager;
	
	public RPiPlatformManager(SensationClient client) {
		this.client = client;
		this.linuxManager = new LinuxManager();
		Arduino device = new Arduino("123", "/dev/ttyUSB0", 9600);
		device.setListener(new MyMeasurementListener());
		devices.add(device);
		
		if(client.isSerialNumberRegistered(device.getSerialNumber())) {
			device.Connect();
		}
	}
	
	public List<Arduino> getDevices() {
		return devices;
	}
	
	public Arduino getDeviceBySerialNumber(String serialNumber) {
		for(Arduino arduino : getDevices()) {
			if(arduino.getSerialNumber().equals(serialNumber)) {
				return arduino;
			}
		}
		return null;
	}

	public List<DiscoveredSensorCollection> getAvailableDevices() {
		availableDevices = new Vector<DiscoveredSensorCollection>();
		
		// Perform network scan, query local database or however your gateway interacts with devices.
		// For this example we just return the set of simulated devices.
		for(int i = 0; i < devices.size(); i++){
			Arduino device = devices.get(i);
			DiscoveredSensorCollection deviceDiscovered = new DiscoveredSensorCollection();
			
			// Set Device unique identifier e.g. serial number.
			// This is what you enter in Sensation when registering a new sensor collection.
			deviceDiscovered.setSerialNumber(device.getSerialNumber()); 
			
			// Set the time of when the device was last seen.
			deviceDiscovered.setLastObserved(System.currentTimeMillis());
			
			// Setting the signal strength makes Sensation able to find the best suitable gateway 
			// to register the device to. The gateway with best signal strength is put in top of the list. 
			deviceDiscovered.setSignalStrength(-45);
			availableDevices.add(deviceDiscovered);
		}
		return availableDevices;
	}
	
	public List<NetworkCard> getNetworkCards() {
		return linuxManager.getNetworkCards();
	}
	
	public ConnectivitySettings getConnectivitySettings(String adapterName) {
		return linuxManager.getConnectivitySettings(adapterName);
	}
	
	public int flashDevice(Arduino device, File file) {

		String command1 = "/usr/bin/ard-reset-arduino " + device.getDevice();
		
		String command2 = "/usr/share/arduino/hardware/tools/avr/../avrdude"
				+ " -q -V -D -p atmega328p"
				+ " -C /usr/share/arduino/hardware/tools/avr/../avrdude.conf"
				+ " -c arduino"
				+ " -b 57600"
				+ " -P " + device.getDevice() 
				+ " -U flash:w:" + file.getAbsoluteFile() + ":i";
		
		logger.info("Flashing file + " + file.getAbsoluteFile());
		
		int exitCode = 0;
		try {
			
			exitCode = linuxManager.executeBash(command1);
			if(exitCode != 0 ) {
				logger.severe("Failed to reset device " + device.getDevice());
				return exitCode;
			}
			
			exitCode = linuxManager.executeBash(command2);
			if(exitCode != 0 ) {
				logger.severe("Failed to flash device " + device.getDevice());
			}
	        logger.fine("Device " + device.getSerialNumber() + " successfully flashed");
		} catch(Exception e) {
			logger.log(Level.SEVERE, "flash device " + device.getSerialNumber(), e);
		}   
		return exitCode;
	}
	
	public int reboot() {	
		return linuxManager.reboot();
	}
	
	private class MyMeasurementListener implements MeasurementListener {
		public void onMeasurement(SensorMeasurement measurement) {
			if(client.isRegistered()) {
				client.sendSensorMeasurement(measurement);
			}
		}		
	}
}
