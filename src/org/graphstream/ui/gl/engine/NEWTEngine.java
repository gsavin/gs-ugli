package org.graphstream.ui.gl.engine;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;

import org.graphstream.ui.gl.Engine;

import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;

public class NEWTEngine implements Engine {
	protected GLWindow window;

	public void init(GLCapabilities caps) {
		if (window != null) {
			// TODO
		}

		window = GLWindow.create(caps);

		window.addWindowListener(new WindowAdapter() {
			public void windowDestroyNotify(WindowEvent arg0) {
				System.exit(0);
			};
		});
		
		System.out.printf("using NEWT engine [display=(%s,%s)]%n",window.getScreen().getDisplay().getType(),window.getScreen().getDisplay().getName());
	}

	public void setWindowSize(int width, int height) {
		window.setSize(width, height);
	}

	public void addGLEventListener(GLEventListener l) {
		window.addGLEventListener(l);
	}

	public void setWindowTitle(String title) {
		window.setTitle(title);
	}

	public void setWindowVisible(boolean on) {
		window.setVisible(on);
	}

	public GLAutoDrawable getGLAutoDrawable() {
		return window;
	}

	public void addKeyListener(KeyListener l) {
		window.addKeyListener(l);
	}
	
	public void removeKeyListener(KeyListener l) {
		window.removeKeyListener(l);
	}
	
	public void setFullscreen(boolean on) {
		window.setFullscreen(on);
	}
	
	public boolean isFullscreen() {
		return window.isFullscreen();
	}
}
