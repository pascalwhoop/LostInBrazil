package pp2014.team32.shared.entities;

import java.util.ArrayList;
import java.util.Date;

import pp2014.team32.shared.enums.AttributeType;
import pp2014.team32.shared.enums.CreatureStatusType;
import pp2014.team32.shared.enums.EnemyStateType;
import pp2014.team32.shared.enums.EnemyType;
import pp2014.team32.shared.enums.UIObjectType;
import pp2014.team32.shared.utils.Coordinates;
import pp2014.team32.shared.utils.PropertyManager;

/**
 * Diese Klasse dient zur Speicherung aller wichtigen Eigenschaften eines
 * Enemies in einer Instanz. Wichtige Komponenten sind bspw. die
 * Bewegungsrichtung des Enemies, der Typ, der Bewegungspfad des Enemies sowie
 * dessen letztem Bewegungszeitpunktes und ggf. seines Einschlafzeitpunktes. Sie
 * bietet desweiteren Manipulationsmethoden fuer eben diese Eigenschaften.
 * 
 * @author Brokmeier, Pascal
 * @author Peter Kings
 */
public class Enemy extends Creature {

	private static final long					serialVersionUID	= 5443403444127683554L;

	// Werte die dem Spieler gutgeschrieben werden
	// protected int exPts; bereits in Attributes enthalten
	// protected int gold; gehoert nicht zum Spielprinzip

	// Vorkommen in Ebenen, je nach Monster- / Feindtyp
	protected int								minDepth;
	protected int								maxDepth;
	private static int							INVENTORY_SIZE		= Integer.parseInt(PropertyManager.getProperty("enemyInventorySize"));
	private int									difficulty;
	private final EnemyType						enemyType;
	private transient EnemyStateType			enemyStateType;
	private transient ArrayList<Coordinates>	movementPath		= new ArrayList<Coordinates>();
	private transient int						movementPathIndex;
	private transient Date						sleepTimeStamp;
	private transient Date						lastMovementTimeStamp;

	/**
	 * Enemy wird stehend initialisiert und das letzte Movement wird auf den
	 * aktuellen Zeitpunkt gesetzt.
	 * 
	 * @author Peter Kings
	 * @param id eindeutig ID
	 * @param x x Position
	 * @param y y Position
	 * @param enemyType Lion, Snake, ...
	 * @param difficulty Schwierigkeitsgrad des Monsters
	 */
	public Enemy(int id, int x, int y, EnemyType enemyType, int difficulty) {
		this(id, x, y, enemyType, difficulty, CreatureStatusType.STANDING);
		/*
		 * nicht auf erste Koordinate, da dies in einem MovementPath die eigene
		 * Position ist
		 */
		this.movementPathIndex = 1;
		this.lastMovementTimeStamp = new Date();
	}

	/**
	 * Enemy wird im Status herumlaufend initialisiert. Es erhaelt ein leeres
	 * Inventar; das letzte Movement wird auf den aktuellen Zeitpunkt gsetzt.
	 * 
	 * @author Peter Kings
	 * @param id eindeutig ID
	 * @param x x Position
	 * @param y y Position
	 * @param enemyType Lion, Snake, ...
	 * @param difficulty Schwierigkeitsgrad des Monsters
	 * @param status Bewegungsrichtung des Enemies
	 */
	public Enemy(int id, int x, int y, EnemyType enemyType, int difficulty, CreatureStatusType status) {
		super(id, UIObjectType.ENEMY, x, y, enemyType.toString(), EnemyType.getEnemyAttributes(enemyType, difficulty), status);
		this.enemyType = enemyType;
		this.inventory = new Inventory(new Item[INVENTORY_SIZE]);
		// Default fuer alle Enemies: sie laufen rum.
		this.enemyStateType = EnemyStateType.WALKAROUND;
		this.width = EnemyType.getEnemyWidth(enemyType);
		this.height = EnemyType.getEnemyHeight(enemyType);
		this.difficulty = difficulty;
		/*
		 * nicht auf erste Koordinate, da dies in einem MovementPath die eigene
		 * Position ist
		 */
		this.movementPathIndex = 1;
		this.lastMovementTimeStamp = new Date();
	}

	/**
	 * Gibt den Typ des Enemies zurueck.
	 * 
	 * @author Peter Kings
	 * @return EnemyType z.B. Lion,...
	 */
	public EnemyType getEnemyType() {
		return enemyType;
	}

	/**
	 * Gibt den Status (Attack,...) des Enemies zurueck.
	 * 
	 * @author Peter Kings
	 * @return enemyStateType Attack, Walkaround,...
	 */
	public EnemyStateType getEnemyStateType() {
		return enemyStateType;
	}

	/**
	 * Setzt den Status (Attack,...) des Enemies.
	 * 
	 * @author Peter Kings
	 * @param enemyStateType Attack, Walkaround,...
	 */
	public void setEnemyStateType(EnemyStateType enemyStateType) {
		this.enemyStateType = enemyStateType;
	}

	/**
	 * Gibt die Schwierigkeit des Enemies zurueck
	 * 
	 * @author Brokmeier, Pascal
	 */
	public int getDifficulty() {
		return difficulty;
	}

	/**
	 * Setzt die Schwierigkeit des Enemies.
	 * 
	 * @author Brokmeier, Pascal
	 */
	public void setDifficulty(int difficulty) {
		this.difficulty = difficulty;
	}

	/**
	 * Gibt den Einschlafzeitpunktes des Enemies zurueck
	 * 
	 * @return Date als Zeitstempel
	 * @author Peter Kings
	 */
	public Date getSleepTimeStamp() {
		return sleepTimeStamp;
	}

	/**
	 * Setzt den Einschlafzeitpunkt des Enmies.
	 * 
	 * @param sleepTimeStamp
	 * @author Peter Kings
	 */
	public void setSleepTimeStamp(Date sleepTimeStamp) {
		this.sleepTimeStamp = sleepTimeStamp;
	}

	/**
	 * Gibt den aktuellen Bewegungspfad des Enemies zurueck
	 * 
	 * @author Peter Kings
	 * @return Bewegungspfad als Koordinaten ArrayList
	 */
	public ArrayList<Coordinates> getMovementPath() {
		return movementPath;
	}

	/**
	 * Setzt den Bewegungspfad eines Enemies.
	 * 
	 * @author Peter Kings
	 * @param movementPath Bewegungspfad
	 */
	public void setMovementPath(ArrayList<Coordinates> movementPath) {
		this.movementPath = movementPath;
	}

	/**
	 * Gibt die Position der Koordinate im Bewegungspfad zurueck, an dem die
	 * Abarbeitung des Bewegungspfades steht.
	 * 
	 * @author Peter Kings
	 * @return
	 */
	public int getMovementPathIndex() {
		return movementPathIndex;
	}

	/**
	 * Setzt die Position der Koordinate im Bewegungspfad , an dem die
	 * Abarbeitung des Bewegungspfades aktuell steht.
	 * 
	 * @author Peter Kings
	 * @param movementPathIndex
	 */
	public void setMovementPathIndex(int movementPathIndex) {
		this.movementPathIndex = movementPathIndex;
	}

	/**
	 * Letzen Zeitpunkt einer Bewegung erhalten
	 * 
	 * @author Peter Kings
	 * @return
	 */
	public Date getLastMovementTimeStamp() {
		return lastMovementTimeStamp;
	}

	/**
	 * Letzen Zeitpunkt einer Bewegung setzen
	 * 
	 * @author Peter Kings
	 * @param lastMovementTimeStamp
	 */
	public void setLastMovementTimeStamp(Date lastMovementTimeStamp) {
		this.lastMovementTimeStamp = lastMovementTimeStamp;
	}

	/**
	 * Diese Methode generiert je nach MovementSpeed des Enemies einen TimeStamp
	 * in der Zukunft, ab dem sich dieses Enemie erneut bewegen darf.
	 * 
	 * @auhtor Peter Kings
	 * @return Date in der Zukunft
	 */
	public Date getNextMovementTimeStamp() {
		/*
		 * Zeit, die bis zur naechsten Abarbeitung einer Coordinate auf dem
		 * MovementPath vergehen soll
		 */
		float movementSpeedTimeAdd;
		/*
		 * Enemy ist nun im Attack Modus: bewegt sich schneller!
		 */
		if (this.getEnemyStateType().equals(EnemyStateType.ATTACK))
			// movement Speed des Enemies einfliessen lassen
			movementSpeedTimeAdd = (100 - this.getAttributeValue(AttributeType.MOVEMENT_SPEED)) / 6 + 40;
		/*
		 * in allen andern Modi bewegt sich der Enemie langsamer:
		 */
		else
			// movement Speed des Enemies einfliessen lassen
			movementSpeedTimeAdd = (100 - this.getAttributeValue(AttributeType.MOVEMENT_SPEED)) / 6 + 100;
		// neues Date erstellen in der Zukunft mit hinzuaddiertem MovementSpeed
		Date nextMovementTimeStamp = new Date(this.getLastMovementTimeStamp().getTime() + (long) movementSpeedTimeAdd);
		return nextMovementTimeStamp;
	}
}
