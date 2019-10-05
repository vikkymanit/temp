package com.celonis.challenge.controllers;

import com.celonis.challenge.model.ProjectGenerationTask;
import com.celonis.challenge.model.SimpleCounterTask;
import com.celonis.challenge.services.FileService;
import com.celonis.challenge.services.TaskService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/")
    public List<ProjectGenerationTask> listTasks() {
        return taskService.listTasks();
    }

    @PostMapping("/")
    public ProjectGenerationTask createTask(@RequestBody @Valid ProjectGenerationTask projectGenerationTask) {
        return taskService.createTask(projectGenerationTask);
    }

    @GetMapping("/{taskId}")
    public ProjectGenerationTask getTask(@PathVariable String taskId) {
        return taskService.getTask(taskId);
    }

    @PutMapping("/{taskId}")
    public ProjectGenerationTask updateTask(@PathVariable String taskId,
                                            @RequestBody @Valid ProjectGenerationTask projectGenerationTask) {
        return taskService.update(taskId, projectGenerationTask);
    }

    @DeleteMapping("/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable String taskId) {
        taskService.delete(taskId);
    }

    @PostMapping("/{taskId}/execute")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void executeTask(@PathVariable String taskId) {
        taskService.executeTask(taskId);
    }

    @GetMapping("/{taskId}/result")
    public ResponseEntity<FileSystemResource> getResult(@PathVariable String taskId) {
        return taskService.getTaskResult(taskId);
    }

    @PostMapping("/simple-counter/")
    public SimpleCounterTask createSimpleCounterTask(@RequestBody @Valid SimpleCounterTask simpleCounterTask) {
        return taskService.createSimpleCounterTask(simpleCounterTask);
    }

}
