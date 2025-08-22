# RepeatStatus : FINISHED vs CONTINUABLE
- <b>RepeatStatus.FINISHED</b> : Step의 처리가 성공이든 실패든 상관없이 해당 Step이 완료되었음을 의미한다.
더 이상 반복할 필요 없이 다음 단계로 넘어가며, 배치 잡은 차근차근 다음 스텝으로 진행된다.
<br><br>
- <b>RepeatStatus.CONTINUABLE</b> : Tasklet의 `execute()` 메서드가 추가로 더 실행되어야 함을 Spring Batch Step에 알리는 신호다. Step의 종료는 보류되고, 필요한 만큼 `execute()` 메서드가 반복 호출된다.
---

# RepeatStatus가 필요한 이유
### : 짧은 트랜잭션을 활용한 안전한 배치 처리
- Spring Batch는 Tasklet의 `execute()` 호출 마다 새로운 트랜잭션을 시작하고 `execute()`의 실행이 끝나 RepeatStatus가 반환되면 해당 트랜잭션을 커밋한다.<br><br>
`execute()` 메서드 내부에 반복문을 직접 구현했다고 가정해보자. <br> 이 경우 모든 반복 작업이 하나의 트랜잭션 안에서 실행된다. 만약 실행 도중 예외가 발생하면, 데이터베이스 결과가 `execute()` 호출 전으로 롤백되어 버린다.<br><br>

- <b>execute() 내부에서 while문을 사용한다면</b> : (예시, 매 만 건 마다 커밋된다고 가정) 80만 건째 처리 중 예외가 발생했을 때, 이미 처리한 79만 건의 데이터도 모두 롤백되어 하나도 정리되지 않은 상태로 돌아간다 
- <b>RepeatStatus.CONTINUABLE로 반복한다면</b>: 매 만 건 처리마다 트랜잭션이 커밋되므로, 예외가 발생하더라도 79만 건의 데이터는 이미 안전하게 정리된 상태로 남는다

### 결국, RepeatStatus를 반환해 `execute()`를 반복 실행하도록 하는 이유는 거대한 하나의 트랜잭션 대신 작은 트랜잭션들로 나누어 안전하게 처리하기 위해서다.