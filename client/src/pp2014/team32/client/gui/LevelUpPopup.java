package pp2014.team32.client.gui;

import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import pp2014.team32.client.comm.ServerConnection;
import pp2014.team32.shared.entities.Attributes;
import pp2014.team32.shared.enums.AttributeType;
import pp2014.team32.shared.messages.AttributeUpgradeRequest;

/**
 * Dieses Panel oeffnet sich, wenn der Spieler ein Level aufsteigt und
 * nun seine Attribute verbessern kann.
 * 
 * @author Christian Hovestadt
 * @version 7.7.14
 */

public class LevelUpPopup extends JDialog {
	private static final long	serialVersionUID	= -2734842083413024303L;
	private static int			DISTRIBUTING_POINTS	= 5;

	/**
	 * Das Popup besteht aus drei JLabels fuer die Ueberschrift und die
	 * Unterueberschriften, einem Grid fuer die Verteilung der Attribute, sowie
	 * einem Button zum bestaetigen.
	 * 
	 * Fuer jedes Attribut gibt es im Grid eine Zeile mit einem JLabel fuer den
	 * Namen des Attributs in der ersten Spalte sowie einer ComboBox zur Auswahl
	 * der Punkte.
	 * 
	 * Nach Klick des Bestaetigen-Buttons wird die Anzahl der verteilten Punkte
	 * ueberprueft: Wenn der Benutzer zu viele Punkte verteilt hat, wird der
	 * Spieler darauf hingewiesen, dass er eine Korrektur vornehmen muss. Wenn
	 * er zu wenig Punkte verteilt hat, wird der Benutzer gefragt, ob er die
	 * weiteren Punkte wirklich nicht verteilen will.
	 * 
	 * @param characterID
	 * @param parent
	 * @author Christian Hovestadt
	 */
	public LevelUpPopup(final int characterID, JFrame parent) {
		super(parent);
		this.setTitle("Level Up!");
		this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		// Labels
		JLabel titleLabel = new JLabel("LEVEL UP!");
		titleLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		titleLabel.setFont(new Font("Arial", Font.PLAIN, 50));
		titleLabel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 30, 0, 30), BorderFactory.createEmptyBorder()));
		JLabel sublineLabel1 = new JLabel("Du bekommst ein Upgrade!");
		sublineLabel1.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		sublineLabel1.setFont(new Font("Arial", Font.PLAIN, 15));
		JLabel sublineLabel2 = new JLabel("Bitte verteile " + DISTRIBUTING_POINTS + " Charakterpunkte.");
		sublineLabel2.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		sublineLabel2.setFont(new Font("Arial", Font.PLAIN, 15));

		// Grid-Panel
		AttributeType[] options = { AttributeType.ATTACK_STRENGTH, AttributeType.DEFENSE, AttributeType.MOVEMENT_SPEED, AttributeType.ATTACK_SPEED, AttributeType.HEALTH_REGENERATION };
		Integer[] comboBoxOptions = new Integer[DISTRIBUTING_POINTS + 1];
		final HashMap<AttributeType, JComboBox<Integer>> comboBoxes = new HashMap<AttributeType, JComboBox<Integer>>();
		for (int i = 0; i <= DISTRIBUTING_POINTS; i++)
			comboBoxOptions[i] = i;
		final JPanel gridPanel = new JPanel();
		gridPanel.setLayout(new GridLayout(options.length, 2));
		for (AttributeType type : options) {
			gridPanel.add(new JLabel(AttributeType.getAttributeName(type)));
			JComboBox<Integer> comboBox = new JComboBox<Integer>(comboBoxOptions);
			comboBoxes.put(type, comboBox);
			gridPanel.add(comboBox);
		}

		// Confirm Button
		final LevelUpPopup frame = this;
		JButton confirmButton = new JButton("Best\u00E4tigen");
		confirmButton.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		confirmButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				int sum = 0;
				for (JComboBox<Integer> box : comboBoxes.values())
					sum += (int) box.getSelectedItem();
				if (sum > DISTRIBUTING_POINTS) {
					JOptionPane.showMessageDialog(null, "Du hast " + (sum - DISTRIBUTING_POINTS) + " Punkte zu viel verteilt.", "", JOptionPane.ERROR_MESSAGE);
					return;
				} else if (sum < DISTRIBUTING_POINTS) {
					int answer = JOptionPane.showConfirmDialog(null, "Du kannst noch " + (DISTRIBUTING_POINTS - sum) + " Punkte verteilen.\nTrotzdem fortfahren?", "", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE);
					if (answer == JOptionPane.NO_OPTION)
						return;
				}
				ServerConnection.sendMessageToServer(new AttributeUpgradeRequest(characterID, new Attributes(0, (int) comboBoxes.get(AttributeType.ATTACK_STRENGTH).getSelectedItem(), //
						(int) comboBoxes.get(AttributeType.DEFENSE).getSelectedItem(), //
						(int) comboBoxes.get(AttributeType.MOVEMENT_SPEED).getSelectedItem(), //
						(int) comboBoxes.get(AttributeType.ATTACK_SPEED).getSelectedItem(), //
						(int) comboBoxes.get(AttributeType.HEALTH_REGENERATION).getSelectedItem(), 0)));
				frame.dispose();
			}
		});

		this.add(titleLabel);
		this.add(sublineLabel1);
		this.add(sublineLabel2);
		this.add(gridPanel);
		this.add(confirmButton);

		this.pack();
		this.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width) / 2 - getWidth() / 2, (Toolkit.getDefaultToolkit().getScreenSize().height) / 2 - getHeight() / 2);
		this.setVisible(true);
		titleLabel.setFocusable(true);
		titleLabel.requestFocusInWindow();
	}
}
