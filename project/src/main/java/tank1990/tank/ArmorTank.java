
package tank1990.tank;

import tank1990.core.*;

import java.util.Random;

/**
 * @class ArmorTank
 * @brief Represents an armored tank in the game.
 * @details This tank has a high armor level and is designed to be more resilient against attacks.
 */
public class ArmorTank extends AbstractTank implements Enemy {

    int lastArmorLevel=0;

    public ArmorTank(int x, int y) {
        super(x, y);

        setSpeedUnit(Globals.ARMOR_TANK_MOVEMENT_SPEED);
        setMaxSpeedUnit(Globals.ARMOR_TANK_MOVEMENT_MAX_SPEED);

        setArmorLevel(4);  // Start with level 4 armor
        this.lastArmorLevel = getArmorLevel();

        setDefaultTankTextureFXs();
        // Decrease the movement speed for ArmorTank since movement speed is set as low as possible, but it is not enough.
        this.movementTick.setDefaultTick(Utils.Time2GameTick(200));
    }

    public ArmorTank(int x, int y, Direction dir) {
        super(x, y, dir);

        setSpeedUnit(Globals.ARMOR_TANK_MOVEMENT_SPEED);
        setMaxSpeedUnit(Globals.ARMOR_TANK_MOVEMENT_MAX_SPEED);

        setArmorLevel(4);  // Start with level 4 armor
        this.lastArmorLevel = getArmorLevel();

        setDefaultTankTextureFXs();
    }

    @Override
    public void update(GameLevel level) {
        super.update(level);

        // Update the tank texture based on the armor level only if it is not a red tank.
        // Since red tank texture animation will be taken care of in super class.
        if (!isRedTank()) {
            int armorLevel = getArmorLevel();

            if (this.lastArmorLevel!=armorLevel) {
                setDefaultTankTextureFXs();
            }
        }
    }

    @Override
    protected void setDefaultTankTextureFXs() {
        int armorLevel = getArmorLevel();

        if (armorLevel==4) {
            this.tankTextureFxStruct = Globals.TEXTURE_ARMOR_TANK_L4_STRUCT;
            createTextureFXs();
            this.lastArmorLevel = armorLevel;
        } else if (armorLevel==3) {
            this.tankTextureFxStruct = Globals.TEXTURE_ARMOR_TANK_L3_STRUCT;
            createTextureFXs();
            this.lastArmorLevel = armorLevel;
        } else if (armorLevel==2) {
            this.tankTextureFxStruct = Globals.TEXTURE_ARMOR_TANK_L2_STRUCT;
            createTextureFXs();
            this.lastArmorLevel = armorLevel;
        } else if (armorLevel==1) {
            this.tankTextureFxStruct = Globals.TEXTURE_ARMOR_TANK_L1_STRUCT;
            createTextureFXs();
            this.lastArmorLevel = armorLevel;
        }
    }

    @Override
    protected void setRedTankTextureFXs() {
        int armorLevel = getArmorLevel();

        if (armorLevel==4) {
            this.tankTextureFxStruct = Globals.TEXTURE_ARMOR_TANK_RED_STRUCT;
            createTextureFXs();
            this.lastArmorLevel = armorLevel;
        } else if (armorLevel==3) {
            this.tankTextureFxStruct = Globals.TEXTURE_ARMOR_TANK_RED_STRUCT;
            createTextureFXs();
            this.lastArmorLevel = armorLevel;
        } else if (armorLevel==2) {
            this.tankTextureFxStruct = Globals.TEXTURE_ARMOR_TANK_RED_STRUCT;
            createTextureFXs();
            this.lastArmorLevel = armorLevel;
        } else if (armorLevel==1) {
            this.tankTextureFxStruct = Globals.TEXTURE_ARMOR_TANK_RED_STRUCT;
            createTextureFXs();
            this.lastArmorLevel = armorLevel;
        }
    }
}
