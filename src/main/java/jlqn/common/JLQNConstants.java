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

package jlqn.common;

/**
 * This class contains some constants for exact models.
 * @author alyf (Andrea Conti), Bertoli Marco
 * @version Date: 11-set-2003 Time: 16.40.25
 *
 * @author Kourosh Sheykhvand
 *  Added modifications regarding ReferenceStation and throughput
 *  Nov 2013
 */

public interface JLQNConstants {
    public static final boolean DEBUG = true;

    /** HTML gray text START*/
    public static final String GRAY_S = "<html><font color=\"aaaaaa\">";
    /** HTML gray text END*/
    public static final String GRAY_E = "</font></html>";

    public static final String[] PROCESSOR_SCHEDULING_TYPENAMES = {
            "FCFS",
            "INF",
            "PS",
            "SIRO",
//            "Discriminatory processor-sharing",
//            "Generalized processor-sharing",
            "SEPT",
//            "Shortest job first",
            "HOL"
    };
    public static final String[] TASK_SCHEDULING_NAMES = {
            "REF",
//            "Bursty Source",
            "FCFS",
            "HOL",
//            "Polling Server",
            "INF",
//            "Preemptive Priority Server",
//            "Semaphore Pseudo-Task",
//            "Read-Write Lock Pseudo-Task"
    };

    public static final String[] PRECEDENCE_TYPES = {
            "SEQUENCE",
            "OR FORK",
            "OR JOIN",
            "AND FORK",
            "AND JOIN",
            "LOOP",
//            "CACHE_ACCESS",
    };

    public static int PRECEDENCE_SEQUENCE = 0;
    public static int PRECEDENCE_OR_FORK = 1;
    public static int PRECEDENCE_OR_JOIN = 2;
    public static int PRECEDENCE_AND_FORK = 3;
    public static int PRECEDENCE_AND_JOIN = 4;
    public static int PRECEDENCE_LOOP = 5;

    public enum SolverType {
        ALL, LQNS, LN
    }

    public enum ViewerType {
        WIZ, GRAPH
    }

    public static final String[] CALL_TYPES = {
      "Synchronous", "Asynchronous", "Forwarding"
    };

    //ArrayList<String> TASK_NAME = new ArrayList<String>();
    public static final int TASK_REF = 0;
    public static final int TASK_FCFS = 1;
    public static final int TASK_HOL = 2;
    public static final int TASK_INF = 3;
//    public static final int TASK_PS = 4;
//    public static final int TASK_PDS = 5;
//    public static final int TASK_PPS = 6;
//    public static final int TASK_SPT = 7;
//    public static final int TASK_RWLPT = 8;


    public static final int PROCESSOR_FCFS = 0;
    public static final int PROCESSOR_INF = 1;
    public static final int PROCESSOR_PS = 2;
    public static final int PROCESSOR_SIRO = 3;
    public static final int PROCESSOR_SEPT = 4;
    public static final int PROCESSOR_HOL = 5;
//    public static final int PROCESSOR_PS_HOL = 6;
//    public static final int PROCESSOR_PS_PPR = 7;
//    public static final int PROCESSOR_CFS = 8;
    public static final int MAX_PROCESSORS = 100;
    public static final int MAX_ENTRIES = 100;
    public static final int MAX_TASKS = 300;
    public static final int MAX_CALLS = 500;

    public static final String DESCRIPTION_PROCESSORS = "<html><body align=\"left\"><font size=\"4\"><b>Processors characteristics</b></font>"
            + "<font size=\"3\"><br>Number, name, type of processor scheduling, quantum, multiplicity, replicas and speed factor. </font></body></html>";

    public static final String DESCRIPTION_ENTRIES = "<html><body align=\"left\"><font size=\"4\"><b>Entries characteristics</b></font>"
            + "<font size=\"3\"><br>Number, name, arrival rate, priority.  </font></body></html>";

    public static final String DESCRIPTION_TASKS = "<html><body align=\"left\"><font size=\"4\"><b>Tasks characteristics</b></font>"
            + "<font size=\"3\"><br>Processor, name, scheduling, priority, think time, multiplicity, replicas. </font></body></html>";
    public static final int MAX_ACTIVITIES = 300;
    public static final String DESCRIPTION_ACTIVITES = "<html><body align=\"left\"><font size=\"4\"><b>Activities characteristics</b></font>"
            + "<font size=\"3\"><br>Number, name, entry it belongs to, type, think time.  </font></body></html>";
    public static final String DESCRIPTION_CALLS = "<html><body align=\"left\"><font size=\"4\"><b>Calls characteristics</b></font>"
            + "<font size=\"3\"><br>Name, activity that initiates the call, entry that is being called, call type, mean repeat times.  </font></body></html>";

    public static final String DESCRIPTION_PRECEDENCES = "<html><body align=\"left\"><font size=\"4\"><b>Precedence characteristics</b></font>"
            + "<font size=\"3\"><br>Type, Pre Activities, Post Activities.  </font></body></html>";

    public static final String DESCRIPTION_PRECEDENCE_ACTIVITIES = "<html><body align=\"left\"><font size=\"4\"><b>Precedence Activity characteristics</b></font>"
            + "<font size=\"3\"><br>Activitiy, Parameters that depend on the precedence type (Might not be present for some).  </font></body></html>";
    public static final String DESCRIPTION_THROUGHPUTS = "<html><body align=\"left\"><font size=\"4\"><b>Throughput</b></font>"
            + "<font size=\"3\"><br>Throughput of each node.</font></body></html>";
    public static final String DESCRIPTION_RESPONSETIMES = "<html><body align=\"left\"><font size=\"4\"><b>Response Time</b></font>"
            + "<font size=\"3\"><br>Response times of each node. </font></body></html>";
    public static final String DESCRIPTION_RESIDENCETIMES = "<html><body align=\"left\"><font size=\"4\"><b>Residence Times</b></font>"
            + "<font size=\"3\"><br>Residence times of each node. </font></body></html>";
    public static final String DESCRIPTION_UTILIZATIONS = "<html><body align=\"left\"><font size=\"4\"><b>Utilization</b></font>"
            + "<font size=\"3\"><br>Utilization of each node.</font></body></html>";

    public static final String DESCRIPTION_QUEUELENGTH = "<html><body align=\"left\"><font size=\"4\"><b>Queue Length</b></font>"
            + "<font size=\"3\"><br>Queue length of each node.</font></body></html>";

    public static final String DESCRIPTION_AVGTABLE = "<html><body align=\"left\"><font size=\"4\"><b>Average Results</b></font>"
            + "<font size=\"3\"><br>Average value of performance measures at steady-state.</font></body></html>";

}

