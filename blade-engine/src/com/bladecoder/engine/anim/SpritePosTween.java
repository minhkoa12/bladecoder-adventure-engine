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
package com.bladecoder.engine.anim;

import com.bladecoder.engine.actions.ActionCallback;
import com.bladecoder.engine.model.SpriteActor;
import com.bladecoder.engine.util.InterpolationMode;

/**
 * Tween for spriteactor position animation
 * 
 * TODO: Y speed depends on scale if fake depth is used
 */
public class SpritePosTween extends Tween<SpriteActor> {
	
	public float startX, startY;
	public float targetX, targetY;
	public InterpolationMode interpolationX;
	public InterpolationMode interpolationY;
	
	public SpritePosTween() {
	}

	public void start(SpriteActor target, Tween.Type repeatType, int count, float tx, float ty, float duration, InterpolationMode interpolation, ActionCallback cb) {
		start(target, repeatType, count, tx, ty, duration, interpolation, interpolation, cb);
	}
	
	public void start(SpriteActor target, Tween.Type repeatType, int count, float tx, float ty, float duration, 
			InterpolationMode interpolationX, InterpolationMode interpolationY, ActionCallback cb) {
		
		this.target = target;
		startX = target.getX();
		startY = target.getY();
		targetX = tx;
		targetY = ty;
		
		setDuration(duration);
		setType(repeatType);
		setCount(count);
		
		this.interpolationX = interpolationX;
		this.interpolationY = interpolationY;

		if (cb != null) {
			setCb(cb);
		}
		
		restart();
	}	
	
	@Override
	public void updateTarget() {
		
		float percentX = getPercent(interpolationX);
		float percentY = getPercent(interpolationY);
		
		target.setPosition(startX + percentX * (targetX - startX),
				startY + percentY * (targetY - startY));
	}
}
