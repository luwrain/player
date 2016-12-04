
package org.luwrain.player.backends;

import java.nio.file.*;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import javax.sound.sampled.*;

import org.luwrain.core.*;

import javazoom.jl.player.advanced.*;
import javazoom.jl.decoder.*;
import javazoom.jl.player.*;

class JLayer implements BackEnd
{
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Listener listener;
    private FutureTask<Boolean> futureTask = null; 
    private boolean mustStop = false;

    JLayer(Listener listener)
    {
	NullCheck.notNull(listener, "listener");
	this.listener = listener;
    }

    @Override public boolean play(Task task)
    {
	NullCheck.notNull(task, "task");
	mustStop = false;
	futureTask = new FutureTask<>(()->{
		AudioDevice device = null;
		AudioInputStream stream = null;
		Bitstream bitstream = null;
		AudioFormat bitFormat = null;
		long currentFrame = 0;
		float currentPosition = 0;
		long lastNotifiedMsec = 0;
		try
		{
		    long offsetStart=task.startPosMsec();
		    Log.debug("jlayer", "offsetStart=" + offsetStart);
		    long offsetEnd=Long.MAX_VALUE;
		    final BufferedInputStream bufferedIn = new BufferedInputStream(task.openStream());
		    stream = AudioSystem.getAudioInputStream(bufferedIn);
		    bitFormat = stream.getFormat();
		    device = FactoryRegistry.systemRegistry().createAudioDevice();
		    if(device==null)
			return false;
		    Decoder decoder=new Decoder();
		    device.open(decoder);
		    bitstream = new Bitstream(stream);
		    while(currentPosition<offsetStart)
		    {
			final Header h=bitstream.readFrame();
			++currentFrame;
			currentPosition = currentFrame * h.ms_per_frame();
			bitstream.closeFrame();
		    }
		    // Starting real playing here
		    listener.onPlayerBackEndTime(0);//FIXME:
		    while(currentPosition<offsetEnd)
		    {
			if(mustStop || Thread.currentThread().interrupted())
			    return false;
			final Header h=bitstream.readFrame();
			if(h == null)
			    return false;
			final SampleBuffer output = (SampleBuffer) decoder.decodeFrame(h, bitstream);
			synchronized (this)
			{
			    device.write(output.getBuffer(), 0, output.getBufferLength());
			}
			++currentFrame;
			currentPosition = currentFrame * h.ms_per_frame();
			if (currentPosition > lastNotifiedMsec + 50)
			{
			    lastNotifiedMsec = new Float(currentPosition).longValue();
			    listener.onPlayerBackEndTime(lastNotifiedMsec);
			}
			bitstream.closeFrame();
		    }
		    listener.onPlayerBackEndFinish();
		}
		catch (Exception ex)
		{
		    ex.printStackTrace();
		}
		finally
		{
		    if(device != null)
			device.close();
		    device=null;
		    if(stream != null)
			stream.close();
		    stream = null;
		}
		return true;
	    });
	executor.execute(futureTask);
	return true;
    }

    @Override public void stop()
    {
	mustStop = true;
    	futureTask.cancel(true);
    }
}
