package pp2014.team32.client.resources;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.util.HashMap;
import java.util.logging.Logger;

import pp2014.team32.shared.enums.CreatureStatusType;

/**
 * Verwaltet die Animationen fuer eine <i>Creature</i>. Eine Creature hat fuer
 * jeden <i>CreatureStatusType</i> ein eigenes Animationsbild. Fuer jeden Typ
 * Creature gibt es ein eigenes <i>CreatureAnimationSet</i>.
 * 
 * @author Christian Hovestadt
 * @version 20.06.2014
 */
public class CreatureAnimationSet {
	private HashMap<CreatureStatusType, Image>	animations;
	private static final Logger					LOGGER	= Logger.getLogger(CreatureStatusType.class.getName());

	/**
	 * Fuer einen bestimmten Typ Creature werden die Animationen ueber Toolkit
	 * eingeladen, da ueber ImageIO keine Animationen unterstuetzt werden.
	 * Trotzdem werden nicht vorhandene Dateien abgefangen und durch das Default
	 * Image (STANDING) ersetzt (wenn dieses vorhanden ist).
	 * 
	 * @param creatureTypeString
	 * @param path
	 * @author Christian Hovestadt
	 */
	public CreatureAnimationSet(String creatureTypeString, String path) {
		this.animations = new HashMap<CreatureStatusType, Image>();
		for (CreatureStatusType type : CreatureStatusType.values())
			if (new File(path + type.toString().toLowerCase() + ".gif").exists())
				animations.put(type, Toolkit.getDefaultToolkit().createImage(path + type.toString().toLowerCase() + ".gif"));
			else {
				// STANDING is default
				if (new File(path + CreatureStatusType.STANDING.toString().toLowerCase() + ".gif").exists()) {
					animations.put(type, Toolkit.getDefaultToolkit().createImage(path + CreatureStatusType.STANDING.toString().toLowerCase() + ".gif"));
					LOGGER.warning("Image for CharacterType/EnemyType " + creatureTypeString + " and StatusType " + type + " was not found.");
				} else
					LOGGER.warning("Image for CharacterType/EnemyType " + creatureTypeString + " and StatusType " + type + " was not found and could not be replaced with the default image.");
			}
	}

	/**
	 * Gibt die Animation der Creature fuer den gegebenen
	 * <i>CreatureStatusType</i> zurueck.
	 * 
	 * @param type
	 * @return Die Animation
	 * @author Christian Hovestadt
	 */
	public Image getAnimation(CreatureStatusType type) {
		return animations.get(type);
	}
}
