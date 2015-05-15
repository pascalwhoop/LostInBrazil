package pp2014.team32.shared.enums;

import pp2014.team32.shared.entities.Attributes;
import pp2014.team32.shared.utils.PropertyManager;

import java.util.HashMap;
import java.util.Random;

/**
 * @author Can
 * @author Moritz
 * @author Christian
 */

public enum EnemyType {
	LION, SNAKE, TIGER, GANG_MEMBER, DOG, CAT;

	private static HashMap<EnemyType, Integer>	width	= new HashMap<>();
	private static HashMap<EnemyType, Integer>	height	= new HashMap<>();
	private static HashMap<LevelMapType, EnemyType[]> enemyTypesInLevel = new HashMap<>();
	private static int							startHealth;

	static {
		// Hoehe und Breite der Bilder
		for (EnemyType type : EnemyType.values()) {
			width.put(type, Integer.parseInt(PropertyManager.getProperty("enemy." + type + ".width")));
			height.put(type, Integer.parseInt(PropertyManager.getProperty("enemy." + type + ".height")));
		}

		// Welche Enemies treten in welchem LevelMapType auf?
		EnemyType[] enemiesAirport = {EnemyType.GANG_MEMBER};
		enemyTypesInLevel.put(LevelMapType.AIRPORT, enemiesAirport);
		EnemyType[] enemiesJungle = {EnemyType.LION, EnemyType.TIGER, EnemyType.SNAKE};
		enemyTypesInLevel.put(LevelMapType.JUNGLE, enemiesJungle);
		EnemyType[] enemiesFavelas = {EnemyType.GANG_MEMBER, EnemyType.DOG, EnemyType.CAT};
		enemyTypesInLevel.put(LevelMapType.FAVELAS, enemiesFavelas);
		
		startHealth = Integer.parseInt(PropertyManager.getProperty("startHealth"));
	}

	/**
	 * @deprecated Bitte getRandomEnemy(LevelMapType levelMapType) verwenden
	 * @return
	 */
	public static EnemyType getRandomEnemy() {
		return values()[(int) (Math.random() * values().length)];
	}
	
	public static EnemyType getRandomEnemy(LevelMapType levelMapType) {
		return enemyTypesInLevel.get(levelMapType)[new Random().nextInt(enemyTypesInLevel.get(levelMapType).length)];
	}

	public static int getEnemyWidth(EnemyType enemyType) {
		return width.get(enemyType);
	}

	public static int getEnemyHeight(EnemyType enemyType) {
		return height.get(enemyType);
	}

    /**
     * Enemy Attributes in Abhaengigkeit vom Type
     * @param enemyType
     * @param factor
     * @return
     */
	public static Attributes getEnemyAttributes(EnemyType enemyType, int factor) {
		switch (enemyType) {
		case LION:
			return new Attributes(startHealth, 2, 3, 80, 2, 0, 80, factor);
		case TIGER:
			return new Attributes(startHealth, 3, 1, 100, 2, 0, 80, factor);
		case SNAKE:
			return new Attributes(startHealth, 1, 1, 20, 1, 0, 40, factor);
		case GANG_MEMBER:
			return new Attributes(startHealth, 4, 2, 0, 1, 1, 40, factor);
		case DOG:
			return new Attributes(startHealth, 1, 1, 60, 3, 0, 40, factor);
		case CAT:
			return new Attributes(startHealth, 1, 1, 40, 3, 0, 40, factor);
		default:
			return new Attributes(startHealth, 0, 0, 0, 0, 0, 0, factor);
		}
	}

}
