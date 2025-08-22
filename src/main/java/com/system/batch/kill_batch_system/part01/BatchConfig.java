package com.system.batch.kill_batch_system.part01;

import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.converter.JobParametersConverter;
import org.springframework.batch.core.converter.JsonJobParametersConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class BatchConfig extends DefaultBatchConfiguration {

    /**
     * Spring Batch의 핵심 컴포넌트들은 자동으로 구성되지만, 몇 가지 필수 인프라 Bean들은 직접 등록 필요
     *
     * DefaultBatchConfiguration : Spring Batch 5부터 도입된 기본 설정 클래스로,
     * JobRepository, JobLauncher 등 Spring Batch의 핵심 컴포넌트들을 자동으로 구성해준다
     * */
    @Bean
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                // Spring Batch가 필요로 하는 메타데이터 테이블들을 자동으로 생성
                .addScript("org/springframework/batch/core/schema-h2.sql")
                .build();
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }

    /**
     * JSON 기반 파라미터 표기법을 사용하려면 JsonJobParametersConverter가 필요
     * DefaultJobParametersConverter를 계승한 클래스로,
     * 내부적으로 ObjectMapper를 사용해 JSON 형태의 파라미터 표기를 해석한다.
     */
    @Bean
    public JobParametersConverter jobParametersConverter() {
        return new JsonJobParametersConverter();
    }
}
