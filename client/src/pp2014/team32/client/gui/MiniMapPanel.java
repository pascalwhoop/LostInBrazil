package pp2014.team32.client.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import pp2014.team32.shared.entities.FixedObject;
import pp2014.team32.shared.entities.LevelMap;
import pp2014.team32.shared.utils.Coordinates;
import pp2014.team32.shared.utils.PropertyManager;

/**
 * Das Panel fuer die MiniMap
 * 
 * Die MiniMap wird dauerhaft in einem BufferedImage vorgehalten, damit es nicht
 * immer neu erzeugt werden muss. Wegen dem Fog of War wird nach Erzeugung des
 * Panels nur schwarz, ueber die Methode <i>uncover</i> wird die MiniMap
 * schrittweise aufgedeckt. Wenn das Level schon (teilweise) besucht ist, werden
 * einige Werte direkt nach Erzeugung der MiniMap aufgedeckt.
 * 
 * @author Christian Hovestadt
 * @author Mareike Fischer
 * @version 7.12.14
 */

public class MiniMapPanel extends JPanel {

	private static final long	serialVersionUID	= 8044736803027670255L;
	private static Image		background;
	private static final int	SCALE_FACTOR;
	private static Logger		log					= Logger.getLogger(MessagesPanel.class.getName());
	private BufferedImage		miniMap;
	private Graphics			miniMapGraphics;
	private final int			MINI_MAP_WIDTH, MINI_MAP_HEIGHT;
	private int					characterX, characterY, levelMapWidth, levelMapHeight, blockWidth, blockHeight;
	private Color				wallColor, groundColor;
	private LevelMap			levelMap;

	/**
	 * Beim ersten Aufruf der Klasse werden Scalefactor und Hintergrundbild
	 * eingelesen.
	 * 
	 * @author Mareike Fischer
	 */
	static {
		SCALE_FACTOR = Integer.parseInt(PropertyManager.getProperty("levgen.scaleFactor"));
		try {
			background = ImageIO.read(new File("images/map.png"));
		} catch (Exception e) {
			log.log(Level.SEVERE, "Fehler: Bild nicht geladen");
		}
	}

	/**
	 * Erzeugt das BufferedImage fuer die MiniMap und legt eine Referenz auf das
	 * Graphics-Objekt an, mit dem spaeter auf das BufferedImage gezeichnet
	 * wird.
	 * 
	 * @author Mareike Fischer
	 */
	public MiniMapPanel() {
		this.MINI_MAP_WIDTH = Integer.parseInt(PropertyManager.getProperty("miniMapPanel.width")) - 30;
		this.MINI_MAP_HEIGHT = Integer.parseInt(PropertyManager.getProperty("miniMapPanel.height")) - 50;

		this.miniMap = new BufferedImage(MINI_MAP_WIDTH, MINI_MAP_HEIGHT, BufferedImage.TYPE_INT_RGB);
		this.miniMapGraphics = this.miniMap.getGraphics();
	}

	/**
	 * Methode zum Aktualisieren der Minimap,
	 * setzt die Farben fuer Untergrund und Wand je nach Leveltyp
	 * 
	 * Rechnet die Groesse eines Feldes auf der MiniMap aus
	 * 
	 * Deckt alle bereits aufgedeckten Felder der MiniMap auf.
	 * 
	 * @author Mareike Fischer
	 */
	void updateMiniMap(LevelMap levelMap) {
		this.levelMap = levelMap;
		this.levelMapWidth = levelMap.max.x;
		this.levelMapHeight = levelMap.max.y;
		this.blockWidth = SCALE_FACTOR * MINI_MAP_WIDTH / levelMapWidth;
		this.blockHeight = SCALE_FACTOR * MINI_MAP_HEIGHT / levelMapHeight;

		// Clear Mini Map
		miniMapGraphics.clearRect(0, 0, MINI_MAP_WIDTH, MINI_MAP_HEIGHT);

		// Set Color Scheme
		switch (levelMap.getLevelMapType()) {
		case JUNGLE:
			this.groundColor = new Color(192, 147, 85);
			this.wallColor = new Color(68, 164, 57);
			break;
		case FAVELAS:
			this.groundColor = new Color(238, 154, 73);
			this.wallColor = new Color(139, 131, 134);
			break;
		default:
			this.groundColor = Color.GRAY;
			this.wallColor = Color.BLACK;
			break;
		}

		// Uncover all tiles, which are already visible
		for (Coordinates c : levelMap.getVisiblePositions())
			uncover(c);
	}

	/**
	 * Aktualisiert die Position des Spielermarkers auf der MiniMap
	 * 
	 * @author Christian Hovestadt
	 */
	void updateCharacterPosition(int x, int y) {
		this.characterX = x * MINI_MAP_WIDTH / levelMapWidth - blockWidth;
		this.characterY = y * MINI_MAP_HEIGHT / levelMapHeight - blockHeight;
	}

	/**
	 * Deckt ein Feld auf der Mini-Map auf. Boden wird mit der Bodenfarbe
	 * gezeichet, Waende und Items mit der Wandfarbe, Items sind also nicht auf
	 * der MiniMap sichtbar.
	 * 
	 * @author Christian Hovestadt
	 */
	void uncover(Coordinates c) {
		FixedObject o = levelMap.getFixedObject(c.x, c.y);
		if (o == null) {
			// No Element in the HashMap --> GROUND
			miniMapGraphics.setColor(groundColor);
			miniMapGraphics.fillRect(c.x * MINI_MAP_WIDTH / levelMapWidth, c.y * MINI_MAP_HEIGHT / levelMapHeight, blockWidth, blockHeight);
		} else
			switch (o.getTYPE()) {
			case WALL:
				miniMapGraphics.setColor(wallColor);
				miniMapGraphics.fillRect(c.x * MINI_MAP_WIDTH / levelMapWidth, c.y * MINI_MAP_HEIGHT / levelMapHeight, blockWidth, blockHeight);
				break;
			case ITEM:
				miniMapGraphics.setColor(groundColor);
				miniMapGraphics.fillRect(c.x * MINI_MAP_WIDTH / levelMapWidth, c.y * MINI_MAP_HEIGHT / levelMapHeight, blockWidth, blockHeight);
				break;
			default:
				break;
			}
	}

	/**
	 * Zeichnet das Hintergrundbild, das Bild der MiniMap sowie einen Marker
	 * fuer die Spielerposition
	 * 
	 * @author Mareike Fischer
	 */
	public void paintComponent(Graphics g) {
		g.drawImage(background, 0, 0, null);
		g.drawImage(miniMap, 15, 35, null);
		g.setColor(Color.WHITE);
		g.fillRect(15 + characterX, 35 + characterY, blockWidth * 3, blockHeight * 3);
	}
}
