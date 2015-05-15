package pp2014.team32.client.resources;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import pp2014.team32.shared.enums.AmmoType;
import pp2014.team32.shared.utils.Coordinates;
import pp2014.team32.shared.utils.PropertyManager;

/**
 * Ein Fernkampfwaffengeschoss
 * Der Client erhaelt einmalig eine Nachricht ueber das Hinzufuegen einer neuen
 * <i>Bullet</i>, danach werden sie nur auf Clientseite geupdated und geloescht.
 * Die <i>Bullet</i> ist das einzige Objekt, bei dem so verfahren wird.
 * 
 * @author Christian Hovestadt
 * @version 30.6.2014
 */
public class Bullet {
	private final static int						SPEED;
	private static HashMap<AmmoType, BufferedImage>	ammoImages;
	private final static Logger						LOGGER;
	private AmmoType								ammoType;
	private double									currentX, currentY, speedX, speedY;
	private int										hitTargetCounter;

	static {
		SPEED = Integer.parseInt(PropertyManager.getProperty("bullet.speed"));
		LOGGER = Logger.getLogger(Bullet.class.getName());
		ammoImages = new HashMap<AmmoType, BufferedImage>();

		for (AmmoType type : AmmoType.values())
			try {
				ammoImages.put(type, ImageIO.read(new File(PropertyManager.getProperty("paths.bulletPath") + type.toString().toLowerCase() + ".png")));
			} catch (IOException e) {
				LOGGER.warning("Image for AmmoType " + type + " was not found.");
			}
	}

	/**
	 * Erstellt eine neue Bullet.
	 * Berechnet die Geschwindigkeit pro Update in x- und y-Richtung ueber den
	 * Satz des Pythagoras und berechnet, wie oft die Bullet upgedated werden
	 * muss, bis sie ihr Ziel erreicht (<i>hitTargetCounter</i>).
	 * 
	 * @param ammoType Kugel oder Stein
	 * @param start Startkoordinaten
	 * @param dest Zielkoordinaten
	 * @author Christian Hovestadt
	 */
	public Bullet(AmmoType ammoType, Coordinates start, Coordinates dest) {
		this.ammoType = ammoType;
		this.currentX = start.x;
		this.currentY = start.y;

		int xDist = dest.x - start.x, yDist = dest.y - start.y;
		double hyp = Math.sqrt(Math.pow(xDist, 2) + Math.pow(yDist, 2));

		this.speedX = xDist * SPEED / hyp;
		this.speedY = yDist * SPEED / hyp;

		this.hitTargetCounter = (int) (hyp / SPEED) - 1;
	}

	/**
	 * Updated die Kugel (dabei werden die aktuellen Koordinaten um
	 * <i>speedX</i> und <i>speedY</i> erhoeht und zeichnet sie abhaengig vom
	 * aktuellen Viewpoint.
	 * Ueberprueft, ob die Bullet ihr Ziel erreicht hat und gibt die Antwort als
	 * Boolean zurueck. Wenn ja, wird die Bullet von der <i>CLevelMap</i>
	 * zerstoert.
	 * 
	 * @param g Graphics-Object zum Zeichnen
	 * @param viewpointX X-Koordinate des Punktes oben links im Fenster
	 * @param viewpointY Y-Koorinate des Punktes oben links im Fenster
	 * @return Hat die Bullet ihr Ziel erreicht? (true/false)
	 * @author Christian Hovestadt
	 */
	public boolean updateAndPaint(Graphics g, int viewpointX, int viewpointY) {
		// Update
		currentX += speedX;
		currentY += speedY;

		// Paint
		g.drawImage(ammoImages.get(ammoType), (int) currentX - viewpointX, (int) currentY - viewpointY, null);

		hitTargetCounter--;
		return hitTargetCounter <= 0;
	}
}
