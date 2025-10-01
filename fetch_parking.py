import requests
import pandas as pd
from datetime import datetime, timedelta
from pathlib import Path

SAVE_DIR = Path("parking_data")
SAVE_DIR.mkdir(exist_ok=True)

def fetch_and_save():
    try:
        url = "http://openapi.seoul.go.kr:8088/56776f4f766b696d3335704f6b434d/json/GetParkingInfo/1/1000/"
        response = requests.get(url)
        data = response.json()

        realtime_list = data.get("GetParkingInfo", {}).get("row", [])

        # ✅ UTC → KST(+9) 변환
        kst_now = datetime.utcnow() + timedelta(hours=9)
        timestamp = kst_now.strftime("%Y-%m-%d %H:%M:%S")

        records = []
        for park in realtime_list:
            now_cnt = int(park.get("NOW_PRK_VHCL_CNT", 0))
            total_cnt = int(park.get("TPKCT", 0))
            remain_cnt = max(total_cnt - now_cnt, 0)

            records.append({
                "timestamp": timestamp,
                "PKLT_CD": park.get("PKLT_CD"),
                "PKLT_NM": park.get("PKLT_NM"),
                "liveCnt": now_cnt,
                "remainCnt": remain_cnt
            })

        today = kst_now.strftime("%Y%m%d")
        csv_file = SAVE_DIR / f"{today}.csv"
        df = pd.DataFrame(records)

        if not csv_file.exists():
            df.to_csv(csv_file, index=False, encoding="utf-8-sig")
            print(f"📁 새 파일 생성: {csv_file}")
        else:
            df.to_csv(csv_file, mode="a", header=False, index=False, encoding="utf-8-sig")
            print(f"✏️ 기존 파일에 추가: {csv_file}")

        print(f"✅ {len(records)} rows saved at {timestamp}")

    except Exception as e:
        print("❌ Error:", e)


if __name__ == "__main__":
    fetch_and_save()