package main;

import java.time.Duration;
import java.time.LocalDateTime;

import com.github.sheigutn.pushbullet.items.channel.OwnChannel;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

public class SoundListener implements GpioPinListenerDigital {
	
	private OwnChannel c;
	
	private static final String TITLE = "The dog is beeing noisy";
	
	/*
	 * Keeps current state
	 */
	private LocalDateTime start;
	private int counter;
	
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
    		Duration sinceStart = Duration.between(start, now);
    		if(start != null && sinceStart.compareTo(timeunit) < 0){
    			counter++;
    		}else{
    			start = now;
    			counter = 1;
    		}
    		
    		if(counter >= threshold){
    			c.pushNote(TITLE, "The dog has barked " + counter + " in " + sinceStart);
    			counter = 0;
    			start = null;
    		}
    	}
    }
}
