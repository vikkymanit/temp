package com.celonis.challenge.repository;

import com.celonis.challenge.model.ProjectGenerationTask;
import com.celonis.challenge.model.SimpleCounterTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.awt.print.Book;

@Repository
public abstract class SimpleCounterTaskRepository implements JpaRepository<SimpleCounterTask, String>, JpaSpecificationExecutor<SimpleCounterTask> {
}
