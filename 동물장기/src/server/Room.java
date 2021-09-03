
public class Room {
	public String title;
	public String head; //방을 만든 사람
	public String[] player = new String[2];
	public int numberOfPlayer;
	public String roomNo;
	public boolean onGame;
	//public boolean turnFirstPlayer;
	
	public Room() {
		title = "null";
		head = "null";
		roomNo = "-1";
		onGame = false;
		//turnFirstPlayer = true;
		numberOfPlayer = 0;
	}
	public Room(String title, String head, String roomNo) {
		this.title = title;
		this.head = head;
		this.roomNo = roomNo;
		onGame = false;
		//turnFirstPlayer = true;
		numberOfPlayer = 0;
	}
	public void set(String title, String head, String roomNo) {
		this.title = title;
		this.head = head;
		this.roomNo = roomNo;
	}
	public void setPlayer(String p1, String p2) {
		player[0] = p1;
		player[1] = p2;
		//turnFirstPlayer = true;
	}
	public void setState(boolean onGame) {
		this.onGame = onGame;
		//turnFirstPlayer = true;
	}
}
