package pp2014.team32.shared.messages;

import pp2014.team32.shared.enums.MessageType;

/**
 * Server-zu-Client-Message
 * 
 * Ein FixedObject soll aus den Leveldaten des Clients geloescht werden
 * Von den Koordinaten des FixedObjects kann direkt auf das FixedObject ueber die HashMap in <i>LevelMap</i> geschlossen werden.
 * 
 * @author Christian Hovestadt
 * @version 16.6.14
 */
public class FixedObjectRemoval extends Message {
	private static final long	serialVersionUID	= -1415047421278528301L;
	public final int FIXED_OBJECT_X, FIXED_OBJECT_Y;

	/**
	 * @param FIXED_OBJECT_X X-Koordinate des FixedObjects, das geloescht werden soll 
	 * @param FIXED_OBJECT_Y Y-Koordinate des FixedObjects, das geloescht werden soll
	 */
	public FixedObjectRemoval(int FIXED_OBJECT_X, int FIXED_OBJECT_Y) {
		super(MessageType.FIXEDOBJECTREMOVAL);
		this.FIXED_OBJECT_X = FIXED_OBJECT_X;
		this.FIXED_OBJECT_Y = FIXED_OBJECT_Y;
	}
}
