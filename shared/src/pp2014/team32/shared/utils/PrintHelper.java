package pp2014.team32.shared.utils;

import pp2014.team32.shared.entities.LevelMap;
import pp2014.team32.shared.entities.MovableObject;
import pp2014.team32.shared.entities.Taxi;
import pp2014.team32.shared.enums.UIObjectType;

import java.util.List;

/**
 * Helper-Klasse, um eine Level-Map in der Konsole zu visualisierten
 * 
 * @author Can Dogan
 * @version 03.06.14
 */
public class PrintHelper {

	/**
	 * Die ueber <i>returnMapString</i> generierte Map wird sofort in der
	 * Konsole ausgegeben.
	 * 
	 * @param map
	 * @author Can Dogan
	 */
	public static void printMap(UIObjectType[][] map) {
		System.out.print(returnMapString(map));
	}

	/**
	 * Erzeugt die String-Visualisierung einer Level-Map
	 * 
	 * @param map LevelMap als zweidimensionales UIObjectType-Array
	 * @return String-Repraesentation
	 * @author Can Dogan
	 */
	public static String returnMapString(UIObjectType[][] map) {
		StringBuffer buffer = new StringBuffer();
		for (UIObjectType[] row : map) {
			for (UIObjectType cell : row) {
				switch (cell) {
				case WALL:
					buffer.append("# ");
					break;
				case FLOOR:
					buffer.append("  ");
					break;
				case OUTER_WALL:
					buffer.append("O ");
					break;
				case ENEMY:
					buffer.append("E ");
					break;
				case ITEM:
					buffer.append("I ");
					break;
				case TAXI:
					buffer.append("T ");
					break;
				case CHARACTER:
					buffer.append("C ");
					break;
				case STADIUM:
					buffer.append("S ");
					break;

				/*
				 * case PREV_STAIRS:
				 * System.out.print("#");
				 * break;
				 */
				}
			}
			buffer.append("\n");
		}
		buffer.append("\n\n\n\n");

		return buffer.toString();
	}

	/**
	 * Ausgabe der Taxis und ihrer Zusammenhaenge
	 * 
	 * @param levelTree Levelbaum, dessen Taxis ausgegeben werden sollen
	 * @author Can Dogan
	 */
	public static void printTaxisInLevelTree(List<LevelMap> levelTree) {
		for (LevelMap lm : levelTree) {
			System.out.println("++++ Now: Level ID: " + lm.getLevelID());

			for (MovableObject mo : lm.getMovableObjects().values()) {
				if (mo.TYPE == UIObjectType.TAXI) {
					Taxi taxi = (Taxi) mo;
					LevelMap destination = taxi.getDestinationLevelMap();
					String destID = "NO";
					if (destination != null) {
						destID = String.valueOf(destination.getLevelID());
					}
					System.out.println("Source: " + lm.getLevelID() + " Destination: " + destID + " direction: " + taxi.getDirection());
				}
			}
		}
	}
}
