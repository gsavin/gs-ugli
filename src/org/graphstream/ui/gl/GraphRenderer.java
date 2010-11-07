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

import java.awt.Font;
import java.io.InputStream;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;

import org.graphstream.ui.layout.LayoutListener;

import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.gl2.GLUT;

public abstract class GraphRenderer implements GLEventListener {
	protected GLU glu;
	protected GLUT glut;

	protected int width;
	protected int height;

	protected Context ctx;

	protected int frames;
	protected long firstFrameDate;
	protected int fps;

	protected TextRenderer textRenderer;

	protected int nodeCount, edgeCount;

	public GraphRenderer(Context ctx) {
		this.ctx = ctx;
		width = height = 1;
		frames = 0;
		firstFrameDate = System.currentTimeMillis();

		InputStream fontIn = ClassLoader
				.getSystemResourceAsStream("org/graphstream/ui/gl/resource/verdanab.ttf");

		Font font = null;

		try {
			font = Font.createFont(Font.TRUETYPE_FONT, fontIn)
					.deriveFont(18.0f);
		} catch (Exception e) {
			e.printStackTrace();
		}

		textRenderer = new TextRenderer(font);
	}

	public void init(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();

		gl.glEnable(GL2.GL_POINT_SMOOTH);

		gl.glClearDepth(65535);

		gl.glClearColor(0.2f, 0.2f, 0.2f, 0.0f);
		gl.glEnable(GL.GL_BLEND);
		gl.glEnable(GL.GL_LINE_SMOOTH);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

		glu = GLU.createGLU();
		glut = new GLUT();

		initFog(gl);
		setLighting(gl);
	}

	public void dispose(GLAutoDrawable drawable) {
		// TODO Auto-generated method stub

	}

	public void display(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		setFog(gl);
		
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

		drawBackground(gl);

		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glFrustum(-1, 1, -1, 1, 1, 10);

		drawContainer(gl);
		
		ctx.getCamera().pushModelView(gl, glu);
		renderGraph(gl);
		ctx.getCamera().popModelView(gl);

		gl.glPopMatrix();
		gl.glMatrixMode(GL2.GL_PROJECTION);

		drawFPS(gl);

		gl.glFlush();
	}

	protected abstract void renderGraph(GL2 gl);
	
	public abstract LayoutListener getLayoutListener();
	
	public void initFog(GL2 gl) {
		gl.glEnable(GL2.GL_FOG);
		gl.glFogi(GL2.GL_FOG_MODE, GL2.GL_EXP2);
		gl.glFogfv(GL2.GL_FOG_COLOR, new float[] { 0.2f, 0.2f, 0.2f, 1.0f }, 0);
		gl.glHint(GL2.GL_FOG_HINT, GL.GL_NICEST);
		setFog(gl);
	}
	
	protected void setFog(GL2 gl) {
		Fog fog = ctx.getFog();
		
		gl.glFogf(GL2.GL_FOG_DENSITY, fog.getFogDensity() );//0.1f);
		gl.glFogf(GL2.GL_FOG_START, fog.getFogStart() );//1);
		gl.glFogf(GL2.GL_FOG_END, fog.getFogEnd() );//5f);
	}

	protected void setLighting(GL2 gl) {

	}

	protected void drawContainer(GL2 gl) {
		gl.glColor4f(0.4f, 0.4f, 0.4f, 0.5f);

		gl.glBegin(GL2.GL_LINE_STRIP);

		gl.glVertex3f(-1, -1, -1);
		gl.glVertex3f(1, -1, -1);
		gl.glVertex3f(1, 1, -1);
		gl.glVertex3f(-1, 1, -1);

		gl.glVertex3f(-1, -1, -1);
		gl.glVertex3f(1, -1, -1);
		gl.glVertex3f(1, -1, 1);
		gl.glVertex3f(-1, -1, 1);

		gl.glVertex3f(-1, -1, -1);
		gl.glVertex3f(-1, 1, -1);
		gl.glVertex3f(-1, 1, 1);
		gl.glVertex3f(-1, 1, -1);

		gl.glEnd();

		gl.glBegin(GL2.GL_LINE_STRIP);

		gl.glVertex3f(1, 1, 1);
		gl.glVertex3f(-1, 1, 1);
		gl.glVertex3f(-1, -1, 1);
		gl.glVertex3f(1, -1, 1);

		gl.glVertex3f(1, 1, 1);
		gl.glVertex3f(-1, 1, 1);
		gl.glVertex3f(-1, 1, -1);
		gl.glVertex3f(1, 1, -1);

		gl.glVertex3f(1, 1, 1);
		gl.glVertex3f(1, -1, 1);
		gl.glVertex3f(1, -1, -1);
		gl.glVertex3f(1, 1, -1);

		gl.glEnd();
	}

	protected void drawBackground(GL2 gl) {
		gl.glDisable(GL.GL_DEPTH_TEST);
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();

		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glDisable(GL2.GL_LIGHTING);

		glu.gluOrtho2D(0, width, 0, height);// -1, 1, -1, 1 );

		gl.glBegin(GL2.GL_LINES);

		gl.glColor3f(0.21f, 0.211f, 0.21f);

		for (float i = 0; i <= width; i += 10) {
			gl.glVertex2f(i, 0);
			gl.glVertex2f(i, height);
		}

		for (float i = 0; i <= height; i += 10) {
			gl.glVertex2f(0, i);
			gl.glVertex2f(width, i);
		}

		gl.glEnd();

		gl.glBegin(GL2.GL_QUADS);

		gl.glColor4f(0, 0, 0, 0.3f);
		gl.glVertex2f(0, 0);
		gl.glVertex2f(width, 0);
		gl.glColor4f(0, 0, 0, 0.1f);
		gl.glVertex2f(width, height / 2.0f);
		gl.glVertex2f(0, height / 2.0f);

		gl.glColor4f(0, 0, 0, 0.3f);
		gl.glVertex2f(0, height);
		gl.glVertex2f(width, height);
		gl.glColor4f(0, 0, 0, 0.1f);
		gl.glVertex2f(width, height / 2.0f);
		gl.glVertex2f(0, height / 2.0f);

		gl.glEnd();

		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glPopMatrix();
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glEnable(GL.GL_DEPTH_TEST);
	}

	protected void drawFPS(GL2 gl) {
		if (System.currentTimeMillis() - firstFrameDate > 2000) {
			long time = System.currentTimeMillis() - firstFrameDate;
			fps = (int) (1000 * frames / time);
			frames = 0;
			firstFrameDate = System.currentTimeMillis();
		}

		frames++;

		textRenderer.beginRendering(width, height);
		textRenderer.setColor(1, 1, 1, 0.6f);
		textRenderer.draw(String.format("%d nodes, %d edges @ %d fps",
				nodeCount, edgeCount, fps), 10, 10);
		textRenderer.endRendering();
	}

	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
			int height) {
		this.width = width;
		this.height = height;
	}
}
