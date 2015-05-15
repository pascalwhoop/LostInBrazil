package pp2014.team32.server.serverOutput;

import java.util.HashSet;
import java.util.Set;

import pp2014.team32.server.LevelMaps.LevelMapsHandler;
import pp2014.team32.server.comm.ClientConnectionHandler;
import pp2014.team32.server.player.PlayerConnectionHandler;
import pp2014.team32.shared.entities.Creature;
import pp2014.team32.shared.entities.GameCharacter;
import pp2014.team32.shared.entities.LevelMap;
import pp2014.team32.shared.entities.Player;
import pp2014.team32.shared.enums.AttributeType;
import pp2014.team32.shared.messages.AttributeChangeInfo;
import pp2014.team32.shared.messages.CharacterDeadInfo;
import pp2014.team32.shared.messages.HealthUpdate;
import pp2014.team32.shared.messages.InventoryUpdate;
import pp2014.team32.shared.messages.LevelData;
import pp2014.team32.shared.messages.LevelUpgrade;
import pp2014.team32.shared.messages.Message;

/**
 * Diese Klasse stellt diverse Schnittstellen zum Senden von Nachrichten an
 * Clients bereit. Diese bedienen sich der entsprechenden Methoden der
 * Kommunikation. So sind haeufig verwendete Funktionen, wie das Senden von
 * Nachrichten an alle aktiven Clients auf einer LevelMap, sowie das Senden von
 * z.B. eines
 * InventoryUpdate an den Client, welcher einen bestimmten GameCharacter spielt,
 * sehr leicht vollzogen.
 * 
 * @author Moritz Bittner
 * 
 */
public class OutputMessageHandler {

	/**
	 * Sendet die uebergebene Nachricht an alle Clients, welche auf der gleichen
	 * LevelMap spielen, zu.
	 * 
	 * @param m zu sendende Message
	 * @param thisLevelMapID LevelMapID
	 * @author Moritz Bittner
	 */
	public static void sendMessageToSetOfUsersOnThisLevelMap(Message m, int thisLevelMapID) {
		// Menge der auf dieser LevelMap befindlichen UserNames
		Set<String> set = new HashSet<String>();
		// wir iterieren ueber die aktiven Player
		for (Player p : PlayerConnectionHandler.getActivePlayers()) {
			// wenn die aktuelle LevelMapID des Spielers mit der uebergebenen ID
			// uebereinstimmt
			if (p.getMyCharacter().getCurrentLevelMapID() == thisLevelMapID) {
				// fuegen wir den Username des Players hinzu
				set.add(p.getUserName());
			}
			// zuletzt senden wir die uebergebene Message an in dem Set
			// befindlichen Player
			ClientConnectionHandler.sendMessageToSetOfUsers(m, set);
		}
	}

	/**
	 * Schickt Message vom Typ InventoryUpdate an den Client, welcher den
	 * GameCharacter spielt.
	 * 
	 * @param gC GameCharacter
	 * @author Moritz Bittner
	 */
	public static void updateInventoryToClient(GameCharacter gC) {
		// wir senden das aktuelle Inventar des GameCharacters als Message des
		// Typs InventoryUpdate an den Client
		ClientConnectionHandler.sendMessageToUser(gC.getUserName(), new InventoryUpdate(gC.inventory));
	}

	/**
	 * Schickt Message vom Typ AttributeUpdate an den Client, welcher den
	 * GameCharacter spielt.
	 * Und zusaetzlich ein HealthUpdate an alle Clients, welche auf der gleichen
	 * LevelMap spielen.
	 * 
	 * @param gC GameCharacter
	 * @author Moritz Bittner
	 */
	public static void updateAttributesToClient(GameCharacter gC) {
		// wir senden die aktuellen Attribute des GameCharacters als
		// AttributeChangeInfo an den Client
		ClientConnectionHandler.sendMessageToUser(gC.getUserName(), new AttributeChangeInfo(gC.getID(), gC.getAttributes()));
		// und ein HealthUpdate an alle auf der LevelMap befindlichen Clients
		updateHealthValueToClients(gC, LevelMapsHandler.getLevelMapForGameCharacter(gC));
	}

	/**
	 * Sendet aktualisiertes Level eines GameCharacter an dessen Client
	 * 
	 * @param gC GameCharacter, dessen Level erhoeht wurde
	 * @author Moritz Bittner
	 */
	public static void updateLevelToClient(GameCharacter gC) {
		// wir senden ein LevelUpgrade an den Client des GameCharacter
		ClientConnectionHandler.sendMessageToUser(gC.getUserName(), new LevelUpgrade(gC.getCurrentCharacterLevel()));
	}

	/**
	 * Informiert alle Clients auf der Map ueber Gesundheitsaenderung einer
	 * Creature
	 * 
	 * @param cr GameCharacter bzw. Enemy
	 * @param lM LevelMap der Creature cr
	 * @author Moritz Bittner
	 */
	public static void updateHealthValueToClients(Creature cr, LevelMap lM) {
		// wir holen uns die den Wert des aktuellen Gesundheitszustands und
		// shicken ihn an alle Clients dieser LevelMap als HealthUpdate
		sendMessageToSetOfUsersOnThisLevelMap(new HealthUpdate(cr.getID(), cr.attributes.get(AttributeType.HEALTH)), lM.getLevelID());
	}

	/**
	 * Schickt dem Client die LevelMap als Ganzes zu.
	 * 
	 * @param gC GameCharacter
	 * @param lM zu schickende LevelMap
	 * @author Moritz Bittner
	 */
	public static void sendLevelDataToClient(GameCharacter gC, LevelMap lM) {
		// wir senden dem Client eine Message des Typs LevelData zu, welche die
		// LevelMap beinhaltet
		ClientConnectionHandler.sendMessageToUser(gC.getUserName(), new LevelData(lM));
	}

	/**
	 * Informiert Client des GameCharacters ueber dessen Tod.
	 * 
	 * @param gC gestorbener GameCharacter
	 * @author Moritz Bittner
	 */
	public static void sendCharacterDeadInfoToClient(GameCharacter gC) {
		// wir senden dem Client eine CharacterDeadInfo um ihn zu informieren
		ClientConnectionHandler.sendMessageToUser(gC.getUserName(), new CharacterDeadInfo());
	}
}
