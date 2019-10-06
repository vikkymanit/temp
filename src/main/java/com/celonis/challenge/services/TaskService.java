package com.celonis.challenge.services;

import com.celonis.challenge.exceptions.InternalException;
import com.celonis.challenge.exceptions.NotFoundException;
import com.celonis.challenge.model.ProjectGenerationTask;
import com.celonis.challenge.model.SimpleCounterTaskStatus;
import com.celonis.challenge.repository.ProjectGenerationTaskRepository;
import com.celonis.challenge.model.SimpleCounterTask;
import com.celonis.challenge.repository.SimpleCounterTaskRepository;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.data.jpa.domain.Specifications.where;

@Service
public class TaskService {

    private final ProjectGenerationTaskRepository projectGenerationTaskRepository;
    private final SimpleCounterTaskRepository simpleCounterTaskRepository;

    private final FileService fileService;
    private final ConcurrentHashMap<String, Thread> simpleCounterThreads;

    public TaskService(ProjectGenerationTaskRepository projectGenerationTaskRepository,
                       SimpleCounterTaskRepository simpleCounterTaskRepository,
                       FileService fileService) {
        this.projectGenerationTaskRepository = projectGenerationTaskRepository;
        this.simpleCounterTaskRepository = simpleCounterTaskRepository;
        this.fileService = fileService;
        this.simpleCounterThreads = new ConcurrentHashMap<>();
    }


    public List<ProjectGenerationTask> listTasks() {
        return projectGenerationTaskRepository.findAll();
    }

    public ProjectGenerationTask createTask(ProjectGenerationTask projectGenerationTask) {
        projectGenerationTask.setId(null);
        projectGenerationTask.setCreationDate(new Date());
        return projectGenerationTaskRepository.save(projectGenerationTask);
    }

    public ProjectGenerationTask getTask(String taskId) {
        return getProjectGenerationTask(taskId);
    }

    public ProjectGenerationTask update(String taskId, ProjectGenerationTask projectGenerationTask) {
        ProjectGenerationTask existing = getProjectGenerationTask(taskId);
        existing.setCreationDate(projectGenerationTask.getCreationDate());
        existing.setName(projectGenerationTask.getName());
        return projectGenerationTaskRepository.save(existing);
    }

    public void delete(String taskId) {
        projectGenerationTaskRepository.delete(taskId);
    }

    public void executeTask(String taskId) {
        URL url = Thread.currentThread().getContextClassLoader().getResource("challenge.zip");
        if (url == null) {
            throw new InternalException("Zip file not found");
        }
        try {
            fileService.storeResult(getProjectGenerationTask(taskId), url);
        } catch (Exception e) {
            throw new InternalException(e);
        }
    }

    private ProjectGenerationTask getProjectGenerationTask(String taskId) {
        ProjectGenerationTask projectGenerationTask = projectGenerationTaskRepository.findOne(taskId);
        if (projectGenerationTask == null) {
            throw new NotFoundException();
        }
        return projectGenerationTask;
    }

    public ResponseEntity<FileSystemResource> getTaskResult(String taskId) {
        ProjectGenerationTask projectGenerationTask = getTask(taskId);

        File inputFile = fileService.getFile(projectGenerationTask.getStorageLocation());
        if (!inputFile.exists()) {
            throw new InternalException("File not generated yet");
        }

        HttpHeaders respHeaders = new HttpHeaders();
        respHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        respHeaders.setContentDispositionFormData("attachment", "challenge.zip");

        return new ResponseEntity<>(new FileSystemResource(inputFile), respHeaders, HttpStatus.OK);
    }

    public SimpleCounterTask createSimpleCounterTask(SimpleCounterTask simpleCounterTask) {
        simpleCounterTask.setId(null);
        simpleCounterTask.setCreationDate(new Date());
        simpleCounterTask.setStatus(SimpleCounterTaskStatus.CREATED.toString());
        return simpleCounterTaskRepository.save(simpleCounterTask);
    }

    public void executeSimpleCounterTask(String taskId) {
        SimpleCounterTask simpleCounterTask = getSimpleCounterTask(taskId);

        Thread thread = new Thread(new SimpleCounterThread(taskId,
                simpleCounterTask.getBeginCounter(), simpleCounterTask.getEndCounter(),
                this));
        thread.start();
        simpleCounterThreads.put(taskId,thread);
    }

    private SimpleCounterTask getSimpleCounterTask(String taskId) {
        SimpleCounterTask simpleCounterTask = simpleCounterTaskRepository.findOne(taskId);
        if (simpleCounterTask == null) {
            throw new NotFoundException();
        }
        return simpleCounterTask;
    }

    public void updateSimpleCounterTaskStatus(String taskId, String status) {
        SimpleCounterTask simpleCounterTask = getSimpleCounterTask(taskId);
        simpleCounterTask.setStatus(status);
        simpleCounterTaskRepository.save(simpleCounterTask);
    }

    public SimpleCounterTask getSimpleCounterTaskStatus(String taskId) {
        return simpleCounterTaskRepository.findOne(taskId);
    }

    public void cancelSimpleCounterTask(String taskId) {
        Thread thread = simpleCounterThreads.get(taskId);
        thread.interrupt();
    }

    public void deleteSimpleCounterTask(String taskId) {
        Thread thread = simpleCounterThreads.get(taskId);
        if(thread != null) {
            thread.interrupt();
            while (!thread.isAlive()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        simpleCounterTaskRepository.delete(taskId);
    }

    public void updateRunningThreadsList(String taskId){
        simpleCounterThreads.remove(taskId);
    }

    public List<SimpleCounterTask> listSimpleCounterTasks() {
        return simpleCounterTaskRepository.findAll();
    }

    @Scheduled(cron ="0 */1 * * * *")
    private void purgeOldUnexecutedTasks(){
        List<SimpleCounterTask> simpleCounterTasks = simpleCounterTaskRepository.findAll
                (where(hasStatus(SimpleCounterTaskStatus.CREATED.toString())).and(isOlderThan(60000L)));
        simpleCounterTaskRepository.delete(simpleCounterTasks);
    }

    static Specification<SimpleCounterTask> hasStatus(String status) {
        return (task, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(task.get("status"), status);
    }

    static Specification<SimpleCounterTask> isOlderThan(Long timeInMilliSeconds) {
        Long currentTime = java.lang.System.currentTimeMillis();
        Long timeToCompate = currentTime - timeInMilliSeconds;
        Date date = new Date(timeToCompate);
        return (task, criteriaQuery, criteriaBuilder) -> criteriaBuilder.lessThan(task.get("creationDate"), date);
    }
}

