package com.bladecoder.engine.serialization;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter.OutputType;
import com.bladecoder.engine.assets.EngineAssetManager;
import com.bladecoder.engine.i18n.I18N;
import com.bladecoder.engine.model.World;
import com.bladecoder.engine.model.World.WorldProperties;
import com.bladecoder.engine.serialization.SerializationHelper.Mode;
import com.bladecoder.engine.util.EngineLogger;

public class JsonSerializer {
	public static final String GAMESTATE_EXT = ".gamestate.v13";

	private static final int SCREENSHOT_DEFAULT_WIDTH = 300;

	private World w;

	public JsonSerializer(World w) {
		this.w = w;
	}

	/**
	 * Load the world description in 'world.json'.
	 * 
	 * @throws IOException
	 */
	public void loadWorldDesc() throws IOException {

		String worldFilename = EngineAssetManager.WORLD_FILENAME;

		if (!EngineAssetManager.getInstance().getModelFile(worldFilename).exists()) {

			// Search the world file with ".json" ext if not found.
			worldFilename = EngineAssetManager.WORLD_FILENAME + ".json";

			if (!EngineAssetManager.getInstance().getModelFile(worldFilename).exists()) {
				EngineLogger.error("ERROR LOADING WORLD: world file not found.");
				w.dispose();
				throw new IOException("ERROR LOADING WORLD: world file not found.");
			}
		}

		SerializationHelper.getInstance().setMode(Mode.MODEL);

		JsonValue root = new JsonReader()
				.parse(EngineAssetManager.getInstance().getModelFile(worldFilename).reader("UTF-8"));

		Json json = new Json();
		json.setIgnoreUnknownFields(true);

		int width = json.readValue("width", Integer.class, root);
		int height = json.readValue("height", Integer.class, root);

		// We know the world width, so we can set the scale
		EngineAssetManager.getInstance().setScale(width, height);
		float scale = EngineAssetManager.getInstance().getScale();

		w.setWidth((int) (width * scale));
		w.setHeight((int) (height * scale));
		w.setInitChapter(json.readValue("initChapter", String.class, root));
		w.getVerbManager().read(json, root);
		I18N.loadWorld(EngineAssetManager.MODEL_DIR + EngineAssetManager.WORLD_FILENAME);
	}

	public void saveWorldDesc(FileHandle file) throws IOException {

		float scale = EngineAssetManager.getInstance().getScale();

		Json json = new Json();
		json.setOutputType(OutputType.javascript);

		SerializationHelper.getInstance().setMode(Mode.MODEL);

		json.setWriter(new StringWriter());

		json.writeObjectStart();
		json.writeValue("width", w.getWidth() / scale);
		json.writeValue("height", w.getHeight() / scale);
		json.writeValue("initChapter", w.getInitChapter());
		w.getVerbManager().write(json);
		json.writeObjectEnd();

		String s = null;

		if (EngineLogger.debugMode())
			s = json.prettyPrint(json.getWriter().getWriter().toString());
		else
			s = json.getWriter().getWriter().toString();

		Writer w = file.writer(false, "UTF-8");
		w.write(s);
		w.close();
	}

	public void saveModel(String chapterId) throws IOException {
		EngineLogger.debug("SAVING GAME MODEL");

		if (w.isDisposed())
			return;

		Json json = new Json();
		json.setOutputType(OutputType.javascript);

		String s = null;

		SerializationHelper.getInstance().setMode(Mode.MODEL);

		json.setWriter(new StringWriter());

		json.writeObjectStart();
			WorldSerializer.write(w, json);
		json.writeObjectEnd();


		if (EngineLogger.debugMode())
			s = json.prettyPrint(json.getWriter().getWriter().toString());
		else
			s = json.getWriter().getWriter().toString();

		Writer w = EngineAssetManager.getInstance().getModelFile(chapterId + EngineAssetManager.CHAPTER_EXT)
				.writer(false, "UTF-8");

		try {
			w.write(s);
			w.flush();
		} catch (IOException e) {
			throw new IOException("ERROR SAVING MODEL", e);
		} finally {
			w.close();
		}
	}
	
	public void loadGameState(FileHandle savedFile) throws IOException {
		EngineLogger.debug("LOADING GAME STATE");

		if (savedFile.exists()) {
			SerializationHelper.getInstance().setMode(Mode.STATE);

			JsonValue root = new JsonReader().parse(savedFile.reader("UTF-8"));

			Json json = new Json();
			json.setIgnoreUnknownFields(true);

			WorldSerializer.read(w, json, root);

		} else {
			throw new IOException("LOADGAMESTATE: no saved game exists");
		}
	}

	public void saveGameState(String filename) throws IOException {
		EngineLogger.debug("SAVING GAME STATE");

		if (w.isDisposed())
			return;

		Json json = new Json();
		json.setOutputType(OutputType.javascript);

		String s = null;

		SerializationHelper.getInstance().setMode(Mode.STATE);
		
		json.setWriter(new StringWriter());

		json.writeObjectStart();
			WorldSerializer.write(w, json);
		json.writeObjectEnd();


		if (EngineLogger.debugMode())
			s = json.prettyPrint(json.getWriter().getWriter().toString());
		else
			s = json.getWriter().getWriter().toString();

		Writer writer = EngineAssetManager.getInstance().getUserFile(filename).writer(false, "UTF-8");

		try {
			writer.write(s);
			writer.flush();
		} catch (IOException e) {
			throw new IOException("ERROR SAVING GAME", e);
		} finally {
			writer.close();
		}

		// Save Screenshot
		w.takeScreenshot(filename + ".png", SCREENSHOT_DEFAULT_WIDTH);
	}
	
	public void loadChapter(String chapterName) throws IOException {
		if (!w.isDisposed())
			w.dispose();

		long initTime = System.currentTimeMillis();

		SerializationHelper.getInstance().setMode(Mode.MODEL);

		if (chapterName == null)
			chapterName = w.getInitChapter();

		w.setCurrentChapter(chapterName);

		if (EngineAssetManager.getInstance().getModelFile(chapterName + EngineAssetManager.CHAPTER_EXT).exists()) {

			JsonValue root = new JsonReader().parse(EngineAssetManager.getInstance()
					.getModelFile(chapterName + EngineAssetManager.CHAPTER_EXT).reader("UTF-8"));

			Json json = new Json();
			json.setIgnoreUnknownFields(true);

			WorldSerializer.read(w, json, root);

			I18N.loadChapter(EngineAssetManager.MODEL_DIR + chapterName);

			w.getCustomProperties().put(WorldProperties.CURRENT_CHAPTER.toString(), chapterName);
			w.getCustomProperties().put(WorldProperties.PLATFORM.toString(), Gdx.app.getType().toString());
		} else {
			EngineLogger.error(
					"ERROR LOADING CHAPTER: " + chapterName + EngineAssetManager.CHAPTER_EXT + " doesn't exists.");
			w.dispose();
			throw new IOException(
					"ERROR LOADING CHAPTER: " + chapterName + EngineAssetManager.CHAPTER_EXT + " doesn't exists.");
		}

		EngineLogger.debug("MODEL LOADING TIME (ms): " + (System.currentTimeMillis() - initTime));
	}
}
