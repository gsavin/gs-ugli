package org.graphstream.ui.gl;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;

import com.jogamp.newt.event.KeyListener;

public interface Engine {
	void init(GLCapabilities caps);

	void setWindowSize(int width, int height);

	void addGLEventListener(GLEventListener l);
	
	void addKeyListener(KeyListener l);
	
	void removeKeyListener(KeyListener l);

	void setWindowTitle(String title);

	void setWindowVisible(boolean on);

	GLAutoDrawable getGLAutoDrawable();
	
	void setFullscreen(boolean on);
	
	boolean isFullscreen();
}
