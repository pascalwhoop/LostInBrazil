package pp2014.team32.shared.messages;

import pp2014.team32.shared.enums.MessageType;

/**
 * Client-zu-Server-Message
 * 
 * Bis zu 25 Mal pro Sekunde sendet der Client eine Nachricht dieses Typs an den
 * Server, wenn der Spieler sich bewegen will. Die Richtung besteht aus einem x-
 * und einem y-Anteil, die beide die Werte -1, 0 und 1 annehmen koennen. So
 * werden 8 Bewegungsrichtungen moeglich und die Werte koennen einfach mit der
 * Geschwindigkeit multipliziert werden. Ausserdem wird ein Boolean mitgesendet,
 * ob der Spieler gerade die Angriffstaste drueckt.
 * 
 * @author Christian Hovestadt
 * @version 2.6.14
 */
public class MovementRequest extends Message {
	private static final long	serialVersionUID	= 8548037046417082963L;

	public final int			MOVABLE_OBJECT_ID, LEVEL_MAP_ID;
	// HOR_DIR und VERT_DIR koennen jeweils die Werte annehmen
	public final int			HOR_DIR, VERT_DIR;
	public final boolean		ATTACKING;

	/**
	 * @param MOVABLE_OBJECT_ID ID des MovableObjects, das sich bewegen will (Im
	 *            Normalfall der GameCharacter des Spielers)
	 * @param LEVEL_MAP_ID ID des Levels, in dem sich das MovableObject befindet
	 * @param HOR_DIR x-Anteil der Bewegungsrichtung (-1, 0 oder 1)
	 * @param VERT_DIR y-Anteil der Bewegungsrichtung (-1, 0 oder 1)
	 * @param ATTACKING
	 * @author Christian Hovestadt
	 */
	public MovementRequest(int MOVABLE_OBJECT_ID, int LEVEL_MAP_ID, int HOR_DIR, int VERT_DIR, boolean ATTACKING) {
		super(MessageType.MOVEMENTREQUEST);

		this.MOVABLE_OBJECT_ID = MOVABLE_OBJECT_ID;
		this.LEVEL_MAP_ID = LEVEL_MAP_ID;
		this.HOR_DIR = HOR_DIR;
		this.VERT_DIR = VERT_DIR;
		this.ATTACKING = ATTACKING;
	}

	/**
	 * String-Repraesentation dieser Nachricht zu Debug-Zwecken
	 * @author Pascal Brokmeier
	 */
	public String toString() {
		return new StringBuffer().append("Object: ").append(MOVABLE_OBJECT_ID).append("\nDir: ").append(HOR_DIR).append(":").append(VERT_DIR).toString();

	}
}
