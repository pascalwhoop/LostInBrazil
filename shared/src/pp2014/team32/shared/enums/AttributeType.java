package pp2014.team32.shared.enums;

import java.util.HashMap;

import pp2014.team32.shared.utils.PropertyManager;

/**
 * AttributeTypes fuer AttributeChangeInfoMessages
 * @author Moritz Bittner
 *
 */
public enum AttributeType {

	HEALTH, ATTACK_STRENGTH, DEFENSE, MOVEMENT_SPEED, ATTACK_SPEED, HEALTH_REGENERATION, EXPOINTS;
	// Attributname wird in HashMap gespeichert
	private static HashMap<AttributeType, String> attributeNames;
	
	static {
		attributeNames = new HashMap<AttributeType, String>();
		for (AttributeType type: AttributeType.values())
			attributeNames.put(type, PropertyManager.getProperty("attributeType." + type));
	}
	
	/**
	 * 
	 * @param type Attribut
	 * @return String: Attributname
	 */
	public static String getAttributeName(AttributeType type) {
		return attributeNames.get(type);
	}
}
