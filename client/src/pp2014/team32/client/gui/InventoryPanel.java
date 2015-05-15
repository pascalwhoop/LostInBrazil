package pp2014.team32.client.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import pp2014.team32.client.comm.ServerConnection;
import pp2014.team32.shared.entities.CraftingReceipts;
import pp2014.team32.shared.entities.Inventory;
import pp2014.team32.shared.entities.Item;
import pp2014.team32.shared.enums.ActionType;
import pp2014.team32.shared.enums.ItemType;
import pp2014.team32.shared.messages.CraftingRequest;
import pp2014.team32.shared.messages.InventoryActionRequest;
import pp2014.team32.shared.utils.PropertyManager;

/**
 * Dieses Panel zeigt das Inventar auf, ermoelicht Item-Interaktionen und
 * Crafting
 * 
 * @author Christian Hovestadt
 * @author Mareike Fischer
 * @version 7.7.14
 */
public class InventoryPanel extends JPanel {

	private static final long				serialVersionUID	= 8044736803027670255L;
	private static BufferedImage			background, craftingLineImage;
	private static HashMap<ItemType, Icon>	itemImages;
	private static Logger					LOGGER				= Logger.getLogger(InventoryPanel.class.getName());
	private static Icon						defaultIcon;
	private int								characterID;
	private Inventory						inventory;
	private JButton[]						regularButtons;
	private CraftingButton[]				craftingButtons;
	private JButton							weaponButton, armourButton, craftingDestButton;
	private boolean							expanded, showCrafting, craftingPossible;
	private final GameWindow				GAME_WINDOW;
	private static final int				INVENTORY_SIZE, CRAFTING_SIZE;

	/**
	 * Das Hintergrundbild, alle Item-Bilder und ein Default-Image fuer einen
	 * leeren Inventarplatz werden einmal bei der ersten Verwendung der klasse
	 * statisch eingeladen.
	 * 
	 * @author Mareike Fischer
	 */
	static {
		INVENTORY_SIZE = Integer.parseInt(PropertyManager.getProperty("inventorySize"));
		CRAFTING_SIZE = Integer.parseInt(PropertyManager.getProperty("craftingSize"));

		try {
			background = ImageIO.read(new File("images/inventory.png"));
			craftingLineImage = ImageIO.read(new File("images/inventoryLine.png"));
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Fehler: Bild nicht geladen");
		}

		itemImages = new HashMap<ItemType, Icon>();
		for (ItemType type : ItemType.values())
			itemImages.put(type, new ImageIcon(PropertyManager.getProperty("paths.itemImagePath") + type.toString().toLowerCase() + ".png"));

		BufferedImage emptyImage = new BufferedImage(34, 34, BufferedImage.TYPE_INT_ARGB);
		Graphics g = emptyImage.getGraphics();
		g.setColor(new Color(0, 0, 0, 0));
		g.fillRect(0, 0, 34, 34);
		defaultIcon = new ImageIcon(emptyImage);
	}

	/**
	 * Erzeugt ein neues InventoryPanel mit der ueber <i>expanded</i> und
	 * <i>showCrafting</i> uebergebenen Groesse.
	 * 
	 * <table>
	 * <tr>
	 * <th>expanded</th>
	 * <th>showCrafting</th>
	 * </tr>
	 * <tr>
	 * <th>false</th>
	 * <th>false</th>
	 * <th>1x InventoryContent</th>
	 * </tr>
	 * <tr>
	 * <th>false</th>
	 * <th>true</th>
	 * <th>1x InventoryContent, 1x CraftingContent</th>
	 * </tr>
	 * <tr>
	 * <th>true</th>
	 * <th>false</th>
	 * <th>2x InventoryContent</th>
	 * </tr>
	 * <tr>
	 * <th>true</th>
	 * <th>true</th>
	 * <th>2x InventoryContent, 1x CraftingContent</th>
	 * </tr>
	 * </table>
	 * 
	 * Die Buttons werden einmal alle erzeugt, die Icons werden bei einem Update
	 * des Inventars geaendert.
	 * 
	 * In jedem InventoryContent wird ein Button mit rotem Rand erzeugt, im
	 * ersten inventoryContent ist dieser fuer die Waffe reserviert, im unteren
	 * fuer die Ruestung.
	 * 
	 * @param gameWindow Das GameWindow, in dem das Panel angezeigt werden soll
	 * @param expanded Inventar soll um die 2. Objektreihe erweitert werden
	 * @param showCrafting Die Craftingzeile soll angezeigt werden
	 * @author Mareike Fischer
	 */
	public InventoryPanel(GameWindow gameWindow, boolean expanded, boolean showCrafting) {
		this.expanded = expanded;
		this.showCrafting = showCrafting;
		this.craftingPossible = false;
		this.GAME_WINDOW = gameWindow;
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.setOpaque(false);

		// Buttons zum InventoryContent hinzufuegen
		regularButtons = new JButton[INVENTORY_SIZE];
		craftingButtons = new CraftingButton[CRAFTING_SIZE];
		InventoryContent inventoryContent = new InventoryContent(0, INVENTORY_SIZE / 2 - 1, true);

		this.add(Box.createRigidArea(new Dimension(0, 35)));
		this.add(inventoryContent);
		if (expanded) {
			InventoryContent inventoryContent2 = new InventoryContent(INVENTORY_SIZE / 2, INVENTORY_SIZE - 1, false);
			this.add(Box.createRigidArea(new Dimension(0, 10)));
			this.add(inventoryContent2);
		}
		CraftingContent craftingContent = new CraftingContent();
		if (showCrafting) {
			JPanel craftingLine = new JPanel() {
				private static final long	serialVersionUID	= 8881438393567230979L;

				public void paintComponent(Graphics g) {
					g.drawImage(craftingLineImage, 0, 0, null);
				}
			};
			craftingLine.setPreferredSize(new Dimension(craftingLineImage.getWidth(), craftingLineImage.getHeight()));

			this.add(Box.createRigidArea(new Dimension(0, 2)));
			this.add(craftingLine);
			this.add(Box.createRigidArea(new Dimension(0, 2)));
			this.add(craftingContent);
		}

		this.add(Box.createRigidArea(new Dimension(0, 5)));

		inventory = new Inventory();
		setInventory(inventory);
	}

	/**
	 * Ruft den anderen Konstruktor auf, zusaetzlich wird die characterID und
	 * das Inventory gesetzt
	 * 
	 * @param gameWindow Das GameWindow, in dem das Panel angezeigt werden soll
	 * @param expanded Inventar soll um die 2. Objektreihe erweitert werden
	 * @param showCrafting Die Craftingzeile soll angezeigt werden
	 * @param characterID ID des GameCharacters
	 * @param inventory Inventar des GameCharacters
	 * @author Mareike Fischer
	 */
	public InventoryPanel(GameWindow gameWindow, boolean expanded, boolean showCrafting, int characterID, Inventory inventory) {
		this(gameWindow, expanded, showCrafting);
		this.characterID = characterID;
		this.setInventory(inventory);
	}

	/**
	 * Zeichnet das Hintergrundbild
	 * 
	 * @author Mareike Fischer
	 */
	public void paintComponent(Graphics g) {
		g.drawImage(background, 0, 0, null);
	}

	void setCharacterID(int characterID) {
		this.characterID = characterID;
	}

	/**
	 * Aktualisiert das angezeigte Inventar, dabei werden die Icons der Buttons
	 * auf die Bilder der Items geaendert. Ausserdem werden die
	 * Beschreibungstexte erzeugt.
	 * 
	 * Der Inhalt der CraftingButtons wird geleert.
	 * 
	 * @param inventory neues Inventar
	 * @author Christian Hovestadt
	 * @author Mareike Fischer
	 */
	void setInventory(Inventory inventory) {
		this.inventory = inventory;
		int lastIndex = (expanded) ? 13 : 6;
		for (int i = 0; i <= lastIndex; i++) {
			Item item = inventory.getItemAtIndex(i);
			if (item == null) {
				regularButtons[i].setIcon(defaultIcon);
				regularButtons[i].setToolTipText(null);
			} else {
				regularButtons[i].setIcon(itemImages.get(item.getItemType()));
				regularButtons[i].setToolTipText(item.getToolTipText());
			}
		}

		if (inventory.getWeapon() == null) {
			weaponButton.setIcon(defaultIcon);
			weaponButton.setToolTipText(null);
		} else {
			weaponButton.setIcon(itemImages.get(inventory.getWeapon().getItemType()));
			weaponButton.setToolTipText(inventory.getWeapon().getToolTipText());
		}

		if (expanded)
			if (inventory.getArmour() == null) {
				armourButton.setIcon(defaultIcon);
				armourButton.setToolTipText(null);
			} else {
				armourButton.setIcon(itemImages.get(inventory.getArmour().getItemType()));
				armourButton.setToolTipText(inventory.getArmour().getToolTipText());
			}

		List<Integer> lastCraftingItems = getCurrentCraftingItems();

		clearCraftingButtons();
		reAddLastItemsToCrafting(lastCraftingItems);
	}

	public Inventory getInventory() {
		return inventory;
	}

	/**
	 * Convenience Method fuer die Erzeugung eines Inventar-Buttons
	 * 
	 * @param borderColor Farbe des Rands
	 * @return neuer Button
	 * @author Christian Hovestadt
	 */
	public JButton createItemButton(Color borderColor) {
		JButton button = new JButton();
		button.setContentAreaFilled(false);
		button.setBorder(BorderFactory.createLineBorder(borderColor, 3));
		button.setIcon(defaultIcon);
		return button;
	}

	/**
	 * Sucht ein moegliches Crafting-Rezept und passt das Icon des
	 * Crafting-Ziel-Buttons entsprechend an.
	 * 
	 * @author Christian Hovestadt
	 */
	private void updateCraftingDestButton() {
		Set<ItemType> input = new HashSet<ItemType>();
		for (int i = 0; i < craftingButtons.length; i++)
			if (craftingButtons[i].refItem != null)
				input.add(craftingButtons[i].refItem.getItemType());

		ItemType craftingResult = CraftingReceipts.craft(input);
		if (craftingResult == null) {
			craftingDestButton.setIcon(defaultIcon);
			craftingDestButton.setToolTipText(null);
			this.craftingPossible = false;
		} else {
			craftingDestButton.setIcon(itemImages.get(craftingResult));
			craftingDestButton.setToolTipText(new Item(0, 0, 0, craftingResult).getToolTipText());
			this.craftingPossible = true;
		}
	}

	/**
	 * Das Item an der uebergebenen Position wird zum Crafting hinzugefuegt.
	 * Dabei wird der urspruengliche Button ausgegraut.
	 * 
	 * @param position Position des Items im Inventar
	 * @author Christian Hovestadt
	 */
	private void useItemForCrafting(int position) {
		// Search first free index, the last Index is chosen if everything
		// is full
		int freeIndex = 0;

		while (freeIndex < craftingButtons.length - 1 && craftingButtons[freeIndex].isOccupied())
			freeIndex++;

		JButton button;
		if (position == -1) {
			button = weaponButton;
			craftingButtons[freeIndex].refItem = inventory.getWeapon();
		} else if (position == -2) {
			button = armourButton;
			craftingButtons[freeIndex].refItem = inventory.getArmour();
		} else {
			button = regularButtons[position];
			craftingButtons[freeIndex].refItem = inventory.getItemAtIndex(position);
		}

		craftingButtons[freeIndex].refButton = button;
		craftingButtons[freeIndex].setIcon(button.getIcon());
		craftingButtons[freeIndex].setToolTipText(button.getToolTipText());
		button.setEnabled(false);
		updateCraftingDestButton();
	}

	/**
	 * Gibt die Items zurueck, die aktuell in den Crafting-Buttons liegen. Wird verwendet, um die Items in einem neuen Panel wieder zum Crafting hinzufuegen zu koennen.
	 * @return
	 */
	public List<Integer> getCurrentCraftingItems() {
		LinkedList<Integer> currentCraftingItems = new LinkedList<Integer>();

		for (CraftingButton cb : craftingButtons)
			if (cb != null && cb.refItem != null)
				currentCraftingItems.add(cb.refItem.getID());

		return currentCraftingItems;
	}

	/**
	 * Fuegt mehrere Items zum Crafting hinzu, wenn es die Items im Inventar
	 * gibt. Wird benutzt, wenn zwischendurch ein InventoryUpdate kommt und wenn
	 * das Panel zum Crafting erst erweitert werden muss.
	 * 
	 * @param lastCraftingItems
	 * @author Christian Hovestadt
	 */
	public void reAddLastItemsToCrafting(List<Integer> lastCraftingItems) {
		for (Integer craftItemID: lastCraftingItems) {
			// Search position
			int i = 0;
			while (i < inventory.getInventoryItems().length) {
				if (inventory.getInventoryItems()[i] != null)
					if (inventory.getInventoryItems()[i].getID() == craftItemID)
						break;
				i++;
			}
			if (i < inventory.getInventoryItems().length)
				useItemForCrafting(i);
		}
		
		/*
		for (int i = 0; i < inventory.getInventoryItems().length; i++)
			if (inventory.getInventoryItems()[i] != null)
				if (lastCraftingItems.contains(inventory.getInventoryItems()[i].getID()))
					useItemForCrafting(i);
		*/
	}

	/**
	 * Loescht den Inhalt aller CraftingButtons
	 * 
	 * @author Christian Hovestadt
	 */
	private void clearCraftingButtons() {
		if (showCrafting) {
			for (CraftingButton button : craftingButtons) {
				if (button.refButton != null)
					button.refButton.setEnabled(true);
				button.refButton = null;
				button.refItem = null;
				button.setIcon(defaultIcon);
				button.setToolTipText(null);
			}
			craftingDestButton.setIcon(defaultIcon);
			craftingDestButton.setToolTipText(null);
			this.craftingPossible = false;
		}
	}

	/**
	 * Eine Zeile des InventoryPanels, bestehend aus einem roten Button und 7
	 * regulaeren Buttons
	 * 
	 * @author Christian Hovestadt
	 * @author Mareike Fischer
	 */
	private class InventoryContent extends JPanel {
		private static final long	serialVersionUID	= 8800471431169034876L;

		/**
		 * Erzeugt das InventoryContent-Panel mit BoxLayout unter Verwendung der
		 * Buttons, die im InventoryPanel gespeichert sind.
		 * 
		 * Erzeugt die Kontextmenues fuer jeden Button
		 * 
		 * @param firstIndex Erster Index des vom InventoryContent abgedeckten
		 *            Bereich des Inventars
		 * @param lastIndex Letzter Index des vom InventoryContent abgedeckten
		 *            Bereich des Inventars
		 * @param isWeapon Ist der rote Button fuer die Waffe oder die Ruestung
		 *            gedacht? (true: Waffe, false: Ruestung)
		 * 
		 * @author Christian Hovestadt
		 * @author Mareike Fischer
		 */
		public InventoryContent(int firstIndex, int lastIndex, boolean isWeapon) {
			this.setOpaque(false);
			this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
			this.add(Box.createRigidArea(new Dimension(5, 0)));

			// Highlighted Button (Weapon or Armour)
			JButton highlightedButton = createItemButton(new Color(170, 0, 0));
			this.add(highlightedButton);
			if (isWeapon)
				weaponButton = highlightedButton;
			else
				armourButton = highlightedButton;

			// Popup Menu
			int index = (isWeapon) ? -1 : -2;
			JPopupMenu menu = new JPopupMenu();
			JMenuItem putBack = new JMenuItem("Zur\u00FCcklegen");
			putBack.addActionListener(new ItemActionListener(index, ActionType.PUT_BACK));
			menu.add(putBack);
			JMenuItem drop = new JMenuItem("Ablegen");
			drop.addActionListener(new ItemActionListener(index, ActionType.DROP));
			menu.add(drop);
			JMenuItem destroy = new JMenuItem("Zerst\u00F6ren");
			destroy.addActionListener(new ItemActionListener(index, ActionType.DESTROY));
			menu.add(destroy);
			JMenuItem craft = new JMenuItem("Zum Crafting benutzen");
			craft.addActionListener(new UseForCraftingListener(index));
			menu.add(craft);
			MouseListener popupListener = new PopupListener(menu, index);
			highlightedButton.addMouseListener(popupListener);

			// Regular Buttons
			for (int i = firstIndex; i <= lastIndex; i++) {
				regularButtons[i] = createItemButton(Color.GRAY);
				regularButtons[i].addActionListener(new ItemActionListener(i, ActionType.USE));
				this.add(Box.createHorizontalGlue());
				this.add(regularButtons[i]);

				// Popup Menu
				menu = new JPopupMenu();
				JMenuItem use = new JMenuItem("Benutzen");
				use.addActionListener(new ItemActionListener(i, ActionType.USE));
				menu.add(use);
				drop = new JMenuItem("Ablegen");
				drop.addActionListener(new ItemActionListener(i, ActionType.DROP));
				menu.add(drop);
				destroy = new JMenuItem("Zerst\u00F6ren");
				destroy.addActionListener(new ItemActionListener(i, ActionType.DESTROY));
				menu.add(destroy);
				craft = new JMenuItem("Zum Crafting benutzen");
				craft.addActionListener(new UseForCraftingListener(i));
				menu.add(craft);

				popupListener = new PopupListener(menu, i);
				regularButtons[i].addMouseListener(popupListener);
			}
			this.add(Box.createRigidArea(new Dimension(2, 0)));
		}
	}

	/**
	 * Panel fuer die Crafting Zeile
	 * 
	 * Besteht aus drei Input-Buttons und einem Output-Button, dazwischen werden
	 * Pluszeichen und ein Pfeil gezeichnet.
	 * 
	 * @author Christian Hovestadt
	 */
	private class CraftingContent extends JPanel {
		private static final long	serialVersionUID	= -6886998287207806624L;

		/**
		 * Erzeugt das CraftingContent-Panel mit BoxLayout, bestehnd aus 3
		 * Input-Buttons und einem Output-Button, dazwischen werden JLabels mit
		 * einem Pluszeichen bzw. einem Pfeil hinzugefuegt.
		 * 
		 * Erzeugt die Kontextmenues fuer jeden Button.
		 * 
		 * @author Christian Hovestadt
		 */
		public CraftingContent() {
			this.setOpaque(false);
			this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
			this.add(Box.createRigidArea(new Dimension(5, 0)));

			for (int i = 0; i < 3; i++) {
				craftingButtons[i] = new CraftingButton();
				craftingButtons[i].setContentAreaFilled(false);
				craftingButtons[i].setBorder(BorderFactory.createLineBorder(Color.GRAY, 3));
				craftingButtons[i].setIcon(defaultIcon);
				craftingButtons[i].addActionListener(new PutCraftingItemBackListener(craftingButtons[i]));
				// Popup Menu
				JPopupMenu menu = new JPopupMenu();
				JMenuItem putBack = new JMenuItem("Zur\u00FCcklegen");
				putBack.addActionListener(new PutCraftingItemBackListener(craftingButtons[i]));
				menu.add(putBack);
				craftingButtons[i].addMouseListener(new CraftingPopupListener(menu, craftingButtons[i]));
			}

			craftingDestButton = createItemButton(new Color(170, 0, 0));
			craftingDestButton.addActionListener(new CraftListener());
			// Popup Menu
			JPopupMenu menu = new JPopupMenu();
			JMenuItem craft = new JMenuItem("Craften");
			craft.addActionListener(new CraftListener());
			menu.add(craft);
			craftingDestButton.addMouseListener(new CraftingDestPopupListener(menu));

			int gap = 10;

			this.add(Box.createHorizontalGlue());
			this.add(Box.createRigidArea(new Dimension(gap, 0)));
			this.add(craftingButtons[0]);
			this.add(Box.createRigidArea(new Dimension(gap, 0)));
			this.add(new JLabel("+"));
			this.add(Box.createRigidArea(new Dimension(gap, 0)));
			this.add(craftingButtons[1]);
			this.add(Box.createRigidArea(new Dimension(gap, 0)));
			this.add(new JLabel("+"));
			this.add(Box.createRigidArea(new Dimension(gap, 0)));
			this.add(craftingButtons[2]);
			this.add(Box.createRigidArea(new Dimension(gap, 0)));
			this.add(new JLabel("\u2192"));
			this.add(Box.createRigidArea(new Dimension(gap, 0)));
			this.add(craftingDestButton);
			this.add(Box.createHorizontalGlue());
			this.add(Box.createRigidArea(new Dimension(2, 0)));
		}
	}

	/**
	 * Erweiterung eines JButtons um ein Referenz-Item. Dieses wird benoetigt,
	 * um von einem CraftingButton auf den urspruenglichen Button des Items
	 * zurueckzuschliessen.
	 * 
	 * @author Christian Hovestadt
	 */
	private class CraftingButton extends JButton {
		private static final long	serialVersionUID	= 7938483350223119703L;
		private JButton				refButton;
		private Item				refItem;

		/**
		 * Gibt zurueck, ob der Button eine Referenz hat (und damit belegt ist)
		 * oder nicht.
		 * 
		 * @return Die Antwort
		 * @author Christian Hovestadt
		 */
		private boolean isOccupied() {
			return refButton != null;
		}
	}

	/**
	 * Dieser Listener registreirt, ob der User auf das Inventar klickt und
	 * oeffnet bei einem Rechtsklick auf einen belegten Button das Kontextmenue.
	 * 
	 * @author Mareike Fischer
	 */
	private class PopupListener extends MouseAdapter {
		private JPopupMenu	menu;
		private int			position;

		public PopupListener(JPopupMenu menu, int position) {
			super();
			this.menu = menu;
			this.position = position;
		}

		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}

		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}

		/**
		 * Wird bei einem Klick ausgeloest
		 * 
		 * Ueberprueft, ob der Button belegt ist und oeffnet das Kontextmenue
		 * fuer diesen Button
		 * 
		 * @param e MouseEvent
		 * 
		 * @author Mareike Fischer
		 */
		private void maybeShowPopup(MouseEvent e) {
			// Only show the popup if there if the inventory is not empty at
			// position
			Item thisItem = null;
			if (position == -1) {
				if (weaponButton.isEnabled())
					thisItem = inventory.getWeapon();
			} else if (position == -2) {
				if (armourButton.isEnabled())
					thisItem = inventory.getArmour();
			} else {
				if (regularButtons[position].isEnabled())
					thisItem = inventory.getItemAtIndex(position);
			}

			if (e.isPopupTrigger() && thisItem != null)
				menu.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	/**
	 * Oeffnet das Kontextmenue fuer die CraftingButtons (Inputs), wenn sie
	 * belegt sind.
	 * Speichert im Gegensatz zum PopupListener direkt ab, zu welchem Button er
	 * gehoert.
	 * 
	 * @author Christian Hovestadt
	 */
	private class CraftingPopupListener extends MouseAdapter {
		JPopupMenu		menu;
		CraftingButton	button;

		public CraftingPopupListener(JPopupMenu menu, CraftingButton button) {
			this.menu = menu;
			this.button = button;
		}

		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}

		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}

		/**
		 * Oeffnet das Kontextmenue fuer den CraftingButton, wenn er belegt ist.
		 * 
		 * @param e MouseEvent
		 * @author Christian Hovestadt
		 */
		private void maybeShowPopup(MouseEvent e) {
			if (e.isPopupTrigger() && button.isOccupied())
				menu.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	/**
	 * PopupListener fuer den CraftingDestButton
	 * 
	 * Braucht keine Referenz, da er nur fuer einen Button vorgesehen ist.
	 * 
	 * @author christian
	 */
	private class CraftingDestPopupListener extends MouseAdapter {
		JPopupMenu	menu;

		public CraftingDestPopupListener(JPopupMenu menu) {
			this.menu = menu;
		}

		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}

		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}

		/**
		 * Oeffnet das Kontextmenue fuer den CraftingDestButton, wenn das
		 * Crafting moeglich ist (wenn er also belegt ist).
		 * 
		 * @param e
		 * @author Christian Hovestadt
		 */
		private void maybeShowPopup(MouseEvent e) {
			if (e.isPopupTrigger() && craftingPossible)
				menu.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	/**
	 * Wird vom Kontextmenue ausgeloest, oder wenn der User direkt auf den
	 * Button klickt.
	 * Sendet eine DrawableObjectInteractionRequest an den Server.
	 * 
	 * @author christian
	 *
	 */
	private class ItemActionListener implements ActionListener {

		private int			position;
		private ActionType	actionType;

		/**
		 * Erzeugt den neuen ItemActionListener
		 * 
		 * @param position Position des zugehoerigen Items im Inventar
		 * @param actionType ActionType der Aktion, die bei Ausloesung
		 *            ausgefuehrt werden soll
		 * @author Christian Hovestadt
		 */
		public ItemActionListener(int position, ActionType actionType) {
			this.position = position;
			this.actionType = actionType;
		}

		/**
		 * Sucht das zum Button gehoerige Item und sendet eine
		 * DrawableObjectInteractionRequest an den Server (wenn es das Item
		 * gibt)
		 * 
		 * @author Christian Hovestadt
		 */
		public void actionPerformed(ActionEvent e) {
			Item thisItem;
			if (position == -1)
				thisItem = inventory.getWeapon();
			else if (position == -2)
				thisItem = inventory.getArmour();
			else
				thisItem = inventory.getItemAtIndex(position);
			if (thisItem != null)
				ServerConnection.sendMessageToServer(new InventoryActionRequest(characterID, thisItem.getID(), actionType));
		}
	}

	/**
	 * ActionListener fuer die Benutzung eines Items zum Crafting
	 * Ein regulaeres Item wird bei Ausloesung dem ersten freien Input-Button
	 * zugewiesen, der regulaere Button wird deaktiviert und ausgegraut.
	 * 
	 * @author Christian Hovestadt
	 */
	private class UseForCraftingListener implements ActionListener {

		private int	position;

		/**
		 * @param position Position des Items im Inventar
		 * @author Christian Hovestadt
		 */
		public UseForCraftingListener(int position) {
			this.position = position;
		}

		/**
		 * Ein regulaeres Item wird bei Ausloesung dem ersten freien
		 * Input-Button zugewiesen, der regulaere Button wird deaktiviert und
		 * ausgegraut.
		 * 
		 * @author Christian Hovestadt
		 */
		public void actionPerformed(ActionEvent e) {
			useItemForCrafting(position);
			GAME_WINDOW.updateInventoryPanelStatus(3);
		}
	}

	/**
	 * ActionListener, der das Gegenteil vom UseForCraftingListener bewirkt.
	 * 
	 * @author Christian Hovestadt
	 */
	private class PutCraftingItemBackListener implements ActionListener {

		private CraftingButton	thisButton;

		public PutCraftingItemBackListener(CraftingButton thisButton) {
			this.thisButton = thisButton;
		}

		/**
		 * Bei Ausloesung wird der CraftingButton wird zurueckgesetzt und
		 * deaktiviert, der zugehoerige Button wird wieder aktiviert.
		 */
		public void actionPerformed(ActionEvent e) {
			if (thisButton.refButton != null) {
				thisButton.refButton.setEnabled(true);
				thisButton.refButton = null;
				thisButton.refItem = null;
				thisButton.setIcon(defaultIcon);
				thisButton.setToolTipText(null);
				updateCraftingDestButton();
			}
		}
	}

	/**
	 * ActionListener fuer das Ausfuehren des Craftings
	 * 
	 * @author Christian Hovestadt
	 */
	private class CraftListener implements ActionListener {

		/**
		 * Bei Klick auf den Crafting-Output-Button (oder ueber das
		 * Kontextmenue) wird eine CraftingRequest mit den Input-Items an den
		 * Server gesendet.
		 * 
		 * @author Christian Hovestadt
		 */
		public void actionPerformed(ActionEvent e) {
			if (craftingPossible) {
				Set<Integer> input = new HashSet<Integer>();
				for (int i = 0; i < craftingButtons.length; i++)
					if (craftingButtons[i].refItem != null)
						input.add(craftingButtons[i].refItem.getID());

				ServerConnection.sendMessageToServer(new CraftingRequest(input, characterID));
				clearCraftingButtons();
			}
		}
	}
}