package pp2014.team32.shared.messages;

import pp2014.team32.shared.enums.MessageType;

import java.io.Serializable;

/**
 * @author Pascal Brokmeier
 * @version 25.05.14
 *          <p/>
 *          Diese Klasse ist die abstrakte Mutterklasse fuer alle unsere
 *          Nachrichtenobjekte die wir zwischen Client & Server uebermitteln
 *          wollen. Wir setzen ein Attribut "MESSAGE_TYPE" um auf
 *          Empfaengerseite effizient zu bestimmen, von welchem Objekttyp unsere
 *          Nachricht ist. Dies ist so gewaehlt, da ein Enum Vergleich
 *          effizienter ist als ein instanceof check (siehe wiki c-s
 *          Kommunikation).
 */
public abstract class Message implements Serializable {
	private static final long	serialVersionUID	= -4207218893700903204L;
	public final MessageType	MESSAGE_TYPE;

	/**
	 * Jede Message muss einen MessageType haben, um sie effizient unterscheiden
	 * zu koennen
	 * 
	 * @param MESSAGE_TYPE Message-Type
	 * @author Pascal Brokmeier
	 */
	public Message(MessageType MESSAGE_TYPE) {
		this.MESSAGE_TYPE = MESSAGE_TYPE;
	}
}
