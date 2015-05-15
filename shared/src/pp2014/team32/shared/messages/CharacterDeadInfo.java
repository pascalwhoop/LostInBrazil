package pp2014.team32.shared.messages;

import pp2014.team32.shared.enums.MessageType;

/**
 * Server-zu-Client-Message
 * 
 * Trigger fuer den Client, dass der Spieler gestorben ist und das GameOver-Popup geoeffnet werden soll.
 * 
 * @author Christian Hovestadt
 * @version 30.6.14
 */
public class CharacterDeadInfo extends Message {
	private static final long	serialVersionUID	= -7939674192619394763L;
	
	/**
	 * @author Christian Hovestadt
	 */
	public CharacterDeadInfo() {
		super(MessageType.CHARACTERDEADINFO);
	}
}
