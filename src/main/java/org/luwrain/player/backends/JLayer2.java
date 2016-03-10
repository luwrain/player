
package org.luwrain.player.backends;

import java.nio.file.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import javax.sound.sampled.*;

import org.luwrain.core.NullCheck;

import javazoom.jl.player.advanced.*;
import javazoom.jl.decoder.*;
import javazoom.jl.player.*;

class JLayer2 implements BackEnd
{
    private Listener listener;
    private FutureTask<Boolean> futureTask = null; 
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private AudioDevice device = null;
    private AudioInputStream stream = null;
   	private Bitstream bitstream=null;
   	private AudioFormat bitFormat = null;

	// current frame and millisecond position while custom playing
	private long currentFrame=0;
	private float currentPosition=0;

	JLayer2(Listener listener)
    {
	NullCheck.notNull(listener, "listener");
	this.listener = listener;
    }

    @Override public boolean play(Task task)
    {
	NullCheck.notNull(task, "task");
	
	futureTask = new FutureTask<>(()->{
    try
    {
    	// TODO: make possible to pause and resume playing without restart stream and seek
		long offsetStart=task.startPosMsec();
		long offsetEnd=Long.MAX_VALUE;
    	// choose source stream
	    if (task.isPath())
		stream = AudioSystem.getAudioInputStream(Files.newInputStream(task.path()));else
		stream = AudioSystem.getAudioInputStream(task.url());
		// get current audio format information
	    bitFormat = stream.getFormat();
		// audio device
	    device = FactoryRegistry.systemRegistry().createAudioDevice();
	    Decoder decoder=new Decoder();
		device.open(decoder);
		//System.out.println("vbr:"+bitFormat.properties().get("mp3.vbr")+", vbrscale:"+bitFormat.properties().get("mp3.vbr.scale"));
		//for(Map.Entry<String,Object> e:bitFormat.properties().entrySet())
		//	System.out.println("* "+e.getKey()+"="+e.getValue());
        //Object isVBR=bitFormat.properties().get("vbr");
		// seek
        bitstream=new Bitstream(stream);
        while(currentPosition<offsetStart)
        {
        	Header h=bitstream.readFrame();
            currentFrame++;
            currentPosition = currentFrame * h.ms_per_frame();
        	bitstream.closeFrame();
        }
        // play
        while(currentPosition<offsetEnd)
        {
            if(device==null)
            	return false;
            if(Thread.currentThread().isInterrupted())
                return false;
            Header h=bitstream.readFrame();
            if(h==null)
                return false;
            SampleBuffer output = (SampleBuffer) decoder.decodeFrame(h, bitstream);
            synchronized (this)
            {
                if(device!=null)
                {
                    device.write(output.getBuffer(), 0, output.getBufferLength());
                }
            }
            currentFrame++;
            currentPosition = currentFrame * h.ms_per_frame();
            bitstream.closeFrame();
        }
    }
    catch (Exception ex)
    {
        ex.printStackTrace();
    }
    finally
    {
		// close all
		if(device!=null) device.close();
		device=null;
		if(stream!=null) stream.close();
		stream=null;
	}
	return true;
	});
	executor.execute(futureTask);
	return true;
    }

    @Override public void stop()
    {
    	futureTask.cancel(true);
    }
}