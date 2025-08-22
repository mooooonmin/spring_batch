### Tasklet 지향 처리는 Spring Batch에서 단순하고 명확한 작업을 수행하는 데 사용되는 Step 유형이다. 
#### 파일 삭제, 데이터 초기화, 알림 발송 등 비교적 단순한 작업에 적합하다. 
- <b>단순 작업에 적합</b> 
  - 태스크릿 지향 처리는 알림 발송, 파일 복사, 오래된 데이터 삭제 등 단순 작업을 처리하는 Step 유형이다.
- <b>Tasklet 인터페이스 구현</b> 
  - Tasklet 인터페이스를 구현해 필요한 로직을 작성한 뒤, 이를 `StepBuilder.tasklet()` 메서드에 전달해 Step을 구성한다.
- <b>RepeatStatus로 실행 제어</b>
  - `Tasklet.execute()` 메서드는 `RepeatStatus`를 반환하며, 이를 통해 실행 반복 여부를 결정할 수 있다. 
- <b>트랜잭션 지원</b>
  - Spring Batch는 `Tasklet.execute()` 메서드 실행 전후로 트랜잭션을 시작하고 커밋하여, 데이터베이스의 일관성과 원자성을 보장한다.
