import java.net.*;
import java.io.*;

import javax.swing.JOptionPane;

public class MultichatClient2 {
	String nickname = "";
	String serverIp = "";
	int serverPort = 0;
	static String version = "v1.0";
	
	Socket socket = null;
	ClientSender sender = null;
	ClientReceiver receiver = null;
	
	MultichatClient2(String nickname, String serverIp, int serverPort) {
		this.nickname = nickname;
		this.serverIp = serverIp;
		this.serverPort = serverPort;
	}
	
	void startClient() {
		try {
			socket = new Socket(serverIp, serverPort);
			
			sender = new ClientSender(socket, nickname);
			receiver = new ClientReceiver(socket);
			
			sender.start();
			receiver.start();
		} catch (ConnectException ce) {
			ce.printStackTrace();
			JOptionPane.showMessageDialog(null, "ConnectException: 서버와 연결할 수 없습니다.", 
					"Cannot connet to server", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Error", 
					"Cannot connet to server", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}
	
	class ClientSender extends Thread {
		Socket socket = null;
		DataOutputStream out = null;
		String name = "";
		
		ClientSender(Socket socket, String name) {
			this.socket = socket;
			try {
				out = new DataOutputStream(socket.getOutputStream());
				this.name = name;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public void run() {
			try {
				if (out != null) {
					out.writeUTF(name + "\\" + version);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public void sendMsg(String msg) {
			try {
				if (out != null) out.writeUTF(name+">"+msg);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		public void sendCommand(String command) {
			try {
				if (out != null) out.writeUTF(command);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	class ClientReceiver extends Thread {
		Socket socket;
		DataInputStream in;
		
		ClientReceiver(Socket socket) {
			this.socket = socket;
			try {
				in = new DataInputStream(socket.getInputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public void run() {
			while (in != null) {
				try {
					String msg = in.readUTF();
					receiveMsg(msg);
				} catch (IOException e) {
					//e.printStackTrace();
					JOptionPane.showMessageDialog(null, "서버와의 접속이 끊어졌습니다", 
						"Disconnet to server", JOptionPane.ERROR_MESSAGE);
					System.exit(0);
					break;
				}
			}
		}
	}
	
	
	public void sendMsg(String msg) {
		if (!msg.startsWith("@") && !msg.startsWith("#")) {
			sender.sendMsg(msg);
		}
	}
	public void sendCommand(String command) {
		if (command.startsWith("@")) {
			System.out.println(command);
			sender.sendCommand(command);
		}
	}
	public void receiveMsg(String msg) {
		System.out.println(msg);
	}
}
