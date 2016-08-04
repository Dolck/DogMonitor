package main;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sheigutn.pushbullet.Pushbullet;
import com.github.sheigutn.pushbullet.items.channel.OwnChannel;
import com.github.sheigutn.pushbullet.items.push.sent.Direction;
import com.github.sheigutn.pushbullet.items.push.sent.defaults.NotePush;
import com.github.sheigutn.pushbullet.stream.PushbulletWebsocketClient;
import com.github.sheigutn.pushbullet.stream.PushbulletWebsocketListener;
import com.github.sheigutn.pushbullet.stream.message.StreamMessage;
import com.github.sheigutn.pushbullet.stream.message.StreamMessageType;
import com.github.sheigutn.pushbullet.stream.message.TickleStreamMessage;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.RaspiPin;

public class Main {

	private static final Logger log = LoggerFactory.getLogger(Main.class);
	
	/**
	 * @param args
	 * 			args[0] = pushbullet api token
	 * 			args[1] = pushbullet channel tag
	 */
	public static void main(String[] args) {
		if(args.length < 2){
			log.error("Not enough arguments. Exiting application");
			return;
		}
		String apitoken = args[0];
		Pushbullet pushbullet = new Pushbullet(apitoken);
		pushbullet.getNewPushes(); //clear new pushes
		
		OwnChannel channel = pushbullet.getOwnChannel(args[1]);
		if(channel == null){
			log.error("Invalid pushbullet channel {}", args[1]);
			return;
		}
		
		final GpioController gpio = GpioFactory.getInstance();
		final GpioPinDigitalInput inPin = gpio.provisionDigitalInputPin(RaspiPin.GPIO_00); // listening at pin 0
		final SoundListener sl = new SoundListener(channel, 5, Duration.ofMinutes(5));
		
		PushbulletWebsocketClient pwc = pushbullet.createWebsocketClient();
		pwc.connect();
		pwc.registerListener(new PushbulletWebsocketListener() {
			
			@Override
			public void handle(Pushbullet pb, StreamMessage message) {
				
				if(message.getType() == StreamMessageType.TICKLE){
					TickleStreamMessage tsm = (TickleStreamMessage)message;
					if(tsm.getSubType().equals("push")){
						for(NotePush np : pb.getNewPushes(NotePush.class)){
							if(np.getDirection().equals(Direction.INCOMING)){
								String body = np.getBody().trim().toUpperCase();
								switch(body){
								case "START":
									log.info("START");
									//make sure that there is maximum of one listener
									gpio.removeAllListeners();
									gpio.addListener(sl, inPin);
									np.dismiss();
									
									channel.pushNote("Started", "Monitor is active");
									break;
								case "STOP": 
									log.info("STOP");
									gpio.removeAllListeners();
									np.dismiss();
									sl.reset();
									
									channel.pushNote("Stopped", "Monitor is now stopped");
									break;
								case "STATUS": 
									if(inPin.getListeners().isEmpty()){
										//Not active
										channel.pushNote("Status", "Monitor is NOT active");
									}else{
										//Active
										channel.pushNote("Status", "Monitor is active");
									}
									break;
								case "HELP": 
									channel.pushNote("Help", "Available commands:\n -Help \n -Start \n -Stop \n -Status");
									break;
								}
							}
						}
					}
				}
				
			}
		});
		log.info("Started monitor");
		while(true){
			try {
				Thread.sleep(1000000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}