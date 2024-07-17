package jlqn.gui;
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
import jline.lang.constant.SolverType;
import jline.lang.layered.LayeredNetwork;
import jline.solvers.LayeredNetworkAvgTable;
import jline.solvers.SolverOptions;
import jline.solvers.ln.SolverLN;
import jline.solvers.lqns.SolverLQNS;
import jlqn.model.JLQNModel;
import jlqn.model.SetLayeredNetwork;
import jlqn.gui.panels.*;
import jlqn.gui.utilities.SolverClient;
import jmt.framework.gui.components.JMTMenuBar;
import jmt.framework.gui.components.JMTToolBar;
import jmt.framework.gui.help.HoverHelp;
import jmt.framework.gui.listeners.AbstractJMTAction;
import jmt.framework.gui.listeners.MenuAction;
import jmt.framework.gui.wizard.Wizard;
import jmt.framework.gui.wizard.WizardPanel;
import jmt.gui.common.CommonConstants;
import jmt.gui.common.JMTImageLoader;
import jlqn.gui.panels.AboutDialogFactory;
import jmt.manual.ChapterIdentifier;
import jmt.manual.PDFViewer;


import jmt.gui.common.panels.WarningWindow;
import jlqn.gui.xml.JLQNModelLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Locale;

public class JLQNWizard extends Wizard {
    private static final long serialVersionUID = 1L;
    private static final String TITLE = "JLQN - Layered Queueing Network Solver";
    public static final String JLQN = "JLQN";
    private static final boolean DEBUG = false;

    private JLQNModel data;
    private HoverHelp help;

    private JLabel helpLabel;

    private JLQNModelLoader modelLoader = new JLQNModelLoader(JLQNModelLoader.JLQN, JLQNModelLoader.ALL_SAVE);

    //A link to the last modified model's temporary file - used to display synopsis
    private File tempFile = null;

    private DropDownPanels dropDownPanels;

    private SolverPanel solverPanel;
    private SolverClient solver;

    public JLQNWizard() {
        this(new JLQNModel());
    }

    public JLQNWizard(String filename) {
        this(new JLQNModel());
        tempFile = new File(filename);
        modelLoader.loadModel(this.data, this, tempFile);
        System.out.print(filename);
        if (!tempFile.isFile()) {
            System.err.print("Invalid model file: " + tempFile.getAbsolutePath());
            System.exit(1);
        }
        updatePanels();
    }

    public JLQNWizard(JLQNModel data) {

        super(TITLE);
        setSize(CommonConstants.MAX_GUI_WIDTH_JMVA, CommonConstants.MAX_GUI_HEIGHT_JMVA);
        this.centerWindow();
        setIconImage(JMTImageLoader.loadImageAwt("JLQNIcon"));
        this.data = data;
        data.resetChanged();
        this.setJMenuBar(makeMenubar());
        getContentPane().add(makeToolbar(), BorderLayout.NORTH);
        //addPanel(new ClassesPanel(this));
        addPanel(new ProcessorsPanel(this));
        addPanel(new TasksPanel(this));
        addPanel(new ActivitiesPanel(this));
        addPanel(new jlqn.gui.panels.EntriesPanel(this));
        addPanel(new CallsPanel(this));
        addPanel(new PrecedencePanel(this));

        setVisible(true);
        /* END */
    }


    private AbstractJMTAction FILE_NEW = new AbstractJMTAction("New...") {

        private static final long serialVersionUID = 1L;

        {
            putValue(Action.SHORT_DESCRIPTION, "Create New Model");
            setIcon("New", JMTImageLoader.getImageLoader());
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
            putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_N));
        }

        public void actionPerformed(ActionEvent e) {
            newModel();
        }

    };
    //GUI listener to open an existing file
    private AbstractJMTAction FILE_OPEN = new AbstractJMTAction("Open...") {

        private static final long serialVersionUID = 1L;

        {
            putValue(Action.SHORT_DESCRIPTION, "Open Saved Model");
            setIcon("Open", JMTImageLoader.getImageLoader());
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_O));
        }

        public void actionPerformed(ActionEvent e) {
            open();
        }

    };

    //GUI listener to save model to a file
    private AbstractJMTAction FILE_SAVE = new AbstractJMTAction("Save as ...") {

        private static final long serialVersionUID = 1L;

        {
            putValue(Action.SHORT_DESCRIPTION, "Save Model");
            setIcon("Save", JMTImageLoader.getImageLoader());
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
        }

        public void actionPerformed(ActionEvent e) {
            save();
        }

    };

    private AbstractJMTAction FILE_EXIT = new AbstractJMTAction("Exit") {

        private static final long serialVersionUID = 1L;

        {
            putValue(Action.SHORT_DESCRIPTION, "Exits Application");
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_Q));
        }

        public void actionPerformed(ActionEvent e) {
            close();
        }

    };

    private AbstractJMTAction ACTION_SOLVE = new AbstractJMTAction("Solve") {

        private static final long serialVersionUID = 1L;

        {
            putValue(Action.SHORT_DESCRIPTION, "Solve model");
            setIcon("Sim", JMTImageLoader.getImageLoader());

            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_L));

        }

        public void actionPerformed(ActionEvent e) {
            if (checkFinish()) {
                finish();
            }
        }

    };

    private AbstractJMTAction HELP = new AbstractJMTAction("JLQN Help") {

        private static final long serialVersionUID = 1L;

        {
            putValue(Action.SHORT_DESCRIPTION, "Show JLQN help");
            setIcon("Help", JMTImageLoader.getImageLoader());
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.CTRL_MASK));
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_H));
        }

        public void actionPerformed(ActionEvent e) {
            Runnable r = new Runnable() {
                public void run() {
                    try {
                        new PDFViewer("JMVA Manual", ChapterIdentifier.JMVA);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            EventQueue.invokeLater(r);
        }

    };

    private AbstractJMTAction ABOUT = new AbstractJMTAction("About JLQN") {

        private static final long serialVersionUID = 1L;

        {
            putValue(Action.SHORT_DESCRIPTION, "About JLQN");
        }

        public void actionPerformed(ActionEvent e) {
            showAbout();
        }

    };

    private void showAbout() {
        AboutDialogFactory.showJLQN(this);
    }

    private JMTMenuBar makeMenubar() {
        JMTMenuBar jmb = new JMTMenuBar(JMTImageLoader.getImageLoader());
        AbstractJMTAction[] menuItems = new AbstractJMTAction[] {
                new MenuAction("File", new AbstractJMTAction[] { FILE_NEW, FILE_OPEN, FILE_SAVE, null, FILE_EXIT }),
                new MenuAction("Action", new AbstractJMTAction[] { ACTION_SOLVE, /*ACTION_RANDOMIZE_MODEL, null, SWITCH_TO_SIMULATOR,*/ null,
                        ACTION_NEXT, ACTION_PREV }),
                //new MenuAction("Help", new AbstractJMTAction[] { HELP, null, ABOUT })
        };

        jmb.populateMenu(menuItems);
        return jmb;
    }

    /**
     * @return the toolbar for the exact wizard. Shamelessly uses icon from the main jmt frame
     */
    protected JMTToolBar makeToolbar() {
        JMTToolBar tb = new JMTToolBar(JMTImageLoader.getImageLoader());
        tb.setFloatable(false);

        //null values add a gap between toolbar icons
        AbstractJMTAction[] actions = { FILE_NEW, FILE_OPEN, FILE_SAVE, null, ACTION_SOLVE, null, HELP, null };
        String[] htext = { "Creates a new model", "Opens a saved model", "Saves the current model", "Solves the current model",
                "Show help"};
        ArrayList<AbstractButton> buttons = new ArrayList<AbstractButton>();
        buttons.addAll(tb.populateToolbar(actions));

        //adds the algorithm selection box

        dropDownPanels = new DropDownPanels(this);
        tb.add(dropDownPanels);

        // Adds help
        for (int i = 0; i < buttons.size(); i++) {
            AbstractButton button = buttons.get(i);
            help.addHelp(button, htext[i]);
        }

        return tb;
    }

    @Override
    protected JComponent makeButtons() {
        help = new HoverHelp();
        helpLabel = help.getHelpLabel();

        helpLabel.setBorder(BorderFactory.createEtchedBorder());
        //helpLabel.setHorizontalAlignment(SwingConstants.CENTER);

        ACTION_FINISH.putValue(Action.NAME, "Solve");
        ACTION_CANCEL.putValue(Action.NAME, "Exit");

        JPanel buttons = new JPanel();

        JButton button_finish = new JButton(ACTION_FINISH);
        help.addHelp(button_finish, "Validates the system and starts the solver");
        JButton button_cancel = new JButton(ACTION_CANCEL);
        help.addHelp(button_cancel, "Exits the wizard discarding all changes");
        JButton button_next = new JButton(ACTION_NEXT);
        help.addHelp(button_next, "Moves on to the next step");
        JButton button_previous = new JButton(ACTION_PREV);
        help.addHelp(button_previous, "Goes back to the previous step");
        buttons.add(button_previous);
        buttons.add(button_next);
        buttons.add(button_finish);
        buttons.add(button_cancel);

        JPanel labelbox = new JPanel();
        labelbox.setLayout(new BorderLayout());
        labelbox.add(Box.createVerticalStrut(30), BorderLayout.WEST);
        labelbox.add(helpLabel, BorderLayout.CENTER);

        Box buttonBox = Box.createVerticalBox();
        buttonBox.add(buttons);
        buttonBox.add(labelbox);
        return buttonBox;
    }

    private void newModel() {
        currentPanel.lostFocus();
        if (checkForSave("<html>Save changes before creating a new model?</html>")) {
            return;
        }
        Rectangle bounds = this.getBounds();
        JLQNWizard jlqnWizard = new JLQNWizard();
        updateTitle(null);
        jlqnWizard.setBounds(bounds);
        jlqnWizard.setVisible(true);
        this.setVisible(false);
        this.dispose();
    }

    public boolean checkForSave(String msg) {
        // Checks if there's an old graph to save
        if (data != null && data.isChanged()) {
            int resultValue = JOptionPane.showConfirmDialog(this, msg, "JLQN - Warning", JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (resultValue == JOptionPane.YES_OPTION) {
                save();
                return true;
            }
            if (resultValue == JOptionPane.CANCEL_OPTION) {
                return true;
            }
        }
        return false;
    }

    /**
     * Saves current model
     */
    private void save() {
        currentPanel.lostFocus();
        if (!checkFinish()) {
            return; // panels with problems are expected to notify the user by themselves
        }
        int retval = modelLoader.saveModel(data, this, null);
        switch (retval) {
            case JLQNModelLoader.SUCCESS:
                data.resetChanged();
                updateTitle(modelLoader.getSelectedFile().getName());
                break;
            case JLQNModelLoader.FAILURE:
                JOptionPane.showMessageDialog(this, modelLoader.getFailureMotivation(), "Error", JOptionPane.ERROR_MESSAGE);
                break;
        }
    }

    /**
     * Opens a new model
     */
    private void open() {
        currentPanel.lostFocus();
        if (checkForSave("<html>Save changes before opening a saved model?</html>")) {
            return;
        }
        JLQNModel newdata = new JLQNModel();
        int retval = modelLoader.loadModel(newdata, this, null);
        switch (retval) {
            case JLQNModelLoader.SUCCESS:
            case JLQNModelLoader.WARNING:
                data = newdata;
                currentPanel.gotFocus();
                updateTitle(modelLoader.getSelectedFile().getName());
                tabbedPane.setSelectedIndex(0);
                break;
            case JLQNModelLoader.FAILURE:
                JOptionPane.showMessageDialog(this, modelLoader.getFailureMotivation(), "Error", JOptionPane.ERROR_MESSAGE);
                break;
        }
        tempFile = modelLoader.getSelectedFile();
        updatePanels();

        // Shows warnings if any
        if (retval == JLQNModelLoader.WARNING) {
            new WarningWindow(modelLoader.getLastWarnings(), this, modelLoader.getInputFileFormat(), JLQN).show();
        }
    }

    @Override
    protected boolean cancel() {
        if (currentPanel != null) {
            currentPanel.lostFocus();
        }
        return !checkForSave("<html>Save changes before closing?</html>");
    }

    private void solve() {
        if (solver == null) {
            solver = new SolverClient(this);
        }
        JLQNModel newdata = new JLQNModel(data);
        newdata.resetResults();

        // Replace check -> set with check and set at the same time.
        // Provide newdata with a error stringbuilder, then make it return either NULL or the new layered network.
        // If NULL, print the error stringbuilder
        data = newdata;
        if (!data.inputValid()) {
            JOptionPane.showMessageDialog(this,
                    String.format("<html><center>The model is invalid.</center><div align='left'>%s</div></html>",
                            data.getErrors()),
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        StringBuilder errors = new StringBuilder();
        LayeredNetwork lqnmodel = SetLayeredNetwork.SetLayeredNetworkFromJLQN(data, errors);
        if (errors.length() > 0) {
            JOptionPane.showMessageDialog(this,
                    String.format("<html><center>The model is invalid.</center><div align='left'>%s</div></html>",
                            errors),
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        SetLayeredNetwork.outputXML(lqnmodel);
        switch (data.getSolverType()) {
            case LN:
                try {
                    SolverLN lnSolver = new SolverLN(lqnmodel, new SolverOptions(SolverType.LN));
                    final LayeredNetworkAvgTable lnAvgTable = (LayeredNetworkAvgTable) lnSolver.getEnsembleAvg(); // Create this
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            createSolutionWindow(lnAvgTable, SolverType.LN);
                        }
                    });
                } catch (Exception e) {
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    String exceptionAsString = sw.toString();
                    JTextArea msg = new JTextArea(exceptionAsString);
                    msg.setEditable(false);
                    JOptionPane.showMessageDialog(this, msg, "Solver LN Exception", JOptionPane.ERROR_MESSAGE);
                }
                break;
            case LQNS:
                try {
                    SolverLQNS lqnsSolver = new SolverLQNS(lqnmodel);
                    final LayeredNetworkAvgTable lqnsAvgTable = lqnsSolver.getAvgTable();
                    lqnsAvgTable.printTable();
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            createSolutionWindow(lqnsAvgTable, SolverType.LQNS);
                        }
                    });
                }  catch (Exception e) {
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    String exceptionAsString = sw.toString();
                    JTextArea msg = new JTextArea(exceptionAsString);
                    msg.setEditable(false);
                    JOptionPane.showMessageDialog(this, msg, "Solver LQNS Exception", JOptionPane.ERROR_MESSAGE);
                }
                break;
            default:
                try {
                    SolverLN lnSolver = new SolverLN(lqnmodel, new SolverOptions(SolverType.LN));
                    final LayeredNetworkAvgTable lnAvgTable = (LayeredNetworkAvgTable) lnSolver.getEnsembleAvg(); // Create this
                    SolverLQNS lqnsSolver = new SolverLQNS(lqnmodel);
                    final LayeredNetworkAvgTable lqnsAvgTable = lqnsSolver.getAvgTable();
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            createSolutionWindow(lnAvgTable, SolverType.LN, lqnsAvgTable, SolverType.LQNS);
                        }
                    });
                } catch (Exception e) {
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    String exceptionAsString = sw.toString();
                    JTextArea msg = new JTextArea(exceptionAsString);
                    msg.setEditable(false);
                    JOptionPane.showMessageDialog(this, msg, "Solver LN Exception", JOptionPane.ERROR_MESSAGE);
                }
                break;
        }
        updatePanels();
        currentPanel.gotFocus();
    }

    private void createSolutionWindow(LayeredNetworkAvgTable avgTable1, SolverType solverType1, LayeredNetworkAvgTable avgTable2, SolverType solverType2) {
        JTabbedPane jtp = new JTabbedPane();
        String resultTitle = "JLQN Solutions";
        JFrame solutionWindow = new JFrame(resultTitle);
        solutionWindow.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        solutionWindow.getContentPane().add(jtp);
        solutionWindow.setIconImage(this.getIconImage());
        jtp.add(new AvgTablePanel(this, avgTable1, solverType1));
        jtp.add(new AvgTablePanel(this, avgTable2, solverType2));
        Rectangle rect = this.getBounds();
        solutionWindow.setBounds(rect.x + 20, rect.y + 20, rect.width, rect.height);
        solutionWindow.setVisible(true);
    }

    private void createSolutionWindow(LayeredNetworkAvgTable avgTable, SolverType solverType) {
        JTabbedPane jtp = new JTabbedPane();
        String resultTitle = "JLQN Solutions";
        JFrame solutionWindow = new JFrame(resultTitle);
        solutionWindow.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        solutionWindow.getContentPane().add(jtp);
        solutionWindow.setIconImage(this.getIconImage());
        jtp.add(new AvgTablePanel(this, avgTable, solverType));
//        jtp.add(new QueueLenPanel(this, avgTable));
//        jtp.add(new UtilizationPanel(this, avgTable));
//        jtp.add(new RespTimePanel(this, avgTable));
//        jtp.add(new ResidTimePanel(this, avgTable));
//        jtp.add(new ThroughputPanel(this, avgTable));

        //BoundingBox of main window
        Rectangle rect = this.getBounds();
        solutionWindow.setBounds(rect.x + 20, rect.y + 20, rect.width, rect.height);
        solutionWindow.setVisible(true);
    }

    public void updatePanels() {
        if (data == null) {
            return;
        }

        for (int i = 0; i < panelCount; i++) {
            if (panels.get(i) instanceof WizardPanel) {
                panels.get(i).gotFocus();
            }
        }
    }

    @Override
    protected void updateActions() {
        super.updateActions();
        if (currentIndex < (panelCount - 1)) {
            if (!tabbedPane.isEnabledAt(currentIndex + 1)) {
                ACTION_NEXT.setEnabled(false);
            }
        }
        if (currentIndex > 0 && currentIndex < tabbedPane.getComponentCount()) {
            if (!tabbedPane.isEnabledAt(currentIndex - 1)) {
                ACTION_PREV.setEnabled(false);
            }
        }
        updatePanels();
    }

    public JLQNModel getData() {
        return data;
    }
    public HoverHelp getHelp() {
        return help;
    }

    /**
     * Sets the file name to be shown in the title
     * @param filename the file name or null to remove it
     */
    public void updateTitle(String filename) {
        if (filename != null) {
            setTitle(TITLE + " - " + filename);
        } else {
            setTitle(TITLE);
        }
    }

    @Override
    protected void finish() {
        solve();
    }


    public static void main(String[] args) {
        Locale.setDefault(Locale.ENGLISH);

        new JLQNWizard();
    }


}
