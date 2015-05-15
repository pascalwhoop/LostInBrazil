package pp2014.team32.client.comm;

import pp2014.team32.client.ClientMain;
import pp2014.team32.shared.enums.MessageType;
import pp2014.team32.shared.messages.LogOffMessage;
import pp2014.team32.shared.messages.Message;
import pp2014.team32.shared.messages.UserAuthentication;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Diese Klasse verwaltet die Verbindung zum Server und ist somit die Hauptklasse bzgl. der Kommunikation auf
 * Clientseite. Sie verbindet sich zum Server, stellt eine API fuer die Clientengine bereit und startet die Threads der
 * einzelnen Aufgaben der Kommunikation.
 *
 * @author Brokmeier, Pascal
 * @version 25.05.14
 */
public class ServerConnection {

    private final static Logger LOGGER = Logger.getLogger(ServerConnection.class.getName());

    private static final int SERVER_PORT = 6032;
    private static String serverAddress = "";
    protected static Socket server;
    private static ThreadPoolExecutor serverOutput = new ThreadPoolExecutor(1, 1, 10, TimeUnit.MINUTES, new ArrayBlockingQueue<Runnable>(1000));
    private static ObjectOutputStream oos;
    private static ObjectInputStream ois;
    private static Thread serverInputThread;
    private static Thread clientAliveThread;

    @SuppressWarnings("unused")
    private static UserAuthentication lastAuthentication;


    /**
     * Die Initiierungsmethode, welche die Kommunikation mit dem Server aufbaut. Hier wird ein Socker erstellt und der
     * {@link ObjectInputStream} und {@link ObjectOutputStream} erstellt. Ausserdem wird der Server Input Thread
     * gestartet, welcher auf Input vom Server lauscht
     *
     * @throws IOException
     * @author Brokmeier, Pascal
     */
    public static void init() throws IOException {
        server = createSocket();
        oos = new ObjectOutputStream(server.getOutputStream());
        oos.flush();
        ois = new ObjectInputStream(server.getInputStream());

        serverInputThread = new Thread(new ServerInputRunnable(ois));
        serverInputThread.start();

    }


    /**
     * Schickt eine Nachricht an den Server.
     *
     * @param message Die an den Server zu versendende Nachricht
     * @author Brokmeier, Pascal
     */
    public static synchronized void sendMessageToServer(Message message) {
        if (!server.isClosed()) {
            serverOutput.submit(new ServerOutputRunnable(oos, message));
        }
        if (message.MESSAGE_TYPE == MessageType.USERAUTHENTICATION) {
            lastAuthentication = (UserAuthentication) message;
        }
    }

    public static String getServerAddress() {
        return serverAddress;
    }

    public static void setServerAddress(String serverAddress) {
        ServerConnection.serverAddress = serverAddress;
    }


    /**
     * Erstellt die Verbindung zum Server mit gegebener IP & Port her.
     *
     * @return ein Socket mit der richtigen IP & Port
     * @throws IOException Sollte eine Exception beim erstellen des Sockets entstehen wird diese weitergereicht.
     * @author Brokmeier, Pascal
     */
    private static Socket createSocket() throws IOException {
        return new Socket(serverAddress, SERVER_PORT);
    }

    /**
     * erstellt einen neuen Thread mit der {@link ClientAliveRunnable}, welche regelmaessig Pings an den Server schickt
     *
     * @author Brokmeier, Pascal
     */
    public static void startPinging() {
        clientAliveThread = new Thread(new ClientAliveRunnable());
        clientAliveThread.start();
    }


    /**
     * Beendet die Verbindung zum server. Dazu schickt sie erst eine logOffMessage, schliesst dann den Socket und
     * unterbricht den Ping- und serverInputThread. Anschliessend wird wieder das LoginWindow angezeigt.
     *
     * @param errorMessage Ein Error Text der dem Benutzer angezeigt werden kann.
     * @author Brokmeier, Pascal
     */
    public static void terminateConnection(String errorMessage) {
        try {
            sendMessageToServer(new LogOffMessage());
            server.close();
            // interrupt both threads, since we'll create new ones.
            clientAliveThread.interrupt();
            serverInputThread.interrupt();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (NullPointerException e) {
        } finally {
            ClientMain.backToLogin(errorMessage);
        }
    }
}
