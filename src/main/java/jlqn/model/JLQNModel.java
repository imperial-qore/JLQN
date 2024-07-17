package jlqn.model;
import jlqn.common.JLQNConstants;
import jlqn.gui.xml.JLQNDocumentConstants;
import jlqn.util.ArrayUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.*;

public class JLQNModel implements JLQNConstants {
    /** PROCESSOR **/
    private int numberOfProcessors;
    private String[] processorNames;
    private int[] processorScheduling;
    private double[] processorQuantum;
    private int[] processorMultiplicity;
    private int[] processorReplicas;
    private double[] processorSpeedFactor;
    /** TASK **/

    private int numberOfTasks;
    private String[] taskNames;
    private int[] taskScheduling;
    private String[] taskProcessor;
    private int[] taskPriority;
    private double[] taskThinkTimeMean;

    private int[] taskMultiplicity;
    private int[] taskReplicas;


    /** ENTRY **/
    private int numberOfEntries;
    private String[] entryNames;
    private String[] entryTask;
    private String[] entryBoundToActivity;
    private String[] entryReplyToActivity;
    private double[] entryArrivalRate;
    private int[] entryPriority;

    /** ACTIVITY **/
    private int numberOfActivities;
    private String[] activityNames;
    private String[] activityTask;
    // Should be activityHost
    private double[] activityHostDemand;

    /** CALL **/
    private int numberOfCalls;
    private String[] callNames;
    private String[] callActivity;
    private String[] callEntry;
    private int[] callType;
    private double[] callMeanRepeatTimes;

    /** PRECEDENCES **/
    private int numberOfPrecedences;
    private int[] precedenceType;
    private String[][] precedencePreActivities;
    private String[][] precedencePostActivities;
    private Double[][] precedencePreParams;
    private Double[][] precedencePostParams;

    /** Checking Tools - Hashmaps and String buffers **/
    HashMap<String, String> entryTaskMap = new HashMap<>();
    HashMap<String, String> activityParentTaskMap = new HashMap<>();
    StringBuilder errors = new StringBuilder();


    // Viewer to use
    private ViewerType viewerType;

    // Solver to use
    private SolverType solverType;

    //true if the model has been modified
    private boolean changed;
    //true if the results are valid (no modify has been made in the model after results computation)
    private boolean resultsOK;

    //public double tolerance = SolverMultiClosedAMVA.DEFAULT_TOLERANCE;
    //private int maxSamples = SolverMultiClosedMonteCarloLogistic.DEFAULT_MAX_SAMPLES;

    /** RESULT **/

    private Double[][] queueLen;

    private Double[][] throughput;

    private Double[][] residTimes;
    private Double[][] respTimes;

    private Double[][] util;
    private int rowCounts;

    /**
     * Get the relevant processor attributes
     * @return the corresponding variables
     */
    public int getNumberOfProcessors() {
        return numberOfProcessors;
    }
    public String[] getProcessorNames() {
        return processorNames;
    }
    public int[] getProcessorReplicas() {
        return processorReplicas;
    }
    public double[] getProcessorQuantum() {
        return processorQuantum;
    }
    public double[] getProcessorSpeedFactor() {
        return processorSpeedFactor;
    }

    public int[] getProcessorMultiplicity() {
        return processorMultiplicity;
    }
    public int[] getProcessorScheduling() {
        return processorScheduling;
    }

    /**
     * Get the relevant task attributes
     * @return the corresponding variables
     */
    public int getNumberOfTasks() {
        return numberOfTasks;
    }
    public String[] getTaskNames() {
        return taskNames;
    }
    public int[] getTaskScheduling() {
        return taskScheduling;
    }
    public String[] getTaskProcessor() {
        return taskProcessor;
    }
    public double[] getTaskThinkTimeMean() {
        return taskThinkTimeMean;
    }

    public int[] getTaskReplicas() {
        return taskReplicas;
    }
    public int[] getTaskPriority() {
        return taskPriority;
    }
    public int[] getTaskMultiplicity() { return taskMultiplicity; }

    /**
     * Get the relevant entry attributes
     * @return the corresponding variables
     */
    public int getNumberOfEntries() {
        return numberOfEntries;
    }
    public String[] getEntryNames() {
        return entryNames;
    }
    public String[] getEntryTask() {
        return entryTask;
    }
    public String[] getEntryBoundToActivity() {
        return entryBoundToActivity;
    }
    public String[] getEntryReplyToActivity() {
        return entryReplyToActivity;
    }
    public double[] getEntryArrivalRate() {
        return entryArrivalRate;
    }
    public int[] getEntryPriority() {
        return entryPriority;
    }

    /**
     * Get the relevant activity attributes
     * @return the corresponding variables
     */
    public int getNumberOfActivities() {
        return numberOfActivities;
    }
    public String[] getActivityNames() {
        return activityNames;
    }
    public String[] getActivityTask() {
        return activityTask;
    }

    public double[] getActivityHostDemand() {
        return activityHostDemand;
    }
    /**
     * Get the relevant call attributes
     * @return the corresponding variables
     */
    public int getNumberOfCalls() {
        return numberOfCalls;
    }
    public String[] getCallNames() {
        return callNames;
    }
    public String[] getCallActivity() {
        return callActivity;
    }
    public String[] getCallEntry() {
        return callEntry;
    }
    public int[] getCallType() {
        return callType;
    }
    public double[] getCallMeanRepeatTimes() {
        return callMeanRepeatTimes;
    }

    /**
     * Get the relevant precedence attributes
     * @return the corresponding variables
     */
    public int getNumberOfPrecedences() { return numberOfPrecedences; }
    public int[] getPrecedenceType() { return precedenceType; }
    public String[][] getPrecedencePreActivities() { return precedencePreActivities; }
    public String[][] getPrecedencePostActivities() { return precedencePostActivities; }
    public Double[][] getPrecedencePreParams() { return precedencePreParams; }
    public Double[][] getPrecedencePostParams() { return precedencePostParams; }
    public ViewerType getViewerType() {
        return viewerType;
    }
    public SolverType getSolverType() {
        return solverType;
    }

    /**
     * Get the hashmaps created during checking phase. This is useful for conversion to LayeredNetwork
     */

    public HashMap<String, String> getEntryTaskMap() { return entryTaskMap; }
    public HashMap<String, String> getActivityParentTaskMap() { return activityParentTaskMap; }

    /**
     * Retrieves the parent or children of parts of the model
     *
     */

    public String[] getActivityChildrenFromTaskParent(String taskName) {
        List<String> children = new ArrayList<>();
        for (int a = 0; a < numberOfActivities; a++) {
            if (activityTask[a].equals(taskName)) {
                children.add(activityNames[a]);
            }
        }
        return children.toArray(new String[0]);
    }
    public String getTaskParentFromActivityChild(String activityName) {
        for (int a = 0; a < numberOfActivities; a++) {
            if (activityNames[a].equals(activityName)) {
                return activityTask[a];
            }
        }
        return null;
    }

    /**
     * Resizes the data structures according to specified parameters. Data is preserved as far as possible
     * @return true if data was changed, false otherwise
     */
    public boolean resizeProcessors(int numberOfProcessors, boolean forceResize) {
        if (numberOfProcessors <= 0) {
            throw new IllegalArgumentException("processors must be > 0");
        }
        if (forceResize || this.numberOfProcessors != numberOfProcessors) {
            // Other cases already handled in setXXX methods
            //discardResults();

            this.numberOfProcessors = numberOfProcessors;

            processorNames = ArrayUtils.resize(processorNames, numberOfProcessors, null);
            processorScheduling = ArrayUtils.resize(processorScheduling, numberOfProcessors, PROCESSOR_INF);
            processorMultiplicity = ArrayUtils.resize(processorMultiplicity, numberOfProcessors, 0);
            processorReplicas = ArrayUtils.resize(processorReplicas, numberOfProcessors, 1);
            processorSpeedFactor = ArrayUtils.resize(processorSpeedFactor, numberOfProcessors, 0.0);
            processorQuantum = ArrayUtils.resize(processorQuantum, numberOfProcessors, 0.0);

            return true;
        }

        return false;
    }
    public boolean resizeTasks(int numberOfTasks, boolean forceResize) {
        if (numberOfTasks <= 0) {
            throw new IllegalArgumentException("tasks must be > 0");
        }
        if (forceResize || this.numberOfTasks != numberOfTasks) {
            // Other cases already handled in setXXX methods
            //discardResults();

            this.numberOfTasks = numberOfTasks;

            taskNames = ArrayUtils.resize(taskNames, numberOfTasks, null);
            taskScheduling = ArrayUtils.resize(taskScheduling, numberOfTasks, TASK_REF);
            taskProcessor = ArrayUtils.resize(taskProcessor, numberOfTasks, null);
            taskPriority = ArrayUtils.resize(taskPriority, numberOfTasks, 0);
            taskThinkTimeMean = ArrayUtils.resize(taskThinkTimeMean, numberOfTasks, 0.0);
            taskMultiplicity = ArrayUtils.resize(taskMultiplicity, numberOfTasks, 0);
            taskReplicas = ArrayUtils.resize(taskReplicas, numberOfTasks, 1);

            return true;
        }

        return false;
    }
    public boolean resizeEntries(int numberOfEntries, boolean forceResize) {
        if (numberOfEntries <= 0) {
            throw new IllegalArgumentException("entries must be > 0");
        }
        if (forceResize || this.numberOfEntries != numberOfEntries) {
            // Other cases already handled in setXXX methods
            //discardResults();

            this.numberOfEntries = numberOfEntries;
            entryTask = ArrayUtils.resize(entryTask, numberOfEntries, null);
            entryBoundToActivity = ArrayUtils.resize(entryBoundToActivity, numberOfEntries, null);
            entryReplyToActivity = ArrayUtils.resize(entryReplyToActivity, numberOfEntries, null);
            entryNames = ArrayUtils.resize(entryNames, numberOfEntries, null);
            entryArrivalRate = ArrayUtils.resize(entryArrivalRate, numberOfEntries, 0.0);
            entryPriority = ArrayUtils.resize(entryPriority, numberOfEntries, 0);

            return true;
        }
        return false;
    }

    public boolean resizeActivities(int numberOfActivities, boolean forceResize) {
        if (numberOfActivities <= 0) {
            throw new IllegalArgumentException("Activities must be > 0");
        }
        if (forceResize || this.numberOfActivities != numberOfActivities) {
            this.numberOfActivities = numberOfActivities;
            activityNames = ArrayUtils.resize(processorNames, numberOfActivities, null);
            activityTask = ArrayUtils.resize(activityTask, numberOfActivities, null);
            activityHostDemand = ArrayUtils.resize(activityHostDemand, numberOfActivities, 0.0);
            return true;
        }

        return false;
    }
    public boolean resizeCalls(int numberOfCalls, boolean forceResize) {
        if (numberOfCalls < 0) {
            throw new IllegalArgumentException("calls must be >= 0");
        }
        if (forceResize || this.numberOfCalls != numberOfCalls) {
            // Other cases already handled in setXXX methods
            //discardResults();

            this.numberOfCalls = numberOfCalls;
            callActivity = ArrayUtils.resize(callActivity, numberOfCalls, null);
            callEntry = ArrayUtils.resize(callEntry, numberOfCalls, null);
            callType = ArrayUtils.resize(callType, numberOfCalls, 0);
            callNames = ArrayUtils.resize(callNames, numberOfCalls, null);
            callMeanRepeatTimes = ArrayUtils.resize(callMeanRepeatTimes, numberOfCalls, 1.0);
            return true;
        }

        return false;
    }

    public boolean resizePrecedences(int numberOfPrecedences, boolean forceResize) {
        if (numberOfPrecedences < 0) {
            throw new IllegalArgumentException("precedences must be >= 0");
        }
        if (forceResize || this.numberOfPrecedences != numberOfPrecedences) {
            // Other cases already handled in setXXX methods
            //discardResults();

            this.numberOfPrecedences = numberOfPrecedences;
            precedenceType = ArrayUtils.resize(precedenceType, numberOfPrecedences, 0);
            precedencePreActivities = ArrayUtils.resize(precedencePreActivities, numberOfPrecedences, activityNames[0]);
            precedencePostActivities = ArrayUtils.resize(precedencePostActivities, numberOfPrecedences, activityNames[0]);
            precedencePreParams = ArrayUtils.resize(precedencePreParams, numberOfPrecedences, 1.0);
            precedencePostParams = ArrayUtils.resize(precedencePostParams, numberOfPrecedences, 1.0);
            return true;
        }
        return false;
    }

    public void deleteProcessor(int i) {
        if (numberOfProcessors < 2) {
            throw new RuntimeException("System must have at least one processor");
        }
        numberOfProcessors--;
        processorNames = ArrayUtils.delete(processorNames, i);
        processorScheduling = ArrayUtils.delete(processorScheduling, i);
        processorQuantum = ArrayUtils.delete(processorQuantum, i);
        processorReplicas = ArrayUtils.delete(processorReplicas, i);
        processorMultiplicity = ArrayUtils.delete(processorMultiplicity, i);
        processorSpeedFactor = ArrayUtils.delete(processorSpeedFactor, i);

        resizeProcessors(numberOfProcessors, false);

        //it was considering the results valid when a class is cancelled
        //hasResults = false;
        //END

    }
    public void deleteTask(int i) {
        if (numberOfTasks < 2) {
            throw new RuntimeException("System must have at least one task");
        }
        numberOfTasks--;
        taskNames = ArrayUtils.delete(taskNames, i);
        taskScheduling = ArrayUtils.delete(taskScheduling, i);
        taskProcessor = ArrayUtils.delete(taskProcessor, i);
        taskPriority = ArrayUtils.delete(taskPriority, i);
        taskThinkTimeMean = ArrayUtils.delete(taskThinkTimeMean, i);
        taskReplicas = ArrayUtils.delete(taskReplicas, i);
        taskMultiplicity = ArrayUtils.delete(taskMultiplicity, i);

        resizeTasks(numberOfTasks, false);

        //it was considering the results valid when a class is cancelled
        //hasResults = false;
        //END

    }
    public void deleteEntry(int i) {
        if (numberOfEntries < 2) {
            throw new RuntimeException("System must have at least one entry.");
        }
        numberOfEntries--;
        entryNames = ArrayUtils.delete(entryNames, i);
        entryTask = ArrayUtils.delete(entryTask, i);
        entryBoundToActivity = ArrayUtils.delete(entryBoundToActivity, i);
        entryReplyToActivity = ArrayUtils.delete(entryReplyToActivity, i);
        entryPriority = ArrayUtils.delete(entryPriority, i);
        entryArrivalRate = ArrayUtils.delete(entryArrivalRate, i);

        resizeEntries(numberOfEntries, false);

        //it was considering the results valid when a class is cancelled
        //hasResults = false;
        //END

    }

    public void deleteActivity(int i) {
        if (numberOfActivities < 2) {
            throw new RuntimeException("System must have at least one activity.");
        }
        numberOfActivities--;
        activityNames = ArrayUtils.delete(activityNames, i);
        activityTask = ArrayUtils.delete(activityTask, i);
        activityHostDemand = ArrayUtils.delete(activityHostDemand, i);
        resizeActivities(numberOfActivities, false);
        //it was considering the results valid when a class is cancelled
        //hasResults = false;
        //END

    }

    public void deleteCall(int i) {
        if (numberOfCalls < 1) {
            throw new RuntimeException("There are no calls to be deleted");
        }
        numberOfCalls--;
        callNames = ArrayUtils.delete(callNames, i);
        callActivity = ArrayUtils.delete(callActivity, i);
        callEntry = ArrayUtils.delete(callEntry, i);
        callType = ArrayUtils.delete(callType, i);
        callMeanRepeatTimes = ArrayUtils.delete(callMeanRepeatTimes, i);
        resizeCalls(numberOfCalls, false);

        //it was considering the results valid when a class is cancelled
        //hasResults = false;
        //END

    }

    public void deletePrecedence(int i) {
        if (numberOfPrecedences < 1) {
            throw new RuntimeException("Cannot delete precedences when there are no precedences.");
        }
        numberOfPrecedences--;
        precedenceType = ArrayUtils.delete(precedenceType, i);
        precedencePreActivities = ArrayUtils.delete(precedencePreActivities, i);
        precedencePostActivities = ArrayUtils.delete(precedencePostActivities, i);
        precedencePreParams = ArrayUtils.delete(precedencePreParams, i);
        precedencePostParams = ArrayUtils.delete(precedencePostParams, i);
        resizePrecedences(numberOfPrecedences, false);
    }

    /**
     * Change all occurrence of 'source' to 'destination' in the array taskProcessor.
     */
    public void changeAllTaskProcessor(String source, String destination) {
        for (int i = 0; i < taskProcessor.length; i++) {
            if (taskProcessor[i] != null && taskProcessor[i].equalsIgnoreCase(source)) {
                 taskProcessor[i] = destination;
            }
        }
    }

    /**
     * Change all occurrence of 'source' to 'destination' in the array entryTask.
     */
    public void changeAllEntryTask(String source, String destination) {
        for (int i = 0; i < entryTask.length; i++) {
            if (entryTask[i] != null && entryTask[i].equalsIgnoreCase(source)) {
                entryTask[i] = destination;
            }
        }
    }

    /**
     * Change all occurrence of 'source' to 'destination' in the array entryBoundToActivity.
     */
    public void changeAllEntryBoundToActivity(String source, String destination) {
        for (int i = 0; i < entryBoundToActivity.length; i++) {
            if (entryBoundToActivity[i] != null && entryBoundToActivity[i].equalsIgnoreCase(source)) {
                entryBoundToActivity[i] = destination;
            }
        }
    }

    /**
     * Change all occurrence of 'source' to 'destination' in the array entryReplyToActivity.
     */
    public void changeAllEntryReplyToActivity(String source, String destination) {
        for (int i = 0; i < entryReplyToActivity.length; i++) {
            if (entryReplyToActivity[i] != null && entryReplyToActivity[i].equalsIgnoreCase(source)) {
                entryReplyToActivity[i] = destination;
            }
        }
    }

    /**
     * Change all occurrence of 'source' to 'destination' in the array activityTask.
     */
    public void changeAllActivityTask(String source, String destination) {
        for (int i = 0; i < activityTask.length; i++) {
            if (activityTask[i] != null && activityTask[i].equalsIgnoreCase(source)) {
                activityTask[i] = destination;
            }
        }
    }

    /**
     * Change all occurrence of 'source' to 'destination' in the array callActivity.
     */
    public void changeAllCallActivity(String source, String destination) {
        for (int i = 0; i < callActivity.length; i++) {
            if (callActivity[i] != null && callActivity[i].equalsIgnoreCase(source)) {
                callActivity[i] = destination;
            }
        }
    }

    /**
     * Change all occurrence of 'source' to 'destination' in the 2D array precedencePreActivities and precedencePostactivities.
     */
    public void changeAllPrecedenceActivities(String source, String destination) {
        for (int i = 0; i < precedencePreActivities.length; i++) {
            for (int pre = 0; pre < precedencePreActivities[i].length; pre++) {
                if (precedencePreActivities[i][pre] != null && precedencePreActivities[i][pre].equalsIgnoreCase(source)) {
                    precedencePreActivities[i][pre] = destination;
                }
            }
            for (int post = 0; post < precedencePostActivities[i].length; post++) {
                if (precedencePostActivities[i][post] != null && precedencePostActivities[i][post].equalsIgnoreCase(source)) {
                    precedencePostActivities[i][post] = destination;
                }
            }
        }
    }

    /**
     * Change all occurrence of 'source' to 'destination' in the array entryBoundToActivity.
     */
    public void changeAllCallEntry(String source, String destination) {
        for (int i = 0; i < callEntry.length; i++) {
            if (callEntry[i] != null && callEntry[i].equalsIgnoreCase(source)) {
                callEntry[i] = destination;
            }
        }
    }

    /**
     * Initialize the object with defaults:
     *
     */
    public void setDefaults() {
        resultsOK = false;
        viewerType = ViewerType.WIZ;
        solverType = SolverType.LN;
        changed = true;

        numberOfProcessors = 1;
        processorQuantum = new double[1];
        processorQuantum[0] = 0.0;

        processorNames = new String[1];
        processorNames[0] = "Processor1";

        processorScheduling = new int[1];
        processorScheduling[0] = PROCESSOR_INF;

        processorMultiplicity = new int[1];
        processorMultiplicity[0] = 1;

        processorReplicas = new int[1];
        processorReplicas[0]  = 1;

        processorSpeedFactor = new double[1];
        processorSpeedFactor[0] = 1.0;

        numberOfTasks = 1;
        taskNames = new String[1];
        taskNames[0] = "Task1";

        taskProcessor = new String[1];
        taskProcessor[0] = processorNames[0];

        taskScheduling = new int[1];
        taskScheduling[0] = TASK_REF;

        taskMultiplicity = new int[1];
        taskMultiplicity[0] = 1;

        taskReplicas = new int[1];
        taskReplicas[0]  = 1;

        taskPriority = new int[1];
        taskPriority[0]  = 0;

        taskThinkTimeMean = new double[1];
        taskThinkTimeMean[0] = 0.0;

        numberOfEntries = 1;
        entryPriority = new int[1];
        entryPriority[0] = 0;

        entryArrivalRate = new double[1];
        entryArrivalRate[0] = 0.0;

        entryNames = new String[1];
        entryNames[0] = "Entry1";

        entryTask =  new String[1];
        entryTask[0] = taskNames[0];

        numberOfActivities = 1;
        activityNames = new String[1];
        activityNames[0] = "Activity1";

        activityTask = new String[1];
        activityTask[0] = taskNames[0];

        activityHostDemand = new double[1];
        activityHostDemand[0] = 0.0;

        // These entry defaults can only be set after initialising default activities
        entryBoundToActivity =  new String[1];
        entryBoundToActivity[0] = activityNames[0];

        entryReplyToActivity =  new String[1];
        entryReplyToActivity[0] = activityNames[0];

        numberOfCalls = 0;
        callMeanRepeatTimes = new double[0];
        callNames = new String[0];
        callActivity =  new String[0];
        callEntry =  new String[0];
        callType =  new int[0];

        numberOfPrecedences = 0;
        precedenceType = new int[0];
        precedencePreActivities = new String[0][0];
        precedencePostActivities = new String[0][0];
        precedencePreParams = new Double[0][0];
        precedencePostParams = new Double[0][0];

        rowCounts =  numberOfProcessors + numberOfTasks + numberOfEntries + numberOfActivities;

    }
    /**
     * @return true if the model has been changed
     */
    public boolean isChanged() {
        return changed;
    }

    /**
     * resets the changed flag.
     * <br>
     * WARNING: this enables change checking on parameter setting, which can be quite time-consuming.
     */
    public void resetChanged() {
        changed = false;
    }

    /**
     * flags the model as changed.
     * There is no need to call this, except to disable time-consuming change checking if you're not interested in it
     */
    public void setChanged() {
        changed = true;
    }

    private boolean containsDuplicate(String[] arrayList) {
        boolean duplicateExists = false;
        Set<String> tempSet = new HashSet<>();
        for (String name: arrayList) {
            if (!tempSet.add(name)) {
                duplicateExists = true;
            }
        }
        return duplicateExists;
    }
    public boolean inputValid() {
        /** Name Checks **/
        // Check for duplicate names
        if (containsDuplicate(getActivityNames())) {
            errors.append(String.format("No duplicate Activity Names are allowed<br>"));
        }
        if (containsDuplicate(getTaskNames())) {
            errors.append(String.format("No duplicate Task Names are allowed<br>"));
        }
        if (containsDuplicate(getProcessorNames())) {
            errors.append(String.format("No duplicate Processor Names are allowed<br>"));
        }
        if (containsDuplicate(getEntryNames())) {
            errors.append(String.format("No duplicate Entry Names are allowed<br>"));
        }
        // Check for duplicate Activity - Entry binding
        if (containsDuplicate(getEntryBoundToActivity())) {
            errors.append(String.format("Activities must be bound to unique Entries<br>"));
        }
        /** Simple value checks
         *  This section of code also sets up the hashmaps that will be used later for the creation of the
         *  LayeredNetwork if there are no errors
         * **/
        // Processor Check - HostDemand cannot be 0
        for (int np = 0; np < numberOfProcessors; np++) {
            if (processorSpeedFactor[np] == 0) {
                errors.append(String.format("Processor \"%s\" cannot have Host Demand 0<br>", processorNames[np]));
            }
        }
        // Check if all tasks provided are valid
        for (int nt = 0; nt < numberOfTasks; nt++) {
            if (taskProcessor[nt] == null) {
                errors.append(String.format("Task \"%s\" cannot have no processor<br>", taskNames[nt]));
            }
        }
        // Check if all entries provided are valid
        for (int ne = 0; ne < numberOfEntries; ne++) {
            if (entryTask[ne] == null) {
                errors.append(String.format("Entry \"%s\" needs a task<br>", entryNames[ne]));
            } else {
                entryTaskMap.put(entryNames[ne], entryTask[ne]);
            }
            if (entryBoundToActivity[ne] == null) {
                errors.append(String.format("Entry \"%s\" needs a \"Bound To\" activity<br>", entryNames[ne]));
            }
            if (entryReplyToActivity[ne] == null) {
                errors.append(String.format("Entry \"%s\" needs a \"Reply To\" activity<br>", entryNames[ne]));
            }
        }
        // Ensure every Task has an Entry
        for (String taskName: taskNames) {
            if (!entryTaskMap.containsValue(taskName)) {
                errors.append(String.format("Task \"%s\" does not have a child Entry", taskName));
            }
        }
        // Check if all activities provided are valid
        for (int na = 0; na < numberOfActivities; na++) {
            if (activityTask[na] == null) {
                errors.append(String.format("Activity \"%s\" needs a task parent<br>", activityNames[na]));
            } else {
                activityParentTaskMap.put(activityNames[na], activityTask[na]);
            }
        }
        // Check if all calls provided are valid
        for (int nc = 0; nc < numberOfCalls; nc++) {
            if (callActivity[nc] == null) {
                errors.append(String.format("Call \"%s\" needs an activity<br>", callNames[nc]));
            }
            if (callEntry[nc] == null) {
                errors.append(String.format("Call \"%s\" needs an entry<br>", callNames[nc]));
            }
        }
        /** Complex value checks **/
        // Precedence Checks
        for (int p = 0; p < numberOfPrecedences; p++) {
            boolean unknownActivityFound = false;
            boolean incorrectParentEntry = false;
            String[] preActs = precedencePreActivities[p];
            String[] postActs = precedencePostActivities[p];
            Double[] preParams = precedencePreParams[p];
            Double[] postParams = precedencePostParams[p];
            for (String preAct: preActs) {
                if (!activityParentTaskMap.containsKey(preAct)) {
                    errors.append(String.format("Precedence \"%d\": Unknown activity \"%s\" in Pre Activities<br>",
                            p + 1, preAct));
                    unknownActivityFound = true;
                }
            }
            for (String postAct: postActs) {
                if (!activityParentTaskMap.containsKey(postAct)) {
                    errors.append(String.format("Precedence \"%d\": Unknown activity \"%s\" in Post Activities<br>",
                            p + 1, postAct));
                    unknownActivityFound = true;
                }
            }
            if (unknownActivityFound) {
                continue;
            }
            // Check if activities of a precedence belong under the same task
            String taskParent = preActs.length > 0 ? activityParentTaskMap.get(preActs[0]) : activityParentTaskMap.get(postActs[0]);
            for (String actName: preActs) {
                if (!activityParentTaskMap.get(actName).equals(taskParent)) {
                    errors.append(String.format("Precedence \"%d\": Pre Activities in this precedence do not share the same parent task<br>",
                            p + 1));
                    incorrectParentEntry = true;
                    break;
                }
            }
            for (String actName: postActs) {
                if (!activityParentTaskMap.get(actName).equals(taskParent)) {
                    errors.append(String.format("Precedence \"%d\": Post Activities in this precedence do not share the same parent task<br>",
                            p + 1));
                    incorrectParentEntry = true;
                    break;
                }
            }
            if (incorrectParentEntry) {
                continue;
            }
            switch(precedenceType[p]) {
                case PRECEDENCE_SEQUENCE:
                    // "SEQUENCE" - pre activity: 1, post activity: >= 1, pre params: 0, post params: 0
                    if (preActs.length > 1) {
                        errors.append(String.format("Precedence \"%d\": SEQUENCE should have 1 pre activity<br>",p + 1));
                    }
                    if (postActs.length < 1) {
                        errors.append(String.format("Precedence \"%d\": SEQUENCE should have at least 1 post activity<br>",p + 1));
                    }
                    // No checks for post params and pre params because they are not enabled on sequence
                    break;
                case PRECEDENCE_OR_FORK:
                    // "OR FORK" - pre activity: 1, post activity: >= 2, pre params: same number as pre activities, post params: 0
                    if (preActs.length != 1) {
                        errors.append(String.format("Precedence \"%d\": OR FORK should have 1 pre activity<br>",p + 1));
                    }
                    if (postActs.length <= 1) {
                        errors.append(String.format("Precedence \"%d\": OR FORK should have more than 1 post activity<br>",p + 1));
                    }
                    for (Double param: postParams) {
                        if (param == null) {
                            errors.append(String.format("Precedence \"%d\": OR FORK post parameter - SELECTION PROBABILITY - cannot be empty<br>",p + 1));
                            break;
                        }
                    }
                    break;
                case PRECEDENCE_OR_JOIN:
                    // "OR JOIN" - pre activity: >= 2, post activity >= 1, pre params: 0, post params: 0
                    if (preActs.length <= 1) {
                        errors.append(String.format("Precedence \"%d\": OR JOIN should have more than 1 pre activity<br>",p + 1));
                    }
                    if (postActs.length != 1) {
                        errors.append(String.format("Precedence \"%d\": OR JOIN should have 1 post activity<br>",p + 1));
                    }
                    break;
                case PRECEDENCE_AND_FORK:
                    // "AND FORK" - pre activity: 1, post activity >= 2, pre params: 0, post params: same as post acitivty
                    if (preActs.length != 1) {
                        errors.append(String.format("Precedence \"%d\": AND FORK should have 1 pre activity<br>",p + 1));
                    }
                    if (postActs.length <= 1) {
                        errors.append(String.format("Precedence \"%d\": AND FORK should have more than 1 post activity<br>",p + 1));
                    }
                    for (Double param: postParams) {
                        if (param == null) {
                            errors.append(String.format("Precedence \"%d\": AND FORK post parameter - FAN OUT - cannot be empty<br>",p + 1));
                            break;
                        }
                    }
                    break;
                case PRECEDENCE_AND_JOIN:
                    // "AND JOIN" - pre activity: >= 2, post activity: 1, pre params: same number as pre activity, post params: 0
                    if (preActs.length <= 1) {
                        errors.append(String.format("Precedence \"%d\": AND JOIN should have more than 1 pre activity<br>",p + 1));
                    }
                    if (postActs.length != 1) {
                        errors.append(String.format("Precedence \"%d\": AND JOIN should have 1 post activity<br>",p + 1));
                    }
                    for (Double param: preParams) {
                        if (param == null) {
                            errors.append(String.format("Precedence \"%d\": AND JOIN pre parameter - FAN IN - cannot be empty<br>",p + 1));
                            break;
                        }
                    }
                    break;
                case PRECEDENCE_LOOP:
                    // "LOOP" - pre activity: 1, post activity: >= 1, pre params: 0, post params: 1 (number of counts)
                    if (preActs.length != 1) {
                        errors.append(String.format("Precedence \"%d\": LOOP must have 1 pre activity<br>",p + 1));
                    }
                    if (postActs.length != 2) {
                        errors.append(String.format("Precedence \"%d\": LOOP must have 2 post activities<br>",p + 1));
                    }
                    if (postParams[0] == null) {
                        errors.append(String.format("Precedence \"%d\": LOOP post parameter - COUNTS - cannot be empty<br>",p + 1));
                    }
                    break;
            }
        }
        return errors.length() == 0;
    }

    /**
     * Retrieve errors generated during validity checks
     * @return
     */
    public String getErrors() {
        return errors.toString();
    }

    /**
     * @return true if results are valid
     */
    public boolean areResultsOK() {
        return resultsOK;
    }

    /**
     * Creates a DOM representation of this object
     * @return a DOM representation of this object
     */
    public Document createDocument() {
        return JLQNDocumentCreator.createDocument(this);
    }

    /**
     * load the state of this object from the Document.
     * @return true if the operation was successful.
     * WARNING: If the operation fails the object is left in an incorrect state and should be discarded.
     */
    public boolean loadDocument(Document doc) {
        Node processorNode = doc.getElementsByTagName("processors").item(0);
        Node taskNode = doc.getElementsByTagName("tasks").item(0);
        Node entryNode = doc.getElementsByTagName("entries").item(0);
        Node activityNode = doc.getElementsByTagName("activities").item(0);
        Node callNode = doc.getElementsByTagName("calls").item(0);
        Node precedenceNode = doc.getElementsByTagName("precedences").item(0);
        Node solNode = doc.getElementsByTagName("solutions").item(0);

        // load processors
        if (processorNode != null) {
            if (!loadProcessors(processorNode)) {
                //classes loading failed!
                return false;
            }
        }

        // load tasks
        if (taskNode != null) {
            if (!loadTasks(taskNode)) {
                //classes loading failed!
                return false;
            }
        }

        // load entries
        if (entryNode != null) {
            if (!loadEntries(entryNode)) {
                //classes loading failed!
                return false;
            }
        }

        // load activities
        if (activityNode != null) {
            if (!loadActivities(activityNode)) {
                //classes loading failed!
                return false;
            }
        }

        // load calls
        if (callNode != null) {
            if (!loadCalls(callNode)) {
                //classes loading failed!
                return false;
            }
        }

        // load precedences
        if (precedenceNode != null) {
            if (!loadPrecedences(precedenceNode)) {
                //classes loading failed!
                return false;
            }
        }

        //load solution
        if (solNode != null) {
            if (!loadSolution(solNode)) {
                return false;
            }
        } else {
            this.resetResults();
        }

        changed = false;
        return true;
    }


    public boolean loadProcessors(Node processorNode) {
        numberOfProcessors = Integer.parseInt(((Element) processorNode).getAttribute("number"));
        processorNames = new String[numberOfProcessors];
        processorScheduling = new int[numberOfProcessors];
        processorQuantum = new double[numberOfProcessors];
        processorMultiplicity = new int[numberOfProcessors];
        processorReplicas = new int[numberOfProcessors];
        processorSpeedFactor = new double[numberOfProcessors];

        NodeList processorList = processorNode.getChildNodes();

        int processorNum = 0;
        Node n;
        Element current;

        for (int i = 0; i < processorList.getLength(); i++) {
            n = processorList.item(i);
            if (!(n instanceof Element)) {
                throw new RuntimeException("Fail to read processors");
            }
            current = (Element) n;
            processorNames[processorNum] = current.getAttribute(JLQNDocumentConstants.DOC_PROCESSOR_NAME);

            String schedulingName = current.getAttribute(JLQNDocumentConstants.DOC_PROCESSOR_SCHEDULING);
            int j = -1;
            for (int k = 0; k < PROCESSOR_SCHEDULING_TYPENAMES.length; k++) {
                if (PROCESSOR_SCHEDULING_TYPENAMES[k].equals(schedulingName)) {
                    j = k;
                }
            }
            if (j < 0) {
                throw new RuntimeException("Processor scheduling typename not exists.");
            }

            processorScheduling[processorNum] = j;
            processorQuantum[processorNum] = Double.parseDouble(current.getAttribute(JLQNDocumentConstants.DOC_PROCESSOR_QUANTUM));
            processorMultiplicity[processorNum] = Integer.parseInt(current.getAttribute(JLQNDocumentConstants.DOC_PROCESSOR_MULTIPLICITY));
            if (processorMultiplicity[processorNum] == -1) {
                processorMultiplicity[processorNum] = Integer.MAX_VALUE;
            }
            processorReplicas[processorNum] = Integer.parseInt(current.getAttribute(JLQNDocumentConstants.DOC_PROCESSOR_REPLICAS));
            processorSpeedFactor[processorNum] = Double.parseDouble(current.getAttribute(JLQNDocumentConstants.DOC_PROCESSOR_SPEED_FACTOR));
            processorNum++;
        }
        return true;
    }

    public boolean loadTasks(Node taskNode) {
        numberOfTasks = Integer.parseInt(((Element) taskNode).getAttribute("number"));
        taskNames = new String[numberOfTasks];
        taskScheduling = new int[numberOfTasks];
        taskProcessor = new String[numberOfTasks];
        taskPriority = new int[numberOfTasks];
        taskThinkTimeMean = new double[numberOfTasks];
        taskMultiplicity = new int[numberOfTasks];
        taskReplicas = new int[numberOfTasks];

        NodeList taskList = taskNode.getChildNodes();

        int taskNum = 0;
        Node n;
        Element current;

        for (int i = 0; i < taskList.getLength(); i++) {
            n = taskList.item(i);
            if (!(n instanceof Element)) {
                throw new RuntimeException("Fail to read tasks");
            }
            current = (Element) n;
            taskNames[taskNum] = current.getAttribute(JLQNDocumentConstants.DOC_TASK_NAME);
            String schedulingName = current.getAttribute(JLQNDocumentConstants.DOC_TASK_SCHEDULING);
            int j = -1;
            for (int k = 0; k < TASK_SCHEDULING_NAMES.length; k++) {
                if (TASK_SCHEDULING_NAMES[k].equals(schedulingName)) {
                    j = k;
                }
            }
            if (j < 0) {
                throw new RuntimeException("Task scheduling typename not exists.");
            }

            taskScheduling[taskNum] = j;
            String taskProcessorName = current.getAttribute(JLQNDocumentConstants.DOC_TASK_PROCESSOR);
            if (taskProcessorName.equals("")) {
                taskProcessor[taskNum] = null;
            } else {
                taskProcessor[taskNum] = taskProcessorName;
            }
            taskPriority[taskNum] = Integer.parseInt(current.getAttribute(JLQNDocumentConstants.DOC_TASK_PRIORITY));
            taskThinkTimeMean[taskNum] = Double.parseDouble(current.getAttribute(JLQNDocumentConstants.DOC_TASK_THINK_TIME_MEAN));
            taskMultiplicity[taskNum] = Integer.parseInt(current.getAttribute(JLQNDocumentConstants.DOC_TASK_MULTIPLICITY));
            taskReplicas[taskNum] = Integer.parseInt(current.getAttribute(JLQNDocumentConstants.DOC_TASK_REPLICAS));
            taskNum++;
        }
        return true;
    }

    public boolean loadEntries(Node entryNode) {
        numberOfEntries = Integer.parseInt(((Element) entryNode).getAttribute("number"));
        entryNames = new String[numberOfEntries];
        entryTask = new String[numberOfEntries];
        entryBoundToActivity = new String[numberOfEntries];
        entryReplyToActivity = new String[numberOfEntries];
        entryArrivalRate = new double[numberOfEntries];
        entryPriority = new int[numberOfEntries];

        NodeList entryList = entryNode.getChildNodes();

        int entryNum = 0;
        Node n;
        Element current;

        for (int i = 0; i < entryList.getLength(); i++) {
            n = entryList.item(i);
            if (!(n instanceof Element)) {
                throw new RuntimeException("Fail to read entries");
            }
            current = (Element) n;
            entryNames[entryNum] = current.getAttribute(JLQNDocumentConstants.DOC_ENTRY_NAME);

            String entryTaskName = current.getAttribute(JLQNDocumentConstants.DOC_ENTRY_TASK);
            if (entryTaskName.equals("")) {
                entryTask[entryNum] = null;
            } else {
                entryTask[entryNum] = entryTaskName;
            }

            String entryBoundToActivityName = current.getAttribute(JLQNDocumentConstants.DOC_ENTRY_BOUND_TO_ACTIVITY);
            if (entryBoundToActivityName.equals("")) {
                entryBoundToActivity[entryNum] = null;
            } else {
                entryBoundToActivity[entryNum] = entryBoundToActivityName;
            }

            String entryReplyToActivityName = current.getAttribute(JLQNDocumentConstants.DOC_ENTRY_REPLY_TO_ACTIVITY);
            if (entryReplyToActivityName.equals("")) {
                entryReplyToActivity[entryNum] = null;
            } else {
                entryReplyToActivity[entryNum] = entryReplyToActivityName;
            }

            entryArrivalRate[entryNum] = Double.parseDouble(current.getAttribute(JLQNDocumentConstants.DOC_ENTRY_ARRIVAL_RATE));
            entryPriority[entryNum] = Integer.parseInt(current.getAttribute(JLQNDocumentConstants.DOC_ENTRY_PRIORITY));
            entryNum++;
        }
        return true;
    }

    public boolean loadActivities(Node activityNode) {
        numberOfActivities = Integer.parseInt(((Element) activityNode).getAttribute("number"));
        activityNames = new String[numberOfActivities];
        activityTask = new String[numberOfActivities];
        activityHostDemand = new double[numberOfActivities];

        NodeList activityList = activityNode.getChildNodes();

        int activityNum = 0;
        Node n;
        Element current;

        for (int i = 0; i < activityList.getLength(); i++) {
            n = activityList.item(i);
            if (!(n instanceof Element)) {
                throw new RuntimeException("Fail to read activities");
            }
            current = (Element) n;
            activityNames[activityNum] = current.getAttribute(JLQNDocumentConstants.DOC_ACTIVITY_NAME);

            String activityTaskName = current.getAttribute(JLQNDocumentConstants.DOC_ACTIVITY_TASK);
            if (activityTaskName.equals("")) {
                activityTask[activityNum] = null;
            } else {
                activityTask[activityNum] = activityTaskName;
            }

            activityHostDemand[activityNum] = Double.parseDouble(current.getAttribute(JLQNDocumentConstants.DOC_ACTIVITY_DEMAND_MEAN));
            activityNum++;
        }
        return true;
    }

    public boolean loadCalls(Node callNode) {
        numberOfCalls = Integer.parseInt(((Element) callNode).getAttribute("number"));
        callNames = new String[numberOfCalls];
        callActivity = new String[numberOfCalls];
        callEntry = new String[numberOfCalls];
        callType = new int[numberOfCalls];
        callMeanRepeatTimes = new double[numberOfCalls];

        NodeList callList = callNode.getChildNodes();

        int callNum = 0;
        Node n;
        Element current;

        for (int i = 0; i < callList.getLength(); i++) {
            n = callList.item(i);
            if (!(n instanceof Element)) {
                throw new RuntimeException("Fail to read calls");
            }
            current = (Element) n;
            callNames[callNum] = current.getAttribute(JLQNDocumentConstants.DOC_CALL_NAME);

            String callActivityName = current.getAttribute(JLQNDocumentConstants.DOC_CALL_ACTIVITY);
            if (callActivityName.equals("")) {
                callActivity[callNum] = null;
            } else {
                callActivity[callNum] = callActivityName;
            }

            String callEntryName = current.getAttribute(JLQNDocumentConstants.DOC_CALL_ENTRY);
            if (callEntryName.equals("")) {
                callEntry[callNum] = null;
            } else {
                callEntry[callNum] = callEntryName;
            }

            String callTypeName = current.getAttribute(JLQNDocumentConstants.DOC_CALL_TYPE);
            int j = -1;
            for (int k = 0; k < CALL_TYPES.length; k++) {
                if (CALL_TYPES[k].equals(callTypeName)) {
                    j = k;
                }
            }
            if (j < 0) {
                throw new RuntimeException("Call type not exists.");
            }
            callType[callNum] = j;

            callMeanRepeatTimes[callNum] = Double.parseDouble(current.getAttribute(JLQNDocumentConstants.DOC_CALL_MEAN_REPEAT));
            callNum++;
        }
        return true;
    }

    public boolean loadPrecedences(Node precedenceNode) {
        numberOfPrecedences = Integer.parseInt(((Element) precedenceNode).getAttribute("number"));
        precedenceType = new int[numberOfPrecedences];
        List<String[]> loadedPrecedencePreActivities = new ArrayList<>();
        List<String[]> loadedPrecedencePostActivities = new ArrayList<>();
        List<Double[]> loadedPrecedencePreParams = new ArrayList<>();
        List<Double[]> loadedPrecedencePostParams = new ArrayList<>();

        NodeList precedenceList = precedenceNode.getChildNodes();

        Node n;
        Element current;

        for (int i = 0; i < precedenceList.getLength(); i++) {
            n = precedenceList.item(i);
            if (!(n instanceof Element)) {
                throw new RuntimeException("Fail to read precedences");
            }
            current = (Element) n;
            precedenceType[i] = Integer.parseInt(current.getAttribute(JLQNDocumentConstants.DOC_PRECEDENCE_TYPE));
            NodeList precedenceChildren = n.getChildNodes();
            List<String> childPrecedencePreActivities = new ArrayList<>();
            List<Double> childPrecedencePreParams = new ArrayList<>();
            List<String> childPrecedencePostActivities = new ArrayList<>();
            List<Double> childPrecedencePostParams = new ArrayList<>();
            for (int childIdx = 0; childIdx < precedenceChildren.getLength(); childIdx++) {
                Node child = precedenceChildren.item(childIdx);
                if (!(child instanceof Element)) {
                    throw new RuntimeException(String.format("Fail to read precedence activity of precedence %d", i));
                }
                Element currChild = (Element) child;
                if (currChild.getAttribute(JLQNDocumentConstants.DOC_PRECEDENCE_ACTIVITY_TYPE).equals("pre")) {
                    childPrecedencePreActivities.add(currChild.getAttribute(JLQNDocumentConstants.DOC_PRECEDENCE_ACTIVITY));
                    childPrecedencePreParams.add(Double.parseDouble(currChild.getAttribute(JLQNDocumentConstants.DOC_PRECEDENCE_PARAMS)));
                } else if (currChild.getAttribute(JLQNDocumentConstants.DOC_PRECEDENCE_ACTIVITY_TYPE).equals("post")) {
                    childPrecedencePostActivities.add(currChild.getAttribute(JLQNDocumentConstants.DOC_PRECEDENCE_ACTIVITY));
                    childPrecedencePostParams.add(Double.parseDouble(currChild.getAttribute(JLQNDocumentConstants.DOC_PRECEDENCE_PARAMS)));
                } else if (currChild.getAttribute(JLQNDocumentConstants.DOC_PRECEDENCE_ACTIVITY_TYPE).equals("end")) {
                    childPrecedencePostActivities.add(currChild.getAttribute(JLQNDocumentConstants.DOC_PRECEDENCE_ACTIVITY));
                    childPrecedencePostParams.add(null);
                } else {
                    throw new RuntimeException(String.format("Cannot identify pre or post activity for precedence %d, child %d", i, childIdx));
                }

            }
            loadedPrecedencePreActivities.add(childPrecedencePreActivities.toArray(new String[0]));
            loadedPrecedencePreParams.add(childPrecedencePreParams.toArray(new Double[0]));
            loadedPrecedencePostActivities.add(childPrecedencePostActivities.toArray(new String[0]));
            loadedPrecedencePostParams.add(childPrecedencePostParams.toArray(new Double[0]));
        }
        precedencePreActivities = loadedPrecedencePreActivities.toArray(new String[0][0]);
        precedencePreParams = loadedPrecedencePreParams.toArray(new Double[0][0]);
        precedencePostActivities = loadedPrecedencePostActivities.toArray(new String[0][0]);
        precedencePostParams = loadedPrecedencePostParams.toArray(new Double[0][0]);
        return true;
    }
    public boolean loadSolution(Node sol) {
        resultsOK = true;
        if (queueLen == null) {
            resetResults();
        }
        rowCounts = Integer.parseInt(((Element) sol).getAttribute(JLQNDocumentConstants.DOC_NODE_COUNT));
        queueLen = new Double[rowCounts][];
        throughput = new Double[rowCounts][];
        residTimes = new Double[rowCounts][];
        respTimes = new Double[rowCounts][];
        util = new Double[rowCounts][];

        NodeList solutionList = sol.getChildNodes();
        int resultNum = 0;
        Node n;
        Element current;

        for (int i = 0; i < solutionList.getLength(); i++) {
            n = solutionList.item(i);
            if (!(n instanceof Element)) {
                throw new RuntimeException("Fail to read solutions");
            }
            current = (Element) n;
            String tagName = current.getTagName();
            if (JLQNDocumentConstants.DOC_RESULT_PROCESSOR.equals(tagName)
                    || JLQNDocumentConstants.DOC_RESULT_TASK.equals(tagName)
                    || JLQNDocumentConstants.DOC_RESULT_ENTRY.equals(tagName)
                    || JLQNDocumentConstants.DOC_RESULT_ACTIVITY.equals(tagName)) {
                queueLen[i] = new Double[1];

                String queueLenItem = current.getAttribute(JLQNDocumentConstants.DOC_QUEUE_LEN);
                if (queueLenItem.equals("")) {
                    queueLen[i][0]  = null;
                } else {
                    queueLen[i][0] = Double.parseDouble(queueLenItem);
                }

                throughput[i] = new Double[1];
                String throughputItem = current.getAttribute(JLQNDocumentConstants.DOC_THROUGHPUT);
                if (throughputItem.equals("")) {
                    throughput[i][0]  = null;
                } else {
                    throughput[i][0] = Double.parseDouble(throughputItem);
                }

                residTimes[i] = new Double[1];
                String residTimesItem = current.getAttribute(JLQNDocumentConstants.DOC_RESID_TIME);
                if (residTimesItem.equals("")) {
                    residTimes[i][0]  = null;
                } else {
                    residTimes[i][0] = Double.parseDouble(residTimesItem);
                }

                respTimes[i] = new Double[1];
                String respTimesItem = current.getAttribute(JLQNDocumentConstants.DOC_RESP_TIME);
                if (respTimesItem.equals("")) {
                    respTimes[i][0]  = null;
                } else {
                    respTimes[i][0] = Double.parseDouble(respTimesItem);
                }

                util[i] = new Double[1];
                String utilItem = current.getAttribute(JLQNDocumentConstants.DOC_UTIL);
                if (utilItem.equals("")) {
                    util[i][0]  = null;
                } else {
                    util[i][0] = Double.parseDouble(utilItem);
                }

                resultNum++;
            } else {
                throw new RuntimeException("Fail to read solutions");
            }
        }
        return true;
    }

    /**
     * make an object with default values
     */
    public JLQNModel() {
        setDefaults();
    }

    public JLQNModel(JLQNModel j) {
        resultsOK = j.resultsOK;
        solverType = j.solverType;

        numberOfProcessors = j.numberOfProcessors;

        changed = j.changed;

        processorQuantum = ArrayUtils.copy(j.processorQuantum);
        processorNames = ArrayUtils.copy(j.processorNames);
        processorScheduling = ArrayUtils.copy(j.processorScheduling);
        processorMultiplicity = ArrayUtils.copy(j.processorMultiplicity);
        processorReplicas = ArrayUtils.copy(j.processorReplicas);
        processorSpeedFactor = ArrayUtils.copy(j.processorSpeedFactor);

        numberOfTasks = j.numberOfTasks;
        taskNames = ArrayUtils.copy(j.taskNames);
        taskProcessor = ArrayUtils.copy(j.taskProcessor);
        taskScheduling = ArrayUtils.copy(j.taskScheduling);
        taskReplicas = ArrayUtils.copy(j.taskReplicas);
        taskPriority = ArrayUtils.copy(j.taskPriority);
        taskThinkTimeMean = ArrayUtils.copy(j.taskThinkTimeMean);
        taskMultiplicity = ArrayUtils.copy(j.taskMultiplicity);

        numberOfEntries = j.numberOfEntries;
        entryNames = ArrayUtils.copy(j.entryNames);
        entryTask = ArrayUtils.copy(j.entryTask);
        entryArrivalRate = ArrayUtils.copy(j.entryArrivalRate);
        entryPriority = ArrayUtils.copy(j.entryPriority);
        entryBoundToActivity = ArrayUtils.copy(j.entryBoundToActivity);
        entryReplyToActivity = ArrayUtils.copy(j.entryReplyToActivity);

        numberOfActivities = j.numberOfActivities;
        activityNames = ArrayUtils.copy(j.activityNames);
        activityTask = ArrayUtils.copy(j.activityTask);
        activityHostDemand = ArrayUtils.copy(j.activityHostDemand);

        numberOfCalls = j.numberOfCalls;
        callNames = ArrayUtils.copy(j.callNames);
        callActivity = ArrayUtils.copy(j.callActivity);
        callEntry = ArrayUtils.copy(j.callEntry);
        callMeanRepeatTimes = ArrayUtils.copy(j.callMeanRepeatTimes);
        callType= ArrayUtils.copy(j.callType);

        numberOfPrecedences = j.numberOfPrecedences;
        precedenceType = ArrayUtils.copy(j.precedenceType);
        precedencePreActivities = ArrayUtils.copy2(j.precedencePreActivities);
        precedencePostActivities = ArrayUtils.copy2(j.precedencePostActivities);
        precedencePreParams = ArrayUtils.copy2(j.precedencePreParams);
        precedencePostParams = ArrayUtils.copy2(j.precedencePostParams);

        rowCounts = j.rowCounts;
        queueLen = new Double[rowCounts][1];
        throughput = new Double[rowCounts][1];
        residTimes = new Double[rowCounts][1];
        util = new Double[rowCounts][1];
        respTimes = new Double[rowCounts][1];
    }

    public boolean setProcessorNames(String[] processorNames) {
        if (processorNames.length != numberOfProcessors) {
            System.out.println(processorNames.length+'\n');
            System.out.println(numberOfProcessors +'\n');
            throw new IllegalArgumentException("processorNames.length!=numberOfProcessors");
        }
        if (Arrays.equals(this.processorNames, processorNames)) {
            return false;
        }
        this.processorNames = processorNames;
        changed = true;
        return true;
    }

    public boolean setProcessorScheduling(int[] processorScheduling) {
        if (processorScheduling.length != numberOfProcessors) {
            throw new IllegalArgumentException("processorScheduling.length!=numberOfProcessors");
        }
        if (Arrays.equals(this.processorScheduling, processorScheduling)) {
            return false;
        }

        this.processorScheduling = processorScheduling;
        changed = true;
        return true;
    }

    public boolean setProcessorQuantum(double[] processorQuantum) {
        if (processorQuantum.length != numberOfProcessors) {
            throw new IllegalArgumentException("processorQuantum.length!=numberOfProcessors");
        }


        if (Arrays.equals(this.processorQuantum, processorQuantum)) {
            return false;
        }
        this.processorQuantum = processorQuantum;
        changed = true;
        return true;
    }
    public boolean setProcessorMultiplicity(int[] processorMultiplicity) {
        if (processorMultiplicity.length != numberOfProcessors) {
            throw new IllegalArgumentException("processorMultiplicity.length!=numberOfProcessors");
        }

        if (Arrays.equals(this.processorMultiplicity, processorMultiplicity)) {
            return false;
        }
        this.processorMultiplicity = processorMultiplicity;
        changed = true;
        return true;
    }
    public boolean setProcessorReplicas(int[] processorReplicas) {
        if (processorReplicas.length != numberOfProcessors) {

            throw new IllegalArgumentException("processorReplicas.length!=numberOfProcessors");
        }


        if (Arrays.equals(this.processorReplicas, processorReplicas)) {
            return false;
        }
        this.processorReplicas = processorReplicas;
        changed = true;
        return true;
    }

    public boolean setProcessorSpeedFactor(double[] processorSpeedFactor) {
        if (processorSpeedFactor.length != numberOfProcessors) {

            throw new IllegalArgumentException("processorSpeedFactor.length!=numberOfProcessors");
        }


        if (Arrays.equals(this.processorSpeedFactor, processorSpeedFactor)) {
            return false;
        }
        this.processorSpeedFactor = processorSpeedFactor;
        changed = true;
        return true;
    }
    public boolean setTaskNames(String[] taskNames) {
        if (taskNames.length != numberOfTasks) {
            System.out.println(taskNames.length+'\n');
            System.out.println(numberOfTasks +'\n');
            throw new IllegalArgumentException("taskNames.length!=numberOfTasks");
        }
        if (Arrays.equals(this.taskNames, taskNames)) {
            return false;
        }
        this.taskNames = taskNames;
        changed = true;
        return true;
    }
    public boolean setTaskScheduling(int[] taskScheduling) {
        if (taskScheduling.length != numberOfTasks) {
            throw new IllegalArgumentException("taskScheduling.length!=numberOfTasks");
        }
        if (Arrays.equals(this.taskScheduling, taskScheduling)) {
            return false;
        }

        this.taskScheduling = taskScheduling;
        changed = true;
        return true;
    }
//    public void setTaskProcessor() {
//        taskProcessor = getTaskProcessor();
//    }
    public boolean setTaskMultiplicity(int[] taskMultiplicity) {
        if (taskMultiplicity.length != numberOfTasks) {
            throw new IllegalArgumentException("taskMultiplicity.length!=numberOfTasks");
        }
        if (Arrays.equals(this.taskMultiplicity, taskMultiplicity)) {
            return false;
        }

        this.taskMultiplicity = taskMultiplicity;
        changed = true;
        return true;
    }
    public boolean setTaskReplicas(int[] taskReplicas) {
        if (taskReplicas.length != numberOfTasks) {
            throw new IllegalArgumentException("taskReplicas.length!=numberOfTasks");
        }
        if (Arrays.equals(this.taskReplicas, taskReplicas)) {
            return false;
        }

        this.taskReplicas = taskReplicas;
        changed = true;
        return true;
    }
//    public void setTaskProcessor() {
//        taskProcessor = getTaskProcessor();
//        changed = true;
//    }
    public boolean setTaskProcessor(String[] taskProcessor) {
        if (taskProcessor.length != numberOfTasks) {
            throw new IllegalArgumentException("taskProcessor.length!=numberOfTasks");
        }
        if (Arrays.equals(this.taskProcessor, taskProcessor)) {
            return false;
        }

        this.taskProcessor = taskProcessor;
        changed = true;
        return true;
    }
    public boolean setTaskThinkTimeMean(double[] taskThinkTimeMean) {
        if (taskThinkTimeMean.length != numberOfTasks) {
            throw new IllegalArgumentException("taskThinkTimeMean.length!=numberOfTasks");
        }
        if (Arrays.equals(this.taskThinkTimeMean, taskThinkTimeMean)) {
            return false;
        }

        this.taskThinkTimeMean = taskThinkTimeMean;
        changed = true;
        return true;
    }

    public boolean setTaskPriority(int[] taskPriority) {
        if (taskPriority.length != numberOfTasks) {
            throw new IllegalArgumentException("taskPriority.length!=numberOfTasks");
        }
        if (Arrays.equals(this.taskPriority, taskPriority)) {
            return false;
        }

        this.taskPriority = taskPriority;
        changed = true;
        return true;
    }

    public boolean setEntryNames(String[] entryNames) {
        if (entryNames.length != numberOfEntries) {
            System.out.println(entryNames.length +'\n');
            System.out.println(numberOfEntries +'\n');
            throw new IllegalArgumentException("entryNames.length!=numberOfEntries");
        }
        if (Arrays.equals(this.entryNames, entryNames)) {
            return false;
        }
        this.entryNames = entryNames;
        changed = true;
        return true;
    }


    public boolean setEntryTask(String[] entryTask) {
        if (entryTask.length != numberOfEntries) {
            System.out.println(entryTask.length +'\n');
            System.out.println(numberOfEntries +'\n');
            throw new IllegalArgumentException("entryTask.length!=numberOfEntries");
        }
        if (Arrays.equals(this.entryTask, entryTask)) {
            return false;
        }
        this.entryTask = entryTask;
        changed = true;
        return true;
    }

    public boolean setEntryBoundToActivity(String[] entryBoundToActivity) {
        if (entryBoundToActivity.length != numberOfEntries) {
            System.out.println(entryBoundToActivity.length +'\n');
            System.out.println(numberOfEntries +'\n');
            throw new IllegalArgumentException("entryBoundToActivity.length!=numberOfEntries");
        }
        if (Arrays.equals(this.entryBoundToActivity, entryBoundToActivity)) {
            return false;
        }
        this.entryBoundToActivity = entryBoundToActivity;
        changed = true;
        return true;
    }

    public boolean setEntryReplyToActivity(String[] entryReplyToActivity) {
        if (entryReplyToActivity.length != numberOfEntries) {
            System.out.println(entryReplyToActivity.length +'\n');
            System.out.println(numberOfEntries +'\n');
            throw new IllegalArgumentException("entryReplyToActivity.length!=numberOfEntries");
        }
        if (Arrays.equals(this.entryReplyToActivity, entryReplyToActivity)) {
            return false;
        }
        this.entryReplyToActivity = entryReplyToActivity;
        changed = true;
        return true;
    }
    public boolean setEntryArrivalRate(double[] entryArrivalRate) {
        if (entryArrivalRate.length != numberOfEntries) {

            throw new IllegalArgumentException("entryArrivalRate.length!=numberOfEntries");
        }


        if (Arrays.equals(this.entryArrivalRate, entryArrivalRate)) {
            return false;
        }
        this.entryArrivalRate = entryArrivalRate;
        changed = true;
        return true;
    }
    public boolean setEntryPriority(int[] entryPriority) {
        if (entryPriority.length != numberOfEntries) {

            throw new IllegalArgumentException("entryArrivalRate.length!=numberOfEntries");
        }


        if (Arrays.equals(this.entryPriority, entryPriority)) {
            return false;
        }
        this.entryPriority = entryPriority;
        changed = true;
        return true;
    }

    public boolean setActivityNames(String[] activityNames) {
        if (activityNames.length != numberOfActivities) {
            throw new IllegalArgumentException("activityNames.length!=numberOfActivities");
        }
        if (Arrays.equals(this.activityNames, activityNames)) {
            return false;
        }
        this.activityNames = activityNames;
        changed = true;
        return true;
    }

//    public void setActivityEntries() {
//        activityEntries = getActivityEntries();
//    }


    public boolean setActivityTask(String[] activityTask) {
        if (activityTask.length != numberOfActivities) {
            throw new IllegalArgumentException("activityTask.length!=numberOfActivities");
        }
        if (Arrays.equals(this.activityTask, activityTask)) {
            return false;
        }
        this.activityTask = activityTask;
        changed = true;
        return true;
}

    public boolean setActivityHostDemand(double[] activityHostDemand) {
        if (activityHostDemand.length != numberOfActivities) {
            throw new IllegalArgumentException("activityHostDemand.length!=numberOfActivities");
        }
        if (Arrays.equals(this.activityHostDemand, activityHostDemand)) {
            return false;
        }
        this.activityHostDemand = activityHostDemand;
        changed = true;
        return true;
    }

    public boolean setCallNames(String[] callNames) {
        if (callNames.length != numberOfCalls) {
            System.out.println(callNames.length +'\n');
            System.out.println(numberOfCalls +'\n');
            throw new IllegalArgumentException("callNames.length!=numberOfCalls");
        }
        if (Arrays.equals(this.callNames, callNames)) {
            return false;
        }
        this.callNames = callNames;
        changed = true;
        return true;
    }


    public boolean setCallActivity(String[] callActivity) {
        if (callActivity.length != numberOfCalls) {
            System.out.println(callActivity.length +'\n');
            System.out.println(numberOfCalls +'\n');
            throw new IllegalArgumentException("callActivity.length!=numberOfCalls");
        }
        if (Arrays.equals(this.callActivity, callActivity)) {
            return false;
        }
        this.callActivity = callActivity;
        changed = true;
        return true;
    }

    public boolean setCallEntry(String[] callEntry) {
        if (callEntry.length != numberOfCalls) {
            System.out.println(callEntry.length +'\n');
            System.out.println(numberOfCalls +'\n');
            throw new IllegalArgumentException("callEntry.length!=numberOfCalls");
        }
        if (Arrays.equals(this.callEntry, callEntry)) {
            return false;
        }
        this.callEntry = callEntry;
        changed = true;
        return true;
    }

    public boolean setCallType(int[] callType) {
        if (callType.length != numberOfCalls) {
            throw new IllegalArgumentException("callType.length!=numberOfCalls");
        }
        if (Arrays.equals(this.callType, callType)) {
            return false;
        }

        this.callType = callType;
        changed = true;
        return true;
    }
    public boolean setCallMeanRepeatTimes(double[] callMeanRepeatTimes) {
        if (callMeanRepeatTimes.length != numberOfCalls) {

            throw new IllegalArgumentException("callMeanRepeatTimes.length!=numberOfCalls");
        }


        if (Arrays.equals(this.callMeanRepeatTimes, callMeanRepeatTimes)) {
            return false;
        }
        this.callMeanRepeatTimes = callMeanRepeatTimes;
        changed = true;
        return true;
    }

    public boolean setPrecedenceType(int[] precedenceType) {
        if (precedenceType.length != numberOfPrecedences) {
            throw new IllegalArgumentException("precedenceType.length!=numberOfPrecedences");
        }
        if (Arrays.equals(this.precedenceType, precedenceType)) {
            return false;
        }
        this.precedenceType = precedenceType;
        changed = true;
        return true;
    }

    public boolean setPrecedencePreActivities(String[][] precedencePreActivities) {
        if (precedencePreActivities.length != numberOfPrecedences) {
            throw new IllegalArgumentException("precedencePreActivities.length!=numberOfPrecedences");
        }
        boolean changesPresent = false;
        if (this.precedencePreActivities.length == precedencePreActivities.length) {
            // Number of precedences stayed the same, check internally for changes
            for (int idx = 0; idx < precedencePreActivities.length; idx++) {
                if (!Arrays.equals(this.precedencePreActivities[idx], precedencePreActivities[idx])) {
                    changesPresent = true;
                }
            }
        } else {
            // Number of precedences changed
            changesPresent = true;
        }
        if (changesPresent) {
            this.precedencePreActivities = precedencePreActivities;
            changed = true;
        }
        return true;
    }

    public boolean setPrecedencePostActivities(String[][] precedencePostActivities) {
        if (precedencePostActivities.length != numberOfPrecedences) {
            throw new IllegalArgumentException("precedencePostActitivies.length!=numberOfPrecedences");
        }
        boolean changesPresent = false;
        if (this.precedencePostActivities.length == precedencePostActivities.length) {
            // Number of precedences stayed the same, check internally for changes
            for (int idx = 0; idx < precedencePostActivities.length; idx++) {
                if (!Arrays.equals(this.precedencePostActivities[idx], precedencePostActivities[idx])) {
                    changesPresent = true;
                }
            }
        } else {
            // Number of precedences changed
            changesPresent = true;
        }
        if (changesPresent) {
            this.precedencePostActivities = precedencePostActivities;
            changed = true;
        }
        return true;
    }

    public boolean setPrecedencePreParams(Double[][] precedencePreParams) {
        if (precedencePreParams.length != numberOfPrecedences) {
            throw new IllegalArgumentException("precedencePreParams.length!=numberOfPrecedences");
        }

        boolean changesPresent = false;
        if (this.precedencePreParams.length == precedencePreParams.length) {
            // Number of precedences stayed the same, check internally for changes
            for (int idx = 0; idx < precedencePreParams.length; idx++) {
                if (!Arrays.equals(this.precedencePreParams[idx], precedencePreParams[idx])) {
                    changesPresent = true;
                }
            }
        } else {
            // Number of precedences changed
            changesPresent = true;
        }
        if (changesPresent) {
            this.precedencePreParams = precedencePreParams;
            changed = true;
        }
        return true;
    }

    public boolean setPrecedencePostParams(Double[][] precedencePostParams) {
        if (precedencePostParams.length != numberOfPrecedences) {
            throw new IllegalArgumentException("precedencePostParams.length!=numberOfPrecedences");
        }

        boolean changesPresent = false;
        if (this.precedencePostParams.length == precedencePostParams.length) {
            // Number of precedences stayed the same, check internally for changes
            for (int idx = 0; idx < precedencePostParams.length; idx++) {
                if (!Arrays.equals(this.precedencePostParams[idx], precedencePostParams[idx])) {
                    changesPresent = true;
                }
            }
        } else {
            // Number of precedences changed
            changesPresent = true;
        }
        if (changesPresent) {
            this.precedencePostParams = precedencePostParams;
            changed = true;
        }
        return true;
    }

    public void setViewType(ViewerType viewerType) {
        this.viewerType = viewerType;
    }

    public void setSolverType(SolverType solverType) {
        this.solverType = solverType;
    }
    /**
     * Check if a task with the same name as input is a reference task.
     * @param taskName String that represents the task name.
     * @return true if the task is a reference task.
     */
    public boolean isReferenceTask(String taskName) {
        if (taskName == null) {
            return false;
        }

        int index = -1;
        for (int i = 0; i < numberOfTasks; i++) {
            if (taskName.equalsIgnoreCase(taskNames[i])) {
                index = i;
            }
        }

        if (index < 0) {
            throw new RuntimeException("Error fetching the task index");
        }
        return taskScheduling[index] == TASK_REF;
    }

    /**
     * Check if an activity with the input name is on a reference task.
     * @param activityName String that represents the activity name.
     * @return true if the activity is on a reference task.
     */
    public boolean isOnReferenceTask(String activityName) {
        if (activityName == null) {
            return false;
        }
        int activityIndex = -1;
        for (int i = 0; i < numberOfActivities; i++) {
            if (activityName.equalsIgnoreCase(activityNames[i])) {
                activityIndex = i;
            }
        }

        if (activityIndex < 0) {
            throw new RuntimeException("Error fetching the activity index");
        }

        String entryName = activityTask[activityIndex];
        if (entryName == null) {
            return false;
        }
        // Retrieve the Entry e of Activity i, then retrieve the task of Entry E

        int entryIndex = -1;
        for (int i = 0; i < numberOfEntries; i++) {
            if (entryName.equalsIgnoreCase(entryNames[i])) {
                entryIndex = i;
            }
        }

        if (entryIndex < 0) {
            return false;
        }
        return isReferenceTask(entryTask[entryIndex]);
    }

    public void updateEntryReplyToActivityBeforeSync() {
        for (int i = 0; i < numberOfEntries; i++) {
            String taskName = entryTask[i];
            String activityName = entryReplyToActivity[i];

            if (isReferenceTask(taskName)) {
                entryReplyToActivity[i] = "N/A";
            } else {
                if ("N/A".equalsIgnoreCase(activityName)
                    || isOnReferenceTask(activityName)) {
                    entryReplyToActivity[i] = null;
                }
            }
        }
    }
    /**
     * Resets arrays used to store results
     */
    public void resetResults() {
        queueLen = new Double[rowCounts][1];
        throughput = new Double[rowCounts][1];
        residTimes = new Double[rowCounts][1];
        respTimes = new Double[rowCounts][1];
        util = new Double[rowCounts][1];

        // Full wipe existing maps
        resetHashMaps();

        errors.setLength(0);
        changed = true;
    }
    public Double[][] getQueueLen(){return queueLen;}
    public Double[][] getThroughput(){return throughput;}
    public Double[][] getResidTimes(){return residTimes;}
    public Double[][] getRespTimes(){return respTimes;}
    public Double[][] getUtil(){return util;}
    private void resetHashMaps() {
        activityParentTaskMap.clear();
        entryTaskMap.clear();
    };
}
