package tintor.sokoban2.cell;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tintor.sokoban2.Key;
import tintor.sokoban2.common.Code;
import tintor.sokoban2.common.Dir;
import tintor.sokoban2.common.Grid;
import tintor.sokoban2.common.StringGrid;

public class Loader {
	public static Key load(final String url) {
		try {
			String file, name = null;
			int id = 1;

			final Matcher m = Pattern.compile("(.+)([#:])(.+)").matcher(url);
			if (m.matches()) {
				name = m.group(2).equals("#") ? m.group(3) : null;
				id = m.group(2).equals(":") ? Integer.parseInt(m.group(3)) : 0;
				file = new File(m.group(1)).exists() ? m.group(1) : m.group(1) + ".soko";
			} else
				file = url;
			return load(new FileReader(file), name, id);
		} catch (final FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private static class Level {
		int id;
		String name;
		final List<String> rows = new ArrayList<String>();

		final BufferedReader in;

		Level(final BufferedReader in) {
			this.in = in;
		}

		boolean read() {
			++id;
			name = "";
			rows.clear();
			try {
				while (true) {
					String line = in.readLine();
					if (line == null) return false;
					if (levelRow(line)) {
						rows.add(line);
						break;
					}
					line = line.trim();
					if (!line.equals("") && name == "") name = line;
				}

				while (true) {
					final String line = in.readLine();
					if (line == null || line.trim().equals("")) break;
					rows.add(line);
				}
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
			return true;
		}

		private static boolean levelRow(final String line) {
			int wall = 0;
			for (int i = 0; i < line.length(); i++) {
				final char c = line.charAt(i);
				if (c == Code.Wall) wall++;
				if (c != Code.Space && c != Code.Wall) return false;
			}
			return wall > 0 && line.length() >= 3;
		}

		Key open() {
			return load(new StringGrid(rows.toArray(new String[rows.size()])));
		}
	}

	public static Key load(final Reader reader, final String levelname, final int levelID) {
		try {
			final Level level = new Level(new BufferedReader(reader));
			while (level.read())
				if (level.name.equalsIgnoreCase(levelname) || level.name.equalsIgnoreCase("Level 1" + levelname)
						|| level.id == levelID) return level.open();
			return null;
		} finally {
			try {
				reader.close();
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static Key load(final Grid grid) {
		final Cell[][] matrix = new Cell[grid.width()][grid.height()];
		final List<Cell> boxes = new ArrayList<Cell>();
		Cell agent = null;

		for (int y = 0; y < grid.height(); y++)
			for (int x = 0; x < grid.width(); x++) {
				if (grid.wall(x, y)) continue;

				final Cell cell = new Cell(x, y, grid.goal(x, y), grid.hole(x, y));

				// attach cell
				if (x > 0) cell.attach(Dir.West, matrix[x - 1][y]);
				if (y > 0) cell.attach(Dir.North, matrix[x][y - 1]);

				// check if special
				if (grid.box(x, y)) boxes.add(cell);
				if (grid.agent(x, y)) agent = cell;

				matrix[x][y] = cell;
			}

		if (agent == null) throw new RuntimeException("Missing agent!");
		return new Key(agent, boxes.toArray(new Cell[boxes.size()]));
	}
}