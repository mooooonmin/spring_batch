package com.system.batch.kill_batch_system.part03.JobParameter2;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;


/**
 * JobParametersValidator를 사용하면 잘못된 파라미터가 들어오는 순간 즉시 차단할 수 있다.
 */
@Slf4j
@Component
public class SystemDestructionValidator implements JobParametersValidator {

    private PlatformTransactionManager transactionManager;
    private JobRepository jobRepository;

    @Override
    public void validate(@Nullable JobParameters parameters) throws JobParametersInvalidException {

        if (parameters == null) {
            throw new JobParametersInvalidException("파라미터가 NULL입니다");
        }

        Long destructionPower = parameters.getLong("destructionPower");
        if (destructionPower == null) {
            throw new JobParametersInvalidException("destructionPower 파라미터는 필수값입니다");
        }

        if (destructionPower > 9) {
            throw new JobParametersInvalidException(
                    "파괴력 수준이 허용치를 초과했습니다: " + destructionPower + " (최대 허용치: 9)");
        }
    }

    @Bean
    public Job systemDestructionJob(
            JobRepository jobRepository,
            Step systemDestructionStep,
            SystemDestructionValidator validator) {
        return new JobBuilder("systemDestructionJob", jobRepository)
                .validator(validator)
                .start(systemDestructionStep)
                .build();
    }

    @Bean
    public Job systemDestructionJob(
            JobRepository jobRepository,
            Step systemDestructionStep
    ) {
        return new JobBuilder("systemDestructionJob", jobRepository)
                // DefaultJobParametersValidator는 정의되지 않은 파라미터의 침입을 허용하지 않는다.
                .validator(new DefaultJobParametersValidator(
                        new String[]{"destructionPower"},  // 필수 파라미터
                        new String[]{"targetSystem"}       // 선택적 파라미터
                        // 그 외의 다른 파라미터 허용 X
                ))
                .start(systemDestructionStep)
                .build();
    }

    // 1. 빈 주입 방식
    @Bean
    public Job systemTerminationJob(Step systemDestructionStep) {  // Spring이 주입
        return new JobBuilder("systemTerminationJob", jobRepository)
                .start(systemDestructionStep)
                .build();
    }

    // 2. 메서드 직접 호출 방식
    @Bean
    public Job systemTerminationJob() {
        return new JobBuilder("systemTerminationJob", jobRepository)
                .start(systemDestructionStep(null))  // Job 파라미터 자리에 null 전달
                .build();
    }

    /*
    @Bean
    @JobScope
    public Step systemDestructionStep(
            @Value("#{jobParameters['destructionPower']}") Long destructionPower) {
        return new StepBuilder("systemDestructionStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info("시스템 파괴 프로세스가 시작되었습니다. 파괴력: {}", destructionPower);
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
     */

    /*
    @Bean
    @JobScope  // Step에 @JobScope를 달았다
    public Step systemDestructionStep(
            @Value("#{jobParameters['targetSystem']}") String targetSystem
    ) {
        return new StepBuilder("systemDestructionStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info("{} 시스템 침투 시작", targetSystem);
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
     */

    @Bean
    public Step systemDestructionStep(
            SystemInfiltrationTasklet tasklet  // Tasklet을 주입받는다
    ) {
        return new StepBuilder("systemDestructionStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }

    @Slf4j
    @Component
    @StepScope  // Tasklet에 @StepScope를 달았다
    public class SystemInfiltrationTasklet implements Tasklet {
        private final String targetSystem;

        public SystemInfiltrationTasklet(
                @Value("#{jobParameters['targetSystem']}") String targetSystem
        ) {
            this.targetSystem = targetSystem;
        }

        @Override
        public RepeatStatus execute(StepContribution contribution, ChunkContext context) {
            log.info("{} 시스템 침투 시작", targetSystem);
            return RepeatStatus.FINISHED;
        }
    }

    @Bean
    public Step infiltrationStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            Tasklet systemInfiltrationTasklet
    ) {
        return new StepBuilder("infiltrationStep", jobRepository)
                .tasklet(systemInfiltrationTasklet, transactionManager)
                .build();
    }

    @Bean
    @StepScope
    public Tasklet systemInfiltrationTasklet(
            @Value("#{jobParameters['infiltrationTargets']}") String infiltrationTargets
    ) {
        return (contribution, chunkContext) -> {
            String[] targets = infiltrationTargets.split(",");
            log.info("시스템 침투 시작");
            log.info("주 타겟: {}", targets[0]);
            log.info("보조 타겟: {}", targets[1]);
            log.info("침투 완료");
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    @JobScope
    public Tasklet systemDestructionTasklet(
            @Value("#{jobExecutionContext['previousSystemState']}") String prevState
    ) {
        // JobExecution의 ExecutionContext에서 이전 시스템 상태를 주입받는다
    }

    @Bean
    @StepScope
    public Tasklet infiltrationTasklet(
            @Value("#{stepExecutionContext['targetSystemStatus']}") String targetStatus
    ) {
        // StepExecution의 ExecutionContext에서 타겟 시스템 상태를 주입받는다
    }

}
