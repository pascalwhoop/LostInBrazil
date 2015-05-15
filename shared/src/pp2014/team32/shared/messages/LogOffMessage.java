package pp2014.team32.shared.messages;

import pp2014.team32.shared.enums.MessageType;

/**
 * Client-zu-Server-Message
 * 
 * Dieser Trigger wird von der Kommunikationseinheit des Clients zum Server
 * gesendet, wenn sich der Client beendet hat, damit der Server die noetigen
 * Schritte zum geordneten Logout vornehmen kann.
 * 
 * @author Pascal Brokmeier
 * @version 17.06.14
 */
public class LogOffMessage extends Message {

	private static final long	serialVersionUID	= -978735269989942235L;

	/**
	 * Diese Message enthaelt keine Werte, ist also nur ein Trigger
	 * 
	 * @author Pascal Brokmeier
	 */
	public LogOffMessage() {
		super(MessageType.LOGOFFMESSAGE);
	}
}
