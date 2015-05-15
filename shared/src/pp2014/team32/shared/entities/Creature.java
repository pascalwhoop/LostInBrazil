package pp2014.team32.shared.entities;

import pp2014.team32.shared.enums.AttributeType;
import pp2014.team32.shared.enums.CreatureStatusType;
import pp2014.team32.shared.enums.UIObjectType;

/**
 * Allgemeine Creature-Klasse
 * Hiervon erben GameCharacter und Enemy
 * 
 * @author Can Dogan
 * @author Christian Hovestadt
 * @author Peter Kings
 * @author Moritz Bittner
 * 
 */
public abstract class Creature extends MovableObject {
	private static final long	serialVersionUID	= -2239170129860174996L;
	public Attributes			attributes;
	private String				name;
	public Inventory			inventory;
	public CreatureStatusType	status;
	public boolean				attackEnabled;
	private int					attackSleepTime;
	private int					healthRegenerationTime;

	// Bewegungsschrittweite zunaechst fuer alle Objekte gleich

	/**
	 * Erzeugt neues Creature Objekt mit den uebergebenen Werten
	 * 
	 * @param id
	 * @param TYPE Character oder Enemy
	 * @param x
	 * @param y
	 * @param name
	 * @param attributes Attributes Objekt
	 * @param status
	 * @author Christian Hovestadt
	 */
	public Creature(int id, UIObjectType TYPE, int x, int y, String name, Attributes attributes, CreatureStatusType status) {
		super(id, TYPE, x, y);
		this.name = name;
		this.attributes = attributes;
		this.status = status;
		this.attackEnabled = true;
		this.healthRegenerationTime = 0;
	}

	/**
	 * @return verbleibende Angriffsschlagspausierungszeit
	 * @author Moritz Bittner
	 */
	public int getAttackSleepTime() {
		return attackSleepTime;
	}

	/**
	 * @param attackSleepTime zu setzende Angriffsschlagpausierung
	 * @author Moritz Bittner
	 */
	public void setAttackSleepTime(int attackSleepTime) {
		this.attackSleepTime = attackSleepTime;
	}

	/**
	 * Senkt Angriffsschlagspausierung genau um 1
	 * 
	 * @author Moritz Bittner
	 */
	public void decreaseAttackSleepTime() {
		attackSleepTime--;
	}

	/**
	 * @return aktuelle Zeiteinheit bis zum naechsten Gesundheitsaufladungspunkt
	 * @author Moritz Bittner
	 */
	public int getHealthRegenerationTime() {
		return healthRegenerationTime;
	}

	/**
	 * @param healthRegenerationTime zu setzende Gesundheitsaufladungspause
	 * @author Moritz Bittner
	 */
	public void setHealthRegenerationTime(int healthRegenerationTime) {
		this.healthRegenerationTime = healthRegenerationTime;
	}

	/**
	 * Senkt verbleibende Gesundheitsaufladungszeit genau um 1
	 */
	public void decreaseHealthRegenerationTime() {
		healthRegenerationTime--;
	}

	/**
	 * @return Name
	 */
	public String getName() {
		return name;
	}

	/**
	 * 
	 * @return Attributes der Creature
	 */
	public Attributes getAttributes() {
		return attributes;
	}

	/**
	 * @param neu zu setzendes Attributes Objekt
	 * @author Moritz Bittner
	 */
	public void setAttributes(Attributes attributes) {
		this.attributes = attributes;
	}

	/**
	 * Alle Attribute einer Kreatur mithilfe eines Attributes-Objektes
	 * manipulieren.
	 * So koennen mehrere Attribute gleichzeitig veraendert werden.
	 * 
	 * @author Peter Kings
	 * @param attributeChanges to ADD
	 * 
	 */
	public void increaseAttributesBy(Attributes attributeChanges) {
		this.attributes.addAttributes(attributeChanges);
	}

	/**
	 * Alle Attribute einer Kreatur mithilfe eines Attributes-Objektes
	 * manipulieren.
	 * So koennen mehrere Attribute gleichzeitig veraendert werden.
	 * 
	 * @author Peter Kings
	 * @param attributeChanges to DECREASE
	 * 
	 */
	public void decreaseAttributesBy(Attributes attributeChanges) {
		this.attributes.subtractAttributes(attributeChanges);
	}

	/**
	 * Einzelnes Attribut erhoehen
	 * 
	 * @param attributeType Attribut
	 * @param increaseAmount wert der addiert wird
	 * @author Peter Kings
	 */
	public void increaseAttribute(AttributeType attributeType, int increaseAmount) {
		this.attributes.increaseValueBy(attributeType, increaseAmount);
	}

	/**
	 * 
	 * @param attributeType Attribut
	 * @return Wert des Attributs
	 * @author Peter Kings
	 */
	public int getAttributeValue(AttributeType attributeType) {
		return this.attributes.get(attributeType);
	}

	/**
	 * Setzt x und y auf uebergebene Koordinaten
	 * 
	 * @param newX
	 * @param newY
	 * @author Peter Kings
	 */
	public void move(int newX, int newY) {
		this.x = newX;
		this.y = newY;
	}

	/**
	 * Setzt x und y auf uebergebene Koordinaten und setzt den
	 * CreatureStatusType
	 * 
	 * @param newX
	 * @param newY
	 * @param status
	 * @author Moritz Bittner
	 */
	public void move(int newX, int newY, CreatureStatusType status) {
		move(newX, newY);
		this.status = status;
	}

	/**
	 * Bewegt die Creature in die entsprechende Richtung mit der uebergebenen
	 * Pixelweite
	 * 
	 * @param direction CreatureStatusType (Himmelsrichtung)
	 * @param moveIncrement Pixelschrittweite
	 * @author Moritz Bittner
	 */
	public void move(CreatureStatusType direction, int moveIncrement) {
		// diagonal wird die pixelweite entsprechend angepasst
		int diagonalMovementIncrement = (int) (moveIncrement / Math.sqrt(2));
		// je nach richtung werden x und y Koordinaten der Creature geaendert
		switch (direction) {
		case MOVING_NORTH:
			y -= moveIncrement; // movementspeed noch einzubauen
			break;
		case MOVING_NORTHEAST:
			x += diagonalMovementIncrement;
			y -= diagonalMovementIncrement;
			break;
		case MOVING_EAST:
			x += moveIncrement;
			break;
		case MOVING_SOUTHEAST:
			x += diagonalMovementIncrement;
			y += diagonalMovementIncrement;
			break;
		case MOVING_SOUTH:
			y += moveIncrement;
			break;
		case MOVING_SOUTHWEST:
			x -= diagonalMovementIncrement;
			y += diagonalMovementIncrement;
			break;
		case MOVING_WEST:
			x -= moveIncrement;
			break;
		case MOVING_NORTHWEST:
			x -= diagonalMovementIncrement;
			y -= diagonalMovementIncrement;

		default:
		}
	}
}