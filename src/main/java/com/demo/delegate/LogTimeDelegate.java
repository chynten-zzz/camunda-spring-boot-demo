package com.demo.delegate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("LogTimeDelegate")
public class LogTimeDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogTimeDelegate.class);

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        LOGGER.info(execution.getVariable("customVariable") + LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME));

        execution.setVariable("fileName","CollabAPI");
        Thread.sleep(30000);
        LOGGER.info("DONE!");
    }

}