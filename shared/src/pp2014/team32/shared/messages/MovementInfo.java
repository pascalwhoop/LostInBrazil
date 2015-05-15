package pp2014.team32.shared.messages;

import pp2014.team32.shared.enums.MessageType;
import pp2014.team32.shared.enums.CreatureStatusType;

/**
 * Server-zu-Client-Message
 * 
 * Eine Creature aendert ihre Position, also muessen alle Clients im selben
 * Level darueber informiert werden. Mit Abstand die am haeufigsten gesendete
 * Message.
 * 
 * @author Christian Hovestadt
 * @version 28.6.14
 */
public class MovementInfo extends Message {
	private static final long		serialVersionUID	= 1817240284602103697L;

	public final int				CREATURE_ID;
	public final CreatureStatusType	CREATURE_STATUS_TYPE;
	public final int				OLD_X;
	public final int				OLD_Y;
	public final int				NEW_X;
	public final int				NEW_Y;

	/**
	 * @param CREATURE_ID ID der Creature, die sich bewegt
	 * @param CREATURE_STATUS_TYPE Bewegungsrichtung der Creature
	 * @param OLD_X Alte x-Koordinate zum Konsistenzcheck
	 * @param OLD_Y Alte y-Koordinate zum Konsistenzcheck
	 * @param NEW_X Neue x-Koordinate
	 * @param NEW_Y Neue y-Koordiante
	 * @author Christian Hovestadt
	 */
	public MovementInfo(int CREATURE_ID, CreatureStatusType CREATURE_STATUS_TYPE, int OLD_X, int OLD_Y, int NEW_X, int NEW_Y) {
		super(MessageType.MOVEMENTINFO);

		this.CREATURE_ID = CREATURE_ID;
		this.CREATURE_STATUS_TYPE = CREATURE_STATUS_TYPE;
		this.OLD_X = OLD_X;
		this.OLD_Y = OLD_Y;
		this.NEW_X = NEW_X;
		this.NEW_Y = NEW_Y;
	}

	/**
	 * @deprecated Diese Message muss jetzt einen CreatureStatusType mitsenden,
	 *             damit der Client weiss, welche Animation gezeichnet werden
	 *             soll.
	 * @author Christian Hovestadt
	 */
	public MovementInfo(int CREATURE_ID, int OLD_X, int OLD_Y, int NEW_X, int NEW_Y) {
		this(CREATURE_ID, CreatureStatusType.STANDING, OLD_X, OLD_Y, NEW_X, NEW_Y);
	}
}
