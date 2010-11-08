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
 * Copyright 2006 - 2010
 * 	Julien Baudry
 * 	Antoine Dutot
 * 	Yoann Pigné
 * 	Guilhelm Savin
 */
package org.graphstream.ui.gl.renderer;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.graphstream.stream.Sink;
import org.graphstream.ui.gl.Context;
import org.graphstream.ui.gl.Context.NodeColorMode;
import org.graphstream.ui.layout.LayoutListener;

public class GraphBuffers implements Sink, LayoutListener {
	public static interface ID2Index {
		void init(int maxNodes);

		int getIndex(String id);

		void setIndex(String id, int index);

		void updateIndex(int oldIndex, int newIndex);

		void removeIndex(int index);
	}

	protected static class DraftID2Index implements ID2Index {
		/**
		 * 
		 */
		private static final long serialVersionUID = 4124961375149112148L;

		static int ID = 0;
		static int INDEX = 1;

		IntBuffer data;
		int max;

		ReentrantLock lock = new ReentrantLock();
		
		public void init(int maxNodes) {
			data = IntBuffer.allocate(maxNodes * 2);
			max = 0;
		}

		protected int getPosition(String id) {
			int hash = id.hashCode();

			for (int i = 0; i < max; i++)
				if (data.get(i * 2 + ID) == hash)
					return i;

			return -1;
		}

		protected int getPosition(int index) {
			for (int i = 0; i < max; i++)
				if (data.get(i * 2 + INDEX) == index)
					return i;

			return -1;
		}

		public int getIndex(String id) {
			lock.lock();
			
			int p = getPosition(id);

			lock.unlock();
			
			if (p >= 0)
				return data.get(2 * p + INDEX);

			System.err.printf("id %s not found%n",id);

			return -1;
		}

		public void setIndex(String id, int index) {
			lock.lock();
			
			int p = getPosition(id);

			if (p == -1) {
				data.put(2 * max + ID, id.hashCode());
				data.put(2 * max + INDEX, index);

				max++;
			} else {
				data.put(2 * p + INDEX, index);
			}
			
			lock.unlock();
		}

		public void updateIndex(int oldIndex, int newIndex) {
			int p = getPosition(oldIndex);

			if (p >= 0) {
				data.put(2 * p + INDEX, newIndex);
			}
		}

		public void removeIndex(int index) {
			int p = getPosition(index);

			if (p != -1) {
				if (p < max - 1) {
					data.put(2 * p + ID, data.get(2 * (max - 1) + ID));
					data.put(2 * p + INDEX, data.get(2 * (max - 1) + INDEX));
				}

				max--;
			}
		}

	}

	ID2Index nodeID2Index;
	ID2Index edgeID2Index;

	int lastNodeIndex;
	ByteBuffer nodeIndexes;

	IntBuffer nodeIndexesI;

	int nodeStyleSize;
	ByteBuffer nodeVertices;
	ByteBuffer nodeStyle;
	ByteBuffer nodeColors;

	FloatBuffer nodeVerticesF;
	FloatBuffer nodeColorsF;

	boolean enableColorPointer = false;

	int lastEdgeIndex;
	ByteBuffer edges;
	IntBuffer edgesI;

	/*
	 * for debug
	 */
	void printBuffers(java.io.PrintStream out) {
		out.printf("node indexes:%n");

		for (int i = 0; i <= lastNodeIndex; i++)
			out.printf("%d ", nodeIndexesI.get(i));

		out.printf("%nnode vertices:%n");

		for (int i = 0; i <= lastNodeIndex; i++) {
			int index = nodeIndexesI.get(i);
			out.printf("%d : (%f;%f;%f)%n", index,
					nodeVerticesF.get(3 * index),
					nodeVerticesF.get(3 * index + 1),
					nodeVerticesF.get(3 * index + 2));
		}
	}

	public Buffer createNewVertexBufferView() {
		return nodeVertices.duplicate().order(ByteOrder.nativeOrder());
	}

	public Buffer createNewIndexBufferView() {
		return nodeIndexes.duplicate().order(ByteOrder.nativeOrder());
	}

	public Buffer createNewEdgeBufferView() {
		return edges.duplicate().order(ByteOrder.nativeOrder());
	}

	public Buffer createNewNodeColorView() {
		return nodeColors.duplicate().order(ByteOrder.nativeOrder());
	}

	public boolean isColorPointerEnabled() {
		return enableColorPointer;
	}

	public int getActiveIndexCount() {
		return lastNodeIndex + 1;
	}

	public int getActiveEdgeCount() {
		return lastEdgeIndex + 1;
	}

	public int getVertexComposantCount() {
		return 3;
	}

	public int getVertexSize() {
		return nodeVerticesF.capacity();
	}

	protected void init(Context ctx, int maxNodes, int maxEdges) {
		nodeIndexes = ByteBuffer.allocateDirect(maxNodes * Integer.SIZE).order(
				ByteOrder.nativeOrder());
		nodeIndexesI = nodeIndexes.asIntBuffer();
		nodeVertices = ByteBuffer.allocateDirect(3 * maxNodes * Float.SIZE)
				.order(ByteOrder.nativeOrder());
		nodeVerticesF = nodeVertices.asFloatBuffer();

		enableColorPointer = ctx.getNodeColorMode() == NodeColorMode.EachNodeOneColor;

		if (enableColorPointer) {
			nodeColors = ByteBuffer.allocateDirect(4 * maxNodes * Float.SIZE)
					.order(ByteOrder.nativeOrder());
			nodeColorsF = nodeColors.asFloatBuffer();
		} else {
			nodeColors = ByteBuffer.allocateDirect(4 * 4 * Float.SIZE).order(
					ByteOrder.nativeOrder());
			nodeColorsF = nodeColors.asFloatBuffer();
		}

		for (int i = 0; i < maxNodes; i++)
			setNodePoolIndex(i, i);

		lastNodeIndex = -1;

		edges = ByteBuffer.allocateDirect(2 * maxEdges).order(
				ByteOrder.nativeOrder());
		edgesI = edges.asIntBuffer();

		nodeID2Index = new DraftID2Index();
		nodeID2Index.init(maxNodes);

		edgeID2Index = new DraftID2Index();
		edgeID2Index.init(maxEdges);

		long size = nodeIndexes.capacity() + nodeVertices.capacity()
				+ edges.capacity() + nodeColors.capacity();
		String symbol = "o";

		if (size > 1024) {
			size /= 1024;
			symbol = "ko";
		}

		if (size > 1024) {
			size /= 1024;
			symbol = "mo";
		}

		if (size > 1024) {
			size /= 1024;
			symbol = "go";
		}

		System.out.printf("buffers use %d%s%n", size, symbol);
	}

	private void setNodePoolIndex(int index, int value) {
		nodeIndexesI.put(index, value);
	}

	private int getNodePoolIndex(int index) {
		return nodeIndexesI.get(index);
	}

	private void setNodeX(int poolIndex, float x) {
		nodeVerticesF.put(poolIndex * 3 + 0, x);
	}

	private void setNodeY(int poolIndex, float y) {
		nodeVerticesF.put(poolIndex * 3 + 1, y);
	}

	private void setNodeZ(int poolIndex, float z) {
		nodeVerticesF.put(poolIndex * 3 + 2, z);
	}

	protected void checkNodeCoords(String nodeId, String attr, Object value) {
		int index = nodeID2Index.getIndex(nodeId);

		if (index != -1) {
			float x, y, z;

			x = y = z = 0;

			if (value instanceof Float) {
				x = y = z = (Float) value;
			} else if (value instanceof float[]) {
				float[] varray = (float[]) value;

				x = varray.length > 0 ? varray[0] : 0;
				y = varray.length > 1 ? varray[1] : 0;
				z = varray.length > 2 ? varray[2] : 0;
			} else if (value instanceof Float[]) {
				Float[] varray = (Float[]) value;

				x = varray.length > 0 ? varray[0] : 0;
				y = varray.length > 1 ? varray[1] : 0;
				z = varray.length > 2 ? varray[2] : 0;
			} else
				System.err.printf("unknown coords type%n");

			int poolIndex = getNodePoolIndex(index);

			if (attr.equals("x")) {
				setNodeX(poolIndex, x);
			} else if (attr.equals("y")) {
				setNodeY(poolIndex, y);
			} else if (attr.equals("z")) {
				setNodeZ(poolIndex, z);
			} else if (attr.equals("xy")) {
				setNodeX(poolIndex, x);
				setNodeY(poolIndex, y);
			} else if (attr.equals("xyz")) {
				setNodeX(poolIndex, x);
				setNodeY(poolIndex, y);
				setNodeZ(poolIndex, z);
			}
		} else
			System.err.printf("id not found%n");
	}

	protected void checkNodeColor(String nodeId, String attr, Object value) {
		int index = nodeID2Index.getIndex(nodeId);

		if (enableColorPointer && index != -1) {
			float[] rgba = { 0, 0, 0, 1 };

			if (value instanceof Float) {
				// XXX
			} else if (value instanceof float[]) {
				float[] varray = (float[]) value;

				rgba[0] = varray.length > 0 ? varray[0] : 0;
				rgba[1] = varray.length > 1 ? varray[1] : 0;
				rgba[2] = varray.length > 2 ? varray[2] : 0;
				rgba[3] = varray.length > 3 ? varray[3] : 0;
			} else if (value instanceof Float[]) {
				Float[] varray = (Float[]) value;

				rgba[0] = varray.length > 0 ? varray[0] : 0;
				rgba[1] = varray.length > 1 ? varray[1] : 0;
				rgba[2] = varray.length > 2 ? varray[2] : 0;
				rgba[3] = varray.length > 3 ? varray[3] : 0;
			} else
				System.err.printf("unknown coords type%n");

			int poolIndex = getNodePoolIndex(index);

			for (int i = 0; i < 4; i++)
				nodeColorsF.put(poolIndex * 4 + i, rgba[i]);
		}
	}

	protected void removeNodeInMemory(int index) {
		if (lastNodeIndex > 0) {
			int i1 = getNodePoolIndex(lastNodeIndex);
			int i2 = getNodePoolIndex(index);

			setNodePoolIndex(lastNodeIndex, i2);
			setNodePoolIndex(index, i1);

			nodeID2Index.removeIndex(index);
			nodeID2Index.updateIndex(lastNodeIndex, index);

			//System.out.printf("%d <--> %d%n", lastNodeIndex, index);
		}

		if (lastNodeIndex >= 0)
			lastNodeIndex--;

		//System.out.printf("after del, last index is : %d%n", lastNodeIndex);
	}

	public void nodeAdded(String sourceId, long timeId, String nodeId) {
		if (lastNodeIndex >= nodeIndexesI.capacity() - 1)
			throw new OutOfMemoryError(String.format(
					"out of memory for nodes%n"
							+ "set \"gs.gl.maxnodes\" to an higher value%n"
							+ "current value: %d nodes%n",
					nodeIndexesI.capacity()));

		int index = ++lastNodeIndex;
		int poolIndex = getNodePoolIndex(index);

		nodeID2Index.setIndex(nodeId, index);

		setNodeX(poolIndex, 0);
		setNodeY(poolIndex, 0);
		setNodeZ(poolIndex, 0);
	}

	public void nodeRemoved(String sourceId, long timeId, String nodeId) {
		int index = nodeID2Index.getIndex(nodeId);
		
		if (index != -1)
			removeNodeInMemory(index);
	}

	public void edgeAdded(String sourceId, long timeId, String edgeId,
			String fromNodeId, String toNodeId, boolean directed) {
		if (2 * lastEdgeIndex >= edgesI.capacity() - 1)
			throw new OutOfMemoryError(String.format(
					"out of memory for edges%n"
							+ "set \"gs.gl.maxedges\" to an higher value%n"
							+ "current value: %d nodes%n", edgesI.capacity()));

		int index = ++lastEdgeIndex;

		int indexA = nodeID2Index.getIndex(fromNodeId);
		int indexB = nodeID2Index.getIndex(toNodeId);

		if (indexA < 0 || indexB < 0)
			return;

		indexA = getNodePoolIndex(indexA);
		indexB = getNodePoolIndex(indexB);

		if (indexA < 0 || indexB < 0)
			return;

		edgeID2Index.setIndex(edgeId, index);

		edgesI.put(2 * index + 0, indexA);
		edgesI.put(2 * index + 1, indexB);
	}

	public void edgeRemoved(String sourceId, long timeId, String edgeId) {
		// TODO Auto-generated method stub

	}

	public void graphAttributeAdded(String sourceId, long timeId,
			String attribute, Object value) {
		// TODO Auto-generated method stub

	}

	public void graphAttributeChanged(String sourceId, long timeId,
			String attribute, Object oldValue, Object newValue) {
		// TODO Auto-generated method stub

	}

	public void graphAttributeRemoved(String sourceId, long timeId,
			String attribute) {
		// TODO Auto-generated method stub

	}

	public void nodeAttributeAdded(String sourceId, long timeId, String nodeId,
			String attribute, Object value) {
		if (attribute != null) {
			if (attribute.matches("^(x|y|z|xy|xyz)$"))
				checkNodeCoords(nodeId, attribute, value);
			else if (attribute.matches("^ui[.]color$"))
				checkNodeColor(nodeId, attribute, value);
		}
	}

	public void nodeAttributeChanged(String sourceId, long timeId,
			String nodeId, String attribute, Object oldValue, Object newValue) {
		if (attribute != null && attribute.matches("^(x|y|z|xy|xyz)$"))
			checkNodeCoords(nodeId, attribute, newValue);
	}

	public void nodeAttributeRemoved(String sourceId, long timeId,
			String nodeId, String attribute) {
		// TODO Auto-generated method stub

	}

	public void edgeAttributeAdded(String sourceId, long timeId, String edgeId,
			String attribute, Object value) {
		// TODO Auto-generated method stub

	}

	public void edgeAttributeChanged(String sourceId, long timeId,
			String edgeId, String attribute, Object oldValue, Object newValue) {
		// TODO Auto-generated method stub

	}

	public void edgeAttributeRemoved(String sourceId, long timeId,
			String edgeId, String attribute) {
		// TODO Auto-generated method stub

	}

	public void graphCleared(String sourceId, long timeId) {
		// TODO Auto-generated method stub

	}

	public void stepBegins(String sourceId, long timeId, double step) {
		// TODO Auto-generated method stub

	}

	// Layout Listener
	
	public void nodeMoved(String id, float x, float y, float z) {
		
		int index = nodeID2Index.getIndex(id);
		
		if( index >= 0 ) {
			setNodeX(index,x);
			setNodeY(index,y);
			setNodeZ(index,z);
		}
	}

	public void nodeInfos(String id, float dx, float dy, float dz) {
		// TODO Auto-generated method stub
		
	}

	public void edgeChanged(String id, float[] points) {
		// TODO Auto-generated method stub
		
	}

	public void nodesMoved(Map<String, float[]> nodes) {
		for( String id: nodes.keySet()
				) {
			float[] xyz = nodes.get(id);
			nodeMoved(id,xyz[0],xyz[1],xyz[2]);
		}
	}

	public void edgesChanged(Map<String, float[]> edges) {
		// TODO Auto-generated method stub
		
	}

	public void stepCompletion(float percent) {
		// TODO Auto-generated method stub
		
	}
}
