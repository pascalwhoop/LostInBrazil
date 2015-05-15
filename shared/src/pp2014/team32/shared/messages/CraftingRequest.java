package pp2014.team32.shared.messages;

import java.util.Set;

import pp2014.team32.shared.enums.MessageType;

/**
 * Client-zu-Server-Message
 * 
 * Crafting-Anfrage des Clients
 * 
 * Die in <i>ITEM_IDS</i> hinterlegten Items sollen in den durch CraftingReceipts vorgegebenen Output gecraftet werden
 * 
 * @author Christian Hovestadt
 * @version 7.7.14
 */
public class CraftingRequest extends Message {
	private static final long	serialVersionUID	= -4180248043032571596L;
	public final Set<Integer> ITEM_IDS;
	public final int CHARACTER_ID;
	
	/**
	 * @param input IDs der Input-Items
	 * @param characterID ID des GameCharacters, der craften will
	 * @author Christian Hovestadt
	 */
	public CraftingRequest(Set<Integer> input, int characterID) {
		super(MessageType.CRAFTINGREQUEST);
		this.ITEM_IDS = input;
		this.CHARACTER_ID = characterID;
	}
}
