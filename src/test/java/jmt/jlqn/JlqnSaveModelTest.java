package jmt.jlqn;

import jlqn.analytical.JLQNDocumentCreator;
import jlqn.analytical.JLQNModel;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class JlqnSaveModelTest {
  @Test
  public void canOpenJlqnModelAndStoreCorrectValues() throws ParserConfigurationException, IOException, SAXException {
    JLQNModel model = new JLQNModel();

    String[] expectedProcessorNames = {"Processor1", "Processor8", "Processor9", "Processor22"};
    int[] expectedProcessorScheduling = {2, 0, 5, 1};
    double[] expectedProcessorQuantum = {0.033, 0.02, 0.04, 0.06};
    int[] expectedProcessorMultiplicity = {1110, 2, 3, 1};
    int[] expectedProcessorReplicas = {32, 3, 6, 77};
    double[] expectedProcessorSpeedFactor = {0.0430, 0.022, 0.01, 0.0886};
    String[] expectedTaskNames = {"Task1", "Task2", "Task3", "Task4", "Task5"};
    int[] expectedTaskScheduling = {0, 0, 2, 1, 1};
    String[] expectedTaskProcessor = {"Processor22", "Processor1", null, null, null};
    int[] expectedTaskPriority = {2, 3, 5, 9, 1};
    double[] expectedTaskThinkTimeMean = {0.0333, 0.011, 0.022, 0, 0};
    double[] expectedTaskThinkTimeSCV = {0.05, 0.02, 0.033, 0.0111, 100};
    int[] expectedTaskMultiplicity = {2, 1, 4, 6, 9};
    int[] expectedTaskReplicas = {43, 1, 2, 3, 5};
    String[] expectedEntryNames = {"Entry1", "Entry2", "Entry3", "Entry4",
            "Entry5", "Entry6", "Entry7"};
    String[] expectedEntryTask = {"Task1", "Task3", "Task2", null, "Task5", null, null};
    String[] expectedEntryBoundToActivity = {"Activity2", "Activity3", "Activity5",
            null, null, null, null};
    String[] expectedEntryReplyToActivity = {"N/A", "Activity5", "N/A", "Activity2", null, null, null};
    double[] expectedEntryArrivalRate = {0.04, 0.02, 0.01, 0.02, 0.08, 0, 0};
    int[] expectedEntryPriority = {3, 2, 9, 7, 3, 0, 0};
    String[] expectedActivityNames = {"Activity1", "Activity2", "Activity3", "Activity4",
            "Activity5", "Activity6"};
    String[] expectedActivityTask = {"Task1", "Task4", "Task3", "Task5", null, null};
    double[] expectedActivityHostDemand = {0.03, 0.04, 0, 0.06, 0.03, 0};
    String[] expectedCallNames = {"Call1", "Call2", "Call3", "Call4",
            "Call5", "Call6", "Call7", "Call8"};
    String[] expectedCallActivity = {null, "Activity1", "Activity5", "Activity3", null, null, null, null};
    String[] expectedCallEntry = {"Entry4", "Entry5", null, "Entry3", null, null, null, null};
    int[] expectedCallType = {1, 0, 2, 1, 0, 0, 0, 0};
    double[] expectedCallMeanRepeatTimes = {30, 0.02, 0.04, 60, 0, 0, 0, 0};

    model.resizeProcessors(4, false);
    model.setProcessorNames(expectedProcessorNames);
    model.setProcessorScheduling(expectedProcessorScheduling);
    model.setProcessorQuantum(expectedProcessorQuantum);
    model.setProcessorMultiplicity(expectedProcessorMultiplicity);
    model.setProcessorReplicas(expectedProcessorReplicas);
    model.setProcessorSpeedFactor(expectedProcessorSpeedFactor);

    model.resizeTasks(5, false);
    model.setTaskNames(expectedTaskNames);
    model.setTaskScheduling(expectedTaskScheduling);
    model.setTaskProcessor(expectedTaskProcessor);
    model.setTaskPriority(expectedTaskPriority);
    model.setTaskThinkTimeMean(expectedTaskThinkTimeMean);
    model.setTaskMultiplicity(expectedTaskMultiplicity);
    model.setTaskReplicas(expectedTaskReplicas);

    model.resizeEntries(7, false);
    model.setEntryNames(expectedEntryNames);
    model.setEntryTask(expectedEntryTask);
    model.setEntryBoundToActivity(expectedEntryBoundToActivity);
    model.setEntryReplyToActivity(expectedEntryReplyToActivity);
    model.setEntryArrivalRate(expectedEntryArrivalRate);
    model.setEntryPriority(expectedEntryPriority);

    model.resizeActivities(6, false);
    model.setActivityNames(expectedActivityNames);
    model.setActivityTask(expectedActivityTask);
    model.setActivityHostDemand(expectedActivityHostDemand);

    model.resizeCalls(8, false);
    model.setCallNames(expectedCallNames);
    model.setCallActivity(expectedCallActivity);
    model.setCallEntry(expectedCallEntry);
    model.setCallType(expectedCallType);
    model.setCallMeanRepeatTimes(expectedCallMeanRepeatTimes);

    Document actual = JLQNDocumentCreator.createDocument(model);
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = dbf.newDocumentBuilder();
    Document expected = db.parse(new File("src/test/java/jmt/jlqn/JLQNTest.jlqn"));
    assertTrue(actual.isEqualNode(expected));

  }
}
