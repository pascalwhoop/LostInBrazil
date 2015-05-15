package pp2014.team32.shared.messages;

import pp2014.team32.shared.enums.ActionType;
import pp2014.team32.shared.enums.MessageType;

/**
 * @author Christian Hovestadt
 * @version 25.05.14
 * 
 * 			Client-zu-Server-Message
 * 
 *          Eine Nachricht vom Client an den Server welche eine Interaktion
 *          anfragt. Wenn dies in der Spielelogik zugelassen ist, verschickt der
 *          Server eine Info an alle Clients, dass diese Interaktion geschehen
 *          soll und die Clients muessen diese dementsprechend zeichen.
 */
public class InventoryActionRequest extends Message {

	private static final long	serialVersionUID	= 1573277338215503789L;
	public final int			INTERACTION_SOURCE_ID;
	public final int			INTERACTION_DESTINATION_ID;
	public final ActionType		ACTION;

	/**
	 * @param INTERACTION_SOURCE_ID
	 *            GameCharcterID
	 * @param INTERACTION_DESTINATION_ID ID des Items, deren auf der eine Aktion
	 *            angewendet werden soll
	 * @param ACTION Typ der Aktion
	 * @version 7.7.14
	 */
	public InventoryActionRequest(int INTERACTION_SOURCE_ID, int INTERACTION_DESTINATION_ID, ActionType ACTION) {
		super(MessageType.INVENTORYACTIONREQUEST);

		this.INTERACTION_SOURCE_ID = INTERACTION_SOURCE_ID;
		this.INTERACTION_DESTINATION_ID = INTERACTION_DESTINATION_ID;
		this.ACTION = ACTION;
	}
}
