
package org.luwrain.app.player;

import java.net.*;

import org.luwrain.core.*;

final class Task
{
    final URL url;
    final long startPosMsec;

    Task(URL url, long startPosMsec)
    {
	NullCheck.notNull(url, "url");
	if (startPosMsec < 0)
	    throw new IllegalArgumentException("startPosMsec (" + startPosMsec + ") may not be negative");
	this.url = url;
	this.startPosMsec = startPosMsec;
    }
}
