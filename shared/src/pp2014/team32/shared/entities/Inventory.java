package pp2014.team32.shared.entities;

import java.io.Serializable;

import pp2014.team32.shared.utils.PropertyManager;

/**
 * Die Klasse beschreibt das Inventar einer Creature mit seinen Eigenschaften
 * und Funktionen.
 * 
 * @author Peter Kings
 * @author Moritz Bittner
 * 
 */
public class Inventory implements Serializable {

	private static final long	serialVersionUID	= 8938963587428813081L;
	// Array der im normalen Inventar enthaltenen Items
	private Item[]				myInventoryItems;
	// Waffe
	private Item				weapon;
	// Ruestung
	private Item				armour;
	// default Inventargroesse, sonst durch uebergebenen Array bestimmt
	private final static int	INVENTORY_SIZE		= Integer.parseInt(PropertyManager.getProperty("inventorySize"));

	/**
	 * Dieser Konstruktor erstellt ein Inventar mit leeren Plaetzen
	 * 
	 * @author Peter Kings
	 */
	public Inventory() {
		this(new Item[INVENTORY_SIZE]);
	}

	/**
	 * Dieser Konstruktor erstellt ein neues Inventory mit den uebergebenen Items
	 * @param myInventoryItems
	 * @author Peter Kings
	 */
	public Inventory(Item[] myInventoryItems) {
		this.myInventoryItems = myInventoryItems;
	}

	
	/**
	 * Getter und Setter des Inventory
	 * @author Moritz Bittner
	 */
	
	public Item getItemAtIndex(int index) {
		return myInventoryItems[index];
	}

	public Item[] getInventoryItems() {
		return myInventoryItems;
	}

	public int getInventorySize() {
		return myInventoryItems.length;
	}

	public Item getWeapon() {
		return weapon;
	}
	
	public void setWeapon(Item weapon) {
		this.weapon = weapon;
	}

	public Item getArmour() {
		return armour;
	}

	public void setArmour(Item armour) {
		this.armour = armour;
	}
}
