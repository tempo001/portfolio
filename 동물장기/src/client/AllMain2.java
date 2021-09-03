import java.awt.*;

import javax.swing.*;

public class AllMain2 {
	static String nickname;
	static String serverIp;
	static String serverPort;
	static WaitRoom waitRoom;
	static GameMain mainGame;
	static JFrame waitFrame;
	static JFrame mainFrame;
	static TextArea ta1; //mainGame
	static TextArea ta2; //waitRoom
	static Multichat chatClient;
	static boolean visibleMainGame;
	static boolean visibleWaitRoom;
	static String version = "v3.1.0";
	
	public static class Multichat extends MultichatClient2 {
		Multichat(String nickname, String serverIp, int serverPort) {
			super(nickname, serverIp, serverPort);
		}
		@Override
		public void receiveMsg(String msg) {
			if (visibleMainGame) {
				if (msg.startsWith("#")) {
					mainGame.command(msg);
				} else {
					ta1.append(msg+"\r\n");
				}
			}
			if (visibleWaitRoom) {
				if (msg.startsWith("#")) {
					waitRoom.command(msg);
				} else {
					ta2.append(msg+"\r\n");
				}
			}
		}
	}
	private static void init(String nick) {
		waitFrame.setTitle("대기실 - nickname: " + nickname);
		waitRoom.nickname = nick;
		
		mainGame = new GameMain("동물 장기 - nickname: " + nickname);
		mainFrame = mainGame.getFrame();
		mainFrame.setVisible(false);
		visibleMainGame = false;
		
		ta1 = mainGame.getTextArea();
		ta2 = waitRoom.getTextArea();
		
		Multichat.version = version;
		//chatClient = new Multichat(nick, "127.0.0.1", 7777); //133.130.110.149
		try {
			chatClient = new Multichat(nick, serverIp, Integer.parseInt(serverPort)); //133.130.110.149
		} catch (NumberFormatException e) {
			e.printStackTrace();
			JOptionPane.showConfirmDialog(null, "서버 포트가 숫자가 아닙니다", "ERROR", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		chatClient.startClient();
	}
	public static void main(String[] args) {
		waitRoom = new WaitRoom("대기실 - nickname: " + nickname);
		waitFrame = waitRoom.getFrame();
		waitFrame.setVisible(true);
		visibleWaitRoom = true;
		try {
			do {
				serverIp = JOptionPane.showInputDialog(waitFrame, "서버 아이피를 입력하세요").trim();
			} while (serverIp.equals(""));
			do {
				serverPort = JOptionPane.showInputDialog(waitFrame, "서버 포트를 입력하세요").trim();
			} while (!serverPort.matches("^[0-9]+$"));
			do {
				nickname = JOptionPane.showInputDialog(waitFrame, "대화명을 입력하세요 (3~15글자)\r\n알파벳 대소문자, 숫자, 밑줄, 한글만 사용가능합니다").trim();
			} while (!nickname.matches("^[ㄱ-ㅎㅏ-ㅣ가-힣A-Za-z0-9_]{3,15}$"));
		} catch (NullPointerException e) {
			System.exit(0);
		}
		
		init(nickname);
	}
	
	
	
	
	public static void setVisibleMainGame(boolean flg) {
		if (flg) {
			mainGame.initGame();
			mainGame.ta.setText("");
			mainGame.tf.setText("");
		}
		mainFrame.setVisible(flg);
		visibleMainGame = flg;
	}
	public static void setVisibleWaitRoom(boolean flg) {
		if (flg) {
			waitRoom.ta.setText("");
			waitRoom.tf.setText("");
		}
		waitFrame.setVisible(flg);
		visibleWaitRoom = flg;
	}
	public static void sendMsg(String msg) {
		chatClient.sendMsg(msg);
	}
	public static void sendCommand(String command) {
		if (command.startsWith("@")) {
			chatClient.sendCommand(command);
		}
	}
}
