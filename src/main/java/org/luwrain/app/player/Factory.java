
package org.luwrain.app.player;

import org.luwrain.core.*;

public class Factory implements org.luwrain.player.Factory
{
    @Override public org.luwrain.player.Player  newPlayer(Params params)
    {
	NullCheck.notNull(params, "params");
	NullCheck.notNull(params.luwrain, "params.luwrain");
	return new Dispatcher(params.luwrain);
    }
}
