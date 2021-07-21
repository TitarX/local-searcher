/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import javax.swing.table.DefaultTableModel;

/**
 *
 * @author TitarX
 */
public class MyDefaultTableModel extends DefaultTableModel
{

    public MyDefaultTableModel()
    {
        super();

        this.addColumn("Type");
        this.addColumn("Name");
        this.addColumn("Absolute path");
        this.addColumn("Last modified");
        this.addColumn("Size");
    }

    @Override
    public boolean isCellEditable(int row, int column)
    {
        return false;
    }
}
