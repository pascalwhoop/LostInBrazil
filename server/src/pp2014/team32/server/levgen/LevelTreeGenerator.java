package pp2014.team32.server.levgen;

import pp2014.team32.server.Database.DatabaseConnection;
import pp2014.team32.shared.entities.LevelMap;
import pp2014.team32.shared.entities.Taxi;
import pp2014.team32.shared.enums.*;
import pp2014.team32.shared.utils.Coordinates;
import pp2014.team32.shared.utils.PropertyManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Da sich unser Spiel mit der Fussball Weltmeisterschaft 2014 befasst, wollen
 * wir den Spielplan mit einbringen.
 * Die Levelstruktur ist nach dem Spielplan aufgebaut, sprich in Gruppenphasen
 * und vom Achtel-, Viertel-, Halbfinale bis zum Finale.
 * Da zum Zeitpunkt der Abgabe (30.06.2014) die WM noch laeuft, sind die bisher
 * nicht festgelegten Spiele "UNDEF", also undefiniert.
 * alle anderen haben die offiziellen Laenderkuerzel.
 *
 * @author Can Dogan
 */
public class LevelTreeGenerator {
    private static final int LEVEL_SIZE = 100;
    private static long seed = DatabaseConnection.getLevelTreeMapSeed();
    private static Random generator;
    private static int levelIDCounter = 0;
    public static ArrayList<UIObjectType[][]> rawData = new ArrayList<>();

    static {
        if (seed == -1) {
            seed = System.currentTimeMillis();
            DatabaseConnection.saveLevelTreeSeed(seed);
        }
        generator = new Random(seed);
    }



    /**
	 * Erzeugt eine LevelMap mit den gegebenen Parametern.
	 * Setzt im aktuellen Level nur die Referenzen nach rechts und unten
	 * Setzt fuer die linken Vorgaenger die rechte Referenz auf das aktuelle
	 * Level
	 * Setzt fuer die oberen Vorgaenger die untere Referenz auf das aktuelle
	 * Level
	 * So ist die Konsistenz der Verknuepfungen immer sichergestellt.
	 * -> Methoden mit minimalen Aenderungen, so ist der Vorgaenger fuer die 1. Gruppenphase irrelevant 
	 * 
	 * 
	 * @param difficulty - Schwierigkeit
	 * @param type - Jungle oder Favela
	 * @param isNightLevel - Nachtlevel ja/nein
	 * @param predecessors - Vorgaenger
	 * @param left - linker Nachbar
	 * @param team1 - Mannschaft 1
	 * @param team2 - Mannschaft 2
	 * @param levelDescription - Beschreibung (Gruppenphase, n-tel-Finale
	 * @param city - Austragungsort
	 * @param topLevelCount	- Anzahl obere Nachbarn
	 * @param leftLevelCount - Anzahl untere Nachbarn
	 * @param rightLevelCount - Anzahl rechte Nachbarn
	 * @param bottomLevelCount - Anzahl untere Nachbarn
	 * @return
	 * 
	 * @author Dogan, Can
	 */
    private static LevelMap createLevel(int difficulty, LevelMapType type, boolean isNightLevel, ArrayList<LevelMap> predecessors, LevelMap left, TeamType team1, TeamType team2, String levelDescription, CityType city, int topLevelCount, int leftLevelCount, int rightLevelCount, int bottomLevelCount) {
        LevelMap levelMap = createLevel(difficulty, type, isNightLevel, left, team1, team2, levelDescription, city, topLevelCount, leftLevelCount, rightLevelCount, bottomLevelCount);
        levelMap.setPreviousStageLevelMap(predecessors);
        for (LevelMap lm : predecessors){
            lm.addSuccessor(levelMap);

        }

        return levelMap;
    }

    private static LevelMap createLevel(int difficulty, LevelMapType type, boolean isNightLevel, LevelMap predecessor, LevelMap left, TeamType team1, TeamType team2, String levelDescription, CityType city, int topLevelCount, int leftLevelCount, int rightLevelCount, int bottomLevelCount) {
        ArrayList<LevelMap> predecessors = new ArrayList<>();
        predecessors.add(predecessor);
        return createLevel(difficulty, type, isNightLevel, predecessors, left, team1, team2, levelDescription, city, topLevelCount, leftLevelCount, rightLevelCount, bottomLevelCount);
    }

    private static LevelMap createLevel(int difficulty, LevelMapType type, boolean isNightLevel, LevelMap left, TeamType team1, TeamType team2, String levelDescription, CityType city, int topLevelCount, int leftLevelCount, int rightLevelCount, int bottomLevelCount) {
        TyrantMapGenerator tyrantMapGenerator = new TyrantMapGenerator(LEVEL_SIZE, difficulty, generator);
        tyrantMapGenerator.generate(topLevelCount, leftLevelCount, rightLevelCount, bottomLevelCount, difficulty);

        //wir holen uns die levelMap
        LevelMap levelMap = tyrantMapGenerator.getGeneratedLevelMap(type);
        //Nachtlevel?

        levelMap.setGame(team1, team2, levelDescription, city);
        //linken von LevelMaps untereinander und setzen der Taxis
        levelMap.setNightLevel(isNightLevel);
        levelMap.setLeftLevelMap(left);
        if (left != null) {
            left.setRightLevelMap(levelMap);
        }


        rawData.add(tyrantMapGenerator.getRawMapData());
        return levelMap;
    }

    /**
     * gets next id and counts up one
     * @return
     */
    protected static int getNextLevelID() {
        return levelIDCounter++;
    }




	/**
	 * Hier werden die Gegner eingetragen, inkl. Austragungsort.
	 * In unserem 1. Meilenstein haben wir die LevelMap-Struktur gezeigt und
	 * hier sind dazu die jeweiligen Fussballspiele eingetragen.
	 * Die LevelMap-Struktur ist der originale Spielplan.
	 * (Schwierigkeit/Jungle oder Favela/Nachtlevel/linker Nachbar/(Vorgaenger[ab
	 * 2.Gruppenphase])/Mannschaft A/Mannschaft B/Gruppenname/Austragungsort)
	 * Das Level wird immer noch automatisch generiert, jedoch ist diese Klasse noetig,
	 * um den Spielplan anzuzeigen, da dieser nicht automatisch generiert werden soll.
	 * 
	 * @return
	 * 
	 * @author Dogan, Can
	 */
    public static ArrayList<LevelMap> generateLevelTree() {
        ArrayList<LevelMap> levelList = new ArrayList<LevelMap>();

        //Airport (manuell)
        int airportSize = Integer.parseInt(PropertyManager.getProperty("levgen.airportSize"));
        //erstellen einen neuen tyrantMapgenerator fuer den Airport
        TyrantMapGenerator tyrantGenForAirport = new TyrantMapGenerator(airportSize, 1, generator);
        //erstelle eine statische airport data struktur
        UIObjectType[][] airportData = AirportGenerator.generateAirportMap();
        //uebergebe diese daten dem tyrantGenerator, welcher diese injected und statt den default werten nutzt
        LevelMap airport = tyrantGenForAirport.generateAirportLevel(airportData);
        //bisschen schoene sache
        airport.setGame(TeamType.NONE, TeamType.NONE, "Airport", CityType.SAO_PAULO);
        airport.start = new Coordinates(airportSize/2 * 50, airportSize/2 * 50);
        airport.unlockAllTaxis();
        levelList.add(airport);


        // http://de.wikipedia.org/wiki/Fussball-Weltmeisterschaft_2014
        // Zum Zeitpunkt der Abgabe war es moeglich, den gesamten originalen Spielplan bis zum Finale zu uebertragen
        // 1. Runde der Spiele
        LevelMap gameA1 = createLevel(1, LevelMapType.FAVELAS, false, airport, null, TeamType.BRA, TeamType.CRO, "Gruppe A", CityType.SAO_PAULO, 1, 0, 1, 1);
        levelList.add(gameA1);
        LevelMap gameB1 = createLevel(1, LevelMapType.JUNGLE, false, airport, gameA1, TeamType.NED, TeamType.ESP, "Gruppe B", CityType.SALVADOR, 1, 1, 1, 1);
        levelList.add(gameB1);
        LevelMap gameC1 = createLevel(1, LevelMapType.FAVELAS, true, airport, gameB1, TeamType.COL, TeamType.GRE, "Gruppe C", CityType.BELO_HORIZONTE, 1, 1, 1, 1);
        levelList.add(gameC1);
        LevelMap gameD1 = createLevel(1, LevelMapType.JUNGLE, false, airport, gameC1, TeamType.URU, TeamType.CRC, "Gruppe D", CityType.FORTALEZA, 1, 1, 1, 1);
        levelList.add(gameD1);
        LevelMap gameE1 = createLevel(1, LevelMapType.JUNGLE, false, airport, gameD1, TeamType.SUI, TeamType.ECU, "Gruppe E", CityType.BRASILIA, 1, 1, 1, 1);
        levelList.add(gameE1);
        LevelMap gameF1 = createLevel(1, LevelMapType.FAVELAS, false, airport, gameE1, TeamType.ARG, TeamType.BIH, "Gruppe F", CityType.SAO_PAULO, 1, 1, 1, 1);
        levelList.add(gameF1);
        LevelMap gameG1 = createLevel(1, LevelMapType.JUNGLE, false, airport, gameF1, TeamType.GER, TeamType.POR, "Gruppe G", CityType.SALVADOR, 1, 1, 1, 1);
        levelList.add(gameG1);
        LevelMap gameH1 = createLevel(1, LevelMapType.FAVELAS, false, airport, gameG1, TeamType.BEL, TeamType.ALG, "Gruppe H", CityType.BELO_HORIZONTE, 1, 1, 0, 1);
        levelList.add(gameH1);
        // 2. Runde der Spiele
        LevelMap gameA2 = createLevel(2, LevelMapType.FAVELAS, false, gameA1, null, TeamType.MEX, TeamType.CMR, "Gruppe A", CityType.NATAL, 1, 0, 1, 1);
        levelList.add(gameA2);
        LevelMap gameB2 = createLevel(2, LevelMapType.JUNGLE, false, gameB1, gameA2, TeamType.CHI, TeamType.AUS, "Gruppe B", CityType.CUIABA, 1, 1, 1, 1);
        levelList.add(gameB2);
        LevelMap gameC2 = createLevel(2, LevelMapType.FAVELAS, false, gameC1, gameB2, TeamType.CIV, TeamType.JPN, "Gruppe C", CityType.BELO_HORIZONTE, 1, 1, 1, 1);
        levelList.add(gameC2);
        LevelMap gameD2 = createLevel(2, LevelMapType.JUNGLE, false, gameD1, gameC2, TeamType.ENG, TeamType.ITA, "Gruppe D", CityType.MANAUS, 1, 1, 1, 1);
        levelList.add(gameD2);
        LevelMap gameE2 = createLevel(2, LevelMapType.JUNGLE, false, gameE1, gameD2, TeamType.FRA, TeamType.HON, "Gruppe E", CityType.PORTO_ALEGRE, 1, 1, 1, 1);
        levelList.add(gameE2);
        LevelMap gameF2 = createLevel(2, LevelMapType.FAVELAS, true, gameF1, gameE2, TeamType.IRN, TeamType.NGA, "Gruppe F", CityType.CURITIBA, 1, 1, 1, 1);
        levelList.add(gameF2);
        LevelMap gameG2 = createLevel(2, LevelMapType.FAVELAS, false, gameG1, gameF2, TeamType.GHA, TeamType.USA, "Gruppe G", CityType.NATAL, 1, 1, 1, 1);
        levelList.add(gameG2);
        LevelMap gameH2 = createLevel(2, LevelMapType.FAVELAS, false, gameH1, gameG2, TeamType.RUS, TeamType.KOR, "Gruppe H", CityType.CUIABA, 1, 1, 0, 1);
        levelList.add(gameH2);
        // 3. Runde der Spiele
        LevelMap gameA3 = createLevel(3, LevelMapType.FAVELAS, true, gameA2, null, TeamType.BRA, TeamType.MEX, "Gruppe A", CityType.FORTALEZA, 1, 0, 1, 1);
        levelList.add(gameA3);
        LevelMap gameB3 = createLevel(3, LevelMapType.JUNGLE, true, gameB2, gameA3, TeamType.AUS, TeamType.NED, "Gruppe B", CityType.PORTO_ALEGRE, 1, 1, 1, 1);
        levelList.add(gameB3);
        LevelMap gameC3 = createLevel(3, LevelMapType.JUNGLE, false, gameC2, gameB3, TeamType.COL, TeamType.CIV, "Gruppe C", CityType.BRASILIA, 1, 1, 1, 1);
        levelList.add(gameC3);
        LevelMap gameD3 = createLevel(3, LevelMapType.FAVELAS, false, gameD2, gameC3, TeamType.URU, TeamType.ENG, "Gruppe D", CityType.SAO_PAULO, 1, 1, 1, 1);
        levelList.add(gameD3);
        LevelMap gameE3 = createLevel(3, LevelMapType.JUNGLE, true, gameE2, gameD3, TeamType.SUI, TeamType.FRA, "Gruppe E", CityType.SALVADOR, 1, 1, 1, 1);
        levelList.add(gameE3);
        LevelMap gameF3 = createLevel(3, LevelMapType.FAVELAS, false, gameF2, gameE3, TeamType.ARG, TeamType.IRN, "Gruppe F", CityType.BELO_HORIZONTE, 1, 1, 1, 1);
        levelList.add(gameF3);
        LevelMap gameG3 = createLevel(3, LevelMapType.FAVELAS, true, gameG2, gameF3, TeamType.GER, TeamType.GHA, "Gruppe G", CityType.FORTALEZA, 1, 1, 1, 1);
        levelList.add(gameG3);
        LevelMap gameH3 = createLevel(3, LevelMapType.FAVELAS, false, gameH2, gameG3, TeamType.BEL, TeamType.RUS, "Gruppe H", CityType.RIO_DE_JANEIRO, 1, 1, 0, 1);
        levelList.add(gameH3);
        // 4. Runde der Spiele, nur Nachtlevel
        LevelMap gameA4 = createLevel(3, LevelMapType.JUNGLE, true, gameA3, null, TeamType.CMR, TeamType.CRO, "Gruppe A", CityType.MANAUS, 1, 0, 1, 1);
        levelList.add(gameA4);
        LevelMap gameB4 = createLevel(3, LevelMapType.FAVELAS, true, gameB3, gameA4, TeamType.ESP, TeamType.CHI, "Gruppe B", CityType.RIO_DE_JANEIRO, 1, 1, 1, 1);
        levelList.add(gameB4);
        LevelMap gameC4 = createLevel(3, LevelMapType.JUNGLE, true, gameC3, gameB4, TeamType.JPN, TeamType.GRE, "Gruppe C", CityType.NATAL, 1, 1, 1, 1);
        levelList.add(gameC4);
        LevelMap gameD4 = createLevel(3, LevelMapType.JUNGLE, true, gameD3, gameC4, TeamType.ITA, TeamType.CRC, "Gruppe D", CityType.RECIFE, 1, 1, 1, 1);
        levelList.add(gameD4);
        LevelMap gameE4 = createLevel(3, LevelMapType.FAVELAS, true, gameE3, gameD4, TeamType.HON, TeamType.ECU, "Gruppe E", CityType.CURITIBA, 1, 1, 1, 1);
        levelList.add(gameE4);
        LevelMap gameF4 = createLevel(3, LevelMapType.JUNGLE, true, gameF3, gameE4, TeamType.NGA, TeamType.BIH, "Gruppe F", CityType.CUIABA, 1, 1, 1, 1);
        levelList.add(gameF4);
        LevelMap gameG4 = createLevel(3, LevelMapType.FAVELAS, true, gameG3, gameF4, TeamType.USA, TeamType.POR, "Gruppe G", CityType.MANAUS, 1, 1, 1, 1);
        levelList.add(gameG4);
        LevelMap gameH4 = createLevel(3, LevelMapType.JUNGLE, true, gameH3, gameG4, TeamType.KOR, TeamType.ALG, "Gruppe H", CityType.PORTO_ALEGRE, 1, 1, 0, 1);
        levelList.add(gameH4);
        // 5. Runde der Spiele
        LevelMap gameA5 = createLevel(4, LevelMapType.FAVELAS, false, gameA4, null, TeamType.CMR, TeamType.BRA, "Gruppe A", CityType.BRASILIA, 1, 0, 1, 1);
        levelList.add(gameA5);
        LevelMap gameB5 = createLevel(4, LevelMapType.JUNGLE, true, gameB4, gameA5, TeamType.AUS, TeamType.ESP, "Gruppe B", CityType.CURITIBA, 1, 1, 1, 1);
        levelList.add(gameB5);
        LevelMap gameC5 = createLevel(4, LevelMapType.FAVELAS, false, gameC4, gameB5, TeamType.JPN, TeamType.COL, "Gruppe C", CityType.CUIABA, 1, 1, 1, 1);
        levelList.add(gameC5);
        LevelMap gameD5 = createLevel(4, LevelMapType.FAVELAS, false, gameD4, gameC5, TeamType.ITA, TeamType.URU, "Gruppe D", CityType.NATAL, 1, 1, 1, 1);
        levelList.add(gameD5);
        LevelMap gameE5 = createLevel(4, LevelMapType.JUNGLE, false, gameE4, gameD5, TeamType.HON, TeamType.SUI, "Gruppe E", CityType.MANAUS, 1, 1, 1, 1);
        levelList.add(gameE5);
        LevelMap gameF5 = createLevel(4, LevelMapType.FAVELAS, true, gameF4, gameE5, TeamType.NGA, TeamType.ARG, "Gruppe F", CityType.PORTO_ALEGRE, 1, 1, 1, 1);
        levelList.add(gameF5);
        LevelMap gameG5 = createLevel(4, LevelMapType.JUNGLE, true, gameG4, gameF5, TeamType.USA, TeamType.GER, "Gruppe G", CityType.RECIFE, 1, 1, 1, 1);
        levelList.add(gameG5);
        LevelMap gameH5 = createLevel(4, LevelMapType.FAVELAS, false, gameH4, gameG5, TeamType.KOR, TeamType.BEL, "Gruppe H", CityType.SAO_PAULO, 1, 1, 0, 1);
        levelList.add(gameH5);
        // 6. Runde der Spiele
        LevelMap gameA6 = createLevel(5, LevelMapType.JUNGLE, false, gameA5, null, TeamType.CRO, TeamType.MEX, "Gruppe A", CityType.RECIFE, 1, 0, 1, 2);
        levelList.add(gameA6);
        LevelMap gameB6 = createLevel(5, LevelMapType.FAVELAS, false, gameB5, gameA6, TeamType.NED, TeamType.CHI, "Gruppe B", CityType.SAO_PAULO, 1, 1, 1, 2);
        levelList.add(gameB6);
        LevelMap gameC6 = createLevel(5, LevelMapType.JUNGLE, true, gameC5, gameB6, TeamType.GRE, TeamType.CIV, "Gruppe C", CityType.FORTALEZA, 1, 1, 1, 2);
        levelList.add(gameC6);
        LevelMap gameD6 = createLevel(5, LevelMapType.FAVELAS, true, gameD5, gameC6, TeamType.CRC, TeamType.ENG, "Gruppe D", CityType.BELO_HORIZONTE, 1, 1, 1, 2);
        levelList.add(gameD6);
        LevelMap gameE6 = createLevel(5, LevelMapType.JUNGLE, false, gameE5, gameD6, TeamType.ECU, TeamType.FRA, "Gruppe E", CityType.RIO_DE_JANEIRO, 1, 1, 1, 2);
        levelList.add(gameE6);
        LevelMap gameF6 = createLevel(5, LevelMapType.FAVELAS, true, gameF5, gameE6, TeamType.BIH, TeamType.IRN, "Gruppe F", CityType.SALVADOR, 1, 1, 1, 2);
        levelList.add(gameF6);
        LevelMap gameG6 = createLevel(5, LevelMapType.JUNGLE, false, gameG5, gameF6, TeamType.POR, TeamType.GHA, "Gruppe G", CityType.BRASILIA, 1, 1, 1, 2);
        levelList.add(gameG6);
        LevelMap gameH6 = createLevel(5, LevelMapType.FAVELAS, true, gameH5, gameG6, TeamType.ALG, TeamType.RUS, "Gruppe H", CityType.CURITIBA, 1, 1, 0, 2);
        levelList.add(gameH6);
        // AchtelFinale
        ArrayList<LevelMap> predecessors = new ArrayList<LevelMap>();
        predecessors.add(gameA6);
        predecessors.add(gameB6);
        LevelMap achtelFinale1 = createLevel(6, LevelMapType.JUNGLE, false, predecessors, null, TeamType.BRA, TeamType.CHI, "Achtelfinale", CityType.BELO_HORIZONTE, 2, 0, 1, 1);
        levelList.add(achtelFinale1);

        predecessors.clear();
        predecessors.add(gameA6);
        predecessors.add(gameB6);
        LevelMap achtelFinale2 = createLevel(6, LevelMapType.FAVELAS, false, predecessors, achtelFinale1, TeamType.NED, TeamType.MEX, "Achtelfinale", CityType.FORTALEZA, 2, 1, 1, 1);
        levelList.add(achtelFinale2);

        predecessors.clear();
        predecessors.add(gameC6);
        predecessors.add(gameD6);
        LevelMap achtelFinale3 = createLevel(6, LevelMapType.FAVELAS, true, predecessors, achtelFinale1, TeamType.COL, TeamType.URU, "Achtelfinale", CityType.RIO_DE_JANEIRO, 2, 1, 1, 1);
        levelList.add(achtelFinale3);

        predecessors.clear();
        predecessors.add(gameC6);
        predecessors.add(gameD6);
        LevelMap achtelFinale4 = createLevel(6, LevelMapType.JUNGLE, false, predecessors, achtelFinale3, TeamType.CRC, TeamType.GRE, "Achtelfinale", CityType.RECIFE, 2, 1, 1, 1);
        levelList.add(achtelFinale4);

        predecessors.clear();
        predecessors.add(gameE6);
        predecessors.add(gameF6);
        LevelMap achtelFinale5 = createLevel(6, LevelMapType.JUNGLE, true, predecessors, achtelFinale4, TeamType.FRA, TeamType.NGA, "Achtelfinale", CityType.BRASILIA, 2, 1, 1, 1);
        levelList.add(achtelFinale5);

        predecessors.clear();
        predecessors.add(gameE6);
        predecessors.add(gameF6);
        LevelMap achtelFinale6 = createLevel(6, LevelMapType.FAVELAS, false, predecessors, achtelFinale5, TeamType.ARG, TeamType.SUI, "Achtelfinale", CityType.SAO_PAULO, 2, 1, 1, 1);
        levelList.add(achtelFinale6);

        predecessors.clear();
        predecessors.add(gameG6);
        predecessors.add(gameH6);
        LevelMap achtelFinale7 = createLevel(6, LevelMapType.JUNGLE, true, predecessors, achtelFinale6, TeamType.GER, TeamType.ALG, "Achtelfinale", CityType.PORTO_ALEGRE, 2, 1, 1, 1);
        levelList.add(achtelFinale7);

        predecessors.clear();
        predecessors.add(gameG6);
        predecessors.add(gameH6);
        LevelMap achtelFinale8 = createLevel(6, LevelMapType.FAVELAS, false, predecessors, achtelFinale7, TeamType.BEL, TeamType.USA, "Achtelfinale", CityType.SALVADOR, 2, 1, 0, 1);
        levelList.add(achtelFinale8);

        // Viertelfinale
        predecessors.clear();
        predecessors.add(achtelFinale1);
        predecessors.add(achtelFinale3);
        LevelMap viertelFinale1 = createLevel(7, LevelMapType.JUNGLE, false, predecessors, null, TeamType.BRA, TeamType.COL, "Viertelfinale", CityType.FORTALEZA, 2, 0, 1, 1);
        levelList.add(viertelFinale1);

        predecessors.clear();
        predecessors.add(achtelFinale5);
        predecessors.add(achtelFinale7);
        LevelMap viertelFinale2 = createLevel(7, LevelMapType.JUNGLE, true, predecessors, viertelFinale1, TeamType.FRA, TeamType.GER, "Viertelfinale", CityType.RIO_DE_JANEIRO, 2, 1, 1, 1);
        levelList.add(viertelFinale2);

        predecessors.clear();
        predecessors.add(achtelFinale2);
        predecessors.add(achtelFinale4);
        LevelMap viertelFinale3 = createLevel(7, LevelMapType.FAVELAS, false, predecessors, viertelFinale2, TeamType.NED, TeamType.CRC, "Viertelfinale", CityType.SALVADOR, 2, 1, 1, 1);
        levelList.add(viertelFinale3);

        predecessors.clear();
        predecessors.add(achtelFinale6);
        predecessors.add(achtelFinale8);
        LevelMap viertelFinale4 = createLevel(7, LevelMapType.JUNGLE, false, predecessors, viertelFinale3, TeamType.ARG, TeamType.BEL, "Viertelfinale", CityType.BRASILIA, 2, 1, 0, 1);
        levelList.add(viertelFinale4);

        // Halbfinale
        predecessors.clear();
        predecessors.add(viertelFinale1);
        predecessors.add(viertelFinale2);
        LevelMap halbFinale1 = createLevel(8, LevelMapType.JUNGLE, true, predecessors, null, TeamType.BRA, TeamType.GER, "Halbfinale", CityType.BELO_HORIZONTE, 2, 0, 1, 1);
        levelList.add(halbFinale1);

        predecessors.clear();
        predecessors.add(viertelFinale3);
        predecessors.add(viertelFinale4);
        LevelMap halbFinale2 = createLevel(8, LevelMapType.FAVELAS, true, predecessors, halbFinale1, TeamType.NED, TeamType.ARG, "Halbfinale", CityType.SAO_PAULO, 2, 1, 0, 1);
        levelList.add(halbFinale2);

        // Spiel um Platz drei
        predecessors.clear();
        predecessors.add(halbFinale1);
        predecessors.add(halbFinale2);
        LevelMap platzDrei = createLevel(9, LevelMapType.JUNGLE, true, predecessors, null, TeamType.BRA, TeamType.NED, "Spiel um Platz drei", CityType.BRASILIA, 2, 0, 0, 1);
        levelList.add(platzDrei);

        // Finale
        LevelMap finale = createLevel(10, LevelMapType.FAVELAS, true, platzDrei, null, TeamType.GER, TeamType.ARG, "Finale", CityType.RIO_DE_JANEIRO, 1, 0, 0, 0);

        levelList.add(finale);

        setTaxisLockBooleanFromDatabase(levelList);
        return levelList;
    }


    /**
     * Methode welche alle bereits geoeffneten Taxis aus der DB ausliest und diese erneut oeffnet.
     * @param levelList
     * @author Brokmeier, Pascal
     */
    private static void setTaxisLockBooleanFromDatabase(List<LevelMap> levelList){

        //gib mir eine levelMap aus dem tree
        for(LevelMap levelMap : levelList){

            if(levelMap.getLevelMapType() == LevelMapType.AIRPORT){
                continue;
            }
            //alle taxis daraus
            List<Taxi> taxis = levelMap.getAllTaxis();
            for(Taxi taxi : taxis){
                //ist das aktuelle taxi direction.DOWN? (nur diese koennen locked sein)
                if(taxi.getDirection() == Direction.DOWN){
                    //setze locked status aus DB
                    taxi.setUnlocked(getTaxiBoolean(levelMap, taxi.getDestinationLevelMap()));
                }
            }
        }
    }

    /**
     *
     * @param origin
     * @param destination
     * @author Brokmeier, Pascal
     */
    protected static boolean getTaxiBoolean(LevelMap origin, LevelMap destination){
        return DatabaseConnection.getTaxiBooleanForOriginAndDestination(origin.getLevelID(), destination.getLevelID());
    }

    public static void reset(){
        generator = new Random(seed);
        rawData = new ArrayList<>();
    }

    public static Random getGenerator() {
        return generator;
    }

    public static void setGenerator(Random generator) {
        LevelTreeGenerator.generator = generator;
    }


}
