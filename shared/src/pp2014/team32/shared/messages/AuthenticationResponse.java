package pp2014.team32.shared.messages;

import pp2014.team32.shared.enums.MessageType;

/**
 * Server-zu-Client-Message
 * 
 * Der Server informiert den Client, ob die Authentifizierung erfolgreich war
 * (<i>success</i>) und teilt dem Client ggf. seinen Usernamen und die ID seines
 * GameCharacters mit. Bei fehlerhafter Authentifizierung wird eine
 * Fehlermeldung mitgeliefert.
 *
 * @author Christian Hovestadt
 * @version 27.05.14
 */
public class AuthenticationResponse extends Message {

	private static final long	serialVersionUID	= 9138423215840745580L;
	public final String			USERNAME;
	public final boolean		success;
	public final String			ERROR;
	public final int			CHARACTER_ID;

	/**
	 * @param USERNAME Username des Spielers bei erfolgreicher Authentifizierung
	 * @param success Authentifizierung erfolgreich/nicht erfolgreich
	 * @param ERROR Fehlermeldung bei fehlgeschlagener Authentifizierung
	 * @param CHARACTER_ID ID des GameCharacters bei erfolgreicher
	 *            Authentifizierung
	 * @author Christian Hovestadt
	 */
	public AuthenticationResponse(String USERNAME, boolean success, String ERROR, int CHARACTER_ID) {
		super(MessageType.AUTHENTICATIONRESPONSE);
		this.USERNAME = USERNAME;
		this.success = success;
		this.ERROR = ERROR;
		this.CHARACTER_ID = CHARACTER_ID;
	}
}
