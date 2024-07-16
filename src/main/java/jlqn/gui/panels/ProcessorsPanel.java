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
import jmt.framework.data.ArrayUtils;
import jmt.framework.gui.help.HoverHelp;
import jmt.framework.gui.table.editors.ButtonCellEditor;
import jmt.framework.gui.wizard.WizardPanel;
import jmt.gui.common.CommonConstants;
import jmt.gui.common.JMTImageLoader;
import jmt.gui.exact.table.*;

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

 * @author
 * Date: 4-Feb-2023
 */

/**
 * 1st panel: processors number, name, scheduling, multiplicity, speed factor data
 * Note that replicas is a column we track, but we do not show on the table. It is defaulted to 1.
 */

public final class ProcessorsPanel extends WizardPanel implements JLQNConstants, ForceUpdatablePanel {
    private static final long serialVersionUID = 1L;

    // Column numbers
    private final static int NUM_COLS = 5;
    private final static int COL_NAME = 0;
    private final static int COL_SCHEDULING = 1;
    private final static int COL_MULTIPLICITY = 2;
    private final static int COL_SPEED_FACTOR = 3;
    private final static int COL_DELETE_BUTTON = 4;

    private HoverHelp help;
    private static final String helpText = "<html>In this panel you can define the number of processors in the system and their properties.<br><br>"
            + " To edit values, double-click on the desired cell"
            + " and start typing.<br> To select processors click or drag on the row headers.<br> <b>For a list of the available operations right-click"
            + " on the table</b>.<br>" + " Pressing DELETE removes all selected processors from the system.</html>";

    private JLQNWizard jw;
    private int numberOfProcessors;
    private String[] processorNames;
    private int[] processorScheduling;

    //processor data
    private int[] processorMultiplicity;
    private int[] processorReplicas;
    private double[] processorSpeedFactor;
    private int nameCounter;
    private List<ListOp> processorOps;
    private boolean hasDeletes;
    private boolean deleting = false;
    private JSpinner processorSpinner = new JSpinner(new SpinnerNumberModel(1, 1, MAX_PROCESSORS, 1));

    private ProcessorTable processorTable;
    private TableCellEditor processorSchedulingEditor;

    private ChangeListener spinnerListener = new ChangeListener() {
        public void stateChanged(ChangeEvent ce) {
            if (!deleting) {
                updateSizes();
            }
        }
    };

    private void deleteSelectedProcessors() {
        int[] selectedRows = processorTable.getSelectedRows();
        int nrows = selectedRows.length;
        if (nrows == 0) {
            return;
        }
        int left = processorTable.getRowCount() - nrows;
        if (left < 1) {
            processorTable.removeRowSelectionInterval(selectedRows[nrows - 1], selectedRows[nrows - 1]);
            deleteSelectedProcessors();
            return;
        }
        deleteProcessors(selectedRows);
    }

    private AbstractAction deleteProcessor = new AbstractAction("Delete selected processors") {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        {
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, false));
            putValue(Action.SHORT_DESCRIPTION, "Deletes selected processors from the system");
        }

        public void actionPerformed(ActionEvent e) {
            deleteSelectedProcessors();
        }
    };
    private AbstractAction deleteOneProcessor = new AbstractAction("") {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        {
            putValue(Action.SHORT_DESCRIPTION, "Delete This Processor");
            putValue(Action.SMALL_ICON, JMTImageLoader.loadImage("Delete"));
        }

        public void actionPerformed(ActionEvent e) {
        }
    };
    private AbstractAction addProcessor = new AbstractAction("New Processor") {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        {
            //putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.ALT_MASK));
            putValue(Action.SHORT_DESCRIPTION, "Adds a new Processor to Model");
        }

        public void actionPerformed(ActionEvent e) {
            addProcessor();
        }
    };

    private void addProcessor() {
        setNumberOfProcessors(numberOfProcessors + 1);
    }

    private void deleteProcessor(int i) {
        numberOfProcessors--;
        processorSpinner.setValue(new Integer(numberOfProcessors));

        String deletedProcessorName = processorNames[i];
        processorNames = ArrayUtils.delete(processorNames, i);
        JLQNModel data = jw.getData();
        synchronized (data) {
            data.changeAllTaskProcessor(deletedProcessorName, null);
        }

        processorScheduling = ArrayUtils.delete(processorScheduling, i);
        processorMultiplicity = ArrayUtils.delete(processorMultiplicity, i);
        processorReplicas = ArrayUtils.delete(processorReplicas, i);
        processorSpeedFactor = ArrayUtils.delete(processorSpeedFactor, i);

        processorOps.add(ListOp.createDeleteOp(i));
        hasDeletes = true;
    }

    private void deleteProcessors(int[] idx) {
        deleting = true;
        Arrays.sort(idx);
        for (int i = idx.length - 1; i >= 0; i--) {
            deleteProcessor(idx[i]);
        }
        updateSizes();
        deleting = false;
    }

    private class ProcessorTable extends ExactTable {

        /**
         *
         */
        private static final long serialVersionUID = 1L;
        TableCellRenderer disabledCellRenderer;
        TableCellEditor processorSchedulingCellEditor;
        //BEGIN Federico Dall'Orso 8/3/2005
        ComboBoxCell processorSchedulingComboBoxCell;
        ButtonCellEditor deleteButtonCellRenderer;
        JButton deleteButton;

        //END Federico Dall'Orso 8/3/2005

        ProcessorTable() {
            super(new ProcessorTableModel());
            setName("ProcessorTable");

            disabledCellRenderer = new DisabledCellRenderer();

            processorSchedulingComboBoxCell = new ComboBoxCell(PROCESSOR_SCHEDULING_TYPENAMES);

            deleteButton = new JButton(deleteOneProcessor);
            deleteButtonCellRenderer = new ButtonCellEditor(deleteButton);
            enableDeletes();
            rowHeader.setRowHeight(CommonConstants.ROW_HEIGHT);
            setRowHeight(CommonConstants.ROW_HEIGHT);
            //END Federico Dall'Orso 8/3/2005


            JComboBox<String> processorSchedulingBox = new JComboBox<String>(PROCESSOR_SCHEDULING_TYPENAMES);
            processorSchedulingCellEditor = new DefaultCellEditor(processorSchedulingBox);
            processorSchedulingBox.setEditable(false);
            processorSchedulingEditor = new DefaultCellEditor(processorSchedulingBox);

            setRowSelectionAllowed(true);
            setColumnSelectionAllowed(false);

            // not beautiful, but effective. See ClassTableModel.getColumnClass()
            setDefaultRenderer(DisabledCellRenderer.class, disabledCellRenderer);
            setDefaultEditor(String.class, processorSchedulingCellEditor);

            setDisplaysScrollLabels(true);

            //int[] selectedRows = processorTable.getSelectedRows();
            installKeyboardAction(getInputMap(), getActionMap(), deleteProcessor);
            mouseHandler = new ExactTable.MouseHandler(makeMouseMenu());
            mouseHandler.install();

            help.addHelp(this,
                    "Click or drag to select processors; to edit data double-click and start typing. Right-click for a list of available operations");
            help.addHelp(moreRowsLabel, "There are more processors: scroll down to see them");
            help.addHelp(selectAllButton, "Click to select all processors");
            tableHeader.setToolTipText(null);
            rowHeader.setToolTipText(null);
            help.addHelp(rowHeader, "Click, SHIFT-click or drag to select processors");

        }

        /**
         * Overridden to ensure proper handling of scheduling type column
         */
        @Override
        public TableCellEditor getCellEditor(int row, int column) {
            if (column == COL_SCHEDULING) {
                return processorSchedulingEditor;
            } else {
                return super.getCellEditor(row, column);
            }
        }

        //BEGIN Federico Dall'Orso 14/3/2005
        /*enables deleting operations with last column's button*/
        private void enableDeletes() {
            deleteOneProcessor.setEnabled(numberOfProcessors > 1);
            /*It seems the only way to implement row deletion...*/
            this.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if ((columnAtPoint(e.getPoint()) == getColumnCount() - 1) && getRowCount() > 1) {
                        setRowSelectionInterval(rowAtPoint(e.getPoint()), rowAtPoint(e.getPoint()));
                        deleteSelectedProcessors();
                    }
                }
            });
            getColumnModel().getColumn(getColumnCount() - 1).setMinWidth(20);
            getColumnModel().getColumn(getColumnCount() - 1).setMaxWidth(20);
        }

        //END Federico Dall'Orso 14/3/2005

        //new Federico Dall'Orso 8/3/2005

        @Override
        public TableCellRenderer getCellRenderer(int row, int column) {
            //if this is type column, i must render it as a combo box instead of a jtextfield
            if (column == COL_SCHEDULING) {
                return processorSchedulingComboBoxCell;
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
            menu.add(deleteProcessor);
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
            deleteOneProcessor.setEnabled(numberOfProcessors > 1);
            getColumnModel().getColumn(getColumnCount() - 1).setMinWidth(20);
            getColumnModel().getColumn(getColumnCount() - 1).setMaxWidth(20);

        }

        //END Federico Dall'Orso 14/3/2005

        @Override
        protected void updateActions() {
            boolean isEnabled = numberOfProcessors > 1 && getSelectedRowCount() > 0;
            deleteProcessor.setEnabled(isEnabled);
            deleteOneProcessor.setEnabled(numberOfProcessors > 1);
        }

    }

    private class ProcessorTableModel extends ExactTableModel {

        /**
         *
         */
        private static final long serialVersionUID = 1L;
        private Object[] prototypes = { "10000", new String(new char[15]), new String(new char[15]), new String(new char[15]), new Integer(1000), new Integer(1000), new String(new char[15]), "" };
        @Override
        public Object getPrototype(int columnIndex) {
            return prototypes[columnIndex + 1];
        }

        public int getRowCount() {
            return numberOfProcessors;
        }

//        @Override
//        @SuppressWarnings({"unchecked", "rawtypes"})
//        public Class getColumnClass(int col) {
//            switch (col) {
//                case COL_SCHEDULING:
//                case COL_MULTIPLICITY:
//                case COL_REPLICAS:
//                    return DisabledCellRenderer.class;
//                case COL_SPEED_FACTOR:
//                default:
//                    return Object.class;
//            }
//        }

        public int getColumnCount() {
            return NUM_COLS;
        }

        @Override
        public String getColumnName(int index) {
            switch (index) {
                case COL_NAME:
                    return "Name";
                case COL_SCHEDULING:
                    return "Scheduling";
                case COL_MULTIPLICITY:
                    return "Multiplicity";
                case COL_SPEED_FACTOR:
                    return "Speed Factor";
                default:
                    return null;
            }
        }

        @Override
        protected Object getValueAtImpl(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case COL_NAME:
                    return processorNames[rowIndex];
                case COL_SCHEDULING:
                    return PROCESSOR_SCHEDULING_TYPENAMES[processorScheduling[rowIndex]];
                case COL_MULTIPLICITY:
                    return processorMultiplicity[rowIndex];
                case COL_SPEED_FACTOR:
                    return processorSpeedFactor[rowIndex];

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
                            data.changeAllTaskProcessor(processorNames[rowIndex], newval);
                        }
                        processorNames[rowIndex] = newval;
                    }
                    break;
                }
                case COL_SCHEDULING:
                    for (int i = 0; i < PROCESSOR_SCHEDULING_TYPENAMES.length; i++) {
                        if (value == PROCESSOR_SCHEDULING_TYPENAMES[i]) { //literal strings are canonicalized, hence == is ok
                            processorScheduling[rowIndex] = i;
                            break;
                        }
                    }
                    break;
                case COL_MULTIPLICITY:{
                    try {
                        int newval = (int) Double.parseDouble((String) value);
                        if (newval >= 0) {
                            processorMultiplicity[rowIndex] = newval;
                        }
                    } catch (NumberFormatException e) {
                    }
                    break;
                }
                case COL_SPEED_FACTOR: {
                    try {
                        double newval = Double.parseDouble((String) value);
                        if (newval >= 0.0) {
                            processorSpeedFactor[rowIndex] = newval;
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
                case COL_MULTIPLICITY:
                    return true;
                case COL_SPEED_FACTOR:
                    return true;
                default:
                    return false;
            }
        }
    }



    private void initComponents() {
        processorSpinner.addChangeListener(spinnerListener);
        help.addHelp(processorSpinner, "Enter the number of processors for this system");

        processorTable = new ProcessorTable();

        /* and now some Box black magic */
        //DEK (Federico Granata) 26-09-2003
        Box processorSpinnerBox = Box.createHorizontalBox();
        //OLD
        //JLabel spinnerLabel = new JLabel("<html><font size=\"4\">Set the Number of classes (1-" + MAX_CLASSES + "):</font></html>");
        //NEW
        //@author Stefano
        JLabel spinnerLabel = new JLabel(DESCRIPTION_PROCESSORS);

        processorSpinnerBox.add(spinnerLabel);
        //END
        //BEGIN Federico Dall'Orso 9/3/2005
        //OLD
		/*
		classSpinnerBox.add(Box.createGlue());
		 */
        //NEW
        processorSpinnerBox.add(Box.createHorizontalStrut(10));
        Box numberBox = Box.createVerticalBox();

        JPanel spinnerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel numberLabel = new JLabel("Number:");
        processorSpinner.setMaximumSize(new Dimension(600, 18));
        spinnerPanel.add(numberLabel);
        spinnerPanel.add(processorSpinner);
        numberBox.add(spinnerPanel);

        numberBox.add(new JButton(addProcessor));

        numberBox.setMaximumSize(new Dimension(300, 150));

        processorSpinnerBox.add(numberBox);
        //END  Federico Dall'Orso 9/3/2005

        Box processorBox = Box.createVerticalBox();
        processorBox.add(Box.createVerticalStrut(30));
        processorBox.add(processorSpinnerBox);
        processorBox.add(Box.createVerticalStrut(10));
        JScrollPane processorTablePane = new JScrollPane(processorTable);
        processorTablePane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        processorTablePane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        processorBox.add(processorTablePane);
        processorBox.add(Box.createRigidArea(new Dimension(10, 20)));

        Box totalBox = Box.createHorizontalBox();
        totalBox.add(Box.createHorizontalStrut(20));
        totalBox.add(processorBox);
        totalBox.add(Box.createHorizontalStrut(20));

        setLayout(new BorderLayout());
        add(totalBox, BorderLayout.CENTER);
    }

    private void sync() {
        hasDeletes = false;
        processorOps.clear();

        /* sync status with data object */
        /* arrays are copied to ensure data object consistency is preserved */
        JLQNModel data = jw.getData();
        synchronized (data) {
            numberOfProcessors = data.getNumberOfProcessors();
            nameCounter = numberOfProcessors;
            processorNames = ArrayUtils.copy(data.getProcessorNames());
            processorScheduling = ArrayUtils.copy(data.getProcessorScheduling());
            processorReplicas = ArrayUtils.copy(data.getProcessorReplicas());
            processorSpeedFactor = ArrayUtils.copy(data.getProcessorSpeedFactor());
            processorMultiplicity = ArrayUtils.copy(data.getProcessorMultiplicity());

        }

        processorSpinner.setValue(new Integer(numberOfProcessors));
    }

    public ProcessorsPanel(JLQNWizard jw) {
        this.jw = jw;
        help = jw.getHelp();
        processorOps = new ArrayList<ListOp>();

        sync();

        initComponents();
        makeNames();
    }

    private void updateSizes() {
        setNumberOfProcessors(((Integer) processorSpinner.getValue()).intValue());
    }

    private void makeNames() {
        for (int i = 0; i < processorNames.length; i++) {
            if (processorNames[i] == null) {
                while (areThereDuplicates("Processor" + (nameCounter+1), i, false)) {
                    nameCounter++;
                }
                processorNames[i] = "Processor" + (++nameCounter);
            }
        }
    }

    private void setNumberOfProcessors(int number) {
        processorTable.stopEditing();
        numberOfProcessors = number;

        processorNames = ArrayUtils.resize(processorNames, numberOfProcessors, null);
        makeNames();
        processorScheduling = ArrayUtils.resize(processorScheduling, numberOfProcessors, PROCESSOR_INF);
        processorMultiplicity = ArrayUtils.resize(processorMultiplicity, numberOfProcessors, 1);
        processorReplicas = ArrayUtils.resize(processorReplicas, numberOfProcessors, 1);
        processorSpeedFactor = ArrayUtils.resize(processorSpeedFactor, numberOfProcessors, 1.0);


        processorTable.updateStructure();
        if (!deleting) {
            processorOps.add(ListOp.createResizeOp(numberOfProcessors));
        }

        processorSpinner.setValue(new Integer(numberOfProcessors));
        processorTable.updateDeleteCommand();
    }

    @Override
    public void lostFocus() {
        commit();
        //release();
    }

    @Override
    public void gotFocus() {
        sync();
        processorTable.update();
    }

    @Override
    public String getName() {
        return "Processors";
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
                    "<html><center>Processor names cannot be empty.<br>Please enter another name.</center></html>",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (value.equalsIgnoreCase("N/A")) {
            JOptionPane.showMessageDialog(this,
                    "<html><center>Processor name is invalid.<br>Please enter another name.</center></html>",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private boolean areThereDuplicates(String value, int index, boolean enableWarning) {
        boolean thereAreDupl = false;
        for (int i = 0; i < processorNames.length && !thereAreDupl; i++) {
            if (i == index || processorNames[i] == null) continue;
            thereAreDupl = thereAreDupl || processorNames[i].equalsIgnoreCase(value);
        }
        if (thereAreDupl) {
            if (enableWarning) {
                JOptionPane.showMessageDialog(this,
                        "<html><center>There is a duplication in processor names.<br>Please enter another name.</center></html>",
                        "Warning", JOptionPane.WARNING_MESSAGE);
            }
            return thereAreDupl;
        }
        return thereAreDupl;
    }

    private void commit() {
        /* stop any editing in progress */
        if (processorSpinner.getEditor().getComponent(0).hasFocus()) {
            //disgusting. there must be a better way...
            try {
                processorSpinner.commitEdit();
                updateSizes();
            } catch (java.text.ParseException e) {
            }
        }

        processorTable.stopEditing();

        JLQNModel data = jw.getData();
        synchronized (data) {

            if (hasDeletes) {
                playbackProcessorOps(data); //play back ops on the data object
            } else {
                data.resizeProcessors(numberOfProcessors, false);
            }
            data.setProcessorNames(processorNames);
            data.setProcessorScheduling(processorScheduling);
            data.setProcessorMultiplicity(processorMultiplicity);
            data.setProcessorReplicas(processorReplicas);
            data.setProcessorSpeedFactor(processorSpeedFactor);


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

    private void playbackProcessorOps(JLQNModel data) {
        for (int i = 0; i < processorOps.size(); i++) {
            ListOp lo = processorOps.get(i);
            if (lo.isDeleteOp()) {
                data.deleteProcessor(lo.getData());
            }
            if (lo.isResizeOp()) {
                data.resizeProcessors(lo.getData(), false);
            }
        }
    }}