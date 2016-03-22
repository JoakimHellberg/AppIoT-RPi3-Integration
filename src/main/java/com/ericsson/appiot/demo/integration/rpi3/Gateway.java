package com.ericsson.appiot.demo.integration.rpi3;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import se.sigma.sensation.gateway.sdk.client.SensationClient;

public class Gateway {
	private final Logger logger = Logger.getLogger(this.getClass().getName()); 

	private RPi3Platform platform;
	private SensationClient sensationClient;
	
	
	public static void main(String[] args) {
		Gateway gateway = new Gateway();
		gateway.start();
	}
	
	private void start() {
		logger.log(Level.INFO, "RPi3 Gateway starting up.");
		
		platform = new RPi3Platform();		
		sensationClient = new SensationClient(platform); 
		sensationClient.start();

		if(!sensationClient.isRegistered()) {
			sensationClient.enableDeploymentApplicationConnection();
		}
		
		final GpioController gpio = GpioFactory.getInstance();
		final GpioPinDigitalInput myButton = gpio.provisionDigitalInputPin(RaspiPin.GPIO_26, PinPullResistance.PULL_DOWN);
		myButton.addListener(new GpioPinListenerDigital() {
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
            	if(event.getState().isHigh()) {
            		sensationClient.enableDeploymentApplicationConnection();
                	logger.log(Level.INFO, "Starting deployment interface.");
            	} 
            }
        });		
		
		while(true) {
			try {Thread.sleep(10000);}
			catch(InterruptedException e) {}
		}
	}	
}
