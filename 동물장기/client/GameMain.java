import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;

public class GameMain {
	String nickname;
	String player1;
	String player2;
	public static final String PIECE_CHICK = "c1";
	public static final String PIECE_CHICKEN = "c2";
	public static final String PIECE_ELEPHANT = "e";
	public static final String PIECE_GIRAFFE = "g";
	public static final String PIECE_LION = "l";
	
	public enum GameState {
		WAITING, PLAYING, WIN, LOSE, DRAW;
	}
	int prisonerPos;
	int[] mapMySide = new int[8];
	int[] mapOpSide = new int[8];
	Piece[] piece = new Piece[9];
	int[][] map = new int[4][3];
	JLabel[] imgLabel = new JLabel[12];
	JLabel[] imgLabelSide = new JLabel[8];
	int clickFlg = 0;
	//int cheat = 0;
	int orgX, orgY;
	GameState state;
	boolean itsMyTurn;
	boolean iamObserver;
	int myLionStay;
	int opLionStay;
	String fileExtension;
	boolean flgGenius = false;
	PiecePrevPosition prevPosition = new PiecePrevPosition(); //���º����� �˻��ϱ� ���� (�������°� 3ȸ �̻� ���ӵǸ� ���º�)
	
	JFrame f;
	JPanel centerPanel;
	JPanel sideMyPanel;
	JPanel sideOpPanel;
	BoardPanel mainPanel;
	JPanel belowPanel;
	JLabel lblOpNickname;
	JLabel lblMyNickname;
	JButton startExitButton;
	JButton readyButton;
	TextArea ta;
	JTextField tf;
	Box box;
	JLabel infoUser;
	List userList;
	
	
	public void initGame() {
		System.out.println("ȭ��, ���� �ʱ�ȭ");
		state = GameState.WAITING;
		mainPanel.removeAll();
		sideMyPanel.removeAll();
		sideOpPanel.removeAll();
		readyButton.setText("Ready");
		readyButton.setEnabled(true);
		userList.removeAll();
		lblMyNickname.setText("�ڽ��� �г���: " + nickname);
		lblOpNickname.setText("���� �г���");
		myLionStay = 0;
		opLionStay = 0;
		iamObserver = false;
		prevPosition.reset();
		
		int j = 0;
		for (int  i = 0; i < 12; i++) {
			String fName = "";
			switch (i) {
			case 0:
			case 11: fName = PIECE_GIRAFFE; break;
			case 1:
			case 10: fName = PIECE_LION; break;
			case 2:
			case 9: fName = PIECE_ELEPHANT; break;
			case 4:
			case 7: fName = PIECE_CHICK; break;
			}
			
			if (!fName.equals("")) {
				map[i/3][i%3] = j;
				piece[j] = new Piece(i/3, i%3, (i < 6) ? false : true, fName, true);
				j++;
			} else {
				map[i/3][i%3] = 8;
			}
			if (i < 6) {
				fName += "_2";
			}
			imgLabel[i] = new JLabel(new ImageIcon("img/img_"+fName+fileExtension));
			imgLabel[i].setLocation(100, 100);
			imgLabel[i].setSize(85, 85);
			mainPanel.add(imgLabel[i]);
		}
		piece[8] = new Piece(-1, -1, false, "", false);
		
		for(int i = 0; i < 8; i++) {
			mapMySide[i] = 8;
			mapOpSide[i] = 8;
		}
		updateSide();
	}
	
	public GameMain(String title) {
		nickname = AllMain2.nickname;
		fileExtension = ".jpg";
		
		f = new JFrame(title);
		f.setSize(750, 700);
		f.setLocation(400, 100);
		f.setLayout(new BorderLayout());
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
		mainPanel = new BoardPanel();
		mainPanel.setPreferredSize(new Dimension(300, 400));
		mainPanel.setMaximumSize(new Dimension(300, 400));
		mainPanel.setMinimumSize(new Dimension(300, 400));
		mainPanel.setLayout(new GridLayout(4,3));
		mainPanel.setBackground(Color.green);
		mainPanel.setSize(300,400);
		mainPanel.setLocation(50,50);
		mainPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		
		sideMyPanel = new JPanel();
		sideMyPanel.setLayout(new GridLayout(4, 2));
		sideMyPanel.setPreferredSize(new Dimension(200, 400));
		sideMyPanel.setMaximumSize(new Dimension(200, 400));
		sideMyPanel.setMinimumSize(new Dimension(200, 400));
		sideMyPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

		sideOpPanel = new JPanel();
		sideOpPanel.setLayout(new GridLayout(4, 2));
		sideOpPanel.setPreferredSize(new Dimension(200, 400));
		sideOpPanel.setMaximumSize(new Dimension(200, 400));
		sideOpPanel.setMinimumSize(new Dimension(200, 400));
		sideOpPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		
		centerPanel = new JPanel();
		centerPanel.setLayout(new BorderLayout());
		centerPanel.add(sideMyPanel, BorderLayout.EAST);
		centerPanel.add(sideOpPanel, BorderLayout.WEST);
		centerPanel.add(mainPanel, BorderLayout.CENTER);
		centerPanel.setPreferredSize(new Dimension(700, 400));
		centerPanel.setMaximumSize(new Dimension(700, 400));
		centerPanel.setMinimumSize(new Dimension(700, 400));
		
		belowPanel = new JPanel();
		belowPanel.setLayout(new GridLayout(1, 2));
		
		lblOpNickname = new JLabel();
		lblOpNickname.setText("���� �г���");
		lblOpNickname.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		lblOpNickname.setFont(new Font("Malgun Gothic", Font.BOLD, 20));
		
		lblMyNickname = new JLabel();
		lblMyNickname.setText("�ڽ��� �г���: " + nickname);
		lblMyNickname.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		lblMyNickname.setFont(new Font("Malgun Gothic", Font.BOLD, 20));
		
		ta = new TextArea("", 4, 10, TextArea.SCROLLBARS_VERTICAL_ONLY);
		ta.setFont(new Font("Malgun Gothic", Font.PLAIN, 15));
		ta.setEditable(false);
		tf = new JTextField();
		tf.setFont(new Font("Malgun Gothic", Font.PLAIN, 15));
		
		EventHandler handler = new EventHandler();
		tf.addActionListener(handler);
		
		startExitButton = new JButton("�� ������");
		startExitButton.setFont(new Font("Malgun Gothic", Font.BOLD, 15));
		startExitButton.setBackground(Color.LIGHT_GRAY);
		startExitButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		
		readyButton = new JButton("Ready");
		readyButton.setFont(new Font("Malgun Gothic", Font.BOLD, 15));
		readyButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		
		box = new Box(BoxLayout.Y_AXIS);
		box.setBackground(Color.black);
        box.add(Box.createVerticalGlue());
		box.add(lblOpNickname);
        box.add(centerPanel);
        box.add(lblMyNickname);
        box.add(Box.createVerticalGlue());
		
		JPanel buttonPanel;
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(2, 1));
		buttonPanel.add(startExitButton);
		buttonPanel.add(readyButton);
		
		infoUser = new JLabel("���� �� : x");
		infoUser.setFont(new Font("Malgun Gothic", Font.BOLD, 15));
		userList = new List();
		userList.setFont(new Font("Malgun Gothic", Font.PLAIN, 15));
		userList.add("���� 1");
		userList.add("���� 2");
		userList.add("���� 3");
		
		JPanel userPanel;
		userPanel = new JPanel();
		userPanel.setLayout(new BorderLayout());
		userPanel.add(infoUser, BorderLayout.NORTH);
		userPanel.add(userList, BorderLayout.CENTER);
		
		JPanel belowLeftPanel = new JPanel();
		belowLeftPanel.setLayout(new BorderLayout());
		belowLeftPanel.add(buttonPanel, BorderLayout.NORTH);
		belowLeftPanel.add(ta, BorderLayout.CENTER);
		belowLeftPanel.add(tf, BorderLayout.SOUTH);
		
		belowPanel.add(belowLeftPanel);
		belowPanel.add(userPanel);
		belowPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		
		initGame();
		
		f.add(box, BorderLayout.CENTER);
		f.add(belowPanel, BorderLayout.SOUTH);
		f.setBackground(Color.gray);
		f.setMinimumSize(new Dimension(720,650));
		f.pack();
		
		/* ���콺 �̺�Ʈ ó�� */
		mainPanel.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (state == GameState.WAITING) return;
				if (!itsMyTurn || iamObserver) return;
				int mouseX = e.getX();
	            int mouseY = e.getY();
	            int row = mouseY / 100;
	            int col = mouseX / 100;
	            //System.out.println("row="+row+" col="+col);
	            /* ��¿� */
	            if (map[row][col] != 8) {
	            	//System.out.println(piece[map[row][col]]);
	            } else {
	            	//System.out.println("��� �ִ� ĭ");
	            }
	            /* ���� �̵��� ��ġ�� �����ϰ� move �޼ҵ� ȣ�� */
	        	if (clickFlg == 0 && piece[map[row][col]].own && piece[map[row][col]].onBoard && movePossibleNum(row, col) > 0) {
	        		//System.out.println("�̵��� �� ����: "+row+" "+col);
	        		orgX = row;
	        		orgY = col;
	        		clickFlg = 1;
	        		mainPanel.revalidate();
	        		mainPanel.repaint();
	        	} else if (clickFlg == 1) {
	        		if (orgX == row && orgY == col) {
	        			//System.out.println("�̵��� �� ���� ���");
	        			clickFlg = 0;
	        		} else {
	            		//System.out.println("�̵� ��� ��ġ ����: "+row+" "+col);
	            		boolean result = move(orgX, orgY, row, col);
	            		if (result) {
	            			//System.out.println("���������� �̵�");
	            		} else {
	            			//System.out.println("�̵� ����");
	            		}
	            		clickFlg = 0;
	        		}
	        		mainPanel.revalidate();
	        		mainPanel.repaint();
	        	} else if (clickFlg == 2) {
	        		if (!piece[map[row][col]].onBoard && (flgGenius && row != 0 || !flgGenius)) {
	        			summonPiece(prisonerPos, row, col);
	        		}
	        		clickFlg = 0;
	        		mainPanel.revalidate();
	        		mainPanel.repaint();
	        	}
			}
		});
		startExitButton.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (state == GameState.PLAYING && !iamObserver) {
					int opt = JOptionPane.showConfirmDialog(null, "���� �߿� �����ø� ��� ó���˴ϴ�.\r\n�׷��� �����ðڽ��ϱ�?", "Ȯ��â", JOptionPane.YES_NO_OPTION);
					if (opt != JOptionPane.YES_OPTION) {
						return;
					} else {
						AllMain2.sendCommand("@iLose");
						Object[] option = {"OK"};
						JOptionPane.showOptionDialog(null, "��� ó���Ǿ����ϴ�", "���", JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE, null, option, option[0]);
					}
				}
				tf.requestFocus();
				AllMain2.waitRoom.enterButton.setText("�� ����");
				AllMain2.waitRoom.createButton.setEnabled(true);
				AllMain2.setVisibleMainGame(false);
				AllMain2.setVisibleWaitRoom(true);
				AllMain2.sendCommand("@exitRoom");
			}
		});
		sideMyPanel.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (state == GameState.WAITING) return;
				if (!itsMyTurn || iamObserver) return;
				int mouseX = e.getX();
	            int mouseY = e.getY();
	            int row = mouseY / 100;
	            int col = mouseX / 100;
	            if (clickFlg == 0 && mapMySide[7 - (row * 2 + col)] != 8) {
	            	prisonerPos = 7 - (row * 2 + col);
	            	clickFlg = 2;
	            }
	            else {
	            	clickFlg = 0;
	            }
	            mainPanel.revalidate();
            	mainPanel.repaint();
			}
		});
		readyButton.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (state == GameState.WAITING) {
					readyButton.setEnabled(false);
					AllMain2.sendCommand("@iamReady");
				}
			}
		});
	}
	
	
	public boolean move(int x, int y, int x2, int y2) {
		System.out.println(x+","+y+"->"+x2+","+y2+"   "+piece[map[x][y]]+" => "+piece[map[x2][y2]]);
		if (!moveValid(x, y, x2, y2)) return false;
		//�̵� ���� ���� ��ġ�� �� ���� �־���ϰ�, �ű� ���� �� ���� �ְ� �� �����϶�
		if (piece[map[x][y]].own && piece[map[x][y]].onBoard) {
			if (piece[map[x2][y2]].onBoard) {	//1) �̵��� ��ġ�� ���� �ִ� ���
				if (piece[map[x2][y2]].own) {	//1-1) �� ���̶�� �̵� �Ұ�
					System.out.println("�� ���� ��� �̵� �Ұ�");
					return false;
				} else {					//1-2)���� ���̸� ���� ���� ��� �̵�
					catchPiece(x2, y2);
					prevPosition.drawCnt = 0;
				}
			} else {						//2) �̵��� ��ġ�� ���� ���� ���
				if (map[x][y] == prevPosition.myPieceNo && x2 == prevPosition.myX && y2 == prevPosition.myY) {
					prevPosition.drawCnt++;
					//System.out.println("my++" + prevPosition.drawCnt);
				}
			}					
			//������ ���� �̵�
			piece[map[x][y]].x = x2;
			piece[map[x][y]].y = y2;
			imgLabel[x2*3 + y2].setIcon(new ImageIcon("img/img_"+piece[map[x][y]].kind+fileExtension));
			imgLabel[x*3 + y].setIcon(new ImageIcon(""));
			map[x2][y2] = map[x][y];
			map[x][y] = 8;
			if (!iamObserver) AllMain2.sendCommand("@moveMyPiece "+x+" "+y+" "+x2+" "+y2);
			//
			prevPosition.myX = x;
			prevPosition.myY = y;
			prevPosition.myPieceNo = map[x2][y2];
		} else if (!piece[map[x][y]].own && piece[map[x][y]].onBoard) {
			if (piece[map[x2][y2]].onBoard) {	//1) �̵��� ��ġ�� ���� �ִ� ���
				if (!piece[map[x2][y2]].own) {	//1-1) ���� �������� ���
					System.out.println("���� �������� ���");
					return false;
				} else {					//1-2) �����ڰ� �ٸ��� ���� ��� �̵�
					catchPiece(x2, y2);
					prevPosition.drawCnt = 0;
				}
			} else {						//2) �̵��� ��ġ�� ���� ���� ���
				if (map[x][y] == prevPosition.opPieceNo && x2 == prevPosition.opX && y2 == prevPosition.opY) {
					prevPosition.drawCnt++;
					//System.out.println("op++" + prevPosition.drawCnt);
				}
			}
			//������ ���� �̵�
			piece[map[x][y]].x = x2;
			piece[map[x][y]].y = y2;
			imgLabel[x2*3 + y2].setIcon(new ImageIcon("img/img_"+piece[map[x][y]].kind+"_2"+fileExtension));
			imgLabel[x*3 + y].setIcon(new ImageIcon(""));
			map[x2][y2] = map[x][y];
			map[x][y] = 8;
			//
			prevPosition.opX = x;
			prevPosition.opY = y;
			prevPosition.opPieceNo = map[x2][y2];
		}
		
		toggleTurn();
		//���� ���¸� ������Ʈ (�����, ������, �¸�, �й�, ���º�)
		updateGameState();
		return true;
	}


	
	//������ ���� ��´� (�ڽ��� ���� �̵� ���� �ʰ�, ������ ���� �ǹ����� �������� �� ������ �Ѵ�)
	//move �޼ҵ忡���� ȣ�� ���� (move �޼ҵ忡�� valid check�� �ϹǷ� ���� valid check �� �ʿ� ����)
	public void catchPiece(int x2, int y2) {
		if (piece[map[x2][y2]].own) { //������ �ڽ��� ���� ��� ���
			for (int i = 0; i < 8; i++) {
				if (mapOpSide[i] == 8) {
					mapOpSide[i] = map[x2][y2];
					break;
				}
			}
		} else { //�ڽ��� ������ ���� ��� ���
			for (int i = 0; i < 8; i++) {
				if (mapMySide[i] == 8) {
					mapMySide[i] = map[x2][y2];
					break;
				}
			}
		}
		updateSide();
		
		piece[map[x2][y2]].onBoard = false;
		piece[map[x2][y2]].own = !piece[map[x2][y2]].own;
		if (piece[map[x2][y2]].kind.equals(PIECE_CHICKEN)) piece[map[x2][y2]].kind = PIECE_CHICK;
		map[x2][y2] = 8;
		imgLabel[x2*3 + y2].setIcon(new ImageIcon(""));
	}
	
	public void summonPiece(int pos, int x, int y) {
		prevPosition.drawCnt = 0; //�ڽ��̵� ����̵� ���� ���� ��ȯ�ϸ� 0���� ����
		if (pos < 8) { //�ڽ��� ���� ���� �� ���� �÷�����
			String kind = piece[mapMySide[pos]].kind;
			map[x][y] = mapMySide[pos];
			for (int i = pos; i < 7; i++) {
				mapMySide[i] = mapMySide[i+1];
			}
			piece[map[x][y]].own = true;
			imgLabel[x*3 + y].setIcon(new ImageIcon("img/img_"+kind+fileExtension));
			if (!iamObserver) AllMain2.sendCommand("@summonMyPiece " + pos + " " + x + " " + y);
		} else { //������ ���� ���� �� ���� �÷�����
			pos = pos - 8;
			String kind = piece[mapOpSide[pos]].kind;
			map[x][y] = mapOpSide[pos];
			for (int i = pos; i < 7; i++) {
				mapOpSide[i] = mapOpSide[i+1]; 
			}
			piece[map[x][y]].own = false;
			imgLabel[x*3 + y].setIcon(new ImageIcon("img/img_"+kind+"_2"+fileExtension));
		}
		piece[map[x][y]].onBoard = true;
		piece[map[x][y]].x = x;
		piece[map[x][y]].y = y;
		updateGameState(); //�̰� ���ָ� ���Ƹ��� �ǳ��ٿ� ��ȯ������ �ٷ� ������ ������ ���� 
		toggleTurn();
	}
	
	//�̵��� ���� ������ ������ ��ġ�� �̵� �������� �˻� (���� ������ ���� �̵��� �� �ִ� ��ġ�� �ٸ��� ����)
	public boolean moveValid(int x, int y, int x2, int y2) {
		if (!isValidPos(x, y) || !isValidPos(x2, y2)) return false;
		String kind = piece[map[x][y]].kind;
		boolean result = false;
		if (!piece[map[x][y]].own) {
			x = 3 - x;
			x2 = 3 - x2;
			y = 2 - y;
			y2 = 2 - y2;
		}
		if (kind.equals(PIECE_CHICK)) {
			if (x - x2 == 1 && y == y2) {
				result = true;
			}
		} else if (kind.equals(PIECE_CHICKEN)) {
			if ( ( Math.abs(x - x2) <= 1 && Math.abs(y - y2) <= 1 )
					&& !( x2 - x == 1 && Math.abs(y2 - y) == 1 ) ) {
				result = true;
			}
		} else if (kind.equals(PIECE_ELEPHANT)) {
			if (Math.abs(x - x2) == 1 && Math.abs(y - y2) == 1) {
				result = true;
			}
		} else if (kind.equals(PIECE_GIRAFFE)) {
			if ( ( Math.abs(x - x2) == 1 && Math.abs(y - y2) == 0 ) 
					|| ( Math.abs(x - x2) == 0 && Math.abs(y - y2) == 1 ) ) {
				result = true;
			}
		} else if (kind.equals(PIECE_LION)) {
			if (Math.abs(x - x2) <= 1 && Math.abs(y - y2) <= 1) {
				result = true;
			}
		}
		return result;
	}
	
	//������ ���¸� ������Ʈ (�Ͻ�����, ���� ������, �¸�, �й�, ���º�) 
	public void updateGameState() {
		//������ ���ڰ� �� ���� ������
		if (!piece[1].onBoard) {
			state = GameState.WIN;
		}
		//�ڽ��� ���ڰ� �� ���� ������
		if (!piece[6].onBoard) {
			state = GameState.LOSE;
		}
		//������ ���ڰ� �� ������ ������ ���
		if (piece[1].x == 3 && piece[1].onBoard) { 
			opLionStay++;
			if (opLionStay == 2) {
				state = GameState.LOSE;
			}
		} else {
			opLionStay = 0;
		}
		//�ڽ��� ���ڰ� ���� ������ ������ ���
		if (piece[6].x == 0 && piece[6].onBoard) { 
			myLionStay++;
			if (myLionStay == 2) {
				state = GameState.WIN;
			}
		} else {
			myLionStay = 0;
		}
		//3�� �̻� �������°� ����Ǵ� ��� ���º�
		if (prevPosition.drawCnt >= 6) {
			state = GameState.DRAW;
		}
		
		//���Ƹ��� ��� ���� ���� ������ ����
		for (int i = 3; i <= 4; i++) {
			if (piece[i].x == 0 && piece[i].own && piece[i].onBoard && piece[i].kind.equals(PIECE_CHICK)) {
				System.out.println("���Ƹ��� ������ ��ȯ - �ڽ�");
				int x = piece[i].x;
				int y = piece[i].y;
				piece[i].kind = PIECE_CHICKEN;
				imgLabel[x*3 + y].setIcon(new ImageIcon("img/img_"+PIECE_CHICKEN+fileExtension));
			}
			if (piece[i].x == 3 && !piece[i].own && piece[i].onBoard && piece[i].kind.equals(PIECE_CHICK)) {
				System.out.println("���Ƹ��� ������ ��ȯ - ���");
				int x = piece[i].x;
				int y = piece[i].y;
				piece[i].kind = PIECE_CHICKEN;
				imgLabel[x*3 + y].setIcon(new ImageIcon("img/img_"+PIECE_CHICKEN+"_2"+fileExtension));
			}
		}
		
		if (state == GameState.PLAYING) {
			
		} else if (state == GameState.WAITING) {
			
		} else if (state == GameState.WIN) {
			if (!iamObserver) {
				AllMain2.sendCommand("@iWin");
			}
		} else if (state == GameState.LOSE) {
			if (!iamObserver) {
				AllMain2.sendCommand("@iLose");
			}
		} else if (state == GameState.DRAW) {
			if (!iamObserver) {
				AllMain2.sendCommand("@iDraw");
			}
		}
	}
	
	//�ش� ��ġ�� ��� �˻�
	public boolean isValidPos(int x, int y) {
		return (0 <= x && x <= 3 && 0 <= y && y <= 2);
	}
	
	//�ش� ��ġ���� �̵��� �� �ִ� ����� ��
	public int movePossibleNum(int x, int y) {
		int cnt = 0;
		for (int i = -1; i <= 1; i++) {
    		for (int j = -1; j <= 1; j++) {
    			if (isValidPos(x + i, y + j) 
    					&& moveValid(x, y, x + i, y + j)
    					&& !piece[map[x+i][y+j]].own) {
    				cnt++;
    			}
    		}
    	}
		return cnt;
	}
	
	@SuppressWarnings("serial")
	public class BoardPanel extends JPanel {
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			BufferedImage img = null;
			try {
				img = ImageIO.read(new File("img/background.png"));
			} catch (IOException e) {
				e.printStackTrace();
			}
			g.drawImage(img, 0, 0, null);
		}
		public void paint(Graphics g) {
			super.paint(g);
	        
	        Graphics2D g2=(Graphics2D)g;
	        g2.setColor(Color.RED);
	        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
	        if (clickFlg == 1) {
	        	//����Ʈ�� �ʱ�ȭ
	        	for (int i = 0; i < 4; i++) {
					for (int j = 0; j < 3; j++) {
						imgLabel[i*3 + j].setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					}
				}
	        	for (int i = -1; i <= 1; i++) {
	        		for (int j = -1; j <= 1; j++) {
	        			if (moveValid(orgX, orgY, orgX + i, orgY + j)
	        					&& !piece[map[orgX+i][orgY+j]].own) {
	        				g2.fill(new Ellipse2D.Float((orgY+j)*100+10,(orgX+i)*100+10,80,80));
	        				imgLabel[(orgX+i) * 3 + (orgY + j)].setCursor(new Cursor(Cursor.HAND_CURSOR));
	        			}
	        			
	        		}
	        	}
	        } else if (clickFlg == 0) {
	        	for (int i = 0; i < 4; i++) {
					for (int j = 0; j < 3; j++) {
						if (piece[map[i][j]].own && piece[map[i][j]].onBoard && movePossibleNum(i, j) > 0 && state != GameState.WAITING && itsMyTurn && !iamObserver) {
							imgLabel[i*3 + j].setCursor(new Cursor(Cursor.HAND_CURSOR));
						} else {
							imgLabel[i*3 + j].setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
						}
					}
				}
	        } else if (clickFlg == 2) {
	        	for (int i = 0; i < 4; i++) {
					for (int j = 0; j < 3; j++) {
						if (!piece[map[i][j]].onBoard && ((flgGenius && i != 0) || !flgGenius) && state != GameState.WAITING && itsMyTurn && !iamObserver) {
							g2.fill(new Ellipse2D.Float(j * 100 + 10, i * 100 + 10, 80, 80));
							imgLabel[i*3 + j].setCursor(new Cursor(Cursor.HAND_CURSOR));
						} else {
							imgLabel[i*3 + j].setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
						}
					}
				}
	        }
	        g2.setColor(Color.BLUE);
	        /*if (cheat == 1) {
	        	int x, y;
	        	for (int k = 0; k < 8; k++) {
	        		if (!piece[k].own && piece[k].onBoard) {
		        		x = piece[k].x;
		        		y = piece[k].y;
			        	for (int i = -1; i <= 1; i++) {
			        		for (int j = -1; j <= 1; j++) {
			        			if (moveValid(x, y, x + i, y + j)) {
			        				g2.fill(new Ellipse2D.Float((y+j)*100+10,(x+i)*100+10,80,80));
			        			}
			        		}
			        	}
	        		}
	        	}
	        }*/
		}
	}
	
	/* �̺�Ʈ �ڵ鷯 */
	class EventHandler extends FocusAdapter implements ActionListener {
		public void actionPerformed(ActionEvent ae) {
			String msg = tf.getText();
			if (msg.equals("")) return;
			/*if (msg.equals("cheat")) {
				cheat = (cheat == 0) ? 1 : 0;
				System.out.println("cheat: "+cheat);
				mainPanel.revalidate();
				mainPanel.repaint();
			}*/
			if (msg.equals("init")) {
				initGame();
			}
			if (msg.equals("#skin")) {
				if(fileExtension.equals(".jpg")) fileExtension = ".png";
				else fileExtension = ".jpg";
				mainPanel.removeAll();
				for (int i=0;i<12;i++) {
					String path = ((ImageIcon) imgLabel[i].getIcon()).getDescription();
					if (path.length() > 4) path = path.substring(0, path.length() - 4);
					imgLabel[i] = new JLabel(new ImageIcon(path + fileExtension));
					mainPanel.add(imgLabel[i]);
				}
				mainPanel.revalidate();
				mainPanel.repaint();
				updateSide();
			}
			try {
				AllMain2.sendMsg(msg);
			} catch (NullPointerException e) {}
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
		String[] splitStrBlank = command.split(" ", 2);
		String prefix = splitStrBlank[0];
		String cmd = "";
		if (splitStrBlank.length == 2) cmd = splitStrBlank[1];
		if (prefix.equals("#moveOpPiece")) {
			String[] splitStr = cmd.split(" ");
			int x = Integer.parseInt(splitStr[0]);
			int y = Integer.parseInt(splitStr[1]);
			int x2 = Integer.parseInt(splitStr[2]);
			int y2 = Integer.parseInt(splitStr[3]);
			if (!iamObserver || !itsMyTurn) {
				x = 3 - x;
				y = 2 - y;
				x2 = 3 - x2;
				y2 = 2 - y2;
			}
			move(x, y, x2, y2);
		} else if (prefix.equals("#summonOpPiece")) {
			String[] splitStr = cmd.split(" ");
			int pos = Integer.parseInt(splitStr[0]);
			int x = Integer.parseInt(splitStr[1]);
			int y = Integer.parseInt(splitStr[2]);
			if (!iamObserver || !itsMyTurn) {
				pos = pos + 8;
				x = 3 - x;
				y = 2 - y;
			}
			summonPiece(pos, x, y);
		} else if (prefix.equals("#setUserList")) {
			String[] splitStr = cmd.split("\\\\");
			infoUser.setText("���� �� : "+splitStr[0]);
			userList.removeAll();
			for (int i = 1; i < splitStr.length; i++) {
				if (splitStr[i].equals(nickname)) {
					userList.add(splitStr[i] + " (��)");
				} else {
					userList.add(splitStr[i]);
				}
			}
		} else if (prefix.equals("#gameStart")) {
			String[] splitStr = cmd.split("\\\\");
			iamObserver = false;
			if (nickname.equals(splitStr[0])) {
				player1 = splitStr[0];
				player2 = splitStr[1];
				itsMyTurn = true;
			} else {
				player1 = splitStr[1];
				player2 = splitStr[0];
				itsMyTurn = false;
			}
			if (!nickname.equals(splitStr[0]) && !nickname.equals(splitStr[1])) {
				//�������� ���
				iamObserver = true;
				player1 = splitStr[0];
				player2 = splitStr[1];
				itsMyTurn = true;
				refreshTurn();
			} else {
				refreshTurn();
			}
			state = GameState.PLAYING;
		} else if (prefix.equals("#readyPlayer")) {
			String[] splitStr = cmd.split("\\\\");
			String[] userNicks = userList.getItems();
			userList.removeAll();
			for (int j = 0; j < userNicks.length; j++) {
				boolean flg = true;
				for (int i = 0; i < splitStr.length; i++) {
					if (userNicks[j].equals(splitStr[i]) || userNicks[j].equals(splitStr[i]+" (��)")) {
						userList.add(userNicks[j] + " (Ready)");
						flg = false;
					}
				}
				if (flg) userList.add(userNicks[j]);
			}
		} else if (prefix.equals("#GameOver")) {
			String[] splitStr = cmd.split("\\\\");
			String winner = splitStr[0];
			String loser = splitStr[1];
			Object[] option = {"OK"};
			if (nickname.equals(winner)) {
				JOptionPane.showOptionDialog(null, "\"" + winner + "\" YOU WIN", nickname + " �¸�", JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE, null, option, option[0]);
			} else if (nickname.equals(loser)) {
				JOptionPane.showOptionDialog(null, "\"" + loser + "\" YOU LOSE", nickname + " �й�", JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE, null, option, option[0]);
			} else {
				//�������� ���
			}
			initGame();
			state = GameState.WAITING;
		} else if (prefix.equals("#GameOverDraw")) {
			String[] splitStr = cmd.split("\\\\");
			String ply1 = splitStr[0];
			String ply2 = splitStr[1];
			Object[] option = {"OK"};
			if (nickname.equals(ply1)) {
				JOptionPane.showOptionDialog(null, "DRAW", nickname + " ���º�", JOptionPane.OK_OPTION, JOptionPane.QUESTION_MESSAGE, null, option, option[0]);
			} else if (nickname.equals(ply2)) {
				JOptionPane.showOptionDialog(null, "DRAW", nickname + " ���º�", JOptionPane.OK_OPTION, JOptionPane.QUESTION_MESSAGE, null, option, option[0]);
			} else {
				//�������� ���
			}
			initGame();
			state = GameState.WAITING;
		}
	}
	public void updateSide() {
		System.out.println("[updateSide] itsMyTurn: "+itsMyTurn+", iamObserver: "+iamObserver);
		sideMyPanel.removeAll();
		sideOpPanel.removeAll();
		for (int i = 0; i < 8; i++) {
			int temp = mapMySide[7-i];
			int temp2 = mapOpSide[i];
			String fName = "";
			switch (temp) {
			case 0:
			case 7: fName = PIECE_GIRAFFE; break;
			case 1:
			case 6: fName = PIECE_LION; break;
			case 2:
			case 5: fName = PIECE_ELEPHANT; break;
			case 3:
			case 4: fName = PIECE_CHICK; break;
			}
			imgLabelSide[i] = new JLabel(new ImageIcon("img/img_"+fName+fileExtension));
			if(!fName.equals("") && itsMyTurn && !iamObserver && state != GameState.WAITING) {
				imgLabelSide[i].setCursor(new Cursor(Cursor.HAND_CURSOR));
			}
			sideMyPanel.add(imgLabelSide[i]);
			
			fName = "";
			switch (temp2) {
			case 0:
			case 7: fName = PIECE_GIRAFFE; break;
			case 1:
			case 6: fName = PIECE_LION; break;
			case 2:
			case 5: fName = PIECE_ELEPHANT; break;
			case 3:
			case 4: fName = PIECE_CHICK; break;
			}
			sideOpPanel.add(new JLabel(new ImageIcon("img/img_"+fName+"_2"+fileExtension)));
		}
		sideOpPanel.revalidate();
		sideOpPanel.repaint();
		sideMyPanel.revalidate();
		sideMyPanel.repaint();
	}
	
	public void toggleTurn() {
		itsMyTurn = !itsMyTurn;
		updateSide();
		refreshTurn();
	}
	public void refreshTurn() {
		if (itsMyTurn) {
			lblMyNickname.setText("> "+player1+" <");
			lblOpNickname.setText(player2);
		} else {
			lblMyNickname.setText(player1);
			lblOpNickname.setText("> "+player2+" <");
		}
	}
	
	public static void main(String[] args) {
		GameMain game = new GameMain("Title");
		game.setVisible(true);
	}

}
