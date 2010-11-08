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

public class Camera {
	public static enum Mode {
		NODE_TRACKING,
		STATIC
	}
	
	public static enum RotationAnim {
		NONE(0,0),
		LEFT(-1,0),
		RIGHT(1,0),
		UP(0,1),
		DOWN(0,-1)
		;
		float deltaGamma;
		float deltaTeta;
		
		RotationAnim(float deltaGamma, float deltaTeta) {
			this.deltaGamma = deltaGamma;
			this.deltaTeta = deltaTeta;
		}
	}
	
	protected Mode mode;
	
	protected Context ctx;
	
	protected final float[] watched = { 0, 0, 0 };
	protected float distance = 7;
	protected float gamma = 0;
	protected float teta = 0;

	protected final float[] eye = { 0, 0, 0 };

	protected float zoomFactor = 0.1f;

	protected float zFar = 100;
	
	protected RotationAnim rotationAnim = RotationAnim.NONE;
	protected float rotationSpeed = 1;
	
	public Camera(Context ctx) {
		this.ctx = ctx;
	}
	
	public void pushModelView(GL2 gl, GLU glu) {
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		/*
		eye[0] = (float) (distance * Math.sin(gamma) * Math.cos(teta));
		eye[1] = (float) (distance * Math.sin(gamma) * Math.sin(teta));
		eye[2] = (float) (distance * Math.cos(gamma));
		 */
		//glu.gluLookAt(eye[0], eye[1], eye[2], watched[0], watched[1],
				//watched[2], 0, 1, 0);
		glu.gluLookAt(0,0,distance, watched[0], watched[1],
				watched[2], 0, 1, 0);

		gl.glPushMatrix();
		gl.glRotatef(gamma,0,1,0);
		gl.glRotatef(teta,1,0,0);
		
		if( rotationAnim != RotationAnim.NONE ) {
			gamma += rotationAnim.deltaGamma * rotationSpeed;
			gamma %= 360;
			teta += rotationAnim.deltaTeta * rotationSpeed;
			teta %= 360;
		}
	}

	public void popModelView(GL2 gl) {
		gl.glPopMatrix();
		gl.glPopMatrix();
		gl.glMatrixMode(GL2.GL_MODELVIEW);
	}

	public float getZFar() {
		return zFar;
	}
	
	public void rotateLeft() {
		gamma += 1;
		gamma %= 360;
	}

	public void rotateRight() {
		gamma -= 1;
		gamma %= 360;
	}

	public void rotateUp() {
		teta += 1;
		teta %= 360;
	}

	public void rotateDown() {
		teta -= 1;
		teta %= 360;
	}

	public void zoomIn() {
		distance *= (1 - zoomFactor);
	}

	public void zoomOut() {
		distance *= (1 + zoomFactor);
	}
	
	public void increaseZFar() {
		zFar += 1;
	}
	
	public void decreaseZFar() {
		zFar -= 1;
		zFar = Math.max(0,zFar);
	}
	
	public void toggleRotationAnim( RotationAnim ra ) {
		rotationAnim = ra;
	}
	
	public void increaseRotationSpeed() {
		rotationSpeed += 0.1f;
		rotationSpeed = Math.max(0,Math.min(2,rotationSpeed));
	}
	
	public void decreaseRotationSpeed() {
		rotationSpeed -= 0.1f;
		rotationSpeed = Math.max(0,Math.min(2,rotationSpeed));
	}
}
