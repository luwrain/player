
package org.luwrain.player;

import java.net.*;
import java.util.*;
import org.luwrain.core.*;
import org.luwrain.app.player.PlayerApp;

public class Extension extends org.luwrain.core.extensions.EmptyExtension
{
    private Player player;

    @Override public String init(Luwrain luwrain)
    {
	player = new PlayerImpl(luwrain.getRegistry());
	Log.debug("player", "player is initialized");
	return null;
    }

    @Override public Command[] getCommands(Luwrain luwrain)
    {
	return new Command[]{

	    new Command(){
		@Override public String getName()
		{
		    return "player";
		}
		@Override public void onCommand(Luwrain luwrain)
		{
		    luwrain.launchApp("player");
		}
	    },

	    new Command(){
		@Override public String getName()
		{
		    return "player-stop";
		}
		@Override public void onCommand(Luwrain luwrain)
		{
		    final Player player = (Player)luwrain.getSharedObject(Player.SHARED_OBJECT_NAME);
		    if (player != null)
			player.stop();
		}
	    },

	};
    }

    @Override public Shortcut[] getShortcuts(Luwrain luwrain)
    {
	return new Shortcut[]{

	    new Shortcut() {
		@Override public String getName()
		{
		    return "player";
		}
		@Override public Application[] prepareApp(String[] args)
		{
		    NullCheck.notNullItems(args, "args");
		    return new Application[]{new PlayerApp(args)};
		}
	    },

	    new Shortcut() {
		@Override public String getName()
		{
		    return "player-single-local";
		}
		@Override public Application[] prepareApp(String[] args)
		{
		    if (args == null || args.length != 1)
			return null;
		    final Player player = (Player)luwrain.getSharedObject(Player.SHARED_OBJECT_NAME);
		    if (player != null)
			player.play(new SingleLocalFilePlaylist("file://" + args[0].replaceAll(" ", "%20")), 0, 0);
		    return null;
		}
	    },

	};
    }

    @Override public SharedObject[] getSharedObjects(Luwrain luwrain)
    {
	return new SharedObject[]{

	    new SharedObject(){
		@Override public String getName()
		{
		    return Player.SHARED_OBJECT_NAME;
		}
		@Override public Object getSharedObject()
		{
		    return player;
		}
	    },

	};
    }
}
