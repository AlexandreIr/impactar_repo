package br.com.impactar.shared;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.ObjIntConsumer;
import java.util.stream.Collectors;

/** Reordenação a partir da lista completa de ids na nova ordem. */
public final class Ordering {

    private Ordering() {
    }

    public static <T> void apply(List<T> items, List<UUID> orderedIds,
                                 Function<T, UUID> idOf, ObjIntConsumer<T> setOrder) {
        Set<UUID> current = items.stream().map(idOf).collect(Collectors.toSet());
        Set<UUID> requested = new HashSet<>(orderedIds);

        if (orderedIds.size() != items.size() || requested.size() != orderedIds.size() || !current.equals(requested)) {
            throw ApiException.badRequest("Lista de ordenação inválida. Recarregue a página e tente novamente.");
        }

        for (T item : items) {
            setOrder.accept(item, orderedIds.indexOf(idOf.apply(item)));
        }
    }
}
