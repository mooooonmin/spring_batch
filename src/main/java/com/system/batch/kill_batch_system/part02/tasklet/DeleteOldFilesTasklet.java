package com.system.batch.kill_batch_system.part02.tasklet;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;

import java.io.File;

@Slf4j
public class DeleteOldFilesTasklet implements Tasklet {

    private final String path;
    private final int daysOld;
    private JobRepository jobRepository;

    public DeleteOldFilesTasklet(String path, int daysOld) {
        this.path = path;
        this.daysOld = daysOld;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        File dir = new File(path);
        long cutoffTime = System.currentTimeMillis() - (daysOld * 24 * 60 * 60 * 1000L);

        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.lastModified() < cutoffTime) {
                    if (file.delete()) {
                        log.info("🔥 파일 삭제: {}", file.getName());
                    } else {
                        log.info("⚠️  파일 삭제 실패: {}", file.getName());
                    }
                }
            }
        }
        return RepeatStatus.FINISHED;
    }

    /**
     * 매일 밤 7일이 지난 레코드를 created 컬럼 기준으로 삭제하는 작업의 예시다.
     */
    @Bean
    public Step deleteOldRecordsStep() {
        return new StepBuilder("deleteOldRecordsStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    int deleted = jdbcTemplate.update("DELETE FROM logs WHERE created < NOW() - INTERVAL 7 DAY");
                    log.info("🗑️ {}개의 오래된 레코드가 삭제되었습니다.", deleted);
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Job deleteOldRecordsJob() {
        return new JobBuilder("deleteOldRecordsJob", jobRepository)
                .start(deleteOldRecordsStep())  // Step을 Job에 등록
                .build();
    }

}
