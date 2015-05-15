package pp2014.team32.client;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Logger;

import pp2014.team32.client.comm.ServerConnection;
import pp2014.team32.client.engine.ServerMessageHandler;
import pp2014.team32.client.gui.GameWindow;
import pp2014.team32.client.login.ChooseCharacter;
import pp2014.team32.client.login.LoginWindow;
import pp2014.team32.shared.utils.PropertyManager;

/**
 *
 * Team32: WAT
 * Teammitglieder: Bittner, Moritz,
 *                 Brokmeier, Pascal, 5868483
 *                 Dogan, Can,
 *                 Fischer, Mareike,
 *                 Kings, Peter,
 *
 * @author Christian Hovestadt, Pascal Brokmeier
 * @version 14.06.2014
 */
public class ClientMain {

	private final static Logger		LOGGER			= Logger.getLogger(ClientMain.class.getName());
	private final static String[]	PROPERTY_FILES	= { "prefs/settings.properties", "prefs/shared_settings.properties" };
	private static GameWindow		gameWindow;
	private static String			lastIp;
	private static Thread			serverConnectionHandlerThread;

	// private static boolean backToLoginLocked = false;

	/**
	 * Main-Methode des ganzen Programms
	 * Oeffnet ein neues <i>LoginWindow</i>
	 * 
	 * @author Christian Hovestadt
	 */
	public static void main(String[] args) {
		// Initialize PropertyManager
		new PropertyManager(Arrays.asList(PROPERTY_FILES), "prefs/logging.properties");
		new LoginWindow();
	}

	/**
	 * Schliesst das GameWindow, wenn es geoeffnet ist, und startet eine neue
	 * Server-Verbindung.
	 * 
	 * @param ip IP-Adresse des Servers
	 * @throws IOException Server-Verbindung fehlgeschlagen
	 * @author Brokmeier, Pascal
	 */
	public static void initializeConnection(String ip) throws IOException {
		if (gameWindow != null) {
			LOGGER.warning("Tried to initialize a session, but there is already one running.");
			return;
		}
		gameWindow = new GameWindow();
		lastIp = ip;

		try {
			ServerConnection.setServerAddress(ip);
			ServerConnection.init();
		} catch (IOException e) {
			gameWindow = null;
			throw e;
		}

		// ServerMessageHandler thread (handles the message queue input)
		serverConnectionHandlerThread = new Thread(new ServerMessageHandler(gameWindow));
		serverConnectionHandlerThread.start();
	}

	/**
	 * Schliesst das aktuelle Fenster und kehrt zum Login zurueck.
	 * 
	 * @author Christian Hovestadt
	 */
	public static void backToLogin() {

		if (gameWindow != null)
			gameWindow.dispose();
		gameWindow = null;

		new LoginWindow(lastIp);
	}

	/**
	 * Erweiterung von backToLogin, bei der eine zusaetzlich eine Fehlermeldung
	 * ueber JOptionPane ausgegeben wird.
	 * 
	 * @param errorMessage Fehlermeldung, die ausgegeben werden soll
	 * @author Christian Hovestadt
	 */
	public static void backToLogin(String errorMessage) {
		backToLogin();
		// TODO JOptionPane.showMessageDialog(null, errorMessage, "",
		// JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Schliesst das aktuelle Fenster und wechselt zur Registrierung.
	 * 
	 * @author Christian Hovestadt
	 * @deprecated
	 */
	public static void backToRegistration() {
		if (gameWindow != null)
			gameWindow.dispose();
		gameWindow = null;

		new ChooseCharacter();
	}
}