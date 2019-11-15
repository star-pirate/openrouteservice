package heigit.ors.routing.graphhopper.extensions.weighting;

import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.HintsMap;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.util.CHEdgeIteratorState;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.Parameters.Routing;


public class MaximumSpeedWeighting extends FastestWeighting {
    protected final static double SPEED_CONV = 3.6; //From km/h to m/s.
    private final double headingPenalty;
    private final  double userMaxSpeed;
    private final String weighting;

    public MaximumSpeedWeighting(FlagEncoder encoder, HintsMap map) {
        super(encoder, map);
        userMaxSpeed = map.getDouble("user_speed",80);
        headingPenalty = map.getDouble(Routing.HEADING_PENALTY, Routing.DEFAULT_HEADING_PENALTY);
        weighting = map.get("weighting","fastest");
    }

    @Override
    public double calcWeight(EdgeIteratorState edge, boolean reverse, int prevOrNextEdgeId) {
        CHEdgeIteratorState tmp = (CHEdgeIteratorState) edge;
        if (tmp.isShortcut()) {
            // if a shortcut is in both directions the weight is identical => no need for 'reverse'
            double speed = tmp.getWeight();
            if (speed == 0) {
                return Double.POSITIVE_INFINITY;
            } else if (speed > userMaxSpeed) {
                speed = userMaxSpeed;


                double time = edge.getDistance() / speed * SPEED_CONV;

                // add direction penalties at start/stop/via points
                boolean unfavoredEdge = edge.getBool(EdgeIteratorState.K_UNFAVORED_EDGE, false);
                if (unfavoredEdge)
                    time += headingPenalty;

                return time;
            } else {
                double time = edge.getDistance() / speed * SPEED_CONV;

                // add direction penalties at start/stop/via points
                boolean unfavoredEdge = edge.getBool(EdgeIteratorState.K_UNFAVORED_EDGE, false);
                if (unfavoredEdge)
                    time += headingPenalty;

                return time;
            }
        }
        else{
            double speed = reverse ? flagEncoder.getReverseSpeed(edge.getFlags()) : flagEncoder.getSpeed(edge.getFlags());
        if (speed == 0) {
            return Double.POSITIVE_INFINITY;
        } else if (speed > userMaxSpeed) {
            speed = userMaxSpeed;


            double time = edge.getDistance() / speed * SPEED_CONV;

            // add direction penalties at start/stop/via points
            boolean unfavoredEdge = edge.getBool(EdgeIteratorState.K_UNFAVORED_EDGE, false);
            if (unfavoredEdge)
                time += headingPenalty;

            return time;
        } else {
                return super.calcWeight(edge, reverse, prevOrNextEdgeId);
        }
    }
    }

    @Override
    public String getName() {
        return "maximum_speed";
    }

}
