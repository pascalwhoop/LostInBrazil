package pp2014.team32.server.comm;

import pp2014.team32.shared.messages.Message;

import java.io.EOFException;
import java.io.ObjectInputStream;
import java.net.SocketTimeoutException;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

/**
 * Diese Klasse ist einer von einem Thread ausfuehrbarer Callable, welcher dann auf den ObjectInputStream horcht, welcher beim konstruieren uebergeben wurde.
 * Sobald eine Message reinkommt wird diese an die Engine weitergegeben.
 *
 * @author Brokmeier, Pascal
 */
@SuppressWarnings("rawtypes")
public class ObjectStreamInputCallable implements Callable {

    private final static Logger LOGGER = Logger.getLogger(ObjectStreamInputCallable.class.getName());

    private ObjectInputStream ois;
    private final String USERNAME;

    private boolean connectionClosed;

    public ObjectStreamInputCallable(ObjectInputStream ois, String username) {
        this.ois = ois;
        connectionClosed = false;
        this.USERNAME = username;
    }


    /**
     *
     * Die Methode welche beim Start des threads ausgefuehrt wird. Hier werden alle Nachrichten vom Client empfangen und in Objekte umgewandelt.
     * Anschliessend werden diese dem ClientObjectInputHandler uebergeben, welche die Nachrichten aller Clients verarbeitet.
     * @return
     * @author Brokmeier, Pascal
     */
    @Override
    public Object call() throws Exception {
        LOGGER.info("started reading input for user in thread with ID: " + Thread.currentThread().getId());

        while (!connectionClosed) {
            try {
                Message message = (Message) ois.readObject();
                LOGGER.info("### MESSAGE ###" + message.MESSAGE_TYPE + " received ### client:" + USERNAME + " and thread " + Thread.currentThread().getId());
                ClientObjectInputHandler.handleReceivedMessage(USERNAME, message);

            } catch (EOFException | SocketTimeoutException e) {
                //the inputStream is closed (EOF means socket closed)
                connectionClosed = true;
                kickUser();
            } catch (Exception e) {
                //something else happened. for now we will end thread and just act as if the socket was closed. error handling should be improved
                //TODO improve error handling for input connection errors
                connectionClosed = true;
                ClientConnectionHandler.exceptionInThreadOccurred(USERNAME);
            }
        }
        //this callable doesnt need to return anything
        return null;
    }


    /**
     * ruft ClientConnectionHandler.connectionClosed auf und sorgt somit dafuer, dass der Benutzer das spiel verlaesst.
     * @author Brokmeier, Pascal
     */
    private void kickUser() {
        ClientConnectionHandler.connectionClosed(USERNAME);
    }
}
