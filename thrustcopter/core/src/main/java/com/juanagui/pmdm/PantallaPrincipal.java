package com.juanagui.pmdm;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;

import java.io.IOException;

public class PantallaPrincipal extends ScreenAdapter implements InputProcessor {

    MainGame game;

    private static TextureRegion aboveGrassTextureRegion;
    private static TextureRegion belowGrassTextureRegion;
    // ATRIBUTOS
    public static final float WIDTH = 800f;
    public static final float HEIGHT = 480f;
    public static final float TERRAIN_SPEED_PPS = 200f;
    public static final float BACKGROUND_SPEED_PPS = 20;
    public static final float PLANE_TAP_SPEED = 200f;
    public static final int GRAVITY = -8;

    //VARIABLES PARA ESCRIBIR EL TIEMPO MÁXIMO JUGADO (TIMER)
    public  FileHandle handle = Gdx.files.local("gameTime.txt") ;
    public  float maxTime = Float.parseFloat(handle.readString());

    //SCORE EN FICHERO
    public FileHandle handleScore = Gdx.files.local("gameScore.txt");
    public int maxScore = Integer.parseInt(handleScore.readString());

    private TextureAtlas textureAtlas;
    private FPSLogger fpsLogger;

    //CAMARA VISION
    private OrthographicCamera camera;

    // AJUSTA LA CAMARA AL TAMAÑO DE LA PANTAllA PARA QUE NO DEFORME LAS IMÁGENES
    private FitViewport viewport;

    // ATLAS DE TEXTURAS
    private TextureRegion backgroundTextureRegion;

    private float terrainOffset = 0f;
    private float backgroundOffset = 0f;

    // PILARES
    private TextureRegion pillarUp;
    private TextureRegion pillarDown;
    private final Array<Vector2> pillars = new Array<>();
    private static final float MIN_PILLAR_DISTANCE = WIDTH;
    private static final float PILLAR_DISTANCE_RANGE = 100;
    private static final float NEW_PILLAR_POSITION_THRESHOLD = WIDTH - 100;
    private Vector2 lastPillarPosition;

    // ANIMACIÓN (AVIÓN)
    private Animation<TextureRegion> planeAnimation;
    private TextureRegion planeTextureRegion1;
    private TextureRegion planeTextureRegion2;
    private TextureRegion planeTextureRegion3;
    private float planeAnimTime;

    // POSICIÓN DEL AVIÓN
    private Vector2 defaultPlanePosition;
    private Vector2 planePosition;
    private Vector2 gravity;
    private Vector2 planeVelocity;
    private float damping = 0.99f;

    //RECTANGULOS PARA LAS COLISIONES
    private final Rectangle planeBoundingBox = new Rectangle();
    private final Rectangle pillarBoundingBox = new Rectangle();

    //MÚSICA
    private Music music;
    private Sound crashSound;

    //TIMER
    float time =0;
    private GlyphLayout counter;
    private GlyphLayout maxTimePlayed;

    //SCORE
    private final Rectangle scoreBoundingBox = new Rectangle();

    private int score;
    private GlyphLayout actualScore;
    private GlyphLayout recordScore;

    // GAME OVER
    private Boolean gameover = false;


    public PantallaPrincipal(MainGame game) {
        this.game = game;

    }
    @Override
    public void show() {
        fpsLogger = new FPSLogger();       // Nos dice los frames por segundo a los que va el juego
        camera = new OrthographicCamera();
        camera.position.set(WIDTH / 2f, HEIGHT / 2F, 0);
        viewport = new FitViewport(WIDTH, HEIGHT, camera);
        game.batch = new SpriteBatch();

        //TEXTURA ATLAS (CONTIENE TODAS LAS IMAGENES TEXTURAS EN UN SOLO DOCUMENTO)
        textureAtlas = new TextureAtlas(Gdx.files.internal("ThrustCopter.pack  "));
        backgroundTextureRegion = textureAtlas.findRegion("background");
        //SUELO
        belowGrassTextureRegion = textureAtlas.findRegion("groundGrass");
        aboveGrassTextureRegion = new TextureRegion(belowGrassTextureRegion);
        aboveGrassTextureRegion.flip(true, true);

        // PILARES PARA ESQUIVAR
        pillarUp = textureAtlas.findRegion("rockGrassUp");
        pillarDown = textureAtlas.findRegion("rockGrassDown");

        // AVION
        planeTextureRegion1 = textureAtlas.findRegion("planeRed1");
        planeTextureRegion2 = textureAtlas.findRegion("planeRed2");
        planeTextureRegion3 = textureAtlas.findRegion("planeRed3");
        planeAnimation = new Animation<>(0.05f, planeTextureRegion1, planeTextureRegion2, planeTextureRegion3, planeTextureRegion2);
        planeAnimation.setPlayMode(Animation.PlayMode.LOOP);

        //POSICION
        defaultPlanePosition = new Vector2(
                planeTextureRegion1.getRegionWidth(),
                HEIGHT / 2f - planeTextureRegion1.getRegionHeight() / 2f);
        planePosition = new Vector2(defaultPlanePosition);
        gravity = new Vector2(0, GRAVITY);
        planeVelocity = new Vector2();

        // añadimos pilares
        addPillar();

        // MUSICA
        crashSound = Gdx.audio.newSound(Gdx.files.internal("sounds/crash.ogg"));
        music = Gdx.audio.newMusic(Gdx.files.internal("sounds/journey.mp3"));
        music.setLooping(true);
        music.play();

        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void render(float delta) {
        fpsLogger.log();
        try {
            updateScene();
        } catch (IOException e) {
            e.printStackTrace();
        }
        drawScene();
    }
    private void updateScene() throws IOException {

        // CESPED
        terrainOffset -= TERRAIN_SPEED_PPS * Gdx.graphics.getDeltaTime();
        if (terrainOffset <= -belowGrassTextureRegion.getRegionWidth()) {
            terrainOffset = 0f;
        }
        planeVelocity.add(gravity);
        planeVelocity.scl(damping);
        planePosition.mulAdd(planeVelocity, Gdx.graphics.getDeltaTime());
        planePosition.x = defaultPlanePosition.x;
        planeAnimTime += Gdx.graphics.getDeltaTime();

        // LE DAMOS UN TAMAÑO A LA CAJA DEL AVION PARA CUANDO CHOQUE CONTRA LA CAJA DEL PILAR
        planeBoundingBox.set(planePosition.x + 10, planePosition.y + 10, planeTextureRegion1.getRegionWidth() - 20, planeTextureRegion1.getRegionHeight() - 20);


        // CONDICIONAMOS QUE SI EL AVION LLEGA A LA POSICION DEL SUELO DE ARRIBA O DE ABAJO TERMINAMOS EL JUEGO
        if (planePosition.y > HEIGHT - aboveGrassTextureRegion.getRegionHeight() / 2f ||
                planePosition.y < belowGrassTextureRegion.getRegionHeight() / 2f) {
            gameOver();
        }
        //POSICION DE LOS PILARES EN MOVIMIENTO

        for (Vector2 pillar : pillars) {
            pillar.x -= TERRAIN_SPEED_PPS * Gdx.graphics.getDeltaTime();

            //RECTANGULO DE LOS PILARES

            // pilares desde abajo
            if (pillar.y == 1) {
                pillarBoundingBox.set(pillar.x + 30, 0, pillarUp.getRegionWidth() - 60, pillarUp.getRegionHeight());

                //pilares desde arriba
            } else {
                pillarBoundingBox.set(pillar.x + 30, HEIGHT - pillarDown.getRegionHeight(), pillarDown.getRegionWidth() - 60, pillarDown.getRegionHeight());
            }

            // VEMOS SI SE CHOCAN LOS RECTANGULOS (con el metodo overLaps())
            if (planeBoundingBox.overlaps(pillarBoundingBox)) {
                gameOver(); //GAMEOVER
            }

            if (pillar.x < -pillarUp.getRegionWidth()) {
                pillars.removeValue(pillar, true);
            }
            //SCORE
            scoreBoundingBox.set(pillar.x + 10, 0, 1, HEIGHT);
            if (planeBoundingBox.overlaps(scoreBoundingBox)) {
                score += 1;
            }
        }

        if (lastPillarPosition.x < NEW_PILLAR_POSITION_THRESHOLD) {
            addPillar();
        }

        //BACKGORUND
        backgroundOffset -= BACKGROUND_SPEED_PPS * Gdx.graphics.getDeltaTime();
        if (backgroundOffset <= -2 * backgroundTextureRegion.getRegionWidth())
            backgroundOffset = 0f;

        //TIMER
        time += Gdx.graphics.getDeltaTime();
        String actualTimeString = String.format("TIME: %ds", (int) time);
        String recordTime = String.format("HIGH-TIME: %ds", (int) maxTime);
        counter = new GlyphLayout(game.fuenteScore, actualTimeString);
        maxTimePlayed = new GlyphLayout(game.fuenteScore, recordTime);
        // SCORE
        String scoreString = String.format("SCORE: %dpts", score) ;
        String maxScoreString = String.format("HIGHSCORE: %dpts", maxScore);
        actualScore = new GlyphLayout(game.fuenteScore, scoreString);
        recordScore = new GlyphLayout(game.fuenteScore, maxScoreString);

    }

    private void drawScene() {

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // limpia la pantalla para pintar lo nuevo

        // ACTUALIZACION DE LA CAMARA
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        // PINTAMOS LA IMAGEN
        game.batch.begin();

        //MOVIMIENTO DEL BACKGROUND
        game.batch.draw(backgroundTextureRegion, backgroundOffset, 0);
        // TREXTURA QUE CONTINÚA
        game.batch.draw(backgroundTextureRegion, backgroundOffset + backgroundTextureRegion.getRegionWidth(), 0);
        game.batch.draw(backgroundTextureRegion, backgroundOffset + 2 * backgroundTextureRegion.getRegionWidth(), 0);
        // PILARES
        //recorremos el array con un bucle

        for (Vector2 pillar : pillars) {
            if (pillar.y == 1) {
                game.batch.draw(pillarUp, pillar.x, 0);
            } else {
                game.batch.draw(pillarDown, pillar.x, HEIGHT - pillarDown.getRegionHeight());
            }
        }

        // pintamos las texturas (QUE SACAMOS DEL ATLAS DE TEXTURAS)

        //TEXTURA QUE VEMOS EN PANTALLA
        game.batch.draw(aboveGrassTextureRegion, terrainOffset, HEIGHT - aboveGrassTextureRegion.getRegionHeight());

        // TREXTURA QUE CONTINUA (NO VEMOS EN PANTALLA  ABAJO)
        game.batch.draw(aboveGrassTextureRegion, terrainOffset + aboveGrassTextureRegion.getRegionWidth(), HEIGHT - aboveGrassTextureRegion.getRegionHeight());

        //TEXTURA QUE VEMOS EN PANTALLA
        game.batch.draw(belowGrassTextureRegion, terrainOffset, 0);

        // TREXTURA QUE CONTINUA (NO VEMOS EN PANTALLA ARRIBA)
        game.batch.draw(belowGrassTextureRegion, terrainOffset + belowGrassTextureRegion.getRegionWidth(), 0);


        //LAS TEXTURAS QUE NO VEMOS EN PANTALLA LAS USARESMO PARA GENERAR EL MOVIMIENTO DEL JUEGO

        // POSICION DEL AVION
        game.batch.draw(planeAnimation.getKeyFrame(planeAnimTime), planePosition.x, planePosition.y);

        //TIMER
        game.fuenteScore.draw(game.batch, counter,570,HEIGHT-430);

        //TIEMPO MAXIMO JUGADO
        game.fuenteScore.draw(game.batch, maxTimePlayed,570,HEIGHT-400);

        //SCORE
        game.fuenteScore.draw(game.batch, actualScore,570,HEIGHT-80 );

        //SCORE MAX
        game.fuenteScore.draw(game.batch, recordScore,570,HEIGHT-50 );


        // CERRAMOS
        game.batch.end();
    }

    private void gameOver(){
        Gdx.app.log(PantallaPrincipal.class.getName(),"Collison-GameOver");
        if(!gameover) {
            crashSound.play();
            music.dispose();
            gameover=true;
            game.setScreen(new GameOver(game));
            if (time > maxTime) {
                handle.writeString(Float.toString(time), false);
            }
            if (score > maxScore) {
                handleScore.writeString(Integer.toString(score), false);
            }

        }
    }

    private void addPillar() {
        Vector2 tmpPosition = new Vector2();
        if (pillars.size == 0) {
            tmpPosition.x = MIN_PILLAR_DISTANCE + (float) (PILLAR_DISTANCE_RANGE * Math.random());
        } else {
            tmpPosition.x = lastPillarPosition.x + MIN_PILLAR_DISTANCE + (float) (PILLAR_DISTANCE_RANGE * Math.random());
        }

        if (MathUtils.randomBoolean()) {
            tmpPosition.y = 1;
        } else {
            tmpPosition.y = -1;
        }
        lastPillarPosition = tmpPosition;
        pillars.add(tmpPosition);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        planeVelocity.add(0, PLANE_TAP_SPEED);
        return true;
    }
    @Override
    public boolean keyDown(int keycode) {
        return false;
    }
    @Override
    public boolean keyUp(int keycode) {
        return false;
    }
    @Override
    public boolean keyTyped(char character) {
        return false;
    }
    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }
    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }
    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }
    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }}

