package pp2014.team32.shared.entities;

import pp2014.team32.shared.enums.AttributeType;
import pp2014.team32.shared.enums.ItemType;
import pp2014.team32.shared.enums.ItemUseType;
import pp2014.team32.shared.enums.UIObjectType;

/**
 * @author Christian Hovestadt
 * @author Moritz Bittner
 */
public class Item extends FixedObject {

	private static final long	serialVersionUID	= 2558682150192688810L;
	// genaue Itemart
	private final ItemType		itemType;
	// Verwendungstyp
	private ItemUseType			itemUseType;
	// Attributes mit Werten welche auf die Attribute des Charakters Einfluss
	// nehmen.
	public Attributes			attributeChanges;

	/**
	 * Erzeugt Item des uebergebenen ItemTypes
	 * 
	 * @param id
	 * @param x
	 * @param y
	 * @param itemType
	 * @author Moritz Bittner
	 */
	public Item(int id, int x, int y, ItemType itemType) {
		super(id, UIObjectType.ITEM, x, y);
		this.itemType = itemType;
		// TODO in prefs legen
		this.height = this.width = 15;
		// ItemUseTypes
		this.itemUseType = ItemType.getItemUseTypeForItemType(itemType);
		this.attributeChanges = ItemType.getAttributesForItemType(itemType);
	}

	public ItemType getItemType() {
		return itemType;
	}

	public ItemUseType getItemUseType() {
		return itemUseType;
	}

	/**
	 * Erstellt den ToolTipText, der angezeit wird, wenn der Spieler mit der
	 * Maus ueber dieses Item im Inventar faehrt.
	 * 
	 * Er besteht aus einer Beschreibung des ItemUseTypes und einer Auflistung
	 * der Attributswerte, die dieses Item bei Benutzung veraendert.
	 * 
	 * Zeilenumbrueche und Fettdrucke werden ueber HTML-Tags realisiert.
	 * 
	 * @return Erstellter TooltipText
	 * @author Christian Hovestadt
	 */
	public String getToolTipText() {
		String output = "<i>" + ItemUseType.getDescription(itemUseType) + "</i><br>";
		for (AttributeType type : AttributeType.values())
			if (attributeChanges.get(type) != 0) {
				output += AttributeType.getAttributeName(type) + ": ";
				if (attributeChanges.get(type) > 0)
					output += "<b>+" + attributeChanges.get(type) + "</b><br>";
				else
					output += "<b>" + attributeChanges.get(type) + "</b><br>";
			}
		return "<html>" + output + "</html>";
	}
}