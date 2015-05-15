package pp2014.team32.server.EnemyHandling;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import javax.naming.TimeLimitExceededException;

import pp2014.team32.server.ServerMain;
import pp2014.team32.server.AStern.ASternAlgorithm;
import pp2014.team32.server.AStern.NoShortestPathFoundException;
import pp2014.team32.server.AStern.ServerNode;
import pp2014.team32.server.RunnableTaskManager.RunnableTaskManager;
import pp2014.team32.server.creatureManagement.FightCalculator;
import pp2014.team32.server.creatureManagement.RangeAndCollisionCalculator;
import pp2014.team32.server.serverOutput.OutputMessageHandler;
import pp2014.team32.server.updateTimer.EnemyAttackSleepRunnable;
import pp2014.team32.shared.entities.Enemy;
import pp2014.team32.shared.entities.GameCharacter;
import pp2014.team32.shared.entities.LevelMap;
import pp2014.team32.shared.entities.MovableObject;
import pp2014.team32.shared.enums.AttributeType;
import pp2014.team32.shared.enums.CreatureStatusType;
import pp2014.team32.shared.enums.EnemyStateType;
import pp2014.team32.shared.enums.UIObjectType;
import pp2014.team32.shared.messages.MovementInfo;
import pp2014.team32.shared.utils.Coordinates;
import pp2014.team32.shared.utils.PropertyManager;

/**
 * Diese Klasse regelt die gesamte Bewegung aller Enemies auf allen LevelMaps.
 * Es gibt 3 verschiedene Stati in der sich ein Enemie befinden kann. Er kann
 * planlos herumlaufen, angreifen und fluechten. Je nach dem, in welchem Status
 * sich ein Enemie befindet, werden unterschiedliche Wege mittels des AStern
 * berechnent. Dieser Thread hat 2 Timertasks
 * 
 * @author Peter Kings
 * 
 */
public class EnemyHandler implements Runnable {
	private final static Logger				LOGGER					= Logger.getLogger(ServerMain.class.getName());
	private static ArrayList<LevelMap>		levelMaps				= new ArrayList<LevelMap>();
	// Comparator zum Vergleich des Einschlafzeitpunkts der Enemies
	public final static Comparator<Enemy>	comparator				= new EnemyWakeUpComparator();
	// Priority queue mit enemies, mit dem Enemie an der Spitze, dessen
	// einschlafzeitpunkt am aeltesten ist, damit dieser wieder als erstes
	// aufgeweckt wird.
	private final static float				calledPerSecond			= (1000 / Integer.parseInt(PropertyManager.getProperty("server.lookForCharacters")));
	private final static float				sleepDayPerMili			= (float) Integer.parseInt(PropertyManager.getProperty("server.enemieSleepDayPerMili"));
	private final static float				sleepNightPerMili		= (float) Integer.parseInt(PropertyManager.getProperty("server.enemieSleepNightPerMili"));
	private final static Long				enemySleepTime			= Long.parseLong(PropertyManager.getProperty("server.EnemySleepTime"));
	private final static int				lookForCharacters		= Integer.parseInt(PropertyManager.getProperty("server.lookForCharacters"));
	private final static int				updateEnemyMovement		= Integer.parseInt(PropertyManager.getProperty("server.updateEnemyMovement"));
	private final static int				enemyRunawayPercentage	= Integer.parseInt(PropertyManager.getProperty("server.enemyRunawayPercentage"));
	private final static int				enemyAggroRange			= Integer.parseInt(PropertyManager.getProperty("server.EnemyAggroRange"));
	private final static int				enemyAttackRange		= Integer.parseInt(PropertyManager.getProperty("server.EnemyAttackRange"));

	/**
	 * Uebergabe aller Levelmaps des Spiels in diesem Konstruktor.
	 * 
	 * @author Peter Kings
	 * @param levelMaps alle LevelMaps des Spiels
	 */
	public EnemyHandler(ArrayList<LevelMap> levelMaps) {
		EnemyHandler.levelMaps = levelMaps;
	}

	/**
	 * Run: Erstellt zwei TimerTasks:
	 * 1) wiederholende Ueberpruefung der Stati aller Enemies auf LevelMaps, auf
	 * denen auch Charactere laufen.
	 * 2) wiederholende Durchfuehrung einer Bewegung auf dem Bewegungspfades
	 * jedes Enemies.
	 * Die Iterationsraten dieser beiden TimerTasks werden jeweils in der
	 * Property Datei festgelegt.
	 * 
	 * @author Peter Kings
	 */
	public void run() {
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					EnemyHandler.checkEnemiesForState();
				} catch (Exception e) {

				}
			}
		}, lookForCharacters, lookForCharacters);
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					EnemyHandler.updateEnemyMovement();
				} catch (Exception e) {

				}
			}
		}, updateEnemyMovement, updateEnemyMovement);
	}

	/* --------------- TimerTask Methoden --------------- */

	/**
	 * Diese Methode ist fuer die Bewegung aller Enemies auf allen LevelMaps
	 * verantwortlich. Sie bewegt jeden Enemie auf einer Levelmap, auf der sich
	 * auch ein Character befindet, bei einer Iteration dieser Methode, um eine
	 * weitere Position des BewegungsPfades. Der Bewegungspfad ist als
	 * Coordinates ArrayList in jedem Enemie gespeichert. Ein Enemie wird nur
	 * bewegt, wenn sein Zeitstempel fuer die naechste Bewegung abgelaufen ist.
	 * Ist der Bewegungspfad abgelaufen, wird dieser auf null gesetzt, damit die
	 * Methode zur kalkulation aller MovementPaths (checkEnemiesForState) erneut
	 * einen MovementPath berechnet. Bei durchgefuehrter Bewegung werden alle
	 * relevanten Clients informiert.
	 * 
	 * @author Peter Kings
	 */
	private static void updateEnemyMovement() {
		/*
		 * versuche, auf allen Levelmaps, auf denen sich ein Character befindet,
		 * die Enemies entlang ihres gespeicherten Bewegungspfades zu bewegen.
		 */
		try {
			// auf allen Levelmaps
			for (LevelMap lM : levelMaps) {
				// wenn sich KEIN Character in diesem Level befindet
				if (!EnemyHandler.isCharacterOnLevelMap(lM))
					// gehe zur naechsten iteration
					continue;
				// gehe alle bewegbaren Objekte dieser levelmap durch
				try {
					// ueber Bewegbare Objekte Iterieren
					Iterator<Map.Entry<Integer, MovableObject>> iterator = lM.getMovableObjects().entrySet().iterator();
					while (iterator.hasNext()) {
						Map.Entry<Integer, MovableObject> entry = iterator.next();
						MovableObject mO = entry.getValue();
						// mache nur was, wenn es ein enemy ist
						if (mO.getTYPE().equals(UIObjectType.ENEMY)) {
							// enemy cast
							Enemy enemy = (Enemy) mO;
							// aktuelle Zeit laden
							Date currentDate = new Date();
							/*
							 * ein eneme darf sich nur Bewegen wenn:
							 * -er nicht schlaeft
							 * -sein Movementpath nicht null ist
							 * -der Movementpath nicht leer ist -> also
							 * Coordinaten
							 * enthaelt
							 * -sein Zeitstaempel fuer den naechsten Move schon
							 * abgelaufen ist
							 */
							if (!enemy.getEnemyStateType().equals(EnemyStateType.SLEEP) && enemy.getMovementPath() != null && !enemy.getMovementPath().isEmpty()
									&& currentDate.after(enemy.getNextMovementTimeStamp())) {
								Coordinates nextCoordinates;
								// index ist noch nicht am ende des Movement
								// Pfades
								if (enemy.getMovementPath().size() > enemy.getMovementPathIndex()) {
									// naechsten Coordinates aus dem Movement
									// Path
									// nehmen
									nextCoordinates = enemy.getMovementPath().get(enemy.getMovementPathIndex());
									// movementPathindex um ein Movement
									// erhoehen
									enemy.setMovementPathIndex(enemy.getMovementPathIndex() + 1);
								}
								// MovementPath ist zuende
								else {
									/*
									 * wieder am Anfang starten, allerdings
									 * erste Koordinate weglassen, da
									 * dies die aktuellen Coordinaten des
									 * Enemies sind. Dadurch wuerde er
									 * sonst kurz stocken
									 */
									enemy.setMovementPathIndex(1);
									/*
									 * MovementPath auf null setzen: wird bei
									 * der
									 * naechsten Iteration von
									 * CheckforEnemyState
									 * neu berechnet
									 */
									enemy.setMovementPath(null);
									// zur naechsten Iteration gehen
									continue;
								}
								// Coordianten des Enemies vor der Bewegung
								Coordinates enemyCoordsBevorMovement = new Coordinates(enemy.getX(), enemy.getY());
								// Koordinaten differenz fuer Bewegungsrichtung
								// berechnen.
								Coordinates coordsDiff = enemyCoordsBevorMovement.subtractCoordinates(nextCoordinates);
								// Enemie nun tatsaechlich bewegen
								enemy.move(nextCoordinates.x, nextCoordinates.y);
								/*
								 * wenn eine Kollision vorliegt, wieder
								 * zurueckbewegen und aktuellen shortest Path
								 * gleich
								 * null setzen. dieser wird dann bei der
								 * naechsten
								 * iteration der shortest Path kalkulation
								 * (checkforenemystate) erneut
								 * berechnet
								 */
								if (RangeAndCollisionCalculator.checkForAndHandleCollision(enemy, lM)) {
									enemy.move(enemyCoordsBevorMovement.x, enemyCoordsBevorMovement.y);
									enemy.setMovementPath(null);
									/*
									 * wieder am Anfang starten, allerdings
									 * erste Koordinate weglassen, da
									 * dies die aktuellen Coordinaten des
									 * Enemies sind. Dadurch wuerde er
									 * sonst kurz stocken
									 */
									enemy.setMovementPathIndex(1);
									continue;
								}
								// Movement Infor an alle relevanten Clients
								// senden
								OutputMessageHandler.sendMessageToSetOfUsersOnThisLevelMap(new MovementInfo(enemy.ID, getCreatureStatusTypeForCoordinatesDifference(coordsDiff),
										enemyCoordsBevorMovement.x, enemyCoordsBevorMovement.y, enemy.getX(), enemy.getY()), lM.getLevelID());
								/*
								 * letzten Bewegungszeitstempel des Enemies auf
								 * aktuelle Zeit setzen
								 */
								enemy.setLastMovementTimeStamp(new Date());
							}
						}
					}
				} catch (ConcurrentModificationException cME) {

				}
			}
		} catch (ConcurrentModificationException cME) {
			/*
			 * threads manipulieren /greifen auf gleiche Objekte
			 */
		} catch (Exception e) {
			LOGGER.warning("Enemies konnten nicht bewegt werden: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Diese Methode geht alle Enemies auf allen Levelmaps durch. Hierbei prueft
	 * sie stets zeurst, ob sich auf einer Levelmap auch Character befinden. Ist
	 * dies der Fall, so werden je nach dem aktuellen Status des Enemies
	 * unterschiedliche Aktionen ausgefuehrt. Ein Enemy kann gerade Attackieren,
	 * Schlafen, Herumlaufen oder Wegrennen. Wenn es attackiert oder herumlaeuft
	 * sucht es jeweils die Umgebung nach angreifbaren Charactern ab. Wenn es
	 * schlaeft wird geprueft ob es wieder aufgeweckt werden kann. Wenn es
	 * wegrennt wird (beim ersten aufruf in diesem Status) ein Bewegungspfad zu
	 * einem weit entfernent Punkt berechnet. Im "Herumlauf-Status" fallen
	 * Enemies zu einem prozentsatz in den Schlaf.
	 * 
	 * @author Peter Kings
	 */
	private static void checkEnemiesForState() {
		try {
			// auf allen LevelMaps
			for (LevelMap lM : levelMaps) {
				// wenn sich dort KEIN Character befindet
				if (!EnemyHandler.isCharacterOnLevelMap(lM))
					// springe zur naechsten Iteration
					continue;
				try {
					// ueber Bewegbare Objekte Iterieren
					Iterator<Map.Entry<Integer, MovableObject>> iterator = lM.getMovableObjects().entrySet().iterator();
					while (iterator.hasNext()) {
						Map.Entry<Integer, MovableObject> entry = iterator.next();
						MovableObject mO = entry.getValue();
						// wenn es sich um einen Enemy handelt
						if (mO.getTYPE().equals(UIObjectType.ENEMY)) {
							// cast
							Enemy enemy = (Enemy) mO;
							// falls die Lebenspunkte unter 40% sind
							if (enemy.getAttributes().get(AttributeType.HEALTH) < enemyRunawayPercentage)
								// Enemy soll weglaufen
								enemy.setEnemyStateType(EnemyStateType.ESCAPE);
							// ueberpruefen, ob ein GameCharacter in der naehe
							// ist
							GameCharacter gC = null;
							/*
							 * wenn sich der Character im walkaround oder im
							 * attack state befindet, soll er nach in der naehe
							 * befindlichen Charactern suchen
							 */
							if (enemy.getEnemyStateType().equals(EnemyStateType.WALKAROUND) || enemy.getEnemyStateType().equals(EnemyStateType.ATTACK)) {
								gC = checkForCharacterToAttack(lM, enemy);
								// wenn ja (= ist nicht leer)
								if (gC != null)
									enemy.setEnemyStateType(EnemyStateType.ATTACK);
								else
									enemy.setEnemyStateType(EnemyStateType.WALKAROUND);
							}
							/* selektierung nach EnemyState */
							switch (enemy.getEnemyStateType()) {
							// Enemy attackiert gerade
							case ATTACK:
								/*
								 * in der Naehe befindliche Characters angreifen
								 * und Bewegungspfad zu diesem berechnen
								 */
								attackGameCharacter(lM, enemy, gC);
								break;
							// Enemy flieht
							case ESCAPE:
								// Bewegungspfad zum Weglaufen berechnen
								runAway(lM, enemy);
								break;
							// Enemy schlaeft
							case SLEEP:
								// Clients informieren das dieser schlaeft
								OutputMessageHandler.sendMessageToSetOfUsersOnThisLevelMap(
										new MovementInfo(enemy.getID(), CreatureStatusType.SLEEPING, enemy.getX(), enemy.getY(), enemy.getX(), enemy.getY()), lM.getLevelID());
								/*
								 * pruefen, ob dieser bereits aufgeweckt werden
								 * kann
								 */
								wakeUpEnemies(enemy);
								break;
							// Enemy laeuft herum
							case WALKAROUND:
								/*
								 * neuen Movement Path berechnen fuer eine
								 * umherirrende Laufbewegung.
								 */
								changeToRandomWalkaroundToSleepEnemyStateType(enemy, lM);
								enemyRandomWalk(enemy, lM);
								break;
							default:
								break;
							}
						}
					}
				} catch (ConcurrentModificationException cME) {
					/*
					 * threads manipulieren /greifen auf gleiche Objekte
					 */
				}
			}
		} catch (ConcurrentModificationException cME) {
			/*
			 * threads manipulieren /greifen auf gleiche Objekte
			 */
		} catch (Exception e) {
			LOGGER.warning("Enemies konnten nicht bewegt werden: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/* --------------- Enemy State Methoden --------------- */
	/**
	 * Diese Methode berechnet einen neuen Bewegungspfad fuer einen Enemie, der
	 * planlos herumlaufen soll. Diese werden allerdings nur neu berechnet, wenn
	 * der Bewegungspfad null, oder bereits vollstaendig abgelaufen ist. Die
	 * Bewegungspfad Berechnung erfolgt mit dem AStern Algorithmus.
	 * 
	 * @param enemy Enemy fuer den neue Herumlauf Coordinates berechnet werden
	 *            sollen
	 * @param lM LevelMap, auf der sich der Enemie befindet
	 */
	private static void enemyRandomWalk(Enemy enemy, LevelMap lM) {
		/*
		 * Abbrechen, falls noch ein shortest Path vorhanden ist, d.h.
		 * immer nur neu berechnent
		 * -wenn dieser gleich null ist oder
		 * -abgelaufen
		 */
		if (enemy.getMovementPath() != null && !enemy.getMovementPath().isEmpty())
			return;

		// zufaellige Coordinates
		Coordinates randomCoords = null;
		try {
			randomCoords = calculateNewRandomCoords(enemy, lM, 0, 250);
		} catch (TimeLimitExceededException e1) {
			LOGGER.warning(e1.getMessage());
		}

		/*
		 * versuche einen kuerzesten Weg zu diesen zufaelligen Koordinaten zu
		 * berechnen.
		 */
		try {
			calculateShortestPath(enemy, lM, randomCoords);
		} catch (NoShortestPathFoundException e) {
			LOGGER.warning("Monster konnte nicht umherirren");
			LOGGER.warning(e.getMessage());
		}
	}

	private static Coordinates calculateNewRandomCoords(Enemy enemy, LevelMap lM, int minRange, int maxRange) throws TimeLimitExceededException {
		Coordinates randomCoords;
		Date abortTime = new Date((new Date()).getTime() + (long) 500);
		/*
		 * solange es eine Kolision eines Enemy an den neuen Koordinaten gaebe,
		 * berechne neue zufaellige Koordinaten
		 */
		do {
			Date currentTime = new Date();
			/*
			 * der Thread soll sich hier auf keinenfall deadlocken. deshalb soll
			 * nach zu langer zeit keine neue Koordinate mehr gesucht werden. in
			 * diesem Fall gibt es dann keine Passenden Random Coords
			 */
			if (currentTime.after(abortTime))
				throw new TimeLimitExceededException("Es konnten keine Random Coordinaten gefunden werden.");
			/*
			 * neue zufaellige Koordinaten, maximal 250 pix entfernt
			 * Abstand
			 * entfernt
			 */
			randomCoords = getRandomCoordinatesAroundCoordinates(new Coordinates(enemy.getX(), enemy.getY()), minRange, maxRange);
		} while (RangeAndCollisionCalculator.checkForCollisionWithNoItemFixedObject(randomCoords.x, randomCoords.y, enemy.getWidth(), enemy.getHeight(), lM));
		return randomCoords;
	}

	/**
	 * Diese Methode berechnet einen neuen Bewegungspfad fuer einen Enemie, der
	 * fluechten will. Diese werden allerdings nur neu berechnet, wenn
	 * der Bewegungspfad null, oder bereits vollstaendig abgelaufen ist. Die
	 * Bewegungspfad Berechnung erfolgt mit dem AStern Algorithmus.
	 * 
	 * @author Peter Kings
	 * @param enemy Enemy fuer den neue Weglauf Coordinates berechnet werden
	 *            sollen
	 * @param lM LevelMap, auf der sich der Enemie befindet
	 */
	private static void runAway(LevelMap lM, Enemy enemy) {
		/*
		 * Abbrechen, falls noch ein shortest Path vorhanden ist, d.h.
		 * immer nur neu berechnent
		 * -wenn dieser gleich null ist oder
		 * -abgelaufen
		 */
		if (enemy.getMovementPath() != null && !enemy.getMovementPath().isEmpty())
			return;
		// status auf Weglaufen setzen
		enemy.setEnemyStateType(EnemyStateType.ESCAPE);
		// zufaellige Coordinates
		Coordinates randomCoords = null;
		/*
		 * solange es eine Kolision eines Enemy an den neuen Koordinaten gaebe,
		 * berechne neue zufaellige Koordinaten
		 */
		try {
			randomCoords = EnemyHandler.calculateNewRandomCoords(enemy, lM, 400, 600);
		} catch (TimeLimitExceededException e1) {
			LOGGER.warning(e1.getMessage());
		}
		/*
		 * versuche einen kuerzesten Weg zu diesen zufaelligen Koordinaten zu
		 * berechnen.
		 */
		try {
			calculateShortestPath(enemy, lM, randomCoords);
		} catch (NoShortestPathFoundException e) {
			LOGGER.warning("Monster konnte nicht weglaufen");
			LOGGER.warning(e.getMessage());
		}
	}

	/**
	 * Diese Methode checkt die Umgebung des uebergebenen Enemies nach
	 * angreifbaren Characteren. Ist ein solcher in der Naehe, wird der
	 * EnemyStateType auf attack gesetzt. Anderenfalls auf Walkaround.
	 * 
	 * @author Peter Kings
	 * @return ist ein GameCharacter in der naehe, wird dieser zurueckgegeben.
	 *         sonst null.
	 * @param enemy Enemy fuer den die Umgebung nach angreifbaren Characteren
	 *            untersucht werden soll
	 * @param lM LevelMap, auf der sich der Enemie befindet
	 */
	private static GameCharacter checkForCharacterToAttack(LevelMap lM, Enemy enemy) {
		/*
		 * Suche einen bzw. den naechsten Character in der Naehe. Die AggroRange
		 * wird dabei aus der Settings Datei geladen.
		 */
		GameCharacter c = (GameCharacter) RangeAndCollisionCalculator.getNearTargetInRange(enemy, UIObjectType.CHARACTER, lM, enemyAggroRange);
		// wenn ein Character gefunden wurde
		if (c != null) {
			// enemie greift nun an!
			enemy.setEnemyStateType(EnemyStateType.ATTACK);
			// ja, es ist ein Enemy der anzugreifen ist
			return c;
		} else {
			// Enemy laeuft rum
			enemy.setEnemyStateType(EnemyStateType.WALKAROUND);
		}
		// nein, es ist kein Character zum angreifen
		return null;
	}

	/**
	 * Greift den uebergebenen GameCharacter an, wenn dieser in ausreichender
	 * Naehe ist. Unter Umstaenden (MovemenPath leer, teilweise abgelaufen
	 * oder ganz abgelaufen) wird der BewegungsPfad neu berechnet. Die
	 * Bewegungspfad Berechnung erfolgt mit dem AStern Algorithmus.
	 * 
	 * @param enemy Enemy der angreift
	 * @param c anzugreifender GameCharacter
	 * @param lM Levelmap, auf der sich beide Befinden
	 */
	private static void attackGameCharacter(LevelMap lM, Enemy enemy, GameCharacter c) {
		// enemie greift nun an!
		enemy.setEnemyStateType(EnemyStateType.ATTACK);
		// Zielkoordinaten sind die Koordinaten des Characters
		Coordinates destinationCoordinates = new Coordinates(c.getX(), c.getY());
		/*
		 * Wenn sich ein Character in der Naehe befindet, greife diesen an.
		 * Die Attack Range wird aus der property file geladen. Die
		 * Distanzberechnung erfolgt ueber eine Manhatten Distanz
		 * Approximation.
		 */
		if (ServerNode.manhattenDistance(new Coordinates(enemy.getX(), enemy.getY()), destinationCoordinates) < enemyAttackRange) {
			if (enemy.attackEnabled == true) {
				// greife den Character an
				FightCalculator.attackGameCharacter(enemy, c, lM);
				/*
				 * der boolean, ob ein Enemy attackieren kann, wird mit
				 * einer Timer Task in x Sekunden wieder auf true gesetzt. Dazu
				 * wird ein Runnable dem RunnableThreadPoolManager uebergeben.
				 */
				RunnableTaskManager.addRunnableTask(new EnemyAttackSleepRunnable(enemy));
			}
		}
		/*
		 * Abbrechen, falls noch ein shortest Path vorhanden ist, d.h.
		 * immer nur neu berechnent
		 * -wenn dieser gleich null ist oder
		 * - die ersten 7 Schritte des Pfades abgelaufen sind (dadurch wird
		 * gewaehrleistet, das der Enemie dem Character wirklich hinterher
		 * laeuft und nicht immer nur zu den alten Koordinaten) oder
		 * - er sogar komplett abgelaufen wurde
		 */
		if (enemy.getMovementPath() != null && !enemy.getMovementPath().isEmpty() && enemy.getMovementPathIndex() < 7)
			return;
		/*
		 * versuche einen kuerzesten Weg zu diesen Character Koordinaten zu
		 * berechnen.
		 */
		try {
			calculateShortestPath(enemy, lM, destinationCoordinates);
		} catch (NoShortestPathFoundException e) {
			LOGGER.warning("Beim Attackieren eines Characters ist ein Fehler aufgetreten");
			LOGGER.warning(e.getMessage());
		}
	}

	/**
	 * Diese Methode prueft fuer einen Enemy, ob dieser wieder aufgeweckt werden
	 * kann, oder ob die Differenz zwischen aktueller Zeit und dem
	 * Einschlafzeitpunkt noch zu gering ist.
	 * 
	 * @author Peter Kings
	 * @param enemy Enemy, der moeglicherweise aufgeweckt wird
	 */
	private static void wakeUpEnemies(Enemy enemy) {
		// aktueller Zeitstempel
		Date currentDate = new Date();
		/*
		 * wenn die Zeitdifferenz des aktuellen Zeistempels und des
		 * Einschlafzeitpunktes eines Enemies groesser ist, als die
		 * Schlafzeit(wird in Property File gesetzt) der Enemies
		 */
		if ((currentDate.getTime() - enemy.getSleepTimeStamp().getTime()) / 1000 > enemySleepTime) {
			// wecke diesen Enemy wieder auf
			enemy.setEnemyStateType(EnemyStateType.WALKAROUND);
		}
	}

	/* --------------- Hilfsmethoden --------------- */
	/**
	 * Diese Methode setzt einen uebergebenen Enemy zufaellig in den Schlaf. Die
	 * Schlafwahrscheinlichkeit pro Sekunde, also zu welcher Wahrscheinlichkeit
	 * ein Enemie (im Status Walkaround) in den Schlaf faellt, wird in der
	 * property file gesetzt. Dabei unterscheidet man zwischen Tag und Nacht
	 * Level.
	 * 
	 * @author Peter Kings
	 * @param enemy Enemy der moeglicherweise eingeschlaefert werden soll
	 * @param levelMap LevelMap, auf der sich der Enemy befindet
	 */
	private static void changeToRandomWalkaroundToSleepEnemyStateType(Enemy enemy, LevelMap levelMap) {
		float sleepPerMili;
		// falles es ein Nachtlevel ist: hoehere Schlafwahrscheinlichkeit
		if (levelMap.isNightLevel())
			// prozent umrechnen auf die aufrufrate dieser methode
			sleepPerMili = (float) (sleepNightPerMili / calledPerSecond);
		else
			// prozent umrechnen auf die aufrufrate dieser methode
			sleepPerMili = (float) (sleepDayPerMili / calledPerSecond);
		/*
		 * nur falls dieser auch rumlaueft. wenn er attackiert oder weglaeuft
		 * soll er nicht schlafen.
		 */
		if (enemy.getEnemyStateType().equals(EnemyStateType.WALKAROUND)) {
			// zufallszahl zwischen 0 und 999
			int random = (int) (Math.random() * 1000);
			// falls die zufallszahl kleiner der umgerechneten Prozentrate ist
			if (random < sleepPerMili) {
				// soll der enemie schlafen
				enemy.setEnemyStateType(EnemyStateType.SLEEP);
				// dies allen relevanten Clients mitteilen
				OutputMessageHandler.sendMessageToSetOfUsersOnThisLevelMap(new MovementInfo(enemy.getID(), CreatureStatusType.SLEEPING, enemy.getX(), enemy.getY(), enemy.getX(), enemy.getY()),
						levelMap.getLevelID());
				// einschlafzeitpunkt setzen
				enemy.setSleepTimeStamp(new Date());
			}
		}
	}

	/**
	 * Berechnen den kuerzesten Weg eines Enemies zu gegebenen Zielkoordinaten.
	 * 
	 * @author Peter Kings
	 * @param enemy der zu Bewegende enemy
	 * @param lM Levelmap, auf der sich der Enemy befindet
	 * @param destinationCoordinates Zielkoodrinaten
	 * @throws NoShortestPathFoundException wird geworfen, falls kein kuerzester
	 *             Weg gefunden wurde
	 */
	private static void calculateShortestPath(Enemy enemy, LevelMap lM, Coordinates destinationCoordinates) throws NoShortestPathFoundException {
		/*
		 * neues aStern Object mit der LevelMap, auf der ein kuerzester Weg
		 * berechnet werden soll
		 */
		ASternAlgorithm aStern = new ASternAlgorithm(lM);
		/*
		 * kuerzesten Weg mit dem AStern Algorithmus berechnen und diesen in
		 * eine ArrayList abspeichern
		 */
		ArrayList<Coordinates> shortestPath = aStern.calculateShortestPathForCreatureToCoordinates(new Coordinates(enemy.getX(), enemy.getY()), destinationCoordinates, enemy);
		// Bewegungspfad des enemies setzen
		enemy.setMovementPath(shortestPath);
		/*
		 * wieder am Anfang starten, allerdings erste Koordinate weglassen, da
		 * dies die aktuellen Coordinaten des Enemies sind. Dadurch wuerde er
		 * sonst kurz stocken
		 */
		enemy.setMovementPathIndex(1);
	}

	/**
	 * Diese Methode liefert zufaellige Koordinaten in einem Umkreis (eigentlich
	 * quadrat) um die gegebenen Koordinaten. Es wird eine maximale Range
	 * uebergeben.
	 * 
	 * @author Peter Kings
	 * @param coords Ausgangskoordinaten
	 * @param range Radius (eigenlich Quadrat)
	 * @return zufaellige Coordinaten
	 */
	@SuppressWarnings("unused")
	private static Coordinates getRandomCoordinatesAroundCoordinates(Coordinates coords, int range) {
		// Ueberlagerte Methode aufrufen
		return getRandomCoordinatesAroundCoordinates(coords, 0, range);
	}

	/**
	 * Diese Methode liefert zufaellige Koordinaten in einem Umkreis (eigentlich
	 * quadrat) um die gegebenen Koordinaten. Es wird eine minimale und eine
	 * maximale Range uebergeben.
	 * 
	 * @author Peter Kings
	 * @param coords Ausgangskoordinaten
	 * @param minRange minimale abweichung jeweil x und y
	 * @param maxRange maximale abweichung jeweils x und y
	 * @return neue random Coordianten
	 */
	private static Coordinates getRandomCoordinatesAroundCoordinates(Coordinates coords, int minRange, int maxRange) {
		// nach rechts oder nach links random?
		int plusMinus = (int) (Math.random() * 2);
		/*
		 * liefert eine Differenz (jeweils fuer x und y) zwischen der minimalen
		 * und der maximalen Range
		 */
		int xDiff = (int) (Math.random() * (maxRange - minRange)) + minRange;
		int yDiff = (int) (Math.random() * (maxRange - minRange)) + minRange;
		// random Zahl in addieren oder subtrahieren umrechnen
		if (plusMinus == 0)
			plusMinus = -1;
		else
			plusMinus = 1;
		// random Coordinaten erzeugen
		Coordinates randomCoords = new Coordinates(coords.x + plusMinus * xDiff, coords.y + plusMinus * yDiff);
		return randomCoords;
	}

	/**
	 * Diese Methode prueft, ob sich ein Character auf der uebergebenen.
	 * LevelMap befindet
	 * 
	 * @param levelMap LevelMap, auf der gecheckt werden soll, ob ein Character
	 *            darauf ist.
	 * @return true: ja, es befindet sich einer darauf. false: nein,...
	 */
	private static boolean isCharacterOnLevelMap(LevelMap levelMap) {
		/* fuer jedes bewegbare Objekt auf der uebergebenen LevelMap */
		for (MovableObject mO : levelMap.getMovableObjects().values())
			// falls dieses objekt ein Character ist
			if (mO.getTYPE().equals(UIObjectType.CHARACTER))
				// ja, es ist ein Character auf der LevelMap
				return true;
		/* falls es hier hin kommt, ist kein Character auf der LevelMap */
		return false;
	}

	/**
	 * Diese Methode berechnet aus einer Koordinatendifferenz zwischen Start-
	 * und Zielkoordinaten die zugehoerige Bewegungsrichtung. Hier sind 8
	 * Bewegungsrichtungen moeglich. z.B. west, northwest, north, ...
	 * 
	 * @author Peter Kings
	 * @param coordsDiff Differenz der Start- und Zielkoordinaten
	 * @return Bewegungsrichtung
	 */
	public static CreatureStatusType getCreatureStatusTypeForCoordinatesDifference(Coordinates coordsDiff) {
		// default: der Character steht
		CreatureStatusType creatureStatusType = CreatureStatusType.STANDING;
		/*
		 * Hier wird je nach Koordinaten Differenz eine andere Bewegungsrichtung
		 * zurueckgegeben.
		 * Jeweils wenn x kleiner 0 geht er nach osten, wenn groesser null geht
		 * er nach westen. Das gleiche gilt fuer x...
		 */
		if (coordsDiff.x < 0 && coordsDiff.y < 0)
			creatureStatusType = CreatureStatusType.MOVING_SOUTHEAST;
		else if (coordsDiff.x == 0 && coordsDiff.y < 0)
			creatureStatusType = CreatureStatusType.MOVING_SOUTH;
		else if (coordsDiff.x < 0 && coordsDiff.y == 0)
			creatureStatusType = CreatureStatusType.MOVING_EAST;
		else if (coordsDiff.x > 0 && coordsDiff.y < 0)
			creatureStatusType = CreatureStatusType.MOVING_SOUTHWEST;
		else if (coordsDiff.x > 0 && coordsDiff.y == 0)
			creatureStatusType = CreatureStatusType.MOVING_WEST;
		else if (coordsDiff.x > 0 && coordsDiff.y > 0)
			creatureStatusType = CreatureStatusType.MOVING_NORTHWEST;
		else if (coordsDiff.x == 0 && coordsDiff.y > 0)
			creatureStatusType = CreatureStatusType.MOVING_NORTH;
		else if (coordsDiff.x < 0 && coordsDiff.y > 0)
			creatureStatusType = CreatureStatusType.MOVING_NORTHEAST;
		// diese Bewegungsrichtung zurueckgeben
		return creatureStatusType;
	}
}
