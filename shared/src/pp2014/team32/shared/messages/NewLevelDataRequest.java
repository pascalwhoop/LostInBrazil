package pp2014.team32.shared.messages;

import pp2014.team32.shared.enums.MessageType;

/**
 * Client-zu-Server-Message
 * 
 * Bei Inkonsistenzen in den Level-Daten des Clients fordert der Client mit
 * diesem Trigger die Leveldaten komplett neu an.
 * 
 * @author Christian Hovestadt
 * @version 12.7.14
 */
public class NewLevelDataRequest extends Message {
	private static final long	serialVersionUID	= -1220766693445514365L;
	public final int			CHARACTER_ID;

	/**
	 * @param CHARACTER_ID ID des GameCharacters des Spielers (wird zur
	 *            Identifizierung des Clients im Server gesendet)
	 * @author Christian Hovestadt
	 */
	public NewLevelDataRequest(int CHARACTER_ID) {
		super(MessageType.NEWLEVELDATAREQUEST);
		this.CHARACTER_ID = CHARACTER_ID;
	}

}
