package pp2014.team32.shared.messages;

import pp2014.team32.shared.enums.MessageType;

/**
 * Server-zu-Client Message
 * 
 * Der Server sendet diese Nachricht an den Client, wenn er ein neues Level
 * erreicht hat und sendet das neue Level mit. Ausserdem fungiert diese
 * Nachricht als Trigger, dass der Client das Popup zur Verteilung neuer
 * Attributspunkte oeffnen soll.
 * 
 * @author Mareike Fischer
 */
public class LevelUpgrade extends Message {
	private static final long	serialVersionUID	= -2746439368001201034L;
	public final int			NEW_LEVEL;

	/**
	 * @param NEW_LEVEL Neues Level des GameCharacters
	 * @author Christian Hovestadt
	 */
	public LevelUpgrade(int NEW_LEVEL) {
		super(MessageType.LEVELUPGRADE);
		this.NEW_LEVEL = NEW_LEVEL;
	}
}
