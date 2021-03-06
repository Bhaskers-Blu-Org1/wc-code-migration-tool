package com.ibm.commerce.dependency.load;

/*
 *-----------------------------------------------------------------
 * Copyright 2018 Trent Hoeppner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *-----------------------------------------------------------------
 */

import java.io.File;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.ibm.commerce.dependency.task.Task;

/**
 * This class finds a ZipEntry with a specified name.
 * 
 * @author Trent Hoeppner
 */
public class DetectJARBinaryTask extends Task<LoadingContext> {

	/**
	 * The required inputs for this task.
	 */
	private static final Set<String> INPUTS = new HashSet<>(Arrays.asList(Name.FILE));

	/**
	 * The expected outputs for this task.
	 */
	private static final Set<String> OUTPUTS = new HashSet<>(Arrays.asList(Name.ZIP_FILE, Name.IS_BINARY));

	/**
	 * Constructor for this.
	 * 
	 * @param name
	 *            The name of this task. This value cannot be null or empty.
	 * @param context
	 *            The context used for input and output during execution. This
	 *            value cannot be null.
	 */
	public DetectJARBinaryTask(String name, LoadingContext context) {
		super(name, context);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<String> getInputConstraints() {
		return INPUTS;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<String> getOutputConstraints() {
		return OUTPUTS;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute(LoadingContext context) throws Exception {
		File file = context.get(Name.FILE);

		Set<String> entryNames = new HashSet<>();
		ZipFile zipFile = new ZipFile(file);
		Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
		while (enumeration.hasMoreElements()) {
			ZipEntry entry = enumeration.nextElement();
			entryNames.add(entry.getName());
		}

		boolean isBinary = false;
		for (String entryName : entryNames) {
			if (entryName.endsWith(".class") && !entryName.contains("$")) {
				// it's a main class file, not an inner class file
				String javaName = entryName.replace(".class", ".java");
				if (!entryNames.contains(javaName)) {
					// we found a class file without a java file, treat the JAR
					// as binary
					isBinary = true;
					break;
				}
			}

		}

		context.put(Name.ZIP_FILE, zipFile);
		context.put(Name.IS_BINARY, isBinary);
	}

}