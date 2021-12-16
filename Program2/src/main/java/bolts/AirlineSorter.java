
/**
 * Author: sayali kudale.
 * This is the bolt 2 class AirlineSorter
 *
 * The responsibility of this class is to print the results
 *
 */
package bolts;

import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Tuple;

import java.util.*;
import java.lang.*;
import java.io.BufferedReader;

public class AirlineSorter extends BaseBasicBolt {

    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_RESET = "\u001B[0m";
    private FileReader fileReader;
    Integer id;
    String name;
    Map<String, Map<String,Integer>> counters;
    Map<String,String> ailinesWithCode;
    long startTime =0, endTime=0;

    /**
     * At the end of the spout (when the cluster is shutdown
     * We will show the count of ailines per the aiport in sorted order
     */
    @Override
    public void cleanup() {
        System.err.println(ANSI_CYAN +"-- Airline Sorter ["+name+"-"+id+"] --"+ANSI_RESET);


        for(Map.Entry<String, Map<String,Integer>> entry : counters.entrySet()){


            System.err.println(ANSI_CYAN + "At Airport : " + entry.getKey()+ANSI_RESET);
            int totalFlights = 0;
            Map<String,Integer> airlineDetails = entry.getValue();


           Map<String, Integer> sortedAirlineDetails = sortByValue(airlineDetails);


            for(Map.Entry<String, Integer> entryInner : sortedAirlineDetails.entrySet()){

                String airlineCode= entryInner.getKey();
                if(ailinesWithCode.containsKey(airlineCode)){
                    airlineCode = airlineCode + "(" + ailinesWithCode.get(airlineCode) + ")";
                }

                totalFlights +=entryInner.getValue();
                System.err.println(ANSI_CYAN  + airlineCode +": \t "+ entryInner.getValue() +ANSI_RESET);


            }

            System.err.println(ANSI_CYAN + "total # of flights   : " + totalFlights+ANSI_RESET);

            System.err.println();


        }

        this.endTime = System.currentTimeMillis();

        long totalTime = endTime - startTime;
        System.err.println(ANSI_CYAN + "Total execution time   : " +totalTime+ANSI_RESET);

    }


    /**
     * On create
     * This method reads the AilineCode information and store into the ailinesWithCode HashMap
     */

    @Override
    public void prepare(Map stormConf, TopologyContext context) {
        try {

            this.startTime = System.currentTimeMillis();

            this.counters = new HashMap<String, Map<String, Integer>>();
            this.name = context.getThisComponentId();
            this.id = context.getThisTaskId();

            this.ailinesWithCode = new HashMap<String, String>();

            this.fileReader = new FileReader((String) stormConf.get("AirlineCodeData"));
            BufferedReader reader1 = new BufferedReader(fileReader);

            String str1;

            //Read all lines from AirlineCode.txt
            while ((str1 = reader1.readLine()) != null) {

                if (!str1.trim().isEmpty()) {

                    if (str1 != ",") {
                        String[] codeinfo = str1.split(",");
                        if (codeinfo[0] != null && codeinfo[0] != "n/a" && codeinfo[0].length() != 0 && codeinfo[1] != null && codeinfo[1] != "n/a" && codeinfo[1].length() != 0) {
                            ailinesWithCode.put(codeinfo[0].trim(), codeinfo[1].trim());

                        }
                    }
                }
            }

            System.out.println(ANSI_CYAN + ailinesWithCode.size()+ ANSI_RESET);
        }catch (Exception e) {

            System.out.println(ANSI_CYAN + e.getMessage() + ANSI_RESET);
            System.out.println(ANSI_CYAN + e.getStackTrace() + ANSI_RESET);


        }

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {}

    /**
     *
     * This method reads the values emitted by the HubIdentifier
     * It gets the AirlineCode from the callSign
     * perform counting of the aicrafts and add into the collection.
     *
     */

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {

        String aiportCity = input.getStringByField("airport.city").trim();
        String aiportCode = input.getStringByField("airport.code").trim();
        String callSign = input.getStringByField("callSign").trim();
        String airLineCode = callSign.substring(0,3);

        String airportKey = aiportCode + "(" + aiportCity + ")";

        if(!counters.containsKey(airportKey)){

            Map<String, Integer> airLine = new HashMap<String, Integer>();
            airLine.put(airLineCode,1);

            counters.put(airportKey,airLine);

        }else{

            Map<String, Integer> airLine = counters.get(airportKey);
            if(airLine.containsKey(airLineCode)){

                int airLineCount = airLine.get(airLineCode);
                airLine.put(airLineCode,airLineCount+1);

            }else{
                airLine.put(airLineCode,1);
            }
        }

    }




    // function to sort hashmap by values
    public static Map<String, Integer> sortByValue(Map<String, Integer> hm)
    {
        // Create a list from elements of HashMap
        List<Map.Entry<String, Integer> > list =
                new LinkedList<Map.Entry<String, Integer> >(hm.entrySet());

        // Sort the list
        Collections.sort(list, new Comparator<Map.Entry<String, Integer> >() {
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2)
            {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        // put data from sorted list to hashmap
        HashMap<String, Integer> temp = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }



}
