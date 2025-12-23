<a href="https://club-project-one.vercel.app/" target="_blank">
<img src="https://raw.githubusercontent.com/clem0927/Parking-project/main/assets/poster.png" alt="프로젝트 배너" width="100%">

</a>

<br/>
<br/>

# 0. Getting Started (시작하기)
```bash
$ npm start
```
[서비스 링크](https://club-project-one.vercel.app/)

<br/>
<br/>

# 1. Project Overview (프로젝트 개요)
- 프로젝트 이름:Ezpark
- 프로젝트 소제목:서울시 공영 주차장 추천 및 실시간 여석정보 제공 웹
- 프로젝트 설명: 서울시 공영 주차장들의 실시간 여석 정보를 제공하고,내비게이션 안내 중 실시간으로 주변 주차장을 추천 받거나 여석을 기반으로 주차장을 예약하는 "주차 보조 서비스"

<br/>
<br/>

# 2. Team Members (팀원 및 팀 소개)

| 김민영 | 김순호 |
|:------:|:------:|
| <img src="https://raw.githubusercontent.com/clem0927/Parking-project/main/assets/토끼.png" alt="김민영" width="150"> | <img src="https://github.com/user-attachments/assets/78ec4937-81bb-4637-975d-631eb3c4601e" alt="김순호" width="150"> |
| **PL** | **FE** |
| [GitHub](https://github.com/clem0927) | [GitHub](https://github.com/20201147-cyber) |
| <ul><li>프로젝트 기획 및 관리</li><li>아키텍처 설계</li><li>백엔드 서버 구축</li></ul> | <ul><li>프론트엔드 UI 구현</li><li>페이지별 기능 개발</li></ul> |

<br/>
<br/>


# 3. Key Features (주요 기능)
- **회원가입 및 로그인**:
  - 스프링 시큐리티를 활용한 회원가입 및 로그인

- **주차장 실시간 여석 정보 제공**:
  - 외부 API를 통한 실시간 여석수 제공

- **주차장 현황 시각화 자료 제공**:
  - 이전주 같은 요일의 주차장 현황 그래프 제공

- **주차장 도착 시점 여석 수 예측**:
  - Github Actions를 활용해 주차장 현황 과거데이터 csv파일 수집
  - 과거데이터를 다항회귀분석해 도착시점의 여석 수를 예측

- **목적지 안내 중 주변 주차장 추천**:
  - 내비게이션 안내를 취소하지 않고 실시간으로 주변 주차장을 추천 및 해당 주차장으로 안내 변경 가능

- **여석 기반 예약 기능**:
  - 주차장을 시간권으로 예약
  - 예약된 주차장중 여석이 부족한 주차장은 주변에 여석이 있는 주차장 추천 기능 활성화
**주차장 관리자 페이지**:
  - 자신이 관리하는 주차장 등록 및 대시보드 확인.
<br/>
<br/>


# 4. Technology Stack (기술 스택)
## 4.1 개발환경
<img src="https://raw.githubusercontent.com/clem0927/Parking-project/main/assets/개발환경.png" alt="프로젝트 배너" width="60%">

## 4.1 외부 API
<ul>
  <li>1.서울시 시영주차장 실시간 주차대수 API(서울 열린 데이터광장)</li>
  <li>2.카카오맵 API</li>
  <li>3.SK Open API</
