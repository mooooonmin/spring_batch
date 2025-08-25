package com.system.batch.kill_batch_system.part04.Listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * ExecutionContextPromotionListener를 활용한 Step 간 데이터 공유
 */
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
                    stepContext.put("targetSystem", target); // Step의 ExecutionContext에 저장
                    log.info("타겟 스캔 완료: {}", target);
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .listener(promotionListener()) // promotionListener 등록
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

    /**
     * 이렇게 하면 scanningStep에서 Step 수준 ExecutionContext에 저장한 targetSystem 값이 자동으로 Job 수준 ExecutionContext로 승격되어,
     * 다음 스텝인 eliminationStep에서 아래와 같이 간단히 조회할 수 있다.
     */
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

    /**
     * 먼저 ExecutionContextPromotionListener에는 Step 수준의 ExecutionContext에서 Job 수준으로 승격할 데이터의 키 값을 지정해주어야 한다.
     * 이는 setKeys() 메서드를 통해 설정할 수 있다.
     */
    @Bean
    public ExecutionContextPromotionListener promotionListener() {
        ExecutionContextPromotionListener listener = new ExecutionContextPromotionListener();
        listener.setKeys(new String[]{"targetSystem"});
        return listener;
    }

}
