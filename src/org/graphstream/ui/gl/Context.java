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

import org.graphstream.stream.Source;

public class Context
{
	public static enum NodeColorMode
	{
		AllNodeOneColor,
		EachNodeOneColor
	}
	
	public static enum EdgeColorMode
	{
		AllEdgeOneColor,
		EachEdgeOneColor,
		ExtremitiesBlending
	}
	
	protected Source source;
	protected Camera camera;
	
	protected NodeColorMode nodeColorMode;
	
	public Context( Source source )
	{
		this.source = source;
		this.camera = new Camera();
		this.nodeColorMode = NodeColorMode.EachNodeOneColor;
	}
	
	public Camera getCamera()
	{
		return camera;
	}
	
	public Source getSource()
	{
		return source;
	}
	
	public NodeColorMode getNodeColorMode()
	{
		return nodeColorMode;
	}
}
