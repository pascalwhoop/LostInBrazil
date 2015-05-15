package pp2014.team32.server.levgen;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import pp2014.team32.shared.enums.Direction;
import pp2014.team32.shared.enums.UIObjectType;
import pp2014.team32.shared.utils.PrintHelper;
import pp2014.team32.shared.utils.PropertyManager;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**

 * @author Brokmeier, Pascal
 * @version 20.06.14
 */

@RunWith(JUnit4.class)
public class TyrantMapGeneratorTest {

    TyrantMapGenerator generator;


    @Before
    public void initGenerator(){
        generator = new TyrantMapGenerator(100, 1, new Random());
    }

    /**
     * Testet ob die Map vollkommen mit Steinen gefuellt wird.
     * @author Brokmeier, Pascal
     */
    @Test
    public void tyrantMapGeneratorFillTest(){
        generator.initializeMapData();
        PrintHelper.printMap(generator.getRawMapData());
    }

    @Test
    public void fillRectTest(){
        generator.initializeMapData();
        generator.fillRect(0,0,10,10, UIObjectType.FLOOR);
        PrintHelper.printMap(generator.getRawMapData());
    }

    @Test
    public void tryPlacingRoomTest(){
        generator.initializeMapData();
        generator.tryPlacingRoom(10,0, Direction.DOWN);
        PrintHelper.printMap(generator.getRawMapData());
    }

    @Test
    public void mapGeneratorTest(){
        generator.generate(8,1,1,1,5);
        PrintHelper.printMap(generator.getRawMapData());

    }








    /**
     * nur notwendig fuer die properties files usw.
     */
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
            propertyPaths.add("server/prefs/settings.properties");
            propertyPaths.add("Shared/prefs/settings.properties");
            loggingPath = "server/prefs/logging.properties";
        }

        // Initialize PropertyManager
        new PropertyManager(propertyPaths, loggingPath);
    }
}
