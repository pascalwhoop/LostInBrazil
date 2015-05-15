package pp2014.team32.server.creatureManagement;

import java.util.ArrayList;

import pp2014.team32.server.Database.DatabaseConnection;
import pp2014.team32.server.LevelMaps.LevelMapsHandler;
import pp2014.team32.server.clientRequestHandler.ChatHandler;
import pp2014.team32.server.serverOutput.OutputMessageHandler;
import pp2014.team32.shared.entities.Creature;
import pp2014.team32.shared.entities.DrawableObject;
import pp2014.team32.shared.entities.FixedObject;
import pp2014.team32.shared.entities.GameCharacter;
import pp2014.team32.shared.entities.Item;
import pp2014.team32.shared.entities.LevelMap;
import pp2014.team32.shared.entities.MovableObject;
import pp2014.team32.shared.entities.Taxi;
import pp2014.team32.shared.enums.ItemType;
import pp2014.team32.shared.enums.UIObjectType;
import pp2014.team32.shared.messages.TorchMessage;
import pp2014.team32.shared.utils.Coordinates;

/**
 * Diese Klasse stellt im Allgemeinen statische Methoden zur Ueberpruefung von
 * Distanzen und Kollisionen von DrawableObjects untereinander zur Verfuegung.
 * Zudem befinden sich hier Methoden zum Setzen von DrawableObjects auf der
 * LevelMap und dem bislang auschliesslich aus der Kollision mit einem Taxi
 * iniitierten Wechel der LevelMap.
 * 
 * @author Moritz Bittner
 * 
 */
public class RangeAndCollisionCalculator {

	private static int	fieldsize	= 50;

	/**
	 * Liefert alle MovableObjects in bestimmter Range um das DrawableObject
	 * herum zurueck. Bedient sich dabei einer ueberladenen Methode. Nimmt sich
	 * selbst aber aus der Liste aus.
	 * 
	 * @param lM
	 * @param range
	 * @return MovableObjects in Range-Naehe werden zurueckgegeben
	 * @author Moritz Bittner
	 */
	public static ArrayList<MovableObject> getMovableObjectsInRange(DrawableObject dO, LevelMap lM, int range) {
		// wir holen uns die movableObjects in der Range mit der eigentlichen
		// Methode
		ArrayList<MovableObject> movsObj = getMovableObjectsInRange(dO.getX(), dO.getY(), dO.getWidth(), dO.getHeight(), lM, range);
		// entfernen das DrawableObject von dem wir ausgehen aus der Liste aus
		movsObj.remove(dO);
		// und geben diese Liste zurueck
		return movsObj;
	}

	/**
	 * Liefert alle MovableObjects in der Range, um die die uebergebene Flaeche
	 * herum zurueck.
	 * 
	 * @param x linke x-Position der Flaeche
	 * @param y obere y-Position der Flaeche
	 * @param width Breite
	 * @param height Hoehe
	 * @param lM LevelMap
	 * @param range
	 * @return
	 * @author Moritz Bittner
	 */
	public static ArrayList<MovableObject> getMovableObjectsInRange(int x, int y, int width, int height, LevelMap lM, int range) {
		// wir speichern die Objekte in der Range in einer Liste ab
		ArrayList<MovableObject> objectsInRange = new ArrayList<>();
		// MovableObjects auf der LevelMap muessen alle ueberprueft werden, da
		// sie sich auch zwischen den Tiles bewegen
		for (MovableObject mO : lM.getMovableObjects().values()) {
			// Wenn Flaeche des MovableObjects auf der LevelMap eine
			// Ueberschneidung aufweist mit der zu ueberpruefenden Flaeche
			// (uebergebene Flaeche + range)
			if (mO.getX() + mO.getWidth() + range > x && mO.getX() < x + width + range && mO.getY() + mO.getHeight() + range > y && mO.getY() < y + height + range) {
				// liegt es in der Range
				objectsInRange.add(mO);
			}
		}
		return objectsInRange;
	}

	/**
	 * Liefert Liste mit FixedObjects, welche sich mit dem uebergebenen
	 * DrawableObject auf der LevelMap ueberschneiden oder mit diesem
	 * kollidieren. Bedient sich dabei einer ueberladenen Methode.
	 * 
	 * @param dO DrawableObject, welches auf Kollision ueberprueft werden soll
	 * @param lM LevelMap
	 * @return ArrayList mit den FixedObjects, die mit
	 * @author Moritz Bittner
	 */
	public static ArrayList<FixedObject> getCollidingFixedObjects(DrawableObject dO, LevelMap lM) {
		return getCollidingFixedObjects(dO.getX(), dO.getY(), dO.getWidth(), dO.getHeight(), lM);
	}

	/**
	 * Methode ueberprueft Position der Creature auf Kollisionen und reagiert
	 * auf spezielle Faelle von Kollisionen
	 * Bei folgenden Kollisionsarten werden besondere Schritte
	 * eingeleitet:
	 * - bei Kollision eines GameCharacters mit einem Taxi wird die LevelMap,
	 * wenn moeglich gewechselt
	 * - bei Kollision einer Creature (Enemy oder GameCharacter) mit einem Item
	 * wird dieses dem Inventar hinzugefuegt
	 * 
	 * @param cr Creature
	 * @param lM LevelMap des GameCharacters
	 * @return Es liegt eine Kollision vor, wenn true zurueckgegeben wird.
	 * @author Moritz Bittner
	 * 
	 */
	public static boolean checkForAndHandleCollision(Creature cr, LevelMap lM) {
		// Movable Object Kollisionen
		ArrayList<MovableObject> collidingMovs = getMovableObjectsInRange(cr, lM, 0);
		// wenn Kollision mit MovableObject vorliegt
		if (!collidingMovs.isEmpty()) {
			// ueberpruefe Kollidierende MovableObjects auf Taxis
			for (MovableObject mO : collidingMovs) {
				// Taxis bringen GameCharacter ins neue Level
				if (mO.getTYPE() == UIObjectType.TAXI && cr.getTYPE() == UIObjectType.CHARACTER) {
					Taxi taxi = (Taxi) mO;
					GameCharacter gC = (GameCharacter) cr;
					// wenn Zugriff moeglich
					tryToChangeLevelMap(lM, taxi, gC);

				}
			}
			return true; // Kollision mit MovableObject
		}
		// FixedObjects Kollisionen
		ArrayList<FixedObject> collidingFixedObjects = getCollidingFixedObjects(cr, lM);
		if (collidingFixedObjects.isEmpty()) {
			// keine Kollision wenn Liste leer
			return false;
		} else {
			for (FixedObject fO : collidingFixedObjects) {
				if (fO != null && !(fO instanceof Item)) {
					return true; // Kollision!
					// aufnehmbares Item
				} else {
					// hier genauere Ueberschneidung erfordert
					if (fO.getX() + fO.getWidth() > cr.getX() && fO.getX() < cr.getX() + cr.getWidth() && fO.getY() + fO.getHeight() > cr.getY() && fO.getY() < cr.getY() + cr.getHeight()) {
						InventoryHandler.pickUpItem((Creature) cr, (Item) fO, lM);
					}
				}
			}
			// sonst keine Kollision
			return false;
		}
	}

	/**
	 * Wenn GameCharacter mit einem Taxi kollidiert ist so hat er die
	 * Moeglichkeit die LevelMap zu wechseln.
	 * Ob das Taxi bereits freigeschaltet ist, oder der GameCharacter ueber den
	 * zur Freischaltung benoetigten Ball im Inventar haelt wird hier
	 * ueberprueft. Wenn eine der Bedingungen erfuellt ist so wird der LevelMap
	 * Wechsel vollzogen.
	 * 
	 * @param originlM Herkunfts LevelMap
	 * @param taxi Taxi
	 * @param gC GameCharacter
	 * @author Moritz Bittner
	 */
	private static void tryToChangeLevelMap(LevelMap originlM, Taxi taxi, GameCharacter gC) {
		Item football = InventoryHandler.checkForFootballInInventory(gC);
		/*
		 * Wenn das Taxi freigeschaltet ist oder der GameCharacter einen Ball im
		 * Inventar haelt wird der LevelMapwechsel vollzogen
		 */
		if (taxi.isUnlocked() || football != null) {
			// wenn das Taxi nicht unlocked war
			if (!taxi.isUnlocked()) {
				// schalte das Taxi frei
				taxi.setUnlocked(true);
				// speichere es in der Datenbank
				DatabaseConnection.saveTaxiWithOriginAndDestination(originlM.getLevelID(), taxi.getDestinationLevelMap().getLevelID(), true);
				// und entferne den Ball aus dem Inventar
				InventoryHandler.deleteItemByIDFromInventory(gC, football.getID());
			}
			// Ziel LevelMap
			LevelMap destinationLM = taxi.getDestinationLevelMap();
			// wird neue LevelMap des GameCharacter
			gC.setCurrentLevelMapID(destinationLM.getLevelID());
			// GameCharacter wird aus alter LevelMap entfernt
			LevelMapsHandler.removeDrawableObjectFromLevelMapAndInformClients(gC, originlM);
			// die Koordinaten entsprechen nun im Zweifel erst mal dem
			// Startpunkt der LevelMap
			gC.setCoordinates(new Coordinates(destinationLM.start.x, destinationLM.start.y));
			// wenn aber unter den MovableObjects der neuen LevelMap ...
			for (MovableObject destMO : destinationLM.getMovableObjects().values()) {
				// ... das Taxi gefunden wird ...
				if (destMO.getTYPE() == UIObjectType.TAXI) {
					Taxi taxi2 = (Taxi) destMO;
					// .. welches als Ziel die HerkunftsLevelMap hat...
					if (taxi2.getDestinationLevelMap().getLevelID() == originlM.getLevelID()) {
						// so wird der GameCharacter kollisionfrei in der Naehe
						// dieses Taxis gesetzt
						setCoordinatesForDrawableObjectPlacing(taxi2, gC, destinationLM);
					}
				}
			}

			// gC.setCurrentLevelMapID(destinationLM.getLevelID());
			// tatsaechliches Hinzufuegen zur neuen LevelMap
			LevelMapsHandler.addDrawableObjectToLevelMapAndInformClients(gC, destinationLM);
			// Systemnachricht an alle Clients ueber vollzogenen LevelMap
			// Wechsel
			ChatHandler.sendSystemChatMessage(gC.getUserName() + " ist nach " + destinationLM.getCity() + " gewechselt!");
			// neue LevelData senden
			OutputMessageHandler.sendLevelDataToClient(gC, destinationLM);
			// aktive Player der neuen LevelMap auf Fackeln ueberpruefen
			for (MovableObject mO : destinationLM.getMovableObjects().values()) {
				// fuer GameCharacter
				if (mO.TYPE == UIObjectType.CHARACTER) {
					GameCharacter tempGC = (GameCharacter) mO;
					// ueberpruefe ob Fackel aktiv
					if (tempGC.getArmourItem() != null && tempGC.getArmourItem().getItemType() == ItemType.TORCH) {
						// und informiere in diesem Fall die Clients
						OutputMessageHandler.sendMessageToSetOfUsersOnThisLevelMap(new TorchMessage(tempGC.getID(), true), destinationLM.getLevelID());
					}
				}
			}
		}
	}

	/**
	 * Ueberprueft moegliche FixedObject Koordinaten an der Position der
	 * uebergebenen Flaeche auf Treffer, also Kollisionen.
	 * 
	 * @param x linke x-Position
	 * @param y obere y-Position
	 * @param width Breite
	 * @param height Hoehe
	 * @param lM LevelMap
	 * @return ArrayList mit FixedObjects welche mit der uebergebenen Flaeche
	 *         kollidieren
	 * @author Moritz Bittner
	 */
	public static ArrayList<FixedObject> getCollidingFixedObjects(int x, int y, int width, int height, LevelMap lM) {
		// wir speichern uns die kollidierenden FixedObjects in einer ArrayList
		ArrayList<FixedObject> collidingFixedObjects = new ArrayList<>();
		// FixedObjects werden nur aus naechster Umgebung abgefragt,
		// zudem muessen nur die fieldsize (50er) Koordinaten abgefragt werden

		// wir speichern uns die abzufragenden koordinatenintervall ab
		int xMin, xMax, yMin, yMax;
		// mithilfe von modulo operationen und der Position unserer Flaeche
		// berechnen wir dieses
		xMin = x - x % fieldsize;
		xMax = x + width - (x + width) % fieldsize;
		yMin = y - y % fieldsize;
		yMax = y + height - (y + height) % fieldsize;

		// wir merken uns fuer die Schleifen durch die Intervalle, die als
		// naechstes zu ueberpruefenden Koordinaten
		int tempX, tempY;
		// wir beginnen bei abzufragenden x Minimum
		tempX = xMin;
		// und iterieren solange wie das aktuelle x noch im Intervall liegt
		while (tempX <= xMax) {
			// in y Dimension beginnnen wir ebenfalls mit dem Minimum
			tempY = yMin;
			// und iterieren solange wie das aktuelle y noch im Intervall liegt
			while (tempY <= yMax) {
				// speichern fixedobject in fO, direkter Zugriff durch HashMap
				// Keys
				FixedObject fO = lM.getFixedObject(tempX, tempY);
				// wenn fO an dieser Stelle vorhanden
				if (fO != null) {
					// zaehlt dieses FixedObject zu den kollidierenden
					collidingFixedObjects.add(fO);
				}
				// in beiden Dimensionen koennen wir in fieldsize Schritten
				// weitergehen, da nur hier FixedObjects platziert werden
				tempY += fieldsize;
			}
			tempX += fieldsize;
		}
		return collidingFixedObjects;
	}

	/**
	 * Ueberprueft ob fuer uebergebene Flaeche eine Kollision mit einem
	 * FixedObject vorliegt, wobei hier Items ausgenommen sind.
	 * 
	 * @param x x-Stelle links
	 * @param y y-Stelle oben
	 * @param width Breite
	 * @param height Hoehe
	 * @param lM LevelMap
	 * @return false := keine Kollision mit FixedObject (Items ausgenommen)
	 * @author Moritz Bittner
	 */
	public static boolean checkForCollisionWithNoItemFixedObject(int x, int y, int width, int height, LevelMap lM) {

		ArrayList<FixedObject> noItemFixedObjectsColliding = getCollidingFixedObjects(x, y, width, height, lM);
		if (noItemFixedObjectsColliding.isEmpty()) {
			return false;
		}
		for (FixedObject fO : noItemFixedObjectsColliding) {
			if (fO.getTYPE() != UIObjectType.ITEM) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Gibt bei jeder Art von Kollision der uebergebenen DrawableObjects ein
	 * true zurueck. Hier keine Ausnahmen und kein Handling!
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param lM
	 * @return true:= Kollision oder Ueberschneidung
	 */
	private static boolean isCollisioning(int x, int y, int width, int height, LevelMap lM) {
		if (getMovableObjectsInRange(x, y, width, height, lM, 0).isEmpty() && getCollidingFixedObjects(x, y, width, height, lM).isEmpty()) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Setzt fuer das Platzieren eines DrawableObjects auf der LevelMap an
	 * der Position des uebergebenen DrawableObjects oder dessen Umgebung die
	 * passenden Koordinaten. Verwendet dafuer ueberladene gleichnamige Methode.
	 * 
	 * @param center DrawableObject auf der Map, in dessen Naehe etwas platziert
	 *            werden soll
	 * @param dOToPlace Objekt, welches platziert werden soll
	 * @param lM LevelMap
	 * @author Moritz Bittner
	 */
	public static void setCoordinatesForDrawableObjectPlacing(DrawableObject center, DrawableObject dOToPlace, LevelMap lM) {
		setCoordinatesForDrawableObjectPlacing(new Coordinates(center.getCenteredX(), center.getCenteredY()), dOToPlace, lM);
	}

	/**
	 * Setzt fuer das Platzieren eines DrawableObjects auf der LevelMap an
	 * den uebergebenen ZIELKOORDINATEN oder deren Umgebung die passenden
	 * Koordinaten. Probiert dafuer
	 * Positionen an und um der uebergebenen Position aus und ueberprueft sie
	 * auf Kollisionen. Wenn eine kollisionfreie Position gefunden wurde, sind
	 * die
	 * die Koordinaten des DrawableObjects korrekt gesetzt und die Methode kann
	 * erfolgreich abgebrochen werden.
	 * 
	 * @param dest Zielkoordinaten an denen das DrawableObject platziert werden
	 *            soll
	 * @param dOToPlace zu platzierenes Item oder GameCharacter
	 * @param lM LevelMap
	 * @author Moritz Bittner
	 */
	public static void setCoordinatesForDrawableObjectPlacing(Coordinates dest, DrawableObject dOToPlace, LevelMap lM) {
		// Zwischenabstand: Abstand zwischen zu probierenen Positionen
		// im Zweifel sollte dieser sich an fieldsize orientieren
		int interspace = fieldsize;
		// wenn es sich um einen Character handelt muss der Zwischenabstand
		// nicht entsprechend der fieldsize gewaehlt werden und sinnvollerweise
		// bietet sich das Maximum der Hoehe bzw. Breite des Characters als
		// Zwischenabstand an
		if (dOToPlace.getTYPE() == UIObjectType.CHARACTER) {
			interspace = Math.max(GameCharacter.getImageHeight(), GameCharacter.getImageWidth());
		}
		// Abstand zu Zielkoordinaten
		int radius = 0; // zunaechst 0
		Coordinates coords;

		coords = getCoordinatesForDrawableObjectPlacingWithShift(dest, dOToPlace, radius, radius);
		// solange Kollision noch nicht ueberprueft (boolean) oder eine
		// Kollision vorliegt
		boolean isCollisioning = true;
		while (isCollisioning && isCollisioning(coords.x, coords.y, dOToPlace.getWidth(), dOToPlace.getHeight(), lM)) {
			// radius, also Abstand zu Zielkoordinaten wird weiter erhoeht um
			// Zwischenabstand
			radius += interspace;
			// Plaetze mit diesem Radius werden rund herum um destination
			// Koordinaten abgefragt
			for (int xShift = -radius; xShift <= radius; xShift += interspace) {
				for (int yShift = -radius; yShift <= radius; yShift += interspace) {
					// hier werden die coords entsprechend neu gesetzt
					coords = getCoordinatesForDrawableObjectPlacingWithShift(dest, dOToPlace, xShift, yShift);
					// und hier geprueft ob keine Kollision mehr vorliegt sind
					if (!isCollisioning(coords.x, coords.y, dOToPlace.getWidth(), dOToPlace.getHeight(), lM)) {
						// wenn keine Kollision mehr vorliegt, merken wir uns
						// das, um die Schleifen schneller zu verlassen
						isCollisioning = false;
						break;
					}
				}
				// wenn bereits festgestellt, dass aktuelle Koordinaten legal
				// sind oder
				if (!isCollisioning) {
					// dann verlasse auch die auessere for-Schleife
					break;
				}
			}

		}
		// setze die finalen und kollisionsfreien Koordinaten
		dOToPlace.setCoordinates(coords);

	}

	/**
	 * Zum Angreifen von Creatures wird hier abgefragt, ob und wer sich in der
	 * moeglichen Range befindet.
	 * Liefert eines der naechsten Zielobjekte des geforderten Creature Typen
	 * auf der gleich LevelMap
	 * innerhalb der gebotenen Range um den Angreifer herum zurueck.
	 * 
	 * @param cr angreifende Creature
	 * @param targetType UIObjectType.ENEMY || UIObjectType.CHARACTER je nach
	 *            dem welches Zielobjekt gesucht wird
	 * @param lM LevelMap
	 * @param range maximale AngriffsRange
	 * @return eines der naechsten Zielobjekte innerhalb der Range, falls
	 *         vorhanden, sonst null
	 * @author Moritz Bittner
	 */
	public static Creature getNearTargetInRange(Creature cr, UIObjectType targetType, LevelMap lM, int range) {
		// Range wird in kleinere Intervalle unterteilt ueber die wir iterieren
		for (int i = 0; i <= range; i += 5) {
			// abfrage der movableObjets in range i
			ArrayList<MovableObject> mOsInRange = RangeAndCollisionCalculator.getMovableObjectsInRange(cr, lM, i);
			// sobald ein oder mehrere movableObject enthalten wird der erste
			// Enemy dieser ausgewaehlt und zurueckgegeben
			if (!mOsInRange.isEmpty()) {
				for (MovableObject mO : mOsInRange) {
					if (mO.getTYPE() == targetType) {
						return (Creature) mO;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Zum Platzieren von DrawableObjects muessen mehrere Koordinaten
	 * ausprobiert werden. Hier werden dieses im Fall von GameCharactern einfach
	 * entsprechend der Verschiebung gesetzt. Im Falle von allen anderen
	 * Objekten werden
	 * hier die allgemein definierte Feldgroesse und Platzierung von 50er
	 * Schritten fuer FixedObjects
	 * beruecksichtigt.
	 * 
	 * @param dest Koordinaten des Ursprungs-Ziels
	 * @param dOToPlace Objekt das Platziert wird
	 * @param xShift Verschiebung in x-Richtung von Ziel aus
	 * @param yShift Verschiebung in y-Richtung vom Ziel aus
	 * @author Moritz Bittner
	 */
	private static Coordinates getCoordinatesForDrawableObjectPlacingWithShift(Coordinates dest, DrawableObject dOToPlace, int xShift, int yShift) {
		// wenn GameCharacter muss Raster nicht beruecksichtigt werden
		if (dOToPlace.getTYPE() == UIObjectType.CHARACTER) {
			return new Coordinates((dest.x + xShift), (dest.y + yShift));
		} else {
			// sonst wird Raster sicherheitshalber beruecksichtigt: fuer
			// FixedItems muessen x und y ein Vielfaches von fieldsize sein
			return new Coordinates((dest.x + xShift) / fieldsize * fieldsize, (dest.y + yShift) / fieldsize * fieldsize);
		}
	}
}
