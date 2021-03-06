package game.damay;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class PlayScreen implements Screen {

	private static final int PANTY_AREA = 48;
	public static final int ASSETS_CREATED = 6;

	private static final int rows = 5;
	private static final int columns = 5;

	private SpriteBatch batch;
	private OrthographicCamera camera;
	private FitViewport viewport;

	private Stage stage;
	private Skin skin;

	public int selections[];
	private int tapAttempts = 0;
	private Panty selectedPanties[];

	private Sound hit_sound, miss_sound;

	private Panty[][] panties;

	private long initialTime;
	private Label timeLabel;
	private Label scoreLabel;
	private int matches;

	private Game g;

	public PlayScreen(SpriteBatch batch, Game g) {
		this.batch = batch;
		this.g = g;
	}

	@Override
	public void show() {
		camera = new OrthographicCamera();
		viewport = new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), camera);
		viewport.apply();

		/* User Interface */
		stage = new Stage(new FitViewport(800 / 2, 600 / 2, new OrthographicCamera()), batch);
		Gdx.input.setInputProcessor(stage);

		Table table = new Table();
		table.setPosition(48 * columns + 80, 40);
		stage.addActor(table);

		// table.setDebug(true);

		skin = new Skin();

		skin.add("my_font", new BitmapFont(Gdx.files.internal("christy.fnt")), BitmapFont.class);

		LabelStyle labelStyle = new LabelStyle(skin.getFont("my_font"), skin.getFont("my_font").getColor());
		timeLabel = new Label("Time", labelStyle);

		matches = 0;
		scoreLabel = new Label("Score", labelStyle);

		table.add(scoreLabel);
		table.row();
		table.add(timeLabel);

		/* Load sound */
		hit_sound = Gdx.audio.newSound(Gdx.files.internal("audio/hit.wav"));
		miss_sound = Gdx.audio.newSound(Gdx.files.internal("audio/miss2.wav"));

		/* Create 2D array of actors */
		panties = new Panty[rows][columns];
		selections = new int[2];
		selectedPanties = new Panty[2];

		for (int i = 0; i < rows; ++i) {
			for (int j = 0; j < columns; ++j) {
				panties[i][j] = new Panty(MathUtils.random(1, ASSETS_CREATED), new Vector2(i * PANTY_AREA, j * PANTY_AREA), this);
				stage.addActor(panties[i][j]);
			}
		}

		Timer.schedule(new Task() {
			@Override
			public void run() {
				for (int h = 0; h < 2; h++) {
					int i, j;
					i = MathUtils.random(rows - 1);
					j = MathUtils.random(columns - 1);

					while (panties[i][j] == selectedPanties[0] || panties[i][j] == selectedPanties[1]) {
						i = MathUtils.random(rows - 1);
						j = MathUtils.random(columns - 1);
					}

					panties[i][j].remove();
					panties[i][j] = new Panty(MathUtils.random(1, ASSETS_CREATED), new Vector2(i * PANTY_AREA, j * PANTY_AREA), PlayScreen.this);
					stage.addActor(panties[i][j]);
				}
			}
		}, 0, 3 / 4f);

		initialTime = TimeUtils.millis();
	}

	public void changeSelection(int i, Panty p) {
		if (!(tapAttempts >= 2)) {
			tapAttempts++;
		} else {
			tapAttempts = 0;
		}

		if (tapAttempts == 1)
			this.selections[0] = i;
		else if (tapAttempts == 2)
			this.selections[1] = i;

		if (tapAttempts != 0) {
			selectedPanties[tapAttempts - 1] = p;
		}
	}

	@Override
	public void render(float delta) {
		if (45 - TimeUtils.timeSinceMillis(initialTime) / 1000 == 0) {
			g.setScreen(new EndScreen(batch, g, skin, matches));
		}

		timeLabel.setText("Time " + (45 - TimeUtils.timeSinceMillis(initialTime) / 1000));
		scoreLabel.setText("Matches: " + matches);

		if (tapAttempts == 2) {
			if (selectedPanties[0].pantyNumber == selectedPanties[1].pantyNumber && selectedPanties[0] != selectedPanties[1]) {
				hit_sound.play();
				matches += 1;

				/* remove the panties and replace them */
				for (Panty p : selectedPanties) {
					for (int i = 0; i < rows; i++) {
						for (int j = 0; j < columns; j++) {
							if (panties[i][j] == p) {
								panties[i][j].remove();
								panties[i][j] = new Panty(MathUtils.random(1, ASSETS_CREATED), new Vector2(i * PANTY_AREA, j * PANTY_AREA), this);
								stage.addActor(panties[i][j]);
							}
						}
					}
				}
			} else {
				miss_sound.play();
			}
			tapAttempts = 0;
		}

		Gdx.gl.glClearColor(0, 0.07f, 0.15f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		batch.setProjectionMatrix(camera.combined);

		batch.begin();
		batch.end();

		stage.act();
		stage.draw();
	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height);
		stage.getViewport().update(width, height, true);
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		stage.dispose();
		skin.dispose();
		batch.dispose();
	}

}
