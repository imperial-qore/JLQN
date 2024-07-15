package jlqn.gui.exact.panels;

import jlqn.analytical.JLQNModel;
import jlqn.util.ArrayUtils;
import jmt.framework.gui.listeners.AbstractJMTAction;
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
import java.util.Arrays;

public final class PrecedenceActivityPanel extends WizardPanel implements JLQNConstants, ForceUpdatablePanel {
    private static final long serialVersionUID = 1L;
    private int precedenceType;
    private String parameterLabel;

    private boolean oneOption;

    // Column numbers
    private int numCol = 3;
    private final static int COL_ACTIVITY = 0;
    private final static int COL_PARAM = 1;
    private final static int COL_DELETE_BUTTON = 2;

    private static final String helpText = "<html>In this panel you can define the number of activites and its parameteres (if there are any) in this precedence.<br><br>"
            + " To edit values, double-click on the desired cell"
            + " and start typing.<br> To select activities click or drag on the row headers.<br> <b>For a list of the available operations right-click"
            + " on the table</b>.<br>" + " Pressing DELETE removes all selected activities from the system.</html>";

    private JLQNWizard jw;
    private int numberOfPrecedenceActivities;
    private PrecedencePanel ppanel;
    private String[] precedenceActivities;
    private String[] PRECEDENCE_ACTIVITY;

    private Double[] precedenceParams;

    private String preOrPost; // Pre or Post

    //Activity data
    private JSpinner precedenceActivitySpinner = new JSpinner(new SpinnerNumberModel(0, 0, MAX_ACTIVITIES, 1));
    private ActivityPrecedenceTable activityPrecedenceTable;
    private TableCellEditor precedenceActivityEditor;

    private int rowIndex;

    private boolean deleting = false;

    private ChangeListener spinnerListener = new ChangeListener() {
        public void stateChanged(ChangeEvent ce) {
            if (!deleting) {
                updateSizes();
            }
        }
    };

    private void deleteSelectedActivities() {
        int[] selectedRows = activityPrecedenceTable.getSelectedRows();
        int nrows = selectedRows.length;
        if (nrows == 0) {
            return;
        }
        int left = activityPrecedenceTable.getRowCount() - nrows;
        if (left < 1) {
            activityPrecedenceTable.removeRowSelectionInterval(selectedRows[nrows - 1], selectedRows[nrows - 1]);
            deleteSelectedActivities();
            return;
        }
        deletePrecedenceActivities(selectedRows);
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
    private AbstractAction addPrecedenceActivity = new AbstractAction("New Precedence Activity") {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        {
            putValue(Action.SHORT_DESCRIPTION, "Adds a new Precedence Activity");
        }

        public void actionPerformed(ActionEvent e) {
            addPrecedenceActivity();
        }
    };

    private void addPrecedenceActivity() {
        setNumberOfPrecedenceActivities(numberOfPrecedenceActivities + 1);
    }

    private void deletePrecedenceActivity(int i) {
        numberOfPrecedenceActivities--;
        precedenceActivitySpinner.setValue(new Integer(numberOfPrecedenceActivities));
        precedenceActivities = ArrayUtils.delete(precedenceActivities, i);
        if (precedenceType != 5) {
            precedenceParams = ArrayUtils.delete(precedenceParams, i);
        }
    }

    private void deletePrecedenceActivities(int[] idx) {
        deleting = true;
        Arrays.sort(idx);
        for (int i = idx.length - 1; i >= 0; i--) {
            deletePrecedenceActivity(idx[i]);
        }
        updateSizes();
        deleting = false;
    }

    private class ActivityPrecedenceTable extends ExactTable {

        /**
         *
         */
        private static final long serialVersionUID = 1L;
        TableCellRenderer disabledCellRenderer;
        TableCellEditor activityCellEditor;
        ComboBoxCell activityComboBoxCell;
        ButtonCellEditor deleteButtonCellRenderer;
        JButton deleteButton;

        ActivityPrecedenceTable() {
            super(new ActivityPrecedenceTableModel());
            setName(String.format("%s Precedence Activity Table", preOrPost));

            disabledCellRenderer = new DisabledCellRenderer();
            activityComboBoxCell = new ComboBoxCell(PRECEDENCE_ACTIVITY);
            activityCellEditor = ComboBoxCellEditor.getEditorInstance(PRECEDENCE_ACTIVITY);

            deleteButton = new JButton(deleteOneActivity);
            deleteButtonCellRenderer = new ButtonCellEditor(deleteButton);
            enableDeletes();
            rowHeader.setRowHeight(CommonConstants.ROW_HEIGHT);
            setRowHeight(CommonConstants.ROW_HEIGHT);

            precedenceActivityEditor = ComboBoxCellEditor.getEditorInstance(PRECEDENCE_ACTIVITY);

            setRowSelectionAllowed(true);
            setColumnSelectionAllowed(false);

            // not beautiful, but effective. See ClassTableModel.getColumnClass()
            setDefaultRenderer(DisabledCellRenderer.class, disabledCellRenderer);

            setDisplaysScrollLabels(true);

            //int[] selectedRows = processorTable.getSelectedRows();
            installKeyboardAction(getInputMap(), getActionMap(), deleteActivity);
            mouseHandler = new ExactTable.MouseHandler(makeMouseMenu());
            mouseHandler.install();
            tableHeader.setToolTipText(null);
            rowHeader.setToolTipText(null);

        }

        /**
         * Overridden to ensure proper handling of type column
         */
        @Override
        public TableCellEditor getCellEditor(int row, int column) {
            if (column == COL_ACTIVITY) {
                /* select the right editor */
                return precedenceActivityEditor;
            } else {
                return super.getCellEditor(row, column);
            }
        }

        /*enables deleting operations with last column's button*/
        private void enableDeletes() {
            deleteOneActivity.setEnabled(numberOfPrecedenceActivities > 1);
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
            if (column == COL_ACTIVITY) {
                return activityComboBoxCell;
            } else if (column == COL_PARAM
                    && precedenceType == 5 /* Loop */
                    && row > 0) {
                return disabledCellRenderer;
            } else if (column == COL_PARAM && numCol == 2) {
                return deleteButtonCellRenderer;
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
        void updateDeleteCommand() {
            deleteOneActivity.setEnabled(numberOfPrecedenceActivities > 1);
            getColumnModel().getColumn(getColumnCount() - 1).setMinWidth(20);
            getColumnModel().getColumn(getColumnCount() - 1).setMaxWidth(20);

        }

        //END Federico Dall'Orso 14/3/2005

        @Override
        protected void updateActions() {
            boolean isEnabled = numberOfPrecedenceActivities > 1 && getSelectedRowCount() > 0;
            deleteActivity.setEnabled(isEnabled);
            deleteOneActivity.setEnabled(numberOfPrecedenceActivities > 1);
        }
    }

    private class ActivityPrecedenceTableModel extends ExactTableModel {

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
            return numberOfPrecedenceActivities;
        }

        public int getColumnCount() {
            return numCol;
        }

        @Override
        public String getColumnName(int index) {
            switch (index) {
                case COL_ACTIVITY:
                    return "Activity";
                case COL_PARAM:
                    if (numCol == 2) {
                        return null;
                    }
                    return parameterLabel;
                default:
                    return null;
            }
        }

        @Override
        protected Object getValueAtImpl(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case COL_ACTIVITY:
                    return precedenceActivities[rowIndex];
                case COL_PARAM:
                    if (numCol == 2) {
                        return null;
                    }
                    return precedenceParams[rowIndex];
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
                case COL_ACTIVITY: {
                    String newval = (String) value;
                    for (int i = 0; i < PRECEDENCE_ACTIVITY.length; i++) {
                        boolean condition = (newval == null)
                                || (PRECEDENCE_ACTIVITY[i] != null && PRECEDENCE_ACTIVITY[i].equalsIgnoreCase(newval));
                        if (condition) {
                            precedenceActivities[rowIndex] = newval;
                            break;
                        }
                    }
                    break;
                }
                case COL_PARAM:
                    if (numCol == 2) {
                        break;
                    }
                    try {
                        Double newval = Double.parseDouble((String) value);
                        if (newval >= 0.0) {
                            precedenceParams[rowIndex] = newval;
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
                case COL_ACTIVITY:
                    return true;
                case COL_PARAM:
                    if (numCol == 2) {
                        return false;
                    } else if (precedenceType == 5 /* Loop */) {
                        return rowIndex == 0;
                    }
                    return true;
                default:
                    return false;
            }
        }
    }

    protected AbstractJMTAction ACTION_CANCEL = new AbstractJMTAction("Cancel") {
        /**
         *
         */
        private static final long serialVersionUID = 1L;
        {
            setTooltipText("Solve this Model");
        }

        public void actionPerformed(ActionEvent e) {
            reactivateParent();
            JComponent comp = (JComponent) e.getSource();
            Window win = SwingUtilities.getWindowAncestor(comp);
            win.dispose();
        }
    };

    protected AbstractJMTAction ACTION_FINISH = new AbstractJMTAction("Save") {
        /**
         *
         */
        private static final long serialVersionUID = 1L;
        {
            setTooltipText("Save Precedence Configuration");
        }

        public void actionPerformed(ActionEvent e) {
            updateParentPanel();
            reactivateParent();
            JComponent comp = (JComponent) e.getSource();
            Window win = SwingUtilities.getWindowAncestor(comp);
            win.dispose();
        }
    };

    private void reactivateParent() {
        this.jw.setEnabled(true);
    }
    private void updateParentPanel() {
        if (this.preOrPost.equals("Pre")) {
            this.ppanel.setPrecedencePreActivities(precedenceActivities, rowIndex);
            this.ppanel.setPrecedencePreParams(precedenceParams, rowIndex);
        } else {
            this.ppanel.setPrecedencePostActivities(precedenceActivities, rowIndex);
            this.ppanel.setPrecedencePostParams(precedenceParams, rowIndex);
        }
    }

    private void initComponents() {
        precedenceActivitySpinner.addChangeListener(spinnerListener);

        activityPrecedenceTable = new ActivityPrecedenceTable();

        /* and now some Box black magic */
        Box activityPrecedenceSpinnerBox = Box.createHorizontalBox();
        JLabel spinnerLabel = new JLabel(DESCRIPTION_PRECEDENCE_ACTIVITIES);

        activityPrecedenceSpinnerBox.add(spinnerLabel);
        activityPrecedenceSpinnerBox.add(Box.createHorizontalStrut(10));
        Box numberBox = Box.createVerticalBox();

        JPanel spinnerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel numberLabel = new JLabel("Number:");
        precedenceActivitySpinner.setMaximumSize(new Dimension(600, 18));
        spinnerPanel.add(numberLabel);
        spinnerPanel.add(precedenceActivitySpinner);
        numberBox.add(spinnerPanel);
        JButton addPrecedenceActivityButton = new JButton(addPrecedenceActivity);
        if (this.oneOption) {
            addPrecedenceActivityButton.setEnabled(false);
        }
        numberBox.add(addPrecedenceActivityButton);

        numberBox.setMaximumSize(new Dimension(300, 150));

        activityPrecedenceSpinnerBox.add(numberBox);

        Box activityBox = Box.createVerticalBox();
        activityBox.add(Box.createVerticalStrut(30));
        activityBox.add(activityPrecedenceSpinnerBox);
        activityBox.add(Box.createVerticalStrut(10));
        JScrollPane activityTablePane = new JScrollPane(activityPrecedenceTable);
        activityTablePane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        activityTablePane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        activityBox.add(activityTablePane);
        activityBox.add(Box.createRigidArea(new Dimension(10, 20)));

        Box totalBox = Box.createHorizontalBox();
        totalBox.add(Box.createHorizontalStrut(20));
        totalBox.add(activityBox);
        totalBox.add(Box.createHorizontalStrut(20));

        JPanel buttonPanel = new JPanel();
        JButton cancel = new JButton(ACTION_CANCEL);
        JButton save = new JButton(ACTION_FINISH);
        buttonPanel.add(cancel);
        buttonPanel.add(save);

        setLayout(new BorderLayout());
        add(totalBox, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void sync() {
        /* sync status with data object */
        /* arrays are copied to ensure data object consistency is preserved */
        JLQNModel data = jw.getData();
        synchronized (data) {
            if (preOrPost.equals("Pre")) {
                precedenceActivities = ppanel.getPrecedencePreActivities(rowIndex);
                precedenceParams = ppanel.getPrecedencePreParams(rowIndex);
            } else{
                precedenceActivities = ppanel.getPrecedencePostActivities(rowIndex);
                precedenceParams = ppanel.getPrecedencePostParams(rowIndex);
            }
            PRECEDENCE_ACTIVITY = data.getActivityNames();
            PRECEDENCE_ACTIVITY = ArrayUtils.resize(PRECEDENCE_ACTIVITY, PRECEDENCE_ACTIVITY.length, null);
        }
        precedenceActivitySpinner.setValue(new Integer(precedenceActivities.length));
    }

    public PrecedenceActivityPanel(
            PrecedencePanel ppanel,
            JLQNWizard jw,
            String preOrPost,
            int rowIndex,
            int precedenceType) {
        this.jw = jw;
        this.precedenceType = precedenceType;
        this.preOrPost = preOrPost;
        this.ppanel = ppanel;
        this.parameterLabel = "None";
        this.oneOption = false;
        this.rowIndex = rowIndex;
        sync();
        this.numberOfPrecedenceActivities = this.precedenceActivities.length;

        switch (precedenceType) {
            case 0: // Sequence, no params at all
                this.numCol = 2;
                if (this.preOrPost.equals("Pre")) {
                    initNumberOfPrecedences(1);
                    this.oneOption = true;
                }
                break;
            case 1: // And-Join, all pre params (label: Fan In), no post params
                if (this.preOrPost.equals("Post")) {
                    initNumberOfPrecedences(1);
                    this.numCol = 2;
                    this.oneOption = true;
                } else {
                    this.parameterLabel = "Fan In";
                }
                break;
            case 2: // Or-Join, no params at all
                this.numCol = 2;
                if (this.preOrPost.equals("Post")) {
                    initNumberOfPrecedences(1);
                    this.oneOption = true;
                }
                break;
            case 3: // And-Fork, all post params, no post params
                if (this.preOrPost.equals("Pre")) {
                    initNumberOfPrecedences(1);
                    this.numCol = 2;
                    this.oneOption = true;
                } else {
                    this.parameterLabel = "Fan Out";
                }
                break;
            case 4: // Or-Fork, no pre params, all post params (label: Selection Probability)
                if (this.preOrPost.equals("Pre")) {
                    initNumberOfPrecedences(1);
                    this.numCol = 2;
                    this.oneOption = true;
                } else {
                    this.parameterLabel = "Selection Probability";
                }
                break;
            case 5: // Loop, no pre params, 1 post param (label: count)
                if (this.preOrPost.equals("Pre")) {
                    initNumberOfPrecedences(1);
                    this.numCol = 2;
                    this.oneOption = true;
                } else {
                    this.parameterLabel = "Counts";
                }
                // Reset all values after first if exist for loop
                for (int i = 1; i < this.precedenceParams.length; i++) {
                    precedenceParams[i] = null;
                }
                break;
        }
        initComponents();
    }

    private void updateSizes() {
        setNumberOfPrecedenceActivities(((Integer) precedenceActivitySpinner.getValue()).intValue());
    }

    private void initNumberOfPrecedences(int number) {
        numberOfPrecedenceActivities = number;
        precedenceActivities = ArrayUtils.resize(precedenceActivities, number, precedenceActivities[0]);

        precedenceParams = ArrayUtils.resize(precedenceParams, number, precedenceType == 5 ? null : 1.0); // Set param to null if loop

        precedenceActivitySpinner.setValue(new Integer(number));
    }

    private void setNumberOfPrecedenceActivities(int number) {
        activityPrecedenceTable.stopEditing();
        numberOfPrecedenceActivities = number;

        precedenceActivities = ArrayUtils.resize(precedenceActivities, numberOfPrecedenceActivities, PRECEDENCE_ACTIVITY[0]);

        precedenceParams = ArrayUtils.resize(precedenceParams, numberOfPrecedenceActivities, precedenceType == 5 ? null : 1.0); // Set param to null if loop

        activityPrecedenceTable.updateStructure();

        precedenceActivitySpinner.setValue(new Integer(numberOfPrecedenceActivities));
        activityPrecedenceTable.updateDeleteCommand();
    }

    @Override
    public void lostFocus() {
        commit();
        //release();
    }

    @Override
    public void gotFocus() {
        sync();
        activityPrecedenceTable.update();
    }

    @Override
    public String getName() {
        return String.format("%s Activities", preOrPost);
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

    private void commit() {
        /* stop any editing in progress */
        if (precedenceActivitySpinner.getEditor().getComponent(0).hasFocus()) {
            //disgusting. there must be a better way...
            try {
                precedenceActivitySpinner.commitEdit();
                updateSizes();
            } catch (java.text.ParseException e) {
            }
        }
        sync();
        activityPrecedenceTable.stopEditing();
    }


    public void retrieveData() {
        sync();
    }

    public void commitData() {
        commit();
    }
}