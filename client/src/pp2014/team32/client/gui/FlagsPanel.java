package pp2014.team32.client.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.JPanel;

import pp2014.team32.shared.enums.CityType;
import pp2014.team32.shared.enums.TeamType;
import pp2014.team32.shared.utils.Coordinates;
import pp2014.team32.shared.utils.PropertyManager;

/**
 * Zeigt das aktuelle WM-Spiel an:
 * - Levelbeschreibung
 * - Flaggen der 2 Mannschaften
 * - Spielort
 * 
 * @author christian
 * @version 1.7.14
 */
public class FlagsPanel extends JPanel {

	private static final long				serialVersionUID	= -5182208539618533755L;
	private static Logger					LOGGER;
	private static HashMap<TeamType, Image>	flagImages;
	private static final int				FLAG_SIZE;
	private TeamType						team1, team2;
	private static Font						vsFont, descriptionFont, cityFont;
	private Coordinates						vsCoordinates;
	private JLabel							descriptionLabel, cityLabel;
	private JPanel							vsPanel;

	/**
	 * Laedt alle Flaggen bei der ersten Benutzung der Klasse einmalig statisch
	 * ein.
	 * 
	 * @author Christian Hovestadt
	 */
	static {
		LOGGER = Logger.getLogger(FlagsPanel.class.getName());
		FLAG_SIZE = Integer.parseInt(PropertyManager.getProperty("flagsPanel.flagSize"));
		vsFont = new Font("Arial", Font.PLAIN, 15);
		descriptionFont = new Font("Arial", Font.PLAIN, 18);
		cityFont = new Font("Arial", Font.PLAIN, 15);
		// Load Flags
		flagImages = new HashMap<TeamType, Image>();
		for (TeamType type : TeamType.values())
			try {
				flagImages.put(type, ImageIO.read(new File(PropertyManager.getProperty("paths.flagPath") + type.toString().toLowerCase() + ".png")));
			} catch (IOException e) {
				if (type != TeamType.NONE)
					LOGGER.warning("Image for TeamType '" + type + "' was not found.");
			}
	}

	/**
	 * Setzt die Werte fuer team1, team2, Spielbeschreibung und Spielort auf
	 * Standardwerte.
	 * Erzeugt die Labels fuer die Spielbeschreibung und Spielort sowie ein
	 * eingenes JPanel fuer die Flaggen, die mit BorderLayout angeordnet werden.
	 * 
	 * @author Christian Hovestadt
	 */
	public FlagsPanel() {
		this.team1 = TeamType.UNDEF;
		this.team2 = TeamType.UNDEF;
		this.descriptionLabel = new JLabel("Nicht gesetzt");
		this.descriptionLabel.setHorizontalAlignment(JLabel.CENTER);
		this.descriptionLabel.setForeground(Color.WHITE);
		this.descriptionLabel.setFont(descriptionFont);
		this.descriptionLabel.setPreferredSize(new Dimension(Integer.MAX_VALUE, descriptionFont.getSize()));
		this.vsPanel = new VSPanel();
		this.vsCoordinates = new Coordinates(0, 0);
		this.cityLabel = new JLabel(CityType.UNDEF.toString());
		this.cityLabel.setHorizontalAlignment(JLabel.CENTER);
		this.cityLabel.setForeground(Color.WHITE);
		this.cityLabel.setFont(cityFont);
		this.cityLabel.setPreferredSize(new Dimension(Integer.MAX_VALUE, cityFont.getSize()));

		this.setLayout(new BorderLayout());
		this.setOpaque(false);
		this.add(descriptionLabel, BorderLayout.NORTH);
		this.add(vsPanel, BorderLayout.CENTER);
		this.add(cityLabel, BorderLayout.SOUTH);
	}

	/**
	 * Die Werte fuer team1, team2, levelDescription und city werden aktualisiert.
	 * 
	 * @param team1
	 * @param team2
	 * @param levelDescription
	 * @param city
	 * @author Christian Hovestadt
	 */
	void updateMatch(TeamType team1, TeamType team2, String levelDescription, CityType city) {
		this.team1 = team1;
		this.team2 = team2;
		this.descriptionLabel.setText(levelDescription);
		this.cityLabel.setText(CityType.getName(city));
		vsCoordinates = new Coordinates(FLAG_SIZE + (vsPanel.getWidth() - 2 * FLAG_SIZE - this.getFontMetrics(vsFont).stringWidth("vs.")) / 2, vsPanel.getHeight() / 2 + vsFont.getSize() / 2);
	}

	/**
	 * In diesem Panel werden die Flaggen sowie der Schriftzug 'vs.' gezeichnet.
	 * 
	 * @author Christian Hovestadt
	 */
	private class VSPanel extends JPanel {
		private static final long	serialVersionUID	= -8877649273945021406L;

		/**
		 * Zeichnet die Flaggen und den Schriftzug 'vs.'
		 * 
		 * @author Christian Hovestadt
		 */
		public void paintComponent(Graphics g) {
			if (team1 != TeamType.NONE) {
				g.drawImage(flagImages.get(team1), 0, (this.getHeight() - FLAG_SIZE) / 2, null);
				g.drawImage(flagImages.get(team2), this.getWidth() - FLAG_SIZE, (this.getHeight() - FLAG_SIZE) / 2, null);
				g.setColor(Color.WHITE);
				g.setFont(vsFont);
				g.drawString("vs.", vsCoordinates.x, vsCoordinates.y);
			}
		}
	}
}
