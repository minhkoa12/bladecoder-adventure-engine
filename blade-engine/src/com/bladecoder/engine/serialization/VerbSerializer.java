package com.bladecoder.engine.serialization;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.SerializationException;
import com.badlogic.gdx.utils.Json.Serializable;
import com.bladecoder.engine.actions.Action;
import com.bladecoder.engine.model.Verb;
import com.bladecoder.engine.model.World;
import com.bladecoder.engine.serialization.SerializationHelper.Mode;
import com.bladecoder.engine.util.ActionUtils;
import com.bladecoder.engine.util.EngineLogger;

public class VerbSerializer {

	static public void write(Verb v, Json json) {

		if (SerializationHelper.getInstance().getMode() == Mode.MODEL) {
			json.writeValue("id", v.getId());

			if (v.getTarget() != null)
				json.writeValue("target", v.getTarget());

			if (v.getState() != null)
				json.writeValue("state", v.getState());

			if (v.getIcon() != null)
				json.writeValue("icon", v.getIcon());

			json.writeArrayStart("actions");
			for (Action a : v.getActions()) {
				ActionUtils.writeJson(a, json);
			}
			json.writeArrayEnd();
		} else {
			json.writeValue("ip", v.getIP());
			json.writeValue("cb", ActionCallbackSerialization.find(v.getCb()));

			if (v.getCurrentTarget() != null)
				json.writeValue("currentTarget", v.getCurrentTarget());

			json.writeArrayStart("actions");
			for (Action a : v.getActions()) {
				if (a instanceof Serializable) {
					json.writeObjectStart();
					((Serializable) a).write(json);
					json.writeObjectEnd();
				}
			}
			json.writeArrayEnd();
		}
	}

	static public void read(World w, Verb v, Json json, JsonValue jsonData) {

		if (SerializationHelper.getInstance().getMode() == Mode.MODEL) {
			v.setId( json.readValue("id", String.class, jsonData) );
			v.setTarget( json.readValue("target", String.class, (String) null, jsonData));
			v.setState(json.readValue("state", String.class, (String) null, jsonData));
			v.setIcon(json.readValue("icon", String.class, (String) null, jsonData));

			v.getActions().clear();
			JsonValue actionsValue = jsonData.get("actions");
			for (int i = 0; i < actionsValue.size; i++) {
				JsonValue aValue = actionsValue.get(i);
				String clazz = aValue.getString("class");

				try {
					Action a = ActionUtils.readJson(w, json, aValue);
					v.getActions().add(a);
				} catch (SerializationException e) {
					EngineLogger.error("Error loading action: " + clazz + " " + aValue.toString());
					throw e;
				}
			}
		} else {
			// MUTABLE
			v.setCurrentTarget(json.readValue("currentTarget", String.class, (String) null, jsonData));
			v.setIP( json.readValue("ip", Integer.class, jsonData));
			String sCb = json.readValue("cb", String.class, jsonData);
			v.setCb(ActionCallbackSerialization.find(sCb));

			JsonValue actionsValue = jsonData.get("actions");

			int i = 0;

			for (Action a : v.getActions()) {
				if (a instanceof Serializable && i < actionsValue.size) {
					if (actionsValue.get(i) == null)
						break;

					((Serializable) a).read(json, actionsValue.get(i));
					i++;
				}
			}
		}
	}
}
