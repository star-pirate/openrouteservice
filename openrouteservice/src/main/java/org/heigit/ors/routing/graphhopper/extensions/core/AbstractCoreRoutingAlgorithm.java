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
package org.heigit.ors.routing.graphhopper.extensions.core;

import com.graphhopper.routing.*;
import com.graphhopper.routing.ch.Path4CH;
import com.graphhopper.routing.ch.PreparationWeighting;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.TurnWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.SPTEntry;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;

/**
 * Calculates best path using core routing algorithm.
 * A core algorithm is separated into phase 1, which is run outside of the core
 * and phase 2, which is run inside the core
 *
 * @author Andrzej Oles
 */

public abstract class AbstractCoreRoutingAlgorithm extends AbstractRoutingAlgorithm {
    protected boolean finishedFrom;
    protected boolean finishedTo;
    int visitedCountFrom1;
    int visitedCountTo1;
    int visitedCountFrom2;
    int visitedCountTo2;

    private CoreDijkstraFilter additionalCoreEdgeFilter;

    boolean inCore;

    protected final TurnWeighting turnWeighting;
    protected final boolean hasTurnWeighting;
    protected boolean approximate = false;

    public AbstractCoreRoutingAlgorithm(Graph graph, Weighting weighting) {
        super(graph, new PreparationWeighting(weighting), TraversalMode.NODE_BASED);

        if (weighting instanceof TurnWeighting) {
            turnWeighting = (TurnWeighting) weighting;
            hasTurnWeighting = true;
        }
        else {
            turnWeighting = null;
            hasTurnWeighting = false;
        }

        int size = Math.min(2000, Math.max(200, graph.getNodes() / 10));
        initCollections(size);

        chGraph = (CHGraph) ((QueryGraph) graph).getMainGraph();
        coreNodeLevel = chGraph.getNodes() + 1;
        turnRestrictedNodeLevel = coreNodeLevel + 1;
    }

    protected abstract void initCollections(int size);
    protected PathBidirRef bestPath;
    protected boolean doUpdateBestPath = true;

    CHGraph chGraph;
    protected final int coreNodeLevel;
    protected final int turnRestrictedNodeLevel;

    public abstract void initFrom(int from, double weight);

    public abstract void initTo(int to, double weight);

    public abstract boolean fillEdgesFrom();

    public abstract boolean fillEdgesTo();

    /**
     * Stopping criterion for phase outside core
     * @return should stop
     */
    public abstract boolean finishedPhase1();

    /**
     * Begin running of the algorithm phase inside the core
     */
    abstract void runPhase2();

    /**
     * Stopping criterion for phase inside core
     * @return should stop
     */
    public abstract boolean finishedPhase2();

    /**
     * Begin the phase that runs outside of the core
     */
    void runPhase1() {
        while (!finishedPhase1() && !isMaxVisitedNodesExceeded()) {
            if (!finishedFrom)
                finishedFrom = !fillEdgesFrom();

            if (!finishedTo)
                finishedTo = !fillEdgesTo();
        }
    }

    @Override
    protected boolean finished() {
        return finishedPhase2();
    }

    protected Path createAndInitPath() {
        bestPath = new Path4CH(graph, graph.getBaseGraph(), weighting);
        return bestPath;
    }

    @Override
    protected Path extractPath() {
        if (finished())
            return bestPath.extract();

        return bestPath;
    }

    protected void runAlgo() {
        // PHASE 1: run modified CH outside of core to find entry points
        inCore = false;
        additionalCoreEdgeFilter.setInCore(false);
        runPhase1();

        // PHASE 2 Perform routing in core with the restrictions filter
        additionalCoreEdgeFilter.setInCore(true);
        inCore = true;
        runPhase2();
    }

    @Override
    public Path calcPath(int from, int to) {
        checkAlreadyRun();
        createAndInitPath();
        initFrom(from, 0);
        initTo(to, 0);
        runAlgo();
        return extractPath();
    }

    @Override
    public int getVisitedNodes() {
        return getVisitedNodesPhase1() + getVisitedNodesPhase2();
    }

    public int getVisitedNodesPhase1() {
        return visitedCountFrom1 + visitedCountTo1;
    }

    public int getVisitedNodesPhase2() {
        return visitedCountFrom2 + visitedCountTo2;
    }

    public RoutingAlgorithm setEdgeFilter(CoreDijkstraFilter additionalEdgeFilter) {
        this.additionalCoreEdgeFilter = additionalEdgeFilter;
        return this;
    }

    //TODO: refactor CoreEdgeFilter to plain EdgeFilter to avoid overriding this method
    @Override
    protected boolean accept(EdgeIteratorState iter, int prevOrNextEdgeId) {
        if (iter.getEdge() == prevOrNextEdgeId) {
            return false;
        } else {
            return additionalCoreEdgeFilter == null || additionalCoreEdgeFilter.accept(iter);
        }
    }

    protected SPTEntry createSPTEntry(int node, double weight) {
        return new SPTEntry(EdgeIterator.NO_EDGE, node, weight);
    }

    void updateBestPath(SPTEntry entryCurrent, SPTEntry entryOther, double newWeight, boolean reverse) {
        bestPath.setSwitchToFrom(reverse);
        bestPath.setSPTEntry(entryCurrent);
        bestPath.setWeight(newWeight);
        bestPath.setSPTEntryTo(entryOther);
    }

    boolean isCoreNode(int node) {
        return chGraph.getLevel(node) >= coreNodeLevel;
    }

    boolean isTurnRestrictedNode(int node) {
        return chGraph.getLevel(node) == turnRestrictedNodeLevel;
    }

    boolean considerTurnRestrictions(int node) {
        if (!hasTurnWeighting)
            return false;
        if (approximate)
            return isTurnRestrictedNode(node);
        return true;
    }

}
