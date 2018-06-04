package com.bladecoder.engineeditor.common;

import java.io.StringWriter;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.bladecoder.engine.actions.Action;
import com.bladecoder.engine.model.World;
import com.bladecoder.engine.serialization.ActionSerializer;
import com.bladecoder.engine.serialization.SerializationHelper;
import com.bladecoder.engine.serialization.SerializationHelper.Mode;

public class ElementUtils {
	public static String getCheckedId(String id, String[] values) {
		boolean checked = false;
		int i = 1;

		String idChecked = id;

		String[] nl = values;

		while (!checked) {
			checked = true;

			for (int j = 0; j < nl.length; j++) {
				String id2 = nl[j];

				if (id2.equals(idChecked)) {
					i++;
					idChecked = id + i;
					checked = false;
					break;
				}
			}
		}

		return idChecked;
	}

	public static Object cloneElement(Object e) {
		Json json = new Json();

		if (e instanceof Action) {
			StringWriter buffer = new StringWriter();
			json.setWriter(buffer);
			ActionSerializer as = new ActionSerializer(World.getInstance(), Mode.MODEL);
			as.write(json, (Action)e, null);
			String serializedAction = buffer.toString();

			JsonValue root = new JsonReader().parse(serializedAction);
			return as.read(json, root, null);
		} else {
			World.getInstance().getSerializer().setSerializers(json, Mode.MODEL);

			SerializationHelper.getInstance().setMode(Mode.MODEL);
			String str = json.toJson(e, (Class<?>) null);
			return json.fromJson(e.getClass(), str);
		}
	}
}
