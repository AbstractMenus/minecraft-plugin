package ru.abstractmenus.services;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;
import ru.abstractmenus.MainConfig;
import ru.abstractmenus.api.Logger;
import ru.abstractmenus.api.variables.Var;
import ru.abstractmenus.variables.VariableManagerImpl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

public final class BungeeManager implements PluginMessageListener {
    
    private static BungeeManager instance;

    private final Plugin plugin;
    private ScheduledExecutorService timer;
    private ScheduledFuture<?> task;

    private final Set<String> servers = new HashSet<>();
    private final Map<String, Integer> playersOnline = new ConcurrentHashMap<>();
    private final Map<String, InetSocketAddress> serverAddresses = new ConcurrentHashMap<>();
    private final Map<String, Boolean> serversOnline = new ConcurrentHashMap<>();

    public BungeeManager(Plugin plugin, MainConfig conf) {
        this.plugin = plugin;

        if(conf.isBungeeCord()) {
            plugin.getServer().getMessenger()
                    .registerOutgoingPluginChannel(plugin, "BungeeCord");
            plugin.getServer().getMessenger()
                    .registerOutgoingPluginChannel(plugin, "abstractmenus:main");
            
            plugin.getServer().getMessenger()
                    .registerIncomingPluginChannel(plugin, "BungeeCord", this);

            if (conf.isBungeePing()) {
                this.timer = Executors.newScheduledThreadPool(1);
                startOnlineTimer();
            }
        }
        
        instance = this;
    }
    
    public static BungeeManager instance() {
        return instance;
    }

    public Set<String> getServers() {
        return servers;
    }

    public void sendPluginMessage(String... data) {
       if (plugin.isEnabled()) {
           Iterator<? extends Player> iterator = Bukkit.getOnlinePlayers().iterator();

           if (iterator.hasNext()){
               sendPluginMessage(iterator.next(), data);
           }
       }
    }

    public void sendPluginMessage(byte[] data) {
        if (plugin.isEnabled()) {
            Iterator<? extends Player> iterator = Bukkit.getOnlinePlayers().iterator();

            if (iterator.hasNext()){
                sendPluginMessage(iterator.next(), data);
            }
        }
    }

    public void sendPluginMessage(Player player, String... data) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        for(String line : data){
            out.writeUTF(line);
        }
        sendPluginMessage(player, out.toByteArray());
    }

    public void sendPluginMessage(Player player, byte[] data) {
        if (plugin.isEnabled())
            player.sendPluginMessage(plugin, "BungeeCord", data);
    }

    public void forwardMessage(String subchannel, byte[] message) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF("Forward");
        out.writeUTF("ALL");
        out.writeUTF(subchannel);
        out.writeShort(message.length);
        out.write(message);

        sendPluginMessage(out.toByteArray());
    }

    public int getOnline(String server) {
        return playersOnline.getOrDefault(server, 0);
    }

    public boolean isOnline(String server) {
        return serversOnline.getOrDefault(server.toLowerCase(), false);
    }

    public InetSocketAddress getAddress(String server) {
        return serverAddresses.get(server.toLowerCase());
    }

    public int getOnline() {
        int online = 0;
        for (int i : playersOnline.values()) online += i;
        return online;
    }

    public boolean ping(String server) {
        try {
            InetSocketAddress address = getAddress(server);

            if (address != null) {
                Socket socket = new Socket();
                socket.setSoTimeout(1000);
                socket.connect(address, 1000);
                socket.close();

                return true;
            }
        } catch (IOException ignore) { }

        return false;
    }

    public void startOnlineTimer() {
        if (task == null && !timer.isShutdown())
            task = timer.scheduleAtFixedRate(this::pingTask, 0, 2000, TimeUnit.MILLISECONDS);
    }

    public void stopOnlineTimer() {
        if (task != null)
            task.cancel(true);

        if (timer != null && !timer.isShutdown())
            timer.shutdown();
    }

    private void pingTask() {
        if(getServers().isEmpty()) {
            sendPluginMessage("GetServers");
            return;
        }

        for (String server : getServers()) {
            if (getAddress(server) == null)
                sendPluginMessage("ServerIP", server);

            sendPluginMessage("PlayerCount", server);

            serversOnline.put(server.toLowerCase(), ping(server));
        }
    }

    @Override
    public void onPluginMessageReceived(String channel, @NotNull Player player, @NotNull byte[] bytes) {
        if (!channel.equals("BungeeCord")) return;

        try{
            ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
            String subChannel = in.readUTF();

            if (subChannel.equals("GetServers")) {
                String[] servers = in.readUTF().split(",");

                for (String server : servers){
                    this.servers.add(server.trim());
                }
            } else if (subChannel.equals("PlayerCount")){
                try {
                    String server = in.readUTF();
                    int count = in.readInt();
                    playersOnline.put(server, count);
                } catch (Exception ignore){ }
            } else if(subChannel.equals("SyncVar") && VariableManagerImpl.instance().isSyncVars()){
                short len = in.readShort();
                byte[] data = new byte[len];
                in.readFully(data);

                in = ByteStreams.newDataInput(data);

                String action = in.readUTF();
                String key = in.readUTF();
                String name = key.split(":")[1];

                if (action.equals("save")){
                    String value = in.readUTF();
                    long expiry = in.readLong();

                    Var var = VariableManagerImpl.instance().createBuilder()
                            .name(name)
                            .value(value)
                            .expiry(expiry)
                            .build();

                    VariableManagerImpl.instance().cache(key, var);
                } else if (action.equals("delete")){
                    VariableManagerImpl.instance().delete(key);
                }
            } else if (subChannel.equals("ServerIP")){
                String serverName = in.readUTF();
                String ip = in.readUTF();
                int port = in.readUnsignedShort();

                serverAddresses.put(serverName.toLowerCase(), new InetSocketAddress(ip, port));
            }
        } catch (Throwable t){
            Logger.warning("Cannot receive message from BungeeCord:");
            t.printStackTrace();
        }
    }
}
