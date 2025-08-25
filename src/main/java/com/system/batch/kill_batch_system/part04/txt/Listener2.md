1. 선택의 정확성
   - `JobExecutionListener` : 전체 작전의 시작과 종료를 통제
   - `StepExecutionListener` : 각 작전 단계의 실행을 감시
   - `ChunkListener` : 시스템을 청크단위로 제거할 때, 반복의 시작과 종료 시점을 통제
   - `Item[Read|Process|Write]Listener` : 개별 아이템 식별 통제
<br><br>
2. 예외 처리는 신중하게
    - JobExecutionListener의 beforeJob()과 StepExecutionListener의 beforeStep()에서 예외가 발생하면 Job과 Step이 실패한 것으로 판단된다.
    - 하지만 모든 예외가 Step을 중단시켜야 할 만큼 치명적인 것은 아니다.
```
@Override
public void beforeStep(StepExecution stepExecution) {
    try {
        // 치명적인 로직 수행
        systemMetricCollector.collect();
    } catch (Exception e) {
        // 심각하지 않은 예외는 로그만 남기고 진행
        log.warn("메트릭 수집 실패. 작전은 계속 진행: {}", e.getMessage());
        // 정말 심각한 문제면 예외를 던져서 Step을 중단시킨다
        // throw new RuntimeException("치명적 오류 발생", e);
    }
}
```
3. 단일 책임 원칙 준수
   - 리스너는 감시와 통제만 담당한다. 
   - 실제 시스템 제거 로직(비즈니스 로직)은 분리하라.
---
