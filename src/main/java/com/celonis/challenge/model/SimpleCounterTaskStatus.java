package com.celonis.challenge.model;

public enum SimpleCounterTaskStatus {
    CREATED("created"), EXECUTING("executing"), CANCELLED("cancelled"), FINISHED("finished");

    private final String status;

    SimpleCounterTaskStatus(String status){
        this.status = status;
    }

    @Override
    public String toString(){
        return status;
    }
}
