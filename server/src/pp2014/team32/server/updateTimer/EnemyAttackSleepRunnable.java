package pp2014.team32.server.updateTimer;

import java.util.Timer;
import java.util.TimerTask;

import pp2014.team32.shared.entities.Creature;
import pp2014.team32.shared.enums.AttributeType;
import pp2014.team32.shared.utils.PropertyManager;

/**
 * Runnable, welches nach Angriffsschlag eines Enemys ausgefuehrt wird, um
 * timeout bis zum naechsten angriffsschlag runterzuzaehlen.
 * Sorgt dafuer, dass entsprechender Boolean, welcher fuer Angriff true sein
 * muss zunaechst auf false gesetzt wird und spaeter mithilfe eines Timers nach 400 bis 600
 * Millisekunden wieder auf true gesetzt wird.
 * 
 * @author Moritz Bittner
 * 
 */
public class EnemyAttackSleepRunnable implements Runnable {
	// maximale Zeit zwischen zwei Angriffsschlaegen
	private static int	maxAttackSleepTime;
	// creature, die den Angriffschlag durchfuehrt
	private Creature	creature;

	static {
		maxAttackSleepTime = Integer.parseInt(PropertyManager.getProperty("maxAttackSleepTime"));
	}

	/**
	 * neue Angriffsschlagspausierung fuer die uebergebene Creature
	 * @param creature die einen Angriffsschlag durchgefuehrt hat
	 * @author Moritz Bittner
	 */
	public EnemyAttackSleepRunnable(Creature creature) {
		this.creature = creature;
	}

	@Override
	public void run() {
		creature.attackEnabled = false;
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				// nach 400 - 600 Millisekunden neuer Angriffsschlag moeglich
				creature.attackEnabled = true;
			}
			// timeout fuer Angriffsschlag zwischen 400 und 600 millisekunden in
			// abhaengigkeit der Angriffsgeschwindigkeit
		}, maxAttackSleepTime - 2 * creature.attributes.get(AttributeType.ATTACK_SPEED));
	}

}
