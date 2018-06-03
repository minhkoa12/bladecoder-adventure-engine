/*******************************************************************************
 * Copyright 2014 Rafael Garcia Moreno.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.bladecoder.engine.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.bladecoder.engine.actions.ActionCallback;
import com.bladecoder.engine.util.RectangleRenderer;

/**
 * A transition is used to fadein/fadeout the screen.
 * 
 * @author rgarcia
 */
public class Transition {
	public static enum Type {
		NONE, FADE_IN, FADE_OUT
	};

	private float time;
	private float currentTime;
	private ActionCallback cb;
	private Color c;
	private Type type = Type.NONE;

	public void update(float delta) {

		if (isFinish()) {

			if (cb != null) {
				ActionCallback tmpcb = cb;
				cb = null;
				tmpcb.resume();
			}

			// reset the transition when finish. Only in 'fade in' case, 'fade out'
			// must stay in screen even when finished
			if (getType() == Type.FADE_IN)
				reset();
		} else {
			currentTime += delta;
		}
	}

	public void reset() {
		setType(Type.NONE);
	}

	public void draw(SpriteBatch batch, float width, float height) {

		if (getType() == Type.NONE)
			return;

		switch (getType()) {
		case FADE_IN:
			c.a = MathUtils.clamp(Interpolation.fade.apply(1 - currentTime / time), 0, 1);
			break;
		case FADE_OUT:
			c.a = MathUtils.clamp(Interpolation.fade.apply(currentTime / time), 0, 1);
			break;
		default:
			break;
		}

		RectangleRenderer.draw(batch, 0, 0, width, height, c);
	}

	public void create(float time, Color color, Type type, ActionCallback cb) {
		this.currentTime = 0f;
		this.c = color.cpy();
		this.setType(type);
		this.time = time;
		this.cb = cb;
	}

	public boolean isFinish() {
		return (currentTime > time || getType() == Type.NONE);
	}

	public float getTime() {
		return time;
	}

	public void setTime(float time) {
		this.time = time;
	}

	public float getCurrentTime() {
		return currentTime;
	}

	public void setCurrentTime(float currentTime) {
		this.currentTime = currentTime;
	}

	public ActionCallback getCb() {
		return cb;
	}

	public void setCb(ActionCallback cb) {
		this.cb = cb;
	}

	public Color getColor() {
		return c;
	}

	public void setColor(Color c) {
		this.c = c;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

}
