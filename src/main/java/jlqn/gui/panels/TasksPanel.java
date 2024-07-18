package jlqn.gui.panels;

/**
 * Original source file license header:
 * Copyright (C) 2016, Laboratorio di Valutazione delle Prestazioni - Politecnico di Milano
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 * <p>
 * Modification notice:
 * Modified by: Yang Bao, Giuliano Casale, Lingxiao Du, Songtao Li, Zhuoyuan Li, Dan Luo, Zifeng Wang, Yelun Yang
 * Modification date: 15-Jul-2024
 * Description of modifications: repurposed for LQN models
 * <p>
 * Modification notice:
 * Modified by: Yang Bao, Giuliano Casale, Lingxiao Du, Songtao Li, Zhuoyuan Li, Dan Luo, Zifeng Wang, Yelun Yang
 * Modification date: 15-Jul-2024
 * Description of modifications: repurposed for LQN models
 */

/**
 * Modification notice:
 * Modified by: Yang Bao, Giuliano Casale, Lingxiao Du, Songtao Li, Zhuoyuan Li, Dan Luo, Zifeng Wang, Yelun Yang
 * Modification date: 15-Jul-2024
 * Description of modifications: repurposed for LQN models
 */

import jline.lang.layered.LayeredNetwork;
import jlqn.model.JLQNModel;
import jlqn.model.SetLayeredNetwork;
import jmt.framework.data.ArrayUtils;
import jmt.framework.gui.help.HoverHelp;
import jmt.framework.gui.table.editors.ButtonCellEditor;
import jmt.framework.gui.table.editors.ComboBoxCellEditor;
import jmt.framework.gui.wizard.WizardPanel;
import jmt.gui.common.CommonConstants;
import jmt.gui.common.JMTImageLoader;
import jmt.gui.table.*;

import jlqn.common.JLQNConstants;
import jlqn.gui.JLQNWizard;


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

/**
 * 1st panel: tasks number, name, scheduling, quantum, multiplicity, speed factor data
 * Replicas are not configurable, hard-set as 1
 */

public final class TasksPanel extends WizardPanel implements JLQNConstants, ForceUpdatablePanel {
    private static final long serialVersionUID = 1L;

    // Column numbers
    private final static int NUM_COL = 8;
    private final static int COL_VIEW = 0;
    private final static int COL_NAME = 1;
    private final static int COL_PROCESSOR = 2;
    private final static int COL_SCHEDULING = 3;
    private final static int COL_MULTIPLICITY = 4;
    private final static int COL_THINK_TIME_MEAN = 5;
    private final static int COL_PRIORITY = 6;
    private final static int COL_DELETE = 0;

    private HoverHelp help;
    private static final String helpText = "<html>In this panel you can define the number of tasks in the system and their properties.<br><br>"
            + " To edit values, double-click on the desired cell"
            + " and start typing.<br> To select tasks click or drag on the row headers.<br> <b>For a list of the available operations right-click"
            + " on the table</b>.<br>" + " Pressing DELETE removes all selected tasks from the system.</html>";

    private JLQNWizard jw;
    //private boolean isLd;
    private int numberOfTasks; // for task scheduling
    private String[] taskNames;
    private int[] taskScheduling;

    private String[] taskProcessor;
    private String[] TASK_PROCESSOR;

    //task data
    private int[] taskPriority; // integer >= 1
    private double[] taskThinkTimeMean; // real >= 0.0
    private int[] taskMultiplicity; // integer >= 1
    private int[] taskReplicas;  // integer >= 1


    private int nameCounter;
    private List<ListOp> taskOps;
    private boolean hasDeletes;
    private boolean deleting = false;
    private JSpinner taskSpinner = new JSpinner(new SpinnerNumberModel(1, 1, MAX_TASKS, 1));

    private TaskTable taskTable;
    private TableCellEditor taskSchedulingEditor;
    private TableCellEditor taskProcessorEditor;

    private ChangeListener spinnerListener = new ChangeListener() {
        public void stateChanged(ChangeEvent ce) {
            if (!deleting) {
                updateSizes();
            }
        }
    };

    private void deleteSelectedTasks() {
        int[] selectedRows = taskTable.getSelectedRows();
        int nrows = selectedRows.length;
        if (nrows == 0) {
            return;
        }
        int left = taskTable.getRowCount() - nrows;
        if (left < 1) {
            taskTable.removeRowSelectionInterval(selectedRows[nrows - 1], selectedRows[nrows - 1]);
            deleteSelectedTasks();
            return;
        }
        deleteTasks(selectedRows);
    }

    private AbstractAction viewEnsembleLayerWiz = new AbstractAction("") {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        {
            putValue(Action.SHORT_DESCRIPTION, "View");
            //putValue(Action.NAME, "View");
            putValue(Action.SMALL_ICON, JMTImageLoader.loadImage("toJSIM", new Dimension(30, 30)));
            //putValue(Action.SMALL_ICON, JMTImageLoader.loadImage("JSIMIcon", new Dimension(20,20)));
        }

        public void actionPerformed(ActionEvent e) {
        }
    };

    private AbstractAction deleteTask = new AbstractAction("Delete selected tasks") {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        {
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, false));
            putValue(Action.SHORT_DESCRIPTION, "Deletes selected tasks from the system");
        }

        public void actionPerformed(ActionEvent e) {
            deleteSelectedTasks();
        }
    };
    private AbstractAction deleteOneTask = new AbstractAction("") {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        {
            putValue(Action.SHORT_DESCRIPTION, "Delete This Task");
            putValue(Action.SMALL_ICON, JMTImageLoader.loadImage("Delete"));
        }

        public void actionPerformed(ActionEvent e) {
        }
    };
    private AbstractAction addTask = new AbstractAction("New Task") {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        {
            //putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.ALT_MASK));
            putValue(Action.SHORT_DESCRIPTION, "Adds a new Task to Model");
        }

        public void actionPerformed(ActionEvent e) {
            addTask();
        }
    };

    private void addTask() {
        setNumberOfTasks(numberOfTasks + 1);
    }

    private void deleteTask(int i) {
        numberOfTasks--;
        taskSpinner.setValue(new Integer(numberOfTasks));

        String deletedTaskName = taskNames[i];
        JLQNModel data = jw.getData();
        synchronized (data) {
            data.changeAllEntryTask(deletedTaskName, null);
            data.changeAllActivityTask(deletedTaskName, null);
        }

        taskNames = ArrayUtils.delete(taskNames, i);
        taskProcessor = ArrayUtils.delete(taskProcessor, i);
        taskScheduling = ArrayUtils.delete(taskScheduling, i);
        taskPriority = ArrayUtils.delete(taskPriority, i);
        taskThinkTimeMean = ArrayUtils.delete(taskThinkTimeMean, i);
        taskMultiplicity = ArrayUtils.delete(taskMultiplicity, i);
        taskReplicas = ArrayUtils.delete(taskReplicas, i);

        taskOps.add(ListOp.createDeleteOp(i));
        hasDeletes = true;
    }

    private void deleteTasks(int[] idx) {
        deleting = true;
        Arrays.sort(idx);
        for (int i = idx.length - 1; i >= 0; i--) {
            deleteTask(idx[i]);
        }
        updateSizes();
        deleting = false;
    }

    private class TaskTable extends ExactTable {

        /**
         *
         */
        private static final long serialVersionUID = 1L;
        TableCellRenderer disabledCellRenderer;
        TableCellEditor taskSchedulingCellEditor;
        TableCellEditor taskProcessorCellEditor;

        //BEGIN Federico Dall'Orso 8/3/2005
        ComboBoxCell taskSchedulingComboBoxCell;
        ComboBoxCell taskProcessorComboBoxCell;
        ButtonCellEditor deleteButtonCellRenderer;
        JButton deleteButton;

        ButtonCellEditor viewWizButtonCellRenderer;
        JButton viewWizButton;

        TaskTable() {
            super(new TaskTableModel());
            setName("TaskTable");

            disabledCellRenderer = new DisabledCellRenderer();

            taskSchedulingComboBoxCell = new ComboBoxCell(TASK_SCHEDULING_NAMES);
            taskProcessorComboBoxCell = new ComboBoxCell(TASK_PROCESSOR);

            deleteButton = new JButton(deleteOneTask);
            deleteButtonCellRenderer = new ButtonCellEditor(deleteButton);

            viewWizButton = new JButton(viewEnsembleLayerWiz);
            viewWizButtonCellRenderer = new ButtonCellEditor(viewWizButton);
            enableWizViews();
            enableDeletes();
            rowHeader.setRowHeight(CommonConstants.ROW_HEIGHT);
            setRowHeight(CommonConstants.ROW_HEIGHT);

            taskProcessorCellEditor = ComboBoxCellEditor.getEditorInstance(TASK_PROCESSOR);
            taskProcessorEditor = ComboBoxCellEditor.getEditorInstance(TASK_PROCESSOR);

            JComboBox<String> taskSchedulingBox = new JComboBox<String>(TASK_SCHEDULING_NAMES);
            taskSchedulingCellEditor = new DefaultCellEditor(taskSchedulingBox);
            taskSchedulingBox.setEditable(false);
            taskSchedulingEditor = new DefaultCellEditor(taskSchedulingBox);

            setRowSelectionAllowed(true);
            setColumnSelectionAllowed(false);

            // not beautiful, but effective. See ClassTableModel.getColumnClass()
            setDefaultRenderer(DisabledCellRenderer.class, disabledCellRenderer);
            setDefaultEditor(String.class, taskProcessorCellEditor);
            setDefaultEditor(String.class, taskSchedulingCellEditor);

            setDisplaysScrollLabels(true);

            //int[] selectedRows = taskTable.getSelectedRows();
            installKeyboardAction(getInputMap(), getActionMap(), deleteTask);
            mouseHandler = new ExactTable.MouseHandler(makeMouseMenu());
            mouseHandler.install();

            help.addHelp(this,
                    "Click or drag to select tasks; to edit data double-click and start typing. Right-click for a list of available operations");
            help.addHelp(moreRowsLabel, "There are more tasks: scroll down to see them");
            help.addHelp(selectAllButton, "Click to select all tasks");
            tableHeader.setToolTipText(null);
            rowHeader.setToolTipText(null);
            help.addHelp(rowHeader, "Click, SHIFT-click or drag to select tasks");

        }

        /**
         * Overridden to ensure proper handling of scheduling type column
         */
        @Override
        public TableCellEditor getCellEditor(int row, int column) {
            if (column == COL_SCHEDULING) {
                /* select the right editor */
                return taskSchedulingEditor;
            } else if (column == COL_PROCESSOR) {
                /* select the right editor */
                return taskProcessorEditor;
            } else {
                return super.getCellEditor(row, column);
            }
        }

        private void enableWizViews() {
            viewEnsembleLayerWiz.setEnabled(true);
            /*It seems the only way to implement row deletion...*/
            this.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if ((columnAtPoint(e.getPoint()) == COL_VIEW) && getRowCount() > 1) {
                        setRowSelectionInterval(rowAtPoint(e.getPoint()), rowAtPoint(e.getPoint()));
                        StringBuilder errors = new StringBuilder();
                        LayeredNetwork lqnmodel = SetLayeredNetwork.SetLayeredNetworkFromJLQN(jw.getData(), errors);
                        if (jw.getData().getViewerType() == ViewerType.WIZ) {
                            lqnmodel.getLayers().get(lqnmodel.getStruct().nhosts + rowAtPoint(e.getPoint())).jsimwView();
                        } else if (jw.getData().getViewerType() == ViewerType.GRAPH) {
                            lqnmodel.getLayers().get(lqnmodel.getStruct().nhosts + rowAtPoint(e.getPoint())).jsimgView();
                        }
                    }
                }
            });
            // getColumnModel().getColumn(getColumnCount() - 1).setMinWidth(30);
            // getColumnModel().getColumn(getColumnCount() - 1).setMaxWidth(30);
        }

        //BEGIN Federico Dall'Orso 14/3/2005
        /*enables deleting operations with last column's button*/
        private void enableDeletes() {
            deleteOneTask.setEnabled(numberOfTasks > 1);
            /*It seems the only way to implement row deletion...*/
            this.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if ((columnAtPoint(e.getPoint()) == COL_DELETE) && getRowCount() > 1) {
                        setRowSelectionInterval(rowAtPoint(e.getPoint()), rowAtPoint(e.getPoint()));
                        deleteSelectedTasks();
                    }
                }
            });
            getColumnModel().getColumn(COL_DELETE).setMinWidth(20);
            getColumnModel().getColumn(COL_DELETE).setMaxWidth(20);
        }

        @Override
        public TableCellRenderer getCellRenderer(int row, int column) {
            //if this is type column, i must render it as a combo box instead of a jtextfield
            if (column == COL_SCHEDULING) {
                return taskSchedulingComboBoxCell;
            } else if (column == COL_DELETE) {
                return deleteButtonCellRenderer;
            } else if (column == COL_VIEW) {
                return viewWizButtonCellRenderer;
            } else if (column == COL_PROCESSOR) {
                return taskProcessorComboBoxCell;
            } else {
                return disabledCellRenderer;
            }
        }

        //end Federico Dall'Orso 8/3/2005

        @Override
        protected void installKeyboard() {
        }


        @Override
        protected void installMouse() {
        }

        @Override
        protected JPopupMenu makeMouseMenu() {
            JPopupMenu menu = new JPopupMenu();
            menu.add(deleteTask);
            return menu;
        }

        /**
         * Make sure the table reflects changes on editing end
         * Overridden to truncate decimals in data if current class is closed
         */
//        @Override
//        public void editingStopped(ChangeEvent ce) {
////            if (taskScheduling[editingRow] == CLASS_CLOSED) {
////                classData[editingRow] = (int) classData[editingRow];
////            }
////            updateRow(editingRow);
////            super.editingStopped(ce);
//        }

        void updateViewCommand() {
            getColumnModel().getColumn(COL_VIEW).setMinWidth(30);
            getColumnModel().getColumn(COL_VIEW).setMaxWidth(30);
        }

        //BEGIN Federico Dall'Orso 14/3/2005
        //NEW
        //Updates appearence of last column's buttons
        void updateDeleteCommand() {
            deleteOneTask.setEnabled(numberOfTasks > 1);
            getColumnModel().getColumn(COL_DELETE).setMinWidth(20);
            getColumnModel().getColumn(COL_DELETE).setMaxWidth(20);

        }

        //END Federico Dall'Orso 14/3/2005

        @Override
        protected void updateActions() {
            boolean isEnabled = numberOfTasks > 1 && getSelectedRowCount() > 0;
            deleteTask.setEnabled(isEnabled);
            deleteOneTask.setEnabled(numberOfTasks > 1);
        }

        public void updateProcessorNames() {
            taskProcessorCellEditor = ComboBoxCellEditor.getEditorInstance(TASK_PROCESSOR);
            taskProcessorEditor = ComboBoxCellEditor.getEditorInstance(TASK_PROCESSOR);
            taskProcessorComboBoxCell = new ComboBoxCell(TASK_PROCESSOR);
        }
    }

    private class TaskTableModel extends ExactTableModel {

        /**
         *
         */
        private static final long serialVersionUID = 1L;
        private Object[] prototypes = {"10000", new String(new char[15]), "", "", new Integer(1000),
                new String(new char[15]), new String(new char[15]), new Integer(1000), new Integer(1000), ""};

        @Override
        public Object getPrototype(int columnIndex) {
            return prototypes[columnIndex + 1];
        }

        public int getRowCount() {
            return numberOfTasks;
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
                case COL_PROCESSOR:
                    return "Processor";
                case COL_SCHEDULING:
                    return "Scheduling";
                case COL_MULTIPLICITY:
                    return "Multiplicity";
                case COL_PRIORITY:
                    return "Priority";
                case COL_THINK_TIME_MEAN:
                    return "Think Time";
                default:
                    return null;
            }
        }

        @Override
        protected Object getValueAtImpl(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case COL_NAME:
                    return taskNames[rowIndex];
                case COL_SCHEDULING:
                    return TASK_SCHEDULING_NAMES[taskScheduling[rowIndex]];
                case COL_PROCESSOR:
                    return taskProcessor[rowIndex];
                case COL_MULTIPLICITY:
                    return taskMultiplicity[rowIndex];
                case COL_PRIORITY:
                    return taskPriority[rowIndex];
                case COL_THINK_TIME_MEAN:
                    return taskThinkTimeMean[rowIndex];
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
                            data.changeAllEntryTask(taskNames[rowIndex], newval);
                            data.changeAllActivityTask(taskNames[rowIndex], newval);
                        }
                        taskNames[rowIndex] = newval;
                    }
                    break;
                }
                case COL_SCHEDULING:
                    for (int i = 0; i < TASK_SCHEDULING_NAMES.length; i++) {
                        if (value == TASK_SCHEDULING_NAMES[i]) { //literal strings are canonicalized, hence == is ok
                            taskScheduling[rowIndex] = i;
                            break;
                        }
                    }
                    break;
                case COL_PROCESSOR: {
                    String newval = (String) value;
                    for (int i = 0; i < TASK_PROCESSOR.length; i++) {
                        boolean condition = (TASK_PROCESSOR[i] == null && newval == null)
                                || (TASK_PROCESSOR[i] != null && TASK_PROCESSOR[i].equalsIgnoreCase(newval));
                        if (condition) {
                            taskProcessor[rowIndex] = newval;
                            break;
                        }
                    }
                    break;
                }
                case COL_MULTIPLICITY: {
                    try {
                        int newval = (int) Double.parseDouble((String) value);
                        if (newval >= 0) {
                            taskMultiplicity[rowIndex] = newval;
                        }
                    } catch (NumberFormatException e) {
                    }
                    break;
                }
                case COL_PRIORITY: {
                    try {
                        int newval = (int) Double.parseDouble((String) value);
                        if (newval >= 0) {
                            taskPriority[rowIndex] = newval;
                        }
                    } catch (NumberFormatException e) {
                    }
                    break;
                }
                case COL_THINK_TIME_MEAN: {
                    try {
                        double newval = Double.parseDouble((String) value);
                        if (newval >= 0.0) {
                            taskThinkTimeMean[rowIndex] = newval;
                        }
                    } catch (NumberFormatException e) {
                    }
                    break;
                }
                default:
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case COL_NAME:
                case COL_SCHEDULING:
                case COL_PROCESSOR:
                case COL_MULTIPLICITY:
                case COL_PRIORITY:
                case COL_THINK_TIME_MEAN:
                    return true;
                default:
                    return false;
            }
        }
    }

    private void initComponents() {
        taskSpinner.addChangeListener(spinnerListener);
        help.addHelp(taskSpinner, "Enter the number of tasks for this system");

        taskTable = new TaskTable();

        /* and now some Box black magic */
        //DEK (Federico Granata) 26-09-2003
        Box taskSpinnerBox = Box.createHorizontalBox();
        //OLD
        //JLabel spinnerLabel = new JLabel("<html><font size=\"4\">Set the Number of classes (1-" + MAX_CLASSES + "):</font></html>");
        //NEW
        //@author Stefano
        JLabel spinnerLabel = new JLabel(DESCRIPTION_TASKS);

        taskSpinnerBox.add(spinnerLabel);
        //END
        //BEGIN Federico Dall'Orso 9/3/2005
        //OLD
		/*
		classSpinnerBox.add(Box.createGlue());
		 */
        //NEW
        taskSpinnerBox.add(Box.createHorizontalStrut(10));
        Box numberBox = Box.createVerticalBox();

        JPanel spinnerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel numberLabel = new JLabel("Number:");
        taskSpinner.setMaximumSize(new Dimension(600, 18));
        spinnerPanel.add(numberLabel);
        spinnerPanel.add(taskSpinner);
        numberBox.add(spinnerPanel);

        numberBox.add(new JButton(addTask));

        numberBox.setMaximumSize(new Dimension(300, 150));

        taskSpinnerBox.add(numberBox);
        //END  Federico Dall'Orso 9/3/2005

        Box taskBox = Box.createVerticalBox();
        taskBox.add(Box.createVerticalStrut(30));
        taskBox.add(taskSpinnerBox);
        taskBox.add(Box.createVerticalStrut(10));
        JScrollPane taskTablePane = new JScrollPane(taskTable);
        taskTablePane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        taskTablePane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        taskBox.add(taskTablePane);
        taskBox.add(Box.createRigidArea(new Dimension(10, 20)));

        Box totalBox = Box.createHorizontalBox();
        totalBox.add(Box.createHorizontalStrut(20));
        totalBox.add(taskBox);
        totalBox.add(Box.createHorizontalStrut(20));

        setLayout(new BorderLayout());
        add(totalBox, BorderLayout.CENTER);
    }

    private void sync() {
        hasDeletes = false;
        taskOps.clear();

        /* sync status with data object */
        /* arrays are copied to ensure data object consistency is preserved */
        JLQNModel data = jw.getData();
        synchronized (data) {
            numberOfTasks = data.getNumberOfTasks();
            nameCounter = numberOfTasks;
            //pop = data.getTotalPop();
            TASK_PROCESSOR = data.getProcessorNames();
            TASK_PROCESSOR = ArrayUtils.resize(TASK_PROCESSOR, TASK_PROCESSOR.length + 1, null);
            taskNames = ArrayUtils.copy(data.getTaskNames());
            taskScheduling = ArrayUtils.copy(data.getTaskScheduling());
            taskProcessor = ArrayUtils.copy(data.getTaskProcessor());
            taskThinkTimeMean = ArrayUtils.copy(data.getTaskThinkTimeMean());
            taskReplicas = ArrayUtils.copy(data.getTaskReplicas());
            taskPriority = ArrayUtils.copy(data.getTaskPriority());
            taskMultiplicity = ArrayUtils.copy(data.getTaskMultiplicity());

        }

        taskSpinner.setValue(new Integer(numberOfTasks));
        if (taskTable != null) {
            taskTable.updateProcessorNames();
        }
    }

    public TasksPanel(JLQNWizard jw) {
        this.jw = jw;
        help = jw.getHelp();
        taskOps = new ArrayList<ListOp>();

        sync();

        initComponents();
        makeNames();
    }

    private void updateSizes() {
        setNumberOfTasks(((Integer) taskSpinner.getValue()).intValue());
    }

    private void makeNames() {
        for (int i = 0; i < taskNames.length; i++) {
            if (taskNames[i] == null) {
                while (areThereDuplicates("T" + (nameCounter + 1), i, false)) {
                    nameCounter++;
                }
                taskNames[i] = "T" + (++nameCounter);
            }
        }
    }

    private void setNumberOfTasks(int number) {
        taskTable.stopEditing();
        numberOfTasks = number;

        taskNames = ArrayUtils.resize(taskNames, numberOfTasks, null);
        makeNames();

        taskScheduling = ArrayUtils.resize(taskScheduling, numberOfTasks, TASK_REF);
        taskProcessor = ArrayUtils.resize(taskProcessor, numberOfTasks, jw.getData().getProcessorNames()[0]);

        taskMultiplicity = ArrayUtils.resize(taskMultiplicity, numberOfTasks, 1);
        taskReplicas = ArrayUtils.resize(taskReplicas, numberOfTasks, 1);
        taskThinkTimeMean = ArrayUtils.resize(taskThinkTimeMean, numberOfTasks, 0.0);
        taskPriority = ArrayUtils.resize(taskPriority, numberOfTasks, 0);


        taskTable.updateStructure();
        if (!deleting) {
            taskOps.add(ListOp.createResizeOp(numberOfTasks));
        }

        taskSpinner.setValue(new Integer(numberOfTasks));
        taskTable.updateViewCommand();
        taskTable.updateDeleteCommand();
    }

    @Override
    public void lostFocus() {
        commit();
        //release();
    }

    @Override
    public void gotFocus() {
        sync();
        taskTable.update();
    }

    @Override
    public String getName() {
        return "Tasks";
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
                    "<html><center>Task names cannot be empty.<br>Please enter another name.</center></html>",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (value.equalsIgnoreCase("N/A")) {
            JOptionPane.showMessageDialog(this,
                    "<html><center>Task name is invalid.<br>Please enter another name.</center></html>",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private boolean areThereDuplicates(String value, int index, boolean enableWarning) {
        boolean thereAreDupl = false;
        for (int i = 0; i < taskNames.length && !thereAreDupl; i++) {
            if (i == index || taskNames[i] == null) continue;
            thereAreDupl = thereAreDupl || taskNames[i].equalsIgnoreCase(value);
        }
        if (thereAreDupl) {
            if (enableWarning) {
                JOptionPane.showMessageDialog(this,
                        "<html><center>There is a duplication in task names.<br>Please enter another name.</center></html>",
                        "Warning", JOptionPane.WARNING_MESSAGE);
                return thereAreDupl;
            }
        }
        return thereAreDupl;
    }

    private void commit() {
        /* stop any editing in progress */
        if (taskSpinner.getEditor().getComponent(0).hasFocus()) {
            //disgusting. there must be a better way...
            try {
                taskSpinner.commitEdit();
                updateSizes();
            } catch (java.text.ParseException e) {
            }
        }

        taskTable.stopEditing();

        JLQNModel data = jw.getData();
        synchronized (data) {

            if (hasDeletes) {
                playbackTaskOps(data); //play back ops on the data object
            } else {
                data.resizeTasks(numberOfTasks, false);
            }

            data.setTaskNames(taskNames);
            data.setTaskScheduling(taskScheduling);
            data.setTaskProcessor(taskProcessor);
            data.setTaskMultiplicity(taskMultiplicity);
            data.setTaskReplicas(taskReplicas);
            data.setTaskThinkTimeMean(taskThinkTimeMean);
            data.setTaskPriority(taskPriority);
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


    private void playbackTaskOps(JLQNModel data) {
        for (int i = 0; i < taskOps.size(); i++) {
            ListOp lo = taskOps.get(i);
            if (lo.isDeleteOp()) {
                data.deleteTask(lo.getData());
            }
            if (lo.isResizeOp()) {
                data.resizeTasks(lo.getData(), false);
            }
        }
    }

}