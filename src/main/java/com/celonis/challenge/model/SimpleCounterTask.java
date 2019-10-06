package com.celonis.challenge.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Date;

@Entity
public class SimpleCounterTask {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    private String name;

    private Date creationDate;

    private Integer beginCounter;

    private Integer endCounter;

    private String status;

    @JsonIgnore
    private String storageLocation;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getStorageLocation() {
        return storageLocation;
    }

    public void setStorageLocation(String storageLocation) {
        this.storageLocation = storageLocation;
    }

    public Integer getBeginCounter() {
        return beginCounter;
    }

    public void setBeginCounter(Integer beginCounter) {
        this.beginCounter = beginCounter;
    }

    public Integer getEndCounter() {
        return endCounter;
    }

    public void setEndCounter(Integer endCounter) {
        this.endCounter = endCounter;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
