package pp2014.team32.client.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.io.File;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import pp2014.team32.shared.entities.Attributes;
import pp2014.team32.shared.enums.AttributeType;

/**
 * Dieses Panel zeigt die aktuellen Attribute des Spielers an. Dazu wird fuer
 * jedes Attribut eine auf 0-100 skalierte JProgressBar verwendet. Angeordnet wird das ganze
 * untereinander in einem GridLayout. Im untersten Level steht das aktuelle
 * Level des Spielers als JLabel.
 * 
 * @author Mareike Fischer
 * @version 29.6.14
 */
public class CharacterPanel extends JPanel {

	private static final long						serialVersionUID	= 8044736803027670255L;
	private static Image							background;
	private JPanel									characterContent, helpPanel;
	private static Logger							log					= Logger.getLogger(MessagesPanel.class.getName());
	private HashMap<AttributeType, JProgressBar>	progressBars;
	private JLabel									levelLabel;

	/**
	 * Das Hintergrundbild wird statisch geladen.
	 */
	static {
		try {
			background = ImageIO.read(new File("images/character.png"));
		} catch (Exception e) {
			log.log(Level.SEVERE, "Fehler: Bild nicht geladen");
		}
	}

	/**
	 * * Ein Box-Layout wird gesetzt, um einen Bereich festzulegen, 
	 * in dem untereinander die einzelnen Progress Bars angeordnet werden. 
	 * Das Help-Panel hilft dabei, rechts und links einen Abstand 
	 * von 15 Pixeln einzuhalten.
	 * Die ProgressBars werden durch ein Grid-Layout angeordnet.
	 * Fuer jeden Attributstyp wird eine ProgressBar erzeugt.
	 * 
	 * @author Mareike Fischer
	 */
	public CharacterPanel() {

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.characterContent = new JPanel();
		this.characterContent.setOpaque(false);
		this.helpPanel = new JPanel();
		this.helpPanel.setOpaque(false);
		this.progressBars = new HashMap<AttributeType, JProgressBar>();

		helpPanel.setLayout(new BoxLayout(helpPanel, BoxLayout.X_AXIS));

		helpPanel.add(Box.createRigidArea(new Dimension(15, 0)));
		helpPanel.add(characterContent);
		helpPanel.add(Box.createRigidArea(new Dimension(15, 0)));

		// GridLayout wird gesetzt
		characterContent.setLayout(new GridLayout(AttributeType.values().length + 1, 1));

		// ProgressBars werden hinzugefuegt
		for (AttributeType attributeType : AttributeType.values()) {
			JProgressBar progressBar = new JProgressBar(0, 100);
			progressBar.setBackground(Color.WHITE);
			progressBar.setForeground(Color.RED);
			progressBar.setValue(0);
			progressBar.setString(AttributeType.getAttributeName(attributeType));
			progressBar.setStringPainted(true);
			progressBars.put(attributeType, progressBar);
			characterContent.add(progressBar);
		}

		// JLabel fuer das aktuelle Level
		this.levelLabel = new JLabel("Level: " + 0);
		characterContent.add(levelLabel);

		this.add(Box.createRigidArea(new Dimension(0, 35)));
		this.add(helpPanel);
		this.add(Box.createRigidArea(new Dimension(0, 10)));
	}

	/**
	 * Zeichnet das Hintergrundbild
	 * 
	 * @author Mareike Fischer
	 */
	public void paintComponent(Graphics g) {
		g.drawImage(background, 0, 0, null);
	}

	/**
	 * Aktualisiert die Werte der JProgressBars
	 * @param attributes Neue Attributwerte
	 * @author Mareike Fischer
	 */
	void setAttributes(Attributes attributes) {
		for (AttributeType attributeType : attributes.keySet())
			progressBars.get(attributeType).setValue(attributes.get(attributeType));
	}

	/**
	 * Setzt das Label fuer das Spielerlevel auf einen neuen Wert.
	 * @param newLevel
	 * @author Mareike Fischer
	 */
	void setLevel(int newLevel) {
		levelLabel.setText("Level: " + newLevel);
	}

	/**
	 * Setzt den Wert einer JProgressBar auf ein neues Wert
	 * @param type Welche JProgressBar soll aktualisiert werden
	 * @param newValue Neuer Wert
	 * @author Mareike Fischer
	 */
	public void updateAttribute(AttributeType type, int newValue) {
		progressBars.get(type).setValue(newValue);
	}
}