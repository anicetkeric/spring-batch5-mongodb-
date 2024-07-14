package com.bootlabs.springbatch5mongodb.job.step;

import com.bootlabs.springbatch5mongodb.domain.model.TripCsvLine;
import com.bootlabs.springbatch5mongodb.domain.enums.ExecutionContextKey;
import com.opencsv.CSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import jakarta.mail.Message;
import jakarta.mail.internet.InternetAddress;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.bootlabs.springbatch5mongodb.domain.constant.BatchConstants.*;

@Slf4j
@Component
public class TripItemWriter implements ItemWriter<TripCsvLine>, StepExecutionListener {

    private StepExecution stepExecution;

    private int totalWriteTrip = 0;

    private final List<TripCsvLine> writeTrips = new ArrayList<>();

    private final String fileDateFormat = new SimpleDateFormat("yyyy_MM_dd_hh_mm").format(new Date());

    private final JavaMailSender mailSender;

    public TripItemWriter(JavaMailSender javaMailSender) {
        this.mailSender = javaMailSender;
    }

    @Override
    public void write(Chunk<? extends TripCsvLine> tripsChunk) {
        totalWriteTrip += tripsChunk.getItems().size();

        writeTrips.addAll(tripsChunk.getItems());
    }


    @BeforeStep
    public void beforeStepExecution(StepExecution stepExecution) {
        this.stepExecution = stepExecution;
    }

    @AfterStep
    public void afterStepExecution() {
        LOGGER.info("logger {}", stepExecution.getExecutionContext().get(ExecutionContextKey.TRIP_TOTAL.getKey()));

        if (stepExecution.getStatus().equals(BatchStatus.COMPLETED)) {
            generateCsvFile(writeTrips);
        }
        stepExecution.getExecutionContext().put(ExecutionContextKey.TRIP_TOTAL.getKey(), totalWriteTrip);
    }


    private void generateCsvFile(List<TripCsvLine> trips) {

        if (!CollectionUtils.isEmpty(trips)) {

            String filePath = MessageFormat.format("./{0}_{1}.csv", CSV_BASE_NAME, fileDateFormat);

            try (Writer writer = Files.newBufferedWriter(Paths.get(filePath))) {
                StatefulBeanToCsv<TripCsvLine> beanToCsv = new StatefulBeanToCsvBuilder(writer)
                        .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                        .withSeparator(CSVWriter.DEFAULT_SEPARATOR)
                        .withEscapechar(CSVWriter.DEFAULT_ESCAPE_CHARACTER)
                        .build();

                beanToCsv.write(trips);
                sendNotificationEmailReport(filePath);

            } catch (CsvRequiredFieldEmptyException | CsvDataTypeMismatchException | IOException e) {
                LOGGER.error(">> CSV file not found {} ", e.getMessage());
            } finally {
                writeTrips.clear();
            }
        }
    }

    public void sendNotificationEmailReport(String fileToAttach) {

        MimeMessagePreparator preparator = mimeMessage -> {

            mimeMessage.setFrom(new InternetAddress(MAIL_FROM));
            mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(MAIL_RECIPIENT));
            mimeMessage.setSubject(MessageFormat.format("{0}{1}", MAIL_SUBJECT, fileDateFormat));


            try {

                FileSystemResource file = new FileSystemResource(new File(fileToAttach));
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
                helper.addAttachment(Objects.requireNonNull(file.getFilename()), file);
                helper.setText("Hi Team,  <br><br> Please find attached the daily report. <br> Regards.", true);

            } catch (Exception ex) {
                LOGGER.error(">> Unable to send report by email {} ", ex.getMessage());
            }

        };
        mailSender.send(preparator);
    }

}
