package pp2014.team32.server.clientRequestHandler;

import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;

import pp2014.team32.server.comm.ClientConnectionHandler;
import pp2014.team32.server.player.PlayerConnectionHandler;
import pp2014.team32.shared.enums.ChatMessageType;
import pp2014.team32.shared.messages.ChatMessage;
import pp2014.team32.shared.messages.Message;
import pp2014.team32.shared.utils.PropertyManager;

/**
 * Dieser RequestHandler kuemmert sich um alle Chatnachrichten.
 * 
 * @author Moritz Bittner
 * @author Peter Kings
 * 
 */
public class ChatHandler implements Runnable {
	// Hier werden einkommende Chat Nachrichten reingelegt
	private static ArrayBlockingQueue<Message>	chatMessageQueue	= new ArrayBlockingQueue<>(Integer.parseInt(PropertyManager.getProperty("server.MessageQueueSize")));

	/**
	 * Nimmt aktuelle Messages aus der Message Queue und sendet sie an alle
	 * aktiven User raus.
	 * 
	 * @author Peter Kings
	 */
	public void run() {
		while (true) {
			Message nextMessage;
			try {
				// Chat Message nehmen
				nextMessage = chatMessageQueue.take();
				// Message Object darauf instanziieren
				ChatMessage cM = (ChatMessage) nextMessage;
				// an alle aktiven User senden
				ClientConnectionHandler.sendMessageToSetOfUsers(cM, PlayerConnectionHandler.getSetOfActiveUserNames());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
	}

	/**
	 * Sendet uebergebenen String an alle Clients als FightChatMessage.
	 * 
	 * @param textMessage
	 * @author Moritz Bittner
	 */
	public static void sendFightChatMessage(String textMessage) {
		try {
			// fuegen der Queue ChatMessage hinzu mit aktueller Zeit, der
			// uebergebene Textmessage und der Derklarierung als Kampfnachricht
			chatMessageQueue.put(new ChatMessage(new Date(), "", textMessage, ChatMessageType.FIGHT));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sendet uebergebenen String an alle Clients als SystemChatMessage.
	 * 
	 * @param textMessage
	 * @author Moritz Bittner
	 */
	public static void sendSystemChatMessage(String textMessage) {
		try {
			// fuegen der Queue ChatMessage hinzu mit aktueller Zeit, der
			// uebergebene Textmessage und der Derklarierung als Systemnachricht
			chatMessageQueue.put(new ChatMessage(new Date(), "", textMessage, ChatMessageType.SYSTEM));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	// TODO Systemnachricht
	/**
	 * Sendet persoenliche Textnachricht an nur einen bestimmten Client.
	 * 
	 * @param gameCharacterUserName
	 * @param textMessage
	 * @author Moritz Bittner
	 */
	public static void sendPersonalSystemChatMessage(String gameCharacterUserName, String textMessage) {
		// wird direkt durchgereicht
		ChatMessage cM = new ChatMessage(new Date(), "Achtung: ", textMessage, ChatMessageType.SYSTEM);
		ClientConnectionHandler.sendMessageToUser(gameCharacterUserName, cM);
	}

	/**
	 * Abzuarbeitende ChatMessages werden hier uebergeben und dem in
	 * ArrayBlockingQueue des ChatHandlers geschrieben.
	 * 
	 * @param chatMessage
	 * @author Moritz BIttner
	 */
	public static void submitIncomingChatMessage(ChatMessage chatMessage) {
		try {
			chatMessageQueue.put(chatMessage);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
