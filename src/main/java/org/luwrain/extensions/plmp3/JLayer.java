/*
   Copyright 2012-2017 Michael Pozhidaev <michael.pozhidaev@gmail.com>
   Copyright 2015-2016 Roman Volovodov <gr.rPman@gmail.com>

   This file is part of LUWRAIN.

   LUWRAIN is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public
   License as published by the Free Software Foundation; either
   version 3 of the License, or (at your option) any later version.

   LUWRAIN is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.
*/

package org.luwrain.extensions.plmp3;

import java.nio.file.*;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import javax.sound.sampled.*;

import javazoom.jl.player.advanced.*;
import javazoom.jl.decoder.*;
import javazoom.jl.player.*;

import org.luwrain.core.*;

class JLayer implements org.luwrain.base.MediaResourcePlayer
{
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Listener listener;
    private FutureTask<Boolean> futureTask = null; 
    private boolean mustStop = false;

    JLayer(Listener listener)
    {
	NullCheck.notNull(listener, "listener");
	this.listener = listener;
    }

    @Override public Result play(URL url, long playFromMsec, Set<Flags> flags)
    {
	NullCheck.notNull(url, "url");
	NullCheck.notNull(flags, "flags");
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
		    long offsetStart = playFromMsec;
		    Log.debug("jlayer", "offsetStart=" + offsetStart);
		    long offsetEnd=Long.MAX_VALUE;
		    final BufferedInputStream bufferedIn = new BufferedInputStream(url.openStream());
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
		    listener.onPlayerTime(0);//FIXME:
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
			    listener.onPlayerTime(lastNotifiedMsec);
			}
			bitstream.closeFrame();
		    }
		    listener.onPlayerFinish();
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
	return new Result();
    }

    @Override public void stop()
    {
	mustStop = true;
    	futureTask.cancel(true);
    }
}
