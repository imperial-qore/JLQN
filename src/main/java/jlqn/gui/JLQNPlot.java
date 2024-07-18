package jlqn.gui;

import jline.lang.layered.LayeredNetwork;
import jline.lang.layered.LayeredNetworkStruct;
import org.graphper.api.*;
import org.graphper.api.attributes.*;
import org.graphper.api.attributes.Color;
import org.graphper.draw.ExecuteException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.IOException;

public class JLQNPlot {

    // TODO
    static public void plotLQN(LayeredNetwork lqn) throws ExecuteException, IOException {
        LayeredNetworkStruct sn = lqn.getStruct();

        Node[] hosts = new Node[sn.nhosts];
        for (int h = 0; h < sn.nhosts; h++) {
            hosts[h] = Node.builder().label(sn.names.get(1 + h)).build();
        }

        Node[] tasks = new Node[sn.ntasks];
        for (int t = 0; t < sn.ntasks; t++) {
            tasks[t] = Node.builder().shape(NodeShapeEnum.RECT).color(Color.BLACK).label(sn.names.get(1 + sn.nhosts + t)).build();
        }

        Node[] entries = new Node[sn.nentries];
        for (int e = 0; e < sn.nentries; e++) {
            entries[e] = Node.builder().shape(NodeShapeEnum.PARALLELOGRAM).color(Color.ORANGE).label(sn.names.get(1 + sn.nhosts + sn.ntasks + e)).build();
        }

        Node[] acts = new Node[sn.nacts];
        for (int a = 0; a < sn.nacts; a++) {
            acts[a] = Node.builder().shape(NodeShapeEnum.PLAIN).color(Color.GREY).label(sn.names.get(1 + sn.nhosts + sn.ntasks + sn.nentries + a)).build();
        }

        Graphviz.GraphvizBuilder builder = Graphviz.digraph();
        for (int t = 0; t < sn.ntasks; t++) {
            builder = builder.addLine(tasks[t], hosts[(int) sn.parent.get(1 + sn.nhosts + t)-1]);
        }
        for (int e = 0; e < sn.nentries; e++) {
            builder = builder.addLine(tasks[(int) sn.parent.get(1 + sn.nhosts + sn.ntasks + e)-sn.ntasks-1],entries[e]);
        }
        for (int a = 0; a < sn.nacts; a++) {
            builder = builder.addLine(entries[(int) sn.parent.get(1 + sn.nhosts + sn.ntasks + sn.nentries + a)-sn.ntasks-1], acts[a]);
        }
        for (int c = 0; c < sn.ncalls; c++) {
            Line l = Line.builder(acts[(int) sn.callpair.get(c+1,1)-sn.nhosts -sn.ntasks-sn.nentries-1], entries[(int) sn.callpair.get(c+1,2)-sn.nhosts -sn.ntasks-1]).style(LineStyle.DASHED).arrowHead(ArrowShape.VEE).build();
            builder = builder.addLine(l);
        }
        Graphviz graphviz = builder.build();

        GraphResource img = graphviz.toFile(FileType.GIF);
        ImageIcon imageIcon = new ImageIcon(img.bytes(), "graphviz");
        img.close();

        showInFrame(imageIcon);
    }

    private static void showInFrame(ImageIcon imageIcon) {
        int imageWidth = imageIcon.getIconWidth();
        int windowWidth = (int) Math.min((int) imageWidth*1.1, 480);
        int imageHeight = imageIcon.getIconHeight();
        int windowHeight = (int) Math.min((int) imageHeight*1.1, 640);
        JFrame mainframe = new JFrame("JLQN Plot");
        JPanel cp = (JPanel) mainframe.getContentPane();
        cp.setLayout(new BorderLayout());

        JLabel label = new JLabel(imageIcon);
        JScrollPane scrollPane = new JScrollPane(label);
        cp.add(scrollPane, BorderLayout.CENTER);

        // Add mouse listeners for dragging
        final Point[] startPoint = {new Point()};
        scrollPane.getViewport().addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    startPoint[0] = SwingUtilities.convertPoint(scrollPane.getViewport(), e.getPoint(), label);
                }
            }
        });

        scrollPane.getViewport().addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    Point dragPoint = SwingUtilities.convertPoint(scrollPane.getViewport(), e.getPoint(), label);
                    Point viewPosition = scrollPane.getViewport().getViewPosition();
                    int newX = viewPosition.x - (dragPoint.x - startPoint[0].x);
                    int newY = viewPosition.y - (dragPoint.y - startPoint[0].y);

                    scrollPane.getViewport().setViewPosition(new Point(newX, newY));
                }
            }
        });

        mainframe.pack();
        mainframe.setVisible(true);
        mainframe.setSize(windowWidth, windowHeight);
    }
}

