package ioio.robotic.rover5;

/**
A class to compute gliding averages.
*/

public class GlidingAverage extends RingBuffer
{   /**
    @param size The size of the average buffer.
    */
    public GlidingAverage (int size)
    {   super(size);
    }
    /**
    Push an element to the gliding average buffer.
    @param x The value to be pushed
    */
    synchronized public void push (int x)
    {   try
        {   if (isFull()) pull();
            super.push(x);
        }
        catch (Exception e)
        {   System.out.println(e);
        }
    }
    /**
    @return The gliding average
    */
    synchronized public int computeAverage ()
    {   try
        {   int n=length();
            int sum=0;
            for (int i=0; i<n; i++)
            {   sum+=get(i);
            }
            return sum/n;
        }
        catch (Exception e)
        {   System.out.println(e);
        }
        return 0;
    }
}