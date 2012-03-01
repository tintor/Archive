package tintor.netrek.client;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import tintor.netrek.client.model.Model;
import tintor.netrek.client.model.Ship;
import tintor.netrek.client.view.Window;
import tintor.netrek.common.Command;
import tintor.netrek.common.Command.Type;
import tintor.netrek.util.ump.Client;

public class Controller {
	public static void mousePressed(final int key, final int x, final int y) {
		switch (key) {
		case MouseEvent.BUTTON1:
			Command.type = Type.Phaser;
			break;
		case MouseEvent.BUTTON3:
			Command.type = Type.Turn;
			break;
		default:
			return;
		}
		Command.x = x;
		Command.y = y;
		Command.write(Client.sendBuffer);
		Client.send();
	}

	public static void keyPress(final int key) {
		switch (key) {
		case KeyEvent.VK_0:
		case KeyEvent.VK_1:
		case KeyEvent.VK_2:
		case KeyEvent.VK_3:
		case KeyEvent.VK_4:
		case KeyEvent.VK_5:
		case KeyEvent.VK_6:
		case KeyEvent.VK_7:
		case KeyEvent.VK_8:
		case KeyEvent.VK_9:
			Command.type = Type.values()[Type.Warp0.ordinal() + key - KeyEvent.VK_0];
			break;
		case KeyEvent.VK_A:
			Command.type = Type.MaxWarp;
			break;
		case KeyEvent.VK_E:
			Command.type = Model.pressor != null ? Type.TractorPressorOff : Type.PressorOn;
			break;
		case KeyEvent.VK_D:
			Command.type = Model.tractor != null ? Type.TractorPressorOff : Type.TractorOn;
			break;
		case KeyEvent.VK_C:
			Command.type = Model.myship.state == Ship.State.Cloaked ? Type.CloakOff : Type.CloakOn;
			break;
		case KeyEvent.VK_V:
			Command.type = Type.DetOwn;
			break;
		case KeyEvent.VK_SPACE:
			Command.type = Type.DetEnemy;
			break;
		case KeyEvent.VK_F:
			Command.type = Type.Photon;
			final Point p = Window.mouse();
			if (p == null) return;
			Command.x = p.x;
			Command.y = p.y;
			break;
		case KeyEvent.VK_W:
			Command.type = Model.myship.shield ? Type.ShieldDown : Type.ShieldUp;
			break;
		case KeyEvent.VK_ESCAPE:
			Command.type = Type.Quit;
			break;
		default:
			return;
		}
		Command.write(Client.sendBuffer);
		Client.send();
	}
}