package pp2014.team32.shared.exceptions;

public class KeyNotFoundException extends Exception {

	/**
	 * Exception fuer nicht gueltige Schluesselzugriffe
	 * @author Peter Kings
	 * @author Moritz Bittner
	 */
	private static final long	serialVersionUID	= 8243131214194198013L;

	public KeyNotFoundException() {
		this("Schluessel fuer Zugriff ist nicht gueltig");
	}
	
	public KeyNotFoundException(String message) {
		super("Fehlerhafter Schluessel: " + message);
	}
}
