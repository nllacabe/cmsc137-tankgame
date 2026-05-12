package tank1990.network;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

import tank1990.player.PlayerType;

/**
 * @class NetworkManager
 * @brief Handles all socket communication for multiplayer (ported from
 *        AquinoLRP04.c).
 *
 *        Design mirrors the C implementation:
 *        - Master listens on its port; each slave connects to it.
 *        - Slave connects to the master's IP:port from the config file.
 *        - Player count = 1 (master) + number of slave entries in config.
 *        - Maximum = 4 players (1 master + 3 slaves).
 *
 *        Usage (Master):
 *        NetworkManager nm = new NetworkManager("config.txt");
 *        nm.startMaster(stateConsumer); // blocks until all slaves connect
 *
 *        Usage (Slave):
 *        NetworkManager nm = new NetworkManager("config.txt");
 *        nm.startSlave(stateConsumer); // retries until master is ready
 */
public class NetworkManager {

    // ------------------------------------------------------------------ inner
    // types

    /** One entry in the config file. */
    public record NodeInfo(String ip, int port) {
    }

    /** Snapshot of game state sent over the network each tick. */
    /** Per-player state inside a FullGameSnapshot. */
    public static class PlayerState implements Serializable {
        public PlayerType playerType;
        public int x, y;
        public String direction;
        public boolean isDestroyed;
        public int remainingLives;

        public PlayerState(PlayerType playerType, int x, int y,
                String direction, boolean isDestroyed, int remainingLives) {
            this.playerType = playerType;
            this.x = x;
            this.y = y;
            this.direction = direction;
            this.isDestroyed = isDestroyed;
            this.remainingLives = remainingLives;
        }
    }

    /** Per-bullet state inside a FullGameSnapshot. */
    public static class BulletState implements Serializable {
        public int x, y;
        public String direction;
        public boolean isEnemyBullet;

        public BulletState(int x, int y, String direction, boolean isEnemyBullet) {
            this.x = x;
            this.y = y;
            this.direction = direction;
            this.isEnemyBullet = isEnemyBullet;
        }
    }

    /**
     * State of one map tile — row, col, destroyed flag, and subpiece visibility.
     */
    public static class TileState implements Serializable {
        public int row, col;
        public boolean isDestroyed;
        public boolean[][] subpieces; // [TILE_SUBDIVISION][TILE_SUBDIVISION]

        public TileState(int row, int col, boolean isDestroyed, boolean[][] subpieces) {
            this.row = row;
            this.col = col;
            this.isDestroyed = isDestroyed;
            // Deep copy subpieces so the array is safely serialized
            int sz = subpieces.length;
            this.subpieces = new boolean[sz][sz];
            for (int r = 0; r < sz; r++)
                for (int c = 0; c < sz; c++)
                    this.subpieces[r][c] = subpieces[r][c];
        }
    }

    /**
     * Full authoritative game snapshot sent by master to all slaves every tick.
     * Contains all player states, bullet states, tile damage, and game-over flag.
     */
    public static class FullGameSnapshot implements Serializable {
        public java.util.List<PlayerState> players;
        public java.util.List<BulletState> bullets;
        public java.util.List<TileState> tiles; // damaged/destroyed tiles
        public boolean gameOver;
        public long timestamp;

        public FullGameSnapshot() {
            this.players = new java.util.ArrayList<>();
            this.bullets = new java.util.ArrayList<>();
            this.tiles = new java.util.ArrayList<>();
            this.gameOver = false;
            this.timestamp = System.currentTimeMillis();
        }
    }

    /**
     * Kept for backward compatibility — wraps a single player update.
     * New code should use FullGameSnapshot instead.
     */
    public static class GameStatePacket implements Serializable {
        public PlayerType playerType;
        public int x, y;
        public String direction;
        public boolean isShooting;
        public boolean isDestroyed;
        public boolean allPlayersEliminated;
        public int remainingLives;
        public long timestamp;

        public GameStatePacket(PlayerType playerType, int x, int y,
                String direction, boolean isShooting, boolean isDestroyed) {
            this.playerType = playerType;
            this.x = x;
            this.y = y;
            this.direction = direction;
            this.isShooting = isShooting;
            this.isDestroyed = isDestroyed;
            this.allPlayersEliminated = false;
            this.remainingLives = -1;
            this.timestamp = System.currentTimeMillis();
        }
    }

    /**
     * Sent once by master to each slave immediately after connection,
     * telling the slave which PlayerType it controls.
     */
    public static class PlayerAssignmentPacket implements Serializable {
        public PlayerType assignedPlayerType;

        public PlayerAssignmentPacket(PlayerType assignedPlayerType) {
            this.assignedPlayerType = assignedPlayerType;
        }
    }

    /** Input command sent from a slave to the master. */
    public static class InputPacket implements Serializable {
        public PlayerType playerType;
        public String action; // "MOVE_UP" "MOVE_DOWN" "MOVE_LEFT" "MOVE_RIGHT" "SHOOT" "STOP_X" "STOP_Y"
        public long timestamp;

        public InputPacket(PlayerType playerType, String action) {
            this.playerType = playerType;
            this.action = action;
            this.timestamp = System.currentTimeMillis();
        }
    }

    // ------------------------------------------------------------------ fields

    private static final String CONFIG_FILE_DEFAULT = "config.txt";
    private static final int MAX_PLAYERS = 4;
    private static final int CONNECT_RETRY_MS = 2000;

    private final String configFile;

    // Master-side state
    private NodeInfo masterInfo;
    private List<NodeInfo> slaveInfos = new ArrayList<>();
    private ServerSocket serverSocket;
    private List<Socket> slaveConns = new ArrayList<>();
    private List<ObjectOutputStream> slaveOut = new ArrayList<>();
    private List<ObjectInputStream> slaveIn = new ArrayList<>();

    // Slave-side state
    private NodeInfo masterNode;
    private Socket masterConn;
    private ObjectOutputStream masterOut;
    private ObjectInputStream masterIn;

    private boolean isMaster = false;
    private int numPlayers = 1; // determined after reading config
    private PlayerType localPlayerType = PlayerType.PLAYER_1; // updated after assignment

    /** Called whenever a packet arrives from the network. */
    private Consumer<GameStatePacket> onStateReceived;
    /** Called whenever an input packet arrives at the master. */
    private Consumer<InputPacket> onInputReceived;

    private volatile boolean running = false;

    // ------------------------------------------------------------------
    // constructor

    public NetworkManager() {
        this(CONFIG_FILE_DEFAULT);
    }

    public NetworkManager(String configFile) {
        this.configFile = configFile;
    }

    // ------------------------------------------------------------------ config
    // parsing
    // Mirrors readfile() from AquinoLRP04.c

    /**
     * Reads the config file as MASTER: populates slaveInfos list.
     * numPlayers = 1 + slaveInfos.size()
     */
    private void parseMasterConfig() throws IOException {
        slaveInfos.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(configFile))) {
            boolean inMaster = false;
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#"))
                    continue;

                if (line.equals("MASTER")) {
                    inMaster = true;
                    continue;
                }
                if (line.equals("SLAVE")) {
                    inMaster = false;
                    continue;
                }

                if (inMaster) {
                    String[] parts = line.split("\\s+");
                    if (parts.length == 2) {
                        slaveInfos.add(new NodeInfo(parts[0], Integer.parseInt(parts[1])));
                    }
                }
            }
        }
        numPlayers = Math.min(1 + slaveInfos.size(), MAX_PLAYERS);
        System.out.printf("[NetworkManager] Master config loaded — %d slave(s) → %d player(s)%n",
                slaveInfos.size(), numPlayers);
    }

    /**
     * Reads the config file as SLAVE: populates masterNode.
     */
    private void parseSlaveConfig() throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(configFile))) {
            boolean inSlave = false;
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#"))
                    continue;

                if (line.equals("SLAVE")) {
                    inSlave = true;
                    continue;
                }
                if (line.equals("MASTER")) {
                    inSlave = false;
                    continue;
                }

                if (inSlave) {
                    String[] parts = line.split("\\s+");
                    if (parts.length == 2) {
                        masterNode = new NodeInfo(parts[0], Integer.parseInt(parts[1]));
                        break;
                    }
                }
            }
        }
        if (masterNode == null)
            throw new IOException("No SLAVE section with master address found in config.");
        System.out.printf("[NetworkManager] Slave config loaded — master at %s:%d%n",
                masterNode.ip(), masterNode.port());
    }

    // ------------------------------------------------------------------ master API

    /**
     * Starts this node as MASTER.
     * Reads config, opens server socket, waits for all slaves to connect,
     * then launches receive threads.
     *
     * @param masterPort      port this master listens on
     * @param onInput         callback invoked on the EDT when a slave sends input
     * @param onStateReceived callback for state packets (usually not used on master
     *                        side)
     */
    public void startMaster(int masterPort,
            Consumer<InputPacket> onInput,
            Consumer<GameStatePacket> onStateReceived) throws IOException {
        isMaster = true;
        this.onInputReceived = onInput;
        this.onStateReceived = onStateReceived;

        parseMasterConfig();

        int numSlaves = slaveInfos.size();
        if (numSlaves == 0) {
            System.out.println("[NetworkManager] No slaves in config — running standalone.");
            running = true;
            return;
        }

        serverSocket = new ServerSocket(masterPort);
        System.out.printf("[NetworkManager] Master listening on port %d, expecting %d slave(s)…%n",
                masterPort, numSlaves);

        for (int i = 0; i < numSlaves; i++) {
            Socket conn = serverSocket.accept();
            slaveConns.add(conn);
            ObjectOutputStream oos = new ObjectOutputStream(conn.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(conn.getInputStream());
            slaveOut.add(oos);
            slaveIn.add(ois);
            System.out.printf("[NetworkManager] Slave %d connected from %s%n",
                    i, conn.getInetAddress());
        }

        running = true;
        System.out.println("[NetworkManager] All slaves connected — starting receive threads.");

        // Tell each slave which PlayerType they control.
        // Master = PLAYER_1, first slave = PLAYER_2, second = PLAYER_3, etc.
        for (int i = 0; i < numSlaves; i++) {
            try {
                PlayerType assigned = PlayerType.fromIndex(i + 1); // +1 because master is index 0
                PlayerAssignmentPacket assignment = new PlayerAssignmentPacket(assigned);
                slaveOut.get(i).writeObject(assignment);
                slaveOut.get(i).reset();
                System.out.printf("[NetworkManager] Assigned %s to slave %d%n", assigned, i);
            } catch (IOException e) {
                System.err.printf("[NetworkManager] Failed to send assignment to slave %d: %s%n", i, e.getMessage());
            }
        }

        // Launch one receive thread per slave (mirrors the C recv loop)
        for (int i = 0; i < numSlaves; i++) {
            final int idx = i;
            Thread t = new Thread(() -> masterReceiveLoop(idx), "SlaveReceiver-" + i);
            t.setDaemon(true);
            t.start();
        }
    }

    /**
     * Background loop: reads InputPackets from one slave and fires onInputReceived.
     */
    private void masterReceiveLoop(int slaveIdx) {
        ObjectInputStream ois = slaveIn.get(slaveIdx);
        while (running) {
            try {
                Object obj = ois.readObject();
                if (obj instanceof InputPacket pkt && onInputReceived != null) {
                    onInputReceived.accept(pkt);
                }
            } catch (EOFException | SocketException e) {
                System.out.printf("[NetworkManager] Slave %d disconnected.%n", slaveIdx);
                break;
            } catch (Exception e) {
                if (running)
                    e.printStackTrace();
                break;
            }
        }
    }

    /**
     * Broadcasts a GameStatePacket to all connected slaves.
     */
    public void broadcastState(GameStatePacket packet) {
        if (!isMaster || !running)
            return;
        for (int i = 0; i < slaveOut.size(); i++) {
            try {
                slaveOut.get(i).writeObject(packet);
                slaveOut.get(i).reset();
            } catch (IOException e) {
                System.err.printf("[NetworkManager] Failed to send state to slave %d: %s%n", i, e.getMessage());
            }
        }
    }

    /**
     * Broadcasts a full FullGameSnapshot to all connected slaves.
     * This is the primary method for synchronising game state each tick.
     */
    public void broadcastSnapshot(FullGameSnapshot snapshot) {
        if (!isMaster || !running)
            return;
        for (int i = 0; i < slaveOut.size(); i++) {
            try {
                slaveOut.get(i).writeObject(snapshot);
                slaveOut.get(i).reset();
            } catch (IOException e) {
                System.err.printf("[NetworkManager] Failed to send snapshot to slave %d: %s%n", i, e.getMessage());
            }
        }
    }

    // ------------------------------------------------------------------ slave API

    /**
     * Starts this node as SLAVE.
     * Reads config, connects to master (retrying like the C version),
     * then launches a receive thread for GameStatePackets.
     *
     * @param onState callback invoked on the EDT for each incoming GameStatePacket
     */
    public void startSlave(Consumer<GameStatePacket> onState) throws IOException {
        this.startSlave(onState, null);
    }

    public void startSlave(Consumer<GameStatePacket> onState,
            Consumer<FullGameSnapshot> onSnapshot) throws IOException {
        isMaster = false;
        this.onStateReceived = onState;
        this.onSnapshotReceived = onSnapshot;

        parseSlaveConfig();

        masterConn = new Socket();
        System.out.printf("[NetworkManager] Slave connecting to master %s:%d…%n",
                masterNode.ip(), masterNode.port());

        // Retry loop — mirrors the C while(connect(...) < 0) loop
        while (true) {
            try {
                masterConn.connect(new InetSocketAddress(masterNode.ip(), masterNode.port()), 3000);
                break;
            } catch (IOException e) {
                System.out.println("[NetworkManager] Master not ready, retrying in 2 s…");
                try {
                    Thread.sleep(CONNECT_RETRY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        masterOut = new ObjectOutputStream(masterConn.getOutputStream());
        masterIn = new ObjectInputStream(masterConn.getInputStream());
        running = true;
        System.out.println("[NetworkManager] Connected to master!");

        // Read the PlayerType assignment from master before starting the receive loop
        try {
            Object firstPacket = masterIn.readObject();
            if (firstPacket instanceof PlayerAssignmentPacket assignment) {
                this.localPlayerType = assignment.assignedPlayerType;
                System.out.printf("[NetworkManager] Assigned player type: %s%n", this.localPlayerType);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("[NetworkManager] Failed to read player assignment: " + e.getMessage());
        }

        // Start receive thread for state packets from master
        Thread t = new Thread(this::slaveReceiveLoop, "MasterReceiver");
        t.setDaemon(true);
        t.start();
    }

    /** Callback for full game snapshots received by slave. */
    private Consumer<FullGameSnapshot> onSnapshotReceived;

    /**
     * Background loop: reads packets from master and fires appropriate callbacks.
     */
    private void slaveReceiveLoop() {
        while (running) {
            try {
                Object obj = masterIn.readObject();
                if (obj instanceof FullGameSnapshot snap && onSnapshotReceived != null) {
                    onSnapshotReceived.accept(snap);
                } else if (obj instanceof GameStatePacket pkt && onStateReceived != null) {
                    onStateReceived.accept(pkt);
                }
            } catch (EOFException | SocketException e) {
                System.out.println("[NetworkManager] Master disconnected.");
                break;
            } catch (Exception e) {
                if (running)
                    e.printStackTrace();
                break;
            }
        }
    }

    /**
     * Sends an InputPacket from this slave to the master.
     * Called from GamePanel when the local player presses a key.
     */
    public void sendInput(InputPacket packet) {
        if (isMaster || !running || masterOut == null)
            return;
        try {
            masterOut.writeObject(packet);
            masterOut.reset();
        } catch (IOException e) {
            System.err.println("[NetworkManager] Failed to send input: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------ shared
    // helpers

    /** How many players will play (1 master + N slaves, max 4). */
    public int getNumPlayers() {
        return numPlayers;
    }

    public boolean isMaster() {
        return isMaster;
    }

    public boolean isRunning() {
        return running;
    }

    /**
     * Returns the PlayerType this node controls.
     * Master is always PLAYER_1.
     * Slaves receive their assignment from the master immediately after connecting
     * via a PlayerAssignmentPacket, so this value is accurate after startSlave()
     * returns.
     */
    public PlayerType getLocalPlayerType() {
        return localPlayerType;
    }

    /** Gracefully shuts down all connections. */
    public void shutdown() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed())
                serverSocket.close();
            for (Socket s : slaveConns) {
                if (!s.isClosed())
                    s.close();
            }
            if (masterConn != null && !masterConn.isClosed())
                masterConn.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ------------------------------------------------------------------ static
    // factory

    /**
     * Convenience: read the config and return how many players would join,
     * without actually opening any sockets. Used by MenuPanel to show the count.
     */
    public static int peekPlayerCount(String configFile) {
        try {
            int slaves = 0;
            try (BufferedReader br = new BufferedReader(new FileReader(configFile))) {
                boolean inMaster = false;
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#"))
                        continue;
                    if (line.equals("MASTER")) {
                        inMaster = true;
                        continue;
                    }
                    if (line.equals("SLAVE")) {
                        inMaster = false;
                        continue;
                    }
                    if (inMaster) {
                        String[] p = line.split("\\s+");
                        if (p.length == 2)
                            slaves++;
                    }
                }
            }
            return Math.min(1 + slaves, MAX_PLAYERS);
        } catch (IOException e) {
            return 1; // fallback to solo
        }
    }
}