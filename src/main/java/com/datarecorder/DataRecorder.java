package com.datarecorder;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.event.CellEditorListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.slf4j.Logger;

import com.datarecorder.plugins.injector.ImageInjector;
import com.datarecorder.util.ManifestReader;
import com.datarecorder.util.MessagingUtils;
import com.datarecorder.util.ui.AboutDialog;
import com.datarecorder.util.ui.MessageBox;
import com.datarecorder.util.ui.WindowUtils;

/**
 *
 */
public class DataRecorder extends JFrame implements TableCellEditor {

    private static final long serialVersionUID = 646748598034496961L;

    // map of brokers: Key is the actual string used for the connection, such as
    // failover://(tcp://localhost:61616)
    // and the value is the actual broker instance
    static Map<String, MessagingUtils> CONNECTED_BROKERS = new HashMap<String, MessagingUtils>();

    public static ResourceBundle bundle;
    private static final String EMPTY_STRING = "";
    private static final String ICONS_ONLY_PROPERTY = "iconsOnly";
    private static final String WIDTH_PROPERTY = "width";
    private static final String HEIGHT_PROPERTY = "height";
    private static final String X_PROPERTY = "windowX";
    private static final String Y_PROPERTY = "windowY";
    private static final String PROJECT_DIRECTORY_PROPERTY = "projectDirectory";
    private static final String PUBLISHER_DIRECTORY_PROPERTY = "publisherDirectory";
    private static final String SUBSCRIBER_DIRECTORY_PROPERTY = "subscriberDirectory";
    private static final String INJECTOR_DIRECTORY_PROPERTY = "injectorDirectory";
    private static final String AUTOSAVE_PROPERTY = "autoSaveOnClose";
    public static final String FILTER = "configuration";
    private static final String BUNDLE_PATH = "bundlePath";
    public static final String PROPERTY_FILE = "datarecorder.properties";
    private static final String PRELOAD_TOPIC_NAMES = "preloadTopicNames";
    private static final String PRELOAD_BROKER_NAMES = "preloadBrokerNames";
    private static final String LEGACY_FILE_MESSAGE = "A project last saved by a previous version of DataRecorder has been detected.\n"
            + "All destinations are assumed to be Topics.";
    private static final String WARNING = "Warning";
    private static Properties propertiesFile;

    private static final String STOP_LABEL = "Stop ";
    private static final String PAUSE_LABEL = "Pause ";
    private static final String START_LABEL = "Start ";
    private static final String DELETE_LABEL = "Delete ";
    private static final String COPY_LABEL = "Copy ";
    private static final String SUB_LABEL = "Subscriber ";
    private static final String PUB_LABEL = "Publisher ";
    private static final String SAVE_LABEL = "Save ";
    private static final String OPEN_LABEL = "Open ";
    private static final String NEW_LABEL = "New  ";
    private static final String SELECT_ALL_LABEL = "Select All ";
    private static final String CLEAR_ALL_LABEL = "Clear All ";
    private static final String RESET_LABEL = "Reset ";
    private static final String DELETE_WARNING = "Are you sure that you want to delete the selection?";
    private static final String DELETE_WARNING_MANY = "Are you sure that you want to delete the selections?";

    private static final String ASTERISK = "*";
    private static final String COMMA = ",";
    private static final char DOT = '.';
    private static final String HYPHEN = " - ";
    private static final String DRP_EXTENSION = ".drp";
    private static final String TITLE = " Data Recorder";
    private static final String VIEWING_PUB = "Viewing Publication ";
    private static final String VIEWING_SUB = "Viewing Subscription ";
    private static final String SEPARATOR = System.getProperty("file.separator");
    private static final String DASH = HYPHEN;
    private static final String UNTITLED = "Untitled";
    private static final String INJECTING = "Injecting ";

    private File projectFile;
    private String bundlePath;
    private boolean saveNeeded;
    // States of these buttons are effect by show/hide label
    private JButton newButton;
    private JButton openButton;
    private JButton saveButton;
    private JButton deleteButton;
    private JButton copyButton;
    private JButton publisherButton;
    private JButton subscriberButton;
    private JButton selectAllButton;
    private JButton clearAllButton;
    private JButton resetButton;
    private JButton stopButton;
    private JButton startButton;
    private JButton pauseButton;
    /**
     * Sets the list of topic names of the broker this subscriber is connected to. Note JMX must be turned on in the
     * server start script and also, the port for the JMX server must be 1099.
     * 
     */

    private JToolBar toolBar;
    private JPanel centerPanel;
    private JPanel statusBar;
    private JLabel statusLabel;

    private JFileChooser fileChooser;
    private PropertyEditor injectorPropertyEditor;
    private PropertyEditor viewerPropertyEditor;

    // Menu Items (these maintain the checked state)
    private JCheckBoxMenuItem iconLabelMenuItem;
    private JCheckBoxMenuItem autoSaveMenuItem;

    // Pop-up Menu Items (state is maintained enable/disabled)
    private JPopupMenu publisherPopupMenu;
    private JMenuItem pPublisherAddMenuItem;
    private JMenuItem pPublisherEditMenuItem;
    private JMenuItem pPublisherDeleteMenuItem;
    private JMenuItem pPublisherCopyMenuItem;
    private JMenuItem pPublisherStartMenuItem;
    private JMenuItem pPublisherPauseMenuItem;
    private JMenuItem pPublisherStopMenuItem;
    private JMenuItem pPublisherResetMenuItem;
    private JMenuItem pPublisherViewMenuItem;
    private JMenuItem pPublisherInjectorMenuItem;

    private JPopupMenu subscriberPopupMenu;
    private JMenuItem pSubscriberAddMenuItem;
    private JMenuItem pSubscriberEditMenuItem;
    private JMenuItem pSubscriberCopyMenuItem;
    private JMenuItem pSubscriberDeleteMenuItem;
    private JMenuItem pSubscriberStartMenuItem;
    private JMenuItem pSubscriberPauseMenuItem;
    private JMenuItem pSubscriberStopMenuItem;
    private JMenuItem pSubscriberResetMenuItem;
    private JMenuItem pSubscriberViewMenuItem;

    private int selectedPublisherRow;
    private int selectedSubscriberRow;

    private PublisherEditor publisherPropertiesEditor;
    private SubscriberEditor subscriberPropertiesEditor;

    private JScrollPane publishPane;
    private JScrollPane subscriberPane;

    public JTable publisherTable;
    public JTable subscriberTable;

    // public DefaultTableModel publisherModel;
    public TableSorter publisherModel;
    public TableSorter subscriberModel;

    // public static String jmxURL;
    private List<String> preloadTopicNames = new ArrayList<String>();
    private List<String> preloadBrokerNames = new ArrayList<String>();

    private Logger logger = LoggingObject.getLogger(this.getClass());

    public static void main(final String[] args) {
        final DataRecorder dr = new DataRecorder();
        dr.setVisible(true);
        try {
            if (args.length == 1) {
                // try to open file
                dr.setProjectFile(new File(args[0]));
                dr.openProject();
            }
        } catch (final Exception ex) {
            System.out.println("An error occured. " + ex);
        }
    }

    public DataRecorder() {
        super(TITLE + DASH + UNTITLED);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        WindowUtils.setNativeLookAndFeel();
        init();
    }

    private void init() {

        // The datarecorder.properties is located here:
        // current run directory
        propertiesFile = Util.getPropertyFile(this, DataRecorder.PROPERTY_FILE);

        // get the list of topics to use in the topic combobox
        final String preloadTopicsString = propertiesFile.getProperty(FILTER + DOT + PRELOAD_TOPIC_NAMES);
        if (preloadTopicsString != null && !preloadTopicsString.trim().equals("")) {
            int beginInd = 0;
            int endInd = preloadTopicsString.indexOf(",");
            while (endInd != -1) {
                preloadTopicNames.add(preloadTopicsString.substring(beginInd, endInd));
                beginInd = endInd + 1;
                endInd = preloadTopicsString.indexOf(",", endInd + 1);
            }
            preloadTopicNames.add(preloadTopicsString.substring(beginInd));
        }

        // get the list of brokers to use in the broker combobox
        final String preloadBrokersString = propertiesFile.getProperty(FILTER + DOT + PRELOAD_BROKER_NAMES);
        if (preloadBrokersString != null && !preloadBrokersString.trim().equals("")) {
            int beginInd = 0;
            int endInd = preloadBrokersString.indexOf(",");
            while (endInd != -1) {
                preloadBrokerNames.add(preloadBrokersString.substring(beginInd, endInd));
                beginInd = endInd + 1;
                endInd = preloadBrokersString.indexOf(",", endInd + 1);
            }
            preloadBrokerNames.add(preloadBrokersString.substring(beginInd));
        }

        bundlePath = "com.datarecorder.bundle";
        if (bundle == null) {
            try {
                bundle = ResourceBundle.getBundle(bundlePath);
            } catch (final MissingResourceException ex1) {
                MessageBox.showWarning(new JFrame(), ex1.getMessage());
            }
        }

        setIconImage(Util.getImageFromResource(bundle, "dataRecorder.SmallIcon").getImage());

        createComponents();
        createPopUpMenus();
        createMenuBar();
        createStatusBar();
        createFileChooser();

        // layout the components
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(toolBar, BorderLayout.NORTH);
        getContentPane().add(centerPanel, BorderLayout.CENTER);
        getContentPane().add(statusBar, BorderLayout.SOUTH);
        configure();
    }

    public List<String> getPreloadTopicNames() {
        return this.preloadTopicNames;
    }

    public List<String> getPreloadBrokerNames() {
        return this.preloadBrokerNames;
    }

    /**
     * createComponents
     */
    private void createComponents() {

        class CheckBoxColumn extends AbstractCellEditor implements TableCellRenderer, TableCellEditor, ActionListener {
            private static final long serialVersionUID = -2436962965501036682L;
            JTable table;
            JCheckBox renderCheckBox;
            JCheckBox editCheckBox;
            String currentValue;

            public CheckBoxColumn(final JTable t, final int column) {
                super();
                table = t;
                renderCheckBox = new JCheckBox();
                renderCheckBox.setHorizontalAlignment(JLabel.CENTER);

                editCheckBox = new JCheckBox();
                editCheckBox.addActionListener(this);
                editCheckBox.setHorizontalAlignment(JLabel.CENTER);

                final TableColumnModel columnModel = table.getColumnModel();
                columnModel.getColumn(column).setCellRenderer(this);
                columnModel.getColumn(column).setCellEditor(this);
            }

            public Component getTableCellRendererComponent(final JTable table, final Object value,
                    final boolean isSelected, final boolean hasFocus, final int row, final int column) {
                if (isSelected) {
                    renderCheckBox.setForeground(table.getSelectionForeground());
                    renderCheckBox.setBackground(table.getSelectionBackground());
                } else {
                    renderCheckBox.setForeground(table.getForeground());
                    renderCheckBox.setBackground(table.getBackground());
                }

                if (value != null)
                    if (value.toString().equals("On") || Boolean.getBoolean(value.toString()) || value == Boolean.TRUE)
                        renderCheckBox.setSelected(true);
                    else
                        renderCheckBox.setSelected(false);

                return renderCheckBox;
            }

            public Component getTableCellEditorComponent(final JTable table, final Object value,
                    final boolean isSelected, final int row, final int column) {
                editCheckBox.setForeground(table.getSelectionForeground());
                editCheckBox.setBackground(table.getSelectionBackground());
                if (value != null)
                    if (value.toString().equals("On") || Boolean.getBoolean(value.toString())
                            || value == Boolean.TRUE) {
                        currentValue = "Off";
                        editCheckBox.setSelected(false);
                    } else {
                        currentValue = "On";
                        editCheckBox.setSelected(true);
                    }

                return editCheckBox;
            }

            public Object getCellEditorValue() {
                return currentValue;
            }

            public void actionPerformed(final ActionEvent e) {
                fireEditingStopped();
            }
        }

        // Publisher Panel
        final JPanel publishPanel = new JPanel(new BorderLayout());
        publishPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Publishers"),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));

        // setup the table so that it is sortable
        final DefaultTableModel t = new DefaultTableModel(PublisherTableValues.data, PublisherTableValues.columnNames);
        publisherModel = new TableSorter(t);
        publisherTable = new JTable(publisherModel) {
            private static final long serialVersionUID = -3455426942142049897L;

            @Override
            public boolean isCellEditable(final int rowIndex, final int vColIndex) {
                if (vColIndex == PublisherTableValues.LOOP)
                    return true;
                else
                    return false;
            }
        };
        publisherModel.setTableHeader(publisherTable.getTableHeader());

        final TableColumnModel tcm = publisherTable.getColumnModel();
        final TableColumn tc = tcm.getColumn(5);
        tc.setCellRenderer(new FileRenderer());
        publisherTable.setDefaultRenderer(Object.class, new CellRenderer());

        new CheckBoxColumn(publisherTable, PublisherTableValues.LOOP);

        // Set the column widths in the publisher table for columns that need
        // dynamic widths
        publisherTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        final TableColumnAdjuster tcaPublisher = new TableColumnAdjuster(publisherTable);
        tcaPublisher.adjustColumn(PublisherTableValues.BROKER);
        tcaPublisher.adjustColumn(PublisherTableValues.CHANNEL);
        tcaPublisher.adjustColumn(PublisherTableValues.FILE);

        // Set the column width for the Status column
        final TableColumn pubStatusColumn = publisherTable.getColumnModel().getColumn(PublisherTableValues.STATUS);
        final int pubStatusColWidth = 59; // tcaPublisher.getColumnHeaderWidth(PublisherTableValues.STATUS)
        // + 15;
        pubStatusColumn.setMinWidth(pubStatusColWidth);
        pubStatusColumn.setMaxWidth(pubStatusColWidth);
        pubStatusColumn.setPreferredWidth(pubStatusColWidth);

        // Set the column width for the Count column
        final TableColumn pubCountColumn = publisherTable.getColumnModel().getColumn(PublisherTableValues.COUNT);
        final int pubCountColWidth = 57; // tcaPublisher.getColumnHeaderWidth(PublisherTableValues.COUNT)
        // + 15;
        pubCountColumn.setMinWidth(pubCountColWidth);
        pubCountColumn.setMaxWidth(pubCountColWidth);
        pubCountColumn.setPreferredWidth(pubCountColWidth);

        // Set the column width for the Type column
        final TableColumn pubTypeColumn = publisherTable.getColumnModel().getColumn(PublisherTableValues.TYPE);
        final int pubTypeColWidth = 52; // tcaPublisher.getColumnHeaderWidth(PublisherTableValues.TYPE)
        // + 5;
        pubTypeColumn.setMinWidth(pubTypeColWidth);
        pubTypeColumn.setMaxWidth(pubTypeColWidth);
        pubTypeColumn.setPreferredWidth(pubTypeColWidth);

        // Set the column width for the Loop column
        final TableColumn pubLoopColumn = publisherTable.getColumnModel().getColumn(PublisherTableValues.LOOP);
        final int pubLoopColWidth = 52; // tcaPublisher.getColumnHeaderWidth(PublisherTableValues.LOOP)
        // + 5;
        pubLoopColumn.setMinWidth(pubLoopColWidth);
        pubLoopColumn.setMaxWidth(pubLoopColWidth);
        pubLoopColumn.setPreferredWidth(pubLoopColWidth);

        // Set the column width for the Rate column
        final TableColumn pubRateColumn = publisherTable.getColumnModel().getColumn(PublisherTableValues.RATE);
        final int pubRateColWidth = 86; // tcaPublisher.getColumnHeaderWidth(PublisherTableValues.RATE)
        // + 50;
        pubRateColumn.setMinWidth(pubRateColWidth);
        pubRateColumn.setMaxWidth(pubRateColWidth);
        pubRateColumn.setPreferredWidth(pubRateColWidth);

        publisherTable.setPreferredScrollableViewportSize(new Dimension(800, 150));
        publishPane = new JScrollPane(publisherTable);
        publishPanel.add(publishPane, BorderLayout.CENTER);

        // Subscriber panel
        final JPanel subscriberPanel = new JPanel(new BorderLayout());
        subscriberPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Subscribers"),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));

        // Setup the table so that it is sortable
        final DefaultTableModel t2 = new DefaultTableModel(SubscriberTableValues.data,
                SubscriberTableValues.columnNames);
        subscriberModel = new TableSorter(t2);

        subscriberTable = new JTable(subscriberModel) {
            private static final long serialVersionUID = -5713556057649358056L;

            @Override
            public boolean isCellEditable(final int row, final int col) {
                if (col == SubscriberTableValues.RECORD)
                    return true;
                else
                    return false;
            }
        };
        subscriberModel.setTableHeader(subscriberTable.getTableHeader());

        final TableColumnModel tcm2 = subscriberTable.getColumnModel();
        final TableColumn tc2 = tcm2.getColumn(5);
        tc2.setCellRenderer(new FileRenderer());
        subscriberTable.setDefaultRenderer(Object.class, new CellRenderer());

        new CheckBoxColumn(subscriberTable, SubscriberTableValues.RECORD);

        // Set the column widths in the subscriber table for columns that need
        // dynamic widths
        subscriberTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        final TableColumnAdjuster tcaSubscriber = new TableColumnAdjuster(subscriberTable);
        tcaSubscriber.adjustColumn(SubscriberTableValues.BROKER);
        tcaSubscriber.adjustColumn(SubscriberTableValues.CHANNEL);
        tcaSubscriber.adjustColumn(SubscriberTableValues.FILE);

        // Set the column widths for the Status column
        final TableColumn subStatusColumn = subscriberTable.getColumnModel().getColumn(SubscriberTableValues.STATUS);
        final int subStatusColWidth = 59; // tcaSubscriber.getColumnHeaderWidth(SubscriberTableValues.STATUS)
        // + 10;
        subStatusColumn.setMinWidth(subStatusColWidth);
        subStatusColumn.setMaxWidth(subStatusColWidth);
        subStatusColumn.setPreferredWidth(subStatusColWidth);

        // Set the column widths for the Count column
        final TableColumn subCountColumn = subscriberTable.getColumnModel().getColumn(SubscriberTableValues.COUNT);
        final int subCountColWidth = 57; // tcaSubscriber.getColumnHeaderWidth(SubscriberTableValues.COUNT)
        // + 15;
        subCountColumn.setMinWidth(subCountColWidth);
        subCountColumn.setMaxWidth(subCountColWidth);
        subCountColumn.setPreferredWidth(subCountColWidth);

        // Set the column widths for the Type column
        final TableColumn subTypeColumn = subscriberTable.getColumnModel().getColumn(SubscriberTableValues.TYPE);
        final int subTypeColWidth = 52; // tcaSubscriber.getColumnHeaderWidth(SubscriberTableValues.TYPE)
        // + 5;
        subTypeColumn.setMinWidth(subTypeColWidth);
        subTypeColumn.setMaxWidth(subTypeColWidth);
        subTypeColumn.setPreferredWidth(subTypeColWidth);

        // Set the column widths for the Record column
        final TableColumn subRecordColumn = subscriberTable.getColumnModel().getColumn(SubscriberTableValues.RECORD);
        final int subRecordColWidth = 62; // tcaSubscriber.getColumnHeaderWidth(SubscriberTableValues.RECORD)
        // + 10;
        subRecordColumn.setMinWidth(subRecordColWidth);
        subRecordColumn.setMaxWidth(subRecordColWidth);
        subRecordColumn.setPreferredWidth(subRecordColWidth);

        subscriberTable.setPreferredScrollableViewportSize(new Dimension(800, 150));
        subscriberPane = new JScrollPane(subscriberTable);
        subscriberPanel.add(subscriberPane, BorderLayout.CENTER);

        // Center Panel
        centerPanel = new DroppablePanel(new BorderLayout());

        /*
         * Box box = Box.createVerticalBox(); box.add(publishPanel); box.add(Box.createVerticalStrut(6));
         * box.add(subscriberPanel); box.add(Box.createVerticalGlue()); centerPanel.add(box);
         */

        final JSplitPane pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        pane.setBorder(null);
        pane.setTopComponent(publishPanel);
        pane.setBottomComponent(subscriberPanel);
        pane.setResizeWeight(0.5);
        centerPanel.add(pane);

        centerPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));

    }

    protected void createMenuBar() {
        final JMenuBar menuBar = new JMenuBar();
        // Project
        final JMenu projectMenu = Util.createMenu("Project", KeyEvent.VK_E);
        final JMenuItem newProjectMenuItem = Util.createMenuItem("newButton", KeyEvent.VK_N, ActionEvent.CTRL_MASK);
        final JMenuItem openProjectMenuItem = Util.createMenuItem("openButton", KeyEvent.VK_O, ActionEvent.CTRL_MASK);
        final JMenuItem closeProjectMenuItem = Util.createMenuItem("closeButton", KeyEvent.VK_F4,
                ActionEvent.CTRL_MASK);
        final JMenuItem saveProjectMenuItem = Util.createMenuItem("saveButton", KeyEvent.VK_S, ActionEvent.CTRL_MASK);
        final JMenuItem saveAsProjectMenuItem = Util.createMenuItem("saveAsButton", KeyEvent.VK_S,
                ActionEvent.CTRL_MASK + ActionEvent.SHIFT_MASK);
        final JMenuItem exitMenuItem = Util.createMenuItem("exitButton", KeyEvent.VK_X, ActionEvent.CTRL_MASK);

        projectMenu.add(newProjectMenuItem);
        projectMenu.add(openProjectMenuItem);
        projectMenu.addSeparator();
        projectMenu.add(closeProjectMenuItem);
        projectMenu.addSeparator();
        projectMenu.add(saveProjectMenuItem);
        projectMenu.add(saveAsProjectMenuItem);
        projectMenu.addSeparator();
        projectMenu.add(exitMenuItem);

        // Action
        final JMenu actionMenu = Util.createMenu("Action", KeyEvent.VK_A);
        final JMenuItem publisherMenuItem = Util.createMenuItem("publisherButton", KeyEvent.VK_P, ActionEvent.ALT_MASK);
        final JMenuItem subscriberMenuItem = Util.createMenuItem("subscriberButton", KeyEvent.VK_S,
                ActionEvent.ALT_MASK);
        final JMenuItem deleteMenuItem = Util.createMenuItem("deleteButton", KeyEvent.VK_DELETE, 0);
        final JMenuItem copyMenuItem = Util.createMenuItem("copyButton", KeyEvent.VK_C, KeyEvent.CTRL_MASK);
        final JMenuItem startMenuItem = Util.createMenuItem("startButton", KeyEvent.VK_F5, 0);
        final JMenuItem pauseMenuItem = Util.createMenuItem("pauseButton", KeyEvent.VK_F6, 0);
        final JMenuItem stopMenuItem = Util.createMenuItem("stopButton", KeyEvent.VK_F7, 0);
        final JMenuItem resetMenuItem = Util.createMenuItem("resetButton", KeyEvent.VK_F8, 0);
        final JMenuItem selectAllMenuItem = Util.createMenuItem("selectAllButton", KeyEvent.VK_A, KeyEvent.CTRL_MASK);
        final JMenuItem clearAllMenuItem = Util.createMenuItem("clearAllButton", KeyEvent.VK_Q, KeyEvent.CTRL_MASK);

        actionMenu.add(publisherMenuItem);
        actionMenu.add(subscriberMenuItem);
        actionMenu.add(copyMenuItem);
        actionMenu.add(deleteMenuItem);
        actionMenu.addSeparator();
        actionMenu.add(startMenuItem);
        actionMenu.add(pauseMenuItem);
        actionMenu.add(stopMenuItem);
        actionMenu.add(resetMenuItem);
        actionMenu.addSeparator();
        actionMenu.add(selectAllMenuItem);
        actionMenu.add(clearAllMenuItem);

        // Option
        final JMenu optionMenu = Util.createMenu("Options", KeyEvent.VK_O);
        final JMenuItem viewerMenuItem = Util.createMenuItem("viewerButton", KeyEvent.VK_V, ActionEvent.CTRL_MASK);
        final JMenuItem injectorMenuItem = Util.createMenuItem("injectorButton", KeyEvent.VK_I, ActionEvent.CTRL_MASK);
        autoSaveMenuItem = new JCheckBoxMenuItem("Auto-Save on Close");
        iconLabelMenuItem = new JCheckBoxMenuItem("Show Icons Only");

        optionMenu.add(viewerMenuItem);
        optionMenu.add(injectorMenuItem);
        optionMenu.addSeparator();
        optionMenu.add(autoSaveMenuItem);
        optionMenu.add(iconLabelMenuItem);

        // Help
        final JMenu helpMenu = Util.createMenu("Help", KeyEvent.VK_H);
        // JMenuItem helpMenuItem = Util.createMenuItem("helpButton", 0, 0);
        final JMenuItem aboutMenuItem = Util.createMenuItem("aboutButton", 0, 0);

        // helpMenu.add(helpMenuItem);
        helpMenu.add(aboutMenuItem);

        // Add menu bars
        menuBar.add(projectMenu);
        menuBar.add(actionMenu);
        menuBar.add(optionMenu);
        menuBar.add(helpMenu);
        this.setJMenuBar(menuBar);

        // Toolbars ------------
        // Project Buttons
        newButton = Util.createButton("newButton");
        openButton = Util.createButton("openButton");
        saveButton = Util.createButton("saveButton");

        // Edit Buttons
        publisherButton = Util.createButton("publisherButton");
        subscriberButton = Util.createButton("subscriberButton");
        deleteButton = Util.createButton("deleteButton");
        copyButton = Util.createButton("copyButton");

        // Action Buttons
        startButton = Util.createButton("startButton");
        pauseButton = Util.createButton("pauseButton");
        stopButton = Util.createButton("stopButton");
        resetButton = Util.createButton("resetButton");
        selectAllButton = Util.createButton("selectAllButton");
        clearAllButton = Util.createButton("clearAllButton");

        // Toolbar
        toolBar = new JToolBar();
        toolBar.setFloatable(false);
        // toolBar.add(Box.createHorizontalStrut(10));
        toolBar.add(newButton);
        toolBar.add(openButton);
        toolBar.add(saveButton);
        toolBar.addSeparator();
        toolBar.add(publisherButton);
        toolBar.add(subscriberButton);
        toolBar.add(copyButton);
        toolBar.add(deleteButton);
        toolBar.addSeparator();
        toolBar.add(startButton);
        toolBar.add(pauseButton);
        toolBar.add(stopButton);
        toolBar.add(resetButton);
        toolBar.addSeparator();
        toolBar.add(selectAllButton);
        toolBar.add(clearAllButton);

        // Listeners -----------
        // Exit Listener
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent event) {
                exit();
            }
        });

        final NewProjectListener newListener = new NewProjectListener();
        final OpenProjectListener openListener = new OpenProjectListener();
        final CloseProjectListener closeListener = new CloseProjectListener();
        final SaveProjectListener saveListener = new SaveProjectListener();
        final SaveAsProjectListener saveAsListener = new SaveAsProjectListener();
        final PublisherListener publisherListener = new PublisherListener();
        final SubscriberListener subscriberListener = new SubscriberListener();
        final AddPublisherListener addPublisherListener = new AddPublisherListener();
        final AddSubscriberListener addSubscriberListener = new AddSubscriberListener();
        final StartProjectListener startProjectListener = new StartProjectListener();
        final StopProjectListener stopProjectListener = new StopProjectListener();
        final PauseProjectListener pauseProjectListener = new PauseProjectListener();
        final DeleteProjectListener deleteProjectListener = new DeleteProjectListener();
        final CopyProjectListener copyProjectListener = new CopyProjectListener();
        final SelectAllListener selectAllListener = new SelectAllListener();
        final ClearAllListener clearAllListener = new ClearAllListener();
        final ResetListener resetListener = new ResetListener();

        newProjectMenuItem.addActionListener(newListener);
        newButton.addActionListener(newListener);

        openProjectMenuItem.addActionListener(openListener);
        openButton.addActionListener(openListener);

        closeProjectMenuItem.addActionListener(closeListener);

        saveProjectMenuItem.addActionListener(saveListener);
        saveButton.addActionListener(saveListener);

        saveAsProjectMenuItem.addActionListener(saveAsListener);

        exitMenuItem.addActionListener(new ExitListener());

        // Publisher Table
        publishPane.addMouseListener(publisherListener);
        publisherTable.addMouseListener(publisherListener);

        // Subscriber Table
        subscriberPane.addMouseListener(subscriberListener);
        subscriberTable.addMouseListener(subscriberListener);

        // Add Publisher
        publisherButton.addActionListener(addPublisherListener);
        publisherMenuItem.addActionListener(addPublisherListener);

        // Add Subscriber
        subscriberButton.addActionListener(addSubscriberListener);
        subscriberMenuItem.addActionListener(addSubscriberListener);

        // Start
        startButton.addActionListener(startProjectListener);
        startMenuItem.addActionListener(startProjectListener);

        // Stop
        stopButton.addActionListener(stopProjectListener);
        stopMenuItem.addActionListener(stopProjectListener);

        // Pause
        pauseButton.addActionListener(pauseProjectListener);
        pauseMenuItem.addActionListener(pauseProjectListener);

        // Delete
        deleteButton.addActionListener(deleteProjectListener);
        deleteMenuItem.addActionListener(deleteProjectListener);

        // Copy
        copyButton.addActionListener(copyProjectListener);
        copyMenuItem.addActionListener(copyProjectListener);

        // Select All
        selectAllButton.addActionListener(selectAllListener);
        selectAllMenuItem.addActionListener(selectAllListener);

        // Clear All
        clearAllButton.addActionListener(clearAllListener);
        clearAllMenuItem.addActionListener(clearAllListener);

        // Reset All
        resetButton.addActionListener(resetListener);
        resetMenuItem.addActionListener(resetListener);

        // Publisher pop-up listeners
        pPublisherAddMenuItem.addActionListener(addPublisherListener);
        pPublisherEditMenuItem.addActionListener(new PublisherEditListener());
        pPublisherStartMenuItem.addActionListener(new PublisherStartListener());
        pPublisherPauseMenuItem.addActionListener(new PublisherPauseListener());
        pPublisherStopMenuItem.addActionListener(new PublisherStopListener());
        pPublisherResetMenuItem.addActionListener(new PublisherResetListener());
        pPublisherCopyMenuItem.addActionListener(new PublisherCopyListener());
        pPublisherDeleteMenuItem.addActionListener(new PublisherDeleteListener());
        pPublisherViewMenuItem.addActionListener(new PublisherViewerListener());
        pPublisherInjectorMenuItem.addActionListener(new PublisherInjectorListener());

        // Subscriber pop-up
        pSubscriberAddMenuItem.addActionListener(addSubscriberListener);
        pSubscriberEditMenuItem.addActionListener(new SubscriberEditListener());
        pSubscriberStartMenuItem.addActionListener(new SubscriberStartListener());
        pSubscriberPauseMenuItem.addActionListener(new SubscriberPauseListener());
        pSubscriberStopMenuItem.addActionListener(new SubscriberStopListener());
        pSubscriberResetMenuItem.addActionListener(new SubscriberResetListener());
        pSubscriberCopyMenuItem.addActionListener(new SubscriberCopyListener());
        pSubscriberDeleteMenuItem.addActionListener(new SubscriberDeleteListener());
        pSubscriberViewMenuItem.addActionListener(new SubscriberViewerListener());

        // Options
        injectorMenuItem.addActionListener(new InjectorOptionListener());
        viewerMenuItem.addActionListener(new ViewerOptionListener());
        iconLabelMenuItem.addActionListener(new IconLabelListener());

        // About
        aboutMenuItem.addActionListener(new AboutListener(this));
    }

    protected void createPopUpMenus() {
        // Publisher Pop-up menu
        publisherPopupMenu = new JPopupMenu();
        pPublisherAddMenuItem = Util.createPopUpMenuItem("addPublisherButton");
        pPublisherEditMenuItem = Util.createPopUpMenuItem("editPublisherButton");
        pPublisherCopyMenuItem = Util.createPopUpMenuItem("copyPublisherButton");
        pPublisherDeleteMenuItem = Util.createPopUpMenuItem("deletePublisherButton");
        pPublisherStartMenuItem = Util.createPopUpMenuItem("startPublisherButton");
        pPublisherPauseMenuItem = Util.createPopUpMenuItem("pausePublisherButton");
        pPublisherStopMenuItem = Util.createPopUpMenuItem("stopPublisherButton");
        pPublisherResetMenuItem = Util.createPopUpMenuItem("resetPublisherButton");
        pPublisherViewMenuItem = Util.createPopUpMenuItem("viewPublisherButton");
        pPublisherInjectorMenuItem = Util.createPopUpMenuItem("openInjector");

        publisherPopupMenu.add(pPublisherAddMenuItem);
        publisherPopupMenu.add(pPublisherEditMenuItem);
        publisherPopupMenu.add(pPublisherViewMenuItem);
        publisherPopupMenu.add(pPublisherInjectorMenuItem);
        publisherPopupMenu.addSeparator();
        publisherPopupMenu.add(pPublisherStartMenuItem);
        publisherPopupMenu.add(pPublisherPauseMenuItem);
        publisherPopupMenu.add(pPublisherStopMenuItem);
        publisherPopupMenu.add(pPublisherResetMenuItem);
        publisherPopupMenu.addSeparator();
        publisherPopupMenu.add(pPublisherCopyMenuItem);
        publisherPopupMenu.add(pPublisherDeleteMenuItem);
        publisherTable.add(publisherPopupMenu);

        // Subscriber Pop-up menu
        subscriberPopupMenu = new JPopupMenu();
        pSubscriberAddMenuItem = Util.createPopUpMenuItem("addSubscriberButton");
        pSubscriberEditMenuItem = Util.createPopUpMenuItem("editSubscriberButton");
        pSubscriberCopyMenuItem = Util.createPopUpMenuItem("copySubscriber");
        pSubscriberDeleteMenuItem = Util.createPopUpMenuItem("deleteSubscriber");
        pSubscriberStartMenuItem = Util.createPopUpMenuItem("startSubscriberButton");
        pSubscriberPauseMenuItem = Util.createPopUpMenuItem("pauseSubscriberButton");
        pSubscriberStopMenuItem = Util.createPopUpMenuItem("stopSubscriberButton");
        pSubscriberResetMenuItem = Util.createPopUpMenuItem("resetSubscriberButton");
        pSubscriberViewMenuItem = Util.createPopUpMenuItem("viewSubscriberButton");

        subscriberPopupMenu.add(pSubscriberAddMenuItem);
        subscriberPopupMenu.add(pSubscriberEditMenuItem);
        subscriberPopupMenu.add(pSubscriberViewMenuItem);
        subscriberPopupMenu.addSeparator();
        subscriberPopupMenu.add(pSubscriberStartMenuItem);
        subscriberPopupMenu.add(pSubscriberPauseMenuItem);
        subscriberPopupMenu.add(pSubscriberStopMenuItem);
        subscriberPopupMenu.add(pSubscriberResetMenuItem);
        subscriberPopupMenu.addSeparator();
        subscriberPopupMenu.add(pSubscriberCopyMenuItem);
        subscriberPopupMenu.add(pSubscriberDeleteMenuItem);
        subscriberTable.add(subscriberPopupMenu);
    }

    protected void createFileChooser() {
        // FileChooser
        fileChooser = new JFileChooser();
        final ProjectFilter filter = new ProjectFilter();
        fileChooser.setFileFilter(filter);
    }

    protected void createStatusBar() {
        // Status bar
        statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createLoweredBevelBorder());
        statusLabel = new JLabel("   ");
        // statusLabel.setFont(new Font("Helvetica", Font.PLAIN, 13));
        statusBar.add(statusLabel, BorderLayout.WEST);
    }

    public Map<String, MessagingUtils> getBrokerMap() {
        return CONNECTED_BROKERS;
    }

    class IconLabelListener implements ActionListener {
        public void actionPerformed(final ActionEvent e) {
            showIconsOnly(((JCheckBoxMenuItem) e.getSource()).isSelected());
        }
    }

    class NewProjectListener implements ActionListener {
        public void actionPerformed(final ActionEvent e) {

            // Check if currently opened project needs saving
            final int result = checkSaveStatus();
            if (result == JOptionPane.CANCEL_OPTION) {
                return;
            }

            // Get the new filename
            fileChooser.setDialogTitle("New Project...");
            final int returnVal = fileChooser.showOpenDialog(DataRecorder.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                clearProject();
                setProjectFileFromFileBrowser();
                setSaveNeeded(false);
            }
        }
    }

    class OpenProjectListener implements ActionListener {
        public void actionPerformed(final ActionEvent e) {
            // Check if currently opened project needs saving
            final int result = checkSaveStatus();
            if (result == JOptionPane.CANCEL_OPTION) {
                return;
            }

            // Get the new filename
            fileChooser.setDialogTitle("Open Project...");
            final int returnVal = fileChooser.showOpenDialog(DataRecorder.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                clearProject();
                setProjectFileFromFileBrowser();
                openProject();
            }
        }
    }

    class CloseProjectListener implements ActionListener {
        public void actionPerformed(final ActionEvent e) {
            closeProject();
        }
    }

    class SaveProjectListener implements ActionListener {
        public void actionPerformed(final ActionEvent e) {
            saveProject();
        }
    }

    class SaveAsProjectListener implements ActionListener {
        public void actionPerformed(final ActionEvent e) {
            saveAsProject();
        }
    }

    class ClearAllListener implements ActionListener {
        public void actionPerformed(final ActionEvent e) {
            clearAll();
        }
    }

    class SelectAllListener implements ActionListener {
        public void actionPerformed(final ActionEvent e) {
            selectAll();
        }
    }

    class ResetListener implements ActionListener {
        public void actionPerformed(final ActionEvent e) {
            resetSelectedPublishers();
            resetSelectedSubscribers();
        }
    }

    class ExitListener implements ActionListener {
        public void actionPerformed(final ActionEvent e) {
            exit();
        }
    }

    // AddPublisherListener
    class AddPublisherListener implements ActionListener {
        public void actionPerformed(final ActionEvent e) {
            if (publisherPropertiesEditor == null) {
                publisherPropertiesEditor = new PublisherEditor(DataRecorder.this);
            }

            publisherPropertiesEditor.setVisible(true, PublisherEditor.ADD_MODE, -1);

            if (publisherPropertiesEditor.isAction()) {
                final String broker = publisherPropertiesEditor.getBroker();
                final String channel = publisherPropertiesEditor.getChannel();
                final FileObject file = publisherPropertiesEditor.getFile();
                final String loop = publisherPropertiesEditor.getLoop();
                final RateObject rate = publisherPropertiesEditor.getRate();
                String type = "T";
                if (publisherPropertiesEditor.isQueue()) {
                    type = "Q";
                }
                addPublisher(broker, channel, type, file, loop, rate);
                setSaveNeeded(true);
            }
        }
    }

    class AddSubscriberListener implements ActionListener {
        public void actionPerformed(final ActionEvent e) {
            if (subscriberPropertiesEditor == null) {
                subscriberPropertiesEditor = new SubscriberEditor(DataRecorder.this);
            }

            subscriberPropertiesEditor.setVisible(true, SubscriberEditor.ADD_MODE, -1);

            if (subscriberPropertiesEditor.isAction()) {
                final String broker = subscriberPropertiesEditor.getBroker();
                final String channel = subscriberPropertiesEditor.getChannel();
                final FileObject file = subscriberPropertiesEditor.getFile();
                final String record = subscriberPropertiesEditor.getRecord();
                String type = "T";
                if (subscriberPropertiesEditor.isQueue()) {
                    type = "Q";
                }
                addSubscriber(broker, channel, type, file, record);
                setSaveNeeded(true);
            }
        }
    }

    class StartProjectListener implements ActionListener {
        @SuppressWarnings("unchecked")
        public void actionPerformed(final ActionEvent e) {
            final int[] list1 = subscriberTable.getSelectedRows();
            for (int i = 0; i < list1.length; i++) {
                final Vector<SubscriberThread> subscriberVector = (Vector<SubscriberThread>) subscriberModel
                        .getDataVector().elementAt(list1[i]);
                startSubscriber(subscriberVector);
            }

            final int[] list2 = publisherTable.getSelectedRows();
            for (int i = 0; i < list2.length; i++) {
                final Vector<PublisherThread> v = (Vector<PublisherThread>) publisherModel.getDataVector()
                        .elementAt(list2[i]);
                startPublisher(v);
            }
        }
    }

    class StopProjectListener implements ActionListener {
        @SuppressWarnings("unchecked")
        public void actionPerformed(final ActionEvent e) {
            final int[] list1 = publisherTable.getSelectedRows();
            for (int i = 0; i < list1.length; i++) {
                final Vector<PublisherThread> v = (Vector<PublisherThread>) publisherModel.getDataVector()
                        .elementAt(list1[i]);
                stopPublisher(v);
            }

            final int[] list2 = subscriberTable.getSelectedRows();
            for (int i = 0; i < list2.length; i++) {
                final Vector<SubscriberThread> v = (Vector<SubscriberThread>) subscriberModel.getDataVector()
                        .elementAt(list2[i]);
                stopSubscriber(v);
            }
        }
    }

    class PauseProjectListener implements ActionListener {
        @SuppressWarnings("unchecked")
        public void actionPerformed(final ActionEvent e) {

            final int[] list1 = publisherTable.getSelectedRows();
            for (int i = 0; i < list1.length; i++) {
                final Vector<PublisherThread> v = (Vector<PublisherThread>) publisherModel.getDataVector()
                        .elementAt(list1[i]);
                pausePublisher(v);
            }

            final int[] list2 = subscriberTable.getSelectedRows();
            for (int i = 0; i < list2.length; i++) {
                final Vector<SubscriberThread> v = (Vector<SubscriberThread>) subscriberModel.getDataVector()
                        .elementAt(list2[i]);
                pauseSubscriber(v);
            }
        }
    }

    class DeleteProjectListener implements ActionListener {
        public void actionPerformed(final ActionEvent e) {

            // Check if there is anything to delete
            int[] pubs = publisherTable.getSelectedRows();
            int[] subs = subscriberTable.getSelectedRows();
            final int totalSelections = pubs.length + subs.length;
            String warning = null;
            if (totalSelections == 0)
                return;
            else if (totalSelections == 1)
                warning = DELETE_WARNING;
            else
                warning = DELETE_WARNING_MANY;

            // Confirm
            final int result = JOptionPane.showConfirmDialog(DataRecorder.this, warning, "Warning",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (result == JOptionPane.YES_OPTION) {
                // Publishers
                while (pubs.length > 0) {
                    deletePublisher(pubs[pubs.length - 1]);
                    pubs = publisherTable.getSelectedRows();
                }
                // Subscribers
                while (subs.length > 0) {
                    deleteSubscriber(subs[subs.length - 1]);
                    subs = subscriberTable.getSelectedRows();
                }
                // Status
                if (totalSelections > 0) {
                    // statusLabel.setText(" Selections Deleted");
                    setSaveNeeded(true);
                }
            }
        }
    }

    class PublisherEditListener implements ActionListener {
        public void actionPerformed(final ActionEvent e) {
            editPublisher(selectedPublisherRow);
        }
    }

    class SubscriberEditListener implements ActionListener {
        public void actionPerformed(final ActionEvent e) {
            editSubscriber(selectedSubscriberRow);
        }
    }

    class PublisherStartListener implements ActionListener {
        @SuppressWarnings("unchecked")
        public void actionPerformed(final ActionEvent e) {
            final Vector<PublisherThread> v = (Vector<PublisherThread>) publisherModel.getDataVector()
                    .elementAt(selectedPublisherRow);
            startPublisher(v);
        }
    }

    class SubscriberStartListener implements ActionListener {
        @SuppressWarnings("unchecked")
        public void actionPerformed(final ActionEvent e) {
            final Vector<SubscriberThread> subscriberVector = (Vector<SubscriberThread>) subscriberModel.getDataVector()
                    .elementAt(selectedSubscriberRow);
            startSubscriber(subscriberVector);
        }
    }

    class PublisherPauseListener implements ActionListener {
        @SuppressWarnings("unchecked")
        public void actionPerformed(final ActionEvent e) {
            final Vector<PublisherThread> v = (Vector<PublisherThread>) publisherModel.getDataVector()
                    .elementAt(selectedPublisherRow);
            pausePublisher(v);
        }
    }

    class SubscriberPauseListener implements ActionListener {
        @SuppressWarnings("unchecked")
        public void actionPerformed(final ActionEvent e) {
            final Vector<SubscriberThread> v = (Vector<SubscriberThread>) subscriberModel.getDataVector()
                    .elementAt(selectedSubscriberRow);
            pauseSubscriber(v);
        }
    }

    class PublisherStopListener implements ActionListener {
        @SuppressWarnings("unchecked")
        public void actionPerformed(final ActionEvent e) {
            final Vector<PublisherThread> v = (Vector<PublisherThread>) publisherModel.getDataVector()
                    .elementAt(selectedPublisherRow);
            stopPublisher(v);
        }
    }

    class SubscriberStopListener implements ActionListener {
        @SuppressWarnings("unchecked")
        public void actionPerformed(final ActionEvent e) {
            final Vector<SubscriberThread> v = (Vector<SubscriberThread>) subscriberModel.getDataVector()
                    .elementAt(selectedSubscriberRow);
            stopSubscriber(v);
        }
    }

    class PublisherResetListener implements ActionListener {
        public void actionPerformed(final ActionEvent e) {
            resetSelectedPublishers();
        }
    }

    class SubscriberResetListener implements ActionListener {
        public void actionPerformed(final ActionEvent e) {
            resetSelectedSubscribers();
        }
    }

    class PublisherDeleteListener implements ActionListener {
        public void actionPerformed(final ActionEvent e) {

            // Confirm
            final int result = JOptionPane.showConfirmDialog(DataRecorder.this, DELETE_WARNING, "Warning",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (result == JOptionPane.YES_OPTION) {
                deletePublisher(selectedPublisherRow);
                setSaveNeeded(true);
            }
        }
    }

    class CopyProjectListener implements ActionListener {
        @SuppressWarnings("unchecked")
        public void actionPerformed(final ActionEvent e) {
            final int[] list = publisherTable.getSelectedRows();
            for (int i = 0; i < list.length; i++) {
                final Vector<Object> v = (Vector<Object>) publisherModel.getDataVector().elementAt(list[i]);
                addPublisher(((MessagingUtils) v.get(PublisherTableValues.BROKER)).getBrokerFullURL(),
                        (String) v.get(PublisherTableValues.CHANNEL), v.get(PublisherTableValues.TYPE),
                        (FileObject) v.get(PublisherTableValues.FILE), (String) v.get(PublisherTableValues.LOOP),
                        (RateObject) v.get(PublisherTableValues.RATE));
                setSaveNeeded(true);
            }

            final int[] list2 = subscriberTable.getSelectedRows();
            for (int i = 0; i < list2.length; i++) {
                final Vector<Object> v = (Vector<Object>) subscriberModel.getDataVector().elementAt(list2[i]);
                addSubscriber(((MessagingUtils) v.get(SubscriberTableValues.BROKER)).getBrokerFullURL(),
                        (String) v.get(SubscriberTableValues.CHANNEL), v.get(PublisherTableValues.TYPE),
                        (FileObject) v.get(SubscriberTableValues.FILE), (String) v.get(SubscriberTableValues.RECORD));
                setSaveNeeded(true);
            }
        }
    }

    class PublisherCopyListener implements ActionListener {
        @SuppressWarnings("unchecked")
        public void actionPerformed(final ActionEvent e) {
            final int[] list = publisherTable.getSelectedRows();
            for (int i = 0; i < list.length; i++) {
                final Vector<Object> v = (Vector<Object>) publisherModel.getDataVector().elementAt(list[i]);
                addPublisher(((MessagingUtils) v.get(PublisherTableValues.BROKER)).getBrokerFullURL(),
                        (String) v.get(PublisherTableValues.CHANNEL), v.get(PublisherTableValues.TYPE),
                        (FileObject) v.get(PublisherTableValues.FILE), (String) v.get(PublisherTableValues.LOOP),
                        (RateObject) v.get(PublisherTableValues.RATE));
                setSaveNeeded(true);
            }
        }
    }

    class SubscriberCopyListener implements ActionListener {
        @SuppressWarnings("unchecked")
        public void actionPerformed(final ActionEvent e) {
            final int[] list = subscriberTable.getSelectedRows();
            for (int i = 0; i < list.length; i++) {
                final Vector<Object> v = (Vector<Object>) subscriberModel.getDataVector().elementAt(list[i]);
                addSubscriber(((MessagingUtils) v.get(SubscriberTableValues.BROKER)).getBrokerFullURL(),
                        (String) v.get(SubscriberTableValues.CHANNEL), v.get(PublisherTableValues.TYPE),
                        (FileObject) v.get(SubscriberTableValues.FILE), (String) v.get(SubscriberTableValues.RECORD));
                setSaveNeeded(true);
            }
        }
    }

    class SubscriberDeleteListener implements ActionListener {
        public void actionPerformed(final ActionEvent e) {

            // Confirm
            final int result = JOptionPane.showConfirmDialog(DataRecorder.this, DELETE_WARNING, "Warning",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (result == JOptionPane.YES_OPTION) {
                deleteSubscriber(selectedSubscriberRow);
                setSaveNeeded(true);
            }
        }
    }

    class PublisherViewerListener implements ActionListener {
        @SuppressWarnings("unchecked")
        public void actionPerformed(final ActionEvent e) {
            final Vector<ChannelViewer> v = (Vector<ChannelViewer>) publisherModel.getDataVector()
                    .elementAt(selectedPublisherRow);
            final ChannelViewer cv = v.get(PublisherTableValues.CHANNEL_VIEWER);
            cv.setVisible(true);
        }
    }

    class SubscriberViewerListener implements ActionListener {
        @SuppressWarnings("unchecked")
        public void actionPerformed(final ActionEvent e) {
            final Vector<ChannelViewer> v = (Vector<ChannelViewer>) subscriberModel.getDataVector()
                    .elementAt(selectedSubscriberRow);
            final ChannelViewer cv = v.get(SubscriberTableValues.CHANNEL_VIEWER);
            cv.show();
        }
    }

    class PublisherInjectorListener implements ActionListener {
        @SuppressWarnings("unchecked")
        public void actionPerformed(final ActionEvent e) {
            final Vector<ChannelInjector> v = (Vector<ChannelInjector>) publisherModel.getDataVector()
                    .elementAt(selectedPublisherRow);
            final ChannelInjector iv = v.get(PublisherTableValues.CHANNEL_INJECTOR);
            iv.show();
        }
    }

    class InjectorOptionListener implements ActionListener {
        public void actionPerformed(final ActionEvent e) {
            if (injectorPropertyEditor == null) {
                injectorPropertyEditor = new PropertyEditor(DataRecorder.this, Injector.class, ChannelInjector.FILTER);
            }
            injectorPropertyEditor.show();
        }
    }

    class ViewerOptionListener implements ActionListener {
        public void actionPerformed(final ActionEvent e) {
            if (viewerPropertyEditor == null) {
                viewerPropertyEditor = new PropertyEditor(DataRecorder.this, Viewer.class, ChannelViewer.FILTER);
            }
            viewerPropertyEditor.show();
        }
    }

    // AboutListener
    class AboutListener implements ActionListener {
        // DataRecorder dataRecorder;

        public AboutListener(final DataRecorder dataRecorder) {
            // this.dataRecorder = dataRecorder;
        }

        public void actionPerformed(final ActionEvent e) {

            final ManifestReader mfer = new ManifestReader(com.datarecorder.DataRecorder.class);

            final AboutDialog ad = new AboutDialog(DataRecorder.this, " About Data Recorder",
                    "<html><b>Data Recorder</b>" + "<br>Version " + mfer.getCiBuildVersion() + "<br>Build #: "
                            + mfer.getCiBuildVersion() + "<br>Build id: " + mfer.getCiBuildId() + "<br>SVN Revision : "
                            + mfer.getCiBuildCmRevision() + "<br>" ,
                    true);
            ad.pack();
            ad.show();

        }
    }

    // PublisherListener
    class PublisherListener extends MouseAdapter {
        @Override
        public void mouseClicked(final MouseEvent e) {
            processMouseEvent(e);
        }

        @Override
        public void mousePressed(final MouseEvent evt) {
            processMouseEvent(evt);
        }

        @Override
        public void mouseReleased(final MouseEvent evt) {
            processMouseEvent(evt);
        }

        @Override
        public void mouseEntered(final MouseEvent evt) {
            processMouseEvent(evt);
        }

        @Override
        public void mouseExited(final MouseEvent evt) {
            processMouseEvent(evt);
        }

        private void processMouseEvent(final MouseEvent evt) {
            if (evt.getClickCount() == 1) {
                // clear selections in publisher table by
                // clicking in white-space on either table
                final Point origin = evt.getPoint();
                final int row = publisherTable.rowAtPoint(origin);
                if (row == -1) {
                    publisherTable.clearSelection();
                }
                // clear selections in subscriber table
                // unless control is down
                if (!(evt.isControlDown())) {
                    subscriberTable.clearSelection();
                }
            }

            if (evt.getClickCount() == 2 && publisherTable.columnAtPoint(evt.getPoint()) != PublisherTableValues.LOOP) {
                final Point origin = evt.getPoint();
                selectedPublisherRow = publisherTable.rowAtPoint(origin);
                editPublisher(selectedPublisherRow);
            } else if (evt.isPopupTrigger()) {
                final Point origin = evt.getPoint();
                selectedPublisherRow = publisherTable.rowAtPoint(origin);
                switch (selectedPublisherRow) {
                case -1:
                    pPublisherAddMenuItem.setEnabled(true);
                    pPublisherEditMenuItem.setEnabled(false);
                    pPublisherViewMenuItem.setEnabled(false);
                    pPublisherInjectorMenuItem.setEnabled(false);
                    pPublisherCopyMenuItem.setEnabled(false);
                    pPublisherDeleteMenuItem.setEnabled(false);
                    pPublisherStartMenuItem.setEnabled(false);
                    pPublisherPauseMenuItem.setEnabled(false);
                    pPublisherStopMenuItem.setEnabled(false);
                    pPublisherResetMenuItem.setEnabled(false);
                    break;
                default:
                    publisherTable.clearSelection();
                    publisherTable.changeSelection(selectedPublisherRow, 0, true, false);
                    pPublisherAddMenuItem.setEnabled(true);
                    pPublisherEditMenuItem.setEnabled(true);
                    pPublisherViewMenuItem.setEnabled(true);
                    pPublisherInjectorMenuItem.setEnabled(true);
                    pPublisherCopyMenuItem.setEnabled(true);
                    pPublisherDeleteMenuItem.setEnabled(true);
                    pPublisherStartMenuItem.setEnabled(true);
                    pPublisherPauseMenuItem.setEnabled(true);
                    pPublisherStopMenuItem.setEnabled(true);
                    pPublisherResetMenuItem.setEnabled(true);
                    break;
                }
                publisherPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
            }
        }
    }

    // SubscriberListener
    class SubscriberListener extends MouseAdapter {
        @Override
        public void mouseClicked(final MouseEvent e) {
            processMouseEvent(e);
        }

        @Override
        public void mousePressed(final MouseEvent evt) {
            processMouseEvent(evt);
        }

        @Override
        public void mouseReleased(final MouseEvent evt) {
            processMouseEvent(evt);
        }

        @Override
        public void mouseEntered(final MouseEvent evt) {
            processMouseEvent(evt);
        }

        @Override
        public void mouseExited(final MouseEvent evt) {
            processMouseEvent(evt);
        }

        private void processMouseEvent(final MouseEvent evt) {
            if (evt.getClickCount() == 1) {
                // clear selections in subscriber table by
                // clicking in white-space on either table
                final Point origin = evt.getPoint();
                final int row = subscriberTable.rowAtPoint(origin);
                if (row == -1) {
                    subscriberTable.clearSelection();
                }
                // clear selections in publisher table
                // unless control is down
                if (!(evt.isControlDown())) {
                    publisherTable.clearSelection();
                }
            }
            if (evt.getClickCount() == 2
                    && subscriberTable.columnAtPoint(evt.getPoint()) != SubscriberTableValues.RECORD) {
                final Point origin = evt.getPoint();
                selectedSubscriberRow = subscriberTable.rowAtPoint(origin);
                editSubscriber(selectedSubscriberRow);
            } else if (evt.isPopupTrigger()) {
                final Point origin = evt.getPoint();
                selectedSubscriberRow = subscriberTable.rowAtPoint(origin);
                switch (selectedSubscriberRow) {
                case -1:
                    pSubscriberAddMenuItem.setEnabled(true);
                    pSubscriberEditMenuItem.setEnabled(false);
                    pSubscriberViewMenuItem.setEnabled(false);
                    pSubscriberCopyMenuItem.setEnabled(false);
                    pSubscriberDeleteMenuItem.setEnabled(false);
                    pSubscriberStartMenuItem.setEnabled(false);
                    pSubscriberPauseMenuItem.setEnabled(false);
                    pSubscriberStopMenuItem.setEnabled(false);
                    pSubscriberResetMenuItem.setEnabled(false);
                    break;
                default:
                    subscriberTable.clearSelection();
                    subscriberTable.changeSelection(selectedSubscriberRow, 0, true, false);
                    pSubscriberAddMenuItem.setEnabled(true);
                    pSubscriberEditMenuItem.setEnabled(true);
                    pSubscriberViewMenuItem.setEnabled(true);
                    pSubscriberCopyMenuItem.setEnabled(true);
                    pSubscriberDeleteMenuItem.setEnabled(true);
                    pSubscriberStartMenuItem.setEnabled(true);
                    pSubscriberPauseMenuItem.setEnabled(true);
                    pSubscriberStopMenuItem.setEnabled(true);
                    pSubscriberResetMenuItem.setEnabled(true);
                    break;
                }
                subscriberPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
            }
        }
    }

    /**
     * This method retrievs an instance of the broker of a given URL if it is already present or creates and returns an
     * instance of a new broker.
     * 
     * @param url
     *            The URL of the broker - including port. For example, localhost:61616
     * @return The instance of the broker.
     * @throws JMSException
     * @throws NamingException
     * @throws Exception
     */
    private MessagingUtils getBroker(final String url) throws JMSException, NamingException {
        if (CONNECTED_BROKERS.containsKey(url))
            return CONNECTED_BROKERS.get(url);

        // broker not found, create a new one
        MessagingUtils newBroker = null;
        if (url.equals("DUMMY_BROKER") || url.trim().equals(""))
            newBroker = new MessagingUtils();
        else
            newBroker = new MessagingUtils(url);

        CONNECTED_BROKERS.put(url, newBroker);

        return newBroker;
    }

    @SuppressWarnings("unchecked")
    private void editPublisher(final int row) {
        if (publisherPropertiesEditor == null) {
            publisherPropertiesEditor = new PublisherEditor(DataRecorder.this);
        }

        try {
            publisherPropertiesEditor.setVisible(true, PublisherEditor.EDIT_MODE, row);
            if (publisherPropertiesEditor.isAction()) {
                final Vector<Object> v = (Vector<Object>) this.publisherModel.getDataVector().elementAt(row);
                final MessagingUtils b = getBroker(publisherPropertiesEditor.getBroker());
                v.set(PublisherTableValues.BROKER, b);

                final String channel = publisherPropertiesEditor.getChannel();
                if (!preloadTopicNames.contains(channel))
                    preloadTopicNames.add(channel);
                final FileObject file = publisherPropertiesEditor.getFile();
                final String loop = publisherPropertiesEditor.getLoop();
                final RateObject rate = publisherPropertiesEditor.getRate();
                v.set(PublisherTableValues.CHANNEL, channel);

                final Boolean isQueue = Boolean.valueOf(publisherPropertiesEditor.isQueue());
                String type = "T";
                if (isQueue) {
                    type = "Q";
                }

                v.set(PublisherTableValues.TYPE, type);
                v.set(PublisherTableValues.FILE, file);
                v.set(PublisherTableValues.LOOP, loop);
                v.set(PublisherTableValues.RATE, rate);

                // this is for when pub is sleeping and you want to change the rate
                final PublisherThread pub = (PublisherThread) v.get(PublisherTableValues.PUBLISHER_THREAD);
                if (pub != null) {
                    pub.interrupt();
                }

                final ChannelPublisher pubClass = new ChannelPublisher(DataRecorder.this, v);
                final ChannelViewer cv = (ChannelViewer) v.get(PublisherTableValues.CHANNEL_VIEWER);
                cv.setTitle(VIEWING_PUB + channel);
                final ChannelInjector pi = new ChannelInjector(DataRecorder.this, pubClass);
                pi.setTitle(INJECTING + channel);
                v.set(PublisherTableValues.CHANNEL_INJECTOR, pi);
                v.set(PublisherTableValues.CHANNEL_PUBLISHER, pubClass);
                v.set(PublisherTableValues.PUBLISHER_THREAD, pub);

                setSaveNeeded(true);
                publisherTable.repaint();
            }

        } catch (final ArrayIndexOutOfBoundsException ex) {
        } catch (final Exception e) {
            JOptionPane.showMessageDialog(DataRecorder.this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            publisherTable.repaint();
        }
    }

    @SuppressWarnings("unchecked")
    private void editSubscriber(final int row) {
        if (subscriberPropertiesEditor == null) {
            subscriberPropertiesEditor = new SubscriberEditor(this);
        }

        try {
            subscriberPropertiesEditor.setVisible(true, SubscriberEditor.EDIT_MODE, row);
            if (subscriberPropertiesEditor.isAction()) {
                final Vector<Object> v = (Vector<Object>) this.subscriberModel.getDataVector().elementAt(row);
                final MessagingUtils b = getBroker(subscriberPropertiesEditor.getBroker());
                v.set(SubscriberTableValues.BROKER, b);

                final String c = subscriberPropertiesEditor.getChannel();
                if (!preloadTopicNames.contains(c))
                    preloadTopicNames.add(c);
                final FileObject f = subscriberPropertiesEditor.getFile();
                final String r = subscriberPropertiesEditor.getRecord();
                v.set(SubscriberTableValues.CHANNEL, c);
                v.set(SubscriberTableValues.FILE, f);
                v.set(SubscriberTableValues.RECORD, r);

                try {
                    v.set(SubscriberTableValues.SUBSCRIPTION, null);
                    final ChannelViewer cv = (ChannelViewer) v.get(SubscriberTableValues.CHANNEL_VIEWER);
                    cv.setTitle(VIEWING_SUB + c);
                } catch (final Exception ex) {
                    logger.error("Error", ex);
                }

                final Boolean isQueue = Boolean.valueOf(subscriberPropertiesEditor.isQueue());
                String type = "T";
                if (isQueue) {
                    type = "Q";
                }
                v.set(SubscriberTableValues.TYPE, type);

                setSaveNeeded(true);
                subscriberTable.repaint();
            }

        } catch (final ArrayIndexOutOfBoundsException ex) {
        } catch (final Exception e) {
            JOptionPane.showMessageDialog(DataRecorder.this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            publisherTable.repaint();
        }
    }

    private void startPublisher(final Vector v) {
        final String status = (String) v.get(PublisherTableValues.STATUS);
        if (status.equals(PublisherTableValues.STOPPED)) {
            final PublisherThread pub = new PublisherThread(DataRecorder.this, v);
            v.add(PublisherTableValues.PUBLISHER_THREAD, pub);
            pub.startThread();
        } else if (status.equals(PublisherTableValues.PAUSED)) {
            final PublisherThread pub = (PublisherThread) v.get(PublisherTableValues.PUBLISHER_THREAD);
            pub.startThread();
        }
    }

    private void startSubscriber(final Vector subscriberVector) {
        final String status = (String) subscriberVector.get(SubscriberTableValues.STATUS);
        if (status.equals(SubscriberTableValues.STOPPED)) {
            final SubscriberThread sub = new SubscriberThread(DataRecorder.this, subscriberVector);
            subscriberVector.add(SubscriberTableValues.SUBSCRIBER_THREAD, sub);
            sub.startThread();
        } else if (status.equals(SubscriberTableValues.PAUSED)) {
            final SubscriberThread sub = (SubscriberThread) subscriberVector
                    .get(SubscriberTableValues.SUBSCRIBER_THREAD);
            sub.startThread();
        }
    }

    private void stopPublisher(final Vector<PublisherThread> v) {
        final PublisherThread pub = v.get(PublisherTableValues.PUBLISHER_THREAD);
        if (pub != null) {
            pub.stopThread();
        }
    }

    private void stopSubscriber(final Vector<SubscriberThread> v) {
        final SubscriberThread sub = v.get(SubscriberTableValues.SUBSCRIBER_THREAD);
        if (sub != null) {
            sub.stopThread();
        }
    }

    private void pausePublisher(final Vector<PublisherThread> v) {
        final PublisherThread pub = v.get(PublisherTableValues.PUBLISHER_THREAD);
        if (pub != null) {
            pub.pauseThread();
        }
    }

    private void pauseSubscriber(final Vector<SubscriberThread> v) {
        final SubscriberThread sub = v.get(SubscriberTableValues.SUBSCRIBER_THREAD);
        if (sub != null) {
            sub.pauseThread();
        }
    }

    @SuppressWarnings("unchecked")
    private void resetSelectedPublishers() {
        try {
            final int[] list = publisherTable.getSelectedRows();
            for (int i = 0; i < list.length; i++) {
                final Vector<String> v = (Vector<String>) publisherModel.getDataVector().elementAt(list[i]);
                v.set(PublisherTableValues.COUNT, PublisherTableValues.RESET);
            }
            publisherTable.repaint();
        } catch (final ArrayIndexOutOfBoundsException ignore) {
        }
    }

    @SuppressWarnings("unchecked")
    private void resetSelectedSubscribers() {
        try {
            final int[] list = subscriberTable.getSelectedRows();
            for (int i = 0; i < list.length; i++) {
                final Vector<String> v = (Vector<String>) subscriberModel.getDataVector().elementAt(list[i]);
                v.set(SubscriberTableValues.COUNT, SubscriberTableValues.RESET);
            }
            subscriberTable.repaint();
        } catch (final ArrayIndexOutOfBoundsException ignore) {
        }
    }

    @SuppressWarnings("unchecked")
    private void deletePublisher(final int i) {

        // Publisher thread
        final Vector<Object> v = (Vector<Object>) publisherModel.getDataVector().elementAt(i);
        final PublisherThread pub = (PublisherThread) v.get(PublisherTableValues.PUBLISHER_THREAD);
        if (pub != null) {
            pub.stopThread();
        }

        // Viewer
        final ChannelViewer viewer = (ChannelViewer) v.get(PublisherTableValues.CHANNEL_VIEWER);
        viewer.dispose();

        // Injector
        final ChannelInjector injector = (ChannelInjector) v.get(PublisherTableValues.CHANNEL_INJECTOR);
        injector.dispose();

        // Update table
        publisherModel.removeRow(i);
    }

    @SuppressWarnings("unchecked")
    private void deleteSubscriber(final int i) {

        // Subscriber thread
        final Vector<Object> v = (Vector<Object>) subscriberModel.getDataVector().elementAt(i);
        final SubscriberThread sub = (SubscriberThread) v.get(SubscriberTableValues.SUBSCRIBER_THREAD);
        if (sub != null) {
            sub.stopThread();
        }

        // Viewer
        final ChannelViewer viewer = (ChannelViewer) v.get(SubscriberTableValues.CHANNEL_VIEWER);
        viewer.dispose();

        // Update table
        subscriberModel.removeRow(i);
    }

    @SuppressWarnings("unchecked")
    private void saveProject() {
        if (projectFile != null) {
            try {
                final BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(new FileOutputStream(projectFile)));
                // Publishers
                final int p = publisherTable.getRowCount();
                String type;
                for (int i = 0; i < p; i++) {
                    final Vector<Object> v = (Vector<Object>) publisherModel.getDataVector().elementAt(i);
                    final MessagingUtils broker = (MessagingUtils) v.get(PublisherTableValues.BROKER);
                    final String channel = (String) v.get(PublisherTableValues.CHANNEL);
                    final FileObject file = (FileObject) v.get(PublisherTableValues.FILE);
                    final String loop = (String) v.get(PublisherTableValues.LOOP);
                    final RateObject rate = (RateObject) v.get(PublisherTableValues.RATE);
                    type = (String) v.get(PublisherTableValues.TYPE);
                    writer.write(ProjectFileValues.PUBLISHER_TYPE);
                    writer.write(COMMA);
                    writer.write(broker.getBrokerFullURL());
                    writer.write(COMMA);
                    writer.write(channel);
                    writer.write(COMMA);
                    writer.write(file.getFullname());
                    writer.write(COMMA);
                    writer.write(loop);
                    // writer.write(COMMA);
                    writer.write(COMMA);
                    writer.write(rate.toString());
                    writer.write(COMMA);
                    writer.write(type);
                    writer.write("\n");
                }
                // Subscribers
                final int s = subscriberTable.getRowCount();
                for (int i = 0; i < s; i++) {
                    final Vector<Object> v = (Vector<Object>) subscriberModel.getDataVector().elementAt(i);
                    final MessagingUtils broker = (MessagingUtils) v.get(PublisherTableValues.BROKER);
                    final String channel = (String) v.get(SubscriberTableValues.CHANNEL);
                    final FileObject file = (FileObject) v.get(SubscriberTableValues.FILE);
                    final String record = (String) v.get(SubscriberTableValues.RECORD);
                    type = (String) v.get(SubscriberTableValues.TYPE);
                    writer.write(ProjectFileValues.SUBSCRIBER_TYPE);
                    writer.write(COMMA);
                    writer.write(broker.getBrokerFullURL());
                    writer.write(COMMA);
                    writer.write(channel);
                    writer.write(COMMA);
                    writer.write(file.getFullname());
                    writer.write(COMMA);
                    writer.write(record);
                    // writer.write(COMMA);
                    // writer.write(COMMA);
                    writer.write(COMMA);
                    writer.write(type);
                    writer.write("\n");
                }
                writer.flush();
                writer.close();
                // statusLabel.setText(" Project saved");
                setSaveNeeded(false);
            } catch (final IOException e) {
                JOptionPane.showMessageDialog(DataRecorder.this, "Error writing project file", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else {
            saveAsProject();
        }
    }

    private int saveAsProject() {
        fileChooser.setDialogTitle("Save Project As...");
        final int returnVal = fileChooser.showDialog(DataRecorder.this, "Save As");
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            // check for overwrite
            final File f = fileChooser.getSelectedFile();
            if (f.exists()) {
                final int result = JOptionPane.showConfirmDialog(DataRecorder.this,
                        "There is already a project with the same name!\n" + "Do you want to overwrite it?", "Warning",
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

                if (result == JOptionPane.YES_OPTION) {
                    setProjectFileFromFileBrowser();
                    saveProject();
                    return JFileChooser.APPROVE_OPTION;
                } else {
                    return JFileChooser.CANCEL_OPTION;
                }
            } else {
                setProjectFileFromFileBrowser();
                saveProject();
                return JFileChooser.APPROVE_OPTION;
            }
        }
        return JFileChooser.CANCEL_OPTION;
    }

    /**
     * Added to commonized functions to read the file from the file browser. Method also sets the title bar. Called from
     * SaveProjectAs, OpenProject, and New Project after the file selection is made from the dialog. Also, called from
     * command line args.
     */
    private void setProjectFileFromFileBrowser() {
        final File f = fileChooser.getSelectedFile();
        setProjectFile(f);
    }

    public void setProjectFile(final File file) {
        final String path = fileChooser.getCurrentDirectory().toString();
        String name = file.getName();

        final int index = name.indexOf(DOT);
        if (index == -1) {
            name = name + DRP_EXTENSION;
        }
        projectFile = new File(path + SEPARATOR + name);
        setTitle(TITLE + HYPHEN + name);
    }

    private void openProject() {
        if (projectFile != null) {
            WindowUtils.setWaitCursor(this, true);
            boolean legacyProject = false;
            try {
                final BufferedReader reader = new BufferedReader(
                        new InputStreamReader(new FileInputStream(projectFile)));
                String item = reader.readLine();

                String type;
                while (item != null) {
                    final String[] result = item.split(COMMA);
                    // Parse
                    final String itemType = result[ProjectFileValues.ITEM_TYPE];

                    // Publisher
                    if (itemType.equals(ProjectFileValues.PUBLISHER_TYPE)) {
                        final String b = result[ProjectFileValues.BROKER];
                        final String c = result[ProjectFileValues.CHANNEL];
                        final FileObject file = new FileObject(result[ProjectFileValues.FILE]);
                        final String loop = result[ProjectFileValues.LOOP];
                        final String rateString = result[ProjectFileValues.RATE];
                        if (result.length == 6) {// If opening a legacy file,
                            // Topic is the only choice
                            type = "T";
                            legacyProject = true;
                        } else {
                            type = result[ProjectFileValues.TYPE];
                        }

                        final String rateName = RateObject.parseName(rateString);
                        final int rateValue = RateObject.parseValue(rateString);
                        final RateObject rate = new RateObject(rateName, rateValue);
                        addPublisher(b, c, type, file, loop, rate);

                        // Subscriber
                    } else if (itemType.equals(ProjectFileValues.SUBSCRIBER_TYPE)) {
                        final String b = result[ProjectFileValues.BROKER];
                        final String c = result[ProjectFileValues.CHANNEL];
                        if (result.length == 5) {// If opening a legacy file,
                            // Topic is the only choice
                            type = "T";
                            legacyProject = true;
                        } else {
                            type = result[ProjectFileValues.SUB_TYPE];
                        }
                        final FileObject file = new FileObject(result[ProjectFileValues.FILE]);
                        final String record = result[ProjectFileValues.RECORD];
                        addSubscriber(b, c, type, file, record);
                    }

                    item = reader.readLine();

                }
                reader.close();
            } catch (final IOException e) {
                WindowUtils.setWaitCursor(this, false);
                logger.error("Error reading project file", e);
                JOptionPane.showMessageDialog(DataRecorder.this, "Error reading project file", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
            WindowUtils.setWaitCursor(this, false);

            // Show user prompt about opening a legacy file.
            if (legacyProject) {
                JOptionPane.showMessageDialog(this, LEGACY_FILE_MESSAGE, WARNING, JOptionPane.WARNING_MESSAGE,
                        Util.getImageFromResource(bundle, "warning.Icon"));
            }
        }
    }

    private void closeProject() {
        // Check if currently opened project needs saving
        checkSaveStatus();
        clearProject();
    }

    /**
     * Method used to add a publisher to the PublisherModel. Called from AddPublisherListener, openProject
     */
    private void addPublisher(final String broker, final String channel, final Object type, final FileObject file,
            final String loop, final RateObject rate) {
        if (!preloadTopicNames.contains(channel))
            preloadTopicNames.add(channel);

        try {
            final MessagingUtils b = getBroker(broker);
            final Vector<Object> v = new Vector<Object>();
            v.add(PublisherTableValues.STATUS, PublisherTableValues.STOPPED);
            v.add(PublisherTableValues.COUNT, PublisherTableValues.RESET);
            // if DUMMY_BROKER or "" use JNDI
            v.add(PublisherTableValues.BROKER, b);
            v.add(PublisherTableValues.CHANNEL, channel);
            v.add(PublisherTableValues.TYPE, type);
            v.add(PublisherTableValues.FILE, file);
            v.add(PublisherTableValues.LOOP, loop);
            v.add(PublisherTableValues.RATE, rate);
            publisherModel.addRow(v);
            v.add(PublisherTableValues.PUBLICATION, null);
            // InformationObject o = new
            // InformationObject(p.getInformationChannel().getInformationType());
            v.add(PublisherTableValues.INFORMATION, null);
            final ChannelViewer cv = new ChannelViewer(DataRecorder.this);
            cv.setTitle(VIEWING_PUB + channel);
            v.add(PublisherTableValues.CHANNEL_VIEWER, cv);

            final ChannelPublisher pubClass = new ChannelPublisher(DataRecorder.this, v);

            final ChannelInjector pi = new ChannelInjector(DataRecorder.this, pubClass);
            pi.setTitle(INJECTING + channel);
            v.add(PublisherTableValues.CHANNEL_INJECTOR, pi);

            v.add(PublisherTableValues.CHANNEL_PUBLISHER, pubClass);
            v.add(PublisherTableValues.PUBLISHER_THREAD, null);
        } catch (final Exception ex) {
            JOptionPane.showMessageDialog(DataRecorder.this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            logger.error("Error getting handle to broker.", ex);
        }
    }

    /**
     * Method used to add a subscriber to the SubscriberModel.
     */
    private void addSubscriber(final String broker, final String channel, final Object type, final FileObject file,
            final String record) {
        if (!preloadTopicNames.contains(channel))
            preloadTopicNames.add(channel);

        try {
            final MessagingUtils b = getBroker(broker);
            final Vector<Object> v = new Vector<Object>();
            v.add(SubscriberTableValues.STATUS, SubscriberTableValues.STOPPED);
            v.add(SubscriberTableValues.COUNT, SubscriberTableValues.RESET);
            v.add(SubscriberTableValues.BROKER, b);

            v.add(SubscriberTableValues.CHANNEL, channel);
            v.add(SubscriberTableValues.TYPE, type);
            v.add(SubscriberTableValues.FILE, file);
            v.add(SubscriberTableValues.RECORD, record);
            subscriberModel.addRow(v);

            // Subscription s = channel.registerSubscription();
            v.add(SubscriberTableValues.SUBSCRIPTION, null);
            // InformationObject o = new InformationObject(type);
            v.add(SubscriberTableValues.INFORMATION, null);
            final ChannelViewer cv = new ChannelViewer(DataRecorder.this);
            cv.setTitle(VIEWING_SUB + channel);
            v.add(SubscriberTableValues.CHANNEL_VIEWER, cv);
            v.add(SubscriberTableValues.SUBSCRIBER_THREAD, null);
        } catch (final Exception ex) {
            JOptionPane.showMessageDialog(DataRecorder.this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            logger.error("Error getting handle to broker.", ex);
        }
    }

    /**
     * 
     */
    private void setSaveNeeded(final boolean value) {
        saveNeeded = value;

        if (saveNeeded) {
            if (!getTitle().endsWith(ASTERISK)) {
                setTitle(getTitle() + ASTERISK);
            }
        } else {
            if (getTitle().endsWith(ASTERISK)) {
                setTitle(getTitle().substring(0, getTitle().length() - 1));
            }
        }
    }

    private void clearProject() {
        int p = publisherTable.getRowCount();
        while (p > 0) {
            deletePublisher(p - 1);
            p = publisherTable.getRowCount();
        }
        int s = subscriberTable.getRowCount();
        while (s > 0) {
            deleteSubscriber(s - 1);
            s = subscriberTable.getRowCount();
        }
        setTitle(TITLE + DASH + UNTITLED);

        setSaveNeeded(false);
    }

    private void exit() {
        final int result = checkSaveStatus();
        if (result == JOptionPane.YES_OPTION) {
            checkSaveConfig();
            System.exit(0);
        } else if (result == JOptionPane.NO_OPTION) {
            System.exit(0);
        }
    }

    private void configure() {
        // window toolbar icon labels
        final String iconLabel = propertiesFile.getProperty(FILTER + DOT + ICONS_ONLY_PROPERTY, "false");
        if (iconLabel != null) {
            final boolean value = Boolean.valueOf(iconLabel).booleanValue();
            // menu item setSelected does not invoke the associated action
            iconLabelMenuItem.setSelected(value);
            // set the icons
            showIconsOnly(value);
        }
        // width and height of the window
        try {
            final int width = Integer.valueOf(propertiesFile.getProperty(FILTER + DOT + WIDTH_PROPERTY)).intValue();
            final int height = Integer.valueOf(propertiesFile.getProperty(FILTER + DOT + HEIGHT_PROPERTY)).intValue();
            this.setSize(width, height);
        } catch (final NumberFormatException ex) {
            // if width and height aren't set then pack the display
            pack();
        }
        // location of the window
        try {
            final int x = Integer.valueOf(propertiesFile.getProperty(FILTER + DOT + X_PROPERTY)).intValue();
            final int y = Integer.valueOf(propertiesFile.getProperty(FILTER + DOT + Y_PROPERTY)).intValue();
            this.setLocation(x, y);
        } catch (final NumberFormatException ex) {
            // if x,y not set then center window
            WindowUtils.centerWindow(this);
        }

        // project
        final String projectDirectory = propertiesFile.getProperty(FILTER + DOT + PROJECT_DIRECTORY_PROPERTY);
        if (projectDirectory != null) {
            final File file = new File(projectDirectory);
            fileChooser.setCurrentDirectory(file);
        }

        // publisher
        final String publisherDirectory = propertiesFile.getProperty(FILTER + DOT + PUBLISHER_DIRECTORY_PROPERTY);
        if (publisherDirectory != null) {
            final File file = new File(publisherDirectory);
            PublisherEditor.fileChooser.setCurrentDirectory(file);
        }

        // subscriber
        final String subscriberDirectory = propertiesFile.getProperty(FILTER + DOT + SUBSCRIBER_DIRECTORY_PROPERTY);
        if (subscriberDirectory != null) {
            final File file = new File(subscriberDirectory);
            SubscriberEditor.fileChooser.setCurrentDirectory(file);
        }

        // injector
        final String injectorDirectory = propertiesFile.getProperty(FILTER + DOT + INJECTOR_DIRECTORY_PROPERTY);
        if (injectorDirectory != null) {
            final File file = new File(injectorDirectory);
            ImageInjector.fileChooser.setCurrentDirectory(file);
        }

        // auto-save option
        final String autoSave = propertiesFile.getProperty(FILTER + DOT + AUTOSAVE_PROPERTY);
        if (autoSave != null) {
            final boolean value = Boolean.valueOf(autoSave).booleanValue();
            // menu item setSelected does not invoke the associated action
            autoSaveMenuItem.setSelected(value);
        }
    }

    private void checkSaveConfig() {
        propertiesFile.setProperty(FILTER + DOT + WIDTH_PROPERTY, Integer.toString(this.getWidth()));
        propertiesFile.setProperty(FILTER + DOT + HEIGHT_PROPERTY, Integer.toString(this.getHeight()));
        propertiesFile.setProperty(FILTER + DOT + X_PROPERTY, Integer.toString(this.getX()));
        propertiesFile.setProperty(FILTER + DOT + Y_PROPERTY, Integer.toString(this.getY()));
        propertiesFile.setProperty(FILTER + DOT + PROJECT_DIRECTORY_PROPERTY,
                fileChooser.getCurrentDirectory().getAbsolutePath());
        propertiesFile.setProperty(FILTER + DOT + PUBLISHER_DIRECTORY_PROPERTY,
                PublisherEditor.fileChooser.getCurrentDirectory().getAbsolutePath());
        propertiesFile.setProperty(FILTER + DOT + SUBSCRIBER_DIRECTORY_PROPERTY,
                SubscriberEditor.fileChooser.getCurrentDirectory().getAbsolutePath());
        propertiesFile.setProperty(FILTER + DOT + INJECTOR_DIRECTORY_PROPERTY,
                ImageInjector.fileChooser.getCurrentDirectory().getAbsolutePath());
        propertiesFile.setProperty(FILTER + DOT + AUTOSAVE_PROPERTY, Boolean.toString(autoSaveMenuItem.isSelected()));
        propertiesFile.setProperty(FILTER + DOT + ICONS_ONLY_PROPERTY,
                Boolean.toString(iconLabelMenuItem.isSelected()));
        propertiesFile.setProperty(FILTER + DOT + BUNDLE_PATH, bundlePath);

        Util.saveProperties(this, propertiesFile, PROPERTY_FILE);
    }

    /**
     * Method used to determine if the current project has changes. Called from New, Open, Exit and Drop Project
     */
    public int checkSaveStatus() {
        if (saveNeeded) {
            int result = JOptionPane.YES_OPTION;
            // prompt if auto save is not selected
            if ((!autoSaveMenuItem.isSelected()) || (projectFile == null && saveNeeded))
                result = JOptionPane.showConfirmDialog(this,
                        "Changes have been made to the current project\nWould you like to save them?", "Warning",
                        JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            // save the project
            if (result == JOptionPane.YES_OPTION) {
                if (projectFile != null) {
                    saveProject();
                    return JOptionPane.YES_OPTION;
                } else {
                    final int value = saveAsProject();
                    if (value == JFileChooser.APPROVE_OPTION) {
                        return JOptionPane.YES_OPTION;
                    } else if (value == JFileChooser.CANCEL_OPTION) {
                        return JOptionPane.CANCEL_OPTION;
                    }
                }
            } else if (result == JOptionPane.NO_OPTION) {
                // setSaveNeeded(false);
                return JOptionPane.NO_OPTION;
            } else if (result == JOptionPane.CANCEL_OPTION) {
                return JOptionPane.CANCEL_OPTION;
            }
        }
        return JOptionPane.YES_OPTION;
    }

    @SuppressWarnings("unchecked")
    public synchronized void dropProject(final DropTargetDropEvent dropTargetDropEvent) {
        try {
            final Transferable tr = dropTargetDropEvent.getTransferable();
            // DataFlavor[] flavors =
            // dropTargetDropEvent.getCurrentDataFlavors();

            // linux
            if (tr.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                dropTargetDropEvent.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                final String fileName = (String) tr.getTransferData(DataFlavor.stringFlavor);
                final int result = checkSaveStatus();
                if (result != JOptionPane.CANCEL_OPTION) {
                    clearProject();
                    final File file = new File(fileName.substring(7, fileName.indexOf("\n") - 1));
                    projectFile = file;
                    setTitle(TITLE + HYPHEN + file.getName());
                    openProject();
                    dropTargetDropEvent.getDropTargetContext().dropComplete(true);
                    return;
                }
            }

            // windows
            if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                dropTargetDropEvent.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                final List<Object> fileList = (List<Object>) tr.getTransferData(DataFlavor.javaFileListFlavor);
                final Iterator<Object> iterator = fileList.iterator();
                while (iterator.hasNext()) {
                    final File file = (File) iterator.next();
                    if (file.getName().lastIndexOf(DRP_EXTENSION) >= 0) {
                        final int result = checkSaveStatus();
                        if (result != JOptionPane.CANCEL_OPTION) {
                            clearProject();
                            projectFile = file;
                            setTitle(TITLE + HYPHEN + file.getName());
                            openProject();
                            dropTargetDropEvent.getDropTargetContext().dropComplete(true);
                            return;
                        }
                    }
                }
            }

            // unknown
            dropTargetDropEvent.rejectDrop();

        } catch (final IOException io) {
            dropTargetDropEvent.rejectDrop();
            logger.warn("An error occured getting data.", io);
        } catch (final UnsupportedFlavorException ufe) {
            dropTargetDropEvent.rejectDrop();
            logger.warn("Unable to get requests data.", ufe);
        }
    }

    /**
     * Shows the button icons only if set to true.
     * 
     * @param value
     *            true to show icons only, false icon and text.
     */
    private void showIconsOnly(final boolean value) {
        if (value) {
            newButton.setText(EMPTY_STRING);
            openButton.setText(EMPTY_STRING);
            saveButton.setText(EMPTY_STRING);
            publisherButton.setText(EMPTY_STRING);
            subscriberButton.setText(EMPTY_STRING);
            deleteButton.setText(EMPTY_STRING);
            copyButton.setText(EMPTY_STRING);
            startButton.setText(EMPTY_STRING);
            pauseButton.setText(EMPTY_STRING);
            stopButton.setText(EMPTY_STRING);
            selectAllButton.setText(EMPTY_STRING);
            clearAllButton.setText(EMPTY_STRING);
            resetButton.setText(EMPTY_STRING);
        } else {
            newButton.setText(NEW_LABEL);
            openButton.setText(OPEN_LABEL);
            saveButton.setText(SAVE_LABEL);
            publisherButton.setText(PUB_LABEL);
            subscriberButton.setText(SUB_LABEL);
            deleteButton.setText(DELETE_LABEL);
            copyButton.setText(COPY_LABEL);
            startButton.setText(START_LABEL);
            pauseButton.setText(PAUSE_LABEL);
            stopButton.setText(STOP_LABEL);
            selectAllButton.setText(SELECT_ALL_LABEL);
            clearAllButton.setText(CLEAR_ALL_LABEL);
            resetButton.setText(RESET_LABEL);
        }
    }

    class DroppablePanel extends JPanel implements DropTargetListener, DragSourceListener, DragGestureListener {
        private static final long serialVersionUID = -5220328525631475695L;
        DropTarget dropTarget = new DropTarget(this, this);
        DragSource dragSource = DragSource.getDefaultDragSource();

        public DroppablePanel(final BorderLayout layout) {
            super(layout);
            dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
        }

        public void dragDropEnd(final DragSourceDropEvent DragSourceDropEvent) {
        }

        public void dragEnter(final DragSourceDragEvent DragSourceDragEvent) {
        }

        public void dragExit(final DragSourceEvent DragSourceEvent) {
        }

        public void dragOver(final DragSourceDragEvent DragSourceDragEvent) {
        }

        public void dropActionChanged(final DragSourceDragEvent DragSourceDragEvent) {
        }

        public void dragEnter(final DropTargetDragEvent dropTargetDragEvent) {
            dropTargetDragEvent.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
        }

        public void dragExit(final DropTargetEvent dropTargetEvent) {
        }

        public void dragOver(final DropTargetDragEvent dropTargetDragEvent) {
        }

        public void dropActionChanged(final DropTargetDragEvent dropTargetDragEvent) {
        }

        public void drop(final DropTargetDropEvent dropTargetDropEvent) {
            DataRecorder.this.dropProject(dropTargetDropEvent);
        }

        public void dragGestureRecognized(final DragGestureEvent dge) {
        }

    }

    public void clearAll() {
        publisherTable.clearSelection();
        subscriberTable.clearSelection();
    }

    public void selectAll() {
        publisherTable.selectAll();
        subscriberTable.selectAll();
    }

    /*
     * (non-Javadoc)
     * @see javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax.swing .JTable, java.lang.Object,
     * boolean, int, int)
     */
    public Component getTableCellEditorComponent(final JTable table, final Object value, final boolean isSelected,
            final int row, final int column) {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see javax.swing.CellEditor#getCellEditorValue()
     */
    public Object getCellEditorValue() {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see javax.swing.CellEditor#isCellEditable(java.util.EventObject)
     */
    public boolean isCellEditable(final EventObject anEvent) {
        return false;
    }

    /*
     * (non-Javadoc)
     * @see javax.swing.CellEditor#shouldSelectCell(java.util.EventObject)
     */
    public boolean shouldSelectCell(final EventObject anEvent) {
        return false;
    }

    /*
     * (non-Javadoc)
     * @see javax.swing.CellEditor#stopCellEditing()
     */
    public boolean stopCellEditing() {
        return false;
    }

    /*
     * (non-Javadoc)
     * @see javax.swing.CellEditor#cancelCellEditing()
     */
    public void cancelCellEditing() {

    }

    /*
     * (non-Javadoc)
     * @seejavax.swing.CellEditor#addCellEditorListener(javax.swing.event. CellEditorListener)
     */
    public void addCellEditorListener(final CellEditorListener l) {

    }

    /*
     * (non-Javadoc)
     * @seejavax.swing.CellEditor#removeCellEditorListener(javax.swing.event. CellEditorListener)
     */
    public void removeCellEditorListener(final CellEditorListener l) {

    }

    /**
     * @return the propertiesFile
     */
    public static Properties getPropertiesFile() {
        return propertiesFile;
    }
}
