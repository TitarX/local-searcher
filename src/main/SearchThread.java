/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

/**
 *
 * @author TitarX
 */
public class SearchThread extends Thread
{

    private MainForm mainForm = null;
    private Params params = null;
    private Vector results = null;
    private boolean suspended = false;

    public SearchThread(MainForm mainForm, Params params)
    {
        this.mainForm = mainForm;
        this.params = params;
        results = new Vector();
    }

    public synchronized void setSuspended(boolean suspended)
    {
        this.suspended = suspended;
        if (!suspended)
        {
            notifyAll();
        }
    }

    private void process(File object)
    {
        Pattern pattern = Pattern.compile(params.getFindString(), params.getRegexParam());
        int searchType = params.getSearchType();
        if (object.exists() && object.isDirectory())
        {
            File[] childrensList = object.listFiles();
            for (File children : childrensList)
            {
                synchronized (this)
                {
                    while (suspended)
                    {
                        try
                        {
                            wait();
                        }
                        catch (InterruptedException ex)
                        {
                            Logger.getLogger(SearchThread.class.getName()).log(Level.SEVERE, null, ex);
                            JOptionPane.showMessageDialog(mainForm, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }

                switch (searchType)
                {
                    case 0:
                        if (children.isDirectory())
                        {
                            if (pattern.matcher(children.getName()).matches())
                            {
                                saveObjectInfo(children);
                            }
                        }
                        break;
                    case 1:
                        if (children.isFile())
                        {
                            if (pattern.matcher(children.getName()).matches())
                            {
                                saveObjectInfo(children);
                            }
                        }
                        break;
                    case 2:
                        if (pattern.matcher(children.getName()).matches())
                        {
                            saveObjectInfo(children);
                        }
                        break;
                }
                if (children.isDirectory())
                {
                    process(children);
                }
            }

        }
    }

    private void saveObjectInfo(final File object)
    {
        Vector result = new Vector();
        if (object.isDirectory())
        {
            result.add("Folder");
        }
        else if (object.isFile())
        {
            result.add("File");
        }
        result.add(object.getName());
        result.add(object.getAbsolutePath());

        if (object.isDirectory())
        {
            result.add("");
            result.add("");
        }
        else if (object.isFile())
        {
            Date date = new Date(object.lastModified());
            SimpleDateFormat simplDateFormat = new SimpleDateFormat("yyyy.MM.dd kk:mm:ss");
            result.add(simplDateFormat.format(date));

            result.add(String.format("%d %s", (object.length() + 1023) / 1024, "Kb"));
        }
        results.add(result);

        MyDefaultTableModel myDefaultTableModel = new MyDefaultTableModel();
        Iterator resultsIterator = results.iterator();
        while (resultsIterator.hasNext())
        {
            myDefaultTableModel.getDataVector().add((Vector) resultsIterator.next());
        }

        myDefaultTableModel.addTableModelListener(new TableModelListener()
        {

            @Override
            public void tableChanged(TableModelEvent e)
            {
//                if (isAlive())
//                {
//                    //
//                }
            }
        });

        showObjectInfo(myDefaultTableModel);
    }

    private void showObjectInfo(final MyDefaultTableModel myDefaultTableModel)
    {
        SwingUtilities.invokeLater(new Runnable()
        {

            @Override
            public void run()
            {
                mainForm.setTableModel(myDefaultTableModel);
            }
        });
    }

    @Override
    public void run()
    {
        process(new File(params.getLocationString()));
    }
}
