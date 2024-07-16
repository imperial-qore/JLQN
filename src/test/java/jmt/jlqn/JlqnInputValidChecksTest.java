package jmt.jlqn;

import jlqn.model.JLQNModel;
import org.junit.Test;

import static org.junit.Assert.*;

public class JlqnInputValidChecksTest {
  @Test
  public void testDuplicateNameCheck(){
    JLQNModel model = new JLQNModel();
    String[] expectedProcessorNames = {"P", "P"};
    int[] expectedProcessorScheduling = {2, 0};
    double[] expectedProcessorQuantum = {0.033, 0.02};
    int[] expectedProcessorMultiplicity = {1110, 2};
    int[] expectedProcessorReplicas = {32, 3};
    double[] expectedProcessorSpeedFactor = {0.0430, 0.022};
    String[] expectedTaskNames = {"T", "T"};
    int[] expectedTaskScheduling = {0, 0};
    String[] expectedTaskProcessor = {"P", "P"};
    int[] expectedTaskPriority = {2, 3};
    double[] expectedTaskThinkTimeMean = {0.0333, 0.011};
    double[] expectedTaskThinkTimeSCV = {0.05, 0.02};
    int[] expectedTaskMultiplicity = {2, 1};
    int[] expectedTaskReplicas = {43, 1};
    String[] expectedEntryNames = {"E", "E"};
    String[] expectedEntryTask = {"T", "T"};
    String[] expectedEntryBoundToActivity = {"A", "A"};
    String[] expectedEntryReplyToActivity = {"N/A", "A"};
    double[] expectedEntryArrivalRate = {0.04, 0.02};
    int[] expectedEntryPriority = {3, 2};
    String[] expectedActivityNames = {"A", "A"};
    String[] expectedActivityTask = {"T", "T"};
    double[] expectedActivityHostDemand = {0.03, 0.04};

    model.resizeProcessors(2, false);
    model.setProcessorNames(expectedProcessorNames);
    model.setProcessorScheduling(expectedProcessorScheduling);
    model.setProcessorQuantum(expectedProcessorQuantum);
    model.setProcessorMultiplicity(expectedProcessorMultiplicity);
    model.setProcessorReplicas(expectedProcessorReplicas);
    model.setProcessorSpeedFactor(expectedProcessorSpeedFactor);

    model.resizeTasks(2, false);
    model.setTaskNames(expectedTaskNames);
    model.setTaskScheduling(expectedTaskScheduling);
    model.setTaskProcessor(expectedTaskProcessor);
    model.setTaskPriority(expectedTaskPriority);
    model.setTaskThinkTimeMean(expectedTaskThinkTimeMean);
    model.setTaskMultiplicity(expectedTaskMultiplicity);
    model.setTaskReplicas(expectedTaskReplicas);

    model.resizeEntries(2, false);
    model.setEntryNames(expectedEntryNames);
    model.setEntryTask(expectedEntryTask);
    model.setEntryBoundToActivity(expectedEntryBoundToActivity);
    model.setEntryReplyToActivity(expectedEntryReplyToActivity);
    model.setEntryArrivalRate(expectedEntryArrivalRate);
    model.setEntryPriority(expectedEntryPriority);

    model.resizeActivities(2, false);
    model.setActivityNames(expectedActivityNames);
    model.setActivityTask(expectedActivityTask);
    model.setActivityHostDemand(expectedActivityHostDemand);

    assertFalse(model.inputValid());
    assertTrue(model.getErrors().contains("No duplicate Activity Names are allowed<br>"));
    assertTrue(model.getErrors().contains("No duplicate Task Names are allowed<br>"));
    assertTrue(model.getErrors().contains("No duplicate Processor Names are allowed<br>"));
    assertTrue(model.getErrors().contains("No duplicate Entry Names are allowed<br>"));
  }

  @Test
  public void testPrecedenceWrongTaskParent(){
    // P1 under T1, P2 under T2, P1 cannot sequence into P2
    JLQNModel model = new JLQNModel();
    String[] expectedProcessorNames = {"P"};
    String[] expectedTaskNames = {"T1", "T2"};
    String[] expectedTaskProcessor = {"P", "P"};
    double[] expectedTaskThinkTimeMean = {0.0333, 0.011};
    String[] expectedEntryNames = {"E1", "E2"};
    String[] expectedEntryTask = {"T1", "T2"};
    String[] expectedEntryBoundToActivity = {"A1", "A2"};
    String[] expectedEntryReplyToActivity = {"N/A", "A2"};
    String[] expectedActivityNames = {"A1", "A2"};
    String[] expectedActivityTask = {"T1", "T2"};
    double[] expectedActivityHostDemand = {0.03, 0.04};
    String[][] expectedPrecedencePreActivities = {{"A1"}};
    String[][] expectedPrecedencePostActivities = {{"A2"}};
    Double[][] expectedPrecedencePreParams = {{0.0}};
    Double[][] expectedPrecedencePostParams = {{0.0}};


    model.resizeProcessors(1, false);
    model.setProcessorNames(expectedProcessorNames);

    model.resizeTasks(2, false);
    model.setTaskNames(expectedTaskNames);
    model.setTaskProcessor(expectedTaskProcessor);
    model.setTaskThinkTimeMean(expectedTaskThinkTimeMean);

    model.resizeEntries(2, false);
    model.setEntryNames(expectedEntryNames);
    model.setEntryTask(expectedEntryTask);
    model.setEntryBoundToActivity(expectedEntryBoundToActivity);
    model.setEntryReplyToActivity(expectedEntryReplyToActivity);

    model.resizeActivities(2, false);
    model.setActivityNames(expectedActivityNames);
    model.setActivityTask(expectedActivityTask);
    model.setActivityHostDemand(expectedActivityHostDemand);

    model.resizePrecedences(1, false);
    model.setPrecedencePreActivities(expectedPrecedencePreActivities);
    model.setPrecedencePostActivities(expectedPrecedencePostActivities);
    model.setPrecedencePreParams(expectedPrecedencePreParams);
    model.setPrecedencePostParams(expectedPrecedencePostParams);

    assertFalse(model.inputValid());
    assertTrue(model.getErrors().contains("Precedence \"1\": Post Activities in this precedence do not share the same parent task<br>"));
  }

  @Test
  public void testSequencePrecedenceChecks(){
    // P1 under T1, P2 under T2, P1 cannot sequence into P2
    JLQNModel model = new JLQNModel();
    String[] expectedProcessorNames = {"P"};
    String[] expectedTaskNames = {"T1"};
    String[] expectedTaskProcessor = {"P"};
    double[] expectedTaskThinkTimeMean = {0.0333};
    String[] expectedEntryNames = {"E1"};
    String[] expectedEntryTask = {"T1"};
    String[] expectedEntryBoundToActivity = {"A1"};
    String[] expectedEntryReplyToActivity = {"N/A"};
    String[] expectedActivityNames = {"A1", "A2", "A3"};
    String[] expectedActivityTask = {"T1", "T1", "T1"};
    double[] expectedActivityHostDemand = {0.03, 0.04, 0.05};
    int[] expectedPrecedenceType = {0, 0};
    String[][] expectedPrecedencePreActivities = {{"A1", "A2"}, {"A1"}};
    String[][] expectedPrecedencePostActivities = {{"A3"}, {}};
    Double[][] expectedPrecedencePreParams = {{0.0}, {0.0}};
    Double[][] expectedPrecedencePostParams = {{0.0}, {0.0}};


    model.resizeProcessors(1, false);
    model.setProcessorNames(expectedProcessorNames);

    model.resizeTasks(1, false);
    model.setTaskNames(expectedTaskNames);
    model.setTaskProcessor(expectedTaskProcessor);
    model.setTaskThinkTimeMean(expectedTaskThinkTimeMean);

    model.resizeEntries(1, false);
    model.setEntryNames(expectedEntryNames);
    model.setEntryTask(expectedEntryTask);
    model.setEntryBoundToActivity(expectedEntryBoundToActivity);
    model.setEntryReplyToActivity(expectedEntryReplyToActivity);

    model.resizeActivities(3, false);
    model.setActivityNames(expectedActivityNames);
    model.setActivityTask(expectedActivityTask);
    model.setActivityHostDemand(expectedActivityHostDemand);

    model.resizePrecedences(2, false);
    model.setPrecedenceType(expectedPrecedenceType);
    model.setPrecedencePreActivities(expectedPrecedencePreActivities);
    model.setPrecedencePostActivities(expectedPrecedencePostActivities);
    model.setPrecedencePreParams(expectedPrecedencePreParams);
    model.setPrecedencePostParams(expectedPrecedencePostParams);

    assertFalse(model.inputValid());
    assertTrue(model.getErrors().contains("Precedence \"1\": SEQUENCE should have 1 pre activity<br>"));
    assertTrue(model.getErrors().contains("Precedence \"2\": SEQUENCE should have at least 1 post activity<br>"));
  }

  @Test
  public void testAndJoinPrecedenceChecks(){
    // P1 under T1, P2 under T2, P1 cannot sequence into P2
    JLQNModel model = new JLQNModel();
    String[] expectedProcessorNames = {"P"};
    String[] expectedTaskNames = {"T1"};
    String[] expectedTaskProcessor = {"P"};
    double[] expectedTaskThinkTimeMean = {0.0333};
    String[] expectedEntryNames = {"E1"};
    String[] expectedEntryTask = {"T1"};
    String[] expectedEntryBoundToActivity = {"A1"};
    String[] expectedEntryReplyToActivity = {"N/A"};
    String[] expectedActivityNames = {"A1", "A2", "A3"};
    String[] expectedActivityTask = {"T1", "T1", "T1"};
    double[] expectedActivityHostDemand = {0.03, 0.04, 0.05};
    int[] expectedPrecedenceType = {1, 1, 1};
    String[][] expectedPrecedencePreActivities = {{"A1"}, {"A1", "A2"}, {"A1", "A2"}};
    String[][] expectedPrecedencePostActivities = {{"A2", "A3"}, {}, {"A3"}};
    Double[][] expectedPrecedencePreParams = {{0.0}, {1.0, 2.0}, {0.0}};
    Double[][] expectedPrecedencePostParams = {{0.0}, {0.0}, {0.0}};


    model.resizeProcessors(1, false);
    model.setProcessorNames(expectedProcessorNames);

    model.resizeTasks(1, false);
    model.setTaskNames(expectedTaskNames);
    model.setTaskProcessor(expectedTaskProcessor);
    model.setTaskThinkTimeMean(expectedTaskThinkTimeMean);

    model.resizeEntries(1, false);
    model.setEntryNames(expectedEntryNames);
    model.setEntryTask(expectedEntryTask);
    model.setEntryBoundToActivity(expectedEntryBoundToActivity);
    model.setEntryReplyToActivity(expectedEntryReplyToActivity);

    model.resizeActivities(3, false);
    model.setActivityNames(expectedActivityNames);
    model.setActivityTask(expectedActivityTask);
    model.setActivityHostDemand(expectedActivityHostDemand);

    model.resizePrecedences(3, false);
    model.setPrecedenceType(expectedPrecedenceType);
    model.setPrecedencePreActivities(expectedPrecedencePreActivities);
    model.setPrecedencePostActivities(expectedPrecedencePostActivities);
    model.setPrecedencePreParams(expectedPrecedencePreParams);
    model.setPrecedencePostParams(expectedPrecedencePostParams);

    assertFalse(model.inputValid());
    assertTrue(model.getErrors().contains("Precedence \"1\": AND JOIN should have more than 1 pre activity<br>"));
    assertTrue(model.getErrors().contains("Precedence \"2\": AND JOIN should have 1 post activity<br>"));
    assertTrue(model.getErrors().contains("Precedence \"3\": AND JOIN pre parameter - FAN IN - cannot be empty<br>"));
  }

  @Test
  public void testOrJoinPrecedenceChecks(){
    // P1 under T1, P2 under T2, P1 cannot sequence into P2
    JLQNModel model = new JLQNModel();
    String[] expectedProcessorNames = {"P"};
    String[] expectedTaskNames = {"T1"};
    String[] expectedTaskProcessor = {"P"};
    double[] expectedTaskThinkTimeMean = {0.0333};
    String[] expectedEntryNames = {"E1"};
    String[] expectedEntryTask = {"T1"};
    String[] expectedEntryBoundToActivity = {"A1"};
    String[] expectedEntryReplyToActivity = {"N/A"};
    String[] expectedActivityNames = {"A1", "A2"};
    String[] expectedActivityTask = {"T1", "T1"};
    double[] expectedActivityHostDemand = {0.04, 0.05};
    int[] expectedPrecedenceType = {2, 2};
    String[][] expectedPrecedencePreActivities = {{}, {"A2"}};
    String[][] expectedPrecedencePostActivities = {{"A2", "A1"}, {"A1"}};
    Double[][] expectedPrecedencePreParams = {{0.0}, {0.0}};
    Double[][] expectedPrecedencePostParams = {{0.0}, {0.0}};


    model.resizeProcessors(1, false);
    model.setProcessorNames(expectedProcessorNames);

    model.resizeTasks(1, false);
    model.setTaskNames(expectedTaskNames);
    model.setTaskProcessor(expectedTaskProcessor);
    model.setTaskThinkTimeMean(expectedTaskThinkTimeMean);

    model.resizeEntries(1, false);
    model.setEntryNames(expectedEntryNames);
    model.setEntryTask(expectedEntryTask);
    model.setEntryBoundToActivity(expectedEntryBoundToActivity);
    model.setEntryReplyToActivity(expectedEntryReplyToActivity);

    model.resizeActivities(2, false);
    model.setActivityNames(expectedActivityNames);
    model.setActivityTask(expectedActivityTask);
    model.setActivityHostDemand(expectedActivityHostDemand);

    model.resizePrecedences(2, false);
    model.setPrecedenceType(expectedPrecedenceType);
    model.setPrecedencePreActivities(expectedPrecedencePreActivities);
    model.setPrecedencePostActivities(expectedPrecedencePostActivities);
    model.setPrecedencePreParams(expectedPrecedencePreParams);
    model.setPrecedencePostParams(expectedPrecedencePostParams);

    assertFalse(model.inputValid());
    assertTrue(model.getErrors().contains("Precedence \"1\": OR JOIN should have 1 pre activity<br>"));
    assertTrue(model.getErrors().contains("Precedence \"2\": OR JOIN should have more than 1 post activity<br>"));
  }

  @Test
  public void testAndForkPrecedenceChecks(){
    // P1 under T1, P2 under T2, P1 cannot sequence into P2
    JLQNModel model = new JLQNModel();
    String[] expectedProcessorNames = {"P"};
    String[] expectedTaskNames = {"T1"};
    String[] expectedTaskProcessor = {"P"};
    double[] expectedTaskThinkTimeMean = {0.0333};
    String[] expectedEntryNames = {"E1"};
    String[] expectedEntryTask = {"T1"};
    String[] expectedEntryBoundToActivity = {"A1"};
    String[] expectedEntryReplyToActivity = {"N/A"};
    String[] expectedActivityNames = {"A1", "A2", "A3"};
    String[] expectedActivityTask = {"T1", "T1", "T1"};
    double[] expectedActivityHostDemand = {0.03, 0.04, 0.05};
    int[] expectedPrecedenceType = {3, 3, 3};
    String[][] expectedPrecedencePreActivities = {{"A1", "A2"}, {"A1"}, {"A1"}};
    String[][] expectedPrecedencePostActivities = {{"A2", "A3"}, {"A2"}, {"A2", "A3"}};
    Double[][] expectedPrecedencePreParams = {{0.0}, {0.0}, {0.0}};
    Double[][] expectedPrecedencePostParams = {{1.0, 2.0}, {1.0}, {0.0}};


    model.resizeProcessors(1, false);
    model.setProcessorNames(expectedProcessorNames);

    model.resizeTasks(1, false);
    model.setTaskNames(expectedTaskNames);
    model.setTaskProcessor(expectedTaskProcessor);
    model.setTaskThinkTimeMean(expectedTaskThinkTimeMean);

    model.resizeEntries(1, false);
    model.setEntryNames(expectedEntryNames);
    model.setEntryTask(expectedEntryTask);
    model.setEntryBoundToActivity(expectedEntryBoundToActivity);
    model.setEntryReplyToActivity(expectedEntryReplyToActivity);

    model.resizeActivities(3, false);
    model.setActivityNames(expectedActivityNames);
    model.setActivityTask(expectedActivityTask);
    model.setActivityHostDemand(expectedActivityHostDemand);

    model.resizePrecedences(3, false);
    model.setPrecedenceType(expectedPrecedenceType);
    model.setPrecedencePreActivities(expectedPrecedencePreActivities);
    model.setPrecedencePostActivities(expectedPrecedencePostActivities);
    model.setPrecedencePreParams(expectedPrecedencePreParams);
    model.setPrecedencePostParams(expectedPrecedencePostParams);

    assertFalse(model.inputValid());
    assertTrue(model.getErrors().contains("Precedence \"1\": AND FORK should have 1 pre activity<br>"));
    assertTrue(model.getErrors().contains("Precedence \"2\": AND FORK should have more than 1 post activity<br>"));
    assertTrue(model.getErrors().contains("Precedence \"3\": AND FORK post parameter - FAN OUT - cannot be empty<br>"));
  }


  @Test
  public void testOrForkPrecedenceChecks(){
    // P1 under T1, P2 under T2, P1 cannot sequence into P2
    JLQNModel model = new JLQNModel();
    String[] expectedProcessorNames = {"P"};
    String[] expectedTaskNames = {"T1"};
    String[] expectedTaskProcessor = {"P"};
    double[] expectedTaskThinkTimeMean = {0.0333};
    String[] expectedEntryNames = {"E1"};
    String[] expectedEntryTask = {"T1"};
    String[] expectedEntryBoundToActivity = {"A1"};
    String[] expectedEntryReplyToActivity = {"N/A"};
    String[] expectedActivityNames = {"A1", "A2", "A3"};
    String[] expectedActivityTask = {"T1", "T1", "T1"};
    double[] expectedActivityHostDemand = {0.03, 0.04, 0.05};
    int[] expectedPrecedenceType = {4, 4, 4};
    String[][] expectedPrecedencePreActivities = {{"A1"}, {"A1", "A2"}, {"A1", "A2"}};
    String[][] expectedPrecedencePostActivities = {{"A2"}, {}, {"A3"}};
    Double[][] expectedPrecedencePreParams = {{0.0}, {0.0}, {0.0}};
    Double[][] expectedPrecedencePostParams = {{1.0}, {1.0}, {0.0}};


    model.resizeProcessors(1, false);
    model.setProcessorNames(expectedProcessorNames);

    model.resizeTasks(1, false);
    model.setTaskNames(expectedTaskNames);
    model.setTaskProcessor(expectedTaskProcessor);
    model.setTaskThinkTimeMean(expectedTaskThinkTimeMean);

    model.resizeEntries(1, false);
    model.setEntryNames(expectedEntryNames);
    model.setEntryTask(expectedEntryTask);
    model.setEntryBoundToActivity(expectedEntryBoundToActivity);
    model.setEntryReplyToActivity(expectedEntryReplyToActivity);

    model.resizeActivities(3, false);
    model.setActivityNames(expectedActivityNames);
    model.setActivityTask(expectedActivityTask);
    model.setActivityHostDemand(expectedActivityHostDemand);

    model.resizePrecedences(3, false);
    model.setPrecedenceType(expectedPrecedenceType);
    model.setPrecedencePreActivities(expectedPrecedencePreActivities);
    model.setPrecedencePostActivities(expectedPrecedencePostActivities);
    model.setPrecedencePreParams(expectedPrecedencePreParams);
    model.setPrecedencePostParams(expectedPrecedencePostParams);

    assertFalse(model.inputValid());
    assertTrue(model.getErrors().contains("Precedence \"1\": OR FORK should have more than 1 pre activity<br>"));
    assertTrue(model.getErrors().contains("Precedence \"2\": OR FORK should have 1 post activity<br>"));
    assertTrue(model.getErrors().contains("Precedence \"3\": OR FORK post parameter - SELECTION PROBABILITY - cannot be empty<br>"));
  }
  
  @Test
  public void testLoopPrecedenceChecks(){
    // P1 under T1, P2 under T2, P1 cannot sequence into P2
    JLQNModel model = new JLQNModel();
    String[] expectedProcessorNames = {"P"};
    String[] expectedTaskNames = {"T1"};
    String[] expectedTaskProcessor = {"P"};
    double[] expectedTaskThinkTimeMean = {0.0333};
    String[] expectedEntryNames = {"E1"};
    String[] expectedEntryTask = {"T1"};
    String[] expectedEntryBoundToActivity = {"A1"};
    String[] expectedEntryReplyToActivity = {"N/A"};
    String[] expectedActivityNames = {"A1", "A2", "A3"};
    String[] expectedActivityTask = {"T1", "T1", "T1"};
    double[] expectedActivityHostDemand = {0.03, 0.04, 0.05};
    int[] expectedPrecedenceType = {5, 5, 5};
    String[][] expectedPrecedencePreActivities = {{}, {"A1"}, {"A1"}};
    String[][] expectedPrecedencePostActivities = {{"A2", "A3"}, {}, {"A2", "A3"}};
    Double[][] expectedPrecedencePreParams = {{0.0}, {0.0}, {0.0}};
    Double[][] expectedPrecedencePostParams = {{1.0}, {1.0}, {0.0}};


    model.resizeProcessors(1, false);
    model.setProcessorNames(expectedProcessorNames);

    model.resizeTasks(1, false);
    model.setTaskNames(expectedTaskNames);
    model.setTaskProcessor(expectedTaskProcessor);
    model.setTaskThinkTimeMean(expectedTaskThinkTimeMean);

    model.resizeEntries(1, false);
    model.setEntryNames(expectedEntryNames);
    model.setEntryTask(expectedEntryTask);
    model.setEntryBoundToActivity(expectedEntryBoundToActivity);
    model.setEntryReplyToActivity(expectedEntryReplyToActivity);

    model.resizeActivities(3, false);
    model.setActivityNames(expectedActivityNames);
    model.setActivityTask(expectedActivityTask);
    model.setActivityHostDemand(expectedActivityHostDemand);

    model.resizePrecedences(3, false);
    model.setPrecedenceType(expectedPrecedenceType);
    model.setPrecedencePreActivities(expectedPrecedencePreActivities);
    model.setPrecedencePostActivities(expectedPrecedencePostActivities);
    model.setPrecedencePreParams(expectedPrecedencePreParams);
    model.setPrecedencePostParams(expectedPrecedencePostParams);

    assertFalse(model.inputValid());
    assertTrue(model.getErrors().contains("Precedence \"1\": LOOP should have 1 pre activity<br>"));
    assertTrue(model.getErrors().contains("Precedence \"2\": LOOP should have 1 or more post activities<br>"));
    assertTrue(model.getErrors().contains("Precedence \"3\": LOOP post parameter - COUNTS - cannot be empty<br>"));
  }
}
