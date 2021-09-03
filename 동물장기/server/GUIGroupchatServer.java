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

/* �濡 ���� �Լ� (0�� �� -> x�� ��) (@enterRoom <roomNo>)
 * ���� ������ �Լ� (x�� �� -> 0�� ��) (@exitRoom)
 * ���� ����� �Լ� (@createRoom <roomTitle>)
 * 
 * �� ����� Ŭ���̾�Ʈ���� ���� (#setRoomList numberOfRoom=3\��1 ����\��2 ����\��3 ����\)
 * �� �ο� ����� Ŭ���̾�Ʈ���� ���� (#setUserList numberOfUser=2\���� �г���1\���� �г���2\)
 */
public class GUIGroupchatServer {
	static String serverPort;
	public final int MAX_ROOM_NUM = 10;
	HashMap<String, String> userInRoom; //<���� �г���, �� ��ȣ>
	HashMap<String, DataOutputStream>[] room; //<���� �г���, ����>
	HashMap<String, HashMap<String, DataOutputStream>> globalMap; //<�� ��ȣ, room �ؽ���>
	boolean[] roomNoManager;
	Room[] roomInfo;
	static String version = "v3.1.0";
	
	static JFrame f;
	TextArea ta = new TextArea();
	JTextField tf = new JTextField();
	
	//HashMap���� value�� �������� key�� ã�� �޼ҵ�
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
		roomInfo[0].set("����", "admin", "0");
		
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
			ta.append(getTime()+"������ �غ�Ǿ����ϴ�. ["+version+"] port : "+serverPort+"\r\n");
			
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
			System.out.println("sendToRoom Error: �� ��ȣ "+roomNo+"�� �������� ����");
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
			System.out.println("sendToRoom Error: �� ��ȣ "+roomNo+"�� �������� ����");
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
			System.out.println("sendToRoom Error: �� ��ȣ "+roomNo+"�� �������� ����");
		}
	}
	String getRoomNo(String nick) {
		return userInRoom.get(nick);
	}
	
	
	public static void main(String[] args) {
		GUIGroupchatServer chatWin = new GUIGroupchatServer();
		try {
			do {
				serverPort = JOptionPane.showInputDialog(f, "���� ��Ʈ�� �Է��ϼ���").trim();
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
					ta.append("\"" + splitStr[1] + "\" ������ �������� �ʽ��ϴ�\r\n");
					return;
				}
				sendToOne(splitStr[1], "�����ڷκ����� ���� ����");
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
					ta.append(getTime()+"["+socket.getInetAddress()+":"+socket.getPort()+"]���� ���� ���� ����: �ֽ� ������ �ƴ� Ŭ���̾�Ʈ \""+name+"\"\r\n");
					out.writeUTF("�������� ���� ���� ����: �ֽ� ������ �ƴմϴ�.");
					ta.append(getTime()+"���� ���������� ���� "+userInRoom.size()+"�Դϴ�.\r\n");
					socket.close();
					return;
				}
				ta.append(getTime()+"\""+name+"\"���� ["+socket.getInetAddress()+":"+socket.getPort()+"]���� �����Ͽ����ϴ�. ("+(userInRoom.size()+1)+")\r\n");
				if (!userInRoom.containsKey(name)) {
					//ó�� ���� ����� �������� ����
					userInRoom.put(name, "0");
					room[0].put(name, out);
					sendRoomList();
					sendUserList("0");
				} else { //��ȭ���� �ߺ��� ���
					ta.append(getTime()+"["+socket.getInetAddress()+":"+socket.getPort()+"]���� ���� ���� ����: ��ȭ�� \""+name+"\" �ߺ�\r\n");
					out.writeUTF("�������� ���� ���� ����: ��ȭ�� \""+name+"\" �ߺ�");
					ta.append(getTime()+"���� ���������� ���� "+userInRoom.size()+"�Դϴ�.\r\n");
					socket.close();
					return;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			try {
				//sendToRoom("0", getTime()+"#"+name+"���� �����ϼ̽��ϴ�.");
				sendToAll(getTime()+"#"+name+"���� �����ϼ̽��ϴ�.");
				while (in != null) {
					try {
						String msg = in.readUTF();
						if (msg.startsWith("@")) {
							receiveRequest(name, msg);
						} else {
							sendToRoom(getRoomNo(name), msg);	//�� ����� ���� ������ �޽��� ����
						}
					} catch (SocketException se) {
						//ta.append(getTime()+"SocketException: ["+socket.getInetAddress()+":"+socket.getPort()+"] ���� ������\r\n");
						break;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				disconnectUser(name); //������ ���������� ���� ��Ŵ
				ta.append(getTime()+"\""+name+"\"���� ["+socket.getInetAddress()+":"+socket.getPort()+"]���� ������ �����Ͽ����ϴ�. ("+userInRoom.size()+")\r\n");
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
			prevRoom.remove(nick); //���� �ִ� ���� ���� ����

			userInRoom.put(nick, roomNo);
			globalMap.get(roomNo).put(nick, out);
			
			int prevRoomSize = prevRoom.size();
			if (!prevRoomNo.equals("0")) { //�� -> ����
				if (prevRoomSize == 0) deleteRoom(prevRoomNo); //��� �������� �濡 �ƹ��� ������ ���� ����
				else sendRoomList(); //���ǿ� ���� �� ����Ʈ�� ����
			}
			else { //���� -> ��
				sendRoomInfo(roomNo); //������ �ƴ� �濡 ���� �� ������ ����
				sendRoomList();
			}
			
			sendUserList(roomNo); //�� �濡 ���� ����Ʈ�� ����
			if (prevRoomSize != 0) sendUserList(prevRoomNo); //������ ���� �־��� �濡 ���� ����Ʈ�� ����
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
		
		return enterRoom(nick, "0"); //���Ƿ� ��
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
	 * ���ǿ� �� ����Ʈ ����
	 */
	public void sendRoomList() {
		//format: #setRoomList numberOfRoom=3\��1 ����\��2 ����\��3 ����\
		String prefix = "#setRoomList ";
		String roomTitle = "";
		int numberOfRoom = 0;
		for (int i = 1; i < MAX_ROOM_NUM; i++) {
			if (roomNoManager[i]) {
				numberOfRoom++;
				roomTitle += "["+i+"] [����: "+roomInfo[i].title+"] (�ο�: "+globalMap.get(""+i).size()+") "+((roomInfo[i].onGame)?"(������)":"(�����)")+"\\";
			}
		}
		sendToRoom("0", prefix + numberOfRoom + "\\" + roomTitle);
	}
	/**
	 * �ش��ϴ� �濡 �� �濡 �ִ� ���� ����Ʈ�� ���� 
	 * @param roomNo
	 */
	public void sendUserList(String roomNo) {
		//#setUserList numberOfUser=2\���� �г���1\���� �г���2\
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
		//#setRoomInfo �� ��ȣ\�� ����\���� ���� ���\
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
	 * �����κ��� ��û�� ����
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
					
				} else { //���� ������ �ִ�� ���� ������ ���Ѵٴ� ���� �˸�
					sendToOne(nick, "���� ������ �ʹ� ���Ƽ� �� �̻� ���� ���� �� �����ϴ�.");
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
						sendToRoom(getRoomNo(nick), getTime()+"\""+roomInfo[roomNoInt].player[0]+"\"���� �¸��߽��ϴ�.");
						sendToRoom(getRoomNo(nick), getTime()+"\""+roomInfo[roomNoInt].player[1]+"\"���� �й��߽��ϴ�.");
						sendToRoom(getRoomNo(nick), "#GameOver "+roomInfo[roomNoInt].player[0]+"\\"+roomInfo[roomNoInt].player[1]);
					} else if (nick.equals(roomInfo[roomNoInt].player[1])) {
						sendToRoom(getRoomNo(nick), getTime()+"\""+roomInfo[roomNoInt].player[1]+"\"���� �¸��߽��ϴ�.");
						sendToRoom(getRoomNo(nick), getTime()+"\""+roomInfo[roomNoInt].player[0]+"\"���� �й��߽��ϴ�.");
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
						sendToRoom(getRoomNo(nick), getTime()+"\""+roomInfo[roomNoInt].player[1]+"\"���� �¸��߽��ϴ�.");
						sendToRoom(getRoomNo(nick), getTime()+"\""+roomInfo[roomNoInt].player[0]+"\"���� �й��߽��ϴ�.");
						sendToRoom(getRoomNo(nick), "#GameOver "+roomInfo[roomNoInt].player[1]+"\\"+roomInfo[roomNoInt].player[0]);
					} else if (nick.equals(roomInfo[roomNoInt].player[1])) {
						sendToRoom(getRoomNo(nick), getTime()+"\""+roomInfo[roomNoInt].player[0]+"\"���� �¸��߽��ϴ�.");
						sendToRoom(getRoomNo(nick), getTime()+"\""+roomInfo[roomNoInt].player[1]+"\"���� �й��߽��ϴ�.");
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
					sendToRoom(getRoomNo(nick), getTime()+"\""+roomInfo[roomNoInt].player[0]+"\"�԰� \""+roomInfo[roomNoInt].player[1]+"\"���� �����ϴ�.");
					sendToRoom(getRoomNo(nick), "#GameOverDraw "+roomInfo[roomNoInt].player[1]+"\\"+roomInfo[roomNoInt].player[0]);
					sendUserList(getRoomNo(nick));
					sendRoomList();
				}
			} else {
				System.out.println("Error: �������� �ʴ� ��ɾ�");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void disconnectUser(String name) {
		int roomNoInt = Integer.parseInt(getRoomNo(name));
		String currentRoomNo = getRoomNo(name);
		//�ؽ��ʿ��� ���� ����
		globalMap.get(currentRoomNo).remove(name);
		userInRoom.remove(name);
		if (roomNoInt != 0) {
			if (roomInfo[roomNoInt].onGame) { //�������� ���̾����� ������ ��ǽ�
				if (name.equals(roomInfo[roomNoInt].player[0])) {
					receiveRequest(roomInfo[roomNoInt].player[1], "@iWin");
				} else if (name.equals(roomInfo[roomNoInt].player[1])) {
					receiveRequest(roomInfo[roomNoInt].player[0], "@iWin");
				}
			} else { //������� ���� ���
				if (name.equals(roomInfo[roomNoInt].player[0]) && roomInfo[roomNoInt].numberOfPlayer > 0) {
					roomInfo[roomNoInt].player[0] = roomInfo[roomNoInt].player[1]; 
					roomInfo[roomNoInt].numberOfPlayer--;
				} else if (name.equals(roomInfo[roomNoInt].player[1]) && roomInfo[roomNoInt].numberOfPlayer > 1) {
					roomInfo[roomNoInt].numberOfPlayer--;
				}
			}
		}
		if (globalMap.get(currentRoomNo).size() == 0) { //��� �������� �濡 �ƹ��� ���� ���
			deleteRoom(currentRoomNo);
		} else {
			//sendToRoom(currentRoomNo, getTime()+"#"+name+"���� ������ �����ϼ̽��ϴ�.");
			sendUserList(currentRoomNo);
		}
		sendToAll(getTime()+"#"+name+"���� ������ �����ϼ̽��ϴ�.");
		if (currentRoomNo.equals("0")) sendRoomList();
		
	}
}
