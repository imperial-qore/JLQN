package jlqn.gui.xml;
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
public class JLQNDocumentConstants {
  public static final String DOC_PROCESSOR_NUMBER = "number";
  public static final String DOC_PROCESSOR_NAME = "name";
  public static final String DOC_PROCESSOR_SCHEDULING = "scheduling";
  public static final String DOC_PROCESSOR_QUANTUM = "quantum";
  public static final String DOC_PROCESSOR_MULTIPLICITY = "multiplicity";
  public static final String DOC_PROCESSOR_REPLICAS = "replicas";
  public static final String DOC_PROCESSOR_SPEED_FACTOR = "speed_factor";
  public static final String DOC_TASK_NUMBER = "number";
  public static final String DOC_TASK_NAME = "name";
  public static final String DOC_TASK_SCHEDULING = "scheduling";
  public static final String DOC_TASK_PROCESSOR = "processor";
  public static final String DOC_TASK_PRIORITY = "priority";
  public static final String DOC_TASK_THINK_TIME_MEAN = "think_time_mean";
  public static final String DOC_TASK_THINK_TIME_SCV = "think_time_scv";
  public static final String DOC_TASK_MULTIPLICITY = "multiplicity";
  public static final String DOC_TASK_REPLICAS = "replicas";
  public static final String DOC_ENTRY_NUMBER = "number";
  public static final String DOC_ENTRY_NAME = "name";
  public static final String DOC_ENTRY_TASK = "task";
  public static final String DOC_ENTRY_BOUND_TO_ACTIVITY = "bound_to_activity";
  public static final String DOC_ENTRY_REPLY_TO_ACTIVITY = "reply_to_activity";
  public static final String DOC_ENTRY_ARRIVAL_RATE = "arrival_rate";
  public static final String DOC_ENTRY_PRIORITY = "priority";
  public static final String DOC_ACTIVITY_NUMBER = "number";
  public static final String DOC_ACTIVITY_NAME = "name";
  public static final String DOC_ACTIVITY_TASK = "task";
  public static final String DOC_ACTIVITY_DEMAND_MEAN = "host_demand_mean";
  public static final String DOC_ACTIVITY_DEMAND_SCV = "host_demand_scv";
  public static final String DOC_CALL_NUMBER = "number";
  public static final String DOC_CALL_NAME = "name";
  public static final String DOC_CALL_ACTIVITY = "activity";
  public static final String DOC_CALL_ENTRY = "entry";
  public static final String DOC_CALL_TYPE = "type";
  public static final String DOC_CALL_MEAN_REPEAT = "mean_repeat";

  public static final String DOC_PRECEDENCE_NUMBER = "number";
  public static final String DOC_PRECEDENCE_TYPE = "type";
  public static final String DOC_PRECEDENCE_ACTIVITY_TYPE = "type"; // pre, post, or end
  public static final String DOC_PRECEDENCE_ACTIVITY = "activity";
  public static final String DOC_PRECEDENCE_PARAMS = "params";

  public static final String DOC_QUEUE_LEN = "queue_length";
  public static final String DOC_THROUGHPUT = "throughput";
  public static final String DOC_RESID_TIME = "residence_times";
  public static final String DOC_RESP_TIME = "system_response_time";
  public static final String DOC_UTIL = "utilization";
  public static final String DOC_NODE_COUNT = "node_count";
  public static final String DOC_RESULT_PROCESSOR = "processor";
  public static final String DOC_RESULT_TASK = "task";
  public static final String DOC_RESULT_ENTRY = "entry";
  public static final String DOC_RESULT_ACTIVITY = "activity";

}
