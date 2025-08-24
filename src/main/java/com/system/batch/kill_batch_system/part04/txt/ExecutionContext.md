# 왜 JobParameters가 아닌 ExecutionContext를 사용할까?
### 한 번 생성된 JobParameters는 변경할 수 없기 때문이다.

---

- **JobParameters는 불변(immutable)하게 설계되었다.** 
  - **재현 가능성** : 동일한 JobParameters로 실행한 Job은 항상 동일한 결과를 생성해야 한다. 실행 중간에 JobParameters가 변경되면 이를 보장할 수 없다. 
  - **추적 가능성** : 배치 작업의 실행 기록(JobInstance, JobExecution)과 JobParameters는 메타데이터 저장소에 저장된다. JobParameters가 변경 가능하다면 기록과 실제 작업의 불일치가 발생할 수 있다.