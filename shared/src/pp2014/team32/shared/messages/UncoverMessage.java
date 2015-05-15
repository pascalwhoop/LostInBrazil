package pp2014.team32.shared.messages;

import java.util.Set;

import pp2014.team32.shared.enums.MessageType;
import pp2014.team32.shared.utils.Coordinates;

/**
 * Server-zu-Client-Message
 * 
 * Der Server weist den Client mit dieser Message an, dass ein neuer Bereich
 * (Set von <i>Coordinates</i>) der MiniMap aufgedeckt werden soll.
 * 
 * @author Christian Hovestadt
 * @version 30.6.14
 */
public class UncoverMessage extends Message {
	private static final long		serialVersionUID	= -3561487340012951037L;
	public final Set<Coordinates>	COORDINATES;

	/**
	 * @param COORDINATES Felder, die aufgedeckt werden sollen
	 * @author Christian Hovestadt
	 */
	public UncoverMessage(Set<Coordinates> COORDINATES) {
		super(MessageType.UNCOVERMESSAGE);
		this.COORDINATES = COORDINATES;
	}
}
