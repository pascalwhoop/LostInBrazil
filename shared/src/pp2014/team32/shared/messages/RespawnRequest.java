package pp2014.team32.shared.messages;

import pp2014.team32.shared.enums.MessageType;

/**
 * Client-zu-Server-Message
 * 
 * Trigger fuer den Server, dass der Client im <i>GameOverPopup</i> die Option 'Respawn' gewaehlt hat.
 * 
 * @author Christian Hovestadt
 * @version 30.6.14
 */
public class RespawnRequest extends Message {
	private static final long	serialVersionUID	= -4624277930693485162L;
	public final int CHARACTER_ID;

	/**
	 * @param CHARACTER_ID ID des GameCharacters, der respawnen soll
	 * @author Christian Hovestadt
	 */
	public RespawnRequest(int CHARACTER_ID) {
		super(MessageType.RESPAWNREQUEST);
		this.CHARACTER_ID = CHARACTER_ID;
	}
}
