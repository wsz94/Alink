package com.alibaba.alink.common.io.catalog.plugin;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.table.factories.Factory;

import com.alibaba.alink.common.io.plugin.ClassLoaderContainer;
import com.alibaba.alink.common.io.plugin.ClassLoaderFactory;
import com.alibaba.alink.common.io.plugin.PluginDescriptor;
import com.alibaba.alink.common.io.plugin.PluginDistributeCache;
import com.alibaba.alink.common.io.plugin.RegisterKey;

import java.io.Serializable;
import java.util.function.Function;
import java.util.function.Predicate;

public class DataHubClassLoaderFactory extends ClassLoaderFactory implements Serializable {

	public DataHubClassLoaderFactory(String version) {
		super(
			new RegisterKey("datahub", version),
			PluginDistributeCache.createDistributeCache("datahub", version)
		);
	}

	@Override
	public ClassLoader create() {
		return ClassLoaderContainer
			.getInstance()
			.create(
				registerKey, distributeCache, Factory.class,
				new DataHubCatalogServiceFilter(registerKey),
				new DataHubCatalogVersionGetter()
			);
	}

	private static class DataHubCatalogServiceFilter implements Predicate <Factory> {
		private final RegisterKey registerKey;

		public DataHubCatalogServiceFilter(RegisterKey registerKey) {
			this.registerKey = registerKey;
		}

		@Override
		public boolean test(Factory factory) {
			String catalogType = factory.factoryIdentifier();

			return catalogType != null
				&& catalogType.equalsIgnoreCase(registerKey.getName());
		}
	}

	private static class DataHubCatalogVersionGetter implements
		Function <Tuple2 <Factory, PluginDescriptor>, String> {

		@Override
		public String apply(Tuple2 <Factory, PluginDescriptor> factory) {
			return factory.f1.getVersion();
		}
	}
}
