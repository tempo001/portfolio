import java.awt.Point;
import java.util.ArrayList;

public class DbClass {
	public int type; //0: �ʼ�, 1: �߼�, 2: ����, 3: ���� ���ħ
	public int group; //0: ������, 1: �Ʒ�, 2: �����ʰ� �Ʒ�
	public String phoneme = ""; //����(����, ����)
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
