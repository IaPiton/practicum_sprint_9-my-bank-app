package ru.yandex.practicum.service;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.kv.model.GetValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;

@Slf4j
@Component
public class ConsulConfigReader {

    @Value("${spring.cloud.consul.host:localhost}")
    private String consulHost;

    @Value("${spring.cloud.consul.port:8500}")
    private int consulPort;

    private final Yaml yaml = new Yaml();

    private ConsulConfigReader() {
    }

    public static ConsulConfigReader createConsulConfigReader() {
        return new ConsulConfigReader();
    }


    public Map<String, Object> readConfig(String path) {
        try {
            ConsulClient client = new ConsulClient(consulHost, consulPort);
            GetValue kvValue = client.getKVValue(path).getValue();

            if (kvValue != null && kvValue.getDecodedValue() != null) {
                String yamlContent = kvValue.getDecodedValue();
                log.debug("Loaded config from Consul path: {}", path);
                return yaml.load(yamlContent);
            } else {
                log.warn("No data found in Consul at path: {}", path);
                return null;
            }
        } catch (Exception e) {
            log.error("Failed to load from Consul at path {}: {}", path, e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> readConfigSection(String path, String prefix) {
        Map<String, Object> config = readConfig(path);
        if (config != null && config.containsKey(prefix)) {
            return (Map<String, Object>) config.get(prefix);
        }
        return null;
    }
}