package main;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sheigutn.pushbullet.Pushbullet;
import com.github.sheigutn.pushbullet.items.channel.OwnChannel;
import com.github.sheigutn.pushbullet.items.device.Device;
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
		List<Device> devices = pushbullet.getDevices();
		for(Device d: devices){
			log.info("device: {}", d.getNickname());
		}
		
		OwnChannel c = pushbullet.getOwnChannel(args[1]);
		log.info("channel: {}", c);
		
		final GpioController gpio = GpioFactory.getInstance();
		final GpioPinDigitalInput inPin = gpio.provisionDigitalInputPin(RaspiPin.GPIO_00); // listening at pin 0
		final SoundListener sl = new SoundListener(c);
		
		PushbulletWebsocketClient pwc = pushbullet.createWebsocketClient();
		pwc.connect();
		pwc.registerListener(new PushbulletWebsocketListener() {
			
			@Override
			public void handle(Pushbullet pb, StreamMessage message) {
				
				StreamMessageType type = message.getType();
				switch(type){
				case TICKLE:
					TickleStreamMessage tsm = (TickleStreamMessage)message;
					if(tsm.getSubType().equals("push")){
						List<NotePush> newPushes = pb.getNewPushes(NotePush.class);
						for(NotePush np : newPushes){
							if(np.getDirection().equals(Direction.INCOMING)){
								String body = np.getBody().trim();
								if(body.equalsIgnoreCase("start")){
									log.info("START");
									//make sure we only have one listener aktive
									gpio.removeAllListeners();
									gpio.addListener(sl, inPin);
									np.dismiss();
								}else if(body.equalsIgnoreCase("stop")){
									log.info("STOP");
									gpio.removeAllListeners();
									np.dismiss();
								}
							}
						}
						break;
					}
				case PUSH:
				case NOP:
				default:
//					log.debug("Unintresting message, {}", message.toString());
					break;
				}
				
			}
		});
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
