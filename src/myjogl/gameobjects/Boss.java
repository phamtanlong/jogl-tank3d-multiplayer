/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package myjogl.gameobjects;

import com.sun.opengl.util.texture.Texture;
import javax.media.opengl.GL;
import myjogl.Global;
import myjogl.particles.Debris;
import myjogl.particles.Explo;
import myjogl.particles.Explo1;
import myjogl.particles.ParticalManager;
import myjogl.particles.RoundSparks;
import myjogl.utils.ResourceManager;
import myjogl.utils.Vector3;

/**
 *
 * @author Jundat
 */
public class Boss {

    public static float BOSS_WIDTH = 3;
    public static float BOSS_HEIGHT = 3;
    private Vector3 position;
    public boolean isAlive;
    //
    private Texture tt;
    public boolean isClientBoss;    

    /**
     * Create a boss
     * @param isClientBoss : indicate boss is your, or opponent's
     */
    public Boss(boolean isClientBoss) {
        position = new Vector3();
        isAlive = true;
        this.isClientBoss = isClientBoss;
    }

    public Boss(Vector3 pos, int dir, boolean isClientBoss) {
        position = pos.Clone();
        isAlive = true;
        this.isClientBoss = isClientBoss;
    }

    public void reset(Vector3 pos, int dir, boolean isClientBoss) {
        position = pos.Clone();
        isAlive = true;
        this.isClientBoss = isClientBoss;
    }

    public void load() {
        
        if (isClientBoss) {
            tt = ResourceManager.getInst().getTexture("data/game/boss.png", false, GL.GL_REPEAT);
        } else {
            tt = ResourceManager.getInst().getTexture("data/game/bossAI.png", false, GL.GL_REPEAT);
        }
        

        //
        Vector3 a = position.Clone();
        float scale = 0.1f;
        Explo shootParticle = new Explo(a, 0.1f, scale);
        shootParticle.LoadingTexture();

        Explo1 shootParticle2 = new Explo1(a, 0.1f, scale);
        shootParticle2.LoadingTexture();

        RoundSparks shootParticle3 = new RoundSparks(a, 0.1f, scale);
        shootParticle3.LoadingTexture();

        Debris shootParticle4 = new Debris(a, 0.1f, scale);
        shootParticle4.LoadingTexture();
    }

    public void explode() {
        Vector3 a = position.Clone();
        float scale = 0.1f;
        float time = 0.1f;
        Explo shootParticle = new Explo(a, time, scale);
        shootParticle.LoadingTexture();
        ParticalManager.getInstance().Add(shootParticle);

        Explo1 shootParticle2 = new Explo1(a, time, scale);
        shootParticle2.LoadingTexture();
        ParticalManager.getInstance().Add(shootParticle2);

        RoundSparks shootParticle3 = new RoundSparks(a, time, scale);
        shootParticle3.LoadingTexture();
        ParticalManager.getInstance().Add(shootParticle3);

        Debris shootParticle4 = new Debris(a, time, scale);
        shootParticle4.LoadingTexture();
        ParticalManager.getInstance().Add(shootParticle4);
    }

    public void update(long dt) {
    }

    public void draw() {
        if (this.isAlive) {
            GL gl = Global.drawable.getGL();
            gl.glColor4f(1, 1, 1, 1);
            
            Global.drawCube(tt, position.x, position.y, position.z, BOSS_WIDTH, 2, BOSS_HEIGHT);

//            tt.enable();
//            tt.bind();
//            gl.glBegin(GL.GL_QUADS);
//            {
//                gl.glTexCoord2f(0, 0);
//                gl.glVertex3f(position.x, 2, position.z);
//
//                gl.glTexCoord2f(1, 0);
//                gl.glVertex3f(position.x + BOSS_WIDTH, 2, position.z);
//
//                gl.glTexCoord2f(1, 1);
//                gl.glVertex3f(position.x + BOSS_WIDTH, 2, position.z + BOSS_HEIGHT);
//
//                gl.glTexCoord2f(0, 1);
//                gl.glVertex3f(position.x, 2, position.z + BOSS_HEIGHT);
//            }
//            gl.glEnd();
//            tt.disable();
        }
    }

    //
    // get and set
    //
    public CRectangle getBound() {
        CRectangle rect = new CRectangle();
        rect.x = position.x;
        rect.y = position.z;
        rect.w = BOSS_WIDTH;
        rect.h = BOSS_HEIGHT;

        return rect;
    }
}
