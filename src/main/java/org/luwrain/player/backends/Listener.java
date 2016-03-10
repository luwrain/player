
package org.luwrain.player.backends;

public interface Listener
{
    void onPlayerBackEndTime(long msec);
    void onPlayerBackEndFinish();
}
