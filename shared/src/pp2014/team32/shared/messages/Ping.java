package pp2014.team32.shared.messages;

import pp2014.team32.shared.enums.MessageType;

import java.util.Date;

/**
 * Server-zu-Client-Message, Client-zu-Server-Message
 * 
 * Diese Nachricht wird regelmaessig versendet, damit Server und Client
 * ueberpruefen koennen, ob die Verbindung noch besteht.
 * 
 * @author Pascal Brokmeier
 * @version 16.06.14
 */
public class Ping extends Message {

	private static final long	serialVersionUID	= -6711788025843003288L;
	public final Date			TIMESTAMP;

	/**
	 * Der Timestamp der Ping-Nachricht ist die aktuelle Systemzeit bei
	 * Generierung der Nachricht
	 * @author Pascal Brokmeier
	 */
	public Ping() {
		super(MessageType.PING);
		this.TIMESTAMP = new Date();
	}
}
