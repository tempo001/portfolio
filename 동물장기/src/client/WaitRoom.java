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
		
		infoUser = new JLabel("´ë±â ÀÎ¿ø¼ö : x");
		infoRoom = new JLabel("¹æ °³¼ö : x");
		enterButton = new JButton("¹æ µé¾î°¡±â");
		createButton = new JButton("¹æ ¸¸µé±â");
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
		userList.add("À¯Àú 1");
		userList.add("À¯Àú 2");
		userList.add("À¯Àú 3");
		roomList.add("¹æ 1");
		roomList.add("¹æ 2");
		roomList.add("¹æ 3");
		
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
				if (enterButton.getText().equals("¹æ µé¾î°¡±â")) {
					if (roomList.getSelectedItem() == null) return;
					if (!roomList.getSelectedItem().endsWith("(´ë±âÁß)")) {
						JOptionPane.showMessageDialog(null, "°ÔÀÓÁßÀÎ ¹æÀº µé¾î°¥ ¼ö ¾ø½À´Ï´Ù.", "¹æ ÀÔÀå ºÒ°¡", JOptionPane.ERROR_MESSAGE);
						return;
					}
					String roomNo = roomList.getSelectedItem(); //it will return "[number] ~~~~"
					String[] splitStr = roomNo.split(" ", 2); //it will return "[number]"
					roomNo = splitStr[0].substring(1,  splitStr[0].length() - 1); //it will return "number"
					AllMain2.sendCommand("@enterRoom "+roomNo);
				} else {
					enterButton.setText("¹æ µé¾î°¡±â");
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
						roomTitle = JOptionPane.showInputDialog(null, "¹æ Á¦¸ñÀ» ÀÔ·ÂÇÏ¼¼¿ä (3~15±ÛÀÚ)\r\n¾ËÆÄºª ´ë¼Ò¹®ÀÚ, ¼ýÀÚ, ¹ØÁÙ, ¶ç¾î¾²±â, ÇÑ±Û, #¸¸ »ç¿ë°¡´ÉÇÕ´Ï´Ù").trim();
					} while (!roomTitle .matches("^[¤¡-¤¾¤¿-¤Ó°¡-ÆRA-Za-z0-9_\\s#]{3,15}$"));
				} catch (NullPointerException ne) { //Ãë¼Ò ¹öÆ°À» ´©¸¥ °æ¿ì
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
	
	/* ÀÌº¥Æ® ÇÚµé·¯ */
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
				infoRoom.setText("¹æ °³¼ö : "+splitStr[0]);
				roomList.removeAll();
				for (int i = 1; i < splitStr.length; i++) {
					roomList.add(splitStr[i]);
				}
			} else if (prefix.equals("#setUserList")) {
				String[] splitStr = cmd.split("\\\\");
				infoUser.setText("´ë±â ÀÎ¿ø ¼ö : "+splitStr[0]);
				userList.removeAll();
				for (int i = 1; i < splitStr.length; i++) {
					if (splitStr[i].equals(nickname)) {
						userList.add(splitStr[i] + " (³ª)");
					} else {
						userList.add(splitStr[i]);
					}
				}
			} else if (prefix.equals("#setRoomInfo")) { //¹æ¿¡ µé¾î°¡´Â°É ¼º°ø
				//#setRoomInfo ¹æ ¹øÈ£\¹æ Á¦¸ñ\¹æÀ» ¸¸µç »ç¶÷\
				String[] splitStr = cmd.split("\\\\");
				infoRoom.setText("¹æ Á¦¸ñ : " + splitStr[1]);
				roomList.removeAll();
				roomList.add("¹æ ¹øÈ£ : " + splitStr[0]);
				roomList.add("¹æ Á¦¸ñ : " + splitStr[1]);
				roomList.add("¹æÀ» ¸¸µç »ç¶÷ : " + splitStr[2]);
				createButton.setEnabled(false);
				enterButton.setText("¹æ ³ª°¡±â");
				
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
				System.out.println("Error: Á¸ÀçÇÏÁö ¾Ê´Â ¸í·É¾î");
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

