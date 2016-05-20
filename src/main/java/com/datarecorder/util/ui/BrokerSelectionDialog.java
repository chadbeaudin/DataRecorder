package com.datarecorder.util.ui;

import javax.swing.JDialog;

/**
 *
 */
public class BrokerSelectionDialog extends JDialog {

	private static final long serialVersionUID = 2180169961464943140L;
	//	private JList _list;
	//	private InformationBroker _chosenBroker;
	//	/**
	//	 * @param arg0
	//	 * @param arg1
	//	 * @throws java.awt.HeadlessException
	//	 */
	//	public BrokerSelectionDialog(Frame parent, boolean  modal){
	//		super(parent, modal);
	//        init();
	//        pack();
	//    }
	//
	//    private void init() {
	//        setTitle("Select");
	//
	//        _list = createList();
	//        _list.setFocusable(true);
	//        _list.addMouseListener(new MouseAdapter() {
	//
	//            public void mouseClicked(MouseEvent e) {
	//                if(e.getClickCount()==2) {
	//                    selectBroker();
	//                }
	//            }
	//        });
	//        JScrollPane scroller = new JScrollPane(_list);
	//        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	//        JPanel panel = new JPanel();
	//        panel.setLayout(new BorderLayout());
	//        panel.setBorder(BorderFactory.createTitledBorder(getTitle()));
	//        panel.add(scroller, BorderLayout.CENTER);
	//        getContentPane().setLayout(new BorderLayout());
	//
	//        JPanel buttonPanel = new JPanel();
	//        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
	//        JButton okButton = new JButton("OK");
	//        getRootPane().setDefaultButton(okButton);
	//        buttonPanel.add(okButton);
	//        okButton.addActionListener(new ActionListener() {
	//            public void actionPerformed(ActionEvent e) {
	//                selectBroker();
	//                dispose();
	//            }
	//        });
	//
	//        JButton cancelButton = new JButton("Cancel");
	//        buttonPanel.add(cancelButton);
	//        cancelButton.addActionListener(new ActionListener() {
	//            public void actionPerformed(ActionEvent e) {
	//                _list.setSelectedIndex(-1);
	//                _chosenBroker = null;
	//                dispose();
	//            }
	//        });
	//        getContentPane().add(panel, BorderLayout.CENTER);
	//        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
	//	}
	//    private void selectBroker() {
	//        _chosenBroker = (InformationBroker) _list.getSelectedValue();
	//        if (_chosenBroker == null)
	//            MessageBox.showInfo(
	//        "No information broker selected.",
	//                BrokerSelectionDialog.this);
	//        dispose();
	//    }
	//	/**
	//	 * Looks up the information brokers available and creates them in a list.
	//	 */
	//	private JList createList() {
	//        ServiceFactory sf = ServiceFactory.getFactory();
	//        InformationBroker[] brokers = sf.getDiscoveryService().lookup();
	//        JList list = new JList(brokers);
	//        return list;
	//	}
	//
	//	/**
	//     * Gets the selected unconnected broker.
	//	 * @return the information broker selected.
	//	 */
	//    public InformationBroker getSelectedBroker() {
	//        return _chosenBroker;
	//	}
	//    /**
	//     * Shows the dialog and centers on the parent.
	//     */
	//    public void show() {
	//        WindowUtils.centerWindow(this, this.getParent());
	//        super.show();
	//    }
}
