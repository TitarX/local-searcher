/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

/**
 *
 * @author TitarX
 */
public class WaitingEndingSearchThread extends Thread
{

    private MainForm mainForm = null;
    private SearchThread searchThread = null;

    public WaitingEndingSearchThread(MainForm mainForm, SearchThread searchThread)
    {
        this.mainForm = mainForm;
        this.searchThread = searchThread;
    }

    @Override
    public void run()
    {
        try
        {
            searchThread.join();
        }
        catch (InterruptedException ex)
        {
            Logger.getLogger(WaitingEndingSearchThread.class.getName()).log(Level.SEVERE, null, ex);
        }

        SwingUtilities.invokeLater(new Runnable()
        {

            @Override
            public void run()
            {
                mainForm.changeStateElements(false, true);
                mainForm.changeStatusProgressBar("Search ended", false, 0);
            }
        });
    }
}
