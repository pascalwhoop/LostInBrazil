package pp2014.team32.server.AStern;

/**
 * Diese Exception wird geworfen, falls kein kuerzester Weg gefunden werden
 * kann.
 * 
 * @author Peter Kings
 * 
 */
public class NoShortestPathFoundException extends Exception {

	private static final long	serialVersionUID	= -7105232983398903839L;
	/**
	 * Konstruktor mit Default Message: Es wurde kein kuerzester Weg gefunden!
	 * @author Peter Kings
	 */
	public NoShortestPathFoundException() {
		super("Es wurde kein kuerzester Weg gefunden!");
	}
	/**
	 * Konstruktor, dem eine Error Message uebergeben werden kann.
	 * @auhtor Peter Kings
	 * @param message ErrorMessage
	 */
	public NoShortestPathFoundException(String message) {
		super(message);
	}
}