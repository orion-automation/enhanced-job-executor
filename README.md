[![License](https://img.shields.io/badge/License-Apache%202.0-yellowgreen.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Maven Library Publish](https://github.com/orion-automation/enhanced-job-executor/actions/workflows/sonatype-publish.yml/badge.svg)](https://github.com/orion-automation/enhanced-job-executor/actions/workflows/sonatype-publish.yml)
[![Tests](https://github.com/orion-automation/application/actions/workflows/push-trigger.yml/badge.svg)](https://github.com/orion-automation/enhanced-job-executor/actions/workflows/push-trigger.yml)
[![Maven Central](https://maven-badges.sml.io/sonatype-central/com.eorion.bo.plugin/enhanced-job-executor/badge.svg)](https://maven-badges.sml.io/sonatype-central/com.eorion.bo.plugin/enhanced-job-executor)

# Enhanced Job Executor for Camunda 7

An extension providing enhanced job executor features as follows:

* Configurable execution timeout for each job
* More detail job execution information

The default job execution mechanism in Camunda 7 is straight and exquisite and 
also performs efficiently in most cases, however users might face troubles in the following situation:

* a dead loop in the job, not only coding missing but also in BPMN diagrams, which causes a job worker thread running forever
* monitor the executing jobs, esp. in the case of all job worker threads are running and no more jobs can be executed
* record which thread executes which job and its execution duration together with execution start and end time

These are just what the extension provided.

## Enhancement Job Log Table

The plugin can record some information into a new table named `ENHANCEMENT_JOB_LOG`, which has the following important fields:

* `JOB_STATE_`  
Indicates the status of current job: 1 - created and running, 2 - normally completed, 3 - error/exception occurs
* `TIMEOUT_`  
The timeout setting of the current job in milliseconds
* `EXECUTION_START_TIMESTAMP_`  
The system time when the job started to be executed
* `EXECUTION_COMPLETE_TIMESTAMP_`  
The system time when the job completed to be executed
* `EXECUTION_DURATION_`  
The duration of the job execution in millisecond

## Installation

> Tested Environment:
>* Camunda Platform 7 v7.22.0
>* Java 21

### Prerequisite

A new table named `ENHANCEMENT_JOB_LOG` should be created manually if `enableDBLog` flag is `true`, the DDL of which can be found under `src/main/resources/db/schema`.
Currently, only `h2`, `mysql`, `mariadb`, `oracle` and `postgresql` are supported.

### For Embedded Process Engine

Add this extension as a dependency to your application. When using Maven, you may add the following codes to the `pom.xml` file:

```xml
<dependency>
  <groupId>com.eorionsolution.camunda.plugin</groupId>
  <artifactId>enhanced-job-executor</artifactId>
  <version>7.22.0</version>
</dependency>
```

### For Shared Process Engine (tomcat for example)

Copy `enhanced-job-executor-1.0.0.jar` to your application server's classpath lib (e.g. `apache-tomcat-<version>\lib`)

### For Spring Boot Application

A spring boot starter `enhanced-job-executor-spring-boot-starter` can be used. 
For detail information, follow that project.

## Configuration

The extension is built on top of the `CommandInterceptor` and introduces it into Camunda Engine via `getCustomPostCommandInterceptorsTxRequired`. 
Therefore, it can be registered in Camunda Configuration's plugins.

For `bpm-platform.xml` in Camunda tomcat distribution:

```xml

<plugins>
    <plugin>
        <class>com.eorionsolution.camunda.plugin.enhancedjobexecutor.EnhancedJobExecutorPlugin</class>
        <properties>
            <property name="timeoutMs">600000</property>
            <property name="enableDBLog">true</property>
        </properties>
    </plugin>
    <!-- Other plugins in the following-->
</plugins>
```

### Configuration Properties

| Name        | Type    | Default Value | Desc.                                                            |
|-------------|---------|---------------|------------------------------------------------------------------|
| timeoutMs   | long    | 6000000       | Timeout of job worker thread  (*)                                |
| enableDBLog | boolean | false         | whether or not record enhanced job execution information into db |

For setting different timeout than the default value for each job, an `extension properties` is used in BPMN diagram.
You can add the following information for the starting element of a job which is usually `timer-event` or `async-continuation`:
```xml
<bpmn:extensionElements>
  <camunda:properties>
    <camunda:property name="timeout" value="30000" />
  </camunda:properties>
</bpmn:extensionElements>
```

## Limitation

In Camunda 7, there are some system jobs, such as `history-cleanup` and `batch-monitor-job`.
They do not associate with a certain BPMN diagram and are executed beyond users' control.
The extension ignores the `history-cleanup` job, and uses the default timeout value for `batch-monitor-job`.
Therefore, please consider not to set the value too small which may cause `batch-monitor-job` timeout during the period.

## Further Consideration

JDK 21 introduces [`Virtual thread`](https://openjdk.org/jeps/444) feature via Project Loom which is lightweight threads designed to handle **high concurrency with minimal resource overhead**.
It may be suitable to use the virtual thread to execute each job itself, according to its design purpose, 
esp. in the following use cases:

* High-Concurrency
* Blocking I/O

However, considering the complexity of jobs, esp. when using script language in `Script Task`, 
the implementation currently uses the traditional thread pattern. 
If `virtual thread` is suitable for your use case, please consider using `Executors#newVirtualThreadPerTaskExecutor` instead.
