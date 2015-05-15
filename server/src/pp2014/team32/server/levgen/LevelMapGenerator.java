package pp2014.team32.server.levgen;

import pp2014.team32.server.Database.DatabaseConnection;
import pp2014.team32.shared.entities.Item;
import pp2014.team32.shared.entities.Enemy;
import pp2014.team32.shared.entities.LevelMap;
import pp2014.team32.shared.entities.Wall;
import pp2014.team32.shared.enums.ItemType;
import pp2014.team32.shared.enums.EnemyType;
import pp2014.team32.shared.enums.LevelMapType;
import pp2014.team32.shared.enums.UIObjectType;
import pp2014.team32.shared.utils.Coordinates;
import pp2014.team32.shared.utils.PropertyManager;

import java.util.Random;

/**
 * 
 * @author Dogan, Can
 * 
 */

@Deprecated
public class LevelMapGenerator {
	private final double		changeDirectionProbability	= 0.2;
	private UIObjectType[][]	data;
	private int					width;
	private int					height;
	private int					difficulty;
	private long				seed						= System.currentTimeMillis();
	private final static int	SCALEFACTOR					= Integer.parseInt(PropertyManager.getProperty("levgen.scaleFactor"));
	private Random				generator					= new Random(seed);
	private static final int	MAX_WALL_INDEX				= Integer.parseInt(PropertyManager.getProperty("wall.maxWallIndex"));

	public LevelMapGenerator(int width, int height, int difficulty, long generatorSeed) {
		this.width = width;
		this.height = height;
		this.difficulty = difficulty;

		// Seeds koennen sich im Gegensatz zu Math.random ihre zufaellig
		// ausgewaehlten Zahlen merken.
		generator = new Random(generatorSeed);
		seed = generatorSeed;

		// setze WALLs auf ausnahmslos gesamtes Feld
		this.data = new UIObjectType[width][height];
		for (int y = 0; y < height; ++y) {
			for (int x = 0; x < width; ++x) {
				data[x][y] = UIObjectType.WALL;
			}
		}
		// Schwierigkeit (difficulty) hat Auswirkungen auf die Anzahl der
		// Monster
		for (int d = 0; d < difficulty; d++) {
			// alte Klasse, daher nicht weiter bearbeitet.
		}

		/**
		 * Weg (im Sinne von Gehweg) mit folgenden
		 * Eigenschaften:(Starkoordinaten x&y,Anzahl Felderdes
		 * Weges/Floors/Paths)
		 * 
		 * @author Can
		 */
		int length = (int) (height * width * Double.parseDouble(PropertyManager.getProperty("levgen.pathAmountFactor")));
		createPath(length);

		/**
		 * Setzt zufaellig Items.
		 * Die Items, die tatsaechlich gesetzt werden, werden zufaellig aus
		 * allen Items gewaehlt.
		 * 
		 * @author Dogan, Can
		 */
		for (int i = 0; i < (int) (height * width * Double.parseDouble(PropertyManager.getProperty("levgen.itemFactor"))); ++i) {
			Coordinates position = getRandomFreePosition();
			if (position != null) {
				data[position.x][position.y] = UIObjectType.ITEM;
			}
		}
		/**
		 * Setzt zufaellig Monster (analog zur Item-Setzung)
		 * Die Monster, die tatsaechlich gesetzt werden, werden zufaellig aus
		 * allen Items gewaehlt.
		 * 
		 * @author Dogan, Can
		 */
		for (int i = 0; i < (int) (height * width * Double.parseDouble(PropertyManager.getProperty("levgen.enemyFactor"))); ++i) {
			Coordinates position = getRandomFreePosition();
			if (position != null) {
				data[position.x][position.y] = UIObjectType.ENEMY;
			}
		}

	}

	/**
	 * Diese Methode erzeugt den Weg, der von unsere Spielfigur 'begangen'
	 * werden kann. Wir zaehlen i so lange hoch, bis die gewuenschte length
	 * erreicht ist. Dabei wird per Math.random nach den unten stehenden
	 * Bedingungen der Weg durch die Mauern freigeschlagen, sowie bei Bedarf
	 * gedreht. Da man den Weg nicht wieder zurueck gehen will, stoesst man in
	 * den vier Ecken auf Probleme. Das wird zusaetzlich abgefangen, indem die
	 * Spielfigur wieder auf die Ausgangsposition, also die Mitte gesetzt wird.
	 * Dies hat den Vorteil, dass die Mitte wie ein Marktplatz ein wenig mehr
	 * Bedeutung bekommt und auch groesser wird. Das Spielfeld wird
	 * dementsprechend zu den Ecken hin kleiner/duenner. Am Ende wird, wenn
	 * alles stimmt und eine WALL auf dem Weg liegt, diese durch eine FLOOR
	 * (Stein durch Weg) ersetzt.
	 * 
	 * @param length
	 * @author Dogan, Can
	 */
	private void createPath(int length) {
		int x = width / 2;
		int y = height / 2;
		int dirX = 0, dirY = 1;
		int i = 0;

		while (i < length) {
			// zur zufaelligenRichtungsaenderung
			if (generator.nextDouble() < changeDirectionProbability || x + dirX >= width || x + dirX <= 0 || y + dirY >= height || y + dirY <= 0) {
				if (dirY == 0) {
					dirX = 0;
					dirY = (generator.nextDouble() < 0.5) ? -1 : 1;
				} else {
					dirY = 0;
					dirX = (generator.nextDouble() < 0.5) ? -1 : 1;
				}
			}
			// Wird ausgefuehrt, wenn der momentan erzeugte Weg in der Ecke ist.
			if (x + dirX >= width || x + dirX <= 0 || y + dirY >= height || y + dirY <= 0) {
				x = width / 2;
				y = height / 2;
			}
			x += dirX;
			y += dirY;
			if (data[x][y] == UIObjectType.WALL) {
				data[x][y] = UIObjectType.FLOOR;
				i++;
			}
		}

		// am ende noch am rand wieder mauern setzen
		for (int j = 0; j < width; j++) {
			data[j][0] = UIObjectType.WALL;
			data[j][data[j].length - 1] = UIObjectType.WALL;
			data[0][j] = UIObjectType.WALL;
			data[data.length - 1][j] = UIObjectType.WALL;
		}
	}

	/** Wirft eine Exception, falls die Map doch verlassen werden sollte
	 * 
	 * @param x
	 * @param y
	 * @return
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public UIObjectType getObjectAt(int x, int y) throws ArrayIndexOutOfBoundsException {
		if (x < 0 || x >= getWidth() || y < 0 || y >= getHeight())
			throw new ArrayIndexOutOfBoundsException("Index out of bounds.");

		return data[x][y];
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	/**Gibt die Map aus. Anfangs noch mit Hashtags fuer Mauern und "O"s fuer Items. Wege bleiben als "default" frei.
	 * 
	 * @author Can
	 */
	public void debugPrint() {
		System.out.println("debug");
		for (int y = 0; y < height; ++y) {
			for (int x = 0; x < width; ++x) {
				switch (data[x][y]) {
				case WALL:
					System.out.print("#");
					break;
				case ITEM:
					System.out.print("O");
					break;

				default:
					System.out.print(" ");
				}
			}
			System.out.print("\n");
		}
	}
	/** Die LevelMap setzt nun richtige Wege, Monster, etc., die aus der Datenbank aufgegriffen werden.
	 * 
	 * @return
	 * @author Dogan, Can
	 */
	public LevelMap getGeneratedLevelMap() {
		LevelMap levelMap = new LevelMap(LevelMapType.JUNGLE, DatabaseConnection.getNextID());
		levelMap.max = new Coordinates(width * SCALEFACTOR, height * SCALEFACTOR);

		// In der Mitte ist definitiv Weg, da der Startpunkt die Mitte ist.
		levelMap.max = new Coordinates(width / 2 * SCALEFACTOR, height / 2 * SCALEFACTOR);
		if (data[width / 2][height / 2] == UIObjectType.WALL) {
			data[width / 2][height / 2] = UIObjectType.FLOOR;
		}

		for (int y = 0; y < height; ++y) {
			for (int x = 0; x < width; ++x) {
				switch (data[x][y]) {
				case WALL:
					levelMap.addFixedObject(new Wall(DatabaseConnection.getNextID(), x * SCALEFACTOR, y * SCALEFACTOR, generator.nextInt(MAX_WALL_INDEX) + 1));
					break;
				case ITEM:
					levelMap.addFixedObject(new Item(DatabaseConnection.getNextID(), x * SCALEFACTOR, y * SCALEFACTOR, ItemType.getRandomSpawningItem()));
					break;
				case ENEMY:
					levelMap.addMovableObject(new Enemy(DatabaseConnection.getNextID(), x * SCALEFACTOR, y * SCALEFACTOR, EnemyType.getRandomEnemy(), difficulty));
					break;
				default:
				}
			}
		}

		return levelMap;
	}

	/** Die freien Felder/Wege werden durch doppelten Schleifendurchlauf ermittelt.
	 *  Die ermittelten freien Felder werden dazu genutzt, um Objekte darauf zu platzieren.
	 *  Dadurch, dass die Anzahl der gesetzten Objekte im Verhaeltnis zu den freien Feldern steht,
	 *  koennen nicht mehr Objekte gesetzt werden, als Felder bestehen.
	 * 
	 * @return
	 * @author Dogan, Can
	 */
	public Coordinates getRandomFreePosition() {
		int numberOfFreePositions = 0;
		for (int y = 0; y < height; ++y) {
			for (int x = 0; x < width; ++x) {
				if (data[x][y] == UIObjectType.FLOOR)
					numberOfFreePositions++;
			}
		}

		if (numberOfFreePositions > 0) {
			int positionIndex = generator.nextInt(numberOfFreePositions);

			for (int y = 0; y < height; ++y) {
				for (int x = 0; x < width; ++x) {
					if (data[x][y] == UIObjectType.FLOOR) {
						if (positionIndex == 0)
							return new Coordinates(x, y);
						positionIndex--;
					}
				}
			}
		}

		return null;
	}

	public UIObjectType[][] getData() {
		return data;
	}

	public void setData(UIObjectType[][] data) {
		this.data = data;
	}

	public long getSeed() {
		return seed;
	}
}

/*
 * SO SIEHT DIE TESTKLASSE AUS! package test;
 * 
 * import java.util.Arrays;
 * 
 * import pp2014.team32.server.levgen.*; import
 * pp2014.team32.shared.utils.PropertyManager; /**
 * 
 * @author Can
 * 
 * 
 * public class LevGenTest { private final static String[] PROPERTY_FILES =
 * {"../Server/prefs/settings.properties",
 * "../Shared/prefs/settings.properties"};
 * 
 * public static void main(String[] args) {
 * 
 * new PropertyManager(Arrays.asList(PROPERTY_FILES));
 * 
 * LevelMap map = new LevelMap(50, 40,0); map.debugPrint();
 * 
 * }
 * 
 * }
 */
