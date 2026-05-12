package tank1990.tank;

import java.awt.*;
import java.io.IOException;
import java.util.*;

import tank1990.core.*;
import tank1990.projectiles.Blast;
import tank1990.projectiles.Bullet;
import tank1990.tile.Tile;
import tank1990.tile.TileType;

/**
 * @class AbstractTank
 * @brief Base class for all tanks (simplified: no powerups, no tiers, no red-tank, no helmet/frozen).
 */
public abstract class AbstractTank extends DynamicGameObject {

    private boolean isBulletDestroyed = true;

    private int points = 200;
    private int armorLevel = 1;

    private int dx, dy;
    private int speedUnit;
    private int maxSpeedUnit;
    private int speed;
    private int maxSpeed;

    protected TimeTick spawnTick;
    protected TimeTick spawnBlinkTick;
    private boolean isSpawnBlinkedOut = false;

    protected TimeTick movementTick;
    private TimeTick shootTick;

    private transient HashMap<Direction, TextureFX> textureFXs = null;
    protected TankTextureStruct tankTextureFxStruct = null;

    protected boolean spawnProtectionEnabled = true;

    protected boolean isFrozen = false;

    protected enum TankState { UNDEFINED, SPAWNING, SEEKING_GOAL, MOVING_FORWARD, RANDOM_ROTATION }
    protected TankState tankState = TankState.UNDEFINED;

    public AbstractTank(int x, int y)                  { this(x, y, Direction.DIRECTION_DOWNWARDS); }

    public AbstractTank(int x, int y, Direction dir) {
        setX(x); setY(y); setDir(dir);
        dx = 0; dy = 0; speedUnit = 0; maxSpeedUnit = 0; speed = 0; maxSpeed = 0;

        spawnTick = new TimeTick(Utils.Time2GameTick(Globals.SPAWN_PROTECTION_COOLDOWN_MS),
                () -> spawnProtectionEnabled = false);
        spawnTick.setRepeats(0);

        spawnBlinkTick = new TimeTick(Utils.Time2GameTick(Globals.SPAWN_PROTECTION_BLINK_PERIOD_MS));
        spawnBlinkTick.setRepeats(-1);

        movementTick = new TimeTick(Utils.Time2GameTick(150));
        movementTick.setRepeats(-1);

        shootTick = new TimeTick(Utils.Time2GameTick(Globals.DEFAULT_SHOOT_PERIOD_MS));
        shootTick.setRepeats(-1);

        tankState = TankState.SPAWNING;
    }

    public void createTextureFXs() {
        if (this.tankTextureFxStruct == null) return;
        if (this.textureFXs == null) this.textureFXs = new HashMap<>();
        else this.textureFXs.clear();

        this.textureFXs.put(Direction.DIRECTION_UPWARDS,   new TextureFX(tankTextureFxStruct.upwardsTexturePath));
        this.textureFXs.put(Direction.DIRECTION_RIGHT,     new TextureFX(tankTextureFxStruct.rightTexturePath));
        this.textureFXs.put(Direction.DIRECTION_DOWNWARDS, new TextureFX(tankTextureFxStruct.downwardsTexturePath));
        this.textureFXs.put(Direction.DIRECTION_LEFT,      new TextureFX(tankTextureFxStruct.leftTexturePath));
    }

    @Override
    public void draw(Graphics g) {
        if (this.spawnProtectionEnabled) {
            if (spawnBlinkTick.isTimeOut()) {
                spawnBlinkTick.reset();
                isSpawnBlinkedOut = !isSpawnBlinkedOut;
            }
            if (isSpawnBlinkedOut) return;
        }

        Dimension tankSize = Utils.normalizeDimension(g, Globals.TANK_WIDTH, Globals.TANK_HEIGHT);
        this.textureFXs.get(dir).setTargetSize(tankSize.width, tankSize.height);
        this.textureFXs.get(dir).draw(g, getX(), getY(), 0.0);

        this.speed    = Utils.normalize(g, this.speedUnit);
        this.maxSpeed = Utils.normalize(g, this.maxSpeedUnit);

        if (Globals.SHOW_BOUNDING_BOX) {
            g.setColor(Color.CYAN);
            g.drawRect((int) getBoundingBox().getX(), (int) getBoundingBox().getY(),
                       (int) getBoundingBox().getWidth(), (int) getBoundingBox().getHeight());
        }
    }

    public void update(GameLevel level) {
        if (this.spawnProtectionEnabled) {
            spawnTick.updateTick();
            spawnBlinkTick.updateTick();
        }

        movementTick.updateTick();
        if (!movementTick.isTimeOut()) return;
        movementTick.reset();

        int tankWidth  = (int) getSize().getWidth();
        int tankHeight = (int) getSize().getHeight();
        Dimension gameAreaSize = level.getGameAreaSize();

        if (tankWidth == 0 || tankHeight == 0) {
            tankWidth  = (int) gameAreaSize.getWidth()  / Globals.COL_TILE_COUNT;
            tankHeight = (int) gameAreaSize.getHeight() / Globals.ROW_TILE_COUNT;
        }
        setSize(new Dimension(tankWidth, tankHeight));

        Thread t = new Thread(() -> { synchronized (this) { move(level); } });
        t.start();
        try { t.join(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    // ------------------------------------------------------------------ getters/setters
    public int  getPoints()      { return points; }
    public int  getArmorLevel()  { return armorLevel; }
    public int  getSpeedUnit()   { return speedUnit; }
    public int  getMaxSpeedUnit(){ return maxSpeedUnit; }
    public boolean isFrozen()    { return isFrozen; }

    public void setPoints(int v)      { points = v; }
    public void setArmorLevel(int v)  { armorLevel = v; }
    public void setSpeedUnit(int v)   { speedUnit = v; }
    public void setMaxSpeedUnit(int v){ maxSpeedUnit = v; }
    public void setFrozen(boolean v)  { isFrozen = v; }

    public int getDx() { return dx; }
    public int getDy() { return dy; }

    public void decrementDx() { resetDy(); dx = Math.max(dx - speed, -maxSpeed); dir = Direction.DIRECTION_LEFT; }
    public void incrementDx() { resetDy(); dx = Math.min(dx + speed,  maxSpeed); dir = Direction.DIRECTION_RIGHT; }
    public void decrementDy() { resetDx(); dy = Math.max(dy - speed, -maxSpeed); dir = Direction.DIRECTION_UPWARDS; }
    public void incrementDy() { resetDx(); dy = Math.min(dy + speed,  maxSpeed); dir = Direction.DIRECTION_DOWNWARDS; }
    public void resetDx()     { dx = 0; }
    public void resetDy()     { dy = 0; }

    public void rotateClockwise() { setDir(getDir().rotateCW()); resetDx(); resetDy(); }

    public Bullet shoot() {
        if (this instanceof Enemy) {
            shootTick.updateTick();
            if (!shootTick.isTimeOut()) return null;
            shootTick.reset();
            if (this.isFrozen) return null;
        }
        if (!this.isBulletDestroyed) return null;
        this.isBulletDestroyed = false;

        Dimension offset = this.textureFXs.get(dir).getOffsets();
        return new Bullet(this,
                getX() + (int) offset.getWidth(),
                getY() + (int) offset.getHeight(),
                getDir(),
                Globals.BULLET_SPEED_PER_TICK);
    }

    public boolean isSpawnProtectionEnabled() { return spawnProtectionEnabled; }

    public boolean getDamage() {
        armorLevel = armorLevel > 0 ? --armorLevel : 0;
        return true;
    }

    public Blast destroy() {
        this.armorLevel = -1;
        return new Blast(getX(), getY());
    }

    public boolean isDestroyed() { return armorLevel <= 0; }

    /** No-op stub kept for source compatibility with GameLevel. BasicTank has no red variant. */
    public void setAsRed() {}

    /** No-op stub kept for source compatibility with old tank subclasses. */
    public boolean isRedTank() { return false; }

    public void setBulletStatus(boolean v) { isBulletDestroyed = v; }

    // ------------------------------------------------------------------ movement helpers
    public RectangleBound moveForwardHint() {
        int newX = getX(), newY = getY();
        switch (getDir()) {
            case DIRECTION_UPWARDS   -> newY -= speed;
            case DIRECTION_RIGHT     -> newX += speed;
            case DIRECTION_DOWNWARDS -> newY += speed;
            case DIRECTION_LEFT      -> newX -= speed;
            default -> throw new IllegalStateException("Invalid direction: " + getDir());
        }
        return new RectangleBound(newX - getSize().width / 2, newY - getSize().height / 2, getSize());
    }

    public void moveForward() {
        switch (getDir()) {
            case DIRECTION_UPWARDS   -> setY(getY() - speed);
            case DIRECTION_RIGHT     -> setX(getX() + speed);
            case DIRECTION_DOWNWARDS -> setY(getY() + speed);
            case DIRECTION_LEFT      -> setX(getX() - speed);
            default -> throw new IllegalStateException("Invalid direction: " + getDir());
        }
    }

    // ------------------------------------------------------------------ abstract
    protected abstract void setDefaultTankTextureFXs();
    protected abstract void setRedTankTextureFXs();   // kept for compatibility, may be empty

    // ------------------------------------------------------------------ AI movement (Dijkstra)
    private static class Cell implements Comparable<Cell> {
        int r, c, dist, firstMoveRow, firstMoveCol;
        Cell(int r, int c, int dist, int firstMoveRow, int firstMoveCol) {
            this.r = r; this.c = c; this.dist = dist;
            this.firstMoveRow = firstMoveRow; this.firstMoveCol = firstMoveCol;
        }
        public int compareTo(Cell o) { return Integer.compare(dist, o.dist); }
    }

    private Direction getWeightedRandomDirection() {
        double r = Math.random();
        if (r < 0.4) return Direction.DIRECTION_LEFT;
        else if (r < 0.7) return Direction.DIRECTION_RIGHT;
        else return Direction.DIRECTION_UPWARDS;
    }

    private GridLocation findBestMove(GameLevel level, GridLocation start) {
        int[][] costMap = new int[Globals.ROW_TILE_COUNT][Globals.COL_TILE_COUNT];
        Tile[][] map = level.getMap();
        int eagleRow = level.getEagleLocation().rowIndex();
        int eagleCol = level.getEagleLocation().colIndex();

        for (int[] row : costMap) Arrays.fill(row, Integer.MAX_VALUE);
        costMap[start.rowIndex()][start.colIndex()] = 0;

        PriorityQueue<Cell> pq = new PriorityQueue<>();
        pq.offer(new Cell(start.rowIndex(), start.colIndex(), 0, start.rowIndex(), start.colIndex()));

        while (!pq.isEmpty()) {
            Cell cur = pq.poll();
            if (cur.r == eagleRow && cur.c == eagleCol) {
                if (cur.r == start.rowIndex() && cur.c == start.colIndex()) return null;
                return new GridLocation(cur.firstMoveRow, cur.firstMoveCol);
            }
            for (Direction d : Direction.values()) {
                if (d == Direction.DIRECTION_INVALID) continue;
                int dRow = 0, dCol = 0;
                switch (d) {
                    case DIRECTION_UPWARDS   -> dRow = -1;
                    case DIRECTION_RIGHT     -> dCol =  1;
                    case DIRECTION_LEFT      -> dCol = -1;
                    case DIRECTION_DOWNWARDS -> dRow =  1;
                    default -> {}
                }
                int nRow = cur.r + dRow, nCol = cur.c + dCol;
                if (nRow < 0 || nRow >= Globals.ROW_TILE_COUNT || nCol < 0 || nCol >= Globals.COL_TILE_COUNT) continue;
                Tile t = map[nRow][nCol];
                if (t != null && !TileType.isPassable(t.getType())) continue;
                if (level.isTileOccupied(new GridLocation(nRow, nCol), start)) continue;
                int tileCost = (t == null) ? 1 : TileType.getCost(t.getType());
                int newCost = cur.dist + tileCost;
                if (newCost < costMap[nRow][nCol]) {
                    costMap[nRow][nCol] = newCost;
                    int fmr = (cur.r == start.rowIndex() && cur.c == start.colIndex()) ? nRow : cur.firstMoveRow;
                    int fmc = (cur.r == start.rowIndex() && cur.c == start.colIndex()) ? nCol : cur.firstMoveCol;
                    pq.offer(new Cell(nRow, nCol, newCost, fmr, fmc));
                }
            }
        }
        return null;
    }

    private void randomMove(GameLevel level, int depth) {
        if (depth < 0) return;
        setDir(getWeightedRandomDirection());
        if (level.checkMovable(this, moveForwardHint())) {
            moveForward();
            randomMove(level, --depth);
        }
    }

    public synchronized void move(GameLevel level) {
        GridLocation startLoc = Utils.loc2GridLoc(new Location(getX(), getY()));
        Location currentLoc = new Location(getX(), getY());
        GridLocation currentGridLoc = Utils.loc2GridLoc(currentLoc);
        Location gridCenterLoc = Utils.gridLoc2Loc(currentGridLoc);

        GridLocation nextTileLoc = null;
        if (currentLoc.x() == gridCenterLoc.x() && currentLoc.y() == gridCenterLoc.y()) {
            nextTileLoc = findBestMove(level, startLoc);
        } else {
            nextTileLoc = currentGridLoc;
        }

        if (Utils.getRandomProbability(10)) nextTileLoc = null;

        if (nextTileLoc != null) {
            int ddx = nextTileLoc.colIndex() - startLoc.colIndex();
            int ddy = nextTileLoc.rowIndex() - startLoc.rowIndex();
            if      (ddx > 0) setDir(Direction.DIRECTION_RIGHT);
            else if (ddx < 0) setDir(Direction.DIRECTION_LEFT);
            else if (ddy > 0) setDir(Direction.DIRECTION_DOWNWARDS);
            else if (ddy < 0) setDir(Direction.DIRECTION_UPWARDS);

            if (level.checkMovable(this, moveForwardHint())) moveForward();
            else randomMove(level, 0);
        } else {
            randomMove(level, 0);
        }
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        createTextureFXs();
    }

    public synchronized void setX(int x) { this.x = x; }
    public synchronized int  getX()      { return this.x; }
    public synchronized void setY(int y) { this.y = y; }
    public synchronized int  getY()      { return this.y; }
    public synchronized void setDir(Direction d) { this.dir = d; }
    public synchronized Direction getDir()       { return this.dir; }
}
