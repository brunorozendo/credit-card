package com.bank.creditcard.config

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import spock.lang.Specification

import java.util.concurrent.Executor

class ConfigurationSpec extends Specification {

    def "should create metrics configuration"() {
        given: "a meter registry"
        def registry = new SimpleMeterRegistry()
        def config = new MetricsConfig()
        
        when: "creating timed aspect"
        def timedAspect = config.timedAspect(registry)
        
        then: "timed aspect is created"
        timedAspect != null
    }

    def "should create async configuration"() {
        given: "async config"
        def config = new AsyncConfig()
        
        when: "creating application processor executor"
        def executor = config.applicationProcessorExecutor()
        
        then: "executor is configured correctly"
        executor instanceof ThreadPoolTaskExecutor
        
        and: "thread pool settings are correct"
        def threadPoolExecutor = executor as ThreadPoolTaskExecutor
        threadPoolExecutor.corePoolSize == 5
        threadPoolExecutor.maxPoolSize == 10
        threadPoolExecutor.queueCapacity == 100
        threadPoolExecutor.threadNamePrefix == "AppProcessor-"
        threadPoolExecutor.awaitTerminationSeconds == 60
        threadPoolExecutor.waitForTasksToCompleteOnShutdown
    }

    def "should create OpenAPI configuration"() {
        given: "OpenAPI config"
        def config = new OpenApiConfig()
        
        when: "creating OpenAPI bean"
        def openAPI = config.creditCardOpenAPI()
        
        then: "OpenAPI is configured correctly"
        openAPI != null
        openAPI.info.title == "Credit Card Application API"
        openAPI.info.description.contains("risk assessment")
        openAPI.info.version == "1.0.0"
        openAPI.info.contact.name == "API Support"
        openAPI.info.contact.email == "support@bank.com"
        openAPI.info.license.name == "Apache 2.0"
        
        and: "security is configured"
        openAPI.security.size() == 1
        openAPI.security[0].get("basicAuth") != null
        openAPI.components.securitySchemes.get("basicAuth").type.toString() == "HTTP"
        openAPI.components.securitySchemes.get("basicAuth").scheme == "basic"
    }

    def "should handle executor shutdown"() {
        given: "an executor from async config"
        def config = new AsyncConfig()
        def executor = config.applicationProcessorExecutor() as ThreadPoolTaskExecutor
        
        when: "submitting a task"
        def taskExecuted = false
        executor.execute({
            taskExecuted = true
        })
        
        then: "task can be executed"
        Thread.sleep(100) // Give time for execution
        taskExecuted == true
        
        cleanup: "shutdown executor"
        executor.shutdown()
    }

    def "should validate thread pool configuration"() {
        given: "async config"
        def config = new AsyncConfig()
        
        when: "getting executor as ThreadPoolTaskExecutor"
        def executor = config.applicationProcessorExecutor() as ThreadPoolTaskExecutor
        
        then: "all properties are set correctly"
        with(executor) {
            corePoolSize == 5
            maxPoolSize == 10
            queueCapacity == 100
            threadNamePrefix == "AppProcessor-"
            awaitTerminationSeconds == 60
            isWaitForTasksToCompleteOnShutdown()
        }
    }

    def "should handle concurrent task execution"() {
        given: "an executor"
        def config = new AsyncConfig()
        def executor = config.applicationProcessorExecutor()
        def counter = 0
        def lock = new Object()
        
        when: "submitting multiple tasks"
        10.times { i ->
            executor.execute({
                synchronized(lock) {
                    counter++
                }
            })
        }
        
        then: "all tasks are executed"
        Thread.sleep(200) // Give time for all tasks
        counter == 10
        
        cleanup:
        (executor as ThreadPoolTaskExecutor).shutdown()
    }
}
