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
import heigit.ors.config.AppConfig;
import heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import heigit.ors.routing.graphhopper.extensions.storages.HeavyVehicleAttributesGraphStorage;

/**
 * This class includes in the core all edges with speed more than the one set in the app.config file max_speed.
 *
 * @author Athanasios Kogios
 */

public class MaximumSpeedCoreEdgeFilter implements EdgeFilter {
    private HeavyVehicleAttributesGraphStorage storage;
    private double maxSpeed = Double.parseDouble(AppConfig.Global().getServiceParameter("routing.profiles.default_params","max_speed")); //Minimum speed of the core.
    public final FlagEncoder flagEncoder;

    public MaximumSpeedCoreEdgeFilter(FlagEncoder encoder, GraphStorage graphStorage) {
        this.flagEncoder = encoder;
        if (!flagEncoder.isRegistered())
            throw new IllegalStateException("Make sure you add the FlagEncoder " + flagEncoder + " to an EncodingManager before using it elsewhere");
        storage = GraphStorageUtils.getGraphExtension(graphStorage, HeavyVehicleAttributesGraphStorage.class);
    }

    @Override
    public boolean accept(EdgeIteratorState edge) {
        if ( flagEncoder.getReverseSpeed(edge.getFlags()) > maxSpeed || flagEncoder.getSpeed(edge.getFlags()) > maxSpeed ) { //If the max speed of the road is greater than that of the limit include it in the core.
            return false;
        } else {
            return true;
        }
    }
}

