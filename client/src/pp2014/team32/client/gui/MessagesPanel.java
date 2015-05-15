package pp2014.team32.client.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

import pp2014.team32.shared.messages.ChatMessage;
import pp2014.team32.shared.utils.PropertyManager;

/**
 * Das Messages-Panel
 * 
 * @author Mareike Fischer
 * @author Christian Hovestadt
 */

public class MessagesPanel extends JPanel {

	private static final long				serialVersionUID	= -2440229143454727922L;
	private static BufferedImage			background;
	private static Logger					log					= Logger.getLogger(MessagesPanel.class.getName());
	private static final Integer			MESSAGETIMER		= Integer.parseInt(PropertyManager.getProperty("messagesPanel.messageTimer")) * GameWindow.getRepaintsPerSec();
	private HashMap<ChatMessage, JLabel>	messageLabels;
	private HashMap<ChatMessage, Integer>	messageTimers;
	private JPanel							messagesPanel;
	private JScrollPane						messagesScrollPane;

	/**
	 * Das Hintergrundbild wird einmal bei der ersten Verwendung der Klasse
	 * statisch eingeladen.
	 * 
	 * @author Mareike Fischer
	 */
	static {
		try {
			background = ImageIO.read(new File("images/messages.png"));
		} catch (Exception e) {
			log.log(Level.SEVERE, "Fehler: Bild nicht geladen");
		}
	}

	/**
	 * Das MessagesPanel besteht (hauptsaechlich) aus einem ScrollPane, zu dem
	 * die ankommenden Nachrichten hinzugefuegt werden.
	 * 
	 * Fuer jede angezeigte Message wird die time to live in der HashMap
	 * <i>messageTimers</i> verwaltet.
	 * 
	 * @author Christian Hovestadt
	 */
	public MessagesPanel() {

		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.messageLabels = new HashMap<ChatMessage, JLabel>();
		this.messageTimers = new HashMap<ChatMessage, Integer>();
		this.messagesPanel = new JPanel();
		this.messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.PAGE_AXIS));
		this.messagesPanel.setOpaque(false);
		this.messagesScrollPane = new JScrollPane(messagesPanel);
		this.messagesScrollPane.getViewport().setOpaque(false);
		this.messagesScrollPane.setOpaque(false);
		this.messagesScrollPane.setPreferredSize(new Dimension(getWidth(), getHeight()));
		this.messagesScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		this.messagesScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		this.messagesScrollPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0), BorderFactory.createEmptyBorder()));
		this.add(Box.createRigidArea(new Dimension(0, background.getHeight())));
		this.add(messagesScrollPane, BorderLayout.SOUTH);

	}

	/**
	 * Methode, um Nachrichten anzuzeigen
	 * Kampfnachrichten, Chatnachrichten und Systemnachrichten werden in
	 * unterschiedlichen Farben angezeigt.
	 * 
	 * Das ScrollPane wird maximal nach unten gescrollt.
	 * 
	 * @author Mareike Fischer
	 */
	public void addChatMessage(ChatMessage message) {
		JLabel label = null;
		switch (message.CHAT_MESSAGE_TYPE) {
		case FIGHT:
			label = new JLabel(message.TEXT);
			label.setForeground(Color.RED);
			break;
		case CHAT:
			label = new JLabel("[" + message.USERNAME + "] " + message.TEXT);
			label.setForeground(Color.WHITE);
			break;
		case SYSTEM:
			label = new JLabel(message.TEXT);
			label.setForeground(Color.YELLOW);
			break;
		}

		messagesPanel.add(label);
		this.validate();
		messageLabels.put(message, label);
		messageTimers.put(message, MESSAGETIMER);
		JScrollBar vertical = messagesScrollPane.getVerticalScrollBar();
		vertical.setValue(vertical.getMaximum());
	}

	/**
	 * 
	 * Aktualisierung der angezeigten Nachrichten: Reduziert den Timer jeder
	 * Message um 1. Wenn die Nachrichten ihre Anzeigedauer ueberschritten
	 * haben, werden sie aus dem Panel entfernt.
	 * 
	 * @author Christian Hovestadt
	 */
	public void update() {
		// Testcode
		/*
		 * if (Math.random() <= 0.02)
		 * {
		 * ServerConnection.sendMessageToServer(new
		 * ChatMessage(Calendar.getInstance().getTime(), "Testuser",
		 * "Nachricht " + counter++));
		 * 
		 * }
		 */
		// Decrement timers
		List<ChatMessage> toRemove = new LinkedList<ChatMessage>();
		for (ChatMessage m : messageTimers.keySet()) {
			messageTimers.put(m, messageTimers.get(m) - 1);
			// Check for timeout
			if (messageTimers.get(m) <= 0)
				toRemove.add(m);
		}
		for (ChatMessage m : toRemove) {
			messagesPanel.remove(messageLabels.get(m));
			messagesPanel.validate();
			messageLabels.remove(m);
			messageTimers.remove(m);
			this.validate();
		}
	}

	/**
	 * Zeichnet das Hintergrundbild.
	 * 
	 * @author Mareike Fischer
	 */
	public void paintComponent(Graphics g) {
		update();
		g.drawImage(background, 0, 0, null);
	}
}