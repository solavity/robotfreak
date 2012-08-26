package ioio.robotic.rover5;

import java.util.*; 

/**
This class implements a ring (filo) buffer. Pushing and pulling
is most effective.
<p><b>This class is not thread save!</b>
*/

public class RingBuffer
{   private int Size, // Maximale Größe
        First, // Erstes Element
        Last, // Letztes Element
        Length; // Gespeicherte Elemente
    private int X[];

    /**
    Initialize with specific size
    @param size Size of the buffer.
    */  
    public RingBuffer (int size)
    {   Size=size;
        X=new int[Size];
        clear();
    }

    /**
    Initialize with default size (32).
    */
    public RingBuffer ()
    {   this(32);
    }

    /**
    Clear the ring buffer.
    */
    public void clear ()
    {   Length=0;
    }

    /**
    Push a new stack element on top of the stack or throw
    RingBufferOverflowException.
    @param x Value to be pushed.
    */
    public void push (int x)
        throws RingBufferOverflowException
    {   if (Length==0)
        {   First=Last=0; X[0]=x; Length=1;
        }
        else if (Length<Size) // not full
        {   Last++;
            if (Last>=Size) Last=0;
            X[Last]=x;
            Length++;
        }
        else throw new RingBufferOverflowException();
    }

    /**
    Pull the first element from the buffer or throw 
    RingBufferEmptyException.
    @return First buffer element
    */
    public int pull ()
        throws RingBufferEmptyException
    {   if (Length==0) throw new RingBufferEmptyException();
        int x=X[First];
        First++;
        if (First>=Size) First=0;
        Length--;
        if (First==Last) Length=0;
        return x;
    }
    
    /**
    Test, if the buffer is empty.
    */
    public boolean isEmpty ()
    {   return Length==0;
    }

    /**
    Test, if the buffer is full.
    */
    public boolean isFull ()
    {   return Length>=Size;
    }

    /**
    @return Number of available elements.
    */
    public int length ()
    {   return Length;
    }
    
    /**
    Get a value by its number. Empty buffers or too large indices
    will throw exceptions.
    @param i Number of element to retrieve
    @return Element number i
    */
    public double get (int i)
        throws RingBufferEmptyException,
            RingBufferOverflowException
    {   if (Length==0) throw new RingBufferEmptyException();
        if (i>=Length) throw new RingBufferOverflowException();
        int k=First+i;
        if (k>=Size) k-=Size;
        return X[k];
    }
}