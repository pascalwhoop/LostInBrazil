package pp2014.team32.shared.entities;

import pp2014.team32.shared.enums.CityType;
import pp2014.team32.shared.enums.Direction;
import pp2014.team32.shared.enums.LevelMapType;
import pp2014.team32.shared.enums.UIObjectType;

/**
 * Taxis dienen dem Wechseln einer LevelMap. Dazu sind alle noetigen
 * Eigenschaften im Taxi Objekt gespeichert.
 * 
 * @author Christian Hovestadt
 */
public class Taxi extends MovableObject {
	private static final long	serialVersionUID	= -5742488178881254529L;

	private transient LevelMap	destinationLevelMap;
	private boolean				unlocked;
	private boolean				moving;
	private Direction			direction;
	private String				label;

	/**
	 * Erstellt ein nicht verlinktes Taxi Objekt
	 * @param id
	 * @param x
	 * @param y
	 */
	public Taxi(int id, int x, int y) {
		this(id, x, y, null, null, false);
	}

	/**
	 * Erzeugt ein neues Taxi mit den uebergebenen Werten inklusive der Verlinkung zu einer LevelMap
	 * 
	 * @param id
	 * @param x
	 * @param y
	 * @param destLevelMap
	 * @param direction
	 * @param unlocked
	 * @author
	 */
	public Taxi(int id, int x, int y, LevelMap destLevelMap, Direction direction, boolean unlocked) {
		super(id, UIObjectType.TAXI, x, y);
		this.destinationLevelMap = destLevelMap;
		this.unlocked = unlocked;
		this.direction = direction;
		this.moving = false;
		this.width = 64;
		this.height = 48;

	}

	/**
	 * Getter und Setter fuer Taxi
	 */
	
	/**
	 * 
	 * @return Ziel LevelMap
	 */
	public LevelMap getDestinationLevelMap() {
		return destinationLevelMap;
	}

	/**
	 * 
	 * @param destinationLevelMap wird fuer das Taxi gesetzt
	 */
	public void setDestinationLevelMap(LevelMap destinationLevelMap) {
		this.destinationLevelMap = destinationLevelMap;
		if (destinationLevelMap.getLevelMapType() == LevelMapType.AIRPORT)
			this.label = CityType.getName(destinationLevelMap.city) + " Airport";
		else
			this.label = CityType.getName(destinationLevelMap.city) + " - " + destinationLevelMap.team1 + " vs. " + destinationLevelMap.team2;
	}

	/**
	 * 
	 * @return Richtung des Taxis
	 */
	public Direction getDirection() {
		return direction;
	}

	/**
	 * 
	 * @param direction wird fuer das Taxi gesetzt
	 */
	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	/**
	 * 
	 * @return ist freigeschaltet
	 */
	public boolean isUnlocked() {
		return unlocked;
	}

	/**
	 * 
	 * @param unlocked freigeschaltet?
	 */
	public void setUnlocked(boolean unlocked) {
		this.unlocked = unlocked;
	}

	/**
	 * 
	 * @return in Bewegung?
	 */
	public boolean isMoving() {
		return moving;
	}

	/**
	 * In Bewegung
	 */
	public void move() {
		this.moving = true;
	}

	/**
	 * 
	 * @return label des Taxis
	 */
	public String getLabel() {
		return label;
	}
}
