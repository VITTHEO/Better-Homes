package net.onehype.VitTheo.BetterHomes.storage;

import net.onehype.VitTheo.BetterHomes.home.Home;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public interface HomeStorage {
    Map<UUID, Map<String, Home>> load() throws IOException;

    void save(Map<UUID, Map<String, Home>> homes) throws IOException;
}
