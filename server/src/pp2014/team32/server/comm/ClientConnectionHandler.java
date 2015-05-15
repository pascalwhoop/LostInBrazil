package pp2014.team32.server.comm;

import pp2014.team32.server.player.PlayerConnectionHandler;
import pp2014.team32.shared.entities.Player;
import pp2014.team32.shared.enums.ChatMessageType;
import pp2014.team32.shared.enums.MessageType;
import pp2014.team32.shared.messages.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Die Klasse, welche sich um die ankommenden Verbindungsanfragen der Benutzer kuemmert und diese verwaltet. Jeder
 * socket wird abgelegt und jeder Client bekommt einen eigenen thread fuer input und fuer output.
 *
 * @author Brokmeier, Pascal
 * @version 2014-05-12
 */
public class ClientConnectionHandler {

    private final static Logger LOGGER = Logger.getLogger(ClientConnectionHandler.class.getName());


    //Variablen
    private static final int SERVER_PORT = 6032;
    private static final int INPUT_QUEUE_LENGTH = 1000;
    private static final int SUPPORTED_USERS = 10;

    public static ServerSocket serverSocket;

    //alle offenen sockets
    public static HashMap<String, Socket> userConnectionsMap = new HashMap<>();

    //eine liste von objekten mit denen wir auf die inputStream threads zugreifen koennen um errorhandling zu erleichtern
    @SuppressWarnings("rawtypes")
	public static HashMap<String, Future> userInputThreadFutureMap = new HashMap<>();
    protected static HashMap<String, ObjectOutputStream> userObjectOutputStreams = new HashMap<>();

    //unsere input threads
    private static ThreadPoolExecutor clientInputThreadPool = new ThreadPoolExecutor(SUPPORTED_USERS, SUPPORTED_USERS, 10, TimeUnit.MINUTES, new ArrayBlockingQueue<Runnable>(INPUT_QUEUE_LENGTH));


    /**
     * Die Methode welche zur Initiierung des ClientConnectionHandlers aufgerufen wird.
     *
     * @throws IOException
     * @author Brokmeier, Pascal
     */
    public static void init() throws IOException {


        serverSocket = new ServerSocket(SERVER_PORT);

        //new background thread that takes messages from queue and sends them out
        ClientOutputService.initOutputQueueHandler();
        handleIncomingConnections();
    }


    /**
     * Blockt und wartet auf neue Verbindungen auf dem socket. Sobald eine Verbindung geoeffnet wird, wird {@link
     * #handleConnection(java.net.Socket)} aufgerufen und der socket uebergeben
     *
     * @author Brokmeier, Pascal
     */
    private static void handleIncomingConnections() {
        while (true) {
            try {
                handleConnection(serverSocket.accept());
            } catch (ClassNotFoundException | IOException e) {
                LOGGER.warning("severe error occured: " + e.getMessage());
            }
        }

    }

    /**
     * Logik, welche fuer neue Clients & deren sockets alle notwendigen ressourcen initiiert. Sie legt den Socket in
     * unsere {@link #userConnectionsMap} ab, legt ObjectOutputStreams in der {@link #userObjectOutputStreams} Map ab
     * und fuegt das {@link Future} des erstellten Threads zum lauschen auf User Input in die {@link
     * #userInputThreadFutureMap} Map.
     * <p/>
     * Die Methode kuemmert sich ausserdem um das Authentifizieren der Benutzer. Denn nach Erstellung der Verbindung kann
     * entweder eine "USERAUTHENTICATION" oder eine "REGISTRATIONREQUEST" Message empfangen werden, je nachdem ob der
     * Benutzer sich zum ersten mal anmeldet oder wiederholt. Beides handelt die Server Engine. Nur bei Erfolg des
     * jeweiligen Prozesses wird der Client zur Kommunikationslogik hinzugefuegt.
     *
     * @param client Der Socket der fuer den jeweiligen Client bei Verbindung erstellt wurde
     * @throws IOException
     * @throws ClassNotFoundException
     * @author Brokmeier, Pascal
     */
    private static void handleConnection(Socket client) throws IOException, ClassNotFoundException {
        LOGGER.info("Client connected: " + client.toString());

        //setting a timeout for sockets to make use of ping messages (2 seconds from client so 10 seconds timeout should suffice.
        //a SocketTimeoutException will be thrown if timeout expires
        client.setSoTimeout(10000);

        ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
        oos.flush();
        ObjectInputStream ois = new ObjectInputStream((client.getInputStream()));

        //erste Message vom Client ist immer eine userAuthentication
        Message message = (Message) ois.readObject();
        UserAuthentication userAuthentication;
        boolean success = false;
        String username = "";

        if (message.MESSAGE_TYPE == MessageType.USERAUTHENTICATION) {
            userAuthentication = (UserAuthentication) message;
            Player connectionPlayer = new Player(userAuthentication.USERNAME, userAuthentication.PASSWORD, null);
            success = PlayerConnectionHandler.newPlayerConnectionForAuthentication(connectionPlayer, userAuthentication.USERNAME);
            if (success) {
                username = userAuthentication.USERNAME;
            }
        }
        if (message.MESSAGE_TYPE == MessageType.REGISTRATIONREQUEST) {
            RegistrationRequest request = (RegistrationRequest) message;
            success = PlayerConnectionHandler.registerNewPlayer(request);
            if (success) {
                username = request.USERNAME;
            }
        }
        //wenn user authentifiziert wurde
        if (success) {
            //we have problems sometimes so before adding all again i make sure everything is out
            forceRemoveOldUserConnectionsIfPresent(username);

            userConnectionsMap.put(username, client);
            userObjectOutputStreams.put(username, oos);
            @SuppressWarnings({ "unchecked", "rawtypes" })
			Future inputFuture = clientInputThreadPool.submit(new ObjectStreamInputCallable(ois, username));
            userInputThreadFutureMap.put(username, inputFuture);
            ClientOutputService.addClientBuffer(username);
            sendMessageToUser(username, new AuthenticationResponse(username, success, "", PlayerConnectionHandler.getIDForUserName(username)));
            PlayerConnectionHandler.playerJoinedGame(username);
        }
        //user nicht richtig authentifiziert, wir senden antwort dass es nicht geklappt hat und schliessen die Verbindung
        else {
            oos.writeObject(new AuthenticationResponse(username, success, "user authentication not successful", PlayerConnectionHandler.getIDForUserName(username)));
            client.close();
        }

    }

    /**
     * Interface Methode fuer Server Engine um Nachrichten zu verschicken
     *
     * @param username der Client an den geschickt werden soll
     * @param message  die zu schickende Nachricht
     * @author Brokmeier, Pascal
     */
    public static synchronized void sendMessageToUser(String username, Message message) {
        ClientOutputService.putMessage(username, message);
    }

    /**
     * Interface Methode fuer Server Engine um Nachrichten zu verschicken
     *
     * @param message die zu schickende Nachricht
     * @param users   die Clients an die geschickt werden soll
     * @author Brokmeier, Pascal
     */
    public static void sendMessageToSetOfUsers(Message message, Set<String> users) {
        for (String user : users) {
            sendMessageToUser(user, message);
        }
    }

    /**
     * Interface Methode fuer Server Engine um Nachrichten zu verschicken
     *
     * @param message Die Nachricht die an den Benutzer geschickt werden soll
     */
    public static void sendBroadcastMessage(Message message) {
        for (String username : userConnectionsMap.keySet()) {
            sendMessageToUser(username, message);
        }
    }

    /**
     * @param username Der user fuer den ein Problem im Thread entstand. Wir beenden die Verbindung.
     * @author Brokmeier, Pascal
     */
    protected synchronized static void exceptionInThreadOccurred(String username) {
        //handle different errors through different error types thrown

        //log exception
        @SuppressWarnings("rawtypes")
		Future future = userInputThreadFutureMap.get(username);
        try {
            future.get();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        //perform orderly shutdown
        try {
            Socket socket = userConnectionsMap.get(username);
            socket.close();
            connectionClosed(username);

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    /**
     * Eine Methode, welche den Benutzer aus der Kommunikation sowie dem Gameplay entfernt. Z.B. zu nutzen wenn die
     * Pings nicht mehr reingekommen sind oder generell keine Nachricht mehr eingegangen ist (siehe timeout des sockets)
     * oder der Benutzer eine LOGOFF Message schickt
     *
     * @param username Der Benutzername des Clients
     * @author Brokmeier, Pascal
     */

    protected static void connectionClosed(String username) {
        forceRemoveOldUserConnectionsIfPresent(username);
        LOGGER.log(Level.INFO, "User " + username + " has left the game!");
        sendBroadcastMessage(new ChatMessage("", "User " + username + " has left the game!", ChatMessageType.SYSTEM));
        PlayerConnectionHandler.logoutUser(username);
    }

    /**
     * Hilfsmethode um sicher zu gehen, dass alle Informationen zum Benutzer aus der Kommunikation geloescht sind und
     * keine "Reste" vorhanden sind, sodass eine neue Verbindung des gleichen Users ohne Probleme von statten geht.
     *
     * @param username
     */
    protected static void forceRemoveOldUserConnectionsIfPresent(String username) {
        ClientOutputService.removeClientBuffer(username);
        userConnectionsMap.remove(username);
        userObjectOutputStreams.remove(username);
        @SuppressWarnings("rawtypes")
		Future userFuture = userInputThreadFutureMap.get(username);
        try {
            userFuture.cancel(true);
        } catch (Exception e) {
        }
        userInputThreadFutureMap.remove(username);
    }
}
