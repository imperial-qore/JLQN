package jmt.jlqn;

import jlqn.model.JLQNModel;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;


public class JlqnOpenModelTest {
  @Test
  public void canOpenJlqnModelAndStoreCorrectValues() throws ParserConfigurationException, IOException, SAXException {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = dbf.newDocumentBuilder();
    Document input = db.parse(new File("src/test/java/jmt/jlqn/JLQNTest.jlqn"));
    JLQNModel model = new JLQNModel();
    model.loadDocument(input);

    double delta = 1e-6;

    int numberOfProcessors = model.getNumberOfProcessors();
    String[] processorNames = model.getProcessorNames();
    int[] processorScheduling = model.getProcessorScheduling();
    double[] processorQuantum = model.getProcessorQuantum();
    int[] processorMultiplicity = model.getProcessorMultiplicity();
    int[] processorReplicas = model.getProcessorReplicas();
    double[] processorSpeedFactor = model.getProcessorSpeedFactor();

    int numberOfTasks = model.getNumberOfTasks();
    String[] taskNames = model.getTaskNames();
    int[] taskScheduling = model.getTaskScheduling();
    String[] taskProcessor = model.getTaskProcessor();
    int[] taskPriority = model.getTaskPriority();
    double[] taskThinkTimeMean = model.getTaskThinkTimeMean();
    int[] taskMultiplicity = model.getTaskMultiplicity();
    int[] taskReplicas = model.getTaskReplicas();

    int numberOfEntries = model.getNumberOfEntries();
    String[] entryNames = model.getEntryNames();
    String[] entryTask = model.getEntryTask();
    String[] entryBoundToActivity = model.getEntryBoundToActivity();
    String[] entryReplyToActivity = model.getEntryReplyToActivity();
    double[] entryArrivalRate = model.getEntryArrivalRate();
    int[] entryPriority = model.getEntryPriority();

    int numberOfActivities = model.getNumberOfActivities();
    String[] activityNames = model.getActivityNames();
    String[] activityTask = model.getActivityTask();
    double[] activityHostDemand = model.getActivityHostDemand();

    int numberOfCalls = model.getNumberOfCalls();
    String[] callNames = model.getCallNames();
    String[] callActivity = model.getCallActivity();
    String[] callEntry = model.getCallEntry();
    int[] callType = model.getCallType();
    double[] callMeanRepeatTimes = model.getCallMeanRepeatTimes();

    assertEquals(4, numberOfProcessors);

    String[] expectedProcessorNames = {"Processor1", "Processor8", "Processor9", "Processor22"};
    assertArrayEquals(expectedProcessorNames, processorNames);

    int[] expectedProcessorScheduling = {2, 0, 5, 1};
    assertArrayEquals(expectedProcessorScheduling, processorScheduling);

    double[] expectedProcessorQuantum = {0.033, 0.02, 0.04, 0.06};
    assertArrayEquals(expectedProcessorQuantum, processorQuantum, delta);

    int[] expectedProcessorMultiplicity = {1110, 2, 3, 1};
    assertArrayEquals(expectedProcessorMultiplicity, processorMultiplicity);

    int[] expectedProcessorReplicas = {32, 3, 6, 77};
    assertArrayEquals(expectedProcessorReplicas, processorReplicas);

    double[] expectedProcessorSpeedFactor = {0.0430, 0.022, 0.01, 0.0886};
    assertArrayEquals(expectedProcessorSpeedFactor, processorSpeedFactor, delta);

    assertEquals(5, numberOfTasks);

    String[] expectedTaskNames = {"Task1", "Task2", "Task3", "Task4", "Task5"};
    assertArrayEquals(expectedTaskNames, taskNames);

    int[] expectedTaskScheduling = {0, 0, 2, 1, 1};
    assertArrayEquals(expectedTaskScheduling, taskScheduling);

    String[] expectedTaskProcessor = {"Processor22", "Processor1", null, null, null};
    assertArrayEquals(expectedTaskProcessor, taskProcessor);

    int[] expectedTaskPriority = {2, 3, 5, 9, 1};
    assertArrayEquals(expectedTaskPriority, taskPriority);

    double[] expectedTaskThinkTimeMean = {0.0333, 0.011, 0.022, 0, 0};
    assertArrayEquals(expectedTaskThinkTimeMean, taskThinkTimeMean, delta);

    int[] expectedTaskMultiplicity = {2, 1, 4, 6, 9};
    assertArrayEquals(expectedTaskMultiplicity, taskMultiplicity);

    int[] expectedTaskReplicas = {43, 1, 2, 3, 5};
    assertArrayEquals(expectedTaskReplicas, taskReplicas);

    assertEquals(7, numberOfEntries);

    String[] expectedEntryNames = {"Entry1", "Entry2", "Entry3", "Entry4",
                                    "Entry5", "Entry6", "Entry7"};
    assertArrayEquals(expectedEntryNames, entryNames);

    String[] expectedEntryTask = {"Task1", "Task3", "Task2", null, "Task5", null, null};
    assertArrayEquals(expectedEntryTask, entryTask);

    String[] expectedEntryBoundToActivity = {"Activity2", "Activity3", "Activity5",
                                              null, null, null, null};
    assertArrayEquals(expectedEntryBoundToActivity, entryBoundToActivity);

    String[] expectedEntryReplyToActivity = {"N/A", "Activity5", "N/A", "Activity2", null, null, null};
    assertArrayEquals(expectedEntryReplyToActivity, entryReplyToActivity);

    double[] expectedEntryArrivalRate = {0.04, 0.02, 0.01, 0.02, 0.08, 0, 0};
    assertArrayEquals(expectedEntryArrivalRate, entryArrivalRate, delta);

    int[] expectedEntryPriority = {3, 2, 9, 7, 3, 0, 0};
    assertArrayEquals(expectedEntryPriority, entryPriority);

    assertEquals(6, numberOfActivities);

    String[] expectedActivityNames = {"Activity1", "Activity2", "Activity3", "Activity4",
                                      "Activity5", "Activity6"};
    assertArrayEquals(expectedActivityNames, activityNames);

    String[] expectedActivityTask = {"Task1", "Task4", "Task3", "Task5", null, null};
    assertArrayEquals(expectedActivityTask, activityTask);

    double[] expectedActivityHostDemand = {0.03, 0.04, 0, 0.06, 0.03, 0};
    assertArrayEquals(expectedActivityHostDemand, activityHostDemand, delta);

    assertEquals(8, numberOfCalls);
    String[] expectedCallNames = {"Call1", "Call2", "Call3", "Call4",
                                  "Call5", "Call6", "Call7", "Call8"};
    assertArrayEquals(expectedCallNames, callNames);

    String[] expectedCallActivity = {null, "Activity1", "Activity5", "Activity3", null, null, null, null};
    assertArrayEquals(expectedCallActivity, callActivity);

    String[] expectedCallEntry = {"Entry4", "Entry5", null, "Entry3", null, null, null, null};
    assertArrayEquals(expectedCallEntry, callEntry);

    int[] expectedCallType = {1, 0, 2, 1, 0, 0, 0, 0};
    assertArrayEquals(expectedCallType, callType);

    double[] expectedCallMeanRepeatTimes = {30, 0.02, 0.04, 60, 0, 0, 0, 0};
    assertArrayEquals(expectedCallMeanRepeatTimes, callMeanRepeatTimes, delta);
  }
}
