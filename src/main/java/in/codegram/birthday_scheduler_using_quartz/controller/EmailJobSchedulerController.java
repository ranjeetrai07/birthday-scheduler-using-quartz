package in.codegram.birthday_scheduler_using_quartz.controller;

import in.codegram.birthday_scheduler_using_quartz.payload.ScheduleEmailRequest;
import in.codegram.birthday_scheduler_using_quartz.payload.ScheduleEmailResponse;
import in.codegram.birthday_scheduler_using_quartz.quartz.job.EmailJob;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.sql.Date;
import java.time.ZonedDateTime;
import java.util.UUID;

@Slf4j
@RestController
public class EmailJobSchedulerController {

    @Autowired
    private Scheduler scheduler;

    @PostMapping("/schedule/email")
    public ResponseEntity<ScheduleEmailResponse> scheduleEmail(@Valid @RequestBody ScheduleEmailRequest emailRequest){
        try {
            ZonedDateTime dateTime = ZonedDateTime.of(emailRequest.getDateTime(), emailRequest.getTimeZone());
            if(dateTime.isBefore(ZonedDateTime.now())){
                ScheduleEmailResponse emailResponse = new ScheduleEmailResponse(false, "dateTime must be after current time");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(emailResponse);
            }
            JobDetail jobDetail = buildJobDetail(emailRequest);
            Trigger trigger =  buildTrigger(jobDetail, dateTime);

            scheduler.scheduleJob(jobDetail, trigger);

            ScheduleEmailResponse emailResponse = new ScheduleEmailResponse(true, jobDetail.getKey().getName(),
                    jobDetail.getKey().getGroup(), "Email Schedule Successfully");

            return ResponseEntity.ok(emailResponse);
        }catch (SchedulerException se){
            log.error("error while scheduling email: ", se);
            ScheduleEmailResponse emailResponse = new ScheduleEmailResponse(false,
                    "Error while scheduling email. Please try again later");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(emailResponse);
        }
    }

    @GetMapping("/get")
    public ResponseEntity<String> getApiTest(){
        return ResponseEntity.ok("Get API test - Pass");
    }

    private JobDetail buildJobDetail(ScheduleEmailRequest scheduleEmailRequest) {
        JobDataMap jobDataMap = new JobDataMap();

        jobDataMap.put("email", scheduleEmailRequest.getEmail());
        jobDataMap.put("subject", scheduleEmailRequest.getSubject());
        jobDataMap.put("body", scheduleEmailRequest.getBody());

        return JobBuilder.newJob(EmailJob.class)
                .withIdentity(UUID.randomUUID().toString(), "email-jobs")
                .withDescription("Send Email Job")
                .usingJobData(jobDataMap)
                .storeDurably()
                .build();
    }

    private Trigger buildTrigger(JobDetail jobDetail, ZonedDateTime startAt){
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(jobDetail.getKey().getName(), "email-trigger")
                .withDescription("Send Email Trigger")
                .startAt(Date.from(startAt.toInstant()))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
                .build();
    }

}




















