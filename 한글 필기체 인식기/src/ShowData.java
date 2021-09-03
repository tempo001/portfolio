import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;


/*
 * ㅇ,ㅎ 문제 -> 각도 변화량의 누적
*/
public class ShowData implements ActionListener {
	final static int MAXN = 200;
	JFrame frame;
	JPanel3 pDraw;
	JPanel2 pPaint;
	JButton btnDbReload = new JButton("DB reload");
	JButton btnClear2 = new JButton("Clear");
	JButton btnRecog2 = new JButton("Move to right");
	JTextField tfLoad = new JTextField("C:/train data/train_01조.txt", 20);
	JButton btnLoad = new JButton("Load");
	JButton btnSaveCho = new JButton("Train Cho");
	JButton btnSaveJung = new JButton("Train Jung");
	JButton btnSaveJong = new JButton("Train Jong");
	JLabel lblHangul = new JLabel("?"); //정답 (파일 안에 들어있는)
	JLabel lblHangul2 = new JLabel("?"); //인식 결과
	JLabel lblMax = new JLabel(" / 0");
	JTextField tfIndex = new JTextField("1", 3);
	JButton btnPrev = new JButton("Prev");
	JButton btnNext = new JButton("Next");
	JButton btnShow = new JButton("Show");
	JButton btnRecog = new JButton("Recognize");
	JButton btnAutoRecog = new JButton("Auto Recognize");
	JButton btnAutoRecog2 = new JButton("ALL Recognize");
	JCheckBox chckBoxPoint = new JCheckBox("Show points", false);
	JCheckBox chckBoxFeaturePoint = new JCheckBox("Show feature points", true);
	JButton btnClear = new JButton("Clear");
	JButton btnSmooth = new JButton("Smooth");
	JCheckBox chckBoxLog = new JCheckBox("로그 사용", false);
	JCheckBox chckBoxCumul = new JCheckBox("로그 누적", true);
	String[] data = new String[MAXN];
	ArrayList<PointPair> listLine = new ArrayList<PointPair>();
	ArrayList<Point> listPoint = new ArrayList<Point>();
	ArrayList<ArrayList<Point>> listStroke = new ArrayList<ArrayList<Point>>();
	ArrayList<ArrayList<Point>> listFeaturePoint = new ArrayList<ArrayList<Point>>();
	ArrayList<Point> listFeaturePointLinear = new ArrayList<Point>();
	ArrayList<Double> inputPattern = new ArrayList<Double>();
	ArrayList<Double> inputPatternLength = new ArrayList<Double>();
	ArrayList<Boolean> inputPatternOnOff = new ArrayList<Boolean>();
	final double zoomRate = 2.0;
	final int padding_x = 10;
	final int padding_y = 10;
	final int INF = 999999;
	int letter_size_x = 0;
	int letter_size_y = 0;
	DPmatching dp = new DPmatching();
	ArrayList<Point> list = new ArrayList<Point>(); //left all points
	int numOfData = 0;
	
	public class PointPair {
		int x1, x2;
		int y1, y2;
		int color;
		PointPair(int x1, int y1, int x2, int y2) {
			this.x1 = x1;
			this.x2 = x2;
			this.y1 = y1;
			this.y2 = y2;
			color = 0;
		}
		PointPair(int x1, int y1, int x2, int y2, int color) {
			this.x1 = x1;
			this.x2 = x2;
			this.y1 = y1;
			this.y2 = y2;
			this.color = color;
		}
	}
	public class JPanel2 extends JPanel {
		private static final long serialVersionUID = 1L;
		void drawLines(Graphics g) {
			getInputPattern();
	        Graphics2D g2d = (Graphics2D) g;
	        /*float[] dashingPattern1 = {2f, 2f};
	        Stroke stroke1 = new BasicStroke(2f, BasicStroke.CAP_BUTT,
	                BasicStroke.JOIN_MITER, 1.0f, dashingPattern1, 2.0f);*/
	        Stroke stroke2 = new BasicStroke(2f);
	        Stroke stroke3 = new BasicStroke(3f);
	        Stroke stroke4 = new BasicStroke(1f);
	        Stroke stroke5 = new BasicStroke(5f);
	        
	        g2d.setStroke(stroke2);
	        for(int i=0;i<listLine.size();i++) {
	        	listLine.get(i).color = listLine.get(i).color % 7;
	        	if(listLine.get(i).color == 0) g2d.setColor(Color.blue);
	        	else if(listLine.get(i).color == 1) g2d.setColor(Color.green);
	        	else if(listLine.get(i).color == 2) g2d.setColor(Color.cyan);
	        	else if(listLine.get(i).color == 3) g2d.setColor(Color.orange);
	        	else if(listLine.get(i).color == 4) g2d.setColor(Color.yellow);
	        	else if(listLine.get(i).color == 5) g2d.setColor(Color.pink);
	        	else if(listLine.get(i).color == 6) g2d.setColor(Color.black);
	        	g2d.drawLine(listLine.get(i).x1, listLine.get(i).y1, listLine.get(i).x2, listLine.get(i).y2);
	        }
	        
	        if(chckBoxPoint.isSelected()) {
		        boolean flg = false;
		        for(int i=0;i<listPoint.size();i++) {
		        	if(i == 0 || flg) {
		        		g2d.setStroke(stroke5);
		        		g2d.setColor(Color.BLACK);
		        		flg = false;
		        	} else {
		        		g2d.setStroke(stroke3);
		        		g2d.setColor(Color.MAGENTA);
		        	}
		        	int x = listPoint.get(i).x;
		        	int y = listPoint.get(i).y;
		        	if(isNullPoint(x, y)) {
		        		flg = true;
		        		continue;
		        	}
		        	g2d.drawLine(x, y, x, y);
		        }
	        }
	        if(chckBoxFeaturePoint.isSelected()) {
		        for(int i=0;i<listFeaturePointLinear.size();i++) {
		        		g2d.setStroke(stroke5);
		        		g2d.setColor(Color.RED);
		        		int x = listFeaturePointLinear.get(i).x;
		        		int y = listFeaturePointLinear.get(i).y;
		        		g2d.drawLine(x, y, x, y);
		        }
	        }
//	        if(chckBoxBox.isSelected()) {
//	        	setListStroke();
//		        g2d.setStroke(stroke4);
//	        	g2d.setColor(Color.RED);
//		        for(int i=0;i<listStroke.size();i++) {
//		        	int max_x = -1;
//		        	int max_y = -1;
//		        	int min_x = INF;
//		        	int min_y = INF;
//		        	for(int j=0;j<listStroke.get(i).size();j++) {
//		        		max_x = Math.max(max_x, listStroke.get(i).get(j).x);
//		        		max_y = Math.max(max_y, listStroke.get(i).get(j).y);
//		        		min_x = Math.min(min_x, listStroke.get(i).get(j).x);
//		        		min_y = Math.min(min_y, listStroke.get(i).get(j).y);
//		        	}
//		        	g2d.drawRect(min_x - 1, min_y - 1, max_x - min_x + 1, max_y - min_y + 1);
//		        }
//	        }
	        //g2d.draw(new Line2D.Double(59.2d, 99.8d, 419.1d, 99.8d));
	        //g2d.draw(new Line2D.Float(21.50f, 132.50f, 459.50f, 132.50f));
	    }
		public void paint(Graphics g) {
	        super.paint(g);
	        drawLines(g);
	    }
	}
	public class JPanel3 extends JPanel {
		private static final long serialVersionUID = 1L;
		Point pointStart = null;
        Point pointEnd = null;
        {
            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    pointStart = e.getPoint();
                    list.add(pointStart);
                }

                public void mouseReleased(MouseEvent e) {
                    pointStart = new Point(-1, -1);
                    list.add(pointStart);
                    repaint();
                }
            });
            addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseMoved(MouseEvent e) {
                    pointEnd = e.getPoint();
                }

                public void mouseDragged(MouseEvent e) {
                    pointEnd = e.getPoint();
                    list.add(pointEnd);
                    repaint();
                }
            });
        }
        public void paint(Graphics g) {
            super.paint(g);
        	Graphics2D g2d = (Graphics2D) g;
        	Stroke stroke1 = new BasicStroke(3f);
        	Stroke stroke2 = new BasicStroke(2f);
	        g2d.setStroke(stroke2);
            g2d.setColor(Color.BLACK);
            for(int i=0;i<list.size();i++) {
            	if( (i > 0 && list.get(i-1).x<0) || list.get(i).x<0) continue;
            	if(i > 0) g2d.drawLine(list.get(i-1).x, list.get(i-1).y, list.get(i).x, list.get(i).y);
            }
            g2d.setStroke(stroke1);
            g2d.setColor(Color.BLUE);
        }
    }
	
		
	@SuppressWarnings("unchecked")
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == btnLoad) {
			if(!loadFile()) {
				
			}
		} else if(e.getSource() == btnSaveCho) {
			saveTrainData("chosung.txt");
		} else if(e.getSource() == btnSaveJung) {
			saveTrainData("jungsung.txt");
		} else if(e.getSource() == btnSaveJong) {
			saveTrainData("jongsung.txt");
		} else if(e.getSource() == btnShow) {
			showData();
		} else if(e.getSource() == btnClear) {
			listClear();
			lblHangul.setText("?");
			lblHangul2.setText("?");
		} else if(e.getSource() == btnRecog) {
			filterSmooth();
			pPaint.repaint();
			Recognition();
		} else if(e.getSource() == btnAutoRecog) {
			AutoRecognition(false);
		} else if(e.getSource() == btnAutoRecog2) {
			AllRecognition();
		} else if(e.getSource() == btnPrev) {
			prevData();
		} else if(e.getSource() == btnNext) {
			nextData();
		} else if(e.getSource() == chckBoxPoint) {
			pPaint.repaint();
		} else if(e.getSource() == chckBoxFeaturePoint) {
			pPaint.repaint();
//		} else if(e.getSource() == chckBoxBox) {
//			pPaint.repaint();
		} else if(e.getSource() == btnSmooth) {
			filterSmooth();
			pPaint.repaint();
		} else if(e.getSource() == btnDbReload) {
			if(!dp.DbReload()) {
				System.exit(1);
			}
		} else if(e.getSource() == btnClear2) {
			list.clear();
			pDraw.repaint();
		} else if(e.getSource() == btnRecog2) {
			listPoint = (ArrayList<Point>) list.clone();
			setLineList();
			setFeaturePoint();
			pPaint.repaint();
		}
	}
	
	
	ShowData(String title) {
		frame = new JFrame(title);
		frame.setLayout(new GridLayout(1, 2));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		pDraw = new JPanel3();
		pPaint = new JPanel2();
		JPanel pRight = new JPanel();
		JPanel pLeft = new JPanel();
		JPanel pLeftDown = new JPanel();
		JPanel p = new JPanel();
		JPanel p2 = new JPanel();
		JPanel p3 = new JPanel();
		JPanel p4 = new JPanel();
		pRight.setLayout(new BorderLayout());
		pLeft.setLayout(new BorderLayout());
		pLeftDown.setLayout(new GridLayout(3, 1));
		p.setLayout(new FlowLayout());
		p2.setLayout(new FlowLayout());
		p3.setLayout(new FlowLayout());
		p4.setLayout(new GridLayout(3, 1));
		p.add(tfLoad);
		p.add(btnLoad);
		p.add(btnSaveCho);
		p.add(btnSaveJung);
		p.add(btnSaveJong);
		JPanel pEast = new JPanel();
		pEast.setLayout(new GridLayout(2, 1));
		pEast.add(lblHangul);
		pEast.add(lblHangul2);
		lblHangul.setFont(new Font("굴림", Font.BOLD, 60));
		lblHangul2.setFont(new Font("굴림", Font.BOLD, 60));
		pRight.add(pEast, BorderLayout.EAST);
		p2.add(tfIndex);
		p2.add(lblMax);
		p2.add(btnPrev);
		p2.add(btnNext);
		p2.add(btnShow);
		p2.add(btnRecog);
		p2.add(btnAutoRecog);
		p2.add(btnAutoRecog2);
		p3.add(chckBoxPoint);
		p3.add(chckBoxFeaturePoint);
//		p3.add(chckBoxBox);
		p3.add(btnClear);
		p3.add(btnSmooth);
		p3.add(chckBoxLog);
		p3.add(chckBoxCumul);
		p4.add(p);
		p4.add(p2);
		p4.add(p3);
		
		pLeftDown.add(btnClear2);
		pLeftDown.add(btnDbReload);
		pLeftDown.add(btnRecog2);
		pLeft.add(pDraw);
		pLeft.add(pLeftDown, BorderLayout.SOUTH);
		pRight.add(pPaint, BorderLayout.CENTER);
		pRight.add(p4, BorderLayout.SOUTH);
		
		frame.add(pLeft);
		frame.add(pRight);
		
		btnDbReload.addActionListener(this);
		btnClear2.addActionListener(this);
		btnRecog2.addActionListener(this);
		btnLoad.addActionListener(this);
		btnSaveCho.addActionListener(this);
		btnSaveJung.addActionListener(this);
		btnSaveJong.addActionListener(this);
		btnPrev.addActionListener(this);
		btnNext.addActionListener(this);
		btnShow.addActionListener(this);
		btnRecog.addActionListener(this);
		btnAutoRecog.addActionListener(this);
		btnAutoRecog2.addActionListener(this);
		chckBoxPoint.addActionListener(this);
		chckBoxFeaturePoint.addActionListener(this);
//		chckBoxBox.addActionListener(this);
		btnClear.addActionListener(this);
		btnSmooth.addActionListener(this);
		
		btnAutoRecog.setBackground(Color.red);
		btnAutoRecog.setOpaque(true);
		btnAutoRecog2.setBackground(Color.green);
		btnAutoRecog2.setOpaque(true);
		btnRecog.setBackground(Color.cyan);
		btnRecog.setOpaque(true);
		
		String path = System.getProperty("user.dir");
		if(!path.endsWith("\\")) path += "\\";
		path += "train data\\train_01조.txt";
		tfLoad.setText(path);
		tfLoad.setFont(new Font("굴림", Font.PLAIN, 16));
		frame.setSize(1300,600);
		//frame.setLocation(600,200);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		tfLoad.setCaretPosition(path.length());
	}
	
	
	
	public static void main(String[] args) {
		new ShowData("HWRecognition_ShowData");
	}
	public void showData() {
		int index;
		try {
			index = Integer.parseInt(tfIndex.getText());
		} catch(Exception e1) {
			index = 1;
		}
		if(numOfData < index) {
			index = numOfData;
		} else if(index < 1) {
			index = 1;
		}
		tfIndex.setText(""+index);
		listClear();
		setPointList(index-1);
	}
	public void prevData() {
		int index;
		try {
			index = Integer.parseInt(tfIndex.getText());
		} catch(Exception e1) {
			index = 1;
		}
		tfIndex.setText(""+(index-1));
		showData();
	}
	public void nextData() {
		int index;
		try {
			index = Integer.parseInt(tfIndex.getText());
		} catch(Exception e1) {
			index = 1;
		}
		tfIndex.setText(""+(index+1));
		showData();
	}
	
	public void setPointList(int index) {
		String[] str = data[index].split(" ");
		int now[] = new int[2];
		
		lblHangul.setText(str[0]);
		for(int i=1;i<str.length;i++) {
			str[i] = str[i].substring(1, str[i].length() - 1);
			int pos = str[i].indexOf(",");
			now[0] = Integer.parseInt(str[i].substring(0, pos));
			now[1] = Integer.parseInt(str[i].substring(pos+1));
			if(listPoint.size() == 0 && now[0] == -1 && now[1] == -1) continue; 
			listPoint.add(new Point((int) (now[0]*zoomRate + padding_x), (int) (now[1]*zoomRate + padding_y)));
		}
		//맨 마지막에 null 포인터가 없으면 추가
		int n = listPoint.size();
		if(!isNullPoint(listPoint.get(n-1).x, listPoint.get(n-1).y)) {
			listPoint.add(new Point((int) (-1*zoomRate + padding_x), (int) (-1*zoomRate + padding_y)));
		}
		setLineList();
		setFeaturePoint();
		pPaint.repaint();
	}
	public void setLineList() {
		listLine.clear();
		int prev[] = new int[2];
		int now[] = new int[2];
		int color = 0;
		prev[0] = prev[1] = -1;
		for(int i=0;i<listPoint.size();i++) {
			now[0] = listPoint.get(i).x;
			now[1] = listPoint.get(i).y;
			if(!isNullPoint(now[0], now[1]) && !isNullPoint(prev[0], prev[1])) {
				listLine.add(new PointPair(prev[0], prev[1], now[0], now[1], color));
			}
			
			if(isNullPoint(prev[0], prev[1])) {
				if(i > 0) color++;
				listLine.add(new PointPair(now[0], now[1], now[0], now[1], color));
			}
			
			prev[0] = now[0];
			prev[1] = now[1];
		}
	}
	public void filterSmooth() {
		ArrayList<Point> listTemp = new ArrayList<Point>();
		listTemp.clear();
		for(int i=0;i<listPoint.size();i++) {
			listTemp.add(listPoint.get(i));
		}
		for(int i=3;i<listPoint.size()-3;i++) {
			boolean flg = true;
			for(int j=-3;j<=3;j++) {
				if(isNullPoint(listPoint.get(i+j).x, listPoint.get(i+j).y)) {
					flg = false;
				}
			}
			if(!flg) continue;
			
			int x, y;
			x = (listPoint.get(i-3).x + 3*listPoint.get(i-2).x + 6*listPoint.get(i-1).x
				+ 7*listPoint.get(i).x + 6*listPoint.get(i+1).x + 3*listPoint.get(i+2).x
				+ listPoint.get(i+3).x) / 27;
			y = (listPoint.get(i-3).y + 3*listPoint.get(i-2).y + 6*listPoint.get(i-1).y
					+ 7*listPoint.get(i).y + 6*listPoint.get(i+1).y + 3*listPoint.get(i+2).y
					+ listPoint.get(i+3).y) / 27;
			listTemp.set(i, new Point(x, y));
		}
		for(int i=0;i<listTemp.size();i++) {
			listPoint.set(i, listTemp.get(i));
		}
		setLineList();
		setFeaturePoint();
		getInputPattern();
	}
	@SuppressWarnings("unchecked")
	public void setListStroke() {
		listStroke.clear();
		ArrayList<Point> listTemp = new ArrayList<Point>();
		listTemp.clear();
		for(int i=0;i<listPoint.size();i++) {
			int x = listPoint.get(i).x;
			int y = listPoint.get(i).y;
			if(isNullPoint(x, y)) {
				listStroke.add((ArrayList<Point>) listTemp.clone());
				listTemp.clear();
			} else {
				listTemp.add(new Point(x, y));
			}
		}
	}
	@SuppressWarnings("unchecked")
	public void setFeaturePoint() { //최대 허용 오차법(maximum permission error method)
		listFeaturePoint.clear();
		listFeaturePointLinear.clear();
		setListStroke();
		ArrayList<Point> listTemp = new ArrayList<Point>();
		for(int i=0;i<listStroke.size();i++) {
			int N = listStroke.get(i).size();
			int start = 0;
			int end = N - 1;
			listTemp.clear();
			listTemp.add(listStroke.get(i).get(start));
			listTemp.add(listStroke.get(i).get(end));
			processMaximumPoint(listStroke.get(i), start, end, listTemp, 0, 0);
			
			listFeaturePoint.add((ArrayList<Point>) listTemp.clone());
		}
		for(int i=0;i<listFeaturePoint.size();i++) {
			for(int j=0;j<listFeaturePoint.get(i).size();j++) {
				listFeaturePointLinear.add(listFeaturePoint.get(i).get(j));
			}
		}
	}
	public int processMaximumPoint(ArrayList<Point> list, int start, int end, ArrayList<Point> addList, int i1, int i2) {
		getLetterSize();
		final double det = Math.min(letter_size_x, letter_size_x) / 22.0;
		int x1 = list.get(start).x;
		int y1 = list.get(start).y;
		int xN = list.get(end).x;
		int yN = list.get(end).y;
		double maxv = -1;
		int maxi = -1;
		for(int i = start + 1; i < end; i++) {
			int x = list.get(i).x;
			int y = list.get(i).y;
			double d = Math.abs( (yN-y1)*x - (xN-x1)*y - x1*yN + xN*y1 ) / Math.sqrt( (yN-y1)*(yN-y1) + (xN-x1)*(xN-x1) );
			if(maxv < d) {
				maxv = d;
				maxi = i;
			}
		}
		if(maxv >= det) {
			addList.add(i1+1, list.get(maxi));
			int cnt = processMaximumPoint(list, start, maxi, addList, i1, i2);
			int cnt2 = processMaximumPoint(list, maxi, end, addList, i1+1+cnt, i2);
			return cnt+cnt2+1;
		} else {
			return 0;
		}
	}
	public void listClear() {
		listLine.clear();
		listPoint.clear();
		listFeaturePoint.clear();
		listFeaturePointLinear.clear();
		listStroke.clear();
		inputPattern.clear();
		inputPatternLength.clear();
		inputPatternOnOff.clear();
		pPaint.repaint();
	}
	public boolean isNullPoint(int x, int y) {
		return (x < padding_x && y < padding_y);
	}
	public double convertToAngle(int x, int y) {
		double ret = Math.toDegrees(Math.atan2((double) y, (double) x));
		if(ret < 0) ret += 360.0;
		return ret;
	}
	public void getLetterSize() {
		int n = listPoint.size();
		int max_x = -1;
    	int max_y = -1;
    	int min_x = INF;
    	int min_y = INF;
		for(int i=0;i<n;i++) {
			max_x = Math.max(max_x, listPoint.get(i).x);
    		max_y = Math.max(max_y, listPoint.get(i).y);
    		min_x = Math.min(min_x, listPoint.get(i).x);
    		min_y = Math.min(min_y, listPoint.get(i).y);
		}
		letter_size_x = max_x - min_x + 1;
		letter_size_y = max_y - min_y + 1;
	}
	public void getInputPattern() {
		inputPattern.clear();
		inputPatternLength.clear();
		inputPatternOnOff.clear();
		int prev_x = -1;
    	int prev_y = -1;
        for(int i=0;i<listFeaturePoint.size();i++) {
        	for(int j=0;j<listFeaturePoint.get(i).size();j++) {
        		int x = listFeaturePoint.get(i).get(j).x;
        		int y = listFeaturePoint.get(i).get(j).y;
        		if(!(i==0 && j==0)) {
        			double angle = convertToAngle(x - prev_x, prev_y - y);
        			double length = Math.sqrt((x-prev_x)*(x-prev_x) + (prev_y - y)*(prev_y - y));
        			//System.out.println(angle+" "+length);
        			//System.out.print(angle+",");
        			inputPattern.add(angle);
        			inputPatternLength.add(length);
        			inputPatternOnOff.add(j != 0);
        		}
        		prev_x = x;
        		prev_y = y;
        	}
        }
        //System.out.println();
	}
	public void delSmallInput() {
		final double detLen = 8.0;
		final double detAngle = 20.0;
		boolean flg = true; //실회 우선
		for(int i=0;i<inputPatternLength.size();i++) {
			if((double) inputPatternLength.get(i) < detLen && inputPatternOnOff.get(i) == flg) {
				System.out.println("remove "+(double) inputPatternLength.get(i)+", i="+i+", isOn="+inputPatternOnOff.get(i));
				Point p1 = null, p2 = null, p0 = null;
				int temp = 0;
				if(flg) {
					listFeaturePointLinear.remove(i);
					listFeaturePointLinear.remove(i);
					try {
						p1 = listFeaturePointLinear.get(i-1);
					} catch (Exception e) {
						p1 = null;
					}
					try {
						p2 = listFeaturePointLinear.get(i);
					} catch (Exception e) {
						p2 = null;
					}
					inputPattern.remove(i);
					inputPatternLength.remove(i);
					inputPatternOnOff.remove(i);
					if(p2 != null) {
						inputPattern.remove(i);
						inputPatternLength.remove(i);
						inputPatternOnOff.remove(i);
					}
					if(p1 != null) {
						inputPattern.remove(i-1);
						inputPatternLength.remove(i-1);
						inputPatternOnOff.remove(i-1);
					}
				} else {
					listFeaturePointLinear.remove(i+1);
					p0 = listFeaturePointLinear.get(i-1);
					p1 = listFeaturePointLinear.get(i);
					p2 = listFeaturePointLinear.get(i+1);
					if(subAngle(convertToAngle(p2.x-p1.x, p1.y-p2.y), convertToAngle(p1.x-p0.x, p0.y-p1.y)) < detAngle) {
						listFeaturePointLinear.remove(i);
						
						inputPattern.remove(i);
						inputPatternLength.remove(i);
						inputPatternOnOff.remove(i);
						inputPattern.remove(i);
						inputPatternLength.remove(i);
						inputPatternOnOff.remove(i);
						inputPattern.remove(i-1);
						inputPatternLength.remove(i-1);
						inputPatternOnOff.remove(i-1);
						
						p1 = listFeaturePointLinear.get(i-1);
						p2 = listFeaturePointLinear.get(i);
						temp = 1;
					} else {
						inputPattern.remove(i);
						inputPatternLength.remove(i);
						inputPatternOnOff.remove(i);
						inputPattern.remove(i);
						inputPatternLength.remove(i);
						inputPatternOnOff.remove(i);
					}
				}
				
				if(flg) {
					if(p1 != null && p2 != null) {
						inputPattern.add(i, convertToAngle(p2.x-p1.x, p1.y-p2.y));
						inputPatternLength.add(i, distancePoints(p2.x-p1.x, p1.y-p2.y));
						inputPatternOnOff.add(i, false);
					}
				} else {
					inputPattern.add(i - temp, convertToAngle(p2.x-p1.x, p1.y-p2.y));
					inputPatternLength.add(i - temp, distancePoints(p2.x-p1.x, p1.y-p2.y));
					inputPatternOnOff.add(i - temp, true);
				}
				i--;
			}
			if(i == inputPatternLength.size() - 1) {
				i = 0;
				if(!flg) break;
				else flg = false;
			}
		}
		pPaint.repaint();
	}
	public double distancePoints(int dx, int dy) {
		return Math.sqrt(dx*dx + dy*dy);
	}
	@SuppressWarnings("unchecked")
	public void Recognition() {
		delSmallInput();
		double[] inputPatternArr = new double[inputPattern.size()];
		for(int i=0;i<inputPatternArr.length;i++) {
			inputPatternArr[i] = inputPattern.get(i);
		}
		double[] inputPatternLengthArr = new double[inputPatternLength.size()];
		for(int i=0;i<inputPatternLengthArr.length;i++) {
			inputPatternLengthArr[i] = inputPatternLength.get(i);
		}
		boolean[] inputPatternOnOffArr = new boolean[inputPatternOnOff.size()];
		for(int i=0;i<inputPatternOnOffArr.length;i++) {
			inputPatternOnOffArr[i] = inputPatternOnOff.get(i);
		}
		ArrayList<Point> listTemp = new ArrayList<Point>();
		listTemp.clear();
		for(int i=0;i<listFeaturePointLinear.size();i++) {
			listTemp.add(listFeaturePointLinear.get(i));
		}
		System.out.println("특징점 개수 = "+listFeaturePointLinear.size());
		dp.setListPoint((ArrayList<Point>) listTemp.clone());
		long stime = System.currentTimeMillis();
		ArrayList<Point>[] dpPath = (ArrayList<Point>[])new ArrayList[4];
		for(int i=0;i<4;i++) {
			dpPath[i] = new ArrayList<Point>();
		}
		dp.dp_matching(inputPatternArr, inputPatternLengthArr, inputPatternOnOffArr, 0, 0, -1, -1, -1, 0, 0, 0, dpPath.clone());
		String ret = dp.printLetterList();
		lblHangul2.setText(ret);
		System.out.println("recognition time: "+(System.currentTimeMillis() - stime)/1000.0+"(s)");
	}
	public void saveTrainData(String fileName) {
		try {
			String input = "";
			do {
				input = JOptionPane.showInputDialog(frame,"데이터 베이스에 추가할 자음 또는 모음", null);
				if(input == null) break;
				else input = input.trim();
			} while(input.equals("") || input.length() != 1);
			if(input == null) return;
			BufferedWriter out = new BufferedWriter(new FileWriter(fileName, true));
			out.write(input+" ");
			for(int i=0;i<inputPattern.size();i++) {
				out.write(String.format("%d,%.4f,%.4f ", inputPatternOnOff.get(i) ? 1:0, inputPattern.get(i), inputPatternLength.get(i)));
			}
			out.newLine();
			out.close();
		} catch (FileNotFoundException e1) {
			System.err.println("File Write Error (FileNotFoundException): "+fileName);
		} catch (IOException e2) {
			System.err.println("File Write Error (IOException): "+fileName);
		}
	}
	public boolean loadFile() {
		listClear();
		lblHangul.setText("?");
		lblHangul2.setText("?");
		try {
			BufferedReader in = new BufferedReader(new FileReader(tfLoad.getText()));
			String str;
			int cnt = 0;
			while( (str = in.readLine()) != null ) {
				data[cnt] = str;
				cnt++;
			}
			numOfData = cnt;
			lblMax.setText(" / "+numOfData);
			in.close();
		} catch (FileNotFoundException e1) {
			System.err.println("File Read Error (FileNotFoundException): "+tfLoad.getText());
			JOptionPane.showMessageDialog(frame, tfLoad.getText()+" 열 수 없음", "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		} catch (IOException e2) {
			System.err.println("File Read Error (IOException): "+tfLoad.getText());
			JOptionPane.showMessageDialog(frame, tfLoad.getText()+" 열 수 없음", "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		tfIndex.setText("1");
		setPointList(0);
		return true;
	}
	public int[] AutoRecognition(boolean isAuto) {
		int correctNum[] = new int[] {-1, -1};
		final String fileName = "recognition_log.txt";
		final String fileName2 = "recognition_log_simple.txt";
		if(!loadFile()) return correctNum;
		
		int cnt = 0, sz = (int)(Math.log10(numOfData) + 1);
		BufferedWriter out = null;
		BufferedWriter out2 = null;
		try {
			if(chckBoxLog.isSelected()) {
				out = new BufferedWriter(new FileWriter(fileName, chckBoxCumul.isSelected()));
				out2 = new BufferedWriter(new FileWriter(fileName2, chckBoxCumul.isSelected()));
				int pos = tfLoad.getText().lastIndexOf("\\");
				out.write("대상 파일 : "+tfLoad.getText().substring(pos+1));
				out.newLine();
				out2.write(tfLoad.getText().substring(pos+1)+" | ");
			}
			for(int i=0;i<numOfData;i++) {
				filterSmooth();
				pPaint.repaint();
				Recognition();
				boolean flg = false;
				if(lblHangul.getText().equals(lblHangul2.getText())) {
					flg = true;
					cnt++;
				}
				if(chckBoxLog.isSelected()) {
					out.write((flg ? "O " : "X ")+"["+String.format("%0"+sz+"d", i+1)+"/"+numOfData+"] 정답: "+lblHangul.getText()+", 인식 결과: "+lblHangul2.getText());
					out.newLine();
				}
				nextData();
			}
			if(chckBoxLog.isSelected()) {
				out.write(String.format("결과 : 총 %d개의 데이터 중에서 %d개 올바르게 인식 (%.2f%% 인식률)", numOfData, cnt, ((double) cnt / numOfData)*100.0));
				out.newLine();
				out.newLine();
				out2.write(String.format("%d / %d 인식 (%.2f%% 인식률)", cnt, numOfData, ((double) cnt / numOfData)*100.0));
				out2.newLine();
				out.close();
				out2.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(!isAuto) JOptionPane.showMessageDialog(frame, String.format("결과 : 총 %d개의 데이터 중에서 %d개 올바르게 인식 (%.2f%% 인식률)", numOfData, cnt, ((double) cnt / numOfData)*100.0), 
				"Auto Recognition Result", JOptionPane.INFORMATION_MESSAGE);
		correctNum[0] = cnt;
		correctNum[1] = numOfData;
		return correctNum;
	}
	public void AllRecognition() {
		File[] file = null;
		JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
		chooser.setMultiSelectionEnabled(true);
		int returnValue = chooser.showOpenDialog(frame);
		if( returnValue == JFileChooser.APPROVE_OPTION ) {
			file = chooser.getSelectedFiles() ;
		} else {
			return;
		}
		
		int[] arr = new int[2];
		int[] sum = new int[] {0, 0};
		if(file != null) {
			for(int i=0;i<file.length;i++) {
				//System.out.println(file[i].getPath());
				tfLoad.setText(file[i].getPath());
				arr = AutoRecognition(true);
				sum[0] += arr[0];
				sum[1] += arr[1];
			}
			JOptionPane.showMessageDialog(frame, String.format("결과 : %d개의 파일의 총 %d개의 데이터 중에서 %d개 올바르게 인식 (%.2f%% 인식률)", file.length, sum[1], sum[0], ((double) sum[0] / sum[1])*100.0), 
					"ALL Recognition Result", JOptionPane.INFORMATION_MESSAGE);
		}
	}
	public double subAngle(double a, double b) {
		double ret = Math.abs(a - b);
		if(ret > 180.0) ret = 360.0 - ret;
		return ret;
	}
}
