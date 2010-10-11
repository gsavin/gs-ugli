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
import org.graphstream.ui.gl.engine.NEWTEngine;
import org.graphstream.ui.gl.engine.SWINGEngine;
import org.graphstream.ui.gl.renderer.GraphBuffers;
import org.graphstream.ui.gl.renderer.VertexArrayRenderer;

import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.FPSAnimator;

public class JOGLViewer
	extends JPanel
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7148882898922182755L;

	/*
	 * Improves openGL performances.
	 */
	static
	{
		GLProfile.initSingleton();
	}
	
	public static enum EngineType
	{
		AWT,
		SWING,
		NEWT
	}
	
	GLProfile glp;
	GLCapabilities glc;

	Engine engine;
	
	GraphBuffers buffers;
	
	Context ctx;
	
	public JOGLViewer( Source source )
	{
		this( source, EngineType.NEWT );
	}
	
	public JOGLViewer( Source source, EngineType engineType )
	{
		glp = GLProfile.getDefault();
		glc = new GLCapabilities(glp);
		
		switch( engineType )
		{
		case AWT:
		case SWING:
			engine = new SWINGEngine();
			break;
		case NEWT:
			engine = new NEWTEngine();
			break;
		}
		
		ctx = new Context( source );
		
		engine.init(glc);
		engine.setWindowSize(600, 600);
		engine.setWindowVisible(true);
		engine.setWindowTitle("The GraphStream GL Viewer");
        engine.addGLEventListener( new VertexArrayRenderer( ctx ) );
        
        Animator animator = new FPSAnimator( engine.getGLAutoDrawable(), 60 );
        animator.add( engine.getGLAutoDrawable() );
        animator.start();
	}
}
