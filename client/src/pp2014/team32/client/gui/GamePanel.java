package pp2014.team32.client.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;

import pp2014.team32.client.resources.CLevelMap;
import pp2014.team32.shared.entities.Attributes;
import pp2014.team32.shared.entities.GameCharacter;
import pp2014.team32.shared.entities.Inventory;
import pp2014.team32.shared.entities.LevelMap;
import pp2014.team32.shared.enums.CharacterType;
import pp2014.team32.shared.enums.CreatureStatusType;
import pp2014.team32.shared.enums.LevelMapType;
import pp2014.team32.shared.utils.Coordinates;

/**
 * Die Hauptebene, auf der das tatsaechliche Spiel gezeichnet wird. Es wird auf
 * Ebene 0 ueber das gesamte Fenster gestreckt.
 * Verwaltet eine CLevelMap, deren Methode <i>paint(Graphics g)</i> in
 * paintComponent aufgerufen wird. Gezeichnet wird mit DoubleBuffering, um ein
 * Flackern zu verhindern.
 * Dieses Panel ist der standardmaessige Focus-Owner.
 * 
 * @author Christian Hovestadt
 * @version 8.7.14
 */
public class GamePanel extends JPanel {

	private static final long	serialVersionUID	= -9178082299859353582L;
	private CLevelMap			levelMap;
	private Graphics			bufferGraphics;
	private Image				offscreen;
	private Dimension			dim;
	private GameCharacter		myCharacter;

	/**
	 * Erzeugt eine neues GamePanel
	 * 
	 * @param width Wird benoetigt, um eine Default-CLevelMap zu erzeugen
	 * @param height Wird benoetigt, um eine Default-CLevelMap zu erzeugen
	 * @author Christian Hovestadt
	 */
	public GamePanel(int width, int height) {
		// Setup
		this.levelMap = new CLevelMap(new LevelMap(LevelMapType.AIRPORT, 0), 0, 0, width, height);
		this.myCharacter = new GameCharacter(0, "", CharacterType.GERMAN, 0, 0, "", new Attributes(0, 0, 0, 0, 0, 0, 0), 0, 0, new Inventory(), CreatureStatusType.STANDING);

		// Focus
		this.setFocusable(true);
		this.requestFocus();
	}

	/**
	 * Initialisiert das Image und das Graphics-Object fuer das Double
	 * Buffering.
	 * 
	 * @author Christian Hovestadt
	 */
	public void init() {
		dim = this.getSize();
		setBackground(Color.BLACK);
		offscreen = this.createImage(dim.width, dim.height);
		bufferGraphics = offscreen.getGraphics();
	}

	/**
	 * Zeichnen des Levels ueber Double Buffering:
	 * Zuerst werden die FixedObjects auf offscreen gezeichnet, danach die
	 * MovableObjects. Zuletzt wird das fertige Bild ueber die gesamte Groesse
	 * des Panels gezeichnet.
	 * 
	 * @author Christian Hovestadt
	 */
	public void paintComponent(Graphics g) {
		this.levelMap.paintFixedObjects(bufferGraphics);
		this.levelMap.paintMovableObjects(bufferGraphics);

		// Draw Level
		g.drawImage(offscreen, 0, 0, null);
	}

	void setLevelMap(CLevelMap levelMap) {
		this.levelMap = (CLevelMap) levelMap;
	}

	void setGameCharacter(GameCharacter newGameCharacter) {
		this.myCharacter = newGameCharacter;
	}

	public void setInventory(Inventory inventory) {
		this.myCharacter.inventory = inventory;
	}

	/**
	 * Der Viewpoint sind die Koordinaten des Levels, die oben links in der Ecke
	 * des GamePanels gezeichnet werden.
	 * Der Viewpoint wird immer so geaendert, sodass sich der GameCharacter in
	 * der Mitte befindet. Erreicht der GameCharacter allerdings den Rand des
	 * Levels, dann laeuft er auch zum Rand des Fensters.
	 * 
	 * @param characterX x-Koordinate des GameCharacters dieses Spielers
	 * @param characterY y-Koordinate des GameCharacters dieses Spielers
	 * @param windowWidth Breite des Spielfensters (= Breite des GamePanels)
	 * @param windowHeight Hoehe des Spielfensters (= Hoehe des GamePanels)
	 * @author Christian Hovestadt
	 */
	public void updateViewpoint(int characterX, int characterY, int windowWidth, int windowHeight) {
		/*
		 * int viewpointX = levelMap.getViewpointX();
		 * int viewpointY = levelMap.getViewpointY();
		 * if (characterX - levelMap.getViewpointX() < windowWidth * 0.3) {
		 * if (characterX > windowWidth * 0.3)
		 * levelMap.setViewpoint(characterX - (int) (windowWidth * 0.3),
		 * viewpointY);
		 * } else if (characterX - levelMap.getViewpointX() > windowWidth * 0.7)
		 * levelMap.setViewpoint(characterX - (int) (windowWidth * 0.7),
		 * viewpointY);
		 * if (characterY - levelMap.getViewpointY() < windowHeight * 0.3) {
		 * if (characterY > windowHeight * 0.3)
		 * levelMap.setViewpoint(viewpointX, characterY - (int) (windowHeight *
		 * 0.3));
		 * } else if (characterY - levelMap.getViewpointY() > windowHeight *
		 * 0.7) {
		 * levelMap.setViewpoint(viewpointX, characterY - (int) (windowHeight *
		 * 0.7));
		 * }
		 */

		int viewpointX, viewpointY;
		if (characterX - windowWidth / 2 < 0)
			viewpointX = 0;
		else if (characterX + windowWidth / 2 > levelMap.getMax().x)
			viewpointX = levelMap.getMax().x - windowWidth;
		else
			viewpointX = characterX - windowWidth / 2;

		if (characterY - windowHeight / 2 < 0)
			viewpointY = 0;
		else if (characterY + windowHeight / 2 > levelMap.getMax().y)
			viewpointY = levelMap.getMax().y - windowHeight;
		else
			viewpointY = characterY - windowHeight / 2;

		levelMap.viewpoint = new Coordinates(viewpointX, viewpointY);
	}
}
