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
import com.bladecoder.engine.actions.ActionCallback;

public class Text {
	private static final float DEFAULT_TIME = 1f;
	
	public enum Type {
		PLAIN, SUBTITLE, TALK, UI
	};
	
	public String str;
	public float x;
	public float y;
	public float time;
	public Type type;
	public Color color;
	public String style;
	public ActionCallback cb;
	public String actorId;
	public String voiceId;

	public Text() {
	}

	public Text(String str, float x, float y, float time, Type type, Color color, String style, String actorId, String voiceId, ActionCallback cb) {
		this.str = str;
		this.x = x;
		this.y = y;
		this.time = time;
		this.type = type;
		this.color = color;
		this.style = style;
		this.cb = cb;
		this.actorId = actorId;
		this.voiceId = voiceId;

		// 0s -> Auto duration
		// <0 -> Infinity
		
		if(this.time < 0 || voiceId != null) {
			this.time = Float.MAX_VALUE;
		} else if (this.time == 0) {
			setAutoTime();
		}
	}
	
	public void setAutoTime() {
		this.time = DEFAULT_TIME + DEFAULT_TIME * str.length() / 20f;
	}
	
	public void callCb() {
		if(cb != null) {
			ActionCallback tmpcb = cb;
			cb = null;
			tmpcb.resume();
		}
	}
}
