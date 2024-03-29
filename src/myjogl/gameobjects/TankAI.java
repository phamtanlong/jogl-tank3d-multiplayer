/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package myjogl.gameobjects;

import java.util.Random;

import myjogl.Global;
import myjogl.utils.GLModel;
import myjogl.utils.ID;
import myjogl.utils.ModelLoaderOBJ;

/**
 *
 * @author Jundat
 */
public class TankAI extends Tank {

    public final static Random TANK_RANDOM = new Random(System.currentTimeMillis());
    public final static float TANK_TIME_TO_FIRE = 1000; //5 seconds    
    //
    protected int id;
    protected float timeToFire = TANK_TIME_TO_FIRE;
    protected int counterFire; //millisecond to fire
    //
    GLModel modelAI;
    GLModel modelAIFast;
    GLModel modelAISlow;

    public TankAI(int id) {
        super(false);
        counterFire = 0;
        this.id = id;

        switch (id) {
            case ID.TANK_AI:
                hitToDie = TANK_NORMAL_HIT;
                velocity = TANK_VELOCITY_NORMAL;
                break;

            case ID.TANK_AI_FAST:
                hitToDie = TANK_FAST_HIT;
                velocity = TANK_VELOCITY_FAST;
                break;

            case ID.TANK_AI_SLOW:
                hitToDie = TANK_SLOW_HIT;
                velocity = TANK_VELOCITY_SLOW;
                break;
        }

        timeToFire = (TANK_TIME_TO_FIRE * TANK_VELOCITY_NORMAL / velocity);
    }

    public void reset(int id) {
        super.reset(position, direction);
        setAlive(false);
        //
        this.id = id;

        switch (id) {
            case ID.TANK_AI:
                hitToDie = TANK_NORMAL_HIT;
                velocity = TANK_VELOCITY_NORMAL;
                model = modelAI;
                break;

            case ID.TANK_AI_FAST:
                hitToDie = TANK_FAST_HIT;
                velocity = TANK_VELOCITY_FAST;
                model = modelAIFast;
                break;

            case ID.TANK_AI_SLOW:
                hitToDie = TANK_SLOW_HIT;
                velocity = TANK_VELOCITY_SLOW;
                model = modelAISlow;
                break;
        }

        timeToFire = (int) (TANK_TIME_TO_FIRE * TANK_VELOCITY_NORMAL / velocity);
    }

    @Override
    public void load() {
        super.load();

        modelAI = ModelLoaderOBJ.LoadModel("data/model/tank.obj",
                "data/model/tank.mtl", "data/model/tankAI.png", Global.drawable);
        modelAIFast = ModelLoaderOBJ.LoadModel("data/model/tank.obj",
                "data/model/tank.mtl", "data/model/tankAIFast.png", Global.drawable);
        modelAISlow = ModelLoaderOBJ.LoadModel("data/model/tank.obj",
                "data/model/tank.mtl", "data/model/tankAISlow.png", Global.drawable);

        switch (id) {
            case ID.TANK_AI:
                model = modelAI;
                break;

            case ID.TANK_AI_FAST:
                model = modelAIFast;
                break;

            case ID.TANK_AI_SLOW:
                model = modelAISlow;
                break;
        }
    }

    public void randomNewDirection() {
        this.setDirection(TANK_RANDOM.nextInt(CDirections.NUMBER_DIRECTION));
    }

    @Override
    public void update(long dt) {
        super.update(dt);

        if (this.isAlive()) {
            //
            //fire
            counterFire += dt;
            if (counterFire >= timeToFire) {
                counterFire = 0;
                super.fire();
            }

            //change direction
            boolean canMove = super.move(getDirection(), dt);

            //chon huong moi
            if (canMove != true) {
                this.randomNewDirection();
            }
        }
    }
}
