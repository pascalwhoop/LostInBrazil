package pp2014.team32.shared.messages;

import pp2014.team32.shared.enums.MessageType;

/**
 * Server-zu-Client-Message
 * 
 * Der Server sendet eine Nachricht dieses Typs an den Client, wenn ein
 * MovableObject aus den Level-Daten entfernt werden soll. Zur Identifizierung
 * des MovableObjects braucht der Client nur die ID.
 * 
 * @author Christian Hovestadt
 * @version 4.7.14
 */
public class MovableObjectRemoval extends Message {
	private static final long	serialVersionUID	= -1415047421278528301L;
	public final int			MOVABLE_OBJECT_ID;

	/**
	 * @param MOVABLE_OBJECT_ID ID des zu entfernenden MovableObjects
	 * @author Christian Hovestadt
	 */
	public MovableObjectRemoval(int MOVABLE_OBJECT_ID) {
		super(MessageType.MOVABLEOBJECTREMOVAL);
		this.MOVABLE_OBJECT_ID = MOVABLE_OBJECT_ID;
	}
}
