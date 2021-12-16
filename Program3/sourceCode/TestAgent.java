import Mobile.*;

/**
 * TestAgent is a test mobile agent that is injected to the 2nd Mobile.Place
 * platform to print the breath message, migrates to the multiple platforms to
 * reads the messages by previous agents and print it
 * @author  Sayali Kudale
 * @version %I% %G%
 * @since   1.0
 */
public class TestAgent extends Agent {
    public int hopCount = 0;
    public String[] destination = null;

    /**
     * The constructor receives a String array as an argument from
     * Mobile.Inject.
     *
     * @param args arguments passed from Mobile.Inject to this constructor
     */
    public TestAgent( String[] args ) {

        destination =args;
        messageForPlace = "This is my original place; but I am going to hop to "+ destination[0] ;//args[args.length-1] ;
        passcodeForMessage ="XXX";

    }

    /**
     * init( ) is the default method called upon an agent inject.
     */
    public void init( ) {
        System.out.println( "agent( " + agentId + ") invoked init: " +
                "hop count = " + hopCount +
                ", next dest = " + destination[hopCount] );

        String[] args = new String[1];
        args[0] = "Hello! I am Test Agent";
        hopCount++;
        messageForPlace = "My Next Destination is :" + destination[hopCount] ;
        hop( destination[0], "step", args );
    }

    /**
     * step( ) is invoked upon an agent migration to destination[0] after 
     * init( ) calls hop( ).
     * reads messages by previous agents and print it
     * @param args arguments passed from init( ).
     */
    public void step( String[] args ) {
        System.out.println( "agent( " + agentId + ") invoked step: " +
                "hop count = " + hopCount +
                ", next dest = " + destination[hopCount] +
                ", message = " + args[0] );
        args[0] = "Oi! By the Test Agent";

        if(messagesReceived.size() > 0){
            for (String s : messagesReceived) {
                System.out.println(s);
            }
        }
        hopCount++;
        messageForPlace = "My Next Destination is " + destination[hopCount];
        hop( destination[1], "jump", args );

    }

    /**
     * jump( ) is invoked upon an agent migration to destination[1] after
     * step( ) calls hop( ).
     * reads messages by previous agents and print it
     * @param args arguments passed from step( ).
     */
    public void jump( String[] args ) {
        System.out.println( "agent( " + agentId + ") invoked jump: " +
                "hop count = " + hopCount +
                ", next dest = " + destination[hopCount] +
                ", message = " + args[0] );

        if(messagesReceived.size() > 0){
            for (String s : messagesReceived) {
                System.out.println(s);
            }
        }

        args[0] = "Ola!!!";

        messageForPlace = destination[hopCount]+ "will be  my final destination";
        hopCount++;
        hop( destination[2], "hoppingToFinalDest", args );
    }


    /**
     * hoppingToFinalDest( ) is invoked upon an agent migration to destination[1] after
     * jump( ) calls hop( ).
     * reads messages by previous agents and print it
     * @param args arguments passed from jump( ).
     */
    public void hoppingToFinalDest( String[] args ) {
        System.out.println( "agent( " + agentId + ") invoked hoppingTofinalDest: " +
                "hop count = " + hopCount +
                ", message = " + args[0] );

        if(messagesReceived.size() > 0){
            for (String s : messagesReceived) {
                System.out.println(s);
            }
        }
    }
}