package pp2014.team32.shared.entities;

import pp2014.team32.shared.enums.UIObjectType;

/**
 * Alle Objekte, die sich bewegen koennen
 * 
 * @author Can Dogan
 * @version 14-05-07
 */
public abstract class MovableObject extends DrawableObject {

	private static final long serialVersionUID = -2274660535373876465L;

	public MovableObject(int id, UIObjectType TYPE, int x, int y) {
		super(id, TYPE, x, y);
		
	}
}
