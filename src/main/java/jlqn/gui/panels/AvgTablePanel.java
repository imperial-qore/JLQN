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

import jline.lang.constant.SolverType;
import jline.solvers.LayeredNetworkAvgTable;
import jlqn.common.JLQNConstants;
import jlqn.gui.panels.SolutionPanel;
import jmt.gui.table.ExactTableModel;
import jlqn.gui.JLQNWizard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AvgTablePanel extends SolutionPanel {
    private List<List<Double>> queueLength;
    private List<List<Double>> utilization;
    private List<List<Double>> respTimes;
    private final List<List<Double>> residTimes;
    private final List<List<Double>> throughput;

    public AvgTablePanel(JLQNWizard jw, LayeredNetworkAvgTable avgTable, SolverType solver) {
        super(jw);
        queueLength = new ArrayList<>();
        helpText = "<html>"+solver.name()+"</html>";
        name = solver.name();
        List<Double> avgTableQLen = avgTable.getQLen();
        List<Double> avgTableUtil = avgTable.getUtil();
        List<Double> avgTableRespT = avgTable.getRespT();
        List<Double> avgTableResidT = avgTable.getResidT();
        List<Double> avgTableTput = avgTable.getTput();
        if (avgTableQLen.size() > rowNames.length) {
            avgTableQLen.remove(0);
            avgTableUtil.remove(0);
            avgTableRespT.remove(0);
            avgTableResidT.remove(0);
            avgTableTput.remove(0);
        }

        for (Double qLen : avgTableQLen) {
            queueLength.add(new ArrayList<>(Collections.singleton(qLen)));
        }
        utilization = new ArrayList<>();

        for (Double util : avgTableUtil) {
            utilization.add(new ArrayList<>(Collections.singleton(util)));
        }
        respTimes = new ArrayList<>();
        for (Double respT : avgTableRespT) {
            respTimes.add(new ArrayList<>(Collections.singleton(respT)));
        }
        residTimes = new ArrayList<>();
        for (Double residT : avgTableResidT) {
            residTimes.add(new ArrayList<>(Collections.singleton(residT)));
        }
        throughput = new ArrayList<>();
        for (Double tput : avgTableTput) {
            throughput.add(new ArrayList<>(Collections.singleton(tput)));
        }
    }

    @Override
    protected void sync() {
        super.sync();
        /* END */
    }

    @Override
    protected ExactTableModel getTableModel() {
        return new TPTableModel();
    }

    @Override
    protected String getDescriptionMessage() {
        return JLQNConstants.DESCRIPTION_AVGTABLE;
    }

    private class TPTableModel extends ExactTableModel {

        private static final long serialVersionUID = 1L;

        TPTableModel() {
            prototype = new Double(1000);
            int maxLen = 0;
            int maxLenRow = 0;
            for (int i = 0; i < rowNames.length; i++) {
                if (rowNames[i].length() > maxLen) {
                    maxLen = rowNames[i].length();
                    maxLenRow = i;
                }
            }
            rowHeaderPrototype = rowNames[maxLenRow]+"     ";
        }

        @Override
        public int getRowCount() {
            return rowCounts;
        }

        @Override
        public int getColumnCount() {
            return 5;
        }

        @Override
        protected Object getRowName(int rowIndex) {
            return rowNames[rowIndex];
        }

        @Override
        public String getColumnName(int index) {
            String ret = "";
            if (index == 0) {
                ret = "Queue Length ";
            } else if (index == 1) {
                ret = "Utilization ";
            } else if (index == 2) {
                ret = "Response Time ";
            } else if (index == 3) {
                ret = "Residence Time ";
            } else if (index == 4) {
                ret = "Throughput ";
            }
            return ret;
        }

        @Override
        protected Object getValueAtImpl(int rowIndex, int columnIndex) {
            Double d = -1.0;
            if (rowIndex >= 0 && columnIndex == 0) {
                d = queueLength.get(rowIndex).get(0);
            } else if (rowIndex >= 0 && columnIndex == 1) {
                d = utilization.get(rowIndex).get(0);
            } else if (rowIndex >= 0 && columnIndex == 2) {
                d = respTimes.get(rowIndex).get(0);
            } else if (rowIndex >= 0 && columnIndex == 3) {
                d = residTimes.get(rowIndex).get(0);
            } else if (rowIndex >= 0 && columnIndex == 4) {
                d = throughput.get(rowIndex).get(0);
            }

            if (d == null || Double.isNaN(d)) {
                //return new String("·");
                return new String("---");
            } else {
                return new Double(d);
            }
        }

    }

}
