package pp2014.team32.server;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;

import pp2014.team32.server.Database.DatabaseConnection;
import pp2014.team32.server.EnemyHandling.EnemyHandler;
import pp2014.team32.server.LevelMaps.LevelMapsHandler;
import pp2014.team32.server.clientRequestHandler.ChatHandler;
import pp2014.team32.server.clientRequestHandler.InventoryActionRequestHandler;
import pp2014.team32.server.clientRequestHandler.MovementRequestHandler;
import pp2014.team32.server.clientRequestHandler.TriggerRequestsHandler;
import pp2014.team32.server.comm.ClientConnectionHandler;
import pp2014.team32.server.comm.ClientObjectInputHandler;
import pp2014.team32.server.player.PlayerConnectionHandler;
import pp2014.team32.server.updateTimer.CharacterUpdateTimer;
import pp2014.team32.shared.utils.PropertyManager;

/**
 * @author Brokmeier, Pascal
 * @author Bittner, Moritz
 * @author Kings, Peter
 */
public class ServerMain {

	private final static Logger	LOGGER	= Logger.getLogger(ServerMain.class.getName());

	static {
		List<String> propertyPaths = new LinkedList<String>();

		String s = (new File("")).getAbsolutePath();

		String loggingPath;
		if (s.substring((s.length() - 3), (s.length())).equals("bin")) {
			propertyPaths.add("../../checkout/server/prefs/settings.properties");
			propertyPaths.add("../../checkout/server/prefs/shared_settings.properties");
			loggingPath = "../../checkout/server/prefs/logging.properties";
		} else {
			propertyPaths.add("prefs/settings.properties");
			propertyPaths.add("prefs/shared_settings.properties");
			loggingPath = "prefs/logging.properties";
		}

		// Initialize PropertyManager
		new PropertyManager(propertyPaths, loggingPath);
	}

	public static void main(String[] args) {
		// bevor irgendwas gemacht wird: erstmal die Letzte ObjectID aus der
		// Datenbank laden.
		DatabaseConnection.loadLatestObjectIDFromDatabase();

		LOGGER.info("ServerMain called");
		System.out.println("ServerMain called");

		// to initiate our ClientConnectionHandler
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					ClientConnectionHandler.init();
				} catch (IOException e) {
					LOGGER.log(Level.SEVERE, "The Server could not be started. Is the address already in use?");
					// this should not happen, shutting down
					System.exit(1);
				}
			}
		}).start();

		// to initiate our ClientObjectInputHandler
		new Thread(new Runnable() {
			@Override
			public void run() {
				ClientObjectInputHandler.init();
			}
		}).start();

		// Client Connection
		/*
		 * try {
		 * ClientConnectionHandler cch = new ClientConnectionHandler();
		 * } catch (IOException e) {
		 * e.printStackTrace();
		 * }
		 */
		try {
			PlayerConnectionHandler.databaseConnectionAtGameStart();
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
		/*
		 * ************************************************************
		 * Threads starten
		 * ************************************************************
		 */
		/*
		 * Dieser Thread erstellt bei Spielstart alle Level
		 */
		Thread levelMapsHandler = new Thread(new LevelMapsHandler()); // TODO
		levelMapsHandler.start();
		/*
		 * Dieser Thread bearbteitet MovementRequests
		 */
		Thread movementHandlerThread = new Thread(new MovementRequestHandler()); // TODO
		movementHandlerThread.start();
		/*
		 * Dieser Thread bearbeitet Interactions
		 */
		Thread inventoryActionHandlerThread = new Thread(new InventoryActionRequestHandler());
		inventoryActionHandlerThread.start();
		/*
		 * Dieser Thread bearbeitet Chatnachrichten
		 */
		Thread chatHandlerThread = new Thread(new ChatHandler());
		chatHandlerThread.start();
		/*
		 * Dieser Thread committet alle x Sekunden in die Database
		 */
		// TimerTask (fuert alle x Sekunden die run Methode aus): Datenbank
		// sollte alle x Sekunden aktuallisiert werden. Dafuer einen Timer, der
		// alle x Sekunden die run Methode aufruft.
		Timer dataTimer = new Timer();
		dataTimer.scheduleAtFixedRate(new DatabaseConnection(), Integer.parseInt(PropertyManager.getProperty("server.DatabaseUpdateTimer")) * 1000,
				Integer.parseInt(PropertyManager.getProperty("server.DatabaseUpdateTimer")) * 1000);
		/*
		 * Dieser Thread bewegt die Enemies in festgesetzten Teilintervallen,
		 * sodass diese planlos umherirren
		 */
		Thread enemyHandlerThread = new Thread(new EnemyHandler(LevelMapsHandler.getLevelMaps()));
		enemyHandlerThread.start();
		/*
		 * Dieser Thread bearbeitet alle Trigger Requests
		 */
		Thread triggerRequestThread = new Thread(new TriggerRequestsHandler());
		triggerRequestThread.start();

		/*
		 * Dieser Timer kuemmert sich um die Gesundheitsaufladung und
		 * Angriffssschlagpausierung von GameCharactern
		 */
		Timer characterUpdateTimer = new Timer();
		// wird alle 1000/25 ms aufgerufen (25 mal pro Sekunde)
		characterUpdateTimer.scheduleAtFixedRate(new CharacterUpdateTimer(), 1000, 1000 / 25);
	}

}
