package com.bladecoder.engine.serialization;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.bladecoder.engine.actions.SceneActorRef;
import com.bladecoder.engine.anim.Timers.Timer;
import com.bladecoder.engine.model.BaseActor;
import com.bladecoder.engine.model.CharacterActor;
import com.bladecoder.engine.model.InteractiveActor;
import com.bladecoder.engine.model.MusicDesc;
import com.bladecoder.engine.model.Scene;
import com.bladecoder.engine.model.SceneLayer;
import com.bladecoder.engine.model.SpriteActor;
import com.bladecoder.engine.model.World;
import com.bladecoder.engine.polygonalpathfinder.PolygonalNavGraph;
import com.bladecoder.engine.serialization.SerializationHelper.Mode;
import com.bladecoder.engine.util.EngineLogger;

public class SceneSerializer {

	static public void write(World w, Scene s, Json json) {
		if (SerializationHelper.getInstance().getMode() == Mode.MODEL) {

			json.writeValue("id", s.getId());
			json.writeValue("layers", s.getLayers(), s.getLayers().getClass(), SceneLayer.class);

			json.writeValue("actors", s.getActors());

			if (s.getBackgroundAtlas() != null) {
				json.writeValue("backgroundAtlas", s.getBackgroundAtlas());
				json.writeValue("backgroundRegionId", s.getBackgroundRegionId());
			}

			json.writeValue("musicDesc", s.getMusicDesc());

			if (s.getDepthVector() != null)
				json.writeValue("depthVector", s.getDepthVector());

			if (s.getPolygonalNavGraph() != null)
				json.writeValue("polygonalNavGraph", s.getPolygonalNavGraph());

			if (s.getSceneSize() != null)
				json.writeValue("sceneSize", s.getSceneSize());

		} else {
			SceneActorRef actorRef;

			json.writeObjectStart("actors");
			for (BaseActor a : s.getActors().values()) {
				actorRef = new SceneActorRef(a.getInitScene(), a.getId());
				json.writeValue(actorRef.toString(), a);
			}
			json.writeObjectEnd();
			
			json.writeObjectStart("camera");
				CameraSerializer.write(w, s.getCamera(), json);
			json.writeObjectEnd();

			if (s.getCameraFollowActor() != null)
				json.writeValue("followActor", s.getCameraFollowActor().getId());

			s.getSoundManager().write(json);

			if (!s.getTimers().isEmpty()) {
				
				json.writeArrayStart("timers");
				for (Timer t : s.getTimers().getTimers()) {
					TimerSerializer.write(w, t, json);
				}
				json.writeArrayEnd();
			}

			if (s.getTextManager().getCurrentText() != null)
				json.writeValue("textmanager", s.getTextManager());
		}

		s.getVerbManager().write(json);

		if (s.getState() != null)
			json.writeValue("state", s.getState());

		if (s.getPlayer() != null)
			json.writeValue("player", s.getPlayer().getId());
	}

	@SuppressWarnings("unchecked")
	static public void read(World w, Scene s, Json json, JsonValue jsonData) {
		if (SerializationHelper.getInstance().getMode() == Mode.MODEL) {

			s.setId(json.readValue("id", String.class, jsonData));
			s.getLayers().addAll(json.readValue("layers", ArrayList.class, SceneLayer.class, jsonData));
			
			// ACTORS
			JsonValue jsonActors = jsonData.get("actors");
			Map<String, BaseActor> actors = s.getActors();

			for (int i = 0; i < jsonActors.size; i++) {
				JsonValue jsonValue = jsonActors.get(i);
				String clazz = jsonValue.getString("class");
				
				Class<?> c;
				try {
					c = Class.forName(clazz);
					BaseActor a = (BaseActor) ClassReflection.newInstance(c);
					
					a.setScene(s);
					a.setInitScene(s.getId());
					
					a.read(json, jsonValue);

					if (a instanceof InteractiveActor) {
						InteractiveActor ia = (InteractiveActor) a;

						SceneLayer layer = s.getLayer(ia.getLayer());
						layer.add(ia);
					}
					
					actors.put(jsonValue.name, a);
				} catch (ClassNotFoundException | ReflectionException e) {
					EngineLogger.error("Error loading class from actor: " + jsonValue.name);
					continue;
				}
			}

			s.orderLayersByZIndex();

			s.setBackgroundAtlas(json.readValue("backgroundAtlas", String.class, jsonData));
			s.setBackgroundRegionId(json.readValue("backgroundRegionId", String.class, jsonData));

			s.setMusicDesc(json.readValue("musicDesc", MusicDesc.class, jsonData));

			s.setDepthVector(json.readValue("depthVector", Vector2.class, jsonData));

			s.setPolygonalNavGraph(json.readValue("polygonalNavGraph", PolygonalNavGraph.class, jsonData));

			s.setSceneSize(json.readValue("sceneSize", Vector2.class, jsonData));

		} else {
			JsonValue jsonValueActors = jsonData.get("actors");
			SceneActorRef actorRef;

			// GET ACTORS FROM HIS INIT SCENE AND MOVE IT TO THE LOADING SCENE.
			for (int i = 0; i < jsonValueActors.size; i++) {
				JsonValue jsonValueAct = jsonValueActors.get(i);
				actorRef = new SceneActorRef(jsonValueAct.name);
				Scene sourceScn = w.getScene(actorRef.getSceneId());

				if (sourceScn != s) {
					BaseActor actor = sourceScn.getActor(actorRef.getActorId(), false);
					sourceScn.removeActor(actor);
					s.addActor(actor);
				}
			}

			// READ ACTOR STATE.
			// The state must be retrieved after getting actors from his init
			// scene to restore verb cb properly.
			for (int i = 0; i < jsonValueActors.size; i++) {
				JsonValue jsonValueAct = jsonValueActors.get(i);
				actorRef = new SceneActorRef(jsonValueAct.name);

				BaseActor actor = s.getActor(actorRef.getActorId(), false);

				if (actor != null)
					actor.read(json, jsonValueAct);
				else
					EngineLogger.debug("Actor not found: " + actorRef);
			}

			s.orderLayersByZIndex();

			CameraSerializer.read(w, s.getCamera(), json, jsonData.get("camera"));
			String followActorId = json.readValue("followActor", String.class, jsonData);

			if (s.getCameraFollowActor() != null)
				s.setCameraFollowActor((SpriteActor) s.getActors().get(followActorId));

			s.getSoundManager().read(json, jsonData);

			// TIMERS
			if (jsonData.get("timers") != null) {
				List<Timer> timers = s.getTimers().getTimers();
				JsonValue jsonValueTimers = jsonData.get("timers");
				for (int i = 0; i < jsonValueTimers.size; i++) {
					JsonValue jsonValueAct = jsonValueTimers.get(i);
					Timer timer = new Timer();
					TimerSerializer.read(w, timer, json, jsonValueAct);
					timers.add(timer);
				}
			}

			if (jsonData.get("textmanager") != null) {
				s.getTextManager().read(json, jsonData);
			}
		}

		s.getVerbManager().read(json, jsonData);
		s.setState(json.readValue("state", String.class, jsonData));
		s.setPlayer((CharacterActor) s.getActor(json.readValue("player", String.class, jsonData), false));

	}
}
