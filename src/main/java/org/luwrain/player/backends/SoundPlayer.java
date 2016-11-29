package org.luwrain.player.backends;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.luwrain.core.Log;
import org.luwrain.core.NullCheck;

public class SoundPlayer implements BackEnd
{
	private static final int NOTIFY_MSEC_COUNT=500;
    private static final int BUF_SIZE = 512;

	private boolean interruptPlayback = false;
	private SourceDataLine audioLine = null;
	
	AudioFormat format=null;

    Task task;
	private Listener listener;
	private FutureTask<Boolean> futureTask = null;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    SoundPlayer(Listener listener)
    {
	NullCheck.notNull(listener, "listener");
	this.listener = listener;
    }

	@Override public boolean play(Task task)
	{
		interruptPlayback = false;
		NullCheck.notEmpty(task, "task");
		this.task=task;
		//
		AudioInputStream audioInputStream;
		try
		{
			if(task.isPath())
			{
				audioInputStream=AudioSystem.getAudioInputStream(task.path().toFile());
				
			} else
			if(task.isUrl())
			{
				audioInputStream=AudioSystem.getAudioInputStream(task.url().openStream());
			} else
			{
				// task have no any file info
				return false;
			}
		} catch(Exception e)
		{
			e.printStackTrace();
			listener.onPlayerBackEndFinish();
			return false;
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
			if(task.startPosMsec()>0)
			{
				// bytes count from msec pos, 8000 is a 8 bits in byte and 1000 ms in second
				long skipBytes=mSecToBytesSamples(task.startPosMsec());
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
				    listener.onPlayerBackEndTime((long)bytesSamplesTomSec(totalBytes));
				    //Log.debug("player","SoundPlayer: step"+(long)bytesSamplesTomSec(totalBytes));
				}
			}
			audioLine.drain();
		} catch(Exception e)
		{
			e.printStackTrace();
			listener.onPlayerBackEndFinish();
			return false;
		} finally
		{
			if(audioLine!=null) audioLine.close();
		}
		//Log.debug("player","SoundPlayer: finish");
		listener.onPlayerBackEndFinish();
		return true;
		});
		executor.execute(futureTask);
		return true;
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
