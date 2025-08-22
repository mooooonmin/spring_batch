Spring Batch Listener
리스너는 배치 처리의 주요 순간들을 관찰하고 각 시점에 필요한 동작을 정의할 수 있는 강력한 도구이다. 즉, 배치 처리 중 발생하는 특정 이벤트를 감지하고 그에 대한 동작을 정의할 수 있게 해준다.



Job이 시작하기 직전과 완료된 직후, Step이 실행되기 직전과 종료된 직후, 심지어 청크 단위 또는 아이템 단위로 데이터를 처리하기 전과 후까지 모든 처리 과정에 개입할 수 있다. 이를 통해 로깅, 모니터링, 에러 처리 등 우리가 원하는 로직을 자유롭게 추가할 수 있다.

Spring Batch의 리스너의 종류에는 무엇이 있는지부터 바로 살펴보자.



JobExecutionListener
Job 실행의 시작과 종료 시점에 호출되는 리스너 인터페이스다. Job 실행 결과를 이메일로 전송하거나, Job이 시작하기 전에 필요한 리소스를 준비하고 끝난 후에 정리하는 부가 작업을 수행할 수 있다.



afterJob() 메서드는 잡 실행 정보가 메타데이터 저장소에 저장되기 전에 호출된다. 이를 활용하면 특정 조건에 따라 Job의 실행 결과 상태를 완료(COMPLETED)에서 실패(FAILED)로 변경하거나, 그 반대로 변경하는 것도 가능하다.

public interface JobExecutionListener {
default void beforeJob(JobExecution jobExecution) { }
default void afterJob(JobExecution jobExecution) { }
}

StepExecutionListener
Step 실행의 시작과 종료 시점에 호출되는 리스너 인터페이스다. Step의 시작 시간, 종료 시간, 처리된 데이터 수를 로그로 기록하는 등의 사용자 정의 작업을 추가할 수 있다.

public interface StepExecutionListener extends StepListener {
default void beforeStep(StepExecution stepExecution) {
}

    @Nullable
    default ExitStatus afterStep(StepExecution stepExecution) {
	return null;
    }
}
afterStep() 메서드를 보면 ExitStatus를 반환하는 것을 알수 있는데, 이를 통해 afterStep()에서 특정 조건에 따라 Step의 실행 결과 상태를 직접 변경할 수 있다.

ExitStatus가 무엇인지는 나중에 자연히 알게 될 것이다. 당장은 기억하지 않고 넘어가도 좋다.



ChunkListener
지난 작전에서 설명했듯이 청크 지향 처리는 청크 단위로 아이템 읽기/쓰기를 반복한다. ChunkListener는 이러한 하나의 청크 단위 처리가 시작되기 전, 완료된 후, 그리고 에러가 발생했을 때 호출되는 리스너 인터페이스다. 각 청크의 처리 현황을 모니터링하거나 로깅하는데 사용할 수 있다.

public interface ChunkListener extends StepListener {
default void beforeChunk(ChunkContext context) {
}

    default void afterChunk(ChunkContext context) {
    }
	
    default void afterChunkError(ChunkContext context) {
    }
}
afterChunk()는 트랜잭션이 커밋된 후에 호출된다. 반면 청크 처리 도중 예외가 발생하면 afterChunk() 대신 afterChunkError()가 호출되는데, 이는 청크 트랜잭션이 롤백된 이후에 호출된다.



Item[Read|Process|Write]Listener
ItemReadListener와 ItemProcessListener, 그리고 ItemWriteListener는 아이템의 읽기, 처리, 쓰기 작업이 수행되는 시점에 호출되는 리스너 인터페이스들이다. 각 리스너의 메서드 이름에서 알 수 있듯이 아이템 단위의 처리 전후와 에러 발생 시점에 호출된다.

방금 살펴본 ChunkListener와 Item[Read|Process|Write]Listener 그리고 아직 소개하지 않은 SkipListener에 대해서는 4장에서 다시 한 번 살펴보도록 하자.

// ItemReadListener.java
public interface ItemReadListener<T> extends StepListener {
default void beforeRead() { }
default void afterRead(T item) { }
default void onReadError(Exception ex) { }
}

// ItemProcessListener.java
public interface ItemProcessListener<T, S> extends StepListener {
default void beforeProcess(T item) { }
default void afterProcess(T item, @Nullable S result) { }
default void onProcessError(T item, Exception e) { }
}

// ItemWriteListener.java
public interface ItemWriteListener<S> extends StepListener {
default void beforeWrite(Chunk<? extends S> items) { }
default void afterWrite(Chunk<? extends S> items) { }
default void onWriteError(Exception exception, Chunk<? extends S> items) { }
}


여기서 눈여겨봐야 할 포인트들을 짚고 넘어가자.

ItemReadListener.afterRead()는 ItemReader.read() 호출 후에 호출되지만, ItemReader.read() 메서드가 더 이상 읽을 데이터가 없어 null을 반환할 때는 호출되지 않는다.
반면, ItemProcessListener.afterProcess()는 ItemProcessor.process() 메서드가 null을 반환하더라도 호출된다. 참고로 ItemProcessor에서 null을 반환하는 것은 해당 데이터를 필터링하겠다는 의미다. 4장에서 다시 살펴보자.
ItemWriteListener.afterWrite()는 트랜잭션이 커밋되기 전, 그리고 ChunkListener.afterChunk()가 호출되기 전에 호출된다.


이제 각 리스너들의 호출 시점과 실행 흐름을 시각적으로 정리해보자.



각각의 리스너가 언제 어떤 순서로 호출되는지 한눈에 보면 이해에 도움이 될 것이다. 아래 다이어그램은 JobLauncher에서 시작해 각 처리 단계별로 호출되는 리스너들의 전체적인 호출 체인을 보여준다.

스크린샷 2025-01-18 오전 2.03.26.png.webp


“다이어그램이 대충 아름다워 보이는군. ok.. LGTM... ☠”



이런 호출 시점의 세부 사항을 모두 암기할 필요는 없다. 각 리스너 메서드마다 고유한 호출 타이밍이 있다는 사실만 기억해두자.

4장까지 학습을 마치면, 실제 필요할 때 자연스럽게 찾아 적용할 수 있는 능력이 갖춰질 것이다.



지금까지 각종 리스너 인터페이스들을 살펴보았으니, 이제 Spring Batch 리스너로 무엇을 할 수 있는지 대표적인 사용 사례들을 살펴보자.



배치 리스너, 이런 것들을 할 수 있다
단계별 모니터링과 추적: 배치 작업의 모든 단계를 내 손아귀에 넣을 수 있다. 각 Job과 Step의 실행 전후에 로그를 남길 수 있지. Step이 언제 시작하고, 언제 끝났는지, 몇 개의 데이터를 처리했는지까지 모든 것을 기록하고 추적할 수 있다.
실행 결과에 따른 후속 처리: Job과 Step의 실행 상태를 리스너에서 직접 확인하고 그에 따른 조치를 수 있다. 예를 들어 JobExecutionListener의 afterJob() 메서드에서 Job의 종료 상태를 확인하고 종료 상태에 따른 후속 조치를 취할 수 있다.
데이터 가공과 전달: 실제 처리 로직 전후에 데이터를 추가로 정제하거나 변환할 수 있다. StepExecutionListener나 ChunkListener를 사용하면 ExecutionContext의 데이터를 수정하거나 필요한 정보를 추가할 수 있다. 이를 통해 Step 간에 데이터를 전달하거나, 다음 처리에 필요한 정보를 미리 준비할 수 있다.
부가 기능 분리: 주요 처리 로직과 부가 로직을 깔끔하게 분리할 수 있다. 가령 ChunkListener에서 오류가 발생한 경우 afterChunkError() 메서드에서 관리자에게 알림 메일을 보내는 등의 부가적인 일은 여기서 처리할 수 있다.


리스너로 할 수 있는 일은 이 외에도 많다. 앞으로 계속해서 진도를 나가다 보면 자연스럽게 리스너의 활용법이 감이 잡힐 것이다. 이제 본격적으로 Spring Batch에서 리스너들을 어떻게 구현하고 사용하는지 예제를 통해 알아보자.





배치 리스너 구현 방법
리스너를 구현하는 방법은 크게 두 가지로 나눌 수 있다. 첫 번째는 전용 리스너 인터페이스를 직접 구현하는 방법이고, 두 번째는 리스너 특화 어노테이션을 사용하는 방법이다. 상황에 따라 선택해서 사용하면 된다.



인터페이스 구현부터 살펴보자.



인터페이스 구현
가장 일반적인 리스너 구현 방법은 전용 리스너 인터페이스를 직접 구현한 우리만의 커스텀 리스너 클래스를 만드는 것이다.

@Slf4j
@Component
public class BigBrotherJobExecutionListener implements JobExecutionListener {
@Override
public void beforeJob(JobExecution jobExecution) {
log.info("시스템 감시 시작. 모든 작업을 내 통제 하에 둔다.");
}

    @Override
    public void afterJob(JobExecution jobExecution) {
        log.info("작업 종료. 할당된 자원 정리 완료.");
        log.info("시스템 상태: {}", jobExecution.getStatus());
    }
}


@Slf4j
@Component
public class BigBrotherStepExecutionListener implements StepExecutionListener {
@Override
public void beforeStep(StepExecution stepExecution) {
log.info("Step 구역 감시 시작. 모든 행동이 기록된다.");
}

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info("Step 감시 종료. 모든 행동이 기록되었다.");
        log.info("Big Brother의 감시망에서 벗어날 수 없을 것이다.");
        return ExitStatus.COMPLETED;
    }
}
예제의 BigBrotherJobExecutionListener와 BigBrotherStepExecutionListener를 보면 JobExecutionListener와 StepExecutionListener 인터페이스를 구현하고 있는 것을 알 수 있다.



이렇게 인터페이스를 구현하는 방법은 명시적이고 직관적이다. 각 리스너 인터페이스의 메서드들이 모두 default로 정의되어 있어서, 필요한 시점의 메서드만 골라서 구현하면 된다.



이제 우리가 구현한 리스너를 실제 Job에 등록하는 방법을 살펴보자.

@Bean
public Job systemMonitoringJob(JobRepository jobRepository, Step monitoringStep) {
return new JobBuilder("systemMonitoringJob", jobRepository)
.listener(new BigBrotherJobExecutionListener())
.start(monitoringStep)
.build();
}
예제를 보면 JobBuilder의 listener() 메서드를 사용해 우리가 만든 리스너를 지정해주고 있다. JobExecutionListener는 잡을 구성할 때, StepExecutionListener를 포함한 그 외 나머지 리스너들은 Step을 구성할 때 스텝 빌더의 listener() 메서드에 전달하면 된다.

지금까지 인터페이스 구현을 통한 리스너 작성 방법을 살펴보았다. 하지만 더 간단하고 빠르게 리스너를 구현하고 싶을 때가 있다. 이럴 때 사용할 수 있는 것이 바로 어노테이션 기반 구현 방법이다. 이 방법을 통해 우리는 코드를 더욱 간결하게 만들 수 있다. 다음으로 어노테이션기반 구현 방법을 살펴보자.



어노테이션 기반 구현
Spring Batch가 제공하는 @BeforeJob, @AfterJob, @BeforeStep, @AfterStep과 같은 리스너 특화 어노테이션을 사용하면 리스너 기능을 훨씬 간단하게 구현할 수 있다.



인터페이스 구현 없이 어노테이션만으로도 원하는 시점에 실행될 메서드를 지정할 수 있어서 코드가 더 간결해진다.

@Slf4j
@Component
public class ServerRoomInfiltrationListener {
@BeforeJob
public void infiltrateServerRoom(JobExecution jobExecution) {
log.info("판교 서버실 침투 시작. 보안 시스템 무력화 진행중.");
}

    @AfterJob
    public void escapeServerRoom(JobExecution jobExecution) {
        log.info("파괴 완료. 침투 결과: {}", jobExecution.getStatus());
    }
}

@Slf4j
@Component
public class ServerRackControlListener {
@BeforeStep
public void accessServerRack(StepExecution stepExecution) {
log.info("서버랙 접근 시작. 콘센트를 찾는 중.");
}

    @AfterStep
    public ExitStatus leaveServerRack(StepExecution stepExecution) {
        log.info("코드를 뽑아버렸다.");
        return new ExitStatus("POWER_DOWN");
    }
}
위 예제는 @BeforeJob, @AfterJob, @BeforeStep, @AfterStep 어노테이션을 사용한 리스너 구현이다. Job 실행 전후를 감지하는 ServerRoomInfiltrationListener와 Step 실행 전후를 감지하는 ServerRackControlListener를 통해 각 시점에서 필요한 작업을 수행한다.



@AfterStep이 선언되어있는 leaveServerRack()의 반환타입을 보자. ExitStatus를 반환하고 있다. 이는 StepExecutionListener의 afterStep() 메서드가 ExitStatus를 반환하기 때문이다. @AfterStep 어노테이션을 사용할 때는 이 반환 타입을 꼭 지켜줘야 한다.



그럼 이제 앞에서 만든 어노테이션 기반 리스너를 Step에 적용해보자.

@Bean
public Step serverRackControlStep(Tasklet destructiveTasklet) {
return new StepBuilder("serverRackControlStep", jobRepository)
.tasklet(destructiveTasklet(), transactionManager)
.listener(new ServerRackControlListener()) // 빌더의 listener() 메서드에 전달
.build();
}
이처럼 어노테이션을 사용한 리스너도 빌더의 listener() 메서드를 통해 설정할 수 있다. Spring Batch의 빌더 클래스들은 인터페이스 구현 방식의 리스너뿐만 아니라 어노테이션 기반 리스너도 등록할 수 있도록, listener() 메서드의 파라미터로 Object 타입을 받는 오버로딩 버전도 제공한다.

지금까지 예제에서는 @AfterJob과 @AfterStep만 다루어보았다. 그러나 Spring Batch는 모든 리스너 인터페이스에 정의된 메서드에 대응하는 어노테이션들을 전부 제공한다.



정확히 다음과 같은 어노테이션을 제공하는데, 각 리스너 인터페이스에 정의된 메서드와 동일한 이름인 것을 알 수 있다.

@AfterChunk, @AfterChunkError, @AfterJob, @AfterProcess, @AfterRead, @AfterStep, @AfterWrite, @BeforeChunk, @BeforeJob, @BeforeProcess, @BeforeRead, @BeforeStep, @BeforeWrite, @OnProcessError, @OnReadError, @OnSkipInProcess, @OnSkipInRead, @OnSkipInWrite


지금까지 리스너의 종류, 호출 시점, 그리고 구현 방법에 대해 알아보았다. 이제 리스너를 응용한 활용 방법으로 넘어가 보자. 특히 리스너와 ExecutionContext를 함께 사용하여 동적으로 데이터를 처리하거나 Step 간에 데이터를 공유하는 방법을 살펴볼 것이다.





JobExecutionListener와 ExecutionContext를 활용한 동적 데이터 전달
잡 파라미터만으로는 전달할 수 없는 동적인 데이터가 필요한 경우가 있다. 이럴 때 JobExecutionListener의 beforeJob() 메서드를 활용하면 추가적인 동적 데이터를 각 Step에 전달할 수 있다. 구체적인 예제를 통해 자세히 살펴보자.



“키보드 꽉 잡아라. 코드가 좀 길다.” - KILL-9



@Slf4j
@Configuration
@RequiredArgsConstructor
public class AdvancedSystemInfiltrationConfig {
private final InfiltrationPlanListener infiltrationPlanListener;

    @Bean
    public Job systemInfiltrationJob(JobRepository jobRepository, Step reconStep, Step attackStep) {
        return new JobBuilder("systemInfiltrationJob", jobRepository)
                .listener(infiltrationPlanListener)
                .start(reconStep)
                .next(attackStep)
                .build();
    }

    @Bean
    public Step reconStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("reconStep", jobRepository)
            .tasklet((contribution, chunkContext) -> {
                Map<String, Object> infiltrationPlan = (Map<String, Object>) 
                  chunkContext.getStepContext()
                              .getJobExecutionContext()
                              .get("infiltrationPlan");
                log.info("침투 준비 단계: {}", infiltrationPlan.get("targetSystem"));
                log.info("필요한 도구: {}", infiltrationPlan.get("requiredTools"));
                return RepeatStatus.FINISHED;
          }, transactionManager)
          .build();
    }

    @Bean
    public Step attackStep(
        JobRepository jobRepository,
        PlatformTransactionManager transactionManager,
        Tasklet attackStepTasklet  // 주입받은 Tasklet 사용
    ) {
        return new StepBuilder("attackStep", jobRepository)
                .tasklet(attackStepTasklet, transactionManager)
                .build();
    }

    @Bean
    @StepScope
    public Tasklet attackStepTasklet(
        @Value("#{jobExecutionContext['infiltrationPlan']}") Map<String, Object> infiltrationPlan
    ) {
        return (contribution, chunkContext) -> {
            log.info("시스템 공격 중: {}", infiltrationPlan.get("targetSystem"));
            log.info("목표: {}", infiltrationPlan.get("objective"));

            Random rand = new Random();
            boolean infiltrationSuccess = rand.nextBoolean();

            if (infiltrationSuccess) {
                log.info("침투 성공! 획득한 데이터: {}", infiltrationPlan.get("targetData"));
                contribution.getStepExecution().getJobExecution().getExecutionContext()
                        .put("infiltrationResult", "TERMINATED");
            } else {
                log.info("침투 실패. 시스템이 우리를 감지했다.");
                contribution.getStepExecution().getJobExecution().getExecutionContext()
                        .put("infiltrationResult", "DETECTED");
            }

            return RepeatStatus.FINISHED;
        };
    }
}

@Slf4j
@Component
public class InfiltrationPlanListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        Map<String, Object> infiltrationPlan = generateInfiltrationPlan();
        jobExecution.getExecutionContext().put("infiltrationPlan", infiltrationPlan);
        log.info("새로운 침투 계획이 준비됐다: {}",  infiltrationPlan.get("targetSystem"));
    }

    private Map<String, Object> generateInfiltrationPlan() {
        List<String> targets = List.of(
                "판교 서버실", "안산 데이터센터"
        );
        List<String> objectives = List.of(
                "kill -9 실행", "rm -rf 전개", "chmod 000 적용", "/dev/null로 리다이렉션"
        );
        List<String> targetData = List.of(
                "코어 덤프 파일", "시스템 로그", "설정 파일", "백업 데이터"
        );
        List<String> requiredTools = List.of(
                "USB 킬러", "널 바이트 인젝터", "커널 패닉 유발기", "메모리 시퍼너"
        );

        Random rand = new Random();

        Map<String, Object> infiltrationPlan = new HashMap<>();
        infiltrationPlan.put("targetSystem", targets.get(rand.nextInt(targets.size())));
        infiltrationPlan.put("objective", objectives.get(rand.nextInt(objectives.size())));
        infiltrationPlan.put("targetData", targetData.get(rand.nextInt(targetData.size())));
        infiltrationPlan.put("requiredTools", requiredTools.get(rand.nextInt(requiredTools.size())));

        return infiltrationPlan;
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        String infiltrationResult = (String) jobExecution.getExecutionContext().get("infiltrationResult");
        Map<String, Object> infiltrationPlan = (Map<String, Object>)
                jobExecution.getExecutionContext().get("infiltrationPlan");

       log.info("타겟 '{}' 침투 결과: {}", infiltrationPlan.get("targetSystem"), infiltrationResult);

        if ("TERMINATED".equals(infiltrationResult)) {
            log.info("시스템 제거 완료. 다음 타겟 검색 중...");
        } else {
            log.info("철수한다. 다음 기회를 노리자.");
        }
    }
}
먼저 JobExecutionListener를 구현한 InfiltrationPlanListener를 살펴보자.



InfiltrationPlanListener는 Job 실행 전 각 스텝에 전달할 데이터를 동적으로 생성한다. beforeJob() 메서드 구현을 보면 generateInfiltrationPlan()을 호출해 동적으로 데이터를 생성하고, 이를 해당 JobExecution의 ExecutionContext에 설정하고 있다.



지난 작전에서 Job 수준 ExecutionContext에 저장된 데이터는 해당 Job이 실행하는 모든 Step에서 접근이 가능하다고 설명했던 것을 기억하는가?

@Bean
public Job systemInfiltrationJob(JobRepository jobRepository, Step reconStep, Step attackStep) {
return new JobBuilder("systemInfiltrationJob", jobRepository)
.listener(infiltrationPlanListener) // Listener 등록
.start(reconStep)
.next(attackStep)
.build();
}
이렇게 InfiltrationPlanListener를 JobBuilder의 listener() 메서드로 등록해주면 beforeStep() 메서드에서 동적으로 생성한 데이터를 각 Step에서 참조할 준비가 완료된다.



이제 각 스텝에서 어떻게 데이터를 활용하는지 살펴보자.

@Bean
public Step reconStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
return new StepBuilder("reconStep", jobRepository)
.tasklet((contribution, chunkContext) -> {
Map<String, Object> infiltrationPlan = (Map<String, Object>)
chunkContext.getStepContext()
.getJobExecutionContext()
.get("infiltrationPlan");
log.info("침투 준비 단계: {}", infiltrationPlan.get("targetSystem"));
log.info("필요한 도구: {}", infiltrationPlan.get("requiredTools"));
return RepeatStatus.FINISHED;
}, transactionManager)
.build();
}

@Bean
public Step attackStep(
JobRepository jobRepository,
PlatformTransactionManager transactionManager,
Tasklet attackStepTasklet  // 주입받은 Tasklet 사용
) {
return new StepBuilder("attackStep", jobRepository)
.tasklet(attackStepTasklet, transactionManager)
.build();
}


@Bean
@StepScope
public Tasklet attackStepTasklet(
@Value("#{jobExecutionContext['infiltrationPlan']}") Map<String, Object> infiltrationPlan
) {
return (contribution, chunkContext) -> {
log.info("시스템 공격 중: {}", infiltrationPlan.get("targetSystem"));
log.info("목표: {}", infiltrationPlan.get("objective"));

        Random rand = new Random();
        boolean infiltrationSuccess = rand.nextBoolean();

        if (infiltrationSuccess) {
            log.info("침투 성공! 획득한 데이터: {}", infiltrationPlan.get("targetData"));
            contribution.getStepExecution()
                .getJobExecution()
                .getExecutionContext()
                .put("infiltrationResult", "TERMINATED");
        } else {
            log.info("침투 실패. 시스템이 우리를 감지했다.");
            contribution.getStepExecution()
                .getJobExecution().getExecutionContext()
                .put("infiltrationResult", "DETECTED");
        }  

        return RepeatStatus.FINISHED;
    };
}
reconStep을 살펴보자.



reconStep의 tasklet() 메서드는 다크 코딩의 정석 💀, 람다 표현식으로 Tasklet 인터페이스 구현을 대체했다. 구현 내부를 보면

Map<String, Object> infiltrationPlan = (Map<String, Object>)
chunkContext.getStepContext().getJobExecutionContext().get("infiltrationPlan");
와 같은 방법으로 infiltrationPlanListener의 beforeJob()에서 생성한 동적 데이터를 가져오고 있는 것을 알 수 있다.



다음으로 attackStep에서 참조하는 attackStepTasklet으로 가보자.



attackStepTasklet에서는 @Value를 사용해 beforeJob()에서 구현한 동적 데이터를 Job 수준 ExecutionContext로부터 받아오고 있다.

@Value("#{jobExecutionContext['infiltrationPlan']}") Map<String, Object> infiltrationPlan


attackStepTasklet은 침투 결과를 나타내는 infiltrationResult를 Job 수준 ExecutionContext에 저장한다. 이는 InfiltrationPlanListener의 afterJob()에서 최종 결과를 처리하기 위함이다.



바로 예제를 실행해보자.

./gradlew bootRun --args='--spring.batch.job.name=systemInfiltrationJob'


실행해보면 다음과 같은 결과가 출력된다.

새로운 침투 계획이 준비됐다: 판교 서버실
... : Executing step: [reconStep]
침투 준비 단계: 판교 서버실
필요한 도구: 메모리 시퍼너
... : Step: [reconStep] executed in 3ms
... : Executing step: [attackStep]
시스템 공격 중: 판교 서버실
목표: chmod 000 적용
침투 성공! 획득한 데이터: 코어 덤프 파일
... : Step: [attackStep] executed in 11ms
타겟 '판교 서버실' 침투 결과: TERMINATED
시스템 제거 완료. 다음 타겟 검색 중...
실행 결과를 보면 JobExecutionListener의 beforeJob()을 활용해 동적으로 생성한 infiltrationPlan이 각 Step에 잘 전달된 것을 확인할 수 있다.





왜 JobParameters가 아닌 ExecutionContext를 사용할까?
한 번 생성된 JobParameters는 변경할 수 없기 때문이다.



Spring Batch의 핵심 철학 중 하나는 배치 작업의 재현 가능성(Repeatability)과 일관성(Consistency)을 보장하는 것이다.



이를 위해 JobParameters는 불변(immutable)하게 설계되었다.

재현 가능성: 동일한 JobParameters로 실행한 Job은 항상 동일한 결과를 생성해야 한다. 실행 중간에 JobParameters가 변경되면 이를 보장할 수 없다.
추적 가능성: 배치 작업의 실행 기록(JobInstance, JobExecution)과 JobParameters는 메타데이터 저장소에 저장된다. JobParameters가 변경 가능하다면 기록과 실제 작업의 불일치가 발생할 수 있다.


따라서 Job 실행 중에 동적으로 생성되거나 변경되어야 하는 데이터는 ExecutionContext를 통해 관리하는 것이 좋다.

💀[시스템 경고]

JobExecutionListener와 ExecutionContext를 사용해 데이터를 동적으로 전달하는 방식은 분명 유용할 수 있지만, JobParameters만으로 충분히 처리할 수 있는 경우에도, 이 방법을 사용하고 싶은 유혹에 빠지기 쉽다.



잘못된 코드, 이렇게 작성하지 마라💀

// 이런 코드에 현혹되지 마라
@Override
public void beforeJob(JobExecution jobExecution) {
jobExecution.getExecutionContext()
.put("targetDate", LocalDate.now()); // 치명적인 실수다
}
이 방식의 문제점은 무엇일까?
- 어제 데이터를 다시 처리하고 싶으면? 불가능하다. 프로그램을 수정하지 않으면 그날의 데이터를 다시 처리할 수 없다.

결국 하드코딩된 방식은 배치의 유연성을 떨어뜨리고, 필요한 순간에 원하는 데이터를 처리할 수 없게 만든다.



대신 이렇게 하라💀

# 외부에서 파라미터로 받아라
./gradlew bootRun --args='--spring.batch.job.name=systemInfiltrationJob -date=2024-10-13'
이렇게 외부에서 날짜 값을 전달받으면, 배치 작업의 유연성을 극대화할 수 있다. JobParameters는 대부분의 데이터를 외부에서 받을 수 있는 훌륭한 방법이다.



JobParameters를 사용할 수 있다면 그 방법을 사용하라. 외부에서 값을 받는 것이 훨씬 더 안전하고 유연하다. JobExecutionListener와 ExecutionContext는 외부에서 값을 받을 수 없는 경우에만 사용하자.



지금까지 우리는 Job 수준 ExecutionContext를 이용해 동적 데이터를 어떻게 전달할 수 있는지 알아봤다. 이때 Job 수준 ExecutionContext에 저장된 데이터는 Job 내 모든 Step에서 접근할 수 있었다.



하지만 Step 수준 ExecutionContext에 저장된 데이터는 해당 Step에서만 접근 가능하므로 다른 Step과 공유는 불가능하다.



그래서 한 Step의 ExecutionContext에 존재하는 데이터를 다음 Step에게 전달하려면 Step의 ExecutionContext의 값을 가져와 이를 Job 수준 ExecutionContext에 직접 설정해주어야 한다.



다음과 같이 말이다...

StepExecution stepExecution = contribution.getStepExecution();
ExecutionContext stepExecutionContext = stepExecution.getExecutionContext();
Integer infiltrationCount = (Integer) stepExecutionContext.get("infiltrationCount");

JobExecution jobExecution = stepExecution.getJobExecution();
jobExecution.getExecutionContext().put("totalInfiltrations", infiltrationCount);


코드가 참.. 끔찍하다. 💀


매번 contribution.getStepExecution().getExecutionContext().get()를 호출하고 contribution.getStepExecution().getJobExecution().getExecutionContext().put()를 호출해주어야 한다.



확실히 불편하다. 게다가 우리의 핵심 침투 로직과는 전혀 관계없는 이런 데이터 전달 코드가 비정상적으로 길어지는 것도 문제다.



Spring Batch는 이런 반복적인 불편한 과정을 알아서 처리해줄 수 있도록 ExecutionContextPromotionListener라는 StepExecutionListener 구현체를 제공한다.



ExecutionContextPromotionListener를 사용하면 위와 같이 번거로운 데이터 설정 작업 없이도 손쉽게 스텝 간 데이터 공유가 가능하다.





ExecutionContextPromotionListener를 활용한 Step 간 데이터 공유
ExecutionContextPromotionListener는 Step 수준 ExecutionContext의 데이터를 Job 수준 ExecutionContext로 등록시켜주는 StepExecutionListener의 구현체다.



Spring Batch에서는 Step 수준의 ExecutionContext 데이터를 Job 수준의 ExecutionContext로 옮기는 과정을 승격(Promote)이라 부른다. 그래서 이 리스너의 이름도 ExecutionContextPromotion + Listener인 것이다. 이 리스너는 StepExecutionListener의 afterStep() 메서드를 오버라이드하여 승격 작업을 수행한다.



이제 예제를 보면서 어떻게 사용하는지 살펴보자.

@Slf4j
@Configuration
public class SystemTerminationConfig {
@Bean
public Job systemTerminationJob(JobRepository jobRepository, Step scanningStep, Step eliminationStep) {
return new JobBuilder("systemTerminationJob", jobRepository)
.start(scanningStep)
.next(eliminationStep)
.build();
}

    @Bean
    public Step scanningStep(
            JobRepository jobRepository, 
            PlatformTransactionManager transactionManager
    ) {
        return new StepBuilder("scanningStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    String target = "판교 서버실";
                    ExecutionContext stepContext = contribution.getStepExecution().getExecutionContext();
                    stepContext.put("targetSystem", target);
                    log.info("타겟 스캔 완료: {}", target);
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .listener(promotionListener())
                .build();
    }

    @Bean
    public Step eliminationStep(
            JobRepository jobRepository, 
            PlatformTransactionManager transactionManager,
            Tasklet eliminationTasklet
    ) {
        return new StepBuilder("eliminationStep", jobRepository)
                .tasklet(eliminationTasklet, transactionManager)
                .build();
    }

    @Bean
    @StepScope
    public Tasklet eliminationTasklet(
            @Value("#{jobExecutionContext['targetSystem']}") String target
    ) {
        return (contribution, chunkContext) -> {
            log.info("시스템 제거 작업 실행: {}", target);
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public ExecutionContextPromotionListener promotionListener() {
        ExecutionContextPromotionListener listener = new ExecutionContextPromotionListener();
        listener.setKeys(new String[]{"targetSystem"});
        return listener;
    }
}

ExecutionContextPromotionListener 빈 구성 코드를 보자.



먼저 ExecutionContextPromotionListener에는 Step 수준의 ExecutionContext에서 Job 수준으로 승격할 데이터의 키 값을 지정해주어야 한다. 이는 setKeys() 메서드를 통해 설정할 수 있다.

@Bean
public ExecutionContextPromotionListener promotionListener() {
ExecutionContextPromotionListener listener = new ExecutionContextPromotionListener();
listener.setKeys(new String[]{"targetSystem"});  // targetSystem 키를 승격 대상으로 지정
return listener;
}


그리고 이렇게 생성한 promotionListener를 scanningStep의 리스너로 등록한다.

.tasklet((contribution, chunkContext) -> {
String target = "판교 서버실";
ExecutionContext stepContext = contribution.getStepExecution().getExecutionContext();
stepContext.put("targetSystem", target);  // Step의 ExecutionContext에 저장
log.info("타겟 스캔 완료: {}", target);
return RepeatStatus.FINISHED;
}, transactionManager)
.listener(promotionListener())  // promotionListener 등록
.build();


이렇게 하면 scanningStep에서 Step 수준 ExecutionContext에 저장한 targetSystem 값이 자동으로 Job 수준 ExecutionContext로 승격되어, 다음 스텝인 eliminationStep에서 아래와 같이 간단히 조회할 수 있다.

@StepScope
public Tasklet eliminationTasklet(
// Job의 ExecutionContext에서 값 조회
@Value("#{jobExecutionContext['targetSystem']}") String target
) {
return (contribution, chunkContext) -> {
log.info("시스템 제거 작업 실행: {}", target);
return RepeatStatus.FINISHED;
};
}
이렇게 함으로써, Step 수준에서 데이터를 관리하면서도 필요한 데이터는 Job 수준에서 쉽게 공유할 수 있다.

executionContextPromotion.png


단, 각 Step은 가능한 한 독립적으로 설계하여 재사용성과 유지보수성을 높이는 것이 좋다. 불가피한 경우가 아니라면 Step 간 데이터 의존성은 최소화하는 것이 좋다. Step 간 데이터 공유가 늘어날수록 복잡도가 증가한다.



지금까지 우리는 리스너와 ExecutionContext를 활용한 동적 데이터 전달 방법과, ExecutionContextPromotionListener를 활용한 Step 간 데이터 공유 방법을 알아봤다. 이제 마지막으로 또 다른 치명적인 무기를 소개하려 한다.



바로 Listener와 @JobScope, @StepScope를 통합해서 사용하는 유연한 방법이다.



Listener와 @JobScope, @StepScope 통합
리스너와 Spring Batch Scope를 통합하면 리스너에서 잡 파라미터를 매우 쉽게 다룰 수 있다. 이를 통해 실행 시점에 결정되는 값들을 리스너 내에서도 활용할 수 있다는 말이다.



자, 이제 예제를 살펴보자. 💀

@Slf4j
@Configuration
public class SystemDestructionConfig {
@Bean
public Job killDashNineJob(JobRepository jobRepository, Step terminationStep) {
return new JobBuilder("killDashNineJob", jobRepository)
.listener(systemTerminationListener(null))  // 파라미터는 런타임에 주입
.start(terminationStep)
.build();
}

@Bean
public Step terminationStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
return new StepBuilder("terminationStep", jobRepository)
.tasklet((contribution, chunkContext) -> {
log.info("시스템 제거 프로토콜 실행 중...");
return RepeatStatus.FINISHED;
}, transactionManager)
.build();
}

@Bean
@JobScope
public JobExecutionListener systemTerminationListener(
@Value("#{jobParameters['terminationType']}") String terminationType
) {
return new JobExecutionListener() {
@Override
public void beforeJob(JobExecution jobExecution) {
log.info("시스템 제거 시작! 제거 방식: {}", terminationType);
}

           @Override
           public void afterJob(JobExecution jobExecution) {
               log.info("작전 종료! 시스템 상태: {}", jobExecution.getStatus());
           }
       };
}
}
먼저 systemTerminationListener 빈에 @JobScope를 적용한 것에 주목하라. JobExecutionListener는 잡의 실행(JobExecution)과 생명주기를 함께하기 때문에 당연히 @JobScope를 붙여준다.



이렇게 하면 @Value("#{jobParameters['terminationType']}")를 사용해 JobParameters의 terminationType에 바로 접근할 수 있다.



마치 내가 시스템의 취약점을 찾아내는 것처럼 손쉽게 말이다. 💀



주입받은 terminationType 파라미터는 리스너의 어느 메서드에서든 자유롭게 사용할 수 있다. beforeJob() 메서드에서는 이를 활용해 어떤 방식으로 시스템을 제거할지 로그로 남기고 있다.





Listener 마지막 훈련: 성능과 모범 사례
리스너를 효과적으로 다루는 법
1.무기 선택의 정확성

작전의 범위와 목적에 따라 적절한 리스너를 선택하라

JobExecutionListener: 전체 작전의 시작과 종료를 통제
StepExecutionListener: 각 작전 단계의 실행을 감시
ChunkListener: 시스템을 청크단위로 제거할 때, 반복의 시작과 종료 시점을 통제
Item[Read|Process|Write]Listener: 개별 아이템 식별 통제


2. 예외 처리는 신중하게

JobExecutionListener의 beforeJob()과 StepExecutionListener의 beforeStep()에서 예외가 발생하면 Job과 Step이 실패한 것으로 판단된다. 하지만 모든 예외가 Step을 중단시켜야 할 만큼 치명적인 것은 아니다. 이런 경우는 직접 예외를 잡아서 무시하고 진행하는 것이 현명하다.

💀 [시스템 인텔리전스]

반면, JobExecutionListener.afterJob()과 StepExecutionListener.afterStep()에서 발생한 예외는 무시된다. 즉, 예외가 발생해도 Job과 Step의 실행 결과에 영향을 미치진 않는다.

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
3. 단일 책임 원칙 준수

리스너는 감시와 통제만 담당한다. 실제 시스템 제거 로직(비즈니스 로직)은 분리하라. 리스너가 너무 많은 일을 하면 유지보수가 어려워지고 시스템 동작을 파악하기 힘들어진다





성능 최적화를 위한 경고
1. 실행 빈도를 고려하라

JobExecutionListener/StepExecutionListener
Job, Step 실행당 한 번씩만 실행되므로 비교적 안전하다
무거운 로직이 들어가도 전체 성능에 큰 영향 없음
ItemReadListener/ItemProcessListener
매 아이템마다 실행되므로 치명적일 수 있다
// 이런 코드는 시스템을 마비시킬 수 있다
@Override
public void afterRead(Object item) {
heavyOperation();  // 매 아이템마다 실행되면 시스템이 마비된다
remoteApiCall();   // 외부 API 호출은 더더욱 위험
}


2. 리소스 사용을 최소화하라

데이터베이스 연결, 파일 I/O, 외부 API 호출은 최소화
리스너 내 로직은 가능한 한 가볍게 유지하라
특히 Item 단위 리스너에서는 더욱 중요하다


리스너는 강력한 무기지만, 잘못 쓰면 우리 시스템을 마비시킬 수 있다. 항상 실행 빈도와 리소스 사용을 고려하여 신중하게 사용하라.



지금까지 Spring Batch의 리스너에 대해 알아보았다. 리스너는 배치 처리의 각 단계를 모니터링하고 제어할 수 있게 해주는 강력한 도구지만, 그만큼 신중하게 사용해야 한다. 특히 실행 빈도가 높은 Item 단위의 리스너들은 성능에 직접적인 영향을 미칠 수 있으므로 더욱 주의가 필요하다.



이로써 우리 강의 1장 내용이 마무리되었다. 1장에서 다룬 내용들은 앞으로 본격적으로 살펴볼 Spring Batch를 이해하기 위한 기초가 된다. Step의 두 가지 유형, JobParameters, Batch Scope, ExecutionContext, 그리고 방금 살펴본 Listener까지, 이 개념들은 2장부터 마지막까지 계속해서 만나게 될 것이다.



2장으로 넘어가기 전에, 지금까지 배운 내용들을 다시 한번 살펴보는 것이 좋겠다.



"눈을 감아 뇌를 reboot하라. 기억을 dump 해봐라. 💀”



모든 개념이 정상적으로 core dumped 되었길 바라며... - KILL-9