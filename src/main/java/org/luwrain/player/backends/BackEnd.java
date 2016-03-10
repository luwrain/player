
package org.luwrain.player.backends;

public interface BackEnd
{
    boolean play(Task task);
    void stop();
}
