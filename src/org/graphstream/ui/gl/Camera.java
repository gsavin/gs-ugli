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
package org.graphstream.ui.gl;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

public class Camera
{
	protected final float [] watched = { 0, 0, 0 };
	protected float distance = 2;
	protected float gamma = 0;
	protected float teta = 0;
	
	protected final float [] eye = { 0, 0, 0 };
	
	protected float zoomFactor = 0.1f;
	
	public void pushModelView( GL2 gl, GLU glu )
	{
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		
		eye [0] = (float) ( distance * Math.sin(gamma) * Math.cos(teta) );
		eye [1] = (float) ( distance * Math.sin(gamma) * Math.sin(teta) );
		eye [2] = (float) ( distance * Math.cos(gamma) );
		
		glu.gluLookAt(
				eye [0], eye [1], eye [2],
				watched [0], watched [1], watched [2],
				0,1,0);
		
		gamma += 0.001f;
		teta  += 0.004f;
	}
	
	public void popModelView( GL2 gl )
	{
		gl.glPopMatrix();
		gl.glMatrixMode(GL2.GL_MODELVIEW);
	}
	
	public void zoomIn()
	{
		distance *= ( 1 - zoomFactor );
	}
	
	public void zoomOut()
	{
		distance *= ( 1 + zoomFactor );
	}
}
