package pp2014.team32.server.levgen;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import pp2014.team32.server.levgen.LevelMapGenerator;
import pp2014.team32.shared.utils.PrintHelper;
import pp2014.team32.shared.utils.PropertyManager;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 *
 * @author Brokmeier, Pascal
 * @version 16.06.14
 */
@RunWith(JUnit4.class)
public class LevelMapGeneratorTest {

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

    @Test
    public void createMapWithSeedTest(){
        long seed1 = 234234123;
        long seed2 = 987687665;

        LevelMapGenerator levelMapGenerator1 = new LevelMapGenerator(100,100,1, seed1);
        LevelMapGenerator levelMapGenerator2 = new LevelMapGenerator(100,100,1, seed1);
        LevelMapGenerator levelMapGenerator3 = new LevelMapGenerator(100,100,1, seed2);
        LevelMapGenerator levelMapGenerator4 = new LevelMapGenerator(100,100,1, seed2);

        assertEquals(PrintHelper.returnMapString(levelMapGenerator1.getData()), PrintHelper.returnMapString(levelMapGenerator2.getData()));
        assertEquals(PrintHelper.returnMapString(levelMapGenerator3.getData()), PrintHelper.returnMapString(levelMapGenerator4.getData()));
        assertNotEquals(PrintHelper.returnMapString(levelMapGenerator1.getData()), PrintHelper.returnMapString(levelMapGenerator3.getData()));

        PrintHelper.printMap(levelMapGenerator2.getData());
    }
}
