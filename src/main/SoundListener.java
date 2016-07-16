package main;

import com.github.sheigutn.pushbullet.items.channel.OwnChannel;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

public class SoundListener implements GpioPinListenerDigital {
	
	private OwnChannel c;
	
	//TODO: keep state here
	
	public SoundListener(OwnChannel channel){
		this.c = channel;
	}
	
    @Override
    public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
    	//TODO: handle pin signal and push notification over pushbullet
    	
    	if(event.getState().isHigh()){
    		c.pushNote("Pin HIGH!", "Noisy mf");
    		
    	}else{
    		c.pushNote("Pin LOW!", "oh sweet quiet");
    	}
    	
    	
    }
}
