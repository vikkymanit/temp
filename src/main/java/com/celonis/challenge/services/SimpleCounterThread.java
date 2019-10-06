package com.celonis.challenge.services;

import com.celonis.challenge.model.SimpleCounterTaskStatus;
import com.celonis.challenge.repository.SimpleCounterTaskRepository;

public class SimpleCounterThread implements Runnable {

    private Integer beginCounter;
    private final Integer endCounter;
    private final String taskId;
    private final TaskService taskService;

    public SimpleCounterThread(String taskId, Integer beginCounter,
                               Integer endCounter,
                               TaskService taskService) {
        this.beginCounter = beginCounter;
        this.endCounter = endCounter;
        this.taskId = taskId;
        this.taskService = taskService;
    }

    @Override
    public void run() {
        taskService.updateSimpleCounterTaskStatus(taskId,SimpleCounterTaskStatus.EXECUTING.toString());
        while(beginCounter < endCounter){
            if(!Thread.interrupted()) {
                beginCounter = beginCounter + 1;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    taskService.updateSimpleCounterTaskStatus(taskId, SimpleCounterTaskStatus.CANCELLED.toString());
                }
            } else {
                taskService.updateSimpleCounterTaskStatus(taskId, SimpleCounterTaskStatus.CANCELLED.toString());
            }
        }
        taskService.updateSimpleCounterTaskStatus(taskId,SimpleCounterTaskStatus.FINISHED.toString());
        taskService.updateRunningThreadsList(taskId);
    }
}
