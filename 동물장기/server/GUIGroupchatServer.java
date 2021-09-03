import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/* 방에 들어가는 함수 (0번 방 -> x번 방) (@enterRoom <roomNo>)
 * 방을 나가는 함수 (x번 방 -> 0번 방) (@exitRoom)
 * 방을 만드는 함수 (@createRoom <roomTitle>)
 * 
 * 방 목록을 클라이언트에게 전달 (#setRoomList numberOfRoom=3\방1 제목\방2 제목\방3 제목\)
 * 방 인원 목록을 클라이언트에게 전달 (#setUserList numberOfUser=2\유저 닉네임1\유저 닉네임2\)
 */
public class GUIGroupchatServer {
	static String serverPort;
	public final int MAX_ROOM_NUM = 10;
	HashMap<String, String> userInRoom; //<유저 닉네임, 방 번호>
	HashMap<String, DataOutputStream>[] room; //<유저 닉네임, 소켓>
	HashMap<String, HashMap<String, DataOutputStream>> globalMap; //<방 번호, room 해쉬맵>
	boolean[] roomNoManager;
	Room[] roomInfo;
	static String version = "v3.1.0";
	
	static JFrame f;
	TextArea ta = new TextArea();
	JTextField tf = new JTextField();
	
	//HashMap에서 value를 기준으로 key를 찾는 메소드
	public static String[] getKeysByValue(HashMap<String, String> HashMap, String value) {
	    Set<String> keys = new HashSet<String>();
	    for (Entry<String, String> entry : HashMap.entrySet()) {
	        if (Objects.equals(value, entry.getValue())) {
	            keys.add(entry.getKey());
	        }
	    }
	    return keys.toArray(new String[0]);
	}
	
	
	@SuppressWarnings("unchecked")
	GUIGroupchatServer() {
		f = new JFrame("Animal Shogi Server " + version);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		room = new HashMap[MAX_ROOM_NUM];
		for (int i = 0; i < MAX_ROOM_NUM; i++) {
			room[i] = new HashMap<String, DataOutputStream>();
			Collections.synchronizedMap(room[i]);
		}
		roomInfo = new Room[MAX_ROOM_NUM];
		for (int i = 0; i < MAX_ROOM_NUM; i++) roomInfo[i] = new Room();
		roomNoManager = new boolean[MAX_ROOM_NUM];
		for (int i = 0; i < MAX_ROOM_NUM; i++) roomNoManager[i] = false;
		
		globalMap = new HashMap<String, HashMap<String, DataOutputStream>>();
		Collections.synchronizedMap(globalMap);
		for (int i = 0; i < MAX_ROOM_NUM; i++) {
			globalMap.put("" + i, room[i]);
		}
		userInRoom = new HashMap<String, String>();
		Collections.synchronizedMap(userInRoom);
		
		roomNoManager[0] = true;
		roomInfo[0].set("대기실", "admin", "0");
		
		ta.setFont(new Font("Malgun Gothic", Font.PLAIN, 15));
		tf.setFont(new Font("Malgun Gothic", Font.PLAIN, 15));
		
		f.setLayout(new BorderLayout());
		f.add(ta, BorderLayout.CENTER);
		f.add(tf, BorderLayout.SOUTH);
		
		EventHandler handler = new EventHandler();
		tf.addActionListener((ActionListener) handler);
		
		ta.setEditable(false);
		f.setLocation(400, 200);
		f.setSize(650,300);
		f.setVisible(true);
		tf.requestFocus();
	}
	
	void startServer() {
		ServerSocket serverSocket = null;
		Socket socket = null;
		
		try {
			serverSocket = new ServerSocket(Integer.parseInt(serverPort));
			ta.append(getTime()+"서버가 준비되었습니다. ["+version+"] port : "+serverPort+"\r\n");
			
			while (true) {
				socket = serverSocket.accept();
				ServerReceiver thread = new ServerReceiver(socket);
				thread.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void sendToAll(String msg) {
		Iterator<String> it = globalMap.keySet().iterator();
		while (it.hasNext()) {
			HashMap<String, DataOutputStream> currentRoom = globalMap.get(it.next());
			Iterator<String> it2 = currentRoom.keySet().iterator();
			while (it2.hasNext()) {
				try {
					DataOutputStream out = (DataOutputStream) currentRoom.get(it2.next());
					out.writeUTF(msg);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	boolean sendToOne(String name, String msg) {
		if (userInRoom.containsKey(name)) {
			try {
				globalMap.get(getRoomNo(name)).get(name).writeUTF(msg);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			return true;
		} else {
			return false;
		}
	}
	void sendToRoom(String roomNo, String msg) {
		if (globalMap.containsKey(roomNo)) {
			HashMap<String, DataOutputStream> currentRoom = globalMap.get(roomNo);
			Iterator<String> it = currentRoom.keySet().iterator();
			
			while (it.hasNext()) {
				try {
					DataOutputStream out = (DataOutputStream) currentRoom.get(it.next());
					out.writeUTF(msg);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else {
			System.out.println("sendToRoom Error: 방 번호 "+roomNo+"가 존재하지 않음");
		}
	}
	void sendToRoomOpPlayer(String roomNo, String msg, String nick) {
		int roomNoInt = Integer.parseInt(roomNo);
		if (roomInfo[roomNoInt].numberOfPlayer != 2) return;
		if (globalMap.containsKey(roomNo)) {
			HashMap<String, DataOutputStream> currentRoom = globalMap.get(roomNo);
			Iterator<String> it = currentRoom.keySet().iterator();
			
			while (it.hasNext()) {
				try {
					String player = (String) it.next();
					if (!player.equals(nick)) {
						DataOutputStream out = (DataOutputStream) currentRoom.get(player);
						out.writeUTF(msg);
						return;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else {
			System.out.println("sendToRoom Error: 방 번호 "+roomNo+"가 존재하지 않음");
		}
	}
	void sendToRoomWithoutMe(String roomNo, String msg, String nick) {
		if (globalMap.containsKey(roomNo)) {
			HashMap<String, DataOutputStream> currentRoom = globalMap.get(roomNo);
			Iterator<String> it = currentRoom.keySet().iterator();
			
			while (it.hasNext()) {
				try {
					String player = (String) it.next();
					if (!player.equals(nick)) {
						DataOutputStream out = (DataOutputStream) currentRoom.get(player);
						out.writeUTF(msg);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else {
			System.out.println("sendToRoom Error: 방 번호 "+roomNo+"가 존재하지 않음");
		}
	}
	String getRoomNo(String nick) {
		return userInRoom.get(nick);
	}
	
	
	public static void main(String[] args) {
		GUIGroupchatServer chatWin = new GUIGroupchatServer();
		try {
			do {
				serverPort = JOptionPane.showInputDialog(f, "서버 포트를 입력하세요").trim();
			} while (!serverPort.matches("^[0-9]+$"));
		} catch (NullPointerException e) {
			System.exit(0);
		}
		chatWin.startServer();
	}
	
	class EventHandler extends FocusAdapter implements ActionListener {
		public void actionPerformed(ActionEvent ae) {
			
			boolean result = false;
			String msg = tf.getText();
			tf.setText("");
			if (msg.equals("")) return;
			String[] splitStr = msg.split(" ", 2);
			String cmd = "";
			if (splitStr.length > 1) cmd = splitStr[1];
			if (splitStr[0].equals("#send")) {
				String[] arg = cmd.split(" ", 2);
				if (arg.length < 2) {
					ta.append("USAGE: #send <nickname> <msg>\r\n");
					return;
				}
				result = sendToOne(arg[0], arg[1]);
				if (!result) {
					ta.append(getTime()+"Error: sendToOne"+"\r\n");
				}
			} else if (splitStr[0].equals("#userinroom")) {
				if (splitStr.length != 2) {
					ta.append("USAGE: #userinroom <room number>\r\n");
					return;
				}
				String[] str = getKeysByValue(userInRoom, splitStr[1]);
				for (int i = 0; i < str.length; i++) {
					ta.append((i+1)+": "+str[i]+", RoomNo: "+splitStr[1]+"\r\n");
				}
			} else if (splitStr[0].equals("#user")) {
				String[] str = userInRoom.keySet().toArray(new String[userInRoom.size()]);
				for (int i = 0; i < str.length; i++) {
					ta.append((i+1) + ": " + str[i] + ", RoomNo: " + userInRoom.get(str[i]) + "\r\n");
				}
			} else if (splitStr[0].equals("#kill")) {
				if (splitStr.length != 2 || splitStr[1].length() == 0) {
					ta.append("USAGE: #kill <nickname>\r\n");
					return;
				}
				if (!userInRoom.containsKey(splitStr[1])) {
					ta.append("\"" + splitStr[1] + "\" 유저가 존재하지 않습니다\r\n");
					return;
				}
				sendToOne(splitStr[1], "관리자로부터의 강제 종료");
				try {
					room[Integer.parseInt(getRoomNo(splitStr[1]))].get(splitStr[1]).close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if (splitStr[0].equals("#notice")){
				if (splitStr.length != 2 || splitStr[1].length() == 0) {
					ta.append("USAGE: #notice <message>\r\n");
					return;
				}
				sendToAll(splitStr[1]);
			} else {
				ta.append("Invalid command\r\n");
			}
			
			ta.append(getTime()+msg+"\r\n");
		}
	}
	
	
	
	
	class ServerReceiver extends Thread {
		Socket socket;
		DataInputStream in;
		DataOutputStream out;
		
		ServerReceiver(Socket socket) {
			this.socket = socket;
			try {
				in = new DataInputStream(socket.getInputStream());
				out = new DataOutputStream(socket.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public void run() {
			String name = "";
			try {
				String str = in.readUTF();
				String[] splitStr = str.split("\\\\", 2);
				name = splitStr[0];
				if (splitStr.length < 2 || !splitStr[1].endsWith(version)) {
					ta.append(getTime()+"["+socket.getInetAddress()+":"+socket.getPort()+"]와의 접속 강제 종료: 최신 버전이 아닌 클라이언트 \""+name+"\"\r\n");
					out.writeUTF("서버와의 접속 강제 종료: 최신 버전이 아닙니다.");
					ta.append(getTime()+"현재 서버접속자 수는 "+userInRoom.size()+"입니다.\r\n");
					socket.close();
					return;
				}
				ta.append(getTime()+"\""+name+"\"님이 ["+socket.getInetAddress()+":"+socket.getPort()+"]에서 접속하였습니다. ("+(userInRoom.size()+1)+")\r\n");
				if (!userInRoom.containsKey(name)) {
					//처음 들어온 사람은 대기방으로 입장
					userInRoom.put(name, "0");
					room[0].put(name, out);
					sendRoomList();
					sendUserList("0");
				} else { //대화명이 중복인 경우
					ta.append(getTime()+"["+socket.getInetAddress()+":"+socket.getPort()+"]와의 접속 강제 종료: 대화명 \""+name+"\" 중복\r\n");
					out.writeUTF("서버와의 접속 강제 종료: 대화명 \""+name+"\" 중복");
					ta.append(getTime()+"현재 서버접속자 수는 "+userInRoom.size()+"입니다.\r\n");
					socket.close();
					return;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			try {
				//sendToRoom("0", getTime()+"#"+name+"님이 접속하셨습니다.");
				sendToAll(getTime()+"#"+name+"님이 접속하셨습니다.");
				while (in != null) {
					try {
						String msg = in.readUTF();
						if (msg.startsWith("@")) {
							receiveRequest(name, msg);
						} else {
							sendToRoom(getRoomNo(name), msg);	//이 사람이 속한 방으로 메시지 보냄
						}
					} catch (SocketException se) {
						//ta.append(getTime()+"SocketException: ["+socket.getInetAddress()+":"+socket.getPort()+"] 접속 끊어짐\r\n");
						break;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				disconnectUser(name); //접속을 정상적으로 종료 시킴
				ta.append(getTime()+"\""+name+"\"님이 ["+socket.getInetAddress()+":"+socket.getPort()+"]에서 접속을 종료하였습니다. ("+userInRoom.size()+")\r\n");
				try {
					in.close();
					out.close();
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	String getTime() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY/MM/dd [HH:mm:ss] ");
		return dateFormat.format(new Date());
	}
	
	public boolean enterRoom(String nick, String roomNo) {
		try {
			String prevRoomNo = getRoomNo(nick);
			HashMap<String, DataOutputStream> prevRoom = globalMap.get(prevRoomNo);
			
			DataOutputStream out = prevRoom.get(nick); 
			prevRoom.remove(nick); //속해 있는 방을 빠져 나감

			userInRoom.put(nick, roomNo);
			globalMap.get(roomNo).put(nick, out);
			
			int prevRoomSize = prevRoom.size();
			if (!prevRoomNo.equals("0")) { //방 -> 대기실
				if (prevRoomSize == 0) deleteRoom(prevRoomNo); //방금 빠져나간 방에 아무도 없으면 방을 없앰
				else sendRoomList(); //대기실에 들어갈때 방 리스트를 전달
			}
			else { //대기실 -> 방
				sendRoomInfo(roomNo); //대기실이 아닌 방에 들어갈때 방 정보를 전달
				sendRoomList();
			}
			
			sendUserList(roomNo); //들어간 방에 유저 리스트를 전달
			if (prevRoomSize != 0) sendUserList(prevRoomNo); //이전에 속해 있었던 방에 유저 리스트를 전달
		} catch (Exception e) {
			System.out.println("enterRoom("+nick+", "+roomNo+") Error: " + e);
			return false;
		}
		return true;
	}
	public boolean exitRoom(String nick) {
		int roomNoInt = Integer.parseInt(getRoomNo(nick));
		if (roomInfo[roomNoInt].onGame) {
			if (nick.equals(roomInfo[roomNoInt].player[0])) {
				receiveRequest(roomInfo[roomNoInt].player[1], "@iWin");
				receiveRequest(roomInfo[roomNoInt].player[0], "@iLose");
			} else if (nick.equals(roomInfo[roomNoInt].player[1])) {
				receiveRequest(roomInfo[roomNoInt].player[0], "@iWin");
				receiveRequest(roomInfo[roomNoInt].player[1], "@iLose");
			}
		} else {
			if (nick.equals(roomInfo[roomNoInt].player[0]) && roomInfo[roomNoInt].numberOfPlayer > 0) {
				roomInfo[roomNoInt].player[0] = roomInfo[roomNoInt].player[1]; 
				roomInfo[roomNoInt].numberOfPlayer--;
			} else if (nick.equals(roomInfo[roomNoInt].player[1]) && roomInfo[roomNoInt].numberOfPlayer > 1) {
				roomInfo[roomNoInt].numberOfPlayer--;
			}
		}
		
		return enterRoom(nick, "0"); //대기실로 들어감
	}
	public int createRoom(String title, String nick) {
		int emptyRoomNo = -1;
		for (int i = 1; i < MAX_ROOM_NUM; i++) {
			if (!roomNoManager[i]) {
				emptyRoomNo = i;
				break;
			}
		}
		if (emptyRoomNo == -1) {
			System.out.println("createRoom Error: number of room is max");
		} else {
			//room[emptyRoomNo] = new HashMap<String, DataOutputStream>();
			//Collections.synchronizedHashMap(room[emptyRoomNo]);
			
			//globalMap.put("" + emptyRoomNo, room[emptyRoomNo]);
			roomNoManager[emptyRoomNo] = true;
			roomInfo[emptyRoomNo].set(title, nick, "" + emptyRoomNo);
			enterRoom(nick, "" + emptyRoomNo);
		}
		sendRoomList();
		return emptyRoomNo;
	}
	public void deleteRoom(String roomNo) {
		int roomNoInt = Integer.parseInt(roomNo);
		if (roomNoInt == 0) return;
		//globalMap.remove(roomNo);
		//room[roomNoInt] = null;
		//roomInfo[roomNoInt] = null;
		roomNoManager[roomNoInt] = false;
		roomInfo[roomNoInt].numberOfPlayer = 0;
		roomInfo[roomNoInt].onGame = false;
		
		sendRoomList();
	}
	/**
	 * 대기실에 방 리스트 전달
	 */
	public void sendRoomList() {
		//format: #setRoomList numberOfRoom=3\방1 제목\방2 제목\방3 제목\
		String prefix = "#setRoomList ";
		String roomTitle = "";
		int numberOfRoom = 0;
		for (int i = 1; i < MAX_ROOM_NUM; i++) {
			if (roomNoManager[i]) {
				numberOfRoom++;
				roomTitle += "["+i+"] [제목: "+roomInfo[i].title+"] (인원: "+globalMap.get(""+i).size()+") "+((roomInfo[i].onGame)?"(게임중)":"(대기중)")+"\\";
			}
		}
		sendToRoom("0", prefix + numberOfRoom + "\\" + roomTitle);
	}
	/**
	 * 해당하는 방에 그 방에 있는 유저 리스트를 전달 
	 * @param roomNo
	 */
	public void sendUserList(String roomNo) {
		//#setUserList numberOfUser=2\유저 닉네임1\유저 닉네임2\
		String prefix = "#setUserList ";
		String userNick = "";
		HashMap<String, DataOutputStream> currentRoom = globalMap.get(roomNo);
		Iterator<String> it = currentRoom.keySet().iterator();
		while (it.hasNext()) {
			userNick += ((String) it.next()) + "\\";
		}
		sendToRoom(roomNo, prefix + currentRoom.size() + "\\" + userNick);
		sendReadyPlayerList(roomNo);
	}
	public void sendRoomInfo(String roomNo) {
		//#setRoomInfo 방 번호\방 제목\방을 만든 사람\
		String prefix = "#setRoomInfo ";
		int roomNoInt = Integer.parseInt(roomNo);
		String cmd = prefix + roomNo + "\\" + roomInfo[roomNoInt].title + "\\" + roomInfo[roomNoInt].head + "\\";
		sendToRoom(roomNo, cmd);
	}
	public void sendReadyPlayerList(String roomNo) {
		//#readyPlayer nick1\
		if (roomNo.equals("0")) return;
		String prefix = "#readyPlayer ";
		String cmd = "";
		int roomNoInt = Integer.parseInt(roomNo);
		for (int i = 0; i < roomInfo[roomNoInt].numberOfPlayer; i++) {
			cmd += roomInfo[roomNoInt].player[i]+"\\";
		}
		sendToRoom(roomNo, prefix + cmd);
	}
	
	/**
	 * 유저로부터 요청을 받음
	 * @param nick
	 * @param command
	 */
	public void receiveRequest(String nick, String command) {
		System.out.println(command + " from \"" + nick + "\"" + " roomNo: " + getRoomNo(nick));
		try {
			String[] splitStrBlank = command.split(" ", 2);
			String prefix = splitStrBlank[0];
			String cmd = (splitStrBlank.length > 1) ? splitStrBlank[1] : "";
			if (prefix.equals("@enterRoom")) {
				if (!cmd.equals("0")) enterRoom(nick, cmd);
			} else if (prefix.equals("@exitRoom")) {
				exitRoom(nick);
			} else if (prefix.equals("@createRoom")) {
				int createdRoomNo = createRoom(cmd, nick);
				if (createdRoomNo != -1) {
					
				} else { //방의 갯수가 최대라서 방을 만들지 못한다는 것을 알림
					sendToOne(nick, "방의 갯수가 너무 많아서 더 이상 방을 만들 수 없습니다.");
				}
			} else if (prefix.equals("@moveMyPiece")) {
				sendToRoomWithoutMe(getRoomNo(nick), "#moveOpPiece " + cmd, nick);
			} else if (prefix.equals("@summonMyPiece")) {
				sendToRoomWithoutMe(getRoomNo(nick), "#summonOpPiece " + cmd, nick);
			} else if (prefix.equals("@iamReady")) {
				int roomNoInt = Integer.parseInt(getRoomNo(nick));
				if (!roomInfo[roomNoInt].onGame) {
					roomInfo[roomNoInt].player[roomInfo[roomNoInt].numberOfPlayer] = nick;
					roomInfo[roomNoInt].numberOfPlayer++;
					sendReadyPlayerList(getRoomNo(nick));
				} else {
					return;
				}
				if (roomInfo[roomNoInt].numberOfPlayer == 2) {
					roomInfo[roomNoInt].onGame = true;
					//#gameStart nick1\nick2\
					sendToRoom(getRoomNo(nick), "#gameStart "+roomInfo[roomNoInt].player[0]+"\\"+roomInfo[roomNoInt].player[1]+"\\");
					sendRoomList();
				}
			} else if (prefix.equals("@iWin")) {
				int roomNoInt = Integer.parseInt(getRoomNo(nick));
				if (!roomInfo[roomNoInt].onGame) return;
				if (!getRoomNo(nick).equals("0")) {
					roomInfo[roomNoInt].onGame = false;
					roomInfo[roomNoInt].numberOfPlayer = 0;
					if (nick.equals(roomInfo[roomNoInt].player[0])) {
						sendToRoom(getRoomNo(nick), getTime()+"\""+roomInfo[roomNoInt].player[0]+"\"님이 승리했습니다.");
						sendToRoom(getRoomNo(nick), getTime()+"\""+roomInfo[roomNoInt].player[1]+"\"님이 패배했습니다.");
						sendToRoom(getRoomNo(nick), "#GameOver "+roomInfo[roomNoInt].player[0]+"\\"+roomInfo[roomNoInt].player[1]);
					} else if (nick.equals(roomInfo[roomNoInt].player[1])) {
						sendToRoom(getRoomNo(nick), getTime()+"\""+roomInfo[roomNoInt].player[1]+"\"님이 승리했습니다.");
						sendToRoom(getRoomNo(nick), getTime()+"\""+roomInfo[roomNoInt].player[0]+"\"님이 패배했습니다.");
						sendToRoom(getRoomNo(nick), "#GameOver "+roomInfo[roomNoInt].player[1]+"\\"+roomInfo[roomNoInt].player[0]);
					}
					sendUserList(getRoomNo(nick));
					sendRoomList();
				}
			} else if (prefix.equals("@iLose")) {
				int roomNoInt = Integer.parseInt(getRoomNo(nick));
				if (!roomInfo[roomNoInt].onGame) return;
				if (!getRoomNo(nick).equals("0")) {
					roomInfo[roomNoInt].onGame = false;
					roomInfo[roomNoInt].numberOfPlayer = 0;
					if (nick.equals(roomInfo[roomNoInt].player[0])) {
						sendToRoom(getRoomNo(nick), getTime()+"\""+roomInfo[roomNoInt].player[1]+"\"님이 승리했습니다.");
						sendToRoom(getRoomNo(nick), getTime()+"\""+roomInfo[roomNoInt].player[0]+"\"님이 패배했습니다.");
						sendToRoom(getRoomNo(nick), "#GameOver "+roomInfo[roomNoInt].player[1]+"\\"+roomInfo[roomNoInt].player[0]);
					} else if (nick.equals(roomInfo[roomNoInt].player[1])) {
						sendToRoom(getRoomNo(nick), getTime()+"\""+roomInfo[roomNoInt].player[0]+"\"님이 승리했습니다.");
						sendToRoom(getRoomNo(nick), getTime()+"\""+roomInfo[roomNoInt].player[1]+"\"님이 패배했습니다.");
						sendToRoom(getRoomNo(nick), "#GameOver "+roomInfo[roomNoInt].player[0]+"\\"+roomInfo[roomNoInt].player[1]);
					}
					sendUserList(getRoomNo(nick));
					sendRoomList();
				}
			} else if (prefix.equals("@iDraw")) {
				int roomNoInt = Integer.parseInt(getRoomNo(nick));
				if (!roomInfo[roomNoInt].onGame) return;
				if (!getRoomNo(nick).equals("0")) {
					roomInfo[roomNoInt].onGame = false;
					roomInfo[roomNoInt].numberOfPlayer = 0;
					sendToRoom(getRoomNo(nick), getTime()+"\""+roomInfo[roomNoInt].player[0]+"\"님과 \""+roomInfo[roomNoInt].player[1]+"\"님이 비겼습니다.");
					sendToRoom(getRoomNo(nick), "#GameOverDraw "+roomInfo[roomNoInt].player[1]+"\\"+roomInfo[roomNoInt].player[0]);
					sendUserList(getRoomNo(nick));
					sendRoomList();
				}
			} else {
				System.out.println("Error: 존재하지 않는 명령어");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void disconnectUser(String name) {
		int roomNoInt = Integer.parseInt(getRoomNo(name));
		String currentRoomNo = getRoomNo(name);
		//해쉬맵에서 먼저 삭제
		globalMap.get(currentRoomNo).remove(name);
		userInRoom.remove(name);
		if (roomNoInt != 0) {
			if (roomInfo[roomNoInt].onGame) { //게임중인 방이었으면 상대방이 기권승
				if (name.equals(roomInfo[roomNoInt].player[0])) {
					receiveRequest(roomInfo[roomNoInt].player[1], "@iWin");
				} else if (name.equals(roomInfo[roomNoInt].player[1])) {
					receiveRequest(roomInfo[roomNoInt].player[0], "@iWin");
				}
			} else { //대기중인 방인 경우
				if (name.equals(roomInfo[roomNoInt].player[0]) && roomInfo[roomNoInt].numberOfPlayer > 0) {
					roomInfo[roomNoInt].player[0] = roomInfo[roomNoInt].player[1]; 
					roomInfo[roomNoInt].numberOfPlayer--;
				} else if (name.equals(roomInfo[roomNoInt].player[1]) && roomInfo[roomNoInt].numberOfPlayer > 1) {
					roomInfo[roomNoInt].numberOfPlayer--;
				}
			}
		}
		if (globalMap.get(currentRoomNo).size() == 0) { //방금 빠져나간 방에 아무도 없는 경우
			deleteRoom(currentRoomNo);
		} else {
			//sendToRoom(currentRoomNo, getTime()+"#"+name+"님이 접속을 종료하셨습니다.");
			sendUserList(currentRoomNo);
		}
		sendToAll(getTime()+"#"+name+"님이 접속을 종료하셨습니다.");
		if (currentRoomNo.equals("0")) sendRoomList();
		
	}
}
