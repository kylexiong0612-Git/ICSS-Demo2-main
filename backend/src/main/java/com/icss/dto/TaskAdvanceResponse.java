package com.icss.dto;

import com.icss.model.ServiceTask;
import lombok.Data;

@Data
public class TaskAdvanceResponse {
    private ServiceTask currentTask;
    private ServiceTask nextTask;
}
