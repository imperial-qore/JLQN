package jlqn.gui.exact.utilities;

import jmt.framework.xml.XMLUtils;

import java.awt.*;

public class SolverClient {
    private XMLUtils xmlUtils;
    private Frame owner;
    public SolverClient(Frame owner) {
        xmlUtils = new XMLUtils();
        this.owner = owner;
    }
}
