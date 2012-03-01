package tintor.netrek.client;

import tintor.netrek.Const;
import tintor.netrek.client.model.Model;
import tintor.netrek.client.view.Sound;
import tintor.netrek.client.view.Window;
import tintor.netrek.util.ump.Client;

public class Daemon {
	public static void main(final String[] args) {
		Client.connect(args.length == 1 ? args[0] : "localhost", Const.Port);
		Window.start();

		while (true) {
			Client.receive();
			Model.read(Client.recvBuffer);
			Window.update();
			Sound.update();
		}
	}
}