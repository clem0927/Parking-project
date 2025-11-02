import pandas as pd
import numpy as np
import os
import joblib
from tensorflow.keras.models import load_model
from datetime import datetime, timedelta

# ===============================
# 설정
# ===============================
target_cd = 171721
predict_minutes_list = [5, 10, 15, 30, 55, 60]  # 예측 단위
csv_today = f"../parking_data/{datetime.today().strftime('%Y%m%d')}.csv"
csv_lastweek = f"../parking_data/{(datetime.today() - pd.Timedelta(days=7)).strftime('%Y%m%d')}.csv"
base_model_dir = "./models"
base_scaler_dir = "./scalers"
steps_ahead = 18  # LSTM 입력 길이

# ===============================
# 오늘 요일 확인
# ===============================
weekday = datetime.today().weekday()

# 모델/스케일러 경로
model_dir = os.path.join(base_model_dir, str(target_cd))
scaler_dir = os.path.join(base_scaler_dir, str(target_cd))
model_path = os.path.join(model_dir, f"{target_cd}_weekday{weekday}.keras")
scaler_path = os.path.join(scaler_dir, f"{target_cd}_weekday{weekday}.save")

# ===============================
# 모델/스케일러 불러오기
# ===============================
model = load_model(model_path, compile=False)
scaler = joblib.load(scaler_path)

# ===============================
# 데이터 준비 함수
# ===============================
def prepare_df(df):
    df = df[df["PKLT_CD"] == target_cd]
    df["timestamp"] = pd.to_datetime(df["timestamp"])
    df = df.sort_values("timestamp").set_index("timestamp")
    df = df[["remainCnt","liveCnt"]].resample("10min").mean().interpolate()

    # 시간 인코딩
    minutes_total = df.index.hour*60 + df.index.minute
    df["time_sin"] = np.sin(2*np.pi*minutes_total/1440)
    df["time_cos"] = np.cos(2*np.pi*minutes_total/1440)

    # 추가 feature
    df["delta_remain"] = df["remainCnt"].diff().fillna(0)
    df["remain_trend"] = df["remainCnt"].rolling(3, min_periods=1).mean()

    # 요일 One-hot
    weekday_cols = [f"wd_{i}" for i in range(7)]
    weekday_onehot = pd.DataFrame(np.zeros((len(df), 7)), columns=weekday_cols, index=df.index)
    weekday_onehot[f"wd_{weekday}"] = 1
    df = pd.concat([df, weekday_onehot], axis=1)

    # feature 순서
    feature_cols = ["remainCnt","liveCnt","time_sin","time_cos","delta_remain","remain_trend"] + weekday_cols
    return df[feature_cols]

# 오늘/지난주 데이터 준비
df_today = prepare_df(pd.read_csv(csv_today))
df_lastweek = prepare_df(pd.read_csv(csv_lastweek))

# 오늘 현재 시점까지
now_time = df_today.index.max()
df_today_recent = df_today[df_today.index <= now_time]

# 합치기
df_combined = pd.concat([df_lastweek, df_today_recent]).sort_index()
features = df_combined.values
scaled = scaler.transform(features)

# ===============================
# multi-step 예측 함수
# ===============================
def predict_multi_step(model, scaled, predict_minutes):
    step_full = predict_minutes // 10
    step_rem = predict_minutes % 10
    feature_len = scaled.shape[1]

    pred_scaled = scaled.reshape(1, scaled.shape[0], feature_len).copy()

    for _ in range(step_full):
        next_pred = model.predict(pred_scaled, verbose=0)[0,0]
        next_pred = max(0, next_pred)

        # 시퀀스 업데이트
        next_feature = np.zeros((1,1,feature_len))
        last_vals = pred_scaled[0,-1,:].copy()
        next_feature[0,0,0] = next_pred  # remainCnt 예측값
        next_feature[0,0,1:] = last_vals[1:]  # 나머지 feature는 그대로
        pred_scaled = np.concatenate([pred_scaled, next_feature], axis=1)

    # 남은 분 보간
    if step_rem > 0:
        last_val = pred_scaled[0,-1,0]
        prev_val = pred_scaled[0,-2,0]
        pred_final_scaled = prev_val + (last_val - prev_val)*(step_rem/10)
    else:
        pred_final_scaled = pred_scaled[0,-1,0]

    # 역변환
    inv_input = np.zeros((1, feature_len))
    inv_input[0,0] = pred_final_scaled
    pred_final = scaler.inverse_transform(inv_input)[0,0]
    pred_final = max(0, pred_final)
    return round(pred_final,1)

# ===============================
# 예측 실행
# ===============================
for pm in predict_minutes_list:
    predicted = predict_multi_step(model, scaled, pm)
    print(f"⏱ 약 {pm}분 후 예상 여석: {predicted}")
