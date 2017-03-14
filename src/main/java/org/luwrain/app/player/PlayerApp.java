
package org.luwrain.app.player;

import java.util.*;
import java.io.*;
import java.nio.charset.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.player.*;
import org.luwrain.popups.*;

public class PlayerApp implements Application, MonoApp
{
    static private final int MAIN_LAYOUT_INDEX = 0;
    static private final int PLAYLIST_PROPERTIES_LAYOUT_INDEX = 1;

    private Luwrain luwrain;
    private Base base;
    private Actions actions;
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
	base = new Base(luwrain, strings);
	if (base.player == null)
	    return false;
	actions = new Actions(luwrain, base, strings);
	createAreas();
	layouts = new AreaLayoutSwitch(luwrain);
	layouts.add(new AreaLayout(AreaLayout.LEFT_TOP_BOTTOM, playlistsArea, playlistArea, controlArea));
	layouts.add(new AreaLayout(propertiesFormArea));
	base.setListener(new Listener(luwrain, this, controlArea));
	if (base.getCurrentPlaylist() != null)
	    base.setNewCurrentPlaylist(playlistArea, base.getCurrentPlaylist());
	if (startingPlaylist != null)
	    base.player.play(startingPlaylist, 0, 0);
	return true;
    }

    private void createAreas()
    {
	final ListArea.Params playlistsParams = new ListArea.Params();
	playlistsParams.environment = new DefaultControlEnvironment(luwrain);
	playlistsParams.model = base.playlistsModel;
	playlistsParams.name = strings.treeAreaName();
	playlistsParams.clickHandler = (area, index, obj)->actions.onPlaylistsClick(playlistArea, obj);

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
			return onPlaylistsAction(event);
		    case PROPERTIES:
			return onTreeProperties();
		    default:
			return super.onEnvironmentEvent(event);
		    }
		}

		@Override public Action[] getAreaActions()
		{
		    return actions.getPlaylistsActions();
		}
	    };

	final ListArea.Params params = new ListArea.Params();
	params.environment = new DefaultControlEnvironment(luwrain);
	params.model = base.playlistModel;
	params.appearance = new PlaylistAppearance(luwrain, base);
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
		    case ACTION:
			return onPlaylistAction(event);

		    case CLOSE:
			closeApp();
			return true;
		    default:
			return super.onEnvironmentEvent(event);
		    }
		}

		@Override public Action[] getAreaActions()
		{
		    return actions.getPlaylistActions();
		}
	    };

	controlArea = new ControlArea(luwrain, this, base, strings);

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


    private boolean onPlaylistsAction(EnvironmentEvent event)
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

    private boolean onPlaylistAction(EnvironmentEvent event)
    {
	NullCheck.notNull(event, "event");
	return false;
    }



    private boolean onPlaylistClick(int index, Object item)
    {
	NullCheck.notNull(item, "item");
	return base.playPlaylistItem(index);
    }

    void pauseResume()
    {
	actions.pauseResume();
    }

    void stop()
    {
	actions.stop();
    }

    void prevTrack()
    {
	base.prevTrack();
    }

    void nextTrack()
    {
	base.nextTrack();
    }

    boolean commonKeys(KeyboardEvent event)
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
		actions.jump(-5000);
		return true;
	    case F10:
		actions.jump(5000);
		return true;
	    default:
		return false;
	    }
	switch(event.getChar())
	{
	case '[':
	    actions.jump(-60000);
	    return true;
	case ']':
	    actions.jump(60000);
	    return true;
	default:
	    return false;
	}
    }

    void onNewPlaylist(org.luwrain.player.Playlist playlist)
    {
	NullCheck.notNull(playlist, "playlist");
	base.setNewCurrentPlaylist(playlistArea, playlist);
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

    void goToPlaylists()
    {
	luwrain.setActiveArea(playlistsArea);
    }

    private void goToPlaylist()
    {
	luwrain.setActiveArea(playlistArea);
    }

    private void goToControl()
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

    void closeApp()
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
