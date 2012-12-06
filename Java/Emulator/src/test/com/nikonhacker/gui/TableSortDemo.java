package com.nikonhacker.gui;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

public class TableSortDemo extends JFrame {
    protected JTable table = new JTable();

    protected MyTableModel tableModel;

    protected JLabel titleLabel = new JLabel("Click table header to sort the column.");

    public TableSortDemo() {
        super();
        setSize(600, 300);

        tableModel = new MyTableModel();

        getContentPane().add(titleLabel, BorderLayout.NORTH);
        table.setModel(tableModel);

        JTableHeader header = table.getTableHeader();
        header.setUpdateTableInRealTime(true);
        header.addMouseListener(tableModel.new ColumnListener(table));
        header.setReorderingAllowed(true);

        JScrollPane ps = new JScrollPane();
        ps.getViewport().add(table);
        getContentPane().add(ps, BorderLayout.CENTER);

        WindowListener wndCloser = new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        };
        addWindowListener(wndCloser);
        setVisible(true);
    }

    public static void main(String argv[]) {
        new TableSortDemo();
    }
}

class MyTableModel extends AbstractTableModel {
    protected int sortCol = 0;

    protected boolean isSortAsc = true;

    protected int m_result = 0;
    protected int columnsCount = 1;
    Vector vector = new Vector();

    public MyTableModel() {
        vector.removeAllElements();
        vector.addElement(new Integer(24976600));
        vector.addElement(new Integer(24));
        vector.addElement(new Integer(2497));
        vector.addElement(new Integer(249766));
        vector.addElement(new Integer(2497660));
        vector.addElement(new Integer(6600));
        vector.addElement(new Integer(76600));
        vector.addElement(new Integer(976600));
        vector.addElement(new Integer(4976600));
    }

    public int getRowCount() {
        return vector == null ? 0 : vector.size();
    }

    public int getColumnCount() {
        return columnsCount;
    }

    public String getColumnName(int column) {
        String str = "data";
        if (column == sortCol)
            str += isSortAsc ? " >>" : " <<";
        return str;
    }

    public boolean isCellEditable(int nRow, int nCol) {
        return false;
    }

    public Object getValueAt(int nRow, int nCol) {
        if (nRow < 0 || nRow >= getRowCount())
            return "";
        if (nCol > 1) {
            return "";
        }
        return vector.elementAt(nRow);
    }

    public String getTitle() {
        return "data ";
    }

    class ColumnListener extends MouseAdapter {
        protected JTable table;

        public ColumnListener(JTable t) {
            table = t;
        }

        public void mouseClicked(MouseEvent e) {
            TableColumnModel colModel = table.getColumnModel();
            int columnModelIndex = colModel.getColumnIndexAtX(e.getX());
            int modelIndex = colModel.getColumn(columnModelIndex).getModelIndex();

            if (modelIndex < 0)
                return;
            if (sortCol == modelIndex)
                isSortAsc = !isSortAsc;
            else
                sortCol = modelIndex;

            for (int i = 0; i < columnsCount; i++) {
                TableColumn column = colModel.getColumn(i);
                column.setHeaderValue(getColumnName(column.getModelIndex()));
            }
            table.getTableHeader().repaint();

            Collections.sort(vector, new MyComparator(isSortAsc));
            table.tableChanged(new TableModelEvent(MyTableModel.this));
            table.repaint();
        }
    }
}

class MyComparator implements Comparator {
    protected boolean isSortAsc;

    public MyComparator(boolean sortAsc) {
        isSortAsc = sortAsc;
    }

    public int compare(Object o1, Object o2) {
        if (!(o1 instanceof Integer) || !(o2 instanceof Integer))
            return 0;
        Integer s1 = (Integer) o1;
        Integer s2 = (Integer) o2;
        int result = 0;
        result = s1.compareTo(s2);
        if (!isSortAsc)
            result = -result;
        return result;
    }

    public boolean equals(Object obj) {
        if (obj instanceof MyComparator) {
            MyComparator compObj = (MyComparator) obj;
            return compObj.isSortAsc == isSortAsc;
        }
        return false;
    }
}

