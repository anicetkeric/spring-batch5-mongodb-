package com.bootlabs.springbatch5mongodb.job;

import com.bootlabs.springbatch5mongodb.domain.TripCsvLine;
import com.bootlabs.springbatch5mongodb.domain.document.Trips;
import com.bootlabs.springbatch5mongodb.job.step.TripItemProcessor;
import com.bootlabs.springbatch5mongodb.job.step.TripItemReader;
import com.bootlabs.springbatch5mongodb.job.step.TripItemWriter;
import com.bootlabs.springbatch5mongodb.job.step.TripStepListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

import static com.bootlabs.springbatch5mongodb.domain.constant.BatchConstants.DEFAULT_LIMIT_SIZE;
import static com.bootlabs.springbatch5mongodb.domain.constant.BatchConstants.DEFAULT_CHUNK_SIZE;


@Configuration
//public class JobConfig extends DefaultBatchConfiguration {
public class JobConfig {

    @Bean
    public DataSource getDataSource() {
        return new EmbeddedDatabaseBuilder()
                .addScript("classpath:org/springframework/batch/core/schema-drop-h2.sql")
                .addScript("classpath:org/springframework/batch/core/schema-h2.sql")
                .setType(EmbeddedDatabaseType.H2)
                .build();
    }


    public TaskExecutor taskExecutor() {
        SimpleAsyncTaskExecutor simpleAsyncTaskExecutor = new SimpleAsyncTaskExecutor();
        simpleAsyncTaskExecutor.setConcurrencyLimit(10);
        return simpleAsyncTaskExecutor;
    }
/*    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setMaxPoolSize(10);
        taskExecutor.setCorePoolSize(10);
        taskExecutor.setQueueCapacity(10);
        taskExecutor.afterPropertiesSet();
        return taskExecutor;
    }*/
/*

    @Bean
    public Job zipInfoJob(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                          MongoTemplate mongoTemplate) {
        return new JobBuilder("zipInfoJob", jobRepository)
                .start(zipInfoStep(jobRepository, transactionManager, mongoTemplate))
                //  .next(appointmentSlotStep(jobRepository, transactionManager, mongoTemplate))
                // .listener(appointmentOrchestratorJobCompletionListener)
                //  .listener(new AppointmentOrchestratorJobCompletionListener())
                .build();
    }

    @Bean
    public Step zipInfoStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                          MongoTemplate mongoTemplate) {
        return new StepBuilder("zipInfoCSVGenerator", jobRepository)
                .startLimit(DEFAULT_LIMIT_SIZE)
                .<ZipInfo, ZipInfo>chunk(DEFAULT_CHUNK_SIZE, transactionManager)

                .reader(new ZipInfoItemReader(mongoTemplate))
               // .reader(zipInfoItemReader(mongoTemplate))
*/
/*                .processor(departmentAppointmentProcessor)*//*

                .writer(new PersonWriter())
                .listener(new TripStepListener())
            //    .taskExecutor(taskExecutor())
                .build();
    }
*/

    @Bean
    public Job tripJob(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                          MongoTemplate mongoTemplate) {
        return new JobBuilder("tripJob", jobRepository)
                //.incrementer(new RunIdIncrementer())
                .start(tripJobStep(jobRepository, transactionManager, mongoTemplate))
                //  .next(appointmentSlotStep(jobRepository, transactionManager, mongoTemplate))
                // .listener(appointmentOrchestratorJobCompletionListener)
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
            //    .taskExecutor(taskExecutor())
                .build();
    }


/*
    @Bean
    public MongoCursorItemReader<ZipInfo> zipInfoItemReader(MongoTemplate mongoTemplate) {
        Map<String, Sort.Direction> sortOptions = new HashMap<>();
        sortOptions.put("id", Sort.Direction.ASC);


        Duration maxTime = Duration.ofMillis(10000);
     //   var query = new Query(Criteria.where("pop").gte(1000));
        var query = new Query(Criteria.where("pop").gte(100000));

        return new MongoCursorItemReaderBuilder<ZipInfo>().name("reader")
                .template(mongoTemplate)
                .collection("zips")
                .targetType(ZipInfo.class)
                .query(query)
               // .query(new Query())
                .sorts(sortOptions)
                .batchSize(DEFAULT_CHUNK_SIZE)
                .limit(DEFAULT_LIMIT_SIZE)
              //  .maxTime(maxTime)
               // .jsonQuery("{}")
               // .jsonQuery("{ \"pop\": { $gte: 100000 }}")
                .build();
*/


//github.com/spring-projects/spring-batch/blob/21555c093e4c9b97a066878c9e392afcf9a58531/spring-batch-infrastructure/src/test/java/org/springframework/batch/item/data/MongoCursorItemReaderTest.java#L47
        // https://github.com/spring-projects/spring-batch/blob/21555c093e4c9b97a066878c9e392afcf9a58531/spring-batch-infrastructure/src/test/java/org/springframework/batch/item/data/builder/MongoCursorItemReaderBuilderTests.java#L37
     //   return customAggregationPaginatedItemReader;

 //   }
    //endregion


}


