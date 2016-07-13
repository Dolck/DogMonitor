package main;

import com.github.sheigutn.pushbullet.Pushbullet;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

public class SoundListener implements GpioPinListenerDigital {
	
	private Pushbullet pushbullet;
	
	//TODO: keep state here
	
	public SoundListener(Pushbullet pushbullet){
		this.pushbullet = pushbullet;
	}
	
    @Override
    public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
    	//TODO: handle pin signal and push notification over pushbullet
    }
}
