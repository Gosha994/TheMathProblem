package com.calculator.app.ui.game;
import com.calculator.app.core.GlobalScore;

import android.content.Context;
import android.util.Log;
import android.graphics.*;
import android.view.*;
import java.util.*;

public class GameSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    public static final int GAME_PLATFORMER = 99;
    public static final int GAME_TETRIS     = 111;

    private final int gameId;
    private GameThread thread;
    private GameState state;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float swipeStartX, swipeStartY;

    public GameSurfaceView(Context ctx, int gameId) {
        super(ctx);
        this.gameId = gameId;
        getHolder().addCallback(this);
        setFocusable(true);
    }

    @Override public void surfaceCreated(SurfaceHolder h) {
        state = gameId == GAME_TETRIS ? new TetrisState() : new PlatformerState();
        thread = new GameThread(h);
        thread.running = true;
        thread.start();
    }
    @Override public void surfaceChanged(SurfaceHolder h, int f, int w, int ht) { if (state!=null) state.resize(w,ht); }
    @Override public void surfaceDestroyed(SurfaceHolder h) { if (thread!=null){thread.running=false;try{thread.join(500);}catch(Exception ignored){}} }

    class GameThread extends Thread {
        final SurfaceHolder holder; volatile boolean running;
        GameThread(SurfaceHolder h){this.holder=h;}
        @Override public void run() {
            long last=System.nanoTime();
            while(running){
                long now=System.nanoTime(); float dt=Math.min((now-last)/1e9f,0.05f); last=now;
                if(state!=null)state.update(dt);
                Canvas c=null;
                try{c=holder.lockCanvas();if(c!=null&&state!=null)state.draw(c,paint);}
                finally{if(c!=null)holder.unlockCanvasAndPost(c);}
                try{Thread.sleep(16);}catch(Exception ignored){}
            }
        }
    }

    @Override public boolean onTouchEvent(MotionEvent e) {
        if(state==null)return true;
        int a=e.getAction();
        if(a==MotionEvent.ACTION_DOWN){swipeStartX=e.getX();swipeStartY=e.getY();state.onDown(e.getX(),e.getY());}
        else if(a==MotionEvent.ACTION_MOVE)state.onMove(e.getX(),e.getY());
        else if(a==MotionEvent.ACTION_UP)state.onUp(e.getX(),e.getY(),e.getX()-swipeStartX,e.getY()-swipeStartY);
        return true;
    }

    
    abstract static class GameState {
        int W,H; boolean inited=false;
        void resize(int w,int h){W=w;H=h;if(!inited){inited=true;init();}}
        abstract void init();
        abstract void update(float dt);
        abstract void draw(Canvas c,Paint p);
        void onDown(float x,float y){}
        void onMove(float x,float y){}
        void onUp(float x,float y,float dx,float dy){}
        void overlay(Canvas c,Paint p,String t1,String t2,int col2){
            p.setColor(0xBB000000);c.drawRect(0,H/2f-100,W,H/2f+100,p);
            p.setColor(Color.WHITE);p.setTextSize(46);p.setTextAlign(Paint.Align.CENTER);
            c.drawText(t1,W/2f,H/2f-10,p);
            p.setColor(col2);p.setTextSize(24);c.drawText(t2,W/2f,H/2f+48,p);
            p.setTextAlign(Paint.Align.LEFT);
        }
    }

    
    
    
    static class PlatformerState extends GameState {

        
        
        static final float ORIG_W=2000f, ORIG_H=600f;
        float scaleX, scaleY, groundY, levelW;

        
        
        
        
        static final float GRAVITY_A  = 2880f; 
        static final float JUMP_VEL_A = 900f;  
        static final float MOVE_SPD_A = 300f;  

        
        float px, py, pvx, pvy;
        boolean onGround, leftHeld, rightHeld, started, gameOver, win;
        float camX, gameTime, legAnim;
        int lives = 3;

        
        float[][] platforms;
        
        float[][] movPlats;

        
        List<float[]> coins = new ArrayList<>();
        
        List<float[]> obstacles = new ArrayList<>();
        
        List<float[]> enemies = new ArrayList<>();

        int score = 0, greenCoins = 0, redCoins = 0, totalCoins = 0;
        static final int SCORE_TARGET = 300;

        
        
        float sx(float ax) { return ax * scaleX; }
        float sy(float ay) { return H - ay * scaleY; }
        
        float ax(float sx) { return sx / scaleX; }
        float ay(float sy) { return (H - sy) / scaleY; }

        @Override void init() {
            scaleX = (float)W / ORIG_W;
            scaleY = (float)H / ORIG_H;
            groundY = 64f; 
            levelW  = ORIG_W;
            buildLevel();
        }

        void buildLevel() {
            px = 100; py = 200; pvx = 0; pvy = 0;
            camX = 0; onGround = false; leftHeld = false; rightHeld = false;
            gameOver = false; win = false; lives = 3; gameTime = 0;
            score = 0; greenCoins = 0; redCoins = 0;
            coins.clear(); obstacles.clear(); enemies.clear();

            
            
            float[][] allPlats = {
                {300,122},{500,160},{700,195},{900,145},
                {1100,175},{1300,137},{1500,192},{1700,100},
                {350,104},{850,119},{1350,106}
            };
            List<float[]> pl = new ArrayList<>();
            for (float[] b : allPlats) {
                pl.add(new float[]{b[0]-32, b[1]-32, 64, 48}); 
            }
            platforms = pl.toArray(new float[0][]);

            
            
            float[][] mpDef = {{450,135,1.5f,70},{750,150,-2,80},{1050,138,1.8f,60},{1250,120,1f,50}};
            movPlats = new float[mpDef.length][8];
            for (int i = 0; i < mpDef.length; i++) {
                float mx = mpDef[i][0]-32, my = mpDef[i][1]-12;
                float mw = 64, mh = 24; 
                float spd = mpDef[i][2] * 60f;
                float rng = mpDef[i][3];
                float origX = mpDef[i][0]-32;
                movPlats[i] = new float[]{mx,my,mw,mh, spd, origX-rng, origX+rng, mx};
            }

            
            Random rnd = new Random(42);

            
            for (float[] p2 : platforms) {
                float cx = p2[0] + p2[2] / 2f;
                float cy = p2[1] + p2[3] + 22f; 
                if (rnd.nextFloat() < 0.7f) {
                    boolean green = rnd.nextFloat() < 0.6f;
                    int val = rnd.nextInt(20) + 1; 
                    coins.add(new float[]{cx, cy, green ? 1 : -1, val});
                }
            }

            
            
            float[] safeX = {150, 420, 620, 800, 980, 1160, 1430, 1600, 1780, 1900};
            float[] enemyX = {500, 1200}; 
            for (float cx : safeX) {
                
                boolean blocked = false;
                for (float[] p2 : platforms) {
                    if (cx + 15 > p2[0] && cx - 15 < p2[0] + p2[2]) { blocked = true; break; }
                }
                for (float ex : enemyX) {
                    if (Math.abs(cx - ex) < 60) { blocked = true; break; }
                }
                if (!blocked) {
                    float cy = groundY + 55 + rnd.nextInt(60); 
                    boolean green = rnd.nextFloat() < 0.55f;
                    int val = rnd.nextInt(20) + 1; 
                    coins.add(new float[]{cx, cy, green ? 1 : -1, val});
                }
            }
            totalCoins = coins.size();

            
            
            float[] bombX = {520,820,1020,1170,1420,1820};
            
            
            for (float bx : bombX)
                obstacles.add(new float[]{bx-18, groundY, 37, 37, 0,0,0,0});

            
            float[][] sawPos = {{500,groundY+50},{900,groundY+50},{1300,groundY+50},{1700,groundY+50}};
            for (float[] s : sawPos) {
                float sdx = (rnd.nextFloat() > 0.5f ? 1 : -1) * 55f;
                obstacles.add(new float[]{s[0]-18, s[1]-18, 36, 36, 1, sdx, s[0]-60, s[0]+60});
            }

            
            enemies.add(new float[]{500, groundY+20, 80f, 400, 600});
            enemies.add(new float[]{1200, groundY+20, -90f, 1100, 1300});

            started = false;
        }

        @Override void update(float dt) {
            if (!started || gameOver || win) return;
            gameTime += dt;
            legAnim += dt * 7;

            
            pvy -= GRAVITY_A * dt;

            
            if (leftHeld && !rightHeld)       pvx = -MOVE_SPD_A;
            else if (rightHeld && !leftHeld)  pvx =  MOVE_SPD_A;
            else                              pvx = 0;

            
            final float HW = 18f; 
            final float HH = 30f; 

            
            px += pvx * dt;
            
            int riderIdx = -1;
            for (int i = 0; i < movPlats.length; i++) {
                float[] mp = movPlats[i];
                mp[7] += mp[4] * dt;
                if (mp[7] < mp[5]) { mp[7] = mp[5]; mp[4] = -mp[4]; }
                if (mp[7] > mp[6]) { mp[7] = mp[6]; mp[4] = -mp[4]; }
                mp[0] = mp[7];
            }

            
            for (float[] pl : platforms) {
                float pL=pl[0], pR=pl[0]+pl[2], pB=pl[1], pT=pl[1]+pl[3];
                if (px+HW > pL && px-HW < pR && py+HH > pB && py-HH < pT) {
                    float overlapL = (px+HW) - pL;
                    float overlapR = pR - (px-HW);
                    if (overlapL < overlapR) { px = pL - HW; pvx = 0; }
                    else                     { px = pR + HW; pvx = 0; }
                }
            }
            
            for (float[] mp : movPlats) {
                float pL=mp[0], pR=mp[0]+mp[2], pB=mp[1], pT=mp[1]+mp[3];
                if (px+HW > pL && px-HW < pR && py+HH > pB && py-HH < pT) {
                    float overlapL = (px+HW) - pL;
                    float overlapR = pR - (px-HW);
                    if (overlapL < overlapR) { px = pL - HW; pvx = 0; }
                    else                     { px = pR + HW; pvx = 0; }
                }
            }

            
            onGround = false;
            py += pvy * dt;

            
            if (py - HH <= groundY) { py = groundY + HH; pvy = 0; onGround = true; }

            
            for (float[] pl : platforms) {
                float pL=pl[0], pR=pl[0]+pl[2], pB=pl[1], pT=pl[1]+pl[3];
                if (px+HW > pL && px-HW < pR && py-HH < pT && py-HH >= pB && pvy <= 0) {
                    py = pT + HH; pvy = 0; onGround = true;
                }
            }

            
            for (int i = 0; i < movPlats.length; i++) {
                float[] mp = movPlats[i];
                float pL=mp[0], pR=mp[0]+mp[2], pB=mp[1], pT=mp[1]+mp[3];
                if (px+HW > pL && px-HW < pR && py-HH < pT && py-HH >= pB && pvy <= 0) {
                    py = pT + HH; pvy = 0; onGround = true; riderIdx = i;
                }
            }
            
            if (riderIdx >= 0) px += movPlats[riderIdx][4] * dt;

            
            for (float[] o : obstacles) {
                if (o[4] == 1) {
                    o[0] += o[5] * dt;
                    if (o[0] < o[6]) { o[0] = o[6]; o[5] = -o[5]; }
                    if (o[0] > o[7]) { o[0] = o[7]; o[5] = -o[5]; }
                }
            }

            
            for (float[] e : enemies) {
                e[0] += e[2] * dt;
                if (e[0] < e[3]) { e[0] = e[3]; e[2] = -e[2]; }
                if (e[0] > e[4]) { e[0] = e[4]; e[2] = -e[2]; }
                if (Math.abs(px-e[0]) < 34 && Math.abs(py-e[1]) < 38) { loseLife(); return; }
            }

            
            for (float[] o : obstacles) {
                if (px+HW > o[0] && px-HW < o[0]+o[2] && py+HH > o[1] && py-HH < o[1]+o[3]) { loseLife(); return; }
            }

            
            float halfW = ax(W / 2f);
            camX = Math.max(0, Math.min(px - halfW, levelW - ax(W)));

            
            Iterator<float[]> ci = coins.iterator();
            while (ci.hasNext()) {
                float[] co = ci.next();
                if (Math.abs(px-co[0]) < 26 && Math.abs(py-co[1]) < 26) {
                    ci.remove();
                    int coinVal = co.length > 3 ? (int) co[3] : 10;
                    if (co[2] > 0) { score += coinVal; greenCoins++; }
                    else           { score -= coinVal; redCoins++; if (score < 0) score = 0; }
                }
            }

            
            if (px < 30) px = 30;
            if (px > levelW - 30) px = levelW - 30;

            
            if (Math.abs(px-1900) < 60 && Math.abs(py-160) < 70) {
                win = true;
                GlobalScore.add(score);
            }

            
            if (py < -200) loseLife();
        }

        void loseLife() {
            lives--;
            if (lives <= 0) gameOver = true;
            else { px = 100; py = 200; pvy = 0; pvx = 0; }
        }

        @Override void draw(Canvas c, Paint p) {
            
            p.setColor(Color.parseColor("#87CEEB")); c.drawRect(0,0,W,H,p);

            
            p.setColor(Color.WHITE);
            for (int i = 0; i < 4; i++) {
                float ccx = ((i*350 + camX*scaleX*0.12f) % (levelW*scaleX+600)) - 200;
                float ccy = H*0.1f + i*35;
                c.drawCircle(ccx,ccy,28,p); c.drawCircle(ccx+22,ccy-9,33,p); c.drawCircle(ccx+46,ccy,28,p);
            }

            
            float groundSy = sy(groundY);
            p.setColor(Color.parseColor("#228B22")); c.drawRect(0, groundSy, W, H, p);
            p.setColor(Color.parseColor("#1a6b1a")); c.drawRect(0, groundSy, W, groundSy+4, p);

            
            for (float[] pl : platforms) {
                float bx = sx(pl[0]-camX), by = sy(pl[1]+pl[3]);
                float bw = sx(pl[2]+camX)-sx(camX), bh = Math.abs(sy(0)-sy(pl[3]));
                p.setColor(Color.parseColor("#8B6914"));  c.drawRect(bx,by,bx+bw,by+bh,p);
                p.setColor(Color.parseColor("#CD853F"));  c.drawRect(bx+3,by+3,bx+bw-3,by+bh-3,p);
                p.setColor(Color.parseColor("#8B6914"));
                c.drawLine(bx+bw/2,by,bx+bw/2,by+bh,p);
                c.drawLine(bx,by+bh/2,bx+bw,by+bh/2,p);
            }

            
            for (float[] mp : movPlats) {
                float bx = sx(mp[0]-camX), by = sy(mp[1]+mp[3]);
                float bw = sx(mp[2]+camX)-sx(camX), bh = Math.abs(sy(0)-sy(mp[3]));
                p.setColor(Color.parseColor("#3CB371")); c.drawRect(bx,by,bx+bw,by+bh,p);
                p.setColor(Color.parseColor("#2E8B57")); c.drawRect(bx,by,bx+bw,by+4,p);
            }

            
            for (float[] o : obstacles) {
                float bx = sx(o[0]-camX), by = sy(o[1]+o[3]);
                float bw = sx(o[2]+camX)-sx(camX), bh = Math.abs(sy(0)-sy(o[3]));
                if (o[4] == 1) {
                    p.setColor(Color.parseColor("#CC0000")); c.drawRect(bx,by,bx+bw,by+bh,p);
                    p.setColor(Color.YELLOW);
                    c.drawLine(bx,by,bx+bw,by+bh,p); c.drawLine(bx+bw,by,bx,by+bh,p);
                } else {
                    p.setColor(Color.DKGRAY); c.drawCircle(bx+bw/2,by+bh/2,bw/2,p);
                    p.setColor(Color.parseColor("#FF4500")); c.drawCircle(bx+bw/2,by+bh*0.2f,4,p);
                }
            }

            
            for (float[] e : enemies) {
                float ex = sx(e[0]-camX), ey = sy(e[1]);
                p.setColor(Color.parseColor("#4169E1")); c.drawOval(ex-22,ey-18,ex+22,ey+12,p);
                p.setColor(Color.WHITE); c.drawCircle(ex-8,ey-8,5,p); c.drawCircle(ex+8,ey-8,5,p);
                p.setColor(Color.BLACK); c.drawCircle(ex-7,ey-8,2,p); c.drawCircle(ex+9,ey-8,2,p);
            }

            
            p.setTextAlign(Paint.Align.CENTER);
            for (float[] co : coins) {
                float cx = sx(co[0]-camX), cy = sy(co[1]);
                boolean green = co[2] > 0;
                p.setColor(green ? Color.parseColor("#00CC00") : Color.parseColor("#CC0000"));
                c.drawCircle(cx,cy,11,p);
                p.setColor(green ? Color.parseColor("#88FF88") : Color.parseColor("#FF8888"));
                c.drawCircle(cx-3,cy-3,4,p);
                p.setColor(Color.WHITE); p.setTextSize(11);
                int coinVal = co.length > 3 ? (int) co[3] : 10;
                c.drawText((green ? "+" : "-") + coinVal, cx, cy+4, p);
            }
            p.setTextAlign(Paint.Align.LEFT);

            
            float finSx = sx(1900-camX), finSy = sy(160);
            p.setColor(Color.parseColor("#FFD700")); c.drawRect(finSx-3,finSy-80,finSx+3,finSy,p);
            p.setColor(Color.parseColor("#FF4444")); c.drawRect(finSx+3,finSy-80,finSx+33,finSy-50,p);

            
            float plSx = sx(px-camX), plSy = sy(py);
            p.setColor(Color.parseColor("#4169E1")); c.drawRect(plSx-18,plSy-28,plSx+18,plSy+26,p);
            p.setColor(Color.parseColor("#FFDAB9")); c.drawCircle(plSx,plSy-40,16,p);
            p.setColor(Color.parseColor("#2F4F8F"));
            float la = (float)Math.sin(legAnim)*9;
            c.drawRect(plSx-10,plSy+26,plSx-2,plSy+46+la,p);
            c.drawRect(plSx+2, plSy+26,plSx+10,plSy+46-la,p);

            
            p.setColor(Color.parseColor("#CC000000")); c.drawRect(0,0,W,58,p);
            p.setTextSize(22); p.setColor(Color.WHITE);
            c.drawText("❤"+lives, 16, 40, p);
            p.setColor(Color.parseColor("#00CC00"));
            c.drawText("+"+greenCoins, W*0.25f, 40, p);
            p.setColor(Color.parseColor("#CC0000"));
            c.drawText("-"+redCoins,  W*0.38f, 40, p);
            p.setColor(Color.YELLOW);
            c.drawText("⭐"+score+"/"+SCORE_TARGET, W*0.52f, 40, p);
            
            float barL=W*0.72f, barR=W-16;
            p.setColor(Color.parseColor("#555555")); c.drawRect(barL,14,barR,28,p);
            float prog = Math.min(1f, px / levelW);
            p.setColor(Color.parseColor("#00CC00")); c.drawRect(barL,14,barL+(barR-barL)*prog,28,p);
            p.setColor(Color.WHITE); p.setTextSize(17);
            c.drawText("⏱"+(int)gameTime+"s", barL, 52, p);
            
            p.setColor(Color.parseColor("#AAFFFFFF")); p.setTextSize(17);
            c.drawText("◀ лево  |  центр = прыжок  |  право ▶", 16, H-8, p);

            if (!started) overlay(c,p,"ПОЛОСА ПРЕПЯТСТВИЙ","Тапни для начала | Цель: ⭐"+SCORE_TARGET,Color.YELLOW);
            else if (gameOver) overlay(c,p,"ИГРА ОКОНЧЕНА","Счёт:"+score+"  Тапни=рестарт",Color.RED);
            else if (win) {
                String msg = score>=SCORE_TARGET ? "ПОБЕДА! 🏆" : "Финиш! Счёт "+score+"/"+SCORE_TARGET;
                overlay(c,p,msg,"Тапни для рестарта",score>=SCORE_TARGET?Color.YELLOW:Color.parseColor("#FF8888"));
            }
        }

        @Override void onDown(float x, float y) {
            if (!started || gameOver || win) { buildLevel(); started = true; return; }
            leftHeld  = x < W * 0.33f;
            rightHeld = x > W * 0.66f;
            if (x >= W*0.33f && x <= W*0.66f && onGround) pvy = JUMP_VEL_A;
        }
        @Override void onMove(float x, float y) { leftHeld = x < W*0.33f; rightHeld = x > W*0.66f; }
        @Override void onUp(float x, float y, float dx, float dy) { leftHeld = false; rightHeld = false; }
    }

    
    
    
    static class TetrisState extends GameState {
        static final int COLS=10,ROWS=20;
        int CELL;
        int[][]board=new int[ROWS][COLS];
        int[][]piece; int px,py,pieceColor,score=0,lines=0;
        boolean started=false,gameOver=false;
        long lastDrop=0;
        static final int[][][]PIECES={{{1,1,1,1}},{{1,1},{1,1}},{{0,1,0},{1,1,1}},{{1,0},{1,0},{1,1}},{{0,1},{0,1},{1,1}},{{0,1,1},{1,1,0}},{{1,1,0},{0,1,1}}};
        static final int[]COLORS={0xFF00FFFF,0xFFFFD700,0xFFAA00FF,0xFFFF8800,0xFF4466FF,0xFF00CC44,0xFFFF3333};
        Random rnd=new Random();

        @Override void init(){CELL=Math.min(W/COLS,(H-70)/ROWS);reset();}

        void reset(){board=new int[ROWS][COLS];score=0;lines=0;gameOver=false;spawnPiece();}

        void spawnPiece(){
            int i=rnd.nextInt(PIECES.length);
            piece=clonePiece(PIECES[i]);pieceColor=COLORS[i];
            px=COLS/2-piece[0].length/2;py=0;
            if(!can(piece,px,py))gameOver=true; if(gameOver) GlobalScore.add(score/10);
        }

        int[][]clonePiece(int[][]p){int[][]r=new int[p.length][];for(int i=0;i<p.length;i++)r[i]=p[i].clone();return r;}

        boolean can(int[][]p,int ox,int oy){
            for(int r=0;r<p.length;r++)for(int c=0;c<p[r].length;c++)
                if(p[r][c]!=0){int nr=oy+r,nc=ox+c;if(nr<0||nr>=ROWS||nc<0||nc>=COLS||board[nr][nc]!=0)return false;}
            return true;
        }

        void place(){
            for(int r=0;r<piece.length;r++)for(int c=0;c<piece[r].length;c++)
                if(piece[r][c]!=0)board[py+r][px+c]=pieceColor;
            clearLines();spawnPiece();
        }

        int _prevScore=0;
    void clearLines(){
        _prevScore=score;
            
            
            List<Integer>cleared=new ArrayList<>();
            for(int r=ROWS-1;r>=0;r--){
                boolean full=true;
                for(int c=0;c<COLS;c++)if(board[r][c]==0){full=false;break;}
                if(full)cleared.add(r);
            }
            if(cleared.isEmpty())return;

            for(int r:cleared){
                
                
                int basePoints=200;
                int rowScore=(int)(basePoints*(r+1f)/ROWS);
                score+=rowScore;
                lines++;
                
                for(int rr=r;rr>0;rr--)board[rr]=board[rr-1].clone();
                board[0]=new int[COLS];
            }
            
            if(cleared.size()>=2)score+=cleared.size()*50;
            if(cleared.size()>=4)score+=300; 
        }

        int[][]rot(int[][]m){int R=m.length,C=m[0].length;int[][]r=new int[C][R];for(int i=0;i<R;i++)for(int j=0;j<C;j++)r[j][R-1-i]=m[i][j];return r;}

        long dropInterval(){return Math.max(80,650-lines*25L);}

        @Override void update(float dt){
            if(!started||gameOver)return;
            long now=System.currentTimeMillis();
            if(now-lastDrop>dropInterval()){lastDrop=now;if(can(piece,px,py+1))py++;else place();}
        }

        @Override void draw(Canvas c,Paint p){
            p.setColor(Color.parseColor("#0d0d1a"));c.drawRect(0,0,W,H,p);

            int offX=(W-COLS*CELL)/2,offY=70;

            
            for(int r=0;r<ROWS;r++){
                
                int alpha=30+(r*2);
                p.setColor(Color.argb(alpha,100,100,255));
                c.drawRect(offX,offY+r*CELL,offX+COLS*CELL,offY+(r+1)*CELL,p);
            }

            
            p.setColor(Color.parseColor("#1a1a33"));p.setStrokeWidth(1);
            for(int r=0;r<=ROWS;r++)c.drawLine(offX,offY+r*CELL,offX+COLS*CELL,offY+r*CELL,p);
            for(int cl=0;cl<=COLS;cl++)c.drawLine(offX+cl*CELL,offY,offX+cl*CELL,offY+ROWS*CELL,p);

            
            p.setTextSize(11);p.setTextAlign(Paint.Align.LEFT);
            for(int r=0;r<ROWS;r+=4){
                int pts=(int)(200*(r+1f)/ROWS);
                p.setColor(Color.argb(160,255,255,100));
                c.drawText("+"+pts,offX+COLS*CELL+4,offY+r*CELL+CELL,p);
            }

            
            for(int r=0;r<ROWS;r++)for(int cl=0;cl<COLS;cl++)
                if(board[r][cl]!=0)drawCell(c,p,offX+cl*CELL,offY+r*CELL,board[r][cl]);

            
            if(piece!=null)for(int r=0;r<piece.length;r++)for(int cl=0;cl<piece[r].length;cl++)
                if(piece[r][cl]!=0)drawCell(c,p,offX+(px+cl)*CELL,offY+(py+r)*CELL,pieceColor);

            
            if(piece!=null&&started&&!gameOver){
                int ghostY=py;
                while(can(piece,px,ghostY+1))ghostY++;
                for(int r=0;r<piece.length;r++)for(int cl=0;cl<piece[r].length;cl++)
                    if(piece[r][cl]!=0){
                        p.setColor(Color.argb(50,255,255,255));
                        c.drawRect(offX+(px+cl)*CELL+2,offY+(ghostY+r)*CELL+2,offX+(px+cl+1)*CELL-2,offY+(ghostY+r+1)*CELL-2,p);
                    }
            }

            
            p.setTextAlign(Paint.Align.LEFT);
            p.setColor(Color.WHITE);p.setTextSize(28);
            c.drawText("⭐"+score,16,44,p);
            p.setTextSize(20);p.setColor(Color.parseColor("#AAAAFF"));
            c.drawText("📏"+lines,16,66,p);
            p.setColor(Color.YELLOW);p.setTextSize(17);
            c.drawText("↓ ниже = больше очков",W/2f-90,H-6,p);

            if(!started)overlay(c,p,"🧩 TETRIS","Свайп=сдвиг  Тап=поворот  ↓=сброс",Color.YELLOW);
            else if(gameOver)overlay(c,p,"GAME OVER","Счёт:"+score+"  Тапни=рестарт",Color.YELLOW);
        }

        void drawCell(Canvas c,Paint p,int x,int y,int color){
            p.setColor(color);c.drawRect(x+2,y+2,x+CELL-2,y+CELL-2,p);
            p.setColor(0x55FFFFFF);c.drawRect(x+2,y+2,x+CELL-2,y+6,p);c.drawRect(x+2,y+2,x+6,y+CELL-2,p);
            p.setColor(0x33000000);c.drawRect(x+2,y+CELL-6,x+CELL-2,y+CELL-2,p);c.drawRect(x+CELL-6,y+2,x+CELL-2,y+CELL-2,p);
        }

        @Override void onDown(float x,float y){if(!started||gameOver){reset();started=true;}}
        @Override void onUp(float x,float y,float dx,float dy){
            if(!started||gameOver)return;
            float dist=(float)Math.sqrt(dx*dx+dy*dy);
            if(dist<22){int[][]r=rot(piece);if(can(r,px,py))piece=r;}
            else if(Math.abs(dx)>Math.abs(dy)){if(dx>0&&can(piece,px+1,py))px++;else if(dx<0&&can(piece,px-1,py))px--;}
            else if(dy>0){while(can(piece,px,py+1))py++;place();}
        }
    }
}
