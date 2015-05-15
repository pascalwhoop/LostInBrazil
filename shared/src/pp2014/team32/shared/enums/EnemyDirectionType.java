package pp2014.team32.shared.enums;

public enum EnemyDirectionType {
	VERTICAL, HORIZONTAL;

	public static EnemyDirectionType getEnemyDirectionType(CreatureStatusType creatureStatusType) {
	if (creatureStatusType == CreatureStatusType.MOVING_NORTH || creatureStatusType == CreatureStatusType.MOVING_SOUTH) {
		return VERTICAL;
	} else  {
		return HORIZONTAL;
	}
	}
}
