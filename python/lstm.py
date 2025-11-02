import pandas as pd
import numpy as np
import os
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import LSTM, Dense
from sklearn.preprocessing import MinMaxScaler
import joblib
from glob import glob

# ===============================
# 설정
# ===============================
steps_ahead = 18  # multi-step 출력 (10분 단위)
base_model_dir = "./models"
base_scaler_dir = "./scalers"
os.makedirs(base_model_dir, exist_ok=True)
os.makedirs(base_scaler_dir, exist_ok=True)

# ===============================
# 데이터 준비 (한 주차장, 요일별 전체) - feature 추가 + 요일 One-hot
# ===============================
def prepare_weekday_data(target_cd, weekday, file_pattern="../parking_data/202510*.csv"):
    files = glob(file_pattern)
    dfs = [pd.read_csv(f) for f in files]
    data = pd.concat(dfs, ignore_index=True)
    data = data[data["PKLT_CD"] == target_cd]
    data["timestamp"] = pd.to_datetime(data["timestamp"])
    data = data[data["timestamp"].dt.weekday == weekday]

    # 06~25시만
    data = data[(data["timestamp"].dt.hour >= 6) & (data["timestamp"].dt.hour <= 25)]
    data = data.sort_values("timestamp").set_index("timestamp")
    data = data[["remainCnt","liveCnt"]].resample("10min").mean().interpolate()

    # 시간 인코딩
    minutes_total = data.index.hour*60 + data.index.minute
    data["time_sin"] = np.sin(2*np.pi*minutes_total/1440)
    data["time_cos"] = np.cos(2*np.pi*minutes_total/1440)

    # 추가 feature: 변화량 + 이동평균(추세)
    data["delta_remain"] = data["remainCnt"].diff().fillna(0)
    data["remain_trend"] = data["remainCnt"].rolling(window=3).mean().fillna(method='bfill')

    # 요일 One-hot (weekday 0~6)
    weekday_cols = [f"wd_{i}" for i in range(7)]
    weekday_onehot = pd.DataFrame(np.zeros((len(data), 7)), columns=weekday_cols, index=data.index)
    weekday_onehot[f"wd_{weekday}"] = 1
    data = pd.concat([data, weekday_onehot], axis=1)

    # 최종 feature
    feature_cols = ["remainCnt","liveCnt","time_sin","time_cos","delta_remain","remain_trend"] + weekday_cols
    return data[feature_cols].values

# ===============================
# 시퀀스 생성 (multi-step)
# ===============================
def create_dataset(data, steps_ahead):
    X, y = [], []
    for i in range(len(data) - steps_ahead):
        X.append(data[i:i+steps_ahead])
        y.append(data[i:i+steps_ahead,0])  # remainCnt multi-step
    return np.array(X), np.array(y)

# ===============================
# 모든 주차장 코드 학습
# ===============================
file_list = glob("../parking_data/202510*.csv")
all_data = pd.concat([pd.read_csv(f) for f in file_list], ignore_index=True)
target_list = all_data["PKLT_CD"].unique()

for target_cd in target_list:
    print(f"\n=== 주차장 {target_cd} 학습 시작 ===")

    # 주차장별 디렉터리 생성
    model_dir = os.path.join(base_model_dir, str(target_cd))
    scaler_dir = os.path.join(base_scaler_dir, str(target_cd))
    os.makedirs(model_dir, exist_ok=True)
    os.makedirs(scaler_dir, exist_ok=True)

    for weekday in range(7):
        print(f"--- 요일 {weekday} 학습 중 ---")
        # 데이터 준비
        data_values = prepare_weekday_data(target_cd, weekday)

        # 스케일링
        scaler = MinMaxScaler()
        scaled = scaler.fit_transform(data_values)

        # 시퀀스 생성
        X, y = create_dataset(scaled, steps_ahead)

        # 모델 정의
        model = Sequential([
            LSTM(128, input_shape=(steps_ahead, X.shape[2])),
            Dense(64, activation='relu'),
            Dense(steps_ahead)  # multi-step 출력
        ])
        model.compile(optimizer='adam', loss='mse')

        # 학습 (에폭 30)
        model.fit(X, y, epochs=30, batch_size=32, verbose=1)

        # 저장
        model_path = os.path.join(model_dir, f"{target_cd}_weekday{weekday}.keras")
        scaler_path = os.path.join(scaler_dir, f"{target_cd}_weekday{weekday}.save")
        model.save(model_path)
        joblib.dump(scaler, scaler_path)
        print(f"✅ 완료: 주차장 {target_cd}, 요일 {weekday}")
