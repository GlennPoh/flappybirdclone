package com.haojin.flappybird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;

import java.util.Random;

public class FlappyBird extends ApplicationAdapter {
	private SpriteBatch batch;
	private Texture background, bottom, top, gameOver;
	private Sound soundDie, soundHit, soundPoint, soundSwoosh, soundWing;
//	ShapeRenderer shapeRenderer;

	private Texture[] birds;
	private int flapState = 0, score = 0, scoringTube = 0, gameState = 0;
	private float birdY = 0, velocity = 0;
	private Rectangle[] topTubeRectangle, bottomTubeRectangle;
	private Circle birdCircle;
	private BitmapFont font;

	private Random randomGenerator;
	private float gravity = 1.5f, gap = 400, maxTubeOffset, tubeVelocity = 4;
	private int numberOfTubes = 4;
	private float[] tubeX = new float[numberOfTubes];
	private float[] tubeOffset = new float[numberOfTubes];
	private float distanceBetweenTubes;

	@Override
	public void create () {
		batch = new SpriteBatch();
		background = new Texture("bg.png");
		gameOver = new Texture("gameover.png");
//		shapeRenderer = new ShapeRenderer();
		birdCircle = new Circle();
		font = new BitmapFont();
		font.setColor(Color.WHITE);
		font.getData().setScale(10);

		birds = new Texture[2];
		birds[0] = new Texture("bird.png");
		birds[1] = new Texture("bird2.png");

		soundDie = Gdx.audio.newSound(Gdx.files.internal("sfx_die.wav"));
		soundHit = Gdx.audio.newSound(Gdx.files.internal("sfx_hit.wav"));
		soundPoint = Gdx.audio.newSound(Gdx.files.internal("sfx_point.wav"));
		soundSwoosh = Gdx.audio.newSound(Gdx.files.internal("sfx_swooshing.wav"));
		soundWing = Gdx.audio.newSound(Gdx.files.internal("sfx_wing.wav"));

		bottom = new Texture("bottomtube.png");
		top = new Texture("toptube.png");
		maxTubeOffset = Gdx.graphics.getHeight() / 2 - gap / 2 - 100;
		randomGenerator = new Random();
		distanceBetweenTubes = Gdx.graphics.getWidth() * 3 / 4;
		topTubeRectangle = new Rectangle[numberOfTubes];
		bottomTubeRectangle = new Rectangle[numberOfTubes];
		initGame();
	}

	public void initGame() {
		birdY = Gdx.graphics.getHeight() / 2 - birds[0].getHeight() / 2;
		for (int i = 0; i < numberOfTubes; i++) {
			tubeOffset[i] = (randomGenerator.nextFloat()- 0.5f) * (Gdx.graphics.getHeight() - gap - 200);
			tubeX[i] = Gdx.graphics.getWidth() / 2 - top.getWidth() / 2 + Gdx.graphics.getWidth() + i * distanceBetweenTubes;
			topTubeRectangle[i] = new Rectangle();
			bottomTubeRectangle[i] = new Rectangle();
		}
	}

	@Override
	public void render() {

		batch.begin();
		batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		if (gameState == 1) {

			if (tubeX[scoringTube] < Gdx.graphics.getWidth() / 2) {
				score++;
				soundPoint.play(1.0f);
				Gdx.app.log("Score", String.valueOf(score));
				if (scoringTube < numberOfTubes - 1) {
					scoringTube++;
				} else {
					scoringTube = 0;
				}
			}

			if (Gdx.input.justTouched()) {
				velocity = -20;
				soundSwoosh.play(1.0f);
			}

			for (int i = 0; i < numberOfTubes; i++) {

				if (tubeX[i] < -top.getWidth()) {
					tubeX[i] += numberOfTubes * distanceBetweenTubes;
					tubeOffset[i] = (randomGenerator.nextFloat()- 0.5f) * (Gdx.graphics.getHeight() - gap - 200);
				} else {
					tubeX[i] = tubeX[i] - tubeVelocity;
				}

				batch.draw(top, tubeX[i], Gdx.graphics.getHeight() / 2 + gap / 2 + tubeOffset[i]);
				batch.draw(bottom, tubeX[i], Gdx.graphics.getHeight() / 2 - gap / 2 - bottom.getHeight() + tubeOffset[i]);

				topTubeRectangle[i] = new Rectangle(tubeX[i], Gdx.graphics.getHeight() / 2 + gap / 2 + tubeOffset[i], top.getWidth(), top.getHeight());
				bottomTubeRectangle[i] = new Rectangle(tubeX[i], Gdx.graphics.getHeight() / 2 - gap / 2 - bottom.getHeight() + tubeOffset[i], bottom.getWidth(), bottom.getHeight());
			}

			if (birdY >= 0) {
				velocity = velocity + gravity;
				birdY -= velocity;
			} else {
				gameState = 2;
			}
		} else if (gameState == 0) {
			if (Gdx.input.justTouched()) {
				gameState = 1;
			}
		} else if (gameState == 2) {
			batch.draw(gameOver, Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2);
			if (Gdx.input.justTouched()) {
				gameState = 1;
				initGame();
				score = 0;
				scoringTube = 0;
				velocity = 0;
				soundHit.play(1.0f);
				soundDie.play(1.0f);
			}

		}

		if (flapState == 0) {
			flapState = 1;
		} else if (flapState == 1) {
			flapState = 0;
		}

		batch.draw(birds[flapState], Gdx.graphics.getWidth() / 2 - birds[flapState].getWidth() / 2, birdY);
		font.draw(batch, String.valueOf(score), 100, 200);
		batch.end();
		birdCircle.set(Gdx.graphics.getWidth() / 2, birdY + birds[flapState].getHeight() / 2, birds[flapState].getWidth() / 2);
//		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
//		shapeRenderer.setColor(Color.RED);
//		shapeRenderer.circle(birdCircle.x, birdCircle.y, birdCircle.radius);

		for (int i = 0; i < numberOfTubes; i++) {
//			shapeRenderer.rect(tubeX[i], Gdx.graphics.getHeight() / 2 + gap / 2 + tubeOffset[i], top.getWidth(), top.getHeight());
//			shapeRenderer.rect(tubeX[i], Gdx.graphics.getHeight() / 2 - gap / 2 - bottom.getHeight() + tubeOffset[i], bottom.getWidth(), bottom.getHeight());

			if (Intersector.overlaps(birdCircle, topTubeRectangle[i]) || Intersector.overlaps(birdCircle, bottomTubeRectangle[i])) {
				gameState = 2;
			}
		}
//		shapeRenderer.end();
	}

}
