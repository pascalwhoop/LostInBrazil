package pp2014.team32.server.AStern;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.logging.Logger;

import pp2014.team32.server.ServerMain;
import pp2014.team32.server.creatureManagement.RangeAndCollisionCalculator;
import pp2014.team32.shared.entities.Creature;
import pp2014.team32.shared.entities.LevelMap;
import pp2014.team32.shared.utils.Coordinates;
import pp2014.team32.shared.utils.PropertyManager;

/**
 * Diese Klasse dient der Berechnung eines gueltigen kuerzesten Weges einer
 * Kreatur von dessen aktuellem Standpunktes zu einer Koordinate. Dies wird
 * mittels des AStern Algorithmus realisert.
 * 
 * @author Peter Kings
 */

public class ASternAlgorithm {
	private final static Logger						LOGGER		= Logger.getLogger(ServerMain.class.getName());
	// Hashmap mit allen fuer die Berechnung relevanten Knoten
	private final HashMap<Coordinates, ServerNode>	serverNodes	= new HashMap<Coordinates, ServerNode>();
	private static int								fieldsize	= Integer.parseInt(PropertyManager.getProperty("server.fieldsize"));

	// Comparator zum Vergleich von Knoten
	final private Comparator<ServerNode>			comparator	= new NodeWeightComparator();
	// Liste mit Knoten, zu denen der kuerzeste Weg noch berechnet werden muss
	final private PriorityQueue<ServerNode>			openList	= new PriorityQueue<ServerNode>(1000, comparator);
	// Menge mit Knoten, zu denen der kuerzeste Weg bereits berechnet ist
	final private Set<ServerNode>					closedList	= new HashSet<ServerNode>();
	private ArrayList<Coordinates>					path;
	final private LevelMap							levelMap;
	// Start und Zielkoordinaten auf feldgroesse approximieren
	private Coordinates								destinationCoordinatesApprox;
	private Coordinates								startCoordinatesApprox;
	private Creature								creatureToMove;

	/**
	 * Konstruktor zum berechnen einen kuerzesten Weges. Es wird lediglich die
	 * LevelMap uebergeben.
	 * 
	 * @author Peter Kings
	 * @param lM LevelMap, auf der ein kuerzester Weg berechnet werden soll.
	 */
	public ASternAlgorithm(LevelMap lM) {
		this.levelMap = lM;
	}

	/**
	 * Diese Methode sucht einen kuerzesten Weg von einem Startknoten zu einem
	 * Zielknoten, den eine Kreatur kann.
	 * 
	 * @author Peter Kings
	 * @param startCoordinates Startkoordinate
	 * @param destinationCoordinates Zielkoordinaten
	 * @param creature zu bewegende Kreatur
	 * @return ArrayList mit den Koordinaten des kuerzesten Weges
	 * @throws NoShortestPathFoundException es wurde kein kuerzester Weg
	 *             gefunden.
	 */
	public ArrayList<Coordinates> calculateShortestPathForCreatureToCoordinates(Coordinates startCoordinates, Coordinates destinationCoordinates, Creature creature)
			throws NoShortestPathFoundException {
		/*
		 * Trick:
		 * -der Algorithmus gibt spaeter vom ZielKnoten ausgehend alle
		 * vorherigen Knotenpunkte aus
		 * -das heisst, das der Weg vom ZIEL zum START zurueckgegeben wird
		 * -um nun den richtigen Weg zu erhalten, muss lediglich Start und Ziel
		 * am Anfang vertauscht werden!
		 */
		this.startCoordinatesApprox = ServerNode.getUpperLeftCorner(destinationCoordinates);
		this.destinationCoordinatesApprox = ServerNode.getUpperLeftCorner(startCoordinates);
		// zu bewegende Kreatur
		this.creatureToMove = creature;
		// Bewegungspfad - der kuerzeste Weg
		path = new ArrayList<Coordinates>();
		// Startknoten wird initialisiert
		ServerNode startNode = new ServerNode(startCoordinatesApprox, 0, null, levelMap, destinationCoordinatesApprox);
		// hier faengt alles an: Startknoten den Knoten hinzufuegen
		serverNodes.put(startNode.getCoordinates(), startNode);
		// Startknoten der zu untersuchenden Queue aus Knoten hinzufuegen
		openList.add(startNode);
		/*
		 * diese Schleife wird durchlaufen bis entweder
		 * - die optimale Loesung gefunden wurde oder
		 * - feststeht, dass keine Loesung existiert
		 */
		// solange die zu untersuchende Queue nicht leer ist...
		while (!openList.isEmpty()) {
			// Knoten mit dem geringsten f Wert aus der Open List entfernen
			ServerNode currentNode = openList.poll();
			// Wurde das Ziel gefunden?
			if (currentNode.getUpperLeftCorner().equals(destinationCoordinatesApprox)) {
				// es wurde gefunden
				LOGGER.fine("AStern hat das Ziel gefunden");
				ServerNode c = currentNode;
				// solange man nicht wieder beim Startknoten angelangt ist
				while (c != startNode) {
					// Koordinaten des "Zwischenknotens" hinzufuegen
					path.add(c.getCoordinates());
					// und zum Vorgaenger wechseln
					c = c.getPreviousNode();
				}
				// hier wird nun der kuerzeste Weg zurueckgegeben
				return path;
			}
			// Der aktuelle Knoten soll durch nachfolgende Funktionen
			// nicht weiter untersucht werden damit keine Zyklen entstehen
			closedList.add(currentNode);
			// Wenn das Ziel noch nicht gefunden wurde: Nachfolgeknoten
			// des aktuellen Knotens auf die Open List setzen
			expandNode(currentNode);
		}
		// die Open List ist leer, es existiert kein Pfad zum Ziel
		if (path.isEmpty())
			throw new NoShortestPathFoundException();
		return null;
	}

	/**
	 * Berechnet zunaechst alle gueltigen Nachfolgerknoten des zu expandierenden
	 * aktuellen Knotens.
	 * Ueberprueft daraufhin alle Nachfolgeknoten und fuegt sie der Open List
	 * hinzu, wenn entweder
	 * - der Nachfolgeknoten zum ersten Mal gefunden wird oder
	 * - ein besserer Weg zu diesem Knoten gefunden wird
	 * 
	 * @author Peter Kings
	 * @param currentNode aktueller Knoten
	 */
	private void expandNode(ServerNode currentNode) {
		// Koordinaten der Nachfolgeknoten des aktuellen Knotens berechnen
		ArrayList<Coordinates> successorCoordinates = this.calculateSuccessorCoordinatesForCoordinates(currentNode.getCoordinates());
		// ArrayList fuer die Nachfolgeknoten instanziieren
		ArrayList<ServerNode> successorServerNodes = new ArrayList<ServerNode>();
		// fuer alle Koordinaten der Nachfolgeknoten
		for (Coordinates c : successorCoordinates) {
			// wenn sie noch nicht betrachtet wurden
			if (!serverNodes.containsKey(c))
				/*
				 * Knoten neu instanziieren und den relevanten Knoten
				 * hinzufuegen. Das Gewicht ist 1 mehr.
				 */
				serverNodes.put(c, new ServerNode(c, (currentNode.getWeight() + 1), currentNode, levelMap, destinationCoordinatesApprox));
			// wenn an diesen Koordinaten keine Kollision auftreten wuerde
			if (!RangeAndCollisionCalculator.checkForCollisionWithNoItemFixedObject(c.x, c.y, creatureToMove.getWidth(), creatureToMove.getHeight(), levelMap)) {
				// zugehoerigen Knoten holen
				ServerNode sN = serverNodes.get(c);
				// und der Nachfolgerknoten ArrayList hinzufuegen
				successorServerNodes.add(sN);
			}
		}
		/*
		 * bis hierhin wurden jetzt alle gueltigen Nachfolgerknoten zu den
		 * successorServerNods hinzugefuegt
		 */
		for (ServerNode successor : successorServerNodes) {
			/*
			 * wenn der Nachfolgeknoten bereits auf der Closed List ist - gehe
			 * zur naechsten Iteration der Schleife.
			 */
			if (closedList.contains(successor))
				continue;
			/*
			 * g Wert fuer den neuen Weg berechnen: g Wert des Vorgaengers plus
			 * die Kosten der gerade benutzten Kante
			 */
			float tentative_g = currentNode.getWeight() + 1;
			/*
			 * wenn der Nachfolgeknoten bereits auf der Open List ist,
			 * aber der neue Weg nicht besser ist als der alte - gehe zur
			 * naechsten Iteration
			 */
			if (openList.contains(successor) && tentative_g >= successor.getWeight())
				continue;
			/*
			 * Knoten aktuelliserein in der Openlist durch:
			 * -herausnehmen
			 * -Gewicht aktualliserien
			 * -Knoten erneut hinzufuegen
			 */
			openList.remove(successor);
			successor.setWeight(tentative_g);
			openList.add(successor);
		}
	}

	/**
	 * Diese Methode berechnet alle Koordinaten der Nachbarknoten eines Knotens
	 * und gibt diese
	 * zurueck.
	 * 
	 * @author Peter Kings
	 * @param c Koordinaten, fuer den die Nachbarknoten ermittelt werden sollen.
	 * @return ArrayList mit Nachbarknoten
	 */
	private ArrayList<Coordinates> calculateSuccessorCoordinatesForCoordinates(Coordinates c) {
		// Knoten der direkten Umgebung erstellen (50er Felder)
		ArrayList<Coordinates> returnCoos = new ArrayList<Coordinates>();
		/*
		 * Hier wird jeweils fuer alle 8 Nachbarknoten geprueft, ob diese noch
		 * im Spielfeld liegen. Ist dies der Fall, werden deren Koordinaten der
		 * Rueckgabe
		 * ArraList hinzugefuegt.
		 */
		Coordinates coords = new Coordinates(c.x - fieldsize, c.y - fieldsize);
		if (!(coords.x < 0 || coords.y < 0 || coords.x > levelMap.max.x || coords.y > levelMap.max.y))
			returnCoos.add(coords);

		coords = new Coordinates(c.x, c.y - fieldsize);
		if (!(coords.x < 0 || coords.y < 0 || coords.x > levelMap.max.x || coords.y > levelMap.max.y))
			returnCoos.add(coords);

		coords = new Coordinates(c.x + fieldsize, c.y - fieldsize);
		if (!(coords.x < 0 || coords.y < 0 || coords.x > levelMap.max.x || coords.y > levelMap.max.y))
			returnCoos.add(coords);

		coords = new Coordinates(c.x - fieldsize, c.y);
		if (!(coords.x < 0 || coords.y < 0 || coords.x > levelMap.max.x || coords.y > levelMap.max.y))
			returnCoos.add(coords);

		coords = new Coordinates(c.x + fieldsize, c.y);
		if (!(coords.x < 0 || coords.y < 0 || coords.x > levelMap.max.x || coords.y > levelMap.max.y))
			returnCoos.add(coords);

		coords = new Coordinates(c.x - fieldsize, c.y + fieldsize);
		if (!(coords.x < 0 || coords.y < 0 || coords.x > levelMap.max.x || coords.y > levelMap.max.y))
			returnCoos.add(coords);

		coords = new Coordinates(c.x, c.y + fieldsize);
		if (!(coords.x < 0 || coords.y < 0 || coords.x > levelMap.max.x || coords.y > levelMap.max.y))
			returnCoos.add(coords);

		coords = new Coordinates(c.x + fieldsize, c.y + fieldsize);
		if (!(coords.x < 0 || coords.y < 0 || coords.x > levelMap.max.x || coords.y > levelMap.max.y))
			returnCoos.add(coords);
		return returnCoos;
	}
}
