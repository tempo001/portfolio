import java.awt.*;
import java.awt.event.*;

import javax.swing.*;


public class WaitRoom {
	String nickname;
	TextArea ta;
	JTextField tf;
	JFrame f;
	JLabel infoUser;
	JLabel infoRoom;
	JButton enterButton;
	JButton createButton;
	List userList;
	List roomList;
	
	
	public WaitRoom(String title) {
		nickname = AllMain2.nickname;
		f = new JFrame(title);
		f.setLayout(new BorderLayout());
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		Panel leftPanel = new Panel();
		Panel rightPanel = new Panel();
		Panel rightSouthPanel = new Panel();
		Panel rightNorthPanel = new Panel();
		Panel southPanel = new Panel();
		leftPanel.setLayout(new BorderLayout());
		leftPanel.setPreferredSize(new Dimension(450, 400));
		rightPanel.setLayout(new BorderLayout());
		rightSouthPanel.setLayout(new BorderLayout());
		rightNorthPanel.setLayout(new BorderLayout());
		southPanel.setLayout(new BorderLayout());
		
		ta = new TextArea("", 4, 60, TextArea.SCROLLBARS_VERTICAL_ONLY);
		tf = new JTextField();
		ta.setFont(new Font("Malgun Gothic", Font.PLAIN, 15));
		tf.setFont(new Font("Malgun Gothic", Font.PLAIN, 15));
		ta.setEditable(false);
		
		EventHandler handler = new EventHandler();
		tf.addActionListener(handler);
		
		infoUser = new JLabel("대기 인원수 : x");
		infoRoom = new JLabel("방 개수 : x");
		enterButton = new JButton("방 들어가기");
		createButton = new JButton("방 만들기");
		infoUser.setFont(new Font("Malgun Gothic", Font.PLAIN, 15));
		infoRoom.setFont(new Font("Malgun Gothic", Font.PLAIN, 15));
		enterButton.setFont(new Font("Malgun Gothic", Font.PLAIN, 15));
		createButton.setFont(new Font("Malgun Gothic", Font.PLAIN, 15));
		enterButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		createButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		
		userList = new List(10, false);
		roomList = new List(10, false);
		userList.setFont(new Font("Malgun Gothic", Font.PLAIN, 15));
		roomList.setFont(new Font("Malgun Gothic", Font.PLAIN, 15));
		userList.add("유저 1");
		userList.add("유저 2");
		userList.add("유저 3");
		roomList.add("방 1");
		roomList.add("방 2");
		roomList.add("방 3");
		
		f.add(leftPanel, BorderLayout.WEST);
		f.add(rightPanel, BorderLayout.CENTER);
		leftPanel.add(ta, BorderLayout.CENTER);
		leftPanel.add(tf, BorderLayout.SOUTH);
		rightPanel.add(rightNorthPanel, BorderLayout.NORTH);
		rightPanel.add(rightSouthPanel, BorderLayout.CENTER);
		rightPanel.add(southPanel, BorderLayout.SOUTH);
		rightNorthPanel.add(infoUser, BorderLayout.NORTH);
		rightNorthPanel.add(userList, BorderLayout.SOUTH);
		rightSouthPanel.add(infoRoom, BorderLayout.NORTH);
		rightSouthPanel.add(roomList, BorderLayout.SOUTH);
		rightSouthPanel.add(roomList, BorderLayout.CENTER);
		
		southPanel.add(enterButton, BorderLayout.NORTH);
		southPanel.add(createButton, BorderLayout.SOUTH);
		
		enterButton.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (enterButton.getText().equals("방 들어가기")) {
					if (roomList.getSelectedItem() == null) return;
					if (!roomList.getSelectedItem().endsWith("(대기중)")) {
						JOptionPane.showMessageDialog(null, "게임중인 방은 들어갈 수 없습니다.", "방 입장 불가", JOptionPane.ERROR_MESSAGE);
						return;
					}
					String roomNo = roomList.getSelectedItem(); //it will return "[number] ~~~~"
					String[] splitStr = roomNo.split(" ", 2); //it will return "[number]"
					roomNo = splitStr[0].substring(1,  splitStr[0].length() - 1); //it will return "number"
					AllMain2.sendCommand("@enterRoom "+roomNo);
				} else {
					enterButton.setText("방 들어가기");
					createButton.setEnabled(true);
					AllMain2.sendCommand("@exitRoom");
				}
				
			}
		});
		createButton.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				String roomTitle = "";
				try {
					do {
						roomTitle = JOptionPane.showInputDialog(null, "방 제목을 입력하세요 (3~15글자)\r\n알파벳 대소문자, 숫자, 밑줄, 띄어쓰기, 한글, #만 사용가능합니다").trim();
					} while (!roomTitle .matches("^[ㄱ-ㅎㅏ-ㅣ가-힣A-Za-z0-9_\\s#]{3,15}$"));
				} catch (NullPointerException ne) { //취소 버튼을 누른 경우
					return;
				}
				AllMain2.sendCommand("@createRoom "+roomTitle);
			}
		});
		
		f.setSize(800,400);
		f.setLocation(400,250);
		f.setVisible(true);
		tf.requestFocus();
	}	
	
	/* 이벤트 핸들러 */
	class EventHandler extends FocusAdapter implements ActionListener {
		public void actionPerformed(ActionEvent ae) {
			String msg = tf.getText();
			if (msg.equals("")) return;
			
			try {
				AllMain2.sendMsg(msg);
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
			tf.setText("");
		}
	}

	public void setVisible(boolean flg) {
		f.setVisible(flg);
	}
	public JFrame getFrame() {
		return f;
	}
	public TextArea getTextArea() {
		return ta;
	}
	public void command(String command) {
		System.out.println(command);
		try {
			String[] splitStrBlank = command.split(" ", 2);
			String prefix = splitStrBlank[0];
			String cmd = "";
			if (splitStrBlank.length == 2) cmd = splitStrBlank[1];
			if (prefix.equals("#setRoomList")) {
				String[] splitStr = cmd.split("\\\\");
				infoRoom.setText("방 개수 : "+splitStr[0]);
				roomList.removeAll();
				for (int i = 1; i < splitStr.length; i++) {
					roomList.add(splitStr[i]);
				}
			} else if (prefix.equals("#setUserList")) {
				String[] splitStr = cmd.split("\\\\");
				infoUser.setText("대기 인원 수 : "+splitStr[0]);
				userList.removeAll();
				for (int i = 1; i < splitStr.length; i++) {
					if (splitStr[i].equals(nickname)) {
						userList.add(splitStr[i] + " (나)");
					} else {
						userList.add(splitStr[i]);
					}
				}
			} else if (prefix.equals("#setRoomInfo")) { //방에 들어가는걸 성공
				//#setRoomInfo 방 번호\방 제목\방을 만든 사람\
				String[] splitStr = cmd.split("\\\\");
				infoRoom.setText("방 제목 : " + splitStr[1]);
				roomList.removeAll();
				roomList.add("방 번호 : " + splitStr[0]);
				roomList.add("방 제목 : " + splitStr[1]);
				roomList.add("방을 만든 사람 : " + splitStr[2]);
				createButton.setEnabled(false);
				enterButton.setText("방 나가기");
				
				if (splitStr[1].endsWith("#G")) {
					AllMain2.mainGame.flgGenius = true;
				} else {
					AllMain2.mainGame.flgGenius = false;
				}
				AllMain2.setVisibleWaitRoom(false);
				AllMain2.setVisibleMainGame(true);
			} else if (prefix.equals("#System:")) {
				ta.append(command+"\r\n");
			} else {
				System.out.println("Error: 존재하지 않는 명령어");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		WaitRoom room = new WaitRoom("WaitRoom");
		room.setVisible(true);
	}
	
}

