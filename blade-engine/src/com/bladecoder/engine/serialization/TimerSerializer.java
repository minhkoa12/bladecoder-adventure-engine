package com.bladecoder.engine.serialization;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.bladecoder.engine.anim.Timers.Timer;
import com.bladecoder.engine.model.World;

public class TimerSerializer {

	static public void write(World w, Timer t, Json json) {
		json.writeValue("time", t.time);
		json.writeValue("currentTime", t.currentTime);
		json.writeValue("cb", ActionCallbackSerialization.find(w, t.cb), t.cb == null ? null : String.class);
	}


	static public void read(World w, Timer t, Json json, JsonValue jsonData) {
		t.time = json.readValue("time", Float.class, jsonData);
		t.currentTime = json.readValue("currentTime", Float.class, jsonData);
		String cbSer = json.readValue("cb", String.class, jsonData);
		t.cb = ActionCallbackSerialization.find(w, cbSer);
	}
}
