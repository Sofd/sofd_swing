package de.sofd.swing.test;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.sofd.swing.DefaultGridListComponentFactory;
import de.sofd.swing.JGridList;


public class GridListTestApp {

    private JGridList gridList;
    private DefaultListModel listModel;
    private JToolBar toolbar;
    
    public GridListTestApp() {
        JFrame f = new JFrame(""+this.getClass().getName());
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(700,700);

        toolbar = new JToolBar("toolbar");
        toolbar.setFloatable(false);
        f.getContentPane().add(toolbar, BorderLayout.PAGE_START);
        
        listModel = new DefaultListModel();
        for (int i=0; i<50; ++i) {
            listModel.addElement("element "+i);
        }
        gridList = new JGridList(listModel, new DefaultGridListComponentFactory());
        f.getContentPane().add(gridList, BorderLayout.CENTER);
        // TODO: DefaultGridListComponentFactory selection visualization doesn't work
        //   (background colors are always reset??)
        /*
        gridList.setComponentFactory(new AbstractFramedSelectionGridListComponentFactory() {
            @Override
            public JComponent createComponent(JGridList source, JPanel parent, Object modelItem) {
                JLabel l = new JLabel(""+modelItem);
                parent.add(l);
                return l;
            }
        });
        */
        gridList.setFirstDisplayedIdx(0);
        gridList.setGridSizes(4, 4);
        gridList.setVisible(true);
        
        gridList.getSelectionModel().setSelectionInterval(7, 7);
        gridList.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        gridList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        initToolBar();
        
        f.setVisible(true);
    }
    
    private void initToolBar() {
        final JTextField dispIdxDisplay = new JTextField() {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(50, super.getMaximumSize().height);
            }
        };
        toolbar.add(new AbstractAction("<") {
            @Override
            public void actionPerformed(ActionEvent e) {
                gridList.setFirstDisplayedIdx(gridList.getFirstDisplayedIdx()-1);
                dispIdxDisplay.setText(""+gridList.getFirstDisplayedIdx());
            }
        });
        toolbar.add(new AbstractAction(">") {
            @Override
            public void actionPerformed(ActionEvent e) {
                gridList.setFirstDisplayedIdx(gridList.getFirstDisplayedIdx()+1);
                dispIdxDisplay.setText(""+gridList.getFirstDisplayedIdx());
            }
        });
        toolbar.add(new JLabel("startIdx:"));
        toolbar.add(dispIdxDisplay);
        toolbar.add(new AbstractAction("set") {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    gridList.setFirstDisplayedIdx(Integer.parseInt(dispIdxDisplay.getText()));
                } catch (NumberFormatException ex) {
                    //ignore
                }
            }
        });
        toolbar.add(new JLabel("gridSize:"));
        final JSpinner gridSizeSpinner = new JSpinner(new SpinnerNumberModel(4,1,9,1)) {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(50, super.getMaximumSize().height);
            }
        };
        gridSizeSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int value = (Integer)gridSizeSpinner.getValue();
                gridList.setGridSizes(value, value);
            }
        });
        toolbar.add(gridSizeSpinner);
        
        final JTextField intStartEntry = new JTextField("10") {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(50, super.getMaximumSize().height);
            }
        };
        final JTextField intLengthEntry = new JTextField("5") {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(50, super.getMaximumSize().height);
            }
        };
        toolbar.add(new JLabel("intStart:"));
        toolbar.add(intStartEntry);
        toolbar.add(new JLabel("intLength:"));
        toolbar.add(intLengthEntry);
        toolbar.add(new AbstractAction("add") {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int intStart = Integer.parseInt(intStartEntry.getText());
                    int intLength = Integer.parseInt(intLengthEntry.getText());
                    for (int i=0; i<intLength; i++) {
                        listModel.add(intStart, "Add"+i);
                    }
                } catch (NumberFormatException ex) {
                    //ignore
                }
            }
        });
        toolbar.add(new AbstractAction("rm") {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int intStart = Integer.parseInt(intStartEntry.getText());
                    int intLength = Integer.parseInt(intLengthEntry.getText());
                    for (int i=0; i<intLength; i++) {
                        listModel.remove(intStart);
                    }
                } catch (NumberFormatException ex) {
                    //ignore
                }
            }
        });
        
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        new GridListTestApp();
    }

}
