package tank1990.core;

/**
 * @class GameMode
 * @brief Game modes — single player, local multiplayer, or networked multiplayer.
 */
public enum GameMode {
    MODE_SINGLE_PLAYER,   // 1 player, local
    MODE_MULTI_PLAYER,    // 2 players, local (same machine)
    MODE_NETWORK_MASTER,  // this node is the master in a network game
    MODE_NETWORK_SLAVE    // this node is a slave in a network game
}
