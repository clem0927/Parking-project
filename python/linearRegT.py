import pandas as pd
import numpy as np
from glob import glob
from datetime import datetime, timedelta
from sklearn.linear_model import LinearRegression
from sklearn.preprocessing import PolynomialFeatures
from sklearn.pipeline import make_pipeline

# ===============================
# 설정
# ===============================
target_cd = 171721
predict_minutes_list = [5, 15, 20, 30, 120]  # 예측 단위
file_pattern = "../parking_data/202510*.csv"
use_polynomial = True  # True면 다항 회귀, False면 단순 선형 회귀
poly_degree = 3        # 다항 차수

# ===============================
# 데이터 불러오기 & 같은 요일만
# ===============================
files = glob(file_pattern)
dfs = [pd.read_csv(f) for f in files]
data = pd.concat(dfs, ignore_index=True)
data["timestamp"] = pd.to_datetime(data["timestamp"])
weekday = datetime.today().weekday()
data = data[(data["PKLT_CD"] == target_cd) & (data["timestamp"].dt.weekday == weekday)]

# 06~25시만
data = data[(data["timestamp"].dt.hour >= 6) & (data["timestamp"].dt.hour <= 25)]
data = data.sort_values("timestamp").set_index("timestamp")

# 10분 단위 평균 + 결측치 보간
data = data[["remainCnt"]].resample("10min").mean().interpolate()

# ===============================
# 회귀용 데이터 준비
# ===============================
# X: 시간(분 단위, 06:00=360)
data["minutes"] = data.index.hour*60 + data.index.minute
X = data["minutes"].values.reshape(-1,1)
y = data["remainCnt"].values

# 회귀 모델 정의
if use_polynomial:
    model = make_pipeline(PolynomialFeatures(poly_degree), LinearRegression())
else:
    model = LinearRegression()

# 학습
model.fit(X, y)

# ===============================
# 현재 시각 기준 예측
# ===============================
now_time = datetime.now().replace(second=0, microsecond=0)
last_slot_time = now_time - timedelta(minutes=now_time.minute % 10)

for pm in predict_minutes_list:
    future_time = last_slot_time + timedelta(minutes=pm)
    future_minute = future_time.hour*60 + future_time.minute
    predicted = model.predict(np.array([[future_minute]]))[0]
    predicted = max(0, round(predicted,1))
    print(f"⏱ 약 {pm}분 후 예상 여석: {predicted}")
