/*
 * ������ �������� ���� ��ġ�� ������ �����ϴ� �ڷᱸ��
 */
public class PiecePrevPosition {
	public int myX, myY; //�ڽ��� �� ��ġ
	public int opX, opY; //��� �� ��ġ
	public int myPieceNo; //�ڽ��� ���� ���� ��ȣ
	public int opPieceNo; //��� �� ����
	public int drawCnt; //�������°� 3ȸ �̻� ���ӵǸ� ���º�
	
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
