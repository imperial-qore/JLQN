package jlqn.model;

import jlqn.gui.xml.JLQNDocumentConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static jlqn.common.JLQNConstants.*;

public class JLQNDocumentCreator {

    /**
     * Creates a DOM representation of this object
     *
     * @return a DOM representation of this object
     */
    public static Document createDocument(JLQNModel model) {
        Document root;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            root = dbf.newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException pce) {
            throw new RuntimeException(pce);
        }

        /* model */
        Element modelElement = createModelElement(root);

        /* parameters */
        Element parametersElement = root.createElement("parameters");
        modelElement.appendChild(parametersElement);

        /* Processors */
        addProcessors(model, root, parametersElement);

        /* Tasks */
        addTasks(model, root, parametersElement);

        /* Entries */
        addEntries(model, root, parametersElement);

        /* Activities */
        addActivities(model, root, parametersElement);

        /* Calls */
        addCalls(model, root, parametersElement);

        /* Precedences */
        addPrecedences(model, root, parametersElement);

        if (model.inputValid() && model.areResultsOK()) {
            appendSolutionElement(model, root, modelElement);
        }

        return root;
    }

    private static Element createModelElement(Document root) {
        Element modelElement = root.createElement("jlqn");

        modelElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        modelElement.setAttribute("xsi:noNamespaceSchemaLocation", "JMTmodel.xsd");

        root.appendChild(modelElement);
        return modelElement;
    }

    private static void addProcessors(JLQNModel model, Document root, Element parametersElement) {
        int numberOfProcessors = model.getNumberOfProcessors();
        Element processors_element = root.createElement("processors");
        parametersElement.appendChild(processors_element);
        processors_element.setAttribute(JLQNDocumentConstants.DOC_PROCESSOR_NUMBER, Integer.toString(numberOfProcessors));
        for (int i = 0; i < numberOfProcessors; i++) {
            processors_element.appendChild(makeProcessorElement(model, root, i));
        }
    }

    private static Element makeProcessorElement(JLQNModel model, Document root, int processorNum) {
        Element processorElement;

        String[] processorNames = model.getProcessorNames();
        int[] processorScheduling = model.getProcessorScheduling();
        double[] processorQuantum = model.getProcessorQuantum();
        int[] processorMultiplicity = model.getProcessorMultiplicity();
        int[] processorReplicas = model.getProcessorReplicas();
        double[] processorSpeedFactor = model.getProcessorSpeedFactor();
        processorElement = root.createElement("processor");
        if (processorMultiplicity[processorNum] == Integer.MAX_VALUE) {
            processorElement.setAttribute(JLQNDocumentConstants.DOC_PROCESSOR_MULTIPLICITY, "-1");
        } else {
            processorElement.setAttribute(JLQNDocumentConstants.DOC_PROCESSOR_MULTIPLICITY, Integer.toString(processorMultiplicity[processorNum]));
        }
        processorElement.setAttribute(JLQNDocumentConstants.DOC_PROCESSOR_NAME, processorNames[processorNum]);
        processorElement.setAttribute(JLQNDocumentConstants.DOC_PROCESSOR_QUANTUM, Double.toString(processorQuantum[processorNum]));
        processorElement.setAttribute(JLQNDocumentConstants.DOC_PROCESSOR_REPLICAS, Integer.toString(processorReplicas[processorNum]));
        processorElement.setAttribute(JLQNDocumentConstants.DOC_PROCESSOR_SCHEDULING, PROCESSOR_SCHEDULING_TYPENAMES[processorScheduling[processorNum]]);
        processorElement.setAttribute(JLQNDocumentConstants.DOC_PROCESSOR_SPEED_FACTOR, Double.toString(processorSpeedFactor[processorNum]));
        return processorElement;
    }

    private static void addTasks(JLQNModel model, Document root, Element parametersElement) {
        int numberOfTasks = model.getNumberOfTasks();
        Element tasks_element = root.createElement("tasks");
        parametersElement.appendChild(tasks_element);
        tasks_element.setAttribute(JLQNDocumentConstants.DOC_TASK_NUMBER, Integer.toString(numberOfTasks));
        for (int i = 0; i < numberOfTasks; i++) {
            tasks_element.appendChild(makeTaskElement(model, root, i));
        }
    }

    private static Element makeTaskElement(JLQNModel model, Document root, int taskNum) {
        Element taskElement;

        String[] taskNames = model.getTaskNames();
        int[] taskScheduling = model.getTaskScheduling();
        String[] taskProcessor = model.getTaskProcessor();
        int[] taskPriority = model.getTaskPriority();
        double[] taskThinkTimeMean = model.getTaskThinkTimeMean();
        int[] taskMultiplicity = model.getTaskMultiplicity();
        int[] taskReplicas = model.getTaskReplicas();

        taskElement = root.createElement("task");
        taskElement.setAttribute(JLQNDocumentConstants.DOC_TASK_MULTIPLICITY, Integer.toString(taskMultiplicity[taskNum]));
        taskElement.setAttribute(JLQNDocumentConstants.DOC_TASK_NAME, taskNames[taskNum]);
        taskElement.setAttribute(JLQNDocumentConstants.DOC_TASK_PRIORITY, Integer.toString(taskPriority[taskNum]));
        taskElement.setAttribute(JLQNDocumentConstants.DOC_TASK_PROCESSOR, taskProcessor[taskNum]);
        taskElement.setAttribute(JLQNDocumentConstants.DOC_TASK_REPLICAS, Integer.toString(taskReplicas[taskNum]));
        taskElement.setAttribute(JLQNDocumentConstants.DOC_TASK_SCHEDULING, TASK_SCHEDULING_NAMES[taskScheduling[taskNum]]);
        taskElement.setAttribute(JLQNDocumentConstants.DOC_TASK_THINK_TIME_MEAN, Double.toString(taskThinkTimeMean[taskNum]));
        return taskElement;
    }

    private static void addEntries(JLQNModel model, Document root, Element parametersElement) {
        int numberOfEntries = model.getNumberOfEntries();
        Element entries_element = root.createElement("entries");
        parametersElement.appendChild(entries_element);
        entries_element.setAttribute(JLQNDocumentConstants.DOC_ENTRY_NUMBER, Integer.toString(numberOfEntries));
        for (int i = 0; i < numberOfEntries; i++) {
            entries_element.appendChild(makeEntryElement(model, root, i));
        }
    }

    private static Element makeEntryElement(JLQNModel model, Document root, int entryNum) {
        Element entryElement;

        String[] entryNames = model.getEntryNames();
        String[] entryTask = model.getEntryTask();
        String[] entryBoundToActivity = model.getEntryBoundToActivity();
        String[] entryReplyToActivity = model.getEntryReplyToActivity();
        double[] entryArrivalRate = model.getEntryArrivalRate();
        int[] entryPriority = model.getEntryPriority();

        entryElement = root.createElement("entry");
        entryElement.setAttribute(JLQNDocumentConstants.DOC_ENTRY_NAME, entryNames[entryNum]);
        entryElement.setAttribute(JLQNDocumentConstants.DOC_ENTRY_TASK, entryTask[entryNum]);
        entryElement.setAttribute(JLQNDocumentConstants.DOC_ENTRY_BOUND_TO_ACTIVITY, entryBoundToActivity[entryNum]);
        entryElement.setAttribute(JLQNDocumentConstants.DOC_ENTRY_REPLY_TO_ACTIVITY, entryReplyToActivity[entryNum]);
        entryElement.setAttribute(JLQNDocumentConstants.DOC_ENTRY_ARRIVAL_RATE, Double.toString(entryArrivalRate[entryNum]));
        entryElement.setAttribute(JLQNDocumentConstants.DOC_ENTRY_PRIORITY, Integer.toString(entryPriority[entryNum]));
        return entryElement;
    }

    private static void addActivities(JLQNModel model, Document root, Element parametersElement) {
        int numberOfActivities = model.getNumberOfActivities();
        Element activities_element = root.createElement("activities");
        parametersElement.appendChild(activities_element);
        activities_element.setAttribute(JLQNDocumentConstants.DOC_ACTIVITY_NUMBER, Integer.toString(numberOfActivities));
        for (int i = 0; i < numberOfActivities; i++) {
            activities_element.appendChild(makeActivityElement(model, root, i));
        }
    }

    private static Element makeActivityElement(JLQNModel model, Document root, int activityNum) {
        Element activityElement;

        String[] activityNames = model.getActivityNames();
        String[] activityTask = model.getActivityTask();
        double[] activityHostDemand = model.getActivityHostDemand();

        activityElement = root.createElement("activity");
        activityElement.setAttribute(JLQNDocumentConstants.DOC_ACTIVITY_NAME, activityNames[activityNum]);
        activityElement.setAttribute(JLQNDocumentConstants.DOC_ACTIVITY_TASK, activityTask[activityNum]);
        activityElement.setAttribute(JLQNDocumentConstants.DOC_ACTIVITY_DEMAND_MEAN, Double.toString(activityHostDemand[activityNum]));
        return activityElement;
    }

    private static void addCalls(JLQNModel model, Document root, Element parametersElement) {
        int numberOfCalls = model.getNumberOfCalls();
        Element calls_element = root.createElement("calls");
        parametersElement.appendChild(calls_element);
        calls_element.setAttribute(JLQNDocumentConstants.DOC_CALL_NUMBER, Integer.toString(numberOfCalls));
        for (int i = 0; i < numberOfCalls; i++) {
            calls_element.appendChild(makeCallElement(model, root, i));
        }
    }

    private static void addPrecedences(JLQNModel model, Document root, Element parametersElement) {
        int numberOfPrecedences = model.getNumberOfPrecedences();
        Element precedence_element = root.createElement("precedences");
        parametersElement.appendChild(precedence_element);
        precedence_element.setAttribute(JLQNDocumentConstants.DOC_PRECEDENCE_NUMBER, Integer.toString(numberOfPrecedences));
        for (int i = 0; i < numberOfPrecedences; i++) {
            precedence_element.appendChild(makePrecedenceElement(model, root, i));
        }
    }

    private static Element makePrecedenceElement(JLQNModel model, Document root, int precedenceNum) {
        Element callElement;

        int[] precedenceType = model.getPrecedenceType();
        String[][] precedencePreActivity = model.getPrecedencePreActivities();
        String[][] precedencePostActivity = model.getPrecedencePostActivities();
        Double[][] precedencePreParams = model.getPrecedencePreParams();
        Double[][] precedencePostParams = model.getPrecedencePostParams();

        callElement = root.createElement("precedence");
        switch (precedenceType[precedenceNum]){
            case PRECEDENCE_SEQUENCE:
                callElement.setAttribute(JLQNDocumentConstants.DOC_PRECEDENCE_TYPE, "seq");
                break;
            case PRECEDENCE_OR_FORK:
                callElement.setAttribute(JLQNDocumentConstants.DOC_PRECEDENCE_TYPE, "or-fork");
                break;
            case PRECEDENCE_OR_JOIN:
                callElement.setAttribute(JLQNDocumentConstants.DOC_PRECEDENCE_TYPE, "or-join");
                break;
            case PRECEDENCE_AND_FORK:
                callElement.setAttribute(JLQNDocumentConstants.DOC_PRECEDENCE_TYPE, "and-fork");
                break;
            case PRECEDENCE_AND_JOIN:
                callElement.setAttribute(JLQNDocumentConstants.DOC_PRECEDENCE_TYPE, "and-join");
                break;
            case PRECEDENCE_LOOP:
                callElement.setAttribute(JLQNDocumentConstants.DOC_PRECEDENCE_TYPE, "loop");
                break;
            default:
                // no-op
        }
        for (int preIdx = 0; preIdx < precedencePreActivity[precedenceNum].length; preIdx++) {
            Element preActivity = root.createElement("precedence-activity");
            preActivity.setAttribute(JLQNDocumentConstants.DOC_PRECEDENCE_ACTIVITY_TYPE, "pre");
            preActivity.setAttribute(JLQNDocumentConstants.DOC_PRECEDENCE_ACTIVITY, precedencePreActivity[precedenceNum][preIdx]);
            preActivity.setAttribute(JLQNDocumentConstants.DOC_PRECEDENCE_PARAMS, precedencePreParams[precedenceNum][preIdx].toString());
            callElement.appendChild(preActivity);
        }
        for (int postIdx = 0; postIdx < precedencePostActivity[precedenceNum].length; postIdx++) {
            Element postActivity = root.createElement("precedence-activity");
            if (precedencePostParams[precedenceNum][postIdx] != null) {
                postActivity.setAttribute(JLQNDocumentConstants.DOC_PRECEDENCE_ACTIVITY_TYPE, "post");
                postActivity.setAttribute(JLQNDocumentConstants.DOC_PRECEDENCE_ACTIVITY, precedencePostActivity[precedenceNum][postIdx]);
                postActivity.setAttribute(JLQNDocumentConstants.DOC_PRECEDENCE_PARAMS, precedencePostParams[precedenceNum][postIdx].toString());
            } else {
                postActivity.setAttribute(JLQNDocumentConstants.DOC_PRECEDENCE_ACTIVITY_TYPE, "end");
                postActivity.setAttribute(JLQNDocumentConstants.DOC_PRECEDENCE_ACTIVITY, precedencePostActivity[precedenceNum][postIdx]);
                postActivity.setAttribute(JLQNDocumentConstants.DOC_PRECEDENCE_PARAMS, "1.0");
            }
            callElement.appendChild(postActivity);
        }
        return callElement;
    }

    private static Element makeCallElement(JLQNModel model, Document root, int callNum) {
        Element callElement;

        String[] callNames = model.getCallNames();
        String[] callActivity = model.getCallActivity();
        String[] callEntry = model.getCallEntry();
        int[] callType = model.getCallType();
        double[] callMeanRepeatTimes = model.getCallMeanRepeatTimes();

        callElement = root.createElement("call");
        callElement.setAttribute(JLQNDocumentConstants.DOC_CALL_NAME, callNames[callNum]);
        callElement.setAttribute(JLQNDocumentConstants.DOC_CALL_ACTIVITY, callActivity[callNum]);
        callElement.setAttribute(JLQNDocumentConstants.DOC_CALL_ENTRY, callEntry[callNum]);
        callElement.setAttribute(JLQNDocumentConstants.DOC_CALL_TYPE, CALL_TYPES[callType[callNum]]);
        callElement.setAttribute(JLQNDocumentConstants.DOC_CALL_MEAN_REPEAT, Double.toString(callMeanRepeatTimes[callNum]));
        return callElement;
    }

    private static void appendSolutionElement(JLQNModel model, Document root, Element parentElement) {
        int numberOfProcessors = model.getNumberOfProcessors();
        int numberOfTasks = model.getNumberOfTasks();
        int numberOfEntries = model.getNumberOfEntries();
        int numberOfActivities = model.getNumberOfActivities();

        String[] processorNames = model.getProcessorNames();
        String[] taskNames = model.getTaskNames();
        String[] entryNames = model.getEntryNames();
        String[] activityNames = model.getActivityNames();

        Double[][] queueLen = model.getQueueLen();
        Double[][] throughput = model.getThroughput();
        Double[][] residTimes = model.getResidTimes();
        Double[][] respTimes = model.getRespTimes();
        Double[][] util = model.getUtil();

        Element result_element = root.createElement("solutions");
        parentElement.appendChild(result_element);
        result_element.setAttribute(JLQNDocumentConstants.DOC_NODE_COUNT, Integer.toString(queueLen.length));

        for (int i = 0; i < numberOfProcessors; i++) {
            Element processor_result_element = root.createElement(JLQNDocumentConstants.DOC_RESULT_PROCESSOR);
            result_element.appendChild(processor_result_element);

            processor_result_element.setAttribute(JLQNDocumentConstants.DOC_PROCESSOR_NAME, processorNames[i]);
            processor_result_element.setAttribute(JLQNDocumentConstants.DOC_QUEUE_LEN, Double.toString(queueLen[i][0]));
            processor_result_element.setAttribute(JLQNDocumentConstants.DOC_THROUGHPUT, Double.toString(throughput[i][0]));
            processor_result_element.setAttribute(JLQNDocumentConstants.DOC_RESID_TIME, Double.toString(residTimes[i][0]));
            processor_result_element.setAttribute(JLQNDocumentConstants.DOC_RESP_TIME, Double.toString(respTimes[i][0]));
            processor_result_element.setAttribute(JLQNDocumentConstants.DOC_UTIL, Double.toString(util[i][0]));
        }

        for (int i = numberOfProcessors; i < numberOfProcessors + numberOfTasks; i++) {
            Element task_result_element = root.createElement(JLQNDocumentConstants.DOC_RESULT_TASK);
            result_element.appendChild(task_result_element);

            task_result_element.setAttribute(JLQNDocumentConstants.DOC_TASK_NAME, taskNames[i]);
            task_result_element.setAttribute(JLQNDocumentConstants.DOC_QUEUE_LEN, Double.toString(queueLen[i][0]));
            task_result_element.setAttribute(JLQNDocumentConstants.DOC_THROUGHPUT, Double.toString(throughput[i][0]));
            task_result_element.setAttribute(JLQNDocumentConstants.DOC_RESID_TIME, Double.toString(residTimes[i][0]));
            task_result_element.setAttribute(JLQNDocumentConstants.DOC_RESP_TIME, Double.toString(respTimes[i][0]));
            task_result_element.setAttribute(JLQNDocumentConstants.DOC_UTIL, Double.toString(util[i][0]));
        }

        for (int i = numberOfProcessors + numberOfTasks;
             i < numberOfProcessors + numberOfTasks + numberOfEntries; i++) {
            Element entry_result_element = root.createElement(JLQNDocumentConstants.DOC_RESULT_ENTRY);
            result_element.appendChild(entry_result_element);

            entry_result_element.setAttribute(JLQNDocumentConstants.DOC_ENTRY_NAME, entryNames[i]);
            entry_result_element.setAttribute(JLQNDocumentConstants.DOC_QUEUE_LEN, Double.toString(queueLen[i][0]));
            entry_result_element.setAttribute(JLQNDocumentConstants.DOC_THROUGHPUT, Double.toString(throughput[i][0]));
            entry_result_element.setAttribute(JLQNDocumentConstants.DOC_RESID_TIME, Double.toString(residTimes[i][0]));
            entry_result_element.setAttribute(JLQNDocumentConstants.DOC_RESP_TIME, Double.toString(respTimes[i][0]));
            entry_result_element.setAttribute(JLQNDocumentConstants.DOC_UTIL, Double.toString(util[i][0]));
        }

        for (int i = numberOfProcessors + numberOfTasks + numberOfEntries;
             i < numberOfProcessors + numberOfTasks + numberOfEntries + numberOfActivities; i++) {
            Element activity_result_element = root.createElement(JLQNDocumentConstants.DOC_RESULT_ACTIVITY);
            result_element.appendChild(activity_result_element);

            activity_result_element.setAttribute(JLQNDocumentConstants.DOC_ACTIVITY_NAME, activityNames[i]);
            activity_result_element.setAttribute(JLQNDocumentConstants.DOC_QUEUE_LEN, Double.toString(queueLen[i][0]));
            activity_result_element.setAttribute(JLQNDocumentConstants.DOC_THROUGHPUT, Double.toString(throughput[i][0]));
            activity_result_element.setAttribute(JLQNDocumentConstants.DOC_RESID_TIME, Double.toString(residTimes[i][0]));
            activity_result_element.setAttribute(JLQNDocumentConstants.DOC_RESP_TIME, Double.toString(respTimes[i][0]));
            activity_result_element.setAttribute(JLQNDocumentConstants.DOC_UTIL, Double.toString(util[i][0]));
        }

    }
}
