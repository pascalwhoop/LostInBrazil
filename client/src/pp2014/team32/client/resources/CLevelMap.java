package pp2014.team32.client.resources;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import pp2014.team32.shared.entities.Enemy;
import pp2014.team32.shared.entities.FixedObject;
import pp2014.team32.shared.entities.GameCharacter;
import pp2014.team32.shared.entities.Item;
import pp2014.team32.shared.entities.LevelMap;
import pp2014.team32.shared.entities.MovableObject;
import pp2014.team32.shared.entities.Taxi;
import pp2014.team32.shared.entities.Wall;
import pp2014.team32.shared.enums.AttributeType;
import pp2014.team32.shared.enums.CharacterType;
import pp2014.team32.shared.enums.EnemyType;
import pp2014.team32.shared.enums.ItemType;
import pp2014.team32.shared.enums.LevelMapType;
import pp2014.team32.shared.enums.UIObjectType;
import pp2014.team32.shared.utils.Coordinates;
import pp2014.team32.shared.utils.PropertyManager;

/**
 * Wrapperklasse fuer LevelMap
 * Erweitert die Klasse um Methoden zur Zeichnung der LevelMap
 * 
 * @author Christian Hovestadt
 * @version 8.7.2014
 */
public class CLevelMap {

	private static HashMap<LevelMapType, HashMap<Integer, BufferedImage>>	wallImages;
	private static HashMap<LevelMapType, BufferedImage>						groundImages;
	private static HashMap<ItemType, BufferedImage>							itemImages;
	private static HashMap<CharacterType, CreatureAnimationSet>				characterImages;
	private static HashMap<EnemyType, CreatureAnimationSet>					enemyImages;
	private static HashMap<Boolean, Image>									taxiImages;
	private static BufferedImage											airportBackground, stadiumImage;
	private static Logger													LOGGER;
	private static final int												SCALE_FACTOR, MAX_WALL_INDEX, LIGHT_RANGE, LIGHT_TRANSITION_RANGE;
	private static final int												LIFEBAR_HEIGHT	= 5;
	private static final Font												TAXI_FONT;
	public Coordinates														viewpoint;
	private LevelMap														levelMap;
	private Set<Bullet>														bullets;
	private Set<GameCharacter>												torchCharacters;
	private final int														screenWidth, screenHeight;
	private Image															darkness;
	private Graphics2D														darknessGraphics;

	/**
	 * Alle zur Zeichnung benoetigten Bilder werden einmalig bei der ersten
	 * Benutzung der Klasse eingeladen. So werden die laufzeitkritischen
	 * Einleseoperationen auf das Minimum reduziert.
	 * 
	 * Bilder, von denenen es mehrere Varianten gibt, werden in HashMaps (mit
	 * dem entsprechenden Enum als Key) abgelegt. So wird ein schneller Zugriff
	 * ermoeglicht und der Code bleibt trotzdem flexibel bei Veraenderungen der
	 * Enums.
	 * 
	 * Fuer Creatures werden komplette CreatureAnimationSets eingeladen.
	 * 
	 * @author Christian Hovestadt
	 */
	static {

		LOGGER = Logger.getLogger(CLevelMap.class.getName());
		SCALE_FACTOR = Integer.parseInt(PropertyManager.getProperty("levgen.scaleFactor"));
		MAX_WALL_INDEX = Integer.parseInt(PropertyManager.getProperty("wall.maxWallIndex"));
		LIGHT_RANGE = Integer.parseInt(PropertyManager.getProperty("player.lightRange"));
		LIGHT_TRANSITION_RANGE = LIGHT_RANGE + Integer.parseInt(PropertyManager.getProperty("player.lightTransitionRange"));
		TAXI_FONT = new Font("Arial", Font.PLAIN, 12);

		wallImages = new HashMap<LevelMapType, HashMap<Integer, BufferedImage>>();
		groundImages = new HashMap<LevelMapType, BufferedImage>();
		characterImages = new HashMap<CharacterType, CreatureAnimationSet>();
		itemImages = new HashMap<ItemType, BufferedImage>();
		enemyImages = new HashMap<EnemyType, CreatureAnimationSet>();
		taxiImages = new HashMap<Boolean, Image>();

		// Load Walls and Grounds
		for (LevelMapType type : LevelMapType.values()) {
			wallImages.put(type, new HashMap<Integer, BufferedImage>());
			try {
				for (int i = 1; i <= MAX_WALL_INDEX; i++)
					wallImages.get(type).put(i, ImageIO.read(new File(PropertyManager.getProperty("paths.wallPath") + i + "_" + type.toString().toLowerCase() + ".png")));
				groundImages.put(type, ImageIO.read(new File(PropertyManager.getProperty("paths.groundPath") + "_" + type.toString().toLowerCase() + ".png")));
			} catch (IOException e) {
				if (type != LevelMapType.AIRPORT)
					LOGGER.warning("Image for LevelMapType '" + type + "' was not found.");
			}
		}

		// Load CharacterImages
		for (CharacterType type : CharacterType.values())
			characterImages.put(type, new CreatureAnimationSet(type.toString(), PropertyManager.getProperty("paths.characterImagePath") + type.toString().toLowerCase() + "/"
					+ type.toString().toLowerCase() + "_"));

		// Load ItemImages
		for (ItemType item : ItemType.values())
			try {
				itemImages.put(item, ImageIO.read(new File(PropertyManager.getProperty("paths.itemImagePath") + item.toString().toLowerCase() + ".png")));
			} catch (IOException e) {
				LOGGER.warning("Image for ItemType '" + item + "' was not found.");
			}

		// Load EnemyImages
		for (EnemyType type : EnemyType.values())
			enemyImages.put(type, new CreatureAnimationSet(type.toString(), PropertyManager.getProperty("paths.enemyImagePath") + type.toString().toLowerCase() + "/" + type.toString().toLowerCase()
					+ "_"));

		// Load TaxiImages
		if (new File(PropertyManager.getProperty("paths.taxiImagePath") + "taxi.gif").exists() && new File(PropertyManager.getProperty("paths.taxiImagePath") + "taxi_locked.gif").exists()) {
			taxiImages.put(true, Toolkit.getDefaultToolkit().createImage(PropertyManager.getProperty("paths.taxiImagePath") + "taxi.gif"));
			taxiImages.put(false, Toolkit.getDefaultToolkit().createImage(PropertyManager.getProperty("paths.taxiImagePath") + "taxi_locked.gif"));
		} else
			LOGGER.warning("Taxi Images were not found both.");

		// Load Airport Background
		try {
			airportBackground = ImageIO.read(new File(PropertyManager.getProperty("paths.airportBackgroundPath")));
		} catch (IOException e1) {
			LOGGER.warning("Airport Background Image was not found.");
		}

		// Load Stadium
		try {
			stadiumImage = ImageIO.read(new File(PropertyManager.getProperty("paths.stadiumImage")));
		} catch (IOException e) {
			LOGGER.warning("Stadium Image was not found.");
		}
	}

	public CLevelMap(LevelMap levelMap, int viewpointX, int viewpointY, int screenWidth, int screenHeight) {
		super();
		this.viewpoint = new Coordinates(viewpointX, viewpointY);
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
		this.levelMap = levelMap;
		this.bullets = new HashSet<Bullet>();
		this.torchCharacters = new HashSet<GameCharacter>();
		this.darkness = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_ARGB);
		this.darknessGraphics = (Graphics2D) darkness.getGraphics();
	}

	/**
	 * Zeichnet alle FixedObjects, die innerhalb der aktuellen Ansicht liegen.\\
	 * Alle FixedObjects haben die Groesse SCALE_FACTOR\\
	 * Wenn kein FixedObject an einer Koordinate gefunden wird, wird das Bild
	 * fuer den Boden gezeichnet.\\
	 * Im Flughafen-Level wird nur ein Hintergrundbild gezeichnet.\\
	 * Zeichnet das Stadion an die Position, die in der Level-Map vorgegeben
	 * ist.\\
	 * 
	 * @param g Graphics-Object des Panels oder BufferedImages, auf dem
	 *            gezeichnet werden soll.
	 * @author Christian Hovestadt
	 */
	public void paintFixedObjects(Graphics g) {
		// Draw FixedObjects
		int leftX = viewpoint.x - viewpoint.x % SCALE_FACTOR;
		int rightX = ((viewpoint.x + screenWidth) / SCALE_FACTOR + 1) * SCALE_FACTOR;
		int topY = viewpoint.y - viewpoint.y % SCALE_FACTOR;
		int bottomY = ((viewpoint.y + screenHeight) / SCALE_FACTOR + 1) * SCALE_FACTOR;

		if (levelMap.getLevelMapType() == LevelMapType.AIRPORT) {
			// In the Airport-Level, there is just a background image
			g.drawImage(airportBackground, -viewpoint.x, -viewpoint.y, null);
			for (int x = leftX; x <= rightX; x += SCALE_FACTOR)
				for (int y = topY; y <= bottomY; y += SCALE_FACTOR) {
					FixedObject o = levelMap.getFixedObject(x, y);
					if (o != null && o.getTYPE() == UIObjectType.ITEM)
						g.drawImage(itemImages.get(((Item) o).getItemType()), x - viewpoint.x, y - viewpoint.y, null);
				}
		} else {

			for (int x = leftX; x <= rightX; x += SCALE_FACTOR)
				for (int y = topY; y <= bottomY; y += SCALE_FACTOR) {
					FixedObject o = levelMap.getFixedObject(x, y);
					if (o == null)
						// No Element in the HashMap --> GROUND
						g.drawImage(groundImages.get(levelMap.getLevelMapType()), x - viewpoint.x, y - viewpoint.y, null);
					else
						switch (o.getTYPE()) {
						case WALL:
							g.drawImage(wallImages.get(levelMap.getLevelMapType()).get(((Wall) o).getWallIndex()), x - viewpoint.x, y - viewpoint.y, null);
							break;
						case ITEM:
							g.drawImage(groundImages.get(levelMap.getLevelMapType()), x - viewpoint.x, y - viewpoint.y, null);
							g.drawImage(itemImages.get(((Item) o).getItemType()), x - viewpoint.x, y - viewpoint.y, null);
							break;
						default:
							LOGGER.warning("Fixed Objects of type " + o.getTYPE() + " can not be drawn.");
							break;
						}
				}

			// Stadium
			if (levelMap.stadium.x + 500 - viewpoint.x > 0 && levelMap.stadium.x - viewpoint.x < screenWidth && levelMap.stadium.y + 500 - viewpoint.y > 0
					&& levelMap.stadium.y - viewpoint.y < screenHeight)
				g.drawImage(stadiumImage, levelMap.stadium.x - viewpoint.x, levelMap.stadium.y - viewpoint.y, null);
		}
	}

	/**
	 * Zeichnet alle MovableObjects, die (mindestens teilweise) im angezeigten
	 * Bereich liegen.\\
	 * Creatures werden in Abhaengigkeit von ihrem
	 * <i>CreatureStatusType</i>gezeichnet.\\
	 * Zeichnet bei Creatures zusaetzlich eine Leiste, in der ihr aktuelles
	 * Leben angezeigt wird.\\
	 * Zusaetzlich werden alle Bullets gezeichnet, da diese nur clientseitig
	 * vorgehalten werden.\\
	 * In Nachtleveln das Bild verdunkelt und es wird ein Lichtkegel um
	 * Characters, die eine Fackel tragen, herumgezeichnet.\\
	 * 
	 * @param g Graphics-Object des Panels oder BufferedImages, auf dem
	 *            gezeichnet werden soll.\\
	 */
	public void paintMovableObjects(Graphics g) {

		// Draw MovableObjects
		for (MovableObject o : levelMap.getMovableObjects().values())
			if (o.getX() + o.getHeight() - viewpoint.x > 0 && o.getX() - viewpoint.x < screenWidth && o.getY() + o.getHeight() - viewpoint.y > 0 && o.getY() - viewpoint.y < screenHeight)
				switch (o.getTYPE()) {
				case ENEMY:
					Enemy e = (Enemy) o;
					g.drawImage(enemyImages.get(e.getEnemyType()).getAnimation(e.status), e.getX() - viewpoint.x, e.getY() - viewpoint.y, null);
					g.setColor(new Color(200, 0, 0));
					g.fillRect(e.getX() - viewpoint.x, e.getY() - viewpoint.y - LIFEBAR_HEIGHT - 2, e.getWidth() * e.getAttributes().get(AttributeType.HEALTH) / 100, LIFEBAR_HEIGHT);
					g.setColor(Color.BLACK);
					g.drawRect(e.getX() - viewpoint.x, e.getY() - viewpoint.y - LIFEBAR_HEIGHT - 2, e.getWidth(), LIFEBAR_HEIGHT);
					break;
				case CHARACTER:
					GameCharacter c = (GameCharacter) o;
					g.drawImage(characterImages.get(c.characterType).getAnimation(c.status), o.getX() - viewpoint.x, o.getY() - viewpoint.y, null);
					g.setColor(new Color(0, 200, 0));
					g.fillRect(c.getX() - viewpoint.x, c.getY() - viewpoint.y - LIFEBAR_HEIGHT - 2, c.getWidth() * c.getAttributes().get(AttributeType.HEALTH) / 100, LIFEBAR_HEIGHT);
					g.setColor(Color.BLACK);
					g.drawRect(c.getX() - viewpoint.x, c.getY() - viewpoint.y - LIFEBAR_HEIGHT - 2, c.getWidth(), LIFEBAR_HEIGHT);
					break;
				case TAXI:
					Taxi t = (Taxi) o;
					g.drawImage(taxiImages.get(t.isUnlocked()), t.getX() - viewpoint.x, t.getY() - viewpoint.y, null);
					g.setColor(Color.YELLOW);
					g.setFont(TAXI_FONT);
					g.drawString(t.getLabel(), t.getCenteredX() - g.getFontMetrics().stringWidth(t.getLabel()) / 2 - viewpoint.x, t.getY() - TAXI_FONT.getSize() - 2 - viewpoint.y);
					break;
				default:
					LOGGER.warning("Movable Objects of type " + o.getTYPE() + " can not be drawn.");
					break;
				}

		// Bullets
		for (Bullet b : bullets) {
			boolean destroy = b.updateAndPaint(g, viewpoint.x, viewpoint.y);
			if (destroy)
				// Bullet has reached it's target
				bullets.remove(b);
		}

		// Darkness
		if (levelMap.isNightLevel()) {
			darknessGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
			darknessGraphics.setColor(new Color(0, 0, 0, 220));
			darknessGraphics.fillRect(0, 0, screenWidth, screenHeight);
			for (GameCharacter character : torchCharacters) {
				darknessGraphics.setColor(new Color(0, 0, 0, 100));
				darknessGraphics.fillOval(character.getCenteredX() - viewpoint.x - LIGHT_TRANSITION_RANGE, character.getCenteredY() - viewpoint.y - LIGHT_TRANSITION_RANGE, 2 * LIGHT_TRANSITION_RANGE,
						2 * LIGHT_TRANSITION_RANGE);
			}

			for (GameCharacter character : torchCharacters) {
				darknessGraphics.setColor(new Color(0, 0, 0, 0));
				darknessGraphics.fillOval(character.getCenteredX() - viewpoint.x - LIGHT_RANGE, character.getCenteredY() - viewpoint.y - LIGHT_RANGE, 2 * LIGHT_RANGE, 2 * LIGHT_RANGE);
			}
			g.drawImage(darkness, 0, 0, null);
		}

		/*
		 * Old darkness code
		 * 
		 * if (myCharacter.getWeaponItem() != null &&
		 * myCharacter.getWeaponItem().getItemType() == ItemType.TORCH) {
		 * // Freiraum um den Character
		 * int x2 = myCharacter.getCenteredX() - viewpoint.x - LIGHT_RANGE,
		 * x3 = myCharacter.getCenteredX() - viewpoint.x + LIGHT_RANGE;
		 * int y2 = myCharacter.getCenteredY() - viewpoint.y - LIGHT_RANGE,
		 * y3 = myCharacter.getCenteredY() - viewpoint.y + LIGHT_RANGE;
		 * int x1 = x2 - LIGHT_TRANSITION_RANGE, x4 = x3 +
		 * LIGHT_TRANSITION_RANGE;
		 * int y1 = y2 - LIGHT_TRANSITION_RANGE, y4 = y3 +
		 * LIGHT_TRANSITION_RANGE;
		 * 
		 * // "Aeussere Dunkelheit"
		 * g.setColor(new Color(0, 0, 0, 200));
		 * g.fillRect(0, 0, x1, screenHeight);
		 * g.fillRect(x4, 0, screenWidth - x4, screenHeight);
		 * g.fillRect(x1, 0, x4 - x1, y1);
		 * g.fillRect(x1, y4, x4 - x1, screenHeight - y4);
		 * 
		 * // "Innere Dunkelheit"
		 * g.setColor(new Color(0, 0, 0, 100));
		 * g.fillRect(x1, y1, x2 - x1, y4 - y1);
		 * g.fillRect(x3, y1, x4 - x3, y4 - y1);
		 * g.fillRect(x2, y1, x3 - x2, y2 - y1);
		 * g.fillRect(x2, y3, x3 - x2, y4 - y3);
		 * } else {
		 * g.setColor(new Color(0, 0, 0, 220));
		 * g.fillRect(0, 0, screenWidth, screenHeight);
		 * }
		 */

	}

	public HashMap<Coordinates, FixedObject> getFixedObjects() {
		return levelMap.getFixedObjects();
	}

	public HashMap<Integer, MovableObject> getMovableObjects() {
		return levelMap.getMovableObjects();
	}

	public void addFixedObject(FixedObject fO) {
		levelMap.addFixedObject(fO);
	}

	public void addMovableObject(MovableObject mO) {
		levelMap.addMovableObject(mO);
	}

	public void addBullet(Bullet b) {
		bullets.add(b);
	}

	public void addTorchCharacter(int characterID) {
		if (levelMap.getMovableObjects().get(characterID) instanceof GameCharacter)
			torchCharacters.add((GameCharacter) levelMap.getMovableObjects().get(characterID));
	}

	public void removeTorchCharacter(int characterID) {
		if (levelMap.getMovableObjects().get(characterID) instanceof GameCharacter)
			torchCharacters.remove((GameCharacter) levelMap.getMovableObjects().get(characterID));
	}

	public void removeFixedObject(int x, int y) {
		levelMap.removeFixedObject(x, y);
	}

	public void removeMovableObject(int index) {
		levelMap.removeMovableObject(index);
	}

	public Coordinates getMax() {
		return levelMap.max;
	}
}
