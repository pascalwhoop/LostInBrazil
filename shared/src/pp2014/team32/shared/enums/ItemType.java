package pp2014.team32.shared.enums;

import pp2014.team32.shared.entities.Attributes;

import java.util.HashMap;
import java.util.Random;

/**
 * 
 * @author Moritz
 * 
 */
// ACHTUNG, wenn neue hinzugefuegt werden muessen diese auch im Item Konstruktor
// beachtet werden!!! siehe Klasse Item
public enum ItemType {

	// Crafting Items
	WOOD, STONE, GOLD, COAL, IRON, DIAMOND, RUBY, CRYSTAL, BRONZE, WOOL, CORN, MEAT,

	// Health Items
	BANANA, CHEESE, PINEAPPLE, BREAD, HAWAIITOAST, BANANABREAD, ROASTEDMEAT, BURGER,

	// Nahkampfwaffen
	BASEBALLBAT, AXE, SWORD, BRONZESWORD, GOLDSWORD, RUBYSWORD, DIAMONDSWORD, MACHETE, TORCH,

	// Fernkampfwaffen
	CATAPULT, PISTOL, SEMIAUTOMATIC, SHOTGUN,

	// Ruestung
	CAP, HELMET, CLOSEDHELMET, HAUBERK, GLOVES, SHOES, RING, FULLARMOUR,

	// Traenke
	ATTACKINGSPEEDPOTION, SPEEDPOTION, STRENGTHPOTION, DEFENSEPOTION, HEALTHREGENERATIONPOTION,

	// Sonstiges
	FOOTBALL;

	private static HashMap<ItemType, ItemUseType>	itemUseTypes		= new HashMap<>();
	private static HashMap<ItemType, Integer>		weaponRange			= new HashMap<>();
	private static HashMap<ItemType, AmmoType>		weaponsAmmoTypes	= new HashMap<>();
	private final static ItemType[]					SPAWNING_ITEMS		= { WOOD, STONE, GOLD, COAL, IRON, DIAMOND, RUBY, CRYSTAL, BRONZE, WOOL, CORN, MEAT, BANANA, CHEESE, PINEAPPLE };

	static {
		// ItemUseTypes Definitionen
		itemUseTypes.put(WOOD, ItemUseType.CRAFT_ITEM);
		itemUseTypes.put(STONE, ItemUseType.CRAFT_ITEM);
		itemUseTypes.put(GOLD, ItemUseType.CRAFT_ITEM);
		itemUseTypes.put(COAL, ItemUseType.CRAFT_ITEM);
		itemUseTypes.put(IRON, ItemUseType.CRAFT_ITEM);
		itemUseTypes.put(RUBY, ItemUseType.CRAFT_ITEM);
		itemUseTypes.put(DIAMOND, ItemUseType.CRAFT_ITEM);
		itemUseTypes.put(BRONZE, ItemUseType.CRAFT_ITEM);
		itemUseTypes.put(CRYSTAL, ItemUseType.CRAFT_ITEM);
		itemUseTypes.put(WOOL, ItemUseType.CRAFT_ITEM);
		itemUseTypes.put(CORN, ItemUseType.CRAFT_ITEM);
		itemUseTypes.put(MEAT, ItemUseType.CRAFT_ITEM);
		itemUseTypes.put(CORN, ItemUseType.CRAFT_ITEM);
		itemUseTypes.put(MEAT, ItemUseType.CRAFT_ITEM);

		itemUseTypes.put(BANANA, ItemUseType.HEALTH_ITEM);
		itemUseTypes.put(CHEESE, ItemUseType.HEALTH_ITEM);
		itemUseTypes.put(PINEAPPLE, ItemUseType.HEALTH_ITEM);
		itemUseTypes.put(BREAD, ItemUseType.HEALTH_ITEM);
		itemUseTypes.put(HAWAIITOAST, ItemUseType.HEALTH_ITEM);
		itemUseTypes.put(BANANABREAD, ItemUseType.HEALTH_ITEM);
		itemUseTypes.put(ROASTEDMEAT, ItemUseType.HEALTH_ITEM);
		itemUseTypes.put(BURGER, ItemUseType.HEALTH_ITEM);

		itemUseTypes.put(BASEBALLBAT, ItemUseType.WEAPON);
		itemUseTypes.put(AXE, ItemUseType.WEAPON);
		itemUseTypes.put(SWORD, ItemUseType.WEAPON);
		itemUseTypes.put(BRONZESWORD, ItemUseType.WEAPON);
		itemUseTypes.put(GOLDSWORD, ItemUseType.WEAPON);
		itemUseTypes.put(RUBYSWORD, ItemUseType.WEAPON);
		itemUseTypes.put(DIAMONDSWORD, ItemUseType.WEAPON);
		itemUseTypes.put(MACHETE, ItemUseType.WEAPON);
		itemUseTypes.put(TORCH, ItemUseType.WEAPON);
		itemUseTypes.put(CATAPULT, ItemUseType.WEAPON);
		itemUseTypes.put(PISTOL, ItemUseType.WEAPON);
		itemUseTypes.put(SEMIAUTOMATIC, ItemUseType.WEAPON);
		itemUseTypes.put(SHOTGUN, ItemUseType.WEAPON);

		itemUseTypes.put(CAP, ItemUseType.ARMOUR);
		itemUseTypes.put(HELMET, ItemUseType.ARMOUR);
		itemUseTypes.put(CLOSEDHELMET, ItemUseType.ARMOUR);
		itemUseTypes.put(HAUBERK, ItemUseType.ARMOUR);
		itemUseTypes.put(GLOVES, ItemUseType.ARMOUR);
		itemUseTypes.put(SHOES, ItemUseType.ARMOUR);
		itemUseTypes.put(RING, ItemUseType.ARMOUR);
		itemUseTypes.put(FULLARMOUR, ItemUseType.ARMOUR);

		itemUseTypes.put(STRENGTHPOTION, ItemUseType.POTION);
		itemUseTypes.put(DEFENSEPOTION, ItemUseType.POTION);
		itemUseTypes.put(SPEEDPOTION, ItemUseType.POTION);
		itemUseTypes.put(ATTACKINGSPEEDPOTION, ItemUseType.POTION);
		itemUseTypes.put(HEALTHREGENERATIONPOTION, ItemUseType.POTION);

		itemUseTypes.put(FOOTBALL, ItemUseType.KEY_ITEM);
		// Waffenrange Definitionen
		weaponRange.put(CATAPULT, 100);
		weaponRange.put(PISTOL, 200);
		weaponRange.put(SEMIAUTOMATIC, 300);
		weaponRange.put(SHOTGUN, 200);
		// Waffen Munition
		weaponsAmmoTypes.put(CATAPULT, AmmoType.STONE);
		weaponsAmmoTypes.put(PISTOL, AmmoType.BULLET);
		weaponsAmmoTypes.put(SEMIAUTOMATIC, AmmoType.BULLET);
		weaponsAmmoTypes.put(SHOTGUN, AmmoType.BULLET);
	}

	public static int getWeaponsRange(ItemType wepaonType) {
		if (weaponRange.get(wepaonType) != null) {
			return weaponRange.get(wepaonType);
		} else
			return 0;
	}

	public static AmmoType getWeaponsAmmoType(ItemType weaponType) {
		return weaponsAmmoTypes.get(weaponType);
	}

	public static ItemType getRandomSpawningItem() {
		return SPAWNING_ITEMS[new Random().nextInt(SPAWNING_ITEMS.length)];

	}

	/**
	 * Hier sind die Attributauswirkungen aller ItemTypes definiert und werden
	 * bei Anforderung durch diesen getter zurueckgegeben
	 * 
	 * @param itemType ItemType, dessen Attributes gefragt sind
	 * @return Attributes Objekt
	 * @author Moritz Bittner
	 */
	public static Attributes getAttributesForItemType(ItemType itemType) {
		switch (itemType) {
		case BANANA:
			return new Attributes(10, 0, 0, 0, 0, 0, 0);
		case CHEESE:
			return new Attributes(15, 0, 0, 0, 0, 0, 0);
		case PINEAPPLE:
			return new Attributes(20, 0, 0, 0, 0, 0, 0);
		case BREAD:
			return new Attributes(30, 0, 0, 0, 0, 0, 0);
		case BANANABREAD:
			return new Attributes(50, 0, 0, 0, 0, 0, 0);
		case ROASTEDMEAT:
			return new Attributes(50, 0, 0, 0, 0, 0, 0);
		case HAWAIITOAST:
			return new Attributes(80, 0, 0, 0, 0, 0, 0);
		case BURGER:
			return new Attributes(100, 0, 0, 0, 0, 0, 0);
		case BASEBALLBAT:
			return new Attributes(0, 10, 0, -5, 0, 0, 0);
		case AXE:
			return new Attributes(0, 20, 0, 0, 0, 0, 0);
		case SWORD:
			return new Attributes(0, 30, 0, 0, 0, 0, 0);
		case BRONZESWORD:
			return new Attributes(0, 35, 0, 5, 0, 0, 0);
		case GOLDSWORD:
			return new Attributes(0, 40, 0, 10, 0, 0, 0);
		case RUBYSWORD:
			return new Attributes(0, 45, 0, 15, 0, 0, 0);
		case DIAMONDSWORD:
			return new Attributes(0, 50, 0, 20, 0, 0, 0);
		case MACHETE:
			return new Attributes(0, 50, 0, 50, 0, 0, 0);
		case CATAPULT:
			return new Attributes(0, 5, 0, 0, 0, 0, 0);
		case PISTOL:
			return new Attributes(0, 10, 0, 0, 0, 0, 0);
		case SEMIAUTOMATIC:
			return new Attributes(0, 20, 0, 0, 0, 0, 0);
		case SHOTGUN:
			return new Attributes(0, 40, 0, 0, 0, 0, 0);
		case CAP:
			return new Attributes(0, 0, 5, 0, 0, 0, 0);
		case HELMET:
			return new Attributes(0, 0, 15, 0, 0, 0, 0);
		case CLOSEDHELMET:
			return new Attributes(0, 0, 20, 0, 0, 0, 0);
		case SHOES:
			return new Attributes(0, 0, 5, 30, 0, 0, 0);
		case HAUBERK:
			return new Attributes(0, 0, 20, 0, 0, 0, 0);
		case GLOVES:
			return new Attributes(0, 0, 5, 0, 10, 0, 0);
		case FULLARMOUR:
			return new Attributes(0, 0, 50, 0, 0, 0, 0);
		case RING:
			return new Attributes(0, 0, 10, 0, 0, 20, 0);
		case STRENGTHPOTION:
			return new Attributes(0, 30, 0, 0, 0, 0, 0);
		case DEFENSEPOTION:
			return new Attributes(0, 0, 30, 0, 0, 0, 0);
		case SPEEDPOTION:
			return new Attributes(0, 0, 0, 30, 0, 0, 0);
		case ATTACKINGSPEEDPOTION:
			return new Attributes(0, 0, 0, 0, 30, 0, 0);
		case HEALTHREGENERATIONPOTION:
			return new Attributes(0, 0, 0, 0, 0, 30, 0);
		default:
			return new Attributes(0, 0, 0, 0, 0, 0, 0);
		}
	}

	/**
	 * 
	 * @param itemType ItemTyp
	 * @return ItemUseType des ItemTypes
	 * @author Moritz Bittner
	 */
	public static ItemUseType getItemUseTypeForItemType(ItemType itemType) {
		return itemUseTypes.get(itemType);
	}
}
