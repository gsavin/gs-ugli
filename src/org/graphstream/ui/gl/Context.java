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
import org.graphstream.ui.gl.engine.AWTEngine;
import org.graphstream.ui.gl.engine.NEWTEngine;
import org.graphstream.ui.gl.engine.SWINGEngine;
import org.graphstream.ui.gl.event.KeyManager;
import org.graphstream.ui.gl.renderer.GraphicGraphRenderer;
import org.graphstream.ui.gl.renderer.VertexArrayRenderer;

import com.jogamp.opengl.util.FPSAnimator;

public class Context {
	public static enum NodeColorMode {
		AllNodeOneColor, EachNodeOneColor
	}

	public static enum EdgeColorMode {
		AllEdgeOneColor, EachEdgeOneColor, ExtremitiesBlending
	}
	
	public static enum NodeSizeMode {
		AllNodeOneSize, EachNodeOneSize
	}

	public static enum EngineType {
		AWT(AWTEngine.class), SWING(SWINGEngine.class), NEWT(NEWTEngine.class)
		;
		final Class<? extends Engine> clazz;

		private EngineType(Class<? extends Engine> clazz) {
			this.clazz = clazz;
		}
	}

	public static enum RendererType {
		GRAPHIC_GRAPH, VERTEX_ARRAY
	}

	protected Source source;
	protected Camera camera;

	protected NodeColorMode nodeColorMode;
	protected NodeSizeMode nodeSizeMode;
	
	protected Fog fog;

	protected Engine engine;

	protected GraphRenderer renderer;

	protected KeyManager keyManager;

	protected boolean displayInfos = false;

	protected boolean displayCompass = true;

	protected final boolean use3d;

	public Context(Source source, EngineType engineType,
			RendererType rendererType) {
		this.use3d = true;
		this.source = source;
		this.camera = new Camera(this);
		this.nodeColorMode = NodeColorMode.EachNodeOneColor;
		this.nodeSizeMode = NodeSizeMode.EachNodeOneSize;
		this.fog = new Fog();

		try {
			this.engine = engineType.clazz.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			System.exit(1);
		}

		switch (rendererType) {
		case GRAPHIC_GRAPH:
			this.renderer = new GraphicGraphRenderer(this, true);
			break;
		case VERTEX_ARRAY:
			this.renderer = new VertexArrayRenderer(this);
			break;
		}

		this.keyManager = new KeyManager(this);
	}

	public void init(GLCapabilities caps, boolean initWindow, String title,
			int width, int height) {
		engine.init(caps);

		if (initWindow) {
			engine.setWindowSize(width, height);
			engine.setWindowVisible(true);
			engine.setWindowTitle(title);
		}

		engine.addGLEventListener(renderer);
		engine.addKeyListener(keyManager);

		FPSAnimator animator = new FPSAnimator(engine.getGLAutoDrawable(), 60);
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
	
	public NodeSizeMode getNodeSizeMode() {
		return nodeSizeMode;
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

	public void toggleFullscreen() {
		engine.setFullscreen(!engine.isFullscreen());
	}

	public void setDisplayInfos(boolean on) {
		this.displayInfos = on;
	}

	public boolean isInfosDisplayed() {
		return displayInfos;
	}

	public boolean isCompassDisplayed() {
		return displayCompass;
	}

	public boolean is3DView() {
		return use3d;
	}
}
