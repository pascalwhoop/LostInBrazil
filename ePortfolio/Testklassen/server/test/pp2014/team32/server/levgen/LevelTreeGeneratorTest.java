package pp2014.team32.server.levgen;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import pp2014.team32.shared.entities.LevelMap;
import pp2014.team32.shared.enums.UIObjectType;
import pp2014.team32.shared.utils.PrintHelper;
import pp2014.team32.shared.utils.PropertyManager;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 *
* @author Brokmeier, Pascal
 * @version 29.06.14
 */

@RunWith(JUnit4.class)
public class LevelTreeGeneratorTest {

    @Test
    public void testGenerateLevelTree(){
        List<LevelMap> levelTree =  LevelTreeGenerator.generateLevelTree();
        System.out.print("foo");

    }

    /**
     * Test, welcher ueberprueft ob der Generator immer wieder die selben Level erzeugt.
     */
    @Test
    public void sameLevelStructureTest(){

        //setzen den generator seed auf 1
        LevelTreeGenerator.setGenerator(new Random(1));
        //erzeugen leveltree1
        LevelTreeGenerator.generateLevelTree();
        ArrayList<UIObjectType[][]> data1 = LevelTreeGenerator.rawData;

        LevelTreeGenerator.reset();
        //setzen seed wieder auf 1
        LevelTreeGenerator.setGenerator(new Random(1));
        //holen uns zweiten leveltree
        LevelTreeGenerator.generateLevelTree();
        ArrayList<UIObjectType[][]> data2 = LevelTreeGenerator.rawData;

        //vergleichen fuer jedes level ob struktur gleich ist.
        for (int i = 0; i<data1.size();i++){
            String map1String = PrintHelper.returnMapString(data1.get(i));
            String map2String = PrintHelper.returnMapString(data1.get(i));
            assertEquals(map1String, map2String);
        }

    }


    @Test
    public void testTaxiConnections(){
        List<LevelMap> levelTree = LevelTreeGenerator.generateLevelTree();
        PrintHelper.printTaxisInLevelTree(levelTree);
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
