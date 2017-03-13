
package org.luwrain.app.player;

import java.util.*;
import java.io.*;
import java.nio.charset.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.player.*;
import org.luwrain.popups.*;

public class PlayerApp implements Application, MonoApp, Actions
{
    static private final int MAIN_LAYOUT_INDEX = 0;
    static private final int PLAYLIST_PROPERTIES_LAYOUT_INDEX = 1;

    private Luwrain luwrain;
    private final Base base = new Base();
    private Strings strings;
    private ListArea playlistsArea;
    private ListArea playlistArea;
    private ControlArea controlArea;
    private FormArea propertiesFormArea;
    private AreaLayoutSwitch layouts;

    private Playlist startingPlaylist = null;

    public PlayerApp()
    {
	startingPlaylist = null;
    }

    public PlayerApp(Playlist startingPlaylist)
    {
	NullCheck.notNull(startingPlaylist, "startingPlaylist");
	this.startingPlaylist = startingPlaylist;
    }

    @Override public boolean onLaunch(Luwrain luwrain)
    {
	final Object o = luwrain.i18n().getStrings(Strings.NAME);
	if (o == null || !(o instanceof Strings))
	    return false;
	strings = (Strings)o;
	this.luwrain = luwrain;
	if (!base.init(luwrain, strings))
	    return false;
	createAreas();
	layouts = new AreaLayoutSwitch(luwrain);
	layouts.add(new AreaLayout(AreaLayout.LEFT_TOP_BOTTOM, playlistsArea, playlistArea, controlArea));
	layouts.add(new AreaLayout(propertiesFormArea));
	base.setListener(controlArea);
	if (startingPlaylist != null)
	    base.playPlaylist(startingPlaylist, 0, 0);
	return true;
    }

    private void createAreas()
    {
	final ListArea.Params playlistsParams = new ListArea.Params();
	playlistsParams.environment = new DefaultControlEnvironment(luwrain);
	playlistsParams.model = base.getPlaylistsModel();
	playlistsParams.name = strings.treeAreaName();
	playlistsParams.clickHandler = (area, index, obj)->onPlaylistsClick(obj);

	playlistsParams.appearance = new ListUtils.DoubleLevelAppearance(playlistsParams.environment){
		@Override public boolean isSectionItem(Object item)
		{
		    NullCheck.notNull(item, "item");
		    return (item instanceof String);
		}
	    };

	playlistsArea = new ListArea(playlistsParams){
		@Override public boolean onKeyboardEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (commonKeys(event))
			return true;
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
			{
			case TAB:
			    goToPlaylist();
			    return true;
			}
		    return super.onKeyboardEvent(event);
		}
		@Override public boolean onEnvironmentEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    switch(event.getCode())
		    {
		    case CLOSE:
			closeApp();
			return true;
		    case ACTION:
			return onPlaylistsActionEvent(event);
		    case PROPERTIES:
			return onTreeProperties();
		    default:
			return super.onEnvironmentEvent(event);
		    }
		}
		@Override public Action[] getAreaActions()
		{
		    return getTreeActions();
		}
	    };

	final ListArea.Params params = new ListArea.Params();
	params.environment = new DefaultControlEnvironment(luwrain);
	params.model = base.getPlaylistModel();
	params.appearance = new ListUtils.DefaultAppearance(params.environment);
	params.clickHandler = (area, index, obj)->onPlaylistClick(index, obj);
	params.name = strings.playlistAreaName();

	playlistArea = new ListArea(params){
		@Override public boolean onKeyboardEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (commonKeys(event))
			return true;
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
			{
			case TAB:
			    goToControl();
			    return true;
			case BACKSPACE:
			    goToPlaylists();
			    return true;
			}
		    return super.onKeyboardEvent(event);
		}
		@Override public boolean onEnvironmentEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    switch(event.getCode())
		    {
		    case CLOSE:
			closeApp();
			return true;
		    default:
			return super.onEnvironmentEvent(event);
		    }
		}
	    };

	controlArea = new ControlArea(luwrain, this, strings);

	propertiesFormArea = new FormArea(new DefaultControlEnvironment(luwrain), strings.playlistPropertiesAreaName()) {
		@Override public boolean onKeyboardEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (commonKeys(event))
			return true;
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
			{
			case ESCAPE:
			    return closeTreeProperties(false);
			}
		    return super.onKeyboardEvent(event);
		}
		@Override public boolean onEnvironmentEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    switch(event.getCode())
		    {
		    case CLOSE:
			closeApp();
		    case OK:
			return closeTreeProperties(true );
		    default:
			return super.onEnvironmentEvent(event);
		    }
		}
	    };
    }

    private Action[] getTreeActions()
    {
	return new Action[]{
	    new Action("add-playlist-without-bookmark", strings.actionAddPlaylistWithoutBookmark()),
	    new Action("add-playlist-with-bookmark", strings.actionAddPlaylistWithBookmark()),
	    new Action("add-streaming-playlist", strings.actionAddStreamingPlaylist()),
	};
    }

    private boolean onPlaylistsActionEvent(EnvironmentEvent event)
    {
	NullCheck.notNull(event, "event");
	if (ActionEvent.isAction(event, "add-playlist-with-bookmark"))
	{
	    if (base.onAddPlaylistWithBookmark())
		playlistsArea.refresh();
	    return true;
	}
	if (ActionEvent.isAction(event, "add-playlist-without-bookmark"))
	{
	    if (base.onAddPlaylistWithoutBookmark())
		playlistsArea.refresh();
	    return true;
	}
	if (ActionEvent.isAction(event, "add-streaming-playlist"))
	{
	    if (base.onAddStreamingPlaylist())
		playlistsArea.refresh();
	    return true;
	}
	return false;
    }

    private boolean onPlaylistsClick(Object obj)
    {
	if (obj == null || !(obj instanceof Playlist))
	    return false;
	final Playlist playlist = (Playlist)obj;
	if (playlist.getFlags().contains(Playlist.Flags.HAS_BOOKMARK) && !playlist.getFlags().contains(Playlist.Flags.STREAMING))
	    base.playPlaylist(playlist, playlist.getStartingTrackNum(), playlist.getStartingPosMsec()); else
	    base.playPlaylist(playlist, 0, 0);
	return true;
    }

    private boolean onPlaylistClick(int index, Object item)
    {
	NullCheck.notNull(item, "item");
	return base.playPlaylistItem(index);
    }

    @Override public void pauseResume()
    {
	base.pauseResume();
    }

    @Override public void stop()
    {
	base.stop();
    }

    @Override public void prevTrack()
    {
	base.prevTrack();
    }

    @Override public void nextTrack()
    {
	base.nextTrack();
    }

    @Override public boolean commonKeys(KeyboardEvent event)
    {
	NullCheck.notNull(event, "event");
	if (event.isModified())
	    return false;
	if (event.isSpecial())
	    switch(event.getSpecial())
	    {
	    case F5:
		pauseResume();
		return true;
	    case F6:
		stop();
		return true;
	    case F7:
		prevTrack();
		return true;
	    case F8:
		nextTrack();
		return true;
	    case F9:
		base.jump(-5000);
		return true;
	    case F10:
		base.jump(5000);
		return true;
	    default:
		return false;
	    }
	switch(event.getChar())
	{
	case '[':
	    base.jump(-60000);
	    return true;
	case ']':
	    base.jump(60000);
	    return true;
	default:
	    return false;
	}
    }

    @Override public void refreshPlaylist()
    {
	playlistArea.refresh();
    }

    private boolean onTreeProperties()
    {
	final Object obj = playlistsArea.selected();
	if (obj == null || !(obj instanceof Playlist))
	    return false;
	base.fillPlaylistProperties((Playlist)obj, propertiesFormArea);
	playlistsArea.refresh();
	layouts.show(PLAYLIST_PROPERTIES_LAYOUT_INDEX);
	luwrain.announceActiveArea();
	return true;
    }

    private boolean closeTreeProperties(boolean save)
    {
	if (save)
	    base.savePlaylistProperties(propertiesFormArea);
	playlistsArea.refresh();
	layouts.show(MAIN_LAYOUT_INDEX);
	luwrain.announceActiveArea();
	return true;
    }

    @Override public void goToPlaylists()
    {
	luwrain.setActiveArea(playlistsArea);
    }

    @Override public void goToPlaylist()
    {
	luwrain.setActiveArea(playlistArea);
    }

    @Override public void goToControl()
    {
	luwrain.setActiveArea(controlArea);
    }

    @Override public String getAppName()
    {
	return strings.appName();
    }

    @Override public AreaLayout getAreasToShow()
    {
	return layouts.getCurrentLayout();
    }

    @Override public Base getBase()
    {
	return base;
    }

    @Override public void closeApp()
    {
	base.removeListener();
	luwrain.closeApp();
    }

    @Override public MonoApp.Result onMonoAppSecondInstance(Application app)
    {
	NullCheck.notNull(app, "app");
	return MonoApp.Result.BRING_FOREGROUND;
    }
}
