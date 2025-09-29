import pandas as pd

# CSV 읽기 (헤더가 없다고 가정)
df = pd.read_csv("20250923.csv", header=None)

# 첫 번째 컬럼(날짜+시간)에서 날짜 부분만 바꿔주기
df[0] = df[0].str.replace(r'(\d{4}-\d{2})-23', r'\1-22', regex=True)

# 결과 저장
df.to_csv("20250923m", index=False, header=False)
