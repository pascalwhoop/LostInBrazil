package pp2014.team32.server.AStern;

import java.util.Comparator;

/**
 * Diese Klasse dient dem Vergleichen von ServerNodes. Diese werden nach Ihrem
 * gesamten Gewicht verglichen. Dies betrifft die Summe aus den bisher
 * angefallenen Kosten und den approximierten noch anstehenden Kosten. Das
 * kleinere Gesamtgewicht wird bevorzugt.
 * 
 * @author Peter Kings
 */
public class NodeWeightComparator implements Comparator<ServerNode> {

	/**
	 * Diese Methode vergleicht zwei Knoten und praeferiert denjenigen, der
	 * weniger Gesamtkosten hat. (bisher angefallene + approximierte noch
	 * anstehende Kosten)
	 * 
	 * @author Peter Kings
	 */
	public int compare(ServerNode o1, ServerNode o2) {
		// wenn das gesamtgewicht von 1 kleiner ist als das von 2
		if (o1.getOverallWeight() < o2.getOverallWeight())
			// ist dieses zu bevorzugen
			return -1;
		else if (o1.getOverallWeight() > o2.getOverallWeight())
			// sonst ist 2 zu bevorzugen
			return 1;
		// in diesem Falle ist keins besser als das andere
		return 0;
	}
}
