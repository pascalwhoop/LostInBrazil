package pp2014.team32.server.levgen;

import pp2014.team32.shared.enums.UIObjectType;

import java.util.Arrays;

/**
 * Prototyp
 *
 * @author Can
 */
@Deprecated
public class LevGen {
    //	int difficulty;
    private UIObjectType[][] map;
    //public generateLevel (difficulty)

    public LevGen(int xSize, int ySize) {
        map = new UIObjectType[xSize][ySize];

        resetMap();
    }

/**
 * Fuellt die gesamte Map mit WALLS
 * @param x
 * @param y
 */
    public void floodFillRecursive(int x, int y) {

        if(map[x][y] == UIObjectType.WALL) map[x][y]  = UIObjectType.FLOOR; // Fels wird weg gehackt

        if (x > 0 &&                map[x - 1][y] == UIObjectType.WALL) floodFillRecursive(x - 1, y);
        if (y+1 < map[x].length &&    map[x][y + 1] == UIObjectType.WALL) floodFillRecursive(x, y + 1);
        if (x+1 < map.length &&       map[x + 1][y] == UIObjectType.WALL) floodFillRecursive(x + 1, y);
        if (y > 0 &&                map[x][y - 1] == UIObjectType.WALL) floodFillRecursive(x, y - 1);

        //achter flood fill falls Anfang auf OUTER_WALL landet
        if(x > 0 && y > 0 && map[x-1][y-1] == UIObjectType.WALL) floodFillRecursive(x - 1, y - 1);
        if(x > 0 && y+1 < map[x].length && map[x-1][y+1] == UIObjectType.WALL) floodFillRecursive(x - 1, y + 1);
        if(x+1 < map.length && y > 0 && map[x+1][y-1] == UIObjectType.WALL) floodFillRecursive(x + 1, y - 1);
        if(x+1 < map.length && y+1 < map[x].length && map[x+1][y+1] == UIObjectType.WALL) floodFillRecursive(x + 1, y + 1);

    }
//TODO
    public void floodFillIterative(int x, int y){
    	
    	
    }

/**
 * Fuellt die gesamte Map mit WALLS
 */
    public void resetMap(){
        for(UIObjectType[] row : map){
            Arrays.fill(row, UIObjectType.WALL);
        }

        // Sorgt dafuer, dass an den Raendern der Karte mit einer nicht loeschbaren Wand besetzt werden, welche beim floodfill niemals weggemacht werden
        UIObjectType[] border = map[0]; //border left
        Arrays.fill(border, UIObjectType.OUTER_WALL);
        border = map[map.length-1]; //border on the right
        Arrays.fill(border, UIObjectType.OUTER_WALL);

        //fill top and bottom row
        for(UIObjectType[] row: map){
            row[0] = UIObjectType.OUTER_WALL;
            row[row.length-1] = UIObjectType.OUTER_WALL;
        }

    }

    public UIObjectType[][] getMap() {
        return map;
    }

    public void setMap(UIObjectType[][] map) {
        this.map = map;
    }
}
