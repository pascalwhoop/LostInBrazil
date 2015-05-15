package pp2014.team32.server.clientRequestHandler;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Handler;
import java.util.logging.Logger;

import pp2014.team32.server.ServerMain;
import pp2014.team32.server.LevelMaps.LevelMapsHandler;
import pp2014.team32.server.creatureManagement.FightCalculator;
import pp2014.team32.server.creatureManagement.RangeAndCollisionCalculator;
import pp2014.team32.server.player.PlayerConnectionHandler;
import pp2014.team32.server.serverOutput.OutputMessageHandler;
import pp2014.team32.shared.entities.GameCharacter;
import pp2014.team32.shared.entities.LevelMap;
import pp2014.team32.shared.enums.AttributeType;
import pp2014.team32.shared.enums.CreatureStatusType;
import pp2014.team32.shared.messages.Message;
import pp2014.team32.shared.messages.MovementInfo;
import pp2014.team32.shared.messages.MovementRequest;
import pp2014.team32.shared.messages.UncoverMessage;
import pp2014.team32.shared.utils.Coordinates;
import pp2014.team32.shared.utils.PropertyManager;

/**
 * Diese Klasse erbt von Runnable und wird zum Serverstart in einem eigenem
 * Thread gestartet.
 * Die Aufgabe dieser Klasse liegt darin, eingehende MovementRequests des
 * Clients zu bearbeiten.
 * 
 * @author Moritz Bittner
 * 
 */
public class MovementRequestHandler implements Runnable {
	private final static Logger							LOGGER;
	
	// Queue fuer eingegangene MovementRequests
	private static ArrayBlockingQueue<MovementRequest>	movementQueue;

	private static int									minMovementIncrement;
	private static int									maxMovementIncrement;
	Handler												handler;

	static {
		LOGGER = Logger.getLogger(ServerMain.class.getName());
		minMovementIncrement = Integer.parseInt(PropertyManager.getProperty("minMovementIncrement"));
		maxMovementIncrement = Integer.parseInt(PropertyManager.getProperty("maxMovementIncrement"));
		movementQueue = new ArrayBlockingQueue<>(Integer.parseInt(PropertyManager.getProperty("server.MessageQueueSize")));
	}

	/**
	 * Methode die den Thread fuer MovementRequestsHandling (dieses Runnables)
	 * beschreibt.
	 * Bearbeitet eingehende MovementRequests. 
	 * 
	 * @author Moritz Bittner
	 * @Override
	 */
	public void run() {
		while (true) {
			MovementRequest mR;
			try {

				mR = movementQueue.take();
				handleMovementRequest(mR);

			} catch (InterruptedException e) {
				LOGGER.warning(e.getMessage());
				e.printStackTrace();
			}

		}
	}

	/**
	 * Auslesen einer MovementRequest und Einleitung entsprechender Schritte:
	 * Aufdecken des FogOfWar an der Postion des GameCharacters, Bewegung des
	 * GameCharacters und Verarbeitung von Angriffsschlaegen.
	 * 
	 * @param mR
	 *            zu bearbeitende MovementRequest
	 * @author Moritz Bittner
	 */
	private static void handleMovementRequest(MovementRequest mR) {
		GameCharacter gC = PlayerConnectionHandler.getGameCharacterWithID(mR.MOVABLE_OBJECT_ID);
		LevelMap lM = LevelMapsHandler.getLevelMapForGameCharacter(gC);
		// Konsistenz-Check (relevant bei Wechsel der LevelMap)
		if (lM.getLevelID() == mR.LEVEL_MAP_ID) {
			// alte Koordinaten fuer Client-seitigen Konsistenz-Check
			int oldX = gC.getX();
			int oldY = gC.getY();
			// wir decken in der Range des GameCharacters den Fog of War auf
			addUncoveredFogOfWar(new Coordinates(oldX, oldY), lM);
			// movement wird ausgefuehrt, wenn keine Kollision vorliegt
			performMovement(gC, lM, CreatureStatusType.getMovingType(mR.HOR_DIR, mR.VERT_DIR));
			// ggf. enthaltene Anfrage fuer Angriffsschlag verarbeiten
			if (mR.ATTACKING) {
				FightCalculator.handleAttackingRequest(gC, lM);
			}
			// wir schicken die entsprechende MovementInfo an alle Clients
			Message outMessage = new MovementInfo(mR.MOVABLE_OBJECT_ID, CreatureStatusType.getMovingType(mR.HOR_DIR, mR.VERT_DIR), oldX, oldY, gC.getX(), gC.getY());
			OutputMessageHandler.sendMessageToSetOfUsersOnThisLevelMap(outMessage, lM.getLevelID());
		}

	}

	/**
	 * Abzuarbeitetende MovementRequest wird hier uebergeben und in
	 * MovementRequest ArrayBlockingQueue geschrieben.
	 * 
	 * @param movementRequest
	 * @author Moritz Bittner
	 */
	public static void submitMovementRequest(MovementRequest movementRequest) {
		try {
			movementQueue.put(movementRequest); // TODO test
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}



	/**
	 * Fuehrt uebergebene Bewegung auf GameCharacter aus, wenn diese ohne
	 * Kollision moeglich ist.
	 * Um pixelgenaue Bewegungsfreiraeume auszunutzen wird Bewegungsschrittweite
	 * temporaer verringert bei Kollisionen
	 * 
	 * @param gC zu bewegender GameCharacter
	 * @param lM LevelMap des GameCharacter
	 * @param intendedMovement CreatureStatusType: angeforderte Bewegungsrichtung
	 * @author Moritz Bittner
	 */
	private static void performMovement(GameCharacter gC, LevelMap lM, CreatureStatusType intendedMovement) {
		// wir speichern die aktuelle MovementIncrement in einer temporaeren
		// Variable
		int tempMoveIncrement = getSpecificMovementIncrement(gC);
		// boolean um schleife vorzeitig abzubrechen, um diese aber zu betreten
		// wird er zunaechst auf true gesetzt
		boolean isColliding = true;
		// nun ueberpruefen wir iterativ die Schrittweite: allerdings verringern
		// wir die Schrittweite nur, wenn eine Kollision vorliegt und das
		// solange wie die moveIncrement noch groesser Null ist
		while (tempMoveIncrement > 0 && isColliding) {
			// wir versuchen die aktuelle Schrittweite
			gC.move(intendedMovement, tempMoveIncrement);
			// bis hier liegt keine Kollision vor und Schleife wuerde bereits
			// beendet
			isColliding = false;
			// bei einer Kollision
			if (RangeAndCollisionCalculator.checkForAndHandleCollision(gC, lM)) {
				// machen wir den Schritt allerdings wieder rueckgaengig
				gC.move(CreatureStatusType.getOppositeMovingType(intendedMovement), tempMoveIncrement);
				// setzen den boolean auf true, da eine Kollision vorlag
				isColliding = true;
				// als naechstes ueberpruefen wir wieder ein kleineres
				// movementincrement
				tempMoveIncrement--;
			}
		}
	}

	/**
	 * Deckt den Fog Of War in der Umgebung der uebergebenen Koordinaten auf der
	 * LevelMap auf.
	 * 
	 * @param gameCharacterCoordinates
	 * @param lM
	 * @author Moritz Bittner
	 */
	private static void addUncoveredFogOfWar(Coordinates gameCharacterCoordinates, LevelMap lM) {
		// Feldgroesse
		int scalefactor = 50;
		// normieren der Koordinaten auf 50er Ecke (Tile-Koordinaten)
		gameCharacterCoordinates.x -= gameCharacterCoordinates.x % scalefactor;
		gameCharacterCoordinates.y -= gameCharacterCoordinates.y % scalefactor;
		// Zahl der aufzudeckenden Felder, jeweils links, rechts, oben und unten
		// vom GameCharacter
		int numberOfFieldsToUnFog = 10;
		// hier merken wir uns die mit diesem Schritt aufgedeckten Koordinaten
		// fuer Uebergabe an Clients
		Set<Coordinates> uncoveredCoords = new HashSet<>();
		// 2 dimensionales Durchlaufen von jeweils
		for (int i = -numberOfFieldsToUnFog; i <= numberOfFieldsToUnFog; i++) {
			for (int j = -numberOfFieldsToUnFog; j <= numberOfFieldsToUnFog; j++) {
				// speichern der aufzudeckenden Position als Koordinaten
				Coordinates coords = new Coordinates(gameCharacterCoordinates.x + i * scalefactor, gameCharacterCoordinates.y + j * scalefactor);
				// wenn Koordinaten noch nicht aufgedeckt
				if (!lM.getVisiblePositions().contains(coords)) {
					// so fuege die aufgedeckten Koordinaten der LevelMap hinzu
					lM.addVisiblePosition(coords);
					// sowie der Menge der in diesem Zug aufgedeckten
					// Koordinaten
					uncoveredCoords.add(coords);
				}
			}
		}
		// informieren Clients ueber neu aufgedeckte Koordinaten
		OutputMessageHandler.sendMessageToSetOfUsersOnThisLevelMap(new UncoverMessage(uncoveredCoords), lM.getLevelID());
	}

	/**
	 * Berechnet die GameCharacter spezifische MovementIncrement aus, diese ist
	 * abhaengig vom Standard MovementIncrement und dem Attribut Movement_Speed
	 * des GameCharacters
	 * 
	 * @param gC GameCharacter
	 * @return spezifische MovementIncrement
	 * @author Moritz Bittner
	 */
	private static int getSpecificMovementIncrement(GameCharacter gC) {
		// in Abhaengigkeit des MovementSpeeds liegt die spezfische
		// MovementIncrement zwischen minimum und maximum
		return minMovementIncrement + (gC.getAttributeValue(AttributeType.MOVEMENT_SPEED) * maxMovementIncrement - minMovementIncrement) / 100;
	}

}
