package pp2014.team32.shared.entities;

import pp2014.team32.shared.enums.AttributeType;
import pp2014.team32.shared.utils.PropertyManager;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;

/**
 * Diese Klasse beschreibt Attribute von Objekten der Klasse Creature
 * 
 * @author Moritz Bittner
 * @author Peter Kings
 * @author Pascal Brokmeier
 * 
 */
public class Attributes implements Serializable {

	private static final long				serialVersionUID	= -5805490128726633746L;
	// HashMap speichert Attributwerte unter AttributeType
	private HashMap<AttributeType, Integer>	attributes;
	// maximaler und
	private static int						maxAttributeValue;
	// minimaler Attributwert sind allgemein fuer alle Attribute gueltig ...
	private static int						minAttributeValue;

	// und werden aus PropertyFile gelesen
	static {
		maxAttributeValue = Integer.parseInt(PropertyManager.getProperty("maxAttributeValue"));
		minAttributeValue = Integer.parseInt(PropertyManager.getProperty("minAttributeValue"));
	}

	/**
	 * Erzeugt neues Attributes Objekt mit uebergebenen Werten
	 * 
	 * @author Peter Kings
	 * @param health
	 * @param attackStrength
	 * @param defense
	 * @param movementSpeed
	 * @param attackSpeed
	 * @param healthRegeneration
	 * @param exPoints
	 */
	public Attributes(int health, int attackStrength, int defense, int movementSpeed, int attackSpeed, int healthRegeneration, int exPoints) {
		super();
		attributes = new HashMap<AttributeType, Integer>();
		attributes.put(AttributeType.HEALTH, health);
		attributes.put(AttributeType.ATTACK_STRENGTH, attackStrength);
		attributes.put(AttributeType.DEFENSE, defense);
		attributes.put(AttributeType.MOVEMENT_SPEED, movementSpeed);
		attributes.put(AttributeType.ATTACK_SPEED, attackSpeed);
		attributes.put(AttributeType.HEALTH_REGENERATION, healthRegeneration);
		attributes.put(AttributeType.EXPOINTS, exPoints);
	}

	/**
	 * 
	 * Der Faktor beeinflusst die anderen Faktoren abgesehen von health und
	 * movementSpeed, da diese sich nicht veraendern sollen.
	 * 
	 * @param health
	 * @param attackStrength
	 * @param defense
	 * @param movementSpeed
	 * @param attackSpeed
	 * @param healthRegeneration
	 * @param exPoints
	 * @param factor
	 * @author Pascal Brokmeier
	 */
	public Attributes(int health, int attackStrength, int defense, int movementSpeed, int attackSpeed, int healthRegeneration, int exPoints, int factor) {

		this(health, attackStrength * factor, defense * factor, movementSpeed, attackSpeed * factor, healthRegeneration * factor, exPoints * factor);
	}

	/**
	 * Gibt die aktuelle Hoehe eines bestimmten AttributeType aus dem Attributes
	 * Objekt zurueck.
	 * Dabei werden Werte die hoeher als maxAttributeValue oder kleiner als
	 * minAttributeValue sind ignoriert.
	 * 
	 * @param attributeType
	 * @return Attributwerthoehe des AttributeTypes
	 * @author Moritz Bittner
	 */
	public int get(AttributeType attributeType) {
		if (attributes.get(attributeType) < minAttributeValue)
			return minAttributeValue;
		else if (attributes.get(attributeType) > maxAttributeValue)
			return maxAttributeValue;
		return attributes.get(attributeType);
	}

	/**
	 * Setzt das Attribut des AttributeTypes auf die uebergebene Hoehe
	 * @param attributeType Attribut
	 * @param newValue neuer Wert
	 * @author Moritz Bittner
	 */
	public void set(AttributeType attributeType, int newValue) {
		// sicherheitshalber werden beide Grenzen ueberprueft (da auch negative
		// Werte uebergeben werden koennten)
		attributes.put(attributeType, newValue);
		// if (newValue > maxAttributeValue && attributeType !=
		// AttributeType.EXPOINTS) {
		// attributes.put(attributeType, maxAttributeValue);
		// } else if (newValue < minAttributeValue) {
		// attributes.put(attributeType, minAttributeValue);
		// } else {
		// attributes.put(attributeType, newValue);
		// }
	}

	/**
	 * Erhoeht das Attribut um absoluten Wert
	 * 
	 * @param attributeType Attribut
	 * @param increaseAmount Wert, welcher auf bisherige Attributhoehe addiert wird
	 * @author Moritz Bittner
	 */
	public void increaseValueBy(AttributeType attributeType, int increaseAmount) {
		int sum = attributes.get(attributeType) + increaseAmount;
		this.set(attributeType, sum);
	}

	/**
	 * Setzt das Attribut auf einen prozentualen Anteil 
	 * @param attributeType Attribut 
	 * @param percentageOfOldValue prozentualer Anteil des neuen Wertes an aktuellem Wert
	 * @author Moritz Bittner
	 */
	public void setValueByPercentageOfOldValue(AttributeType attributeType, double percentageOfOldValue) {
		if (percentageOfOldValue > 0 && percentageOfOldValue < 1) {
			attributes.put(attributeType, (int) (attributes.get(attributeType) * percentageOfOldValue));
		}
	}

	/**
	 * Senkt das Attribut um den uebergebenen absoluten Wert
	 * 
	 * @param attributeType Attribut
	 * @param decreaseAmount Wert, der abgezogen wird
	 * @author Moritz Bittner
	 */
	public void decreaseValueBy(AttributeType attributeType, int decreaseAmount) {
		int dif = attributes.get(attributeType) - decreaseAmount;
		this.set(attributeType, dif);
	}

	/**
	 * 
	 * @return keySet der Attributes Hashmap
	 * @author Moritz Bittner
	 */
	public Set<AttributeType> keySet() {
		return attributes.keySet();
	}

	/**
	 * Addiert zu jedem Attribut, den im uebergebenen Attributes Object
	 * enthaltenen Wert hinzu.
	 * So koennen mehrere Attribute gleichzeitig veraendert werden. Zum Beispiel
	 * Angriffsstaerke und -geschwindigkeit
	 * 
	 * @author Moritz Bittner
	 * @param addAttributes Attribut Objekt mit den zu veraendernen Werten
	 */
	public void addAttributes(Attributes addAttributes) {
		for (AttributeType attributeType : addAttributes.keySet()) {
			increaseValueBy(attributeType, addAttributes.get(attributeType));
		}
	}

	/**
	 * Setzt Attributwerte auf die Werte des uebergebenen Attributes Objektes
	 * @param setAttributes neue Attribute deren Werte uebernommen werden sollen
	 * @author Moritz Bittner
	 */
	public void setAttributes(Attributes setAttributes) {
		for (AttributeType attributeType : setAttributes.keySet()) {
			set(attributeType, setAttributes.get(attributeType));
		}
	}

	/**
	 * Subtrahiert von jedem Attributwert den Wert aus den uebergebenen Attributes ab.
	 * Z.B. um Wirkung eines Items rueckgaengig zu machen
	 * 
	 * @author Moritz Bittner
	 * @param subtractAttributes abzuziehende Attributes
	 */
	public void subtractAttributes(Attributes subtractAttributes) {
		for (AttributeType attributeType : subtractAttributes.keySet()) {
			decreaseValueBy(attributeType, subtractAttributes.get(attributeType));
		}
	}
}
