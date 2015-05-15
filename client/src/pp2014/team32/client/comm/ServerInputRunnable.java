package pp2014.team32.client.comm;

import pp2014.team32.client.engine.ServerMessageHandler;
import pp2014.team32.shared.enums.MessageType;
import pp2014.team32.shared.messages.BulkMessagesPackage;
import pp2014.team32.shared.messages.Message;

import java.io.EOFException;
import java.io.ObjectInputStream;
import java.util.logging.Logger;

/**
 *
 * Die Runnable, welche die Nachrichten des Servers entgegen nimmt und diese an die Client Engine weitergibt.
 *
 * @author Brokmeier, Pascal
 * @version 25.05.14
 */
public class ServerInputRunnable implements Runnable {

    @SuppressWarnings("unused")
	private final static Logger LOGGER = Logger.getLogger(ServerInputRunnable.class.getName());
    private final ObjectInputStream ois;

    public ServerInputRunnable(ObjectInputStream ois){
        this.ois = ois;
    }

    /**
     * Diese Methode nimmt immer wieder Objekte vom Server entgegen (via Socket) und reicht diese weiter an die {@link #unpackBulkAndForwardToClientEngine(pp2014.team32.shared.messages.BulkMessagesPackage)} Methode, welche diese dann entpackt.
     * @author Brokmeier, Pascal
     */
    @Override
    public void run() {
        boolean errorOccured = false;
        while(!errorOccured && !ServerConnection.server.isClosed()){
            try{
                Message message = (Message) ois.readObject();
                //LOGGER.info("### MESSAGE ###" + message.MESSAGE_TYPE + " received");

                if(message.MESSAGE_TYPE == MessageType.BULKMESSAGEPACKAGE){
                    unpackBulkAndForwardToClientEngine((BulkMessagesPackage) message);
                }
                if(message.MESSAGE_TYPE == MessageType.AUTHENTICATIONRESPONSE){
                    ServerMessageHandler.putMessage(message);
                }
            }
            catch (EOFException  e){
               ServerConnection.terminateConnection("Connection closed");
               errorOccured = true;
            }
            catch (Exception e){
                //LOGGER.log(Level.SEVERE, e.getMessage(), e);
                errorOccured = true;
            }


        }
    }

    /**
     * entpackt die Nachrichtenpakete in einzelne Messages, welche dann an die Client Engine weitergegeben werden.
     * @author Brokmeier, Pascal
     * @param messagesPackage Ein Nachrichtenpaket
     */

    private void unpackBulkAndForwardToClientEngine(BulkMessagesPackage messagesPackage) {
        for(Message m : messagesPackage.MESSAGES){
            ServerMessageHandler.putMessage(m);
        }
    }
}
