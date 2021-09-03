/*
 * 이전에 움직였던 말의 위치와 종류를 저장하는 자료구조
 */
public class PiecePrevPosition {
	public int myX, myY; //자신의 말 위치
	public int opX, opY; //상대 말 위치
	public int myPieceNo; //자신의 말의 고유 번호
	public int opPieceNo; //상대 말 종류
	public int drawCnt; //교착상태가 3회 이상 지속되면 무승부
	
	public PiecePrevPosition() {
		myX = myY = 0;
		opX = opY = 0;
		myPieceNo = opPieceNo = 8;
		drawCnt = 0;
	}
	public void reset() {
		myX = myY = 0;
		opX = opY = 0;
		myPieceNo = opPieceNo = 8;
		drawCnt = 0;
	}
}
