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
package org.graphstream.ui.gl.engine;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.JFrame;

import org.graphstream.ui.gl.Engine;
import org.graphstream.ui.gl.event.KeyListener_SWING_to_NEWT;

import com.jogamp.newt.event.KeyListener;

public class SWINGEngine implements Engine {
	GLCanvas canvas;
	JFrame frame;

	KeyListener_SWING_to_NEWT keyListeners;
	
	public void init(GLCapabilities caps) {
		if (canvas != null) {
			// TODO
		}

		keyListeners = new KeyListener_SWING_to_NEWT();
		
		canvas = new GLCanvas(caps);
		canvas.addKeyListener(keyListeners);
		if (frame != null) {
			// TODO
		}

		frame = new JFrame();
		frame.add(canvas);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public void setWindowSize(int width, int height) {
		frame.setSize(width, height);
	}

	public void addGLEventListener(GLEventListener l) {
		canvas.addGLEventListener(l);
	}

	public void setWindowTitle(String title) {
		frame.setTitle(title);
	}

	public void setWindowVisible(boolean on) {
		frame.setVisible(on);
	}

	public GLAutoDrawable getGLAutoDrawable() {
		return canvas;
	}

	public void addKeyListener(KeyListener l) {
		keyListeners.add(l);
	}

	public void removeKeyListener(KeyListener l) {
		keyListeners.remove(l);
	}
}
