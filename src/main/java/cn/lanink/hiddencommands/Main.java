package cn.lanink.hiddencommands;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerCommandPreprocessEvent;
import cn.nukkit.event.server.DataPacketSendEvent;
import cn.nukkit.network.protocol.AvailableCommandsPacket;
import cn.nukkit.network.protocol.TextPacket;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;

import java.util.HashMap;

/**
 * @author lt_name
 */
public class Main extends PluginBase implements Listener {

    @Override
    public void onLoad() {
        this.saveDefaultConfig();
    }

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);

        this.getLogger().info("加载完成！");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            Config config = this.getConfig();
            switch (args[0]) {
                case "cmd":
                    if (config.getBoolean("隐藏命令")) {
                        config.set("隐藏命令", false);
                        config.save();
                        sender.sendMessage("已关闭隐藏命令");
                    }else {
                        config.set("隐藏命令", true);
                        sender.sendMessage("已开启隐藏命令");
                    }
                    //重新发送命令
                    for (Player player : this.getServer().getOnlinePlayers().values()) {
                        player.sendCommandData();
                    }
                    break;
                case "tip":
                    if (config.getBoolean("隐藏提示")) {
                        config.set("隐藏提示", false);
                        config.save();
                        sender.sendMessage("已关闭隐藏提示");
                    }else {
                        config.set("隐藏提示", true);
                        config.save();
                        sender.sendMessage("已开启隐藏提示");
                    }
                    break;
                default:
                    this.sendCommandHelp(sender);
                    break;
            }
        }else {
            this.sendCommandHelp(sender);
        }
        return true;
    }

    private void sendCommandHelp(CommandSender sender) {
        sender.sendMessage(
                "§a/hcmd cmd §7开启/关闭隐藏命令\n" +
                "§a/hcmd tip §7开启/关闭隐藏提示"
        );
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (event.getPlayer().isOp()) {
            return;
        }
        if ("/help".equalsIgnoreCase(event.getMessage().split(" ")[0]) &&
                this.getConfig().getBoolean("隐藏命令")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDataPacketSend(DataPacketSendEvent event) {
        if (event.getPlayer().isOp()) {
            return;
        }
        if (event.getPacket() instanceof AvailableCommandsPacket) {
            AvailableCommandsPacket packet = (AvailableCommandsPacket) event.getPacket();
            if (this.getConfig().getBoolean("隐藏命令")) {
                packet.commands = new HashMap<>();
                packet.encode(); //重新编码
            }
        }else if (event.getPacket() instanceof TextPacket) {
            TextPacket packet = (TextPacket) event.getPacket();
            if (packet.type == TextPacket.TYPE_TRANSLATION &&
                    packet.message.startsWith("§c%commands.generic.unknown") &&
                    this.getConfig().getBoolean("隐藏提示")) {
                event.setCancelled(true);
            }
        }
    }

}
