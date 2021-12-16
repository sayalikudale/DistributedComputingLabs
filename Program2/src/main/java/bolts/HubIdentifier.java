package bolts;

/**
 * Created by sayali on 5/1/21.
 * This the Bolt 1 HubIdentifier
 * The main task of this bolt is to evalute whether flight lattitude is near to any aiport
 * and emit the aiport and flight information to the next bolt AirlineSorter.
 */

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HubIdentifier extends BaseBasicBolt {

    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_RESET = "\u001B[0m";
    private FileReader fileReader;
    private static final int fastVelocity = 200;

    List<Airport> topAirports = new ArrayList<Airport>();

    public void cleanup() {
    }


    /**
     * On create
     */
    @Override
    public void prepare(Map stormConf, TopologyContext context) {

        try {

            this.fileReader = new FileReader((String) stormConf.get("AirportsData"));
            BufferedReader reader = new BufferedReader(fileReader);
            topAirports = new ArrayList<Airport>();
            String str;

            //Read all lines
            while ((str = reader.readLine()) != null) {

                if (!str.trim().isEmpty()) {
                    Airport airport = readAirportsData(str);

                    topAirports.add(airport);

                }
            }

        } catch (Exception e) {

            System.out.println(ANSI_CYAN + e.getMessage() + ANSI_RESET);
            System.out.println(ANSI_CYAN + e.getStackTrace() + ANSI_RESET);


        }

    }

    /**
     * The bolt will receive the line from the
     * words file and process it to Normalize this line
     * <p>
     * The normalize will be put the words in lower case
     * and split the line to get all words in this
     */
    public void execute(Tuple input, BasicOutputCollector collector) {


        String callSign = input.getStringByField("call sign").trim();
        String latitude = input.getStringByField("latitude").trim();
        String longitude = input.getStringByField("longitude").trim();

        String verticalRate = input.getStringByField("vertical rate").trim();
        String velocity = input.getStringByField("velocity (meters/sec)").trim();


        if (callSign.length() > 2) {

            List<String> nearestAiport = getNearestAirport(latitude, longitude);


            if (nearestAiport.size() > 0) {

                for (int i = 0; i <= nearestAiport.size() - 2; i = i + 2) {

                    collector.emit(new Values(nearestAiport.get(i), nearestAiport.get(i + 1), callSign));

                }

            }

        }


    }


    /**
     * The bolt will only emit the field "aiport city", aiport code , and call sign
     */
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("airport.city", "airport.code", "callSign"));
    }

    /**
     * This method split the file line by comma and convert into airport object
     */
    private Airport readAirportsData(String str) {

        String[] airportData = str.split(",");

        Airport airport = new Airport(airportData[0], airportData[1], airportData[2], airportData[3]);

        return airport;
    }


    /**
     * This method performs the logic to determine whether flight lattitude longitude is within the 20 miles to the any of the topflights
     */
    private List<String> getNearestAirport(String latitudeStr, String longitudeStr) {

        List<String> nearestAirport = new ArrayList<String>();
        ;

        Double latitude = latitudeStr.isEmpty() || latitudeStr == null || latitudeStr.equals("null") ? 0 : Double.parseDouble(latitudeStr);
        Double longitude = longitudeStr.isEmpty() || longitudeStr == null || longitudeStr.equals("null") ? 0 : Double.parseDouble(longitudeStr);


        for (Airport arpt : topAirports) {

            Double latitudeDiff = Math.abs(latitude - arpt.latitude) * 70;
            Double longitudeDiff = Math.abs(longitude - arpt.longitude) * 45;


            if (latitudeDiff <= 20 && longitudeDiff <= 20) {

                nearestAirport.add(arpt.city);
                nearestAirport.add(arpt.code);


            }

        }

        return nearestAirport;

    }


    private boolean checkMoreAccuracy(String verticalRate, String velocity) {

        Double vr = verticalRate.isEmpty() || verticalRate == null || verticalRate.equals("null") ? 0 : Double.parseDouble(verticalRate);
        Double vel = velocity.isEmpty() || velocity == null || velocity.equals("null") ? 0 : Double.parseDouble(velocity);


        if (vr == 0 && vel >= fastVelocity)
            return  false;


        return  true;

    }


    }

    class Airport {

        String city;
        String code;
        float latitude;
        float longitude;

        Airport(String city, String code, String latitude, String longitude) {

            this.city = city.trim();
            this.code = code.trim();
            this.latitude = Float.parseFloat(latitude);
            this.longitude = Float.parseFloat(longitude);

        }


    }


