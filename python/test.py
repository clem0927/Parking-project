import pandas as pd
import numpy as np
from glob import glob
from datetime import datetime, timedelta

# ===============================
# 설정
# ===============================
target_cd = 171730
predict_minutes_list = [5, 10, 20, 30, 100]  # 예측 단위
file_pattern = "../parking_data/202510*.csv"

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
data = data[["remainCnt"]].resample("10min").mean()

# ===============================
# 현재 시각 기준 예측
# ===============================
now_time = datetime.now().replace(second=0, microsecond=0)
# 지금이 20:18이면 20:10까지 평균 계산
last_slot_time = now_time - timedelta(minutes=now_time.minute % 10)

for pm in predict_minutes_list:
    # 예측 시점
    future_time = last_slot_time + timedelta(minutes=pm)

    # 같은 요일, 같은 시간대 평균
    hour = future_time.hour
    minute_slot = (future_time.minute // 10) * 10
    mask = (data.index.hour == hour) & (data.index.minute // 10 == minute_slot // 10)
    predicted = data[mask]["remainCnt"].mean()

    # 값이 없으면 직전 시간대 평균 사용
    if np.isnan(predicted):
        predicted = data[(data.index.hour == hour) & (data.index.minute // 10 == (minute_slot // 10 - 1))]["remainCnt"].mean()

    print(f"⏱ 약 {pm}분 후 예상 여석: {round(predicted,1)}")
