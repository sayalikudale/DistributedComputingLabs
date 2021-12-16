package Mobile;

import java.io.*;
import java.net.*;
import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mobile.Place is the our mobile-agent execution platform that accepts an
 * agent transferred by Mobile.Agent.hop( ), deserializes it, and resumes it
 * as an independent thread.
 *
 * @author Sayali Kudale
 * @version %I% %G$
 * @since 1.0
 */
public class Place extends UnicastRemoteObject implements PlaceInterface {
    private AgentLoader loader = null;  // a loader to define a new agent class
    private int agentSequencer = 0;     // a sequencer to give a unique agentId
    private static Map<String,Map<String, String>> lstMessage  = new HashMap<>(); // map of messages at this place
    private static List<String> msges = new ArrayList<>(); // list of messages to return to agent

    /**
     * This constructor instantiates a Mobiel.AgentLoader object that
     * is used to define a new agen class coming from remotely.
     */
    public Place() throws RemoteException {
        super();
        loader = new AgentLoader();
    }

    /**
     * deserialize( ) deserializes a given byte array into a new agent.
     *
     * @param buf a byte array to be deserialized into a new Agent object.
     * @return a deserialized Agent object
     */
    private Agent deserialize(byte[] buf)
            throws IOException, ClassNotFoundException {
        // converts buf into an input stream
        ByteArrayInputStream in = new ByteArrayInputStream(buf);

        // AgentInputStream identify a new agent class and deserialize
        // a ByteArrayInputStream into a new object
        AgentInputStream input = new AgentInputStream(in, loader);
        return (Agent) input.readObject();
    }

    /**
     * transfer( ) accepts an incoming agent and launches it as an independent
     * thread.
     *
     * @param classname The class name of an agent to be transferred.
     * @param bytecode  The byte code of  an agent to be transferred.
     * @param entity    The serialized object of an agent to be transferred.
     * @return true if an agent was accepted in success, otherwise false.
     */
    public boolean transfer(String classname, byte[] bytecode, byte[] entity)
            throws RemoteException {

        try {
            //register the agent into the agentLoader
            Class newClass = loader.loadClass(classname, bytecode);

            //Deserialize this agent’s entity
            Agent newAgent = deserialize(entity);

            String hostaddress = InetAddress.getLocalHost( ).getHostAddress( );

            //set id with unique identifier
            if (newAgent.getId() == -1) {

                //generate unique Id with sequence number and hostID
                agentSequencer++;
                String newHostAddress = hostaddress.replace(".", "");
                newHostAddress = newHostAddress + String.valueOf(agentSequencer);
                int id = Integer.parseInt(newHostAddress);
                newAgent.setId(id);

               }

            //reading messages by other agents
            newAgent.setMessagesReceived(getMessageFromPreviousAgent(newAgent.passcodeForMessage, hostaddress));

            String message = "Message  given by the Agent : "+ String.valueOf(newAgent.getId()) + " is : "+ newAgent.messageForPlace ;

            //writing messages
            depositMessage(newAgent.passcodeForMessage, message , hostaddress); //writing

            Thread thread = new Thread(newAgent);
            thread.start();

            return true;


        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error is : " +e.getMessage());
            return  false;

        }
    }

    /**
     * main( ) starts an RMI registry in local, instantiates a Mobile.Place
     * agent execution platform, and registers it into the registry.
     *
     * @param args receives a port, (i.e., 5001-65535).
     */
    public static void main(String args[]) {

        // verify arguments
        int port = 0;
        try {
            if (args.length == 1) {
                port = Integer.parseInt(args[0]);
                if (port < 5001 || port > 65535)
                    throw new Exception();
            } else
                throw new Exception();
        } catch (Exception e) {
            System.err.println("usage: java Server port");
            System.exit(-1);
        }

        try {

            //invoke RMI registry for given port
            startRegistry(port);

            //instansiate the place Object
            Mobile.Place placeObj = new Mobile.Place();

            //register into the rmi registery
            Naming.rebind("rmi://localhost:" + port + "/Place", placeObj);
            System.out.println("Place ready.");

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

    }

    /**
     * startRegistry( ) starts an RMI registry process in local to this Place.
     *
     * @param port the port to which this RMI should listen.
     */
    private static void startRegistry(int port) throws RemoteException {
        try {
            Registry registry =
                    LocateRegistry.getRegistry(port);
            registry.list();
        } catch (RemoteException e) {
            Registry registry =
                    LocateRegistry.createRegistry(port);
        }
    }

    /**
     * getMessageFromPreviousAgent( )  returns the messages by previous agents by matching the key and hostname.
     *
     * @param key key to access the message
     * @param hostName hostname of the place on which messages needs to be accessed
     *
     */
    public List<String> getMessageFromPreviousAgent(String key,String hostName){

        if(lstMessage.size()>0){
            Map<String, String> mp = lstMessage.get(key);
            if(mp.size() >0){
                for (Map.Entry<String, String> ent: mp.entrySet()) {
                    if(ent.getKey().equals(hostName)) {
                        msges.add(ent.getValue());
                    }
                }
            }

        }
        return msges;
    }

    /**
     * depositMessage( ) agent deposits message at this place by using key and hostname
     *
     * @param messageKey key to access the message
     * @param msg message for other agents
     * @param hostname hostname of the place on which messages needs to be deposited
     */
    public void depositMessage(String messageKey, String msg, String hostname){

        if(lstMessage.containsKey(messageKey)){
            Map<String,String> ht = lstMessage.get(messageKey);
            ht.put(hostname,msg);

        }else{
            Map<String,String> ht = new HashMap<>();
            ht.put(hostname, msg);
            lstMessage.put(messageKey,ht);
        }

    }
}

