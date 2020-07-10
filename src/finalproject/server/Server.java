package finalproject.server;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileSystemView;

import finalproject.entities.Person;

import javax.swing.JLabel;

/**
 * 
 * NYU TANDON END TERM PROJECT_ASSIGNMENT
 * 
 * The Following class implements the Server Interface according to the requirements Provided
 * 
 * @author shubham Ingale
 *
 */



public class Server extends JFrame implements Runnable {
	
	private JPanel ControlPanel;
	private Connection conn;
	private JTextArea DBAndStatusArea;
	private JLabel DBState;
	DataOutputStream toClient = null;
	public static final int DEFAULT_PORT = 8001;
	private static final int FRAME_WIDTH = 600;
	private static final int FRAME_HEIGHT = 800;
	final int AREA_ROWS = 10;
	final int AREA_COLUMNS = 40;
	private JFrame frame;
	private JFileChooser jFileChooser;
	private int clientNo = 0;

	public static void main(String[] args) {
		
		Server MyServer = new Server();
		MyServer.setVisible(true);
		
	}

	public Server() {
		setResizable(false);
		initialize();
		Thread t = new Thread(this);
		t.start();
	}

	private void initialize() {

		this.setBounds(100, 100, FRAME_WIDTH, FRAME_HEIGHT);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JMenuBar menuBar = new JMenuBar();
		menuBar.setBackground(SystemColor.window);
		this.setJMenuBar(menuBar);
		
		JMenu FileMenu = createFileMenu();
		menuBar.add(FileMenu);
			
		ControlPanel = new JPanel();
		getContentPane().add(ControlPanel, BorderLayout.CENTER);
		ControlPanel.setLayout(null);
		
		JButton QueryButton = new JButton("Query DB");
		QueryButton.setBounds(235, 57, 107, 23);
		QueryButton.addActionListener(new QueryDBListener());
		ControlPanel.add(QueryButton);
		
		JPanel OutputPanel = new JPanel();
		OutputPanel.setBounds(0, 122, 584, 618);
		ControlPanel.add(OutputPanel);
		OutputPanel.setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 0, 574, 618);
		OutputPanel.add(scrollPane);
		
		jFileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
		
		DBAndStatusArea = new JTextArea();
		DBAndStatusArea.setEditable(false);
		scrollPane.setViewportView(DBAndStatusArea);
		
		DBState = new JLabel("DB: <None>");
		DBState.setHorizontalAlignment(SwingConstants.CENTER);
		DBState.setBounds(235, 11, 107, 14);
		ControlPanel.add(DBState);
		
		
	}
	
	public JMenu createFileMenu()
	{
		JMenu menu = new JMenu("File");
		JMenuItem OpenDBMenu = new JMenuItem("Open DB");
		OpenDBMenu.setHorizontalAlignment(SwingConstants.CENTER);
		OpenDBMenu.addActionListener(new OpenDBListener());
		menu.add(OpenDBMenu);

		JMenuItem ExitMenu = new JMenuItem("Exit");
		ExitMenu.addActionListener(e -> {System.exit(0);});
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
					DBState.setText("DB: "+dbFileName.substring(dbFileName.lastIndexOf("\\")+1));
				} catch (Exception e ) {
					System.err.println("error connection to db: "+ e.getMessage());
					e.printStackTrace();
					DBState.setText("DB: <None>");
				}

			}
		}     
	}
	
	class QueryDBListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			try
			{
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
				for(int i = 1; i<=numColumns;i++)
				{
					rowString += "---" + "\t";
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
				DBAndStatusArea.append(rowString);
			}
			catch (SQLException e1) {
				
				System.err.println(e1.getMessage());
			}
			
		}
		 
	 }

	public void connectToDB(String dbFileName) throws SQLException {
		
		conn = DriverManager.getConnection("jdbc:sqlite:" + dbFileName);
	}


	@Override
	public void run() {
		try
		{
			ServerSocket serverSocket = new ServerSocket(DEFAULT_PORT);
			DBAndStatusArea.append("MultiThreadServer started at "+(new Date())  + '\n');
			
			while(true)
			{
				// Listener Socket
				Socket socket = serverSocket.accept();

				// Increment ClientNo
				clientNo++;
				
				DBAndStatusArea.append("Starting thread for client " + clientNo +
			              " at " + new Date() + '\n');
				
				// Find the client's host name, and IP address
	            InetAddress inetAddress = socket.getInetAddress();
	            DBAndStatusArea.append("Client " + clientNo + "'s host name is "
	              + inetAddress.getHostName() + "\n");
	            DBAndStatusArea.append("Client " + clientNo + "'s IP Address is "
	              + inetAddress.getHostAddress() + "\n");
	            
	         // Create and start a new thread for the connection
	          new Thread(new HandleAClient(socket, clientNo)).start();
				
			}
			
		}
		catch(IOException e)
		{
			System.err.println(e.getMessage());
		}	
	}

	
	// Define the thread class for handling new connection
	class HandleAClient implements Runnable {
		private Socket socket; // A connected socket
		private int clientNum;

		/** Construct a thread */
		public HandleAClient(Socket socket, int clientNum) {
			this.socket = socket;
			this.clientNum = clientNum;
		}

		/** Run a thread */
		public void run() {
			try {
				
				ObjectInputStream OS = null;
				toClient = new DataOutputStream(socket.getOutputStream());
				
				while (socket.isConnected()) {
					
					OS = new ObjectInputStream(socket.getInputStream());
					Person Prsn = (Person) OS.readObject();
					
				
					if(conn !=null && !conn.isClosed())
					{
						PreparedStatement stmt = conn.prepareStatement("INSERT INTO People(First, Last, age, city, sent, Id) VALUES( ?, ?, ?, ?, ?, ?)");
						stmt.setString(1, Prsn.getFirstName());
						stmt.setString(2, Prsn.getLastName());
						stmt.setInt(3, Integer.parseInt(Prsn.getAge()));
						stmt.setString(4, Prsn.getCity());
						if(!Prsn.getSent().contentEquals("0"))
						{
							stmt.setInt(5, Integer.parseInt(Prsn.getSent()));
						}
						else
						{
							stmt.setInt(5, Integer.parseInt("1"));
						}
						stmt.setInt(6, Integer.parseInt(Prsn.getID()));
						stmt.executeUpdate();
						toClient.writeUTF("Success");
						toClient.flush();
						DBAndStatusArea.append("Successfully added to db\n");
						DBAndStatusArea.append("Person received from client: " + this.clientNum + "\n" +
								Prsn.toString() + '\n');
					}
					else
					{
						toClient.writeUTF("Failed");
						toClient.flush();
						DBAndStatusArea.append("Failed\n");
					}
					
				}
				
				DBAndStatusArea.append("Client"+this.clientNum +"Closed \n");
				
			}
			catch(IOException | ClassNotFoundException ex) {
				try {
					toClient.writeUTF("Failed");
					toClient.flush();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				System.out.println("IO Error : " + ex.getMessage() + " Connection ended");
				DBAndStatusArea.append("Client "+this.clientNum +" Connection Endded \n");
				clientNo--;
				//ex.printStackTrace();
			} catch (SQLException | NullPointerException e) {
				// TODO Auto-generated catch block
				try {
					toClient.writeUTF("Failed");
					toClient.flush();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}		
				System.out.println("Null Pointer Exception:" + e.getMessage());
			}

		}
	}

	
}
