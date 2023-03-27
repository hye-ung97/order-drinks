# order-drinks

## **프로젝트 주제**

음료를 주문 할 수 어플리케이션

## **프로젝트 기획 배경 (목적)**

- 어플에서만 이용가능했던 사이렌 오더를 보며 웹으로 구현해보고 싶다 라는 생각에서 하게 되었습니다.
- 스타벅스 사이렌오더와 유사한 온라인으로 음료 주문 가능한 웹 어플리케이션입니다.
- 스프링부트를 이용하여 웹페이지 제작하는 것을 학습하여 익히고자 합니다.
- 스프링부트에 외부 api 를 적용하여 작동되는 것을 실습 하고자 합니다.

## 프로젝트 구조
![projectStructure](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/1a79c21f-fd88-4283-9a57-767e730e6a32/Untitled.png)

## ERD

![무제.drawio (3).png](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/88c93283-18f9-494d-aa5b-26805fd8cfd6/%E1%84%86%E1%85%AE%E1%84%8C%E1%85%A6.drawio_(3).png)

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

### 1. 회원가입 api

- POST /auth/signup
- 파라미터 : 이메일, 비밀번호, 권한
- 정책 : 이미 등록되어 있는 이메일이라면 실패 응답 / 가입 완료 후 이메일로 인증 메일 전송 / 회원가입시 쿠폰 1장 제공
- 성공 응답 : 이메일, 패스워드(암호화), 권한
- 요청 / 응답 구조
  - 요청
    ```
    {
      "username" : "abc123@naver.com",
      "password" : "test1234!",
      "roles" : ["ROLE_USER", "ROLE_ADMIN"]
    }
    ```
  - 응답
    ```
    {
      "username": "abc123@naver.com",
      "password": "$2a$10$Op2RhqDMIKmKDNcgMAzSf.N60eV/J8nkU/ohi8uhW9wXPK6RSV0Ji",
      "roles": [
        "ROLE_USER",
        "ROLE_ADMIN"
      ]
    }
    ```

### 2. 로그인 api

- POST /auth/signin
- 파라미터 : 이메일, 비밀번호
- 정책 : 이메일 인증 완료된 회원만 로그인 가능
- 성공 응답 : token
- 요청 / 응답 구조
  - 요청
    ```
    {
      "username" : "abc123@naver.com",
      "password" : "test1234!"
    }
    ```
  - 응답
    ```
    eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhYmMxMjNAbmF2ZXIuY29tIiwicm9sZXMiOlsiUk9MRV9VU0VSIiwiUk9MRV9BRE1JTiJdLCJpYXQiOjE2Nzg2MjIyODksImV4cCI6MTY3ODYyNTg4OX0.9zmdZr6oJYivHpQmCrZC3JcropQmatPdGO7caYx5wqxzt3xXL6ubEGyNXOtefEZRVeU1uGWInAv9b9UlM5X9Fw
    ```

### 3. 카드 충전 api
- GET /member/charge
- 파라미터 : 금액
- 정책 : 현재 잔액에서 요청 금액 누적
- 성공 응답 : 금액, 충전 일자
- 요청 / 응답 구조
  - 요청
    ```
    GET /member/charge?price=xxxxx
    ex) /member/charge?price=5000
    ```
  - 응답
    ```
    {
      "id": 3,
      "price": 5000,
      "chargedDate": "2023-03-12T20:59:37.533544"
    }
    ```

### 4. wallet 보기 api
- GET /member/wallet
- 파라미터 : x
- 정책 : 인증 정보를 통해 사용자 정보를 가져온 뒤 해당 wallet 을 보여줌
- 성공 응답 : 카드, 포인트, 쿠폰
- 요청 / 응답 구조
  - 응답
    ```
    {
        "card": {
            "id": 3,
            "price": 5000,
            "chargedDate": "2023-03-12T20:59:37.533544"
            },
        "point": {
            "id": 3,
            "count": 0,
            "updatedDate": "2023-03-12T20:44:41.002705"
            },
        "coupon": {
            "id": 3,
            "count": 1,
            "updatedDate": "2023-03-12T20:44:40.995368"
            }
    }
    ```

### 5. 지점 정보 api
- GET /order/find-store
- 파라미터 : 주소
- 정책 : 입력 받은 주소 기준으로 Google Map Api 이용하여 가까운 지점 정보 응답
- 성공 응답 : 지점 이름, 지점 주소, 입력 받은 주소지에서 지점까지의 거리
- 요청 / 응답 구조
  - 요청
    ```
    GET /order/find-store?address=xxxxxxx
    ex) /order/find-store?address=서울특별시 영등포구 63로 50
    ```
  - 응답
    ```
    [
      {
        "storeName": "스타벅스 노량진역점",
        "address": "대한민국 노량진로 134 동작구 서울특별시 KR",
        "distance": "0.686 km"
      },
      {
        "storeName": "스타벅스 동여의도점",
        "address": "대한민국 서울특별시 영등포구 국제금융로 86",
        "distance": "0.741 km"
      }
    ]
    ```


  



### 5. 메뉴 등록 api
### 6. 음료 주문 api
### 7. 음료 상태 변경 api
### 8. 음료 상태별 리스트 api
### 9. 주문 완료 시 알림 api 
### 10. 지점 매출 및 주문 내역 api 
### 11. 모든 지점 매출 현황 api



## **프로젝트 주제**

음료를 주문 할 수 어플리케이션

## **프로젝트 기획 배경 (목적)**

- 어플에서만 이용가능했던 사이렌 오더를 보며 웹으로 구현해보고 싶다 라는 생각에서 하게 되었습니다.
- 스타벅스 사이렌오더와 유사한 온라인으로 음료 주문 가능한 웹 어플리케이션입니다.
- 스프링부트를 이용하여 웹페이지 제작하는 것을 학습하여 익히고자 합니다.
- 스프링부트에 외부 api 를 적용하여 작동되는 것을 실습 하고자 합니다.

## 프로젝트 구조
![projectStructure](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/1a79c21f-fd88-4283-9a57-767e730e6a32/Untitled.png)

## ERD

![무제.drawio (3).png](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/88c93283-18f9-494d-aa5b-26805fd8cfd6/%E1%84%86%E1%85%AE%E1%84%8C%E1%85%A6.drawio_(3).png)

## 사용 기술스택

- SpringBoot
- Java
- MySQL
- Google Map api

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
- 음료 종류 관리
- 지점별 매출 확인


## API 명세서

### 1. 회원가입 api

- POST /auth/signup
- 파라미터 : 이메일, 비밀번호, 권한
- 정책 : 이미 등록되어 있는 이메일이라면 실패 응답 / 가입 완료 후 이메일로 인증 메일 전송 / 회원가입시 쿠폰 1장 제공
- 성공 응답 : 이메일, 패스워드(암호화), 권한
- 요청 / 응답 구조
  - 요청
    ```
    {
      "username" : "abc123@naver.com",
      "password" : "test1234!",
      "roles" : ["ROLE_USER", "ROLE_ADMIN"]
    }
    ```
  - 응답
    ```
    {
      "username": "abc123@naver.com",
      "password": "$2a$10$Op2RhqDMIKmKDNcgMAzSf.N60eV/J8nkU/ohi8uhW9wXPK6RSV0Ji",
      "roles": [
        "ROLE_USER",
        "ROLE_ADMIN"
      ]
    }
    ```

### 2. 로그인 api

- POST /auth/signin
- 파라미터 : 이메일, 비밀번호
- 정책 : 이메일 인증 완료된 회원만 로그인 가능
- 성공 응답 : token
- 요청 / 응답 구조
  - 요청
    ```
    {
      "username" : "abc123@naver.com",
      "password" : "test1234!"
    }
    ```
  - 응답
    ```
    eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhYmMxMjNAbmF2ZXIuY29tIiwicm9sZXMiOlsiUk9MRV9VU0VSIiwiUk9MRV9BRE1JTiJdLCJpYXQiOjE2Nzg2MjIyODksImV4cCI6MTY3ODYyNTg4OX0.9zmdZr6oJYivHpQmCrZC3JcropQmatPdGO7caYx5wqxzt3xXL6ubEGyNXOtefEZRVeU1uGWInAv9b9UlM5X9Fw
    ```

### 3. 카드 충전 api
- GET /member/charge
- 파라미터 : 금액
- 정책 : 현재 잔액에서 요청 금액 누적
- 성공 응답 : 금액, 충전 일자
- 요청 / 응답 구조
  - 요청
    ```
    GET /member/charge?price=xxxxx
    ex) /member/charge?price=5000
    ```
  - 응답
    ```
    {
      "id": 3,
      "price": 5000,
      "chargedDate": "2023-03-12T20:59:37.533544"
    }
    ```

### 4. wallet 보기 api
- GET /member/wallet
- 파라미터 : x
- 정책 : 인증 정보를 통해 사용자 정보를 가져온 뒤 해당 wallet 을 보여줌
- 성공 응답 : 카드, 포인트, 쿠폰
- 요청 / 응답 구조
  - 응답
    ```
    {
        "card": {
            "id": 3,
            "price": 5000,
            "chargedDate": "2023-03-12T20:59:37.533544"
            },
        "point": {
            "id": 3,
            "count": 0,
            "updatedDate": "2023-03-12T20:44:41.002705"
            },
        "coupon": {
            "id": 3,
            "count": 1,
            "updatedDate": "2023-03-12T20:44:40.995368"
            }
    }
    ```

### 5. 지점 정보 api
- GET /order/find-store
- 파라미터 : 주소
- 정책 : 입력 받은 주소 기준으로 Google Map Api 이용하여 가까운 지점 정보 응답
- 성공 응답 : 지점 이름, 지점 주소, 입력 받은 주소지에서 지점까지의 거리
- 요청 / 응답 구조
  - 요청
    ```
    GET /order/find-store?address=xxxxxxx
    ex) /order/find-store?address=서울특별시 영등포구 63로 50
    ```
  - 응답
    ```
    [
      {
        "storeName": "스타벅스 노량진역점",
        "address": "대한민국 노량진로 134 동작구 서울특별시 KR",
        "distance": "0.686 km"
      },
      {
        "storeName": "스타벅스 동여의도점",
        "address": "대한민국 서울특별시 영등포구 국제금융로 86",
        "distance": "0.741 km"
      }
    ]
    ```

### 6. 메뉴 등록 api
- POST /menu/register
- 파라미터 : 메뉴명, 금액
- 정책 : 등록되지 않은 메뉴만 응답 (admin 권한만 접근 가능)
- 성공 응답 : 메뉴명, 금액
- 요청 / 응답 구조
  - 요청
    ```
    {
      "menuName" : "프라프치노",
      "price" : 6100
    }
    ```
  - 응답
    ```
    {
      "menuName" : "프라프치노",
      "price" : 6100
    }
    ```

### 7. 메뉴보기 api

- GET /menu/list
- 파라미터 : x
- 정책 : db 에 저장된 메뉴 리스트를 응답
- 성공 응답 : 
- 요청 / 응답 구조
  - 요청
    ```
    GET /menu/list
    ```
  - 응답
    ```
    {
      "content": [
          {
              "menuName": "아메리카노",
              "price": 4100
          },
          {
              "menuName": "라떼",
              "price": 4500
          },
          {
              "menuName": "프라프치노",
              "price": 6100
          }
      ]
    }
    ```


### 8. 음료 주문 api

- POST /order
- 파라미터 : 메뉴명, 수량, 지점명, 결제수단
- 정책 : 결제 수단 잔액을 비교하여 결제 가능한 주문만 응답 & 존재하는 지점명, 메뉴만 응답 / 음료 1잔당 1포인트 적립 / 포인트 12 이상 되면 포인트 12 차감 후 쿠폰 1개로 교환
- 성공 응답 : 메뉴명, 수량, 전체금액, 주문 지점명, 결제수단, 주문 시간, 주문 상태
- 요청 / 응답 구조
  - 요청
    ```
    {
      "item" : "아메리카노",
      "quantity" : 2,
      "storeName" : "스타벅스 노량진역점",
      "pay" : "CARD"
    }
    
    ```
  - 응답
    ```
    {
      "item": "아메리카노",
      "quantity": 2,
      "totalPrice": 8200,
      "storeName": "스타벅스 노량진역점",
      "pay": "CARD",
      "orderTime": "2023-03-12T21:28:41.140938",
      "status": "ING"
    }
    
    ```
    
### 9. 회원 주문 리스트 api

- GET /member/myOrder
- 파라미터 : x
- 정책 : 인증 정보를 통해 userName 확인 후 해당 회원의 주문 리스트를 응답
- 성공 응답 : 메뉴명, 수량, 전체 금액, 지점명, 결제 수단, 주문 시간, 주문 상태
- 요청 / 응답 구조
  - 요청
    ```
    GET /member/myOrder
    ```
  - 응답
    ```
    {
      "content": [
          {
              "item": "아메리카노",
              "quantity": 2,
              "totalPrice": 8200,
              "storeName": "스타벅스 노량진역점",
              "pay": "CARD",
              "orderTime": "2023-03-12T21:28:41.140938",
              "status": "ING"
          }
      ]
    }
    ```


### 10. 음료 상태 변경 api

- GET /order/status-change
- 파라미터 : 주문번호
- 정책 : 완료되지 않은 주문에만 응답 (admin 권한만 접근 가능)
- 성공 응답 : 주문 번호, 주문자, 주문시간, 메뉴, 금액, 지점명, 주문상태, 수량, 주문완료 시간, 결제 수단
- 요청 / 응답 구조
  - 요청
    ```
    GET /order/status-change?orderNo=xx
    ex) GET /order/status-change?orderNo=12
    ```
  - 응답
    ```
    {
      "no": 25,
      "userName": "abc123@naver.com",
      "orderDateTime": "2023-03-12T21:28:41.140938",
      "menu": "아메리카노",
      "price": 8200,
      "store": "스타벅스 노량진역점",
      "orderStatus": "COMPLETE",
      "quantity": 2,
      "orderCompleteDateTime": "2023-03-12T21:42:42.87535",
      "pay": "CARD"
    }
    ```
    
### 11. 음료 제작 완료 후 알림 api


- GET /subscribe
- 파라미터 : x
- 정책 : 인증된 사용자가 알림 구독 상태에서 알림
- 성공 응답 : 알림 받는 사람, 알림내용, url, 알림 읽음 여부
- 요청 / 응답 구조
  - 요청
    ```
    GET /subscribe
    ```
  - 응답
    ```
    {
      "receiver": "abc123@naver.com",
      "content": "Your drink is ready!! Pick up please :)",
      "url": "/order/25",
      "isRead": false
    }
    ```


### 12. 음료 상태별 리스트 api


- GET /order/list/{status}
- 파라미터 : 음료 주문 상태 (ING or COMPLETE)
- 정책 : 요청받은 주문 상태 응답
- 성공 응답 : 주문 번호, 주문자, 주문시간, 메뉴, 금액, 지점명, 주문상태, 수량, 주문완료 시간, 결제 수단
- 요청 / 응답 구조
  - 요청
    ```
    GET /order/list/xxxx
    ex) GET /order/list/ING or COMPLETE
    ```
  - 응답
    ```
    {
        "no": 25,
        "userName": "abc123@naver.com",
        "orderDateTime": "2023-03-12T21:28:41.140938",
        "menu": "아메리카노",
        "price": 8200,
        "store": "스타벅스 노량진역점",
        "orderStatus": "COMPLETE",
        "quantity": 2,
        "orderCompleteDateTime": "2023-03-12T21:42:42.87535",
        "pay": "CARD"
    }
    
    ```


### 13. 지점 매출 및 주문 내역 api 

- GET /order/list-store
- 파라미터 : 지점명, 조회 기간
- 정책 : 요청 받은 기간에서 해당 지점의 판매 합계와 주문 리스트 응답
- 성공 응답 : 합계, 해당 지점의 주문 리스트
- 요청 / 응답 구조
  - 요청
    ```
    GET /order/list-store?storeName=xxxx&startDate=xxxx-xx-xx&endDate=xxxx-xx-xx
    ex) GET /order/list-store?storeName=스타벅스 노원마들역점&startDate=2023-03-01&endDate=2023-03-31
    ```
  - 응답
    ```
    {
      "sum": 8200,
      "orderList": [
          {
              "no": 23,
              "userName": "hysung714@naver.com",
              "orderDateTime": "2023-03-12T15:38:45.927354",
              "menu": "아메리카노",
              "price": 4100,
              "store": "스타벅스 노원마들역점",
              "orderStatus": "ING",
              "quantity": 1,
              "orderCompleteDateTime": null,
              "pay": "CARD"
          },
          {
              "no": 24,
              "userName": "hysung714@naver.com",
              "orderDateTime": "2023-03-12T15:39:36.37034",
              "menu": "아메리카노",
              "price": 4100,
              "store": "스타벅스 노원마들역점",
              "orderStatus": "ING",
              "quantity": 1,
              "orderCompleteDateTime": null,
              "pay": "COUPON"
          }
       ]
    }
    
    ```

### 14. 모든 지점 매출 현황 api

- GET /order/list-each
- 파라미터 : 조회기간
- 정책 : 조회 날짜 기준으로 각 지점별로 주문 합계 응답
- 성공 응답 : 판매 금액 합계, 지점명
- 요청 / 응답 구조
  - 요청
    ```
    GET /order/list-each?startDate=xxxx-xx-xx&endDate=xxxx-xx-xx
    ex) GET /order/list-each?startDate=2023-03-01&endDate=2023-03-12
    ```
  - 응답
    ```
    [
      {
          "totalPrice": 12300,
          "storeName": "스타벅스 노량진역점"
      },
      {
          "totalPrice": 8200,
          "storeName": "스타벅스 노원마들역점"
      }
    ]
    ```
