package pp2014.team32.server.EnemyHandling;

import java.util.Comparator;

import pp2014.team32.shared.entities.Enemy;

/**
 * Dieser Komparator dient dem Vergleich von Enemies nach ihrem
 * Einschlafzeitpunkt. Es wird das Enemy bevorzugt, dessen Einschlafzeitpunkt
 * weiter in der Vergangenheit liegt.
 * 
 * @author Peter Kings
 */
public class EnemyWakeUpComparator implements Comparator<Enemy> {

	/**
	 * Diese Methode Vergleicht zwei Enemies nach ihrem Einschlafzeitpunkt. Das
	 * Enemie, welches frueher eingeschlafgen ist, wird priorisiert.
	 * 
	 * @author Peter Kings
	 */
	public int compare(Enemy o1, Enemy o2) {
		// wenn erster vor dem zweiten eingeschlafen ist
		if (o1.getSleepTimeStamp().before(o2.getSleepTimeStamp())) {
			// bevorzuge diesen
			return -1;
		}
		// wenn zweiter vor dem ersten eingeschlafen ist
		else if (o1.getSleepTimeStamp().after(o2.getSleepTimeStamp())) {
			// bevorzuge diesen
			return 1;
		}
		// sonst sind sie gleichwertig
		return 0;
	}
}
