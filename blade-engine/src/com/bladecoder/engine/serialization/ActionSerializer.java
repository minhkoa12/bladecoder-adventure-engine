package com.bladecoder.engine.serialization;

import java.lang.reflect.Field;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Json.Serializable;
import com.badlogic.gdx.utils.Json.Serializer;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.SerializationException;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.bladecoder.engine.actions.Action;
import com.bladecoder.engine.actions.ActionFactory;
import com.bladecoder.engine.actions.ActionProperty;
import com.bladecoder.engine.actions.ActorAnimationRef;
import com.bladecoder.engine.actions.Param;
import com.bladecoder.engine.actions.SceneActorRef;
import com.bladecoder.engine.model.World;
import com.bladecoder.engine.serialization.SerializationHelper.Mode;
import com.bladecoder.engine.util.ActionUtils;
import com.bladecoder.engine.util.EngineLogger;

public class ActionSerializer implements Serializer<Action> {

	final private World w;
	final private Mode mode;

	private Action action;

	public ActionSerializer(World w, Mode mode) {
		this.w = w;
		this.mode = mode;
	}

	public void setCurrent(Action act) {
		this.action = act;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void write(Json json, Action a, Class knownType) {
		if (mode == Mode.MODEL) {
			Class<?> clazz = a.getClass();
			json.writeObjectStart(clazz, null);
			while (clazz != null && clazz != Object.class) {
				for (Field field : clazz.getDeclaredFields()) {
					final ActionProperty property = field.getAnnotation(ActionProperty.class);
					if (property == null) {
						continue;
					}

					// json.writeField(a, field.getName());
					final boolean accessible = field.isAccessible();
					field.setAccessible(true);

					try {
						Object o = field.get(a);

						// doesn't write null fields
						if (o == null)
							continue;

						if (o instanceof SceneActorRef) {
							SceneActorRef sceneActor = (SceneActorRef) o;
							json.writeValue(field.getName(), sceneActor.toString());
						} else if (o instanceof ActorAnimationRef) {
							ActorAnimationRef aa = (ActorAnimationRef) o;
							json.writeValue(field.getName(), aa.toString());
						} else if (o instanceof Color) {
							json.writeValue(field.getName(), ((Color) o).toString());
						} else if (o instanceof Vector2) {
							json.writeValue(field.getName(), Param.toStringParam((Vector2) o));
						} else {
							json.writeValue(field.getName(), o);
						}
					} catch (IllegalArgumentException | IllegalAccessException e) {

					}

					field.setAccessible(accessible);
				}
				clazz = clazz.getSuperclass();
			}
			json.writeObjectEnd();
		} else {
			if (a instanceof Serializable) {
				json.writeObjectStart();
				((Serializable) a).write(json);
				json.writeObjectEnd();
			}
		}
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public Action read(Json json, JsonValue jsonData, Class type) {
		if (mode == Mode.MODEL) {
			String className = jsonData.getString("class", null);

			if (className != null) {
				jsonData.remove("class");

				try {
					action = ActionFactory.createByClass(className, null);

				} catch (ClassNotFoundException | ReflectionException e1) {
					throw new SerializationException(e1);
				}

				for (int j = 0; j < jsonData.size; j++) {
					JsonValue v = jsonData.get(j);
					try {
						if (v.isNull())
							ActionUtils.setParam(action, v.name, null);
						else
							ActionUtils.setParam(action, v.name, v.asString());
					} catch (NoSuchFieldException e) {
						EngineLogger.error("Action field not found - class: " + className + " field: " + v.name);
					} catch (IllegalArgumentException | IllegalAccessException e) {
						EngineLogger.error("Action field error - class: " + className + " field: " + v.name + " value: "
								+ (v == null ? "null" : v.asString()));
					}
				}

				action.init(w);
			}
		} else {
			if(action instanceof Serializable)
				((Serializable) action).read(json, jsonData);
			else
				return null;
		}

		return action;
	}
}
