
package org.luwrain.player.backends;

import org.luwrain.core.*;

public interface BackEnd
{
    boolean play(Task task);
    void stop();

    static public BackEnd createBackEnd(Listener listener,
					String name)
    {
	NullCheck.notNull(listener, "listener");
	NullCheck.notNull(name, "name");
	switch(name.toLowerCase())
	{
	case "jlayer":
	return new JLayer(listener);

	case "jlayer-streaming":
	return new JLayerStreaming(listener);
	default:
	    return null;
	}
    }
}
