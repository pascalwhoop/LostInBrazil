package pp2014.team32.shared.utils;

import java.io.Serializable;

/**
 * Helper-Klasse zur Speicherung einer x- und einer y-Koordinate in einem
 * Objekt.
 * 
 * Implementiert die Methoden <i>hashCode</i> und <i>equals</i>, damit zwei
 * Coordinates-Objekte mit unterschiedlichen Speicheradressen in einer Map (z.B.
 * HashMap) als gleicher Key verwendet werden koennen.
 * 
 * @author Christian Hovestadt
 * @version 29.6.14
 */
public class Coordinates implements Serializable {
	private static final long	serialVersionUID	= 6580596897354271998L;
	public int					x, y;

	/**
	 * Setzt die x- und y-Koordinate der Coordinaters
	 * 
	 * @param x x-Koordinate
	 * @param y y-Koordinate
	 * @author Christian Hovestadt
	 */
	public Coordinates(int x, int y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Vergleicht das Coordinates mit einem anderen Objekt. Handelt es sich
	 * ebenfalls um ein Coordinates-Objekt, werden die Koorinaten verglichen,
	 * sonst wird im jeden Fall false zurueckgegeben.
	 * 
	 * @param otherObject Vergleichsobjekt
	 * @return Vergleichsergebnis
	 * @author Christian Hovestadt
	 */
	public boolean equals(Object otherObject) {
		if (otherObject instanceof Coordinates) {
			Coordinates otherCoordinates = (Coordinates) otherObject;
			return (x == otherCoordinates.x && y == otherCoordinates.y);
		}
		return false;
	}

	/**
	 * Verwandelt die x- und y-Koordinate in eine Zahl ueber die
	 * hashCode-Methode von Strings. So koennen Coordinates-Objekte als Key fuer
	 * Maps (z.B. HashMaps) verwendet werden.
	 * 
	 * @return hashCode
	 * @author Christian Hovestadt
	 */
	public int hashCode() {
		return (x + " " + y).hashCode();
	}

	/**
	 * Subtrahiert die uebergebenen Koordinaten von diesen Coordinates.
	 * 
	 * @param coords Subtrahend
	 * @return Differenz der Coordinates als neues Coordinates-Objekt
	 * @author Moritz Bittner
	 */
	public Coordinates subtractCoordinates(Coordinates coords) {
		return new Coordinates((this.x - coords.x), (this.y - coords.y));
	}

	/**
	 * Konvertierung der Koordinaten als String zu Debug-Zwecken
	 * 
	 * @return Erstellter String
	 * @author Christian Hovestadt
	 */
	public String toString() {
		return " x: " + this.x + "y: " + this.y;
	}
}
