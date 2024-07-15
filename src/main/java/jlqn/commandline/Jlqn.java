/**
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

package jlqn.commandline;

import java.io.File;

import jlqn.gui.exact.JLQNWizard;

public class Jlqn {

	//private static final String OPTION_SEED = "-seed";

	public static void help() {
		System.err.println("Usage: jlqn.commandline.Jlqn [modelfilename]");
		//System.err.println("  -seed N : sets the random number seed to N (e.g., N=23000)");
		System.exit(0);
	}

	public static void main(String[] args) throws Exception {
		if (args.length < 1) {
			new JLQNWizard();
		} else {
			File model = new File(args[0]);
			if (!model.isFile()) {
				System.err.print("Invalid model file: " + model.getAbsolutePath());
				System.exit(1);
			}
			new JLQNWizard(args[0]);
		}
	}

}
