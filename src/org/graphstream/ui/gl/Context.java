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

import javax.media.opengl.GLCapabilities;

import org.graphstream.stream.Source;
import org.graphstream.ui.gl.engine.NEWTEngine;
import org.graphstream.ui.gl.engine.SWINGEngine;
import org.graphstream.ui.gl.event.KeyManager;
import org.graphstream.ui.gl.renderer.VertexArrayRenderer;

import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.FPSAnimator;

public class Context {
	public static enum NodeColorMode {
		AllNodeOneColor, EachNodeOneColor
	}

	public static enum EdgeColorMode {
		AllEdgeOneColor, EachEdgeOneColor, ExtremitiesBlending
	}

	public static enum EngineType {
		AWT, SWING, NEWT
	}

	protected Source source;
	protected Camera camera;

	protected NodeColorMode nodeColorMode;

	protected Fog fog;
	
	protected Engine engine;

	protected GraphRenderer renderer;
	
	protected KeyManager keyManager;
	
	public Context(Source source,EngineType engineType) {
		this.source = source;
		this.camera = new Camera(this);
		this.nodeColorMode = NodeColorMode.EachNodeOneColor;
		
		this.fog = new Fog();

		switch (engineType) {
		case AWT:
		case SWING:
			engine = new SWINGEngine();
			break;
		case NEWT:
			engine = new NEWTEngine();
			break;
		}
		
		this.renderer = new VertexArrayRenderer(this);
		this.keyManager = new KeyManager(this);
	}
	
	public void init(GLCapabilities caps,String title, int width, int height) {
		engine.init(caps);
		engine.setWindowSize(width,height);
		engine.setWindowVisible(true);
		engine.setWindowTitle(title);
		engine.addGLEventListener(renderer);
		engine.addKeyListener(keyManager);

		Animator animator = new FPSAnimator(
				engine.getGLAutoDrawable(), 60);
		animator.add(engine.getGLAutoDrawable());
		animator.start();
	}

	public Camera getCamera() {
		return camera;
	}

	public Source getSource() {
		return source;
	}

	public NodeColorMode getNodeColorMode() {
		return nodeColorMode;
	}
	
	public Fog getFog() {
		return fog;
	}
	
	public Engine getEngine() {
		return engine;
	}
	
	public GraphRenderer getRenderer() {
		return renderer;
	}
}
