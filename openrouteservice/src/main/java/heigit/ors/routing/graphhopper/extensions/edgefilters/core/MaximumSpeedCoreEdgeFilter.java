/*  This file is part of Openrouteservice.
 *
 *  Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.

 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License along with this library;
 *  if not, see <https://www.gnu.org/licenses/>.
 */
package heigit.ors.routing.graphhopper.extensions.edgefilters.core;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.GraphStorage;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.routing.util.FlagEncoder;
import heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import heigit.ors.routing.graphhopper.extensions.storages.HeavyVehicleAttributesGraphStorage;


public class MaximumSpeedCoreEdgeFilter implements EdgeFilter {
    private HeavyVehicleAttributesGraphStorage storage;
    private double setSpeed = 80; //Minimum speed of the core.
    public final FlagEncoder flagEncoder;

    public MaximumSpeedCoreEdgeFilter(FlagEncoder encoder, GraphStorage graphStorage) {
        this.flagEncoder = encoder;
        if (!flagEncoder.isRegistered())
            throw new IllegalStateException("Make sure you add the FlagEncoder " + flagEncoder + " to an EncodingManager before using it elsewhere");
        storage = GraphStorageUtils.getGraphExtension(graphStorage, HeavyVehicleAttributesGraphStorage.class);
    }

    @Override
    public boolean accept(EdgeIteratorState edge) {
        double speed ;
        double speedFwd ;
        if ( flagEncoder.getReverseSpeed(edge.getFlags()) > setSpeed || flagEncoder.getSpeed(edge.getFlags()) > setSpeed ) { //If the max speed of the road is greater than that of the limit include it in the core.
            speed = flagEncoder.getReverseSpeed(edge.getFlags());
            speedFwd = flagEncoder.getSpeed(edge.getFlags());
            return false;
        } else {
            return true;
        }
    }
}

