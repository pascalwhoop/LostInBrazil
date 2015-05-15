package pp2014.team32.shared.entities;

import pp2014.team32.shared.enums.CharacterType;
import pp2014.team32.shared.enums.CreatureStatusType;
import pp2014.team32.shared.enums.UIObjectType;
import pp2014.team32.shared.utils.Coordinates;
import pp2014.team32.shared.utils.PropertyManager;

/**
 * Diese Klasse beschreibt alle zu speichernden Eigenschaften und
 * grundsaetzlichen Faehigkeiten eines GameCharacter. Wie alle Entity Klassen
 * wurde hier auch moeglichst auf zu viel Faehigkeiten verzichtet, da vieles nur
 * direkt Server entschieden werden soll.
 * 
 * @author Moritz Bittner
 * @author Christian Hovestadt
 * 
 * @version 14-05-07
 */
public class GameCharacter extends Creature {

	private static final long	serialVersionUID	= 4824081601822682475L;
	// image Groessen (fuer alle GameCharacter gleich)
	private static final int	imageWidth;
	private static final int	imageHeight;
	// aktuelles Charakter Level
	public int					currentCharacterLevel;
	// ID der aktuellen LevelMap
	public int					currentLevelMapID;
	// ID der 
	public CharacterType		characterType;
	private String				userName;

	static {
		imageWidth = Integer.parseInt(PropertyManager.getProperty("characterWidth"));
		imageHeight = Integer.parseInt(PropertyManager.getProperty("characterHeight"));
	}

	/**
	 * Erstellen eines neuen GameCharacter Objekts mit den uebergebenen Eigenschaften
	 * @param id
	 * @param userName
	 * @param characterType
	 * @param x
	 * @param y
	 * @param name
	 * @param attributes
	 * @param currentCharacterLevel
	 * @param currentLevelMapID
	 * @param cI Inventory 
	 * @param status Bewegungsrichtung
	 * @author Christian Hovestadt
	 */
	public GameCharacter(int id, String userName, CharacterType characterType, int x, int y, String name, Attributes attributes, int currentCharacterLevel, int currentLevelMapID, Inventory cI,
			CreatureStatusType status) {
		super(id, UIObjectType.CHARACTER, x, y, name, attributes, status);
		this.currentCharacterLevel = currentCharacterLevel;
		this.currentLevelMapID = currentLevelMapID;
		this.inventory = cI;
		this.characterType = characterType;
		this.userName = userName;
		this.width = imageWidth;
		this.height = imageHeight;
	}

	public int getCurrentLevelMapID() {
		return currentLevelMapID;
	}

	public void setCurrentLevelMapID(int currentLevelMapID) {
		this.currentLevelMapID = currentLevelMapID;
	}

	public int getCurrentCharacterLevel() {
		return currentCharacterLevel;
	}

	public void upgradeCurrentCharacterLevel() {
		this.currentCharacterLevel++;
	}

	public void setWeapon(Item weaponItem) {
		this.inventory.setWeapon(weaponItem);
	}

	public Item getWeaponItem() {
		return this.inventory.getWeapon();
	}

	public Item getArmourItem() {
		return this.inventory.getArmour();
	}

	public void setArmourItem(Item armourItem) {
		this.inventory.setArmour(armourItem);
	}

	public String getUserName() {
		return userName;
	}

	public Inventory getInventory() {
		return inventory;
	}

	public CharacterType getCharacterType() {
		return this.characterType;
	}

	public void setCoordinates(Coordinates coordinates) {
		this.x = coordinates.x;
		this.y = coordinates.y;
	}

	public static int getImageHeight() {
		return imageHeight;
	}

	public static int getImageWidth() {
		return imageWidth;
	}
}
