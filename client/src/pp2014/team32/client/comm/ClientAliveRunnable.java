package pp2014.team32.client.comm;

import pp2014.team32.shared.messages.Ping;

/**
 * Runnable fuer einen Thread, welcher regelmaessig eine Ping Nachricht an den Server schickt. Dieser horcht auf Nachrichten vom Client und wenn mehrere Pings nicht ankommen, wird der Client disconnected.
 *
 * @author Brokmeier, Pascal
 * @version 16.06.14
 */
public class ClientAliveRunnable implements Runnable{

    //private final static Logger LOGGER = Logger.getLogger(ClientAliveRunnable.class.getName());
    private final int TIMEOUT = 2000;


    /**
     * Die Run Methode, welche ausgefuehrt wird sobald der Thread startet
     */
    @Override
    public void run(){
        boolean errorOccured = false;
        while(!errorOccured && !ServerConnection.server.isClosed()){
            try {
                ServerConnection.sendMessageToServer(new Ping());
                Thread.sleep(TIMEOUT);
            }catch (InterruptedException e){
                //LOGGER.log(Level.SEVERE, e.getMessage(), e);
                errorOccured = true;
            }

        }
    }
}
