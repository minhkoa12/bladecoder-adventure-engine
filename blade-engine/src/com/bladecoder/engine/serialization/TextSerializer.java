package com.bladecoder.engine.serialization;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.bladecoder.engine.model.Text;
import com.bladecoder.engine.model.Text.Type;
import com.bladecoder.engine.model.World;

public class TextSerializer {

	static public void write(World w, Text t, Json json) {
		json.writeValue("str", t.str);
		json.writeValue("x", t.x);
		json.writeValue("y", t.y);
		json.writeValue("time",t.time);
		json.writeValue("type", t.type);
		json.writeValue("color", t.color);
		json.writeValue("style", t.style);
		json.writeValue("actorId", t.actorId);
		json.writeValue("voiceId", t.voiceId);
		json.writeValue("cb", ActionCallbackSerialization.find(w, t.cb), t.cb == null ? null
				: String.class);
	}


	static public void read(World w, Text t, Json json, JsonValue jsonData) {
		t.str = json.readValue("str", String.class, jsonData);
		t.x = json.readValue("x", Float.class, jsonData);
		t.y = json.readValue("y", Float.class, jsonData);
		t.time = json.readValue("time", Float.class, jsonData);
		t.type = json.readValue("type", Type.class, jsonData);
		t.color = json.readValue("color", Color.class, jsonData);
		t.style = json.readValue("style", String.class, jsonData);
		t.actorId = json.readValue("actorId", String.class, jsonData);
		t.voiceId = json.readValue("voiceId", String.class, jsonData);
		String cbSer = json.readValue("cb", String.class, jsonData);
		if(cbSer != null)
			t.cb = ActionCallbackSerialization.find(w, cbSer);
	}
}
