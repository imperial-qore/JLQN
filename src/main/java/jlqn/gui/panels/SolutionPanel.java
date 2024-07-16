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

import javax.swing.*;

import jmt.framework.gui.help.HoverHelp;
import jmt.framework.gui.wizard.WizardPanel;
import jmt.gui.common.CommonConstants;
import jmt.gui.exact.table.ExactTableModel;
import jlqn.gui.table.JLQNResultsTable;
import jlqn.model.JLQNModel;
import jlqn.gui.JLQNWizard;

import java.awt.*;
/**
 * generic solution panel
 */
public abstract class SolutionPanel extends WizardPanel {
    private static final long serialVersionUID = 1L;

    protected String helpText;

    protected String name;

    protected HoverHelp help;

    protected JLQNModel data;

    protected int numberOfProcessors;

    protected String[] processorNames;
    protected int numberOfTasks;
    protected String[] taskNames;
    protected int numberOfEntries;
    protected String[] entryNames;

    protected int numberOfActivities;
    protected String[] activityNames;
    protected boolean resultsOK;
    protected JLQNResultsTable table;
    protected JLQNWizard jw;
    protected JLabel statusLabel = new JLabel();
    protected int rowCounts;
    protected String[] rowNames;
    private JTabbedPane tabber = new JTabbedPane();

    public SolutionPanel(JLQNWizard jw) {
        this.jw = jw;
        help = jw.getHelp();

        /* sync status with data object */
        sync();
        initComponents();
    }
    /**
     * gets status from data object
     */
    protected void sync() {
        /* arrays are copied to ensure data object consistency is preserved */
        data = jw.getData();
        numberOfProcessors = data.getNumberOfProcessors();
        numberOfTasks = data.getNumberOfTasks();
        numberOfEntries = data.getNumberOfEntries();
        numberOfActivities = data.getNumberOfActivities();
        rowCounts =  numberOfProcessors + numberOfTasks + numberOfEntries + numberOfActivities;
        rowNames = new String[rowCounts];
        processorNames = data.getProcessorNames();
        taskNames = data.getTaskNames();
        entryNames = data.getEntryNames();
        activityNames = data.getActivityNames();
        System.arraycopy(processorNames, 0, rowNames, 0, numberOfProcessors);
        System.arraycopy(taskNames, 0, rowNames, numberOfProcessors, numberOfTasks);
        System.arraycopy(entryNames, 0, rowNames, numberOfProcessors + numberOfTasks, numberOfEntries);
        System.arraycopy(activityNames, 0, rowNames,numberOfProcessors + numberOfTasks + numberOfEntries, numberOfActivities);
        resultsOK = data.areResultsOK();
        statusLabel.setVisible(!resultsOK);
    }
    /**
     * Set up the panel contents and layout
     */
    protected void initComponents() {
        setLayout(new BorderLayout());
        add(tabber, BorderLayout.CENTER);
        table = new JLQNResultsTable(getTableModel(), help);
        table.setRowHeight(CommonConstants.ROW_HEIGHT);
        statusLabel.setForeground(Color.RED);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        statusLabel.setText("WARNING: parameters have been changed since this solution was computed!");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        help.addHelp(statusLabel, "This solution is not current with the parameters of the model. Click solve to compute a new solution.");

        JPanel intPanel = new JPanel(new BorderLayout(10, 10));

        JScrollPane jsp = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        JLabel descrLabel = new JLabel(getDescriptionMessage());

        intPanel.add(descrLabel, BorderLayout.NORTH);
        intPanel.add(jsp, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(intPanel, BorderLayout.CENTER);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    }
    public void gotFocus() {
        sync();
        table.updateStructure();
    }

    public void help() {
        JOptionPane.showMessageDialog(null, helpText, "Help", JOptionPane.INFORMATION_MESSAGE);
    }

    public String getName() {
        return name;
    }
    /**Returns table model that must describe data contained in this panel's table.
     * @return :this panel's table model.
     * */
    protected abstract ExactTableModel getTableModel();
    /**Returns this panel's purpose description.
     * @return :message describing data contained in this panel.*/
    protected abstract String getDescriptionMessage();

}
