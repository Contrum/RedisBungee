/*
 * Copyright (c) 2013-present RedisBungee contributors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *
 *  http://www.eclipse.org/legal/epl-v10.html
 */

package com.imaginarycode.minecraft.redisbungee.api.tasks;

import com.imaginarycode.minecraft.redisbungee.AbstractRedisBungeeAPI;
import com.imaginarycode.minecraft.redisbungee.api.RedisBungeePlugin;
import com.imaginarycode.minecraft.redisbungee.api.summoners.JedisClusterSummoner;
import com.imaginarycode.minecraft.redisbungee.api.summoners.JedisPooledSummoner;
import com.imaginarycode.minecraft.redisbungee.api.summoners.Summoner;
import com.imaginarycode.minecraft.redisbungee.api.RedisBungeeMode;
import redis.clients.jedis.UnifiedJedis;

import java.util.concurrent.Callable;

public abstract class RedisTask<V> implements Runnable, Callable<V> {

    protected final Summoner<?> summoner;
    protected final AbstractRedisBungeeAPI api;
    protected RedisBungeePlugin<?> plugin;

    @Override
    public V call() throws Exception {
        return execute();
    }

    public RedisTask(AbstractRedisBungeeAPI api) {
        this.api = api;
        this.summoner = api.getSummoner();
    }

    public RedisTask(RedisBungeePlugin<?> plugin) {
        this.plugin = plugin;
        this.api = plugin.getAbstractRedisBungeeApi();
        this.summoner = api.getSummoner();
    }

    public abstract V unifiedJedisTask(UnifiedJedis unifiedJedis);

    @Override
    public void run() {
        this.execute();
    }

    public V execute(){
        // JedisCluster, JedisPooled in fact is just UnifiedJedis does not need new instance since its single instance anyway.
        if (api.getMode() == RedisBungeeMode.SINGLE) {
            JedisPooledSummoner jedisSummoner = (JedisPooledSummoner) summoner;
            return this.unifiedJedisTask(jedisSummoner.obtainResource());
        } else if (api.getMode() == RedisBungeeMode.CLUSTER) {
            JedisClusterSummoner jedisClusterSummoner = (JedisClusterSummoner) summoner;
            return this.unifiedJedisTask(jedisClusterSummoner.obtainResource());
        }
        return null;
    }

    public RedisBungeePlugin<?> getPlugin() {
        if (plugin == null) {
            throw new NullPointerException("Plugin is null in the task");
        }
        return plugin;
    }
}
