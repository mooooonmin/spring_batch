package com.system.batch.kill_batch_system.part04.Listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 어노테이션 기반 구현
 * Spring Batch가 제공하는 @BeforeJob, @AfterJob, @BeforeStep, @AfterStep과 같은 리스너 특화 어노테이션을 사용하면 리스너 기능을 훨씬 간단하게 구현할 수 있다.
 * 인터페이스 구현 없이 어노테이션만으로도 원하는 시점에 실행될 메서드를 지정할 수 있어서 코드가 더 간결해진다.
 */
@Slf4j
@Component
public class ServerRackControlListener {

    private JobRepository jobRepository;
    private PlatformTransactionManager transactionManager;

    @BeforeStep
    public void accessServerRack(StepExecution stepExecution) {
        log.info("서버랙 접근 시작. 콘센트를 찾는 중.");
    }

    @AfterStep
    public ExitStatus leaveServerRack(StepExecution stepExecution) {
        log.info("코드를 뽑아버렸다.");
        return new ExitStatus("POWER_DOWN");
    }

    @Bean
    public Step serverRackControlStep(Tasklet destructiveTasklet) {
        return new StepBuilder("serverRackControlStep", jobRepository)
                .tasklet(destructiveTasklet(), transactionManager)
                .listener(new ServerRackControlListener()) // 빌더의 listener() 메서드에 전달
                .build();
    }

}