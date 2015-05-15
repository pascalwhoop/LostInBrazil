package pp2014.team32.shared.messages;

import pp2014.team32.shared.enums.AmmoType;
import pp2014.team32.shared.enums.MessageType;
import pp2014.team32.shared.utils.Coordinates;

/**
 * Server-zu-Client-Message
 * 
 * Teilt dem Client mit, dass eine Bullet vom Typ <i>AMMO_TYPE</i> von
 * <i>START</i> zu <i>DEST</i> fliegen soll. Die Bullet wird komplett
 * clientseitig aktualisiert und geloescht, wenn sie das Ziel erreicht hat.
 * 
 * @author Christian Hovestadt
 * @version 1.7.14
 */
public class BulletAddition extends Message {

	private static final long	serialVersionUID	= -3192467498400847589L;
	public final AmmoType		AMMO_TYPE;
	public final Coordinates	START, DEST;

	/**
	 * @param ammoType Bild der Kugel, das verwendet werden soll (Stein oder
	 *            Kugel), haengt von der verwendeten Waffe ab
	 * @param start Startkoordinaten
	 * @param dest Zielkoordinaten
	 * @author Christian Hovestadt
	 */
	public BulletAddition(AmmoType ammoType, Coordinates start, Coordinates dest) {
		super(MessageType.BULLETADDITION);
		this.AMMO_TYPE = ammoType;
		this.START = start;
		this.DEST = dest;
	}
}
