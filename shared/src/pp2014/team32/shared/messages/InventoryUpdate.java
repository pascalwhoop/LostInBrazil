package pp2014.team32.shared.messages;

import pp2014.team32.shared.entities.Inventory;
import pp2014.team32.shared.enums.MessageType;

/**
 * Server-zu-Client-Message
 * 
 * Uebergibt dem Client ein neues Spielerinventar fuer den Character des Spielers
 * 
 * @author Mareike Fischer
 * @version 14.06.2014
 */
public class InventoryUpdate extends Message {
	
	private static final long	serialVersionUID	= -6903427023336215640L;
	public final Inventory INVENTORY;

	/**
	 * @param inventory Neues Inventar
	 * @author Mareike Fischer
	 */
	public InventoryUpdate(Inventory inventory) {
		super(MessageType.INVENTORYUPDATE);
		this.INVENTORY = inventory;
	}

}
