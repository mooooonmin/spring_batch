# JobScope와 StepScope 사용 시 주의사항

1. 프록시 대상의 타입이 클래스인 경우 반드시 상속 가능한 클래스여야 한다.
```
@Scope(value = "job", proxyMode = ScopedProxyMode.TARGET_CLASS)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JobScope {}
```

2. Step 빈에는 @StepScope와 @JobScope와를 사용하지 말라.
```
@Bean
@StepScope
public Step systemDestructionStep(
    SystemInfiltrationTasklet tasklet
) {
    return new StepBuilder("systemDestructionStep", jobRepository)
        .tasklet(tasklet, transactionManager)
        .build();
}
```
- Spring Batch Step에 @StepScope를 달면 Step 빈 생성과 스코프 활성화 시점이 맞지 않아 오류가 발생한다.