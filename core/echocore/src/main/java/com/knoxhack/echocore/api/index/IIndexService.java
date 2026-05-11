package com.knoxhack.echocore.api.index;

public interface IIndexService {
    default boolean available() {
        return true;
    }

    IIndexRegistry registry();

    IIndexRecipeService recipes();

    IIndexSearchService search();

    IIndexDiscoveryService discovery();

    IIndexOverlayService overlay();

    default void refresh() {
        search().invalidate();
    }
}
