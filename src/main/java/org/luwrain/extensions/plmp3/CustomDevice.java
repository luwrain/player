// 2000 mdm@techie.com
// Mat McGowan

package org.luwrain.extensions.plmp3;

import javax.sound.sampled.*;

import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.*;

/**
   This class gives the custom implementation of a device for JLayer. Its
   creation was caused by the inaccessible instance of {@code
   SourceDataLine} in the original class.
*/
class CustomDevice extends AudioDeviceBase
{
    SourceDataLine	source = null;
    private final float initialVolume;
    private AudioFormat		fmt = null;
    	private byte[]			byteBuf = new byte[4096];

    CustomDevice(float initialVolume)
    {
	this.initialVolume = initialVolume;
    }

int millisecondsToBytes(AudioFormat fmt, int time)
	{
		return (int)(time*(fmt.getSampleRate()*fmt.getChannels()*fmt.getSampleSizeInBits())/8000.0);
	}

    	public int getPosition()
	{
		int pos = 0;
		if (source!=null)
		{
			pos = (int)(source.getMicrosecondPosition()/1000);
		}
		return pos;
	}

	protected void setAudioFormat(AudioFormat fmt0)
	{
		fmt = fmt0;
	}

	protected AudioFormat getAudioFormat()
	{
		if (fmt==null)
		{
			Decoder decoder = getDecoder();
			fmt = new AudioFormat(decoder.getOutputFrequency(),
								  16,
								  decoder.getOutputChannels(),
								  true,
								  false);
		}
		return fmt;
	}

	protected DataLine.Info getSourceLineInfo()
	{
		AudioFormat fmt = getAudioFormat();
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, fmt);
		return info;
	}

	protected void openImpl() throws JavaLayerException
	{
	}

   
	// createSource fix.
	protected void createSource() throws JavaLayerException
    {
        Throwable t = null;
        try
        {
	    final Line line = createLine();
            if (line instanceof SourceDataLine)
            {
         		source = (SourceDataLine)line;
				source.open(fmt);
                if (source.isControlSupported(FloatControl.Type.MASTER_GAIN))
                {
					FloatControl c = (FloatControl)source.getControl(FloatControl.Type.MASTER_GAIN);
					//                    c.setValue(c.getMaximum());
					c.setValue(initialVolume);
                }
                source.start();
            }
}
catch (RuntimeException ex)
          {
			  t = ex;
          }
          catch (LinkageError ex)
          {
              t = ex;
          }
          catch (LineUnavailableException ex)
          {
              t = ex;
          }
		if (source==null) throw new JavaLayerException("cannot obtain source audio line", t);
    }

    protected Line createLine() throws LineUnavailableException {
        Line line = AudioSystem.getLine(getSourceLineInfo());
        return line;
    }

	protected void closeImpl()
	{
		if (source!=null)
		{
			source.close();
		}
	}

	protected void writeImpl(short[] samples, int offs, int len) throws JavaLayerException
	{
		if (source==null)
			createSource();
		byte[] b = toByteArray(samples, offs, len);
		source.write(b, 0, len*2);
	}

	protected byte[] getByteArray(int length)
	{
		if (byteBuf.length < length)
		{
			byteBuf = new byte[length+1024];
		}
		return byteBuf;
	}

	protected byte[] toByteArray(short[] samples, int offs, int len)
	{
		byte[] b = getByteArray(len*2);
		int idx = 0;
		short s;
		while (len-- > 0)
		{
			s = samples[offs++];
			b[idx++] = (byte)s;
			b[idx++] = (byte)(s>>>8);
		}
		return b;
	}

	protected void flushImpl()
	{
		if (source!=null)
		{
			source.drain();
		}
	}

}