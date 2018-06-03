package com.bladecoder.engine.serialization;

import java.util.HashMap;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Json.Serializable;
import com.badlogic.gdx.utils.JsonValue;
import com.bladecoder.engine.anim.AnimationDesc;
import com.bladecoder.engine.anim.AtlasAnimationDesc;
import com.bladecoder.engine.anim.FATween;
import com.bladecoder.engine.anim.Tween;
import com.bladecoder.engine.assets.EngineAssetManager;
import com.bladecoder.engine.model.ActorRenderer;
import com.bladecoder.engine.model.AnimationRenderer;
import com.bladecoder.engine.model.AtlasRenderer;
import com.bladecoder.engine.model.ImageRenderer;
import com.bladecoder.engine.model.ParticleRenderer;
import com.bladecoder.engine.model.Sprite3DRenderer;
import com.bladecoder.engine.model.TextRenderer;
import com.bladecoder.engine.model.World;
import com.bladecoder.engine.serialization.SerializationHelper.Mode;

public class RendererSerializer {

	static public void write(World w, ActorRenderer r, Json json) {
		
		if(r instanceof AnimationRenderer)
			AnimationRendererSerializer.write(w, (AnimationRenderer)r, json);
		
		
		if (r instanceof AtlasRenderer) {
			AtlasRenderer r2 = (AtlasRenderer)r;
			
			if (SerializationHelper.getInstance().getMode() == Mode.MODEL) {

			} else {
				json.writeValue("currentFrameIndex", r2.currentFrameIndex);
				
				if(r2.faTween != null) {
					json.writeObjectStart("faTween");
					TweenSerializer.write(w, r2.faTween, json);
					json.writeObjectEnd();
				}
			}
			
		} else 	if (r instanceof ParticleRenderer) {
			ParticleRenderer r2 = (ParticleRenderer)r;
			
			if (SerializationHelper.getInstance().getMode() == Mode.MODEL) {
				json.writeValue("atlasName", r2.getAtlasName());
				json.writeValue("particleName",r2. getParticleName());
				json.writeValue("orgAlign", r2.getOrgAlign());
			} else {		
				json.writeValue("lastAnimationTime", r2.lastAnimationTime);
			}
		} else 	if (r instanceof Sprite3DRenderer) {
			Sprite3DRenderer r2 = (Sprite3DRenderer)r;
			
			if (SerializationHelper.getInstance().getMode() == Mode.MODEL) {
				float worldScale = EngineAssetManager.getInstance().getScale();
				json.writeValue("width", r2.getWidth() / worldScale);
				json.writeValue("height", r2.getHeight() / worldScale);
				json.writeValue("cameraPos", r2.cameraPos, r2.cameraPos == null ? null : Vector3.class);
				json.writeValue("cameraRot", r2.cameraRot, r2.cameraRot == null ? null : Vector3.class);
				json.writeValue("cameraName", r2.getCameraName(), r2.getCameraName() == null ? null : String.class);
				json.writeValue("cameraFOV", r2.getCameraFOV());
				json.writeValue("renderShadow", r2.renderShadow);
			} else {


				json.writeValue("animationCb", ActionCallbackSerialization.find(w, r2.animationCb));

				json.writeValue("currentCount", r2.currentCount);
				json.writeValue("currentAnimationType", r2.currentAnimationType);
				json.writeValue("lastAnimationTime", r2.lastAnimationTime);

				// TODO: SAVE AND RESTORE CURRENT DIRECTION
				// TODO: shadowlight, cel light
			}

			json.writeValue("modelRotation", r2.modelRotation);
			
		} else 	if (r instanceof TextRenderer) {
			TextRenderer r2 = (TextRenderer)r;
			
			if (SerializationHelper.getInstance().getMode() == Mode.MODEL) {
				json.writeValue("text", r2.getText());
				json.writeValue("fontName", r2.getFontName());
				json.writeValue("fontSize", r2.getFontSize());
				json.writeValue("borderWidth", r2.getBorderWidth());
				json.writeValue("borderColor", r2.getBorderColor());
				json.writeValue("borderStraight", r2.isBorderStraight());
				json.writeValue("shadowOffsetX", r2.getShadowOffsetX());
				json.writeValue("shadowOffsetY", r2.getShadowOffsetY());
				json.writeValue("shadowColor", r2.getShadowColor());
				json.writeValue("align", r2.getAlign());
				json.writeValue("orgAlign", r2.getOrgAlign());
			}
			
		} else 	if (r instanceof Serializable) {
			((Serializable)r).write(json);
		}
		
		
	}

	@SuppressWarnings("unchecked")
	static public void read(World w, ActorRenderer r, Json json, JsonValue jsonData) {
		
		if(r instanceof AnimationRenderer)
			AnimationRendererSerializer.read(w, (AnimationRenderer)r, json, jsonData);
		
		if (r instanceof AtlasRenderer) {
			AtlasRenderer r2 = (AtlasRenderer)r;
			
			if (SerializationHelper.getInstance().getMode() == Mode.MODEL) {
				r2.fanims = json.readValue("fanims", HashMap.class, AtlasAnimationDesc.class, jsonData);
			} else {

				r2.currentFrameIndex = json.readValue("currentFrameIndex", Integer.class, jsonData);

				if(jsonData.get("faTween") != null) {
					FATween t = new FATween();
					TweenSerializer.read(w, t, json, jsonData.get("faTween"));
					t.setTarget(r2);
				}
			}
			
		} else 	if (r instanceof ImageRenderer) {
			ImageRenderer r2 = (ImageRenderer)r;
			
			if (SerializationHelper.getInstance().getMode() == Mode.MODEL) {
				r2.fanims = json.readValue("fanims", HashMap.class, AnimationDesc.class, jsonData);
			} else {

			}
		} else 	if (r instanceof ParticleRenderer) {
			ParticleRenderer r2 = (ParticleRenderer)r;
			
			if (SerializationHelper.getInstance().getMode() == Mode.MODEL) {
				r2.setAtlasName(json.readValue("atlasName", String.class, jsonData));
				r2.setParticleName(json.readValue("particleName", String.class, jsonData));
				r2.setOrgAlign(json.readValue("orgAlign", int.class, Align.bottom, jsonData));
			} else {		
				r2.lastAnimationTime = json.readValue("lastAnimationTime", Float.class, jsonData);
			}
		} else 	if (r instanceof Sprite3DRenderer) {
			Sprite3DRenderer r2 = (Sprite3DRenderer)r;
			
			if (SerializationHelper.getInstance().getMode() == Mode.MODEL) {
				r2.fanims = json.readValue("fanims", HashMap.class, AnimationDesc.class, jsonData);
				
				float worldScale = EngineAssetManager.getInstance().getScale();
				r2.width = (int) (json.readValue("width", Integer.class, jsonData) * worldScale);
				r2.height = (int) (json.readValue("height", Integer.class, jsonData) * worldScale);
				r2.cameraPos = json.readValue("cameraPos", Vector3.class, jsonData);
				r2.cameraRot = json.readValue("cameraRot", Vector3.class, jsonData);
				r2.setCameraName(json.readValue("cameraName", String.class, jsonData));
				r2.setCameraFOV( json.readValue("cameraFOV", Float.class, jsonData));
				r2.renderShadow = json.readValue("renderShadow", Boolean.class, jsonData);
			} else {

				r2.animationCb = ActionCallbackSerialization.find(w, json.readValue("animationCb", String.class, jsonData));

				r2.currentCount = json.readValue("currentCount", Integer.class, jsonData);
				r2.currentAnimationType = json.readValue("currentAnimationType", Tween.Type.class, jsonData);
				r2.lastAnimationTime = json.readValue("lastAnimationTime", Float.class, jsonData);
			}

			r2.modelRotation = json.readValue("modelRotation", Float.class, jsonData);		
		} else 	if (r instanceof TextRenderer) {
			TextRenderer r2 = (TextRenderer)r;
			
			if (SerializationHelper.getInstance().getMode() == Mode.MODEL) {
				r2.setText(json.readValue("text", String.class, jsonData));
				r2.setFontName( json.readValue("fontName", String.class, jsonData));
				r2.setFontSize( json.readValue("fontSize", int.class, jsonData));
				r2.setBorderWidth( json.readValue("borderWidth", int.class, jsonData));
				r2.setBorderColor(json.readValue("borderColor", Color.class, jsonData));
				r2.setBorderStraight( json.readValue("borderStraight", boolean.class, jsonData));
				r2.setShadowOffsetX( json.readValue("shadowOffsetX", int.class, jsonData));
				r2.setShadowOffsetY( json.readValue("shadowOffsetY", int.class, jsonData));
				r2.setShadowColor( json.readValue("shadowColor", Color.class, jsonData));
				r2.setAlign( json.readValue("align", int.class, Align.left, jsonData));
				r2.setOrgAlign( json.readValue("orgAlign", int.class, Align.bottom, jsonData));
			}
		} else 	if (r instanceof Serializable) {
			((Serializable)r).read(json, jsonData);			
		}
	}
}
