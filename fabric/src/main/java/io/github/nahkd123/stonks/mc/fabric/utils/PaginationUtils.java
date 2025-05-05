package io.github.nahkd123.stonks.mc.fabric.utils;

import java.util.ArrayList;
import java.util.List;

public class PaginationUtils {
	public static int getPageCount(int perPage, int count) {
		return count / perPage + ((perPage % count) > 0 ? 1 : 0);
	}

	public static <T> List<T> paginate(int page, int perPage, List<T> all) {
		int from = page * perPage;
		int to = (page + 1) * perPage;
		List<T> collected = new ArrayList<>();
		for (int i = from; i < to; i++) collected.add(i < all.size() ? all.get(i) : null);
		return collected;
	}
}
