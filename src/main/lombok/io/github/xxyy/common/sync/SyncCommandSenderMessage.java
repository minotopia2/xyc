package io.github.xxyy.common.sync;

import io.github.xxyy.common.xyplugin.AbstractXyPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.lang.ref.WeakReference;
import java.util.concurrent.Callable;


/**
 * Class that helps with sending messages to a {@link CommandSender} when operationg in an asyncronous environment.
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 */
public class SyncCommandSenderMessage implements Callable<Boolean>
{
    private WeakReference<CommandSender> senderRef;
    private String message;
    
    private SyncCommandSenderMessage(CommandSender sender, String message){
        this.senderRef = new WeakReference<>(sender);
        this.message = message;
    }
    
    @Override
    public Boolean call() throws Exception
    {
        CommandSender sender = this.senderRef.get();
        if(sender == null){
            System.err.println("CommandSender turned garbage before run() could be performed for SyncCommandMessage."); return false;
        }
        sender.sendMessage(this.message);
        return true;
    }

    /**
     * Invokes a new instance on the Main Server Thread, non-blocking.
     * Use this if you don't care when the message is being sent.
     * @param sender Sender to be messaged
     * @param message Message to send
     * @return Always true, for methods which want to return booleans in a single statement.
     */
    public static boolean send(CommandSender sender, String message){
        Bukkit.getScheduler().callSyncMethod(AbstractXyPlugin.getInstances().get(0), new SyncCommandSenderMessage(sender, message));
        return true;
    }
}