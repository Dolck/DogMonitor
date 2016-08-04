package main;

import java.time.Duration;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sheigutn.pushbullet.items.channel.OwnChannel;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

public class SoundListener implements GpioPinListenerDigital {
	
	private static final Logger log = LoggerFactory.getLogger(SoundListener.class);
	
	private OwnChannel c;
	
	private static final String TITLE = "The dog is beeing noisy";
	
	/*
	 * Keeps current state
	 */
	private volatile LocalDateTime start;
	private volatile int counter;
	
	/*
	 * threshold and timeunit decides when to notify on pushbullet channel
	 */
	private final int threshold;
	private final Duration timeunit;
	
	/**
	 * 
	 * @param channel
	 * @param threshold
	 * @param timeunit
	 */
	public SoundListener(OwnChannel channel, int threshold, Duration timeunit){
		this.c = channel;
		this.threshold = threshold;
		this.timeunit = timeunit;
	}
	
    @Override
    public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
    	
    	if(event.getState().isHigh()){
    		
    		LocalDateTime now = LocalDateTime.now();
    		log.debug("Sound detected, counter {}", counter);
    		if(start != null && Duration.between(start, now).compareTo(timeunit) < 0){
    			counter++;
    		}else{
    			start = now;
    			counter = 1;
    		}
    		
    		if(counter >= threshold){
    			c.pushNote(TITLE, "The dog has barked " + counter + " in " + Duration.between(start, now));
    			reset();
    		}
    	}
    }
    
    public void reset(){
    	counter = 0;
    	start = null;
    }
}
