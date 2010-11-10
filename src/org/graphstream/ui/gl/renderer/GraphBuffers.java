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
 * 
 * UGLI : GraphStream OpenGL Viewer
 *  Copyright 2010 Guilhelm Savin
 */
package org.graphstream.ui.gl.renderer;

import java.awt.Color;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.graphstream.stream.Sink;
import org.graphstream.ui.gl.Context;
import org.graphstream.ui.gl.Context.NodeColorMode;
import org.graphstream.ui.gl.Context.NodeSizeMode;
import org.graphstream.ui.graphicGraph.stylesheet.Rule;
import org.graphstream.ui.graphicGraph.stylesheet.Style;
import org.graphstream.ui.graphicGraph.stylesheet.StyleSheet;
import org.graphstream.ui.graphicGraph.stylesheet.StyleSheetListener;
import org.graphstream.ui.layout.LayoutListener;

public class GraphBuffers implements Sink, LayoutListener, StyleSheetListener {
	public static interface ID2Index {
		void init(int maxNodes);

		int getIndex(String id);

		void setIndex(String id, int index);

		void updateIndex(int oldIndex, int newIndex);

		void removeIndex(int index);

		Iterable<String> eachID();
	}

	protected static class HashMapID2Index implements ID2Index {

		ConcurrentHashMap<String, Integer> data;
		String[] reverse;

		public void init(int maxNodes) {
			reverse = new String[maxNodes];
			data = new ConcurrentHashMap<String, Integer>();
		}

		public int getIndex(String id) {
			Integer i = data.get(id);
			return i == null ? -1 : i;
		}

		public void setIndex(String id, int index) {
			data.put(id, index);
			reverse[index] = id;
		}

		public void updateIndex(int oldIndex, int newIndex) {

			reverse[newIndex] = reverse[oldIndex];
			reverse[oldIndex] = null;

			if (reverse[newIndex] != null)
				data.put(reverse[newIndex], newIndex);
		}

		public void removeIndex(int index) {
			if (reverse[index] != null) {
				data.remove(reverse[index]);
				reverse[index] = null;
			}
		}

		public Iterable<String> eachID() {
			return data.keySet();
		}
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

			System.err.printf("id %s not found%n", id);

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

		public Iterable<String> eachID() {
			throw new Error("not implemented");
		}
	}

	public static enum NodeColor {
		RED, GREEN, BLUE, ALPHA
	}

	public static enum GraphBuffer {
		NODE_INDEXES, NODE_VERTICES, NODE_COLORS, NODE_SIZES, EDGES
	}

	/**
	 * Converter of node id to index.
	 */
	private ID2Index nodeID2Index;
	/**
	 * Converter of edge id to index.
	 */
	private ID2Index edgeID2Index;

	private EnumMap<GraphBuffer, ByteBuffer> buffers;

	/**
	 * The last node index in buffers. Buffers are used from 0 to this last
	 * index value.
	 */
	private int lastNodeIndex;
	/**
	 * The last edge index in buffers. Buffers are used from 0 to this last
	 * index value.
	 */
	private int lastEdgeIndex;

	/**
	 * Active nodes indexes.
	 */
	private IntBuffer nodeIndexes;
	/**
	 * Node coordinates.
	 */
	private FloatBuffer nodeVertices;
	/**
	 * Node colors. Used only if {@link #enableColorPointer} is set to true.
	 */
	private FloatBuffer nodeColors;
	/**
	 * Node sizes. Used only if {@link #enableSizePointer} is set to true.
	 */
	private FloatBuffer nodeSizes;
	/**
	 * Edge data. Contains id of edges extremities.
	 */
	private IntBuffer edges;

	/**
	 * Flag indicating if each node should have a color.
	 */
	private boolean enableColorPointer = false;
	/**
	 * Flag indicating if each node should have a size.
	 */
	private boolean enableSizePointer = false;

	protected Context ctx;

	protected StyleSheet stylesheet;

	public GraphBuffers(Context ctx) {
		this.ctx = ctx;
		this.buffers = new EnumMap<GraphBuffer, ByteBuffer>(GraphBuffer.class);

		this.stylesheet = new StyleSheet();
		this.stylesheet.addListener(this);
	}

	/*
	 * for debug
	 */
	void printBuffers(java.io.PrintStream out) {
		out.printf("node indexes:%n");

		for (int i = 0; i <= lastNodeIndex; i++)
			out.printf("%d ", nodeIndexes.get(i));

		out.printf("%nnode vertices:%n");

		for (int i = 0; i <= lastNodeIndex; i++) {
			int index = nodeIndexes.get(i);
			out.printf("%d : (%f;%f;%f)%n", index, nodeVertices.get(3 * index),
					nodeVertices.get(3 * index + 1),
					nodeVertices.get(3 * index + 2));
		}
	}

	public Buffer createNewVertexBufferView() {
		return buffers.get(GraphBuffer.NODE_VERTICES).duplicate()
				.order(ByteOrder.nativeOrder());
	}

	public Buffer createNewIndexBufferView() {
		return buffers.get(GraphBuffer.NODE_INDEXES).duplicate()
				.order(ByteOrder.nativeOrder());
	}

	public Buffer createNewEdgeBufferView() {
		return buffers.get(GraphBuffer.EDGES).duplicate()
				.order(ByteOrder.nativeOrder());
	}

	public Buffer createNewNodeColorView() {
		return buffers.get(GraphBuffer.NODE_COLORS).duplicate()
				.order(ByteOrder.nativeOrder());
	}

	public Buffer createNewNodeSizeView() {
		return buffers.get(GraphBuffer.NODE_SIZES).duplicate()
				.order(ByteOrder.nativeOrder());
	}

	public boolean isColorPointerEnabled() {
		return enableColorPointer;
	}

	public boolean isSizePointerEnabled() {
		return enableSizePointer;
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
		return nodeVertices.capacity();
	}

	public int getColorComposantCount() {
		return 4;
	}

	private void allocateBuffers(int maxNodes, int maxEdges) {
		int colorBufferSize = enableColorPointer ? 4 * maxNodes : 1;
		int sizeBufferSize = enableSizePointer ? maxNodes : 1;

		if (buffers.size() != 0) {
			for (ByteBuffer buffer : buffers.values())
				buffer.clear();
			buffers.clear();
		}

		/*
		 * Allocation
		 */
		buffers.put(GraphBuffer.NODE_INDEXES,
				ByteBuffer.allocateDirect(maxNodes * Integer.SIZE));
		buffers.put(GraphBuffer.NODE_VERTICES,
				ByteBuffer.allocateDirect(3 * maxNodes * Float.SIZE));
		buffers.put(GraphBuffer.NODE_COLORS,
				ByteBuffer.allocateDirect(colorBufferSize * Float.SIZE));
		buffers.put(GraphBuffer.NODE_SIZES,
				ByteBuffer.allocateDirect(sizeBufferSize * Float.SIZE));
		buffers.put(GraphBuffer.EDGES, ByteBuffer.allocateDirect(2 * maxEdges));

		/*
		 * Ordering
		 */
		for (ByteBuffer buffer : buffers.values())
			buffer.order(ByteOrder.nativeOrder());

		/*
		 * Update last indexes
		 */
		lastNodeIndex = -1;
		lastEdgeIndex = -1;

		/*
		 * Create views
		 */
		nodeIndexes = buffers.get(GraphBuffer.NODE_INDEXES).asIntBuffer();
		nodeVertices = buffers.get(GraphBuffer.NODE_VERTICES).asFloatBuffer();
		nodeColors = buffers.get(GraphBuffer.NODE_COLORS).asFloatBuffer();
		nodeSizes = buffers.get(GraphBuffer.NODE_SIZES).asFloatBuffer();
		edges = buffers.get(GraphBuffer.EDGES).asIntBuffer();
	}

	protected void init(Context ctx, int maxNodes, int maxEdges) {
		/*
		 * nodeIndexes = ByteBuffer.allocateDirect(maxNodes *
		 * Integer.SIZE).order( ByteOrder.nativeOrder()); nodeIndexesI =
		 * nodeIndexes.asIntBuffer(); nodeVertices = ByteBuffer.allocateDirect(3
		 * * maxNodes * Float.SIZE) .order(ByteOrder.nativeOrder());
		 * nodeVerticesF = nodeVertices.asFloatBuffer();
		 * 
		 * 
		 * if (enableColorPointer) { nodeColors = ByteBuffer.allocateDirect(4 *
		 * maxNodes * Float.SIZE) .order(ByteOrder.nativeOrder()); nodeColorsF =
		 * nodeColors.asFloatBuffer(); } else { nodeColors =
		 * ByteBuffer.allocateDirect(4 * 4 * Float.SIZE).order(
		 * ByteOrder.nativeOrder()); nodeColorsF = nodeColors.asFloatBuffer(); }
		 * 
		 * if (enableSizePointer) { nodeSizes =
		 * ByteBuffer.allocateDirect(maxNodes * Float.SIZE).order(
		 * ByteOrder.nativeOrder()); nodeSizesF = nodeColors.asFloatBuffer(); }
		 * else { nodeSizes = ByteBuffer.allocateDirect(4 * 4 *
		 * Float.SIZE).order( ByteOrder.nativeOrder()); nodeColorsF =
		 * nodeColors.asFloatBuffer(); }
		 * 
		 * edges = ByteBuffer.allocateDirect(2 * maxEdges).order(
		 * ByteOrder.nativeOrder()); edgesI = edges.asIntBuffer();
		 */

		enableColorPointer = ctx.getNodeColorMode() == NodeColorMode.EachNodeOneColor;
		enableSizePointer = ctx.getNodeSizeMode() == NodeSizeMode.EachNodeOneSize;

		allocateBuffers(maxNodes, maxEdges);

		for (int i = 0; i < maxNodes; i++)
			setNodePoolIndex(i, i);

		nodeID2Index = new HashMapID2Index();
		nodeID2Index.init(maxNodes);

		edgeID2Index = new HashMapID2Index();
		edgeID2Index.init(maxEdges);

		long size = 0;
		for (ByteBuffer buffer : buffers.values())
			size += buffer.capacity();

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
		
		java.util.Random random = new java.util.Random();
		for(int i=0; i<maxNodes; i++)
			setNodeSize(i,random.nextInt(10)+1);
	}

	private void setNodePoolIndex(int index, int value) {
		nodeIndexes.put(index, value);
	}

	private int getNodePoolIndex(int index) {
		return nodeIndexes.get(index);
	}

	private void setNodeX(int poolIndex, float x) {
		nodeVertices.put(poolIndex * 3 + 0, x);
	}

	private void setNodeY(int poolIndex, float y) {
		nodeVertices.put(poolIndex * 3 + 1, y);
	}

	private void setNodeZ(int poolIndex, float z) {
		nodeVertices.put(poolIndex * 3 + 2, z);
	}

	private void setNodeColor(int poolIndex, NodeColor color, float value) {
		if (enableColorPointer)
			nodeColors.put(poolIndex * 4 + color.ordinal(), value);
	}

	private void setNodeSize(int poolIndex, float size) {
		if (enableSizePointer)
			nodeSizes.put(poolIndex, size);
	}

	protected void checkNodeCoords(String nodeId, String attr, Object value) {
		int index = nodeID2Index.getIndex(nodeId);

		if (index != -1) {
			float x, y, z;

			x = y = z = 0;

			if (value instanceof Object[]) {
				Object[] varray = (Object[]) value;

				if (varray[0] instanceof Float) {
					Float[] ar = new Float[varray.length];
					for (int i = 0; i < varray.length; i++)
						ar[i] = (Float) varray[i];
					value = ar;
				} else if (varray[0] instanceof Double) {
					Double[] ar = new Double[varray.length];
					for (int i = 0; i < varray.length; i++)
						ar[i] = (Double) varray[i];
					value = ar;
				}
			}

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
			} else if (value instanceof Double[]) {
				Double[] varray = (Double[]) value;

				x = varray.length > 0 ? varray[0].floatValue() : 0;
				y = varray.length > 1 ? varray[1].floatValue() : 0;
				z = varray.length > 2 ? varray[2].floatValue() : 0;
			} else
				System.err.printf("unknown coords type : %s%n",
						value.getClass());

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
				nodeColors.put(poolIndex * 4 + i, rgba[i]);
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

			// System.out.printf("%d <--> %d%n", lastNodeIndex, index);
		}

		if (lastNodeIndex >= 0)
			lastNodeIndex--;

		// System.out.printf("after del, last index is : %d%n", lastNodeIndex);
	}

	public void nodeAdded(String sourceId, long timeId, String nodeId) {
		if (lastNodeIndex >= nodeIndexes.capacity() - 1)
			throw new OutOfMemoryError(String.format(
					"out of memory for nodes%n"
							+ "set \"gs.gl.maxnodes\" to an higher value%n"
							+ "current value: %d nodes%n",
					nodeIndexes.capacity()));

		int index = ++lastNodeIndex;
		int poolIndex = getNodePoolIndex(index);

		nodeID2Index.setIndex(nodeId, index);

		setNodeX(poolIndex, 0);
		setNodeY(poolIndex, 0);
		setNodeZ(poolIndex, 0);

		setupNodeStyle(nodeId, null);
	}

	public void nodeRemoved(String sourceId, long timeId, String nodeId) {
		int index = nodeID2Index.getIndex(nodeId);

		if (index != -1)
			removeNodeInMemory(index);
	}

	public void edgeAdded(String sourceId, long timeId, String edgeId,
			String fromNodeId, String toNodeId, boolean directed) {
		if (2 * lastEdgeIndex >= edges.capacity() - 1)
			throw new OutOfMemoryError(String.format(
					"out of memory for edges%n"
							+ "set \"gs.gl.maxedges\" to an higher value%n"
							+ "current value: %d nodes%n", edges.capacity()));

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

		edges.put(2 * index + 0, indexA);
		edges.put(2 * index + 1, indexB);
	}

	public void edgeRemoved(String sourceId, long timeId, String edgeId) {
		// TODO Auto-generated method stub

	}

	public void graphAttributeAdded(String sourceId, long timeId,
			String attribute, Object value) {
		if (attribute.equals("ui.stylesheet")) {
			if (value instanceof String) {
				try {
					stylesheet.load((String) value);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void graphAttributeChanged(String sourceId, long timeId,
			String attribute, Object oldValue, Object newValue) {
		if (attribute.equals("ui.stylesheet")) {
			if (newValue instanceof String) {
				try {
					stylesheet.load((String) newValue);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void graphAttributeRemoved(String sourceId, long timeId,
			String attribute) {
		if (attribute.equals("ui.stylesheet")) {
			stylesheet.clear();
		}
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

		if (index >= 0) {
			setNodeX(index, x);
			setNodeY(index, y);
			setNodeZ(index, z);
		}
	}

	public void nodeInfos(String id, float dx, float dy, float dz) {
		// TODO Auto-generated method stub

	}

	public void edgeChanged(String id, float[] points) {
		// TODO Auto-generated method stub

	}

	public void nodesMoved(Map<String, float[]> nodes) {
		for (String id : nodes.keySet()) {
			float[] xyz = nodes.get(id);
			nodeMoved(id, xyz[0], xyz[1], xyz[2]);
		}
	}

	public void edgesChanged(Map<String, float[]> edges) {
		// TODO Auto-generated method stub

	}

	public void stepCompletion(float percent) {
		// TODO Auto-generated method stub

	}

	public void setupNodeStyle(String id, String clazz) {
		StyleSheet.NameSpace namespace = stylesheet.getNodeStyleNameSpace();

		// One node
		if (id != null) {
			if (clazz != null) {
				System.err
						.printf("css class not implemented yet for this viewer%n");
			}

			Rule idRule = namespace.byId.get(id);

			if (idRule == null)
				idRule = namespace.defaultRule;

			applyRuleToNode(id, idRule);
		}
		// All nodes with specific class
		else if (clazz != null) {
			System.err
					.printf("css class not implemented yet for this viewer%n");
		}
		// All nodes
		else {
			System.err.printf("here%n");
			for (String nodeId : nodeID2Index.eachID()) {
				setupNodeStyle(nodeId, null);
			}
		}
	}

	protected void applyRuleToNode(String id, Rule rule) {
		Style style = rule.style;
		int index = nodeID2Index.getIndex(id);

		if (index >= 0) {
			if (style.getParent() != null && style.getParent() != rule)
				applyRuleToNode(id, rule.style.getParent());

			if (style.hasValue("fill-color")) {
				switch (style.getFillColorCount()) {
				case 0:
					System.err
							.printf("WTF ? No color, but fill-color defined%n");
					break;
				case 1:
					Color c = style.getFillColor(0);

					setNodeColor(getNodePoolIndex(index), NodeColor.RED,
							c.getRed() / 255.0f);
					setNodeColor(getNodePoolIndex(index), NodeColor.GREEN,
							c.getGreen() / 255.0f);
					setNodeColor(getNodePoolIndex(index), NodeColor.BLUE,
							c.getBlue() / 255.0f);
					setNodeColor(getNodePoolIndex(index), NodeColor.ALPHA,
							c.getAlpha() / 255.0f);

					break;
				default:
					System.err.printf("multiple color not implemented%n");
					break;
				}
			}
		}
	}

	public void styleAdded(Rule oldRule, Rule newRule) {
		System.out.printf("new rule: %s %s %s%n", newRule.selector.type,
				newRule.selector.id, newRule.selector.clazz);
		switch (newRule.selector.type) {
		case GRAPH:
			break;
		case NODE:
			setupNodeStyle(newRule.selector.getId(),
					newRule.selector.getClazz());
			break;
		case EDGE:
		}
	}

	public void styleSheetCleared() {
		// TODO Auto-generated method stub

	}
}
