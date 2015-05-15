package pp2014.team32.server.clientRequestHandler;

import java.util.concurrent.ArrayBlockingQueue;

import pp2014.team32.server.LevelMaps.LevelMapsHandler;
import pp2014.team32.server.creatureManagement.InventoryHandler;
import pp2014.team32.server.player.PlayerConnectionHandler;
import pp2014.team32.server.serverOutput.OutputMessageHandler;
import pp2014.team32.shared.entities.GameCharacter;
import pp2014.team32.shared.entities.LevelMap;
import pp2014.team32.shared.enums.ActionType;
import pp2014.team32.shared.messages.InventoryActionRequest;
import pp2014.team32.shared.utils.PropertyManager;

/**
 * Diese Klasse erbt von Runnable und wird zum Serverstart in einem eigenem
 * Thread gestartet.
 * Die Aufgabe dieser Klasse liegt darin, eingehende InteractionRequests des
 * Clients zu bearbeiten.
 * 
 * @author Moritz Bittner
 * 
 */
public class InventoryActionRequestHandler implements Runnable {

	private static ArrayBlockingQueue<InventoryActionRequest>	interactionQueue	= new ArrayBlockingQueue<>(Integer.parseInt(PropertyManager.getProperty("server.MessageQueueSize")));
	
	/**
	 * Nimmt sich laufend die naechste anstehende Request aus der
	 * ArrayBlockingQueue. Und wartet, wenn keine zu bearbeitende Request mehr
	 * vorliegt.
	 * 
	 * @author Moritz Bittner
	 * @Override
	 */
	public void run() {
		// wird das ganze Spiel ueber ausgefuehrt
		while (true) {
			// speichert neachste Message
			InventoryActionRequest interactionRequest;
			try {
				// aus der eigenen Queue, take() laesst den Thread solange warten bis ein neachstes Element in der Schlange vorhanden ist
				interactionRequest = interactionQueue.take();
				// bearbeiten der InteractionRequest
				handleInteractionRequest(interactionRequest);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	/**
	 * Abzuarbeitende InteractionRequests werden hier uebergeben und in ArrayBlockingQueue des InteractionHandlers geschrieben.
	 * @param drawableObjectInteractionRequest
	 * @author Moritz Bittner
	 */
	public static void submitInteractionRequest(InventoryActionRequest drawableObjectInteractionRequest) {
		try {
			interactionQueue.put(drawableObjectInteractionRequest);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Diese Methode liest eine InteractionRequest aus und leitet in
	 * Abhaengigkeit des in der Request enthaltenen actiontype die zu
	 * erledigenden Prozesse ein.
	 * 
	 * @param interactionRequest
	 *            zu bearbeitende DrawableObjectInteractionRequest
	 * @author Moritz Bittner
	 */
	private static void handleInteractionRequest(InventoryActionRequest interactionRequest) {
		// INTERACTION_SOURCE_ID ist die ID des GameCharacters
		GameCharacter gC = PlayerConnectionHandler.getGameCharacterWithID(interactionRequest.INTERACTION_SOURCE_ID);
		// das Attribut ACTION gibt den ActionType an
		ActionType actiontype = interactionRequest.ACTION;
		// destinationID speichert die ID des Interaktionszielobjekts
		int destinationID = interactionRequest.INTERACTION_DESTINATION_ID;
		// lM speichert die LevelMap des GameCharacters
		LevelMap lM = LevelMapsHandler.getLevelMapForGameCharacter(gC);
		// in Abhaengigkeit des ActionTypes werden die noetigen Prozesse
		// eingeleitet
		switch (actiontype) {
		// Ablegen eines Items aus dem Inventar
		case DROP:
			InventoryHandler.dropInventoryItemWithID(gC, destinationID, lM);
			break;
		// Gebrauchen eines InventarItems
		case USE:
			InventoryHandler.useInventoryItem(gC, destinationID);
			break;

		// Zerstoeren eines InventarItems angefordert
		case DESTROY:
			InventoryHandler.deleteItemByIDFromInventory(gC, destinationID);
			break;
		// Zuruecklegen eines Items das als Waffe oder Ruestung gesetzt ist
		case PUT_BACK:
			InventoryHandler.putBackItem(gC, destinationID);
			break;
		}
		// alle ActionTypes betreffen Aenderungen am Inventar, diese wird hier
		// an den Client uebermittelt
		OutputMessageHandler.updateInventoryToClient(gC);
	}

}
