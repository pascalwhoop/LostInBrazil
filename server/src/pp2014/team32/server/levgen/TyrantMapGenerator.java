package pp2014.team32.server.levgen;

import pp2014.team32.server.Database.DatabaseConnection;
import pp2014.team32.shared.entities.*;
import pp2014.team32.shared.enums.*;
import pp2014.team32.shared.utils.Coordinates;
import pp2014.team32.shared.utils.PropertyManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * source: http://www.roguebasin.com/?title=Dungeon-Building_Algorithm
 * Diese Klasse generiert das Level
 * 
 * @author Dogan, Can
 */
public class TyrantMapGenerator {

	private int								size				= Integer.parseInt(PropertyManager.getProperty("levgen.mapSize"));
	private UIObjectType[][]				data				= new UIObjectType[size][size];
	private final static int				SCALEFACTOR			= Integer.parseInt(PropertyManager.getProperty("levgen.scaleFactor"));
	private Random							generator;
	private int								difficulty			= 1;																		// default
	// value
	private ArrayList<Coordinates>			possibleEntryPoints	= new ArrayList<>();
	private ArrayList<Coordinates>			emptyPositions;
	private HashMap<Coordinates, Direction>	taxiPositions		= new HashMap<>();
	private static final int				MAX_WALL_INDEX		= Integer.parseInt(PropertyManager.getProperty("wall.maxWallIndex"));
	private static final int				STADIUM_POSITION	= Integer.parseInt(PropertyManager.getProperty("levmap.stadiumPosition"));

	/**
	 * @param size
	 * @param difficulty value from 1 - 10
	 * @param generator
	 * @author Dogan, Can
	 */
	public TyrantMapGenerator(int size, int difficulty, Random generator) {
		this.size = size;
		this.generator = generator;
		setDifficulty(difficulty);
	}

	/**
	 * fuehrt einen Generierungsbefehl durch mit den aktuellen Werten.
	 * 
	 * @author Dogan, Can
	 */
	public void generate(int topExits, int leftExits, int rightExits, int bottomExits) {

		initializeMapData();

		// place blocking area for arena
		fillRect(STADIUM_POSITION, STADIUM_POSITION, 10, 10, UIObjectType.STADIUM); // with
		// size
		// 100
		// it
		// would
		// be
		// placed
		// at

		// place first room
		tryPlacingRoom(size / 2, 0, Direction.DOWN);
		data[size / 2][0] = UIObjectType.WALL; // we close the opening in the
		// outer wall again

		for (int i = 0; i < 1000 * difficulty; i++) {
			int nextPassageTry = generator.nextInt(possibleEntryPoints.size());
			Coordinates passage = possibleEntryPoints.get(nextPassageTry);
			tryPlacingNextRandomFeature(passage.x, passage.y, getAdjacentRoomDirection(passage.x, passage.y));
		}

		// place taxis on the four sides. the top taxi count is -1 because
		placeTaxis(topExits, leftExits, rightExits, bottomExits);
		// generating rooms is done. now add items
		placeItemsInMap();
		// now place creeps
		placeCreepsInMap();

	}

	/**
	 * Generate mit difficulty als parameter
	 * 
	 * @param topExits
	 * @param leftExits
	 * @param rightExits
	 * @param bottomExits
	 * @param difficulty
	 * @author Brokmeier, Pascal
	 */
	public void generate(int topExits, int leftExits, int rightExits, int bottomExits, int difficulty) {
		setDifficulty(difficulty);
		this.generate(topExits, leftExits, rightExits, bottomExits);
	}

	/**
	 * Generiert speziell fuer den Airport
	 * 
	 * @param airportData
	 * @return eine LevelMap
	 * @author Brokmeier, Pascal
	 */
	public LevelMap generateAirportLevel(UIObjectType[][] airportData) {
		this.data = airportData;
		size = airportData.length;

		placeAirportTaxis();

		return getGeneratedLevelMap(LevelMapType.AIRPORT);
	}

	/**
	 * Platziert unten vier taxis links und vier taxis rechts vom "zentrum" also
	 * der Mitte der unteren seite der Map
	 */
	private void placeAirportTaxis() {
		int taxiColumn = data.length - 3;
		int taxiRowStart = data.length / 2;
		for (int i = 0; i < 4; i++) {
			taxiRowStart -= 2;
			placeTaxi(new Coordinates(taxiRowStart, taxiColumn), Direction.DOWN);

		}
		taxiRowStart = data.length / 2;
		for (int i = 0; i < 4; i++) {
			taxiRowStart += 2;
			placeTaxi(new Coordinates(taxiRowStart, taxiColumn), Direction.DOWN);
		}
	}

	/**
	 * Ersetzt den Pseudorandom Generator mit einem neuen basierend auf einem
	 * neuen seed, sodass Maps mit anderen (aber
	 * wiederholbaren) Werten generiert werden. Die Seeds werden gebraucht, um
	 * das selbe Level zu generieren, wenn man nach
	 * einem Levelwechsel wieder in das alte zurueck gehen will.
	 * 
	 * @param seed
	 * @author Dogan, Can
	 */
	public void updateGeneratorWithNewSeed(long seed) {
		generator = new Random(seed);
	}

	/**
	 * Gibt das zweidimensionale array zurueck, undzwar als LevelMap mit
	 * HashMaps
	 * 
	 * @return
	 * @author Dogan, Can
	 */
	public LevelMap getGeneratedLevelMap(LevelMapType type) {
		LevelMap levelMap = new LevelMap(type, LevelTreeGenerator.getNextLevelID());
		levelMap.max = new Coordinates(size * SCALEFACTOR, size * SCALEFACTOR);

		// setting start position for player (and making sure its empty as well)
		levelMap.start = new Coordinates(size / 2 * SCALEFACTOR, SCALEFACTOR);

		// setting stadium coordinates
		levelMap.stadium = new Coordinates(STADIUM_POSITION * SCALEFACTOR, STADIUM_POSITION * SCALEFACTOR);

		for (int y = 0; y < size; y++) {
			for (int x = 0; x < size; x++) {
				switch (data[x][y]) {
				case WALL:
					levelMap.addFixedObject(new Wall(DatabaseConnection.getNextID(), x * SCALEFACTOR, y * SCALEFACTOR, generator.nextInt(MAX_WALL_INDEX) + 1));
					break;
				case ITEM:
					// wir erstellen uns random items, aber wollen keine
					// footballs, deswegen die do while schleife
					Item item;
					do {
						item = new Item(DatabaseConnection.getNextID(), x * SCALEFACTOR, y * SCALEFACTOR, ItemType.getRandomSpawningItem());
					} while (item.getItemType() == ItemType.FOOTBALL);
					levelMap.addFixedObject(item);
					break;
				case ENEMY:
					levelMap.addMovableObject(new Enemy(DatabaseConnection.getNextID(), x * SCALEFACTOR, y * SCALEFACTOR, EnemyType.getRandomEnemy(type), difficulty));
					break;
				default:
					break;
				}
			}
		}
		setTaxisOnLevelMap(levelMap);

		// fog of war von deer Datenbank, falls verfuegbar.
		levelMap.setVisiblePositions(DatabaseConnection.getFogOfWarSetForLevelID(levelMap.getLevelID()));

		return levelMap;
	}

	/**
	 * Diese Klasse setzt die Taxis in den Level (ausser dem Airport)
	 * und schaltet die Taxis frei, die nach links und rechts fuehren (und
	 * natuerlich zurueck nach oben)
	 * 
	 * @param levelMap
	 * @author Dogan, Can
	 */
	private void setTaxisOnLevelMap(LevelMap levelMap) {
		// setting taxis seperately, since we need special knowledge (direction)
		for (Map.Entry<Coordinates, Direction> entry : taxiPositions.entrySet()) {
			Taxi taxi = new Taxi(DatabaseConnection.getNextID(), entry.getKey().x * SCALEFACTOR, entry.getKey().y * SCALEFACTOR);
			taxi.setDirection(entry.getValue());

			// schaltet alle Taxis frei, ausser die, die nach "unten" fuehren
			switch (taxi.getDirection()) {
			case DOWN:
				if (levelMap.getLevelMapType() != LevelMapType.AIRPORT) {
					addTenFootballToLevelMap(levelMap);
				}
				break;
			default:
				taxi.setUnlocked(true);
				break;
			}
			levelMap.addMovableObject(taxi);

		}
	}

	/**
	 * Setzt einen Fussball, der Taxis freischaltet. Fussbaelle sind bei uns
	 * Schluessel und Taxis ersetzen Leitern
	 * 
	 * @param levelMap
	 * @author Dogan, Can
	 */

	private void addTenFootballToLevelMap(LevelMap levelMap) {
		for (int i = 0; i < 10; i++) {
			// fuer jedes Taxi noch einen football
			Coordinates co = getRandomFreePosition();
			data[co.x][co.y] = UIObjectType.ITEM;
			Item football = new Item(DatabaseConnection.getNextID(), co.x * SCALEFACTOR, co.y * SCALEFACTOR, ItemType.FOOTBALL);
			levelMap.addFixedObject(football);
		}
	}

	/**
	 * Gibt die rohe Map zurueck, also noch in ihrer zweidimensionalen
	 * Arraystruktur
	 * 
	 * @return
	 * @author Dogan, Can
	 */
	public UIObjectType[][] getRawMapData() {
		return data;
	}

	/**
	 * Setzt Taxis in allen vier Himmelsrichtungen. Nord und Sued (up/down)
	 * gehen ein Level weiter. Oestliche und westliche
	 * (left/right) Taxis ermoeglichen Wechsel innerhalb einer Ebene in andere
	 * (gleichwertige Bereiche)
	 * 
	 * @param topExits
	 * @param leftExits
	 * @param rightExits
	 * @param bottomExits
	 * @author Dogan, Can
	 */
	private void placeTaxis(int topExits, int leftExits, int rightExits, int bottomExits) {

		while (topExits > 0) {
			Coordinates exitPoint = getRandomTaxiPointBySide(Direction.UP);
			placeTaxi(exitPoint, Direction.UP);
			topExits--;
		}
		while (leftExits > 0) {
			Coordinates exitPoint = getRandomTaxiPointBySide(Direction.LEFT);
			placeTaxi(exitPoint, Direction.LEFT);
			leftExits--;
		}
		while (rightExits > 0) {
			Coordinates exitPoint = getRandomTaxiPointBySide(Direction.RIGHT);
			placeTaxi(exitPoint, Direction.RIGHT);
			rightExits--;
		}
		while (bottomExits > 0) {
			Coordinates exitPoint = getRandomTaxiPointBySide(Direction.DOWN);
			placeTaxi(exitPoint, Direction.DOWN);
			bottomExits--;
		}

	}

	private void placeTaxi(Coordinates exitPoint, Direction direction) {
		data[exitPoint.x][exitPoint.y] = UIObjectType.TAXI;
		taxiPositions.put(exitPoint, direction);
	}

	/**
	 * Diese Methode durchsucht die Map von "unten" und platziert in der
	 * untersten (erreichbaren) Zeile zufaellig ein
	 * ExitTaxi
	 * 
	 * @return
	 * @author Dogan, Can
	 */
	private Coordinates getRandomTaxiPointBySide(Direction direction) {
		ArrayList<Coordinates> possibleExitPoints = new ArrayList<>();
		boolean firstRoomRowFound = false; // wir iterieren immer vom rand aus
		// und suchen uns die erste Reihe,
		// welche nicht nur aus Stein
		// besteht um dort Taxi Platzhalter
		// zu platzieren

		switch (direction) {
		case DOWN:
			// Wir iterieren ueber die daten (von "unten") (x = spalte, y =
			// reihe)
			for (int y = size - 1; !firstRoomRowFound; y--) {
				for (int x = 0; x < data.length; x++) {
					addPossibleExitPointIfFloor(possibleExitPoints, y, x);
				}
				firstRoomRowFound = possibleRowFound(possibleExitPoints, firstRoomRowFound);

			}
			break;
		case UP:
			// Wir iterieren ueber die Daten (von "oben")
			for (int y = 0; !firstRoomRowFound; y++) {
				for (int x = 0; x < data.length; x++) {
					addPossibleExitPointIfFloor(possibleExitPoints, y, x);
				}
				firstRoomRowFound = possibleRowFound(possibleExitPoints, firstRoomRowFound);
			}
			break;
		case LEFT:
			// Wir iterieren ueber die Daten (von "links")
			for (int x = 0; !firstRoomRowFound; x++) {
				for (int y = 0; y < data.length; y++) {
					addPossibleExitPointIfFloor(possibleExitPoints, y, x);
				}
				firstRoomRowFound = possibleRowFound(possibleExitPoints, firstRoomRowFound);
			}
			break;
		case RIGHT:
			// Wir iterieren ueber die Daten (von "links")
			for (int x = size - 1; !firstRoomRowFound; x--) {
				for (int y = 0; y < data.length; y++) {
					addPossibleExitPointIfFloor(possibleExitPoints, y, x);
				}
				firstRoomRowFound = possibleRowFound(possibleExitPoints, firstRoomRowFound);
			}
			break;
		}
		return possibleExitPoints.get(generator.nextInt(possibleExitPoints.size()));
	}

	private boolean possibleRowFound(ArrayList<Coordinates> possibleExitPoints, boolean firstRoomRowFound) {
		// Wenn schon possibles existieren wurde wohl eine reihe gefunden in der
		// freier boden verfuegbar ist. Also kein weiteres mal nach "oben"
		// springen
		// und suchen
		if (possibleExitPoints.size() != 0) {
			firstRoomRowFound = true;
		}
		return firstRoomRowFound;
	}

	private void addPossibleExitPointIfFloor(ArrayList<Coordinates> possibleExitPoints, int y, int x) {
		// Wenn freie Flaeche gefunden wurde zu possibles hinzufuegen
		if (data[x][y] == UIObjectType.FLOOR) {
			possibleExitPoints.add(new Coordinates(x, y));
		}
	}

	// alter Code, wir haben nun effizienteren
	// possibleEntryPoints

	/*
	 * private Coordinates findPassagePointForNextRoom() throws
	 * MapIsFullException{
	 * 
	 * boolean entryPointFound = false;
	 * for(int i = 0; i<size*size;i++){
	 * int x = generator.nextInt(size);
	 * int y = generator.nextInt(size);
	 * if(getAdjacentRoomDirection(x, y) != null) {
	 * return new Coordinates(x,y);
	 * }
	 * }
	 * throw new MapIsFullException();
	 * 
	 * }
	 */

	/**
	 * Gibt mir eine zufaellige freie Position zurueck
	 * 
	 * @author Dogan, Can
	 */
	private Coordinates getRandomFreePosition() {
		setupEmptyPositionsArray();

		// so lange freie Felder vorhanden sind, wird die

		if (emptyPositions.size() != 0) {
			int index = generator.nextInt(emptyPositions.size());
			return emptyPositions.get(index);
		} else {
			// keine freien platze mehr im level vorhanden (sehr
			// unwahrscheinlich)
			return null;
		}
	}

	/**
	 * Loescht ein Coordinate Objekt aus der Liste.
	 * 
	 * @param coordinates
	 * @author Dogan, Can
	 */
	@SuppressWarnings("unused")
	private void removeFreePosition(Coordinates coordinates) {
		emptyPositions.remove(coordinates);
	}

	/**
	 * Sorgt dafuer, dass wir ein Array haben in dem alle freien Felder
	 * vorliegen.
	 * 
	 * @author Dogan, Can
	 */
	private void setupEmptyPositionsArray() {
		if (emptyPositions == null) {
			emptyPositions = new ArrayList<>();
			for (int y = 0; y < size; ++y) {
				for (int x = 0; x < size; ++x) {
					addPossibleExitPointIfFloor(emptyPositions, y, x);
				}
			}
		}
	}

	/**
	 * Plaziert Items auf der Map (bzw. setzt ObjectType der Coordinates auf
	 * Item) Die Items muessen nicht alle einzeln
	 * gesetzt werden, sondern nur der Typ Item, sodass dieser dafuer sorgt,
	 * dass unterschiedliche Items gesetzt werden.
	 * 
	 * @author Dogan, Can
	 */
	private void placeItemsInMap() {
		setupEmptyPositionsArray(); // gibt uns eine zahl mit der wir arbeiten
		// koennen
		// Setzen der Items (Lebensmittel, Buecher, Waffen)
		int itemCount = (int) (emptyPositions.size() * Double.parseDouble(PropertyManager.getProperty("levgen.itemFactor")));
		for (int i = 0; i < itemCount; ++i) {
			Coordinates coordinates = getRandomFreePosition();
			if (coordinates != null) {
				data[coordinates.x][coordinates.y] = UIObjectType.ITEM;
				emptyPositions.remove(coordinates);
			}
		}
	}

	/**
	 * Plaziert Gegner auf der Map (bzw. setzt ObjectType der Coordinates auf
	 * enemy) Analog zu den Items werden auch hier
	 * "nur" Monster gesetzt, die Monsterarten werden zufaellig gewaehlt.
	 * 
	 * @author Dogan, Can
	 */
	private void placeCreepsInMap() {
		for (int i = 0; i < (int) (size * size * Double.parseDouble(PropertyManager.getProperty("levgen.enemyFactor"))); ++i) {
			Coordinates coordinates = getRandomFreePosition();
			if (coordinates != null) {
				data[coordinates.x][coordinates.y] = UIObjectType.ENEMY;
				emptyPositions.remove(coordinates);
			}
		}
	}

	/**
	 * Eine Methode welche die Richtung des naechsten Raums berechnet (indem sie
	 * prueft welche Felder drum herum bereits
	 * Raeume sind)
	 * 
	 * @param x
	 * @param y
	 * @return
	 * @author Dogan, Can
	 */
	private Direction getAdjacentRoomDirection(int x, int y) {
		if (x - 1 > 0) {
			if (isFloor(x - 1, y))
				return Direction.RIGHT;
		}
		if (x + 1 < data.length) {
			if (isFloor(x + 1, y))
				return Direction.LEFT;
		}
		if (y - 1 > 0) {
			if (isFloor(x, y - 1))
				return Direction.DOWN;
		}
		if (y + 1 < data[x].length) {
			if (isFloor(x, y + 1))
				return Direction.UP;
		}
		return null;
	}

	private boolean isFloor(int x, int y) {
		return data[x][y] == UIObjectType.FLOOR;
	}

	protected void fillRect(int x, int y, int width, int height, UIObjectType type) {
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				data[i + x][j + y] = type;
			}
		}
	}

	protected void tryPlacingNextRandomFeature(int x, int y, Direction direction) {
		int hallwayOrRoom = generator.nextInt(10);

		if (hallwayOrRoom < 9) {
			tryPlacingRoom(x, y, direction);
		} else {
			tryPlacingHallway(x, y, direction);
		}

	}

	/**
	 * Versucht an definierter Stelle ein Feature (Raum oder Flur) zu setzen, in
	 * die angegebene Richtung.
	 * 
	 * @param x
	 * @param y
	 * @author Dogan, Can
	 */
	protected boolean tryPlacingRoom(int x, int y, Direction direction) {
		// max height/width for room
		int maxSize = (int) Math.floor(Float.parseFloat(PropertyManager.getProperty("levgen.roomsizeFactor")) * size);
		int minSize = Integer.parseInt(PropertyManager.getProperty("levgen.minRoomsizeAbsolute"));
		int roomWidth = minSize + generator.nextInt(maxSize - minSize);
		int roomHeight = minSize + generator.nextInt(maxSize - minSize);

		return tryPlacingFeature(x, y, direction, roomWidth, roomHeight);

	}

	/**
	 * Gleich wie tryPlacingRoom nur dass laengere Raeume in Form eines Flurs
	 * erzeugt werden
	 * 
	 * @param x
	 * @param y
	 * @param direction
	 * @return
	 * @author Dogan, Can
	 */

	protected boolean tryPlacingHallway(int x, int y, Direction direction) {
		// max height/width for room
		int maxSize = (int) Math.floor(Float.parseFloat(PropertyManager.getProperty("levgen.hallwaysizeFactor")) * size);
		int minSize = Integer.parseInt(PropertyManager.getProperty("levgen.minHallwaysizeAbsolute"));
		int roomWidth = Integer.parseInt(PropertyManager.getProperty("levgen.minHallwayWidthAbsolute"));
		int roomHeight = minSize + generator.nextInt(maxSize - minSize);

		return tryPlacingFeature(x, y, direction, roomWidth, roomHeight);

	}

	protected boolean tryPlacingFeature(int x, int y, Direction direction, int roomWidth, int roomHeight) {
		int entryPoint;
		switch (direction) {
		case LEFT:
			entryPoint = 1 + generator.nextInt(roomHeight - 2);
			break;
		case RIGHT:
			entryPoint = 1 + generator.nextInt(roomHeight - 2);
			break;
		case UP:
			entryPoint = 1 + generator.nextInt(roomWidth - 2);
			break;
		case DOWN:
			entryPoint = 1 + generator.nextInt(roomWidth - 2);
			break;
		default:
			entryPoint = 1 + generator.nextInt(roomWidth - 2);
		}
		Coordinates suspensionPoint = getSuspensionPointForRoom(x, y, entryPoint, roomWidth, roomHeight, direction);
		if (suspensionPoint.x >= 0 && suspensionPoint.y >= 0) {
			if (hasSpace(suspensionPoint.x, suspensionPoint.y, roomWidth + 2, roomHeight + 2)) {
				// fill the desired area with floor tiles
				fillRect(suspensionPoint.x + 1, suspensionPoint.y + 1, roomWidth, roomHeight, UIObjectType.FLOOR);
				// fill the entry poing with floor tiles as well (so we have one
				// fully connected maze)
				data[x][y] = UIObjectType.FLOOR;

				makeRoomPassageWider(x, y);

				// add surrounding walls as new possible entry points for next
				// rooms
				addWallPointsToEntryPointSet(suspensionPoint.x, suspensionPoint.y, roomWidth, roomHeight);

				// room was placed
				return true;
			}
		}
		// room could not be placed
		return false;
	}

	/**
	 * Diese Methode sorgt dafuer dass die Durchgaenge immer min 3 einheiten
	 * breit sind. Damit kann man einfacher von raum zu raum wechseln, da man
	 * sonst ab und an an den Waenden fest gehangen ist.
	 * 
	 * @param x
	 * @param y
	 */

	private void makeRoomPassageWider(int x, int y) {

		// wenn keiner der umliegenden Orte aus dem array rauslaeuft (am rand
		// ist) machen wir um den eingang "platz" (sodass der durchgang weiter
		// ist)
		if (getType(x - 1, y) != null && getType(x + 1, y) != null && getType(x, y - 1) != null && getType(x, y + 1) != null) {
			data[x - 1][y] = UIObjectType.FLOOR;
			data[x + 1][y] = UIObjectType.FLOOR;
			data[x][y - 1] = UIObjectType.FLOOR;
			data[x][y + 1] = UIObjectType.FLOOR;
		}
	}

	/**
	 * Eine Methode welche alle Mauerpunkte um einen Raum herum extrahiert und
	 * diese in ein Set legt, welches wir nutzen um
	 * moegliche Uebergaenge zu erraten
	 * 
	 * @param x
	 * @param y
	 * @param roomWidth
	 * @param roomHeight
	 * @author Dogan, Can
	 */
	protected void addWallPointsToEntryPointSet(int x, int y, int roomWidth, int roomHeight) {
		for (int i = 0; i < roomWidth + 2; i++) {
			// die obere reihe an mauerbloecken
			addIfMissingRemoveIfPresent(new Coordinates(x + i, y));
			// die untere reihe (unter dem raum) an mauerbloecken
			addIfMissingRemoveIfPresent(new Coordinates(x + i, y + roomHeight + 1));
		}

		for (int i = 0; i < roomHeight + 2; i++) {
			// die linke reihe an mauerbloecken
			addIfMissingRemoveIfPresent(new Coordinates(x, y + i));
			// die rechte reihe (rechts vom raum) an mauerbloecken
			addIfMissingRemoveIfPresent(new Coordinates(x + roomWidth + 1, y + i));
		}
	}

	/**
	 * Methode fuer moegliche Einstiegspunkte
	 * 
	 * @param coordinates
	 * @author Dogan, Can
	 */
	protected void addIfMissingRemoveIfPresent(Coordinates coordinates) {
		if (possibleEntryPoints.contains(coordinates)) {
			possibleEntryPoints.remove(coordinates);
		} else {
			possibleEntryPoints.add(coordinates);
		}
	}

	/**
	 * Berechnet "Aufhaengepunkt" fuer Raum von gegebener Stelle mit
	 * uebergebenen Werten
	 * 
	 * @param entryX Der Einstiegspunkt in den Raum. Quasi der Tuerrahmen (also
	 *            noch in der angrenzenden Wand)
	 * @param entryY Der Einstiegspunkt in den Raum. Quasi der Tuerrahmen (also
	 *            noch in der angrenzenden Wand)
	 * @param entryPointInWall Wo ist der Einstiegspunkt innerhalb des Raums,
	 *            also an welchem Punkt an der Wand (ganz in
	 *            der Ecke oder in der Mitte?)
	 * @param roomWidth
	 * @param roomHeight
	 * @param directionOfEntry Von Wo kommen wir?
	 * @return Aufhaengepunkt des Raums. Also oben links in der Ecke des raums +
	 *         umgebende Wand (also nochmal entryX-1,
	 *         y-1)
	 * @author Dogan, Can
	 */
	protected Coordinates getSuspensionPointForRoom(int entryX, int entryY, int entryPointInWall, int roomWidth, int roomHeight, Direction directionOfEntry) {
		int returnX = entryX;
		int returnY = entryY;

		switch (directionOfEntry) {
		case UP:
			// holt uns den Punkt wenn wir von unten aus kommen
			returnX = entryX - (entryPointInWall);
			returnY = entryY - 1 - roomHeight;
			break;
		case DOWN:
			// holt uns den Punkt wenn wir von oben aus kommen
			returnX = entryX - entryPointInWall;
			break;
		case RIGHT:
			// holt uns den Punkt wenn wir von links aus kommen
			returnY = returnY - entryPointInWall;
			break;
		case LEFT:
			// holt uns den Punkt wenn wir von rechts aus kommen
			returnX = entryX - roomWidth;
			returnY = entryY - entryPointInWall;
			break;
		}
		return new Coordinates(returnX, returnY);
	}

	/**
	 * Methode welche prueft ob an gegebener Stelle Platz fuer einen Objekt mit
	 * gegebener Hoehe und Breite ist
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @return
	 * @author Dogan, Can
	 */
	protected boolean hasSpace(int x, int y, int width, int height) {

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (getType(x + i, y + j) == null || getType(x + i, y + j) != UIObjectType.WALL) {
					return false;
				}
			}
		}
		return true;
	}

	protected UIObjectType getType(int x, int y) {
		if (x >= 0 && x < data.length) {
			if (y >= 0 && y < data[x].length) {
				return data[x][y];
			}
		}
		return null;
	}

	/**
	 * fuellt unsere Map ausnahmslos mit Waenden
	 * 
	 * @author Dogan, Can
	 */
	protected void initializeMapData() {
		this.data = new UIObjectType[size][size];
		fillRect(0, 0, size, size, UIObjectType.WALL);
		possibleEntryPoints.clear();
		emptyPositions = null;
		taxiPositions.clear();

	}

	/**
	 * Setter fuer die Schwierigkeitsstufe
	 * 
	 * @param difficulty
	 * @author Dogan, Can
	 */
	protected void setDifficulty(int difficulty) {
		if (difficulty > 0 && difficulty <= 10) {
			this.difficulty = difficulty;
		}
	}

}
