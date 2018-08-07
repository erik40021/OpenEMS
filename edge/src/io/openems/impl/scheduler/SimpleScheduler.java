/*******************************************************************************
 * OpenEMS - Open Source Energy Management System
 * Copyright (c) 2016, 2017 FENECON GmbH and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *   FENECON GmbH - initial API and implementation and initial documentation
 *******************************************************************************/
package io.openems.impl.scheduler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import info.faljse.SDNotify.SDNotify;
import io.openems.api.channel.thingstate.ThingStateChannels;
import io.openems.api.controller.Controller;
import io.openems.api.doc.ThingInfo;
import io.openems.api.scheduler.Scheduler;

@ThingInfo(title = "App-Planner")
public class SimpleScheduler extends Scheduler {

	private ThingStateChannels thingState;

	/*
	 * Constructors
	 */
	public SimpleScheduler() {
		this.thingState = new ThingStateChannels(this);
	}

	/*
	 * Methods
	 */
	@Override
	protected void dispose() {}

	@Override
	protected void execute() {
		// kick the watchdog
		SDNotify.sendWatchdog();
		List<Controller> controllers = new ArrayList<>(this.controllers.values());
		Collections.sort(controllers, (c1, c2) -> c2.priority.valueOptional().orElse(Integer.MIN_VALUE)
				- c1.priority.valueOptional().orElse(Integer.MIN_VALUE));
		for (Controller controller : controllers) {
			// TODO: check if WritableChannels can still be changed, before executing
			controller.executeRun();
		}
	}

	@Override
	protected boolean initialize() {
		return true;
	}

	@Override
	public ThingStateChannels getStateChannel() {
		return this.thingState;
	}

}