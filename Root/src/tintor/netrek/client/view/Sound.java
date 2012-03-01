package tintor.netrek.client.view;

import javax.sound.sampled.Clip;

import tintor.netrek.client.model.Model;
import tintor.netrek.client.model.Torp;
import tintor.util.SoundUtils;

public enum Sound {
	ShieldUp, ShieldDown, FireTorp, FireTorpOther, TorpHit;

	public static void update() {
		if (shield != Model.myship.shield) {
			shield = Model.myship.shield;
			ShieldDown.stop();
			ShieldUp.stop();
			if (shield)
				ShieldUp.start();
			else
				ShieldDown.start();
		}

		for (final Torp torp : Model.torps)
			if (torp.state == Torp.State.Exploded) {
				TorpHit.restart();
				break;
			}

		for (final Torp torp : Model.torps)
			if (torp.state == Torp.State.Fired) {
				FireTorp.restart();
				break;
			}
	}

	private static boolean shield;

	private Sound() {
		final String a = toString();
		final StringBuilder b = new StringBuilder();
		for (int i = 0; i < a.length(); i++) {
			if (i > 0 && Character.isUpperCase(a.charAt(i))) b.append('_');
			b.append(a.charAt(i));
		}
		clip = SoundUtils.open("data/sounds/nt_" + b.toString() + ".wav");
	}

	private void start() {
		clip.setFramePosition(0);
		clip.start();
	}

	private void restart() {
		clip.stop();
		start();
	}

	private void stop() {
		clip.stop();
	}

	private Clip clip;
}