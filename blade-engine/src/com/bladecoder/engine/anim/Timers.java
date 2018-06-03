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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.bladecoder.engine.actions.ActionCallback;

public class Timers {
	private final List<Timer> timers = new ArrayList<>(3);
	transient private List<Timer> timersTmp = new ArrayList<>(3);

	public void addTimer(float time, ActionCallback cb) {
		Timer t = new Timer();

		t.time = time;
		t.cb = cb;

		timers.add(t);
	}
	
	public List<Timer> getTimers() {
		return timers;
	}

	public void clear() {
		timers.clear();
	}
	
	public boolean isEmpty() {
		return timers.isEmpty();
	}
	
	public void removeTimerWithCb(ActionCallback cb) {
		final Iterator<Timer> it = timers.iterator();
		
		while (it.hasNext()) {
			final Timer t = it.next();
			if(t.cb == cb) {
				it.remove();
				
				return;
			}
		}
	}

	public void update(float delta) {
		final Iterator<Timer> it = timers.iterator();
		while (it.hasNext()) {
			final Timer t = it.next();

			t.currentTime += delta;

			if (t.currentTime >= t.time) {
				it.remove();

				// we add the timers to call the 'cb' later because the 'cb' can add new timers
				// while iterating.
				timersTmp.add(t);
			}
		}

		if (timersTmp.size() > 0) {
			// process ended timers
			for (Timer t : timersTmp) {
				t.cb.resume();
			}

			timersTmp.clear();
		}
	}

	public static class Timer {
		public float time;
		public float currentTime = 0;
		public ActionCallback cb;
	}
}
