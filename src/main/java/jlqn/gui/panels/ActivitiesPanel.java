package jlqn.gui.panels;

/**
 * Original source file license header:
 * Copyright (C) 2016, Laboratorio di Valutazione delle Prestazioni - Politecnico di Milano

 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 */

/**
 * Modification notice:
 * Modified by: Yang Bao, Giuliano Casale, Lingxiao Du, Songtao Li, Zhuoyuan Li, Dan Luo, Zifeng Wang, Yelun Yang
 * Modification date: 15-Jul-2024
 * Description of modifications: repurposed for LQN models
 */

import jlqn.common.JLQNConstants;
import jlqn.gui.JLQNWizard;
import jlqn.model.JLQNModel;
import jmt.framework.data.ArrayUtils;
import jmt.framework.gui.help.HoverHelp;
import jmt.framework.gui.table.editors.ButtonCellEditor;
import jmt.framework.gui.table.editors.ComboBoxCellEditor;
import jmt.framework.gui.wizard.WizardPanel;
import jmt.gui.common.CommonConstants;
import jmt.gui.common.JMTImageLoader;
import jlqn.gui.panels.*;
import jmt.gui.table.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ActivitiesPanel extends WizardPanel implements JLQNConstants, jlqn.gui.panels.ForceUpdatablePanel {
  private static final long serialVersionUID = 1L;

  // Column numbers
  private final static int NUM_COL = 4;
  private final static int COL_NAME = 0;
  private final static int COL_TASK = 1;
  private final static int COL_HOST_DEMAND = 2;
  private final static int COL_DELETE_BUTTON = 3;

  private HoverHelp help;
  private static final String helpText = "<html>In this panel you can define the number of activites in the system and their properties.<br><br>"
          + " To edit values, double-click on the desired cell"
          + " and start typing.<br> To select activities click or drag on the row headers.<br> <b>For a list of the available operations right-click"
          + " on the table</b>.<br>" + " Pressing DELETE removes all selected activities from the system.</html>";

  private JLQNWizard jw;
  private int numberOfActivities;
  private String[] activityNames;
  private String[] activityTask;
  private String[] ACTIVITY_TASK;

  //Activity data
  private double[] activityHostDemand;

  private int nameCounter;
  private List<ListOp> activityOps;
  private boolean hasDeletes;
  private boolean deleting = false;
  private JSpinner activitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, MAX_ACTIVITIES, 1));

  private ActivityTable activityTable;
  private TableCellEditor activityTaskEditor;

  private ChangeListener spinnerListener = new ChangeListener() {
    public void stateChanged(ChangeEvent ce) {
      if (!deleting) {
        updateSizes();
      }
    }
  };

  private void deleteSelectedActivities() {
    int[] selectedRows = activityTable.getSelectedRows();
    int nrows = selectedRows.length;
    if (nrows == 0) {
      return;
    }
    int left = activityTable.getRowCount() - nrows;
    if (left < 1) {
      activityTable.removeRowSelectionInterval(selectedRows[nrows - 1], selectedRows[nrows - 1]);
      deleteSelectedActivities();
      return;
    }
    deleteActivities(selectedRows);
  }

  private AbstractAction deleteActivity = new AbstractAction("Delete selected activities") {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    {
      putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, false));
      putValue(Action.SHORT_DESCRIPTION, "Deletes selected activities from the system");
    }

    public void actionPerformed(ActionEvent e) {
      deleteSelectedActivities();
    }
  };
  private AbstractAction deleteOneActivity = new AbstractAction("") {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    {
      putValue(Action.SHORT_DESCRIPTION, "Delete This Activity");
      putValue(Action.SMALL_ICON, JMTImageLoader.loadImage("Delete"));
    }

    public void actionPerformed(ActionEvent e) {
    }
  };
  private AbstractAction addActivity = new AbstractAction("New Activity") {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    {
      //putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.ALT_MASK));
      putValue(Action.SHORT_DESCRIPTION, "Adds a new Activity to Model");
    }

    public void actionPerformed(ActionEvent e) {
      addActivity();
    }
  };

  private void addActivity() {
    setNumberOfActivities(numberOfActivities + 1);
  }

  private void deleteActivity(int i) {
    numberOfActivities--;
    activitySpinner.setValue(new Integer(numberOfActivities));

    String deletedActivityName = activityNames[i];
    JLQNModel data = jw.getData();
    synchronized (data) {
      data.changeAllEntryBoundToActivity(deletedActivityName, null);
      data.changeAllEntryReplyToActivity(deletedActivityName, null);
      data.changeAllCallActivity(deletedActivityName, null);
      data.changeAllPrecedenceActivities(deletedActivityName, null);
    }

    activityNames = ArrayUtils.delete(activityNames, i);
    activityTask = ArrayUtils.delete(activityTask, i);
    activityHostDemand = ArrayUtils.delete(activityHostDemand, i);

    activityOps.add(ListOp.createDeleteOp(i));
    hasDeletes = true;
  }

  private void deleteActivities(int[] idx) {
    deleting = true;
    Arrays.sort(idx);
    for (int i = idx.length - 1; i >= 0; i--) {
      deleteActivity(idx[i]);
    }
    updateSizes();
    deleting = false;
  }

  private class ActivityTable extends ExactTable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    TableCellRenderer disabledCellRenderer;
    TableCellEditor activityTaskCellEditor;

    ComboBoxCell activityTaskComboBoxCell;
    ButtonCellEditor deleteButtonCellRenderer;
    JButton deleteButton;

    //END Federico Dall'Orso 8/3/2005

    ActivityTable() {
      super(new ActivityTableModel());
      setName("Activity Table");

      disabledCellRenderer = new DisabledCellRenderer();

      activityTaskComboBoxCell = new ComboBoxCell(ACTIVITY_TASK);

      deleteButton = new JButton(deleteOneActivity);
      deleteButtonCellRenderer = new ButtonCellEditor(deleteButton);
      enableDeletes();
      rowHeader.setRowHeight(CommonConstants.ROW_HEIGHT);
      setRowHeight(CommonConstants.ROW_HEIGHT);


      activityTaskCellEditor = ComboBoxCellEditor.getEditorInstance(ACTIVITY_TASK);
      activityTaskEditor = ComboBoxCellEditor.getEditorInstance(ACTIVITY_TASK);

      setRowSelectionAllowed(true);
      setColumnSelectionAllowed(false);

      // not beautiful, but effective. See ClassTableModel.getColumnClass()
      setDefaultRenderer(DisabledCellRenderer.class, disabledCellRenderer);
      setDefaultEditor(String.class, activityTaskCellEditor);

      setDisplaysScrollLabels(true);

      //int[] selectedRows = processorTable.getSelectedRows();
      installKeyboardAction(getInputMap(), getActionMap(), deleteActivity);
      mouseHandler = new ExactTable.MouseHandler(makeMouseMenu());
      mouseHandler.install();

      help.addHelp(this,
              "Click or drag to select activities; to edit data double-click and start typing. Right-click for a list of available operations");
      help.addHelp(moreRowsLabel, "There are more activities: scroll down to see them");
      help.addHelp(selectAllButton, "Click to select all activities");
      tableHeader.setToolTipText(null);
      rowHeader.setToolTipText(null);
      help.addHelp(rowHeader, "Click, SHIFT-click or drag to select activities");

    }

    /**
     * Overridden to ensure proper handling of type column
     */
    @Override
    public TableCellEditor getCellEditor(int row, int column) {
       if (column == COL_TASK) {
        /* select the right editor */
        return activityTaskEditor;
      } else {
        return super.getCellEditor(row, column);
      }
    }

    /*enables deleting operations with last column's button*/
    private void enableDeletes() {
      deleteOneActivity.setEnabled(numberOfActivities > 1);
      /*It seems the only way to implement row deletion...*/
      this.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          if ((columnAtPoint(e.getPoint()) == getColumnCount() - 1) && getRowCount() > 1) {
            setRowSelectionInterval(rowAtPoint(e.getPoint()), rowAtPoint(e.getPoint()));
            deleteSelectedActivities();
          }
        }
      });
      getColumnModel().getColumn(getColumnCount() - 1).setMinWidth(20);
      getColumnModel().getColumn(getColumnCount() - 1).setMaxWidth(20);
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
      //if this is type column, i must render it as a combo box instead of a jtextfield
      if (column == COL_TASK) {
        return activityTaskComboBoxCell;
      } else if (column == COL_DELETE_BUTTON) {
        return deleteButtonCellRenderer;
      } else {
        return disabledCellRenderer;
      }
    }

    @Override
    protected void installKeyboard() {
    }


    @Override
    protected void installMouse() {
    }

    @Override
    protected JPopupMenu makeMouseMenu() {
      JPopupMenu menu = new JPopupMenu();
      menu.add(deleteActivity);
      return menu;
    }

    /**
     * Make sure the table reflects changes on editing end
     * Overridden to truncate decimals in data if current class is closed
     */
//        @Override
//        public void editingStopped(ChangeEvent ce) {
////            if (processorScheduling[editingRow] == CLASS_CLOSED) {
////                classData[editingRow] = (int) classData[editingRow];
////            }
////            updateRow(editingRow);
////            super.editingStopped(ce);
//        }

    //BEGIN Federico Dall'Orso 14/3/2005
    //NEW
    //Updates appearence of last column's buttons
    void updateDeleteCommand() {
      deleteOneActivity.setEnabled(numberOfActivities > 1);
      getColumnModel().getColumn(getColumnCount() - 1).setMinWidth(20);
      getColumnModel().getColumn(getColumnCount() - 1).setMaxWidth(20);

    }

    //END Federico Dall'Orso 14/3/2005

    @Override
    protected void updateActions() {
      boolean isEnabled = numberOfActivities > 1 && getSelectedRowCount() > 0;
      deleteActivity.setEnabled(isEnabled);
      deleteOneActivity.setEnabled(numberOfActivities > 1);
    }

    public void updateTaskNames() {
      activityTaskCellEditor = ComboBoxCellEditor.getEditorInstance(ACTIVITY_TASK);
      activityTaskEditor = ComboBoxCellEditor.getEditorInstance(ACTIVITY_TASK);
      activityTaskComboBoxCell = new ComboBoxCell(ACTIVITY_TASK);
    }

  }

  private class ActivityTableModel extends ExactTableModel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private Object[] prototypes = { "10000", new String(new char[15]), "",
            new String(new char[15]), new String(new char[15]), ""};
    @Override
    public Object getPrototype(int columnIndex) {
      return prototypes[columnIndex + 1];
    }

    public int getRowCount() {
      return numberOfActivities;
    }

//        @Override
//        @SuppressWarnings({"unchecked", "rawtypes"})
//        public Class getColumnClass(int col) {
//            switch (col) {
//                case COL_SCHEDULING:
//                    return String.class;
//                case COL_QUANTUM:
//                    return Integer.class;
//                case COL_MULTIPLICITY:
//                case COL_REPLICAS:
//                    return DisabledCellRenderer.class;
//                case COL_SPEED_FACTOR:
//                default:
//                    return Object.class;
//            }
//        }

    public int getColumnCount() {
      return NUM_COL;
    }

    @Override
    public String getColumnName(int index) {
      switch (index) {
        case COL_NAME:
          return "Name";
        case COL_TASK:
          return "Task";
        case COL_HOST_DEMAND:
          return "Host Demand";
          /*case COL_EXEC_DEMAND:
          return "Exec Demand";
        case COL_MAX_SERV_TIME:
          return "Max Serv Time";
        case COL_COEFF_OF_VARIATION:
          return "Coeff of Variation";*/
        default:
          return null;
      }
    }

    @Override
    protected Object getValueAtImpl(int rowIndex, int columnIndex) {
      switch (columnIndex) {
        case COL_NAME:
          return activityNames[rowIndex];
        case COL_TASK:
          return activityTask[rowIndex];
        case COL_HOST_DEMAND:
          return activityHostDemand[rowIndex];
        /*case COL_EXEC_DEMAND:
          return activityExecDemand[rowIndex];
        case COL_MAX_SERV_TIME:
          return activityMaxServTime[rowIndex];
        case COL_COEFF_OF_VARIATION:
          return activityCoeffOfVariation[rowIndex];*/
        default:
          return null;
      }
    }

    @Override
    protected Object getRowName(int rowIndex) {
      return new Integer(rowIndex + 1);
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
      switch (columnIndex) {
        case COL_NAME: {
          String newval = ((String) value).trim();

          if (isNameValid(newval) && !areThereDuplicates(newval, rowIndex, true)) {

            JLQNModel data = jw.getData();
            synchronized (data) {
              data.changeAllEntryBoundToActivity(activityNames[rowIndex], newval);
              data.changeAllEntryReplyToActivity(activityNames[rowIndex], newval);
              data.changeAllCallActivity(activityNames[rowIndex], newval);
              data.changeAllPrecedenceActivities(activityNames[rowIndex], newval);
            }
            activityNames[rowIndex] = newval;
          }
          break;
        }
        case COL_TASK: {
          String newval = (String) value;
          for (int i = 0; i < ACTIVITY_TASK.length; i++) {
            boolean condition = (newval == null)
                    || (ACTIVITY_TASK[i] != null && ACTIVITY_TASK[i].equalsIgnoreCase(newval));
            if (condition) {
              activityTask[rowIndex] = newval;
              break;
            }
          }
          break;
        }

        case COL_HOST_DEMAND:
          try {
            double newval = Double.parseDouble((String) value);
            if (newval >= 0.0) {
              activityHostDemand[rowIndex] = newval;
            }
          } catch (NumberFormatException e) {
          }
          break;
        default:
      }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
      switch (columnIndex) {
        case COL_NAME:
        case COL_TASK:
        case COL_HOST_DEMAND:
          return true;
        /*case COL_EXEC_DEMAND:
        case COL_MAX_SERV_TIME:
        case COL_COEFF_OF_VARIATION:
          return true;*/
        default:
          return false;
      }
    }
  }

  private void initComponents() {
    activitySpinner.addChangeListener(spinnerListener);
    help.addHelp(activitySpinner, "Enter the number of activities for this system");

    activityTable = new ActivityTable();

    /* and now some Box black magic */
    Box activitySpinnerBox = Box.createHorizontalBox();
    //JLabel spinnerLabel = new JLabel("<html><font size=\"4\">Set the Number of classes (1-" + MAX_CLASSES + "):</font></html>");
    //NEW
    //@author Stefano
    JLabel spinnerLabel = new JLabel(DESCRIPTION_ACTIVITES);

    activitySpinnerBox.add(spinnerLabel);
    //END
    //BEGIN Federico Dall'Orso 9/3/2005
    //OLD
		/*
		classSpinnerBox.add(Box.createGlue());
		 */
    //NEW
    activitySpinnerBox.add(Box.createHorizontalStrut(10));
    Box numberBox = Box.createVerticalBox();

    JPanel spinnerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JLabel numberLabel = new JLabel("Number:");
    activitySpinner.setMaximumSize(new Dimension(600, 18));
    spinnerPanel.add(numberLabel);
    spinnerPanel.add(activitySpinner);
    numberBox.add(spinnerPanel);

    numberBox.add(new JButton(addActivity));

    numberBox.setMaximumSize(new Dimension(300, 150));

    activitySpinnerBox.add(numberBox);
    //END  Federico Dall'Orso 9/3/2005

    Box activityBox = Box.createVerticalBox();
    activityBox.add(Box.createVerticalStrut(30));
    activityBox.add(activitySpinnerBox);
    activityBox.add(Box.createVerticalStrut(10));
    JScrollPane activityTablePane = new JScrollPane(activityTable);
    activityTablePane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    activityTablePane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    activityBox.add(activityTablePane);
    activityBox.add(Box.createRigidArea(new Dimension(10, 20)));

    Box totalBox = Box.createHorizontalBox();
    totalBox.add(Box.createHorizontalStrut(20));
    totalBox.add(activityBox);
    totalBox.add(Box.createHorizontalStrut(20));

    setLayout(new BorderLayout());
    add(totalBox, BorderLayout.CENTER);
  }

  private void sync() {
    hasDeletes = false;
    activityOps.clear();

    /* sync status with data object */
    /* arrays are copied to ensure data object consistency is preserved */
    JLQNModel data = jw.getData();
    synchronized (data) {
      numberOfActivities = data.getNumberOfActivities();
      nameCounter = numberOfActivities;

      ACTIVITY_TASK = data.getTaskNames();
      ACTIVITY_TASK = ArrayUtils.resize(ACTIVITY_TASK, ACTIVITY_TASK.length+1, null);

      activityNames = ArrayUtils.copy(data.getActivityNames());

      activityTask = ArrayUtils.copy(data.getActivityTask());

      activityHostDemand = ArrayUtils.copy(data.getActivityHostDemand());
      /*activityExecDemand = ArrayUtils.copy(data.getActivityExecDemand());
      activityMaxServTime = ArrayUtils.copy(data.getActivityMaxServTime());
      activityCoeffOfVariation = ArrayUtils.copy(data.getActivityCoeffOfVariation());*/

    }

    activitySpinner.setValue(new Integer(numberOfActivities));
    if (activityTable != null) {
      activityTable.updateTaskNames();
    }
  }

  public ActivitiesPanel(JLQNWizard jw) {
    this.jw = jw;
    help = jw.getHelp();
    activityOps = new ArrayList<ListOp>();

    sync();

    initComponents();
    makeNames();
  }

  private void updateSizes() {
    setNumberOfActivities(((Integer) activitySpinner.getValue()).intValue());
  }

  private void makeNames() {
    for (int i = 0; i < activityNames.length; i++) {
      if (activityNames[i] == null) {
        while (areThereDuplicates("Activity" + (nameCounter+1), i, false)) {
          nameCounter++;
        }
        activityNames[i] = "Activity" + (++nameCounter);
      }
    }
  }

  private void setNumberOfActivities(int number) {
    activityTable.stopEditing();
    numberOfActivities = number;

    activityNames = ArrayUtils.resize(activityNames, numberOfActivities, null);
    makeNames();

    activityTask = ArrayUtils.resize(activityTask, numberOfActivities, jw.getData().getTaskNames()[0]);

    activityHostDemand = ArrayUtils.resize(activityHostDemand, numberOfActivities, 0.0);

    activityTable.updateStructure();
    if (!deleting) {
      activityOps.add(ListOp.createResizeOp(numberOfActivities));
    }

    activitySpinner.setValue(new Integer(numberOfActivities));
    activityTable.updateDeleteCommand();
  }

  @Override
  public void lostFocus() {
    commit();
    //release();
  }

  @Override
  public void gotFocus() {
    sync();
    activityTable.update();
  }

  @Override
  public String getName() {
    return "Activities";
  }

  @Override
  public boolean canFinish() {
    return true;
  }

  @Override
  public boolean canGoBack() {
    return true;
  }

  @Override
  public boolean canGoForward() {
    return true;
  }

  private boolean isNameValid(String value) {
    if (value.equalsIgnoreCase("")) {
      JOptionPane.showMessageDialog(this,
              "<html><center>Activity names cannot be empty.<br>Please enter another name.</center></html>",
              "Warning", JOptionPane.WARNING_MESSAGE);
      return false;
    }
    if (value.equalsIgnoreCase("N/A")) {
      JOptionPane.showMessageDialog(this,
              "<html><center>Activity name is invalid.<br>Please enter another name.</center></html>",
              "Warning", JOptionPane.WARNING_MESSAGE);
      return false;
    }
    return true;
  }

  private boolean areThereDuplicates(String value, int index, boolean enableWarning) {
    boolean thereAreDupl = false;
    for (int i = 0; i < activityNames.length && !thereAreDupl; i++) {
      if (i == index || activityNames[i] == null) continue;
      thereAreDupl = thereAreDupl || activityNames[i].equalsIgnoreCase(value);
    }
    if (thereAreDupl) {
      if (enableWarning) {
        JOptionPane.showMessageDialog(this,
                "<html><center>There is a duplication in activity names.<br>Please enter another name.</center></html>",
                "Warning", JOptionPane.WARNING_MESSAGE);
        return thereAreDupl;
      }
    }
    return thereAreDupl;
  }

  private void commit() {
    /* stop any editing in progress */
    if (activitySpinner.getEditor().getComponent(0).hasFocus()) {
      //disgusting. there must be a better way...
      try {
        activitySpinner.commitEdit();
        updateSizes();
      } catch (java.text.ParseException e) {
      }
    }

    activityTable.stopEditing();

    JLQNModel data = jw.getData();
    synchronized (data) {

      if (hasDeletes) {
        playbackActivityOps(data); //play back ops on the data object
      } else {
        data.resizeActivities(numberOfActivities, false);
      }
      data.setActivityNames(activityNames);
      data.setActivityTask(activityTask);
      data.setActivityHostDemand(activityHostDemand);
      //data.setActivityExecDemand(activityExecDemand);
      //data.setActivityMaxServTime(activityMaxServTime);
      //data.setActivityCoeffOfVariation(activityCoeffOfVariation);

      //NEW
      //@author Stefano Omini
      sync();
      //end NEW
    }

  }


  public void retrieveData() {
    sync();
  }

  public void commitData() {
    commit();
  }
  @Override
  public void help() {
    JOptionPane.showMessageDialog(this, helpText, "Help", JOptionPane.INFORMATION_MESSAGE);
  }

  private void playbackActivityOps(JLQNModel data) {
    for (int i = 0; i < activityOps.size(); i++) {
      ListOp lo = activityOps.get(i);
      if (lo.isDeleteOp()) {
        data.deleteActivity(lo.getData());
      }
      if (lo.isResizeOp()) {
        data.resizeActivities(lo.getData(), false);
      }
    }
  }}