import java.awt.Point;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.JOptionPane;

public class DPmatching {
	final int INF = 999999;
	ArrayList<Point> listPoint = new ArrayList<Point>();
	ArrayList<DbClass> dbList = new ArrayList<DbClass>();
	ArrayList<Letter> letterList = new ArrayList<Letter>(); //후보군
	
	DPmatching() {
		DbReload();
		/*for(int i=0;i<inputPattern.length;i++) {
			inputPattern[i] = subAngle(inputPattern[i], inputPattern[0]); 
		}
		for(int i=0;i<dbList.size();i++) {
			for(int j=0;j<dbList.get(i).pattern.size();j++) {
				dbList.get(i).pattern.set(j, subAngle(dbList.get(i).pattern.get(j), dbList.get(i).pattern.get(0))); 
			}
		}*/
	}
	public class Letter implements Comparable<Letter>{
		String letter = "";
		double penalty = INF;
		int strokeLength = 0;
		int type;
		double[] inputPattern;
		double[] lengthArr;
		int[] pointIndex = new int[] {-1, -1, -1, -1};
		int[] phonemeIndex = new int[] {-1, -1, -1, -1}; //dbList의 index
		@SuppressWarnings("unchecked")
		ArrayList<Point>[] dpPath = (ArrayList<Point>[]) new ArrayList[4];
		Letter(String letter, double penalty, int strokeLength, int type, double[] inputPattern, double[] lengthArr, int[] pointIndex, int[] phonemeIndex, ArrayList<Point>[] dpPath) {
			this.letter = letter;
			this.penalty = penalty;
			this.strokeLength = strokeLength;
			this.type = type;
			this.inputPattern = inputPattern;
			this.lengthArr = lengthArr;
			this.pointIndex = pointIndex;
			this.phonemeIndex = phonemeIndex;
			for(int i=0;i<4;i++) {
				this.dpPath[i] = dpPath[i];
			}
		}
		@Override
		public int compareTo(Letter o) {
			if (this.penalty < o.penalty) {
	            return -1;
	        } else if (this.penalty > o.penalty) {
	            return 1;
	        }
			return 0;
		}
	}
	static class cmpStrokeLength implements Comparator<Letter> {
		@Override
		public int compare(Letter arg0, Letter arg1) {
			return arg1.strokeLength - arg0.strokeLength;
		}
	}
	static class cmpPenalty implements Comparator<Letter> {
		@Override
		public int compare(Letter arg0, Letter arg1) {
			return (int)(arg1.penalty*10000 - arg0.penalty*10000);
		}
	}
	public static void main(String[] args) {
		new DPmatching();
	}
	
	
	
	
	public boolean readFile(int opt) { //0: chosung, 1: jungsung, 2: jongsung
		String fileName = "";
		if(opt == 0) {
			fileName = "chosung.txt";
		} else if(opt == 1) {
			fileName = "jungsung.txt";
		} else if(opt == 2) {
			fileName = "jongsung.txt";
		}
		try {
			BufferedReader in = new BufferedReader(new FileReader(fileName));
			String str;
			int group = 0;
			while( (str = in.readLine()) != null ) {
				if(str.trim().equals("")) continue;
				else if(str.trim().equals("#group1")) {group = 0; continue;}
				else if(str.trim().equals("#group2")) {group = 1; continue;}
				else if(str.trim().equals("#group3")) {group = 2; continue;}
				String[] strArr = str.split(" ");
				ArrayList<Double> tempList = new ArrayList<Double>();
				ArrayList<Double> tempList2 = new ArrayList<Double>();
				ArrayList<Boolean> tempList3 = new ArrayList<Boolean>();
				
				for(int i=1;i<strArr.length;i++) {
					String[] strNum = strArr[i].split(",");
					
					tempList.add(Double.parseDouble(strNum[1]));
					tempList2.add(Double.parseDouble(strNum[2]));
					tempList3.add( (Integer.parseInt(strNum[0]) == 1) ? true : false );
				}
				DbClass dbInstance = new DbClass(opt, group, strArr[0], tempList, tempList2, tempList3);
//				System.out.print("["+dbList.size()+"] ");
//				dbInstance.show();
				dbList.add(dbInstance);
			}
			in.close();
		} catch (FileNotFoundException e1) {
			System.err.println("File Read Error (FileNotFoundException): " + fileName);
			JOptionPane.showMessageDialog(null, fileName+" 열 수 없음", "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		} catch (IOException e2) {
			System.err.println("File Read Error (IOException): " + fileName);
			JOptionPane.showMessageDialog(null, fileName+" 열 수 없음", "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
	public void showDbList() {
		int n = dbList.size();
		for(int i=0;i<n;i++) {
			System.out.print("["+i+"] ");
			dbList.get(i).show();
		}
	}
	public boolean isNullPoint(int x, int y) {
		return (x < 0 && y < 0);
	}
	public double subAngle(double a, double b) {
		double ret = Math.abs(a - b);
		if(ret > 180.0) ret = 360.0 - ret;
		return ret;
	}
	@SuppressWarnings("unchecked")
	public void dp_matching(double[] inputPattern, double[] lengthArr, boolean[] onoff, int std_type, int start, int cho, int jung, int jong1, int index1, int index2, int index3, ArrayList<Point>[] dpPath) {
		final double detAngle = 50.0;
		if(start == 0) letterList.clear();
		
		for(int k=0;k<dbList.size();k++) {
			dpPath[std_type].clear();
			int type = dbList.get(k).type;
			if(type != std_type && !(std_type == 3 && type == 2) && !(std_type == 0 && type == 1)) continue;
			//System.out.print("[k = "+k+"] ["+dbList.get(k).phoneme[type]+"("+dbList.get(k).pattern.size()+")] ");
			
			int i = start, j = 0;
			dpPath[std_type].add(new Point(i, j));
			while(true) {
				try {
					if(subAngle(inputPattern[i], dbList.get(k).pattern.get(j)) > detAngle) {
						break;
					}
					if(dbList.get(k).isOnOff.get(j) && !onoff[i]) {
						final double detLen = 15.0;
						if(distancePoints(listPoint.get(i).x - listPoint.get(i+1).x, listPoint.get(i).y - listPoint.get(i+1).y) > detLen) {
							break;
						}
					}
					
					if(j == dbList.get(k).pattern.size() - 1) { //정합 성공
						//2개의 세그먼트를 추가할 것인가
						int add[] = new int[] {0, -1, -1};
						for(int l=1;l<=2 && i+l < inputPattern.length && onoff[i+l];l++) {
							double a = subAngle(inputPattern[i+l-1], dbList.get(k).pattern.get(j));
							double b = subAngle(inputPattern[i+l], dbList.get(k).pattern.get(j));
							if(b <= detAngle) {//if(subAngle(a, b) <= 100.0) {
								add[l] = l;
							} else {
								break;
							}
						}
						
						if(std_type == 0) cho = k;
						else if(std_type == 1) jung = k;
						else if(std_type == 2) jong1 = k;
						
						if(std_type > 0 && dbList.get(cho).type != 0) return;
						if(std_type > 0 && dbList.get(jung).type != 1) return;
						if(std_type > 1 && dbList.get(jong1).type != 2) return;
						if(std_type == 3 && dbList.get(k).type != 2) return;
						
						int cutline = -1;
						if(std_type > 0 && dbList.get(jung).group == 2) {
							if(dbList.get(jung).phoneme.equals("ㅢ")) {
								cutline = index1+2 + 1;
							} else {
								cutline = index1+2 + 3;
							}
						}
						
						//min_x, min_y, max_x, max_y
						int[] boxPos1 = new int[] {INF, INF, INF+1, INF+1};
						int[] boxPos2 = new int[] {INF, INF, INF+1, INF+1};
						int[] boxPos2_2 = new int[] {INF, INF, INF+1, INF+1};
						int[] boxPos3 = new int[] {INF, INF, INF+1, INF+1};
						int[] boxPos4 = new int[] {INF, INF, INF+1, INF+1};
						int[] pointIndex = new int[] {-1, -1, -1, -1};
						if(std_type == 0) {
							boxPos1 = getBoxPos(0, i+1);
							pointIndex[0] = i+1;
						} else if(std_type == 1) {
							boxPos1 = getBoxPos(0, index1+1);
							if(cutline != -1) {
								boxPos2 = getBoxPos(index1+2, cutline);
								boxPos2_2 = getBoxPos(cutline+1, i+1);
							} else {
								boxPos2 = getBoxPos(index1+2, i+1);
							}
							pointIndex[0] = index1+1;
							pointIndex[1] = i+1;
						} else if(std_type == 2) {
							boxPos1 = getBoxPos(0, index1+1);
							if(cutline != -1) {
								boxPos2 = getBoxPos(index1+2, cutline);
								boxPos2_2 = getBoxPos(cutline+1, index2+1);
							} else {
								boxPos2 = getBoxPos(index1+2, index2+1);
							}
							boxPos3 = getBoxPos(index2+2, i+1);
							pointIndex[0] = index1+1;
							pointIndex[1] = index2+1;
							pointIndex[2] = i+1;
						} else if(std_type == 3) {
							boxPos1 = getBoxPos(0, index1+1);
							if(cutline != -1) {
								boxPos2 = getBoxPos(index1+2, cutline);
								boxPos2_2 = getBoxPos(cutline+1, index2+1);
							} else {
								boxPos2 = getBoxPos(index1+2, index2+1);
							}
							boxPos3 = getBoxPos(index2+2, index3+1);
							boxPos4 = getBoxPos(index3+2, i+1);
							pointIndex[0] = index1+1;
							pointIndex[1] = index2+1;
							pointIndex[2] = index3+1;
							pointIndex[3] = i+1;
						}
						
						boolean flg = true;
						if(std_type > 0) {
							switch(dbList.get(jung).group) {
							case 0:
								if(boxPos1[0] > boxPos2[0] || boxPos1[2] > boxPos2[2]) flg = false;
								if(boxPos1[3] < boxPos2[1]) flg = false;
								break;
							case 1:
								if(boxPos1[1] > boxPos2[1] || boxPos1[3] > boxPos2[3]) flg = false;
								if(boxPos1[2] < boxPos2[0]) flg = false;
								break;
							case 2:
								if(boxPos1[0] > boxPos2_2[0] || boxPos1[2] > boxPos2_2[2]) flg = false;
								if(boxPos1[3] < boxPos2_2[1]) flg = false;
								if(boxPos1[1] > boxPos2[1] || boxPos1[3] > boxPos2[3]) flg = false;
								if(boxPos1[2] < boxPos2[0]) flg = false;
								if(boxPos2[1] < boxPos2_2[1] || boxPos2[0] > boxPos2_2[0]) flg = false;
								break;
							}
							if(flg && std_type >= 2) {
								if(boxPos1[1] > boxPos3[1] || boxPos1[3] > boxPos3[3]) flg = false;
								if(boxPos2[1] > boxPos3[1] || boxPos2[3] > boxPos3[3]) flg = false;
								if(boxPos1[3] > boxPos3[1]) flg = false;
							}
							if(flg && std_type == 3) {
								if(boxPos1[1] > boxPos4[1] || boxPos1[3] > boxPos4[3]) flg = false;
								if(boxPos2[1] > boxPos4[1] || boxPos2[3] > boxPos4[3]) flg = false;
								if(boxPos3[0] > boxPos4[0] || boxPos3[2] > boxPos4[2]) flg = false;
							}
						}
						if(std_type == 0 && dbList.get(cho).group == 2) { //이중 중성만 있는 것 따로 체크
							if(dbList.get(cho).phoneme.equals("ㅢ")) {
								cutline = 1;
							} else {
								cutline = 3;
							}
							boxPos2 = getBoxPos(0, cutline);
							boxPos2_2 = getBoxPos(cutline+1, i+1);
							if(boxPos2[1] > boxPos2_2[1] || boxPos2[0] > boxPos2_2[0]) flg = false;
						}
						
						
						String s1 = (cho>=0) ? dbList.get(cho).phoneme : " ";
						String s2 = (jung>=0) ? dbList.get(jung).phoneme : " ";
						String s3 = (jong1 >= 0) ? dbList.get(jong1).phoneme : " ";
						String s4 = (std_type == 3) ? dbList.get(k).phoneme : " ";
						if(std_type == 3) {
							s3 = merge2(s3, s4);
							if(s3.equals(" ") || s3.equals("")) {
								flg = false;
							}
						}
						String[] strArr = new String[] {s1, s2, s3};
						int[] phonemeIndex = new int[] {(cho>=0) ? cho : -1, (jung>=0) ? jung : -1, (jong1 >= 0) ? jong1 : -1, (std_type == 3) ? k : -1};
						if(flg) {
							ArrayList<Point>[] temp = (ArrayList<Point>[]) new ArrayList[4];
							for(int i_=0;i_<4;i_++) {
								temp[i_] = (ArrayList<Point>) dpPath[i_].clone();
							}
							letterList.add(new Letter(mergeHangul(strArr), 0, (i+1), std_type, inputPattern, lengthArr, pointIndex.clone(), phonemeIndex.clone(), temp.clone()));
							//System.out.println(flg +" "+std_type+" "+(index1+1)+","+(index2+1)+","+(i+1)+" "+mergeHangul(strArr)+" end at "+(i+1)+" / "+inputPattern.length);
						}
						
						//탐색 (DFS)
						if(i < inputPattern.length - 1 && std_type < 3) {
							for(int l=0;l<=2 && add[l] != -1;l++) {
								if(i+add[l]+2 < inputPattern.length) {
									ArrayList<Point>[] temp = (ArrayList<Point>[]) new ArrayList[4];
									for(int i_=0;i_<4;i_++) {
										temp[i_] = (ArrayList<Point>) dpPath[i_].clone();
									}
									if(std_type == 0) {
										dp_matching(inputPattern, lengthArr, onoff, std_type + 1, i+add[l]+2, k, -1, -1, i+add[l], 0, 0, temp.clone());
									} else if(std_type == 1) {
										dp_matching(inputPattern, lengthArr, onoff, std_type + 1, i+add[l]+2, cho, k, -1, index1, i+add[l], 0, temp.clone());
									} else if(std_type == 2) {
										dp_matching(inputPattern, lengthArr, onoff, std_type + 1, i+add[l]+2, cho, jung, k, index1, index2, i+add[l], temp.clone());
									}
								}
							}
						}
						break;
					}
					if(i == inputPattern.length - 1) { //정합 실패
						//System.out.println("matching fail");
						break;
					}
					double minv = INF;
					int mini = -1;
					for(int i_ = 0; i_ <= 1; i_++) {
						for(int j_ = 0; j_<= 1; j_++) {
							if(i_ == 0 && j_ == 0) continue;
							double temp = subAngle(inputPattern[i+i_], dbList.get(k).pattern.get(j+j_));
							if(minv > temp) {
								minv = temp;
								mini = 100 * (i+i_) + (j+j_);
							}
						}
					}
					
					i = mini / 100;
					j = mini % 100;
					dpPath[std_type].add(new Point(i, j));
				} catch (Exception e) {
					e.printStackTrace();
					//System.err.printf("Error: ArrayIndexOutOfBoundsException");
					break;
				}
			}
		}
	}
	
	public int[] getBoxPos(int start, int end) {
		int[] ret = new int[] {INF, INF, -1, -1}; //min_x, min_y, max_x, max_y
		for(int i=start;i<=end;i++) {
			ret[0] = Math.min(ret[0], listPoint.get(i).x);
			ret[1] = Math.min(ret[1], listPoint.get(i).y);
			ret[2] = Math.max(ret[2], listPoint.get(i).x);
			ret[3] = Math.max(ret[3], listPoint.get(i).y);
		}
		return ret;
	}
	public void setListPoint(ArrayList<Point> list) {
		listPoint.clear();
		listPoint = list;
	}
	public String merge2(String ss1, String ss2) {
		char c = ' ';
		char s1 = ss1.charAt(0);
		char s2 = ss2.charAt(0);
		char list[][] = {
				{'ㄱ','ㄱ','ㄲ'}, {'ㄱ','ㅅ','ㄳ'}, {'ㄴ','ㅈ','ㄵ'}, {'ㄴ','ㅎ','ㄶ'},
				{'ㄹ','ㄱ','ㄺ'}, {'ㄹ','ㅁ','ㄻ'}, {'ㄹ','ㅂ','ㄼ'}, {'ㄹ','ㅅ','ㄽ'},
				{'ㄹ','ㅌ','ㄾ'}, {'ㄹ','ㅍ','ㄿ'}, {'ㄹ','ㅎ','ㅀ'}, {'ㅂ','ㅅ','ㅄ'},
				{'ㅅ','ㅅ','ㅆ'}
		};
		for(int i=0;i<list.length;i++) {
			if(s1 == list[i][0] && s2 == list[i][1]) {
				c = list[i][2];
				break;
			}
		}
		return String.valueOf(c);
	}
	public String mergeHangul(String[] ss) {
		char[][] sound = { {
			'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ',
			'ㅂ', 'ㅃ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ',
			'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ' }, {
			'ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ', 'ㅔ', 'ㅕ', 'ㅖ',
			'ㅗ', 'ㅘ', 'ㅙ', 'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ', 'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ',
			'ㅢ', 'ㅣ' }, {
			' ', 'ㄱ', 'ㄲ', 'ㄳ', 'ㄴ', 'ㄵ', 'ㄶ', 'ㄷ',
			'ㄹ', 'ㄺ', 'ㄻ', 'ㄼ', 'ㄽ', 'ㄾ', 'ㄿ', 'ㅀ', 'ㅁ',
			'ㅂ', 'ㅄ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ' 
		} };
		int[] s = new int[3];
		for(int j=0;j<3;j++) {
			for(int i=0;i<sound[j].length;i++) {
				if(ss[j].charAt(0) == sound[j][i]) {
					s[j] = i;
					break;
				}
			}
		}
		char c = (char) ((((s[0] * 28 * 21) + s[1] * 28) + s[2]) + 0xAC00);
		if(ss[1].equals(" ")) {
			return ss[0];
		} else {
			return String.valueOf(c);
		}
	}
	public String printLetterList() {
		int n = letterList.size();
		if(n == 0) return "?";
		Collections.sort(letterList, new cmpStrokeLength());
		int maxn = letterList.get(0).strokeLength;
		Collections.sort(letterList);
		double minv = INF;
		int mini = -1;
		for(int i=0;i<n;i++) {
			if(!(letterList.get(i).strokeLength >= maxn /*-(listPoint.size() > 10 ? 1:0)*/)) continue;
			letterList.get(i).penalty = evalLetter(i);
			if(minv > letterList.get(i).penalty) {
				minv = letterList.get(i).penalty;
				mini = i;
			}
			System.out.println(letterList.get(i).type+","+letterList.get(i).letter+", "+letterList.get(i).penalty+", "+letterList.get(i).pointIndex[0]+", "+letterList.get(i).pointIndex[1]+", "+letterList.get(i).pointIndex[2]+", "+letterList.get(i).strokeLength);
//			for(int j=0;j<4 && letterList.get(i).phonemeIndex[j] >= 0;j++) System.out.print(dbList.get(letterList.get(i).phonemeIndex[j]).phoneme+"("+letterList.get(i).phonemeIndex[j]+") ");
//			System.out.println();
//			for(int k=0;k<3;k++) {
//				int N = letterList.get(i).dpPath[k].size(); //입력
//				for(int j=0;j<N;j++) {
//					System.out.print(letterList.get(i).dpPath[k].get(j));
//				}
//				System.out.println();
//			}
		}
		return letterList.get(mini).letter;
	}
	public double evalLetter(int index) {
		double penalty = 0;
		double d[] = new double[4]; //방향각
		double l[] = new double[4]; //길이 비
		for(int k=0;k<letterList.get(index).type+1;k++) { //초성, 중성, 종성
			int M = dbList.get(letterList.get(index).phonemeIndex[k]).pattern.size(); //기준
			int N = letterList.get(index).dpPath[k].size(); //입력
			int M2 = -1;
			for(int i=0;i<N;i++) {
				int x = letterList.get(index).dpPath[k].get(i).x;
				int y = letterList.get(index).dpPath[k].get(i).y;
				M2 = Math.max(M2, x);
				M2 = Math.max(M2, y);
			}
			M2 = M2 + 1; //개수는 인덱스 + 1
			
			if(M <= M2) {
				double I[] = new double[M];
				for(int i=0;i<M;i++) {
					I[i] = dbList.get(letterList.get(index).phonemeIndex[k]).pattern.get(i);
				}
				@SuppressWarnings("unchecked")
				ArrayList<Double>[] R = (ArrayList<Double>[]) new ArrayList[M];
				for(int i=0;i<M;i++) {
					R[i] = new ArrayList<Double>();
				}
				@SuppressWarnings("unchecked")
				ArrayList<Double>[] L = (ArrayList<Double>[]) new ArrayList[M];
				for(int i=0;i<M;i++) {
					L[i] = new ArrayList<Double>();
				}
				
				double I2[] = new double[M];
				double sumInput = 0, sumStd = 0;
				int prev_x = 0, prev_y = 0;
				int pos = 0;
				for(int i=0;i<M;i++) {
					sumStd += dbList.get(letterList.get(index).phonemeIndex[k]).lengthList.get(i);
				}
				for(int i=0;i<M;i++) {
					I2[i] = dbList.get(letterList.get(index).phonemeIndex[k]).lengthList.get(i) / sumStd;
				}
				
				int lastIndex = letterList.get(index).dpPath[k].size() - 1;
				int delay = letterList.get(index).dpPath[k].get(0).x;
				for(int i=delay;i<=letterList.get(index).dpPath[k].get(lastIndex).x;i++) {
					sumInput += letterList.get(index).lengthArr[i];
				}
				
//				double partSum = 0;
//				double partIndex = 0;
				for(int i=0;i<N;i++) {
					int x = letterList.get(index).dpPath[k].get(i).x;
					int y = letterList.get(index).dpPath[k].get(i).y;
					if(prev_y != y) {
						pos++;
					} else {
						//partSum += dbList.get(letterList.get(index).phonemeIndex[k]).lengthList.get(x);
					}
					R[pos].add(letterList.get(index).inputPattern[x]);
					L[pos].add(letterList.get(index).lengthArr[x] / sumInput);
					prev_x = x;
					prev_y = y;
				}
				double temp = 0;
				for(int i=0;i<M;i++) {
					for(int j=0;j<R[i].size();j++) {
						temp += L[i].get(j) * subAngle(R[i].get(j), I[i]);
					}
				}
				d[k] = temp;
				
				temp = 0;
				for(int i=0;i<M;i++) {
					double temp2 = 0;
					for(int j=0;j<L[i].size();j++) {
						temp2 += L[i].get(j);
					}
					temp += Math.abs(I2[i] - temp2);
				}
				l[k] = temp;
				
			} else {
				System.out.println("크리티컬 에러");
			}
//			System.out.println(d[k]+","+l[k]);
			penalty += d[k] + 0.3 * l[k];
		}
		penalty /= letterList.get(index).type+1;
		return penalty;
	}
//	public int isRight(double a, double b) {
//		int ret = 0;
//		if(a < 180) {
//			if(a<b && b<a+180) ret = -1;
//			else if(a == b || a + 180 == b) ret = 0;
//			else ret = 1;
//		} else {
//			if(a-180<b && b<a) ret = 1;
//			else if(a == b || a - 180 == b) ret = 0;
//			else ret = -1;
//		}
//		return ret;
//	}
	public double distancePoints(int dx, int dy) {
		return Math.sqrt(dx*dx + dy*dy);
	}
	public boolean DbReload() {
		dbList.clear();
		if(!readFile(0)) return false;
		if(!readFile(1)) return false;
		if(!readFile(2)) return false;
		return true;
	}
}
