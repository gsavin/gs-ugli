package org.graphstream.ui.gl;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import javax.media.opengl.GL;

public abstract class GLBuffer<T>
{
	private GL gl;
	private int id = -1;
	private int size = -1;
	protected int block = 1;
	protected ByteBuffer getbuffer;
	protected ByteBuffer setbuffer;
	
	protected GLBuffer( GL gl, int size, int block )
	{
		this.gl = gl;
		this.size = size;
		this.block = block;
		
		__init_gl_buffer();

		getbuffer = ByteBuffer.allocateDirect( block ).order( ByteOrder.nativeOrder() );
		setbuffer = ByteBuffer.allocateDirect( Float.SIZE ).order( ByteOrder.nativeOrder() );
	}
	
	private void __init_gl_buffer()
	{
		if( id > 0 ) return;
		
		IntBuffer buffer = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asIntBuffer();
		
		gl.glGenBuffers(1, buffer);
		gl.glBindBuffer( GL.GL_ARRAY_BUFFER, buffer.get(0) );
		gl.glBufferData( GL.GL_ARRAY_BUFFER, size * block, null, GL.GL_DYNAMIC_COPY );
		
		id = buffer.get(0);
	}
	
	protected void __gl_set( int i, Buffer data )
	{
		gl.glBindBuffer( GL.GL_ARRAY_BUFFER, id );
		gl.glBufferSubData( GL.GL_ARRAY_BUFFER, i * block, block, data );
	}
	
	protected void __gl_get( int i, Buffer data )
	{
		gl.glBindBuffer( GL.GL_ARRAY_BUFFER, id );
		gl.glGetBufferSubData( GL.GL_ARRAY_BUFFER, i * block, block, data );
	}
	
	public synchronized T get( int i )
	{
		__gl_get( i, getbuffer );
		return read( 0, getbuffer );
	}
	
	public synchronized void set( int i, T v )
	{
		write( 0, v, setbuffer );
		__gl_set( i, setbuffer );
	}
	
	public void destroy()
	{
		IntBuffer buffer = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asIntBuffer();
		buffer.put(0,id);
		
		gl.glDeleteBuffers( 1, buffer );
		
		id = -1;
	}
	
	protected abstract T read( int offset, ByteBuffer buffer );
	protected abstract void write( int offset, T value, ByteBuffer buffer );
	
	// -------------------------
	// Ways to create GL buffers
	// -------------------------
	
	public static GLBuffer<Float> createFloatGLBuffer( GL gl, int size )
	{
		return new GLBufferFloat( gl, size );
	}
	
	public static GLBuffer<Double> createDoubleGLBuffer( GL gl, int size )
	{
		return new GLBufferDouble( gl, size );
	}
	
	public static GLBuffer<Byte> createByteGLBuffer( GL gl, int size )
	{
		return new GLBufferByte( gl, size );
	}
	
	public static GLBuffer<Character> createCharGLBuffer( GL gl, int size )
	{
		return new GLBufferChar( gl, size );
	}
	
	public static GLBuffer<Short> createShortGLBuffer( GL gl, int size )
	{
		return new GLBufferShort( gl, size );
	}
	
	public static GLBuffer<Integer> createIntGLBuffer( GL gl, int size )
	{
		return new GLBufferInt( gl, size );
	}
	
	public static GLBuffer<Long> createLongGLBuffer( GL gl, int size )
	{
		return new GLBufferLong( gl, size );
	}
	
	// -----------------------------
	// Implementation for each types
	// -----------------------------
	
	private static final class GLBufferFloat extends GLBuffer<Float>
	{
		
		public GLBufferFloat( GL gl, int size )
		{
			super( gl, size, Float.SIZE );
			
		}
		
		public Float read( int offset, ByteBuffer buffer )
		{
			return buffer.getFloat( offset );
		}
		
		public void write( int offset, Float value, ByteBuffer buffer )
		{
			buffer.putFloat( offset, value );
		}
	}
	
	private static final class GLBufferDouble extends GLBuffer<Double>
	{
		
		public GLBufferDouble( GL gl, int size )
		{
			super( gl, size, Double.SIZE );
			
		}
		
		public Double read( int offset, ByteBuffer buffer )
		{
			return buffer.getDouble( offset );
		}
		
		public void write( int offset, Double value, ByteBuffer buffer )
		{
			buffer.putDouble( offset, value );
		}
	}
	
	private static final class GLBufferByte extends GLBuffer<Byte>
	{
		
		public GLBufferByte( GL gl, int size )
		{
			super( gl, size, Byte.SIZE );
			
		}
		
		public Byte read( int offset, ByteBuffer buffer )
		{
			return buffer.get( offset );
		}
		
		public void write( int offset, Byte value, ByteBuffer buffer )
		{
			buffer.put( offset, value );
		}
	}
	
	private static final class GLBufferChar extends GLBuffer<Character>
	{
		
		public GLBufferChar( GL gl, int size )
		{
			super( gl, size, Character.SIZE );
			
		}
		
		public Character read( int offset, ByteBuffer buffer )
		{
			return buffer.getChar( offset );
		}
		
		public void write( int offset, Character value, ByteBuffer buffer )
		{
			buffer.putChar( offset, value );
		}
	}
	
	private static final class GLBufferShort extends GLBuffer<Short>
	{
		
		public GLBufferShort( GL gl, int size )
		{
			super( gl, size, Short.SIZE );
			
		}
		
		public Short read( int offset, ByteBuffer buffer )
		{
			return buffer.getShort( offset );
		}
		
		public void write( int offset, Short value, ByteBuffer buffer )
		{
			buffer.putShort( offset, value );
		}
	}
	
	private static final class GLBufferInt extends GLBuffer<Integer>
	{
		
		public GLBufferInt( GL gl, int size )
		{
			super( gl, size, Integer.SIZE );
			
		}
		
		public Integer read( int offset, ByteBuffer buffer )
		{
			return buffer.getInt( offset );
		}
		
		public void write( int offset, Integer value, ByteBuffer buffer )
		{
			buffer.putInt( offset, value );
		}
	}
	
	private static final class GLBufferLong extends GLBuffer<Long>
	{
		
		public GLBufferLong( GL gl, int size )
		{
			super( gl, size, Long.SIZE );
			
		}
		
		public Long read( int offset, ByteBuffer buffer )
		{
			return buffer.getLong( offset );
		}
		
		public void write( int offset, Long value, ByteBuffer buffer )
		{
			buffer.putLong( offset, value );
		}
	}
}
