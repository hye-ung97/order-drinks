# order-drinks

## **프로젝트 주제**

음료를 주문 할 수 어플리케이션

## **프로젝트 기획 배경 (목적)**

- 어플에서만 이용가능했던 사이렌 오더를 보며 웹으로 구현해보고 싶다 라는 생각에서 하게 되었습니다.
- 스타벅스 사이렌오더와 유사한 온라인으로 음료 주문 가능한 웹 어플리케이션입니다.
- 스프링부트를 이용하여 웹페이지 제작하는 것을 학습하여 익히고자 합니다.
- 스프링부트에 외부 api 를 적용하여 작동되는 것을 실습 하고자 합니다.

## 프로젝트 구조
![projectStructure](https://user-images.githubusercontent.com/117243197/227930328-86d09620-78dc-4f44-bf3e-d0a8b3a3d7c2.png)

## ERD

![drawio.png](https://user-images.githubusercontent.com/117243197/227930525-39ddc0e2-f7bb-4782-a720-1e6dbe75f94e.png)

## 사용 기술스택

- SpringBoot
- Java
- MySQL
- Google Map api
- Swagger

## 프로젝트 기능

**[회원]**

- 회원가입 / 이메일 인증
- 회원정보 수정 / 탈퇴
- 카드 잔액 보기 / 별 확인
- 카드 충전
- 주문 이력

**[결제]**

- 쿠폰 / 카드
- 별적립 (12개가 되었을 때는 무료 음료 쿠폰으로 교환 / 카드 결제 시 적립가능)

**[주문]**

- 현재 위치에서 가까운 지점 추천 (Google map api 이용)
- 음료 선택
- 제작 완료 후 알림 (admin 계정이 음료 제작 완료 상태로 변경시 sse 를 이용하여 알림)

**[관리자]**

- 회원 관리
- 음료 관리
- 지점별 매출 확인


## API 명세서

http://localhost:8080/swagger-ui/index.html
