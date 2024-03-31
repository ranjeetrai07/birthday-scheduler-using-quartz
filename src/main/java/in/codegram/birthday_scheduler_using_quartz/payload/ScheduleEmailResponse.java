package in.codegram.birthday_scheduler_using_quartz.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Setter
@Getter
public class ScheduleEmailResponse {

    private boolean success;

    private String jobId;

    private String jobGroup;

    private String message;

    public ScheduleEmailResponse(boolean success, String message){
        this.success = success;
        this.message = message;
    }

    public ScheduleEmailResponse(boolean success, String jobId, String jobGroup, String message){
        this.success =  success;
        this.jobId = jobId;
        this.jobGroup = jobGroup;
        this.message = message;
    }
}




















