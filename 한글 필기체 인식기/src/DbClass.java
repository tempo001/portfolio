import java.awt.Point;
import java.util.ArrayList;

public class DbClass {
	public int type; //0: 초성, 1: 중성, 2: 종성, 3: 종성 겹받침
	public int group; //0: 오른쪽, 1: 아래, 2: 오른쪽과 아래
	public String phoneme = ""; //음소(자음, 모음)
	public ArrayList<Double> pattern;
	public ArrayList<Double> lengthList;
	public ArrayList<Boolean> isOnOff;
	
	public DbClass() {
		type = group = -1;
		pattern = lengthList = null;
	}
	public DbClass(int type, int group, String phoneme, ArrayList<Double> pattern, ArrayList<Double> lengthList, ArrayList<Boolean> isOnOff) {
		this.type = type;
		this.group = group;
		this.phoneme = phoneme;
		this.pattern = pattern;
		this.lengthList = lengthList;
		this.isOnOff = isOnOff;
	}
	public void show() {
		System.out.println("phoneme : "+phoneme+", type : "+type+", group : "+group);
		int n = pattern.size();
		for(int i=0;i<n;i++) {
			//System.out.print("("+pattern.get(i)+","+lengthList.get(i)+") ");
			System.out.print(pattern.get(i)+", ");
		}
		System.out.println();
	}
}
