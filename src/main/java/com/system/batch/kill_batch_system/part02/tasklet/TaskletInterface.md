package com.system.batch.kill_batch_system.part02.tasklet;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.lang.Nullable;

/**
 * 태스크릿(Tasklet) 지향 처리 모델은 Spring Batch에서 가장 기본적인 Step 구현 방식으로 비교적 복잡하지 않은 단순한 작업을 실행할 때 사용된다.
 * 단일 비즈니스 로직 실행에 초점을 맞춘 작업 위주
*/

@FunctionalInterface
public interface Tasklet extends org.springframework.batch.core.step.tasklet.Tasklet {

    /**
     * Spring Batch가 제공하는 Tasklet 인터페이스의 execute() 메서드에 우리가 원하는 로직을 구현하고,
     * 이 구현체를 Spring Batch에 넘기기만 하면 된다. 그 이후의 실행과 흐름 관리는 Spring Batch가 알아서 처리한다.
     * */

    @Nullable
    RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception;
}
