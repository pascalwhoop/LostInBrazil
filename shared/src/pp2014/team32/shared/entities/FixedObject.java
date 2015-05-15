package pp2014.team32.shared.entities;

import pp2014.team32.shared.enums.UIObjectType;

/**
 * Oberklasse fuer alle nicht beweglichen Objekte. Keine grossen Besonderheiten.
 * 
 * @author christian
 * @version 14-05-07
 */
public abstract class FixedObject extends DrawableObject {
	
	private static final long serialVersionUID = -4799114009938237151L;
	
	/**
	 * Konsturktor fuer FixedObject
	 * @param id
	 * @param TYPE
	 * @param x
	 * @param y
	 */
	public FixedObject(int id, UIObjectType TYPE, int x, int y) {
		super(id, TYPE, x, y);

	}
}
