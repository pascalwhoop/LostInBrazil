package pp2014.team32.shared.messages;

import pp2014.team32.shared.entities.Attributes;
import pp2014.team32.shared.enums.MessageType;

/**
 * Client-zu-Server-Message
 * 
 * Der Client beantragt damit die Aenderung seines Attributes-Objekts
 * 
 * @author Christian Hovestadt
 * @version 29.6.14
 */
public class AttributeUpgradeRequest extends Message {
	private static final long	serialVersionUID	= 3210856376967804344L;
	
	public final int CHARACTER_ID;
	public final Attributes ATTRIBUTE_ADDITIONS;

	/**
	 * @param CHARACTER_ID ID des GameCharacters, fuer den die Message ausgeloest wird
	 * @param ATTRIBUTE_ADDITIONS Attributes-Objekt mit den Werten, die zu den Attributen des GameCharacters <b>addiert</b> werden sollen
	 * @author Christian Hovestadt
	 */
	public AttributeUpgradeRequest(int CHARACTER_ID, Attributes ATTRIBUTE_ADDITIONS) {
		super(MessageType.ATTRIBUTEUPGRADEREQUEST);
		this.CHARACTER_ID = CHARACTER_ID;
		this.ATTRIBUTE_ADDITIONS = ATTRIBUTE_ADDITIONS;
	}
}
