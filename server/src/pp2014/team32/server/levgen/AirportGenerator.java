package pp2014.team32.server.levgen;

import pp2014.team32.shared.enums.UIObjectType;
import pp2014.team32.shared.utils.PropertyManager;

/**
 * Dieser Levelgenerator erstellt das Flughafen-Level.
 * Das Flughafen-Level wird im Client als statisches Hintergrundbild gezeichnet.
 * Auf dem Server
 *
 * @author Christian Hovestadt
 * @version 01.07.14
 */
public class AirportGenerator {

	private static final int		AIRPORT_MAP_SIZE	= Integer.parseInt(PropertyManager.getProperty("levgen.airportSize"));
	private static UIObjectType[][]	airportMap			= new UIObjectType[AIRPORT_MAP_SIZE][AIRPORT_MAP_SIZE];

	/**
	 * Statische Generierung des Airport-Levels
	 * 
	 * @return LevelMap als zweidimensionales Array aus UIObjectTypes
	 * @author Christian Hovestadt
	 */
	public static UIObjectType[][] generateAirportMap() {
		initAirportLevel();
		
		verticalWall(4, 10, 27);
		verticalWall(11, 10, 16);
		verticalWall(33, 16, 27);

		horizontalWall(5, 10, 10);
		horizontalWall(12, 32, 16);
		horizontalWall(16, 22, 18);
		horizontalWall(16, 22, 19);
		horizontalWall(16, 22, 20);
		horizontalWall(5, 16, 27);
		horizontalWall(22, 32, 27);

		return airportMap;
	}

	/**
	 * Setzt WALL bzw. FLOOR an die entsprechenden Stellen
	 * @author Brokmeier, Pascal
	 */
	private static void initAirportLevel() {
		// place rock everywhere
		for (int i = 0; i < AIRPORT_MAP_SIZE; i++) {
			for (int j = 0; j < AIRPORT_MAP_SIZE; j++) {
				airportMap[i][j] = UIObjectType.WALL;
			}
		}
		// place floor everywhere except borders
		for (int i = 1; i < AIRPORT_MAP_SIZE - 1; i++) {
			for (int j = 1; j < AIRPORT_MAP_SIZE - 1; j++) {
				airportMap[i][j] = UIObjectType.FLOOR;
			}
		}

		/*
		 * for (int i = 0; i < AIRPORT_MAP_SIZE; i++) {
		 * for (int j = 0; j < AIRPORT_MAP_SIZE; j++) {
		 * airportMap[i][j] = UIObjectType.FLOOR;
		 * }
		 * }
		 */

	}

	/**
	 * Erstellt eine horizontale Wand
	 * @param startX
	 * @param endX
	 * @param y
	 * @author Christian Hovestadt
	 */
	private static void horizontalWall(int startX, int endX, int y) {
		for (int x = startX; x <= endX; x++)
			airportMap[x][y] = UIObjectType.WALL;
	}
	
	/**
	 * Erstellt eine vertiklae Wand
	 * @param x
	 * @param startY
	 * @param endY
	 * @author Christian Hovestadt
	 */
	private static void verticalWall(int x, int startY, int endY) {
		for (int y = startY; y <= endY; y++)
			airportMap[x][y] = UIObjectType.WALL;
	}
}
