/**
 * @name        Simple Java NotePad
 * @package ph.notepad
 * @file UI.java
 *
 * @author Pierre-Henry Soria
 * @email pierrehenrysoria@gmail.com
 * @link        http://github.com/pH-7
 *
 * @copyright   Copyright Pierre-Henry SORIA, All Rights Reserved.
 * @license     Apache (http://www.apache.org/licenses/LICENSE-2.0)
 * @create      2012-04-05
 * @update      2017-02-18
 *
 * @modifiedby  Achintha Gunasekara
 * @modemail    contact@achinthagunasekara.com
 *
 * @modifiedby  Marcus Redgrave-Close
 * @modemail    marcusrc1@hotmail.co.uk
 *
 * @Modifiedby SidaDan
 * @modemail Fschultz@sinf.de
 * Added Tooltip Combobox Font type and Font size
 * Overwrite method processWindowEvent to detect window closing event.
 * Added safety query to save the file before exit
 * or the user select "newfile"
 * Only available if the user has pressed a key
 * Added safety query if user pressed the clearButton
 *
 * @Modifiedby SidaDan
 * @modemail Fschultz@sinf.de
 * Removed unuse objects like container,  Border emptyBorder
 * Removed unsused imports
 *
 * @Modifiedby Giorgos Pasentsis
 * @modemail gpasents@gmail.com
 */
package simplejavatexteditor;

import java.lang.reflect.Method;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.*;
import javax.swing.event.DocumentEvent; 
import javax.swing.event.DocumentListener; 
import javax.swing.text.Element; 
import java.io.BufferedReader; 
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException; 
import java.io.InputStreamReader; 
import java.io.OutputStreamWriter; 
import java.io.PrintWriter; 
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Date; 
import java.text.SimpleDateFormat; 
import java.util.HashMap; 
import java.util.Map;
import javax.swing.text.DefaultEditorKit;

public class UI extends JFrame implements ActionListener {

    private final String[] dragDropExtensionFilter = {".txt", ".dat", ".log", ".xml", ".mf", ".html", ".java", ".cpp", ".py"};
    private static long serialVersionUID = 1L;
    
    // [Tabbed Interface] Main Component
    private final JTabbedPane tabbedPane;
    
    // [Status Bar] New component for User Assurance
    private final JLabel statusLabel;

    // [Tabbed Interface] Maps to track state per tab
    private final Map<JTextArea, File> fileMap = new HashMap<>();
    private final Map<JTextArea, JTextArea> linesMap = new HashMap<>();
    private final Map<JTextArea, AutoComplete> autoCompleteMap = new HashMap<>();

    private final JMenuBar menuBar;
    private final JComboBox<String> fontType;
    private final JComboBox<Integer> fontSize;
    private final JMenu menuFile, menuEdit, menuFind, menuAbout, menuRun; 
    
    private final JMenuItem newFile, openFile, saveFile, close, closeTab, cut, copy, paste, clearFile, selectAll, quickFind,
            aboutMe, aboutSoftware, wordWrap, itemRun, fileProperties; 
            
    private final JToolBar mainToolbar;
    
    // Auto-Save Checkbox
    private final JCheckBoxMenuItem itemAutoSave; 
    private volatile boolean isAutoSaveEnabled = false; 

    JButton newButton, openButton, saveButton, clearButton, quickButton, aboutMeButton, aboutButton, closeButton, boldButton, italicButton;

    //setup icons
    private final ImageIcon boldIcon = new ImageIcon(UI.class.getResource("icons/bold.png"));
    private final ImageIcon italicIcon = new ImageIcon(UI.class.getResource("icons/italic.png"));
    private final ImageIcon newIcon = new ImageIcon(UI.class.getResource("icons/new.png"));
    private final ImageIcon openIcon = new ImageIcon(UI.class.getResource("icons/open.png"));
    private final ImageIcon saveIcon = new ImageIcon(UI.class.getResource("icons/save.png"));
    private final ImageIcon closeIcon = new ImageIcon(UI.class.getResource("icons/close.png"));
    private final ImageIcon clearIcon = new ImageIcon(UI.class.getResource("icons/clear.png"));
    private final ImageIcon cutIcon = new ImageIcon(UI.class.getResource("icons/cut.png"));
    private final ImageIcon copyIcon = new ImageIcon(UI.class.getResource("icons/copy.png"));
    private final ImageIcon pasteIcon = new ImageIcon(UI.class.getResource("icons/paste.png"));
    private final ImageIcon selectAllIcon = new ImageIcon(UI.class.getResource("icons/selectall.png"));
    private final ImageIcon wordwrapIcon = new ImageIcon(UI.class.getResource("icons/wordwrap.png"));
    private final ImageIcon searchIcon = new ImageIcon(UI.class.getResource("icons/search.png"));
    private final ImageIcon aboutMeIcon = new ImageIcon(UI.class.getResource("icons/about_me.png"));
    private final ImageIcon aboutIcon = new ImageIcon(UI.class.getResource("icons/about.png"));

    private SupportedKeywords kw = new SupportedKeywords();
    private HighlightText languageHighlighter = new HighlightText(Color.GRAY);

    public UI() {
        try {
            ImageIcon image = new ImageIcon(UI.class.getResource("icons/ste.png"));
            super.setIconImage(image.getImage());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setSize(800, 500);
        setTitle("Simple Java Text Editor");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Layout
        getContentPane().setLayout(new BorderLayout()); 
        tabbedPane = new JTabbedPane();
        getContentPane().add(tabbedPane, BorderLayout.CENTER);
        
        // [Status Bar] Initialize
        statusLabel = new JLabel(" Ready");
        statusLabel.setBorder(BorderFactory.createEtchedBorder());
        getContentPane().add(statusLabel, BorderLayout.SOUTH); // Add to bottom

        // Menus
        menuFile = new JMenu("File");
        menuEdit = new JMenu("Edit");
        menuFind = new JMenu("Search");
        menuRun = new JMenu("Run"); 
        menuAbout = new JMenu("About");
        
        // Menu Items
        newFile = new JMenuItem("New Tab", newIcon); 
        openFile = new JMenuItem("Open", openIcon);
        saveFile = new JMenuItem("Save", saveIcon);
        fileProperties = new JMenuItem("File Properties");
        fileProperties.addActionListener(this);
        
        closeTab = new JMenuItem("Close Tab"); 
        closeTab.addActionListener(this);
        
        close = new JMenuItem("Quit", closeIcon);
        clearFile = new JMenuItem("Clear", clearIcon);
        quickFind = new JMenuItem("Quick", searchIcon);
        aboutMe = new JMenuItem("About Me", aboutMeIcon);
        aboutSoftware = new JMenuItem("About Software", aboutIcon);
        
        // Run Item
        itemRun = new JMenuItem("Run Code (Java/C++/Py)");
        itemRun.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK)); 
        itemRun.addActionListener(this);

        // Auto-Save Item
        itemAutoSave = new JCheckBoxMenuItem("Auto Save (10s)");
        itemAutoSave.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                isAutoSaveEnabled = itemAutoSave.isSelected();
                if(isAutoSaveEnabled) {
                    JOptionPane.showMessageDialog(UI.this, "Auto-Save Enabled! Open files will save every 10 seconds.");
                } else {
                    statusLabel.setText(" Auto-Save Disabled");
                }
            }
        });

        menuBar = new JMenuBar();
        menuBar.add(menuFile);
        menuBar.add(menuEdit);
        menuBar.add(menuFind);
        menuBar.add(menuRun); 
        menuBar.add(menuAbout);

        this.setJMenuBar(menuBar);

        newFile.addActionListener(this);  
        newFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK)); 
        menuFile.add(newFile); 

        openFile.addActionListener(this);
        openFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        menuFile.add(openFile);

        saveFile.addActionListener(this);
        saveFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        menuFile.add(saveFile);

        menuFile.addSeparator();
        menuFile.add(fileProperties);
        menuFile.addSeparator();

        menuFile.add(itemAutoSave);
        
        menuFile.addSeparator();
        menuFile.add(closeTab);

        close.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        close.addActionListener(this);
        menuFile.add(close);

        selectAll = new JMenuItem("Select All", selectAllIcon);
        selectAll.setToolTipText("Select All");
        selectAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK));
        selectAll.addActionListener(this); 
        menuEdit.add(selectAll);

        clearFile.addActionListener(this);
        clearFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.CTRL_DOWN_MASK));
        menuEdit.add(clearFile);

        cut = new JMenuItem(new DefaultEditorKit.CutAction());
        cut.setText("Cut");
        cut.setIcon(cutIcon);
        cut.setToolTipText("Cut");
        cut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK));
        menuEdit.add(cut);

        wordWrap = new JMenuItem();
        wordWrap.setText("Word Wrap");
        wordWrap.setIcon(wordwrapIcon);
        wordWrap.setToolTipText("Word Wrap");
        wordWrap.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK));
        menuEdit.add(wordWrap);

        wordWrap.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                JTextArea ta = getCurrentTextArea();
                if (ta != null) {
                    ta.setLineWrap(!ta.getLineWrap());
                    statusLabel.setText(ta.getLineWrap() ? " Word Wrap: ON" : " Word Wrap: OFF");
                }
            }
        });

        copy = new JMenuItem(new DefaultEditorKit.CopyAction());
        copy.setText("Copy");
        copy.setIcon(copyIcon);
        copy.setToolTipText("Copy");
        copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
        menuEdit.add(copy);

        paste = new JMenuItem(new DefaultEditorKit.PasteAction());
        paste.setText("Paste");
        paste.setIcon(pasteIcon);
        paste.setToolTipText("Paste");
        paste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));
        menuEdit.add(paste);

        quickFind.addActionListener(this);
        quickFind.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK));
        menuFind.add(quickFind);
        
        menuRun.add(itemRun);

        aboutMe.addActionListener(this);
        aboutMe.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        menuAbout.add(aboutMe);

        aboutSoftware.addActionListener(this);
        aboutSoftware.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
        menuAbout.add(aboutSoftware);

        mainToolbar = new JToolBar();
        this.add(mainToolbar, BorderLayout.NORTH);
        
        newButton = new JButton(newIcon);
        newButton.setToolTipText("New Tab");
        newButton.addActionListener(this);
        mainToolbar.add(newButton);
        mainToolbar.addSeparator();

        openButton = new JButton(openIcon);
        openButton.setToolTipText("Open");
        openButton.addActionListener(this);
        mainToolbar.add(openButton);
        mainToolbar.addSeparator();

        saveButton = new JButton(saveIcon);
        saveButton.setToolTipText("Save");
        saveButton.addActionListener(this);
        mainToolbar.add(saveButton);
        mainToolbar.addSeparator();

        clearButton = new JButton(clearIcon);
        clearButton.setToolTipText("Clear All");
        clearButton.addActionListener(this);
        mainToolbar.add(clearButton);
        mainToolbar.addSeparator();

        quickButton = new JButton(searchIcon);
        quickButton.setToolTipText("Quick Search");
        quickButton.addActionListener(this);
        mainToolbar.add(quickButton);
        mainToolbar.addSeparator();

        aboutMeButton = new JButton(aboutMeIcon);
        aboutMeButton.setToolTipText("About Me");
        aboutMeButton.addActionListener(this);
        mainToolbar.add(aboutMeButton);
        mainToolbar.addSeparator();

        aboutButton = new JButton(aboutIcon);
        aboutButton.setToolTipText("About NotePad PH");
        aboutButton.addActionListener(this);
        mainToolbar.add(aboutButton);
        mainToolbar.addSeparator();

        closeButton = new JButton(closeIcon);
        closeButton.setToolTipText("Quit");
        closeButton.addActionListener(this);
        mainToolbar.add(closeButton);
        mainToolbar.addSeparator();

        boldButton = new JButton(boldIcon);
        boldButton.setToolTipText("Bold");
        boldButton.addActionListener(this);
        mainToolbar.add(boldButton);
        mainToolbar.addSeparator();

        italicButton = new JButton(italicIcon);
        italicButton.setToolTipText("Italic");
        italicButton.addActionListener(this);
        mainToolbar.add(italicButton);
        mainToolbar.addSeparator();

        // Fonts
        fontType = new JComboBox<String>();
        String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (String font : fonts) {
            fontType.addItem(font);
        }
        fontType.setMaximumSize(new Dimension(170, 30));
        fontType.setToolTipText("Font Type");
        mainToolbar.add(fontType);
        mainToolbar.addSeparator();

        fontType.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                String p = fontType.getSelectedItem().toString();
                JTextArea ta = getCurrentTextArea();
                if (ta != null) {
                    int s = ta.getFont().getSize();
                    ta.setFont(new Font(p, Font.PLAIN, s));
                    JTextArea lines = linesMap.get(ta);
                    if (lines != null) lines.setFont(new Font(p, Font.PLAIN, s));
                }
            }
        });

        fontSize = new JComboBox<Integer>();
        for (int i = 5; i <= 100; i++) {
            fontSize.addItem(i);
        }
        fontSize.setMaximumSize(new Dimension(70, 30));
        fontSize.setToolTipText("Font Size");
        mainToolbar.add(fontSize);

        fontSize.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                String sizeValue = fontSize.getSelectedItem().toString();
                int sizeOfFont = Integer.parseInt(sizeValue);
                JTextArea ta = getCurrentTextArea();
                if (ta != null) {
                    String fontFamily = ta.getFont().getFamily();
                    Font font1 = new Font(fontFamily, Font.PLAIN, sizeOfFont);
                    ta.setFont(font1);
                    JTextArea lines = linesMap.get(ta);
                    if (lines != null) lines.setFont(font1);
                }
            }
        });
        
        // Initial Tab
        createNewTab(null, "Untitled");
        
        startAutoSaveThread();
    }
    
    // [FIXED] Updated logic: Add tab to pane BEFORE enabling AutoComplete
    // This ensures 'getEditor()' returns a valid component, preventing NPE/BadLocationException.
    private void createNewTab(File file, String title) {
        JTextArea textArea = new JTextArea();
        textArea.setFont(new Font("Century Gothic", Font.PLAIN, 12));
        textArea.setTabSize(2);
        textArea.setLineWrap(true);
        
        JTextArea lines = new JTextArea("1");
        lines.setBackground(Color.LIGHT_GRAY);
        lines.setEditable(false);
        lines.setFont(new Font("Century Gothic", Font.PLAIN, 12)); 
        lines.setMargin(new Insets(0, 5, 0, 5)); 
        
        if (file != null) {
            fileMap.put(textArea, file);
        }
        linesMap.put(textArea, lines);

        textArea.getDocument().addDocumentListener(new DocumentListener() {
            public void getText() {
                updateLineNumbers(textArea, lines);
            }
            @Override public void changedUpdate(DocumentEvent de) { getText(); }
            @Override public void insertUpdate(DocumentEvent de) { getText(); }
            @Override public void removeUpdate(DocumentEvent de) { getText(); }
        });
        
        textArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent ke) {
                languageHighlighter.highLight(textArea, kw.getCppKeywords());
                languageHighlighter.highLight(textArea, kw.getJavaKeywords());
            }
        });
        
        DropTarget dropTarget = new DropTarget(textArea, dropTargetListener);

        if (file != null) {
            try {
                Scanner scan = new Scanner(new FileReader(file));
                while (scan.hasNext()) {
                    textArea.append(scan.nextLine() + "\n");
                }
                scan.close();
                updateLineNumbers(textArea, lines); 
            } catch (Exception ex) { ex.printStackTrace(); }
        }

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setRowHeaderView(lines);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        // 1. Add the tab first
        tabbedPane.addTab(title, scrollPane);
        // 2. Select it to make it the 'Current' Editor
        tabbedPane.setSelectedComponent(scrollPane);
        
        // 3. NOW enable AutoComplete (it will find the correct editor)
        if (file != null) {
            enableAutoComplete(file, textArea);
        }
        
        // [Assurance] Reset status
        statusLabel.setText(" Opened: " + title);
    }
    
    private JTextArea getCurrentTextArea() {
        Component c = tabbedPane.getSelectedComponent();
        if (c instanceof JScrollPane) {
            JViewport viewport = ((JScrollPane) c).getViewport();
            return (JTextArea) viewport.getView();
        }
        return null; 
    }
    
    // [Kept for Compatibility] Used by AutoComplete.java
    public JTextArea getEditor() {
        return getCurrentTextArea();
    }
    
    private File getCurrentFile() {
        JTextArea ta = getCurrentTextArea();
        if (ta != null) {
            return fileMap.get(ta);
        }
        return null;
    }
    
    private void updateLineNumbers(JTextArea ta, JTextArea lines) {
        int totalLines = ta.getLineCount();
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= totalLines; i++) {
            sb.append(i).append(System.lineSeparator());
        }
        lines.setText(sb.toString());
    }
    
    private void showFileProperties() {
        File currentFile = getCurrentFile();
        if (currentFile == null) {
            JOptionPane.showMessageDialog(this, "This tab is not saved to a file yet!", "No File Open", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        String lastModified = sdf.format(new Date(currentFile.lastModified()));
        long sizeBytes = currentFile.length();
        String sizeKB = String.format("%.2f KB", sizeBytes / 1024.0);

        StringBuilder props = new StringBuilder();
        props.append("Name: ").append(currentFile.getName()).append("\n");
        props.append("Path: ").append(currentFile.getAbsolutePath()).append("\n");
        props.append("Size: ").append(sizeKB).append(" (").append(sizeBytes).append(" bytes)\n");
        props.append("Last Modified: ").append(lastModified).append("\n");
        props.append("Readable: ").append(currentFile.canRead() ? "Yes" : "No").append("\n");
        props.append("Writable: ").append(currentFile.canWrite() ? "Yes" : "No").append("\n");

        JOptionPane.showMessageDialog(this, props.toString(), "File Properties", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    protected void processWindowEvent(WindowEvent e) {
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            System.exit(0); 
        }
    }

    public void enableAutoComplete(File file, JTextArea textArea) {
        if (autoCompleteMap.containsKey(textArea)) {
            textArea.getDocument().removeDocumentListener(autoCompleteMap.get(textArea));
            autoCompleteMap.remove(textArea);
        }

        ArrayList<String> arrayList = null;
        String[] list = kw.getSupportedLanguages();

        for (int i = 0; i < list.length; i++) {
            if (file.getName().endsWith(list[i])) {
                switch (i) {
                    case 0 -> {
                        String[] jk = kw.getJavaKeywords();
                        arrayList = kw.setKeywords(jk);
                    }
                    case 1 -> {
                        String[] ck = kw.getCppKeywords();
                        arrayList = kw.setKeywords(ck);
                    }
                }
            }
        }
        
        if (arrayList != null) {
            AutoComplete newAutoComplete = new AutoComplete(this, arrayList);
            textArea.getDocument().addDocumentListener(newAutoComplete);
            autoCompleteMap.put(textArea, newAutoComplete);
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == close || e.getSource() == closeButton) {
            System.exit(0);
        } else if (e.getSource() == newFile || e.getSource() == newButton) {
            createNewTab(null, "Untitled");
        } else if (e.getSource() == closeTab) {
            int idx = tabbedPane.getSelectedIndex();
            if (idx != -1) {
                JTextArea ta = getCurrentTextArea();
                fileMap.remove(ta);
                linesMap.remove(ta);
                autoCompleteMap.remove(ta);
                tabbedPane.remove(idx);
            }
        } else if (e.getSource() == openFile || e.getSource() == openButton) {
            JFileChooser open = new JFileChooser(); 
            int option = open.showOpenDialog(this); 
            if (option == JFileChooser.APPROVE_OPTION) {
                File openFile = open.getSelectedFile();
                createNewTab(openFile, openFile.getName());
            }
        } else if (e.getSource() == saveFile || e.getSource() == saveButton) {
            saveFile();
        } else if (e.getSource() == fileProperties) {
            showFileProperties(); 
        } else if (e.getSource() == clearFile || e.getSource() == clearButton) {
            JTextArea ta = getCurrentTextArea();
            if (ta != null) {
                Object[] options = {"Yes", "No"};
                int n = JOptionPane.showOptionDialog(this, "Are you sure to clear the text Area ?", "Question",
                        JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
                if (n == 0) ta.setText("");
            }
        } else if (e.getSource() == quickFind || e.getSource() == quickButton) {
            JTextArea ta = getCurrentTextArea();
            if (ta != null) new Find(ta);
        } else if (e.getSource() == aboutMe || e.getSource() == aboutMeButton) {
            new About(this).me();
        } else if (e.getSource() == aboutSoftware || e.getSource() == aboutButton) {
            new About(this).software();
        } else if (e.getSource() == itemRun) {
            runCode();
        } else if (e.getSource() == selectAll) { 
            JTextArea ta = getCurrentTextArea();
            if (ta != null) ta.selectAll();
        }
    }

    private void saveFile() {
        JTextArea ta = getCurrentTextArea();
        if (ta == null) return;
        
        File currentFile = fileMap.get(ta);
        
        if (currentFile == null) { 
            JFileChooser fileChoose = new JFileChooser();
            int option = fileChoose.showSaveDialog(this);
            if (option == JFileChooser.APPROVE_OPTION) {
                try {
                    File openFile = fileChoose.getSelectedFile();
                    fileMap.put(ta, openFile); 
                    tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(), openFile.getName()); 
                    
                    BufferedWriter out = new BufferedWriter(new FileWriter(openFile.getPath()));
                    out.write(ta.getText());
                    out.close();
                    
                    enableAutoComplete(openFile, ta);
                    
                    // [Assurance] Update Label
                    String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
                    statusLabel.setText(" Saved successfully at " + time);
                    
                } catch (Exception ex) { 
                    System.err.println(ex.getMessage());
                }
            }
        } else { 
            try {
                BufferedWriter out = new BufferedWriter(new FileWriter(currentFile.getPath()));
                out.write(ta.getText());
                out.close();
                
                // [Assurance] Update Label
                String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
                statusLabel.setText(" Saved successfully at " + time);
                
            } catch (Exception ex) { 
                System.err.println(ex.getMessage());
            }
        }
    }

    DropTargetListener dropTargetListener = new DropTargetListener() {
        @Override public void dragEnter(DropTargetDragEvent e) {}
        @Override public void dragExit(DropTargetEvent e) {}
        @Override public void dragOver(DropTargetDragEvent e) {}
        @Override public void drop(DropTargetDropEvent e) {
            try {
                Transferable tr = e.getTransferable();
                DataFlavor[] flavors = tr.getTransferDataFlavors();
                for (DataFlavor flavor : flavors) {
                    if (flavor.isFlavorJavaFileListType()) {
                        e.acceptDrop(e.getDropAction());
                        java.util.List files = (java.util.List) tr.getTransferData(flavor);
                        for (Object o : files) {
                            File file = (File) o;
                            createNewTab(file, file.getName());
                        }
                        e.dropComplete(true);
                        return;
                    }
                }
            } catch (Throwable t) { t.printStackTrace(); }
            e.rejectDrop();
        }
        @Override public void dropActionChanged(DropTargetDragEvent e) {}
    };

    public void runCode() {
        File currentFile = getCurrentFile(); 
        if (currentFile == null) {
            JOptionPane.showMessageDialog(this, "Please save the file first!");
            return;
        }

        new Thread(() -> {
            try {
                String parentDir = currentFile.getParent();
                String fileName = currentFile.getName();
                
                ProcessBuilder pb = null;
                String executableName = fileName.replace(".cpp", "").replace(".java", "");
                
                String tempDir = System.getProperty("java.io.tmpdir"); 

                if (fileName.endsWith(".java")) {
                    ProcessBuilder compileBuilder = new ProcessBuilder("javac", fileName);
                    compileBuilder.directory(new File(parentDir));
                    compileBuilder.redirectErrorStream(true);
                    Process compileProcess = compileBuilder.start();
                    String compileOut = readProcessOutput(compileProcess);
                    if (compileProcess.waitFor() != 0) {
                        showOutputDialog("Java Compilation Error", compileOut);
                        return; 
                    }
                    pb = new ProcessBuilder("java", executableName);
                    pb.directory(new File(parentDir));
                
                } else if (fileName.endsWith(".cpp")) {
                    String exeOutput = tempDir + File.separator + executableName + (System.getProperty("os.name").toLowerCase().contains("win") ? ".exe" : "");
                    
                    ProcessBuilder compileBuilder = new ProcessBuilder("g++", fileName, "-o", exeOutput);
                    compileBuilder.directory(new File(parentDir));
                    compileBuilder.redirectErrorStream(true);
                    Process compileProcess = compileBuilder.start();
                    String compileOut = readProcessOutput(compileProcess);
                    if (compileProcess.waitFor() != 0) {
                        showOutputDialog("C++ Compilation Error", compileOut);
                        return;
                    }
                    
                    pb = new ProcessBuilder(exeOutput);
                    pb.directory(new File(parentDir)); 

                } else if (fileName.endsWith(".py")) {
                    pb = new ProcessBuilder("python", fileName); 
                    pb.directory(new File(parentDir));
                
                } else {
                    JOptionPane.showMessageDialog(this, "File extension not supported for running.");
                    return;
                }

                if (pb != null) {
                    pb.redirectErrorStream(true);
                    Process process = pb.start();
                    SwingUtilities.invokeLater(() -> new ConsoleWindow(process));
                }

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }).start();
    }

    private String readProcessOutput(Process p) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }
        return output.toString();
    }

    private void showOutputDialog(String title, String content) {
        SwingUtilities.invokeLater(() -> {
            JTextArea textArea = new JTextArea(content);
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new java.awt.Dimension(500, 300));
            JOptionPane.showMessageDialog(this, scrollPane, title, JOptionPane.INFORMATION_MESSAGE);
        });
    }

    // [FIXED] Thread-safe Auto-Save Helper
    // Uses SwingUtilities.invokeAndWait to read text safely, preventing BadLocationException
    private void autoSaveHelper() {
        for (Map.Entry<JTextArea, File> entry : fileMap.entrySet()) {
            File f = entry.getValue();
            JTextArea t = entry.getKey();
            if (f != null && t != null) {
                try {
                    // Safe way to get text from GUI thread
                    final String[] text = new String[1];
                    try {
                        SwingUtilities.invokeAndWait(() -> text[0] = t.getText());
                    } catch (Exception e) {
                        continue; // Skip if UI interaction failed
                    }
                    
                    BufferedWriter out = new BufferedWriter(new FileWriter(f.getPath()));
                    out.write(text[0]);
                    out.close();
                    
                    // [Assurance] Visual feedback on Auto-Save
                    String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
                    SwingUtilities.invokeLater(() -> statusLabel.setText(" Auto-saved " + f.getName() + " at " + time));
                    
                } catch (Exception ex) {
                    System.err.println("Auto-save failed for " + f.getName());
                }
            }
        }
    }

    private void startAutoSaveThread() {
        Thread backgroundThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(10000); 
                    if (isAutoSaveEnabled) {
                        autoSaveHelper();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        
        backgroundThread.setDaemon(true); 
        backgroundThread.start();
    }
    
    class ConsoleWindow extends JFrame {
        private final JTextArea outputArea;
        private final JTextField inputField;
        private final Process process;
        private final PrintWriter writer;

        public ConsoleWindow(Process p) {
            this.process = p;
            
            setTitle("Console Output");
            setSize(600, 400);
            setLocationRelativeTo(null);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            
            setLayout(new BorderLayout());
            
            outputArea = new JTextArea();
            outputArea.setEditable(false);
            outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            outputArea.setBackground(Color.BLACK);
            outputArea.setForeground(Color.LIGHT_GRAY);
            JScrollPane scrollPane = new JScrollPane(outputArea);
            add(scrollPane, BorderLayout.CENTER);
            
            inputField = new JTextField();
            inputField.setBackground(Color.DARK_GRAY);
            inputField.setForeground(Color.WHITE);
            inputField.setCaretColor(Color.WHITE);
            inputField.setFont(new Font("Monospaced", Font.PLAIN, 12));
            add(inputField, BorderLayout.SOUTH);
            
            writer = new PrintWriter(new OutputStreamWriter(process.getOutputStream()), true);
            
            inputField.addActionListener(e -> {
                String text = inputField.getText();
                writer.println(text); 
                outputArea.append(text + "\n"); 
                outputArea.setCaretPosition(outputArea.getDocument().getLength());
                inputField.setText("");
            });
            
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    if (process.isAlive()) {
                        process.destroy();
                    }
                }
            });
            
            new Thread(() -> {
                try (InputStreamReader isr = new InputStreamReader(process.getInputStream());
                     BufferedReader reader = new BufferedReader(isr)) {
                    
                    char[] buffer = new char[1024];
                    int bytesRead;
                    
                    while ((bytesRead = reader.read(buffer)) != -1) {
                        String chunk = new String(buffer, 0, bytesRead);
                        
                        SwingUtilities.invokeLater(() -> {
                            outputArea.append(chunk);
                            outputArea.setCaretPosition(outputArea.getDocument().getLength());
                        });
                    }
                } catch (IOException ex) {
                }
                
                SwingUtilities.invokeLater(() -> {
                    outputArea.append("\n[Process Finished with exit code " + process.exitValue() + "]\n");
                    inputField.setEditable(false);
                });
            }).start();
            
            setVisible(true);
        }
    }
}