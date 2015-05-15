package pp2014.team32.server.comm;

import pp2014.team32.server.clientRequestHandler.*;
import pp2014.team32.server.player.PlayerConnectionHandler;
import pp2014.team32.shared.entities.Player;
import pp2014.team32.shared.messages.*;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Statische Klasse, welche die Nachrichten von den Usern managed und in einer
 * Queue ablegt, sodass diese sequentiell
 * abgearbeitet werden.
 *
 * @author Brokmeier, Pascal, Moritz Bittner, Peter Kings
 * @version 13.05.14
 */
public class ClientObjectInputHandler {

	private final static Logger								LOGGER		= Logger.getLogger(ClientObjectInputHandler.class.getName());

	private static final int								QUEUE_SIZE	= 10000;
	private static ArrayBlockingQueue<MessageQueueEntry>	objectQueue	= new ArrayBlockingQueue<>(QUEUE_SIZE);

	/**
	 * Legt die uebergebene Nachricht (vom Client) in die Queue fuer den Server.
	 *
	 * @param username
	 * @param message
	 * @author Brokmeier, Pascal
	 */
	public static synchronized void handleReceivedMessage(String username, Message message) {
		try {
			objectQueue.put(new MessageQueueEntry(username, message));
		} catch (InterruptedException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	/**
	 * @author Bittner, Moritz
	 */

	public static void init() {
		while (true) {
			try {
				MessageQueueEntry messageQueueEntry = objectQueue.take();

				switch (messageQueueEntry.getMessage().MESSAGE_TYPE) {
				case MOVEMENTREQUEST:
					MovementRequest mr = (MovementRequest) messageQueueEntry.getMessage();
					LOGGER.info(mr.toString());
					MovementRequestHandler.submitMovementRequest(mr);
					break;
				case CHATMESSAGE:
					ChatMessage cm = (ChatMessage) messageQueueEntry.getMessage();
					ChatHandler.submitIncomingChatMessage(cm);
					break;
				case INVENTORYACTIONREQUEST:
					InventoryActionRequest moir = (InventoryActionRequest) messageQueueEntry.getMessage();
					InventoryActionRequestHandler.submitInteractionRequest(moir);
					break;
				case ATTRIBUTEUPGRADEREQUEST:
					TriggerRequestsHandler.submitIncommingOccasionalRequest(messageQueueEntry.getMessage());
					break;
				case RESPAWNREQUEST:
					TriggerRequestsHandler.submitIncommingOccasionalRequest(messageQueueEntry.getMessage());
					break;
				case USERAUTHENTICATION:
					UserAuthentication ua = (UserAuthentication) messageQueueEntry.getMessage();
					Player connectionPlayer = new Player(ua.USERNAME, ua.PASSWORD, null);
		            PlayerConnectionHandler.newPlayerConnectionForAuthentication(connectionPlayer, ua.USERNAME);
					break;
				case REGISTRATIONREQUEST:
					System.out.println("Registrierung erhalten.");
					PlayerConnectionHandler.registerNewPlayer((RegistrationRequest) messageQueueEntry.getMessage());
					break;
				case PING:
					ClientConnectionHandler.sendMessageToUser(messageQueueEntry.getUsername(), messageQueueEntry.getMessage());
					break;
				case LOGOFFMESSAGE:
					ClientConnectionHandler.connectionClosed(messageQueueEntry.getUsername());
					break;
				case CRAFTINGREQUEST:
					TriggerRequestsHandler.submitIncommingOccasionalRequest(messageQueueEntry.getMessage());
					break;
				case NEWLEVELDATAREQUEST:
					TriggerRequestsHandler.submitIncommingOccasionalRequest(messageQueueEntry.getMessage());
					break;
				default:
					break;

				}

				// for now just the dummy end to test full blown multi threaded
				// client server communication
				/*
				 * if (o instanceof String) {
				 * String obj = (String) o;
				 * System.out.println(obj);
				 * }
				 */
			} catch (InterruptedException e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
			}
		}
	}

}
