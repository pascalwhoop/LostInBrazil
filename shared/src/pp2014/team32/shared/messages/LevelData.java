package pp2014.team32.shared.messages;

import pp2014.team32.shared.entities.LevelMap;
import pp2014.team32.shared.enums.MessageType;

/**
 * Server-zu-Client-Message
 * 
 * Sendet ein komplettes Level zum Client. Das im Client vorhandene Level wird
 * im Client sofort ausgetauscht. Diese Nachricht zum Einsatz, wenn der Spieler
 * das Level wechselt oder wenn der Client wegen Inkonsistenzen neue Level-Daten
 * angefordert hat.
 * 
 * @author Christian Hovestadt
 * @version 12.6.14
 */
public class LevelData extends Message {

	private static final long	serialVersionUID	= 8183629683417614549L;
	public final LevelMap		LEVEL;

	/**
	 * Initialisiert die Nachricht mit einem Objekt vom Type LevelData
	 * 
	 * @param LEVEL LevelData-Objekt, das verschickt werden soll
	 * @author Christian Hovestadt
	 */
	public LevelData(LevelMap LEVEL) {
		super(MessageType.LEVELDATA);
		this.LEVEL = LEVEL;
	}
}
