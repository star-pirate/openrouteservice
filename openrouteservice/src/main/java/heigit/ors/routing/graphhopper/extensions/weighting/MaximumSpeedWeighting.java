package heigit.ors.routing.graphhopper.extensions.weighting;

import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.HintsMap;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;
import com.graphhopper.util.Parameters.Routing;
import heigit.ors.api.requests.routing.RouteRequest;
import heigit.ors.routing.RouteSearchParameters;
import heigit.ors.routing.RoutingRequest;


public class MaximumSpeedWeighting extends FastestWeighting {
    protected final static double SPEED_CONV = 3.6; //From km/h to m/s.
    private final double headingPenalty;
    static double userMaxSpeed;
   //double userMaxSpeedTest= RouteRequest.getUserSpeed()''



    private static RouteSearchParameters searchParameters= new RouteSearchParameters();
    private static double rspspeed ;

    private  static RoutingRequest req = new RoutingRequest();
    private static double rqspeed;



    public MaximumSpeedWeighting(FlagEncoder encoder, HintsMap map) {
        super(encoder, map);
        headingPenalty = map.getDouble(Routing.HEADING_PENALTY, Routing.DEFAULT_HEADING_PENALTY);
        userMaxSpeed = map.getUserMaxSpeed();
    }

    public static void setUserRouteSearchParametersMaxSpeed(double speed){
        searchParameters.setUserSpeed(speed);
        rspspeed = searchParameters.getUserSpeed();
    }


    public static void setUserRoutingRequestMaxSpeed(double speed){
        req.setUserSpeed(speed);
        userMaxSpeed = rspspeed >= 80.0 ? rspspeed : 80.0;
    }


    public double calcWeight(EdgeIteratorState edge, boolean reverse, int prevOrNextEdgeId) {
        double speed = reverse ? flagEncoder.getReverseSpeed(edge.getFlags()) : flagEncoder.getSpeed(edge.getFlags());
        if (speed == 0) {
            return Double.POSITIVE_INFINITY;
        }else if(speed > userMaxSpeed) {
            speed = userMaxSpeed;


            double time = edge.getDistance() / speed * SPEED_CONV;

            // add direction penalties at start/stop/via points
            boolean unfavoredEdge = edge.getBool(EdgeIteratorState.K_UNFAVORED_EDGE, false);
            if (unfavoredEdge)
                time += headingPenalty;

            return time;
        }else{
            return super.calcWeight(edge, reverse,  prevOrNextEdgeId);
        }
    }

    @Override
    public String getName() {
        return "maximum_speed";
    }

}
