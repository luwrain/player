
package org.luwrain.player.backends;

import java.io.*;
import java.nio.file.*;
import java.net.*;

import org.luwrain.core.*;

public class Task
{
    final Path path;
final URL url;
public long startPosMsec = 0;

    public Task(Path path)
    {
	NullCheck.notNull(path, "path");
	this.path = path;
	this.url = null;
	this.startPosMsec = 0;
    }

    public Task(Path path, long startPosMsec)
    {
	NullCheck.notNull(path, "path");
	this.path = path;
	this.url = null;
	this.startPosMsec = startPosMsec;
    }

    public Task(URL url)
    {
	NullCheck.notNull(url, "url");
	this.url = url;
	this.path = null;
	this.startPosMsec = 0;
    }

    public Task(URL url, long startPosMsec)
    {
	NullCheck.notNull(url, "url");
	this.url = url;
	this.path = null;
	this.startPosMsec = startPosMsec;
    }

    InputStream openStream() throws IOException
    {
	if (isPath())
	    return Files.newInputStream(path);
	return url.openStream();
    }

    public boolean isPath() {return path != null;}
	public boolean isUrl() {return url != null;}
    public long startPosMsec() {return startPosMsec;}
}
