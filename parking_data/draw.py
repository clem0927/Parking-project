import pandas as pd
import matplotlib.pyplot as plt
# 한글 폰트 설정 (Windows: 맑은 고딕)
plt.rc('font', family='Malgun Gothic')

# 마이너스 깨짐 방지
plt.rcParams['axes.unicode_minus'] = False
# CSV 읽기
df = pd.read_csv("20250922.csv")

# timestamp를 datetime 형식으로 변환
df['timestamp'] = pd.to_datetime(df['timestamp'])

# "종묘주차장"만 필터링
jongmyo_df = df[df['PKLT_NM'].str.contains("종묘주차장")]

# 시간순으로 정렬
jongmyo_df = jongmyo_df.sort_values('timestamp')

# 그래프 그리기
plt.figure(figsize=(10, 5))
plt.plot(jongmyo_df['timestamp'], jongmyo_df['liveCnt'], marker='o', label="주차된 차량(liveCnt)")
plt.plot(jongmyo_df['timestamp'], jongmyo_df['remainCnt'], marker='s', label="남은 자리(remainCnt)")

plt.xlabel("시간")
plt.ylabel("차량 수")
plt.title("종묘주차장 시간별 자리 변화")
plt.legend()
plt.grid(True)
plt.xticks(rotation=45)

plt.tight_layout()
plt.show()
