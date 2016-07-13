package main;

import com.github.sheigutn.pushbullet.Pushbullet;
import com.github.sheigutn.pushbullet.stream.PushbulletWebsocketClient;
import com.github.sheigutn.pushbullet.stream.PushbulletWebsocketListener;
import com.github.sheigutn.pushbullet.stream.message.StreamMessage;

public class Main {

	public static void main(String[] args) {
		String apitoken = "";
		Pushbullet pb = new Pushbullet(apitoken);
		PushbulletWebsocketClient pwc = pb.createWebsocketClient();
		pwc.registerListener(new PushbulletWebsocketListener() {
			
			@Override
			public void handle(Pushbullet arg0, StreamMessage arg1) {
				
			}
		});
	}

}
