package com.bootlabs.springbatch5mongodb.job;

import com.bootlabs.springbatch5mongodb.domain.document.Trips;
import com.bootlabs.springbatch5mongodb.domain.model.TripCsvLine;
import com.bootlabs.springbatch5mongodb.job.step.TripItemProcessor;
import com.bootlabs.springbatch5mongodb.job.step.TripItemReader;
import com.bootlabs.springbatch5mongodb.job.step.TripItemWriter;
import com.bootlabs.springbatch5mongodb.job.step.TripStepListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

import static com.bootlabs.springbatch5mongodb.domain.constant.BatchConstants.DEFAULT_CHUNK_SIZE;
import static com.bootlabs.springbatch5mongodb.domain.constant.BatchConstants.DEFAULT_LIMIT_SIZE;


@Configuration
public class JobConfig {

    @Bean
    public DataSource getDataSource() {
        return new EmbeddedDatabaseBuilder()
                .addScript("classpath:org/springframework/batch/core/schema-drop-h2.sql")
                .addScript("classpath:org/springframework/batch/core/schema-h2.sql")
                .setType(EmbeddedDatabaseType.H2)
                .build();
    }


    @Bean
    public Job tripJob(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                       MongoTemplate mongoTemplate) {
        return new JobBuilder("tripJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(tripJobStep(jobRepository, transactionManager, mongoTemplate))
                .listener(new TripJobCompletionListener())
                .build();
    }

    @Bean
    public Step tripJobStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                            MongoTemplate mongoTemplate) {
        return new StepBuilder("tripJobCSVGenerator", jobRepository)
                .startLimit(DEFAULT_LIMIT_SIZE)
                .<Trips, TripCsvLine>chunk(DEFAULT_CHUNK_SIZE, transactionManager)

                .reader(new TripItemReader(mongoTemplate))
                .processor(new TripItemProcessor())
                .writer(new TripItemWriter())
                .listener(new TripStepListener())
                .build();
    }

}


