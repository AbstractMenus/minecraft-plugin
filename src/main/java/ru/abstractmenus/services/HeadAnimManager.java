package ru.abstractmenus.services;

import ru.abstractmenus.hocon.api.ConfigNode;
import ru.abstractmenus.api.Logger;
import ru.abstractmenus.hocon.api.ConfigurationLoader;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class HeadAnimManager {

    private static HeadAnimManager instance;

    private final ConfigurationLoader confLoader;
    private Map<String, List<String>> frames;
    private Map<String, Integer> currentFrames;

    public HeadAnimManager(ConfigurationLoader confLoader) throws Exception {
        this.confLoader = confLoader;
        loadAnimations();
        instance = this;
    }

    public static HeadAnimManager instance() {
        return instance;
    }

    private String createKey(String animName, String id){
        return animName + ":" + id;
    }

    public String getNextFrame(String animName, String id){
        List<String> list = frames.get(animName);

        if(list != null){
            String key = createKey(animName, id);
            int currentFrame = currentFrames.computeIfAbsent(key, v -> 0);
            String frame = list.get(currentFrames.computeIfAbsent(key, v -> 0));
            if(++currentFrame > list.size()-1) currentFrame = 0;
            currentFrames.put(key, currentFrame);
            return frame;
        }

        return null;
    }

    public void loadAnimations() throws Exception {
        ConfigNode node = confLoader.load();

        if(frames != null) frames.clear();
        if(currentFrames != null) currentFrames.clear();

        frames = new ConcurrentHashMap<>();
        currentFrames = new ConcurrentHashMap<>();

        for (Map.Entry<String, ConfigNode> entry : node.childrenMap().entrySet()){
            String name = entry.getKey();
            List<String> fr = entry.getValue().getList(String.class);
            frames.put(name, fr);
            Logger.info("Loaded head animation \"" + name + "\"");
        }
    }
}
