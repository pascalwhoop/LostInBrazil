package pp2014.team32.server.player;

import pp2014.team32.shared.entities.GameCharacter;
import pp2014.team32.shared.entities.Player;

/**
 * Diese Klasse erbt von der Shared Klasse Player. Hier werden UserName,
 * Passwort und der zugehoerige GameCharacter verwaltet.
 * 
 * @author Peter Kings
 * 
 */
public class ServerPlayer extends Player {
	// hat einen GameCharacter
	protected GameCharacter	myGameCharacter;

	/**
	 * Konstruktor, dem ein Username, Passwort und ein GameCharacter uebergeben
	 * wird
	 * 
	 * @author Peter Kings
	 * @param userName name des Users
	 * @param userPassword Passwort
	 * @param gC zugehoeriger GameCharacter
	 */
	public ServerPlayer(String userName, String userPassword, GameCharacter gC) {
		super(userName, userPassword, gC);
		this.myGameCharacter = gC;
	}

	/**
	 * Diese Methode ueberprueft, ob das uebergebene Passwort mit dem Passwort
	 * des ServerPlayer uebereinstimmt.
	 * 
	 * @auhtor Peter Kings
	 * @param password Passwort
	 * @return boolean true: Passwort korrekt, false: Passwort falsch
	 */
	public boolean checkPassword(String password) {
		if (password.equals(userPassword))
			return true;
		else
			return false;
	}

	/**
	 * Gibt den GameCharacter dieses ServerPlayers zurueck
	 * 
	 * @author Peter Kings
	 */
	public GameCharacter getMyCharacter() {
		return myGameCharacter;
	}
}
