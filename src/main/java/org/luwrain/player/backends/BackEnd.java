
package org.luwrain.player.backends;

public interface BackEnd
{
    boolean play(Task task);
    void stop();

    static public BackEnd createBackEnd(Listener listener,
					String type, boolean streaming)
    {
	return new JLayer2(listener);
    }
}
