package com.bladecoder.engine.serialization;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.bladecoder.engine.anim.MusicVolumeTween;
import com.bladecoder.engine.model.MusicDesc;
import com.bladecoder.engine.model.MusicManager;
import com.bladecoder.engine.model.World;

public class MusicManagerSerializer {

	static public void write(World w, MusicManager mm, Json json) {
		json.writeValue("desc", mm.desc);
		json.writeValue("currentMusicDelay", mm.currentMusicDelay);
		json.writeValue("isPlaying", mm.music != null && (mm.music.isPlaying() || mm.isPaused));
		json.writeValue("musicPos",
				mm.music != null && (mm.music.isPlaying() || mm.isPaused) ? mm.music.getPosition() : 0f);

		if (mm.volumeTween != null) {
			json.writeObjectStart("volumeTween");
			TweenSerializer.write(w, mm.volumeTween, json);
			json.writeObjectEnd();
		}
	}

	static public void read(World w, MusicManager mm, Json json, JsonValue jsonData) {
		mm.desc = json.readValue("desc", MusicDesc.class, jsonData);
		mm.currentMusicDelay = json.readValue("currentMusicDelay", float.class, jsonData);
		mm.isPlayingSer = json.readValue("isPlaying", boolean.class, jsonData);
		mm.musicPosSer = json.readValue("musicPos", float.class, jsonData);

		if(jsonData.get("volumeTween") != null) {
			MusicVolumeTween t = new MusicVolumeTween();
			TweenSerializer.read(w, t, json, jsonData.get("volumeTween"));
			mm.volumeTween.setTarget(mm);
		}
	}
}
