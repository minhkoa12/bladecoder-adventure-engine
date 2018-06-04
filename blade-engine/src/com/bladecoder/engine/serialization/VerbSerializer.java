package com.bladecoder.engine.serialization;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.SerializationException;
import com.bladecoder.engine.actions.Action;
import com.bladecoder.engine.model.Verb;
import com.bladecoder.engine.model.World;
import com.bladecoder.engine.serialization.SerializationHelper.Mode;
import com.bladecoder.engine.util.EngineLogger;

public class VerbSerializer {

	static public void write(World w, Verb v, Json json) {

		if (SerializationHelper.getInstance().getMode() == Mode.MODEL) {
			json.writeValue("id", v.getId());

			if (v.getTarget() != null)
				json.writeValue("target", v.getTarget());

			if (v.getState() != null)
				json.writeValue("state", v.getState());

			if (v.getIcon() != null)
				json.writeValue("icon", v.getIcon());
			
			ActionSerializer as = new ActionSerializer(w, Mode.MODEL);
			json.writeArrayStart("actions");
			for (Action a : v.getActions()) {
				as.write(json, a, null);
			}
			json.writeArrayEnd();
		} else {
			json.writeValue("ip", v.getIP());
			json.writeValue("cb", ActionCallbackSerialization.find(w, v.getCb()));

			if (v.getCurrentTarget() != null)
				json.writeValue("currentTarget", v.getCurrentTarget());

			ActionSerializer as = new ActionSerializer(w, Mode.STATE);
			json.writeArrayStart("actions");
			for (Action a : v.getActions()) {
				as.write(json, a, null);
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
			ActionSerializer as = new ActionSerializer(w, Mode.MODEL);
			JsonValue actionsValue = jsonData.get("actions");
			for (int i = 0; i < actionsValue.size; i++) {
				JsonValue aValue = actionsValue.get(i);
				String clazz = aValue.getString("class");

				try {
					Action a = as.read(json, aValue, null);
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
			v.setCb(ActionCallbackSerialization.find(w, sCb));

			ActionSerializer as = new ActionSerializer(w, Mode.STATE);
			JsonValue actionsValue = jsonData.get("actions");

			int i = 0;

			for (Action a : v.getActions()) {
				if (i < actionsValue.size) {
					if (actionsValue.get(i) == null)
						break;

					as.setCurrent(a);
					if( as.read(json, actionsValue.get(i), null) != null)
						i++;
				}
			}
		}
	}
}
