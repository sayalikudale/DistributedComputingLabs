package spouts;

/**
 * Created by sayali on 4/30/21.
 * This is the spout Class
 * Main responsibility of this class is to read the flight data and emit to the bolt
 *
 */

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


    public class FlightsDataReader extends BaseRichSpout  {

    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_RESET = "\u001B[0m";

    private SpoutOutputCollector collector;
    private FileReader fileReader;
    private boolean completed = false;
    private List<List<String>> allStatesData ;

    public void ack(Object msgId) {
        System.out.println( ANSI_CYAN +"OK:"+msgId+ ANSI_RESET);
    }
    public void close() {}

    public void fail(Object msgId) {
        System.out.println(ANSI_CYAN +"FAIL:"+msgId+ ANSI_RESET);
    }


    /**
     * The only thing that the methods will do It is emit each
     * file line
     */
    public void nextTuple() {
        /**
         * The nextuple it is called forever, so if we have been readed the file
         * we will wait and then return
         */
        if(completed){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                //Do nothing
            }
            return;
        }

        //Open the reader
        BufferedReader reader = new BufferedReader(fileReader);

        allStatesData = new ArrayList<List<String>>();
        try{


            JSONParser jsonParser = new JSONParser();

            Object obj = jsonParser.parse(fileReader);

            JSONObject jsonObject = (JSONObject)obj;

            JSONArray statesList = (JSONArray) jsonObject.get("states");


            for (int i=0 ; i< statesList.size(); i++) {

                List<String> list =getFlightParameters( (JSONArray) statesList.get(i));
                allStatesData.add(list);
            }

            //Read all lines
            for(int i=0; i < allStatesData.size(); i++){

                List<String> flightData = allStatesData.get(i);

                this.collector.emit(new Values(flightData.get(0),
                                            flightData.get(1),
                                            flightData.get(2),
                                            flightData.get(3),
                                            flightData.get(4),
                                            flightData.get(5),
                                            flightData.get(6),
                                            flightData.get(7),
                                            flightData.get(8),
                                            flightData.get(9),
                                            flightData.get(10),
                                            flightData.get(11),
                                            flightData.get(12),
                                            flightData.get(13),
                                            flightData.get(14),
                                            flightData.get(15),
                                            flightData.get(16)));

            }
        }catch(Exception e){
            throw new RuntimeException("Error reading tuple",e);
        }finally{
            completed = true;
        }
    }


    /**
     * We will create the file and get the collector object
     */
    public void open(Map conf, TopologyContext context,
                     SpoutOutputCollector collector) {
        try {

            this.fileReader = new FileReader(conf.get("flightFile").toString());


        } catch (FileNotFoundException e) {
            throw new RuntimeException("Error reading file ["+conf.get("FlightsFile")+"]");
        }catch (IOException ioe){
            throw new RuntimeException("Error reading file ["+conf.get("FlightsFile")+"]");
        }catch (Exception e){
            throw new RuntimeException("Error reading file ["+conf.get("FlightsFile")+"]");
        }
        this.collector = collector;
    }

    /**
     * Declare the output 17 fields of flight.txt
     */
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("transponder address", "call sign", "origin country", "last timestamp", "last timestamp1", "longitude",
                "latitude", "altitude (barometric)", "surface or air", "velocity (meters/sec)", "degree north = 0", "vertical rate", "sensors",
                "altitude (geometric)", "transponder code", "special purpose",
                "origin"));

    }



        /**
         * This method gets the JSON data into the Array of Flights parameter
         */
    public static List<String> getFlightParameters(JSONArray input){

        List<String> flightPara = new ArrayList<String>();

        Iterator<JSONObject> iterator = input.iterator();

        while (iterator.hasNext()) {

            String para = String.valueOf(iterator.next());

            flightPara.add(para);

        }

        return flightPara;
    }
}

