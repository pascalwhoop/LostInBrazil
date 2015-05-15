package pp2014.team32.shared.enums;

import java.util.HashMap;

import pp2014.team32.shared.utils.PropertyManager;

/**
 * ItemUseTyp definiert die Verwendbarkeit, eines Items zu vereinfachen
 * WEAPON koennen als Waffen gesetzt werden und erhoehen fuer gewoehnlich GameCharacter Angriffsstaerke
 * ARMOUR koennen als Ruestung gesetzt werden und erhoehen fuer gewoehnlich GameCharacter Verteidigung
 * HEALTH_ITEM erhoehen die Gesundheit des Benutzers
 * POTION haben eine voruebergehende Wirkung auf die GameCharacter Attributes
 * CRAFT_ITEMS koennen zum Craften benutzt werden
 * 
 * @author Moritz Bittner
 */
public enum ItemUseType {
	WEAPON, ARMOUR, HEALTH_ITEM, POTION, CRAFT_ITEM, KEY_ITEM, USELESS_ITEM;
	
	private static HashMap<ItemUseType, String> descriptions;
	
	static {
		try {
		descriptions = new HashMap<ItemUseType, String>();
		for (ItemUseType type: ItemUseType.values())
			descriptions.put(type, PropertyManager.getProperty("itemUseType." + type));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String getDescription(ItemUseType type) {
		return descriptions.get(type);
	}
}
