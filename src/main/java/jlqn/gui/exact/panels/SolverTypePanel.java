package jlqn.gui.exact.panels;

import jlqn.analytical.JLQNConstants;
import jlqn.analytical.JLQNModel;
import jmt.framework.gui.help.HoverHelp;
import jlqn.gui.exact.JLQNWizard;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SolverTypePanel extends JPanel {
  private static final long serialVersionUID = 1L;
  private static final String LABEL = "Solver Type:";

  private HoverHelp help;
  private JLQNWizard jw;
  private JComboBox<String> solverTypes;
  private JLabel solverLabel;

  private ActionListener ACTION_CHANGE_SOLVER = new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
      solverTypes = (JComboBox<String>) e.getSource();
      String solverType = (String) solverTypes.getSelectedItem();
      JLQNConstants.SolverType type = JLQNConstants.SolverType.ALL;

      switch (solverType) {
        case "LQNS":
          type = JLQNConstants.SolverType.LQNS;
          break;
        case "LN":
          type = JLQNConstants.SolverType.LN;
          break;
        case "ALL":
        default:
          type = JLQNConstants.SolverType.ALL;
          break;
      }
      JLQNModel data = jw.getData();
      synchronized (data) {
        // TODO: On Solver Change, remove values that no longer fit the solver
        // Keep values that seep the solver
        if (data.getSolverType() != type) {
          System.out.printf("Solver Swapped to %s%n", type);
          data.setSolverType(type);
        }
      }
    }
  };

  public SolverTypePanel(JLQNWizard jw) {
    super(new BorderLayout());
    this.jw = jw;
    help = jw.getHelp();

    initialize();
  }

  private void initialize() {
    JPanel mainPanel = new JPanel(new FlowLayout());
    mainPanel.add(solverLabel());
    mainPanel.add(solverList());
    this.add(mainPanel, BorderLayout.WEST);
  }

  private JComponent solverLabel() {
    Dimension d = new Dimension(65, 30);
    solverLabel = new JLabel(LABEL);
    solverLabel.setMaximumSize(d);
    solverLabel.setFocusable(false);
    help.addHelp(solverLabel, "Solver used to solve the model");
    return solverLabel;
  }

  private JComponent solverList() {
    String[] solverNameList = {"LN", "LQNS", "LN+LQNS"};
    solverTypes = new JComboBox<>(solverNameList);

    Dimension d = new Dimension(160, 30);
    solverTypes.setMaximumSize(d);
    JLQNModel data = jw.getData();
    synchronized (data) {
      solverTypes.setSelectedItem(data.getSolverType());
    }
    solverTypes.addActionListener(ACTION_CHANGE_SOLVER);
    solverTypes.setRenderer(new DefaultListCellRenderer());


    help.addHelp(solverTypes, "Algorithm for solving model");
    return solverTypes;
  }

}
