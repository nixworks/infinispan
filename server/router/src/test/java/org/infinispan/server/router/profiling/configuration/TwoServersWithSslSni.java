package org.infinispan.server.router.profiling.configuration;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.server.hotrod.HotRodServer;
import org.infinispan.server.hotrod.test.HotRodTestingUtil;
import org.infinispan.server.router.MultiTenantRouter;
import org.infinispan.server.router.configuration.builder.MultiTenantRouterConfigurationBuilder;
import org.infinispan.server.router.profiling.PerfTestConfiguration;
import org.infinispan.server.router.router.Router;
import org.infinispan.server.router.routes.Route;
import org.infinispan.server.router.routes.RouteDestination;
import org.infinispan.server.router.routes.RouteSource;
import org.infinispan.server.router.routes.hotrod.NettyHandlerRouteDestination;
import org.infinispan.server.router.routes.hotrod.SniNettyRouteSource;
import org.infinispan.server.router.utils.HotRodClientTestingUtil;

public class TwoServersWithSslSni implements PerfTestConfiguration {

    private final String KEYSTORE_LOCATION_FOR_HOTROD_1 = getClass().getClassLoader().getResource("sni_server_keystore.jks").getPath();
    private final String TRUSTSTORE_LOCATION_FOT_HOTROD_1 = getClass().getClassLoader().getResource("sni_client_truststore.jks").getPath();

    private final String KEYSTORE_LOCATION_FOR_HOTROD_2 = getClass().getClassLoader().getResource("default_server_keystore.jks").getPath();
    private final char[] KEYSTORE_PASSWORD = "secret".toCharArray();

    @Override
    public List<HotRodServer> initServers() {

        HotRodServer hotrodServer1 = HotRodTestingUtil.startHotRodServerWithoutTransport();
        HotRodServer hotrodServer2 = HotRodTestingUtil.startHotRodServerWithoutTransport();
        return Arrays.asList(hotrodServer1, hotrodServer2);
    }

    @Override
    public Optional<Set<Route<? extends RouteSource, ? extends RouteDestination>>> initRoutes(List<HotRodServer> servers) {
        Set<Route<? extends RouteSource, ? extends RouteDestination>> routes = new HashSet<>();
        NettyHandlerRouteDestination hotrod1Destination = new NettyHandlerRouteDestination("hotrod1", servers.get(0).getInitializer());
        SniNettyRouteSource hotrod1Source = new SniNettyRouteSource("hotrod1", KEYSTORE_LOCATION_FOR_HOTROD_1, KEYSTORE_PASSWORD);
        routes.add(new Route<>(hotrod1Source, hotrod1Destination));

        NettyHandlerRouteDestination hotrod2Destination = new NettyHandlerRouteDestination("hotrod2", servers.get(1).getInitializer());
        SniNettyRouteSource hotrod2Source = new SniNettyRouteSource("hotrod2", KEYSTORE_LOCATION_FOR_HOTROD_2, KEYSTORE_PASSWORD);
        routes.add(new Route<>(hotrod2Source, hotrod2Destination));

        return Optional.of(routes);
    }

    @Override
    public Optional<MultiTenantRouter> initRouter(Optional<Set<Route<? extends RouteSource, ? extends RouteDestination>>> routes) {
        MultiTenantRouterConfigurationBuilder routerConfigurationBuilder = new MultiTenantRouterConfigurationBuilder();
        routerConfigurationBuilder
                .hotrod()
                .port(0)
                .ip(InetAddress.getLoopbackAddress());

        routes.get().stream().forEach(r -> routerConfigurationBuilder.routing().add(r));

        MultiTenantRouter router = new MultiTenantRouter(routerConfigurationBuilder.build());
        router.start();
        return Optional.of(router);
    }

    @Override
    public RemoteCacheManager initClient(Optional<MultiTenantRouter> router, Optional<Set<Route<? extends RouteSource, ? extends RouteDestination>>> routes, List<HotRodServer> servers) {
        InetAddress ip = router.flatMap(r -> r.getRouter(Router.Protocol.HOT_ROD)).flatMap(r -> r.getIp()).get();
        int port = router.flatMap(r -> r.getRouter(Router.Protocol.HOT_ROD)).flatMap(r -> r.getPort()).get();
        return HotRodClientTestingUtil.createWithSni(ip, port, "hotrod1", TRUSTSTORE_LOCATION_FOT_HOTROD_1, KEYSTORE_PASSWORD);
    }

}
