package pp2014.team32.server.levgen;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import pp2014.team32.server.levgen.LevGen;
import pp2014.team32.shared.enums.UIObjectType;
import pp2014.team32.shared.utils.PrintHelper;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * @author Can
 * @version 03.06.14
 */

@RunWith(JUnit4.class)
public class LevGenTest {

    @Before
    public void setUp() throws Exception {

    }

    /*@Test
    public void printMapTest(){
        UIObjectType[][] map = new UIObjectType[10][10];

        for(UIObjectType[] row : map){
            Arrays.fill(row, UIObjectType.WALL);
        }

        map[2][4] = UIObjectType.FLOOR;
        map[2][2] = UIObjectType.FLOOR;
        map[2][3] = UIObjectType.FLOOR;
        map[3][4] = UIObjectType.FLOOR;

        PrintHelper.printMap(map);
    }
*/

    @Test
    public void createRandomMapTest() {
       /* LevGen levGen = new LevGen(40);
        levGen.createRandomMap();
        PrintHelper.printMap(levGen.getMap());*/
    }

    @Test
    @Ignore
    public void floodFillTest() {
        //perform method to be tested
        LevGen levGen = new LevGen(50, 50);
        levGen.floodFillRecursive(49, 49);

        /*LevGen levGen2 = new LevGen(100,100);
        levGen2.floodFillRecursive(4,2);*/

        //create test map with all floor attributes

        //fill all with floor
        UIObjectType[][] testMap = new UIObjectType[50][50];
        for (UIObjectType[] row : testMap) {
            Arrays.fill(row, UIObjectType.FLOOR);
        }

        //make borders OUTER_WALL
        UIObjectType[] border = testMap[0]; //border left
        Arrays.fill(border, UIObjectType.OUTER_WALL);
        border = testMap[testMap.length - 1]; //border on the right
        Arrays.fill(border, UIObjectType.OUTER_WALL);


        for (UIObjectType[] row : testMap) {
            row[0] = UIObjectType.OUTER_WALL;
            row[row.length - 1] = UIObjectType.OUTER_WALL;
        }

        PrintHelper.printMap(testMap);
        System.out.print("\n");
        PrintHelper.printMap(levGen.getMap());
        System.out.print("\n");
        /*PrintHelper.printMap(levGen2.getMap());*/


        assertEquals(PrintHelper.returnMapString(testMap), PrintHelper.returnMapString(levGen.getMap()));
        /*assertEquals(PrintHelper.returnMapString(testMap), PrintHelper.returnMapString(levGen2.getMap()));*/


    }

}


