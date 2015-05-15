package pp2014.team32.server.LevelMaps;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import pp2014.team32.server.ServerMain;
import pp2014.team32.server.levgen.LevelTreeGenerator;
import pp2014.team32.server.serverOutput.OutputMessageHandler;
import pp2014.team32.shared.entities.DrawableObject;
import pp2014.team32.shared.entities.FixedObject;
import pp2014.team32.shared.entities.GameCharacter;
import pp2014.team32.shared.entities.LevelMap;
import pp2014.team32.shared.entities.MovableObject;
import pp2014.team32.shared.messages.DrawableObjectAddition;
import pp2014.team32.shared.messages.FixedObjectRemoval;
import pp2014.team32.shared.messages.MovableObjectRemoval;

/**
 * Diese Klasse dient der Verwaltung aller Levelmaps. Sie werden bei Gamestart
 * erstellt und je nach dem, ob fuer eine LevelID schon einmal erstellt wurde,
 * ein Seed aus der Datenbank geladen, oder ein neues Level generiert.
 * Desweiteren stehen Methoden zum Hinzufuegen und Entfernen von DrawableObjects
 * auf LevelMaps zur Verfuegung.
 * 
 * @author Peter Kings
 * @author Moritz Bittner
 */
public class LevelMapsHandler implements Runnable {
	private final static Logger			LOGGER		= Logger.getLogger(ServerMain.class.getName());
	private static ArrayList<LevelMap>	levelMaps	= new ArrayList<>();
	private static Lock					lock		= new ReentrantLock();

	/**
	 * Diese Methode sucht fuer jede LevelMap ID zuerst in der Datenbank, ob
	 * schon einmal eine LevelMap mit dieser ID erstellt wurde. Ist dies der
	 * Fall, wird der dort hinterlegte Seed (zur Generierung des selben Level)
	 * geladen und das entsprechende Level mit diesem Seed erstellt.
	 * Ist in der Datenbank kein Seed hinterlegt, so wird mit der aktuellen
	 * Millisekundenzahl ein neues Levelgeneriert.
	 * Hier ist der lock besonders wichtig, da sonst moeglicherweise LevelMaps
	 * angefragt werden, obwohl sie noch erstellt werden.
	 * 
	 * @author Peter Kings
	 */
	public static void generateLevelMaps() {

		// wir locken unsere Ressource und geben sie erst wieder frei, wenn wir
		// fertig sind
		try {
			lock.tryLock(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			LOGGER.warning(e.getMessage());
			generateLevelMaps();
		}
		// Selbst bei gleich bleibendem Seed kommen irgendwie andere Level raus
		// TODO Level muss gleich bleiben
		// seed fuer die level id
		// TODO je nach level id, ist jetzt hier auf levelid =0 hard coded
		levelMaps = LevelTreeGenerator.generateLevelTree();
		lock.unlock();
		LOGGER.info("Es wurden " + levelMaps.size() + " LevelMaps erstellt.");
	}

	/*
	 * ************************************************************
	 * Manipulate Levelmap Methoden
	 * ************************************************************
	 */
	/**
	 * Methode welche DrawableObject zur LevelMap hinzufuegt und aktive Clients
	 * entsprechendes Update schickt
	 * 
	 * @author Moritz Bittner
	 * @param dO
	 *            hinzuzufuegendes DrawableObject
	 * @param lM
	 *            betroffene LevelMap
	 * 
	 */
	public static void addDrawableObjectToLevelMapAndInformClients(DrawableObject dO, LevelMap lM) {
		lock.lock();

		// adden vom DrawableObject zur LevelMap auf Serverseite
		// MovableObject
		if (dO instanceof MovableObject) {
			// hinzufuegen zu LevelMap
			lM.addMovableObject((MovableObject) dO);
		//FixedObject
		} else if (dO instanceof FixedObject) {
			// hinzufuegen zu LevelMap
			lM.addFixedObject((FixedObject) dO);
		}
		// Senden der Aenderung an alle auf der Map aktiven Clients
		DrawableObjectAddition dOA = new DrawableObjectAddition(dO);
		OutputMessageHandler.sendMessageToSetOfUsersOnThisLevelMap(dOA, lM.getLevelID());

		lock.unlock();
	}

	/**
	 * Methode welche DrawableObject zur LevelMap loescht und aktive Clients
	 * entsprechendes Update schickt
	 * 
	 * @author Moritz Bittner
	 * @param dO
	 *            zu loeschendes DrawableObject
	 * @param lM
	 *            betroffene LevelMap
	 */
	public static void removeDrawableObjectFromLevelMapAndInformClients(DrawableObject dO, LevelMap lM) {
		lock.lock();
		// Unterscheidung zwischen MovableObjects und FixedObjects, da
		// unterschiedliche Index-Zugriffe

		if (dO instanceof MovableObject) {
			// entfernen auf LevelMap
			lM.removeMovableObject(dO.getID());
			// ClientMessages
			OutputMessageHandler.sendMessageToSetOfUsersOnThisLevelMap(new MovableObjectRemoval(dO.getID()), lM.getLevelID());
		} else if (dO instanceof FixedObject) {
			// entfernen auf LevelMap
			lM.removeFixedObject(dO.getX(), dO.getY());
			// ClientMessages
			OutputMessageHandler.sendMessageToSetOfUsersOnThisLevelMap(new FixedObjectRemoval(dO.getX(), dO.getY()), lM.getLevelID());

		}

		lock.unlock();

	}

	/*
	 * ************************************************************
	 * Getter Methoden
	 * ************************************************************
	 */
	/**
	 * Diese Methode sucht die LevelMap, auf der sich der uebergebene
	 * GameCharacter befindet.
	 * 
	 * @author Peter Kings
	 * @param character
	 * @return Levelmap, auf der sich der Character befindet
	 */
	public static LevelMap getLevelMapForGameCharacter(GameCharacter character) {
		lock.lock();
		// Levelmap des characters suchen
		LevelMap levelMap = null;
		// fuer alle LevelMaps
		for (LevelMap l : levelMaps) {
			// Vergleiche die ID
			if (l.getLevelID() == character.currentLevelMapID) {
				levelMap = l;
			}
		}
		lock.unlock();
		return levelMap;
	}

	/**
	 * Diese Methode sucht die LevelMap mit der zugehoerigen LevelID.
	 * 
	 * @author Peter Kings
	 * @param levelID ID des Levels
	 * @return Levelmap mit der zugehoerigen LevelID
	 */
	public static LevelMap getLevelMapWithLevelID(int levelID) {
		// lock setzen
		lock.lock();
		// Levelmap des characters suchen
		LevelMap levelMap = null;
		// fuer alle LevelMaps
		for (LevelMap l : levelMaps) {
			// Vergleiche die IDs
			if (l.getLevelID() == levelID) {
				levelMap = l;
			}
		}
		// lock aufheben
		lock.unlock();
		return levelMap;
	}

	/**
	 * Gibt alle erstellten LevelMaps in einer ArrayList zurueck.
	 * 
	 * @author Peter Kings
	 * @return ArrayList der LevelMaps
	 */
	public static ArrayList<LevelMap> getLevelMaps() {
		// lock setzen
		lock.lock();
		// Liste zwischenspeichern
		ArrayList<LevelMap> returnList = levelMaps;
		// lock aufheben
		lock.unlock();
		return returnList;
	}

	/**
	 * Die Run Methode dient einem eigenen Thread zur Level Generierung. Bei
	 * Game Start werden nun alle Level auf einmal erstellt. Zur Uebersicht
	 * wurde diese Levelgenerierung in die Methode @generateLevelMaps
	 * ausgelagert.
	 * 
	 * @author Peter Kings
	 */
	public void run() {
		generateLevelMaps();
	}
}
