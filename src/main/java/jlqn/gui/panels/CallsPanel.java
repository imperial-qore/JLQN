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
import jlqn.model.JLQNModel;
import jmt.framework.data.ArrayUtils;
import jmt.framework.gui.help.HoverHelp;
import jmt.framework.gui.table.editors.ButtonCellEditor;
import jmt.framework.gui.table.editors.ComboBoxCellEditor;
import jmt.framework.gui.wizard.WizardPanel;
import jmt.gui.common.CommonConstants;
import jmt.gui.common.JMTImageLoader;
import jmt.gui.table.*;

import jlqn.gui.JLQNWizard;
import jmt.gui.table.ComboBoxCell;
import jmt.gui.table.DisabledCellRenderer;
import jmt.gui.table.ListOp;

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

 * @author
 * Date: 4-Feb-2023
 */

/**
 * 1st panel: calls number, name, arrivalcrate, priority data
 */

public final class CallsPanel extends WizardPanel implements JLQNConstants, jlqn.gui.panels.ForceUpdatablePanel {
    private static final long serialVersionUID = 1L;

    // Column numbers
    private final static int COL_NAME = 0;
    private final static int COL_ACTIVITY = 1;
    private final static int COL_ENTRY = 2;
    private final static int COL_TYPE = 3;
    private final static int COL_MEAN_CALLS = 4;
    private final static int COL_DELETE_BUTTON = 5;

    private HoverHelp help;
    private static final String helpText = "<html>In this panel you can define the number of calls in the system and their properties.<br><br>"
            + " To edit values, double-click on the desired cell"
            + " and start typing.<br> To select calls click or drag on the row headers.<br> <b>For a list of the available operations right-click"
            + " on the table</b>.<br>" + " Pressing DELETE removes all selected calls from the system.</html>";

    private JLQNWizard jw;
    //private boolean isLd;
    private int numberOfCalls;
    private String[] callNames;
    private String[] callActivity;
    private String[] CALL_ACTIVITY;
    private String[] callEntry;
    private String[] CALL_ENTRY;
    private int[] callType;

    //call data
    private double[] callMeanRepeatTimes;
    private int nameCounter;
    private List<ListOp> callOps;
    private boolean hasDeletes;
    private boolean deleting = false;
    private JSpinner callSpinner = new JSpinner(new SpinnerNumberModel(0, 0, MAX_CALLS, 1));

    private CallTable callTable;
    private TableCellEditor callActivityEditor;
    private TableCellEditor callEntryEditor;
    private TableCellEditor callTypeEditor;

    private ChangeListener spinnerListener = new ChangeListener() {
        public void stateChanged(ChangeEvent ce) {
            if (!deleting) {
                updateSizes();
            }
        }
    };

    private void deleteSelectedCalls() {
        int[] selectedRows = callTable.getSelectedRows();
        int nrows = selectedRows.length;
        if (nrows == 0) {
            return;
        }
        //int left = callTable.getRowCount() - nrows;
        //if (left < 1) {
        //    callTable.removeRowSelectionInterval(selectedRows[nrows - 1], selectedRows[nrows - 1]);
        //    deleteSelectedCalls();
        //    return;
        //}
        deleteCalls(selectedRows);
    }

    private AbstractAction deleteCall = new AbstractAction("Delete selected calls") {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        {
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, false));
            putValue(Action.SHORT_DESCRIPTION, "Deletes selected calls from the system");
        }

        public void actionPerformed(ActionEvent e) {
            deleteSelectedCalls();
        }
    };
    private AbstractAction deleteOneCall = new AbstractAction("") {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        {
            putValue(Action.SHORT_DESCRIPTION, "Delete This Call");
            putValue(Action.SMALL_ICON, JMTImageLoader.loadImage("Delete"));
        }

        public void actionPerformed(ActionEvent e) {
        }
    };
    private AbstractAction addCall = new AbstractAction("New Call") {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        {
            //putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.ALT_MASK));
            putValue(Action.SHORT_DESCRIPTION, "Adds a new Call to Model");
        }

        public void actionPerformed(ActionEvent e) {
            addCall();
        }
    };

    private void addCall() {
        setNumberOfCalls(numberOfCalls + 1);

    }

    private void deleteCall(int i) {
        numberOfCalls--;
        callSpinner.setValue(new Integer(numberOfCalls));

        callNames = ArrayUtils.delete(callNames, i);
        callActivity = ArrayUtils.delete(callActivity, i);
        callEntry = ArrayUtils.delete(callEntry, i);
        callType = ArrayUtils.delete(callType, i);
        callMeanRepeatTimes = ArrayUtils.delete(callMeanRepeatTimes, i);

        callOps.add(ListOp.createDeleteOp(i));
        hasDeletes = true;
    }

    private void deleteCalls(int[] idx) {
        deleting = true;
        Arrays.sort(idx);
        for (int i = idx.length - 1; i >= 0; i--) {
            deleteCall(idx[i]);
        }
        updateSizes();
        deleting = false;
//        updateAlgoPanel();
    }

    private class CallTable extends ExactTable {

        /**
         *
         */
        private static final long serialVersionUID = 1L;
        TableCellRenderer disabledCellRenderer;
        TableCellEditor callActivityCellEditor;
        ComboBoxCell callActivityComboBoxCell;
        TableCellEditor callEntryCellEditor;
        ComboBoxCell callEntryComboBoxCell;
        TableCellEditor callTypeCellEditor;
        ComboBoxCell callTypeComboBoxCell;
        ButtonCellEditor deleteButtonCellRenderer;
        JButton deleteButton;

        CallTable() {
            super(new CallTableModel());
            setName("CallTable");

            disabledCellRenderer = new DisabledCellRenderer();

            callActivityComboBoxCell = new ComboBoxCell(CALL_ACTIVITY);
            callEntryComboBoxCell = new ComboBoxCell(CALL_ENTRY);
            callTypeComboBoxCell = new ComboBoxCell(CALL_TYPES);

            deleteButton = new JButton(deleteOneCall);
            deleteButtonCellRenderer = new ButtonCellEditor(deleteButton);
            enableDeletes();
            rowHeader.setRowHeight(CommonConstants.ROW_HEIGHT);
            setRowHeight(CommonConstants.ROW_HEIGHT);

            callActivityCellEditor = ComboBoxCellEditor.getEditorInstance(CALL_ACTIVITY);
            callActivityEditor = ComboBoxCellEditor.getEditorInstance(CALL_ACTIVITY);

            callEntryCellEditor = ComboBoxCellEditor.getEditorInstance(CALL_ENTRY);
            callEntryEditor = ComboBoxCellEditor.getEditorInstance(CALL_ENTRY);

            callTypeCellEditor = ComboBoxCellEditor.getEditorInstance(CALL_TYPES);
            callTypeEditor = ComboBoxCellEditor.getEditorInstance(CALL_TYPES);

            setRowSelectionAllowed(true);
            setColumnSelectionAllowed(false);

            setDefaultRenderer(DisabledCellRenderer.class, disabledCellRenderer);
            setDefaultEditor(String.class, callActivityCellEditor);
            setDefaultEditor(String.class, callEntryCellEditor);
            setDefaultEditor(String.class, callTypeCellEditor);
            setDisplaysScrollLabels(true);

            //int[] selectedRows = callTable.getSelectedRows();
            installKeyboardAction(getInputMap(), getActionMap(), deleteCall);
            mouseHandler = new ExactTable.MouseHandler(makeMouseMenu());
            mouseHandler.install();

            help.addHelp(this,
                    "Click or drag to select calls; to edit data double-click and start typing. Right-click for a list of available operations");
            help.addHelp(moreRowsLabel, "There are more calls: scroll down to see them");
            help.addHelp(selectAllButton, "Click to select all calls");
            tableHeader.setToolTipText(null);
            rowHeader.setToolTipText(null);
            help.addHelp(rowHeader, "Click, SHIFT-click or drag to select calls");

        }

        /**
         * Overridden to ensure proper handling of scheduling type column
         */
        @Override
        public TableCellEditor getCellEditor(int row, int column) {
            if (column == COL_ACTIVITY) {
                /* select the right editor */
                return callActivityEditor;
            } else if (column == COL_ENTRY) {
                return callEntryEditor;
            } else if (column == COL_TYPE) {
                return callTypeEditor;
            } else {
                return super.getCellEditor(row, column);
            }
        }

        private void enableDeletes() {
            deleteOneCall.setEnabled(numberOfCalls > 0);
            /*It seems the only way to implement row deletion...*/
            this.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if ((columnAtPoint(e.getPoint()) == getColumnCount() - 1) && getRowCount() > 0) {
                        setRowSelectionInterval(rowAtPoint(e.getPoint()), rowAtPoint(e.getPoint()));
                        deleteSelectedCalls();
                    }
                }
            });
            getColumnModel().getColumn(getColumnCount() - 1).setMinWidth(20);
            getColumnModel().getColumn(getColumnCount() - 1).setMaxWidth(20);
        }

        @Override
        public TableCellRenderer getCellRenderer(int row, int column) {
            //if this is type column, i must render it as a combo box instead of a jtextfield
            if (column == COL_ACTIVITY) {
                return callActivityComboBoxCell;
            } else if (column == COL_ENTRY) {
                return callEntryComboBoxCell;
            } else if (column == COL_TYPE) {
                return callTypeComboBoxCell;
            } else if (column == COL_DELETE_BUTTON) {
                return deleteButtonCellRenderer;
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
            menu.add(deleteCall);
            return menu;
        }

        /**
         * Make sure the table reflects changes on editing end
         * Overridden to truncate decimals in data if current class is closed
         */
//        @Override
//        public void editingStopped(ChangeEvent ce) {
////            if (callScheduling[editingRow] == CLASS_CLOSED) {
////                classData[editingRow] = (int) classData[editingRow];
////            }
////            updateRow(editingRow);
////            super.editingStopped(ce);
//        }

        //BEGIN Federico Dall'Orso 14/3/2005
        //NEW
        //Updates appearence of last column's buttons
        void updateDeleteCommand() {
            deleteOneCall.setEnabled(numberOfCalls > 0);
            getColumnModel().getColumn(getColumnCount() - 1).setMinWidth(20);
            getColumnModel().getColumn(getColumnCount() - 1).setMaxWidth(20);
        }

        //END Federico Dall'Orso 14/3/2005

        @Override
        protected void updateActions() {
            boolean isEnabled = numberOfCalls > 0 && getSelectedRowCount() > 0;
            deleteCall.setEnabled(isEnabled);
            deleteOneCall.setEnabled(numberOfCalls > 0);
        }

        public void updateActivityNames() {
            callActivityCellEditor = ComboBoxCellEditor.getEditorInstance(CALL_ACTIVITY);
            callActivityEditor = ComboBoxCellEditor.getEditorInstance(CALL_ACTIVITY);
            callActivityComboBoxCell = new ComboBoxCell(CALL_ACTIVITY);
        }

        public void updateEntryNames() {
            callEntryCellEditor = ComboBoxCellEditor.getEditorInstance(CALL_ENTRY);
            callEntryEditor = ComboBoxCellEditor.getEditorInstance(CALL_ENTRY);
            callEntryComboBoxCell = new ComboBoxCell(CALL_ENTRY);
        }

    }

    private class CallTableModel extends ExactTableModel {
        /**
         *
         */
        private static final long serialVersionUID = 1L;
        private Object[] prototypes = { "10000", new String(new char[15]), "", "", "", new String(new char[15]), "" };
        @Override
        public Object getPrototype(int columnIndex) {
            return prototypes[columnIndex + 1];
        }

        public int getRowCount() {
            return numberOfCalls;
        }

//        @Override
//        @SuppressWarnings({"unchecked", "rawtypes"})
//        public Class getColumnClass(int col) {
//            switch (col) {
//                case COL_ACTIVITY_NAME:
//                    return String.class;
//                case COL_MeanRepeatTimes:
//                    return Integer.class;
//                case COL_MULTIPLICITY:
//                case COL_REPLICAS:
//                    return DisabledCellRenderer.class;
//                case COL_PRIORITY:
//                default:
//                    return Object.class;
//            }
//        }

        public int getColumnCount() {
            return 6;
        }

        @Override
        public String getColumnName(int index) {
            switch (index) {
                case COL_NAME:
                    return "Name";
                case COL_ACTIVITY:
                    return "Activity";
                case COL_ENTRY:
                    return "Target Entry";
                case COL_TYPE:
                    return "Call Type";
                case COL_MEAN_CALLS:
                    return "Mean Number of Calls";
                default:
                    return null;
            }
        }

        @Override
        protected Object getValueAtImpl(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case COL_NAME:
                    return callNames[rowIndex];
                case COL_ACTIVITY:
                    return callActivity[rowIndex];
                case COL_ENTRY:
                    return callEntry[rowIndex];
                case COL_TYPE:
                    return CALL_TYPES[callType[rowIndex]];
                case COL_MEAN_CALLS:
                    return callMeanRepeatTimes[rowIndex];

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
                        callNames[rowIndex] = newval;
                    }
                    break;
                }
                case COL_ACTIVITY: {
                    String newval = (String) value;
                    for (int i = 0; i < CALL_ACTIVITY.length; i++) {
                        boolean condition = (newval == null)
                                || (CALL_ACTIVITY[i] != null && CALL_ACTIVITY[i].equalsIgnoreCase(newval));
                        if (condition) {
                            callActivity[rowIndex] = newval;
                            break;
                        }
                    }
                    break;
                }
                case COL_ENTRY: {
                    String newval = (String) value;
                    for (int i = 0; i < CALL_ENTRY.length; i++) {
                        boolean condition = (newval == null)
                                || (CALL_ENTRY[i] != null && CALL_ENTRY[i].equalsIgnoreCase(newval));
                        if (condition) {
                            callEntry[rowIndex] = newval;
                            break;
                        }
                    }
                    break;
                }
                case COL_TYPE: {
                    for (int i = 0; i < CALL_TYPES.length; i++) {
                        if (value == CALL_TYPES[i]) {
                            callType[rowIndex] = i;
                            break;
                        }
                    }
                    break;
                }
                case COL_MEAN_CALLS:{
                    try {
                        double newval = Double.parseDouble((String) value);
                        if (newval >= 0.0) {
                            callMeanRepeatTimes[rowIndex] = newval;
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
                case COL_ACTIVITY:
                case COL_ENTRY:
                case COL_TYPE:
                case COL_MEAN_CALLS:
                    return true;
                default:
                    return false;
            }
        }
    }

    private void initComponents() {
        callSpinner.addChangeListener(spinnerListener);
        help.addHelp(callSpinner, "Enter the number of calls for this system");

        callTable = new CallTable();

        Box callSpinnerBox = Box.createHorizontalBox();

        JLabel spinnerLabel = new JLabel(DESCRIPTION_CALLS);

        callSpinnerBox.add(spinnerLabel);

        callSpinnerBox.add(Box.createHorizontalStrut(10));
        Box numberBox = Box.createVerticalBox();

        JPanel spinnerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel numberLabel = new JLabel("Number:");
        callSpinner.setMaximumSize(new Dimension(600, 18));
        spinnerPanel.add(numberLabel);
        spinnerPanel.add(callSpinner);
        numberBox.add(spinnerPanel);

        numberBox.add(new JButton(addCall));

        numberBox.setMaximumSize(new Dimension(300, 150));

        callSpinnerBox.add(numberBox);
        //END  Federico Dall'Orso 9/3/2005

        Box callBox = Box.createVerticalBox();
        callBox.add(Box.createVerticalStrut(30));
        callBox.add(callSpinnerBox);
        callBox.add(Box.createVerticalStrut(10));
        JScrollPane callTablePane = new JScrollPane(callTable);
        callTablePane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        callTablePane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        callBox.add(callTablePane);
        callBox.add(Box.createRigidArea(new Dimension(10, 20)));

        Box totalBox = Box.createHorizontalBox();
        totalBox.add(Box.createHorizontalStrut(20));
        totalBox.add(callBox);
        totalBox.add(Box.createHorizontalStrut(20));

        setLayout(new BorderLayout());
        add(totalBox, BorderLayout.CENTER);
    }

    private void sync() {
        hasDeletes = false;
        callOps.clear();

        /* sync status with data object */
        /* arrays are copied to ensure data object consistency is preserved */
        JLQNModel data = jw.getData();
        synchronized (data) {
            numberOfCalls = data.getNumberOfCalls();
            nameCounter = numberOfCalls;

            CALL_ACTIVITY = data.getActivityNames();
            CALL_ACTIVITY = ArrayUtils.resize(CALL_ACTIVITY, CALL_ACTIVITY.length+1, null);

            CALL_ENTRY = data.getEntryNames();
            CALL_ENTRY = ArrayUtils.resize(CALL_ENTRY,
                    CALL_ENTRY.length+1, null);

            callNames = ArrayUtils.copy(data.getCallNames());
            callActivity = ArrayUtils.copy(data.getCallActivity());
            callEntry = ArrayUtils.copy(data.getCallEntry());
            callType = ArrayUtils.copy(data.getCallType());
            callMeanRepeatTimes = ArrayUtils.copy(data.getCallMeanRepeatTimes());
        }

        callSpinner.setValue(new Integer(numberOfCalls));
        if (callTable != null) {
            callTable.updateActivityNames();
            callTable.updateEntryNames();
        }
    }

    public CallsPanel(JLQNWizard jw) {
        this.jw = jw;
        help = jw.getHelp();
        callOps = new ArrayList<ListOp>();

        sync();

        initComponents();
        makeNames();
    }

    private void updateSizes() {
        setNumberOfCalls(((Integer) callSpinner.getValue()).intValue());
    }

    private void makeNames() {
        for (int i = 0; i < callNames.length; i++) {
            if (callNames[i] == null) {
                while (areThereDuplicates("C" + (nameCounter+1), i, false)) {
                    nameCounter++;
                }
                callNames[i] = "C" + (++nameCounter);
            }
        }
    }

    private void setNumberOfCalls(int number) {
        callTable.stopEditing();
        numberOfCalls = number;

        callNames = ArrayUtils.resize(callNames, numberOfCalls, null);
        makeNames();

        callMeanRepeatTimes = ArrayUtils.resize(callMeanRepeatTimes, numberOfCalls, 0.0);

        callActivity = ArrayUtils.resize(callActivity, numberOfCalls, jw.getData().getActivityNames()[0]);
        String callActivityTask = jw.getData().getActivityTask()[0];
        String foundEntry = null;
        for (int e = 0; e < jw.getData().getEntryNames().length; e++) {
            if (foundEntry != null) {
                break;
            }
            if (jw.getData().getEntryTask()[e] != callActivityTask) {
                foundEntry = jw.getData().getEntryNames()[e];
            }
        }
        callEntry = ArrayUtils.resize(callEntry, numberOfCalls, foundEntry);
        callType = ArrayUtils.resize(callType, numberOfCalls, 0);

        callTable.updateStructure();
        if (!deleting) {
            callOps.add(ListOp.createResizeOp(numberOfCalls));
        }

        callSpinner.setValue(new Integer(numberOfCalls));
        callTable.updateDeleteCommand();
        //updateAlgoPanel();
    }


    /**
     * Updates the algorithm panel based on the current classes.
     */

    @Override
    public void lostFocus() {
        commit();
        //release();
    }

    @Override
    public void gotFocus() {
        sync();
        callTable.update();
    }

    @Override
    public String getName() {
        return "Calls";
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
                    "<html><center>Call names cannot be empty.<br>Please enter another name.</center></html>",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (value.equalsIgnoreCase("N/A")) {
            JOptionPane.showMessageDialog(this,
                    "<html><center>Call name is invalid.<br>Please enter another name.</center></html>",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }
    private boolean areThereDuplicates(String value, int index, boolean enableWarning) {
        boolean thereAreDupl = false;
        for (int i = 0; i < callNames.length && !thereAreDupl; i++) {
            if (i == index || callNames[i] == null) continue;
            thereAreDupl = thereAreDupl || callNames[i].equalsIgnoreCase(value);
        }
        if (thereAreDupl) {
            if (enableWarning) {
                JOptionPane.showMessageDialog(this,
                        "<html><center>There is a duplication in call names.<br>Please enter another name.</center></html>",
                        "Warning", JOptionPane.WARNING_MESSAGE);
                return thereAreDupl;
            }
        }
        return thereAreDupl;
    }

    private void commit() {
        /* stop any editing in progress */
        if (callSpinner.getEditor().getComponent(0).hasFocus()) {
            //disgusting. there must be a better way...
            try {
                callSpinner.commitEdit();
                updateSizes();
            } catch (java.text.ParseException e) {
            }
        }

        callTable.stopEditing();

        JLQNModel data = jw.getData();
        synchronized (data) {
            if (hasDeletes) {
                playbackCallOps(data); //play back ops on the data object
            } else {
                data.resizeCalls(numberOfCalls, false);
            }
            data.setCallNames(callNames);
            data.setCallActivity(callActivity);
            data.setCallEntry(callEntry);
            data.setCallType(callType);
            data.setCallMeanRepeatTimes(callMeanRepeatTimes);
            sync();
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

    private void playbackCallOps(JLQNModel data) {
        for (int i = 0; i < callOps.size(); i++) {
            ListOp lo = callOps.get(i);
            if (lo.isDeleteOp()) {
                data.deleteCall(lo.getData());
            }
            if (lo.isResizeOp()) {
                data.resizeCalls(lo.getData(), false);
            }
        }
    }}