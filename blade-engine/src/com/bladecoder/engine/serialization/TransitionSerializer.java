package com.bladecoder.engine.serialization;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.bladecoder.engine.actions.ActionCallback;
import com.bladecoder.engine.model.Transition;
import com.bladecoder.engine.model.Transition.Type;
import com.bladecoder.engine.model.World;

public class TransitionSerializer {

	static public void write(World w, Transition t, Json json) {
		json.writeValue("currentTime", t.getCurrentTime());
		json.writeValue("time", t.getTime());
		json.writeValue("color", t.getColor());
		json.writeValue("type", t.getType());
		json.writeValue("cb", ActionCallbackSerialization.find(w, t.getCb()), t.getCb() == null ? null : String.class);
	}


	static public void read(World w, Transition t, Json json, JsonValue jsonData) {
		t.setCurrentTime(json.readValue("currentTime", Float.class, jsonData));
		t.setTime( json.readValue("time", Float.class, jsonData));
		t.setColor(json.readValue("color", Color.class, jsonData));
		t.setType(json.readValue("type", Type.class, jsonData));
		String cbSer = json.readValue("cb", String.class, jsonData);
		
		ActionCallback cb = null;
		
		if (cbSer != null)
			cb = ActionCallbackSerialization.find(w, cbSer);

		t.setCb(cb);
	}
}
