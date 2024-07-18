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
import jmt.framework.gui.help.HoverHelp;
import jlqn.gui.JLQNWizard;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DropDownPanels extends JPanel {
  private static final long serialVersionUID = 1L;
  private static final String SOLVER_LABEL = "Solver:";
//  private static final String VIEWER_LABEL = "                                                                                                               Viewer Type:";
  private static final String VIEWER_LABEL = "Layer Viewer:";

  private HoverHelp help;
  public JLQNWizard jw;
  public JComboBox<String> solverTypes;
  public JComboBox<String> viewTypes;
  private JLabel solverLabel;
  private JLabel viewerLabel;

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
          System.out.printf("Solver changed to %s%n", type);
          data.setSolverType(type);
        }
      }
    }
  };

  private ActionListener ACTION_CHANGE_VIEWER = new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
      viewTypes = (JComboBox<String>) e.getSource();
      String viewType = (String) viewTypes.getSelectedItem();
      JLQNConstants.ViewerType type = JLQNConstants.ViewerType.WIZ;

      switch (viewType) {
        case "JSIMwiz":
          type = JLQNConstants.ViewerType.WIZ;
          break;
        case "JSIMgraph":
          type = JLQNConstants.ViewerType.GRAPH;
          break;
        default:
      }
      JLQNModel data = jw.getData();
      synchronized (data) {
        // TODO: On Solver Change, remove values that no longer fit the solver
        // Keep values that seep the solver
        if (data.getViewerType() != type) {
          System.out.printf("Viewer changed to %s%n", type);
          data.setViewType(type);
        }
      }
    }
  };

  public DropDownPanels(JLQNWizard jw) {
    super(new BorderLayout());
    this.jw = jw;
    help = jw.getHelp();

    initialize();
  }

  private void initialize() {
    JPanel mainPanel = new JPanel(new FlowLayout());
    mainPanel.add(solverLabel());
    mainPanel.add(solverList());
    mainPanel.add(viewerLabel());
    mainPanel.add(viewerList());
    this.add(mainPanel, BorderLayout.WEST);
  }


  private JComponent solverLabel() {
    Dimension d = new Dimension(65, 30);
    solverLabel = new JLabel(SOLVER_LABEL);
    solverLabel.setMaximumSize(d);
    solverLabel.setFocusable(false);
    help.addHelp(solverLabel, "Solver used to solve the model");
    return solverLabel;
  }

  private JComponent viewerLabel() {
    Dimension d = new Dimension(65, 30);
    viewerLabel = new JLabel(VIEWER_LABEL);
    viewerLabel.setMaximumSize(d);
    viewerLabel.setFocusable(false);
    help.addHelp(viewerLabel, "Solver used to solve the model");
    return viewerLabel;
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

  private JComponent viewerList() {
    String[] solverNameList = {"JSIMwiz", "JSIMgraph"};
    viewTypes = new JComboBox<>(solverNameList);

    Dimension d = new Dimension(160, 30);
    viewTypes.setMaximumSize(d);
    JLQNModel data = jw.getData();
    synchronized (data) {
      viewTypes.setSelectedItem(data.getViewerType());
    }
    viewTypes.addActionListener(ACTION_CHANGE_VIEWER);
    viewTypes.setRenderer(new DefaultListCellRenderer());


    help.addHelp(viewTypes, "Tool to view the model");
    return viewTypes;
  }

}
