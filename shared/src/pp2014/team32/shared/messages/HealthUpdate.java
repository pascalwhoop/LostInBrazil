package pp2014.team32.shared.messages;

import pp2014.team32.shared.enums.MessageType;

/**
 * Server-zu-Client-Message
 * 
 * Spezialfall der AttributeChangeInfo, bei dem nur der Wert der Gesundheit
 * aktualisiert wird.
 * 
 * @author Christian Hovestadt
 * @version 28.6.14
 */
public class HealthUpdate extends Message {
	private static final long	serialVersionUID	= 1785327321480343691L;
	public final int			CREATURE_ID;
	public final int			NEW_HEALTH;

	/**
	 * @param CREATURE_ID ID der Creature, deren Gesundheitswert aktualisiert werden soll
	 * @param NEW_HEALTH Neuer Wert der Gesundheit
	 * @author Christian Hovestadt 
	 */
	public HealthUpdate(int CREATURE_ID, int NEW_HEALTH) {
		super(MessageType.HEALTHUPDATE);
		this.CREATURE_ID = CREATURE_ID;
		this.NEW_HEALTH = NEW_HEALTH;
	}
}
