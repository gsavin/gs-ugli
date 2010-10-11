package org.graphstream.ui.gl;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;

public class BackgroundRenderer
	implements GLEventListener
{
	GLU glu;
	
	public void init(GLAutoDrawable drawable)
	{
		GL2 gl = drawable.getGL().getGL2();
		
		glu = GLU.createGLU();

	    gl.glClearDepth( 65535 );
	    
		gl.glClearColor( 0.2f, 0.2f, 0.2f, 0.0f );
		gl.glEnable( GL.GL_BLEND );
	    gl.glEnable( GL.GL_LINE_SMOOTH );
		gl.glBlendFunc( GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA );
	    gl.glClear( GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT );
	}

	public void dispose(GLAutoDrawable drawable)
	{
		glu.destroy();
	}

	public void display(GLAutoDrawable drawable)
	{
		GL2 gl = drawable.getGL().getGL2();
		
		gl.glDisable(GL.GL_DEPTH_TEST);
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();

		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glDisable(GL2.GL_LIGHTING);
		
		glu.gluOrtho2D( -1, 1, -1, 1 );
		
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
		gl.glBegin( GL2.GL_QUADS );
		gl.glColor4f( 0, 0, 0, 0.5f );
		gl.glVertex2f( -1, -1 );
		gl.glVertex2f( 1, -1 );
		gl.glColor4f( 0, 0, 0, 0 );
		gl.glVertex2f( 1, 1 );
		gl.glVertex2f( -1, 1 );
		gl.glEnd();

		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glPopMatrix();
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glEnable(GL.GL_DEPTH_TEST);
		
		gl.glFlush();
	}

	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
			int height)
	{
		
	}

}
