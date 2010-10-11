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

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

import org.graphstream.ui.gl.Context;
import org.graphstream.ui.gl.GraphRenderer;

public class VertexArrayRenderer
	extends GraphRenderer
{
	protected static final String NODE_SHADER = "org/graphstream/ui/gl/shader/node.glsl";
	
	protected GraphBuffers graphBuffers;
	
	protected Buffer vertexPointer;
	protected Buffer indexPointer;
	protected Buffer edgesPointer;
	protected Buffer colorPointer;
	
	protected int shaderProgram;
	
	public VertexArrayRenderer( Context ctx )
	{
		super( ctx );
		
		int maxNodes = Integer.parseInt( System.getProperty( "gs.gl.maxnodes", "10000" ) );
		int maxEdges = Integer.parseInt( System.getProperty( "gs.gl.maxedges", "10000" ) );
		
		graphBuffers = new GraphBuffers();
		graphBuffers.init( ctx, maxNodes, maxEdges );
		
		ctx.getSource().addSink( graphBuffers );
	
		vertexPointer = graphBuffers.createNewVertexBufferView();
		indexPointer  = graphBuffers.createNewIndexBufferView();
		edgesPointer  = graphBuffers.createNewEdgeBufferView();
		colorPointer  = graphBuffers.createNewNodeColorView();
	}
	
	public void init( GLAutoDrawable drawable )
	{
		super.init(drawable);
		// loadShader(drawable.getGL().getGL2());
	}
	
	protected void loadShader( GL2 gl )
	{
		InputStream in = ClassLoader.getSystemResourceAsStream(NODE_SHADER);
		
		if( in != null )
		{
			BufferedReader reader = new BufferedReader( new InputStreamReader(in) );
			String code = "";
			
			try
			{
				while( reader.ready() )
					code += reader.readLine();
			}
			catch( IOException e )
			{
				e.printStackTrace();
			}
			
			int v = gl.glCreateShader(GL2.GL_VERTEX_SHADER);
			
			gl.glShaderSource(v, 1, new String [] { code }, (int[])null, 0);
			gl.glCompileShader(v);
			
			shaderProgram = gl.glCreateProgram();
			gl.glAttachShader(shaderProgram, v);
			gl.glLinkProgram(shaderProgram);
			gl.glValidateProgram(shaderProgram);
		}
	}
	
	protected void renderGraph( GL2 gl )
	{
		gl.glEnableClientState( GL2.GL_VERTEX_ARRAY );
		gl.glVertexPointer( graphBuffers.getVertexComposantCount(), GL.GL_FLOAT, 0, vertexPointer.rewind() );
		
		if( graphBuffers.isColorPointerEnabled() )
		{
			gl.glEnableClientState( GL2.GL_COLOR_ARRAY );
			gl.glColorPointer( graphBuffers.getVertexComposantCount(), GL.GL_FLOAT, 0, colorPointer.rewind() );
		}
		else
		{
			gl.glColor4f( 1, 1, 1, 0.4f );
		}
		
		gl.glPointSize( 10 );
		
		indexPointer.position(0);
		indexPointer.limit( graphBuffers.getActiveIndexCount() );
		nodeCount = indexPointer.limit();
		
		gl.glDrawElements( GL.GL_POINTS, indexPointer.limit(), GL2.GL_UNSIGNED_INT, indexPointer );
		
		edgesPointer.position(0);
		edgesPointer.limit( 2 * graphBuffers.getActiveEdgeCount() );
		edgeCount = edgesPointer.limit() / 2;
		
		gl.glDrawElements( GL.GL_LINES, edgesPointer.limit(), GL2.GL_UNSIGNED_INT, edgesPointer );
		
		if( graphBuffers.isColorPointerEnabled() )
			gl.glDisableClientState( GL2.GL_COLOR_ARRAY );
		
		gl.glDisableClientState( GL2.GL_VERTEX_ARRAY );
	}
}
