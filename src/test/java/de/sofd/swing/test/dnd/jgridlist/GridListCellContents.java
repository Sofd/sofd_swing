package de.sofd.swing.test.dnd.jgridlist;

import java.io.Serializable;

public class GridListCellContents implements Serializable {
    
    private static final long serialVersionUID = 4463854966626444221L;

    private String[] strings;
    
    public GridListCellContents(String[] strings) {
        this.strings = strings;
    }
    
    public GridListCellContents(Object[] objs) {
        this.strings = new String[objs.length];
        for (int i = 0; i < objs.length; i++) {
            strings[i] = objs[i].toString();
        }
    }

    public String[] getStrings() {
        return strings;
    }

}
