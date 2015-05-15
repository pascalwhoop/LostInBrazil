package pp2014.team32.server.creatureManagement;

import pp2014.team32.server.LevelMaps.LevelMapsHandler;
import pp2014.team32.server.RunnableTaskManager.RunnableTaskManager;
import pp2014.team32.server.clientRequestHandler.ChatHandler;
import pp2014.team32.server.serverOutput.OutputMessageHandler;
import pp2014.team32.server.updateTimer.TempAttributeChangeTimer;
import pp2014.team32.shared.entities.Creature;
import pp2014.team32.shared.entities.GameCharacter;
import pp2014.team32.shared.entities.Item;
import pp2014.team32.shared.entities.LevelMap;
import pp2014.team32.shared.enums.ItemType;
import pp2014.team32.shared.enums.ItemUseType;
import pp2014.team32.shared.enums.UIObjectType;
import pp2014.team32.shared.messages.TorchMessage;

/**
 * 
 * Diese Klasse haelt fuer saemtliche Inventory-Item Aktionen Methoden bereit.
 * 
 * @author Moritz Bittner
 * 
 */
public class InventoryHandler {

	/**
	 * Fuer das Aufheben eines Items von der LevelMap wird ueberprueft ob Platz
	 * im Inventar ist. Wenn ja wird das Item von der LevelMap entfernt. Zudem
	 * wird, wenn es sich um einen GameCharacter handelt, dieser ueber sein
	 * neues Inventar benachrichtigt.
	 * 
	 * @param cr Creature, welche Item aufnehmen soll
	 * @param item aufzunehmendes Item
	 * @param lM LevelMap
	 * @author Moritz Bittner
	 */
	public static boolean pickUpItem(Creature cr, Item item, LevelMap lM) {
		// wenn hinzufuegen zum Inventar erfolgreich ist
		if (addItemToInventory(cr, item)) {
			// entfernen wir das Item von der LevelMap
			LevelMapsHandler.removeDrawableObjectFromLevelMapAndInformClients(item, lM);
			// und im Falle dessen, dass es sich um einen GameCharacter handelt
			if (cr.getTYPE() == UIObjectType.CHARACTER) {
				// informieren wir den Client ueber sein InventarUpdate
				OutputMessageHandler.updateInventoryToClient((GameCharacter) cr);
			}
			return true; // Aufnahme erfolgreich
		}
		return false; // Inventar voll

	}

	/**
	 * Benutzen eines Items. Spezifische Auswirkungen, welche hier definiert
	 * werden, werden durch
	 * ItemUseType und ItemType unterschieden. ItemType.FOOTBALL und Crafting
	 * Items sind von einer normalen "Benutzen"-Aktion ausgeschlossen und wirken
	 * sich daher nicht aus. Beim wirksamen Benutzen eines dafuer zulaessigen
	 * Items aendern sich grundsaetzlich die Attribute des GameCharacters und
	 * das Item wird zumindest aus dem normalen Inventar entfernt. Des Weiteren
	 * haben Traenke nur voruebergehende Wirkung. Waffen oder Ruestung werden
	 * beim Benutzen aktiviert.
	 * 
	 * @param gC GameCharacter, welcher Items nutzen will
	 * @param inventoryItemID
	 * @author Moritz Bittner
	 */
	public static void useInventoryItem(GameCharacter gC, int inventoryItemID) {
		// wir speichern uns das Item der uebergebenen ID
		Item useItem = getInventoryItemWithID(gC, inventoryItemID);
		// wenn vorhanden und wenn kein Fussball (einziges Item, das nicht
		// benutzbar ist
		if (useItem != null && useItem.getItemUseType() != ItemUseType.CRAFT_ITEM && useItem.getItemType() != ItemType.FOOTBALL) {
			// erhoehen wir GameCharacter-Attribute um Attributwerte des Items
			gC.increaseAttributesBy(useItem.attributeChanges);
			// informieren den Client ueber Aenderung
			OutputMessageHandler.updateAttributesToClient(gC);
			// und entfernen das Item aus seinem Inventar
			deleteItemByIDFromInventory(gC, inventoryItemID);

			// ItemUseType bestimmt weiteres Handling
			switch (useItem.getItemUseType()) {
			case POTION:
				// haben nur voruebergehende Wirkung
				RunnableTaskManager.addRunnableTask(new TempAttributeChangeTimer(gC, useItem.attributeChanges));
				break;
			case WEAPON:
				// Waffe nutzen
				setWeapon(gC, useItem);
				break;
			case ARMOUR:
				// Ruestung nutzen
				setArmour(gC, useItem);
				break;
			default:
				// fuer die anderen Typen passiert nichts weiter
				break;

			}
		}
	}

	/**
	 * Setzen / Aktivieren eines Waffen-Item und ggf. Entfernen der vorherigen
	 * Waffe. Im Falle
	 * einer Taschenlampe wird zudem alle auf der LevelMap Client ueber dieses
	 * Ereignis informiert.
	 * 
	 * @param gC
	 * @param weapon neue Waffe
	 * @author Moritz Bittner
	 */
	private static void setWeapon(GameCharacter gC, Item weapon) {
		// wenn noch eine alte Waffe in Benutzung
		if (gC.getWeaponItem() != null) {
			// entfernen wir die alte Waffe
			removeWeapon(gC, gC.getWeaponItem().getID());
		}
		// dann setzen wir die uebergebene Waffe als neue Waffe
		gC.setWeapon(weapon);
		if (weapon.getItemType() == ItemType.TORCH) {
			// Clients ueber gesetzte Fackel informieren
			OutputMessageHandler.sendMessageToSetOfUsersOnThisLevelMap(new TorchMessage(gC.getID(), true), gC.getCurrentLevelMapID());
		}
	}

	/**
	 * Setzen / Aktivieren eines Ruestungsitems und ggf. Entfernen der zuvor
	 * aktiven Ruestungsitems
	 * 
	 * @param gC
	 * @param armour
	 * @author Moritz Bittner
	 */
	private static void setArmour(GameCharacter gC, Item armour) {
		// wenn noch in eine alte Ruestung in Benutzung
		if (gC.getArmourItem() != null) {
			// entfernen wir die alte Ruestung
			removeArmour(gC, gC.getArmourItem().getID());
		}
		// und setzen das neue Ruestungsitem
		gC.setArmourItem(armour);
	}

	/**
	 * Ueberprueft, ob es sich um eine Waffe oder Ruestung handelt, die
	 * zurueckgelegt werden soll und ruft die entsprechende Methode auf.
	 * 
	 * @param gC
	 * @param itemID
	 * @author Moritz Bittner
	 */
	public static void putBackItem(GameCharacter gC, int itemID) {
		// ueberpruftes Item
		Item item = gC.getArmourItem();
		// wenn es sich um die Ruestung handelt
		if (item != null && item.getID() == itemID) {
			// bewege diese aus dem aktiven Feld
			removeArmour(gC, itemID);
		} else
			// Waffe
			item = gC.getWeaponItem();
		// wenn es sich um die Waffe handelt
		if (item != null && item.getID() == itemID) {
			// bewege diese aus dem aktiven Feld
			removeWeapon(gC, itemID);
		}
	}

	/**
	 * Wenn im Inventory ein Platz fuer die aktive Waffe frei ist, wird das
	 * Weapon Item aus dem aktivem Feld des GameCharacters ins Inventory
	 * verschoben
	 * und seine Wirkung zurueckgesetzt.
	 * 
	 * @param gC
	 * @param usedItemID ID des zu entfernenden Items
	 * @author Moritz Bittner
	 */
	private static void removeWeapon(GameCharacter gC, int weaponID) {
		// wir merken uns die alte Waffe fuer spaeter
		Item itemToRemove = gC.getWeaponItem();
		// wenn Waffe vorhanden und ItemID uebereinstimmt und hinzufuegen zu
		// Inventar moeglich
		if (gC.getWeaponItem() != null && weaponID == gC.getWeaponItem().getID() && addItemToInventory(gC, itemToRemove)) {
			// dann entfernen wir die Waffe
			gC.setWeapon(null);
			resetItemsImpactOnAttributes(gC, itemToRemove);
		} else {
			ChatHandler.sendPersonalSystemChatMessage(gC.getUserName(), "Dein Inventar bietet nicht genug Platz!");
		}
	}

	/**
	 * Wenn im Inventory ein Platz fuer die aktive Ruestung frei ist, wird das
	 * RuestungsItem von dem aktivem Feld des GameCharacters in das Inventory
	 * verschoben
	 * und seine Wirkung zurueckgesetzt.
	 * 
	 * @param gC
	 * @param usedItemID ID des zu entfernenden Items
	 * @author Moritz Bittner
	 */
	private static void removeArmour(GameCharacter gC, int armourID) {
		Item itemToRemove = gC.getArmourItem();
		// andernfalls, wenn das Ruestungsitem mit der ID uebereinstimmt und
		// hinzufuegen zu Inventar moeglich
		if (gC.getArmourItem() != null && armourID == gC.getArmourItem().getID() && addItemToInventory(gC, itemToRemove)) {
			// dann setzen aktive Ruestung auf null
			gC.setArmourItem(null);
			resetItemsImpactOnAttributes(gC, itemToRemove);
		} else {
			ChatHandler.sendPersonalSystemChatMessage(gC.getUserName(), "Dein Inventar bietet nicht genug Platz!");
		}
	}

	/**
	 * Setzt die AttributWirkungen eines Items zurueck, indem AttributChanges
	 * von den GameCharacter Attributes wieder subtrahiert werden.
	 * Auch wird der Client informiert. Hier wird auch ueberprueft ob die
	 * Wirkung einer
	 * Fackel zurueckzusetzen ist.
	 * 
	 * @param gC GameCharacter
	 * @param inactiveItem nicht mehr wirksames Item
	 * @author Moritz Bittner
	 */
	private static void resetItemsImpactOnAttributes(GameCharacter gC, Item inactiveItem) {
		// wenn Ruestungsitem oder Waffenitem entfernt wurde
		if (inactiveItem != null) {
			// verringern wir die Attribute des GameCharacter um die Werte des
			// Items
			gC.decreaseAttributesBy(inactiveItem.attributeChanges);
			// und informieren den Client darueber
			OutputMessageHandler.updateAttributesToClient(gC);
			// Fackel zuruecksetzen
			if (inactiveItem.getItemType() == ItemType.TORCH) {
				// Clients ueber gesetzte Fackel informieren
				OutputMessageHandler.sendMessageToSetOfUsersOnThisLevelMap(new TorchMessage(gC.getID(), false), gC.getCurrentLevelMapID());
			}
		}
	}

	/**
	 * Fuegt Item dem Inventar der Creature hinzu, wenn ein Platz frei ist.
	 * 
	 * @param item
	 * @return false := Inventar voll
	 * @author Moritz Bittner
	 */
	public static boolean addItemToInventory(Creature creature, Item item) {
		// wir suchen uns die naechste freie Position
		int nextFreePosition = -1;
		// dazu iterieren wir ueber die Inventarplaetze
		for (int i = 0; i < creature.inventory.getInventorySize(); i++) {
			// und suchen nach einem freien Platz
			if (creature.inventory.getInventoryItems()[i] == null) {
				// haben wir diesen gefunden speichern wir ihn in der obigen
				// Variable
				nextFreePosition = i;
				// und brechen die Schleife ab
				break;
			}
		}
		// wenn an dieser Stelle ein Platz gefunden wurde
		if (nextFreePosition >= 0) {
			// setzen wir das Item auf diesen Platz im Inventar
			creature.inventory.getInventoryItems()[nextFreePosition] = item;
			// und melden zurueck, dass das Hinzufuegen geklappt hat
			return true;
		} else {
			return false;

		}
	}

	/**
	 * Loescht Item mit der uebergebenen ItemID aus dem Inventar der Creature
	 * und legt es auf der
	 * LevelMap kollisionsfrei ab.
	 * 
	 * @param inventoryItemID abzulegendes Item
	 * @param lM Ziel LevelMap
	 * @author Moritz Bittner
	 */
	public static void dropInventoryItemWithID(Creature creature, int itemID, LevelMap lM) {
		// wir holen uns das Item mit der entsprechenden ID aus dem Inventar
		Item dropItem = getInventoryItemWithID(creature, itemID);
		// wenn es denn eines ist
		if (dropItem != null) {
			// entfernen wir es aus dem Inventar
			deleteItemByIDFromInventory(creature, dropItem.getID());
			// suchen uns einen
			RangeAndCollisionCalculator.setCoordinatesForDrawableObjectPlacing(creature, dropItem, lM);
			// hinzufuegen des Items zur Map
			LevelMapsHandler.addDrawableObjectToLevelMapAndInformClients(dropItem, lM);
		}
	}

	/**
	 * Loescht ein Item aus Inventar sowie aus Waffen oder Ruestungsposition im
	 * Inventory und laesst es verschwinden (zerstoeren). Im Falle einer
	 * gesetzten Waffe oder Ruestung werden zudem die Attribute entsprechend
	 * zurueckgesetzt.
	 * 
	 * @param ItemID des zu loeschenden Items
	 * @author Moritz Bittner
	 */
	public static void deleteItemByIDFromInventory(Creature creature, int itemID) {
		Item[] inventoryItems = creature.inventory.getInventoryItems();
		for (int i = 0; i < inventoryItems.length; i++) {
			if (inventoryItems[i] != null && itemID == inventoryItems[i].getID()) {
				inventoryItems[i] = null;
				// erfolgreicher Abbruch der Methode
				return;
			}
		}
		// Waffen und Ruestung
		if (creature.getTYPE() == UIObjectType.CHARACTER) {
			GameCharacter gC = (GameCharacter) creature;
			if (gC.getArmourItem() != null && gC.getArmourItem().getID() == itemID) {
				resetItemsImpactOnAttributes(gC, gC.getArmourItem());
				gC.setArmourItem(null);
			} else if (gC.getWeaponItem() != null && gC.getWeaponItem().getID() == itemID) {
				resetItemsImpactOnAttributes(gC, gC.getWeaponItem());
				gC.setWeapon(null);
			}
		}

	}

	/**
	 * Liefert das Item Objekt mit der uebergebenen ID, wenn dieses im Inventory
	 * der Creature oder als gesetzte Ruestung oder Waffe vorhanden ist.
	 * @param creature
	 * @param itemID id des Items
	 * @return falls enthalten wird das Item mit der entsprechenden ID
	 *         zurueckgegeben
	 *         falls nicht, wird null zurueckgegeben
	 * @author Moritz Bittner
	 */
	public static Item getInventoryItemWithID(Creature creature, int itemID) {
		// wir iterieren ueber die Inventaritems
		for (Item i : creature.inventory.getInventoryItems()) {
			// wenn die gesucht ID mit der des InventarItems uebereinstimmt
			if (i != null && i.getID() == itemID) {
				// liefern wir dieses zurueck (und brechen damit die Methode
				// erfolgreich ab)
				return i;
			}
		}
		// Waffen und Ruestung: falls nicht im normalen Inventar enthalten und
		// es sich um einen GameCharacter handlet, wird
		// Waffen- und Ruestungsposition ueberprueft
		if (creature.getTYPE() == UIObjectType.CHARACTER) {
			GameCharacter gC = (GameCharacter) creature;
			// wenn RuestungsID uebereinstimmt
			if (gC.getArmourItem() != null && gC.getArmourItem().getID() == itemID) {
				// gib das RustungsID zurueck
				return gC.getArmourItem();
				// wenn WaffenID uebereinstimmt
			} else if (gC.getWeaponItem() != null && gC.getWeaponItem().getID() == itemID) {
				// gib WaffenItem zurueck
				return gC.getWeaponItem();
			}
		}
		// wenn bishier kein return, so ist das Item mit der uebergebenen ID
		// nicht im Besitz der Creature, gebe daher null zurueck
		return null;

	}

	/**
	 * Ueberprueft ob sich ein Ball im Inventar des GameCharacters befindet und
	 * gibt ihn in diesem Fall zurueck, sonst null
	 * 
	 * @param gC
	 * @return Item: Ball, wenn enthalten. Sonst null.
	 * @author Moritz Bittner
	 */
	public static Item checkForFootballInInventory(GameCharacter gC) {
		// iteriere ueber Items
		for (Item i : gC.inventory.getInventoryItems()) {
			// wenn Item vom Typ FOOTBALL enthalten
			if (i != null && i.getItemType() == ItemType.FOOTBALL) {
				// so gib ihn zurueck
				return i;
			}
		}
		// kein Ball im Inventar:
		return null;
	}

}
