package com.bladecoder.engine.serialization;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.bladecoder.engine.actions.SceneActorRef;
import com.bladecoder.engine.model.BaseActor;
import com.bladecoder.engine.model.Inventory;
import com.bladecoder.engine.model.Scene;
import com.bladecoder.engine.model.SpriteActor;
import com.bladecoder.engine.model.World;

public class InventorySerializer {

	static public void write(Inventory inv, Json json) {
		SceneActorRef actorRef;
		
		json.writeValue("visible", inv.isVisible());

		json.writeObjectStart("items");
		for (SpriteActor a : inv.getItems()) {
			actorRef = new SceneActorRef(a.getInitScene(), a.getId());
			json.writeValue(actorRef.toString(), a);
		}
		json.writeObjectEnd();
	}


	static public void read(World w, Inventory inv, Json json, JsonValue jsonData) {
		Boolean v = json.readValue("visible", Boolean.class, jsonData);
		
		inv.setVisible(v);
		
		inv.getItems().clear();
		
		JsonValue jsonValueActors = jsonData.get("items");
		SceneActorRef actorRef;

		// GET ACTORS FROM HIS INIT SCENE.
		for (int i = 0; i < jsonValueActors.size; i++) {
			JsonValue jsonValueAct = jsonValueActors.get(i);
			actorRef = new SceneActorRef(jsonValueAct.name);
			Scene sourceScn = w.getScene(actorRef.getSceneId());

			BaseActor actor = sourceScn.getActor(actorRef.getActorId(), false);
			sourceScn.removeActor(actor);
			inv.addItem((SpriteActor)actor);
		}
		
		// READ ACTOR STATE. 
		// The state must be retrieved after getting actors from his init scene to restore verb cb properly.
		for (int i = 0; i < jsonValueActors.size; i++) {
			JsonValue jsonValueAct = jsonValueActors.get(i);
			actorRef = new SceneActorRef(jsonValueAct.name);

			SpriteActor actor = inv.getItems().get(i);
			actor.read(json, jsonValueAct);
		}
	}
}
