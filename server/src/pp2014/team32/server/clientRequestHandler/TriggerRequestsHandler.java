package pp2014.team32.server.clientRequestHandler;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

import pp2014.team32.server.Database.DatabaseConnection;
import pp2014.team32.server.LevelMaps.LevelMapsHandler;
import pp2014.team32.server.creatureManagement.InventoryHandler;
import pp2014.team32.server.creatureManagement.RangeAndCollisionCalculator;
import pp2014.team32.server.player.PlayerConnectionHandler;
import pp2014.team32.server.serverOutput.OutputMessageHandler;
import pp2014.team32.shared.entities.CraftingReceipts;
import pp2014.team32.shared.entities.GameCharacter;
import pp2014.team32.shared.entities.Item;
import pp2014.team32.shared.entities.LevelMap;
import pp2014.team32.shared.enums.CreatureStatusType;
import pp2014.team32.shared.enums.ItemType;
import pp2014.team32.shared.enums.MessageType;
import pp2014.team32.shared.messages.AttributeUpgradeRequest;
import pp2014.team32.shared.messages.CraftingRequest;
import pp2014.team32.shared.messages.Message;
import pp2014.team32.shared.messages.MovementInfo;
import pp2014.team32.shared.messages.NewLevelDataRequest;
import pp2014.team32.shared.messages.RespawnRequest;
import pp2014.team32.shared.utils.Coordinates;
import pp2014.team32.shared.utils.PropertyManager;

/**
 * Hier kuemmern wir uns um seltener eingehende und schnell abgehandelte
 * Requests wie
 * AttributeUpgradRequests, RespawnRequests, CraftingRequests und
 * NewLevelDataRequest.
 * Dabei handelt es sich hauptsaechlich um Trigger, also Ausloeser, die schnell
 * abgearbeitet sind und mit einer Message an mehrere Clients oder einen Client
 * abschliessen.
 * 
 * @author Moritz Bittner
 * 
 */
public class TriggerRequestsHandler implements Runnable {
	// BlockingQueue fuer eingehende Requests
	private static ArrayBlockingQueue<Message>	occasionalRequestQueue	= new ArrayBlockingQueue<>(Integer.parseInt(PropertyManager.getProperty("server.MessageQueueSize")));

	/**
	 * warten auf und nehmen von ArrayBlockingQueue.
	 */
	@Override
	public void run() {
		while (true) {
			Message inMessage;
			try {
				inMessage = occasionalRequestQueue.take();
				if (inMessage.MESSAGE_TYPE == MessageType.ATTRIBUTEUPGRADEREQUEST)
					handleAttributeUpgradeRequest((AttributeUpgradeRequest) inMessage);
				else if (inMessage.MESSAGE_TYPE == MessageType.RESPAWNREQUEST)
					handleRespawnRequest((RespawnRequest) inMessage);
				else if (inMessage.MESSAGE_TYPE == MessageType.CRAFTINGREQUEST)
					handleCraftingRequest((CraftingRequest) inMessage);
				else if (inMessage.MESSAGE_TYPE == MessageType.NEWLEVELDATAREQUEST)
					handleNewLevelDataRequest((NewLevelDataRequest) inMessage);

			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
	}

	/**
	 * Bearbeitet NewLevelDataRequest, indem es die aktuelle LevelMap an den
	 * anfordernden Client erneut sendet. Dieser Vorfall tritt auf, wenn
	 * clientseitig Inkonsistenzen auftreten.
	 * 
	 * @param req NewLevelDataRequest
	 * @author Moritz Bittner
	 */
	private void handleNewLevelDataRequest(NewLevelDataRequest req) {
		GameCharacter gC = PlayerConnectionHandler.getGameCharacterWithID(req.CHARACTER_ID);
		LevelMap lM = LevelMapsHandler.getLevelMapForGameCharacter(gC);
		OutputMessageHandler.sendLevelDataToClient(gC, lM);
	}

	/**
	 * Entsprechend der AttributeUpgradeRequest erhoehen wir hier die Attribute
	 * des entsprechenden GameCharacters und informieren anschliessend den
	 * Client ueber die Aenderungen.
	 * 
	 * (Client ueberprueft selbst, dass nur bestimmte Anzahl von Punkten fuer
	 * Attribute vergeben werden.)
	 * 
	 * @param aUR AttributeUpgradRequest des Clients
	 * @author Moritz
	 */
	private static void handleAttributeUpgradeRequest(AttributeUpgradeRequest aUR) {
		// Wir holen uns die GameCharacter Referenz mithilfe der in der Message
		// enthaltenene CharacterID
		GameCharacter gC = PlayerConnectionHandler.getGameCharacterWithID(aUR.CHARACTER_ID);
		// erhoehen die Attribute mithilfe der von Attributes bereitgestellten
		// Methode
		gC.attributes.addAttributes(aUR.ATTRIBUTE_ADDITIONS);
		// und informieren den Client ueber seine neuen Attribute
		OutputMessageHandler.updateAttributesToClient(gC);
	}

	/**
	 * Hier wird die Anfrage des Clients nach einem Tod des GameCharacters neu
	 * zu starten.
	 * 
	 * @param inMessage
	 * @author Moritz Bittner
	 */
	private static void handleRespawnRequest(RespawnRequest inMessage) {
		GameCharacter gC = PlayerConnectionHandler.getGameCharacterWithID(inMessage.CHARACTER_ID);
		LevelMap lM = LevelMapsHandler.getLevelMapForGameCharacter(gC);
		// gC.setCoordinates(new Coordinates(lM.getStartX(), lM.getStartY()));
		// // TODO evtl. anderer Respawnpunkt
		RangeAndCollisionCalculator.setCoordinatesForDrawableObjectPlacing(new Coordinates(lM.start.x, lM.start.y), gC, lM);
		LevelMapsHandler.addDrawableObjectToLevelMapAndInformClients(gC, lM);
		// leere movementInfo
		OutputMessageHandler.sendMessageToSetOfUsersOnThisLevelMap(new MovementInfo(gC.getID(), CreatureStatusType.STANDING, lM.start.x, lM.start.y, lM.start.x, lM.start.y), lM.getLevelID());
	}

	/**
	 * Verarbeitung von CraftingRequest:
	 * Ueberprueft wird, ob Rezept fuer uebergebenen ItemTypes vorhanden ist.
	 * Wenn ja, so werden die verwendeten Items aus dem Inventar geloescht und
	 * das Ergebnis des Craftingprozesses hinzugefuegt. Client wird ueber sein
	 * neues Inventar informiert.
	 * 
	 * @param craftingRequest
	 * @author Moritz Bittner
	 */
	private static void handleCraftingRequest(CraftingRequest craftingRequest) {
		GameCharacter gC = PlayerConnectionHandler.getGameCharacterWithID(craftingRequest.CHARACTER_ID);
		int size = craftingRequest.ITEM_IDS.size();
		java.util.Iterator<Integer> idIterator = craftingRequest.ITEM_IDS.iterator();
		// Set<Item> craftingInput = new HashSet<>();
		Set<ItemType> craftingInputItemTypes = new HashSet<>();
		while (idIterator.hasNext()) {
			craftingInputItemTypes.add(InventoryHandler.getInventoryItemWithID(gC, idIterator.next()).getItemType());
		}
		// ItemType des Ergebnisses aus dem CraftingRezepts
		ItemType resultType = CraftingReceipts.craft(craftingInputItemTypes);
		// wenn es ein Rezept gibt
		if (resultType != null) {
			// reset des Iterators fuer erneutes Durchlaufen
			idIterator = craftingRequest.ITEM_IDS.iterator();
			for (int i = 0; i < size; i++) {
				InventoryHandler.deleteItemByIDFromInventory(gC, idIterator.next());
			}
			InventoryHandler.addItemToInventory(gC, new Item(DatabaseConnection.getNextID(), 0, 0, resultType));
		}
		// abhaengig davon ob Crafting erfolgreich erhaelt der Client eine
		// korrekte Aktualisierung seines Inventars
		OutputMessageHandler.updateInventoryToClient(gC);
	}

	/**
	 * Uebernimmt eingehende Message in die ArrayBlockingQueue.
	 * 
	 * @param Message vom Typ AttributeUpgradRequests, RespawnRequests,
	 *            CraftingRequests oder NewLevelDataRequest.
	 * @author Moritz Bittner
	 */
	public static void submitIncommingOccasionalRequest(Message message) {
		try {
			occasionalRequestQueue.put(message);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
