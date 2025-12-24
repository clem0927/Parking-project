# src/predict/routes.py

from flask import Blueprint, request, jsonify
import pandas as pd
import numpy as np
from glob import glob
from datetime import datetime, timedelta
from sklearn.linear_model import LinearRegression
from sklearn.preprocessing import PolynomialFeatures
from sklearn.pipeline import make_pipeline

# ===============================
# Blueprint 생성
# ===============================
predict_bp = Blueprint("predict", __name__)

# ===============================
# 설정
# ===============================
file_pattern = "../../parking_data/2025*.csv"
use_polynomial = True   # True면 다항 회귀, False면 단순 선형 회귀
poly_degree = 2         # 다항 차수
lookback_hours = 2      # 예측 시각 ± 몇 시간 범위 사용할지

# ===============================
# 상세 분석
# ===============================
@predict_bp.route("/parking_data", methods=["GET"])
def get_parking_data():
    today = datetime.today()
    last_week_date = today - timedelta(days=7)

    # 오늘 요일 기준으로 지난주 같은 요일
    diff = today.weekday() - last_week_date.weekday()
    last_week_date = last_week_date + timedelta(days=diff)

    file_name = f"../../parking_data/{last_week_date.strftime('%Y%m%d')}.csv"

    try:
        df = pd.read_csv(file_name)
        df = df[["PKLT_NM", "timestamp", "liveCnt", "remainCnt"]]
        return jsonify(df.to_dict(orient="records"))
    except Exception as e:
        return jsonify({"error": str(e)}), 500


# ===============================
# 주차 여석 예측
# ===============================
@predict_bp.route("/predict_remain", methods=["POST"])
def predict_remain():
    data_json = request.get_json()

    if not data_json:
        return jsonify({"error": "JSON body required"}), 400

    target_cd = str(data_json.get("target_cd"))
    minutes_ahead = data_json.get("minutesAhead")

    if target_cd is None or minutes_ahead is None:
        return jsonify({"error": "target_cd and minutesAhead required"}), 400

    try:
        minutes_ahead = int(minutes_ahead)
    except ValueError:
        return jsonify({"error": "minutesAhead must be an integer"}), 400

    # ===============================
    # CSV 로드
    # ===============================
    files = glob(file_pattern)
    if not files:
        return jsonify({"error": "No CSV files found"}), 500

    dfs = []
    for f in files:
        try:
            df = pd.read_csv(f)
            if {"PKLT_CD", "timestamp", "remainCnt"}.issubset(df.columns):
                dfs.append(df)
        except Exception as e:
            print(f"⚠️ CSV load error in {f}: {e}")

    if not dfs:
        return jsonify({"error": "No valid CSV data"}), 500

    data = pd.concat(dfs, ignore_index=True)

    # ===============================
    # 타입 변환 & 필터링
    # ===============================
    data["PKLT_CD"] = data["PKLT_CD"].astype(str)
    data["timestamp"] = pd.to_datetime(data["timestamp"], errors="coerce")
    data = data.dropna(subset=["timestamp"])

    today = datetime.today()
    today_date = today.date()
    target_weekday = today.weekday()

    data = data[data["PKLT_CD"] == target_cd]
    data = data[(data["timestamp"].dt.hour >= 6) & (data["timestamp"].dt.hour <= 25)]

    data = data.sort_values("timestamp").set_index("timestamp")
    data = data[["remainCnt"]].resample("10min").mean().interpolate()
    data["weekday"] = data.index.weekday
    data["minutes"] = data.index.hour * 60 + data.index.minute

    if data.empty or data["remainCnt"].isna().all():
        return jsonify({"error": "No valid remainCnt data"}), 400

    # ===============================
    # 예측 시각 계산
    # ===============================
    now_time = datetime.now().replace(second=0, microsecond=0)
    future_time = now_time + timedelta(minutes=minutes_ahead)
    future_minute = future_time.hour * 60 + future_time.minute

    start_minute = future_minute - lookback_hours * 60
    end_minute = future_minute + lookback_hours * 60

    # ===============================
    # 과거 같은 요일 + 오늘 데이터 필터링
    # ===============================
    past_same_weekday = data[
        (data["weekday"] == target_weekday) &
        (data.index.date != today_date) &
        (data["minutes"] >= start_minute) &
        (data["minutes"] <= end_minute)
        ]

    today_data = data[
        (data.index.date == today_date) &
        (data["minutes"] >= start_minute) &
        (data["minutes"] <= end_minute)
        ]

    train_data = pd.concat([past_same_weekday, today_data])

    print(train_data.head(50))
    print(train_data.tail(50))

    # ===============================
    # 이동평균 스무싱
    # ===============================
    train_data["remainCnt_smooth"] = (
        train_data["remainCnt"]
        .rolling(3, min_periods=1)
        .mean()
    )

    X = train_data["minutes"].values.reshape(-1, 1)
    y = train_data["remainCnt_smooth"].values

    # ===============================
    # 최근 데이터 가중치 적용
    # ===============================
    weights = 1 + 10 / (future_minute - train_data["minutes"] + 1)
    weights = np.clip(weights, 1, 10)

    if use_polynomial:
        model = make_pipeline(
            PolynomialFeatures(poly_degree),
            LinearRegression()
        )
        model.fit(X, y, linearregression__sample_weight=weights)
    else:
        model = LinearRegression()
        model.fit(X, y, sample_weight=weights)

    # ===============================
    # 예측
    # ===============================
    predicted = model.predict(np.array([[future_minute]]))[0]
    predicted = max(0, round(predicted, 1))

    print("\n✅ 예측 결과:")
    print(f"  - 예측 target_cd: {target_cd}")
    print(f"  - 예측 시각: {future_time.strftime('%Y-%m-%d %H:%M')}")
    print(f"  - 예측 여석수(predictedRemain): {predicted}")
    print(f"  - 사용된 데이터 개수: {len(train_data)}\n")

    return jsonify({
        "predictedRemain": predicted,
        "target_cd": target_cd,
        "future_time": future_time.strftime("%Y-%m-%d %H:%M"),
        "weekday": target_weekday,
        "count": len(train_data)
    })
