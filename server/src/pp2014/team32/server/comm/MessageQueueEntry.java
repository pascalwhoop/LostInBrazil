package pp2014.team32.server.comm;

import pp2014.team32.shared.messages.Message;

/**
 * Kleine Helferklasse, welche eine Nachricht und einem Benutzer enthaehlt und in eine ArrayBlockingQueue abgelegt werden kann.
 *
* @author Brokmeier, Pascal
 * @version 19.06.14
 */
public class MessageQueueEntry {

    private String username;
    private Message message;

    public MessageQueueEntry(String username, Message message) {
        this.message = message;
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}
