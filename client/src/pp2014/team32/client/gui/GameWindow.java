package pp2014.team32.client.gui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.UIManager;

import pp2014.team32.client.comm.ServerConnection;
import pp2014.team32.client.engine.ClientControls;
import pp2014.team32.client.resources.CLevelMap;
import pp2014.team32.shared.entities.Attributes;
import pp2014.team32.shared.entities.GameCharacter;
import pp2014.team32.shared.entities.Inventory;
import pp2014.team32.shared.entities.LevelMap;
import pp2014.team32.shared.entities.MovableObject;
import pp2014.team32.shared.enums.AttributeType;
import pp2014.team32.shared.messages.ChatMessage;
import pp2014.team32.shared.messages.NewLevelDataRequest;
import pp2014.team32.shared.utils.Coordinates;
import pp2014.team32.shared.utils.PropertyManager;

/**
 * Das Spielfenster
 * 
 * @author Christian Hovestadt 
 * @author Mareike Fischer
 * @version 7.7.14
 */
public class GameWindow extends JFrame {

	private static final long	serialVersionUID		= -4207709194407076088L;
	private final static Logger	LOGGER					= Logger.getLogger(GameWindow.class.getName());
	private final static int	OUTER_BORDER			= 6;
	private final static int	REPAINTS_PER_SEC		= Integer.parseInt(PropertyManager.getProperty("gameWindow.repaintsPerSecond"));
	private final static int	WIDTH					= Integer.parseInt(PropertyManager.getProperty("gameWindow.width"));
	private final static int	HEIGHT					= Integer.parseInt(PropertyManager.getProperty("gameWindow.height"));
	private GamePanel			gamePanel;
	private MessagesPanel		messagesPanel;
	private FlagsPanel			flagsPanel;
	private CharacterPanel		characterPanel;
	private InventoryPanel		inventoryPanel;
	private MiniMapPanel		miniMapPanel;
	private ClientControls		clientControls;
	private Timer				repaintTimer;
	private int					characterID;
	private CLevelMap			levelMap;
	private JLayeredPane		layeredPane;
	private boolean				isFlagsPanelVisible		= true, isCharacterPanelVisible = true, isMiniMapPanelVisible = true;
	// 0 = hidden, 1 = standard, 2 = expanded
	private int					inventoryPanelStatus	= 1;
	private static int			inventoryPanelWidth, inventoryPanelHeight, inventoryPanelExpandedHeight, inventoryPanelExpandedWithCraftingHeight;

	/**
	 * Einige Properties werden als statische Variablen festgelegt.
	 * 
	 * @author Mareike Fischer
	 */
	static {
		inventoryPanelWidth = Integer.parseInt(PropertyManager.getProperty("inventoryPanel.width"));
		inventoryPanelHeight = Integer.parseInt(PropertyManager.getProperty("inventoryPanel.height"));
		inventoryPanelExpandedHeight = Integer.parseInt(PropertyManager.getProperty("inventoryPanel.expandedHeight"));
		inventoryPanelExpandedWithCraftingHeight = Integer.parseInt(PropertyManager.getProperty("inventoryPanel.expandedWithCraftingHeight"));
	}

	/**
	 * Das JFrame wird erzeugt und einige Einstellungen werden getroffen.
	 * 
	 * Um das modulare Layout mit Ein- und Ausblenden von einzelnen Panels zu
	 * ermoeglichen, werden die Panels mit einem <i>JLayeredPane</i> in Ebenen
	 * angeordnet. Die untere Ebene enthaelt das <i>GamePanel</i>, in dem das
	 * tatsaechliche Spiel stattfindet. Die obere Ebene enthaelt alle alle
	 * InformationsPanels: <i>MessagesPanel, FlagsPanel, CharacterPanel,
	 * InventoryPanel und MiniMapPanel</i>. Die Panels werden mit Null-Layout
	 * angeordnet, da sie aufgrund der in ihrer Groesse fixen Hintergrundbilder
	 * nicht gestreckt werden. Allerdings sind die Koordinaten trotzdem
	 * abhaengig von den Raendern des Fensters, sodass ein Aendern der
	 * Fenstergroesse einfach moelich waere.
	 * 
	 * @author Mareike Fischer
	 */
	public GameWindow() {
		// GUI
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setTitle("LOST IN BRAZIL");
		this.setResizable(false);
		this.setFocusTraversalKeysEnabled(false);

		// Initialize Panels
		this.gamePanel = new GamePanel(WIDTH, HEIGHT);
		this.messagesPanel = new MessagesPanel();
		this.flagsPanel = new FlagsPanel();
		this.characterPanel = new CharacterPanel();
		this.inventoryPanel = new InventoryPanel(this, false, false);
		this.miniMapPanel = new MiniMapPanel();

		// Set Panel-Bounds
		Dimension d = new Dimension(WIDTH, HEIGHT);
		this.gamePanel.setBounds(0, 0, d.width, d.height);
		int width = Integer.parseInt(PropertyManager.getProperty("messagesPanel.width"));
		int height = Integer.parseInt(PropertyManager.getProperty("messagesPanel.height"));
		this.messagesPanel.setBounds(OUTER_BORDER, OUTER_BORDER, width, height);
		width = Integer.parseInt(PropertyManager.getProperty("flagsPanel.flagSize")) * 2 + Integer.parseInt(PropertyManager.getProperty("flagsPanel.addWidth"));
		height = Integer.parseInt(PropertyManager.getProperty("flagsPanel.flagSize")) + Integer.parseInt(PropertyManager.getProperty("flagsPanel.addHeight"));
		this.flagsPanel.setBounds((int) d.getWidth() - OUTER_BORDER - width - 4, OUTER_BORDER, width, height);
		width = Integer.parseInt(PropertyManager.getProperty("characterPanel.width"));
		height = Integer.parseInt(PropertyManager.getProperty("characterPanel.height"));
		this.characterPanel.setBounds(OUTER_BORDER, (int) d.getHeight() - OUTER_BORDER - height, width, height);
		this.inventoryPanel.setBounds(((int) d.getWidth() - inventoryPanelWidth) / 2, (int) d.getHeight() - OUTER_BORDER - inventoryPanelHeight, inventoryPanelWidth, inventoryPanelHeight);
		width = Integer.parseInt(PropertyManager.getProperty("miniMapPanel.width"));
		height = Integer.parseInt(PropertyManager.getProperty("miniMapPanel.height"));
		this.miniMapPanel.setBounds((int) d.getWidth() - OUTER_BORDER - width, (int) d.getHeight() - OUTER_BORDER - height, width, height);

		// Create LayeredPane
		layeredPane = new JLayeredPane();
		layeredPane.setLayout(null);
		layeredPane.setPreferredSize(d);

		layeredPane.add(gamePanel);
		layeredPane.add(messagesPanel);
		layeredPane.add(flagsPanel);
		layeredPane.add(characterPanel);
		layeredPane.add(inventoryPanel);
		layeredPane.add(miniMapPanel);

		layeredPane.setLayer(gamePanel, 0);
		layeredPane.setLayer(messagesPanel, 1);
		layeredPane.setLayer(flagsPanel, 1);
		layeredPane.setLayer(characterPanel, 1);
		layeredPane.setLayer(inventoryPanel, 1);
		layeredPane.setLayer(miniMapPanel, 1);

		// Focus Listener
		this.addWindowListener(new WindowListener() {
			public void windowOpened(WindowEvent e) {
			}

			public void windowIconified(WindowEvent e) {
			}

			public void windowDeiconified(WindowEvent e) {
			}

			/**
			 * Wenn das Window seinen Fokus verliert, muessen alle aktuell
			 * gedrueckten Tasten in der ClientControls auf 'not pressed'
			 * gesetzt werden, da ClientControls das Released-Event dann nicht
			 * mehr mitbekommt.
			 * 
			 * @author Christian Hovestadt
			 */
			public void windowDeactivated(WindowEvent e) {
				clientControls.resetDirections();
			}

			public void windowClosing(WindowEvent e) {
			}

			public void windowClosed(WindowEvent e) {
			}

			public void windowActivated(WindowEvent e) {
			}
		});

		this.add(layeredPane);
		this.pack();
		this.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width) / 2 - getWidth() / 2, (Toolkit.getDefaultToolkit().getScreenSize().height) / 2 - getHeight() / 2);

		// Change JOptionPane-Buttons to German
		UIManager.put("OptionPane.yesButtonText", "Ja");
		UIManager.put("OptionPane.noButtonText", "Nein");
		UIManager.put("OptionPane.cancelButtonText", "Abbrechen");
	}

	/**
	 * Macht dieses Fenster sichtbar und startet die Repaint-Routine
	 * 
	 * @author Christian Hovestadt
	 */
	public void showWindow() {
		gamePanel.init();
		this.setVisible(true);
		startTimerTask();
	}

	/**
	 * Startet die Repaint-Routine:
	 * REPAINTS_PER_SEC Mal pro Sekunde wird die Kontorlleinheit aktualisiert
	 * und <i>repaint()</i> augefuehrt. Ausserdem holt sich das GamePanel den
	 * Fokus zurueck, wenn es ihn verloren hat. Durch die <i>TimerTask</i> wird
	 * sichergestellt, dass die Zeit zwichen den <b>Starts</b> der Ausfuehrungen
	 * immer 1000/REPAINTS_PER_SEC ms betraegt.
	 * 
	 * @author Christian Hovestadt
	 */
	public void startTimerTask() {
		// Auto Repainter
		this.repaintTimer = new Timer();
		this.repaintTimer.scheduleAtFixedRate(new TimerTask() {

			public void run() {
				gamePanel.requestFocusInWindow();
				if (clientControls != null)
					clientControls.update();
				repaint();
			}

		}, 50, 1000 / REPAINTS_PER_SEC);
	}

	/**
	 * Setzt die CharacterID und uebermittelt diese Information an alle Panels,
	 * die sie benoetigen.
	 * 
	 * @param characterID Neue characterID
	 * @param username Username des aktuellen Players
	 * @author Christian Hovestadt
	 */
	public void setCharacterID(int characterID, String username) {
		this.characterID = characterID;
		this.clientControls = new ClientControls(this, gamePanel, characterID, username);
		this.inventoryPanel.setCharacterID(characterID);
	}

	/**
	 * Erzeugt eine neue CLevelMap und setzt die Referenz darauf in allen
	 * Panels, die die CLevelMap benoetigen
	 * Identifiziert den GameCharacter anhand der gepeicherten characterID (wird
	 * in der AuthenticationResponse mitgeliefert) in der LevelMap(ohne Suche,
	 * da HashMap). Setzt den Character, die characterID, die Attributes des
	 * Characters, das Inventar und das aktuelle Level jeweils in dem Panel, das
	 * den Wert benoetigt. Der Viewpoint wird den Koordinaten des GameCharacters
	 * angepasst.
	 * 
	 * Loggt den Fall, das es keinen GameCharacter zu der uebergebenen ID gibt.
	 * 
	 * @param levelMap neue LevelMap
	 * 
	 * @author Christian Hovestadt
	 */
	public void setLevelMap(LevelMap levelMap) {
		CLevelMap cLevelMap = new CLevelMap(levelMap, 0, 0, this.getWidth(), this.getHeight());
		this.levelMap = cLevelMap;
		gamePanel.setLevelMap(cLevelMap);
		miniMapPanel.updateMiniMap(levelMap);
		flagsPanel.updateMatch(levelMap.getTeam1(), levelMap.getTeam2(), levelMap.getLevelDescription(), levelMap.getCity());
		try {
			GameCharacter character = (GameCharacter) levelMap.getMovableObjects().get(characterID);
			characterPanel.setAttributes(character.getAttributes());
			characterPanel.setLevel(character.currentCharacterLevel);
			inventoryPanel.setInventory(character.getInventory());
			gamePanel.setGameCharacter(character);
			updateViewpoint(character);
		} catch (ClassCastException | NullPointerException e) {
			LOGGER.severe("There is no GameCharacter for the given ID in MovableObjects.");
		}
		clientControls.setLevelMapID(levelMap.getLevelID());
		clientControls.resetDirections();
	}

	/**
	 * Das neue Inventar wird an die Panels weitergegeben, die es benoetigen.
	 * 
	 * @param inventory neues Inventar
	 * 
	 * @author Christian Hovestadt
	 */
	public void setInventory(Inventory inventory) {
		inventoryPanel.setInventory(inventory);
		gamePanel.setInventory(inventory);
	}

	/**
	 * Wenn die Attributaenderung den GameCharacter des Spielers betrifft,
	 * werden alle Werte im CharacterPanel aktualisiert.
	 * 
	 * @param creatureID ID der Creature, deren Attribute geaendert werden
	 * @param attributes neue Attribute
	 * @author Christian Hovestadt
	 */
	public void updateAttributes(int creatureID, Attributes attributes) {
		if (creatureID == characterID)
			characterPanel.setAttributes(attributes);
	}

	/**
	 * Wenn die Attributaenderung den GameCharacter des Spielers betrifft,
	 * wird der Wert des Attributes von <i>type</i> im CharacterPanel
	 * aktualisiert.
	 * 
	 * @param creatureID ID der Creature, deren Attribute geaendert werden
	 * @param type AttributeType des Attributs, das geaendert werden soll.
	 * @param newValue neuer Wert des Attributs
	 * @author Christian Hovestadt
	 */
	public void updateOneAttribute(int creatureID, AttributeType type, int newValue) {
		if (creatureID == characterID)
			characterPanel.updateAttribute(type, newValue);
	}

	/**
	 * Setzt das Level des Spielers im CharacterPanel auf den uebergebenen neuen
	 * Wert
	 * 
	 * Oeffnet ein Popup, in dem der Spieler 5 dauerhafte Attributspunkte
	 * verteilen kann.
	 * 
	 * @param newLevel neuer Wert des Levels
	 * @author Christian Hovestadt
	 */
	public void levelUpgrade(int newLevel) {
		characterPanel.setLevel(newLevel);
		new LevelUpPopup(characterID, this);
	}

	/**
	 * Uebergibt eine neue ChatMessage an das MessagesPanel
	 * 
	 * @param message neue Message
	 * @author Christian Hovestadt
	 */
	public void addChatMessage(ChatMessage message) {
		messagesPanel.addChatMessage(message);
	}

	/**
	 * Wenn das uebergebene MovableObject der GameCharacter ist, wird der
	 * Viewpoint aktualisiert und die Position des Spielers auf der MiniMap
	 * angepasst.
	 * 
	 * @param mO
	 * @author Christian Hovestadt
	 */
	public void updateViewpoint(MovableObject mO) {
		if (mO.getID() == characterID) {
			gamePanel.updateViewpoint(mO.getCenteredX(), mO.getCenteredY(), this.getWidth(), this.getHeight());
			miniMapPanel.updateCharacterPosition(mO.getX(), mO.getY());
		}
	}
	
	public CLevelMap getLevelMap() {
		return levelMap;
	}

	public static int getRepaintsPerSec() {
		return REPAINTS_PER_SEC;
	}

	/**
	 * Zeigt, erweitert oder versteckt das InventoryPanel
	 * Der inventoryPanelStatus wird aum 1 erhoeht, wenn er 3 ueberschreitet, wird er auf 0 gesetzt (Modulo).
	 * 
	 * @author Christian Hovestadt
	 */
	public void showExpandOrHideInventoryPanel() {
		int newInventoryPanelStatus = (this.inventoryPanelStatus + 1) % 4;
		updateInventoryPanelStatus(newInventoryPanelStatus);
	}

	/**
	 * Erzeugt ein neues InventoryPanel je nach InventoryStatusType
	 * 
	 * @param newInventoryPanelStatus neuer InventoryStatusType
	 * @author Christian Hovestadt
	 */
	public void updateInventoryPanelStatus(int newInventoryPanelStatus) {
		if (this.inventoryPanelStatus != newInventoryPanelStatus) {
			this.inventoryPanelStatus = newInventoryPanelStatus;
			List<Integer> lastCraftingItems = inventoryPanel.getCurrentCraftingItems();
			layeredPane.remove(inventoryPanel);
			if (inventoryPanelStatus == 1) {
				inventoryPanel = new InventoryPanel(this, false, false, characterID, inventoryPanel.getInventory());
				inventoryPanel.setBounds((WIDTH - inventoryPanelWidth) / 2, HEIGHT - OUTER_BORDER - inventoryPanelHeight, inventoryPanelWidth, inventoryPanelHeight);
				layeredPane.setLayer(inventoryPanel, 1);
				layeredPane.add(inventoryPanel);
			} else if (inventoryPanelStatus == 2) {
				inventoryPanel = new InventoryPanel(this, true, false, characterID, inventoryPanel.getInventory());
				inventoryPanel.setBounds((WIDTH - inventoryPanelWidth) / 2, HEIGHT - OUTER_BORDER - inventoryPanelExpandedHeight, inventoryPanelWidth, inventoryPanelExpandedHeight);
				layeredPane.setLayer(inventoryPanel, 1);
				layeredPane.add(inventoryPanel);
			} else if (inventoryPanelStatus == 3) {
				inventoryPanel = new InventoryPanel(this, true, true, characterID, inventoryPanel.getInventory());
				inventoryPanel.setBounds((WIDTH - inventoryPanelWidth) / 2, HEIGHT - OUTER_BORDER - inventoryPanelExpandedWithCraftingHeight, inventoryPanelWidth,
						inventoryPanelExpandedWithCraftingHeight);
				layeredPane.setLayer(inventoryPanel, 1);
				layeredPane.add(inventoryPanel);
				inventoryPanel.reAddLastItemsToCrafting(lastCraftingItems);
			}
			this.validate();
		}
	}

	/**
	 * Versteckt oder zeigt das FlagsPanel, je nachdem ob es aktuell sichtbar ist oder nicht.
	 * 
	 * @author Christian Hovestadt 
	 */
	public void showOrHideFlagsPanel() {
		if (isFlagsPanelVisible)
			layeredPane.remove(flagsPanel);
		else
			layeredPane.add(flagsPanel);
		isFlagsPanelVisible ^= true;
		this.validate();
	}

	/**
	 * Versteckt oder zeigt das CharacterPanel, je nachdem ob es aktuell sichtbar ist oder nicht.
	 * 
	 * @author Christian Hovestadt 
	 */
	public void showOrHideCharacterPanel() {
		if (isCharacterPanelVisible)
			layeredPane.remove(characterPanel);
		else
			layeredPane.add(characterPanel);
		isCharacterPanelVisible ^= true;
		this.validate();
	}

	/**
	 * Versteckt oder zeigt das MiniMapPanel, je nachdem ob es aktuell sichtbar ist oder nicht.
	 * 
	 * @author Christian Hovestadt 
	 */
	public void showOrHideMiniMapPanel() {
		if (isMiniMapPanelVisible)
			layeredPane.remove(miniMapPanel);
		else
			layeredPane.add(miniMapPanel);
		isMiniMapPanelVisible ^= true;
		this.validate();
	}

	/**
	 * Oeffnet neue Koordinaten auf der MiniMap
	 * 
	 * @param coordinates Koordinaten, die aufgedeckt werden sollen
	 * @author Christian Hovestadt
	 */
	public void uncover(Coordinates coordinates) {
		this.miniMapPanel.uncover(coordinates);
	}

	/**
	 * Oeffnet das GameOverPopup
	 * 
	 * @author Christian Hovestadt
	 */
	public void gameOver() {
		new GameOverPopup(characterID);
	}
	
	/**
	 * Teilt dem Server mit, dass der Client wegen Inkonsistenzen neue Leveldaten braucht.
	 * @deprecated 
	 * @author Christian Hovestadt
	 */
	public void requestNewLevelData() {
		ServerConnection.sendMessageToServer(new NewLevelDataRequest(characterID));
	}
	
	public int getCharacterID() {
		return characterID;
	}
}
