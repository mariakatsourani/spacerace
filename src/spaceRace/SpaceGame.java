package spaceRace;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class SpaceGame implements Runnable {
   // Connect status constants
   public final static int NULL          = 0;
   public final static int DISCONNECTED  = 1;
   public final static int DISCONNECTING = 2;
   public final static int CONNECTING = 3;
   public final static int CONNECTED     = 4;

   // Message constants
   public final static String statusMessages[] = {
      " Could not connect!", 
      " Disconnected",
      " Disconnecting...", 
      " Connecting...", 
      " Connected"
   };
   public final static SpaceGame spaceGame = new SpaceGame();//creating a new thread (because implements runnable)
   public final static String END_CONN = "0";
   private static JPanel spaceShips;//JPanel that contains the spaceships
   
   // Connection state info
   public static String hostIP = "localhost";
   public static int port      = 1234;
   public static int delayCounter;
   public static int curState = DISCONNECTED;
   public static boolean isHost = true;
   public static String statusString = statusMessages[curState];
   public static StringBuffer toSend  = new StringBuffer("");

   // Various GUI components and info
   public static JFrame mainFrame = null;
   public static JPanel statusBar = null;
   public static JLabel statusField = null;
   public static JTextField statusColor = null;
   public static JTextField ipField = null;
   public static JTextField portField = null;
   public static JRadioButton hostOption = null;
   public static JRadioButton guestOption = null;
   public static JButton connectButton = null;
   public static JButton disconnectButton = null;

   // TCP Components
   public static ServerSocket hostServer = null;
   public static Socket socket = null;
   public static BufferedReader in = null;
   public static PrintWriter   out = null;

   /////////////////////////////////////////////////////////////////

   private static JPanel initOptionsPane() {//JPanel for the IP field
      ActionAdapter buttonListener = null;
      JPanel optionsPane = new JPanel(new GridLayout(4, 1));
      JPanel ipPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      ipPanel.add(new JLabel("Host IP:"));
      ipField = new JTextField(10); 
      ipField.setText(hostIP);
      ipField.setEnabled(false);
      ipField.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
               ipField.selectAll();
               if(curState != DISCONNECTED) {
                  changeStatusNTS(NULL, true);
               }else{
                  hostIP = ipField.getText();
               }
            }
      });
      ipPanel.add(ipField);
      optionsPane.add(ipPanel);
      
      // JPanel for port input
      JPanel portPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      portPanel.add(new JLabel("Port:"));
      portField = new JTextField(10); portField.setEditable(true);
      portField.setText((new Integer(port)).toString());
      portField.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
               // should be editable only when disconnected
               if(curState != DISCONNECTED) {
                  changeStatusNTS(NULL, true);
               }else{
                  int temp;
                  try{
                     temp = Integer.parseInt(portField.getText());
                     port = temp;
                  }catch(NumberFormatException nfe) {
                     portField.setText((new Integer(port)).toString());
                     mainFrame.repaint();
                  }
               }
            }
      });
      portPanel.add(portField);
      optionsPane.add(portPanel);

      // Host/guest option
      buttonListener= new ActionAdapter() {
            public void actionPerformed(ActionEvent e) {
               if(curState != DISCONNECTED) {
                  changeStatusNTS(NULL, true);
               }else{//if disconnected enable ipField to connect again whether host or guest
                  isHost = e.getActionCommand().equals("host");
                  // Cannot give host IP if host  is chosen
                  if(isHost) {
                     ipField.setEnabled(false);
                     ipField.setText("localhost");
                     hostIP = "localhost";
                  }else{
                     ipField.setEnabled(true);
                  }
               }
            }
      };
      //creating the JRadioButtons for the host/guest option
      ButtonGroup bg= new ButtonGroup();
      hostOption = new JRadioButton("Host", true);
      hostOption.setActionCommand("host");
      hostOption.addActionListener(buttonListener);
      
      guestOption = new JRadioButton("Guest", false);
      guestOption.setActionCommand("guest");
      guestOption.addActionListener(buttonListener);
      bg.add(hostOption);//adding them to the ButtonGroup
      bg.add(guestOption);
      JPanel radioPanel = new JPanel(new GridLayout(1, 2));//creating the JPanel to add the ButtonGroups 
      radioPanel.add(hostOption);
      radioPanel.add(guestOption);
      optionsPane.add(radioPanel);

      // Connect and disconnect buttons
      JPanel buttonPane = new JPanel(new GridLayout(1, 2));
      buttonListener = new ActionAdapter() {
            public void actionPerformed(ActionEvent e) {
               if(e.getActionCommand().equals("connect")) {
                  changeStatusNTS(CONNECTING, true);
               }else{
                  changeStatusNTS(DISCONNECTING, true);
               }
            }
      };
      connectButton = new JButton("Connect");
      connectButton.setActionCommand("connect");
      connectButton.addActionListener(buttonListener);
      connectButton.setEnabled(true);
      
      disconnectButton = new JButton("Disconnect");
      disconnectButton.setActionCommand("disconnect");
      disconnectButton.addActionListener(buttonListener);
      disconnectButton.setEnabled(false);
      buttonPane.add(connectButton);
      buttonPane.add(disconnectButton);
      optionsPane.add(buttonPane);
      return optionsPane;
   }

   // Initialize all the GUI components and display the frame
   private static void initGUI() {
      // Set up the status bar
      statusField = new JLabel();
      statusField.setText(statusMessages[DISCONNECTED]);
      statusColor = new JTextField(1);
      statusColor.setBackground(Color.red);
      statusColor.setEditable(false);
      statusBar = new JPanel(new BorderLayout());
      statusBar.add(statusColor, BorderLayout.WEST);
      statusBar.add(statusField, BorderLayout.CENTER);
      // Set up the options pane
      JPanel optionsPane = new JPanel();
      optionsPane.add(initOptionsPane());
      // Set up the Race
      spaceShips= new SpaceShips();
      // Set up the main pane
      JPanel mainPane = new JPanel(new BorderLayout());
      mainPane.add(statusBar, BorderLayout.SOUTH);
      mainPane.add(optionsPane, BorderLayout.NORTH);
      mainPane.add(spaceShips, BorderLayout.CENTER);
      // Set up the main frame
      mainFrame = new JFrame("Space Racing Game");
      mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      mainFrame.setContentPane(mainPane);
      mainFrame.setSize(mainFrame.getPreferredSize());
      mainFrame.setLocation(0, 0);
      mainFrame.pack();
      mainFrame.setVisible(true);
   }

   // The Thread-Safe way to change the GUI components while changing state
   private static void changeStatusTS(int newState, boolean noError) {
      if(newState != NULL) {
         curState = newState;
      }
      if(noError) {
         statusString= statusMessages[curState]; 	//display status message
      }else {
         statusString= statusMessages[NULL]; 		//display error message
      }
      SwingUtilities.invokeLater(spaceGame);	//post a "job" to Swing that will run on the event dispatch thread when possible
   }

   // The Non-Thread-Safe way to change the GUI while changing state
   private static void changeStatusNTS(int newState, boolean noError) {
      if(newState != NULL) {
         curState= newState;
      }
      if(noError) {
         statusString = statusMessages[curState];	//display status message
      }else{
         statusString = statusMessages[NULL]; 		//display error message
      }
      spaceGame.run(); // call run method from current thread
   }

   // Add text to send-buffer
   public static void sendString(String s) {
      synchronized (toSend) {
         toSend.append(s + "\n");
      }
   }

   // Cleanup for disconnect
   private static void cleanUp() {
      try {
         if (hostServer != null) {
            hostServer.close();
            hostServer = null;
         }
      }catch (IOException e) { hostServer = null; }

      try {
         if (socket != null) {
            socket.close();
            socket = null;
         }
      }catch (IOException e) { socket = null; }

      try {
         if (in != null) {
            in.close();
            in = null;
         }
      }catch (IOException e) { in = null; }

      if (out != null) {
         out.close();
         out = null;
      }
   }

   // Checks the current state and adjust GUI according to state
   public void run() {
      switch (curState) {
      case DISCONNECTED:
         connectButton.setEnabled(true);//enable buttons are radio buttons to connect again
         disconnectButton.setEnabled(false);
         if(!isHost)ipField.setEnabled(true);
         portField.setEnabled(true);
         hostOption.setEnabled(true);
         guestOption.setEnabled(true);
         statusColor.setBackground(Color.red);
         break;

      case DISCONNECTING:
         connectButton.setEnabled(false);//lock buttons are radio buttons while disconnecting
         disconnectButton.setEnabled(false);
         ipField.setEnabled(false);
         portField.setEnabled(false);
         hostOption.setEnabled(false);
         guestOption.setEnabled(false);
         statusColor.setBackground(Color.orange);
         break;

      case CONNECTED:
         connectButton.setEnabled(false);//disable buttons are radio buttons when connected
         disconnectButton.setEnabled(true);
         ipField.setEnabled(false);
         portField.setEnabled(false);
         hostOption.setEnabled(false);
         guestOption.setEnabled(false);
         statusColor.setBackground(Color.green);
         break;

      case CONNECTING:
         connectButton.setEnabled(false);//lock buttons are radio buttons while connecting
         disconnectButton.setEnabled(false);
         ipField.setEnabled(false);
         portField.setEnabled(false);
         hostOption.setEnabled(false);
         guestOption.setEnabled(false);
         statusColor.setBackground(Color.orange);
         break;
      }

      // button states are consistent with states
      ipField.setText(hostIP);
      portField.setText((new Integer(port)).toString());
      hostOption.setSelected(isHost);
      guestOption.setSelected(!isHost);
      statusField.setText(statusString);
      mainFrame.repaint();
   }

   //main
   public static void main(String args[]) {
      String s;
      initGUI();
      while(true) {
         try { // poll every 10 ms
            Thread.sleep(10);
         }catch (InterruptedException e) {}
         
         switch(curState) {
         case CONNECTING:
            try {
               if(isHost) {	// try to set up the server if host
                  hostServer = new ServerSocket(port);
                  socket = hostServer.accept();
               }else{		// try to connect to the server if guest
                  socket = new Socket(hostIP, port);
               }
               in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
               out= new PrintWriter(socket.getOutputStream(), true);
               changeStatusTS(CONNECTED, true);
            }catch(IOException e) {	// if error, clean up and output an error message
               cleanUp();
               changeStatusTS(DISCONNECTED, false);
            }
            ((SpaceShips)spaceShips).setM(1);
            ((SpaceShips)spaceShips).startup();
            break;

         case CONNECTED:
            try {
               if(toSend.length() != 0) {//send data 
                  out.print(toSend); out.flush();
                  toSend.setLength(0);
                  changeStatusTS(NULL, true);
               }
               if(in.ready()) {// receive data
                  s = in.readLine();
                  if((s != null) &&  (s.length() != 0)) {
                     if(s.equals(END_CONN)||((SpaceShips)spaceShips).isShipCollision()) {
                    	 ((SpaceShips)spaceShips).setM(2);
                        changeStatusTS(DISCONNECTING, true);
                     }else{
                        Scanner in = new Scanner(s);
                        if(isHost){//using the Scanner get the variables of the other spaceship
                        	((SpaceShips)spaceShips).setX2(in.nextInt());
                        	((SpaceShips)spaceShips).setY2(in.nextInt());
                        	((SpaceShips)spaceShips).setA2(in.nextDouble());
                        	((SpaceShips)spaceShips).setV2(in.nextDouble());
	                        
                        }else{
                        	((SpaceShips)spaceShips).setX1(in.nextInt());
                        	((SpaceShips)spaceShips).setY1(in.nextInt());
                        	((SpaceShips)spaceShips).setA1(in.nextDouble());
                        	((SpaceShips)spaceShips).setV1(in.nextDouble());
	                        
                        }
                        
                        in.close();
                        changeStatusTS(NULL, true);
                     }
                  }
               }
            }catch(IOException e) {
               cleanUp();
               changeStatusTS(DISCONNECTED, false);
            }
            break;

         case DISCONNECTING:
            out.print(END_CONN); out.flush(); //tell opponent to disconnect
            cleanUp();
            changeStatusTS(DISCONNECTED, true);
            ((SpaceShips)spaceShips).setV1(0);
            ((SpaceShips)spaceShips).setV2(0);
            delayCounter=0;
            break;
            
         case DISCONNECTED:
        	 if(delayCounter++>1000) ((SpaceShips)spaceShips).setM(0);
        	 break;
         default: break; // do nothing
         }
      }
   }
   
   public static boolean isConnected(){
	   return curState==CONNECTED;
   }
}

// action adapter used event-listener coding
class ActionAdapter implements ActionListener {
   public void actionPerformed(ActionEvent e) {}
}

