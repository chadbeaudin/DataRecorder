package com.datarecorder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultButtonModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import org.slf4j.Logger;

import com.datarecorder.util.MessagingUtils;
import com.datarecorder.util.ui.DialogTitlePane;
import com.datarecorder.util.ui.EscapeDialog;
import com.datarecorder.util.ui.WindowUtils;

public class PublisherEditor extends EscapeDialog {

    private static final long serialVersionUID = 4050766004831467568L;
    private static final String EDIT_DETAILS = DataRecorder.bundle.getString("publisherEditor.EditDetails");
    private static final String EDIT_TITLE = DataRecorder.bundle.getString("publisherEditor.EditTitle");
    private static final String ADD_DETAILS = DataRecorder.bundle.getString("publisherEditor.AddDetails");
    private static final String ADD_TITLE = DataRecorder.bundle.getString("publisherEditor.AddTitle");
    public static final String ADD_MODE = "ADD";
    public static final String EDIT_MODE = "EDIT";
    public static final String TOPIC = "Topic";
    public static final String QUEUE = "Queue";
    private String CANCEL = "CANCEL";
    private String ADD = "ADD";
    private String APPLY = "APPLY";
    private Boolean isQueue = false;

    private DataRecorder dataRecorder;
    private JComboBox broker = new JComboBox();
    private JComboBox topic = new JComboBox();
    private JCheckBox loop = new JCheckBox("Loop");
    private JTextField file = new JTextField(30);
    public static final JFileChooser fileChooser = new JFileChooser();

    static {
    	fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        fileChooser.setFileFilter(new RecordFileFilter());
    }

    private JButton browseButton = new JButton();
    private JButton clearButton = new JButton();
    private JButton previewButton = new JButton("Preview...");
    private JRadioButton actualButton = new JRadioButton("Actual");
    private JRadioButton fasterButton = new JRadioButton("Faster by");
    private JRadioButton slowerButton = new JRadioButton("Slower by");
    private JRadioButton constantButton = new JRadioButton("Constant at");
    private ButtonGroup rateGroup = new ButtonGroup();
    private JSpinner faster = new JSpinner();
    private JSpinner slower = new JSpinner();
    private JSpinner constant = new JSpinner();
    private JButton actionButton = new JButton();
    private JButton cancelButton = new JButton();
    private String mode;
    private String action;
    private int minWidth;
    private int minHeight;
    private DialogTitlePane titlePane;
    private ImageIcon smallOpenIcon;
    private ImageIcon addIcon;
    private ImageIcon clearIcon;

    private ButtonGroup topicQueueGroup = new ButtonGroup();
    private JRadioButton topicButton = new JRadioButton(TOPIC);
    private JRadioButton queueButton = new JRadioButton(QUEUE);

    private Logger logger = LoggingObject.getLogger(this.getClass());

    public PublisherEditor(final JFrame parent) {

        super(parent, " Add New Publisher", true);
        dataRecorder = (DataRecorder) parent;

        WindowUtils.setWaitCursor(parent, true);

        // Set element attributes
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                cancel();
            }
        });

        setModal(true);
        setResizable(true);

        smallOpenIcon = Util.getImageFromResource(DataRecorder.bundle, "openButton.SmallIcon");
        addIcon = Util.getImageFromResource(DataRecorder.bundle, "addPublisherButton.Icon");
        clearIcon = Util.getImageFromResource(DataRecorder.bundle, "clearButton.SmallIcon");

        browseButton.setIcon(smallOpenIcon);
        browseButton.setToolTipText("Open file for playing");
        clearButton.setIcon(clearIcon);
        clearButton.setToolTipText("Clear current selection");
        broker.setEditable(false);
        broker.setEnabled(false);
        topic.setEditable(true);
        topic.setEnabled(true);
        browseButton.setMargin(new Insets(1, 1, 1, 1));
        clearButton.setMargin(new Insets(1, 1, 1, 1));
        file.setEditable(false);
        faster.setModel(new SpinnerNumberModel(2, 2, 999, 1));
        faster.setEnabled(false);
        slower.setModel(new SpinnerNumberModel(2, 2, 999, 1));
        slower.setEnabled(false);
        constant.setModel(new SpinnerNumberModel(1, 1, 999, 1));
        constant.setEnabled(false);
        actualButton.setSelected(true);
        rateGroup.add(actualButton);
        rateGroup.add(fasterButton);
        rateGroup.add(slowerButton);
        rateGroup.add(constantButton);

        previewButton.setMargin(new Insets(0, 4, 0, 4));

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

        topicPanel.add(new JLabel("Type:"), new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
                GridBagConstraints.NONE, new Insets(4, 4, 4, 4), 0, 0));

        topicPanel.add(broker, new GridBagConstraints(1, 0, 2, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST,
                GridBagConstraints.HORIZONTAL, new Insets(4, 0, 4, 4), 0, 0));

        topicPanel.add(topic, new GridBagConstraints(1, 1, 2, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST,
                GridBagConstraints.HORIZONTAL, new Insets(4, 0, 4, 4), 0, 0));

        topicPanel.add(radioButtonPanel, new GridBagConstraints(1, 2, 2, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST,
                GridBagConstraints.REMAINDER, new Insets(4, 0, 4, 4), 0, 0));

        // File Panel
        final JPanel filePanel = new JPanel(new GridBagLayout());
        filePanel.setBorder(BorderFactory.createTitledBorder("File Selection"));

        /*
         * filePanel.add(previewButton, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0 ,GridBagConstraints.CENTER,
         * GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
         */

        filePanel.add(loop, new GridBagConstraints(2, 1, 2, 1, 0.0, 0.0, GridBagConstraints.WEST,
                GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

        filePanel.add(new JLabel("File:"), new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
                GridBagConstraints.NONE, new Insets(4, 4, 4, 4), 0, 0));

        filePanel.add(file, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST,
                GridBagConstraints.HORIZONTAL, new Insets(4, 0, 4, 4), 0, 0));

        filePanel.add(browseButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST,
                GridBagConstraints.HORIZONTAL, new Insets(4, 4, 4, 4), 0, 0));

        filePanel.add(clearButton, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST,
                GridBagConstraints.HORIZONTAL, new Insets(4, 4, 4, 4), 0, 0));

        // Rate Panel
        final JPanel ratePanel = new JPanel(new GridBagLayout());
        ratePanel.setBorder(BorderFactory.createTitledBorder("Message Rate Selection"));
        ratePanel.add(actualButton, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
                GridBagConstraints.NONE, new Insets(0, 2, 2, 2), 0, 0));

        ratePanel.add(fasterButton, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
                GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));

        ratePanel.add(faster, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
                GridBagConstraints.NONE, new Insets(2, 0, 2, 2), 0, 0));

        ratePanel.add(new JLabel("times"), new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));

        ratePanel.add(slowerButton, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
                GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));

        ratePanel.add(slower, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
                GridBagConstraints.NONE, new Insets(2, 0, 2, 2), 0, 0));

        ratePanel.add(new JLabel("times"), new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));

        ratePanel.add(constantButton, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
                GridBagConstraints.NONE, new Insets(2, 2, 2, 0), 0, 0));

        ratePanel.add(constant, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
                GridBagConstraints.NONE, new Insets(2, 0, 2, 2), 0, 0));

        ratePanel.add(new JLabel("per second"), new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));

        ratePanel.add(new JLabel(), new GridBagConstraints(3, 3, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST,
                GridBagConstraints.HORIZONTAL, new Insets(0, 2, 2, 2), 0, 0));

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

        centerPanel.add(topicPanel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTH,
                GridBagConstraints.HORIZONTAL, new Insets(0, 0, 4, 0), 0, 0));
        centerPanel.add(filePanel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.NORTH,
                GridBagConstraints.HORIZONTAL, new Insets(0, 0, 4, 0), 0, 0));
        centerPanel.add(ratePanel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.NORTH,
                GridBagConstraints.HORIZONTAL, new Insets(0, 0, 4, 0), 0, 0));
        centerPanel.add(new JLabel(), new GridBagConstraints(0, 3, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST,
                GridBagConstraints.BOTH, new Insets(0, 2, 2, 2), 0, 0));

        // Listeners
        // broker.addActionListener(new BrokerListener());
        // topic.addActionListener(new TopicListener());
        browseButton.addActionListener(new BrowseListener());
        clearButton.addActionListener(new ClearListener());
        cancelButton.addActionListener(new CancelListener());
        actionButton.addActionListener(new MyActionListener());
        actionButton.setMnemonic(KeyEvent.VK_ENTER);
        this.getRootPane().setDefaultButton(actionButton);
        addComponentListener(new MyComponentListener());
        faster.addKeyListener(new MyKeyAdapter());
        slower.addKeyListener(new MyKeyAdapter());
        constant.addKeyListener(new MyKeyAdapter());
        actualButton.addActionListener(new ActualListener());
        fasterButton.addActionListener(new FasterListener());
        slowerButton.addActionListener(new SlowerListener());
        constantButton.addActionListener(new ConstantListener());
        topicButton.addActionListener(new TopicButtonListener());
        queueButton.addActionListener(new QueueButtonListener());

        // Title Pane (title and details are set on open
        titlePane = new DialogTitlePane(ADD_TITLE, ADD_DETAILS, addIcon);

        c.setLayout(new GridBagLayout());
        c.add(titlePane, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST,
                GridBagConstraints.HORIZONTAL, new Insets(0, 0, 2, 0), 0, 0));
        c.add(centerPanel, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST,
                GridBagConstraints.BOTH, new Insets(0, 0, 2, 0), 0, 0));
        c.add(buttonPanel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST,
                GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

        // Default the Topic button to be selected.
        topicButton.setSelected(true);

        pack();
        minWidth = getWidth();
        minHeight = getHeight();

        WindowUtils.setWaitCursor(parent, false);
    }

    public void setVisible(final boolean state, final String mode, final int row) {
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
        // show
        if (state) {
            if (mode.equals(ADD_MODE)) {
                setTitle(" Add New Publisher");
                broker.setEnabled(true);
                broker.setEditable(true);
                broker.setSelectedItem("tcp://localhost:61616");
                topic.setEnabled(true);
                browseButton.setEnabled(true);
                loop.setEnabled(true);
                topic.setSelectedIndex(-1);
                topic.setEnabled(true);
                file.setText("");
                loop.setSelected(false);
                actualButton.setSelected(true);
                faster.setValue(Integer.valueOf(2));
                faster.setEnabled(false);
                slower.setValue(Integer.valueOf(2));
                slower.setEnabled(false);
                constant.setValue(Integer.valueOf(1));
                constant.setEnabled(false);
                actionButton.setText("Add");
            } else {
                if (mode.equals(EDIT_MODE)) {
                    setTitle(" Edit Publisher");
                    final Vector<Object> v = (Vector<Object>) dataRecorder.publisherModel.getDataVector()
                            .elementAt(row);
                    broker.setSelectedItem(((MessagingUtils) v.get(PublisherTableValues.BROKER)).getBrokerFullURL());
                    broker.getModel()
                            .setSelectedItem(((MessagingUtils) v.get(PublisherTableValues.BROKER)).getBrokerFullURL());
                    topic.setSelectedItem(v.get(PublisherTableValues.CHANNEL));
                    topic.getModel().setSelectedItem(v.get(PublisherTableValues.CHANNEL));

                    // file & size
                    final FileObject f = (FileObject) v.get(PublisherTableValues.FILE);
                    if (f != null) {
                        file.setText(f.getFullname());
                        final File myFile = new File(f.getFullname());
                        fileChooser.setCurrentDirectory(myFile);
                    }

                    // loop
                    final String l = (String) v.get(PublisherTableValues.LOOP);
                    if (l.equals(PublisherTableValues.ON)) {
                        loop.setSelected(true);
                    } else {
                        loop.setSelected(false);
                    }

                    // rate
                    final RateObject r = (RateObject) v.get(PublisherTableValues.RATE);
                    faster.setValue(Integer.valueOf(2));
                    faster.setEnabled(false);
                    slower.setValue(Integer.valueOf(2));
                    slower.setEnabled(false);
                    constant.setValue(Integer.valueOf(1));
                    constant.setEnabled(false);
                    if (r.getName().equals(RateObject.ACTUAL)) {
                        actualButton.setSelected(true);
                    } else if (r.getName().equals(RateObject.FASTER)) {
                        fasterButton.setSelected(true);
                        faster.setValue(Integer.valueOf(r.getValue()));
                        faster.setEnabled(true);
                    } else if (r.getName().equals(RateObject.SLOWER)) {
                        slowerButton.setSelected(true);
                        slower.setValue(Integer.valueOf(r.getValue()));
                        slower.setEnabled(true);
                    } else if (r.getName().equals(RateObject.CONSTANT)) {
                        constantButton.setSelected(true);
                        constant.setValue(Integer.valueOf(r.getValue()));
                        constant.setEnabled(true);
                    }

                    // check status
                    final String status = (String) v.get(PublisherTableValues.STATUS);
                    if (!status.equals("Stopped")) {
                        broker.setEnabled(false);
                        topic.setEnabled(false);
                        browseButton.setEnabled(false);
                        clearButton.setEnabled(false);
                        loop.setEnabled(false);
                        topicButton.setEnabled(false);
                        queueButton.setEnabled(false);
                    } else {
                        broker.setEnabled(true);
                        broker.setEditable(true);
                        topic.setEnabled(true);
                        browseButton.setEnabled(true);
                        clearButton.setEnabled(true);
                        loop.setEnabled(true);
                        topicButton.setEnabled(true);
                        queueButton.setEnabled(true);
                    }

                    // Get Type (Queue or Topic)
                    final String type = (String) v.get(PublisherTableValues.TYPE);

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

    public String getLoop() {
        if (loop.isSelected())
            return PublisherTableValues.ON;
        else
            return PublisherTableValues.OFF;
    }

    public RateObject getRate() {
        RateObject rate = null;

        if (isSelected(actualButton)) {
            rate = new RateObject(RateObject.ACTUAL);
            return rate;
        }
        if (isSelected(fasterButton)) {
            final int value = ((Integer) faster.getModel().getValue()).intValue();
            rate = new RateObject(RateObject.FASTER, value);
            return rate;
        }
        if (isSelected(slowerButton)) {
            final int value = ((Integer) slower.getModel().getValue()).intValue();
            rate = new RateObject(RateObject.SLOWER, value);
            return rate;
        }
        if (isSelected(constantButton)) {
            final int value = ((Integer) constant.getModel().getValue()).intValue();
            rate = new RateObject(RateObject.CONSTANT, value);
            return rate;
        }
        return rate;
    }

    public static boolean isSelected(final JRadioButton btn) {
        final DefaultButtonModel model = (DefaultButtonModel) btn.getModel();
        return model.getGroup().isSelected(model);
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
     * Sets the list of topic names of the broker this publisher is connected to. Note JMX must be turned on in the
     * server start script and also, the port for the JMX server must be 1099.
     *
     */
    private void getTopicNames() {
        topic.removeAllItems();
        final List<String> topicNames = this.dataRecorder.getPreloadTopicNames();
        for (int i = 0; i < topicNames.size(); i++) {
            topic.addItem(topicNames.get(i));
        }
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
        setVisible(false);
    }

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

    private class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyTyped(final KeyEvent e) {
            final JTextField field = (JTextField) e.getComponent();
            final int length = field.getText().length();
            final char c = e.getKeyChar();
            if (!(Character.isDigit(c) || (c == KeyEvent.VK_BACK_SPACE) || (c == KeyEvent.VK_DELETE))
                    || (length >= 4)) {
                getToolkit().beep();
                e.consume();
            }
        }
    }

    private class ActualListener implements ActionListener {
        public void actionPerformed(final ActionEvent event) {
            faster.setEnabled(false);
            slower.setEnabled(false);
            constant.setEnabled(false);
        }
    }

    private class FasterListener implements ActionListener {
        public void actionPerformed(final ActionEvent event) {
            faster.setEnabled(true);
            slower.setEnabled(false);
            constant.setEnabled(false);
        }
    }

    private class SlowerListener implements ActionListener {
        public void actionPerformed(final ActionEvent event) {
            faster.setEnabled(false);
            slower.setEnabled(true);
            constant.setEnabled(false);
        }
    }

    private class ConstantListener implements ActionListener {
        public void actionPerformed(final ActionEvent event) {
            faster.setEnabled(false);
            faster.setForeground(Color.RED);
            slower.setEnabled(false);
            constant.setEnabled(true);
        }
    }

    private class CancelListener implements ActionListener {
        public void actionPerformed(final ActionEvent event) {
            cancel();
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
            logger.debug("setting queue to true");
        }
    }

    private class MyActionListener implements ActionListener {
        public void actionPerformed(final ActionEvent event) {
            // Validate
            if ((broker.getSelectedItem() == null) || broker.getSelectedItem().toString().trim().equals("")) {
                JOptionPane.showMessageDialog(PublisherEditor.this, "A valid broker must be selected", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (topic.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(PublisherEditor.this, "A valid topic must be selected", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
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

            final int returnVal = fileChooser.showOpenDialog(PublisherEditor.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                String separator;
                if (fileChooser.getCurrentDirectory().toString().endsWith(System.getProperty("file.separator"))) {
                    separator = "";
                } else {
                    separator = System.getProperty("file.separator");
                }
                file.setText(fileChooser.getCurrentDirectory().toString() + separator
                        + fileChooser.getSelectedFile().getName());
            }
        }
    }

    private class ClearListener implements ActionListener {
        public void actionPerformed(final ActionEvent event) {
            file.setText("");
        }
    }
}
