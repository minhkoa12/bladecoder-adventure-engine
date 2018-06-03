package com.bladecoder.engine.serialization;

import java.util.HashMap;

import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.bladecoder.engine.model.AnimationRenderer;
import com.bladecoder.engine.model.World;
import com.bladecoder.engine.serialization.SerializationHelper.Mode;

public class AnimationRendererSerializer {

	static public void write(World w, AnimationRenderer r, Json json) {
		
		if (SerializationHelper.getInstance().getMode() == Mode.MODEL) {

			json.writeValue("fanims", r.fanims, HashMap.class, null);
			json.writeValue("initAnimation", r.getInitAnimation());
			json.writeValue("orgAlign", r.getOrgAlign());

		} else {

			String currentAnimationId = null;

			if (r.currentAnimation != null)
				currentAnimationId = r.currentAnimation.id;

			json.writeValue("currentAnimation", currentAnimationId);

			json.writeValue("flipX", r.flipX);
		}		
	}

	static public void read(World w, AnimationRenderer r, Json json, JsonValue jsonData) {
		if (SerializationHelper.getInstance().getMode() == Mode.MODEL) {

			// In next versions, the fanims loading will be generic
			// fanims = json.readValue("fanims", HashMap.class, AnimationDesc.class, jsonData);
			
			r.setInitAnimation( json.readValue("initAnimation", String.class, jsonData));
			r.setOrgAlign( json.readValue("orgAlign", int.class, Align.bottom, jsonData));
		} else {

			String currentAnimationId = json.readValue("currentAnimation", String.class, jsonData);

			if (currentAnimationId != null)
				r.currentAnimation = r.fanims.get(currentAnimationId);
			r.flipX = json.readValue("flipX", Boolean.class, jsonData);
		}
	}
}
