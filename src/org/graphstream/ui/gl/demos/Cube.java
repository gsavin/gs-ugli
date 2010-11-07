/*
 * This file is part of GraphStream.
 * 
 * GraphStream is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * GraphStream is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with GraphStream.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright 2006 - 2009
 * 	Julien Baudry
 * 	Antoine Dutot
 * 	Yoann Pigné
 * 	Guilhelm Savin
 */
package org.graphstream.ui.gl.demos;

import java.util.Random;

import org.graphstream.stream.SourceBase;
import org.graphstream.ui.gl.JOGLViewer;

public class Cube {
	protected static class ManualSource extends SourceBase {
		public void addNode(String id) {
			sendNodeAdded("manual-source", id);
		}

		public void setNodeCoords(String id, float... xyz) {
			sendNodeAttributeAdded("manual-source", id, "xyz", xyz);
		}

		public void setNodeColor(String id, float... rgba) {
			sendNodeAttributeAdded("manual-source", id, "ui.color", rgba);
		}

		public void addEdge(String id, String from, String to, boolean directed) {
			sendEdgeAdded("manual-source", id, from, to, directed);
		}
	}

	public static void main(String... args) {
		System.setProperty("gs.gl.maxnodes", "100000");
		System.setProperty("gs.gl.maxedges", "2000000");

		// Graph g = new DefaultGraph( "cube-graph" );
		ManualSource src = new ManualSource();

		// JOGLViewer viewer =
		new JOGLViewer(src);

		Random random = new Random();

		float size = 5;

		if (args != null && args.length > 0)
			size = Integer.parseInt(args[0]);

		for (int i = 0; i < size; i++)
			for (int j = 0; j < size; j++)
				for (int k = 0; k < size; k++) {
					String id = String.format("%d_%d_%d", i, j, k);
					float[] coords = new float[] { -1 + i / (size / 2.0f),
							-1 + j / (size / 2.0f), -1 + k / (size / 2.0f) };

					/*
					 * g.addNode( id ) .addAttribute( "xyz", coords );
					 */

					src.addNode(id);
					src.setNodeCoords(id, coords);
					src.setNodeColor(id, new float[] { random.nextFloat(),
							random.nextFloat(), random.nextFloat(), 0.8f });

					if (i > 0) {
						String backXid = String.format("%d_%d_%d", i - 1, j, k);
						// g.addEdge( id + "::" + backXid, id, backXid );
						src.addEdge(id + "::" + backXid, id, backXid, false);
					}

					if (j > 0) {
						String backYid = String.format("%d_%d_%d", i, j - 1, k);
						// g.addEdge( id + "::" + backYid, id, backYid );
						src.addEdge(id + "::" + backYid, id, backYid, false);

					}

					if (k > 0) {
						String backZid = String.format("%d_%d_%d", i, j, k - 1);
						// g.addEdge( id + "::" + backZid, id, backZid );
						src.addEdge(id + "::" + backZid, id, backZid, false);
					}
				}
	}
}
