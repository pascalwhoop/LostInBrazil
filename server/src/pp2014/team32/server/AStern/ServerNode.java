package pp2014.team32.server.AStern;

import java.util.ArrayList;

import pp2014.team32.shared.entities.LevelMap;
import pp2014.team32.shared.utils.Coordinates;
import pp2014.team32.shared.utils.PropertyManager;

/**
 * Diese Klasse dient der Berechnung eines kuerzesten Weges von einem
 * Startknoten zu einem Zielknoten. Diese Klasse speichert Koordinaten, Vor- und
 * Nachfolgerknoten, um somit von einem Startknoten hin zum Zielknoten hin zu
 * expanindieren. Desweitern bietet sie Methoden zum Distanzen zu berechnen und
 * die Kosten jeden Knotens zu berechnen. Diese Kosten betreffen sowohl die
 * bisher angefallen Kosten, um diesen Knoten zu erreichen, als auch die
 * approximierten Kosten, die noch entstehen um den Zielknoten zu erreichen.
 * 
 * @author Peter Kings
 * 
 */
public class ServerNode {
	private Coordinates				coordinates;
	private float					weight;
	private ServerNode				previousNode;
	private ArrayList<Coordinates>	successorNodesCoordinates	= new ArrayList<Coordinates>();
	private static int				fieldsize					= Integer.parseInt(PropertyManager.getProperty("server.fieldsize"));
	private int						maxX, maxY;
	private Coordinates				destinationCoordinates;

	// ---------------------------- Constructor ----------------------------
	/**
	 * Dieser Konstruktor setzt alle Werte, die uebergeben werden und darueber
	 * 
	 * @author Peter Kings
	 * @param coordinates Koordinaten des Knotens
	 * @param weight bisherige Gewicht
	 * @param previousNode Vorgaengerknoten
	 * @param lM LevelMap des Knotens
	 * @param destinationCoordinates Zielknoten, zu dem ein Weg gefunden werden
	 *            soll
	 */
	public ServerNode(Coordinates coordinates, float weight, ServerNode previousNode, LevelMap lM, Coordinates destinationCoordinates) {
		this.coordinates = coordinates;
		this.weight = weight;
		this.previousNode = previousNode;
		this.maxX = lM.max.x;
		this.maxY = lM.max.y;
		this.destinationCoordinates = destinationCoordinates;
		// nachfolge Knoten hinzufuegen
		this.calculateSuccessorNodes();
	}

	/**
	 * In dieser Methode berechnet der Knoten seine Nachfolgerknoten, also die
	 * Knoten, die direkt in seinem Umfeld liegen.
	 * 
	 * @author Peter Kings
	 */
	private void calculateSuccessorNodes() {
		// Knoten der direkten Umgebung erstellen (50er Felder)
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				// hierbei handelt es sich um die position des aktuellen Knotens
				if (!(i == 1 && j == 1)) {
					int xPos = this.getCoordinates().x - this.getCoordinates().x % fieldsize - fieldsize * (1 - i);
					int yPos = this.getCoordinates().y - this.getCoordinates().y % fieldsize - fieldsize * (1 - j);
					if (!(xPos < 0 || yPos < 0 || xPos > maxX || yPos > maxY)) {
						// Vorgaengerzeiger setzen und Gewicht merken
						Coordinates coos = new Coordinates(xPos, yPos);
						if (coos.equals(new Coordinates(20, 20)))
							// if (!this.isFixedObjectAtThisCoordinates(coos))
							successorNodesCoordinates.add(coos);
					}
				}
			}
		}
	}

	/**
	 * Hier wird die als Manhattendistance bekannte approximierte Entfernung von
	 * den uebergebenen Koordinaten zu dem Knoten selbst uebergeben.
	 * 
	 * @author Peter Kings
	 * @param coordinates Koordinaten, zu denne die Distanz uebergeben werden
	 *            soll
	 * @return Distanz zwischen den Knoten
	 */
	public float manhattenDistance(Coordinates coordinates) {
		// Betrag der Delta x und Delta y addiert
		return (Math.abs(this.getCoordinates().x - coordinates.x) + Math.abs(this.getCoordinates().y - coordinates.y));
	}

	/**
	 * Hier wird die als Manhattendistance bekannte approximierte Entfernung
	 * zwischen den uebergebenen Koordinaten berechnet.
	 * 
	 * @author Peter Kings
	 * @param startCoordinates Starkoordinaten
	 * @param destinationCoordinates Zielkoordinaten
	 * @return Distanz zwischen den Koordinaten
	 */
	public static float manhattenDistance(Coordinates startCoordinates, Coordinates destinationCoordinates) {
		return (Math.abs(startCoordinates.x - destinationCoordinates.x) + Math.abs(startCoordinates.y - destinationCoordinates.y));
	}

	/**
	 * Hier wird die euklidische Distanz zwischen den uebergebenen Koordinaten
	 * und dem eigenen Knoten berechnet.
	 * 
	 * @author Peter Kings
	 * @param coordinates Koordinaten, zu denne die Distanz uebergeben werden
	 * @return Distanz zwischen den Knoten
	 */
	public float euklidischeDistance(Coordinates coordinates) {
		return (float) Math.sqrt(((this.getCoordinates().x - coordinates.x) ^ 2 - (this.getCoordinates().y - coordinates.y) ^ 2));
	}

	// ---------------------------- Getter/Setter ----------------------------

	/**
	 * Berechnet die auf die Feldgroesse im Spiel gerechnete, obere linke Ecke
	 * als Koordinate.
	 * 
	 * @auhtor Peter Kings
	 * @return auf Feldgroesse bezogenen obere linke Ecke dieser Koordinate
	 */
	public Coordinates getUpperLeftCorner() {
		return ServerNode.getUpperLeftCorner(this.getCoordinates().x, this.getCoordinates().y);
	}

	/**
	 * Rechnet die uebergebenen x und x Positionen auf die Feldgroesse des
	 * Spielfeldes
	 * um. Es wird dann die obere linke Ecke einer "Kachel" zurueckgegeben.
	 * 
	 * @author Peter Kings
	 * @param xPos x Position
	 * @param yPos y Position
	 * @return obere linke Ecke
	 */
	public static Coordinates getUpperLeftCorner(int xPos, int yPos) {
		int xPosNew = xPos - xPos % fieldsize;
		int yPosNew = yPos - yPos % fieldsize;
		return new Coordinates(xPosNew, yPosNew);
	}

	/**
	 * Rechnet die uebergebene Koordainte auf die Feldgroesse des Spielfeldes
	 * um. Es wird dann die obere linke Ecke einer "Kachel" zurueckgegeben.
	 * 
	 * @author Peter Kings
	 * @param coos Koordinate
	 * @return Koordinate der oberen linken Ecke
	 */
	public static Coordinates getUpperLeftCorner(Coordinates coos) {
		int xPosNew = coos.x - coos.x % fieldsize;
		int yPosNew = coos.y - coos.y % fieldsize;
		return new Coordinates(xPosNew, yPosNew);
	}

	/**
	 * @author Peter Kings
	 * @return eigene Koordinaten
	 */
	public Coordinates getCoordinates() {
		return coordinates;
	}

	/**
	 * @author Peter Kings
	 * @return vorgaenger Knoten
	 */
	public ServerNode getPreviousNode() {
		return previousNode;
	}

	/**
	 * @author Peter Kings
	 * @param previousNode nachfolge Knoten
	 */
	public void setPreviousNode(ServerNode previousNode) {
		this.previousNode = previousNode;
	}

	/**
	 * @author Peter Kings
	 * @param coordinates neue Koordinaten dieses Knotens
	 */
	public void setCoordinates(Coordinates coordinates) {
		this.coordinates = coordinates;
	}

	/**
	 * Das Gewicht beschreibt die Kosten, die bisher tatsaechlich entstanden
	 * sind, um diesen Knoten zu erreichen.
	 * 
	 * @author Peter Kings
	 * @return Gewicht
	 */
	public float getWeight() {
		return weight;
	}

	/**
	 * Das Gewicht beschreibt die Kosten, die bisher tatsaechlich entstanden
	 * sind, um diesen Knoten zu erreichen.
	 * 
	 * @author Peter Kings
	 * @param weight Gewicht
	 */
	public void setWeight(float weight) {
		this.weight = weight;
	}

	/**
	 * @author Peter Kings
	 * @return ArrayList der Nachfolgerknoten dieses Knotens
	 */
	public ArrayList<Coordinates> getSuccessorNodesCoordinates() {
		return successorNodesCoordinates;
	}

	/**
	 * Diese Methode berechnet die approximierten Kosten die anfallen, um von
	 * diesem Knoten zum Zielknoten zu gelanden. Sie werden hier mittels der
	 * Manhatten Distanz berechnet.
	 * 
	 * @author Peter Kings
	 * @return approximierte Distanz/Kosten von diesem Knoten aus bis zum Ziel
	 */
	public float getEstimatedWeight() {
		return (this.manhattenDistance(destinationCoordinates) / fieldsize);
	}

	/**
	 * Diese Methode gibt die gesamten Kosten dieses Knotens zurueck. Also die,
	 * die bisher schon angefallen sind addiert mit denen, die noch
	 * (approximiert) anfallen werden, um den Zielknoten zu erreichen.
	 * 
	 * @author Peter Kings
	 * @return gesamte Kosten: bisher angefallene + noch anfallende Kosten
	 *         approximiert
	 */
	public float getOverallWeight() {
		return (this.getEstimatedWeight() + this.getWeight());
	}
}
