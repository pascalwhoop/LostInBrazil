package pp2014.team32.client.engine;

import java.awt.event.ActionEvent;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import pp2014.team32.client.comm.ServerConnection;
import pp2014.team32.client.gui.GameWindow;
import pp2014.team32.shared.enums.ChatMessageType;
import pp2014.team32.shared.messages.ChatMessage;
import pp2014.team32.shared.messages.MovementRequest;

/**
 * <i>ClientControls</i> verwaltet die KeyBindings fuer die Tastatur.
 * Fuer jede verwendete Taste wird eine AbstractAction definiert, in der der bei
 * Tastendruck auszufuehrende Code steht.
 * Die Bewegungen werden gesammelt und in der Methode <i>update</i> in eine
 * Richtung ueberfuehrt.
 * 
 * @version 24.6.2014
 * @author Christian Hovestadt
 */
public class ClientControls {
	public boolean		upPressed	= false, leftPressed = false, downPressed = false, rightPressed = false;
	public boolean		wPressed	= false, aPressed = false, sPressed = false, dPressed = false;
	public long			lastChat	= System.currentTimeMillis();
	public boolean		lastDirectionStanding;
	public boolean		spacePressed;
	public final int	CHARACTER_ID;
	public int			levelMapID;
	public final String	USERNAME;
	public GameWindow	gameWindow;

	/**
	 * Fuegt alle KeyBindings zum gegebenen Panel hinzu.
	 * 
	 * @param panel
	 * @param characterID ID des GameCharacters, von dem die Bewegung ausgeht.
	 * @param username Benutzername des Spielers
	 * @author Christian Hovestadt
	 */
	public ClientControls(final GameWindow gameWindow, final JPanel panel, int characterID, String username) {
		this.gameWindow = gameWindow;
		this.CHARACTER_ID = characterID;
		this.levelMapID = 0;
		this.USERNAME = username;
		panel.setFocusTraversalKeysEnabled(false);
		this.lastDirectionStanding = false;

		// Move up - W
		panel.getInputMap().put(KeyStroke.getKeyStroke("pressed W"), "moveUpW");
		panel.getActionMap().put("moveUpW", new AbstractAction() {
			private static final long	serialVersionUID	= 5412899015069527732L;

			public void actionPerformed(ActionEvent e) {
				wPressed = true;
			}
		});
		panel.getInputMap().put(KeyStroke.getKeyStroke("released W"), "endMoveUpW");
		panel.getActionMap().put("endMoveUpW", new AbstractAction() {
			private static final long	serialVersionUID	= -2453295735649786992L;

			public void actionPerformed(ActionEvent e) {
				wPressed = false;
			}
		});

		// Move up - Arrow UP
		panel.getInputMap().put(KeyStroke.getKeyStroke("pressed UP"), "moveUp");
		panel.getActionMap().put("moveUp", new AbstractAction() {
			private static final long	serialVersionUID	= 5412899015069527732L;

			public void actionPerformed(ActionEvent e) {
				upPressed = true;
			}
		});
		panel.getInputMap().put(KeyStroke.getKeyStroke("released UP"), "endMoveUp");
		panel.getActionMap().put("endMoveUp", new AbstractAction() {
			private static final long	serialVersionUID	= -2453295735649786992L;

			public void actionPerformed(ActionEvent e) {
				upPressed = false;
			}
		});

		// Move left - A
		panel.getInputMap().put(KeyStroke.getKeyStroke("pressed A"), "moveLeftA");
		panel.getActionMap().put("moveLeftA", new AbstractAction() {

			private static final long	serialVersionUID	= 8472657197210510980L;

			public void actionPerformed(ActionEvent e) {
				aPressed = true;
			}
		});
		panel.getInputMap().put(KeyStroke.getKeyStroke("released A"), "endMoveLeftA");
		panel.getActionMap().put("endMoveLeftA", new AbstractAction() {

			private static final long	serialVersionUID	= -7636872114081751601L;

			public void actionPerformed(ActionEvent e) {
				aPressed = false;
			}
		});

		// Move left - Arrow LEFT
		panel.getInputMap().put(KeyStroke.getKeyStroke("pressed LEFT"), "moveLeft");
		panel.getActionMap().put("moveLeft", new AbstractAction() {

			private static final long	serialVersionUID	= 8472657197210510980L;

			public void actionPerformed(ActionEvent e) {
				leftPressed = true;
			}
		});
		panel.getInputMap().put(KeyStroke.getKeyStroke("released LEFT"), "endMoveLeft");
		panel.getActionMap().put("endMoveLeft", new AbstractAction() {

			private static final long	serialVersionUID	= -7636872114081751601L;

			public void actionPerformed(ActionEvent e) {
				leftPressed = false;
			}
		});

		// Move down - S
		panel.getInputMap().put(KeyStroke.getKeyStroke("pressed S"), "moveDownS");
		panel.getActionMap().put("moveDownS", new AbstractAction() {

			private static final long	serialVersionUID	= 43364374855532529L;

			public void actionPerformed(ActionEvent e) {
				sPressed = true;
			}
		});
		panel.getInputMap().put(KeyStroke.getKeyStroke("released S"), "endMoveDownS");
		panel.getActionMap().put("endMoveDownS", new AbstractAction() {

			private static final long	serialVersionUID	= 8521153819185458330L;

			public void actionPerformed(ActionEvent e) {
				sPressed = false;
			}
		});

		// Move down - Arrow DOWN
		panel.getInputMap().put(KeyStroke.getKeyStroke("pressed DOWN"), "moveDown");
		panel.getActionMap().put("moveDown", new AbstractAction() {

			private static final long	serialVersionUID	= 43364374855532529L;

			public void actionPerformed(ActionEvent e) {
				downPressed = true;
			}
		});
		panel.getInputMap().put(KeyStroke.getKeyStroke("released DOWN"), "endMoveDown");
		panel.getActionMap().put("endMoveDown", new AbstractAction() {

			private static final long	serialVersionUID	= 8521153819185458330L;

			public void actionPerformed(ActionEvent e) {
				downPressed = false;
			}
		});

		// Move right - D
		panel.getInputMap().put(KeyStroke.getKeyStroke("pressed D"), "moveRightD");
		panel.getActionMap().put("moveRightD", new AbstractAction() {

			private static final long	serialVersionUID	= -2486313431016060414L;

			public void actionPerformed(ActionEvent e) {
				dPressed = true;
			}
		});
		panel.getInputMap().put(KeyStroke.getKeyStroke("released D"), "endMoveRightD");
		panel.getActionMap().put("endMoveRightD", new AbstractAction() {

			private static final long	serialVersionUID	= 4502313765740328445L;

			public void actionPerformed(ActionEvent e) {
				dPressed = false;
			}
		});

		// Move right - Arrow RIGHT
		panel.getInputMap().put(KeyStroke.getKeyStroke("pressed RIGHT"), "moveRight");
		panel.getActionMap().put("moveRight", new AbstractAction() {

			private static final long	serialVersionUID	= -2486313431016060414L;

			public void actionPerformed(ActionEvent e) {
				rightPressed = true;
			}
		});
		panel.getInputMap().put(KeyStroke.getKeyStroke("released RIGHT"), "endMoveRight");
		panel.getActionMap().put("endMoveRight", new AbstractAction() {

			private static final long	serialVersionUID	= 4502313765740328445L;

			public void actionPerformed(ActionEvent e) {
				rightPressed = false;
			}
		});

		panel.getInputMap().put(KeyStroke.getKeyStroke("pressed SPACE"), "attack");
		panel.getActionMap().put("attack", new AbstractAction() {

			private static final long	serialVersionUID	= -7276035432882414925L;

			public void actionPerformed(ActionEvent e) {
				spacePressed = true;
			}

		});

		panel.getInputMap().put(KeyStroke.getKeyStroke("released SPACE"), "endAttack");
		panel.getActionMap().put("endAttack", new AbstractAction() {

			private static final long	serialVersionUID	= -7276035432882414925L;

			public void actionPerformed(ActionEvent e) {
				spacePressed = false;
			}

		});

		// Chatnachricht - C/Enter
		panel.getInputMap().put(KeyStroke.getKeyStroke("released C"), "chat");
		panel.getInputMap().put(KeyStroke.getKeyStroke("released ENTER"), "chat");
		panel.getActionMap().put("chat", new AbstractAction() {
			private static final long	serialVersionUID	= -7276035432882414925L;

			public void actionPerformed(ActionEvent e) {
				if (System.currentTimeMillis() - lastChat > 150) {
					String message = JOptionPane.showInputDialog(panel, "Chat-Nachricht eingeben: ", "SYSTEM", JOptionPane.INFORMATION_MESSAGE);
					if (message != null)
						ServerConnection.sendMessageToServer(new ChatMessage(new Date(), USERNAME, message, ChatMessageType.CHAT));
					lastChat = System.currentTimeMillis();
				}
			}
		});

		// Hide or show FlagsPanel - F
		panel.getInputMap().put(KeyStroke.getKeyStroke("released F"), "flags");
		panel.getActionMap().put("flags", new AbstractAction() {
			private static final long	serialVersionUID	= 5420406803870889067L;

			public void actionPerformed(ActionEvent e) {
				gameWindow.showOrHideFlagsPanel();
			}
		});

		// Hide or show CharacterPanel - X
		panel.getInputMap().put(KeyStroke.getKeyStroke("released X"), "character");
		panel.getActionMap().put("character", new AbstractAction() {
			private static final long	serialVersionUID	= -8339748079424234107L;

			public void actionPerformed(ActionEvent e) {
				gameWindow.showOrHideCharacterPanel();
			}
		});

		// Hide or show MiniMapPanel - M/Q
		panel.getInputMap().put(KeyStroke.getKeyStroke("released M"), "miniMap");
		panel.getInputMap().put(KeyStroke.getKeyStroke("released Q"), "miniMap");
		panel.getActionMap().put("miniMap", new AbstractAction() {
			private static final long	serialVersionUID	= -1025900278897454374L;

			public void actionPerformed(ActionEvent e) {
				gameWindow.showOrHideMiniMapPanel();
			}

		});

		// Change Inventory View - E/I/TAB
		panel.getInputMap().put(KeyStroke.getKeyStroke("released E"), "inventory");
		panel.getInputMap().put(KeyStroke.getKeyStroke("released I"), "inventory");
		panel.getInputMap().put(KeyStroke.getKeyStroke("released TAB"), "inventory");
		panel.getActionMap().put("inventory", new AbstractAction() {
			private static final long	serialVersionUID	= 5420406803870889067L;

			public void actionPerformed(ActionEvent arg0) {
				gameWindow.showExpandOrHideInventoryPanel();
			}
		});

		// Logout - Escape
		panel.getInputMap().put(KeyStroke.getKeyStroke("released ESCAPE"), "logout");
		panel.getActionMap().put("logout", new AbstractAction() {
			private static final long	serialVersionUID	= -7276035432882414925L;

			public void actionPerformed(ActionEvent e) {

				if (JOptionPane.showConfirmDialog(panel, "Moechten Sie sich wirklich ausloggen?", "", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_NO_OPTION)
					ServerConnection.terminateConnection("");
			}
		});
	}

	/**
	 * Wandelt die aktuell gedrueckten Richtungstasten in eine Richtung um und
	 * sendet die entsprechende <i>MovementInfo</i> an den Server.
	 * 
	 * Wenn der Spieler sich nicht mehr bewegt und sich beim letzten Mal noch
	 * bewegt hat, wird der Server darueber informiert, damit er den Status des
	 * GameCharacters auf STANDING aendern kann. Danach wird so lange auf neue
	 * MovementRequests verzichtet, bis der Spieler wieder eine Bewegungstaste
	 * drueckt.
	 * 
	 * @author Christian Hovestadt
	 */
	public void update() {
		int horDirection = 0, vertDirection = 0;
		if (upPressed || wPressed)
			vertDirection--;
		if (leftPressed || aPressed)
			horDirection--;
		if (downPressed || sPressed)
			vertDirection++;
		if (rightPressed || dPressed)
			horDirection++;

		if (horDirection != 0 || vertDirection != 0 || spacePressed || !lastDirectionStanding)
			ServerConnection.sendMessageToServer(new MovementRequest(CHARACTER_ID, levelMapID, horDirection, vertDirection, spacePressed));

		lastDirectionStanding = (horDirection == 0 && vertDirection == 0);
	}

	/**
	 * Diese Methode wird aufgerufen, wenn das Fenster den Fokus verliert
	 * (Zum Beispiel nach dem Tod des Spielers)
	 * 
	 * Alle gedrueckten Tasten werden wieder auf false gesetzt, da das
	 * GameWindow das Release-Event nicht mitbekommt, wenn es nicht im Fokus
	 * ist.
	 * 
	 * @author Christian Hovestadt
	 */
	public void resetDirections() {
		upPressed = leftPressed = downPressed = rightPressed = wPressed = aPressed = sPressed = dPressed = spacePressed = lastDirectionStanding = false;
	}

	public void setLevelMapID(int levelMapID) {
		this.levelMapID = levelMapID;
	}
}
