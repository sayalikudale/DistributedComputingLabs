import Mobile.*;

/**
 * MyAgent is a test mobile agent that is injected to the 1st Mobile.Place
 * platform to print the breath message, migrates to the 2nd platform to
 * say "Hello!", and even moves to the 3rd platform to say "Oi!".
 *
 * @author  Sayali Kudale
 * @version %I% %G%
 * @since   1.0
 */
public class MyAgent extends Agent {
    public int hopCount = 0;
    public String[] destination = null;

    /**
     * The constructor receives a String array as an argument from
     * Mobile.Inject.
     *
     * @param args arguments passed from Mobile.Inject to this constructor
     */
    public MyAgent( String[] args ) {

        destination =args;
        messageForPlace = "This is my original place; but I am going to hop to :"+ destination[0] ;//args[args.length-1] ;
        passcodeForMessage ="XXX";
    }

    /**
     * init( ) is the default method called upon an agent inject.
     */
    public void init( ) {
        System.out.println( "agent( " + agentId + ") invoked init: " +
                "hop count = " + hopCount +
                ", next dest = " + destination[hopCount] );

        System.out.println();
        String[] args = new String[1];
        args[0] = "Hello!";
        hopCount++;
        messageForPlace = "My Next Destination is " + destination[hopCount];
        hop( destination[0], "step", args );
    }

    /**
     * step( ) is invoked upon an agent migration to destination[0] after 
     * init( ) calls hop( ).
     *
     * @param args arguments passed from init( ).
     */
    public void step( String[] args ) {
        System.out.println( "agent( " + agentId + ") invoked step: " +
                "hop count = " + hopCount +
                ", next dest = " + destination[hopCount] +
                ", message = " + args[0] );
        args[0] = "Oi!";

        messageForPlace = "My final Destination is :"+ destination[hopCount];
        hopCount++;


        if(messagesReceived.size() > 0){
            for (String s : messagesReceived) {
                System.out.println(s);
            }
        }
        hop( destination[1], "jump", args );

    }

    /**
     * jump( ) is invoked upon an agent migration to destination[1] after
     * step( ) calls hop( ).
     *
     * @param args arguments passed from step( ).
     */
    public void jump( String[] args ) {
        System.out.println( "agent( " + agentId + ") invoked jump: " +
                "hop count = " + hopCount +
                ", message = " + args[0] );

        if(messagesReceived.size() > 0){
            for (String s : messagesReceived) {
                    System.out.println(s);
            }
        }
    }
}