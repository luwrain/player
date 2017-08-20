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

package org.luwrain.extensions.plwave;

import java.util.*;
import java.util.concurrent.*;

import java.net.*;
import java.io.*;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.luwrain.core.*;

class WavePlayer implements org.luwrain.base.MediaResourcePlayer
{
	private static final int NOTIFY_MSEC_COUNT=500;
    private static final int BUF_SIZE = 512;

	private boolean interruptPlayback = false;
	private SourceDataLine audioLine = null;

	AudioFormat format=null;

	private final Listener listener;
	private FutureTask<Boolean> futureTask = null;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    WavePlayer(Listener listener)
    {
	NullCheck.notNull(listener, "listener");
	this.listener = listener;
    }

    @Override public Result play(URL url, long playFromMsec, Set<Flags> flags)
	{
	    NullCheck.notNull(url, "url");
	    NullCheck.notNull(flags, "flags");
		interruptPlayback = false;
		NullCheck.notNull(url, "url");
		NullCheck.notNull(flags, "flags");
		AudioInputStream audioInputStream;
		try {
				audioInputStream=AudioSystem.getAudioInputStream(url.openStream());
		} 
catch(Exception e)
		{
			e.printStackTrace();
			listener.onPlayerFinish();
			return new Result();
		}
		format=audioInputStream.getFormat();
		futureTask = new FutureTask<>(()->{
		try
		{
			final DataLine.Info info=new DataLine.Info(SourceDataLine.class,format);
			synchronized(this)
			{
				audioLine=(SourceDataLine)AudioSystem.getLine(info);
				// FloatControl volume=(FloatControl)line.getControl(FloatControl.Type.MASTER_GAIN); 
				audioLine.open(format);
				audioLine.start();
			}
			long totalBytes=0;
			// skip if task need it
			if(playFromMsec > 0)
			{
				// bytes count from msec pos, 8000 is a 8 bits in byte and 1000 ms in second
				long skipBytes=mSecToBytesSamples(playFromMsec);
				audioInputStream.skip(skipBytes);
				totalBytes+=skipBytes;
			}
			long lastNotifiedMsec=totalBytes;
			long notifyBytesCount=mSecToBytesSamples(NOTIFY_MSEC_COUNT);
			int bytesRead=0;
			byte[] buf=new byte[BUF_SIZE];
			while(bytesRead!=-1&&!interruptPlayback)
			{
				bytesRead=audioInputStream.read(buf,0,buf.length);
				// System.out.println("bytesRead=" + bytesRead);
				if(bytesRead>=0) synchronized(this)
				{
					audioLine.write(buf,0,bytesRead);
					totalBytes+=bytesRead;
				}
				if (totalBytes > lastNotifiedMsec + notifyBytesCount)
				{
				    lastNotifiedMsec = totalBytes;
				    listener.onPlayerTime((long)bytesSamplesTomSec(totalBytes));
				    //Log.debug("player","SoundPlayer: step"+(long)bytesSamplesTomSec(totalBytes));
				}
			}
			audioLine.drain();
		} catch(Exception e)
		{
			e.printStackTrace();
			listener.onPlayerFinish();
			return false;
		} finally
		{
			if(audioLine!=null) audioLine.close();
		}
		//Log.debug("player","SoundPlayer: finish");
		listener.onPlayerFinish();
		return true;
		});
		executor.execute(futureTask);
		return new Result();
	}

	private long mSecToBytesSamples(float msec)
	{
		return (long)(format.getSampleRate()*format.getSampleSizeInBits()*msec/8000);
	}
	private float bytesSamplesTomSec(long samples)
	{
		return (8000f*samples/format.getSampleRate()*format.getSampleSizeInBits());
	}
	
	@Override public void stop()
	{
		interruptPlayback=true;
	}

}
