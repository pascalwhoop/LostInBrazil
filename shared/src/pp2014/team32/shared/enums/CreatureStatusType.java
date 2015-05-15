package pp2014.team32.shared.enums;
/**
 * EnumType beschreibt die Ausrichtung einer Creature
 * @author Moritz Bittner
 * @author Christian Hovestadt
 *
 */
public enum CreatureStatusType {
	SLEEPING, STANDING, MOVING_NORTH, MOVING_NORTHEAST, MOVING_EAST, MOVING_SOUTHEAST, MOVING_SOUTH, MOVING_SOUTHWEST, MOVING_WEST, MOVING_NORTHWEST;

	/**
	 * Gibt entsprechend der uebergebenen durch x und y-Wert definierten Richtung den CreatureStatusType zurueck
	 * @param xDirection
	 * @param yDirection
	 * @return CreatureStatusType: Ausrichtungstyp des GameCharacters
	 * @author Moritz Bittner
	 */
	public static CreatureStatusType getMovingType(int xDirection, int yDirection) {
		if (xDirection == 0) {
			if (yDirection == 0)
				return STANDING;
			else if (yDirection == 1)
				return MOVING_SOUTH;
			else
				return MOVING_NORTH;
		} else if (xDirection == 1) {
			if (yDirection == 0)
				return MOVING_EAST;
			else if (yDirection == 1)
				return MOVING_SOUTHEAST;
			else
				return MOVING_NORTHEAST;
		} else {
			if (yDirection == 0)
				return MOVING_WEST;
			else if (yDirection == 1)
				return MOVING_SOUTHWEST;
			else
				return MOVING_NORTHWEST;
		}
	}
	/**
	 * Gibt die entgegengesetzte Ausrichtung zurueck
	 * @param intendedMovement Ausrichtung
	 * @return entgegengesetzte Ausrichtung
	 * @author Moritz Bittner
	 */
	public static CreatureStatusType getOppositeMovingType(CreatureStatusType intendedMovement) {
		
		switch (intendedMovement) {
		case MOVING_NORTH:
			return CreatureStatusType.MOVING_SOUTH;
		case MOVING_NORTHEAST:
			return CreatureStatusType.MOVING_SOUTHWEST;
		case MOVING_EAST:
			return CreatureStatusType.MOVING_WEST;
		case MOVING_SOUTHEAST:
			return CreatureStatusType.MOVING_NORTHWEST;
		case MOVING_SOUTH:
			return CreatureStatusType.MOVING_NORTH;
		case MOVING_SOUTHWEST:
			return CreatureStatusType.MOVING_NORTHEAST;
		case MOVING_WEST:
			return CreatureStatusType.MOVING_EAST;
		case MOVING_NORTHWEST:
			return CreatureStatusType.MOVING_SOUTHEAST;			
		default:
			return STANDING; 
		}
		
	}
}
