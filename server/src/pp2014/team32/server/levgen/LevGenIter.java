package pp2014.team32.server.levgen;

import pp2014.team32.shared.enums.UIObjectType;

import java.util.Arrays;
import java.util.Stack;
/**
 * Iteratives Floodfill, um Overflow zu vermeiden
 * Als Alternative zur StackOverflow erzeugenden Rekursion
 * Anfangs falscher Ansatz mit 8seitigem FloodFill in der Rekursion, deswegen diese Klasse (nicht verwendet)
 * @author Can
 *
 */
@Deprecated
public class LevGenIter {
	  //	int difficulty;
    private UIObjectType[][] map;
    //public generateLevel (difficulty)

    public LevGenIter(int xSize, int ySize) {
        map = new UIObjectType[xSize][ySize];

        resetMap();
    }
    //Klasse Flood fuer Stack
    public class Flood{
    	int x,y;
    	Flood (int x, int y){
        	this.x=x;
        	this.y=y;
    	}
    }
    
    public void floodFillIteration(int x, int y) {

    	Stack <Flood> f = new Stack <Flood>();
    	f.push(new Flood(x,y));
    	while(f.isEmpty()){
    		Flood fl = f.pop();
    		//[x].length fuer die Hoehe der Map, .length fuer die Laenge
    		if ((fl.x>=0) && (fl.y>=0) && (fl.y<=map[x].length) && (fl.x<=map.length)){
    			//Vierer Floodfill
        		f.push (new Flood(fl.x+1, fl.y));
        		f.push (new Flood(fl.x, fl.y+1));
        		f.push (new Flood(fl.x-1, fl.y));
        		f.push (new Flood(fl.x, fl.y-1));
        		//Achter Floodfill:
        		f.push (new Flood(fl.x+1, fl.y+1));
        		f.push (new Flood(fl.x+1, fl.y-1));
        		f.push (new Flood(fl.x-1, fl.y+1));
        		f.push (new Flood(fl.x-1, fl.y-1));
    		}
    		
    	}
    	
    }
    public void floodFillIterative(int x, int y){
    	//todo ehemals
    	
    }


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