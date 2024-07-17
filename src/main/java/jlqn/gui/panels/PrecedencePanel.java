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

import jlqn.model.JLQNModel;
import jlqn.util.ArrayUtils;
import jmt.framework.gui.components.JMTToolBar;
import jmt.framework.gui.help.HoverHelp;
import jmt.framework.gui.table.editors.ButtonCellEditor;
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
 * 1st panel: precedence type, pre-activity, post-activity, pre-params, post-params
 */

public final class PrecedencePanel extends WizardPanel implements JLQNConstants, ForceUpdatablePanel {
    private static final long serialVersionUID = 1L;

    // Column numbers
    private final static int COL_PRECEDENCE_TYPE = 0;
    private final static int COL_PRE_ACTIVITIES = 1;
    private final static int COL_POST_ACTIVITIES = 2;
    private final static int COL_DELETE_BUTTON = 3;

    private HoverHelp help;
    private static final String helpText = "<html>In this panel you can define the precedence of activities.<br><br>"
            + " To edit values, double-click on the desired cell"
            + " and start typing.<br> To select precedences click or drag on the row headers.<br> <b>For a list of the available operations right-click"
            + " on the table</b>.<br>" + " Pressing DELETE removes all selected entries from the system.</html>";

    private JLQNWizard jw;
    //private boolean isLd;
    private int numberOfPrecedences;
    private int[] precedenceType;
    private String[][] precedencePreActivities;
    private String[][] precedencePostActivities;
    private Double[][] precedencePreParams;
    private Double[][] precedencePostParams;

    //precedence data
    private List<ListOp> precedenceOps;
    private boolean hasDeletes;
    private boolean deleting = false;
    private JSpinner precedenceSpinner = new JSpinner(new SpinnerNumberModel(0, 0, MAX_ACTIVITIES, 1));

    private PrecedenceTable precedenceTable;
    private TableCellEditor precedenceTypeEditor;

    private ChangeListener spinnerListener = new ChangeListener() {
        public void stateChanged(ChangeEvent ce) {
            if (!deleting) {
                updateSizes();
            }
        }
    };

    private void deleteSelectedPrecedences() {
        int[] selectedRows = precedenceTable.getSelectedRows();
        int nrows = selectedRows.length;
        if (nrows == 0) {
            return;
        }
//        int left = precedenceTable.getRowCount() - nrows;
//        if (left < 1) {
//            precedenceTable.removeRowSelectionInterval(selectedRows[nrows - 1], selectedRows[nrows - 1]);
//            deleteSelectedPrecedences();
//            return;
//        }
        deletePrecedences(selectedRows);
    }

    private AbstractAction deletePrecedence = new AbstractAction("Delete selected precedences") {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        {
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, false));
            putValue(Action.SHORT_DESCRIPTION, "Deletes selected precedences from the system");
        }

        public void actionPerformed(ActionEvent e) {
            deleteSelectedPrecedences();
        }
    };
    private AbstractAction deleteOnePrecedence = new AbstractAction("") {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        {
            putValue(Action.SHORT_DESCRIPTION, "Delete This Precedence");
            putValue(Action.SMALL_ICON, JMTImageLoader.loadImage("Delete"));
        }

        public void actionPerformed(ActionEvent e) {
        }
    };

    private AbstractAction editPrecedence = new AbstractAction("") {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        {
            putValue(Action.SHORT_DESCRIPTION, "Edit");
            putValue(Action.NAME, "Edit");
        }

        public void actionPerformed(ActionEvent e) {}
    };

    private AbstractAction addPrecedence = new AbstractAction("New Precedence") {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        {
            //putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.ALT_MASK));
            putValue(Action.SHORT_DESCRIPTION, "Adds a new Precedence to Model");
        }

        public void actionPerformed(ActionEvent e) {
            addPrecedence();
        }
    };

    private void addPrecedence() {
        setNumberOfPrecedences(numberOfPrecedences + 1);

    }

    private void deletePrecedence(int i) {
        numberOfPrecedences--;
        precedenceSpinner.setValue(new Integer(numberOfPrecedences));
        precedenceType = ArrayUtils.delete(precedenceType, i);
        precedencePreActivities = ArrayUtils.delete(precedencePreActivities, i);
        precedencePostActivities = ArrayUtils.delete(precedencePostActivities, i);
        precedencePreParams = ArrayUtils.delete(precedencePreParams, i);
        precedencePostParams = ArrayUtils.delete(precedencePostParams, i);
        precedenceOps.add(ListOp.createDeleteOp(i));
        hasDeletes = true;
    }

    private void deletePrecedences(int[] idx) {
        deleting = true;
        Arrays.sort(idx);
        for (int i = idx.length - 1; i >= 0; i--) {
            deletePrecedence(idx[i]);
        }
        updateSizes();
        deleting = false;
//        updateAlgoPanel();
    }

    private class PrecedenceTable extends ExactTable {

        /**
         *
         */
        private static final long serialVersionUID = 1L;
        TableCellRenderer disabledCellRenderer;
        TableCellEditor precedenceTypeCellEditor;
        ComboBoxCell precedenceTypeComboBoxCell;
        ButtonCellEditor deleteButtonCellRenderer;
        ButtonCellEditor editButtonCellRenderer;
        JButton deleteButton;
        JButton editButton;

        PrecedenceTable() {
            super(new PrecedenceTableModel());
            setName("PrecedenceTable");

            disabledCellRenderer = new DisabledCellRenderer();

            precedenceTypeComboBoxCell = new ComboBoxCell(PRECEDENCE_TYPES);

            deleteButton = new JButton(deleteOnePrecedence);
            editButton = new JButton(editPrecedence);
            deleteButtonCellRenderer = new ButtonCellEditor(deleteButton);
            editButtonCellRenderer = new ButtonCellEditor(editButton);
            enableDeletes();
            enableEdits();
            rowHeader.setRowHeight(CommonConstants.ROW_HEIGHT);
            setRowHeight(CommonConstants.ROW_HEIGHT);

            JComboBox<String> precedenceTypeBox = new JComboBox<>(PRECEDENCE_TYPES);
            precedenceTypeCellEditor = new DefaultCellEditor(precedenceTypeBox);
            precedenceTypeEditor = new DefaultCellEditor(precedenceTypeBox);

            setRowSelectionAllowed(true);
            setColumnSelectionAllowed(false);

            setDefaultRenderer(DisabledCellRenderer.class, disabledCellRenderer);
            setDefaultEditor(String.class, precedenceTypeCellEditor);
            setDisplaysScrollLabels(true);

            installKeyboardAction(getInputMap(), getActionMap(), deletePrecedence);
            mouseHandler = new MouseHandler(makeMouseMenu());
            mouseHandler.install();

            help.addHelp(this,
                    "Click or drag to select precedences; to edit data double-click and start typing. Right-click for a list of available operations");
            help.addHelp(moreRowsLabel, "There are more precedences: scroll down to see them");
            help.addHelp(selectAllButton, "Click to select all precedences");
            tableHeader.setToolTipText(null);
            rowHeader.setToolTipText(null);
            help.addHelp(rowHeader, "Click, SHIFT-click or drag to select precedences");

        }

        /**
         * Overridden to ensure proper handling of scheduling type column
         */
        @Override
        public TableCellEditor getCellEditor(int row, int column) {
            if (column == COL_PRECEDENCE_TYPE) {
                /* select the right editor */
                return precedenceTypeEditor;
            } else {
                return super.getCellEditor(row, column);
            }
        }

        private void enableDeletes() {
            deleteOnePrecedence.setEnabled(numberOfPrecedences > 0);
            /*It seems the only way to implement row deletion...*/
            this.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if ((columnAtPoint(e.getPoint()) == getColumnCount() - 1) && getRowCount() > 0) {
                        setRowSelectionInterval(rowAtPoint(e.getPoint()), rowAtPoint(e.getPoint()));
                        deleteSelectedPrecedences();
                    }
                }
            });
            getColumnModel().getColumn(getColumnCount() - 1).setMinWidth(20);
            getColumnModel().getColumn(getColumnCount() - 1).setMaxWidth(20);
        }

        private void enableEdits() {
            editPrecedence.setEnabled(numberOfPrecedences > 0);
            /*It seems the only way to implement row deletion...*/
            this.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if ((columnAtPoint(e.getPoint()) == COL_PRE_ACTIVITIES) && getRowCount() > 0) {
                        // Clicked on Pre activities
                        setRowSelectionInterval(rowAtPoint(e.getPoint()), rowAtPoint(e.getPoint()));
                        createEditWindow(COL_PRE_ACTIVITIES, rowAtPoint(e.getPoint()));
                    } else if ((columnAtPoint(e.getPoint()) == COL_POST_ACTIVITIES) && getRowCount() > 0) {
                        // Clicked on Post activities
                        setRowSelectionInterval(rowAtPoint(e.getPoint()), rowAtPoint(e.getPoint()));
                        createEditWindow(COL_POST_ACTIVITIES, rowAtPoint(e.getPoint()));
                    }
                }
            });
        }

        @Override
        public TableCellRenderer getCellRenderer(int row, int column) {
            //if this is type column, i must render it as a combo box instead of a jtextfield
            if (column == COL_PRECEDENCE_TYPE) {
                return precedenceTypeComboBoxCell;
            } else if (column == COL_DELETE_BUTTON) {
                return deleteButtonCellRenderer;
            } else if (column == COL_PRE_ACTIVITIES) {
                return editButtonCellRenderer;
            } else if (column == COL_POST_ACTIVITIES) {
                return editButtonCellRenderer;
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
            menu.add(deletePrecedence);
            return menu;
        }

        void updateDeleteCommand() {
            deleteOnePrecedence.setEnabled(numberOfPrecedences > 0);
            getColumnModel().getColumn(getColumnCount() - 1).setMinWidth(20);
            getColumnModel().getColumn(getColumnCount() - 1).setMaxWidth(20);
        }

        void updateEditCommand() {
            editPrecedence.setEnabled(numberOfPrecedences > 0);
        }

        //END Federico Dall'Orso 14/3/2005

        @Override
        protected void updateActions() {
            boolean isEnabled = numberOfPrecedences > 0 && getSelectedRowCount() > 0;
            deletePrecedence.setEnabled(isEnabled);
            deleteOnePrecedence.setEnabled(numberOfPrecedences > 0);
            editPrecedence.setEnabled(numberOfPrecedences > 0);
        }
    }

    private class PrecedenceTableModel extends ExactTableModel {
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
            return numberOfPrecedences;
        }

        public int getColumnCount() {
            return 4;
        }

        @Override
        public String getColumnName(int index) {
            switch (index) {
                case COL_PRECEDENCE_TYPE:
                    return "Type";
                case COL_PRE_ACTIVITIES:
                    return "Pre Activities";
                case COL_POST_ACTIVITIES:
                    return "Post Activities";
                default:
                    return null;
            }
        }

        @Override
        protected Object getValueAtImpl(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case COL_PRECEDENCE_TYPE:
                    return PRECEDENCE_TYPES[precedenceType[rowIndex]];
                case COL_PRE_ACTIVITIES:
                    return precedencePreActivities[rowIndex];
                case COL_POST_ACTIVITIES:
                    return precedencePostActivities[rowIndex];
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
                case COL_PRECEDENCE_TYPE: {
                    for (int i = 0; i < PRECEDENCE_TYPES.length; i++) {
                        if (value == PRECEDENCE_TYPES[i]) { //literal strings are canonicalized, hence == is ok
                            precedenceType[rowIndex] = i;
                            break;
                        }
                    }
                    break;
                }
                case COL_PRE_ACTIVITIES: {
                    String newval = (String) value;
                    System.out.println("PRE ACTIVIIY SET VALUE");
                    break;
                }
                case COL_POST_ACTIVITIES: {
                    String newval = (String) value;
                    System.out.println("POST ACTIVIIY SET VALUE");
                    break;
                }
                default:
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case COL_PRECEDENCE_TYPE:
                    return true;
                default:
                    return false;
            }
        }
    }

    private void initComponents() {
        precedenceSpinner.addChangeListener(spinnerListener);
        help.addHelp(precedenceSpinner, "Enter the number of Precedences for this system");

        precedenceTable = new PrecedenceTable();

        Box precedenceSpinnerBox = Box.createHorizontalBox();

        JLabel spinnerLabel = new JLabel(DESCRIPTION_PRECEDENCES);

        precedenceSpinnerBox.add(spinnerLabel);

        precedenceSpinnerBox.add(Box.createHorizontalStrut(10));

        Box numberBox = Box.createVerticalBox();

        JPanel spinnerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel numberLabel = new JLabel("Number:");
        precedenceSpinner.setMaximumSize(new Dimension(600, 18));
        spinnerPanel.add(numberLabel);
        spinnerPanel.add(precedenceSpinner);
        numberBox.add(spinnerPanel);
        numberBox.add(new JButton(addPrecedence));

        numberBox.setMaximumSize(new Dimension(300, 150));

        precedenceSpinnerBox.add(numberBox);

        //END  Federico Dall'Orso 9/3/2005

        Box precedenceBox = Box.createVerticalBox();
        precedenceBox.add(Box.createVerticalStrut(30));
        precedenceBox.add(precedenceSpinnerBox);
        precedenceBox.add(Box.createVerticalStrut(10));
        JScrollPane precedenceTablePane = new JScrollPane(precedenceTable);
        precedenceTablePane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        precedenceTablePane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        precedenceBox.add(precedenceTablePane);
        precedenceBox.add(Box.createRigidArea(new Dimension(10, 20)));

        Box totalBox = Box.createHorizontalBox();
        totalBox.add(Box.createHorizontalStrut(20));
        totalBox.add(precedenceBox);
        totalBox.add(Box.createHorizontalStrut(20));

        setLayout(new BorderLayout());
        add(totalBox, BorderLayout.CENTER);
    }

    private void sync() {
        hasDeletes = false;
        precedenceOps.clear();

        /* sync status with data object */
        /* arrays are copied to ensure data object consistency is preserved */
        JLQNModel data = jw.getData();
        synchronized (data) {
            numberOfPrecedences = data.getNumberOfPrecedences();
            precedenceType = ArrayUtils.copy(data.getPrecedenceType());
            precedencePreActivities = ArrayUtils.copy2(data.getPrecedencePreActivities());
            precedencePostActivities = ArrayUtils.copy2(data.getPrecedencePostActivities());
            precedencePreParams = ArrayUtils.copy2(data.getPrecedencePreParams());
            precedencePostParams = ArrayUtils.copy2(data.getPrecedencePostParams());
        }
        precedenceSpinner.setValue(new Integer(numberOfPrecedences));
    }

    public PrecedencePanel(JLQNWizard jw) {
        this.jw = jw;
        help = jw.getHelp();
        precedenceOps = new ArrayList<ListOp>();

        sync();

        initComponents();
    }

    public String[] getPrecedencePreActivities(int rowIndex) { return precedencePreActivities[rowIndex]; }
    public String[] getPrecedencePostActivities(int rowIndex) { return precedencePostActivities[rowIndex]; }
    public Double[] getPrecedencePreParams(int rowIndex) { return precedencePreParams[rowIndex]; }
    public Double[] getPrecedencePostParams(int rowIndex) { return precedencePostParams[rowIndex]; }
    public void setPrecedencePreActivities(String[] preActivities, int rowIndex) {
        this.precedencePreActivities[rowIndex] = new String[preActivities.length];
        ArrayUtils.copy(preActivities, this.precedencePreActivities[rowIndex]);
    }
    public void setPrecedencePostActivities(String[] postActivities, int rowIndex) {
        this.precedencePostActivities[rowIndex] = new String[postActivities.length];
        ArrayUtils.copy(postActivities, this.precedencePostActivities[rowIndex]);
    }
    public void setPrecedencePreParams(Double[] preParams, int rowIndex) {
        this.precedencePreParams[rowIndex] = new Double[preParams.length];
        ArrayUtils.copy(preParams, this.precedencePreParams[rowIndex]);
    }
    public void setPrecedencePostParams(Double[] postParams, int rowIndex) {
        this.precedencePostParams[rowIndex] = new Double[postParams.length];
        ArrayUtils.copy(postParams, this.precedencePostParams[rowIndex]);
    }

    private void updateSizes() {
        setNumberOfPrecedences(((Integer) precedenceSpinner.getValue()).intValue());
    }

    private void setNumberOfPrecedences(int number) {
        precedenceTable.stopEditing();
        numberOfPrecedences = number;

        precedenceType = ArrayUtils.resize(precedenceType, numberOfPrecedences, 0);
        precedencePostActivities = ArrayUtils.resize(precedencePostActivities, numberOfPrecedences, jw.getData().getActivityNames()[0]);
        precedencePreActivities = ArrayUtils.resize(precedencePreActivities, numberOfPrecedences, jw.getData().getActivityNames()[0]);
        precedencePostParams = ArrayUtils.resize(precedencePostParams, numberOfPrecedences, 1.0);
        precedencePreParams = ArrayUtils.resize(precedencePreParams, numberOfPrecedences, 1.0);

        precedenceTable.updateStructure();
        if (!deleting) {
            precedenceOps.add(ListOp.createResizeOp(numberOfPrecedences));
        }

        precedenceSpinner.setValue(new Integer(numberOfPrecedences));
        precedenceTable.updateDeleteCommand();
        precedenceTable.updateEditCommand();
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
        precedenceTable.update();
    }

    @Override
    public String getName() {
        return "Precedences";
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
        if (precedenceSpinner.getEditor().getComponent(0).hasFocus()) {
            //disgusting. there must be a better way...
            try {
                precedenceSpinner.commitEdit();
                updateSizes();
            } catch (java.text.ParseException e) {
            }
        }

        precedenceTable.stopEditing();

        JLQNModel data = jw.getData();
        synchronized (data) {

            if (hasDeletes) {
                playbackPrecedenceOps(data); //play back ops on the data object
            } else {
                data.resizePrecedences(numberOfPrecedences, false);
            }
            data.setPrecedenceType(precedenceType);
            data.setPrecedencePreActivities(precedencePreActivities);
            data.setPrecedencePostActivities(precedencePostActivities);
            data.setPrecedencePreParams(precedencePreParams);
            data.setPrecedencePostParams(precedencePostParams);
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

    private void playbackPrecedenceOps(JLQNModel data) {
        for (int i = 0; i < precedenceOps.size(); i++) {
            ListOp lo = precedenceOps.get(i);
            if (lo.isDeleteOp()) {
                data.deletePrecedence(lo.getData());
            }
            if (lo.isResizeOp()) {
                data.resizePrecedences(lo.getData(), false);
            }
        }
    }

    private void createEditWindow(int type, int rowIndex) {
        this.jw.setEnabled(false);
        JTabbedPane jtp = new JTabbedPane();
        String preOrPost = type == COL_PRE_ACTIVITIES ? "Pre" : "Post";
        JFrame editWindow = new JFrame(String.format("Edit window for %s activities", preOrPost));
        editWindow.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        editWindow.getContentPane().add(jtp);

        JMTToolBar toolbar = new JMTToolBar(JMTImageLoader.getImageLoader());

        editWindow.add(toolbar, BorderLayout.NORTH);
        jtp.add(new PrecedenceActivityPanel(this, this.jw, preOrPost, rowIndex, this.precedenceType[rowIndex]));
        //END
        //BoundingBox of main window
        Rectangle rect = this.getBounds();
        editWindow.setBounds(rect.x + 20, rect.y + 20, rect.width, rect.height);
        editWindow.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                enableWizard();
            }});
        editWindow.setVisible(true);
    }

    private void enableWizard() {
        this.jw.setEnabled(true);
    }
}