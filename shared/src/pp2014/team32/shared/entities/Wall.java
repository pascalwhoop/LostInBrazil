package pp2014.team32.shared.entities;

import pp2014.team32.shared.enums.UIObjectType;

/**
 * Eine Wall ist ein FixedObjects welches nicht betreten werden kann. Alle Walls haben die Masse 50 * 50
 * 
 * @author Moritz Bittner
 * @author Christian Hovestadt
 * 
 */
public class Wall extends FixedObject {

	private static final long	serialVersionUID	= -3467544805804640337L;
	private final int			WALL_INDEX;

	/**
	 * Erstellt eine Wall.
	 * @param id
	 * @param x
	 * @param y
	 * @param WALL_INDEX
	 * @author Christian Hovestadt
	 */
	public Wall(int id, int x, int y, int WALL_INDEX) {
		super(id, UIObjectType.WALL, x, y);
		this.height = 50;
		this.width = 50;
		this.WALL_INDEX = WALL_INDEX;
	}
	
	/**
	 * 
	 * @return Index der Wall
	 */
	public int getWallIndex() {
		return WALL_INDEX;
	}
}
