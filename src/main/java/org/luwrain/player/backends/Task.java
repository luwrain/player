
package org.luwrain.player.backends;

import java.io.*;
import java.net.*;

import org.luwrain.core.*;

public class Task
{
public final URL url;
public long startPosMsec = 0;

    public Task(URL url)
    {
	NullCheck.notNull(url, "url");
	this.url = url;
	this.startPosMsec = 0;
    }

    public Task(URL url, long startPosMsec)
    {
	NullCheck.notNull(url, "url");
	this.url = url;
	this.startPosMsec = startPosMsec;
    }

    InputStream openStream() throws IOException
    {
	return url.openStream();
    }

    public long startPosMsec() {return startPosMsec;}
}
