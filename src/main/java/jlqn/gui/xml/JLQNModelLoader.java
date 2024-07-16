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


import java.awt.Component;
import java.awt.HeadlessException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import jmt.framework.xml.XMLUtils;
import jmt.gui.common.Defaults;
import jmt.gui.common.xml.*;
import jlqn.model.JLQNModel;

import org.w3c.dom.Document;

public class JLQNModelLoader {

    /**
     * Filters for input files
     */
    public static final JmtFileFilter ALL = new JmtFileFilter(".jlqn; .lqnx; .xml", "All JLQN data files");
    public static final JmtFileFilter JLQN = new JmtFileFilter(".jlqn", "JLQN data file");
    public static final JmtFileFilter JLQX = new JmtFileFilter(".lqnx; .xml", "LQNX data file");

    public static final JmtFileFilter ALL_SAVE = new JmtFileFilter(".jlqn; .lqnx; .xml", "All JLQN data files");
    //public static final JmtFileFilter JLQN_SAVE = new JmtFileFilter(".jlqn", "JLQN data file");
    //public static final JmtFileFilter JLQX_SAVE = new JmtFileFilter(".lqnx; .xml", "JLQN data file");

    /**
     * Constants used for output
     */
    public static final int SUCCESS = 0;
    public static final int CANCELLED = 1;
    public static final int FAILURE = 2;
    public static final int WARNING = 3;

    /**
     * Constants to define xml types
     */
    protected static final int XML_SIM = 0;
    protected static final int XML_ARCHIVE = 1;
    protected static final int XML_MVA = 2;
    protected static final int XML_JABA = 3;
    protected static final int XML_RES_SIM = 4;
    protected static final int XML_RES_GUI = 5;
    protected static final int XML_PNML = 6;
    protected static final int XML_LQN = 7;
    protected static final int FILE_UNKNOWN = 255;

    /**
     * Failure motivations
     */
    protected static final String FAIL_UNKNOWN = "Unknown input file format";

    // Better to move this element elsewhere...
    protected static final String XML_MVA_ROOT = "model";
    protected static final String XML_PNML_ROOT = "pnml";

    protected static final String XML_LQN_ROOT = "jlqn";

    protected JmtFileChooser dialog;

    protected JmtFileFilter defaultFilter;
    protected JmtFileFilter defaultSaveFilter;

    // Motivation of last failure
    protected String failureMotivation;

    // Warnings found during last conversion
    protected List<String> warnings = new ArrayList<String>();

    protected String fileFormat;

    protected XMLUtils xmlutils = new XMLUtils();

    /**
     * Initializes a new model loader with specified default file filters
     * @param defaultFilter default file filter for current application
     * @param defaultSaveFilter default file save filter for current application
     */
    public JLQNModelLoader(JmtFileFilter defaultFilter, JmtFileFilter defaultSaveFilter) {
        this.defaultFilter = defaultFilter;
        this.defaultSaveFilter = defaultSaveFilter;
        // Initialize filechooser dialog
        dialog = new JmtFileChooser(defaultSaveFilter);
    }

    /**
     * Gets the motivation of last failure
     * @return the motivation of last failure
     */
    public String getFailureMotivation() {
        return failureMotivation;
    }

    /**
     * Gets a vector containing warnings of last performed operation
     * @return a Vector of String with every found warning
     */
    public List<String> getLastWarnings() {
        return warnings;
    }

    /**
     * @return the format of opened input file
     */
    public String getInputFileFormat() {
        return fileFormat;
    }

    // --- Methods used to load models ------------------------------------------------------------
    /**
     * Loads a model from a file into specified model data
     * @param modelData model data where information should be stored. Note that <b>its type
     * must be compatible with defaultFilter chosen in the constructor</b>, otherwise a
     * ClassCastException will be thrown
     * @param parent parent component of loading window
     * @param file model file where information should be loaded. If null, a Load window will
     * be shown for choosing the file
     * @return SUCCESS on success, CANCELLED if loading is cancelled,
     * FAILURE if an error occurs and WARNING is one or more warnings are generated due to
     * format conversion
     * @throws ClassCastException if modelData is not of instance of the correct class
     * @see #getFailureMotivation getFailureMotivation()
     */
    public int loadModel(Object modelData, Component parent, File file) {
        if (file == null) {
            int status = showOpenDialog(parent);
            if (status == JFileChooser.CANCEL_OPTION) {
                return CANCELLED;
            } else if (status == JFileChooser.ERROR_OPTION) {
                failureMotivation = "Error selecting input file";
                return FAILURE;
            }
            file = dialog.getSelectedFile();
        } else {
            dialog.setSelectedFile(file);
        }

        warnings.clear();
        try {
           if (defaultFilter == JLQN) {
                // Handles loading of JLQN models
                switch (getXmlFileType(file.getAbsolutePath())) {
                    case XML_LQN:
                        ((JLQNModel) modelData).loadDocument(xmlutils.loadXML(file));
                        fileFormat = "JLQN";
                        break;
                    default:
                        failureMotivation = FAIL_UNKNOWN;
                        return FAILURE;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            failureMotivation = e.getClass().getName() + ": " + e.getMessage();
            return FAILURE;
        }
        // If no warnings were found, report success
        if (warnings.size() > 0) {
            return WARNING;
        } else {
            return SUCCESS;
        }
    }

    /**
     * Returns name of selected file for i/o operations
     * @return name of selected file for open or save
     */
    public File getSelectedFile() {
        return dialog.getSelectedFile();
    }
    // --------------------------------------------------------------------------------------------

    // --- Methods used to save models ------------------------------------------------------------
    /**
     * Saves specified model into specified file or shows save as window if file is null
     * @param modelData data file where information should be stored. Note that <b>its type
     * must be compatible with defaultFilter chosen in the constructor</b>, otherwise a
     * ClassCastException will be thrown
     * @param parent parent window that will own the save as dialog
     * @param file location where pecified model must be saved or null if save as must be shown
     * @return SUCCESS on success, CANCELLED if loading is cancelled,
     * FAILURE if an error occurs
     * @throws ClassCastException if modelData is not of instance of the correct class
     * @see #getFailureMotivation getFailureMotivation()
     */
    public int saveModel(Object modelData, Component parent, File file) {
        if (file == null) {
            // Shows save as window
            int status;
            status = this.showSaveDialog(parent);
            if (status == JFileChooser.CANCEL_OPTION) {
                return CANCELLED;
            } else if (status == JFileChooser.ERROR_OPTION) {
                failureMotivation = "Error selecting output file";
                return FAILURE;
            }
            file = dialog.getSelectedFile();
        } else {
            // Check extension to avoid saving over a converted file
            boolean hasValidExtension = false;
            String[] extensions = defaultSaveFilter.getExtensions();
            for (String e : extensions) {
                if (file.getName().toLowerCase().endsWith(e)) {
                    hasValidExtension = true;
                    break;
                }
            }
            if (!hasValidExtension) {
                int resultValue = JOptionPane.showConfirmDialog(parent, "<html>File <font color=#0000ff>" + file.getName()
                                + "</font> does not have valid extension.<br>Do you want to replace it anyway?</html>", "JMT - Warning",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                if (resultValue != JOptionPane.OK_OPTION) {
                    return CANCELLED;
                }
            }
        }

        // Now checks to save correct type of model
        try {
            xmlutils.saveXML(((JLQNModel) modelData).createDocument(), file);
        } catch (Exception e) {
            e.printStackTrace();
            failureMotivation = e.getClass().getName() + ": " + e.getMessage();
            return FAILURE;
        }
        return SUCCESS;
    }
    // --------------------------------------------------------------------------------------------

    // --- Methods to open and parse files --------------------------------------------------------
    protected int getXmlFileType(String fileName) {
        // Opens without validating (as we do not know document type)
        try {
            Document doc = XMLReader.loadXML(fileName);
            String root = doc.getDocumentElement().getNodeName();
            // Uses root name to determine document type
            if (root.equals(JLQNModelLoader.XML_LQN_ROOT)) {
                return XML_LQN;
            } else {
                return FILE_UNKNOWN;
            }
        } catch (Exception e) {
            // If an exception is thrown, reports that format is unknown
            return FILE_UNKNOWN;
        }
    }
    // --------------------------------------------------------------------------------------------

    // --- Methods to show dialogs ----------------------------------------------------------------
    /**
     * Adds only compatible filters to current dialog
     */
    protected void addCompatibleFilters() {
        dialog.addChoosableFileFilter(ALL);
        if (defaultFilter == JLQN) {
            dialog.addChoosableFileFilter(JLQX);
            dialog.addChoosableFileFilter(JLQN);
        } else {
            dialog.addChoosableFileFilter(JLQX);
            dialog.addChoosableFileFilter(JLQN);
        }
        dialog.setFileFilter(ALL);
    }

    /**
     * Shows open file dialog
     * @param parent parent component for this dialog
     * @return   the return state of the file chooser on popdown:
     * <ul>
     * <li>JFileChooser.CANCEL_OPTION
     * <li>JFileChooser.APPROVE_OPTION
     * <li>JFileCHooser.ERROR_OPTION if an error occurs or the
     *			dialog is dismissed
     * </ul>
     * @exception HeadlessException if GraphicsEnvironment.isHeadless()
     * returns true.
     */
    protected int showOpenDialog(Component parent) {
        dialog.resetChoosableFileFilters();
        addCompatibleFilters();
        return dialog.showOpenDialog(parent);
    }

    /**
     * Shows save file dialog
     * @param    parent  the parent component of the dialog,
     *			can be <code>null</code>;
     *                  see <code>showDialog</code> for details
     * @return   the return state of the file chooser on popdown:
     * <ul>
     * <li>JFileChooser.CANCEL_OPTION
     * <li>JFileChooser.APPROVE_OPTION
     * <li>JFileCHooser.ERROR_OPTION if an error occurs or the
     *			dialog is dismissed
     * </ul>
     * @exception HeadlessException if GraphicsEnvironment.isHeadless()
     * returns true.
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    protected int showSaveDialog(Component parent) {
        dialog.resetChoosableFileFilters();
        dialog.addChoosableFileFilter(defaultSaveFilter);
        dialog.setFileFilter(defaultSaveFilter);
        return dialog.showSaveDialog(parent);
    }
    // --------------------------------------------------------------------------------------------

    // --- Inner classes --------------------------------------------------------------------------
    /**
     * Custom file chooser class
     */
    protected static class JmtFileChooser extends JFileChooser {

        private static final long serialVersionUID = 1L;

        protected JmtFileFilter defaultSaveFilter;

        /**
         * Creates a File chooser in the appropriate directory user default
         * @param defaultSaveFilter default file save filter
         */
        public JmtFileChooser(JmtFileFilter defaultSaveFilter) {
            super(Defaults.getWorkingPath());
            this.defaultSaveFilter = defaultSaveFilter;
        }

        /**
         * Overrides default method to provide a warning if saving over an existing file
         */
        @Override
        public void approveSelection() {
            // Gets the chosen file name
            String name = getSelectedFile().getName();
            String parent = getSelectedFile().getParent();
            if (getDialogType() == OPEN_DIALOG) {
                super.approveSelection();
            }
            if (getDialogType() == SAVE_DIALOG) {
                boolean hasValidExtension = false;
                String[] extensions = defaultSaveFilter.getExtensions();
                for (String e : extensions) {
                    if (name.toLowerCase().endsWith(e)) {
                        hasValidExtension = true;
                        break;
                    }
                }
                if (!hasValidExtension) {
                    name += extensions[0];
                    setSelectedFile(new File(parent, name));
                }
                if (getSelectedFile().exists()) {
                    int resultValue = JOptionPane.showConfirmDialog(this, "<html>File <font color=#0000ff>" + name
                                    + "</font> already exists in this folder.<br>Do you want to replace it?</html>", "JMT - Warning",
                            JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (resultValue == JOptionPane.OK_OPTION) {
                        getSelectedFile().delete();
                        super.approveSelection();
                    }
                } else {
                    super.approveSelection();
                }
            }
        }

    }

    /**
     * Inner class used to create simple file filters with only extension check
     */
    public static class JmtFileFilter extends FileFilter {

        public static final String SEP = ";";

        private String[] extensions;
        private String description;

        /**
         * Creates a new file filter with specified semicolon separated list of
         * extensions and specified description
         * @param extensionList semicolon separated list of extensions of this
         * filter (for example ".jsimg;.jmodel")
         * @param text text description of this filter
         */
        public JmtFileFilter(String extensionList, String text) {
            this.extensions = extensionList.split(SEP);
            this.description = createDescription(text);
        }

        /**
         * Whether the given file is accepted by this filter.
         */
        @Override
        public boolean accept(File f) {
            String name = f.getName().toLowerCase();
            if (f.isDirectory()) {
                return true;
            }
            for (String e : extensions) {
                if (name.endsWith(e)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Creates the description for this filter
         * @param text text description of the filter
         * @return created description
         */
        private String createDescription(String text) {
            StringBuffer sb = new StringBuffer();
            sb.append(text);
            sb.append(" (*");
            for (int i = 0; i < extensions.length - 1; i++) {
                sb.append(extensions[i]);
                sb.append("; *");
            }
            sb.append(extensions[extensions.length - 1]);
            sb.append(")");
            return sb.toString();
        }

        /**
         * Gets the extensions of this filter
         * @return extensions of this filter
         */
        public String[] getExtensions() {
            return extensions;
        }

        /**
         * The description of this filter.
         */
        @Override
        public String getDescription() {
            return description;
        }

    }
    // --------------------------------------------------------------------------------------------

}
