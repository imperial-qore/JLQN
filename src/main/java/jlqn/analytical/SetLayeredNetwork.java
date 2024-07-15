package jlqn.analytical;

import jline.lang.constant.SchedStrategy;
import jline.lang.layered.*;
import jline.util.Matrix;
import jmt.framework.data.ArrayUtils;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

public class SetLayeredNetwork {
    public static LayeredNetwork SetLayeredNetworkFromJLQN(JLQNModel model, StringBuilder error) {
        LayeredNetwork myLN = new LayeredNetwork("LayeredNetwork Model");
        int processors = model.getNumberOfProcessors();
        String[] processorNames = model.getProcessorNames();
        int[] processorScheduling = model.getProcessorScheduling();
        String[] processorSchedAbbr = {
                "fcfs",
                "inf",
                "ps",
                "siro",
                "sept",
                "hol"
        };
        double[] processorQuantum = model.getProcessorQuantum();
        int[] processorMultiplicity = model.getProcessorMultiplicity();
        int[] processorReplicas = model.getProcessorReplicas();
        double[] processorSF = model.getProcessorSpeedFactor();

        //task attributes
        int tasks = model.getNumberOfTasks();
        String[] taskNames = model.getTaskNames();
        int[] taskScheduling = model.getTaskScheduling();
        //this taskSched needs to be modified after making clear which task scheduling are valid
        String[] taskSchedAbbr = {
                "ref",
                "fcfs",
                "hol",
                "inf"
        };
        String[] taskProcessor = model.getTaskProcessor();
        int[] taskPriority = model.getTaskPriority();
        double[] taskThinkTimeMean = model.getTaskThinkTimeMean();
        int[] taskMultiplicity = model.getTaskMultiplicity();
        int[] taskReplicas = model.getTaskReplicas();

        //entry attributes;
        int entries = model.getNumberOfEntries();
        String[] entryNames = model.getEntryNames();
        String[] entryTaskNames = model.getEntryTask();
        String[] entryBoundToActivities = model.getEntryBoundToActivity();
        String[] entryReplyToActivities = model.getEntryReplyToActivity();
        double[] entryArrivalRate = model.getEntryArrivalRate();
        int[] entryPriority = model.getEntryPriority();

        //activity attributes
        int numberOfActivities = model.getNumberOfActivities();
        String[] activityNames = model.getActivityNames();
        String[] activityTask = model.getActivityTask();
        double[] activityThinkTimeMean = model.getActivityHostDemand();

        // calls information
        int numberOfCalls = model.getNumberOfCalls();
        String[] callActivities = model.getCallActivity();
        String[] callEntries = model.getCallEntry();
        int[] callTypes = model.getCallType();
        double[] callMeanRepeatTimes = model.getCallMeanRepeatTimes();

        // precedence information
        int numberOfPrecedences = model.getNumberOfPrecedences();
        int[] precedenceTypes = model.getPrecedenceType();
        String[][] precedencePreActivities = model.getPrecedencePreActivities();
        String[][] precedencePostActivities = model.getPrecedencePostActivities();
        Double[][] precedencePreParams = model.getPrecedencePreParams();
        Double[][] precedencePostParams = model.getPrecedencePostParams();


        HashMap<String, Activity> activityMap = new HashMap<>();
        HashMap<String, Processor> processorMap = new HashMap<>();
        HashMap<String, Task> taskMap = new HashMap<>();
        HashMap<String, Entry> entryMap = new HashMap<>();
        // These Hashmaps are for validity checking purposes.
        // This maps an entry name to its parent task name
        HashMap<String, String> entryTaskMap = model.getEntryTaskMap();
        HashMap<String, String> activityParentTaskMap = model.getActivityParentTaskMap();
        // This maps an activity name to its parent task name

        // Creating the Layered Network

        // Initialise base components (processors, tasks, etc)
        for (int p = 0; p < processors; p++) {
            SchedStrategy procSched = SchedStrategy.fromLINEString(processorSchedAbbr[processorScheduling[p]]);
            Processor newProc = new Processor(myLN, processorNames[p], processorMultiplicity[p], procSched, 0.001, processorSF[p]);
            newProc.setReplication(processorReplicas[p]);
            processorMap.put(processorNames[p], newProc);
        }

        for (int t = 0; t < tasks; t++) {
            SchedStrategy taskSched = SchedStrategy.fromLINEString(taskSchedAbbr[taskScheduling[t]]);
            Task newTask = new Task(myLN, taskNames[t], taskMultiplicity[t], taskSched);
            // TODO: This works with LN solver but not with LQNS:
//            newTask.setThinkTime(new Exp(taskThinkTimeMean[t]));
            // TODO: This works with LQNS solver:
            newTask.setThinkTime(taskThinkTimeMean[t]);
            newTask.setReplication(taskReplicas[t]);
            taskMap.put(taskNames[t], newTask);

            // Link Task to Processors
            Processor parentProc = processorMap.get(taskProcessor[t]);
            newTask.on(parentProc);
        }

        for (int e = 0; e < entries; e++) {
            Entry newEntry = new Entry(myLN, entryNames[e]);
            entryMap.put(entryNames[e], newEntry);

            // Link Entry to Task
            Task parentTask = taskMap.get(entryTaskNames[e]);
            newEntry.on(parentTask);
            entryTaskMap.put(entryNames[e], entryTaskNames[e]);
        }

        for (int a = 0; a < numberOfActivities; a++) {
            Activity newActivity = new Activity(myLN, activityNames[a]);
            newActivity.setHostDemand(activityThinkTimeMean[a]);
            activityMap.put(activityNames[a], newActivity);

            // Link Activity to Task (Through its entry)
            String parentTaskName = activityTask[a];
            Task parentTask = taskMap.get(parentTaskName);
            activityParentTaskMap.put(activityNames[a], parentTaskName);
            newActivity.on(parentTask);
        }

        for (int e = 0; e < entries; e++) {
            Entry currentEntry = entryMap.get(entryNames[e]);
            // Bind Activity to Entry
            String entryTaskParent = entryTaskMap.get(entryNames[e]);
            Activity boundToActivity = activityMap.get(entryBoundToActivities[e]);
            boundToActivity.boundTo(currentEntry);

            // Bind Activity to Entry
            if (!entryReplyToActivities[e].equals("N/A")) {
                try {
                    // This is the only error that SetLayeredNetwork will catch
                    // Because the error originates from the Activity Class.
                    // All other errors will be checked in JLQNModelErrorChecker
                    activityMap.get(entryReplyToActivities[e]).repliesTo(currentEntry);
                } catch (Exception exception) {
                    error.append(exception);
                    error.append("<br>");
                }
            }
        }

        // Adding in calls
        for (int c = 0; c < numberOfCalls; c++) {
            Entry callEntry = entryMap.get(callEntries[c]);
            Activity callActivity = activityMap.get(callActivities[c]);
            switch (callTypes[c]) {
                case 0: // Synchronous
                    callActivity.synchCall(callEntry, callMeanRepeatTimes[c]);
                    break;
                case 1: // Asynchronous
                    callActivity.asynchCall(callEntry, callMeanRepeatTimes[c]);
                    break;
                case 2: // Forwarding
                    // TODO: Implement forwarding capability
                    break;
                default:
                    break;
            }
        }
        // Adding in precedences
        for (int p = 0; p < numberOfPrecedences; p++) {
            String[] preActs = precedencePreActivities[p];
            String[] postActs = precedencePostActivities[p];
            Double[] preParams = precedencePreParams[p];
            Double[] postParams = precedencePostParams[p];
            // All tasks are sanitised to be under the same parent task
            String associatedTaskName = activityParentTaskMap.get(preActs[0]); //
            Task associatedTask = taskMap.get(associatedTaskName);
            switch(precedenceTypes[p]) {
                case 0: // SEQUENCE
                    int preActsLength = preActs.length;
                    // Leading activity is controlled to only be 1 activity
                    preActs = ArrayUtils.resize(preActs,preActs.length + postActs.length, null);
                    for (int i = 0; i < postActs.length; i++) {
                        preActs[i + preActsLength] = postActs[i];
                    }
                    ActivityPrecedence sequence;
                    for (int i = 0; i < preActs.length - 1; i++) {
                        String curr = preActs[i];
                        String next = preActs[i + 1];
                        sequence = ActivityPrecedence.Sequence(curr, next);
                        associatedTask.addPrecedence(sequence);
                    }
                    break;
                case 1: // OR FORK
                    Matrix selectionProbabilityParam = new Matrix(1, postActs.length);
                    for (int i = 0; i < postActs.length; i++) {
                        selectionProbabilityParam.set(0, i, postParams[i]);
                    }
                    ActivityPrecedence orFork = ActivityPrecedence.OrFork(preActs[0],Arrays.asList(postActs), selectionProbabilityParam);
                    associatedTask.addPrecedence(orFork);
                    break;
                case 2: // OR JOIN
                    ActivityPrecedence orJoin = ActivityPrecedence.OrJoin(Arrays.asList(preActs), postActs[0]);
                    associatedTask.addPrecedence(orJoin);
                    break;
                case 3: // AND FORK
                    Matrix fanOutParam = new Matrix(1, postActs.length);
                    for (int i = 0; i < postActs.length; i++) {
                        fanOutParam.set(0, i, postParams[i]);
                    }
                    ActivityPrecedence andFork = ActivityPrecedence.AndFork(preActs[0],Arrays.asList(postActs), fanOutParam);
                    associatedTask.addPrecedence(andFork);
                    break;
                case 4: // AND JOIN
                    Matrix fanInParam = new Matrix(1, preActs.length);
                    for (int i = 0; i < preActs.length; i++) {
                        fanInParam.set(0, i, preParams[i]);
                    }
                    ActivityPrecedence andJoin = ActivityPrecedence.AndJoin(Arrays.asList(preActs), postActs[0], fanInParam);
                    associatedTask.addPrecedence(andJoin);
                    break;
                case 5: // LOOP
                    Matrix loopParam = new Matrix(postParams[0]);
                    ActivityPrecedence loop = ActivityPrecedence.Loop(preActs[0],Arrays.asList(postActs), loopParam);
                    associatedTask.addPrecedence(loop);
                    break;
                default:
                    break;
            }
        }
        return myLN;
    }

    public static void outputXML(LayeredNetwork myLN) {
        try {
            File file = new File("");
            String filePath = file.getCanonicalPath();
            myLN.writeXML(filePath + "/result.xml", true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


//    public static void main(String[] args) throws IOException {
//        JLQNModel model = new JLQNModel();
//        model.setMockModel();
////        LayeredNetwork myLN = new LayeredNetwork("Mock LayeredNetwork Model");
//        LayeredNetwork myLN = SetLayeredNetwork(model);
//        myLN.summary(); //prints LayeredNetworkStruct field values
//
//        try{
//            File file = new File("");
//            String filePath = file.getCanonicalPath();
//            myLN.writeXML(filePath+"/mockXML.xml", true);
//        }
//        catch (IOException e){
//            e.printStackTrace();
//        }
//    }
}
