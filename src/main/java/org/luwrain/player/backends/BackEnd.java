
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
	return new JLayer2(listener);
    }
}
