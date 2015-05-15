package pp2014.team32.server.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import pp2014.team32.server.levgen.AirportGenerator;
import pp2014.team32.server.levgen.TyrantMapGenerator;
import pp2014.team32.shared.entities.LevelMap;
import pp2014.team32.shared.enums.UIObjectType;
import pp2014.team32.shared.utils.PrintHelper;
import pp2014.team32.shared.utils.PropertyManager;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**

 *
 * @author Brokmeier, Pascal
 * @version 01.07.14
 */

@RunWith(JUnit4.class)
public class AirportGeneratorTest {


    @Test
    public void testAirportRawData(){
        PrintHelper.printMap(AirportGenerator.generateAirportMap());
    }

    @Test
    public void testAirportLevelGeneration(){
        int airportSize = Integer.parseInt(PropertyManager.getProperty("levgen.airportSize"));
        TyrantMapGenerator tyrantGenForAirport = new TyrantMapGenerator(airportSize, 1, new Random(123456789));

        UIObjectType[][] airportData = AirportGenerator.generateAirportMap();
        LevelMap airport = tyrantGenForAirport.generateAirportLevel(airportData);
        System.out.print("foo");



    }

    static {
        String s = (new File("")).getAbsolutePath();
        String serverPrefs = "";
        String sharedPrefs = "";

        List<String> propertyPaths = new LinkedList<String>();
        String loggingPath;
        if (s.substring((s.length()-3), (s.length())).equals("bin")){
            propertyPaths.add("../../checkout/server/prefs/settings.properties");
            propertyPaths.add("../../checkout/shared/prefs/settings.properties");
            loggingPath = "../../checkout/server/prefs/logging.properties";
        }
        else {
            propertyPaths.add("prefs/settings.properties");
            propertyPaths.add("../Shared/prefs/settings.properties");
            loggingPath = "prefs/logging.properties";
        }

        // Initialize PropertyManager
        new PropertyManager(propertyPaths, loggingPath);
    }
}
