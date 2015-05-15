package pp2014.team32.server.player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import pp2014.team32.server.ServerMain;
import pp2014.team32.server.Database.DatabaseConnection;
import pp2014.team32.server.LevelMaps.LevelMapsHandler;
import pp2014.team32.server.player.ServerPlayer;
import pp2014.team32.server.comm.ClientConnectionHandler;
import pp2014.team32.server.creatureManagement.RangeAndCollisionCalculator;
import pp2014.team32.shared.entities.Attributes;
import pp2014.team32.shared.entities.GameCharacter;
import pp2014.team32.shared.entities.Inventory;
import pp2014.team32.shared.entities.LevelMap;
import pp2014.team32.shared.entities.Player;
import pp2014.team32.shared.enums.CreatureStatusType;
import pp2014.team32.shared.messages.LevelData;
import pp2014.team32.shared.messages.RegistrationRequest;
import pp2014.team32.shared.utils.Coordinates;
import pp2014.team32.shared.utils.PropertyManager;

/**
 * 
 * @author Peter Kings
 * @author Moritz Bittner
 * 
 */
public class PlayerConnectionHandler {
	private final static Logger				LOGGER				= Logger.getLogger(ServerMain.class.getName());
	// ArrayList fuer registrierte Player
	private static ArrayList<ServerPlayer>	registeredPlayers	= new ArrayList<ServerPlayer>();
	// ArrayList fuer aktiv spielende Player
	private static ArrayList<ServerPlayer>	activePlayers		= new ArrayList<ServerPlayer>();

	/*
	 * ************************************************************
	 * registrierte Player aus der Datenbank laden
	 * ************************************************************
	 */
	/**
	 * Diese Methode wird direkt am Spielstart aufgerufen, um aktuelle
	 * Spielstaende zu laden.
	 * Es wird eine Verbindung zur Datenbank aufgebaut und die registrierten
	 * Player aus der Datenbank laden. Diese beinhalten jeweils einen
	 * GameCharacter, Attribute und ein Inventar. Danach werden alle aktuell
	 * registrierten User in registeredPlayers gespeichert.
	 * 
	 * @author Peter Kings
	 */
	public static void databaseConnectionAtGameStart() {
		/*
		 * Datenbank Schnittstellenklasse aufrufen und registrierte Player in
		 * static Variable speichern
		 */
		PlayerConnectionHandler.registeredPlayers = DatabaseConnection.getPlayerFromDatabase();
	}

	/*
	 * ************************************************************
	 * Einloggen/Ausloggen eines Players
	 * ************************************************************
	 */
	/**
	 * Methode zum Abmelden eines Clients: Entfernt GameCharacter aus LevelMap,
	 * und entfernt Player aus der Liste von activePlayers
	 * 
	 * @param userName
	 * @author Moritz Bittner
	 */
	public static void logoutUser(String userName) {
		// GameCharacter des users aus LevelMap entfernen
		GameCharacter gCtoRemove = getGameCharacterWithID(getIDForUserName(userName));
		LevelMap lM = LevelMapsHandler.getLevelMapForGameCharacter(gCtoRemove);
		LevelMapsHandler.removeDrawableObjectFromLevelMapAndInformClients(gCtoRemove, lM);
		// Player aus ActivePlayer entfernen
		activePlayers.remove(getPlayerWithUserNameFromActivePlayers(userName));
	}

	/**
	 * Hier werden einkommende User Anmeldungen verwaltet. Der User muss dafuer
	 * schon registriert sein. Die Registierung erfolgt in einer anderen
	 * Methode.
	 * 
	 * @param aPlayer ein Player mit Username und Passwort, der sich
	 *            registrieren will
	 * @param characterName der CharacterName des Characters eines Players
	 * @return true: User bereits registriert und Passwort korrekt.
	 *         false: User bereits registriert und Passwort falsch, oder User
	 *         nicht registriert.
	 */
	public static boolean newPlayerConnectionForAuthentication(Player aPlayer, String characterName) {
		LOGGER.info("neue Player Connection");
		for (ServerPlayer a : registeredPlayers) {
			/*
			 * Player bereits registriert und Passwort wurde korrekt eingegeben
			 */
			if (a.getUserName().equals(aPlayer.getUserName()) && a.checkPassword(aPlayer.getUserPassword())) {
				LOGGER.info("Player Anmeldung erfolgreich");
				/*
				 * Der Client erhaelt nun den boolean true zurueck und weiss,
				 * dass er authentifiziert wurde. Nun sollte er eine erneute
				 * Anfrage zum joinen des Games schicken. daraufhin erhaelt er
				 * erst die LevelMap.
				 */
				return true;
			}
			/*
			 * Player bereits registriert, aber Passwort ist falsch.
			 */
			else if (a.getUserName().equals(aPlayer.getUserName()) && !a.checkPassword(aPlayer.getUserPassword())) {
				LOGGER.info("Passwort vom Player falsch");
				/*
				 * Der Client erhaelt nun ein false, und weiss das etwas nicht
				 * stimmt. In diesem Falle war das Passwort falsch. Er fragt
				 * erneut nach Benutzernamen und Passwort.
				 */
				return false;
			}
		}
		/*
		 * in diesem Falle ist der Player noch nicht registriert.
		 */
		return false;
	}

	/**
	 * Diese Methode ist fuer die Registrierung eines Users zustaendig. Es wird
	 * geguckt, ob bereits ein User mit diesem Usernamen existiert. Ist dies der
	 * Fall, schlaegt die Registrierung fehl.
	 * 
	 * @author Peter Kings
	 * @param rR Message fuer eine Registrierung
	 * @return true: User erfolgreich registiert. false: Username bereits
	 *         vorhanden.
	 */
	public static boolean registerNewPlayer(RegistrationRequest rR) {
		LOGGER.info("Neuer Player will sich registrieren.");
		/*
		 * hier wird gecheckt, ob es bereits einen Player mit diesem Usernamen
		 * gibt.
		 */
		for (ServerPlayer a : registeredPlayers) {
			if (a.getUserName().equals(rR.USERNAME)) {
				// Player bereits registriert
				LOGGER.fine("Es gibt bereits einen Player mit diesem Namen.");
				/*
				 * in diesem Falle kann der User nicht registriert werden.
				 * Also false zurueckgeben.
				 */
				return false;
			}
		}
		/*
		 * Im folgenden wird ein neuer Player mit den Defaul Werten/Daten
		 * erstellt.
		 */
		/*
		 * Default Attribute werden aus der Property Datei geladen. Der
		 * gespeicherte String wird jeweils zu einem integer gecastet.
		 */
		Attributes startAttribute = new Attributes(Integer.parseInt(PropertyManager.getProperty("server.startAttr.health")), Integer.parseInt(PropertyManager
				.getProperty("server.startAttr.attackStrength")), Integer.parseInt(PropertyManager.getProperty("server.startAttr.defense")), Integer.parseInt(PropertyManager
				.getProperty("server.startAttr.movementSpeed")), Integer.parseInt(PropertyManager.getProperty("server.startAttr.attackSpeed")), Integer.parseInt(PropertyManager
				.getProperty("server.startAttr.healthRegeneration")), Integer.parseInt(PropertyManager.getProperty("server.startAttr.exPoints")));
		/*
		 * neues, leeres Inventar
		 */
		Inventory startInventar = new Inventory();
		// Character startet natuerlich auf der ersten LevelMap
		int startLevelID = LevelMapsHandler.getLevelMaps().get(0).getLevelID();
		/*
		 * x und y sind auf -1, damit man spaeter weiss, das dieser Character
		 * ein
		 * Default character ist. somit werden spaeter seine Spawn Coordinaten
		 * auf definierte Coordinaten gesetzt.
		 */
		GameCharacter defaultCG = new GameCharacter(DatabaseConnection.getNextID(), rR.USERNAME, rR.CHARACTER_TYPE, -1, -1, rR.USERNAME, startAttribute, 1, startLevelID, startInventar,
				CreatureStatusType.STANDING);
		// neuer ServerPlayer mit Character uebergabe
		ServerPlayer neuerPlayer = new ServerPlayer(rR.USERNAME, rR.PASSWORD, defaultCG);
		// Player den registrierten Playern adden
		registeredPlayers.add(neuerPlayer);

		LOGGER.info("neuer Player wurde erfolgreich registriert");
		/*
		 * Der Client erhaelt nun die nachricht, das er erfolgreich registriert
		 * wurde. Nun schickt er erneut eine Anfrage, dem spiel beizutreten.
		 * Daraufhin erhaelt er erst die LevelMap.
		 */
		return true;
	}

	/**
	 * Player will dem Spiel betreten. Er wird der LevelMap hinzugefuegt und
	 * alle Clients auf dieser LevelMap werden benachrichtigt. Daraufhin erhaelt
	 * der Player die LevelMap.
	 * 
	 * @param userName des Players
	 * @author Peter Kings
	 */
	public static void playerJoinedGame(String userName) {
		// registierten Player holen
		ServerPlayer joinedPlayer = null;
		try {
			joinedPlayer = PlayerConnectionHandler.getPlayerWithUserNameFromRegisteredPlayers(userName);
		} catch (IllegalArgumentException e) {
			LOGGER.warning(e.getMessage());
			return;
		}
		// LevelMap des Characters suchen
		LevelMap lP = LevelMapsHandler.getLevelMapForGameCharacter(joinedPlayer.getMyCharacter());
		// Character auf die Startposition setzen
		/*
		 * x == -1 bedeutet, das es noch ein neu registrierter Default Player
		 * ist.
		 */
		if (joinedPlayer.getMyCharacter().getX() == -1)
			// in diesem Falle auf die Startkoodinaten des Levels setzen
			RangeAndCollisionCalculator.setCoordinatesForDrawableObjectPlacing(new Coordinates(lP.start.x, lP.start.y), joinedPlayer.getMyCharacter(), lP);
		// Character der LevelMap hinzufuegen und Clients informieren
		LevelMapsHandler.addDrawableObjectToLevelMapAndInformClients(joinedPlayer.getMyCharacter(), LevelMapsHandler.getLevelMapForGameCharacter(joinedPlayer.getMyCharacter()));
		// danach neuen Player zu aktiven Playern adden
		activePlayers.add(joinedPlayer);
		LOGGER.info(userName + " ist dem Spiel beigetreten.");
		// neue Level Message instanziieren
		LevelData levelData = new LevelData(LevelMapsHandler.getLevelMapForGameCharacter(joinedPlayer.getMyCharacter()));
		// LevelMap an Client senden
		ClientConnectionHandler.sendMessageToUser(userName, levelData);
		LOGGER.info("LevelMap wurde an " + userName + " gesendet.");
	}

	/*
	 * ************************************************************
	 * Getter Methoden rund um den Player und Character
	 * ************************************************************
	 */
	/**
	 * Diese Methode gibt ein Set mit allen derzeit aktiven Usern des Spiels
	 * zurueck.
	 * 
	 * @author Peter Kings
	 * @return Set mit aktiven Usern
	 */
	public static Set<String> getSetOfActiveUserNames() {
		// neues Set fuer aktive User
		Set<String> set = new HashSet<String>();
		/*
		 * alle aktiven Player werden dem Set hinzugefuegt
		 */
		for (ServerPlayer p : PlayerConnectionHandler.activePlayers) {
			set.add(p.getUserName());
		}
		return set;
	}

	/**
	 * Diese Methode sucht in den momentan aktiven Playern nach dem Player mit
	 * dem uebergebenen Usernamen.
	 * Wird ein Match gefunden, wird dieser zurueckgegeben.
	 * 
	 * @author Peter Kings
	 * @param userName Username einesm aktiven Players
	 * @return Zugehoerige Player
	 * @throws IllegalArgumentException wenn diese Exception geworfen wird,
	 *             wurde kein Player mit diesem
	 *             Usernamen gefunden.
	 */
	public static ServerPlayer getPlayerWithUserNameFromActivePlayers(String userName) throws IllegalArgumentException {
		ServerPlayer matchingPlayer = null;
		// suche in aktiven Playern nach dem Player mit uebergebenem UserName
		for (ServerPlayer p : activePlayers) {
			// falls Player vorhanden
			if (p.getUserName().equals(userName))
				matchingPlayer = p;
		}
		// es wurde kein matching Player gefunden
		if (matchingPlayer == null)
			// kein Player gefunden...
			throw new IllegalArgumentException("Es konnte kein aktiver Player mit diesem Usernamen gefunden werden.");
		return matchingPlayer;
	}

	/**
	 * Diese Methode sucht in den momentan registrierten Playern nach einem
	 * Player mit dem uebergebeben Usernamen.
	 * Wird ein Match gefunden, wird dieser zurueckgegeben.
	 * 
	 * @author Peter Kings
	 * @param userName Username eines registrierten Players
	 * @return Zugehoerige Player
	 * @throws IllegalArgumentException wenn diese Exception geworfen wird,
	 *             wurde kein Player mit diesem
	 *             Usernamen gefunden.
	 */
	public static ServerPlayer getPlayerWithUserNameFromRegisteredPlayers(String userName) throws IllegalArgumentException {
		ServerPlayer matchingPlayer = null;
		// suche in registrierten Playern nach dem Player mit uebergebenem
		// UserName
		for (ServerPlayer p : registeredPlayers) {
			// falls Player vorhanden
			if (p.getUserName().equals(userName))
				matchingPlayer = p;
		}
		// es wurde kein matching Player gefunden
		if (matchingPlayer == null)
			// kein Player gefunden...
			throw new IllegalArgumentException("Es konnte kein registrierter Player mit diesem Usernamen gefunden werden.");
		return matchingPlayer;
	}

	/**
	 * Diese Methode sucht den GameCharacter mit der zugehoerigen ObjectID
	 * 
	 * @author Peter Kings
	 * @param ObjectID die einmalige ObjectID jedes GameCharacters
	 * @return GameCharacter mit der zugehoerigen ObjectID
	 */
	public static GameCharacter getGameCharacterWithID(int ObjectID) {
		// Character fuer ObjectID Suchen
		GameCharacter gc = null;
		// suche in den aktiven Playern anch der ObjectID
		for (ServerPlayer p : activePlayers) {
			// falls match vorhanden
			if (p.getMyCharacter().getID() == ObjectID) {
				gc = p.getMyCharacter();
			}
		}
		return gc;
	}

	/**
	 * Diese Methode sucht in den registrierten Playern nach dem uebergebenen
	 * Usernamen und gibt die GameCharacterID zurueck.
	 * 
	 * @param userName
	 * @author Peter Kings
	 * @return gibt GameCharacterID zurueck. ist -1, falls dieser Player noch
	 *         keinen GameCharacter hat.
	 */
	public static int getIDForUserName(String userName) {
		// suche in den registrierten Playern
		for (ServerPlayer p : registeredPlayers) {
			// nach dem match der Usernamen
			if (p.getUserName().equals(userName))
				// und gib die ID zurueck
				return p.getMyCharacter().ID;
		}
		// in diesem Fall hat der username noch keinen Character
		return -1;
	}

	/**
	 * Getter Methode fuer die RegisteredPlayers ArrayList
	 * 
	 * @return registrierte Player in einer ArrayList
	 * @author Peter Kings
	 */
	public static ArrayList<ServerPlayer> getRegisteredPlayers() {
		return registeredPlayers;
	}

	/**
	 * Getter Methode fuer die ActivePlayers ArrayList
	 * 
	 * @return registrierte Player in einer ArrayList
	 * @author Peter Kings
	 */
	public static ArrayList<ServerPlayer> getActivePlayers() {
		return activePlayers;
	}
}
