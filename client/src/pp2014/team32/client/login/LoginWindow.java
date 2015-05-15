package pp2014.team32.client.login;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import pp2014.team32.client.ClientMain;
import pp2014.team32.client.comm.ServerConnection;
import pp2014.team32.shared.messages.UserAuthentication;
import pp2014.team32.shared.utils.PropertyManager;

/**
 * Das JFrame zum Einloggen
 * 
 * @author Mareike Fischer
 * @version 14.06.2014
 */
public class LoginWindow extends JFrame implements ActionListener {

	private static final long		serialVersionUID	= 5136171075705809618L;
	private static BufferedImage	backgroundImage;
	private static Logger			LOGGER;
	private static final Font		LABEL_FONT;
	public String					enteredIp, enteredUsername, enteredPassword;
	private JPanel					layoutPanel;
	private JTextField				ipField, usernameField;
	private JPasswordField			passwordField;
	private JButton					confirmButton, registerButton;

	static {
		LOGGER = Logger.getLogger(LoginWindow.class.getName());
		LABEL_FONT = new Font("Helvetica", Font.PLAIN, 22);
		try {
			backgroundImage = ImageIO.read(new File(PropertyManager.getProperty("paths.loginBackgroundPath")));
		} catch (IOException e) {
			LOGGER.warning("Login-background-image not found.");
		}
	}

	/**
	 * Die Labels, Bilder und Eingabefelder fuer das Login-Fenster werden gesetzt
	 * 
	 * @author Mareike Fischer
	 */
	public LoginWindow() {
		
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

		// IP
		JLabel ipLabel = new JLabel("IP-Adresse");
		ipLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		ipLabel.setFont(LABEL_FONT);
		this.layoutPanel.add(ipLabel);

		this.ipField = new JTextField();
		this.ipField.setMaximumSize(new Dimension(300, 50));
		this.ipField.setHorizontalAlignment(JTextField.CENTER);
		this.ipField.setText("localhost");
		this.layoutPanel.add(ipField);

		this.layoutPanel.add(Box.createRigidArea(new Dimension(0, 20)));

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

		// Password
		JLabel passwordLabel = new JLabel("Passwort");
		passwordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		passwordLabel.setFont(LABEL_FONT);
		this.layoutPanel.add(passwordLabel);

		this.passwordField = new JPasswordField();
		this.passwordField.setMaximumSize(new Dimension(300, 50));
		this.passwordField.setHorizontalAlignment(JPasswordField.CENTER);
		this.layoutPanel.add(passwordField);

		this.layoutPanel.add(Box.createRigidArea(new Dimension(0, 40)));

		// Button Confirm
		this.confirmButton = new JButton("Login");
		confirmButton.setFont(LABEL_FONT);
		confirmButton.setForeground(Color.WHITE);
		confirmButton.setBackground(Color.GRAY);
		confirmButton.setAlignmentX(Component.CENTER_ALIGNMENT);

		confirmButton.addActionListener(this);

		this.layoutPanel.add(confirmButton);

		this.layoutPanel.add(Box.createRigidArea(new Dimension(0, 80)));

		// Button Register
		this.registerButton = new JButton("Register");
		registerButton.setFont(LABEL_FONT);
		registerButton.setForeground(Color.WHITE);
		registerButton.setBackground(Color.GRAY);
		registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);

		registerButton.addActionListener(this);

		this.layoutPanel.add(registerButton);
		this.layoutPanel.add(Box.createVerticalGlue());

		this.add(layoutPanel);
		this.pack();
		this.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width) / 2 - getWidth() / 2, (Toolkit.getDefaultToolkit().getScreenSize().height) / 2 - getHeight() / 2);
		this.setVisible(true);
		this.usernameField.requestFocus();
	}

	/**
	 * Ruft den anderen Konstruktor auf und setzt den uebergebenen Wert in das IP-Feld ein.
	 * 
	 * @param lastIp
	 * @author Mareike Fischer
	 */
	public LoginWindow(String lastIp) {
		this();
		this.ipField.setText(lastIp);
	}

	/**
	 * Behandelt die Button-Events
	 * Die Server-Connection wird aufgebaut
	 * Das Fenster wird geschlossen.
	 * 
	 * confirmButton: Eine <i>UserAuthentication</i>-Nachricht mit dem eingegebenen
	 * Benutzernamen und Passwort wird an den Server gesendet
	 * 
	 * registerButton: Das Fenster zur Character-Auswahl wird geoeffnet
	 * 
	 * @author Mareike Fischer
	 * @param ae ActionEvent (Ausloeser kann der confirmButton oder der
	 *            registerButton sein)
	 */
	public void actionPerformed(ActionEvent ae) {
		try {
			ClientMain.initializeConnection(ipField.getText());
			
			if (ae.getSource() == confirmButton) {

				enteredUsername = usernameField.getText();
				enteredPassword = new String(passwordField.getPassword());

				if (enteredUsername.length() == 0) {
					JOptionPane.showMessageDialog(this, "Die Felder 'Benutzername' und 'Passwort' duerfen nicht leer sein.", "", JOptionPane.ERROR_MESSAGE);
				} else if (enteredPassword.length() == 0) {
					JOptionPane.showMessageDialog(this, "Die Felder 'Benutzername' und 'Passwort' duerfen nicht leer sein.", "", JOptionPane.ERROR_MESSAGE);
				} else {
					ServerConnection.sendMessageToServer(new UserAuthentication(enteredUsername, enteredPassword));
					this.dispose();
				}
			}

			if (ae.getSource() == registerButton) {
				new ChooseCharacter();
				this.dispose();	
			}
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(this, "Verbindung zum Server konnte nicht aufgebaut werden.", "", JOptionPane.ERROR_MESSAGE);
		}
	}
}
