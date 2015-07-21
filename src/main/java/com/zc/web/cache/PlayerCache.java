package com.zc.web.cache;

import com.zc.web.data.model.Player;
import com.zc.web.service.PlayerService;
import com.zc.web.util.cache.LRUCache;

public class PlayerCache {
	/**
	 * 玩家缓存
	 */
	private LRUCache<Long, Player> playerSet = new LRUCache<Long, Player>(5000);

	public static PlayerCache INSTANCE = new PlayerCache();

	private PlayerCache() {

	}

	/**
	 * 先从缓存中取，不存在则从DB拉取放入缓存中
	 * 
	 * @param playerId
	 * @return
	 */
	public Player getPlayer(long playerId) {
		Player player = null;
		synchronized (playerSet) {
			player = playerSet.get(playerId);
			if (player == null) {
				player = PlayerService.loadPlayerById(playerId);
			}
			playerSet.put(playerId, player);
		}

		return player;
	}

	public LRUCache<Long, Player> getPlayerSet() {
		return playerSet;
	}
}
