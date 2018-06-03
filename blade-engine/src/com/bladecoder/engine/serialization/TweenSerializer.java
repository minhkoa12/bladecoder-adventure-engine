package com.bladecoder.engine.serialization;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.bladecoder.engine.anim.CameraTween;
import com.bladecoder.engine.anim.MusicVolumeTween;
import com.bladecoder.engine.anim.SpriteAlphaTween;
import com.bladecoder.engine.anim.SpritePosTween;
import com.bladecoder.engine.anim.SpriteRotateTween;
import com.bladecoder.engine.anim.SpriteScaleTween;
import com.bladecoder.engine.anim.SpriteTintTween;
import com.bladecoder.engine.anim.Tween;
import com.bladecoder.engine.anim.Tween.Type;
import com.bladecoder.engine.anim.WalkTween;
import com.bladecoder.engine.model.World;
import com.bladecoder.engine.util.InterpolationMode;

public class TweenSerializer {

	static public void write(World w, Tween<?> t, Json json) {
		json.writeValue("duration", t.getDuration());
		json.writeValue("time", t.getTime());
		json.writeValue("reverse", t.isReverse());
		json.writeValue("began", t.isBegan());
		json.writeValue("complete", t.isComplete());
		json.writeValue("type", t.getType());
		json.writeValue("count", t.getCount());

		json.writeValue("interpolation", t.getInterpolation());
		json.writeValue("cb", ActionCallbackSerialization.find(w, t.getCb()), t.getCb() == null ? null : String.class);

		if (t instanceof WalkTween) {
			WalkTween wt = (WalkTween)t;
			
			json.writeValue("path", wt.walkingPath);
			json.writeValue("currentStep", wt.currentStep);
			json.writeValue("speed", wt.speed);

			json.writeValue("walkCb", ActionCallbackSerialization.find(w, wt.walkCb), wt.walkCb == null ? null : String.class);
		} else if (t instanceof CameraTween) {
			CameraTween ct = (CameraTween)t;
			
			json.writeValue("startX", ct.startX);
			json.writeValue("startY", ct.startY);
			json.writeValue("startZoom", ct.startZoom);
			json.writeValue("targetX", ct.targetX);
			json.writeValue("targetY", ct.targetY);
			json.writeValue("targetZoom", ct.targetZoom);
		} else if (t instanceof MusicVolumeTween) {
			MusicVolumeTween mvt = (MusicVolumeTween)t;
			
			json.writeValue("startVolume", mvt.startVolume);
			json.writeValue("targetVolume", mvt.targetVolume);
		} else if (t instanceof SpriteAlphaTween) {
			SpriteAlphaTween t2 = (SpriteAlphaTween)t;
			
			json.writeValue("startAlpha", t2.startAlpha);
			json.writeValue("targetAlpha", t2.targetAlpha);
		} else if (t instanceof SpritePosTween) {
			SpritePosTween t2 = (SpritePosTween)t;
			
			json.writeValue("startX", t2.startX);
			json.writeValue("startY", t2.startY);
			json.writeValue("targetX", t2.targetX);
			json.writeValue("targetY", t2.targetY);
			json.writeValue("interpolationX", t2.interpolationX);
			json.writeValue("interpolationY", t2.interpolationY);
		} else if (t instanceof SpriteRotateTween) {
			SpriteRotateTween t2 = (SpriteRotateTween)t;
			
			json.writeValue("startRot", t2.startRot);
			json.writeValue("targetRot", t2.targetRot);
		} else if (t instanceof SpriteScaleTween) {
			SpriteScaleTween t2 = (SpriteScaleTween)t;
			
			json.writeValue("startScl", t2.startScl);
			json.writeValue("targetScl", t2.targetScl);
		} else if (t instanceof SpriteTintTween) {
			SpriteTintTween t2 = (SpriteTintTween)t;
			
			json.writeValue("startColor", t2.startColor);
			json.writeValue("targetColor", t2.targetColor);
		}
	}

	@SuppressWarnings("unchecked")
	static public void read(World w, Tween<?> t, Json json, JsonValue jsonData) {
		t.setDuration(json.readValue("duration", Float.class, jsonData));
		t.setTime(json.readValue("time", Float.class, jsonData));

		t.setReverse(json.readValue("reverse", Boolean.class, jsonData));
		t.setBegan(json.readValue("began", Boolean.class, jsonData));
		t.setComplete(json.readValue("complete", Boolean.class, jsonData));
		t.setType(json.readValue("type", Type.class, jsonData));
		t.setCount(json.readValue("count", Integer.class, jsonData));

		t.setInterpolation(json.readValue("interpolation", InterpolationMode.class, jsonData));

		String cbSer = json.readValue("cb", String.class, jsonData);
		t.setCb(ActionCallbackSerialization.find(w, cbSer));

		if (t instanceof WalkTween) {
			WalkTween wt = (WalkTween)t;
			
			wt.walkingPath = json.readValue("path", ArrayList.class, Vector2.class, jsonData);
			wt.currentStep = json.readValue("currentStep", Integer.class, jsonData);
			wt.speed = json.readValue("speed", Float.class, jsonData);

			String walkCbSer = json.readValue("walkCb", String.class, jsonData);
			wt.walkCb = ActionCallbackSerialization.find(w, walkCbSer);
		} else if (t instanceof CameraTween) {
			CameraTween ct = (CameraTween)t;
			
			ct.startX = json.readValue("startX", Float.class, jsonData);
			ct.startY = json.readValue("startY", Float.class, jsonData);
			ct.startZoom = json.readValue("startZoom", Float.class, jsonData);
			ct.targetX = json.readValue("targetX", Float.class, jsonData);
			ct.targetY = json.readValue("targetY", Float.class, jsonData);
			ct.targetZoom = json.readValue("targetZoom", Float.class, jsonData);
		} else if (t instanceof MusicVolumeTween) {
			MusicVolumeTween mvt = (MusicVolumeTween)t;
			
			mvt.startVolume = json.readValue("startVolume", Float.class, 1f, jsonData);
			mvt.targetVolume = json.readValue("targetVolume", Float.class, 1f, jsonData);
		} else if (t instanceof SpriteAlphaTween) {
			SpriteAlphaTween t2 = (SpriteAlphaTween)t;
			
			t2.startAlpha = json.readValue("startAlpha", float.class, 1.0f, jsonData);
			t2.targetAlpha = json.readValue("targetAlpha", float.class, 1.0f, jsonData);
		} else if (t instanceof SpritePosTween) {
			SpritePosTween t2 = (SpritePosTween)t;
			
			t2.startX = json.readValue("startX", Float.class, jsonData);
			t2.startY = json.readValue("startY", Float.class, jsonData);
			t2.targetX = json.readValue("targetX", Float.class, jsonData);
			t2.targetY = json.readValue("targetY", Float.class, jsonData);
			t2.interpolationX = json.readValue("interpolationX", InterpolationMode.class, jsonData);
			t2.interpolationY = json.readValue("interpolationY", InterpolationMode.class, jsonData);

		} else if (t instanceof SpriteRotateTween) {
			SpriteRotateTween t2 = (SpriteRotateTween)t;
			
			t2.startRot = json.readValue("startRot", Float.class, jsonData);
			t2.targetRot = json.readValue("targetRot", Float.class, jsonData);
		} else if (t instanceof SpriteScaleTween) {
			SpriteScaleTween t2 = (SpriteScaleTween)t;
			
			t2.startScl = json.readValue("startScl", Float.class, jsonData);
			t2.targetScl = json.readValue("targetScl", Float.class, jsonData);
		} else if (t instanceof SpriteTintTween) {
			SpriteTintTween t2 = (SpriteTintTween)t;
			
			t2.startColor = json.readValue("startColor", Color.class, Color.WHITE, jsonData);
			t2.targetColor = json.readValue("targetColor", Color.class, Color.WHITE, jsonData);
		}
	}
}
