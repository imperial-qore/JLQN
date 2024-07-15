package jlqn.gui.exact.panels;

import jlqn.analytical.JLQNModel;
import jmt.framework.data.ArrayUtils;
import jmt.framework.gui.help.HoverHelp;
import jmt.framework.gui.table.editors.ButtonCellEditor;
import jmt.framework.gui.table.editors.ComboBoxCellEditor;
import jmt.framework.gui.wizard.WizardPanel;
import jmt.gui.common.CommonConstants;
import jmt.gui.common.JMTImageLoader;
import jmt.gui.exact.table.*;

import jlqn.analytical.JLQNConstants;
import jlqn.gui.exact.JLQNWizard;

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
 * 1st panel: entries number, name, arrivalcrate, priority data
 */

public final class EntriesPanel extends WizardPanel implements JLQNConstants, ForceUpdatablePanel {
    private static final long serialVersionUID = 1L;

    // Column numbers
    private final static int COL_NAME = 0;
    private final static int COL_TASK = 1;
    private final static int COL_BOUND_TO_ACTIVITY = 2;
    private final static int COL_REPLY_TO_ACTIVITY = 3;
    private final static int COL_ARRIVAL_RATE = 4;
    private final static int COL_PRIORITY = 5;
    private final static int COL_DELETE_BUTTON = 6;

    private HoverHelp help;
    private static final String helpText = "<html>In this panel you can define the number of entries in the system and their properties.<br><br>"
            + " To edit values, double-click on the desired cell"
            + " and start typing.<br> To select entries click or drag on the row headers.<br> <b>For a list of the available operations right-click"
            + " on the table</b>.<br>" + " Pressing DELETE removes all selected entries from the system.</html>";

    private JLQNWizard jw;
    //private boolean isLd;
    private int numberOfEntries;
    private String[] entryNames;
    private String[] entryTask;
    private String[] ENTRY_TASK;
    private String[] entryBoundToActivity;
    private String[] ENTRY_BOUND_TO_ACTIVITY;
    private String[] entryReplyToActivity;
    private String[] ENTRY_REPLY_TO_ACTIVITY;
    private String[] ENTRY_REPLY_TO_ACTIVITY_WITH_NA;

    //entry data
    private double[] entryArrivalRate;
    private int[] entryPriority;
    private int nameCounter;
    private List<ListOp> entryOps;
    private boolean hasDeletes;
    private boolean deleting = false;
    private JSpinner entrySpinner = new JSpinner(new SpinnerNumberModel(1, 1, MAX_ENTRIES, 1));

    private EntryTable entryTable;
    private TableCellEditor entryTaskEditor;
    private TableCellEditor entryBoundToActivityEditor;
    private TableCellEditor entryReplyToActivityEditor;

    private ChangeListener spinnerListener = new ChangeListener() {
        public void stateChanged(ChangeEvent ce) {
            if (!deleting) {
                updateSizes();
            }
        }
    };

    private void deleteSelectedEntries() {
        int[] selectedRows = entryTable.getSelectedRows();
        int nrows = selectedRows.length;
        if (nrows == 0) {
            return;
        }
        int left = entryTable.getRowCount() - nrows;
        if (left < 1) {
            entryTable.removeRowSelectionInterval(selectedRows[nrows - 1], selectedRows[nrows - 1]);
            deleteSelectedEntries();
            return;
        }
        deleteEntries(selectedRows);
    }

    private AbstractAction deleteEntry = new AbstractAction("Delete selected entries") {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        {
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, false));
            putValue(Action.SHORT_DESCRIPTION, "Deletes selected entries from the system");
        }

        public void actionPerformed(ActionEvent e) {
            deleteSelectedEntries();
        }
    };
    private AbstractAction deleteOneEntry = new AbstractAction("") {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        {
            putValue(Action.SHORT_DESCRIPTION, "Delete This Entry");
            putValue(Action.SMALL_ICON, JMTImageLoader.loadImage("Delete"));
        }

        public void actionPerformed(ActionEvent e) {
        }
    };
    private AbstractAction addEntry = new AbstractAction("New Entry") {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        {
            //putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.ALT_MASK));
            putValue(Action.SHORT_DESCRIPTION, "Adds a new Entry to Model");
        }

        public void actionPerformed(ActionEvent e) {
            addEntry();
        }
    };

    private void addEntry() {
        setNumberOfEntries(numberOfEntries + 1);

    }

    private void deleteEntry(int i) {
        numberOfEntries--;
        entrySpinner.setValue(new Integer(numberOfEntries));

        String deletedEntryName = entryNames[i];
        JLQNModel data = jw.getData();
        synchronized (data) {
            data.changeAllCallEntry(deletedEntryName, null);
            data.changeAllActivityTask(deletedEntryName, null);
        }

        entryNames = ArrayUtils.delete(entryNames, i);
        entryTask = ArrayUtils.delete(entryTask, i);
        entryBoundToActivity = ArrayUtils.delete(entryBoundToActivity, i);
        entryReplyToActivity = ArrayUtils.delete(entryReplyToActivity, i);
        entryArrivalRate = ArrayUtils.delete(entryArrivalRate, i);
        entryPriority = ArrayUtils.delete(entryPriority, i);

        entryOps.add(ListOp.createDeleteOp(i));
        hasDeletes = true;
    }

    private void deleteEntries(int[] idx) {
        deleting = true;
        Arrays.sort(idx);
        for (int i = idx.length - 1; i >= 0; i--) {
            deleteEntry(idx[i]);
        }
        updateSizes();
        deleting = false;
//        updateAlgoPanel();
    }

    private class EntryTable extends ExactTable {

        /**
         *
         */
        private static final long serialVersionUID = 1L;
        TableCellRenderer disabledCellRenderer;
        TableCellEditor entryTaskCellEditor;
        ComboBoxCell entryTaskComboBoxCell;
        TableCellEditor entryBoundToActivityCellEditor;
        ComboBoxCell entryBoundToActivityComboBoxCell;
        TableCellEditor entryReplyToActivityCellEditor;
        ComboBoxCell entryReplyToActivityComboBoxCell;
        ButtonCellEditor deleteButtonCellRenderer;
        JButton deleteButton;

        EntryTable() {
            super(new EntryTableModel());
            setName("EntryTable");

            disabledCellRenderer = new DisabledCellRenderer();

            entryTaskComboBoxCell = new ComboBoxCell(ENTRY_TASK);
            entryBoundToActivityComboBoxCell = new ComboBoxCell(ENTRY_BOUND_TO_ACTIVITY);
            entryReplyToActivityComboBoxCell = new ComboBoxCell(ENTRY_REPLY_TO_ACTIVITY_WITH_NA);

            deleteButton = new JButton(deleteOneEntry);
            deleteButtonCellRenderer = new ButtonCellEditor(deleteButton);
            enableDeletes();
            rowHeader.setRowHeight(CommonConstants.ROW_HEIGHT);
            setRowHeight(CommonConstants.ROW_HEIGHT);

            entryTaskCellEditor = ComboBoxCellEditor.getEditorInstance(ENTRY_TASK);
            entryTaskEditor = ComboBoxCellEditor.getEditorInstance(ENTRY_TASK);

            entryBoundToActivityCellEditor = ComboBoxCellEditor.getEditorInstance(ENTRY_BOUND_TO_ACTIVITY);
            entryBoundToActivityEditor = ComboBoxCellEditor.getEditorInstance(ENTRY_BOUND_TO_ACTIVITY);

            entryReplyToActivityCellEditor = ComboBoxCellEditor.getEditorInstance(ENTRY_REPLY_TO_ACTIVITY);
            entryReplyToActivityEditor = ComboBoxCellEditor.getEditorInstance(ENTRY_REPLY_TO_ACTIVITY);

            setRowSelectionAllowed(true);
            setColumnSelectionAllowed(false);
            
            setDefaultRenderer(DisabledCellRenderer.class, disabledCellRenderer);
            setDefaultEditor(String.class, entryTaskCellEditor);
            setDefaultEditor(String.class, entryBoundToActivityCellEditor);
            setDefaultEditor(String.class, entryReplyToActivityCellEditor);
            setDisplaysScrollLabels(true);

            //int[] selectedRows = entryTable.getSelectedRows();
            installKeyboardAction(getInputMap(), getActionMap(), deleteEntry);
            mouseHandler = new ExactTable.MouseHandler(makeMouseMenu());
            mouseHandler.install();

            help.addHelp(this,
                    "Click or drag to select entries; to edit data double-click and start typing. Right-click for a list of available operations");
            help.addHelp(moreRowsLabel, "There are more entries: scroll down to see them");
            help.addHelp(selectAllButton, "Click to select all entries");
            tableHeader.setToolTipText(null);
            rowHeader.setToolTipText(null);
            help.addHelp(rowHeader, "Click, SHIFT-click or drag to select entries");

        }

        /**
         * Overridden to ensure proper handling of scheduling type column
         */
        @Override
        public TableCellEditor getCellEditor(int row, int column) {
            if (column == COL_TASK) {
                /* select the right editor */
                return entryTaskEditor;
            } else if (column == COL_BOUND_TO_ACTIVITY) {
                return entryBoundToActivityEditor;
            } else if (column == COL_REPLY_TO_ACTIVITY) {
                return entryReplyToActivityEditor;
            } else {
                return super.getCellEditor(row, column);
            }
        }

        private void enableDeletes() {
            deleteOneEntry.setEnabled(numberOfEntries > 1);
            /*It seems the only way to implement row deletion...*/
            this.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if ((columnAtPoint(e.getPoint()) == getColumnCount() - 1) && getRowCount() > 1) {
                        setRowSelectionInterval(rowAtPoint(e.getPoint()), rowAtPoint(e.getPoint()));
                        deleteSelectedEntries();
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
                return entryTaskComboBoxCell;
            } else if (column == COL_BOUND_TO_ACTIVITY) {
//                return new ComboBoxCell(jw.getData().getActivityChildrenFromTaskParent(entryTask[row]));
                return entryBoundToActivityComboBoxCell;
            } else if (column == COL_REPLY_TO_ACTIVITY) {
//                String[] replyToActivities = jw.getData().getActivityChildrenFromTaskParent(entryTask[row]);
//                ArrayUtils.resize(replyToActivities,
//                        replyToActivities.length+1, "N/A");
//                return new ComboBoxCell(replyToActivities);
                return entryReplyToActivityComboBoxCell;
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
            menu.add(deleteEntry);
            return menu;
        }

        /**
         * Make sure the table reflects changes on editing end
         * Overridden to truncate decimals in data if current class is closed
         */
//        @Override
//        public void editingStopped(ChangeEvent ce) {
////            if (entryScheduling[editingRow] == CLASS_CLOSED) {
////                classData[editingRow] = (int) classData[editingRow];
////            }
////            updateRow(editingRow);
////            super.editingStopped(ce);
//        }

        //BEGIN Federico Dall'Orso 14/3/2005
        //NEW
        //Updates appearence of last column's buttons
        void updateDeleteCommand() {
            deleteOneEntry.setEnabled(numberOfEntries > 1);
            getColumnModel().getColumn(getColumnCount() - 1).setMinWidth(20);
            getColumnModel().getColumn(getColumnCount() - 1).setMaxWidth(20);
        }

        //END Federico Dall'Orso 14/3/2005

        @Override
        protected void updateActions() {
            boolean isEnabled = numberOfEntries > 1 && getSelectedRowCount() > 0;
            deleteEntry.setEnabled(isEnabled);
            deleteOneEntry.setEnabled(numberOfEntries > 1);
        }

        public void updateTaskNames() {
            entryTaskCellEditor = ComboBoxCellEditor.getEditorInstance(ENTRY_TASK);
            entryTaskEditor = ComboBoxCellEditor.getEditorInstance(ENTRY_TASK);
            entryTaskComboBoxCell = new ComboBoxCell(ENTRY_TASK);
        }

        public void updateActivityNames() {
            entryBoundToActivityCellEditor = ComboBoxCellEditor.getEditorInstance(ENTRY_BOUND_TO_ACTIVITY);
            entryBoundToActivityEditor = ComboBoxCellEditor.getEditorInstance(ENTRY_BOUND_TO_ACTIVITY);
            entryBoundToActivityComboBoxCell = new ComboBoxCell(ENTRY_BOUND_TO_ACTIVITY);

            entryReplyToActivityCellEditor = ComboBoxCellEditor.getEditorInstance(ENTRY_REPLY_TO_ACTIVITY);
            entryReplyToActivityEditor = ComboBoxCellEditor.getEditorInstance(ENTRY_REPLY_TO_ACTIVITY);
            entryReplyToActivityComboBoxCell = new ComboBoxCell(ENTRY_REPLY_TO_ACTIVITY_WITH_NA);
        }

    }

    private class EntryTableModel extends ExactTableModel {
        /**
         *
         */
        private static final long serialVersionUID = 1L;
        private Object[] prototypes = { "10000", new String(new char[15]), "", "", "", new String(new char[15]), new String(new char[15]), "" };
        @Override
        public Object getPrototype(int columnIndex) {
            return prototypes[columnIndex + 1];
        }

        public int getRowCount() {
            return numberOfEntries;
        }

//        @Override
//        @SuppressWarnings({"unchecked", "rawtypes"})
//        public Class getColumnClass(int col) {
//            switch (col) {
//                case COL_TASK_NAME:
//                    return String.class;
//                case COL_ARRIVALRATE:
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
            return 7;
        }

        @Override
        public String getColumnName(int index) {
            switch (index) {
                case COL_NAME:
                    return "Name";
                case COL_TASK:
                    return "Task";
                case COL_BOUND_TO_ACTIVITY:
                    return "BoundTo Activity";
                case COL_REPLY_TO_ACTIVITY:
                    return "ReplyTo Activity";
                case COL_ARRIVAL_RATE:
                    return "Arrival Rate";
                case COL_PRIORITY:
                    return "Priority";
                default:
                    return null;
            }
        }

        @Override
        protected Object getValueAtImpl(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case COL_NAME:
                    return entryNames[rowIndex];
                case COL_TASK:
                    return entryTask[rowIndex];
                case COL_BOUND_TO_ACTIVITY:
                    return entryBoundToActivity[rowIndex];
                case COL_REPLY_TO_ACTIVITY:
                    return entryReplyToActivity[rowIndex];
                case COL_ARRIVAL_RATE:
                    return entryArrivalRate[rowIndex];
                case COL_PRIORITY:
                    return entryPriority[rowIndex];

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
                            data.changeAllCallEntry(entryNames[rowIndex], newval);
                        }
                        entryNames[rowIndex] = newval;
                    }
                    break;
                }
                case COL_TASK: {
                    String newval = (String) value;
                    for (int i = 0; i < ENTRY_TASK.length; i++) {
                        boolean condition = (newval == null)
                                || (ENTRY_TASK[i] != null && ENTRY_TASK[i].equalsIgnoreCase(newval));
                        if (condition) {
                            entryTask[rowIndex] = newval;
                            JLQNModel data = jw.getData();
                            synchronized (data) {
                                if (data.isReferenceTask(newval)) {
                                    setValueAt("N/A", rowIndex, COL_REPLY_TO_ACTIVITY);
                                } else {
                                    if ("N/A".equalsIgnoreCase(entryReplyToActivity[rowIndex])) {
                                        setValueAt(null, rowIndex, COL_REPLY_TO_ACTIVITY);
                                    }
                                }
                            }
                            entryTable.repaint();
                            break;
                        }
                    }
                    break;
                }
                case COL_BOUND_TO_ACTIVITY: {
                    String newval = (String) value;
                    for (int i = 0; i < ENTRY_BOUND_TO_ACTIVITY.length; i++) {
                        boolean condition = (newval == null)
                                || (ENTRY_BOUND_TO_ACTIVITY[i] != null && ENTRY_BOUND_TO_ACTIVITY[i].equalsIgnoreCase(newval));
                        if (condition) {
                            entryBoundToActivity[rowIndex] = newval;
                            break;
                        }
                    }
                    break;
                }
                case COL_REPLY_TO_ACTIVITY: {
                    String newval = (String) value;
                    for (int i = 0; i < ENTRY_REPLY_TO_ACTIVITY_WITH_NA.length; i++) {

                        boolean condition = (newval == null)
                                || (ENTRY_REPLY_TO_ACTIVITY_WITH_NA[i] != null &&
                                ENTRY_REPLY_TO_ACTIVITY_WITH_NA[i].equalsIgnoreCase(newval));
                        if (condition) {
                            entryReplyToActivity[rowIndex] = newval;
                            break;
                        }
                    }
                    break;
                }
                case COL_ARRIVAL_RATE:{
                    try {
                        double newval = Double.parseDouble((String) value);
                        if (newval >= 0.0) {
                            entryArrivalRate[rowIndex] = newval;
                        }
                    } catch (NumberFormatException e) {
                    }
                    break;
                }
                
                case COL_PRIORITY: {
                    try {
                        int newval = Integer.parseInt((String) value);
                        if (newval >= 0.0) {
                            entryPriority[rowIndex] = newval;
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
                case COL_TASK:
                case COL_BOUND_TO_ACTIVITY:
                    return true;
                case COL_REPLY_TO_ACTIVITY:
                    return !isOnReferenceTask(rowIndex);
                case COL_ARRIVAL_RATE:
                    return true;
                case COL_PRIORITY:
                    return true;
                default:
                    return false;
            }
        }
    }

    private void initComponents() {
        entrySpinner.addChangeListener(spinnerListener);
        help.addHelp(entrySpinner, "Enter the number of entries for this system");

        entryTable = new EntryTable();

        Box entrySpinnerBox = Box.createHorizontalBox();
       
        JLabel spinnerLabel = new JLabel(DESCRIPTION_ENTRIES);

        entrySpinnerBox.add(spinnerLabel);

        entrySpinnerBox.add(Box.createHorizontalStrut(10));
        Box numberBox = Box.createVerticalBox();

        JPanel spinnerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel numberLabel = new JLabel("Number:");
        entrySpinner.setMaximumSize(new Dimension(600, 18));
        spinnerPanel.add(numberLabel);
        spinnerPanel.add(entrySpinner);
        numberBox.add(spinnerPanel);

        numberBox.add(new JButton(addEntry));

        numberBox.setMaximumSize(new Dimension(300, 150));

        entrySpinnerBox.add(numberBox);
        //END  Federico Dall'Orso 9/3/2005

        Box entryBox = Box.createVerticalBox();
        entryBox.add(Box.createVerticalStrut(30));
        entryBox.add(entrySpinnerBox);
        entryBox.add(Box.createVerticalStrut(10));
        JScrollPane entryTablePane = new JScrollPane(entryTable);
        entryTablePane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        entryTablePane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        entryBox.add(entryTablePane);
        entryBox.add(Box.createRigidArea(new Dimension(10, 20)));

        Box totalBox = Box.createHorizontalBox();
        totalBox.add(Box.createHorizontalStrut(20));
        totalBox.add(entryBox);
        totalBox.add(Box.createHorizontalStrut(20));

        setLayout(new BorderLayout());
        add(totalBox, BorderLayout.CENTER);
    }

    private void sync() {
        hasDeletes = false;
        entryOps.clear();

        /* sync status with data object */
        /* arrays are copied to ensure data object consistency is preserved */
        JLQNModel data = jw.getData();
        synchronized (data) {
            data.updateEntryReplyToActivityBeforeSync();
            numberOfEntries = data.getNumberOfEntries();
            nameCounter = numberOfEntries;

            ENTRY_TASK = data.getTaskNames();
            ENTRY_TASK = ArrayUtils.resize(ENTRY_TASK, ENTRY_TASK.length+1, null);

            ENTRY_BOUND_TO_ACTIVITY = data.getActivityNames();
            ENTRY_BOUND_TO_ACTIVITY = ArrayUtils.resize(ENTRY_BOUND_TO_ACTIVITY,
                    ENTRY_BOUND_TO_ACTIVITY.length+1, null);

            ENTRY_REPLY_TO_ACTIVITY = data.getActivityNames();

            for (int i = ENTRY_REPLY_TO_ACTIVITY.length-1; i >= 0; i--) {
                if (data.isOnReferenceTask(ENTRY_REPLY_TO_ACTIVITY[i])) {
                    ENTRY_REPLY_TO_ACTIVITY = ArrayUtils.delete(ENTRY_REPLY_TO_ACTIVITY, i);
                }
            }

            ENTRY_REPLY_TO_ACTIVITY = ArrayUtils.resize(ENTRY_REPLY_TO_ACTIVITY,
                    ENTRY_REPLY_TO_ACTIVITY.length+1, null);
            ENTRY_REPLY_TO_ACTIVITY_WITH_NA = ArrayUtils.resize(ENTRY_REPLY_TO_ACTIVITY,
                    ENTRY_REPLY_TO_ACTIVITY.length+1, "N/A");

            entryNames = ArrayUtils.copy(data.getEntryNames());
            entryTask = ArrayUtils.copy(data.getEntryTask());
            entryBoundToActivity = ArrayUtils.copy(data.getEntryBoundToActivity());
            entryReplyToActivity = ArrayUtils.copy(data.getEntryReplyToActivity());
            entryArrivalRate = ArrayUtils.copy(data.getEntryArrivalRate());
            entryPriority = ArrayUtils.copy(data.getEntryPriority());
        }

        entrySpinner.setValue(new Integer(numberOfEntries));
        if (entryTable != null) {
            entryTable.updateTaskNames();
            entryTable.updateActivityNames();
        }
    }

    public EntriesPanel(JLQNWizard jw) {
        this.jw = jw;
        help = jw.getHelp();
        entryOps = new ArrayList<ListOp>();

        sync();

        initComponents();
        makeNames();
    }

    private void updateSizes() {
        setNumberOfEntries(((Integer) entrySpinner.getValue()).intValue());
    }

    private void makeNames() {
        for (int i = 0; i < entryNames.length; i++) {
            if (entryNames[i] == null) {
                while (areThereDuplicates("Entry" + (nameCounter+1), i, false)) {
                    nameCounter++;
                }
                entryNames[i] = "Entry" + (++nameCounter);
            }
        }
    }

    private void setNumberOfEntries(int number) {
        entryTable.stopEditing();
        numberOfEntries = number;

        entryNames = ArrayUtils.resize(entryNames, numberOfEntries, null);
        makeNames();

        entryPriority = ArrayUtils.resize(entryPriority, numberOfEntries, 0);
        entryArrivalRate = ArrayUtils.resize(entryArrivalRate, numberOfEntries, 0.0);

        entryTask = ArrayUtils.resize(entryTask, numberOfEntries, jw.getData().getTaskNames()[0]);
        entryBoundToActivity = ArrayUtils.resize(entryBoundToActivity, numberOfEntries, jw.getData().getActivityNames()[0]);
        entryReplyToActivity = ArrayUtils.resize(entryReplyToActivity, numberOfEntries, jw.getData().getActivityNames()[0]);

        entryTable.updateStructure();
        if (!deleting) {
            entryOps.add(ListOp.createResizeOp(numberOfEntries));
        }

        entrySpinner.setValue(new Integer(numberOfEntries));
        entryTable.updateDeleteCommand();
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
        entryTable.update();
    }

    @Override
    public String getName() {
        return "Entries";
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
                    "<html><center>Entry names cannot be empty.<br>Please enter another name.</center></html>",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (value.equalsIgnoreCase("N/A")) {
            JOptionPane.showMessageDialog(this,
                    "<html><center>Entry name is invalid.<br>Please enter another name.</center></html>",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }
    private boolean areThereDuplicates(String value, int index, boolean enableWarning) {
        boolean thereAreDupl = false;
        for (int i = 0; i < entryNames.length && !thereAreDupl; i++) {
            if (i == index || entryNames[i] == null) continue;
            thereAreDupl = thereAreDupl || entryNames[i].equalsIgnoreCase(value);
        }
        if (thereAreDupl) {
            if (enableWarning) {
                JOptionPane.showMessageDialog(this,
                        "<html><center>There is a duplication in entry names.<br>Please enter another name.</center></html>",
                        "Warning", JOptionPane.WARNING_MESSAGE);
                return thereAreDupl;
            }
        }
        return thereAreDupl;
    }

    /**
     * Determine if the entry is on the reference task.
     * @param index
     * @return true if the index-th entry is on the reference task.
     */
    private boolean isOnReferenceTask(int index) {
        String taskName = entryTask[index];

        JLQNModel data = jw.getData();
        synchronized (data) {
            return data.isReferenceTask(taskName);
        }
    }

    private void commit() {
        /* stop any editing in progress */
        if (entrySpinner.getEditor().getComponent(0).hasFocus()) {
            //disgusting. there must be a better way...
            try {
                entrySpinner.commitEdit();
                updateSizes();
            } catch (java.text.ParseException e) {
            }
        }

        entryTable.stopEditing();

        JLQNModel data = jw.getData();
        synchronized (data) {

            if (hasDeletes) {
                playbackEntryOps(data); //play back ops on the data object
            } else {
                data.resizeEntries(numberOfEntries, false);
            }
            data.setEntryNames(entryNames);
            data.setEntryTask(entryTask);
            data.setEntryBoundToActivity(entryBoundToActivity);
            data.setEntryReplyToActivity(entryReplyToActivity);
            data.setEntryArrivalRate(entryArrivalRate);
            data.setEntryPriority(entryPriority);

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

    private void playbackEntryOps(JLQNModel data) {
        for (int i = 0; i < entryOps.size(); i++) {
            ListOp lo = entryOps.get(i);
            if (lo.isDeleteOp()) {
                data.deleteEntry(lo.getData());
            }
            if (lo.isResizeOp()) {
                data.resizeEntries(lo.getData(), false);
            }
        }
    }}