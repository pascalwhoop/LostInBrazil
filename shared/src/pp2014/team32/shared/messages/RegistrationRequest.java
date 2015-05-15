package pp2014.team32.shared.messages;

import pp2014.team32.shared.enums.CharacterType;
import pp2014.team32.shared.enums.MessageType;

/**
 * Client-zu-Server-Message
 * 
 * Der Client fordert mit dieser Nachricht die Registrierung eines neuen
 * Spieleraccounts an. Der Server antwortet mit einer AuthenticationResponse.
 * 
 * @author Mareike Fischer
 * @version 16.6.14
 */
public class RegistrationRequest extends Message {
	private static final long	serialVersionUID	= -7335821612287440658L;

	public final String			USERNAME;
	public final String			PASSWORD;
	public final CharacterType	CHARACTER_TYPE;

	/**
	 * @param USERNAME Benutzername des neuen Spielers
	 * @param PASSWORD Passwort des neuen Spielers
	 * @param CHARACTER_TYPE Gewaehlter Charakter-Typ des neuen Spielers
	 * @author Mareike Fischer
	 */
	public RegistrationRequest(String USERNAME, String PASSWORD, CharacterType CHARACTER_TYPE) {
		super(MessageType.REGISTRATIONREQUEST);
		this.USERNAME = USERNAME;
		this.PASSWORD = PASSWORD;
		this.CHARACTER_TYPE = CHARACTER_TYPE;
	}
}
