package pp2014.team32.server.comm;

import pp2014.team32.shared.messages.BulkMessagesPackage;
import pp2014.team32.shared.messages.Message;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

/**
 * Diese Klasse sammelt Messages fuer einzelne Benutzer und packt sie in eine gemeinsame Nachricht. So werden nicht ~
 * 25* 30 Nachrichten / Sekunde / Benutzer geschickt sondern nur pro Frame (1/25 sec) eine BulkMessage fuer alle
 * Nachrichten / User. Das ist viel weniger als vorher. Grund: Wir haben etwa 30 Creeps pro Map. Wenn sich diese alle
 * bewegen haben wir 30 (creeps) * 25 (frames) * Player Nachrichten + R(sonstiges) / Sekunde. Jetz haben wir max Player
 * * Frames / Sekunde Nachrichten.
 *
 * @author Brokmeier, Pascal
 * @version 20.06.14
 */
public class ClientOutputService {
    private final static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ClientOutputService.class.getName());
    private static HashMap<String, ArrayList<Message>> messagesForClient = new HashMap<>();
    private static long lastSendTimeFinished = System.currentTimeMillis();
    private static Lock lockForMessages = new ReentrantLock();


    /**
     * Startet einen neuen thread, welcher die Nachrichten aus der Queue nimmt und an die jeweiligen Benutzer schickt
     *
     * @author Brokmeier, Pascal
     */
    protected static void initOutputQueueHandler() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean exceptionOccured = false;
                while (!exceptionOccured) {
                    try {
                        runTimedBulkSending();
                    } catch (IOException | InterruptedException e) {
                        LOGGER.warning("ClientOutputService was faulty and had to be restarted \n" + e.getMessage());
                        exceptionOccured = true;
                        initOutputQueueHandler();
                    } catch (NullPointerException e) {
                        //TODO ggf. automatisch restarten
                        exceptionOccured = true;
                        LOGGER.log(Level.SEVERE, "Error in Communication, Server must be restarted");
                    }

                }
            }
        }).start();
    }


    /**
     * Methode zum ablegen von neuen Messagenachrichten. Buffert die Message und schickt sie gemeinsam mit vielen
     * anderen los sobald es Zeit ist.
     *
     * @param username Der Benutzername des Clients an den gesendet werden soll.
     * @param message  Die Nachricht die geschickt werden soll
     */
    protected static void putMessage(String username, Message message) {
        lockForMessages.lock();
        LOGGER.info("+++ putting message for client into queue " + message.MESSAGE_TYPE);

        ArrayList<Message> buffer = getMessageBufferForUser(username);
        if (buffer != null) {
            buffer.add(message);
        }
        lockForMessages.unlock();

    }


    /**
     * nimmt die Messages fuer jeden Benutzer, packt sie in eine BulkMessage und schickt sie an die Benutzer. Danach
     * schlafen wir so lange, dass wir im endeffekt alle 40ms die Bulkmessages an die Benutzer schicken.
     *
     * Also:
     *
     * ....________~SENDING~________(40ms)_________~SENDING~________(80ms)_________~SENDING~________(120ms)_________....
     *
     * @throws IOException
     * @throws InterruptedException
     * @author Brokmeier, Pascal
     */

    public static void runTimedBulkSending() throws IOException, InterruptedException {

        lockForMessages.lock(); //wir sperren die Buffer
        for (Map.Entry<String, ArrayList<Message>> entry : messagesForClient.entrySet()) {
            if (entry.getValue().size() != 0) {
                BulkMessagesPackage messagePackage = new BulkMessagesPackage(entry.getKey(), entry.getValue());
                performSend(messagePackage);
                entry.getValue().clear();
            }
        }
        lockForMessages.unlock();
        sleepAppropriateTime();
    }

    /**
     * Berechnet die notwendige Zeit die geschlafen werden muss. Dies ist eine Hilfsfunktion fuer {@link #runTimedBulkSending()}.
     * @throws InterruptedException
     */
    private static void sleepAppropriateTime() throws InterruptedException {
        long nextStart = lastSendTimeFinished + 40;
        long currentTime = System.currentTimeMillis();
        long sleepTime = nextStart - currentTime;
        if (sleepTime <= 0) { //wir sollten schon wieder schicken da senden ueber 40millis gebraucht hat
            LOGGER.warning("+++ sending messages took too long. It doesnt hurt but it might cause lag");
            lastSendTimeFinished = System.currentTimeMillis();
        } else {              //noch zeit bis zum naechsten geplanten start (alle 40 millis)
            Thread.sleep(sleepTime);
            lastSendTimeFinished = System.currentTimeMillis();
        }
    }


    /**
     * Gibt uns die Liste zurueck und initialisiert sie, falls noch nicht vorhanden
     *
     * @param username Der Benutzername des Clients an den gesendet werden soll.
     * @return Die Bufferliste die fuer den jeweiligen Client vorgehalten wird.
     */
    private static ArrayList<Message> getMessageBufferForUser(String username) {
        return messagesForClient.get(username);
    }


    /**
     * Tatsaechliches schicken wird hier durchgefuehrt.
     *
     * @param messagePackage ein MessageQueueEntry mit Username & Message
     * @throws IOException
     */
    private static synchronized void performSend(BulkMessagesPackage messagePackage) throws IOException {
        ObjectOutputStream oos = ClientConnectionHandler.userObjectOutputStreams.get(messagePackage.USERNAME);
        if (oos != null) {
            //TODO is reset really necessary
            LOGGER.info("### sending bulkMessage to client");
            oos.writeObject(messagePackage);
            oos.flush();
            oos.reset();
            //ClientConnectionHandler.userObjectOutputStreams.get(entry.getUsername()).writeUnshared(entry.getMessage());
        }

    }

    protected static void addClientBuffer(String username) {
        messagesForClient.put(username, new ArrayList<Message>());
    }

    /**
     * @author Brokmeier, Pascal
     * @param username
     */
    protected static void removeClientBuffer(String username) {
        lockForMessages.lock(); //wir sperren die Buffer
        messagesForClient.remove(username);
        lockForMessages.unlock(); //wir entsperren die Buffer
    }


}
