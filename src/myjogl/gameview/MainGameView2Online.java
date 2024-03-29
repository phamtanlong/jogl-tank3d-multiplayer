/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package myjogl.gameview;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

import myjogl.GameEngine;
import myjogl.Global;
import myjogl.KeyboardState;
import myjogl.gameobjects.Boss;
import myjogl.gameobjects.CDirections;
import myjogl.gameobjects.CRectangle;
import myjogl.gameobjects.Tank;
import myjogl.gameobjects.TankBullet;
import myjogl.particles.Explo;
import myjogl.particles.ParticalManager;
import myjogl.utils.Camera;
import myjogl.utils.CameraFo;
import myjogl.utils.ResourceManager;
import myjogl.utils.Sound;
import myjogl.utils.TankMap;
import myjogl.utils.Vector3;
import myjogl.utils.Writer;
import tank3dclient.IMessageHandler;
import tank3dclient.Tank3DMessage;
import tank3dclient.Tank3DMessageListener;

/**
 *
 * @author Jundat
 */
public class MainGameView2Online extends MainGameView2Offline implements IMessageHandler {
	
	private Tank3DMessageListener m_listener;
	
	private boolean isLoaded = false;
	public long m_dt = 0;

    public MainGameView2Online() {
        super();
    }

    //---------------------------------
    
	@Override
	public void onConnected() {
		// TODO Auto-generated method stub
		System.out.println("Connected in MainGameView");
	}
	
	@Override
	public void onReceiveMessage(Tank3DMessage message) {
		if(message.ClientId != Global.clientId && message.OpponentClientId == Global.clientId) {
			switch(message.Cmd) {
			case Tank3DMessage.CMD_FIRE:
				receiveOpponentTankFire();
				break;
				
			case Tank3DMessage.CMD_MOVE:
				receiveOpponentTankMove(message.Position, message.Direction);
				break;
				
			case Tank3DMessage.CMD_QUIT:
				receiveOpponentTankQuit();
				break;
				
			case Tank3DMessage.CMD_RESTART:
				receiveOpponentTankRestart();
				break;
			}
		}
	}
	
	//---------------------------------
	
	private void receiveOpponentTankFire() {
    	if (isLoaded == false) return;
    	
        if (opponentTank.isAlive()) {
            if (opponentTank.fire()) {
                GameEngine.sFire.clone().setVolume(6.0f);
                GameEngine.sFire.clone().play();
            }
        }
    }
    
    private void receiveOpponentTankMove(Vector3 position, int direction) {
    	if (isLoaded == false) return;
    	
    	opponentTank.setPosition(position);
    	opponentTank.setDirection(direction);
    }
    
    private void receiveOpponentTankQuit() {
    	if(isPause == true) {
			GameEngine.getInstance().detachTopDialog();
			isPause = false;
		}
		LostGameView dialog = new LostGameView(this);
		GameEngine.getInstance().attach(dialog);
    }
    
    private void receiveOpponentTankRestart() {
    	if(isPause == true) {
			GameEngine.getInstance().detachTopDialog();
			isPause = false;
		}
		GameEngine.getInstance().detachTopDialog();
    	this.loadLevel(Global.level);
    }
    
    //---------------------------------
    
    private void sendPlayerFire() {
    	Tank3DMessage newmessage = new Tank3DMessage();
		newmessage.ClientId = Global.clientId;
		newmessage.OpponentClientId = Global.opponentClientId;
		newmessage.Cmd = Tank3DMessage.CMD_FIRE;
		this.m_listener.sendMessage(newmessage);
    }
    
    private void sendPlayerMove(Vector3 position, int direction) {
    	Tank3DMessage newmessage = new Tank3DMessage();
		newmessage.ClientId = Global.clientId;
		newmessage.OpponentClientId = Global.opponentClientId;
		newmessage.Cmd = Tank3DMessage.CMD_MOVE;
		newmessage.Position = position;
		newmessage.Direction = direction;
		this.m_listener.sendMessage(newmessage);
    }
    
    private void sendPlayerQuit() {
    	Tank3DMessage newmessage = new Tank3DMessage();
		newmessage.ClientId = Global.clientId;
		newmessage.OpponentClientId = Global.opponentClientId;
		newmessage.Cmd = Tank3DMessage.CMD_QUIT;
		this.m_listener.sendMessage(newmessage);
    }
    
    private void sendPlayerRestart() {
    	Tank3DMessage newmessage = new Tank3DMessage();
		newmessage.ClientId = Global.clientId;
		newmessage.OpponentClientId = Global.opponentClientId;
		newmessage.Cmd = Tank3DMessage.CMD_RESTART;
		this.m_listener.sendMessage(newmessage);
    }
    
    //---------------------------------
    
    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (isPause) {
            return;
        }

        //common
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE && bSliding == false) {
            GameEngine.sClick.play();
            this.isPause = true;
            GameEngine.getInstance().attach(new PauseView(this));
        }

        //playerTank
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (playerTank.isAlive()) {
                if (playerTank.fire()) {
                	
                	sendPlayerFire();
                	
                    GameEngine.sFire.clone().setVolume(6.0f);
                    GameEngine.sFire.clone().play();
                }
            }
        }
        
        //opponentTank
        //if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
        //    opponentTankFire();
        //}
    }

    @Override
    public void pointerPressed(MouseEvent e) {
    }

    @Override
    public void pointerMoved(MouseEvent e) {
    }

    @Override
    public void pointerReleased(MouseEvent e) {
    }

    private void handleInput(long dt) {
        if (isPause) {
            return;
        }
        
        KeyboardState state = KeyboardState.getState();
        
        //up
        if (state.isDown(KeyEvent.VK_UP)) {
            if (playerTank.isAlive()) {
            	
                playerTank.move(CDirections.UP, dt);
                if (this.checkTankCollision(playerTank)) {
                    this.playerTank.rollBack();
                }
                
                sendPlayerMove(playerTank.getPosition(), playerTank.getDirection());
            }
        } //down
        if (state.isDown(KeyEvent.VK_DOWN)) {
            if (playerTank.isAlive()) {
            	
                playerTank.move(CDirections.DOWN, dt);
                if (this.checkTankCollision(playerTank)) {
                    this.playerTank.rollBack();
                }
                
                sendPlayerMove(playerTank.getPosition(), playerTank.getDirection());
            }
        }  //left
        if (state.isDown(KeyEvent.VK_LEFT)) {
            if (playerTank.isAlive()) {
            	
                playerTank.move(CDirections.LEFT, dt);
                if (this.checkTankCollision(playerTank)) {
                    this.playerTank.rollBack();
                }
                
                sendPlayerMove(playerTank.getPosition(), playerTank.getDirection());
            }
        }  //right
        if (state.isDown(KeyEvent.VK_RIGHT)) {
            if (playerTank.isAlive()) {
            	
                playerTank.move(CDirections.RIGHT, dt);
                if (this.checkTankCollision(playerTank)) {
                    this.playerTank.rollBack();
                }
                
                sendPlayerMove(playerTank.getPosition(), playerTank.getDirection());
            }
        }
    }

    @Override
    public void restart() {
    	this.isPause = false;
    	this.loadLevel(Global.level);
    	
    	sendPlayerRestart();
    }
    
    @Override
    public void loadLevel(int level) {
        Global.level = level;

        try {
            //init map
            TankMap.getInst().LoadMap(level);

            if(Global.isHost == false) {
            	TankMap.getInst().SwapTank();
            	
            	//life label
            	Point temp = new Point(pPlayerLife);
            	pPlayerLife = new Point(pOpponentLife);
            	pOpponentLife = temp;            	
            }

        	//playerBoss
            this.playerBossPosition = TankMap.getInst().bossPosition.Clone();
            this.playerBoss.reset(playerBossPosition, CDirections.UP, playerBoss.isClientBoss);

            //opponentBoss
            this.opponentBossPosition = TankMap.getInst().bossAiPosition.Clone();
            this.opponentBoss.reset(opponentBossPosition, CDirections.UP, opponentBoss.isClientBoss);
            
            //playerTank
            Vector3 v = ((Vector3) TankMap.getInst().listTankPosition.get(0)).Clone();
            playerTank.reset(v.Clone(), CDirections.RIGHT);
            
            //opponentTank
            v = ((Vector3) TankMap.getInst().listTankAiPosition.get(0)).Clone();
            opponentTank.reset(v.Clone(), CDirections.LEFT);
     
            sendPlayerMove(playerTank.getPosition(), playerTank.getDirection());
     
            playerLife = NUMBER_OF_LIEF;
            opponentLife = NUMBER_OF_LIEF;
            ParticalManager.getInstance().Clear();
            sBackground.setVolume(Sound.MAX_VOLUME);
        } catch (Exception e) {
        }
        
        float mapW = TankMap.getInst().width;
        float mapH = TankMap.getInst().height;
        camera.Position_Camera(
                mapW/2, 28.869976f, mapH + 0.8f, 
                mapW/2, 27.494007f, mapH + 0.0f, 
                0.0f, 1.0f, 0.0f
        );
        
        cameraFo.SetPosition(
                mapW/2, 0, mapW/2, 
                Math.toRadians(45), Math.toRadians(0), 5, 
                0, 1, 0
        );
        
        isLoaded = true;
    }

    @Override
    public void load() {
    	
    	//network 
    	
    	this.m_listener = Tank3DMessageListener.getInstance();
    	this.m_listener.setMessageHandler(this);
    	
    	//---------------
    	
    	pOpponentLife.x += 50;
    	
    	//---------------
    	
        isPause = false;
        bSliding = true;
        deltaBeta = DELTA_BETA;
        deltaR = DELTA_R;
        delayTime = 0;

        camera = new Camera(); //init position when load map
        cameraFo = new CameraFo(1, 1, 1, Math.toRadians(45), Math.toRadians(0), 5, 0, 1, 0);
        
        //skybox
        //m_skybox = new SkyBox();
        //m_skybox.Initialize(5.0f);
        //m_skybox.LoadTextures(
        //        "data/skybox/top.jpg", "data/skybox/bottom.jpg",
        //        "data/skybox/front.jpg", "data/skybox/back.jpg",
        //        "data/skybox/left.jpg", "data/skybox/right.jpg");

        //writer
        writer = new Writer("data/font/Motorwerk_80.fnt");
        //sound
        sBackground = ResourceManager.getInst().getSound("sound/bg_game.wav", true);
        sBackground.stop();
        sBackground.play();


        //==============================
        
    	//playerBoss
        playerBoss = new Boss(Global.isHost);
        playerBoss.load();
        
        //playerTank
        playerTank = new Tank(Global.isHost);
        playerTank.load();
        
        
        //opponentBoss
        opponentBoss = new Boss(! Global.isHost);
        opponentBoss.load();
        
        //opponentTank
        opponentTank = new Tank(! Global.isHost);
        opponentTank.load();
        
        //==============================
        

        //init map
        this.loadLevel(Global.level); //start at Global.level 0
    }

    @Override
    public void unload() {
    	sendPlayerQuit();
		
    	this.m_listener.setMessageHandler(null);
        GameEngine.getInstance().tank3d.frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
    
    @Override
    public void checkPlayerLose() {
        if (playerLife <= 0) { //player lose
            GameEngine.sFire.clone().play();
            GameEngine.getInstance().attach(new GameOverView(this, 0, true));
        } else { // reset new life
            for (Object o : TankMap.getInst().listTankPosition) {
                Vector3 v = (Vector3) o;
                playerTank.reset(v, CDirections.RIGHT);
                boolean isOK = true;
                
                //check valid position
                if (opponentTank.isAlive()) {
                    if (opponentTank.getBound().isIntersect(playerTank.getBound())) {
                        isOK = false;
                    }
                }

                if (isOK == true) {
                    playerLife--;
                    particleNewTank(v);
                    break;
                }
            }
        }
    }
    
    @Override
    public void checkOpponentLose() {
        if (opponentLife <= 0) { //opponent lose
            GameEngine.sFire.clone().play();
            GameEngine.getInstance().attach(new GameOverView(this, 1, true));
        } else { // reset new life

            for (Object o : TankMap.getInst().listTankAiPosition) {
                Vector3 v = (Vector3) o;

                opponentTank.reset(v, CDirections.LEFT);

                boolean isOK = true;
                
                //check valid position
                if (playerTank.isAlive()) {
                    if (playerTank.getBound().isIntersect( opponentTank.getBound())) {
                        isOK = false;
                    }
                }

                if (isOK == true) {
                    opponentLife--;
                    particleNewTank(v);
                    break;
                }
            }
        }
    }

    @Override
    public boolean checkTankCollision(Tank tank) {
        CRectangle rectTank = tank.getBound();
        if (tank.isAlive() == false) {
            return false;
        }

        //playerBoss
        if (playerBoss.isAlive && rectTank.isIntersect(playerBoss.getBound())) {
            return true;
        }
        
        //opponentBoss
        if (opponentBoss.isAlive && rectTank.isIntersect(opponentBoss.getBound())) {
            return true;
        }

        //playerTank
        if (tank == playerTank) { //check player vs opponentTank
            if (opponentTank.isAlive()) {
                if (rectTank.isIntersect(opponentTank.getBound())) {
                    return true;
                }
            }
        } else if (tank == opponentTank) { //opponentTank
            //vs playerTank
            if (playerTank.isAlive()) {
                if (rectTank.isIntersect(playerTank.getBound())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void checkBulletCollision() {
        //player's Bullets
        for (int i = 0; i < Tank.TANK_NUMBER_BULLETS; i++) {
            TankBullet bullet = playerTank.bullets[i];

            if (bullet.isAlive) {
                //vs playerBoss
                if (bullet.isAlive && playerBoss.isAlive) {
                    if (bullet.getBound().isIntersect(playerBoss.getBound())) {
                        playerBoss.explode();
                        bullet.isAlive = false;
                        playerBoss.isAlive = false;
                        this.isPause = true;
                        GameEngine.sFire.clone().play();
                        GameEngine.getInstance().attach(new GameOverView(this, 0, true));
                        return;
                    }
                }
                
                //vs opponentBoss
                if (bullet.isAlive && opponentBoss.isAlive) {
                    if (bullet.getBound().isIntersect(opponentBoss.getBound())) {
                        opponentBoss.explode();
                        bullet.isAlive = false;
                        opponentBoss.isAlive = false;
                        this.isPause = true;
                        GameEngine.sFire.clone().play();
                        GameEngine.getInstance().attach(new GameOverView(this, 1, true));
                        return;
                    }
                }

                if (opponentTank.isAlive() && bullet.isAlive) {
                    if (opponentTank.getBound().isIntersect(bullet.getBound())) {
                        bullet.isAlive = false;
                        if (opponentTank.hit()) {
                            opponentTank.explode();
                            Global.playerScore += SCORE_DELTA;
                            this.checkOpponentLose();
                        } else {
                            bullet.explode();
                        }

                        continue; //bullet is dead, next to another bullet
                    }
                }

                //vs opponent's bullets
                for (int k = 0; k < Tank.TANK_NUMBER_BULLETS; k++) {
                    TankBullet aiBullet = opponentTank.bullets[k];
                    if (aiBullet.isAlive && bullet.isAlive) {
                        if (aiBullet.getBound().isIntersect(bullet.getBound())) {
                            aiBullet.isAlive = false;
                            bullet.isAlive = false;
                            bullet.explode();
                            aiBullet.explode();
                            break;
                        }
                    }
                }
            }
        }
        
        ////////////////////////////////////////////////////////////////////////

        //opponent's Bullets
        for (int i = 0; i < Tank.TANK_NUMBER_BULLETS; i++) {
            TankBullet bullet = opponentTank.bullets[i];

            if (bullet.isAlive) {
                if (bullet.isAlive && playerBoss.isAlive) {
                    if (bullet.getBound().isIntersect(playerBoss.getBound())) {
                        playerBoss.explode();
                        bullet.isAlive = false;
                        playerBoss.isAlive = false;
                        this.isPause = true;
                        GameEngine.sFire.clone().play();
                        GameEngine.getInstance().attach(new GameOverView(this, 0, true));
                        return;
                    }
                }
                
                //vs opponentBoss
                if (bullet.isAlive && opponentBoss.isAlive) {
                    if (bullet.getBound().isIntersect(opponentBoss.getBound())) {
                        opponentBoss.explode();
                        bullet.isAlive = false;
                        opponentBoss.isAlive = false;
                        this.isPause = true;
                        GameEngine.sFire.clone().play();
                        GameEngine.getInstance().attach(new GameOverView(this, 1, true));
                        return;
                    }
                }

                //vs playerTank
                if (playerTank.isAlive() && bullet.isAlive) {
                    if (playerTank.getBound().isIntersect(bullet.getBound())) {
                        bullet.isAlive = false;
                        if (playerTank.hit()) {
                            playerTank.explode();
                            Global.opponentScore += SCORE_DELTA;
                            this.checkPlayerLose();
                        } else {
                            bullet.explode();
                        }

                        continue; //bullet is dead, next to another bullet
                    }
                }

                //vs player's bullets
                for (int k = 0; k < Tank.TANK_NUMBER_BULLETS; k++) {
                    TankBullet aiBullet = playerTank.bullets[k];
                    if (aiBullet.isAlive && bullet.isAlive) {
                        if (aiBullet.getBound().isIntersect(bullet.getBound())) {
                            aiBullet.isAlive = false;
                            bullet.isAlive = false;
                            bullet.explode();
                            aiBullet.explode();
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void particleStartGame() {
        float mapW = TankMap.getInst().width;
        float mapH = TankMap.getInst().height;
        
        float d = 5;
        
        Vector3 a = new Vector3(d, 0, d);
        float scale = 0.4f;
        float time = 0.02f;
        Explo shootParticle = new Explo(a.Clone(), time, scale);
        shootParticle.LoadingTexture();
        ParticalManager.getInstance().Add(shootParticle);

        a = new Vector3(mapW - d, 0, d);
        Explo shootParticle1 = new Explo(a.Clone(), time, scale);
        shootParticle1.LoadingTexture();
        ParticalManager.getInstance().Add(shootParticle1);
        
        a = new Vector3(mapW - d, 0, mapH - d);
        Explo shootParticle2 = new Explo(a.Clone(), time, scale);
        shootParticle2.LoadingTexture();
        ParticalManager.getInstance().Add(shootParticle2);
        
        a = new Vector3(d, 0, mapH - d);
        Explo shootParticle3 = new Explo(a.Clone(), time, scale);
        shootParticle3.LoadingTexture();
        ParticalManager.getInstance().Add(shootParticle3);
    }

    @Override
    public void particleNewTank(Vector3 position) {
        float scale = 0.4f;
        float time = 0.01f;
        Explo shootParticle = new Explo(position.Clone(), time, scale);
        shootParticle.LoadingTexture();
        ParticalManager.getInstance().Add(shootParticle);
    }

    @Override
    public void update(long dt) {
    	m_dt = dt;
    	
        cameraFo.Update();

        if (bSliding) {
            cameraFo.beta -= deltaBeta;
            cameraFo.r += deltaR;
            if (cameraFo.r > 15.0f) {
                deltaBeta -= 0.0005;
            }
            if (deltaBeta < 0) {
                deltaBeta = 0;
                deltaR = 0;
                delayTime++;
                if (delayTime > DELAY_TIME) {
                    bSliding = false;
                    particleStartGame();
                }
            }
        } else {
            handleInput(dt);
            //opponentHandleInput(dt);
            
            playerTank.update(dt);
            opponentTank.update(dt);

            this.checkBulletCollision();
        }
        
        ParticalManager.getInstance().Update();
    }

    @Override
    public void display() {
        GL gl = Global.drawable.getGL();
        GLU glu = new GLU();
        gl.glLoadIdentity();
        gl.glDisable(GL.GL_LIGHTING);
        gl.glDisable(GL.GL_BLEND);
        gl.glDisable(GL.GL_MULTISAMPLE);

        if (bSliding) {
            glu.gluLookAt(cameraFo.x, cameraFo.y, cameraFo.z, cameraFo.lookAtX, cameraFo.lookAtY, cameraFo.lookAtZ, cameraFo.upX, cameraFo.upY, cameraFo.upZ);
        } else {
            glu.gluLookAt(
                camera.mPos.x, camera.mPos.y, camera.mPos.z,
                camera.mView.x, camera.mView.y, camera.mView.z,
                camera.mUp.x, camera.mUp.y, camera.mUp.z
        		);
        }

        //m_skybox.Render(camera.mPos.x, camera.mPos.y, camera.mPos.z);

        TankMap.getInst().Render();

        playerTank.draw();
        opponentTank.draw();

        playerBoss.draw();
        opponentBoss.draw();

        ParticalManager.getInstance().Draw(gl, camera);

        float scale = 0.7f;
        playerLife = (playerLife < 0) ? 0 : playerLife;
        opponentLife = (opponentLife < 0) ? 0 : opponentLife;
        writer.Render("LIFE " + playerLife, pPlayerLife.x, pPlayerLife.y, scale, scale, 1, 1, 1);
        writer.Render("LIFE " + opponentLife, pOpponentLife.x, pOpponentLife.y, scale, scale, 1, 1, 1);
    }

}
