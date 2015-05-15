package pp2014.team32.shared.messages;

import pp2014.team32.shared.enums.MessageType;

import java.util.ArrayList;

/**
 * Fasst mehrere Nachrichten zu einer zusammen.
 * 
 * Die Kommunikationseinheit sammelt alle Nachrichten, die vom Server zum Client
 * gesendet werden und schickt die gesammelten Nachrichten 25 Mal pro Sekunde in
 * einem BulkMessagesPackage zum Client, wo es von der
 * Client-Kommunikationseinheit entpackt wird. So wird Traffic zwischen Server
 * und Client gespart.
 * 
 * @author Pascal Brokmeier
 * @version 24.06.14
 */
public class BulkMessagesPackage extends Message {

	private static final long		serialVersionUID	= 4020580197742961832L;
	public final ArrayList<Message>	MESSAGES;
	public final String				USERNAME;

	/**
	 * @param USERNAME Benutzername des Users, fuer den die BulkMessage bestimmt ist
	 * @param MESSAGES Liste der zusammengefassten Nachrichten
	 * @author Pascal Brokmeier
	 */
	public BulkMessagesPackage(String USERNAME, ArrayList<Message> MESSAGES) {
		super(MessageType.BULKMESSAGEPACKAGE);
		this.MESSAGES = MESSAGES;
		this.USERNAME = USERNAME;

	}
}
