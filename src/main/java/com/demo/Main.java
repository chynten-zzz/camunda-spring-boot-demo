package com.demo;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootApplication
@EnableProcessApplication
@RestController
public class Main {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private RepositoryService repositoryService;

    public static void main(String... args) {
        SpringApplication.run(Main.class, args);
    }

    @RequestMapping("/import")
    public String importDef() throws IOException {
        BpmnModelInstance bpmnModelInstance = Bpmn.createExecutableProcess("example.bpmn").id("example")
                .startEvent()
                .serviceTask().camundaAsyncBefore()
                .camundaClass("com.demo.delegate.LogTimeDelegate")
                .endEvent()
                .done();

        repositoryService.createDeployment().addModelInstance("example.bpmn", bpmnModelInstance).deploy();

        return "Success";
    }

    @RequestMapping("/hello")
    public String hello() {

        Map<String, Object> variables = new HashMap<>();
        variables.put("customVariable", "This message needs to print in log ");

        ProcessInstance loanApproval = runtimeService.startProcessInstanceByKey("example", variables);
        return "Hello World! " + loanApproval.getId();
    }

    @RequestMapping("/list/{status}")
    public List<String> hello1(@PathVariable("status") String status) {
        // query for latest process definition with given name
        ProcessDefinition myProcessDefinition =
                repositoryService.createProcessDefinitionQuery().processDefinitionKey("example")
                        .latestVersion()
                        .singleResult();
        List<String> processInstances = null;
        if (status.equals("r")) {
            processInstances =
                    runtimeService.createProcessInstanceQuery()
                            .processDefinitionId(myProcessDefinition.getId())
                            .active() // we only want the unsuspended process instances
                            .list().stream().map(pi -> pi.getId())
                            .collect(Collectors.toList());
        } else if (status.equals("f")) {
            processInstances =
                    historyService.createHistoricProcessInstanceQuery()
                            .processDefinitionId(myProcessDefinition.getId())
                            .finished() // we only want the unsuspended process instances
                            .list().stream().map(pi -> pi.getId())
                            .collect(Collectors.toList());
        }
        return processInstances;
    }

}