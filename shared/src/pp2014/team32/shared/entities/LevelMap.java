package pp2014.team32.shared.entities;

import pp2014.team32.shared.enums.*;
import pp2014.team32.shared.utils.Coordinates;

import java.io.Serializable;
import java.util.*;

/**
 * Diese Klasse beschreibt eine LevelMap in ihren Eigenschaften und Funktionen. Sie enthaelt alle wichtigen Elemente für
 * das Spiel und ist DIE Kernklasse in der Serverlogik. Hier werden die Level untereinander verlinkt, die enemies sowie
 * die Leveldaten (Waende, Enemies usw), die Gamecharacter usw. abgelegt.
 *
 * @author Moritz Bittner
 * @author Can Dogan
 * @author Pascal Brokmeier
 */
public class LevelMap implements Serializable {

    private static final long serialVersionUID = -4388684382737342180L;

    // Vorschlag: mehrfach verkettete LevelMaps
    // vorherige Ebene
    private transient ArrayList<LevelMap> previousStageLevelMap;
    // naechste Ebene
    private transient ArrayList<LevelMap> nextStageLevelMap;
    // zur Linken
    private transient LevelMap leftLevelMap;
    // zur Rechten
    private transient LevelMap rightLevelMap;
    // ID des Levels
    protected int levelID;
    // LevelTiefe
    protected int levelDepth;
    // LevelTyp
    protected LevelMapType levelType;
    // gesperrt
    protected boolean locked;
    // wichtige Koordinaten
    public Coordinates start, max, stadium;
    // Nachtlevel?
    protected boolean isNightLevel;

    // Mapstruktur

    // fixer Hintergrund einer Map
    protected HashMap<Coordinates, FixedObject> fixedObjects;
    // beweglicher Vordergrund, so allerdings je Feld nur ein Object moeglich
    protected HashMap<Integer, MovableObject> movableObjects;

    // Spiel
    protected TeamType team1, team2;
    protected String levelDescription;
    protected CityType city;

    // Fog Of War
    protected Set<Coordinates> visiblePositions = new HashSet<>();

    /**
     * Default Level generieren.
     *
     * @param levelMapType
     * @param levelID
     * @author Brokmeier, Pascal
     */
    public LevelMap(LevelMapType levelMapType, int levelID) {
        /*
         * previousStageLevelMap = new
		 * LevelMap[levelPredAmounts.get(levelDepth)];
		 * nextStageLevelMap = new LevelMap[levelSuccAmounts.get(levelDepth)];
		 */
        fixedObjects = new HashMap<Coordinates, FixedObject>();
        movableObjects = new HashMap<Integer, MovableObject>();
        levelDepth = 0;
        previousStageLevelMap = new ArrayList<LevelMap>();
        nextStageLevelMap = new ArrayList<LevelMap>();
        leftLevelMap = null;
        rightLevelMap = null;
        this.levelID = levelID;
        levelType = levelMapType;
        locked = false;
        isNightLevel = false;
        this.team1 = TeamType.UNDEF;
        this.team2 = TeamType.UNDEF;
        this.levelDescription = "Undefined";
        this.city = CityType.UNDEF;

    }


    /**
     *
     *
     * @param leftLevelMap setzt die ID der LevelMap Links fuer das Taxi links. Dies ist notwendig fuer den Client, der
     *                     die "Ziel" LevelID braucht wenn der Spieler das Level wechseln will.
     * @author Brokmeier, Pascal
     */

    private void setLeftLevelMapIDForLeftTaxi(LevelMap leftLevelMap) {
        // wir iterieren ueber alle movables
        for (MovableObject movable : movableObjects.values()) {
            // filtern auf Taxis
            if (movable.TYPE == UIObjectType.TAXI) {
                Taxi taxi = (Taxi) movable;
                // und setzen fuer unser nach links geh taxi die levelID
                if (taxi.getDirection() == Direction.LEFT) {
                    taxi.setDestinationLevelMap(leftLevelMap);
                }
            }
        }
    }

    public ArrayList<LevelMap> getPreviousStageLevelMap() {
        return previousStageLevelMap;
    }

    public void setPreviousStageLevelMap(ArrayList<LevelMap> previousStageLevelMap) {
        this.previousStageLevelMap = previousStageLevelMap;

        setPreviousLevelMapIDsForPrevTaxis(previousStageLevelMap);
    }

    /**
     * siehe {@link #setLeftLevelMapIDForLeftTaxi(LevelMap)}
     * @author Brokmeier, Pascal
     * @param previousStageLevelMap
     */

    private void setPreviousLevelMapIDsForPrevTaxis(ArrayList<LevelMap> previousStageLevelMap) {
        ArrayList<Taxi> taxis = new ArrayList<>();
        // wir iterieren ueber alle previousStageLevelMaps
        for (MovableObject movable : movableObjects.values()) {
            // filtern auf taxis und legen sie in eine Liste
            if (movable.TYPE == UIObjectType.TAXI) {
                Taxi taxi = (Taxi) movable;
                if (taxi.getDirection() == Direction.UP && taxi.getDestinationLevelMap() == null) {
                    taxis.add((Taxi) movable);
                }
            }
        }

        // wir gehen ueber alle previousStageLevelMaps
        for (LevelMap levelMap : previousStageLevelMap) {
            // wenn noch taxis da sind
            if (taxis.size() > 0) {
                // nehmen wir uns eins und setzen die levelmapID
                taxis.get(0).setDestinationLevelMap(levelMap);
                // danach loeschen wir das taxi aus der liste
                taxis.remove(0);
            }
        }
    }

    public ArrayList<LevelMap> getNextStageLevelMap() {
        return nextStageLevelMap;
    }

    public LevelMap getRightLevelMap() {
        return rightLevelMap;
    }

    public void setRightLevelMap(LevelMap rightLevelMap) {
        this.rightLevelMap = rightLevelMap;
        setRightLevelMapIDForRightTaxi(rightLevelMap);

    }

    /**
     * siehe {@link #setLeftLevelMapIDForLeftTaxi(LevelMap)}
     * @author Brokmeier, Pascal
     * @param rightLevelMap
     */

    private void setRightLevelMapIDForRightTaxi(LevelMap rightLevelMap) {
        // wir iterieren ueber alle movables
        for (MovableObject movable : movableObjects.values()) {
            // filtern auf Taxis
            if (movable.TYPE == UIObjectType.TAXI) {
                Taxi taxi = (Taxi) movable;
                // und setzen fuer unser nach links geh taxi die levelID
                if (taxi.getDirection() == Direction.RIGHT) {
                    taxi.setDestinationLevelMap(rightLevelMap);
                }
            }
        }
    }

    public void setGame(TeamType team1, TeamType team2, String levelDescription, CityType city) {
        this.team1 = team1;
        this.team2 = team2;
        this.levelDescription = levelDescription;
        this.city = city;
    }

    public String getLevelDescription() {
        return levelDescription;
    }

    public void addSuccessor(LevelMap levMap) {

        setNextLevelMapIDForNextTaxi(levMap);

        this.nextStageLevelMap.add(levMap);
    }

    public Set<Coordinates> getVisiblePositions() {
        return visiblePositions;
    }

    public void addVisiblePosition(Coordinates visiblePosition) {
        this.visiblePositions.add(visiblePosition);
    }

    public void setVisiblePositions(Set<Coordinates> visiblePositions) {
        this.visiblePositions = visiblePositions;
    }


    /**
     * siehe {@link #setLeftLevelMapIDForLeftTaxi(LevelMap)}
     * @author Brokmeier, Pascal
     * @param levMap
     */

    private void setNextLevelMapIDForNextTaxi(LevelMap levMap) {
        ArrayList<Taxi> taxis = new ArrayList<>();
        // wir iterieren ueber alle previousStageLevelMaps
        for (MovableObject movable : movableObjects.values()) {
            // filtern auf taxis und legen sie in eine Liste
            if (movable.TYPE == UIObjectType.TAXI) {
                Taxi taxi = (Taxi) movable;
                if (taxi.getDirection() == Direction.DOWN && taxi.getDestinationLevelMap() == null) {
                    taxis.add((Taxi) movable);
                }
            }
        }

        // wir gehen ueber alle previousStageLevelMaps

        if (taxis.size() > 0) {
            // nehmen wir uns eins und setzen die levelmapID
            taxis.get(0).setDestinationLevelMap(levMap);
            // danach loeschen wir das taxi aus der liste
            taxis.remove(0);
        }
    }

    /**
     * convenience Method to unlock all taxis on airport level
     *
     * @author Brokmeier, Pascal
     */
    public void unlockAllTaxis() {
        for (MovableObject movableObject : movableObjects.values()) {
            if (movableObject.TYPE == UIObjectType.TAXI) {
                Taxi taxi = (Taxi) movableObject;
                taxi.setUnlocked(true);
            }
        }
    }

    /**
     * Gets the taxis for the passed direction.
     * @param direction
     * @return
     * @author Brokmeier, Pascal
     */
    public List<Taxi> getAllTaxisForDirection(Direction direction) {
        List<Taxi> taxis = getAllTaxis();
        for (Taxi taxi : taxis) {
            if (taxi.getDirection() != direction) {
                taxis.remove(taxi);
            }
        }
        return taxis;
    }

    /**
     * @return
     * @author Brokmeier, Pascal
     */
    public List<Taxi> getAllTaxis() {
        List<Taxi> taxis = new ArrayList<>();

        for (MovableObject movableObject : movableObjects.values()) {
            if (movableObject.TYPE == UIObjectType.TAXI) {
                Taxi taxi = (Taxi) movableObject;
                taxis.add(taxi);
            }
        }
        return taxis;
    }

    /**
     * Getter und Setter sowie einfache Operationen auf der LevelMap (add / remove)
     *
     * @return
     */
    public HashMap<Coordinates, FixedObject> getFixedObjects() {
        return fixedObjects;
    }

    public void addFixedObject(FixedObject fO) {
        this.fixedObjects.put(new Coordinates(fO.x, fO.y), fO);
    }

    public HashMap<Integer, MovableObject> getMovableObjects() {
        return movableObjects;
    }

    public void addMovableObject(MovableObject mO) {
        movableObjects.put(mO.getID(), mO);
    }

    public void removeFixedObject(int x, int y) {
        fixedObjects.remove(new Coordinates(x, y));
    }

    public FixedObject getFixedObject(int x, int y) {
        return fixedObjects.get(new Coordinates(x, y));
    }

    public void removeMovableObject(int movableObjectID) {
        movableObjects.remove(movableObjectID);

    }

    public int getLevelID() {
        return this.levelID;
    }

    public LevelMapType getLevelMapType() {
        return levelType;
    }

    public TeamType getTeam1() {
        return team1;
    }

    public TeamType getTeam2() {
        return team2;
    }

    public CityType getCity() {
        return city;
    }

    public boolean isNightLevel() {
        return isNightLevel;
    }

    public LevelMap getLeftLevelMap() {
        return leftLevelMap;
    }

    public void setLeftLevelMap(LevelMap leftLevelMap) {
        this.leftLevelMap = leftLevelMap;
        setLeftLevelMapIDForLeftTaxi(leftLevelMap);
    }

    public void setNightLevel(boolean isNightLevel) {
        this.isNightLevel = isNightLevel;
    }

}
