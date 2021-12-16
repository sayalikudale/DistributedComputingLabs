/**
 * Created by sayali on 5/1/21.
 *
 * This is the main file to create the configurations and topology
 */

import bolts.HubIdentifier;
import bolts.AirlineSorter;
import spouts.FlightsDataReader;
import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;

public class TopologyMain {
	public static void main(String[] args) throws InterruptedException {
         
        //Topology definition

		System.err.println( "Spout : none , Bolt HubIdentifier :3 ,  Bolt AirlineSorter : 6 ");
		TopologyBuilder builder = new TopologyBuilder();
		builder.setSpout("FlightsData-reader",new FlightsDataReader());

		builder.setBolt("hub-Identifier", new HubIdentifier(),3)
				.shuffleGrouping("FlightsData-reader");

		builder.setBolt("airline-sorter", new AirlineSorter(),6)
				.fieldsGrouping("hub-Identifier", new Fields("airport.city"));

		//Configuration : reading the files
		Config conf = new Config();
		conf.put("flightFile", args[0]);
		conf.put("AirportsData", args[1]);
		conf.put("AirlineCodeData", args[2]); // for Feature 1 added one extra file to read the airlineCode and Airline Names

		conf.setDebug(false);

		//Topology run
		conf.put(Config.TOPOLOGY_MAX_SPOUT_PENDING, 1);
		LocalCluster cluster = new LocalCluster();
		cluster.submitTopology("Getting-Started-Toplogie", conf, builder.createTopology());

		//increased the thread sleeping time
		Thread.sleep(5000);
		cluster.shutdown();

	}
}
