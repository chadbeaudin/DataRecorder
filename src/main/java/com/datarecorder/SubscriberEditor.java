package com.datarecorder;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.slf4j.Logger;

import com.datarecorder.util.MessagingUtils;
import com.datarecorder.util.ui.DialogTitlePane;
import com.datarecorder.util.ui.EscapeDialog;
import com.datarecorder.util.ui.WindowUtils;

public class SubscriberEditor extends EscapeDialog {

    private static final long serialVersionUID = -4662917671182817559L;
    private static final String EDIT_DETAILS = DataRecorder.bundle.getString("subscriberEditor.EditDetails");
    private static final String EDIT_TITLE = DataRecorder.bundle.getString("subscriberEditor.EditTitle");
    private static final String ADD_DETAILS = DataRecorder.bundle.getString("subscriberEditor.AddDetails");
    private static final String ADD_TITLE = DataRecorder.bundle.getString("subscriberEditor.AddTitle");
    public static final String ADD_MODE = "ADD";
    public static final String EDIT_MODE = "EDIT";
    private static final String CANCEL = "CANCEL";
    private static final String ADD = "ADD";
    private static final String APPLY = "APPLY";
    private static final char DOT = '.';
    private static final String CAPTURE_EXTENSION = ".capture";
    public static final String TOPIC = "Topic";
    public static final String QUEUE = "Queue";

    // private static String DEFAULT_JMX_SERVICE_URL =
    // "service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi";

    private DataRecorder dataRecorder;
    private JComboBox broker = new JComboBox();
    private JComboBox topic = new JComboBox();
    private JCheckBox record = new JCheckBox("Record");
    private JTextField file = new JTextField(30);
    public static final JFileChooser fileChooser = new JFileChooser();

    static {
        fileChooser.setFileFilter(new SubscriberFileFilter());
    }

    // private JTextField size = new JTextField(10);
    private JButton browseButton = new JButton();
    private JButton clearButton = new JButton();
    private JButton actionButton = new JButton();
    private JButton cancelButton = new JButton();
    private String mode;
    private String action = "";
    private int minWidth;
    private int minHeight;
    private DialogTitlePane titlePane;
    private ImageIcon smallOpenIcon;
    private ImageIcon addIcon;
    private ImageIcon clearIcon;
    private String originalFileName = "";
    private Boolean isQueue = false;
    private ButtonGroup topicQueueGroup = new ButtonGroup();
    private JRadioButton topicButton = new JRadioButton(TOPIC);
    private JRadioButton queueButton = new JRadioButton(QUEUE);

    private Logger logger = LoggingObject.getLogger(this.getClass());

    // private int currentRow = -1;

    public SubscriberEditor(final DataRecorder parent) {
        super(parent, " Add New Subscriber", true);

        dataRecorder = parent;

        // Set element attributes
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                cancel();
            }
        });

        setResizable(true);

        smallOpenIcon = Util.getImageFromResource(DataRecorder.bundle, "openButton.SmallIcon");
        addIcon = Util.getImageFromResource(DataRecorder.bundle, "addSubscriberButton.Icon");
        clearIcon = Util.getImageFromResource(DataRecorder.bundle, "clearButton.SmallIcon");

        browseButton.setIcon(smallOpenIcon);
        browseButton.setToolTipText("Open file for recording");
        clearButton.setIcon(clearIcon);
        clearButton.setToolTipText("Clear current selection");
        broker.setEditable(true);
        broker.setEnabled(false);
        topic.setEditable(true);
        topic.setEnabled(true);
        browseButton.setMargin(new Insets(1, 1, 1, 1));
        clearButton.setMargin(new Insets(1, 1, 1, 1));
        file.setEditable(false);

        topicQueueGroup.add(topicButton);
        topicQueueGroup.add(queueButton);
        final JPanel radioButtonPanel = new JPanel();
        radioButtonPanel.add(topicButton);
        radioButtonPanel.add(queueButton);

        // topic Panel
        final JPanel topicPanel = new JPanel(new GridBagLayout());
        topicPanel.setBorder(BorderFactory.createTitledBorder("Topic Selection"));
        topicPanel.add(new JLabel("Broker:"), new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
                GridBagConstraints.NONE, new Insets(4, 4, 4, 4), 0, 0));

        topicPanel.add(new JLabel("Topic:"), new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
                GridBagConstraints.NONE, new Insets(4, 4, 4, 4), 0, 0));

        topicPanel.add(broker, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST,
                GridBagConstraints.HORIZONTAL, new Insets(4, 0, 4, 4), 0, 0));

        topicPanel.add(topic, new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST,
                GridBagConstraints.HORIZONTAL, new Insets(4, 0, 4, 4), 0, 0));

        topicPanel.add(new JLabel("Type:"), new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
                GridBagConstraints.NONE, new Insets(4, 4, 4, 4), 0, 0));

        topicPanel.add(radioButtonPanel, new GridBagConstraints(1, 2, 2, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST,
                GridBagConstraints.REMAINDER, new Insets(4, 0, 4, 4), 0, 0));

        // File Panel
        final JPanel filePanel = new JPanel(new GridBagLayout());
        filePanel.setBorder(BorderFactory.createTitledBorder("File Selection"));

        filePanel.add(record, new GridBagConstraints(2, 1, 2, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST,
                GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

        filePanel.add(new JLabel("File:"), new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
                GridBagConstraints.NONE, new Insets(4, 4, 4, 4), 0, 0));

        filePanel.add(file, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST,
                GridBagConstraints.HORIZONTAL, new Insets(4, 0, 4, 4), 0, 0));

        filePanel.add(browseButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST,
                GridBagConstraints.NONE, new Insets(4, 4, 4, 4), 0, 0));

        filePanel.add(clearButton, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST,
                GridBagConstraints.NONE, new Insets(4, 4, 4, 4), 0, 0));

        // Button Panel
        final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        cancelButton.setText("Cancel");
        buttonPanel.add(actionButton);
        buttonPanel.add(cancelButton);

        // Center Panel
        final Container c = getContentPane();
        c.setLayout(new BorderLayout());
        final JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBorder(BorderFactory.createRaisedBevelBorder());
        centerPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(),
                BorderFactory.createEmptyBorder(8, 8, 0, 8)));

        centerPanel.add(topicPanel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST,
                GridBagConstraints.HORIZONTAL, new Insets(0, 0, 4, 0), 0, 0));
        centerPanel.add(filePanel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST,
                GridBagConstraints.HORIZONTAL, new Insets(0, 0, 4, 0), 0, 0));
        centerPanel.add(new JLabel(), new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST,
                GridBagConstraints.BOTH, new Insets(0, 2, 2, 2), 0, 0));

        // Listeners
        browseButton.addActionListener(new BrowseListener());
        clearButton.addActionListener(new ClearListener());
        cancelButton.addActionListener(new CancelListener());
        actionButton.addActionListener(new MyActionListener());
        actionButton.setMnemonic(KeyEvent.VK_ENTER);
        topicButton.addActionListener(new TopicButtonListener());
        queueButton.addActionListener(new QueueButtonListener());
        this.getRootPane().setDefaultButton(actionButton);
        addComponentListener(new MyComponentListener());

        // Title Pane (title and details are set on open
        titlePane = new DialogTitlePane("", "", addIcon);
        // Finish
        c.setLayout(new GridBagLayout());
        c.add(titlePane, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTH,
                GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        c.add(centerPanel, new GridBagConstraints(0, 1, 1, 1, 2.0, 5.0, GridBagConstraints.NORTH,
                GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        c.add(buttonPanel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.NORTH,
                GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

        // Default the Topic button to be selected.
        topicButton.setSelected(true);

        pack();
        minWidth = getWidth();
        minHeight = getHeight();
    }

    public void setVisible(final boolean state, final String mode, final int row) {
        // currentRow = row;
        this.mode = mode;
        getBrokers();
        getTopicNames();
        String title = null;
        String details = null;
        if (mode.equals(ADD_MODE)) {
            title = ADD_TITLE;
            details = ADD_DETAILS;
        } else {
            title = EDIT_TITLE;
            details = EDIT_DETAILS;
        }
        titlePane.setTitle(title);
        titlePane.setDetails(details);
        pack();
        minWidth = getWidth();
        minHeight = getHeight();

        // show
        if (state) {
            if (mode.equals(ADD_MODE)) {
                broker.setEnabled(true);
                broker.setSelectedItem("tcp://localhost:61616");
                record.setEnabled(true);
                topic.setSelectedIndex(-1);
                topic.setEnabled(true); // since there is no broker for jms at
                                        // this time
                // topic.setEnabled(false);
                file.setText("");
                record.setSelected(false);
                actionButton.setText("Add");
            } else {
                if (mode.equals(EDIT_MODE)) {
                    setTitle(" Edit Subscriber");
                    final Vector<Object> v = (Vector<Object>) dataRecorder.subscriberModel.getDataVector()
                            .elementAt(row);
                    broker.setSelectedItem(((MessagingUtils) v.get(SubscriberTableValues.BROKER)).getBrokerFullURL());
                    broker.getModel()
                            .setSelectedItem(((MessagingUtils) v.get(SubscriberTableValues.BROKER)).getBrokerFullURL());
                    topic.setSelectedItem(v.get(SubscriberTableValues.CHANNEL));
                    topic.getModel().setSelectedItem(v.get(SubscriberTableValues.CHANNEL));

                    // file

                    final FileObject f = (FileObject) v.get(SubscriberTableValues.FILE);
                    if (f != null) {
                        originalFileName = f.getFullname();
                    }

                    if (file != null) {
                        file.setText(originalFileName);
                        final File myFile = new File(originalFileName);
                        fileChooser.setCurrentDirectory(myFile);
                    }

                    // record
                    final String l = (String) v.get(SubscriberTableValues.RECORD);
                    if (l.equals("On")) {
                        record.setSelected(true);
                    } else {
                        record.setSelected(false);
                    }

                    // check status
                    final String status = (String) v.get(SubscriberTableValues.STATUS);
                    if (!status.equals("Stopped")) {
                        broker.setEnabled(false);
                        topic.setEnabled(false);
                        record.setEnabled(false);
                        topicButton.setEnabled(false);
                        queueButton.setEnabled(false);
                        browseButton.setEnabled(false);
                        clearButton.setEnabled(false);
                    } else {
                        broker.setEnabled(true);
                        topic.setEnabled(true);
                        record.setEnabled(true);
                        topicButton.setEnabled(true);
                        queueButton.setEnabled(true);
                        browseButton.setEnabled(true);
                        clearButton.setEnabled(true);
                    }

                    // Get Type (Queue or Topic)
                    final String type = (String) v.get(SubscriberTableValues.TYPE);

                    if (type.equals("T")) {
                        topicButton.setSelected(true);
                    } else {
                        queueButton.setSelected(true);
                    }

                    actionButton.setText("Apply");
                }
            }
            WindowUtils.centerWindow(this);
        }

        super.setVisible(state);
    }

    public boolean isAction() {
        if (action == null || (action.equals(CANCEL)) || action.equals("")) {
            return false;
        } else {
            return true;
        }
    }

    public FileObject getFile() {
        final FileObject f = new FileObject(file.getText());
        return f;
    }

    public String getRecord() {
        if (record.isSelected())
            return SubscriberTableValues.ON;
        else
            return SubscriberTableValues.OFF;
    }

    public String getBroker() {
        return broker.getSelectedItem().toString();
    }

    public String getChannel() {
        return (String) topic.getSelectedItem();
    }

    public Boolean isQueue() {
        return this.isQueue;
    }

    /**
     * Sets the list of topic names of the broker this subscriber is connected to. Note JMX must be turned on in the
     * server start script and also, the port for the JMX server must be 1099.
     * 
     */
    private void getTopicNames() {
        topic.removeAllItems();
        final List<String> topicNames = this.dataRecorder.getPreloadTopicNames();
        for (int i = 0; i < topicNames.size(); i++) {
            topic.addItem(topicNames.get(i));
        }
        //
        // //adding a new publisher
        // if (currentRow < 0)
        // return;
        //
        // Vector v = null;
        // MessagingUtils broker = null;
        // if (currentRow > -1) {
        // v =
        // (Vector)dataRecorder.subscriberModel.getDataVector().elementAt(currentRow);
        // broker = (MessagingUtils)v.get(SubscriberTableValues.BROKER);
        // }
        //
        // String brokerName = broker.getBrokerURL().substring(0,
        // broker.getBrokerURL().indexOf(":"));
        // String jmxURL =
        // DEFAULT_JMX_SERVICE_URL.replace("localhost",brokerName);
        //
        // Object[] topicList = broker.getTopicNames(jmxURL).toArray();
        // for (int i = 0; i < topicList.length; i++) {
        // topic.setEnabled(true);
        // // String c = (String)topicList[i];
        // topic.addItem(topicList[i]);
        // }
        // topic.setSelectedIndex(-1);
    }

    private void getBrokers() {
        WindowUtils.setWaitCursor(dataRecorder, true);
        final Map<String, MessagingUtils> brokerMap = dataRecorder.getBrokerMap();
        broker.removeAllItems();
        final Set<String> keySet = brokerMap.keySet();
        final Iterator<String> iter = keySet.iterator();
        while (iter.hasNext()) {
            final MessagingUtils b = brokerMap.get(iter.next());
            broker.addItem(b.getBrokerFullURL());
        }

        final List<String> brokerNames = this.dataRecorder.getPreloadBrokerNames();
        for (int i = 0; i < brokerNames.size(); i++) {
            if (!brokerMap.containsKey(brokerNames.get(i)))
                broker.addItem(brokerNames.get(i));
        }

        WindowUtils.setWaitCursor(dataRecorder, false);
    }

    private void cancel() {
        // Check if things have changed
        action = CANCEL;
        this.setVisible(false);
    }

    // private class BrokerListener implements ActionListener {
    // public void actionPerformed(ActionEvent event) {
    // try {
    // channel.removeAllItems();
    // if (broker.getSelectedItem() != null) {
    // InformationBroker b = (InformationBroker)broker.getSelectedItem();
    // b.connect(); // connection exception
    // InformationChannel[] channelList = b.getInformationChannels();
    // for (int i = 0; i < channelList.length; i++) {
    // channel.setEnabled(true);
    // InformationChannel c = (InformationChannel)channelList[i];
    // channel.addItem(c);
    // }
    // channel.setSelectedIndex(-1);
    // type.setText("");
    // }
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // }
    // }
    //
    // private class TopicListener implements ActionListener {
    // public void actionPerformed(ActionEvent event) {
    // if (topic.getSelectedItem() != null)
    // {
    // String c = (String)topic.getSelectedItem();
    // // InformationType t = (InformationType)c.getInformationType();
    // // type.setText(t.getName());
    // }
    // }
    // }

    private class MyComponentListener implements ComponentListener {
        public void componentHidden(final ComponentEvent c) {
        }

        public void componentShown(final ComponentEvent c) {
        }

        public void componentMoved(final ComponentEvent c) {
        }

        public void componentResized(final ComponentEvent c) {
            final JDialog source = (JDialog) c.getSource();
            int width = source.getWidth();
            int height = source.getHeight();
            if (width < minWidth)
                width = minWidth;
            if (height < minHeight)
                height = minHeight;
            source.setSize(width, height);
        }
    }

    private class CancelListener implements ActionListener {
        public void actionPerformed(final ActionEvent event) {
            cancel();
        }
    }

    private class MyActionListener implements ActionListener {
        public void actionPerformed(final ActionEvent event) {

            // broker
            if ((broker.getSelectedItem() == null) || broker.getSelectedItem().toString().trim().equals("")) {
                JOptionPane.showMessageDialog(SubscriberEditor.this, "A valid broker must be selected", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // topic
            if (topic.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(SubscriberEditor.this, "A valid topic must be selected", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // record
            if (record.isSelected()) {
                if (file.getText().equals("")) {
                    JOptionPane.showMessageDialog(SubscriberEditor.this, "A valid file must be entered", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            // check if file exists
            final String subFileName = file.getText();
            
            if (!subFileName.equals(""))  {
	            final File subFile = new File(subFileName);
	            if (!subFile.exists()) {
	                try {
	                    subFile.createNewFile();
	                } catch (final IOException e) {
	                    logger.warn("File does not exist.", e);
	                }
	            } else {
	                // also check if file name changed
	                if (!subFileName.equals(originalFileName)) {
	                    final int result = JOptionPane.showConfirmDialog(SubscriberEditor.this,
	                            "This will overwrite all data in:\n" + file.getText() + "\nAre you sure?", "Warning",
	                            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
	
	                    if (result == JOptionPane.NO_OPTION) {
	                        return;
	                    }
	                }
	            }
            }
            
            if (mode.equals(ADD_MODE)) {
                action = ADD;
            }

            if (mode.equals(EDIT_MODE)) {
                action = APPLY;
            }

            setVisible(false);
        }
    }

    private class BrowseListener implements ActionListener {
        public void actionPerformed(final ActionEvent event) {
            final int returnVal = fileChooser.showOpenDialog(SubscriberEditor.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {

                // try and append the extension
                String fileName = fileChooser.getSelectedFile().getName();
                final int index = fileName.indexOf(DOT);
                if (index == -1) {
                    fileName = fileName + CAPTURE_EXTENSION;
                }

                // show in text field
                String separator;
                if (fileChooser.getCurrentDirectory().toString().endsWith(System.getProperty("file.separator"))) {
                    separator = "";
                } else {
                    separator = System.getProperty("file.separator");
                }
                file.setText(fileChooser.getCurrentDirectory().toString() + separator + fileName);
            }
        }
    }

    private class ClearListener implements ActionListener {
        public void actionPerformed(final ActionEvent event) {
            file.setText("");
        }
    }

    private class TopicButtonListener implements ActionListener {
        public void actionPerformed(final ActionEvent event) {
            isQueue = false;
            logger.debug("Setting queue to false");
        }
    }

    private class QueueButtonListener implements ActionListener {
        public void actionPerformed(final ActionEvent event) {
            isQueue = true;
            logger.debug("Setting queue to true");
        }
    }

}
