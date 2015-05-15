package pp2014.team32.shared.entities;

/**
 * Diese Klasse dient zur Speicherung der wichtigen Eigenschaften eines Players:
 * Name, Passwort und dessen GameCharacter.
 * 
 * @author Peter Kings
 * @author Moritz Bittner
 * 
 */
public class Player {
	protected String		userName;
	protected String		userPassword;
	protected GameCharacter	myCharacter;

	/**
	 * Konstruktor zur instanziierung eines Players mit Usernamen, Passwort und
	 * dessen GameCharacter.
	 * 
	 * @author Peter Kings
	 * @param userName
	 * @param userPassword
	 * @param testC1
	 */
	public Player(String userName, String userPassword, GameCharacter testC1) {
		this.userName = userName;
		this.userPassword = userPassword;
		this.myCharacter = testC1;
	}

	/**
	 * Gibt den Usernamen des Players zurueck.
	 * 
	 * @author Peter Kings
	 * @return Usernamen
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Gibt das Passwort des Users zurueck.
	 * 
	 * @author Peter Kings
	 * @return Passwort
	 */
	public String getUserPassword() {
		return userPassword;
	}

	/**
	 * Gibt den zugewiesenen GameCharacter zurueck.
	 * 
	 * @author Peter Kings
	 * @return GameCharacter
	 */
	public GameCharacter getMyCharacter() {
		return this.myCharacter;
	}
}
