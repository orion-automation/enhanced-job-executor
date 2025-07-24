-- liquibase formatted sql

-- changeset eorion:1 dbms:mysql,mariadb
create table if not exists ENHANCEMENT_JOB_LOG
(
    ID_ varchar(64) not null primary key,
    JOB_ID_ varchar(64) not null,
    JOB_EXCEPTION_MSG_ varchar(4000),
    JOB_STATE_ integer,
    TIMEOUT_ bigint,
    JOB_DEF_ID_ varchar(64),
    JOB_DEF_TYPE_ varchar(255),
    ACT_ID_ varchar(255),
    EXECUTION_ID_ varchar(64),
    ROOT_PROC_INST_ID_ varchar(64),
    PROCESS_INSTANCE_ID_ varchar(64),
    PROCESS_DEF_ID_ varchar(64),
    PROCESS_DEF_KEY_ varchar(255),
    DEPLOYMENT_ID_ varchar(64),
    TENANT_ID_ varchar(64),
    HOSTNAME_ varchar(255),
    EXECUTION_THREAD_NAME_ varchar(32),
    EXECUTION_START_TIMESTAMP_ timestamp not null,
    EXECUTION_COMPLETE_TIMESTAMP_ timestamp,
    EXECUTION_DURATION_ bigint,
    primary key (ID_)
)