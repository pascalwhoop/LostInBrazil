package pp2014.team32.client.comm;

import pp2014.team32.shared.messages.Message;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.logging.Logger;

/**
 * Dieses Runnable wird in einem seperaten Thread ausgefuehrt und schreibt die Objekte in den outputStream, sodass diese
 * den Server erreichen.
 *
 * @author Brokmeier, Pascal
 * @version 25.05.14
 */
public class ServerOutputRunnable implements Runnable {

    @SuppressWarnings("unused")
	private final static Logger LOGGER = Logger.getLogger(ServerOutputRunnable.class.getName());
    private final ObjectOutputStream oos;
    private final Message messageToSend;

    public ServerOutputRunnable(ObjectOutputStream oos, Message message) {
        this.oos = oos;
        messageToSend = message;
    }


    /**
     * @author Brokmeier, Pascal
     */
    @Override
    public void run() {
        try {
            //LOGGER.info("### OUTBOUND MESSAGE ###" + messageToSend.MESSAGE_TYPE);
            sendObject();

        } catch (IOException e) {
            //LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    /**
     * @author Brokmeier, Pascal
     */
    public synchronized void sendObject() throws IOException {
        oos.reset();
        oos.writeObject(messageToSend);
        oos.flush();
    }
}
