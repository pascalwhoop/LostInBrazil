package pp2014.team32.shared.utils;

/**
 * Sammlung von Helper-Methoden fuer mathematischen Berechnungen
 * 
 * @author Brokmeier, Pascal
 * @version 06.06.14
 */
public class MathHelper {

	/**
	 * Gibt eine Zufallszahl zwischen <i>min</i> und <i>max</i> aus. (<i>min</i> und <i>max</i> sind beide auch moeglich)
	 * @param min Mindestwert
	 * @param max Maximalwert
	 * @return Zufallswert
	 * @author Brokmeier, Pascal
	 */
    public static int randomWithRange(int min, int max) {
        int range = (max - min) + 1;
        return (int) (Math.random() * range) + min;
    }
}
