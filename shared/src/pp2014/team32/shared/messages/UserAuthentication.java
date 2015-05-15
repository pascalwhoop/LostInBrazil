package pp2014.team32.shared.messages;

import pp2014.team32.shared.enums.MessageType;

/**
 * Client-zu-Server-Message
 * 
 * Der Client fordert ein Login eines bestehenden Spieleraccounts an.
 * 
 * @author Mareike Fischer
 * @version 27.05.14
 */
public class UserAuthentication extends Message {

    private static final long serialVersionUID = 1610886786886758927L;
    public final String USERNAME;
    public final String PASSWORD;

    /**
     * @param USERNAME Eingegebener Benutzername
     * @param PASSWORD Eingegebenes Passwort
     * @author Mareike Fischer
     */
    public UserAuthentication(String USERNAME, String PASSWORD) {
        super(MessageType.USERAUTHENTICATION);
        this.USERNAME = USERNAME;
        this.PASSWORD = PASSWORD;
    }
}
