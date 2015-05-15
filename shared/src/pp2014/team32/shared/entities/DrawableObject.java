package pp2014.team32.shared.entities;

import pp2014.team32.shared.enums.UIObjectType;
import pp2014.team32.shared.utils.Coordinates;

import java.io.Serializable;

/**
 * Allgemeine Klasse fuer alle zeichenbaren Objekte
 * 
 * @author Can
 * @author Pascal
 * @author Christian
 * @author Peter
 * @author Moritz
 */
public abstract class DrawableObject implements Serializable {

	private static final long	serialVersionUID	= -1245042888769883937L;
	protected int				x, y;
	protected int				height, width;

	public final int			ID;
	// Grafik- und Erkennungstyp
	public final UIObjectType	TYPE;

	// // Sichtbar fuer den jeweiligen client, aber vorhanden
	// public boolean visible;
	// // Voruebergehend aus dem Spiel genommen (z.B. Items mit Cooldown)
	// public boolean hidden;

	/**
	 * Erzeugt neues DrawableObject
	 * 
	 * @param id
	 * @param TYPE enumtype
	 * @param x X-Koordinate
	 * @param y Y-Koordinate
	 * @author Moritz Bittner
	 */
	public DrawableObject(int id, UIObjectType TYPE, int x, int y) {
		this.ID = id;
		this.TYPE = TYPE;
		// this.visible = true;
		// this.hidden = false;
		this.x = x;
		this.y = y;
	}

	/**
	 * 
	 * @return x-Position des DrawableObjects
	 */
	public int getX() {
		return x;
	}

	/**
	 * 
	 * @return y-Position des DrawableObjects
	 */
	public int getY() {
		return y;
	}

	/**
	 * 
	 * @return UIObjectType des DrawableObjects
	 */
	public UIObjectType getTYPE() {
		return TYPE;
	}

	/**
	 * 
	 * @return ID des DrawableObjects
	 */
	public int getID() {
		return ID;
	}

	/**
	 * 
	 * @return Hoehe des DrawableObjects
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * 
	 * @return Breite des DrawableObjects
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Methode, die die MITTIGEN Koordinaten von drawable zurueckgeben
	 * 
	 * @author Moritz Bittner
	 * @return mittige x Koordinaten
	 */
	public int getCenteredX() {
		return x + width / 2;
	}

	/**
	 * Methode, die die MITTIGEN Koordinaten von drawable zurueckgeben
	 * 
	 * @author Moritz Bittner
	 * @return mittige y Koordinaten
	 */
	public int getCenteredY() {
		return y + width / 2;
	}

	/**
	 * 
	 * @param coordinates welche fuer Drawableobjet zu setzen sind
	 */
	public void setCoordinates(Coordinates coordinates) {
		this.x = coordinates.x;
		this.y = coordinates.y;
	}
}
