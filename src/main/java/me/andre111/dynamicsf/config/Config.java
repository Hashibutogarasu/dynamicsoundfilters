/*
 * Copyright (c) 2023 Andre Schweiger
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
 */
package me.andre111.dynamicsf.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import net.minecraft.util.Identifier;

public class Config {
	private static class IdentifierTypeAdapter extends TypeAdapter<Identifier> {
		@Override
		public void write(JsonWriter out, Identifier value) throws IOException {
			if (value == null) {
				out.nullValue();
			} else {
				out.value(value.toString());
			}
		}

		@Override
		public Identifier read(JsonReader in) throws IOException {
			if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
				in.nextNull();
				return null;
			}
			return Identifier.of(in.nextString());
		}
	}

	private static Gson gson;
	static {
		gson = new GsonBuilder()
				.registerTypeAdapter(Identifier.class, new IdentifierTypeAdapter())
				.create();
	}

	private static ConfigData data = new ConfigData();

	public static ConfigData getData() {
		return data;
	}

	public static void loadData() {
		File file = getConfigFile();
		if (file.exists()) {
			try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
				data = gson.fromJson(reader, ConfigData.class);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void saveData() {
		File file = getConfigFile();
		if (!file.exists()) {
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try (JsonWriter writer = new JsonWriter(new BufferedWriter(new FileWriter(file)))) {
			writer.setIndent("    ");
			gson.toJson(data, ConfigData.class, writer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static File getConfigFile() {
		return new File("./config/dynamicsf/config.json");
	}
}
