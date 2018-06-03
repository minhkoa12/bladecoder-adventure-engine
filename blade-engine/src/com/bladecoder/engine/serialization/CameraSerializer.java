package com.bladecoder.engine.serialization;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.bladecoder.engine.anim.CameraTween;
import com.bladecoder.engine.assets.EngineAssetManager;
import com.bladecoder.engine.model.SceneCamera;
import com.bladecoder.engine.model.World;

public class CameraSerializer {

	static public void write(World w, SceneCamera cam, Json json) {
		float worldScale = EngineAssetManager.getInstance().getScale();
	
		json.writeValue("width", cam.viewportWidth / worldScale);
		json.writeValue("height", cam.viewportHeight / worldScale);
		json.writeValue("scrollingWidth", cam.getScrollingWidth() / worldScale);
		json.writeValue("scrollingHeight", cam.getScrollingHeight() / worldScale);
		
		Vector2 p = cam.getPosition();
		p.x = p.x/worldScale;
		p.y = p.y/worldScale;
		json.writeValue("pos", p);
		json.writeValue("zoom", cam.getZoom());
		
		if(cam.cameraTween != null) {
			json.writeObjectStart("cameraTween");
			TweenSerializer.write(w, cam.cameraTween, json);
			json.writeObjectEnd();
		}
	}


	static public void read(World w, SceneCamera cam, Json json, JsonValue jsonData) {
		float worldScale = EngineAssetManager.getInstance().getScale();
		
		cam.viewportWidth = json.readValue("width", Float.class, jsonData) * worldScale;
		cam.viewportHeight = json.readValue("height", Float.class, jsonData) * worldScale;
		
		float scrollingWidth = json.readValue("scrollingWidth", Float.class, jsonData) * worldScale;
		float scrollingHeight = json.readValue("scrollingHeight", Float.class, jsonData) * worldScale;
		cam.setScrollingDimensions(scrollingWidth, scrollingHeight);
		
		Vector2 pos = json.readValue("pos", Vector2.class, jsonData);
		pos.x *=  worldScale;
		pos.y *=  worldScale;
		float z = json.readValue("zoom", Float.class, jsonData);
		
		cam.create(cam.viewportWidth, cam.viewportHeight);
		cam.zoom = z;
		cam.position.set(pos.x, pos.y, 0);
		cam.update();

		if(jsonData.get("cameraTween") != null) {
			CameraTween t = new CameraTween();
			TweenSerializer.read(w, t, json, jsonData.get("cameraTween"));
			cam.cameraTween.setTarget(cam);
		}
	}
}
