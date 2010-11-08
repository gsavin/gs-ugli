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
import javax.media.opengl.GLProfile;
import javax.swing.JPanel;

import org.graphstream.stream.Source;
import org.graphstream.ui.layout.Layout;
import org.graphstream.ui.layout.LayoutRunner;
import org.graphstream.ui.layout.springbox.SpringBox;

public class JOGLViewer extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7148882898922182755L;

	/*
	 * Improves openGL performances.
	 */
	static {
		GLProfile.initSingleton();
	}

	GLProfile glp;
	GLCapabilities glc;

	Context ctx;

	Source source;

	LayoutRunner layout;
	Layout layoutAlgorithm;
	
	public JOGLViewer(Source source) {
		this(source, true, Context.EngineType.NEWT);
	}

	public JOGLViewer(Source source, boolean autoLayout) {
		this(source, autoLayout, Context.EngineType.NEWT);
	}

	public JOGLViewer(Source source, boolean autoLayout,
			Context.EngineType engineType) {
		this.source = source;

		glp = GLProfile.getDefault();
		glc = new GLCapabilities(glp);

		ctx = new Context(source, engineType);
		ctx.init(glc, "The GraphStream GL Viewer", 600, 600);

		if (autoLayout)
			enableAutoLayout();
	}

	public void enableAutoLayout() {
		enableAutoLayout(new SpringBox(true));
	}

	public void enableAutoLayout(Layout layoutAlgorithm) {
		disableAutoLayout();
		layout = new LayoutRunner(source, layoutAlgorithm, true);
		layoutAlgorithm.addListener(ctx.getRenderer().getLayoutListener());
		this.layoutAlgorithm = layoutAlgorithm;
	}

	public void disableAutoLayout() {
		if (layout != null) {
			layoutAlgorithm.removeListener(ctx.getRenderer().getLayoutListener());
			layout.release();
			layout = null;
		}
	}
}
