package com.prtec.tasks.application.utils;

import java.util.Arrays;
import java.util.List;

/**
 * Clase para facilitar operaciones relacionadas con archivo de configuracion
 * Yaml
 */
public class YamlUtil {

	private YamlUtil() {
	}

	/**
	 * Recibe una cadena multilínea (como las definidas con '>' en YAML)
	 * y devuelve un arreglo limpio, separado por comas, sin espacios ni saltos de
	 * línea.
	 * 
	 * @param original
	 * @return
	 */
	public static String[] cleanMultilineProperty(List<String> original) {
		if (original == null || original.isEmpty()) {
			return new String[0];
		}

		return original.stream()
				.map(String::trim)
				.toArray(String[]::new);
	}

	/**
	 * Sobrecarga del método cleanMultilineProperty para arreglos de String
	 * 
	 * @param original
	 * @return
	 */
	public static String[] cleanMultilineProperty(String[] original) {
		return cleanMultilineProperty(original != null ? Arrays.asList(original) : List.of());
	}
}
