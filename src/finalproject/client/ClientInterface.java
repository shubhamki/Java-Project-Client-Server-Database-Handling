package finalproject.client;

import java.awt.EventQueue;

import javax.swing.JFrame;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileSystemView;

import finalproject.entities.Person;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.awt.Insets;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * NYU TANDON END TERM PROJECT_ASSIGNMENT
 * 
 * The following class implements the client interface.
 * 
 * @author shubham Ingale
 *
 */


public class ClientInterface extends JFrame {

	DataOutputStream toServer = null;
	DataInputStream fromServer = null;
	private JPanel ControlPanel;
	private JMenu menu;
	private JComboBox peopleSelect;
	private Connection conn;
	private JLabel DBState;
	private JTextArea textAreaForDB;
	private JLabel ConnectionState;
	private JButton QuerDBData;
	PreparedStatement SelectedItemStmt;
	private static final long serialVersionUID = 1L;
	public static final int DEFAULT_PORT = 8001;
	private static final int FRAME_WIDTH = 600;
	private static final int FRAME_HEIGHT = 400;
	final int AREA_ROWS = 10;
	final int AREA_COLUMNS = 40;
	private JFileChooser jFileChooser;
	private Socket socket;
	private int port;
	

	public ClientInterface() {
		setResizable(false);
		setBackground(SystemColor.activeCaption);
		setForeground(SystemColor.activeCaptionText);
		initializeControlPanel();	      
	}


	private void initializeControlPanel() {
		
		this.setBounds(100, 100, FRAME_WIDTH, FRAME_HEIGHT);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		ControlPanel = new JPanel();
		getContentPane().add(ControlPanel, BorderLayout.NORTH);
		ControlPanel.setLayout(null);
		
		DBState = new JLabel("Active DB: <None>");
		DBState.setHorizontalAlignment(SwingConstants.CENTER);
		DBState.setBounds(215, 11, 128, 22);
		ControlPanel.add(DBState);
		
		ConnectionState = new JLabel("Active Connection: <None>");
		ConnectionState.setHorizontalAlignment(SwingConstants.CENTER);
		ConnectionState.setBounds(134, 44, 294, 14);
		ControlPanel.add(ConnectionState);
		
		JButton ConnectButton = new JButton("Open Connection");
		ConnectButton.setHorizontalAlignment(SwingConstants.RIGHT);
		ConnectButton.setBounds(117, 100, 134, 22);
		ConnectButton.addActionListener(new OpenConnectionListener());
		ControlPanel.add(ConnectButton);
		
		JButton btnNewButton = new JButton("Close Connection");
		btnNewButton.setHorizontalAlignment(SwingConstants.LEFT);
		btnNewButton.addActionListener(new CloseConnectionListener());
		btnNewButton.setBounds(316, 100, 134, 22);
		ControlPanel.add(btnNewButton);
		
		peopleSelect = new JComboBox();
		peopleSelect.addItemListener(new ComboBoxItemListener());
		peopleSelect.setBounds(215, 69, 138, 20);
		ControlPanel.add(peopleSelect);
		
		JPanel PanelForTextArea = new JPanel();
		PanelForTextArea.setBounds(10, 170, 574, 169);
		ControlPanel.add(PanelForTextArea);
		PanelForTextArea.setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(0, 0, 574, 169);
		PanelForTextArea.add(scrollPane);
		
		textAreaForDB = new JTextArea();
		textAreaForDB.setEditable(false);
		scrollPane.setViewportView(textAreaForDB);
		
		JButton SendDataButton = new JButton("Send Data");
		SendDataButton.setBounds(134, 133, 117, 23);
		SendDataButton.addActionListener(new SendButtonListener());
		ControlPanel.add(SendDataButton);
		
		QuerDBData = new JButton("Query DB Data");
		QuerDBData.addActionListener(new QueryDBListener());
		QuerDBData.setBounds(316, 133, 117, 23);
		ControlPanel.add(QuerDBData);
		
		JMenuBar menuBar = new JMenuBar();
		menuBar.setForeground(SystemColor.inactiveCaptionText);
		menuBar.setBackground(SystemColor.window);
		setJMenuBar(menuBar);
		
		jFileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
		
		menu = createFileMenu();
		menu.setHorizontalAlignment(SwingConstants.LEFT);
		menuBar.add(menu);
		getContentPane().add(ControlPanel);
		this.setJMenuBar(menuBar);
		
		
		
	}
	
	
	class OpenConnectionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				if(socket == null || socket.isClosed())
				{
					socket = new Socket("localhost", DEFAULT_PORT);
					ConnectionState.setText("Active Connection: "+"localhost "+ DEFAULT_PORT);
				}
			} catch (IOException e1) {
				System.err.println("Exception Caught :" + e1.getMessage());
				e1.printStackTrace();
			} 		
		}
		
	}
	
	class CloseConnectionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			try { 
				socket.close(); 
				ConnectionState.setText("Active Connection: <None>");	
			} catch (Exception e1) {
				System.err.println("Could not close connection (error):" + e1.getMessage()); 
			}

		}
	}
	
	class SendButtonListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			
			try {
				ResultSet rset = SelectedItemStmt.executeQuery();
				ResultSetMetaData rsmd = rset.getMetaData();
				int numColumns = rsmd.getColumnCount();
				String Data[] = new String[numColumns];
				fromServer = new DataInputStream(socket.getInputStream());
				while (rset.next()) 
				{
					for (int i=1;i<=numColumns;i++) 
					{
						Object o = rset.getObject(i);	
						Data[i-1] = o.toString();	
					}
				}				
				ObjectOutputStream OS = new ObjectOutputStream(socket.getOutputStream());
				Person Prsn = new Person(Data[0], Data[1], Data[2], Data[3], Data[4], Data[5]);
				System.out.println(Prsn.toString());
				OS.writeObject(Prsn);
				OS.flush();
				String MyStr = fromServer.readUTF();
				if(MyStr.contentEquals("Success"))
				{
					int ItemSize = peopleSelect.getItemCount();
					if(ItemSize == 1)
					{
						peopleSelect.removeAllItems();
					}
					else
					{
						peopleSelect.removeItemAt(peopleSelect.getSelectedIndex());
					}
						PreparedStatement stmt = conn.prepareStatement("UPDATE People SET sent = ? WHERE id = ?");
					stmt.setBoolean(1, true);
					stmt.setInt(2, Integer.parseInt(Prsn.getID()));
					stmt.executeUpdate();
					QuerDBData.doClick();
					
				}	
				
				
				
			} catch (SQLException | IOException e1) {
				
				System.out.println("Exception Caught: "+ e1.getMessage());
			}
			
		}
		
	}
	
	
	class ComboBoxItemListener implements ItemListener
	{

		@Override
		public void itemStateChanged(ItemEvent e) {
			
			try {
				SelectedItemStmt = conn.prepareStatement("Select * from People WHERE first = ? AND last = ?");
			} catch (SQLException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			if(peopleSelect.getItemCount() > 0) {
				String SelectedItem = peopleSelect.getSelectedItem().toString();
				try {
					SelectedItemStmt.setString(1, SelectedItem.substring(0, SelectedItem.indexOf(" ")));
					SelectedItemStmt.setString(2, SelectedItem.substring(SelectedItem.indexOf(" ")+1));
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
		
	}
	
	public JMenu createFileMenu()
	{
		JMenu menu = new JMenu("File");
		JMenuItem OpenDBMenu = new JMenuItem("Open DB");
		OpenDBMenu.setHorizontalAlignment(SwingConstants.CENTER);
		OpenDBMenu.addActionListener(new OpenDBListener());
		menu.add(OpenDBMenu);

		JMenuItem ExitMenu = new JMenuItem("Exit");
		ExitMenu.addActionListener(e -> {
			if(socket!=null && socket.isConnected())
			{
				try {
					socket.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			System.exit(0);});
		menu.add(ExitMenu);
		return menu;
	}
	
	class OpenDBListener implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{
			int returnVal = jFileChooser.showOpenDialog(getParent());
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				System.out.println("You chose to open this file: " + jFileChooser.getSelectedFile().getAbsolutePath());
				String dbFileName = jFileChooser.getSelectedFile().getAbsolutePath();
				try {
					connectToDB(dbFileName);
					DBState.setText("Active DB: "+dbFileName.substring(dbFileName.lastIndexOf("\\")+1));
					clearComboBox();
					fillComboBox();

				} catch (Exception e ) {
					System.err.println("error connection to db: "+ e.getMessage());
					e.printStackTrace();
					DBState.setText("Active DB: <None>");
					try {
						fillComboBox();
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}

			}
		}     
	}

	 
	 class QueryDBListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			try
			{
				textAreaForDB.setText("");
				PreparedStatement stmt = conn.prepareStatement("Select * from People");
				ResultSet rset = stmt.executeQuery();
				ResultSetMetaData rsmd = rset.getMetaData();
				int numColumns = rsmd.getColumnCount();
				String rowString = "";
				for(int i = 1; i<=numColumns;i++)
				{
					rowString += rsmd.getColumnName(i) + "\t";
				}
				rowString += "\n";
				System.out.println("numcolumns is "+ numColumns);
				while (rset.next()) 
				{
					for (int i=1;i<=numColumns;i++) 
					{
						Object o = rset.getObject(i);	
						rowString += o.toString() + "\t";	
					}
					rowString += "\n";
				}
				System.out.print("rowString  is  " + rowString);
				textAreaForDB.setText(rowString);
			}
			catch (SQLException e1) {
				
				System.err.println(e1.getMessage());
			}
			
		}
		 
	 }
	public static void main(String[] args) {
		
		
		ClientInterface Ci = new ClientInterface();
		Ci.setVisible(true);
		
	}


	public void clearComboBox() {
		
		peopleSelect.removeAllItems();
		
	}


	public void connectToDB(String dbFileName) throws SQLException {
		
		conn = DriverManager.getConnection("jdbc:sqlite:" + dbFileName);
	}


	public void fillComboBox() throws SQLException {
		
		peopleSelect.removeAllItems();
		PreparedStatement theNamesListQuery = conn.prepareStatement("Select First, Last from People");
		
		ResultSet Rset = theNamesListQuery.executeQuery();
		
		ResultSetMetaData rsmd = Rset.getMetaData();
		int numColumns = rsmd.getColumnCount();
	
		String rowString = "";
		while(Rset.next()) 
		{
			for (int i=1;i<=numColumns;i++) 
			{
				Object o = Rset.getObject(i);
				if( i != numColumns)
				{
					rowString += o.toString() + " ";
				}
				else
				{
					rowString += o.toString();
				}
			}
			peopleSelect.addItem(rowString);
			rowString = "";
		}

	}
}
