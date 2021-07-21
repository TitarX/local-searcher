/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.*;
import java.nio.channels.FileChannel;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import jframeformlibrary.*;

/**
 *
 * @author TitarX
 */
public class MainForm extends JFrame
{

    private FormPosition formPosition = null;
    private SearchThread searchThread = null;
    private WaitingEndingSearchThread waitingEndingSearchThread = null;
    private JPopupMenu resultPopupMenu = null;
    private MainForm mainForm = null;
    private MyDefaultTableModel currentTableModel = null;

    /**
     * Creates new form MainForm
     */
    public MainForm()
    {
        initComponents();
        myInitComponent();
        mainForm = this;
    }

    private void myInitComponent()
    {
        formPosition = new FormPosition(this, "configs/formPosition.xml");
        try
        {
            formPosition.restore();
        }
        catch (Exception ex)
        {
            Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, ex);

            formPosition.resetToDefault();
            formPosition.showErrorMessageDialog(ex);
        }

        findTextField.setToolTipText("Regex Java");

        statusProgressBar.setStringPainted(true);
        statusProgressBar.setString("");

        resultTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setTableModel(new MyDefaultTableModel());

        foldersRadioButton.setActionCommand("0");
        filesRadioButton.setActionCommand("1");
        allRadioButton.setActionCommand("2");
        buttonGroup.add(foldersRadioButton);
        buttonGroup.add(filesRadioButton);
        buttonGroup.add(allRadioButton);

        findTextField.getDocument().addDocumentListener(new DocumentListener()
        {

            @Override
            public void insertUpdate(DocumentEvent e)
            {
                changeStateStartButton();
            }

            @Override
            public void removeUpdate(DocumentEvent e)
            {
                changeStateStartButton();
            }

            @Override
            public void changedUpdate(DocumentEvent e)
            {
                changeStateStartButton();
            }
        });

        locationTextField.getDocument().addDocumentListener(new DocumentListener()
        {

            @Override
            public void insertUpdate(DocumentEvent e)
            {
                changeStateStartButton();
            }

            @Override
            public void removeUpdate(DocumentEvent e)
            {
                changeStateStartButton();
            }

            @Override
            public void changedUpdate(DocumentEvent e)
            {
                changeStateStartButton();
            }
        });

        initResultPopupMenu();
    }

    private void initResultPopupMenu()
    {
        resultPopupMenu = new JPopupMenu();

        JMenuItem openObjectMenuItem = new JMenuItem("Open object");
        openObjectMenuItem.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (Desktop.isDesktopSupported())
                {
                    try
                    {
                        String objectPath = resultTable.getValueAt(resultTable.getSelectedRow(), 2).toString();
                        Desktop.getDesktop().open(new File(objectPath));
                    }
                    catch (Exception ex)
                    {
                        Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, ex);
                        JOptionPane.showMessageDialog(mainForm, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        JMenuItem openContainerMenuItem = new JMenuItem("Open container");
        openContainerMenuItem.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (Desktop.isDesktopSupported())
                {
                    try
                    {
                        String containerPath = resultTable.getValueAt(resultTable.getSelectedRow(), 2).toString().replaceAll(resultTable.getValueAt(resultTable.getSelectedRow(), 1).toString(), "");
                        Desktop.getDesktop().open(new File(containerPath));
                    }
                    catch (Exception ex)
                    {
                        Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, ex);
                        JOptionPane.showMessageDialog(mainForm, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        JMenuItem copyPathMenuItem = new JMenuItem("Copy path to clipboard");
        copyPathMenuItem.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                String objectPath = resultTable.getValueAt(resultTable.getSelectedRow(), 2).toString();
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(objectPath), null);
            }
        });

        JMenuItem copyObjectMenuItem = new JMenuItem("Copy object to");
        copyObjectMenuItem.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                String objectPath = resultTable.getValueAt(resultTable.getSelectedRow(), 2).toString();
                JFileChooser fileChooser = new JFileChooser();
                UIManager.put("FileChooser.openDialogTitleText", "Copy object to");
                UIManager.put("FileChooser.openButtonText", "Copy");
                fileChooser.updateUI();
                fileChooser.setAcceptAllFileFilterUsed(false);
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if (fileChooser.showOpenDialog(mainForm) == JFileChooser.APPROVE_OPTION)
                {
                    String copyToPath = String.format("%s%s%s", fileChooser.getSelectedFile().getPath(),
                            File.separator,
                            resultTable.getValueAt(resultTable.getSelectedRow(), 1).toString());
                    File copyToFile = new File(copyToPath);
                    int toCopy = 0;
                    if (copyToFile.exists())
                    {
                        toCopy = JOptionPane.showConfirmDialog(mainForm, "Object with same name already exist. Continue?", "Warning",
                                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    }
                    if (toCopy == 0)
                    {
                        try
                        {
                            copyObject(new File(objectPath), copyToFile, false);
                            JOptionPane.showMessageDialog(mainForm, "Object copied successfully", "Info", JOptionPane.INFORMATION_MESSAGE);
                        }
                        catch (Exception ex)
                        {
                            Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, ex);
                            JOptionPane.showMessageDialog(mainForm, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });

        JMenuItem moveObjectMenuItem = new JMenuItem("Move object to");
        moveObjectMenuItem.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                int selectedRow = resultTable.getSelectedRow();
                String objectPath = resultTable.getValueAt(selectedRow, 2).toString();
                JFileChooser fileChooser = new JFileChooser();
                UIManager.put("FileChooser.openDialogTitleText", "Move object to");
                UIManager.put("FileChooser.openButtonText", "Move");
                fileChooser.updateUI();
                fileChooser.setAcceptAllFileFilterUsed(false);
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if (fileChooser.showOpenDialog(mainForm) == JFileChooser.APPROVE_OPTION)
                {
                    String copyToPath = String.format("%s%s%s", fileChooser.getSelectedFile().getPath(),
                            File.separator,
                            resultTable.getValueAt(resultTable.getSelectedRow(), 1).toString());
                    File copyToFile = new File(copyToPath);
                    int toCopy = 0;
                    if (copyToFile.exists())
                    {
                        toCopy = JOptionPane.showConfirmDialog(mainForm, "Object with same name already exist. Continue?", "Warning",
                                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    }
                    if (toCopy == 0)
                    {
                        try
                        {
                            int errorDelete = copyObject(new File(objectPath), copyToFile, true);
                            if (errorDelete == 0)
                            {
                                currentTableModel.removeRow(selectedRow);
                                setTableModel(currentTableModel);
                                int toNewSearch = 0;
                                toNewSearch = JOptionPane.showConfirmDialog(mainForm, "Object moved successfully. Repeat search?",
                                        "Info", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                                if (toNewSearch == 0)
                                {
                                    startButtonProcess();
                                }
                            }
                            else
                            {

                                JOptionPane.showMessageDialog(mainForm, "Source object delete failed", "Warning", JOptionPane.WARNING_MESSAGE);
                            }
                        }
                        catch (Exception ex)
                        {
                            Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, ex);
                            JOptionPane.showMessageDialog(mainForm, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });

        JMenuItem deleteObjectMenuItem = new JMenuItem("Delete object");
        deleteObjectMenuItem.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                int selectedRow = resultTable.getSelectedRow();
                String objectPath = resultTable.getValueAt(selectedRow, 2).toString();
                File object = new File(objectPath);
                int errorDelete = deleteObject(object);
                if (errorDelete == 0)
                {
                    currentTableModel.removeRow(selectedRow);
                    setTableModel(currentTableModel);
                    int toNewSearch = 0;
                    toNewSearch = JOptionPane.showConfirmDialog(mainForm, "Object deleted successfully. Repeat search?",
                            "Info", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                    if (toNewSearch == 0)
                    {
                        startButtonProcess();
                    }
                }
                else
                {
                    JOptionPane.showMessageDialog(mainForm, "Object delete failed", "Warning", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        resultPopupMenu.add(openObjectMenuItem);
        resultPopupMenu.add(openContainerMenuItem);
        resultPopupMenu.add(copyPathMenuItem);
        resultPopupMenu.add(copyObjectMenuItem);
        resultPopupMenu.add(moveObjectMenuItem);
        resultPopupMenu.add(deleteObjectMenuItem);

//        resultTable.setComponentPopupMenu(resultPopupMenu);
    }

    private int copyObject(File from, File to, boolean toDeleted) throws FileNotFoundException, IOException
    {
        int errorDelete = 0;

        if (from.isFile())
        {
            FileChannel objectChanel = new FileInputStream(from).getChannel();
            FileChannel copyToChanel = new FileOutputStream(to).getChannel();
            copyToChanel.transferFrom(objectChanel, 0, objectChanel.size());
            objectChanel.close();
            copyToChanel.close();
        }
        else if (from.isDirectory())
        {
            to.mkdir();
            for (File file : from.listFiles())
            {
                copyObject(file, new File(String.format("%s%s%s", to, File.separator, file.getName())), toDeleted);
            }
        }

        if (toDeleted)
        {
            if (!from.delete())
            {
                errorDelete = 1;
            }
        }

        return errorDelete;
    }

    private int deleteObject(File object)
    {
        int errorDelete = 0;

        if (object.isDirectory())
        {
            for (File file : object.listFiles())
            {
                deleteObject(file);
            }
        }

        if (!object.delete())
        {
            errorDelete = 1;
        }

        return errorDelete;
    }

    protected void setTableModel(MyDefaultTableModel myDefaultTableModel)
    {
        currentTableModel = myDefaultTableModel;
        resultTable.setModel(myDefaultTableModel);
        myDefaultTableModel.newRowsAdded(new TableModelEvent(myDefaultTableModel));
    }

    private void changeStateStartButton()
    {
        if (findTextField.getText().equals("") || locationTextField.getText().equals(""))
        {
            startButton.setEnabled(false);
        }
        else
        {
            startButton.setEnabled(true);
        }
    }

    protected void changeStateElements(boolean isStarting, boolean isStopping)
    {
        findTextField.setEditable(isStopping);
        ignoreCaseCheckBox.setEnabled(isStopping);
        plainTextCheckBox.setEnabled(isStopping);
        foldersRadioButton.setEnabled(isStopping);
        filesRadioButton.setEnabled(isStopping);
        allRadioButton.setEnabled(isStopping);
        startButton.setEnabled(isStopping);
        openButton.setEnabled(isStopping);

        pauseButton.setEnabled(isStarting);
        stopButton.setEnabled(isStarting);

        pauseButton.setText("Pause");
    }

    private void startButtonProcess()
    {
        setTableModel(new MyDefaultTableModel());
        changeStateElements(true, false);

        Params params = new Params();
        params.setFindString(findTextField.getText());
        params.setLocationString(locationTextField.getText());
        int regexParam = 0;
        if (ignoreCaseCheckBox.isSelected())
        {
            regexParam |= Pattern.CASE_INSENSITIVE;
            regexParam |= Pattern.UNICODE_CASE;
        }
        if (plainTextCheckBox.isSelected())
        {
            regexParam |= Pattern.LITERAL;
        }
        params.setRegexParam(regexParam);
        params.setSearchType(Integer.parseInt(buttonGroup.getSelection().getActionCommand()));
        searchThread = new SearchThread(this, params);
        waitingEndingSearchThread = new WaitingEndingSearchThread(this, searchThread);
        searchThread.start();
        changeStatusProgressBar("Search started", true, 0);
        waitingEndingSearchThread.start();
    }

    private void pauseButtonProcess()
    {
        if (pauseButton.getActionCommand().equalsIgnoreCase("Pause"))
        {
            pauseButton.setText("Continue");

            changeStatusProgressBar("Search paused", false, 30);
        }
        else if (pauseButton.getActionCommand().equalsIgnoreCase("Continue"))
        {
            pauseButton.setText("Pause");

            changeStatusProgressBar("Search continued", true, 0);
        }
    }

    private void stopButtonProcess()
    {
        changeStateElements(false, true);
        changeStatusProgressBar("Search stopped", false, 0);
    }

    protected void changeStatusProgressBar(String text, boolean isIndeterminate, int progress)
    {
        statusProgressBar.setString(text);
        statusProgressBar.setIndeterminate(isIndeterminate);
        statusProgressBar.setValue(progress);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup = new javax.swing.ButtonGroup();
        exitButton = new javax.swing.JButton();
        startButton = new javax.swing.JButton();
        findLabel = new javax.swing.JLabel();
        findTextField = new javax.swing.JTextField();
        ignoreCaseCheckBox = new javax.swing.JCheckBox();
        plainTextCheckBox = new javax.swing.JCheckBox();
        topSeparator = new javax.swing.JSeparator();
        locationLabel = new javax.swing.JLabel();
        locationTextField = new javax.swing.JTextField();
        openButton = new javax.swing.JButton();
        foldersRadioButton = new javax.swing.JRadioButton();
        filesRadioButton = new javax.swing.JRadioButton();
        allRadioButton = new javax.swing.JRadioButton();
        resultScrollPane = new javax.swing.JScrollPane();
        resultTable = new javax.swing.JTable();
        pauseButton = new javax.swing.JButton();
        stopButton = new javax.swing.JButton();
        statusProgressBar = new javax.swing.JProgressBar();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        exitButton.setText("Exit");
        exitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitButtonActionPerformed(evt);
            }
        });

        startButton.setText("Start");
        startButton.setEnabled(false);
        startButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startButtonActionPerformed(evt);
            }
        });

        findLabel.setText("Find:");

        ignoreCaseCheckBox.setText("Ignore case");

        plainTextCheckBox.setText("Plain text");

        locationLabel.setText("Location:");

        locationTextField.setEditable(false);

        openButton.setText("Open");
        openButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openButtonActionPerformed(evt);
            }
        });

        foldersRadioButton.setText("Folders");

        filesRadioButton.setText("Files");

        allRadioButton.setSelected(true);
        allRadioButton.setText("All");

        resultTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                resultTableMouseReleased(evt);
            }
        });
        resultTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                resultTableKeyReleased(evt);
            }
        });
        resultScrollPane.setViewportView(resultTable);

        pauseButton.setText("Pause");
        pauseButton.setEnabled(false);
        pauseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pauseButtonActionPerformed(evt);
            }
        });

        stopButton.setText("Stop");
        stopButton.setEnabled(false);
        stopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(topSeparator)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(resultScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 620, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(exitButton)
                        .addGap(18, 18, 18)
                        .addComponent(statusProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(stopButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pauseButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(startButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(findLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(ignoreCaseCheckBox)
                                .addGap(18, 18, 18)
                                .addComponent(plainTextCheckBox)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(findTextField)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(locationLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(foldersRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(filesRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(allRadioButton)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(locationTextField)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(openButton)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(findLabel)
                    .addComponent(findTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ignoreCaseCheckBox)
                    .addComponent(plainTextCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(topSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(locationLabel)
                    .addComponent(locationTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(openButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(foldersRadioButton)
                    .addComponent(filesRadioButton)
                    .addComponent(allRadioButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(resultScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 311, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(exitButton)
                        .addComponent(startButton)
                        .addComponent(pauseButton)
                        .addComponent(stopButton))
                    .addComponent(statusProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void exitButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_exitButtonActionPerformed
    {//GEN-HEADEREND:event_exitButtonActionPerformed
        try
        {
            formPosition.memorize();
            //waitingEndingSearchThread.join();
        }
        catch (Exception ex)
        {
            Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally
        {
            System.exit(0);
        }
    }//GEN-LAST:event_exitButtonActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt)//GEN-FIRST:event_formWindowClosing
    {//GEN-HEADEREND:event_formWindowClosing
        try
        {
            formPosition.memorize();
        }
        catch (Exception ex)
        {
            Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, ex);

            formPosition.showErrorMessageDialog(ex);
        }
    }//GEN-LAST:event_formWindowClosing

    private void openButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_openButtonActionPerformed
    {//GEN-HEADEREND:event_openButtonActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
        {
            locationTextField.setText(fileChooser.getSelectedFile().getPath());
        }
    }//GEN-LAST:event_openButtonActionPerformed

    private void startButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_startButtonActionPerformed
    {//GEN-HEADEREND:event_startButtonActionPerformed
        startButtonProcess();
    }//GEN-LAST:event_startButtonActionPerformed

    private void pauseButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_pauseButtonActionPerformed
    {//GEN-HEADEREND:event_pauseButtonActionPerformed
        if (evt.getActionCommand().equalsIgnoreCase("Pause"))
        {
            if (searchThread != null && searchThread.isAlive())
            {
                searchThread.setSuspended(true);
            }
        }
        else if (evt.getActionCommand().equalsIgnoreCase("Continue"))
        {
            if (searchThread != null && searchThread.isAlive())
            {
                searchThread.setSuspended(false);
            }
        }
        pauseButtonProcess();
    }//GEN-LAST:event_pauseButtonActionPerformed

    private void stopButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_stopButtonActionPerformed
    {//GEN-HEADEREND:event_stopButtonActionPerformed
        if (searchThread != null && searchThread.isAlive())
        {
            searchThread.setSuspended(true);
        }
        stopButtonProcess();
    }//GEN-LAST:event_stopButtonActionPerformed

    private void resultTableKeyReleased(java.awt.event.KeyEvent evt)//GEN-FIRST:event_resultTableKeyReleased
    {//GEN-HEADEREND:event_resultTableKeyReleased
//        if (evt.getKeyCode() == KeyEvent.VK_CONTEXT_MENU)
//        {
//            //
//        }
    }//GEN-LAST:event_resultTableKeyReleased

    private void resultTableMouseReleased(java.awt.event.MouseEvent evt)//GEN-FIRST:event_resultTableMouseReleased
    {//GEN-HEADEREND:event_resultTableMouseReleased
        if (evt.getButton() == MouseEvent.BUTTON3)
        {
            int row = resultTable.rowAtPoint(evt.getPoint());
            resultTable.setRowSelectionInterval(row, row);
//            int column = resultTable.columnAtPoint(evt.getPoint());
//            resultTable.setColumnSelectionInterval(column, column);

            resultPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }

//        if (evt.isPopupTrigger())
//        {
//            resultPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
//        }
    }//GEN-LAST:event_resultTableMouseReleased
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton allRadioButton;
    private javax.swing.ButtonGroup buttonGroup;
    private javax.swing.JButton exitButton;
    private javax.swing.JRadioButton filesRadioButton;
    private javax.swing.JLabel findLabel;
    private javax.swing.JTextField findTextField;
    private javax.swing.JRadioButton foldersRadioButton;
    private javax.swing.JCheckBox ignoreCaseCheckBox;
    private javax.swing.JLabel locationLabel;
    private javax.swing.JTextField locationTextField;
    private javax.swing.JButton openButton;
    private javax.swing.JButton pauseButton;
    private javax.swing.JCheckBox plainTextCheckBox;
    private javax.swing.JScrollPane resultScrollPane;
    private javax.swing.JTable resultTable;
    private javax.swing.JButton startButton;
    private javax.swing.JProgressBar statusProgressBar;
    private javax.swing.JButton stopButton;
    private javax.swing.JSeparator topSeparator;
    // End of variables declaration//GEN-END:variables
}
