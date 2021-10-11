# 한글 필기체 인식기

![3_pre](https://user-images.githubusercontent.com/83110819/131841358-056a8eb5-8309-4c1e-b9f9-ef6d2aff4b9f.png)

> 실시간 한글 필기체 정보를 입력 받아 흘림체를 포함한 한글 필기체 글자 하나를 인식하는 자바기반 GUI 프로그램, 핵심 알고리즘은 논문 두세 편을 참고하여 구현

## 시연 영상
`시연 영상1` : <https://www.youtube.com/watch?v=3UoUnyWYk0k>
`시연 영상2` : <https://www.youtube.com/watch?v=_tPfyUpuRsg>


## 파일 설명
- 소스 파일 : ShowData.java, DPmatching.java, DbClass.java
- 실행 파일 : ShowData.jar (규칙 데이터 파일 3개와 같은 폴더에 있어야 한다)
- 규칙 데이터 파일 : chosung.txt, jungsung.txt, jongsung.txt
- 학습 데이터 파일 : train data 폴더 내 txt 파일


## 인터페이스 설명
![3_preview](https://user-images.githubusercontent.com/83110819/131812503-1e264b31-4059-4787-a2c6-0e957b7901ce.png)

왼쪽 화면에 마우스로 한글을 적고, 오른쪽 화면에 그 한글에 대해 획마다 색을 다르게 하고 특징점을 빨간점으로 표시한 뒤, 한글을 인식한다.
- ① : 왼쪽 화면을 지우는 버튼
- ② : DB를 다시 불러오는 버튼
- ③ : 왼쪽 화면에 적은 문자를 오른쪽 화면에 나타내는 버튼 (특징점과 획 별로 색을 나타냄)
- ④ : 인식하고 싶은 파일 (실습 초반에 제출했던 형식의 파일) (반드시 ANSI 인코딩으로 되어 있어야 함)
- ⑤ : ④의 파일을 불러들이는 버튼
- ⑥ : 초성, 중성, 종성을 학습시키는 버튼
- ⑦ : ④의 파일의 몇 번째 문자인지 나타냄 (입력 가능)
- ⑧ : ④의 파일의 문자를 보기 위해 이동하는 버튼
- ⑨
  + Recognize : 현재 오른쪽 화면에 나타난 문자를 인식하고 결과는 ⑬에 나타남 (자동으로 평활화한 뒤 인식)
  + Auto Recognize : ④의 파일의 모든 문자를 자동으로 인식하는 버튼. 결과는 팝업창으로 나타남 (로그 사용을 체크하면 로그 파일에서도 확인 가능)
  + ALL Recognize : 파일을 여러 개 선택 가능한 팝업창이 뜨고, 선택한 모든 파일에 대해 각각의 파일마다 모든 문자를 자동으로 인식하는 버튼. 결과는 팝업창으로 나타남 (로그 사용을 체크하면 로그 파일에서도 확인 가능)
- ⑩ : 오른쪽 화면을 지우는 버튼
- ⑪ : 오른족 화면의 문자를 평활화(Smooth)하는 버튼
- ⑫ : ④의 파일에 적혀 있는 정답
- ⑬ : 인식 결과


 #### [체크 버튼]
- Show points: ④의 파일에 있는 좌표점을 마젠타 색으로 표시
- Show feature points: 특징점을 빨간점으로 표시
- 로그 사용: 체크하면 Auto Recognize, ALL Recognize 버튼을 눌렀을 때의 인식 결과를 로그 파일 recognition_log.txt, recognition_log_simple.txt 에 기록을 남김
- 로그 누적: 체크하면 recognition_log.txt, recognition_log_simple.txt의 로그 파일을 덮어쓰지 않고 이어서 씀


## 현재 사용자가 마우스로 입력한 한글 인식 확인법
1. 왼쪽 빈 공간에 마우스로 한글 한 글자를 입력
2. 왼쪽 맨 아래 버튼 'Move to right' 클릭
3. 오른쪽 파란색 버튼 'Recognize' 클릭

* 글자를 지우기 위해서는 'Clear' 버튼 클릭


## 학습 데이터에 대한 평가 확인법
[수동 확인]
1. ShowData.jar 를 실행 (chosung.txt, jungsung.txt, jongsung.txt 파일이 같은 폴더에 있어야 함)
2. 오른쪽 첫번째 텍스트 박스에 인식하고 싶은 파일 경로를 입력
3. 바로 옆 Load 버튼을 클릭 (Prev, Next 버튼 사용 가능)
4. 하늘색 버튼(Recognition)을 클릭
5. 오른쪽 위는 파일에 적힌 한글(정답), 오른쪽 아래는 인식 결과

[자동 확인]
1. ShowData.jar 를 실행 (chosung.txt, jungsung.txt, jongsung.txt 파일이 같은 폴더에 있어야 함)
2. 초록색 버튼(ALL Recognize)을 클릭
3. 인식하고 싶은 파일을 선택 (여러 개 선택 가능)
4. 팝업창으로 결과가 나타남
5. 초록색 버튼(ALL Recognize)을 누르기 전에 로그 파일 2개를 지우고, 로그 사용, 로그 누적을 체크하면 결과가 로그 파일에 자동으로 기록됨 (하나는 상세한 로그, 다른 하나는 간단한 로그)


## Reference
>  - 심동규, 함영국, 박래홍. "DP 매칭과 퍼지이론을 이용한 흘림체 온라인 한글인식 (Recognition of Cursive Korean Characters Using DP Matching and Fuzzy Theory)." 전자공학회논문지B 30.4 (1993): 369-382.
>  - 최영규, 이상범. "화상 및 음성처리: 필기체 한글 문자 인식을 위한 획 추출에 관한 연구." 정보처리학회논문지B 9.3 (2002): 375-382.
>  - 김태균, 이은주. "한글에 적합한 획 해석에 의한 연속 필기 한글의 On-line 인식에 관한 연구." (구)정보과학회논문지 15.3 (1988): 171-181.

