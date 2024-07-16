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

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import jmt.framework.gui.components.JMTDialog;
import jmt.gui.common.CommonConstants;
import jmt.gui.common.JMTImageLoader;
import jmt.gui.common.startScreen.GraphStartScreen;

/**
 * <p>Title: About Dialog Factory</p>
 * <p>Description: This class will create dialogs to show credits for each
 * application.</p>
 *
 * @author Bertoli Marco
 *         Date: 1-feb-2006
 *         Time: 16.42.10
 */
public class AboutDialogFactory {

    public enum Company {

        POLIMI, ICL;
        /**
         * @return the logo of the contributor company
         */
        public JComponent getLogo() {
            switch (this) {
                case POLIMI:
                    JLabel logo = new JLabel(GraphStartScreen.HTML_POLI);
                    logo.setHorizontalTextPosition(SwingConstants.TRAILING);
                    logo.setVerticalTextPosition(SwingConstants.CENTER);
                    logo.setIconTextGap(10);
                    logo.setIcon(JMTImageLoader.loadImage("logo", new Dimension(70, 70)));
                    return logo;
                case ICL:
                    return new JLabel(JMTImageLoader.loadImage("logo_icl", new Dimension(-1, 40)));
                default:
                    return null;
            }
        }

    }
    public static final String IMG_JLQNICON = "JLQNIcon";


//	Li, Zhuoyuan
//* Bao, Yang
//* Du, Lingxiao
//* Li, Songtao
//* Luo, Dan
//* Wang, Zifeng

    /** JWAT main contributors */
    private static final Contributors[] JLQN = {
            new Contributors(Company.ICL, "Luo Dan", "Du Lingxiao", "Li Songtao", "Bao Yang", "Yang Yelun", "Wang Zifeng", "Li Zhuoyuan")
    };

    /** Overall contributors is merged by the others */
    private static final Contributors[] ALL;

    static {
        List<Contributors> allContributors = new ArrayList<AboutDialogFactory.Contributors>();
        allContributors.addAll(Arrays.asList(JLQN));
        Map<Company, Set<String>> m = new EnumMap<Company, Set<String>>(Company.class);
        for (Contributors c : allContributors) {
            Set<String> set = m.get(c.getCompany());
            if (set == null) {
                set = new HashSet<String>();
                m.put(c.getCompany(), set);
            }
            set.addAll(c.getNames());
        }
        ALL = new Contributors[m.size()];
        int idx = 0;
        for (Company c : m.keySet()) {
            Set<String> names = m.get(c);
            ALL[idx] = new Contributors(c, names.toArray(new String[names.size()]));
            idx++;
        }
    }

    /**
     * Variables
     */
    private static final long AUTOCLOSE_TIMEOUT = 2500;

    private static boolean autoJMVAshown = false;

    /**
     * Creates a new modal JMTDialog with specified owner and with panel inside, displaying current text.
     * @param owner owner of the dialog. If it is null or invalid, created dialog will not
     * be modal
     * @param autoclose to automatically close the dialog after a timeout
     * @return created dialog
     */
    protected static JMTDialog createDialog(Window owner, AboutDialogPanel panel, boolean autoclose) {
        final JMTDialog dialog;
        if (owner == null) {
            dialog = new JMTDialog();
        } else if (owner instanceof Dialog) {
            dialog = new JMTDialog((Dialog) owner, true);
        } else if (owner instanceof Frame) {
            dialog = new JMTDialog((Frame) owner, true);
        } else {
            dialog = new JMTDialog();
        }
        dialog.setTitle(panel.getDialogTitle());
        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().add(panel, BorderLayout.CENTER);

        // Adds exit button
        JButton exit = new JButton();
        exit.setText("Close");
        exit.addActionListener(new ActionListener() {

            /**
             * Invoked when an action occurs.
             */
            public void actionPerformed(ActionEvent e) {
                dialog.close();
            }
        });

        JPanel bottom = new JPanel();
        bottom.add(exit);
        dialog.getContentPane().add(bottom, BorderLayout.SOUTH);
        dialog.centerWindow(CommonConstants.MAX_GUI_WIDTH_ABOUT, CommonConstants.MAX_GUI_HEIGHT_ABOUT);

        // Handles autoclose
        if (autoclose) {
            ExecutorService es = Executors.newSingleThreadExecutor();
            es.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(AUTOCLOSE_TIMEOUT);
                        dialog.close();
                    } catch (InterruptedException ex) {
                        // Nothing to do
                    }
                }
            });
            es.shutdown();
        }

        return dialog;
    }

     /**
     * Shows JLQN about window
     * @param owner owner of this window (if null, window will not be modal)
     */
    public static void showJLQN(Window owner) {
        AboutDialogPanel panel = new AboutDialogPanel();
        panel.setTitles("About JLQN", "JLQN", IMG_JLQNICON);
        panel.setNames(JLQN);
        createDialog(owner, panel, false).setVisible(true);
    }

    /**
     * Defines the major contributors of a tool, grouped by company
     */
    public static class Contributors {

        private Company company;
        private List<String> names;
        private List<String> processedNames;

        /**
         * Builds a contributors list
         * @param company the company
         * @param names the name of the contributors for that company
         */
        private Contributors(Company company, String... names) {
            this(company, Arrays.asList(names));
        }

        /**
         * Builds a contributors list
         * @param company the company
         * @param names the name of the contributors for that company
         */
        private Contributors(Company company, List<String> names) {
            this.company = company;
            Collections.sort(names);
            // Process to show first name and surname.
            ArrayList<String> processedNames = new ArrayList<String>(names.size());
            for (String name : names) {
                while (name.startsWith("*")) {
                    name = name.substring(1);
                }
                int nameIdx = name.lastIndexOf(" ");
                if (nameIdx > 0) {
                    name = name.substring(nameIdx + 1) + " " + name.substring(0, nameIdx);
                }
                processedNames.add(name);
            }
            this.names = Collections.unmodifiableList(names);
            this.processedNames = Collections.unmodifiableList(processedNames);
        }

        /**
         * @return the company of the contributors
         */
        public Company getCompany() {
            return company;
        }

        /**
         * @return the contributor names
         */
        public List<String> getNames() {
            return names;
        }

        /**
         * @return the contributor names showing first name and surname
         */
        public List<String> getProcessedNames() {
            return processedNames;
        }

    }

}
