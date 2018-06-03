/*******************************************************************************
 * Copyright 2014 Rafael Garcia Moreno.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.bladecoder.engine.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.bladecoder.engine.actions.ActionCallback;
import com.bladecoder.engine.anim.Timers;
import com.bladecoder.engine.assets.AssetConsumer;
import com.bladecoder.engine.assets.EngineAssetManager;
import com.bladecoder.engine.polygonalpathfinder.NavNodePolygonal;
import com.bladecoder.engine.polygonalpathfinder.PolygonalNavGraph;
import com.bladecoder.engine.util.EngineLogger;

public class Scene implements AssetConsumer {

	public static final Color ACTOR_BBOX_COLOR = new Color(0.2f, 0.2f, 0.8f, 1f);
	public static final Color WALKZONE_COLOR = Color.GREEN;
	public static final Color OBSTACLE_COLOR = Color.RED;
	public static final Color ANCHOR_COLOR = Color.RED;
	public static final float ANCHOR_RADIUS = 14f;

	public static final String VAR_PLAYER = "$PLAYER";

	/**
	 * All actors in the scene
	 */
	private final Map<String, BaseActor> actors = new ConcurrentHashMap<String, BaseActor>();

	/**
	 * BaseActor layers
	 */
	private final List<SceneLayer> layers = new ArrayList<SceneLayer>();

	private final Timers timers = new Timers();

	private SceneCamera camera = new SceneCamera();

	private Array<AtlasRegion> background;
	private String backgroundAtlas;
	private String backgroundRegionId;

	/** For polygonal PathFinding */
	private PolygonalNavGraph polygonalNavGraph;

	/**
	 * depth vector. X: the actor 'y' position for a 0.0 scale, Y: the actor 'y'
	 * position for a 1.0 scale.
	 */
	private Vector2 depthVector;

	private String player;

	/** The actor the camera will follow */
	private SpriteActor followActor;

	/**
	 * Music for the scene.
	 */
	private MusicDesc musicDesc;

	private Vector2 sceneSize;

	private String id;

	/** internal state. Can be used for actions to maintain a state machine */
	private String state;

	private final VerbManager verbs;

	private final SceneSoundManager soundManager;

	private final TextManager textManager;
	
	private World w;

	public Scene(World w) {
		this.w = w;
		
		textManager = new TextManager(w);
		soundManager = new SceneSoundManager(w);
		verbs = new VerbManager(w);
	}
	
	public World getWorld() {
		return w;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getState() {
		return state;
	}

	public void setState(String s) {
		state = s;
	}

	public List<SceneLayer> getLayers() {
		return layers;
	}

	public SceneLayer getLayer(String name) {
		for (SceneLayer l : layers) {
			if (name.equals(l.getName()))
				return l;
		}

		return null;
	}

	public void addLayer(SceneLayer layer) {
		layers.add(layer);
	}

	public TextManager getTextManager() {
		return textManager;
	}

	public Timers getTimers() {
		return timers;
	}

	public void addTimer(float time, ActionCallback cb) {
		timers.addTimer(time, cb);
	}

	public MusicDesc getMusicDesc() {
		return musicDesc;
	}

	public void setMusicDesc(MusicDesc musicDesc) {
		this.musicDesc = musicDesc;
	}

	public float getFakeDepthScale(float y) {
		if (depthVector == null)
			return 1.0f;

		float worldScale = EngineAssetManager.getInstance().getScale();

		return Math.max(0, (y - depthVector.x * worldScale) / ((depthVector.y - depthVector.x) * worldScale));
	}

	public void init() {
		w.setCutMode(false);

		timers.clear();
		textManager.reset();

		// Run INIT action
		if (getVerb("init") != null)
			runVerb("init");
	}

	public VerbManager getVerbManager() {
		return verbs;
	}

	public Verb getVerb(String id) {
		return verbs.getVerb(id, state, null);
	}

	public void runVerb(String id) {
		verbs.runVerb(id, state, null, null);
	}

	public void update(float delta) {
		// We draw the elements in order: from top to bottom.
		// so we need to order the array list
		for (SceneLayer layer : layers)
			layer.update();

		for (BaseActor a : actors.values()) {
			a.update(delta);
		}

		camera.update(delta);

		if (followActor != null) {
			camera.updatePos(followActor);
		}

		timers.update(delta);
		textManager.update(delta);
	}

	public void draw(SpriteBatch batch) {

		if (background != null) {
			batch.disableBlending();
			batch.setProjectionMatrix(camera.calculateParallaxMatrix(1, 1));
			batch.begin();

			float x = 0;

			for (AtlasRegion tile : background) {
				batch.draw(tile, x, 0f);
				x += tile.getRegionWidth();
			}

			batch.end();
			batch.enableBlending();
		}

		// draw layers from bottom to top
		for (int i = layers.size() - 1; i >= 0; i--) {
			SceneLayer layer = layers.get(i);

			batch.setProjectionMatrix(camera.calculateParallaxMatrix(layer.getParallaxMultiplier(), 1));
			batch.begin();
			layer.draw(batch);
			batch.end();
		}
	}

	public void drawBBoxLines(ShapeRenderer renderer) {
		// renderer.begin(ShapeType.Rectangle);
		renderer.begin(ShapeType.Line);

		for (BaseActor a : actors.values()) {
			Polygon p = a.getBBox();

			if (p == null) {
				EngineLogger.error("ERROR DRAWING BBOX FOR: " + a.getId());
			}

			if (a instanceof ObstacleActor) {
				renderer.setColor(OBSTACLE_COLOR);
				renderer.polygon(p.getTransformedVertices());
			} else if (a instanceof AnchorActor) {
				renderer.setColor(Scene.ANCHOR_COLOR);
				renderer.line(p.getX() - Scene.ANCHOR_RADIUS, p.getY(), p.getX() + Scene.ANCHOR_RADIUS, p.getY());
				renderer.line(p.getX(), p.getY() - Scene.ANCHOR_RADIUS, p.getX(), p.getY() + Scene.ANCHOR_RADIUS);
			} else {
				renderer.setColor(ACTOR_BBOX_COLOR);
				renderer.polygon(p.getTransformedVertices());
			}

			// Rectangle r = a.getBBox().getBoundingRectangle();
			// renderer.rect(r.getX(), r.getY(), r.getWidth(), r.getHeight());
		}

		if (polygonalNavGraph != null) {
			renderer.setColor(WALKZONE_COLOR);
			renderer.polygon(polygonalNavGraph.getWalkZone().getTransformedVertices());

			// DRAW LINEs OF SIGHT
			renderer.setColor(Color.WHITE);
			ArrayList<NavNodePolygonal> nodes = polygonalNavGraph.getGraphNodes();
			for (NavNodePolygonal n : nodes) {
				for (NavNodePolygonal n2 : n.neighbors) {
					renderer.line(n.x, n.y, n2.x, n2.y);
				}
			}
		}

		renderer.end();
	}

	public BaseActor getActor(String id, boolean searchInventory) {

		if (VAR_PLAYER.equals(id))
			return actors.get(player);

		BaseActor a = id == null ? null : actors.get(id);

		if (a == null && searchInventory) {
			a = w.getInventory().get(id);

			// Search the uiActors
			if (a == null)
				a = w.getUIActors().get(id);
		}

		return a;
	}

	public Map<String, BaseActor> getActors() {
		return actors;
	}

	public void addActor(BaseActor actor) {
		BaseActor prev = actors.put(actor.getId(), actor);

		if (prev != null) {
			EngineLogger.error("Actor '" + actor.getId() + "' already exists in scene '" + id + "'.");
		}

		actor.setScene(this);

		if (actor instanceof InteractiveActor) {
			InteractiveActor ia = (InteractiveActor) actor;

			SceneLayer layer = getLayer(ia.getLayer());

			if (layer == null) { // fallback for compatibility
				layer = new SceneLayer();
				layer.setName(ia.getLayer());
				layers.add(layer);
			}

			layer.add(ia);
		}
	}

	public void setBackground(String bgAtlas, String bgId, String lightMapAtlas, String lightMapId) {
		this.backgroundAtlas = bgAtlas;
		this.backgroundRegionId = bgId;
	}

	/**
	 * Returns the Interactive actor at the position. The actor must have the
	 * interaction property enabled.
	 */
	public InteractiveActor getInteractiveActorAt(float x, float y) {

		for (SceneLayer layer : layers) {

			if (!layer.isVisible())
				continue;

			// Obtain actors in reverse (close to camera)
			for (int i = layer.getActors().size() - 1; i >= 0; i--) {
				BaseActor a = layer.getActors().get(i);

				if (a instanceof InteractiveActor && ((InteractiveActor) a).canInteract() && a.hit(x, y)) {
					return (InteractiveActor) a;
				}
			}
		}

		return null;
	}

	private Rectangle tmpToleranceRect = new Rectangle();

	/**
	 * Obtains the actor at (x,y) with TOLERANCE.
	 * 
	 * Creates a square with size = TOLERANCE and checks:
	 * 
	 * 1. if some vertex from the TOLERANCE square is inside an actor bbox. 2. if
	 * some actor of the actor vertexes is inside the TOLERANCE square.
	 */
	public InteractiveActor getInteractiveActorAt(float x, float y, float tolerance) {
		if (tolerance <= 0) {
			return getInteractiveActorAt(x, y);
		}

		List<SceneLayer> layers = getLayers();

		tmpToleranceRect.x = x - tolerance / 2;
		tmpToleranceRect.y = y - tolerance / 2;
		tmpToleranceRect.width = tolerance;
		tmpToleranceRect.height = tolerance;

		for (SceneLayer layer : layers) {

			if (!layer.isVisible())
				continue;

			// Obtain actors in reverse (close to camera)
			for (int l = layer.getActors().size() - 1; l >= 0; l--) {
				BaseActor a = layer.getActors().get(l);

				if (a instanceof InteractiveActor && ((InteractiveActor) a).canInteract()) {

					if (a.hit(x, y) || a.hit(tmpToleranceRect.x, tmpToleranceRect.y)
							|| a.hit(tmpToleranceRect.x + tmpToleranceRect.width, tmpToleranceRect.y)
							|| a.hit(tmpToleranceRect.x, tmpToleranceRect.y + tmpToleranceRect.height)
							|| a.hit(tmpToleranceRect.x + tmpToleranceRect.width,
									tmpToleranceRect.y + tmpToleranceRect.height))
						return (InteractiveActor) a;

					float[] verts = a.getBBox().getTransformedVertices();
					for (int i = 0; i < verts.length; i += 2) {
						float vx = verts[i];
						float vy = verts[i + 1];

						if (tmpToleranceRect.contains(vx, vy))
							return (InteractiveActor) a;
					}
				}
			}

		}

		return null;
	}

	/**
	 * Returns the actor at the position. Including not interactive actors.
	 */
	public BaseActor getActorAt(float x, float y) {

		// 1. Search for ANCHOR Actors
		for (BaseActor a : actors.values()) {
			if (a instanceof AnchorActor) {
				float dst = Vector2.dst(x, y, a.getX(), a.getY());

				if (dst < ANCHOR_RADIUS)
					return a;
			}
		}

		// 2. Search for INTERACTIVE Actors
		for (SceneLayer layer : layers) {

			if (!layer.isVisible())
				continue;

			// Obtain actors in reverse (close to camera)
			for (int i = layer.getActors().size() - 1; i >= 0; i--) {
				BaseActor a = layer.getActors().get(i);

				if (a.hit(x, y)) {
					return a;
				}
			}
		}

		// 3. Search for OBSTACLE actors
		for (BaseActor a : actors.values()) {
			if (a instanceof ObstacleActor && a.hit(x, y)) {
				return a;
			}
		}

		return null;
	}

	public void setPlayer(CharacterActor a) {
		if (a != null) {
			player = a.getId();
			a.setInteraction(false);
		} else {
			player = null;
		}
	}

	public CharacterActor getPlayer() {
		if (player == null)
			return null;

		return (CharacterActor) actors.get(player);
	}

	public Vector2 getDepthVector() {
		return depthVector;
	}

	public void setDepthVector(Vector2 v) {
		depthVector = v;
	}

	public String getBackgroundAtlas() {
		return backgroundAtlas;
	}

	public void setBackgroundAtlas(String backgroundAtlas) {
		this.backgroundAtlas = backgroundAtlas;
	}

	public String getBackgroundRegionId() {
		return backgroundRegionId;
	}

	public void setBackgroundRegionId(String backgroundRegionId) {
		this.backgroundRegionId = backgroundRegionId;
	}

	public void removeActor(BaseActor a) {

		if (player != null && a.getId().equals(player)) {
			player = null;
		}

		BaseActor r = actors.remove(a.getId());

		if (r == null) {
			EngineLogger.error("Removing actor from scene: Actor not found");
			return;
		}

		if (a instanceof InteractiveActor) {
			InteractiveActor ia = (InteractiveActor) a;
			SceneLayer layer = getLayer(ia.getLayer());
			layer.getActors().remove(ia);
		}

		if (a instanceof ObstacleActor && polygonalNavGraph != null)
			polygonalNavGraph.removeDinamicObstacle(a.getBBox());

		a.setScene(null);

	}

	public Array<AtlasRegion> getBackground() {
		return background;
	}

	public SceneCamera getCamera() {
		return camera;
	}

	public void resetCamera(float worldWidth, float worldHeight) {
		camera.create(worldWidth, worldHeight);

		if (getPlayer() != null)
			setCameraFollowActor(getPlayer());
	}

	public void setCameraFollowActor(SpriteActor a) {
		followActor = a;

		if (a != null)
			camera.updatePos(a);
	}

	public SpriteActor getCameraFollowActor() {
		return followActor;
	}

	public SceneSoundManager getSoundManager() {
		return soundManager;
	}

	@Override
	public void loadAssets() {

		soundManager.loadAssets();
		textManager.getVoiceManager().loadAssets();

		if (backgroundAtlas != null && !backgroundAtlas.isEmpty()) {
			EngineAssetManager.getInstance().loadAtlas(backgroundAtlas);
		}

		for (BaseActor a : actors.values()) {
			if (a instanceof AssetConsumer)
				((AssetConsumer) a).loadAssets();
		}

		// CALC WALK GRAPH
		if (polygonalNavGraph != null) {
			polygonalNavGraph.createInitialGraph(actors.values());
		}
	}

	@Override
	public void retrieveAssets() {

		// RETRIEVE BACKGROUND
		if (backgroundAtlas != null && !backgroundAtlas.isEmpty()) {
			background = EngineAssetManager.getInstance().getRegions(backgroundAtlas, backgroundRegionId);

			int width = 0;

			for (int i = 0; i < background.size; i++) {
				width += background.get(i).getRegionWidth();
			}

			int height = background.get(0).getRegionHeight();

			// Sets the scrolling dimensions. It must be done here because
			// the background must be loaded to calculate the bbox
			if (sceneSize == null)
				camera.setScrollingDimensions(width, height);

			// if(followActor != null)
			// camera.updatePos(followActor);
		}

		if (sceneSize != null) {
			float scale = EngineAssetManager.getInstance().getScale();
			camera.setScrollingDimensions(sceneSize.x * scale, sceneSize.y * scale);
		}

		// RETRIEVE ACTORS
		for (BaseActor a : actors.values()) {
			if (a instanceof AssetConsumer)
				((AssetConsumer) a).retrieveAssets();
		}

		soundManager.retrieveAssets();
		textManager.getVoiceManager().retrieveAssets();
	}

	@Override
	public void dispose() {

		if (backgroundAtlas != null && !backgroundAtlas.isEmpty()) {
			EngineAssetManager.getInstance().disposeAtlas(backgroundAtlas);
		}

		// orderedActors.clear();

		for (BaseActor a : actors.values()) {
			if (a instanceof AssetConsumer)
				((AssetConsumer) a).dispose();
		}

		soundManager.dispose();
		getTextManager().getVoiceManager().dispose();
	}

	public Vector2 getSceneSize() {
		return sceneSize;
	}

	public void setSceneSize(Vector2 sceneSize) {
		this.sceneSize = sceneSize;
	}

	public void orderLayersByZIndex() {
		for (SceneLayer l : layers) {
			l.orderByZIndex();
		}
	}

	public PolygonalNavGraph getPolygonalNavGraph() {
		return polygonalNavGraph;
	}

	public void setPolygonalNavGraph(PolygonalNavGraph polygonalNavGraph) {
		this.polygonalNavGraph = polygonalNavGraph;
	}
}
