package tank1990.core;

import java.io.Serializable;

/**
 * @class GameScoreStruct
 * @brief Holds scoring data for a game session (simplified to one enemy type).
 */
public class GameScoreStruct implements Serializable {
    private int hiScore = 0;
    private int totalScore = 0;
    private int playerRemainingLives = 0;
    private int remainingTankCount = 0;
    private int reachedLevel = 0;
    private int enemyTankCount = 0;
    private int enemyTankScore = 0;

    public GameScoreStruct() {}

    public int getHiScore()               { return hiScore; }
    public int getTotalScore()            { return totalScore; }
    public int getPlayerRemainingLives()  { return playerRemainingLives; }
    public int getRemainingTankCount()    { return remainingTankCount; }
    public int getReachedLevel()          { return reachedLevel; }
    public int getEnemyTankCount()        { return enemyTankCount; }
    public int getEnemyTankScore()        { return enemyTankScore; }

    // Keep old getters as aliases so existing panel code compiles without changes
    public int getBasicTankCount()  { return enemyTankCount; }
    public int getBasicTankScore()  { return enemyTankScore; }
    public int getFastTankCount()   { return 0; }
    public int getFastTankScore()   { return 0; }
    public int getPowerTankCount()  { return 0; }
    public int getPowerTankScore()  { return 0; }
    public int getArmorTankCount()  { return 0; }
    public int getArmorTankScore()  { return 0; }

    public void setHiScore(int v)               { hiScore = v; }
    public void setTotalScore(int v)            { totalScore = v; }
    public void setPlayerRemainingLives(int v)  { playerRemainingLives = v; }
    public void setRemainingTankCount(int v)    { remainingTankCount = v; }
    public void setReachedLevel(int v)          { reachedLevel = v; }
    public void setEnemyTankCount(int v)        { enemyTankCount = v; }
    public void setEnemyTankScore(int v)        { enemyTankScore = v; }

    // Keep old setters as aliases
    public void setBasicTankCount(int v) { enemyTankCount = v; }
    public void setBasicTankScore(int v) { enemyTankScore = v; }
    public void setFastTankCount(int v)  {}
    public void setFastTankScore(int v)  {}
    public void setPowerTankCount(int v) {}
    public void setPowerTankScore(int v) {}
    public void setArmorTankCount(int v) {}
    public void setArmorTankScore(int v) {}
}
