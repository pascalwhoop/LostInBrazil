package pp2014.team32.shared.messages;

import pp2014.team32.shared.enums.ChatMessageType;
import pp2014.team32.shared.enums.MessageType;

import java.util.Date;

/**
 * Eine ChatMessage besteht aus Zeit, Benutzername, ChatMessageType und einem
 * Text
 * 
 * Server-zu-Client-Fall:
 * Der Client sendet die ChatMessage zum Server, damit diese an alle Clients
 * weitergesendet wird
 * 
 * Client-zu-Server-Message:
 * Der Server sendet die ChatMessage, die er erhalten hat, zum Client, damit
 * diese im MessagesPanel der Clients angezeigt werden. Alternativ sendet der
 * Server eine Systemnachricht oder Kampfnachricht an den Client.
 * 
 * @author Christian Hovestadt
 * @version 12.7.14
 */
public class ChatMessage extends Message {

	private static final long		serialVersionUID	= -1746008818683810828L;
	public final Date				TIMESTAMP;
	public final String				USERNAME;
	public final String				TEXT;
	public final ChatMessageType	CHAT_MESSAGE_TYPE;

	/**
	 * @param TIMESTAMP Zeit der Chatnachricht
	 * @param USERNAME Benutzername
	 * @param TEXT Inhalt
	 * @param CHAT_MESSAGE_TYPE Nachrichtentyp: Chatnachricht, Kampfnachricht
	 *            oder Systemnachricht
	 * @author Christian Hovestadt
	 */
	public ChatMessage(Date TIMESTAMP, String USERNAME, String TEXT, ChatMessageType CHAT_MESSAGE_TYPE) {
		super(MessageType.CHATMESSAGE);
		this.TIMESTAMP = TIMESTAMP;
		this.USERNAME = USERNAME;
		this.TEXT = TEXT;
		this.CHAT_MESSAGE_TYPE = CHAT_MESSAGE_TYPE;
	}

	/**
	 * ChatMessage wird mit der aktuellen Systemzeit erstellt.
	 * 
	 * @param USERNAME Benutzername
	 * @param TEXT Inhalt
	 * @param CHAT_MESSAGE_TYPE Nachrichtentyp: Chatnachricht, Kampfnachricht
	 *            oder Systemnachricht
	 * @author Christian Hovestadt
	 */
	public ChatMessage(String USERNAME, String TEXT, ChatMessageType CHAT_MESSAGE_TYPE) {
		super(MessageType.CHATMESSAGE);
		this.TIMESTAMP = new Date();
		this.USERNAME = USERNAME;
		this.TEXT = TEXT;
		this.CHAT_MESSAGE_TYPE = CHAT_MESSAGE_TYPE;
	}

	/**
	 * String-Repraesentation der Chatnachricht fuer Debug-Zwecke
	 * @author Pascal Brokmeier
	 */
	public String toString() {
		return new StringBuffer().append(USERNAME).append(": ").append(TEXT).append("\n@ ").append(TIMESTAMP.getTime()).toString();
	}
}
