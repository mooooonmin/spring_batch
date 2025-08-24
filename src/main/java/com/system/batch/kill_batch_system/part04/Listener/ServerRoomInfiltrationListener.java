package com.system.batch.kill_batch_system.part04.Listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.annotation.AfterJob;
import org.springframework.batch.core.annotation.BeforeJob;
import org.springframework.stereotype.Component;

/**
 * 어노테이션 기반 구현
 * Spring Batch가 제공하는 @BeforeJob, @AfterJob, @BeforeStep, @AfterStep과 같은 리스너 특화 어노테이션을 사용하면 리스너 기능을 훨씬 간단하게 구현할 수 있다.
 * 인터페이스 구현 없이 어노테이션만으로도 원하는 시점에 실행될 메서드를 지정할 수 있어서 코드가 더 간결해진다.
 */
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
