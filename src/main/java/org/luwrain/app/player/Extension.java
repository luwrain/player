
package org.luwrain.app.player;

import java.net.*;
import java.util.*;
import java.io.*;

import org.luwrain.base.*;
import org.luwrain.core.*;
import org.luwrain.util.*;

public final class Extension extends org.luwrain.core.extensions.EmptyExtension
{
    static private final int VOLUME_STEP = 3;

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
		    final org.luwrain.player.Player player = luwrain.getPlayer();
		    if (player != null)
			player.stop();
		}
	    },

	    new Command(){
		@Override public String getName()
		{
		    return "player-pause";
		}
		@Override public void onCommand(Luwrain luwrain)
		{
		    final org.luwrain.player.Player player = luwrain.getPlayer();
		    if (player != null)
			player.pauseResume();
		}
	    },

	    new Command(){
		@Override public String getName()
		{
		    return "player-jump-forward";
		}
		@Override public void onCommand(Luwrain luwrain)
		{
		    final org.luwrain.player.Player player = luwrain.getPlayer();
		    if (player != null)
			player.jump(5000);
		}
	    },

	    new Command(){
		@Override public String getName()
		{
		    return "player-jump-backward";
		}
		@Override public void onCommand(Luwrain luwrain)
		{
		    final org.luwrain.player.Player player = luwrain.getPlayer();
		    if (player != null)
			player.jump(-5000);
		}
	    },

	    	    new Command(){
		@Override public String getName()
		{
		    return "player-volume-inc";
		}
		@Override public void onCommand(Luwrain luwrain)
		{
		    NullCheck.notNull(luwrain, "luwrain");
		    final org.luwrain.player.Player player = luwrain.getPlayer();
		    if (player == null)
			return;
		    player.setVolume(Math.min(player.getVolume() + VOLUME_STEP, 100));
		}
	    },

	    	    	    new Command(){
		@Override public String getName()
		{
		    return "player-volume-dec";
		}
		@Override public void onCommand(Luwrain luwrain)
		{
		    NullCheck.notNull(luwrain, "luwrain");
		    final org.luwrain.player.Player player = luwrain.getPlayer();
		    if (player == null)
			return;
		    player.setVolume(Math.max(player.getVolume() - VOLUME_STEP, 0));
		}
	    },

	};
    }

    @Override public Shortcut[] getShortcuts(Luwrain luwrain)
    {
	return new Shortcut[]{

	    new Shortcut() {
		@Override public String getExtObjName()
		{
		    return "player";
		}
		@Override public Application[] prepareApp(String[] args)
		{
		    NullCheck.notNullItems(args, "args");
		    return new Application[]{new App(args)};
		}
	    },

	    new Shortcut() {
		@Override public String getExtObjName()
		{
		    return "player-single-local";
		}
		@Override public Application[] prepareApp(String[] args)
		{
		    NullCheck.notNullItems(args, "args");
		    if (args.length != 1 || args[0].isEmpty())
			return null;
		    final org.luwrain.player.Player player = luwrain.getPlayer();
		    if (player != null)
		    {
			final File f = new File(args[0]).getAbsoluteFile();
			final URL url = Urls.toUrl(f);
			if (url == null)
			    return null;//FIXME:message
			player.play(new org.luwrain.player.Playlist(url.toString()), 0, 0, org.luwrain.player.Player.DEFAULT_FLAGS);
		    }
		    return null;
		}
	    },

	};
    }
}
