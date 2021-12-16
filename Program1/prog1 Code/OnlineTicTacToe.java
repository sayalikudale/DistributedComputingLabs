import com.jcraft.jsch.*;

import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.Console;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.util.Random;

/**
 * @author Sayali Kudale
 */

public class OnlineTicTacToe implements ActionListener {
    private final int INTERVAL = 1000; // 1 second
    private final int NBUTTONS = 9; // #buttons
    private ObjectInputStream input = null; // input from my counterpart
    private ObjectOutputStream output = null; // output from my counterpart
    private JFrame window = null; // the tic-tac-toe window
    private JButton[] button = new JButton[NBUTTONS]; // button[0] - button[9]
    private boolean[] myTurn = new boolean[1]; // T: my turn, F: your turn
    private String myMark = null; // "O" or "X"
    private String yourMark = null; // "X" or "O"
    ArrayList<Integer> filledCells;
    private boolean isGameOver = false;

    /**
     * Prints out the usage.
     */
    private static void usage() {
        System.err.
                println("Usage: java OnlineTicTacToe ipAddr ipPort(>=5000) [auto]");
        System.exit(-1);
    }

    /**
     * Prints out the track trace upon a given error and quits the application.
     *
     * @param an exception
     */
    private static void error(Exception e) {
        e.printStackTrace();
        System.exit(-1);
    }

    /**
     * Starts the online tic-tac-toe game.
     *
     * @param args[0]: my counterpart's ip address, args[1]: his/her port, (arg[2]: "auto")
     *                 if args.length == 0, this Java program is remotely launched by JSCH.
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            // if no arguments, this process was launched through JSCH
            try {
                OnlineTicTacToe game = new OnlineTicTacToe();
            } catch (IOException e) {
                error(e);
            }
        } else {
            // this process wa launched from the user console.
            // verify the number of arguments
            if (args.length != 2 && args.length != 3) {
                System.err.println("args.length = " + args.length);
                usage();
            }
            // verify the correctness of my counterpart address
            InetAddress addr = null;
            try {
                addr = InetAddress.getByName(args[0]);
            } catch (UnknownHostException e) {
                error(e);
            }
            // verify the correctness of my counterpart port
            int port = 0;
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                error(e);
            }
            if (port < 5000) {
                usage();
            }
            // check args[2] == "auto"
            if (args.length == 3 && args[2].equals("auto")) {
                // auto play
                OnlineTicTacToe game = new OnlineTicTacToe(args[0]);
            } else {
                // interactive play
                OnlineTicTacToe game = new OnlineTicTacToe(addr, port);
            }
        }
    }

    /**
     * Is the constructor that is remote invoked by JSCH. It behaves as a server.
     * The constructor uses a Connection object for communication with the client.
     * It always assumes that the client plays first.
     */
    public OnlineTicTacToe() throws IOException {
        filledCells = new ArrayList<>();
        myMark = "X"; // auto player is always the 2nd.
        yourMark = "O";

        // receive an ssh2 connection from a user-local master server.
        Connection connection = new Connection();
        input = connection.in;
        output = connection.out;


        PrintWriter logs = new PrintWriter(new FileOutputStream("logs.txt"));
        logs.println("Autoplay: got started.");
        logs.flush();
        // the main body of auto play.
        // start my counterpart thread
        logs.println("Current turn Value:" + myTurn[0]);
        logs.flush();

        CounterpartAuto counterpart = new CounterpartAuto();
        counterpart.start();


        synchronized (myTurn) {
            //initially turn is false for auto player hence wait till turn value to change
            try {
                myTurn.wait();
            } catch (InterruptedException e) {
                logs.println("Error while starting the auto player" + e.getMessage());
                logs.flush();
            }
            logs.println("Wait is over");
            logs.flush();

            //while loop for 9 turns
            int i = 1;
            while (i < 10) {

                if (myTurn[0]) {

                    Random rn = new Random();
                    //initialize to 10 initally
                    int nextMove = 10;
                    while (nextMove == 10 || filledCells.contains(nextMove))
                        nextMove = rn.nextInt(9 - 1 + 0) + 0;

                    logs.println("next move:" + nextMove);
                    logs.flush();
                    output.writeObject(nextMove);
                    output.flush();
                    filledCells.add(nextMove);
                    myTurn[0] = false;
                    myTurn.notifyAll();
                    logs.println("turn changed:" + myTurn[0]);
                    logs.flush();
                    i++;

                } else {
                    try {
                        myTurn.wait();
                    } catch (Exception e) {
                        logs.println("Error while waiting in the auto player" + e.getMessage());
                        logs.flush();

                    }
                }

            }
        }
    }

    /**
     * Is the constructor that, upon receiving the "auto" option,
     * launches a remote OnlineTicTacToe through JSCH. This
     * constructor always assumes that the local user should play
     * first. The constructor uses a Connection object for
     * communicating with the remote process.
     *
     * @param my auto counter part's ip address
     */
    public OnlineTicTacToe(String hostname) {
        final int JschPort = 22; // Jsch IP port
        filledCells = new ArrayList<>();

        // Read username, password, and a remote host from keyboard
        Scanner keyboard = new Scanner(System.in);
        String username = null;
        String password = null;
        // establish an ssh2 connection to ip and run
        // Server there.

        try {
            // read the user name from the console
            System.out.print("User: ");
            username = keyboard.nextLine();
            // read the password from the console
            Console console = System.console();
            password = new String(console.readPassword("Password: "));

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }


        String cur_dir = System.getProperty("user.dir");

        String command
                = "java -cp " + cur_dir + "/jsch-0.1.54.jar:" + cur_dir +
                " OnlineTicTacToe";

        Connection connection = new Connection(username, password,
                hostname, command);
        // the main body of the master server
        input = connection.in;
        output = connection.out;


        // set up a window
        makeWindow(true); // I'm a former
        // start my counterpart thread
        Counterpart counterpart = new Counterpart();
        counterpart.start();


    }

    /**
     * Is the constructor that sets up a TCP connection with my counterpart,
     * brings up a game window, and starts a slave thread for listenning to
     * my counterpart.
     *
     * @param my counterpart's ip address
     * @param my counterpart's port
     */
    public OnlineTicTacToe(InetAddress addr, int port) {
        // set up a TCP connection with my counterpart
        // Prepare a server socket and make it non-blocking
        ServerSocket server = null;
        Socket client = null;
        try {
            InetAddress localHost=InetAddress.getLocalHost();
            String hostname=addr.getHostName();
            server = new ServerSocket(port);
            if(!(localHost==addr || hostname.contains("localhost"))) {
                System.out.println("set SO TimeOut");
                server.setSoTimeout(INTERVAL);
            }
            System.out.println("serverSocket created");
        } catch (BindException be) {
            System.out.println(be.getMessage());
            try {
                client = new Socket(addr, port);
                makeWindow(false);
                System.out.println("I am client");

            } catch (IOException ioe) {
                error(ioe);
            }

            if (client != null)
                // Exchange a message with my counter part.
                try {
                    System.out.println("TCP connection established...");
                    output = new ObjectOutputStream(client.getOutputStream());

                    input = new ObjectInputStream(client.getInputStream());

                } catch (Exception ie) {
                    error(ie);
                }
        }catch (Exception e){
              error(e);
        }

        if (client == null) {
            // While accepting a remote request, try to send my connection request
            while (true) {
                try {
                    client = server.accept();
                    makeWindow(true);
                    System.out.println("I am server");

                } catch (SocketTimeoutException ste) {
                    // Couldn't receive a connection request withtin INTERVAL
                } catch (IOException ioe) {
                    error(ioe);
                }
                // Check if a connection was established. If so, leave the loop
                if (client != null) {
                    System.out.println("Connection was established.leaving the loop");
                    break;
                }

                try {
                    client = new Socket(addr, port);
                    makeWindow(false);
                    System.out.println("I am client");

                } catch (IOException ioe) {
                    error(ioe);
                }

                // Check if a connection was established, If so, leave the loop
                if (client != null)
                    break;

            }

            // Check if a connection was established. If so, leave the loop
            if (client != null)
                // Exchange a message with my counter part.
                try {
                    System.out.println("TCP connection established...");
                    output = new ObjectOutputStream(client.getOutputStream());

                    input = new ObjectInputStream(client.getInputStream());

                } catch (Exception e) {
                    error(e);
                }
        }

        // start my counterpart thread
        Counterpart counterpart = new Counterpart();
        counterpart.start();
    }

    /**
     * Creates a 3x3 window for the tic-tac-toe game
     *
     * @param true if this window is created by the former, (i.e., the
     *             person who starts first. Otherwise false.
     */
    private void makeWindow(boolean amFormer) {
        myTurn[0] = amFormer;
        myMark = (amFormer) ? "O" : "X"; // 1st person uses "O"
        yourMark = (amFormer) ? "X" : "O"; // 2nd person uses "X"
        // create a window
        window = new JFrame("OnlineTicTacToe(" +
                ((amFormer) ? "former)" : "latter)") + myMark);
        window.setSize(300, 300);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setLayout(new GridLayout(3, 3));
        // initialize all nine cells.
        for (int i = 0; i < NBUTTONS; i++) {
            button[i] = new JButton();
            window.add(button[i]);
            button[i].addActionListener(this);
        }
        // make it visible
        window.setVisible(true);
    }

    /**
     * Marks the i-th button with mark ("O" or "X")
     *
     * @param the  i-th button
     * @param a    mark ( "O" or "X" )
     * @param true if it has been marked in success
     */
    private boolean markButton(int i, String mark) {
        if (button[i].getText().equals("")) {
            button[i].setText(mark);
            button[i].setEnabled(false);
            return true;
        }
        return false;
    }

    /**
     * Checks which button has been clicked
     *
     * @param an event passed from AWT
     * @return an integer (0 through to 8) that shows which button has been
     * clicked. -1 upon an error.
     */
    private int whichButtonClicked(ActionEvent event) {
        for (int i = 0; i < NBUTTONS; i++) {
            if (event.getSource() == button[i])
                return i;
        }
        return -1;
    }

    /**
     * Checks if the i-th button has been marked with mark( "O" or "X" ).
     *
     * @param the i-th button
     * @param a   mark ( "O" or "X" )
     * @return true if the i-th button has been marked with mark.
     */
    private boolean buttonMarkedWith(int i, String mark) {
        return button[i].getText().equals(mark);
    }

    /**
     * Pops out another small window indicating that mark("O" or "X") won!
     *
     * @param a mark ( "O" or "X" )
     */
    private void showWon(String mark) {
        JOptionPane.showMessageDialog(null, mark + " won!");
    }

    /**
     * Is called by AWT whenever any button has been clicked. You have to:
     * <ol>
     * <li> check if it is my turn,
     * <li> check which button was clicked with whichButtonClicked( event ),
     * <li> mark the corresponding button with markButton( buttonId, mark ),
     * <li> send this informatioin to my counterpart,
     * <li> checks if the game was completed with
     * buttonMarkedWith( buttonId, mark )
     * <li> shows a winning message with showWon( )
     */
    public void actionPerformed(ActionEvent event) {
        synchronized (myTurn) {
            if (myTurn[0] && !isGameOver) {
                int actionId = whichButtonClicked(event);
                markButton(actionId, myMark);
                try {
                    output.writeObject(actionId);
                    output.flush();
                } catch (Exception e) {

                }

                if (checkWinner()) {
                    showWon(myMark);
                    isGameOver = true;
                } else {
                    myTurn[0] = false;
                    myTurn.notifyAll();
//                    System.out.println("Current turn :" + myTurn[0]);
                }
            } else {
//                System.out.println("Waiting  foe the turn :" + myTurn[0]);
                try {
                    myTurn.wait();

                } catch (InterruptedException e) {

                }
            }
        }

//        } catch (Exception e) {
//
//        }
    }

    /**
     * This is a reader thread that keeps reading fomr and behaving as my
     * counterpart.
     */
    private class Counterpart extends Thread {
        /**
         * Is the body of the Counterpart thread.
         */
        @Override
        public void run() {
            while (true) {
                synchronized (myTurn) {
//                    System.out.println("turn: " + myTurn[0]);
                    if (!myTurn[0]) {
                        try {
                            int actionId = (int) (input.readObject());
                            System.out.println("received message from " + yourMark + " " + actionId);
                            markButton(actionId, yourMark);

                            if (checkWinner()) {
                                showWon(yourMark);
                                break;
                            }
                            myTurn[0] = true;

                            myTurn.notifyAll();
                        } catch (Exception e) {

                        }
                    } else {
                        try {
                            System.out.println("Waiting for the turn");
                            myTurn.wait();

                        } catch (InterruptedException e) {

                        }
                    }

                }

            }
        }
    }

    /**
     * This is a reader thread that keeps reading fomr and behaving as my
     * counterpart for the Robot.
     */
    private class CounterpartAuto extends  Thread {
        @Override
        public void run() {
            try {
                PrintWriter logs = new PrintWriter(new FileOutputStream("logs3.txt"));

                while (true) {
                    synchronized (myTurn) {
                        try {
                            logs.println("turn: " + myTurn[0]);
                            if (!myTurn[0]) {
                                int actionId = (int) (input.readObject());
                                filledCells.add(actionId);

                                logs.println("action id: " + actionId);
                                logs.flush();

                                myTurn[0] = true;
                                myTurn.notifyAll();
                            } else {
                                myTurn.wait();
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            System.exit(-1);
                        }
                    }
                }
            } catch (Exception e) {

            }
        }
    }

    private boolean threeEqual(String a, String b, String c) {
        boolean isEqual = false;

        if (a != "" && a == b && a == c)
            isEqual = true;

        return isEqual;
    }

    public boolean checkWinner() {
        if (threeEqual(button[0].getText(), button[1].getText(), button[2].getText()))
            return true;
        if (threeEqual(button[3].getText(), button[4].getText(), button[5].getText()))
            return true;
        if (threeEqual(button[0].getText(), button[3].getText(), button[6].getText()))
            return true;
        if (threeEqual(button[1].getText(), button[4].getText(), button[7].getText()))
            return true;
        if (threeEqual(button[2].getText(), button[5].getText(), button[8].getText()))
            return true;
        if (threeEqual(button[0].getText(), button[4].getText(), button[8].getText()))
            return true;
        if (threeEqual(button[2].getText(), button[4].getText(), button[6].getText()))
            return true;

        return false;
    }

}