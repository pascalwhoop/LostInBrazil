package pp2014.team32.client.engine;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import pp2014.team32.client.comm.ServerConnection;
import pp2014.team32.client.gui.GameWindow;
import pp2014.team32.client.resources.Bullet;
import pp2014.team32.shared.entities.Creature;
import pp2014.team32.shared.entities.DrawableObject;
import pp2014.team32.shared.entities.FixedObject;
import pp2014.team32.shared.entities.MovableObject;
import pp2014.team32.shared.enums.AttributeType;
import pp2014.team32.shared.messages.AttributeChangeInfo;
import pp2014.team32.shared.messages.AuthenticationResponse;
import pp2014.team32.shared.messages.BulletAddition;
import pp2014.team32.shared.messages.ChatMessage;
import pp2014.team32.shared.messages.DrawableObjectAddition;
import pp2014.team32.shared.messages.FixedObjectRemoval;
import pp2014.team32.shared.messages.HealthUpdate;
import pp2014.team32.shared.messages.InventoryUpdate;
import pp2014.team32.shared.messages.LevelData;
import pp2014.team32.shared.messages.LevelUpgrade;
import pp2014.team32.shared.messages.Message;
import pp2014.team32.shared.messages.MovableObjectRemoval;
import pp2014.team32.shared.messages.MovementInfo;
import pp2014.team32.shared.messages.NewLevelDataRequest;
import pp2014.team32.shared.messages.Ping;
import pp2014.team32.shared.messages.TorchMessage;
import pp2014.team32.shared.messages.UncoverMessage;
import pp2014.team32.shared.utils.Coordinates;

/**
 * <i>ServerMessageHandler</i> interpretiert die Server-Nachrichten (Eigener
 * Thread)
 * Es wird eine <i>ArrayBlockingQueue</i>verwendet. Diese bietet die Methode
 * <i>take()</i>, die das naechste Element aus der Queue nimmt bzw. auf das
 * naechste Element wartet, wenn die Queue leer ist.
 * 
 * @author Christian
 * @version 24.06.2014
 */
public class ServerMessageHandler implements Runnable {
	private static ArrayBlockingQueue<Message>	messages							= new ArrayBlockingQueue<Message>(10000);
	private static Logger						LOGGER								= Logger.getLogger(ServerMessageHandler.class.getName());
	private static GameWindow					gameWindow;
	private static Boolean						lastAuthenticationWasRegistration	= false;

	public ServerMessageHandler(GameWindow gameWindow) {
		ServerMessageHandler.gameWindow = gameWindow;
	}

	/**
	 * Die Kommunikationseinheit benutzt diese Methode um eine Nachricht in die
	 * Messages-Queue zu legen.
	 * 
	 * @param m Die angekommene Server-Nachricht
	 * @author Christian Hovestadt
	 */
	public static void putMessage(Message m) {
		try {
			messages.put(m);

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Der Interpretationsthread nimmt die Nachrichten aus der Queue und
	 * interpretiert diese.
	 * 
	 * - MOVEMENTINFO: Die entsprechende Creature wird anhand ihrer ID gesucht
	 * und die Koordinaten werden aktualisiert. Bei Inkonsistenzen wird die
	 * LevelMap neu angefordert.\\
	 * - UNCOVERINFO: Der Fog of War wird in der Mini-Map fuer die uebergebenen
	 * Koordinaten entfernt.\\
	 * - ATTRIBUTECHANGE_INFO: Sucht die Creature anhand der uebergebenen ID und
	 * aktualisiert die Attributes. Wenn die Creature nicht existiert, wird eine
	 * neue LevelMap angefordert.\\
	 * - HEALTH_UPDATE: Spezialfall von AttributeChangeInfo, bei der nur die
	 * Gesundheit aktualisiert wird.\\
	 * - LEVEL_UPGRADE: Trigger vom Server, dass der Spieler ein Level-Upgrade
	 * bekommt. In der GUI wird daraufhin ein Popup geoeffnet, in dem der
	 * Spieler neue Erfahrungspunkte verteilen kann.\\
	 * - CHAT_MESSAGE: Eine neue ChatMessage wird dem MessagesPanel uebergeben.
	 * - LEVEL_DATA: Die aktuelle LevelMap wird aktualisiert und alle davon
	 * abhaengigen Werte werden in den entsprechenden Panels angepasst.\\
	 * - DRAWABLE_OBJECT_ADDITION: Es wird geprueft, ob es sich um ein neues
	 * MovableObject oder ein FixedObject handelt. Das neue Object wird dann zur
	 * entsprechenden HashMap der aktuellen LevelMap hinzugefuegt.\\
	 * - BULLET_ADDITION: Eine neue Fernkampfwaffen-Kugel wird hinzugefuegt und
	 * dann bis zum Erreichen des Ziels clientseitig vorgehalten und
	 * aktualisiert.\\
	 * - FIXEDOBJECTREMOVAL: Das FixedObject wird anhand der uebergebenen
	 * Koordinaten aus der HashMap der LevelMap entfernt.\\
	 * - MOVABLEOBJECTREMOVAL: Das MovableObject wird anhand der uebergebenen ID
	 * aus der HashMap der LevelMap entfernt.\\
	 * - CHARACTERDEADINFO: Trigger vom Server, dass das GameOver-Popup
	 * geoeffnet werden soll.\\
	 * - AUTHENTICATIONRESPONSE: Wenn die Authentifizierung erfolgreich war,
	 * wird das Spielfenster geoeffnet. Sonst kehrt das Programm zum LoginWindow
	 * bzw. zum RegistrationWindow mit einer entsprechenden Fehlermeldung
	 * zurueck.\\
	 * - INVENTORYUPDATE: Das Inventar des GameCharacters wird aktualisiert.\\
	 * - PING: Ich-bin-noch-da-Nachricht\\
	 * 
	 * Wenn eine Message eines anderen Typs ankommt, wird eine Warnung im Log
	 * ausgegeben.
	 * 
	 * @author Christian Hovestadt
	 */
	public static void init() {
		while (true) {
			try {
				Message message = messages.take();

				switch (message.MESSAGE_TYPE) {
				case MOVEMENTINFO:
					MovementInfo movementInfo = (MovementInfo) message;
					Creature creature = (Creature) gameWindow.getLevelMap().getMovableObjects().get(movementInfo.CREATURE_ID);
					if (creature != null) {
						if (creature.getX() == movementInfo.OLD_X && creature.getY() == movementInfo.OLD_Y)
							LOGGER.fine("Tried to move " + creature + ", but the old coordinates are not consistent.");
						creature.move(movementInfo.NEW_X, movementInfo.NEW_Y, movementInfo.CREATURE_STATUS_TYPE);
						gameWindow.updateViewpoint(creature);
					} else {
						LOGGER.warning("Got a MovementInfo for object ID " + movementInfo.CREATURE_ID + ", but this object doesn't exist.");
						ServerConnection.sendMessageToServer(new NewLevelDataRequest(gameWindow.getCharacterID()));
					}
					break;
				case UNCOVERMESSAGE:
					for (Coordinates c : ((UncoverMessage) message).COORDINATES)
						gameWindow.uncover(c);
					break;
				case ATTRIBUTECHANGEINFO:
					AttributeChangeInfo attributeChangeInfo = (AttributeChangeInfo) message;
					DrawableObject object = gameWindow.getLevelMap().getMovableObjects().get(attributeChangeInfo.CREATURE_ID);
					if (object instanceof Creature) {
						((Creature) object).setAttributes(attributeChangeInfo.newAttributes);
						gameWindow.updateAttributes(attributeChangeInfo.CREATURE_ID, attributeChangeInfo.newAttributes);
					} else {
						LOGGER.warning("Got a AttributeChangeInfo for object ID " + attributeChangeInfo.CREATURE_ID + ", but this object is not a creature.");
						ServerConnection.sendMessageToServer(new NewLevelDataRequest(gameWindow.getCharacterID()));
					}
					break;
				case HEALTHUPDATE:
					HealthUpdate healthUpdate = (HealthUpdate) message;
					DrawableObject drawableObject = gameWindow.getLevelMap().getMovableObjects().get(healthUpdate.CREATURE_ID);
					if (drawableObject instanceof Creature) {
						((Creature) drawableObject).getAttributes().set(AttributeType.HEALTH, healthUpdate.NEW_HEALTH);
						gameWindow.updateOneAttribute(healthUpdate.CREATURE_ID, AttributeType.HEALTH, healthUpdate.NEW_HEALTH);
					} else {
						LOGGER.warning("Got a AttributeChangeInfo for object ID " + healthUpdate.CREATURE_ID + ", but this object is not a creature.");
						ServerConnection.sendMessageToServer(new NewLevelDataRequest(gameWindow.getCharacterID()));
					}
					break;
				case LEVELUPGRADE:
					gameWindow.levelUpgrade(((LevelUpgrade) message).NEW_LEVEL);
					break;
				case CHATMESSAGE:
					gameWindow.addChatMessage((ChatMessage) message);
					break;
				case LEVELDATA:
					if (gameWindow.getCharacterID() == 0)
						messages.put(message);
					gameWindow.setLevelMap(((LevelData) message).LEVEL);
					break;
				case DRAWABLEOBJECTADDITION:
					DrawableObjectAddition drawableObjectAddition = (DrawableObjectAddition) message;
					if (drawableObjectAddition.DRAWABLE_OBJECT instanceof FixedObject)
						gameWindow.getLevelMap().addFixedObject((FixedObject) drawableObjectAddition.DRAWABLE_OBJECT);
					else
						gameWindow.getLevelMap().addMovableObject((MovableObject) drawableObjectAddition.DRAWABLE_OBJECT);
					break;
				case BULLETADDITION:
					BulletAddition bulletAddition = (BulletAddition) message;
					gameWindow.getLevelMap().addBullet(new Bullet(bulletAddition.AMMO_TYPE, bulletAddition.START, bulletAddition.DEST));
					break;
				case TORCHMESSAGE:
					TorchMessage torchMessage = (TorchMessage) message;
					if (torchMessage.TORCH_IN_USE)
						gameWindow.getLevelMap().addTorchCharacter(torchMessage.GAMECHARACTER_ID);
					else
						gameWindow.getLevelMap().removeTorchCharacter(torchMessage.GAMECHARACTER_ID);
					break;
				case FIXEDOBJECTREMOVAL:
					FixedObjectRemoval fixedObjectRemoval = (FixedObjectRemoval) message;
					gameWindow.getLevelMap().removeFixedObject(fixedObjectRemoval.FIXED_OBJECT_X, fixedObjectRemoval.FIXED_OBJECT_Y);
					break;
				case MOVABLEOBJECTREMOVAL:
					gameWindow.getLevelMap().removeMovableObject(((MovableObjectRemoval) message).MOVABLE_OBJECT_ID);
					break;
				case CHARACTERDEADINFO:
					gameWindow.gameOver();
					break;
				case AUTHENTICATIONRESPONSE:
					AuthenticationResponse authenticationResponse = (AuthenticationResponse) message;
					if (authenticationResponse.success) {
						ServerConnection.startPinging();
						gameWindow.showWindow();
						gameWindow.setCharacterID(authenticationResponse.CHARACTER_ID, authenticationResponse.USERNAME);
					} else if (lastAuthenticationWasRegistration) {
						// TODO ClientMain.backToRegistration();
						ServerConnection.terminateConnection("");
						JOptionPane.showMessageDialog(null, "Der Benutzername ist bereits vergeben.", "", JOptionPane.ERROR_MESSAGE);
					} else {
						ServerConnection.terminateConnection("");
						JOptionPane.showMessageDialog(null, "Der Benutzername und/oder das Password sind nicht korrekt.", "", JOptionPane.ERROR_MESSAGE);
					}
					lastAuthenticationWasRegistration = false;
					break;
				case INVENTORYUPDATE:
					gameWindow.setInventory(((InventoryUpdate) message).INVENTORY);
					break;
				case PING:
					LOGGER.info("Ping Roundtrip time: " + (System.currentTimeMillis() - ((Ping) message).TIMESTAMP.getTime()));
					break;
				default:
					LOGGER.warning("Got a " + message.MESSAGE_TYPE + ", but it is not handled in ServerMessageHandler.");
					break;
				}
			} catch (InterruptedException e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
			}
		}
	}

	@Override
	/**
	 * Start des Threads
	 * @author Christian Hovestadt
	 */
	public void run() {
		messages.clear();
		init();
	}

	public static void lastAuthenticationWasRegistration() {
		lastAuthenticationWasRegistration = true;
	}
}
