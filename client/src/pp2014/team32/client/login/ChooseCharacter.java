package pp2014.team32.client.login;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import pp2014.team32.client.comm.ServerConnection;
import pp2014.team32.client.engine.ServerMessageHandler;
import pp2014.team32.shared.enums.CharacterType;
import pp2014.team32.shared.messages.RegistrationRequest;

/**
 * Das JFrame zum Registrieren eines neuen Spielers
 * 
 * @author Mareike Fischer
 * @version 14.06.2014
 */
public class ChooseCharacter extends JFrame implements ActionListener {

	private static final long		serialVersionUID	= 5136171075705809618L;
	private static BufferedImage	backgroundImage;
	private static Logger			LOGGER;
	private static final Font		LABEL_FONT;
	private JPanel					layoutPanel;
	public String					enteredUsername, enteredPassword;
	private JButton					chooseGerman, chooseAmerican, chooseAfrican;
	private JPasswordField			passwordField;
	private JTextField				usernameField;

	static {
		LABEL_FONT = new Font("Helvetica", Font.PLAIN, 14);
		LOGGER = Logger.getLogger(ChooseCharacter.class.getName());
		try {
			backgroundImage = ImageIO.read(new File("images/choose.png"));
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Fehler: Bild nicht geladen");
		}
	}

	/**
	 * Die Bilder, Labels und Eingabefelder fuer das Character-Fenster werden
	 * konfiguriert
	 * 
	 * @author Mareike Fischer
	 */
	public ChooseCharacter() {

		// GUI
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setTitle("LOST IN BRAZIL - LOGIN");
		this.setResizable(false);

		// LayoutPanel
		this.layoutPanel = new JPanel() {
			private static final long	serialVersionUID	= -805036255580770582L;

			public void paintComponent(Graphics g) {
				g.drawImage(backgroundImage, 0, 0, null);
			}
		};
		this.layoutPanel.setPreferredSize(new Dimension(backgroundImage.getWidth(), backgroundImage.getHeight()));
		this.layoutPanel.setLayout(new BoxLayout(layoutPanel, BoxLayout.Y_AXIS));
		this.layoutPanel.add(Box.createVerticalGlue());

		// Username
		JLabel usernameLabel = new JLabel("Benutzername");
		usernameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		usernameLabel.setFont(LABEL_FONT);
		this.layoutPanel.add(usernameLabel);

		this.usernameField = new JTextField();
		this.usernameField.setMaximumSize(new Dimension(300, 50));
		this.usernameField.setHorizontalAlignment(JTextField.CENTER);
		this.layoutPanel.add(usernameField);

		this.layoutPanel.add(Box.createRigidArea(new Dimension(0, 20)));

		// Passwort
		JLabel passwordLabel = new JLabel("Passwort");
		passwordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		passwordLabel.setFont(LABEL_FONT);
		this.layoutPanel.add(passwordLabel);

		this.passwordField = new JPasswordField();
		this.passwordField.setMaximumSize(new Dimension(300, 50));
		this.passwordField.setHorizontalAlignment(JTextField.CENTER);
		this.layoutPanel.add(passwordField);

		this.layoutPanel.add(Box.createRigidArea(new Dimension(0, 40)));

		JLabel chooseLabel = new JLabel("Charakter auswaehlen");
		chooseLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		chooseLabel.setFont(LABEL_FONT);
		this.layoutPanel.add(chooseLabel);

		// German
		this.chooseGerman = new JButton(new ImageIcon("images/choosegerman.png"));
		this.chooseGerman.setMaximumSize(new Dimension(300, 50));
		chooseGerman.setOpaque(false);
		chooseGerman.setContentAreaFilled(false);
		chooseGerman.setBorderPainted(false);
		chooseGerman.setAlignmentX(Component.CENTER_ALIGNMENT);
		chooseGerman.addActionListener(this);
		this.layoutPanel.add(chooseGerman);

		this.layoutPanel.add(Box.createRigidArea(new Dimension(0, 5)));

		// American
		this.chooseAmerican = new JButton(new ImageIcon("images/chooseamerican.png"));
		this.chooseAmerican.setMaximumSize(new Dimension(300, 50));
		chooseAmerican.setOpaque(false);
		chooseAmerican.setContentAreaFilled(false);
		chooseAmerican.setBorderPainted(false);
		chooseAmerican.setAlignmentX(Component.CENTER_ALIGNMENT);
		chooseAmerican.addActionListener(this);
		this.layoutPanel.add(chooseAmerican);

		this.layoutPanel.add(Box.createRigidArea(new Dimension(0, 5)));

		// African
		this.chooseAfrican = new JButton(new ImageIcon("images/chooseafrican.png"));
		this.chooseAfrican.setMaximumSize(new Dimension(300, 50));
		chooseAfrican.setOpaque(false);
		chooseAfrican.setContentAreaFilled(false);
		chooseAfrican.setBorderPainted(false);
		chooseAfrican.setAlignmentX(Component.CENTER_ALIGNMENT);
		chooseAfrican.addActionListener(this);
		this.layoutPanel.add(chooseAfrican);

		this.layoutPanel.add(Box.createVerticalGlue());

		this.add(layoutPanel);
		this.pack();
		this.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width) / 2 - getWidth() / 2, (Toolkit.getDefaultToolkit().getScreenSize().height) / 2 - getHeight() / 2);
		this.setVisible(true);
	}

	/**
	 * Diese Methode wird aufgerufen, wenn der Benutzer einen Button klickt.
	 * Es wird abgefragt, welchen Button der Benutzer geklickt hat und es wird
	 * eine entsprechende RegistrationRequest an den Server gesendet.
	 * Ein <i>GameWindow</i> wird erst geoeffnet, wenn der Client eine erfolgreiche AuthenticationResponse erhaelt.
	 * 
	 * @param ae
	 * @author Mareike Fischer
	 */
	public void actionPerformed(ActionEvent ae) {

		enteredUsername = usernameField.getText();
		enteredPassword = new String(passwordField.getPassword());

		if (enteredUsername.length() == 0) {
			JOptionPane.showMessageDialog(this, "Die Felder 'Benutzername' und 'Passwort' duerfen nicht leer sein.", "", JOptionPane.ERROR_MESSAGE);
		} else if (enteredPassword.length() == 0) {
			JOptionPane.showMessageDialog(this, "Die Felder 'Benutzername' und 'Passwort' duerfen nicht leer sein.", "", JOptionPane.ERROR_MESSAGE);
		} else {
			
			if (ae.getSource() == chooseGerman) {
				ServerConnection.sendMessageToServer(new RegistrationRequest(enteredUsername, enteredPassword, CharacterType.GERMAN));
				ServerMessageHandler.lastAuthenticationWasRegistration();
				this.dispose();
			}

			if (ae.getSource() == chooseAmerican) {
				ServerConnection.sendMessageToServer(new RegistrationRequest(enteredUsername, enteredPassword, CharacterType.AMERICAN));
				ServerMessageHandler.lastAuthenticationWasRegistration();
				this.dispose();
			}

			if (ae.getSource() == chooseAfrican) {
				ServerConnection.sendMessageToServer(new RegistrationRequest(enteredUsername, enteredPassword, CharacterType.AFRICAN));
				ServerMessageHandler.lastAuthenticationWasRegistration();
				this.dispose();
			}
		}
	}
		
}
