### Chunk : 데이터를 작은 덩어리로 나누어 처리하는 방식
- 청크의 크기는 직접 지정 가능
---
### ItemReader, ItemProcessor, ItemWriter
1. 완벽한 책임 분리
   - 각 컴포넌트는 자신의 역할만 수행한다. 
   - ItemReader는 읽기, ItemProcessor는 깎기(가공), ItemWriter는 쓰기에만 집중한다.

2. 재사용성 극대화
   - 컴포넌트들은 독립적으로 설계되어 있어 어디서든 재사용 가능하다. 
   - 새로운 배치를 만들 때도 기존 컴포넌트들을 조합해서 빠르게 구성할 수 있다.

3. 높은 유연성
   - 요구사항이 변경되어도 해당 컴포넌트만 수정하면 된다. 
   - 데이터 형식이 바뀌면 ItemProcessor만, 데이터 소스가 바뀌면 ItemReader만 수정하면 된다. 

4. 대용량 처리의 표준
   - 데이터를 다루는 배치 작업은 결국 '읽고-처리하고-쓰는' 패턴을 따른다.

---

```
@Bean
public Step processStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
   return new StepBuilder("processStep", jobRepository)
           .<CustomerDetail, CustomerSummary>chunk(10, transactionManager)  // 청크 지향 처리 활성화
           .reader(itemReader())       // 데이터 읽기 담당
           .processor(itemProcessor()) // 데이터 처리 담당
           .writer(itemWriter())      // 데이터 쓰기 담당
           .build();
}

@Bean
public Job customerProcessingJob(JobRepository jobRepository, Step processStep) {
   return new JobBuilder("customerProcessingJob", jobRepository)
           .start(processStep)  // processStep으로 Job 시작
           .build();
}
```
- `chunk()` 메서드를 호출 : 호출을 통해 Step이 청크 지향 처리 모델로 동작
- `chunk(10, transactionManager)` : 청크의 크기 지정 (ex. 데이터를 10개씩 묶어서 처리)
  - ItemReader가 데이터 10개를 읽어오면, 이를 하나의 청크로 만들어 ItemProcessor에서 처리하고 ItemWriter에서 쓰기를 수행
- `.<CustomerDetail, CustomerSummary>chunk(..)`
  - 첫 번째 타입(CustomerDetail): ItemReader가 반환할 타입. 예를 들어 파일에서 읽은 데이터를 CustomerDetail 객체로 변환하여 반환한다. 
  - 두 번째 타입(CustomerSummary): CustomerDetail를 입력 받은 ItemProcessor가 아이템을 처리 후 반환할 타입이자 ItemWriter가 전달받을 Chunk의 제네릭 타입이다.
---

## **청크 크기만큼 ItemReader.read()가 모두 호출된 후에 그 다음 청크 크기만큼 ItemProcessor.process()가 호출된다**