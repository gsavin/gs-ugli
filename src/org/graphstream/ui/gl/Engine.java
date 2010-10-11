package org.graphstream.ui.gl;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;

public interface Engine
{
	void init( GLCapabilities caps );
	
	void setWindowSize( int width, int height );
	
	void addGLEventListener( GLEventListener l );
	
	void setWindowTitle( String title );
	
	void setWindowVisible( boolean on );
	
	GLAutoDrawable getGLAutoDrawable();
}
