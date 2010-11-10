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
package org.graphstream.ui.gl.renderer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.Buffer;
import java.nio.IntBuffer;
import java.util.LinkedList;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2GL3;
import javax.media.opengl.GL3;
import javax.media.opengl.GLAutoDrawable;

import org.graphstream.ui.gl.Context;
import org.graphstream.ui.gl.GraphRenderer;
import org.graphstream.ui.graphicGraph.stylesheet.StyleSheetListener;
import org.graphstream.ui.layout.LayoutListener;

import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import com.jogamp.opengl.util.glsl.ShaderState;
import com.jogamp.opengl.util.glsl.ShaderUtil;

public class VertexArrayRenderer extends GraphRenderer {
	protected static final String NODE_VERTEX_SHADER = "org/graphstream/ui/gl/shader/node-vertex-shader.glsl";
	protected static final String NODE_FRAGMENT_SHADER = "org/graphstream/ui/gl/shader/node-fragment-shader.glsl";

	protected GraphBuffers graphBuffers;

	protected Buffer vertexPointer;
	protected Buffer indexPointer;
	protected Buffer edgesPointer;
	protected Buffer colorPointer;
	protected Buffer sizePointer;

	protected ShaderProgram shaderProgram;
	protected ShaderState shaderState;

	public VertexArrayRenderer(Context ctx) {
		super(ctx);

		int maxNodes = Integer.parseInt(System.getProperty("gs.gl.maxnodes",
				"10000"));
		int maxEdges = Integer.parseInt(System.getProperty("gs.gl.maxedges",
				"10000"));

		graphBuffers = new GraphBuffers(ctx);
		graphBuffers.init(ctx, maxNodes, maxEdges);

		ctx.getSource().addSink(graphBuffers);

		vertexPointer = graphBuffers.createNewVertexBufferView();
		indexPointer = graphBuffers.createNewIndexBufferView();
		edgesPointer = graphBuffers.createNewEdgeBufferView();
		colorPointer = graphBuffers.createNewNodeColorView();
		sizePointer = graphBuffers.createNewNodeSizeView();
	}

	public void init(GLAutoDrawable drawable) {
		super.init(drawable);
		shaderProgram = loadShader2(NODE_VERTEX_SHADER, NODE_FRAGMENT_SHADER,
				drawable.getGL().getGL2());
	}

	protected String[] getResourceContent(String url) {
		InputStream resourceContent = ClassLoader
				.getSystemResourceAsStream(url);
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				resourceContent));
		
		LinkedList<String> content = new LinkedList<String>();

		try {
			while (reader.ready())
				content.add(reader.readLine());

			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String[] array = new String[content.size()];
		return content.toArray(array);
	}
	
	protected ShaderProgram loadShader2(String vertexShader,
			String fragmentShader, GL2 gl) {
		if(!ShaderUtil.isShaderCompilerAvailable(gl)) {
			System.err.printf("[shader] no compiler available%n");
			return null;
		}
		
		ShaderProgram shaderProgram;

		ShaderCode vertexShaderCode = new ShaderCode(GL2.GL_VERTEX_SHADER, 1,
				new String[][] { getResourceContent(vertexShader) });

		if (!vertexShaderCode.compile(gl,System.out))
			System.err.printf("[shader] \"%s\" invalid%n", vertexShader);

		ShaderCode fragmentShaderCode = new ShaderCode(GL2.GL_FRAGMENT_SHADER, 1,
				new String[][] { getResourceContent(fragmentShader) });

		if (!fragmentShaderCode.compile(gl,System.out))
			System.err.printf("[shader] \"%s\" invalid%n", fragmentShader);

		shaderProgram = new ShaderProgram();
		shaderProgram.add(vertexShaderCode);
		//shaderProgram.add(fragmentShaderCode);

		shaderProgram.link(gl, System.out);
		shaderState = new ShaderState();
		shaderState.attachShaderProgram(gl, shaderProgram);
		shaderState.glBindAttribLocation(gl, 0, "nodeSize");


		if (!shaderProgram.linked())
			System.err.printf("[shader] error while linking program%n");
		else if(ShaderUtil.isProgramValid(gl, shaderProgram.program()))
			System.out.printf("[shader] program linked and valid%n");
		else
			System.err.printf("[shader] invalid program%n");
		
		return shaderProgram;
	}

	protected int loadShader(String vertexShader, String fragmentShader, GL2 gl) {
		int shaderProgram = -1;
		InputStream vertexShaderIn = ClassLoader
				.getSystemResourceAsStream(vertexShader);
		InputStream fragmentShaderIn = ClassLoader
				.getSystemResourceAsStream(fragmentShader);

		if (vertexShaderIn != null && fragmentShaderIn != null) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					vertexShaderIn));
			String vertexShaderCode = "";

			try {
				while (reader.ready())
					vertexShaderCode += reader.readLine();

				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			reader = new BufferedReader(new InputStreamReader(fragmentShaderIn));
			String fragmentShaderCode = "";

			try {
				while (reader.ready())
					fragmentShaderCode += reader.readLine();

				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			gl.glEnable(GL2.GL_VERTEX_PROGRAM_POINT_SIZE);

			System.out.printf("[shader] load vertex shader \"%s\"%n",
					vertexShader);

			int v = gl.glCreateShader(GL2.GL_VERTEX_SHADER);

			gl.glShaderSource(v, 1, new String[] { vertexShaderCode },
					(int[]) null, 0);
			gl.glCompileShader(v);

			System.out.printf("[shader] load fragment shader \"%s\"%n",
					fragmentShader);

			int f = gl.glCreateShader(GL2.GL_FRAGMENT_SHADER);

			gl.glShaderSource(f, 1, new String[] { fragmentShaderCode },
					(int[]) null, 0);
			gl.glCompileShader(v);

			int[] status = new int[1];

			shaderProgram = gl.glCreateProgram();
			gl.glAttachShader(shaderProgram, v);
			gl.glAttachShader(shaderProgram, f);

			gl.glBindAttribLocation(shaderProgram, 1, "nodeSize");

			gl.glLinkProgram(shaderProgram);
			gl.glGetProgramiv(shaderProgram, GL2.GL_LINK_STATUS, status, 0);

			if (status[0] != GL2.GL_TRUE) {
				System.err.printf("shader program link failed%n");

				byte[] infolog = new byte[8000];
				int[] length = new int[1];
				gl.glGetProgramInfoLog(shaderProgram, 8000, length, 0, infolog,
						0);

				if (length[0] > 0) {
					String log = new String(infolog, 0, length[0]);
					System.err.printf("%s%n", log);
				}
			} else
				System.out.printf("[shader] link success%n");

			gl.glValidateProgram(shaderProgram);
			gl.glGetProgramiv(shaderProgram, GL2.GL_VALIDATE_STATUS, status, 0);

			if (status[0] != GL2.GL_TRUE) {
				System.err.printf("shader program validation failed%n");

				byte[] infolog = new byte[8000];
				int[] length = new int[1];
				gl.glGetProgramInfoLog(shaderProgram, 8000, length, 0, infolog,
						0);

				if (length[0] > 0) {
					String log = new String(infolog, 0, length[0]);
					System.err.printf("%s%n", log);
				}
			} else
				System.out.printf("[shader] validation success%n");

			int[] active_attributes = new int[1], max_length = new int[1];

			gl.glGetProgramiv(shaderProgram, GL2.GL_ACTIVE_ATTRIBUTES,
					active_attributes, 0);
			gl.glGetProgramiv(shaderProgram,
					GL2.GL_ACTIVE_ATTRIBUTE_MAX_LENGTH, max_length, 0);

			if (active_attributes[0] == 0)
				System.out.printf("[shader] no active attributes%n");

			for (int i = 0; i < active_attributes[0]; i++) {
				int[] size = new int[1], type = new int[1], length = new int[1];
				byte[] buffer = new byte[max_length[0] + 1];
				gl.glGetActiveAttrib(shaderProgram, i, max_length[0] + 1,
						length, 0, size, 0, type, 0, buffer, 0);
				String name = new String(buffer, 0, length[0]);
				String typeName;

				switch (type[0]) {
				case GL2.GL_FLOAT:
					typeName = "float";
					break;
				case GL2.GL_FLOAT_VEC2:
					typeName = "vec2";
					break;
				case GL2.GL_FLOAT_VEC3:
					typeName = "vec3";
					break;
				case GL2.GL_FLOAT_VEC4:
					typeName = "vec4";
					break;
				case GL2.GL_FLOAT_MAT2:
					typeName = "mat2";
					break;
				case GL2.GL_FLOAT_MAT3:
					typeName = "mat3";
					break;
				case GL2.GL_FLOAT_MAT4:
					typeName = "mat4";
					break;
				case GL2.GL_FLOAT_MAT2x3:
					typeName = "mat2x3";
					break;
				case GL2.GL_FLOAT_MAT2x4:
					typeName = "mat2x4";
					break;
				case GL2.GL_FLOAT_MAT3x2:
					typeName = "mat3x2";
					break;
				case GL2.GL_FLOAT_MAT3x4:
					typeName = "mat3x4";
					break;
				case GL2.GL_FLOAT_MAT4x2:
					typeName = "mat4x2";
					break;
				case GL2.GL_FLOAT_MAT4x3:
					typeName = "mat4x3";
					break;
				default:
					typeName = "unknown";
					break;
				}

				System.out.printf(
						"[shader] attribute \"%s %s\" is at location %d\n",
						typeName, name,
						gl.glGetAttribLocation(shaderProgram, name));
			}
		}

		return shaderProgram;
	}

	int[] currentProgram = new int[1];

	protected void renderGraph(GL2 gl) {
		gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
		gl.glVertexPointer(graphBuffers.getVertexComposantCount(), GL.GL_FLOAT,
				0, vertexPointer.rewind());

		if (graphBuffers.isColorPointerEnabled()) {
			gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
			gl.glColorPointer(graphBuffers.getColorComposantCount(),
					GL.GL_FLOAT, 0, colorPointer.rewind());
		} else {
			gl.glColor4f(1, 1, 1, 0.4f);
		}

		indexPointer.position(0);
		indexPointer.limit(graphBuffers.getActiveIndexCount());
		nodeCount = indexPointer.limit();

		if (graphBuffers.isSizePointerEnabled()) {
			/*
			 * int sizeLoc = gl.glGetAttribLocation(shaderProgram, "nodeSize");
			 * 
			 * if (sizeLoc < 0) System.err.printf("bad attribute location%n");
			 * 
			 * sizePointer.rewind();
			 * 
			 * gl.glGetIntegerv(GL2.GL_CURRENT_PROGRAM, currentProgram, 0);
			 * 
			 * gl.glEnable(GL2.GL_VERTEX_PROGRAM_POINT_SIZE);
			 * 
			 * gl.glUseProgram(shaderProgram);
			 * 
			 * gl.glVertexAttribPointer(sizeLoc, nodeCount, GL.GL_FLOAT, false,
			 * 0, sizePointer);
			 * 
			 * gl.glEnableVertexAttribArray(sizeLoc);
			 */
			gl.glEnable(GL2.GL_VERTEX_PROGRAM_POINT_SIZE);
			
			shaderState.glUseProgram(gl, true);
		} else {
			gl.glPointSize(10);
		}

		gl.glDrawElements(GL.GL_POINTS, indexPointer.limit(),
				GL2.GL_UNSIGNED_INT, indexPointer);

		if (graphBuffers.isSizePointerEnabled()) {
			gl.glUseProgram(currentProgram[0]);
		}

		edgesPointer.position(0);
		edgesPointer.limit(2 * graphBuffers.getActiveEdgeCount());
		edgeCount = edgesPointer.limit() / 2;

		gl.glDrawElements(GL.GL_LINES, edgesPointer.limit(),
				GL2.GL_UNSIGNED_INT, edgesPointer);

		if (graphBuffers.isColorPointerEnabled())
			gl.glDisableClientState(GL2.GL_COLOR_ARRAY);

		gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
	}

	public LayoutListener getLayoutListener() {
		return graphBuffers;
	}

	public StyleSheetListener getStyleSheetListener() {
		return graphBuffers;
	}
}
