package pp2014.team32.server.creatureManagement;

import java.util.ConcurrentModificationException;

import pp2014.team32.server.LevelMaps.LevelMapsHandler;
import pp2014.team32.server.clientRequestHandler.ChatHandler;
import pp2014.team32.server.serverOutput.OutputMessageHandler;
import pp2014.team32.shared.entities.Creature;
import pp2014.team32.shared.entities.Enemy;
import pp2014.team32.shared.entities.GameCharacter;
import pp2014.team32.shared.entities.Item;
import pp2014.team32.shared.entities.LevelMap;
import pp2014.team32.shared.enums.AttributeType;
import pp2014.team32.shared.enums.EnemyStateType;
import pp2014.team32.shared.enums.ItemType;
import pp2014.team32.shared.enums.UIObjectType;
import pp2014.team32.shared.messages.BulletAddition;
import pp2014.team32.shared.utils.Coordinates;
import pp2014.team32.shared.utils.PropertyManager;

/**
 * In dieser Klasse befinden sich eine Reihe von statischen Methoden, die
 * Kaempfe zwischen
 * Enemies und GameCharactern durchfuehren und deren Konsequenzen berechnen und
 * behandeln.
 * Dazu existieren zwei Schnittstellen nach aussen: die oeffentlichen Methoden
 * handleAttackRequest als Methode zur Bearbeitung von in MovementRequests
 * enthaltenen Angriffsversuchen des GameCharacters und die Methode
 * attackGameCharacter fuer Angriffe von Enemies auf GameCharacter.
 * Zudem bedienen sich diese beiden Methoden einer Reihe von interner privater
 * Methoden, welche die Angriffswerte berechnen und Angriffe umsetzen.
 * 
 * @author Moritz Bittner
 * 
 */
public class FightCalculator {
	// Maximalwert der Erfahrungspunkte welcher gebraucht wird, um LevelUpgrade
	// zu erkennen
	private static int	maxExptsValue;

	static {
		maxExptsValue = Integer.parseInt(PropertyManager.getProperty("maxAttributeValue"));
	}

	/**
	 * Kuemmert sich um Anfrage aus einer MovementRequest
	 * fuer einen Angriffsschlag.
	 * Ueberprueft, ob ein Kampfziel (Enemy) in Reichweite des GameCharacters
	 * ist
	 * und ruft fuer solche die Angriffs-Methode auf.
	 * Setzt zudem den Cooldown bis zum naechsten erlaubten Angriff.
	 * 
	 * @param gC GameCharacter
	 * @param lM LevelMap des GameCharacters
	 * @author Moritz Bittner
	 */
	public static void handleAttackingRequest(GameCharacter gC, LevelMap lM) {
		// falls nicht im Cooldown
		if (gC.attackEnabled) {
			// wird alles weitere ausgefuehrt
			// Reichweite grundsaetzlich auf 5
			int range = 5;
			// erhoeht sich bei speziell definierten Waffentypen um jeweilige
			// Range
			if (gC.getWeaponItem() != null) {
				range += ItemType.getWeaponsRange(gC.getWeaponItem().getItemType());
			}
			// Angriffsobjekt suchen
			Enemy en = (Enemy) RangeAndCollisionCalculator.getNearTargetInRange(gC, UIObjectType.ENEMY, lM, range);
			// wenn Enemy in Reichweite gefunden
			if (en != null) {
				// wird der Angriffsschlag auf diesen ausgefuehrt
				attackEnemy(gC, en, lM);
				// AttackSleepTime setzen wird 25 mal pro sekunde
				// runtergezaehlt
				// Sleep dauert in Abhaengigkeit des AttackSpeeds zwischen
				// bestenfalls 10/25 Sekunde und im schlechtesten Fall 15/25
				// Sekunde
				final int MAX_ATTACK_SLEEP = 15;
				final int MIN_ATTACK_SLEEP = 10;
				gC.setAttackSleepTime(MAX_ATTACK_SLEEP - gC.getAttributeValue(AttributeType.ATTACK_SPEED) * (MAX_ATTACK_SLEEP - MIN_ATTACK_SLEEP) / 100);
				// Attackieren bis zum countdown ende nicht moeglich
				gC.attackEnabled = false;
			}
		}
	}

	/**
	 * Attackieren eines Enemy mit einem GameCharacter.
	 * Enemy kriegt Schaden von Gesundheit abgezogen.
	 * Wenn der Enemy besiegt wurde werden
	 * zusaetzlich folgende Schritte eingeleitet:
	 * 
	 * 
	 * @param attackingGameCharacter
	 * @param attackedEnemy
	 * @param lM LevelMap lM
	 * @author Moritz Bittner
	 */
	private static void attackEnemy(GameCharacter attackingGameCharacter, Enemy attackedEnemy, LevelMap lM) {
		// Waffe des GameCharacters wird in lokaler Variable zwischengespeichert
		Item weapon = attackingGameCharacter.getWeaponItem();
		// wenn die Waffe des GameCharacters eine Munitionswaffe (Fernwaffe) ist
		if (weapon != null && ItemType.getWeaponsAmmoType(weapon.getItemType()) != null) {
			// wird mit Bullet geschossen
			shootBullet(attackingGameCharacter, attackedEnemy, lM, weapon);
		}
		// Verarbeitung des Damage
		damageCreature(attackingGameCharacter, attackedEnemy, lM);
		// aufwecken des Enemys
		if (attackedEnemy.getEnemyStateType() == EnemyStateType.SLEEP) {
			try {
				attackedEnemy.setEnemyStateType(EnemyStateType.ATTACK);
			} catch (ConcurrentModificationException ex) {
				// ignorieren
			}
		}
		// im Falle eines Todesschlages ...
		if (!isCreatureAlive(attackedEnemy)) {
			handleDefeatedEnemy(attackingGameCharacter, attackedEnemy, lM);
		}
	}

	/**
	 * @param attackingGameCharacter
	 * @param attackedEnemy
	 * @param lM
	 * @param weapon
	 */
	private static void shootBullet(GameCharacter attackingGameCharacter, Enemy attackedEnemy, LevelMap lM, Item weapon) {
		// speichern wir die mittigen Koordinaten des GameCharacters in
		// startCoords als Ausgangskoordinaten
		Coordinates startCoords = new Coordinates(attackingGameCharacter.getCenteredX(), attackingGameCharacter.getCenteredY());
		// und die Koordinaten des angegriffenen Enemys in destCoords als
		// Zielkoordinaten
		Coordinates destCoords = new Coordinates(attackedEnemy.getCenteredX(), attackedEnemy.getCenteredY());
		// und informieren alle Client ueber den Fernwaffenschuss fuer die
		// Animation
		OutputMessageHandler.sendMessageToSetOfUsersOnThisLevelMap(new BulletAddition(ItemType.getWeaponsAmmoType(weapon.getItemType()), startCoords, destCoords), lM.getLevelID());
	}

	/**
	 * Enemy wurde im Zweikampf besiegt. Deshalb wird hier Enemy wird von der
	 * LevelMap entfernt und sein Inventory Inhalt an seiner alten Position
	 * abgelegt. Zudem erhaelt der GameCharacter Erfahrungspunkte fuer den Sieg
	 * und alle Clients erhalten eine Fight ChatMessage.
	 * 
	 * @param attackingGameCharacter
	 * @param attackedEnemy
	 * @param lM
	 * @author Moritz Bittner
	 */
	private static void handleDefeatedEnemy(GameCharacter attackingGameCharacter, Enemy attackedEnemy, LevelMap lM) {
		// entfernen wir den enemy vom Spielfeld
		LevelMapsHandler.removeDrawableObjectFromLevelMapAndInformClients(attackedEnemy, lM);
		// und legen dessen InventarItems auf die LevelMap an dessen alter
		// Position
		for (Item i : attackedEnemy.inventory.getInventoryItems()) {
			if (i != null) {
				InventoryHandler.dropInventoryItemWithID(attackedEnemy, i.getID(), lM);
			}
		}
		// Erfahrungspunkte sind von ExPts des Enemies bestimmt und vom
		// Level des GameCharacters: je hoeher das Level desto weniger
		// bringt der gleiche Monstertyp an Erfahrungspunkten

		// Erfahrungspunkte des GameCharacters werden um die vom Enemy
		// definierten Erfahrungspunkte erhoeht
		attackingGameCharacter.increaseAttribute(AttributeType.EXPOINTS, (int) (attackedEnemy.getAttributeValue(AttributeType.EXPOINTS) / Math.pow(1.1, attackingGameCharacter.currentCharacterLevel)));
		// wenn Erfahrungspunkte groesser gleich 100 sind
		if (attackingGameCharacter.attributes.get(AttributeType.EXPOINTS) >= maxExptsValue) {
			upgradeGameCharacterLevel(attackingGameCharacter);
		}
		// abschliessend wird der Client des GameCharacters ueber die
		// finalen Erfahrungspunkte informiert
		OutputMessageHandler.updateAttributesToClient((GameCharacter) attackingGameCharacter);
		// und eine Kampfnachricht ueber den Sieg ueber den Enemy
		// rausgeschickt
		ChatHandler.sendFightChatMessage(attackingGameCharacter.getUserName().toUpperCase() + " besiegte " + attackedEnemy.getName());
	}

	/**
	 * Der GameCharacter hat einen Enemy zuvor besiegt und seine
	 * Erfahrungspunktzahl hat den Schwellenwert von 100 uebertroffen. Deshalb
	 * wird hier das GameCharacter Level um 1 erhoeht. Die Erfahrungspunkte
	 * angepasst und der zustaendige Client informiert.
	 * 
	 * @param attackingGameCharacter
	 * @author Moritz Bittner
	 */
	private static void upgradeGameCharacterLevel(GameCharacter attackingGameCharacter) {
		// wird das Level des GameCharacter um 1 erhoeht ...
		attackingGameCharacter.currentCharacterLevel++;
		// ,der Client darueber informiert
		OutputMessageHandler.updateLevelToClient(attackingGameCharacter);
		// und die Zahl der Erfahrungspunkte um 100 gesenkt
		attackingGameCharacter.attributes.decreaseValueBy(AttributeType.EXPOINTS, maxExptsValue);
	}

	/**
	 * Angriff eines Enemy auf einen GameCharacter:
	 * Schadensabzug und ggf. Tod von GameCharacter behandeln
	 * 
	 * @param attackingEnemy
	 * @param attackedGameCharacter
	 * @param lM
	 * @author Moritz Bittner
	 */
	public static void attackGameCharacter(Enemy attackingEnemy, GameCharacter attackedGameCharacter, LevelMap lM) {
		// GameCharacter wird Leben abgezogen
		damageCreature(attackingEnemy, attackedGameCharacter, lM);
		// wenn attackierter GameCharacter nicht mehr lebendig
		if (!isCreatureAlive(attackedGameCharacter)) {
			handleDeadGameCharacter(attackingEnemy, attackedGameCharacter, lM);

		}
	}

	/**
	 * Der GameCharacter wurde besiegt und hat keine Lebenspunkte mehr. Daher
	 * wird GameCharacter von der LevelMap entfernt, seine Erfahrungspunktzahl
	 * um 25% verringert und der Client fuer weitere Optionen ueber sein
	 * Ausscheiden informiert.
	 * 
	 * @param attackingEnemy
	 * @param attackedGameCharacter
	 * @param lM
	 * @author Moritz Bittner
	 */
	private static void handleDeadGameCharacter(Enemy attackingEnemy, GameCharacter attackedGameCharacter, LevelMap lM) {
		// entfernen wir den GameCharacter von seiner alten Position auf der
		// LEvelMap
		LevelMapsHandler.removeDrawableObjectFromLevelMapAndInformClients(attackedGameCharacter, lM);
		// informieren die Clients mit einer Kampfnachricht
		ChatHandler.sendFightChatMessage(attackedGameCharacter.getUserName() + " wurde von " + attackingEnemy.getName() + " besiegt!");
		// und senden dem Client des GameCharacters die Info ueber seine
		// Niederlage
		OutputMessageHandler.sendCharacterDeadInfoToClient(attackedGameCharacter);
		// 25% erfahrungspunkte gehen verloren
		attackedGameCharacter.attributes.setValueByPercentageOfOldValue(AttributeType.EXPOINTS, 0.75);
		attackedGameCharacter.attributes.set(AttributeType.HEALTH, 100);
		// Fight Message
		// informieren Client ueber Erfahrungspunkte und Lebenspunkte
		// informieren andere Clients ueber neue Health (mit inbegriffen)
		OutputMessageHandler.updateAttributesToClient(attackedGameCharacter);
	}

	/**
	 * Zieht damage eines Angriffschlags der angegriffenen Creature ab und
	 * informiert Clients ueber Aenderung der Lebenspunkte.
	 * 
	 * @param attackingCreature
	 * @param attackedCreature
	 * @author Moritz Bittner
	 */
	private static void damageCreature(Creature attackingCreature, Creature attackedCreature, LevelMap lM) {
		// Schaden der Gesundheit abziehen
		attackedCreature.attributes.decreaseValueBy(AttributeType.HEALTH, getDamageValue(attackingCreature, attackedCreature));
		// Client ueber Gesundheitsaenderung informieren
		OutputMessageHandler.updateHealthValueToClients(attackedCreature, lM);
	}

	/**
	 * Berechnet den Wert des Schadens eines Angriffsschlages, welcher von
	 * Attributen der aufeinander treffenden Creatures abhaengt.
	 * 
	 * @param attackingCreature
	 * @param attackedCreature
	 * @return Hoehe des Schadens
	 * @author Moritz Bittner
	 */
	private static int getDamageValue(Creature attackingCreature, Creature attackedCreature) {
		final int BASE_DAMAGE = 2;
		final int MAX_ADDIONAL_DAMAGE = 10;
		final int MAX_DEFENSE_DISCOUNT = 5;
		// Zusatz durch Angriffsstaerke zwischen 0 und MAX_ADDIONAL_DAMAGE
		int additionalDamage = Math.round(((attackingCreature.getAttributeValue(AttributeType.ATTACK_STRENGTH) * MAX_ADDIONAL_DAMAGE) / 100));
		// Zusatz durch Verteidigung zwischen 0 und MAX_DEFENSE_DISCOUNT
		int defenseDiscount = Math.round((attackedCreature.getAttributeValue(AttributeType.DEFENSE) * MAX_DEFENSE_DISCOUNT / 100));
		// tatsaechlicher Schaden berechnet sich aus summe von Grundwert und dem
		// Verhaeltnis der beiden obigen Werte
		return BASE_DAMAGE + Math.round(additionalDamage / (defenseDiscount + 1));
	}

	/**
	 * Prueft, ob Creature noch mehr als 0 Gesundheitspunkte hat.
	 * 
	 * @param attackedCreature
	 * @return true := Creature hat noch mehr als 0 Lebenspunkte, lebt also noch
	 * @author Moritz Bittner
	 */
	private static boolean isCreatureAlive(Creature attackedCreature) {
		if (attackedCreature.getAttributeValue(AttributeType.HEALTH) <= 0) {
			return false;
		} else
			return true;
	}
}
