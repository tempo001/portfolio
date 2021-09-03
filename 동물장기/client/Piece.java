
public class Piece {
	public int x;
	public int y;
	public boolean own;
	public String kind;
	public boolean onBoard;
	
	Piece(int x, int y, boolean own, String kind, boolean onBoard) {
		this.x = x;
		this.y = y;
		this.own = own;
		this.kind = kind;
		this.onBoard = onBoard;
	}
	
	//내부 변수를 출력
	public String toString() {
		return "x="+x+" y="+y+" own="+own+" kind="+kind+" onBoard="+onBoard;
	}
}
