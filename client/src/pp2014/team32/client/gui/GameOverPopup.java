package pp2014.team32.client.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import pp2014.team32.client.comm.ServerConnection;
import pp2014.team32.shared.messages.RespawnRequest;

/**
 * Dieses Popup wird geoeffnet, wenn der Spieler stirbt.
 * Zwei Buttons ('Respawn' und 'Zurueck zum Login') werden auf dem Panel
 * angeordnet.
 * 
 * @author Christian Hovestadt
 * @version 30.6.14
 */
public class GameOverPopup extends JDialog {
	private static final long		serialVersionUID	= -8276342697331363604L;
	private final int				CHARACTER_ID;
	private static BufferedImage	background;
	private static final Logger		LOGGER;

	/**
	 * Das Hintergrundbild wird einmal bei der ersten Verwendung der Klasse
	 * statisch eingeladen.
	 */
	static {
		LOGGER = Logger.getLogger(GameOverPopup.class.getName());
		try {
			background = ImageIO.read(new File("images/gameOver.png"));
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Fehler: Bild nicht geladen");
		}
	}

	/**
	 * Erzeugt ein neues Popup mit zwei Buttons zum Respawn und Back To Login
	 * 
	 * @param characterID Wird fuer das Senden der RespawnRequest benoetigt.
	 * @author Christian Hovestadt
	 */
	public GameOverPopup(int characterID) {
		super();

		this.CHARACTER_ID = characterID;
		this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.PAGE_AXIS));
		this.setTitle("GAME OVER");
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		final GameOverPopup GAME_OVER_POPUP = this;

		// GameOverPanel
		JPanel gameOverPanel = new JPanel() {
			private static final long	serialVersionUID	= 1284790958851829681L;

			public void paintComponent(Graphics g) {
				g.drawImage(background, 0, 0, null);
			}
		};
		this.add(gameOverPanel);
		gameOverPanel.setPreferredSize(new Dimension(background.getWidth(), background.getHeight()));
		gameOverPanel.setLayout(null);

		// Back-to-Login-Button
		JButton backToLoginButton = new JButton("Zur\u00FCck zum Login");
		backToLoginButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
		Dimension buttonSize = backToLoginButton.getPreferredSize();
		backToLoginButton.setBounds(background.getWidth() - buttonSize.width - 5, 120, buttonSize.width, buttonSize.height);
		backToLoginButton.addActionListener(new ActionListener() {

			/**
			 * Beendet die Server-Verbindung und schliesst das Popup. Die
			 * ServerConnection veranlasst dann ueber
			 * <i>ClientMain.backToLogin()</i>, dass das GameWindow geschlossen
			 * und ein neues LoginWindow geoeffnet wird.\\
			 * Wird ausgefuehrt, wenn der backToLoginButton geklickt wird.
			 * 
			 * @author Christian Hovestadt
			 */
			public void actionPerformed(ActionEvent e) {
				ServerConnection.terminateConnection("");
				GAME_OVER_POPUP.dispose();
			}
		});
		gameOverPanel.add(backToLoginButton);

		// Respawn-Button
		JButton respawnButton = new JButton("Respawn");
		respawnButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
		respawnButton.setBounds(5, 120, buttonSize.width, buttonSize.height);
		respawnButton.addActionListener(new ActionListener() {

			/**
			 * Sendet einen RespawnRequest an den Server und schliesst das
			 * Popup. Wird ausgefuehrt, wenn der respawnButton geklickt wird.
			 */
			public void actionPerformed(ActionEvent e) {
				ServerConnection.sendMessageToServer(new RespawnRequest(CHARACTER_ID));
				GAME_OVER_POPUP.dispose();
			}
		});
		gameOverPanel.add(respawnButton);

		this.pack();
		this.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width) / 2 - getWidth() / 2, (Toolkit.getDefaultToolkit().getScreenSize().height) / 2 - getHeight() / 2);
		this.setVisible(true);
		gameOverPanel.setFocusable(true);
		gameOverPanel.requestFocusInWindow();
	}
}
