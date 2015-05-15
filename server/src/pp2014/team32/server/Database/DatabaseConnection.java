package pp2014.team32.server.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TimerTask;
import java.util.logging.Logger;

import pp2014.team32.server.ServerMain;
import pp2014.team32.server.LevelMaps.LevelMapsHandler;
import pp2014.team32.server.creatureManagement.InventoryHandler;
import pp2014.team32.server.player.PlayerConnectionHandler;
import pp2014.team32.server.player.ServerPlayer;
import pp2014.team32.shared.entities.Attributes;
import pp2014.team32.shared.entities.GameCharacter;
import pp2014.team32.shared.entities.Inventory;
import pp2014.team32.shared.entities.Item;
import pp2014.team32.shared.entities.LevelMap;
import pp2014.team32.shared.enums.AttributeType;
import pp2014.team32.shared.enums.CharacterType;
import pp2014.team32.shared.enums.CreatureStatusType;
import pp2014.team32.shared.enums.ItemType;
import pp2014.team32.shared.utils.Coordinates;
import pp2014.team32.shared.utils.PropertyManager;

/**
 * Diese statische Klasse dient der kompletten Datenbank Anbindung des Spiels.
 * Sie
 * bieten jeweils Methoden zum Speichern und Laden von relevanten Objekten, um
 * den Spielfortschritt festzuhalten.
 * Dazu gehoeren:
 * -alle registierten Player im Spiel(inklusive deren GameCharacter, Inventar,
 * Attribute),
 * -Fog of War aller LevelMaps oder von einzelnen,
 * -Status (offen ode geschlossen) der Verbindungen zwischen LevelMaps (Taxies)
 * -der Verwaltung einer ID fuer alle Objekte im gesamten Projekt.
 * 
 * Sie ist als TimerTask implementiert, um die Datenbank in periodischen
 * Intervallen zu aktuallisieren.
 * 
 * @author Peter Kings
 */
public final class DatabaseConnection extends TimerTask {
	/*
	 * Konfigurationselemente um die URL zur Datenbankanbindung schematisch
	 * aufzubauen.
	 */
	final static String			hostname	= "localhost";
	final static String			port		= "3306";
	final static String			dbname		= "team32";

	/* das ist die Server Konfiguration */
	final static String			user		= PropertyManager.getProperty("teamUsername");
	final static String			password	= PropertyManager.getProperty("teamPassword");

	/* das ist die locale Konfiguration */
	// final static String user = "root";
	// final static String password = "";

	// Datenbank Connection Object wird mit null instanziiert, um spaeter
	// abzufragen, ob bereits eine Verbindung besteht
	static Connection			c			= null;
	/*
	 * wird anfangs auf -1 gesetzt, da bei Rueckgabe einer ID immer zuerst
	 * erhoeht, und danach zurueckgegeben wird
	 */
	private static int			latestID	= -1;
	// Logger Einbindung
	private final static Logger	LOGGER		= Logger.getLogger(ServerMain.class.getName());

	/**
	 * Konstruktor der Database Connection. Dieser fuehrt jedoch nichts aus und
	 * findet auch keine Verwendung, da es sich um eine statische Klasse
	 * handelt.
	 * 
	 * @author Peter Kings
	 */
	public DatabaseConnection() {
	}

	/*
	 * ***************************
	 * Datenbank Connection Methoden
	 * ***************************
	 */
	/**
	 * Eine Connection zur Database aufbauen
	 * 
	 * @author Peter Kings
	 */
	public static void connectToDatabase() {
		/*
		 * Treiber zur Verbindung mit der Datenbank werden versucht zu laden
		 */
		try {
			LOGGER.fine("* Treiber laden");
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			LOGGER.fine("* Treiber wurde geladen");
		} catch (Exception e) {
			LOGGER.fine("Treiber fuer die Datenbank konnte nicht geladen werden.");
		}
		/*
		 * Verbindung mit der Datenbank herstellen
		 */
		try {
			LOGGER.fine("Verbindung aufbauen...");
			/* das ist die Server Konfiguration */
			String url = "jdbc:mysql://localhost/";
			url += dbname;

			/* das ist die Locale Konfiguration */
			// String url = "jdbc:mysql://" + hostname + ":" + port + "/" +
			// dbname;
			/*
			 * Connection aufbauen mit der URL zur Datenbank, dem Usernamen und
			 * Passwort.
			 */
			c = DriverManager.getConnection(url, user, password);
			LOGGER.fine("Connection to database was successful.");
		} catch (Exception sqle) {
			/*
			 * Hier wird eine aufgetretene SQL Exception abgefangen.
			 */
			LOGGER.fine("Verbinndung zur Datenbank konnte nicht hergestellt werden.");
		}
	}

	/**
	 * Diese Methode schliesst die Verbindung mit der Datenbank. Auf Rueckfrage
	 * mit Philip Klinke wird diese jedoch nicht verwendet. Man kann die
	 * Verbindung mit der Datenbank ueber die gesamte Laufzeit des Servers
	 * bestehen lassen. Dies wuerde nur Sinn machen, wenn mehrere Verbindungen
	 * aufgebaut werden. Da aber einzig und allein der Server eine Verbindung
	 * zur Datenbank herstellt, ist dies nicht noetig.
	 * Zur Vollstaendigkeit ist diese Methode aber trotzdem implementiert.
	 * 
	 * @author Peter Kings
	 */
	public static void closeDatabaseConnection() {
		try {
			LOGGER.fine("Datenbank Connection wurde erfolgreich geschlossen.");
			// wenn eine Verbindung besteht, also c nicht null ist
			if (c != null)
				// schliesse diese Verbindung
				c.close();
		} catch (SQLException e) {
			/*
			 * Hier wird eine aufgetretene SQL Exception abgefangen.
			 */
			LOGGER.fine("Datenbank Connection konnte nicht geschlossen werden.");
		}
	}

	/**
	 * Verwaltung einer einmaligen ID fuer alle Objekte. Hierzu wird die letzte
	 * ID in der Datenbank gespeichert. Diese wird auch fortlaufend
	 * aktuallisiert, damit im schlimmsten Falle(Server Absturz), keine IDs
	 * doppelt vergeben werden.
	 * Alle IDs die im gesamten Spiel vergeben werden, werden ueber diese
	 * Methode angefordert. Dadurch wird eine doppelte ID Vergabe
	 * ausgeschlossen.
	 * 
	 * @author Peter Kings
	 * @return naechste zu vergeben ID
	 */
	public static int getNextID() {
		// zuerst die letzte ID um eins erhoehen
		latestID++;
		// danach diese zurueckgeben
		return latestID;
	}

	/*
	 * ***************************
	 * Player Data Methoden
	 * ***************************
	 */
	/**
	 * Datenbank wird nach aktuell registrierten Playern abgefragt. Jeder Player
	 * enthaelt einen GameCharacter und dieser wiederum Attribute und ein
	 * Inventar. Bei vorhandenen Daten werden diese in einer ArrayList mit
	 * Playern (registrierten Playern) hinzugefuegt. Sind keine Player
	 * registriert, wird eine leere ArrayList zurueckgegeben.
	 * 
	 * @author Peter Kings
	 * @return ArrayList mit registrierten Playern. Leer, falls keine Player
	 *         registriert sind.
	 */
	public static ArrayList<ServerPlayer> getPlayerFromDatabase() {
		// Verbindung zur Datenbank herstellen (falls noch nicht vorhanden)
		if (c == null)
			connectToDatabase();
		/*
		 * neue ArrayList zur verwaltung aller registrierten Player
		 * instanziieren.
		 * Diese wird im Falle vorhandener Daten in der Datenbank befuellt.
		 */
		ArrayList<ServerPlayer> registeredPlayers = new ArrayList<ServerPlayer>();
		// versuche, die Player Daten aus der Datenbank zu laden.
		try {
			// neues Query Statement instanziieren
			Statement stmt = c.createStatement();
			/*
			 * ein Query Abfrage zusammenstellen und direkt ausfuehren, welche
			 * alle Spalten (Player, Character,
			 * Attribute und das Inventar) eines Spielers miteinander joined.
			 */
			ResultSet rs = stmt
					.executeQuery("SELECT * FROM `Player` p, `Character` c, Attributes a, Inventory i WHERE p.userName = c.userName AND c.characterName = a.characterName AND c.characterName = i.characterName");
			/*
			 * solange es noch weitere Resultate gibt, fuehre den Block der
			 * while Schleife aus...
			 */
			while (rs.next()) {
				/*
				 * Die Attribute eines Characters werden aus der Abfrage in
				 * einem Attribute Object neu erzeugt
				 */
				Attributes a = new Attributes(Integer.parseInt(rs.getString("health")), Integer.parseInt(rs.getString("attackStrength")), Integer.parseInt(rs.getString("defense")),
						Integer.parseInt(rs.getString("movementSpeed")), Integer.parseInt(rs.getString("attackSpeed")), Integer.parseInt(rs.getString("healthRegeneration")), Integer.parseInt(rs
								.getString("exPoints")));
				/*
				 * hier wird zuerst ein neues Inventar erstellt, da der
				 * Konstruktor eines GameCharacters dessen Inventar fordert. Um
				 * Manipulationen auszuschliessen haben wir uns allerdings
				 * entschieden, dass ein Inventar nur ueber Methoden des
				 * jeweiligen GameCharacters veraendert werden kann. Somit wird
				 * zunaechst ein leeres Inventar uebergeben, und anschliessend
				 * erste befuellt, wenn der GameCharacter bereits instanziiert
				 * ist.
				 */
				Inventory inventory = new Inventory();
				/*
				 * GameCharacter aus der Abfrage erzeugen, inklusive der
				 * Attribute und einem leeren Inventar.
				 */
				GameCharacter aChar = new GameCharacter(Integer.parseInt(rs.getString("ID")), rs.getString("userName"), CharacterType.valueOf(rs.getString("characterType")), Integer.parseInt(rs
						.getString("xPos")), Integer.parseInt(rs.getString("yPos")), rs.getString("characterName"), a, Integer.parseInt(rs.getString("level")), Integer.parseInt(rs
						.getString("levelMapID")), inventory, CreatureStatusType.STANDING);
				/*
				 * jetzt kann ein neuer Player mit dem GameCharacter
				 * instanziiert werden
				 */
				ServerPlayer aPlayer = new ServerPlayer(rs.getString("userName"), rs.getString("userPassword"), aChar);
				/*
				 * fuer alle Inventarplaetze durchgehen, ob ein Item, oder empty
				 * ind er Datenbank gespeichert wurde.
				 */
				for (int j = 0; j < aPlayer.getMyCharacter().getInventory().getInventorySize(); j++) {
					/*
					 * jeweils den Spaltennamen zusammenbauen, wie sie in der
					 * Datenbank angelegt sind.
					 * Diese beginnen nicht bei 0, sondern bei 1.
					 */
					String inventoryPlace = rs.getString("item" + (j + 1));
					/*
					 * wenn in der Datenbank fuer diesen Platz kein empty
					 * gespeichert ist, wird das Item neu erzeugt und dem
					 * Inventar hinzugefuegt.
					 */
					if (!inventoryPlace.equals("empty")) {
						/*
						 * Neues Item Object mit einzigartiger ID erzeugen. x
						 * und y Koordinaten sind egal, werden erst wieder beim
						 * Drop neu festgesetzt. Der String aus der Datenbank
						 * wird wieder zu einem ItemType Enum gecastet.
						 */
						Item item = new Item(getNextID(), 0, 0, ItemType.valueOf(inventoryPlace));
						/*
						 * an dieser Stelle wird nun das Inventar des
						 * GameCharacters befuellt.
						 */
						InventoryHandler.addItemToInventory(aChar, item);
					}
				}
				/*
				 * vollstaenig aus der Datenbank geladenen Player inklusive
				 * GameCharacter, Inventar und Attributen den registrierten
				 * Playern hinzufuegen.
				 */
				registeredPlayers.add(aPlayer);
			}
			// resultset schliessen
			rs.close();
			// Statement Object schliessen
			stmt.close();
			LOGGER.fine("Daten wurden erfolgreich aus der Datenbank geladen.");
		} catch (SQLException sqle) {
			/*
			 * Hier wird eine aufgetretene SQL Exception seperat abgefangen, um
			 * Fehlerquellen schneller zu finden.
			 */
			LOGGER.fine("Beim Laden der Player Daten aus der Datenbank ist eine sql Exception aufgetreten: " + sqle.getMessage());
		} catch (Exception e) {
			LOGGER.fine("registrierte Player konnten nicht aus der Datenbank geladen werden.");
		}
		/*
		 * Rueckgabe der ArrayList mit Daten aus der Datenbank, falls diese
		 * vorhanden sind. Sonst eine leere ArrayList.
		 */
		return registeredPlayers;
	}

	/**
	 * Diese Methode laed die zuletzt verwendete ObjectID aus der Datenbank in
	 * die statische Variable latestID der Klasse DatabaseConnection. Dies muss
	 * direkt bei Server Start durchgefuehrt werden.
	 * 
	 * @author Peter Kings
	 */
	public static void loadLatestObjectIDFromDatabase() {
		// Verbindung zur Datenbank herstellen (falls noch nicht vorhanden)
		if (c == null)
			connectToDatabase();
		try {
			// neues Query Statement instanziieren
			Statement stmt = c.createStatement();

			/*
			 * SQL Query fuer die Id ausfuehren
			 */
			ResultSet rs = stmt.executeQuery("SELECT * FROM `Settings`");
			while (rs.next()) {
				/*
				 * in der Datenbank zurueckgelegte ObjectID (als String), wieder
				 * zu einem int casten und in latestID speichern.
				 */
				latestID = Integer.parseInt(rs.getString("ObjectID"));
			}
			// resultset schliessen
			rs.close();
			// Statement Object schliessen
			stmt.close();
		} catch (SQLException sqle) {
			/*
			 * Hier wird eine aufgetretene SQL Exception seperat abgefangen, um
			 * Fehlerquellen schneller zu finden.
			 */
			LOGGER.fine("Beim Laden der letzten ID aus der Datenbank ist eine sql Exception aufgetreten: " + sqle.getMessage());
		} catch (Exception e) {
			LOGGER.fine("Letzte ObjectID konnten nicht aus der Datenbank geladen werden.");
		}
	}

	/**
	 * Diese Methode speichert alle relevanten Daten aller registrierten Player
	 * in der Datenbank. Ein Player wird in seine Einzelteile (GameCharacter,
	 * Attribtutes, Inventory) zerlegt. Die betroffenen Tabellen werden komplett
	 * geleert und neu mit den aktuellen Daten befuellt.
	 * 
	 * @author Peter Kings
	 * @param arrayList alle registrierten Player mit ihren Charakteren
	 */
	public static void updateRegisteredPlayers(ArrayList<ServerPlayer> arrayList) {
		// Verbindung zur Datenbank herstellen, falls noch nicht vorhanden
		if (c == null)
			connectToDatabase();
		/*
		 * alle Tabellen der Datenbank vollstaendig leeren
		 */
		try {
			Statement stmt = c.createStatement();
			// Player
			String insertSQLStatement = "truncate Player;";
			stmt.executeUpdate(insertSQLStatement);
			// Attributes
			insertSQLStatement = "truncate Attributes;";
			stmt.executeUpdate(insertSQLStatement);
			// Characters
			insertSQLStatement = "truncate `Character`;";
			stmt.executeUpdate(insertSQLStatement);
			// Inventory
			insertSQLStatement = "truncate Inventory;";
			stmt.executeUpdate(insertSQLStatement);
			// Statement schliessen
			stmt.close();
			LOGGER.fine("Alles um den Player wurde erfolgreich zurueckgesetzt.");
		} catch (SQLException sqle) {
			/*
			 * Hier wird eine aufgetretene SQL Exception seperat abgefangen, um
			 * Fehlerquellen schneller zu finden.
			 */
			LOGGER.fine("Beim leeren der Player in der Datenbank ist eine SQLException aufgetreten: " + sqle.getMessage());
		} catch (Exception e) {
			LOGGER.fine("Player, Attributs, Character oder Inventory konnte nicht konnte nicht geleert werden.");
		}
		/*
		 * vollstaendige Player Speicherung. Dazu wird jeder Player in der
		 * uebergebenen ArrayList komplett zerlegt in Player, GameCharacter,
		 * Attributes und Inventory und anschliessend in die Datenbank
		 * geschrieben
		 */
		for (ServerPlayer p : arrayList) {
			/*
			 * zerlegung des Players in Elemente
			 */
			GameCharacter mychar = p.getMyCharacter();
			Attributes myAtt = mychar.getAttributes();
			Inventory myInv = mychar.inventory;
			try {
				Statement stmt = c.createStatement();
				/*
				 * Player speichern
				 */
				String insertSQLStatement = "INSERT INTO `Player`(`userName`, `userPassword`) VALUES ('" + p.getUserName() + "','" + p.getUserPassword() + "');";
				LOGGER.fine(insertSQLStatement);
				stmt.executeUpdate(insertSQLStatement);
				/*
				 * Charakter speichern
				 */
				insertSQLStatement = "INSERT INTO `Character`(`userName`, `characterName`, `ID`, `level`, `levelMapID`, `xPos`, `yPos`, `characterType`) VALUES ('" + p.getUserName() + "','"
						+ mychar.getName() + "','" + mychar.getID() + "','" + mychar.currentCharacterLevel + "','" + mychar.currentLevelMapID + "','" + mychar.getX() + "','" + mychar.getY() + "','"
						+ mychar.characterType.toString() + "');";
				LOGGER.fine(insertSQLStatement);
				stmt.executeUpdate(insertSQLStatement);
				/*
				 * Attribute speichern
				 */
				insertSQLStatement = "INSERT INTO `Attributes`(`characterName`, `health`, `healthRegeneration`, `defense`, `attackStrength`, `movementSpeed`, `attackSpeed`, `exPoints`) VALUES ('"
						+ mychar.getName() + "','" + myAtt.get(AttributeType.HEALTH) + "','" + myAtt.get(AttributeType.HEALTH_REGENERATION) + "','" + myAtt.get(AttributeType.DEFENSE) + "','"
						+ myAtt.get(AttributeType.ATTACK_STRENGTH) + "','" + myAtt.get(AttributeType.MOVEMENT_SPEED) + "','" + myAtt.get(AttributeType.ATTACK_SPEED) + "','"
						+ myAtt.get(AttributeType.EXPOINTS) + "');";
				LOGGER.fine(insertSQLStatement);
				stmt.executeUpdate(insertSQLStatement);
				/*
				 * Inventar speichern
				 */
				insertSQLStatement = "INSERT INTO `Inventory`(`characterName`, `item1`, `item2`, `item3`, `item4`, `item5`, `item6`, `item7`, `item8`, `item9`, `item10`, `item11`, `item12`, `item13`, `item14`) VALUES ('"
						+ mychar.getName() + "'";
				/*
				 * jeden Inventory Platz durchgehen, und je nach dem, ob dieser
				 * ein Item enthaelt, dieses zu einem String casten, oder einen
				 * String fuer empty in den SQL Befehl einbauen
				 */
				for (int i = 0; i < myInv.getInventorySize(); i++) {
					Item aItem = myInv.getItemAtIndex(i);
					// falls kein Item an diesem Platz
					if (aItem == null)
						// empty String anhaengen
						insertSQLStatement += ", 'empty'";
					else
						// sonst Item Enum zu einem String casten und anhaengen
						insertSQLStatement += ", '" + aItem.getItemType().toString() + "'";
				}
				// Abschliessen des SQL Befehls
				insertSQLStatement += ");";
				LOGGER.fine(insertSQLStatement);
				// Ausfuehrung
				stmt.executeUpdate(insertSQLStatement);
				// Statement schliessen
				stmt.close();
				LOGGER.fine("Daten wurden erfolgreich in die Datenbank geschrieben.");
			} catch (SQLException sqle) {
				/*
				 * Hier wird eine aufgetretene SQL Exception seperat abgefangen,
				 * um
				 * Fehlerquellen schneller zu finden.
				 */
				LOGGER.fine("Beim Schreiben der Player Daten in die Datenbank ist eine sql Exception aufgetreten: " + sqle.getMessage());
			} catch (Exception e) {
				LOGGER.fine("Daten in die Datenbank schreiben ist fehlgeschlagen.");
			}
		}

	}

	/**
	 * 
	 * @author Peter Kings
	 */
	public static void updateLatestObjectID() {
		// Verbindung zur Datenbank herstellen, falls noch nicht vorhanden
		if (c == null)
			connectToDatabase();
		/*
		 * Settings Tabelle leeren, damit sie aktuellisiert werden kann.
		 */
		try {
			Statement stmt = c.createStatement();
			// SQL Befehl erstellen
			String insertSQLStatement = "truncate Settings;";
			LOGGER.fine(insertSQLStatement);
			// diesen ausfuehren
			stmt.executeUpdate(insertSQLStatement);
			stmt.close();
			LOGGER.fine("Datenbank wurde erfolgreich zurueckgesetzt.");
		} catch (SQLException sqle) {
			/*
			 * Hier wird eine aufgetretene SQL Exception seperat abgefangen, um
			 * Fehlerquellen schneller zu finden.
			 */
			LOGGER.fine("Beim leeren der Datenbank ist eine SQLException aufgetreten: " + sqle.getMessage());
		} catch (Exception e) {
			LOGGER.fine("Datenbank konnte nicht geleert werden.");
		}
		/*
		 * letzte ObjectID speichern
		 */
		try {
			Statement stmt = c.createStatement();
			// SQL Befehl erstellen
			String insertSQLStatement = "INSERT INTO `Settings`(`ObjectID`) VALUES (" + latestID + ")";
			LOGGER.fine(insertSQLStatement);
			// diesen ausfuehren
			stmt.executeUpdate(insertSQLStatement);
			stmt.close();
			LOGGER.fine("latest ObjectID wurde erfolgreich in der Datenbank gespeichert.");
		} catch (SQLException sqle) {
			/*
			 * Hier wird eine aufgetretene SQL Exception seperat abgefangen, um
			 * Fehlerquellen schneller zu finden.
			 */
			LOGGER.fine("Beim Schreiben der letzten ObjectID in der Datenbank ist eine SQLException aufgetreten: " + sqle.getMessage());
		} catch (Exception e) {
			LOGGER.fine("latest ObjectID konnte nicht in die Datenbank gespeichert werden.");
		}
	}

	/*
	 * ******************************************
	 * LevelMapTree Methoden
	 * ******************************************
	 */
	/**
	 * Diese Methode war urspruenglich gedacht, um einzelne Level erneut
	 * generieren zu koennen. Ist aber nun unoetig, da der gesamte LevelMap Baum
	 * einen einzigen Seed benoetigt, um daraufhin alle Level erneut identisch
	 * zu generieren.
	 * 
	 * @author Peter Kings
	 * @param id ID eines Levels
	 * @deprecated
	 * @return Seed zur Levelgenerierung
	 */
	public static long getLevelMapSeedForLevelID(int id) {
		/*
		 * Seed fuer um ein Level mit der LevelId zu speichern
		 */
		long seed = -1;
		if (c == null)
			connectToDatabase();
		try {
			Statement stmt = c.createStatement();
			/*
			 * eine Query Abfrage, die einen Seed fuer eine LevelID zurueckgibt
			 */
			ResultSet rs = stmt.executeQuery("SELECT * FROM `LevelMap` WHERE levelID = " + id);

			while (rs.next()) {
				seed = Long.parseLong(rs.getString("seed"));
			}
			LOGGER.fine("LevelMap Seed mit ID: " + id + " wurde aus der Datenbank geladen.");
		} catch (SQLException sqle) {
			LOGGER.fine("Beim Laden des Seed fuer die LevelMap mit der ID: " + id + " ist folgende SQLException aufgetreten: " + sqle.getMessage());
		} catch (Exception e) {
			LOGGER.fine("LevelMap Seed mit ID: " + id + " konnte aus der Datenbank geladen werden.");
		}
		return seed;
	}

	/**
	 * Diese Methode war urspruenglich gedacht, um einzelne Level erneut
	 * generieren zu koennen. Ist aber nun unoetig, da der gesamte LevelMap Baum
	 * einen einzigen Seed benoetigt, um daraufhin alle Level erneut identisch
	 * zu generieren.
	 * 
	 * @author Peter Kings
	 * @param levelID LeveID einer LevelMap
	 * @param seed verwendeter LevelMap Seed
	 * @deprecated
	 */
	public static void saveLevelSeed(int levelID, long seed) {
		/*
		 * Speichert den Seed eines Levels in der Datenbank
		 */
		if (c == null)
			connectToDatabase();
		try {
			Statement stmt = c.createStatement();
			String insertSQLStatement = "INSERT INTO `LevelMap`(`levelID`, `seed`) VALUES ('" + levelID + "','" + seed + "');";
			LOGGER.fine(insertSQLStatement);
			stmt.executeUpdate(insertSQLStatement);
			stmt.close();
			LOGGER.fine("LevelMap Seed mit ID: " + levelID + " wurde erfolgreich in der Datenbank gespeichert.");
		} catch (SQLException sqle) {
			LOGGER.fine("Beim Speichern des Seed fuer die LevelMap mit der ID: " + levelID + " ist folgende SQLException aufgetreten: " + sqle.getMessage());
		} catch (Exception e) {
			LOGGER.fine("LevelMap Seed mit ID: " + levelID + " konnte nicht in die Datenbank gespeichert werden.");
		}
	}

	/**
	 * Laden des Seeds zur identischen Generierung des LevelMap Baums in der
	 * Datenbank.
	 * 
	 * @author Peter Kings
	 * @return -1: es liegt kein Seed in der Datenbank vor. alles andere: dies
	 *         ist der Seed.
	 */
	public static long getLevelTreeMapSeed() {
		/*
		 * mit -1 instanziiert: Absprache als wert, falls kein Seed in der
		 * Datenbank vorliegt.
		 */
		long seed = -1;
		/*
		 * Verbindung zur Datenbank herstellen, falls noch keine besteht.
		 */
		if (c == null)
			connectToDatabase();
		/*
		 * versuche, den LevelMapTree Seed aus der Datenbank zu laden.
		 */
		try {
			Statement stmt = c.createStatement();
			/*
			 * eine Query Abfrage, die einen Seed zur Generierung des gesamten
			 * LevelMap Baums zurueckgibt
			 */
			ResultSet rs = stmt.executeQuery("SELECT * FROM `LevelMapTree`");
			while (rs.next()) {
				/*
				 * Seed zu einem Long casten und in Variable abspeichern.
				 */
				seed = Long.parseLong(rs.getString("seed"));
			}
			LOGGER.fine("LevelTreeMap Seed wurde aus der Datenbank geladen.");
		} catch (SQLException sqle) {
			/*
			 * Hier wird eine aufgetretene SQL Exception seperat abgefangen, um
			 * Fehlerquellen schneller zu finden.
			 */
			LOGGER.fine("Beim Laden des Seed fuer die LevelTreeMap ist folgende SQLException aufgetreten: " + sqle.getMessage());
		} catch (Exception e) {
			LOGGER.fine("LevelMap Seed konnte aus der Datenbank geladen werden.");
		}
		/*
		 * gibt -1 zurueck, falls kein Seed in der Datenbank vorliegt. Sonst den
		 * aus der Datenbank geladenen Seed.
		 */
		return seed;
	}

	/**
	 * Speichert den Seed zur Generierung des LevelMap Baums in der Datenbank
	 * ab.
	 * 
	 * @author Peter Kings
	 * @param seed Seed zur Generierung des LevelMapTrees
	 */
	public static void saveLevelTreeSeed(long seed) {
		/*
		 * Datenbank Verbindung aufbauen, falls noch nicht vorhanden
		 */
		if (c == null)
			connectToDatabase();
		/*
		 * Versuche, diesen zu speichern.
		 */
		try {
			Statement stmt = c.createStatement();
			// SQL Befehl, um Seed in die Tabelle LevelMapTree einzufuegen
			String insertSQLStatement = "INSERT INTO `LevelMapTree`(`seed`) VALUES ('" + seed + "');";
			LOGGER.fine(insertSQLStatement);
			// SQL Befehl ausfuehren
			stmt.executeUpdate(insertSQLStatement);
			stmt.close();
			LOGGER.fine("LevelTreeMap Seed wurde erfolgreich in der Datenbank gespeichert.");
		} catch (SQLException sqle) {
			/*
			 * Hier wird eine aufgetretene SQL Exception seperat abgefangen, um
			 * Fehlerquellen schneller zu finden.
			 */
			LOGGER.fine("Beim Speichern des Seed fuer die LevelTreeMap ist folgende SQLException aufgetreten: " + sqle.getMessage());
		} catch (Exception e) {
			LOGGER.fine("LeveltreeMap Seed konnte nicht in die Datenbank gespeichert werden.");
		}
	}

	/**
	 * Speichert die Coordinaten, welche bereits von einer LevelMap sichtbar
	 * sind (VisiblePositions) in der Datenbank.
	 * 
	 * @param levelID ID des Levels
	 * @author Peter Kings
	 */
	public static void saveFogOfWarCoordinatesForLevelWithID(int levelID) {
		/*
		 * LevelMap mit der zugehoerigen ID holen
		 */
		LevelMap levelMap = LevelMapsHandler.getLevelMapWithLevelID(levelID);
		// Datenbank Anbindung, falls diese noch nicht besteht.
		if (c == null)
			connectToDatabase();
		/*
		 * versuche, alle sichtbaren Coordinaten des levels mit der uebergeben
		 * LevelID zu speichern.
		 */
		try {
			Statement stmt = c.createStatement();
			/*
			 * fuer alle Coordinaten, die in VisiblePositions enthalten sind...
			 */
			for (Coordinates coord : levelMap.getVisiblePositions()) {
				/*
				 * fuege diese Coordinaten der Tabelle FogOf War hinzu
				 */
				String insertSQLStatement = "INSERT INTO `FogOfWar`(`levelMapID`,`xPos`,`yPos`) VALUES ('" + levelID + "','" + coord.x + "','" + coord.y + "');";
				LOGGER.fine(insertSQLStatement);
				// SQL Befehl ausfuehren
				stmt.executeUpdate(insertSQLStatement);
			}
			stmt.close();
			LOGGER.fine("Fog of War fuer LevelID: " + levelID + " wurde erfolgreich in der Datenbank gespeichert.");
		} catch (SQLException sqle) {
			/*
			 * Hier wird eine aufgetretene SQL Exception seperat abgefangen, um
			 * Fehlerquellen schneller zu finden.
			 */
			LOGGER.fine("Beim Speichern des Fog of War fuer LevelID: " + levelID + " ist folgende SQLException aufgetreten: " + sqle.getMessage());
		} catch (Exception e) {
			LOGGER.fine("Fog of War fuer LevelID: " + levelID + " konnte nicht in die Datenbank gespeichert werden.");
		}
	}

	/**
	 * Diese Methode liefert ein Set aus Coordinates, welche sichtbare
	 * Positionen der LevelMap repraesentieren, falls diese in der Datenbank
	 * gespeichert sind.
	 * 
	 * @author Peter Kings
	 * @param levelID levelID der gewuenschten LevelMap
	 * @return Set mit sichtbaren Coordinates der LevelMap. leer: keine
	 *         Sichtbaren Bereiche auf dieser LevelMap
	 */
	public static Set<Coordinates> getFogOfWarSetForLevelID(int levelID) {
		/*
		 * Rueckgabe Set mit sichtbaren Coordinaten auf dieser LevelMap
		 */
		Set<Coordinates> visiblePositions = new HashSet<>();
		// Verbindung mit der Datenbank, falls noch nicht vorhanden.
		if (c == null)
			connectToDatabase();
		/*
		 * versuche, alle Sichtbaren Coordinaten der zugehorigen LevelMap aus
		 * der Datenbank zu laden.
		 */
		try {
			Statement stmt = c.createStatement();
			/*
			 * Query Abfrage, die alle Sichtbaren Coordinaten zurueckgibt, bei
			 * denen die LevelMapID = der uebergebenen levelID sind.
			 */
			ResultSet rs = stmt.executeQuery("SELECT * FROM `FogOfWar` WHERE levelMapID = " + levelID);
			// solange es weitere Coordinaten gibt...
			while (rs.next()) {
				// neues CoordinatenObject erstellen
				Coordinates coos = new Coordinates(Integer.parseInt(rs.getString("xPos")), Integer.parseInt(rs.getString("yPos")));
				// und dem Set hinzufuegen
				visiblePositions.add(coos);
			}
			LOGGER.fine("FogOfWar Coordinates mit ID: " + levelID + " wurde aus der Datenbank geladen.");
		} catch (SQLException sqle) {
			/*
			 * Hier wird eine aufgetretene SQL Exception seperat abgefangen, um
			 * Fehlerquellen schneller zu finden.
			 */
			LOGGER.fine("Beim Laden der FogOfWar Coordinates fuer die LevelMap mit der ID: " + levelID + " ist folgende SQLException aufgetreten: " + sqle.getMessage());
		} catch (Exception e) {
			LOGGER.fine("FogOfWar Coordinates fuer LevelMap mit ID: " + levelID + " konnte aus der Datenbank geladen werden.");
		}
		// Set mit sichtbaren Coordinates
		return visiblePositions;
	}

	/**
	 * Diese Methode speichert alle sichtbaren Coordinates aller existierenden
	 * LevelMaps in die Datenbank.
	 * 
	 * @author Peter Kings
	 */
	public static void saveFogOfWarCoordinatesForAllLevels() {
		// Verbindung zur Datenbank, falls noch nicht vorhanden
		if (c == null)
			connectToDatabase();
		/*
		 * versuche, alle sichtbaren Coordinates aller LevelMaps in der
		 * Datenbank zu speichern.
		 */
		try {
			Statement stmt = c.createStatement();
			// Tabelle komplett leeren.
			String insertSQLStatement = "truncate FogOfWar;";
			LOGGER.fine(insertSQLStatement);
			// SQL Befehl ausfuehren
			stmt.executeUpdate(insertSQLStatement);
			stmt.close();
			LOGGER.fine("Datenbank wurde erfolgreich zurueckgesetzt.");
		} catch (SQLException sqle) {
			/*
			 * Hier wird eine aufgetretene SQL Exception seperat abgefangen, um
			 * Fehlerquellen schneller zu finden.
			 */
			LOGGER.fine("Beim leeren der Datenbank ist eine SQLException aufgetreten: " + sqle.getMessage());
		} catch (Exception e) {
			LOGGER.fine("Datenbank konnte nicht geleert werden.");
		}
		// fuer alle LevelMaps, die im LevelMapsHandler existieren
		for (LevelMap levelMap : LevelMapsHandler.getLevelMaps()) {
			// speicher die sichtbaren Coordinates
			DatabaseConnection.saveFogOfWarCoordinatesForLevelWithID(levelMap.getLevelID());
		}
	}

	/**
	 * Diese Methode gibt einen boolean zurueck, ob ein Taxi zwischen dem
	 * uebergebenen Startlevel und Ziellevel gesperrt ist.
	 * 
	 * @param originID StartLevelID
	 * @param destinationID ZielLevelID
	 * @return true: Level ist offen, false: Level ist geschlossen
	 */
	public static boolean getTaxiBooleanForOriginAndDestination(int originID, int destinationID) {
		// standard: geschlossen = nicht offen = (!unlocked)
		boolean unlocked = false;
		// Verbindung zur Datenbank, falls noch nicht vorhanden
		if (c == null)
			connectToDatabase();
		/*
		 * versuche, versperrte Taxies aus der Datenbank zu laden.
		 */
		try {
			Statement stmt = c.createStatement();
			/*
			 * SQL Befehl: Taxi aus der Tabelle Taxi zu laden, bei dem
			 * startLevel und ziellevel mit den uebergebenen Parametern
			 * uebereinstimmen
			 */
			String sqlQuery = "SELECT * FROM `Taxi` WHERE originLeveID = " + originID + " AND destinationLevelID = " + destinationID;
			ResultSet rs = stmt.executeQuery(sqlQuery);
			while (rs.next()) {
				// 0 steht fuer false, 1 fuer true
				if (Integer.parseInt(rs.getString("unlocked")) == 0)
					unlocked = false;
				else
					unlocked = true;
			}
			LOGGER.fine("Taxi wurde aus der Datenbank geladen.");
		} catch (SQLException sqle) {
			/*
			 * Hier wird eine aufgetretene SQL Exception seperat abgefangen, um
			 * Fehlerquellen schneller zu finden.
			 */
			LOGGER.warning("Beim Laden eines Taxies ist folgende SQLException aufgetreten: " + sqle.getMessage());
		} catch (Exception e) {
			LOGGER.fine("Taxi konnte aus der Datenbank geladen werden.");
		}
		return unlocked;
	}

	/**
	 * Diese Methode speichert ein Taxi von einer LevelMap zu einer anderen in
	 * der Datenbank. Somit kann ermittelt werden, welche Taxies offen und
	 * welche geschlossen sind.
	 * 
	 * @param originID ID der StarLevelMap
	 * @param destinationID ID der ZielLevelMap
	 * @param bool true: offen, false: geschlossen
	 */
	public static void saveTaxiWithOriginAndDestination(int originID, int destinationID, boolean bool) {
		// Verbindung zur Datenbank herstellen, falls noch nicht vorhanden
		if (c == null)
			connectToDatabase();
		// existieren bereits Daten zu diesem Taxi?
		boolean dataExists = false;
		/*
		 * Versuche herauszufinden, ob zu diesem Taxi bereits Daten in der
		 * Datenbank vorliegen.
		 */
		try {
			Statement stmt = c.createStatement();
			/*
			 * SQL Befehl: Taxi aus der Tabelle Taxi zu laden, bei dem
			 * startLevel und ziellevel mit den uebergebenen Parametern
			 * uebereinstimmen
			 */
			String sqlBefehl = "SELECT * FROM `Taxi` WHERE originLeveID = " + originID + " AND destinationLevelID = " + destinationID;
			ResultSet rs = stmt.executeQuery(sqlBefehl);
			// Abfrage checken, ob bereits Daten zu diesem Taxi vorliegen
			if (!rs.isBeforeFirst())
				// es existieren keine Daten -> insert
				dataExists = false;
			else
				// sonst existieren Daten -> update
				dataExists = true;
		} catch (SQLException sqle) {
			LOGGER.warning("Beim Suchen des Taxis ist eine SQLException aufgetreten: " + sqle.getMessage());
		} catch (Exception e) {
			LOGGER.fine("Taxi Suchte fehlgeschlagen.");
		}
		/*
		 * versuche, das taxi einzufuegen oder upzudaten.
		 */
		try {
			int unlocked;
			// 0 = false, 1 = true
			if (bool)
				unlocked = 1;
			else
				unlocked = 0;
			Statement stmt = c.createStatement();
			String insertSQLStatement;
			// wenn ein Datensatz zu diesem Taxi existiert
			if (dataExists)
				// updaten
				insertSQLStatement = "UPDATE `Taxi` SET `originLeveID`='" + originID + "',`destinationLevelID`='" + destinationID + "',`unlocked`='" + unlocked + "' WHERE `originLeveID`='" + originID
						+ "' AND `destinationLevelID`='" + destinationID + "';";
			else
				// sonst neu eunfuegen
				insertSQLStatement = "INSERT INTO `Taxi`(`originLeveID`,`destinationLevelID`,`unlocked`) VALUES ('" + originID + "','" + destinationID + "','" + unlocked + "');";
			LOGGER.fine(insertSQLStatement);
			// SQL Befehl ausfuehren
			stmt.executeUpdate(insertSQLStatement);
			stmt.close();
			LOGGER.info("Taxi wurde erfolgreich in der Datenbank gespeichert.");
		} catch (SQLException sqle) {
			/*
			 * Hier wird eine aufgetretene SQL Exception seperat abgefangen, um
			 * Fehlerquellen schneller zu finden.
			 */
			LOGGER.warning("Beim Speichern des Taxi ist folgende SQLException aufgetreten: " + sqle.getMessage());
		} catch (Exception e) {
			LOGGER.warning("Taxi konnte nicht in die Datenbank gespeichert werden.");
		}
	}

	/**
	 * Dies ist die Run Methode der Timer Taks. Bei jeder Durchfuehrung werden
	 * alle registrierten Player in der Datenbank neu abgespeichert, die zuletzt
	 * vergebene ObjectID wird gespeichert und der Fog of War aller Level wird
	 * in die Datenbank geschrieben.
	 * 
	 * @auhtor Peter Kings
	 */
	public void run() {
		DatabaseConnection.updateRegisteredPlayers(PlayerConnectionHandler.getRegisteredPlayers());
		DatabaseConnection.updateLatestObjectID();
		DatabaseConnection.saveFogOfWarCoordinatesForAllLevels();
	}
}
