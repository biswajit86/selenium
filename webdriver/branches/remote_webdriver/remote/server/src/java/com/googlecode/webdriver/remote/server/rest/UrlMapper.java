package com.googlecode.webdriver.remote.server.rest;

import com.googlecode.webdriver.remote.server.DriverSessions;
import com.googlecode.webdriver.remote.server.renderer.JsonResult;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Map;
import java.util.LinkedHashMap;

public class UrlMapper {
    private Map<ResultType, Renderer> globals = new LinkedHashMap<ResultType, Renderer>();
    private Set<ResultConfig> configs = new LinkedHashSet<ResultConfig>();
	private final DriverSessions sessions;

	public UrlMapper(DriverSessions sessions) {
		this.sessions = sessions;
	}

	public ResultConfig bind(String url, Class<? extends Handler> handlerClazz) {
		ResultConfig config = new ResultConfig(url, handlerClazz, sessions);
		configs.add(config);
        for (Map.Entry<ResultType, Renderer> entry : globals.entrySet()) {
            config.on(entry.getKey(), entry.getValue());
        }
        return config;
	}

	public ResultConfig getConfig(String url) throws Exception {
		for (ResultConfig config : configs) {
			if (config.isFor(url))
				return config;
		}
		
		return null;
	}

    public void addGlobalHandler(ResultType type, Renderer renderer) {
        globals.put(type, renderer);

        for (ResultConfig config : configs) {
            config.on(type, renderer);
        }
    }
}
